/* 
 * Copyright (C) 2015 DECOIT GmbH
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.decoit.simu.fim.FileFolder;

import de.decoit.simu.fim.Controller;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.sql.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.File;

/**
 *
 * @author Artur Schmidt (schmidt@decoit.de)
 */
public class Folder extends Thread {

    Connection connect = Database.getDBConnection();

    private final HashSet<Path> files = new HashSet<>();
    private Path path;
    private PreparedStatement pstmt = null;
    private ResultSet rs = null;
    private final Controller.Importance importance;

    /**
     * Berechnet initial einen Hashwert über alle Dateien innerhalb des
     * überwachten Ordners und speichert diesen in der Datenbank. Ist der
     * Eintrag vorhanden, so wird nur der Hashwert und das Datum geupdated,
     * ansonsten ein neuer Eintrag angelegt
     *
     * @param path
     * @param importance
     */
    public Folder(Path path, Controller.Importance importance) {
        this.path = path;
        this.importance = importance;
        fileList(path.toString());
        for (Path p : files) {
            try {
                // Hashwert berechnen und überprüfen ob Datei bereits 
                // in Datenbank ist
                pstmt = connect.prepareStatement("SELECT * FROM hashwerte WHERE File LIKE '" + p + "';");
                rs = pstmt.executeQuery();
                if (Files.isRegularFile(p) == true) {     
                    if (!rs.next()) {
                        insertInto(p, importance);
                    } else {
                        update(p, importance);
                    }
                }
            } catch (Exception ex) {
                Logger.getLogger(Folder.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                rs.close();
                pstmt.close();
            } catch (SQLException ex) {
                Logger.getLogger(Folder.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     *  
     */
    @Override
    public void run() {
        try {
            createWatcher(path);
        } catch (Exception ex) {
            Logger.getLogger(Folder.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Creates the Watcher and monitors the Folder in @param path
     * @param path
     * @throws Exception
     */
    private void createWatcher(Path path) throws SQLException {
        WatchService watcher = null;
        String kind = "laeuft";
        try {
            watcher = FileSystems.getDefault().newWatchService();
        } catch (IOException ex) {
            Logger.getLogger(Folder.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            path.register(watcher,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_DELETE,
                    StandardWatchEventKinds.ENTRY_MODIFY);
        } catch (IOException ex) {
            Logger.getLogger(Folder.class.getName()).log(Level.SEVERE, null, ex);
        }
        WatchKey key = null;
        do {
            do {
                try {
                    key = watcher.poll(1, TimeUnit.SECONDS);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Folder.class.getName()).log(Level.SEVERE, null, ex);
                }
            } while (key == null);
            List<WatchEvent<?>> events = key.pollEvents();
            if (events.size() > 0) {
                for (WatchEvent<?> e : events) {
                    System.out.println(e.kind() + ": " + e.context());
                    if (!regex(e.context() + "")) {
                        try {
                            pstmt = connect.prepareStatement("SELECT * FROM hashwerte WHERE File LIKE '" + path + File.separator + e.context() + "';");
                        } catch (SQLException ex) {
                            Logger.getLogger(Folder.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        kind = e.kind() + "";
                        rs = pstmt.executeQuery();
                        if (!rs.next()) {
                            switch (kind) {
                                case "ENTRY_CREATE":
                                    kind = "new";
                                    break;
                                case ("ENTRY_DELETE"):
                                    kind = "deleted";
                                    break;
                            }
                            Controller.getController().send(kind, path + File.separator + e.context(), importance + "");
                        } else {
                            if (kind.equals("ENTRY_DELETE")) {
                                kind = "deleted";
                                Controller.getController().send(kind, path + File.separator + e.context(), importance + "");
                            } else {
                                Controller.getController().send("modified", path + File.separator + e.context(), importance + "");
                            }
                        }
                    }
                }
            }
        } while (key.reset());
        //System.err.println("Folder ist beendet.");
    }


    private void fileList(String directory) {
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(directory))) {
            for (Path p : directoryStream) {
                files.add(p);
            }
        } catch (IOException ex) {
        }
    }

    private void insertInto(Path p, Controller.Importance i) throws Exception {
        PreparedStatement _pstmt = connect.prepareStatement("INSERT INTO hashwerte VALUES('" + p
                + "', '" + Hashfunction.getSHA1Checksum(p.toString())
                + "', Date('now'), Time('now'), '" + importance + "')");
        _pstmt.executeUpdate();
        _pstmt.close();
    }

    private void update(Path p, Controller.Importance i) throws Exception {
        PreparedStatement _pstmt = connect.prepareStatement("UPDATE hashwerte SET Hashwert = '"
                + Hashfunction.getSHA1Checksum(p.toString())
                + "', Datum = Date('now'), Uhrzeit = Time('now'), Importance = '" 
                + i + "'  WHERE File = '" + p + "';");
        _pstmt.executeUpdate();
        _pstmt.close();
    }


    /**
     *
     * @param s
     * @return
     */
    public boolean regex(String s) {
        Matcher editor = Pattern.compile("[.*](goutputstream-).").matcher(s);
        Matcher vi = Pattern.compile(".[.]sw.").matcher(s);
        Matcher libreoffice = Pattern.compile(".~lock+").matcher(s);
        Matcher vim = Pattern.compile("sh-thd-[1-9]+").matcher(s);

        if (editor.find()) {
            return true;
        } else {
            if (vi.find()) {
                return true;
            } else {
                if (vim.find()) {
                    return true;
                } else {
                    if (libreoffice.find()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}

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
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Artur Schmidt (schmidt@decoit.de)
 */
public class File extends Thread {

    Connection connect = Database.getDBConnection();
    private String hash;
    private static boolean stop;
    private ResultSet rs;
    private PreparedStatement pstmt;
    private HashMap<Path,String> map = new HashMap<Path,String>();
    private final Controller.Importance importance;
    private String tempImportance;
    private ArrayList<Path> pathsToRemove = new ArrayList<>();
    
    /**
     *  Constructor
     * @param paths
     */
    public File(HashMap<Path, Controller.Importance> paths) {
        importance = null;
        stop = false;
        for (Path p : paths.keySet()) {
            try {
                //Überprüfen ob Datei bereits in der Datenbank hinterlegt ist
                pstmt = connect.prepareStatement("SELECT * FROM hashwerte WHERE File LIKE '" + p + "';");
                rs = pstmt.executeQuery();
                hash = Hashfunction.getSHA1Checksum(p.toString());
                map.put(p,hash);
                if (Files.isRegularFile(p) == true) {
                    //Controller.setFiles(p);
                    if (!rs.next()) {
                        insertInto(p, paths.get(p));
                        //Controller.getController().send("existing", p.toString(), paths.get(p).toString());
                        //Wenn der Dateipfad nicht vorhanden ist
                    } else {
                        //Controller.getController().send("existing", p.toString(), paths.get(p).toString());
                        update(p, paths.get(p));
                        //Update des Hashwerts, wenn Datei bereits vorhanden
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
     *  run-Method 
     */
    @Override
    public void run() {
        while (!stop) {
            for (Path p : map.keySet()) {
                try {
                    /* Hashwert berechnen und mit dem aus der Datenbank 
                     * vergleichen. 
                     * Eventuell weitere Variable anlegen um den geänderten 
                     * Hashwert zwischen zu speichern, damit es nicht zu 
                     * dauernden Warnmeldungen kommt. 
                     */
                    //System.out.println(map);
                    if(!p.toFile().exists()){
                        //System.err.println("EINTRAG WURDE GELÖSCHT");
                        Controller.getController().send("deleted", p +"", map.get(p));
                        pathsToRemove.add(p);
                        continue;
                    }
                    hash = Hashfunction.getSHA1Checksum(p.toString());
                    if (!hash.equals(map.get(p))) {/*
                            pstmt = connect.prepareStatement("SELECT Importance FROM hashwerte WHERE File LIKE '" + p + "';");
                            rs = pstmt.executeQuery();
                            tempImportance = rs.getString("Importance");
                            rs.close();
                            pstmt.close();*/
                            
                            //System.err.println("ES HAT SICH WAS GEÄNDERT");
                            Controller.getController().send("modified", p +"", map.get(p));
                            map.put(p, hash);
                        }
                } catch (Exception ex) {
                    Logger.getLogger(File.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            for(Path p : pathsToRemove){
                map.remove(p);
            }
            pathsToRemove.clear();
            try {
                TimeUnit.SECONDS.sleep(30);
                // Zeit die der Thread zwischen den Berechnungen warten soll
            } catch (InterruptedException ex) {
                Logger.getLogger(File.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void insertInto(Path p, Controller.Importance i) throws Exception {
        PreparedStatement _pstmt = connect.prepareStatement("INSERT INTO hashwerte VALUES('" + p
                + "', '" + Hashfunction.getSHA1Checksum(p.toString())
                + "', Date('now'), Time('now'), '" + i + "')");
        _pstmt.executeUpdate();
        _pstmt.close();
    }

    private void update(Path p, Controller.Importance i) throws Exception {
        PreparedStatement _pstmt = connect.prepareStatement("UPDATE hashwerte SET Hashwert = '"
                + Hashfunction.getSHA1Checksum(p.toString())
                + "', Datum = Date('now'), Uhrzeit = Time('now'), Importance = '" 
                + i + "' WHERE File = '" + p + "';");
        _pstmt.executeUpdate();
        _pstmt.close();
    }
    
    /**
     *  Just used when GUI is acitvated
     * @param b
     */
    public static void setStop(boolean b) {
        stop = b;
    }
}

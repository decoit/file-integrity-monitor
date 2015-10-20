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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Artur Schmidt (schmidt@decoit.de)
 */
public class Database {

    
    private static final Path databasePath = Paths.get(System.getProperty("user.home")
                        + File.separator + "Hashwerte.sqlite");
    private static Connection con;
    private final static String makeDatabase
            = "CREATE TABLE IF NOT EXISTS Hashwerte("
            + "File VARCHAR(255) NOT NULL PRIMARY KEY,"
            + "Hashwert VARCHAR(100),"
            + "Datum DATE,"
            + "Uhrzeit TIME,"
            + "Importance VARCHAR(20),"
            + "CHECK((Datum IS NOT NULL AND Hashwert IS NOT NULL) or (Datum IS NULL AND Hashwert IS NULL) ));";

    /**
     * Die Methode getDBConnection stellt die Verbindung zur Datenbank her.
     *
     * @return
     */
    public static Connection getDBConnection() {
        if (con == null) {
            if(!Files.exists(databasePath)){
                try {
                    Runtime.getRuntime().exec("touch Hashwerte.sqlite");
                } catch (IOException ex) {
                    Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            try {
                Class.forName("org.sqlite.JDBC");
                con = DriverManager.getConnection("jdbc:sqlite:"
                        + System.getProperty("user.home")
                        + File.separator + "Hashwerte.sqlite");
                Statement stmt = con.createStatement();
                stmt.executeUpdate(makeDatabase);
            } catch (ClassNotFoundException | SQLException e) {
                System.err.println(e.getClass().getName() + ": " + e.getMessage() + "getDBConnection");
                System.exit(0);
            }
        }
        return con;
    }

    /**
     * Die Methode getCloseDBConnection schließt die Verbindung zur Datenbank.
     *
     */
    public void getCloseDBConnection() {
        try {
            con.close();
        } catch (SQLException e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        System.out.println("Closed database successfully");
    }

    /**
     * Select-Befehl um den Wert in der Spalte "Importance" zu erhalten
     * @param p
     * @return
     */
    synchronized public static String getImportance(Path p){
        ResultSet rs = null;
        PreparedStatement pstmt;
        String tempImportance = ""; 
        try {
            pstmt = getDBConnection().prepareStatement("SELECT Importance FROM hashwerte WHERE File LIKE '" + p + "';");
            rs = pstmt.executeQuery();
            while(rs.next())
                tempImportance = rs.getString("importance");
            rs.close();
            pstmt.close();
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
        return tempImportance;
    }
    
}

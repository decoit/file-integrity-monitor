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
package de.decoit.simu.fim.IFmap;

import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author Artur Schmidt (schmidt@decoit.de)
 */
public class ReadConfig {
   
    String config = "/config/Config.properties";
    Properties prop = new Properties();
    
    
    public String icingaIP(){
        try {
            prop.load(ReadConfig.class.getResourceAsStream(config));
        } catch (IOException ex) {
            Logger.getLogger("Ich kann nicht mehr||" + ReadConfig.class.getName()).log(Level.SEVERE, null, ex);
        }
    return prop.getProperty("IP-Address");
    }
    
    public String useIcingaOrIfmap(){
        try {
            prop.load(ReadConfig.class.getResourceAsStream(config));
        } catch (IOException ex) {
            Logger.getLogger("ICINGA OR IFMAP...WAS?!||" + ReadConfig.class.getName()).log(Level.SEVERE, null, ex);
        }
    return prop.getProperty("icinga.or.ifmap");
    }
    
    public String mapServerUrl(){
        try {
            prop.load(ReadConfig.class.getResourceAsStream(config));
        } catch (IOException ex) {
            Logger .getLogger(ReadConfig.class.getName()).log(Level.SEVERE, null, ex);
        }
    return prop.getProperty("mapserver.url");
    }
    
    public String mapServerbasicAuthUser(){
          try {
            prop.load(ReadConfig.class.getResourceAsStream(config));
        } catch (IOException ex) {
            Logger.getLogger(ReadConfig.class.getName()).log(Level.SEVERE, null, ex);
        }
    return prop.getProperty("mapserver.basicauth.user");
    }
    
    public String mapServerbasicAuthPassword(){
            try {
            prop.load(ReadConfig.class.getResourceAsStream(config));
        } catch (IOException ex) {
            Logger.getLogger(ReadConfig.class.getName()).log(Level.SEVERE, null, ex);
        }
        return prop.getProperty("mapserver.basicauth.password");
    }
   
    public String mapServerKeystorePath(){
        try {
            prop.load(ReadConfig.class.getResourceAsStream(config));
        } catch (IOException ex) {
            Logger.getLogger(ReadConfig.class.getName()).log(Level.SEVERE, null, ex);
        }
        return prop.getProperty("mapserver.keystore.path");
    }
    
    public String mapServerKeystorePassword(){
        try {
            prop.load(ReadConfig.class.getResourceAsStream(config));
        } catch (IOException ex) {
            Logger.getLogger(ReadConfig.class.getName()).log(Level.SEVERE, null, ex);
        }
        return prop.getProperty("mapserver.keystore.password");
    }
    
    public boolean mapServerBasicAuthEnabled(){
         try {
            prop.load(ReadConfig.class.getResourceAsStream(config));
        } catch (IOException ex) {
            Logger.getLogger(ReadConfig.class.getName()).log(Level.SEVERE, null, ex);
        }
        return Boolean.parseBoolean(prop.getProperty("mapserver.basicauth.enabled"));
    }
}

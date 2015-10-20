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

import de.hshannover.f4.trust.ifmapj.IfmapJ;
import de.hshannover.f4.trust.ifmapj.channel.SSRC;
import de.hshannover.f4.trust.ifmapj.config.BasicAuthConfig;
import de.hshannover.f4.trust.ifmapj.config.CertAuthConfig;
import de.hshannover.f4.trust.ifmapj.exception.IfmapErrorResult;
import de.hshannover.f4.trust.ifmapj.exception.IfmapException;
import de.hshannover.f4.trust.ifmapj.identifier.Identifier;
import de.hshannover.f4.trust.ifmapj.identifier.IpAddress;
import de.hshannover.f4.trust.ifmapj.exception.InitializationException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;

import de.decoit.simumetadata.*;
/**
 *
 * @author Artur Schmidt (schmidt@decoit.de)
 */
public class SetUpConnection {
    
    private static SSRC mSSRC = null;
    private static final String ipAddress = "10.240.1.127";
    private static Identifier device;
    private static IpAddress deviceIP;
    private static SimuMetadataFactoryImpl smfi;
    private static ReadConfig cfg;
    
    /**
     * establish a connection with the mapserver
     * @return
     */
    public static SSRC connection(){
        try {
            smfi = new SimuMetadataFactoryImpl();
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(SetUpConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
        cfg = new ReadConfig();
        try {
            if(cfg.mapServerBasicAuthEnabled() && mSSRC == null) {
                mSSRC = IfmapJ.createSsrc(new BasicAuthConfig(
                        cfg.mapServerUrl(),
                        cfg.mapServerbasicAuthUser(),
                        cfg.mapServerbasicAuthPassword(),
                        cfg.mapServerKeystorePath(),
                        cfg.mapServerKeystorePassword()));

            } else if(mSSRC == null){
                mSSRC = IfmapJ.createSsrc(new CertAuthConfig(
                        cfg.mapServerUrl(),
                        cfg.mapServerKeystorePath(),
                        cfg.mapServerKeystorePassword(),
                        cfg.mapServerKeystorePath(), 
                        cfg.mapServerKeystorePassword()));
            }
        } catch (InitializationException ex) {
            Logger.getLogger(SetUpConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
        return mSSRC;
    }
  
    /**
     *  close the connection with the mapserver
     * @param mSSRC
     */
    public static void closeConnection(SSRC mSSRC){
        try {
            mSSRC.endSession();
        } catch (IfmapErrorResult | IfmapException ex) {
            Logger.getLogger(SetUpConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}


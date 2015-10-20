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
package de.decoit.simu.fim;

import de.hshannover.f4.trust.ifmapj.IfmapJ;
import de.hshannover.f4.trust.ifmapj.channel.SSRC;
import de.hshannover.f4.trust.ifmapj.exception.IfmapErrorResult;
import de.hshannover.f4.trust.ifmapj.exception.IfmapException;
import de.hshannover.f4.trust.ifmapj.identifier.Identifier;
import de.hshannover.f4.trust.ifmapj.identifier.Identifiers;
import de.hshannover.f4.trust.ifmapj.messages.MetadataLifetime;
import de.hshannover.f4.trust.ifmapj.messages.PublishRequest;
import de.hshannover.f4.trust.ifmapj.messages.PublishUpdate;
import de.hshannover.f4.trust.ifmapj.messages.Requests;
import de.decoit.simu.fim.IFmap.SetUpConnection;
import de.hshannover.f4.trust.ifmapj.exception.MarshalException;
import de.hshannover.f4.trust.ifmapj.messages.PublishDelete;
import de.hshannover.f4.trust.ifmapj.metadata.StandardIfmapMetadataFactory;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;

import de.decoit.simumetadata.*;
import java.util.HashMap;

/**
 *
 * @author Artur Schmidt (schmidt@decoit.de)
 */
public class TransmitterIFmap extends Thread{

    private SSRC mSSRC = null;
    private Identifier device;
    private Identifier deviceIP;
    private SimuMetadataFactoryImpl smfi;
    private String[] string = new String[3];
    private final StandardIfmapMetadataFactory mf = IfmapJ
			.createStandardMetadataFactory();     
    //private IpAddress deviceIP;
    private HashMap<Path,Controller.Importance> file = new HashMap<>();
    
    /**
     * run-Method of this Thread
     */
    @Override
    public void run(){
        try {
            smfi = new SimuMetadataFactoryImpl();
            mSSRC = SetUpConnection.connection(); 
            try {
                mSSRC.newSession();
                mSSRC.purgePublisher();
            } catch (IfmapErrorResult | IfmapException ex) {
                Logger.getLogger(TransmitterIFmap.class.getName()).log(Level.SEVERE, null, ex);
            }
            file = Controller.getFiles();
            publishDeviceAndIP();            
            
            publishFiles(file);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(TransmitterIFmap.class.getName()).log(Level.SEVERE, null, ex);
        }
        while(true){
            try {
                string = Queue.getElement();
                if(string == null)
                    continue;
                send();} 
            catch (InterruptedException ex) {
                System.out.println("Fehler beim einlesen des Wertes");
            } 
        }
    }    
       
    /**
     *  First publish! Publish the Device and the IP-Adress
     */
    public void publishDeviceAndIP(){
        try {
            PublishRequest pr = Requests.createPublishReq();
            PublishUpdate pu = Requests.createPublishUpdate();
            device = Identifiers.createDev(mSSRC.getPublisherId());
            deviceIP = Identifiers.createIp4(Controller.getIP());
            pu.setLifeTime(MetadataLifetime.session);
            pu.setIdentifier1(device);
            pu.setIdentifier2(deviceIP);
            pu.addMetadata(mf.createDevIp());
            pr.addPublishElement(pu);
            mSSRC.publish(pr);
           
        } catch (IfmapErrorResult e){
            System.out.println("Invalid IP-Adress format (" + Controller.getIP() + ")");
        }
        catch(IfmapException ex) {
            Logger.getLogger(TransmitterIFmap.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     *
     * @param path
     */
    public void publishFiles(/*HashSet<Path> path*/ HashMap<Path, Controller.Importance> path){
        for(Path p : path.keySet()){
            try {
                PublishRequest pr = Requests.createPublishReq();
                PublishUpdate pu = Requests.createPublishUpdate();
                PublishUpdate pu2 = Requests.createPublishUpdate();
                pu.addMetadata(smfi.createFileMonitored());
                pu.setIdentifier2(smfi.createFileIdentifier(p.toString(), mSSRC.getPublisherId()));
                pu.setIdentifier1(device);
                pr.addPublishElement(pu);
                pu2.setIdentifier1(null);
                pu2.setIdentifier2(smfi.createFileIdentifier(p.toString(), mSSRC.getPublisherId())); 
                pu2.addMetadata(smfi.createFileChanged("existing", Controller.getTime(),""+path.get(p)));
                pr.addPublishElement(pu2);
                mSSRC.publish(pr);
                
            } catch (IfmapErrorResult | IfmapException ex) {
                Logger.getLogger(TransmitterIFmap.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    /**
     *  publish the modification
     */
    public void send(){
        
        //System.out.println("SEND BETRETEN!");
        //string [0] = time - yyyy-mm-dd hh-mm-ss
        //string [1] = kind
        //string [2] = path
        //string [3] = file
        try {
            PublishRequest pr = Requests.createPublishReq();
            PublishUpdate pu = Requests.createPublishUpdate();
            PublishUpdate pu2 = Requests.createPublishUpdate();
            
            switch(string[1]){
                case("new"):
                    //System.out.println("ES WURDE EINE NEUE DATEI ANGELEGT!");
                    pu.setIdentifier2(smfi.createFileIdentifier(string[2], mSSRC.getPublisherId()));
                    pu.setIdentifier1(device);
                    pu.addMetadata(smfi.createFileMonitored());
                    pr.addPublishElement(pu);
                    //mSSRC.publish(pr);
                    pu2.setIdentifier1(null);
                    pu2.setIdentifier2(smfi.createFileIdentifier(string[2], mSSRC.getPublisherId()));
                    pu2.addMetadata(smfi.createFileChanged("new", string[0], string[3]));
                    pr.addPublishElement(pu2);
                    mSSRC.publish(pr);
                    break;
                case("deleted"):
                    //System.out.println("ES WURDE EINE DATEI GELÖSCHT!");
                    Identifier fileIdentifier = smfi.createFileIdentifier(string[2], mSSRC.getPublisherId());
                    
                    PublishDelete del = Requests.createPublishDelete
                                (fileIdentifier, device);
                    
                    
                    /*PublishDelete del = Requests.createPublishDelete
                                (deviceIP, device);*/
                    del.addNamespaceDeclaration(SimuMetaDataFactory.SIMU_METADATA_PREFIX,
						SimuMetaDataFactory.SIMU_METADATA_URI);
                        del.setFilter("simu:file-monitored");
                    //PublishRequest prdel = Requests.createPublishReq(del);
                    pr.addPublishElement(del);
                    //mSSRC.publish(prdel);
                    pu.setIdentifier1(fileIdentifier);
                    pu.addMetadata(smfi.createFileChanged("deleted", string[0], string[3]));
                    pr.addPublishElement(pu);
                    mSSRC.publish(pr);
                    break;
                case("modified"):
                    pu.setIdentifier2(smfi.createFileIdentifier(string[2], mSSRC.getPublisherId()));
                    pu.setIdentifier1(null);
                    pu.addMetadata(smfi.createFileChanged("modified", string[0], string[3]));
                    pr.addPublishElement(pu);
                    mSSRC.publish(pr);
                    break;
            }
        } catch (MarshalException ex) {
            Logger.getLogger(TransmitterIFmap.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IfmapErrorResult | IfmapException ex) {
            Logger.getLogger(TransmitterIFmap.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
 
    /**
     *  Not used! 
    
    @Override
    public void converter() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    } */
}
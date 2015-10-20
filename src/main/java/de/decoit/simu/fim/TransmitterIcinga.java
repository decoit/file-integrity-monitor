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

import de.decoit.simu.fim.IFmap.ReadConfig;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Artur Schmidt (schmidt@decoit.de)
 */
public class TransmitterIcinga extends Thread implements ITransmitter {

    private String alert = "";
    private String s;
    private String[] string = new String[3];
    private static boolean stop = false;
    private String IP ="";
    
    
    
    public TransmitterIcinga(){
        ReadConfig rc = new ReadConfig();
        IP = rc.icingaIP();
    }
            
    
    /**
     *
     */
    @Override
    public void run(){      
       while(!stop){
           send();
       } 
    }
    
    public static void setStop(boolean b){
        stop = b;
    }
    
    @Override
    public void send() {
        converter();
            s = "FIM='"+alert+"'&& /bin/echo -e \"iMonitor-Sensors+File Integrity Monitor+0+$FIM\""
                + " | send_nsca -b -H "+ IP +" -d + -c /etc/send_nsca.cfg 1>/dev/null &";
        try {
            Runtime run = Runtime.getRuntime();
            Process proc = run.exec(new String[]{"/bin/bash", "-c", s});        
        } catch (IOException ex) {
            Logger.getLogger(TransmitterIcinga.class.getName()).log(Level.SEVERE, null, ex);
        }
      //  System.out.println("Befehl: " + s);
    }

    @Override
    public void converter() {
        try {
            string = Queue.getElement();
        } catch (InterruptedException ex) {
            Logger.getLogger(TransmitterIcinga.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        //string [0] = time - yyyy-mm-dd hh-mm-s[s]
        //string [1] = kind
        //string [2] = path
        //string [3] = file
        alert = ""
                + "{"
                + "\"type\": \"FIM\","
                + "\"timestamp\": \""+ string[0] +"\","
                + "\"status\": \"WARNING\","
                + "\"ipsrc\": \"\","
                + "\"ipdest\": \"\","
                + "\"protocol\": \"\","
                + "\"message\": \""+ string[1] +": "+ string[2] +"\","
                + "\"class\": \"\","
                +"\"data\":"
                        +"{\"filename\":\""+string[2]+"\","
                        +"\"operation\":\""+string[1]+"\""
                        +"}"
                + "}";
    }
}

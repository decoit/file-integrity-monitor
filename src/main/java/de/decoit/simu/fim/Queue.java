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

import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Artur Schmidt (schmidt@decoit.de)
 */
public class Queue{
    
    private static final LinkedBlockingQueue<String[]> QUEUE = new LinkedBlockingQueue<>();
    private static Queue queue;
    
    /**
     *  
     * @return instace of the Class Queue
     */
    public static Queue getQueue(){
        if(queue==null)
            queue = new Queue();
        return queue;
    }
    
    /**
     *  
     * @return first element in the Queue
     * @throws InterruptedException
     */
    public static String[] getElement() throws InterruptedException {
        System.out.println("WARTE AUF ELEMENT");
        return QUEUE.take();
    }
    
    /**
     * put an String Array into the Queue
     * @param message
     */
    public static void fillQueue(final String[] message){
        
        System.out.println("Element eingefügt");
        try {
            QUEUE.put(message);
        } catch (InterruptedException ex) {
            Logger.getLogger(Queue.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
   
}

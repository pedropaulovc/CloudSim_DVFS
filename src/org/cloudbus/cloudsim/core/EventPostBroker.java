/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.core;

/**
 *
 * EventPost Broker class.
 * 
 * This class define an event that will be started at the end of a broker.
 * 
 * The user can define in his simulation file if a broker has a PostEvent or not.
 * 
 * @param dest The unique id number of the destination entity
 * @param tag An user-defined number representing the type of event.
 * 
 * A EventPostBroker is schedule directly at the end of its broker.
 * 
 * 
 * @author Guérout Tom, Monteil Thierry, Da Costa Georges  |  LAAS - IRIT , Toulouse, France
 */
public class EventPostBroker {

    int dest;
    int tag;
    

   public EventPostBroker(int dest_, int tag_)
   {
      dest=dest_;
      tag=tag_;
   
   }
   
   
   
    public int getDest() {
        return dest;
    }

    public void setDest(int dest) {
        this.dest = dest;
    }

    public int getTag() {
        return tag;
    }

    public void setTag(int tag) {
        this.tag = tag;
    }
   
    
}

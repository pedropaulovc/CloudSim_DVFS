/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cloudbus.cloudsim.workflow;

import java.util.ArrayList;

/**
 *
 * @author tguerout
 */
public class OneCriticalPath {
    
    private ArrayList<Task> tasks ;
    private double time;

   
    
    public OneCriticalPath(ArrayList<Task> tasks_, double time_)
    {
        tasks = tasks_;
        time = time_ ;
    }
    
    
     public ArrayList<Task> getTasks() {
        return tasks;
    }

    public double getTime() {
        return time;
    }
}

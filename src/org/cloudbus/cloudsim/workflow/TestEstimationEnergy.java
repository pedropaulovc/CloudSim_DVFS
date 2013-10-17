/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cloudbus.cloudsim.workflow;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.power.models.PowerModelSpecPower_REIMS;

/**
 *
 * @author tguerout
 */
public class TestEstimationEnergy {
    
    
    PowerModelSpecPower_REIMS model = new PowerModelSpecPower_REIMS();
    boolean pooling;

    
    public TestEstimationEnergy()
    {
        pooling = Boolean.parseBoolean(Properties.POOLING.getProperty());
    }
    
    public double getTaskEnergyConsumed(Task ti, double endTime,  boolean opt, String dvfs)
    {
       
        
        //double execTime = ti.getCloudlet().getCloudletLength()/1000;
        double execTime = ti.getCloudlet().getFinishTime()-ti.getCloudlet().getExecStartTime();
        double powerFull,powerIdle;
        
        powerFull= model.getPMax(model.getnbFreq()-1); // OK si la tache app au CP
        powerIdle= model.getPMin(model.getnbFreq()-1); // OK si la tache app au CP
        if(pooling)
            powerIdle = powerFull;
        
        if(!ti.isCritical() && opt)
        {
           
                if(dvfs.equalsIgnoreCase("ondemand"))
                {
                    powerFull = model.getPMax(model.getnbFreq()-1);
                    powerIdle = model.getPMin(0);
                    if(pooling)
                        powerIdle = powerFull;
                }
                else
                {
                    powerFull = model.getPMax(ti.getOptIndexFreq());
                    powerIdle = model.getPMin(ti.getOptIndexFreq());
                    if(pooling)
                        powerIdle = powerFull;
                }            
        }
        Log.printLine("task : " + ti.getId() +" app au CP : " + ti.isCritical() + " => power utilise : " + powerIdle+"/"+powerFull + "length = " + execTime);
        
        
        double E = execTime*powerFull + (endTime-execTime)*powerIdle;
        return E/3600;
    
    }
    
/*    public void main(String[] args)
    {
    
    }*/
    public boolean isPooling() {
        return pooling;
    }
}

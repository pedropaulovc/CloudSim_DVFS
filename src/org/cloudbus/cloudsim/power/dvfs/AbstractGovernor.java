/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */
package org.cloudbus.cloudsim.power.dvfs;

/**
 *
 * Governor DVFS Class
 * 
 * The Governor decides if it's time to increase/decrease the CPU frequency, regarding a certain threshold.
 * 
 * @author Guérout Tom, Monteil Thierry, Da Costa Georges  |  LAAS - IRIT , Toulouse, France
 * Please cite:
 * T. Guérout et al., Energy-aware simulation with DVFS, Simulat. Modell. Pract. Theory (2013), http://dx.doi.org/10.1016/j.simpat.2013.04.007
 */
public abstract class AbstractGovernor {

    private  String name;
    protected int defautIndexFreq;

    private int downThreshold;
    private int upThreshold;
    
    
    /*
     * 
     * This methode is overwited on OnDemand and Conservative governor
     * because they have special behaviour
     * A decision of increase or decrease frequency has not the same effect on the frequency (different step)
     * with OnDemand or with Conservative mode.
     * Moreover, these two mode may have special Attribute to take in account.
     * 
     * 
     */
   public  int SpecificDecision(double util){return 0 ;}
    
    /**
     * 
     * Return the dvfs governor Decision regarding the current CPU Utilization
     * This methode is the same for all mode, it just decide to increase or decrease frequency
    * in relantion with Thresholds.
    * 
     * @return 0/-1/1  , the decision : increase/decrease or no the frequency
     * 
     */
    protected int decision(double util)
    {
        
      /*  System.out.println("decision abstract governor :");
        System.out.println("util = " + util);
        System.out.println("down thresh = " + down_threshold);
        System.out.println("up thresh = " + up_threshold);*/
        
        // -1 step_bw
        // 0 no change
        // 1 step_fw
        
        if(util < downThreshold)
            return -1;
        else if(util > upThreshold)
            return 1;
        else
            return 0;

    }
      
    
    protected void setDownThreshold(int down_threshold) {
        this.downThreshold = down_threshold;
    }

    protected void setUpThreshold(int up_threshold) {
        this.upThreshold = up_threshold;
    }
    protected void setName(String name) {
        this.name = name;
    }
    public String getName(){return name;}
    
  
    public int getDefautIndexFreq() {
        return defautIndexFreq;
    }

    public void setDefautIndexFreq(int nb_freq) 
    {
        defautIndexFreq = 0;
    };

}

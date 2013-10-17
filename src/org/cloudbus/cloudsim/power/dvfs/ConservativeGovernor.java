/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.power.dvfs;

import java.util.HashMap;


/**
 *
 * Conservative governor.
 * This class override "specificDecision" method to return the right value
 * in relation with Conservative mod behavior
 * 
 * @author Guérout Tom, Monteil Thierry, Da Costa Georges  |  LAAS - IRIT , Toulouse, France
 *  Please cite:
 * T. Guérout et al., Energy-aware simulation with DVFS, Simulat. Modell. Pract. Theory (2013), http://dx.doi.org/10.1016/j.simpat.2013.04.007
 */
public class ConservativeGovernor extends AbstractGovernor {
    
    
    private boolean enableFreqStep = false;
    private int freqStep = 5;   // % 


    

    public ConservativeGovernor(HashMap<String,Integer> configConservative_)
    {
        setName("Conservative");
        setDownThreshold(20);
        setUpThreshold(80);
        ConfigParameters(configConservative_);
    }


     private int getFreq_step() {
        return freqStep;
    }


        @Override
       public int SpecificDecision(double utilPe) {

           int desc = decision(utilPe); //Thresholds[1],Thresholds[0]);

           return desc;
    }
             
        
    private void ConfigParameters(HashMap<String,Integer> configConservative)
    {
        
        
        if(configConservative.containsKey("up_threshold"))
        {
            Object o = configConservative.get("up_threshold");
            setUpThreshold((int)o);
            System.out.println("Conservative UP_Threshold new value : " + (int)o);
        }
        if(configConservative.containsKey("down_threshold"))
        {
            Object o = configConservative.get("down_threshold");
            setDownThreshold((int)o);
            System.out.println("Conservative DOWN_Threshold new value : " + (int)o);
        }
        if(configConservative.containsKey("enablefreqstep"))
        {
            Object o = configConservative.get("enablefreqstep");
            int tmp_enablefreqstep = (int)o;
            if(tmp_enablefreqstep==0)
                enableFreqStep = false;
            else if(tmp_enablefreqstep==1)
                enableFreqStep = true;
            System.out.println("Conservative ENABLEFREQSTEP new value : " + enableFreqStep);
        }
        if(configConservative.containsKey("freqstep"))
        {
            Object o = configConservative.get("freqstep");
            freqStep = (int)o;
            System.out.println("Conservative FREQSTEP new value : " + freqStep);
        }
    }
             
                
 public boolean isEnable_freq_step() {
        return enableFreqStep;
    }

 
    
}

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
 * OnDemand Governor.
 * 
 * This class override "specificDecision" method to return the right value
 * in relation with OnDemand mod behavior, especially for the SamplingDownFactor parameter
 * 
 * 
 * @author Guérout Tom, Monteil Thierry, Da Costa Georges  |  LAAS - IRIT , Toulouse, France
 *  Please cite:
 * T. Guérout et al., Energy-aware simulation with DVFS, Simulat. Modell. Pract. Theory (2013), http://dx.doi.org/10.1016/j.simpat.2013.04.007
 */

public class OnDemandGovernor extends AbstractGovernor{
    
                /*
         * Value between 0 and 100
         * wait "samplingDownFactor" iteration before decrease frequency
         */
        private int samplingDownFactor=100; 
        
        
        /*
         * Count iteration before decrease frequency
         * Until downIt < samplingDownFactor
         */
        private int downIt=0;

    
        
        
    public OnDemandGovernor(HashMap<String,Integer> configOnDemand_)
    {
        setName("OnDemand");
        setDownThreshold(95);
        setUpThreshold(95);
        ConfigParameters(configOnDemand_);
        
    }
      
    @Override
       public int SpecificDecision(double utilPe)
       {
           System.out.println("spec descision ondemand , util = " + utilPe);
           int desc = decision(utilPe) ;
           System.out.println("desc == " + desc);
           if(desc == -1)
           {
                if(IsTimetoDown())
                    setDownItReset();
                else
                {
                    setDownItIncr();
                    return 0;
                }
           }
           else if(desc==1)
               return 2;
           
           
           return desc;
       }
      
    
    
    
    
     private void ConfigParameters(HashMap<String,Integer> configOnDemand)
    {
        
        if(configOnDemand.containsKey("up_threshold"))
        {
            int Threshold;
            Object o = configOnDemand.get("up_threshold");
            Threshold = (int)o;
            setDownThreshold(Threshold);
            setUpThreshold(Threshold);
            System.out.println("OnDemand UP_Threshold new value : " + Threshold);
        }

        if(configOnDemand.containsKey("sampling_down_factor"))
        {
            Object o = configOnDemand.get("sampling_down_factor");
            setSampling_down_factor((int)o);
            System.out.println("OnDemand SamplingDownFactor new value : " + samplingDownFactor);
        }
    }
    
    
    
    
    
       
    private boolean IsTimetoDown() {
        if(downIt>=samplingDownFactor)
            return true;
        else
            return false;
    }
      
    
    private void setDownItReset() {
        downIt = 0;
    }
    private void setDownItIncr() {
        downIt++;
    }
        
    /*
     * Configure the specific OnDemand parameter "sampling_down_factor"
     * 
     */
    private void setSampling_down_factor(int samplingDownFactor_) {
        samplingDownFactor = samplingDownFactor_;
    }
        
    
    
}

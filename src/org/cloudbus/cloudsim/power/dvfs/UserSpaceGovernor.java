/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cloudbus.cloudsim.power.dvfs;

import java.util.HashMap;

/**
 *
 * UserSpace Governor
 * 
 * This class override "setDefautIndexFreq" method, to set the frequency
 * to the value chosen by the user
 * 
 * 
 * @author Guérout Tom, Monteil Thierry, Da Costa Georges  |  LAAS - IRIT , Toulouse, France
 *  Please cite:
 * T. Guérout et al., Energy-aware simulation with DVFS, Simulat. Modell. Pract. Theory (2013), http://dx.doi.org/10.1016/j.simpat.2013.04.007
 */
public class UserSpaceGovernor extends AbstractGovernor {
    
    
    int userFreq;
    
    public UserSpaceGovernor(HashMap<String,Integer> configUserSpace)
    {
        setName("UserSpace");
        ConfigParameters(configUserSpace);
    }
    
    @Override
    public void setDefautIndexFreq(int freq) {
        defautIndexFreq = userFreq;
        //System.out.println("freqqqq : " + freq);
    }
    
     

    
    // If the user does not specify the Frequency to use
    // it's set to the Maximum frequency by default

    private void ConfigParameters(HashMap<String,Integer> configUserSpace)
    {
        int freq;
        if(configUserSpace.containsKey("frequency"))
        {
            Object o = configUserSpace.get("frequency");
            freq = (int)o;
            userFreq = freq-1;
            setDefautIndexFreq(userFreq);
            System.out.println("UserSpace Frequency value : f" + freq);
        }

    }
    
     
}



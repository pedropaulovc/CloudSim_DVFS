/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cloudbus.cloudsim.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;


/**
 *
 * @author Guérout Tom, Monteil Thierry, Da Costa Georges  |  LAAS - IRIT , Toulouse, France
 * Please cite:
 * T. Guérout et al., Energy-aware simulation with DVFS, Simulat. Modell. Pract. Theory (2013), http://dx.doi.org/10.1016/j.simpat.2013.04.007
 */
public class DvfsDatas {
    
    
    private HashMap<String, Integer> hashT_OnDemand;
    private HashMap<String, Integer> hashT_Conservative;
    private HashMap<String, Integer> hashT_UserSpace;
    
    
    public DvfsDatas()
    {
    
        hashT_OnDemand = new HashMap<>();
        hashT_Conservative = new HashMap<>();
        hashT_UserSpace = new HashMap<>();
    }
    
    

    public HashMap<String, Integer> getHashMapConservative() {
        return hashT_Conservative;
    }

    public void setHashMapConservative(HashMap<String, Integer> hashT_Conservative) {
        this.hashT_Conservative = hashT_Conservative;
    }

    public HashMap<String, Integer> getHashMapOnDemand() {
        return hashT_OnDemand;
    }

    public void setHashMapOnDemand(HashMap<String, Integer> hashT_OnDemand) {
        this.hashT_OnDemand = hashT_OnDemand;
    }
            
    
      public HashMap<String, Integer> getHashMapUserSpace() {
        return hashT_UserSpace;
    }

    public void setHashMapUserSpace(HashMap<String, Integer> hashT_UserSpace) {
        this.hashT_UserSpace = hashT_UserSpace;
    }
}

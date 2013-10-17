/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.power.models;

import java.util.Iterator;
import java.util.List;
import org.cloudbus.cloudsim.Pe;




/**
 * 
 * PowerModel for Grid'5000 Reims site Host.
 * https://www.grid5000.fr/mediawiki/index.php/Reims:Hardware
 * 
 * 
 * @author Guérout Tom, Monteil Thierry, Da Costa Georges  |  LAAS - IRIT , Toulouse, France
 * *  Please cite:
 * T. Guérout et al., Energy-aware simulation with DVFS, Simulat. Modell. Pract. Theory (2013), http://dx.doi.org/10.1016/j.simpat.2013.04.007
 */
public class PowerModelSpecPower_REIMS extends PowerModelSpecPowerDVFS {

	/** Tables power, in relation with CPU Frequency !
     * tabIdle[f1][f2]....[fn]
     * tabFull[f1][f2]....[fn]
     */

        double Tab_Power_idle[]={140,146,153,159,167};
        double Tab_Power_full[]={228,238,249,260,272};
	List<Pe> peList;

        Pe tmp_pe;
        
         public PowerModelSpecPower_REIMS () {}
        public PowerModelSpecPower_REIMS ( List<Pe> PeList_) 
        {                
                peList = PeList_;
                Iterator it = peList.iterator();
                Object o = it.next();
                tmp_pe = (Pe)o;
	}
        
        
        /**
         * 
         * The power model use here is the classical linear power model
         * 
         * Cmin + UtilizationPe [Cmax - Cmin]
         * 
         * 
         * @param utilization
         * @return
         * @throws IllegalArgumentException 
         */
    @Override
    public double getPower(double utilization) throws IllegalArgumentException {
           double power ;
           int index = tmp_pe.getIndexFreq();           
           
                power = (1-utilization)*Tab_Power_idle[index] + utilization*Tab_Power_full[index];
                //System.out.println("Power computation : index current freq = "  + index + " / associated value = "  +  Tab_Power_idle[index] +"/"+ Tab_Power_full[index]);                             
                //System.out.println("(1 - " + utilization + ")*"+Tab_Power_idle[index]+" + " + utilization + " * " + Tab_Power_full[index]);
          //      System.out.println("Power = " + power);
		
                return power;
    }
    

    
    
  
    public double getPMin(int frequency)
    {
        return Tab_Power_idle[frequency];
    }

    public double getPMax(int frequency)
    {
        return Tab_Power_full[frequency];
    }
    public int getnbFreq()
    {
        return Tab_Power_full.length;
        
    }
}

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
 * Performance Governor
 * This class override setDefautIndexFreq method to set the defaut frequency
 * at the maximum.
 * 
 * @author Guérout Tom, Monteil Thierry, Da Costa Georges  |  LAAS - IRIT , Toulouse, France
 *  Please cite:
 * T. Guérout et al., Energy-aware simulation with DVFS, Simulat. Modell. Pract. Theory (2013), http://dx.doi.org/10.1016/j.simpat.2013.04.007
 */
public class PerformanceGovernor extends AbstractGovernor {
    

    public PerformanceGovernor()
    {
        setName("Performance");
    }

    @Override
    public void setDefautIndexFreq(int index) {
        defautIndexFreq = index;
    }
    
}

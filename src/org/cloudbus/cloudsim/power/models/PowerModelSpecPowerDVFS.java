/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.power.models;

import org.cloudbus.cloudsim.Log;


/**
 *
 * Generic Power Model Class Using DVFS
 * Your PowerModel class (with yours power consumption values must extend this class
 * 
 * @author Guérout Tom, Monteil Thierry, Da Costa Georges  |  LAAS - IRIT , Toulouse, France
 * *  Please cite:
 * T. Guérout et al., Energy-aware simulation with DVFS, Simulat. Modell. Pract. Theory (2013), http://dx.doi.org/10.1016/j.simpat.2013.04.007
 */
public abstract class PowerModelSpecPowerDVFS implements PowerModel {

	/*
	 * (non-Javadoc)
	 * @see org.cloudbus.cloudsim.power.models.PowerModel#getPower(double)
	 */

	public double getPower(double utilization) throws IllegalArgumentException {
		if (utilization < 0 || utilization > 1) {
			throw new IllegalArgumentException("Utilization value must be between 0 and 1 (value = " + utilization +" )");
		}
		
                double power = getPower(utilization);
		
		return power;
	}

        
    public abstract double getPMin(int frequency);
    

    public abstract double getPMax(int frequency);


}

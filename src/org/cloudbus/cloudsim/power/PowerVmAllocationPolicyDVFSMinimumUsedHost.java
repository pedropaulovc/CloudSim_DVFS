/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.power;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;

/**
 * 
 * @author Guérout Tom, Monteil Thierry, Da Costa Georges  |  LAAS - IRIT , Toulouse, France
 *  Please cite:
 * T. Guérout et al., Energy-aware simulation with DVFS, Simulat. Modell. Pract. Theory (2013), http://dx.doi.org/10.1016/j.simpat.2013.04.007
 */
public class PowerVmAllocationPolicyDVFSMinimumUsedHost extends PowerVmAllocationPolicyAbstract {

	/**
	 * Instantiates a new power vm allocation policy simple.
	 * 
	 * @param list the list
	 */
	public PowerVmAllocationPolicyDVFSMinimumUsedHost(List<? extends Host> list) {
		super(list);
	}

	/*
	 * (non-Javadoc)
	 * @see org.cloudbus.cloudsim.VmAllocationPolicy#optimizeAllocation(java.util.List)
	 */
	@Override
	public List<Map<String, Object>> optimizeAllocation(List<? extends Vm> vmList) {
		// This policy does not optimize the VM allocation
		return null;
	}
        
        /**
	 * Find host for vm.
	 * 
	 * @param vm the vm
	 * @return the power host
	 */
        @Override
	public PowerHost findHostForVm(Vm vm) {
            ArrayList<PowerHost> SuitablesHosts = new ArrayList<>();
            PowerHost moreAvailHost = null;
		for (PowerHost host : this.<PowerHost> getHostList()) {
			if (host.isSuitableForVm(vm)) {
                                SuitablesHosts.add(host);
			}
		}
                
                if(!SuitablesHosts.isEmpty())
                {
                    double maxAvailMips=-1;
                    
                    for(int i = 0 ; i < SuitablesHosts.size() ; i++)
                    {
                        if(maxAvailMips < SuitablesHosts.get(i).getAvailableMips() )
                        {
                            maxAvailMips = SuitablesHosts.get(i).getAvailableMips();
                            moreAvailHost = SuitablesHosts.get(i);
                        }
                    }
                    Log.printLine("CHOSEN HOST for VM " + vm.getId()+ " is : Host #" + moreAvailHost.getId());
                    return moreAvailHost;
                }
                
                else
                {
                    
                    Log.printLine("No host available for VM " + vm.getId()+ " can be found without modifications");
                // if we are here, it means that no Host are suitable...
                // HOST that used DVFS can be modified to host a new VM
                // OR Vm size has to be decrease
                    
                    double maxAvailMips=-1;
                    // so we take the host with the higher FREE mips
                    for(int i = 0 ; i < getHostList().size() ; i++)
                    {
                        if(maxAvailMips < getHostList().get(i).getAvailableMips() )
                        {
                            maxAvailMips = getHostList().get(i).getAvailableMips();
                            moreAvailHost = (PowerHost) getHostList().get(i);
                        }
                    }
                   
                    // Normally, there is no case where moreAvailHost can be NULL
                    //  moreAvailHost is NOT NULL
                     if(moreAvailHost.MakeSuitableHostForVm(vm)) // change Pe frequency
                            return moreAvailHost;
                        else
                        {
                            if(moreAvailHost.decreaseVMMipsToHostNewVm(vm))
                                return moreAvailHost;
                        }
                    Log.printLine("CHOSEN HOST for VM " + vm.getId()+ " is : Host #" + moreAvailHost.getId());
                }
                
		return null;
	}


}

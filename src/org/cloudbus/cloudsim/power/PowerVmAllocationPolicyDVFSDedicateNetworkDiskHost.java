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
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;

/**
 * 
 * @author Guérout Tom, Monteil Thierry, Da Costa Georges  |  LAAS - IRIT , Toulouse, France
 *  Please cite:
 * T. Guérout et al., Energy-aware simulation with DVFS, Simulat. Modell. Pract. Theory (2013), http://dx.doi.org/10.1016/j.simpat.2013.04.007
 */

public class PowerVmAllocationPolicyDVFSDedicateNetworkDiskHost extends PowerVmAllocationPolicyAbstract {

	/**
	 * Instantiates a new power vm allocation policy simple.
	 * 
	 * @param list the list
	 */
        private List<PowerHost> hostList;
	public PowerVmAllocationPolicyDVFSDedicateNetworkDiskHost(List<PowerHost> list) {
		super(list);
                hostList=list;
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
            
            int ss_Id_int=-1;
            Log.printLine("VM ID = " + vm.getId());
         
            String s_Id = String.valueOf(vm.getId());
            if(s_Id.length() > 3 )
            {
                String ss_Id = s_Id.substring(s_Id.length()-2, s_Id.length());
                ss_Id_int = Integer.parseInt(ss_Id);
           }
            else
                ss_Id_int = vm.getId();
            
       
            if(ss_Id_int < 10)
                return findCPUHostforVm(vm);
            else if (ss_Id_int < 20)
                return findNETHostforVm(vm);
            else if  (ss_Id_int >= 20)
                 return findDISKHostforVm(vm);
            else
                return null;
            
	}

public PowerHost findNETHostforVm(Vm vm){return hostList.get(hostList.size()-3);}

public PowerHost findDISKHostforVm(Vm vm)
{

    ArrayList<PowerHost> SuitablesHosts = new ArrayList<>();
            PowerHost moreAvailHost = null;

                for (int i = 3 ; i < getHostList().size() ; i++ ) {
                    PowerHost host = (PowerHost)getHostList().get(i);

			if (host.isSuitableForVm(vm)) {
                                Log.printLine("Saved Host : " + host.getId());
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
                    return moreAvailHost;
                }
    
return null;

}
        
public PowerHost findCPUHostforVm(Vm vm)
{
             
            
            ArrayList<PowerHost> SuitablesHosts = new ArrayList<>();
            PowerHost moreAvailHost = null;

                for (int i = 0 ; i < getHostList().size()-3 ; i++ ) {
                    PowerHost host = (PowerHost)getHostList().get(i);
                    Log.formatLine("In findHostCPU_ForVM");
			if (host.isSuitableForVm(vm)) {
                                Log.printLine("Saved Host : " + host.getId());
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
                    return moreAvailHost;
                }
                
                else
                {
                //if we are here, it means that no Host are suitable...
                //HOST that used DVFS can be modified to host a new VM
                // OR Vm size has to be decrease
                    
                    double maxAvailMips=-1;
                    
                    for(int i = 0 ; i < getHostList().size() ; i++)
                    {
                        if(maxAvailMips < getHostList().get(i).getAvailableMips() )
                        {
                            maxAvailMips = getHostList().get(i).getAvailableMips();
                            moreAvailHost = (PowerHost) getHostList().get(i);
                        }
                    }
                    // moreAvailHost IS NOT NULL
                     if(moreAvailHost.MakeSuitableHostForVm(vm)) // change Pe frequency
                            return moreAvailHost;
                        else
                        {
                            if(moreAvailHost.decreaseVMMipsToHostNewVm(vm))
                                return moreAvailHost;
                        }
                    
                }
                
		return null;
}
        
}

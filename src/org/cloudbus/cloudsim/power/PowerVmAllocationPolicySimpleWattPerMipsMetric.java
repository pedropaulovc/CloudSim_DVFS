/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.power;

import java.util.List;
import java.util.Map;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.provisioners.PeProvisioner;

/**
 *
 * This class defines a "watt per mips" metric too choose a Host when all are overused.
 * 
 * The aim is to choose the Host that have the best ratio Watt/Mips weighted by the 
 * theoretical capacity to host a new VM 
 * 
 * 
 * @author Guérout Tom, Monteil Thierry, Da Costa Georges  |  LAAS - IRIT , Toulouse, France
 *  Please cite:
 * T. Guérout et al., Energy-aware simulation with DVFS, Simulat. Modell. Pract. Theory (2013), http://dx.doi.org/10.1016/j.simpat.2013.04.007
 */
public class PowerVmAllocationPolicySimpleWattPerMipsMetric extends PowerVmAllocationPolicyAbstract{

    
    public PowerVmAllocationPolicySimpleWattPerMipsMetric(List<? extends Host> list) {
		super(list);
	}

    @Override
    public List<Map<String, Object>> optimizeAllocation(List<? extends Vm> vmList) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    @Override    
      public boolean allocateHostForVm(Vm vm)
        {
            double minMetric = -1;
            PowerHost ChoosenHost = null;
            //Log.formatLine("In ChooseHostForNewVm function...");
                for (PowerHost host : this.<PowerHost> getHostList()) {
                    
                    double tmpMetric = MetricWattPerMips(host); 
             //       Log.printLine("MetricHost value for HOST #" + host.getId() + " = " + tmpMetric);
                     if(minMetric != -1 && tmpMetric < minMetric) 
                     {
                            minMetric = tmpMetric;
                            ChoosenHost = host;
//                            Log.printLine("Temp CHOSEN HOST is : Host #" + ChosenHost.getId());
                     }
                     else if(minMetric == -1)
                     {
                         minMetric = tmpMetric;
                         ChoosenHost = host;                        
                     }
                }
             
                Log.printLine("CHOSEN HOST for VM " + vm.getId()+ " is : Host #" + ChoosenHost.getId());
                boolean allocationOK = false;
                allocationOK = allocateHostForVm(vm,ChoosenHost);
                
                if(allocationOK)
                    return true;
                if(!allocationOK && ChoosenHost.isEnableDVFS() )               
                {
                    Log.printLine("Not enough free MIPS in the choosen HOST !");
                    Log.printLine("Trying to decrease VMs size in this HOST!");
                    ChoosenHost = TryDVFSEnableHost(ChoosenHost,vm);
                    if( allocateHostForVm(vm,ChoosenHost))
                    {
                        Log.printLine("VMs size decreased successfully !");
                        return true;
                    }
                    else
                        Log.printLine("Error , VMs size not enough decreased successfully !");
                }
                                      
                return false;
        }
    
      private double MetricWattPerMips(PowerHost h)
        {
            int nb_pe = h.getPeList().size();
            double tmp_metric=0;
            
            for(Pe pe : h.getPeList())
            {
                
                PeProvisioner pe_p = pe.getPeProvisioner();
                int indexFreqPe = pe.getIndexFreq();
                double mipsFreqPe = pe.getMips();
                double H_Pmax = h.getPMax(indexFreqPe);
                double H_Pmin = h.getPMin(indexFreqPe);
                
                double Pe_Pmax = H_Pmax * mipsFreqPe / h.getTotalMips();
                double Pe_Pmin = H_Pmin * mipsFreqPe / h.getTotalMips();
                
                double UtilPe = pe_p.getUtilization()*100;
                double MaxMipsHost = h.getTotalMaxMips();
                
                List<Vm> vmList= h.getVmList();
                double SumVmMaxMips=0;
                 for (Vm vm : vmList) 
                     SumVmMaxMips+=vm.getMaxMips();
                 if(SumVmMaxMips==0)
                     SumVmMaxMips=0.1;
                
                //tmp_metric+= (Pe_Pmax - Pe_Pmin) * pe_p.getUtilization() / pe_p.getAvailableMips();
                tmp_metric+= (Pe_Pmax - Pe_Pmin) * SumVmMaxMips / MaxMipsHost;
                             
                //Log.printLine("tmpMetricPe = (" + Pe_Pmax + " - " + Pe_Pmin + " ) * " + SumVmMaxMips + " / " + MaxMipsHost);
                
            }
                return tmp_metric / nb_pe;
        }
}

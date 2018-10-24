package org.cloudbus.cloudsim.workflow;

import java.util.Hashtable;

import org.cloudbus.cloudsim.Vm;

public class VmOffersSimple_2 extends VMOffers {
	
	int baseMem;
	int baseCost;
	long baseStorage;
	
        double [] perf = {47.06,58.82,70.59,88.24,1};
        
	@Override
	public Hashtable<Vm, Integer> getVmOffers() {
		double baseMips = Integer.parseInt(Properties.MIPS_PERCORE.getProperty());
		baseMem = Integer.parseInt(Properties.MEMORY_PERHOST.getProperty());
		baseStorage = Long.parseLong(Properties.STORAGE_PERHOST.getProperty());
		baseCost = 10;
		
                vmOffersTable.put(new Vm(0,0,2*perf[0]*baseMips/100,1, 2*baseMem,1250,2*baseStorage,"",null),   new Double(2*perf[0]*baseMips/100).intValue());
                vmOffersTable.put(new Vm(0,0,2*perf[1]*baseMips/100,1, 2*baseMem,1250,2*baseStorage,"",null),   new Double(2*perf[1]*baseMips/100).intValue());
                vmOffersTable.put(new Vm(0,0,2*perf[2]*baseMips/100,1, 2*baseMem,1250,2*baseStorage,"",null),   new Double(2*perf[2]*baseMips/100).intValue());
                vmOffersTable.put(new Vm(0,0,2*perf[3]*baseMips/100,1, 2*baseMem,1250,2*baseStorage,"",null),   new Double(2*perf[3]*baseMips/100).intValue());
		//vmOffersTable.put(new Vm(1,0,baseMips,1,  baseMem,0,  baseStorage,"",null),   baseCost);
		vmOffersTable.put(new Vm(1,0,2*perf[4]*baseMips,1,2*baseMem,1250,2*baseStorage,"",null), new Double(2*perf[4]*baseMips).intValue());
		//vmOffersTable.put(new Vm(2,0,4*baseMips,1,4*baseMem,0,4*baseStorage,"",null), 4*baseCost);
		
		return vmOffersTable;
	}

	@Override
	public int getCost(double mips, int memory, long bw) {
			if (memory==baseMem) return baseCost;
			if (memory==2*baseMem) return 2*baseCost;
			return 4*baseCost;
	}

	@Override
	public long getTimeSlot() {
		return 3600; //one hour, in seconds
	}

	@Override
	public long getBootTime() {
		return Long.parseLong(Properties.VM_DELAY.getProperty());
	}
        
        public double valuePerf(int index)
        {return perf[index];}
}

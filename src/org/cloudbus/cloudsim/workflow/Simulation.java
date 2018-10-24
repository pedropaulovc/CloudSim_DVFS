package org.cloudbus.cloudsim.workflow;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.NetworkTopology;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.power.PowerDatacenter;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.power.PowerVmAllocationPolicySimple;
import org.cloudbus.cloudsim.power.models.PowerModelSpecPower_REIMS;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

/**
 * This class contains the main method for execution of the simulation.
 * Here, simulation parameters are defined. Parameters that are dynamic
 * are read from the properties file, whereas other parameters are hardcoded.
 * 
 * Decision on what should be configurable and what is hardcoded was somehow
 * arbitrary. Therefore, if you think that some of the hardcoded values should
 * be customizable, it can be added as a Property. In the Property code there
 * is comments on how to add new properties to the experiment.
 *
 */
public class Simulation {
	
	public Simulation(){
		
	}

	/**
	 * Prints input parameters and execute the simulation a number of times,
	 * as defined in the configuration.
	 * 
	 */
	public static void main(String[] args) {
		new Simulation();
		Log.printLine("========== Simulation configuration ==========");
		for (Properties property: Properties.values()){
			Log.printLine("= "+property+": "+property.getProperty());
		}
		Log.printLine("==============================================");
		Log.printLine("");
				
		int rounds = Integer.parseInt(Properties.EXPERIMENT_ROUNDS.getProperty());
		for (int round=1; round<=rounds; round++) {
			runSimulationRound(round);
		}
	}
				
	/**
	 * One round of the simulation is executed by this method. Output
	 * is printed to the log.
	 * 
	 */
	private static void runSimulationRound(int round) {
		Log.printLine("Starting simulation round "+round+".");
		
		long seed = SeedGenerator.getSeed(round-1);

		try {
			CloudSim.init(1,Calendar.getInstance(),false);

			WorkflowDatacenter datacenter = createDatacenter("Datacenter", seed);
                        //PowerDatacenter datacenter = createDatacenter("Datacenter", seed);
			WorkflowEngine engine = createWorkflowEngine();
			
			double latency = Double.parseDouble(Properties.NETWORK_LATENCY.getProperty());
			NetworkTopology.addLink(datacenter.getId(),engine.getId(),100000,latency);

			CloudSim.startSimulation();
			engine.printExecutionSummary();
			double energy = engine.computeEnergy();
			
			FileWriter fw = new FileWriter("/home/user/CloudSim_DVFS_Results.txt", true);
//			fw.write("policy,dag,optimize,modedvfs,energy,endTime\n");
			fw.write(Properties.SCHEDULING_POLICY.getProperty() + ",");
			fw.write(Properties.DAG_FILE.getProperty() + ",");
			fw.write(Properties.OPTIMIZE.getProperty() + ",");
			fw.write(Properties.MODEDVFS.getProperty() + ",");
			fw.write(energy + ",");
			fw.write(engine.getEndTime() + "\n");
			fw.close();
                        
              //          Log.printLine(String.format("Power Sum :  %.8f W", datacenter.getPower() ));                    
                        

			Log.printLine("");
			Log.printLine("");
		} catch (Exception e) {
			Log.printLine("Unwanted errors happen.");
			e.printStackTrace();
		} finally {
			CloudSim.stopSimulation();
		}
	}

	private static WorkflowDatacenter createDatacenter(String name, long seed) throws Exception{
       // private static PowerDatacenter createDatacenter(String name, long seed) throws Exception{
		int hosts = Integer.parseInt(Properties.HOSTS_PERDATACENTER.getProperty());
		int ram = 8*Integer.parseInt(Properties.MEMORY_PERHOST.getProperty());
		int cores = 8*Integer.parseInt(Properties.CORES_PERHOST.getProperty());
		int mips = 8*Integer.parseInt(Properties.MIPS_PERCORE.getProperty());
		long storage = 8*Long.parseLong(Properties.STORAGE_PERHOST.getProperty());
		double bw = Double.parseDouble(Properties.INTERNAL_BANDWIDTH.getProperty());
		double latency = Double.parseDouble(Properties.INTERNAL_LATENCY.getProperty());
		long delay = Long.parseLong(Properties.VM_DELAY.getProperty());
		String offerName = Properties.VM_OFFERS.getProperty();
		
		VMOffers offers = null;
		try{				
			Class<?> offerClass = Class.forName(offerName,true,VMOffers.class.getClassLoader());
			offers = (VMOffers) offerClass.newInstance();
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e){
			e.printStackTrace();
			return null;
		}
							
		List<Host> hostList = new ArrayList<>();
               // List<PowerHost> hostList = new ArrayList<>();
		for(int i=0;i<hosts;i++){
			List<Pe> peList = new ArrayList<>();
			for(int j=0;j<cores;j++) peList.add(new Pe(j, new PeProvisionerSimple(mips)));
			
                        
			hostList.add(new Host(i,new RamProvisionerSimple(ram),new BwProvisionerSimple(1000000),
				  storage,peList,new VmSchedulerTimeShared(peList)));
                        
			//hostList.add(new PowerHost(i,new RamProvisionerSimple(ram),new BwProvisionerSimple(1000000),
			//	  storage,peList,new VmSchedulerTimeShared(peList), new PowerModelSpecPower_REIMS(peList) , false));
		}

		DatacenterCharacteristics characteristics = new DatacenterCharacteristics("Xeon","Linux","Xen",hostList,10.0,0.0,0.00,0.00,0.00);
				
		return new WorkflowDatacenter(name,characteristics,new VmAllocationPolicySimple(hostList),bw,latency,mips,delay,offers,seed);
        
        
        /*PowerDatacenter powerDatacenter = new PowerDatacenter(
					name,
					characteristics,
//					new PowerVmAllocationPolicySimple(hostList),	
//					new PowerVmAllocationPolicySimpleWattPerMipsMetric(hostList),
					//new PowerVmAllocationPolicyDVFSMinimumUsedHost(hostList),
					new PowerVmAllocationPolicySimple(hostList),
					new LinkedList<Storage>(),
                                        0.1);
        
         
              return powerDatacenter;*/
        
	}
        
        
        
        
        	
        
        
        

	private static WorkflowEngine createWorkflowEngine(){
		String dagFile = Properties.DAG_FILE.getProperty();
		String className = Properties.SCHEDULING_POLICY.getProperty();
		String offerName = Properties.VM_OFFERS.getProperty();
		long deadline = Long.parseLong(Properties.DAG_DEADLINE.getProperty());
		int baseMIPS = Integer.parseInt(Properties.MIPS_PERCORE.getProperty());
		Policy policy = null;
		VMOffers offers = null;
		
		try{		
			Class<?> policyClass = Class.forName(className,true,Policy.class.getClassLoader());
			policy = (Policy) policyClass.newInstance();
			
			Class<?> offerClass = Class.forName(offerName,true,VMOffers.class.getClassLoader());
			offers = (VMOffers) offerClass.newInstance();
		
			return new WorkflowEngine(dagFile,deadline,baseMIPS,policy,offers);
		} catch (Exception e){
			e.printStackTrace();
			return null;
		}
	}
}

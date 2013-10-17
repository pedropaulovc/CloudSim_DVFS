package Self_Description.DVFS_2;


/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation
 *               of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009, The University of Melbourne, Australia
 */


/*
*
*@author Guérout Tom
*Experiment to compare Dvfs behaviour and power consumption resultat between CloudSim and a Real Host.
*/

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import java.util.Map.Entry;
import java.util.Collections;

import org.cloudbus.cloudsim.xml.*;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerSpaceShared;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.core.EventPostBroker;
import org.cloudbus.cloudsim.power.PowerDatacenter;
import org.cloudbus.cloudsim.power.PowerDatacenterBroker;
import org.cloudbus.cloudsim.power.PowerHost;


import org.cloudbus.cloudsim.power.PowerVmAllocationPolicySimpleWattPerMipsMetric;


import org.cloudbus.cloudsim.power.models.PowerModelLinear;
import org.cloudbus.cloudsim.power.models.PowerModelSpecPower_BAZAR;

import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

/**
 * An example of a heterogeneous DVFS-enabled data center: the voltage
 * and clock frequency of the CPU are adjusted ideally according
 * to current resource requirements.
 */
public class Dvfs_example_more_complex {

	private static SimulationXMLParse ConfSimu;

	private static DvfsDatas ConfigDvfs;

	/** The cloudlet list. */
	private static List<Cloudlet> cloudletList;

	/** The vm list. */
	private static List<Vm> vmList;

	private static int user_id;

	private static int DCNumber;
	private static int hostsNumber;
	private static int vmsTotalNumber;
	private static int no_cur_vm=0;

	private static int cloudletsTotalNumber;
	private static int no_cur_cloudlet=0;


	private static ArrayList<DatacenterBroker> vect_dcbroker ;


	/**
	 * Creates main() to run this example.
	 *
	 * @param args the args
	 */
	public static void main(String[] args) {

		Log.printLine("Starting Dvfs_example_more_complex...");
		

		try {
		
			DCNumber = 1;
			cloudletsTotalNumber = 1;
			hostsNumber = 1;
			vmsTotalNumber = 1;

			
			vect_dcbroker = new ArrayList<DatacenterBroker>();
			int num_user = 1; 
			Calendar calendar = Calendar.getInstance();
			boolean trace_flag = false;

			// Initialize the CloudSim library
			CloudSim.init(num_user, calendar, trace_flag);

			// Second step: Create Datacenters
			// Datacenters are the resource providers in CloudSim. We need at
			// list one of them to run a CloudSim simulation


			PowerDatacenter datacenter = createDatacenter("Datacenter_0");
			datacenter.setDisableMigrations(true);
			
			//creation of GlobalBroker
			GlobalBroker globalBroker = new GlobalBroker("GlobalBroker");

			vmList = new ArrayList<>();
			cloudletList = new ArrayList<>();

			// Sixth step: Starts the simulation
			double lastClock = CloudSim.startSimulation();

			// Final step: Print results when simulation is over

			List<Cloudlet> newList = vect_dcbroker.get(0).getCloudletReceivedList();
			for(int dcb=1 ; dcb < vect_dcbroker.size(); dcb++)
				newList.addAll(vect_dcbroker.get(dcb).getCloudletReceivedList());

			Log.printLine("Received " + newList.size() + " cloudlets");

			CloudSim.stopSimulation();

			printCloudletList(newList);


			Log.printLine();

			Log.printLine(String.format("Total simulation time: %.2f sec", lastClock));
			Log.printLine(String.format("Power Sum :  %.8f W", datacenter.getPower() ));
			Log.printLine(String.format("Power Average : %.8f W", datacenter.getPower() / (lastClock*100)));
			Log.printLine(String.format("Energy consumption: %.8f Wh", (datacenter.getPower() / (lastClock*100)) * (lastClock*100 / 3600)));

			Log.printLine();


		} catch (Exception e) {
			e.printStackTrace();
			Log.printLine("Unwanted errors happen");
		}

		Log.printLine("Dvfs_example_simple finished!");
	}

	/**
	 * Creates the cloudlet list.
	 *
	 * @param userId
	 * @param nb_cloudlet : number of cloudlet we want to create
	 * @param IdShift : id of cloudlet
	 * @param type_cl : type of cloudlet related to the XML file
	 *
	 * @return the cloudlet list
	 */
	private static List<Cloudlet> createCloudletList(int userId, int nb_cloudlet, int IdShift , int type_cl) {
		List<Cloudlet> list = new ArrayList<>();

		long[] length= {5000,4486,299,2315};
		int pesNumber=1;
		long fileSize = 300;
		long outputSize = 300;
		int offset = no_cur_cloudlet;
		System.out.println("IdShift = " + IdShift );

		for (int i = no_cur_cloudlet ; i < (offset+nb_cloudlet) ; i++) 
		{


				Cloudlet cloudlet = new Cloudlet((IdShift+i), length[type_cl], pesNumber, fileSize, outputSize, new UtilizationModelFull(), new UtilizationModelFull(), new UtilizationModelFull());
				cloudlet.setUserId(userId);
				cloudlet.setVmId((IdShift+i)); 
				list.add(cloudlet);
				no_cur_cloudlet++;
				System.out.println("Cloudlet created // No de Cloudlet =  "+ no_cur_cloudlet +"  //  Cloudlet List Size = " + list.size());
		}
		return list;
	}

	public static void UpdateCloudletList(List<Cloudlet> list_)
	{
		cloudletList.addAll(list_);
	}

	/**
	 * Creates the vms.
	 *
	 * @param userId
	 * @param nb_vm : number of cloudlet we want to create
	 * @param IdShift : id of cloudlet
	 * @param type_vm : type of cloudlet related to the XML file
	 * @return the list< vm>
	 */

	private static List<Vm> createVms(int userId, int nb_vm, int IdShift ,  int type_vm) {
		List<Vm> vms = new ArrayList<>();

		// VM description
		int [] mips={50,75,150,75} ;
		int pesNumber = 1; // number of cpus
		int ram = 128; // vm memory (MB)
		long bw = 2500; // bandwidth
		long size = 2500; // image size (MB)
		String vmm= "Xen"; // VMM name

		System.out.println(no_cur_vm + "//" + nb_vm);
		int offset=no_cur_vm;
		for (int i = no_cur_vm ; i < (offset+nb_vm); i++) {
				vms.add(
					new Vm((IdShift+i), userId, mips[type_vm], pesNumber, ram, bw, size, vmm,  new CloudletSchedulerSpaceShared())
				);
				no_cur_vm++;
				System.out.println("VM created  // No VM =  "+ no_cur_vm +"  //  List Vm size = " + vms.size() + "  // ID Shift+1 = " + (IdShift+i) + "id vm = " + vms.get(0).getId());
		}

		return vms;
	}

	public static void UpdateVmList(List<Vm> list_)
	{
		vmList.addAll(list_);
	}



	/**
	 * Creates the datacenter.
	 *
	 * @param name the name
	 *
	 * @return the datacenter
	 *
	 * @throws Exception the exception
	 */
	private static PowerDatacenter createDatacenter(String name) throws Exception {

		List<PowerHost> hostList = new ArrayList<>();

		double maxPower =   250; // 250W
		double staticPowerPercent = 0.7; // 70%
		int mips=2500;;
		int ram = 10000; // host memory (MB)
		long storage = 1000000; // host storage
		int bw =300000;
		boolean enableDVFS = true; // is the Dvfs enable on the host
		ArrayList<Double> freqs = new ArrayList<>(); // frequencies available by the CPU
		freqs.add(59.925);// frequencies are defined in % , it make free to use Host MIPS like we want.
		freqs.add(69.93);  // frequencies must be in increase order !
		freqs.add(79.89);
		freqs.add(89.89);
		freqs.add(100.0);
		
		HashMap<Integer,String> govs = new HashMap<Integer,String>();  // Define wich governor is used by each CPU
		govs.put(0,"ondemand");  // CPU 1 use OnDemand Dvfs mode
		
		for (int i = 0; i < hostsNumber; i++) {

			//HostDatas tmp_host = vect_hosts.get(i);

			ConfigDvfs = new DvfsDatas();
			HashMap<String,Integer> tmp_HM_OnDemand = new HashMap<>();
			tmp_HM_OnDemand.put("up_threshold",95);
			tmp_HM_OnDemand.put("sampling_down_factor",100);
			HashMap<String,Integer> tmp_HM_Conservative = new HashMap<>();
			tmp_HM_Conservative.put("up_threshold",80);
			tmp_HM_Conservative.put("down_threshold",20);
			tmp_HM_Conservative.put("enablefreqstep",0);
			tmp_HM_Conservative.put("freqstep",5);
			HashMap<String,Integer> tmp_HM_UserSpace = new HashMap<>();
			tmp_HM_UserSpace.put("frequency",3);
			ConfigDvfs.setHashMapOnDemand(tmp_HM_OnDemand);
			ConfigDvfs.setHashMapConservative(tmp_HM_Conservative);
			ConfigDvfs.setHashMapUserSpace(tmp_HM_UserSpace);

			
			List<Pe> peList = new ArrayList<Pe>();

			int nb_pe = 1 ; 
			System.out.println("Number of CPU :  "  + nb_pe );

			
			for(int pe=0 ; pe < nb_pe ; pe++)
			{
				peList.add(new Pe(pe, new PeProvisionerSimple(mips),freqs,govs.get(pe), ConfigDvfs));
			}
			hostList.add(
				new PowerHost(
					i,
					new RamProvisionerSimple(ram),
					new BwProvisionerSimple(bw),
					storage,
					peList,
					new VmSchedulerTimeShared(peList), 
					new PowerModelSpecPower_BAZAR(peList),false,
					enableDVFS
					
				)
			); // This is our machine
		}


		String arch = "x86"; // system architecture
		String os = "Linux"; // operating system
		String vmm ="Xen";
		double time_zone = 10.0; // time zone this resource located
		double cost = 3.0; // the cost of using processing in this resource
		double costPerMem = 0.05; // the cost of using memory in this resource
		double costPerStorage =  0.001; // the cost of using storage in this resource
		double costPerBw =  0.0; // the cost of using bw in this resource

		DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
				arch, os, vmm, hostList, time_zone, cost, costPerMem, costPerStorage, costPerBw);

		// 6. Finally, we need to create a PowerDatacenter object.
		PowerDatacenter powerDatacenter = null;
		try {
			powerDatacenter = new PowerDatacenter(
					name,
					characteristics,
					new PowerVmAllocationPolicySimpleWattPerMipsMetric(hostList),	
					new LinkedList<Storage>(),
					0.01); // fix to 0.1 as the Dvfs Sampling Rate in the Linux Kernel
		} catch (Exception e) {
			e.printStackTrace();
		}

		return powerDatacenter;
	}

	
	private static PowerDatacenterBroker createBroker(String name){

		PowerDatacenterBroker broker = null;
		try {
			broker = new PowerDatacenterBroker(name);
			vect_dcbroker.add(broker);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return broker;
	}


	private static PowerDatacenterBroker createBrokerWithEvent(String name, EventPostBroker evtp){

		System.out.println("Broker Creation " + name + " with PostEvent ");

		PowerDatacenterBroker broker = null;
		try {
			broker = new PowerDatacenterBroker(name,evtp);
			vect_dcbroker.add(broker);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return broker;
	}


	/**
	 * Prints the Cloudlet objects.
	 *
	 * @param list list of Cloudlets
	 */
	private static void printCloudletList(List<Cloudlet> list) {
		int size = list.size();
		Cloudlet cloudlet;

		String indent = "\t";
		Log.printLine();
		Log.printLine("========== OUTPUT ==========");
		Log.printLine("Cloudlet ID" + indent + "STATUS" + indent
				+ "Resource ID" + indent + "VM ID" + indent + "Time" + indent
				+ "Start Time" + indent + "Finish Time");

		DecimalFormat dft = new DecimalFormat("###.##");
		for (int i = 0; i < size; i++) {
			cloudlet = list.get(i);
			Log.print(indent + cloudlet.getCloudletId());

			if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS) {
				Log.printLine(indent + "SUCCESS"
					+ indent + indent + cloudlet.getResourceId()
					+ indent + cloudlet.getVmId()
					+ indent + dft.format(cloudlet.getActualCPUTime())
					+ indent + dft.format(cloudlet.getExecStartTime())
					+ indent + indent + dft.format(cloudlet.getFinishTime())
				);
			}
		}
	}



public static class GlobalBroker extends SimEntity {

/*

This GlobalBroker class is used to create successively broker that launch one or multiple VM. Indeed, a broker can "contain" one or more VM.
One (and only one) cloudlet is attached to one VM. The reasone is if we use only one VM, this has to have to the same size as the Host
to be able to generate 100% cpu load.

In our simulation we want to generate a pre-defined scenario, with multiple predefine phase.
It's more convinient to create different type of VM and different type of Cloudlet.
By mixing these different type of VM/Cloudlet we can easaly manage the CPU load of the simulation.
The size of the VM define the grain with we are able to increase/decrease the CPU load, and the Cloudlet length determine the duration of its execution,
depend on which type of VM it is launched.


DVFS
----

This scenario is defined at Maximum frequency. So all broker is set to have the good CPU load curve at Maximum frequency.
When the CPU is decrease, the execution of VM/CloudLet could be slowed and the sequence of brokers must take into account this slowdown.
If we create broker with STATIC delay start, it could be works at Maximum frequency, but not at all for another (lower) frequency.

So, the launching of a broker could be create dynamically at the end of another broker. (Event Post Broker)

*/



		private static final int STEP_1 = 0;
		private static final int STEP_2 = 1;
		private static final int STEP_3 = 2;
		private static final int STEP_4 = 3;
		
		private List<Vm> vmList_=null;
		private List<Cloudlet> cloudletList_=null;
		private List<PowerDatacenterBroker> created_brokers=Collections.synchronizedList(new ArrayList<PowerDatacenterBroker>());
		PowerDatacenterBroker broker;
		
		int no_broker = 1;

		
		
		public GlobalBroker(String name) {
			super(name);
		}

		@Override
		public void processEvent(SimEvent ev) {
				
			List<Vm> vmList = null;	
			int type_vm;
			int type_cl;

			switch (ev.getTag()) 
			{

				case STEP_1:
					type_vm = 0;
					type_cl = 0;

					if(no_broker==21)
						setBroker(createBrokerWithEvent("Broker_"+no_broker, new EventPostBroker(getId(), STEP_2) ));
					else
						setBroker(createBroker("Broker_"+no_broker));

					System.out.println("Add a Vm and a Cloudlet... IdBroker=" + getBroker().getId());

					vmList_=createVms(getBroker().getId(), 1, (no_broker*100) , type_vm);
					UpdateVmList(vmList_);
					cloudletList_=createCloudletList(getBroker().getId(), 1, (no_broker*100),type_cl);
					UpdateCloudletList(cloudletList_);

					broker.submitVmList(getVmList());
					broker.submitCloudletList(getCloudletList());

					no_broker++;
					if(no_broker<49)				
						schedule(getId(), 1, STEP_1);
					CloudSim.resumeSimulation();

					break;


				case STEP_2:
					type_vm = 1;
					type_cl = 1;

					setBroker(createBrokerWithEvent("Broker_"+no_broker ,  new EventPostBroker(getId(), STEP_3) ));

					System.out.println("Add a Vm and a Cloudlet... IdBroker=" + getBroker().getId());

					vmList_=createVms(getBroker().getId(), 10, (no_broker*100), type_vm);
					UpdateVmList(vmList_);
					cloudletList_=createCloudletList(getBroker().getId(), 10, (no_broker*100),type_cl);
					UpdateCloudletList(cloudletList_);

					broker.submitVmList(getVmList());
					broker.submitCloudletList(getCloudletList());
					CloudSim.resumeSimulation();

					break;
				case STEP_3:
					type_vm = 2;
					type_cl = 2;
					no_broker++;

					setBroker(createBrokerWithEvent("Broker_"+no_broker ,  new EventPostBroker(getId(), STEP_4) ));
					System.out.println("Add a Vm and a Cloudlet... IdBroker=" + getBroker().getId());

					vmList_=createVms(getBroker().getId(), 16, (no_broker*100) , type_vm);
					UpdateVmList(vmList_);
					cloudletList_=createCloudletList(getBroker().getId(), 16, (no_broker*100) ,type_cl);
					UpdateCloudletList(cloudletList_);

					broker.submitVmList(getVmList());
					broker.submitCloudletList(getCloudletList());
					CloudSim.resumeSimulation();

					break;

				case STEP_4:
					type_vm = 3;
					type_cl = 3;
					no_broker++;

					setBroker(createBroker("Broker_"+no_broker));

					System.out.println("Add a Vm and a Cloudlet... IdBroker=" + getBroker().getId());

					vmList_=createVms(getBroker().getId(), 10, (no_broker*100) , type_vm);
					UpdateVmList(vmList_);
					cloudletList_=createCloudletList(getBroker().getId(), 10, (no_broker*100) ,type_cl);
					UpdateCloudletList(cloudletList_);

					broker.submitVmList(getVmList());
					broker.submitCloudletList(getCloudletList());
					CloudSim.resumeSimulation();
					break;

				default:
					Log.printLine(getName() + ": unknown event type");
					break;
				}
		}

		@Override
		public void startEntity() {
			Log.printLine("GlobalBroker is starting...");
			schedule(getId(), 0, STEP_1);
			Log.printLine("Event Saved...");
		}

		@Override
		public void shutdownEntity() {
		}

		public List<Vm> getVmList() {
			return vmList_;
		}

		protected void setVmList(List<Vm> vmList) {
			this.vmList_ = vmList;
		}

		public List<Cloudlet> getCloudletList() {
			return this.cloudletList_;
		}

		protected void setCloudletList(List<Cloudlet> cloudletList) {
			if(this.cloudletList_==null)
				this.cloudletList_=cloudletList;
			else
				this.cloudletList_.addAll(cloudletList);			
		}

		public PowerDatacenterBroker getLastBroker(int index) {
			return created_brokers.get(index);
		}
		public PowerDatacenterBroker getBroker() {
			return this.broker;
		}
		public List<PowerDatacenterBroker> getListBroker() {
			return created_brokers;
		}

		protected void setBroker(PowerDatacenterBroker broker_) {
				this.broker = broker_;
		}

}

}



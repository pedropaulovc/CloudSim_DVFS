package XML.DVFS_TLM;


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
*Simulation of the TLM parrallel MPI application
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
import org.cloudbus.cloudsim.CloudletSchedulerDynamicWorkload;
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
import org.cloudbus.cloudsim.VmSchedulerSpaceShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.core.EventPostBroker;
import org.cloudbus.cloudsim.power.PowerDatacenter;
import org.cloudbus.cloudsim.power.PowerDatacenterBroker;
import org.cloudbus.cloudsim.power.PowerHost;

import org.cloudbus.cloudsim.power.PowerVmAllocationPolicyDVFSDedicateNetworkDiskHost;

import org.cloudbus.cloudsim.power.models.PowerModelSpecPower_REIMS;
import org.cloudbus.cloudsim.power.models.PowerModelSpecPower_REIMS_Disk;
import org.cloudbus.cloudsim.power.models.PowerModelSpecPower_REIMS_Net;

import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

/**
 * An example of a heterogeneous DVFS-enabled data center: the voltage
 * and clock frequency of the CPU are adjusted ideally according
 * to current resource requirements.
 */
public class TLM_dvfs {

	private static SimulationXMLParse ConfSimu;

	private static DvfsDatas ConfigDvfs;

	private static List<Cloudlet> cloudletList;

	private static List<Vm> vmList;

	private static int user_id;

	private static int DCNumber;
	private static int hostsNumber;
	private static int vmsTotalNumber;
	private static int no_cur_vm=0;

	private static int cloudletsTotalNumber;
	private static int no_cur_cloudlet=0;

	private static int NbHostTLM = 2;
	private static int NbProcessusTLM = 2;



	private static ArrayList<DatacenterBroker> vect_dcbroker ;

	private static ArrayList<DatacenterDatas> vect_dcs ;
	private static ArrayList<HostDatas> vect_hosts ;
	private static ArrayList<VmDatas> vect_vms ;
	private static ArrayList<CloudletDatas> vect_cls ;
	/**
	 * Creates main() to run this example.
	 *
	 * @param args the args
	 */
	public static void main(String[] args) {

		Log.printLine("Starting Exeperience TLM_dvfs...");
		/* Configuration Variables*/		
		vect_dcs = new ArrayList<DatacenterDatas>();
		vect_hosts = new ArrayList<HostDatas>();
		vect_vms = new ArrayList<VmDatas>();
		vect_cls = new ArrayList<CloudletDatas>();


		try {
			
			/* XML configuration file Parsing*/
			ConfSimu = new SimulationXMLParse(System.getProperty("user.dir")+"/DVFS_Examples/XML/DVFS_TLM/Experience.xml");
			vect_dcs = ConfSimu.getArrayListDCS();
			vect_hosts = vect_dcs.get(0).getArrayListHosts();
			vect_vms = ConfSimu.getArrayListVMS();
			vect_cls = ConfSimu.getArrayListCLS();
			
			DCNumber = vect_dcs.size();
			cloudletsTotalNumber = vect_cls.size();
			hostsNumber =  vect_hosts.size();

			vmsTotalNumber = vect_vms.size();
			/*********************************/

			
			vect_dcbroker = new ArrayList<DatacenterBroker>();
			int num_user = 1; // number of cloud users
			Calendar calendar = Calendar.getInstance();
			boolean trace_flag = false; // mean trace GridSim events

			// Initialize the CloudSim library
			CloudSim.init(num_user, calendar, trace_flag);

			// Second step: Create Datacenters
			// Datacenters are the resource providers in CloudSim. We need at
			// list one of them to run a CloudSim simulation


			PowerDatacenter datacenter = createDatacenter("Datacenter_0");
			datacenter.setDisableMigrations(true);
			
			//creation of GlobalBroker
			GlobalBroker globalBroker = new GlobalBroker("GlobalBroker");

			vmList = new ArrayList<Vm>();
			cloudletList = new ArrayList<Cloudlet>();


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

			Log.printLine(String.format("Total simulation time: %.4f sec", lastClock));
			Log.printLine(String.format("Power Sum :  %.4f W", datacenter.getPower() ));
			Log.printLine(String.format("Energy consumption: %.4f Wh", (datacenter.getPower() / (lastClock*100)) * (lastClock*100 / 3600)));

			Log.printLine();


		} catch (Exception e) {
			e.printStackTrace();
			Log.printLine("Unwanted errors happen");
		}

		Log.printLine("TLM_dvfs Experience finished!");
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
	private static List<Cloudlet> createCloudletList(int userId, int nb_cloudlet, int IdShift, int type_cl) {
		List<Cloudlet> list = new ArrayList<Cloudlet>();

		long length;
		int pesNumber;
		long fileSize;
		long outputSize;
		int offset = no_cur_cloudlet;
	//	System.out.println("IdShift = " + IdShift );
		
		int CloudletID;

		for (int i = no_cur_cloudlet ; i < (offset+nb_cloudlet) ; i++) 
		{
				CloudletDatas tmp_cl = vect_cls.get(type_cl);

				length = tmp_cl.getLength();
				pesNumber = tmp_cl.getPes();
				fileSize = tmp_cl.getFilesize();
				outputSize = tmp_cl.getOutputsize();

				CloudletID=IdShift+list.size()+(type_cl*NbProcessusTLM);	

				Cloudlet cloudlet = new Cloudlet(CloudletID, length, pesNumber, fileSize, outputSize, new UtilizationModelFull(), new UtilizationModelFull(), new UtilizationModelFull());
				cloudlet.setUserId(userId);
				cloudlet.setVmId(CloudletID); 
				list.add(cloudlet);
				no_cur_cloudlet++;
			//	System.out.println("Cloudlet created // No de Cloudlet =  "+ CloudletID +"  //  Cloudlet List Size = " + list.size());
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

	private static List<Vm> createVms(int userId, int nb_vm, int IdShift, int type_vm) {
		List<Vm> vms = new ArrayList<Vm>();

		VmDatas tmp_vm = vect_vms.get(type_vm);


		// VM description
		int mips ;
		int pesNumber;
		int ram ;
		long bw;
		long size ;
		String vmm;

		System.out.println(no_cur_vm + "//" + nb_vm);
		int offset=no_cur_vm;

		int VmID;
		for (int i = no_cur_vm ; i < (offset+nb_vm); i++) {
				mips = tmp_vm.getMips();
				pesNumber = tmp_vm.getCpu();
				ram=tmp_vm.getRam();
				bw=tmp_vm.getBw();
				size=tmp_vm.getSize();
				vmm=tmp_vm.getVmm();

				VmID=IdShift+vms.size()+(type_vm*NbProcessusTLM);

				vms.add(new Vm(VmID, userId, mips, pesNumber, ram, bw, size, vmm,  new CloudletSchedulerSpaceShared()));
				no_cur_vm++;
				//System.out.println("VM created  // No VM =  "+ VmID +"  //  List Vm size = " + vms.size() + "  // ID Shift+1 = " + (IdShift+i) + "id vm = " + vms.get(0).getId());
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

		List<PowerHost> hostList = new ArrayList<PowerHost>();

		double maxPower;
		double staticPowerPercent;

		int mips;
		int ram ;
		long storage;
		int bw ;

		boolean enableDVFS; // is the Dvfs enable on the host
		boolean enableONOFF; // is the OnOff technique is enable on the host
		ArrayList<Double> freqs ; // frequencies available by the CPU
		HashMap<Integer,String> govs;  // Definition of Dvfs Governor , and redefine specifics values
		
		for (int i = 0; i < NbHostTLM; i++) {
			HostDatas tmp_host = vect_hosts.get(0); // Host type 0 used for CPU.
			ConfigDvfs = tmp_host.getDvfsDatas();
			maxPower = tmp_host.getMaxP();
			staticPowerPercent = tmp_host.getStaticPP();
			mips = tmp_host.getMips();
			ram = tmp_host.getRam();
			storage = tmp_host.getStorage();
			bw = tmp_host.getBw();
			freqs = tmp_host.getCpuFrequencies();
			govs = tmp_host.getHTGovs();
			enableONOFF = tmp_host.isOnoffEnable();
			enableDVFS = tmp_host.isDvfsEnable();

			List<Pe> peList = new ArrayList<Pe>();

			int nb_pe = tmp_host.getCpus();
			
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
					new PowerModelSpecPower_REIMS(peList),
					enableONOFF,
					enableDVFS
		
				)
			);
		}


		//NET host creation
			HostDatas tmp_host = vect_hosts.get(1); // Host type 1 used for NET
			ConfigDvfs = tmp_host.getDvfsDatas();
			maxPower = tmp_host.getMaxP();
			staticPowerPercent = tmp_host.getStaticPP();
			mips = tmp_host.getMips();
			ram = tmp_host.getRam();
			storage = tmp_host.getStorage();
			bw = tmp_host.getBw();
			freqs = tmp_host.getCpuFrequencies();
			govs = tmp_host.getHTGovs();
			enableDVFS = tmp_host.isDvfsEnable();
			enableONOFF = tmp_host.isOnoffEnable();

			List<Pe> peList = new ArrayList<Pe>();

			int nb_pe = tmp_host.getCpus();
			
			for(int pe=0 ; pe < nb_pe ; pe++)
			{
				peList.add(new Pe(pe, new PeProvisionerSimple(mips),freqs,govs.get(pe), ConfigDvfs));
			}
			hostList.add(
				new PowerHost(
					NbHostTLM,
					new RamProvisionerSimple(ram),
					new BwProvisionerSimple(bw),
					storage,
					peList,
					new VmSchedulerTimeShared(peList), 
					new PowerModelSpecPower_REIMS_Net(peList),
					enableONOFF,
					enableDVFS
		
				)
			);

		//Host DISK creation
		for (int i = 0; i < NbHostTLM; i++) {
			tmp_host = vect_hosts.get(2); // Host type 2 used for DISK.
			ConfigDvfs = tmp_host.getDvfsDatas();
			maxPower = tmp_host.getMaxP();
			staticPowerPercent = tmp_host.getStaticPP();
			mips = tmp_host.getMips();
			ram = tmp_host.getRam();
			storage = tmp_host.getStorage();
			bw = tmp_host.getBw();
			freqs = tmp_host.getCpuFrequencies();
			govs = tmp_host.getHTGovs();
			enableDVFS = tmp_host.isDvfsEnable();
			enableONOFF = tmp_host.isOnoffEnable();

			peList = new ArrayList<Pe>();

			nb_pe = tmp_host.getCpus();
			
			for(int pe=0 ; pe < nb_pe ; pe++)
			{
				peList.add(new Pe(pe, new PeProvisionerSimple(mips),freqs,govs.get(pe), ConfigDvfs));
			}
			hostList.add(
				new PowerHost(
					NbHostTLM+1,
					new RamProvisionerSimple(ram),
					new BwProvisionerSimple(bw),
					storage,
					peList,
					new VmSchedulerTimeShared(peList), 
					new PowerModelSpecPower_REIMS_Disk(peList),
					enableONOFF,
					enableDVFS
		
				)
			);
		}


		DatacenterDatas tmp_dc = vect_dcs.get(0);
		String arch = tmp_dc.getArch();         // "x86"; // system architecture
		String os = tmp_dc.getOs();         //"Linux"; // operating system
		String vmm = tmp_dc.getVmm();         //"Xen";
		double time_zone = tmp_dc.getTimezone();         //10.0; // time zone this resource located
		double cost = tmp_dc.getCost();         //3.0; // the cost of using processing in this resource
		double costPerMem = tmp_dc.getCostPerMem();         //0.05; // the cost of using memory in this resource
		double costPerStorage = tmp_dc.getCostPerStorage();      //   0.001; // the cost of using storage in this resource
		double costPerBw = tmp_dc.getCostPerbW();         // 0.0; // the cost of using bw in this resource

		DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
				arch, os, vmm, hostList, time_zone, cost, costPerMem, costPerStorage, costPerBw);

		// 6. Finally, we need to create a PowerDatacenter object.
		PowerDatacenter powerDatacenter = null;
		try {
			powerDatacenter = new PowerDatacenter(
					name,
					characteristics,
					new PowerVmAllocationPolicyDVFSDedicateNetworkDiskHost(hostList),
					new LinkedList<Storage>(),
					0.01); // fix to 0.01 as the Dvfs Sampling Rate in the Linux Kernel
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

	//	System.out.println("Broker Creation " + name + " with PostEvent ");

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


		private static final int STEP_1 = 0;
		private static final int STEP_2 = 1;
		private static final int STEP_3 = 2;
		private static final int STEP_4 = 3;
		
		private List<Vm> vmList_=null;
		private List<Cloudlet> cloudletList_=null;
		private List<PowerDatacenterBroker> created_brokers=Collections.synchronizedList(new ArrayList<PowerDatacenterBroker>());
		PowerDatacenterBroker broker;
		
		int no_broker = 0;
		
		private static int nbIteration = 26;  
		private static int noIteration = 0;
		
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

					setBroker(createBrokerWithEvent("Broker_"+no_broker, new EventPostBroker(getId(), STEP_2) ));

					vmList_=createVms(getBroker().getId(), NbProcessusTLM, (no_broker*100), type_vm);
					UpdateVmList(vmList_);
					cloudletList_=createCloudletList(getBroker().getId(), NbProcessusTLM, (no_broker*100),type_cl);
					UpdateCloudletList(cloudletList_);

					broker.submitVmList(getVmList());
					broker.submitCloudletList(getCloudletList());

					
					CloudSim.resumeSimulation();

					break;


				case STEP_2:
					type_vm = 1;
					type_cl = 1;
				
					setBroker(createBrokerWithEvent("Broker_"+no_broker ,  new EventPostBroker(getId(), STEP_3) ));		
					vmList_=createVms(getBroker().getId(), NbProcessusTLM, (no_broker*100)+10, type_vm); // Network
					UpdateVmList(vmList_);
					cloudletList_=createCloudletList(getBroker().getId(), NbProcessusTLM, (no_broker*100)+10,type_cl); // Network
					

					broker.submitVmList(getVmList());
					broker.submitCloudletList(getCloudletList());

					CloudSim.resumeSimulation();

					break;
				case STEP_3:
					type_vm = 2;
					type_cl = 2;
						if(noIteration<nbIteration-1)
							setBroker(createBrokerWithEvent("Broker_"+no_broker ,  new EventPostBroker(getId(), STEP_1) ));
						else
							setBroker(createBroker("Broker_"+no_broker));
					vmList_ = createVms(getBroker().getId(), NbProcessusTLM, (no_broker*100)+20, type_vm); // Disk

					cloudletList_ = createCloudletList(getBroker().getId(), NbProcessusTLM, (no_broker*100)+20,type_cl); // Disk

					UpdateCloudletList(cloudletList_);
					UpdateVmList(vmList_);			

					broker.submitVmList(getVmList());
					broker.submitCloudletList(getCloudletList());

					no_broker++;
					noIteration++;
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



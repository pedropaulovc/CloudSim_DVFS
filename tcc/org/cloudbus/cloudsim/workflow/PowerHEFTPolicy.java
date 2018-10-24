package org.cloudbus.cloudsim.workflow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.power.models.PowerModelSpecPower_REIMS;

public class PowerHEFTPolicy extends Policy {
	private Map<Task, Map<Vm, Double>> computationCosts;
	private Map<Task, Map<Task, Double>> transferCosts;
	private Map<Task, Double> rank;

	private Map<Vm, List<Event>> schedules;
	private Map<Task, Double> earliestFinishTimes;

	private double averageBandwidth;

	private List<Vm> vmList;
	private Map<Integer, Vm> idToVM;
	private int nextVMId = 0;
	private List<Vm> sortedVmOffers;
	private Map<Vm, Integer> indexFreq;
	private long availableExecTime;

	private class Event {
		public double start;
		public double finish;
		public Task task;

		public Event(double start, double finish, Task task) {
			this.start = start;
			this.finish = finish;
			this.task = task;
		}
	}

	private class TaskRank implements Comparable<TaskRank> {
		public Task task;

		public TaskRank(Task task) {
			this.task = task;
		}

		@Override
		public int compareTo(TaskRank o) {
			return rank.get(task).compareTo(rank.get(o.task));
		}

		public String toString() {
			return task.toString();
		}
	}

	public PowerHEFTPolicy() {
		computationCosts = new HashMap<>();
		transferCosts = new HashMap<>();
		rank = new HashMap<>();
		earliestFinishTimes = new HashMap<>();
		schedules = new HashMap<>();
		vmList = new ArrayList<>();
		indexFreq = new HashMap<>();
		idToVM = new HashMap<>();
	}

	@Override
	public void doScheduling(long availableExecTime, VMOffers vmOffers) {
		//Log.printLine("Power HEFT planner running with " + tasks.size() + " tasks.");

		//Log.printLine("== Sorting the the VM options by MIPS ==");
		sortedVmOffers = sortVmOfferList();
		this.availableExecTime = availableExecTime;

		//Log.printLine("== Instantiating the first VM, the fastest ==");
		instantiateVM(sortedVmOffers.get(sortedVmOffers.size() - 1), sortedVmOffers.size() - 1); // Fastest

		averageBandwidth = calculateAverageBandwidth();

		// Prioritization phase
		//Log.printLine("== Calculating the HEFT parameters, computation and transfer costs ==");
		updateComputationCosts();
		updateTransferCosts();
		updateRanks();

		List<TaskRank> taskRank = new ArrayList<>();
		for (Task task : rank.keySet()) {
			taskRank.add(new TaskRank(task));
		}

		Task next;
		while (taskRank.size() > 0) {
			//Log.printLine("== We have " + taskRank.size() + " tasks yet to schedule ==");
			
			// Sorting in non-DESCENDING order of rank
			Collections.sort(taskRank);
			//Log.printLine("taskRank: " + taskRank);

			Vm bestVm = vmList.get(0);
			boolean isNewVm = false;
			double bestEnergy = Double.MAX_VALUE;
			int bestIndex = -1;
			
			next = taskRank.remove(taskRank.size() - 1).task;
			
			//Log.printLine("== Trying to schedule " + next + " ... ==");
			for (Vm vm : vmList) {
				//Log.print("=== Trying to schedule " + next + " in " + vm + "... ");
				
				allocateTask(next, vm);
				double energy = simulateAllocation(new ArrayList<>(taskRank));
				deallocateTask(next);
				
				//Log.printLine("Energy = " + energy + " ===");
				if (energy < bestEnergy) {
					//Log.printLine("=== The best VM to schedule is now " + vm + " ===");
					bestVm = vm;
					bestEnergy = energy;
				}
			}
			taskRank.add(new TaskRank(next));
			
			//Log.printLine("== Trying to schedule " + next + " in a new VM ---- mips "+next.getExecTime()+"==");
			for (int i = 0; i < sortedVmOffers.size(); i++) {
				//Log.print("== Trying option #" + i + " ... ");
				Vm option = sortedVmOffers.get(i);

				Vm newVm = instantiateVM(option, i);

				updateComputationCosts();
				updateRanks();
				Collections.sort(taskRank);

				next = taskRank.remove(taskRank.size() - 1).task;

				allocateTask(next, newVm);

				double energy = simulateAllocation(new ArrayList<>(taskRank));

				//Log.printLine("Energy = " + energy + " ===");
				
				deallocateTask(next);
				deinstantiateVM(newVm);
				taskRank.add(new TaskRank(next));

				if (energy < bestEnergy) {
					//Log.printLine("=== The best VM to schedule is a new one (Option #" + i + " ===");
					bestVm = option;
					bestIndex = i;
					bestEnergy = energy;
					isNewVm = true;
				}
			}

			if (isNewVm) {
				//Log.printLine("=== Instantiating a new VM ===");
				bestVm = instantiateVM(bestVm, bestIndex);

				updateComputationCosts();
				updateTransferCosts();
				Collections.sort(taskRank);
			}

			next = taskRank.remove(taskRank.size() - 1).task;
			//Log.printLine("=== Allocating " + next + " definitely in " + bestVm + " ===");
			allocateTask(next, bestVm);
		}// End for

	//	Log.printLine("=== Setting up the resultant data dependencies ===");
		setDataDependencies();

	}

	private Vm instantiateVM(Vm instance, int vmIndexFreq) {
//		Log.printLine("instantiateVM(" + instance + ", " + vmIndexFreq + ")");

		Vm newVm = new Vm(nextVMId++, ownerId, instance.getMips(), instance.getNumberOfPes(),
				instance.getRam(), instance.getBw(), instance.getSize(), "",
				new CloudletSchedulerTimeShared());
		int cost = vmOffers.getCost(newVm.getMips(), newVm.getRam(), newVm.getBw());

		provisioningInfo.add(new ProvisionedVm(newVm, 0, availableExecTime, cost));

		schedulingTable.put(newVm.getId(), new ArrayList<Task>());
		schedules.put(newVm, new ArrayList<Event>());
		vmList.add(newVm);
		idToVM.put(newVm.getId(), newVm);
		indexFreq.put(newVm, vmIndexFreq);
		return newVm;
	}

	private void deinstantiateVM(Vm vm) {
//		Log.printLine("deinstantiateVM(" + vm + ")");

		int i = 0;
		while (provisioningInfo.get(i).getVm().getId() != vm.getId())
			i++;

		schedulingTable.remove(vm.getId());
		schedules.remove(vm);
		provisioningInfo.remove(i);
		vmList.remove(vm);
		idToVM.remove(vm.getId());
		indexFreq.remove(vm);
	}

	/**
	 * Calculates the average available bandwidth among all VMs in Mbit/s
	 * 
	 * @return Average available bandwidth in Mbit/s
	 */
	private double calculateAverageBandwidth() {
//		Log.printLine("calculateAverageBandwidth()");

		double avg = 0.0;
		for (Vm vm : vmList) {
			avg += vm.getBw();
		}

		//Log.printLine("averageBandwidth == " + avg / vmList.size());
		return avg / vmList.size();
	}

	/**
	 * Populates the computationCosts field with the time in seconds to compute
	 * a task in a vm.
	 */
	private void updateComputationCosts() {
//		Log.printLine("updateComputationCosts()");

		Map<Vm, Double> costsVm;
		for (Task task : tasks) {
			if (computationCosts.containsKey(task))
				costsVm = computationCosts.get(task);
			else
				costsVm = new HashMap<Vm, Double>();

			for (Vm vm : vmList) {
				if (vm.getNumberOfPes() < task.getCloudlet().getNumberOfPes()) {
					costsVm.put(vm, Double.MAX_VALUE);
				} else {
					costsVm.put(vm, task.getCloudlet().getCloudletTotalLength() / vm.getMips());
				}
			}
			computationCosts.put(task, costsVm);
		}

		// for(Task t: tasks){
		// for (Vm vm: vmList){
		// System.out.println(t + ", " + vm + ", " +
		// computationCosts.get(t).get(vm));
		// }
		// }
	}

	/**
	 * Populates the transferCosts map with the time in seconds to transfer all
	 * files from each parent to each child
	 */
	private void updateTransferCosts() {
//		Log.printLine("updateTransferCosts()");

		Map<Task, Double> taskTransferCosts;
		// Initializing the matrix
		for (Task task1 : tasks) {
			if (transferCosts.containsKey(task1))
				taskTransferCosts = transferCosts.get(task1);
			else
				taskTransferCosts = new HashMap<Task, Double>();

			for (Task task2 : tasks) {
				taskTransferCosts.put(task2, 0.0);
			}

			transferCosts.put(task1, taskTransferCosts);
		}

		// Calculating the actual values
		for (Task parent : tasks) {
			for (Task child : parent.getChildren()) {
				transferCosts.get(parent).put(child, calculateTransferCost(parent, child));
			}
		}

		// for(Task t1: tasks){
		// for(Task t2:tasks){
		// if(transferCosts.get(t1).get(t2) != 0.0)
		// System.out.println(t1 + ", " + t2 + ", " +
		// transferCosts.get(t1).get(t2));
		// }
		// }
	}

	/**
	 * Accounts the time in seconds necessary to transfer all files described
	 * between parent and child
	 * 
	 * @param parent
	 * @param child
	 * @return Transfer cost in seconds
	 */
	private double calculateTransferCost(Task parent, Task child) {
		// Log.printLine("calculateTransferCost(" + parent + ", " + child +
		// ")");

		List<DataItem> parentOutput = (List<DataItem>) parent.getOutput();
		List<DataItem> childInput = (List<DataItem>) child.getDataDependencies();

		double acc = 0.0;

		for (DataItem parentFile : parentOutput) {
			for (DataItem childFile : childInput) {
				if (childFile.getName().equals(parentFile.getName())) {
					acc += childFile.getSize();
					break;
				}
			}
		}

		// acc in MB, averageBandwidth in Mb/s
		return acc * 8 / averageBandwidth;
	}

	/**
	 * Invokes calculateRank for each task to be scheduled
	 */
	private void updateRanks() {
//		Log.printLine("updateRanks()");

		rank.clear();
		for (Task task : tasks) {
			calculateRank(task);
		}
	}

	/**
	 * Populates rank.get(task) with the rank of task as defined in the HEFT
	 * paper.
	 * 
	 * @param task
	 *            The task have the rank calculates
	 * @return The rank
	 */
	private double calculateRank(Task task) {
		// Log.printLine("calculateRank(" + task + ")");

		if (rank.containsKey(task))
			return rank.get(task);

		double averageComputationCost = 0.0;

		for (Double cost : computationCosts.get(task).values())
			averageComputationCost += cost;

		averageComputationCost /= computationCosts.get(task).size();

		double max = 0.0;
		for (Task child : task.getChildren()) {
			double childCost = transferCosts.get(task).get(child) + calculateRank(child);
			max = Math.max(max, childCost);
		}

		rank.put(task, averageComputationCost + max);

		return rank.get(task);
	}

	private Double simulateAllocation(List<TaskRank> toSchedule) {
//		Log.printLine("simulateAllocation(" + toSchedule + ")");

		Task next;
		for (int i = toSchedule.size() - 1; i >= 0; i--) {
			next = toSchedule.get(i).task;

			allocateTask(next);
		}

		double energy = estimateEnergyConsumption();

		for (TaskRank tr : toSchedule) {
			deallocateTask(tr.task);
		}

		return energy;
	}

	private void deallocateTask(Task task) {
//		Log.printLine("deallocateTask(" + task + ")");

		Vm vm = idToVM.get(task.getVmId());
		List<Event> schedule = schedules.get(vm);
		int i = 0;
		while (schedule.get(i).task.getId() != task.getId())
			i++;

		schedule.remove(i);

		schedulingTable.get(vm.getId()).remove(task);

		earliestFinishTimes.remove(task);
		task.setVmId(-1);
		task.setOptIndexFreq(-1);
	}

	private double estimateEnergyConsumption() {
//		Log.printLine("estimateEnergyConsumption()");

		int vmIndexFreq;
		long length = 0;
		double energy = 0.0;
		double execTime;
		double powerFull, powerIdle;
		PowerModelSpecPower_REIMS model = new PowerModelSpecPower_REIMS();

		for (Vm vm : schedules.keySet()) {
			vmIndexFreq = indexFreq.get(vm);

			for (Event e : schedules.get(vm)) {
				length += e.task.getCloudlet().getCloudletTotalLength();
			}

			execTime = length / vm.getMips();

			powerFull = model.getPMax(vmIndexFreq);
			powerIdle = model.getPMin(vmIndexFreq);

			double E = execTime * powerFull + (availableExecTime - execTime) * powerIdle;
			energy += E / 3600;
		}

		return energy;
	}

	private void allocateTask(Task task) {
//		Log.printLine("allocateTask(" + task + ")");

		Vm chosenVM = null;
		double earliestFinishTime = Double.MAX_VALUE;
		double bestReadyTime = 0.0;
		double finishTime;

		for (Vm vm : vmList) {
			double minReadyTime = 0.0;

			for (Task parent : task.getParents()) {
				double readyTime = earliestFinishTimes.get(parent);
				if (parent.getVmId() != vm.getId())
					readyTime += transferCosts.get(parent).get(task);

				minReadyTime = Math.max(minReadyTime, readyTime);
			}

			finishTime = findFinishTime(task, vm, minReadyTime, false);

			if (finishTime < earliestFinishTime) {
				bestReadyTime = minReadyTime;
				earliestFinishTime = finishTime;
				chosenVM = vm;
			}
		}

		findFinishTime(task, chosenVM, bestReadyTime, true);
		earliestFinishTimes.put(task, earliestFinishTime);

		task.setVmId(chosenVM.getId());
		task.setOptIndexFreq(indexFreq.get(chosenVM));
	}

	/**
	 * Schedules the task given in one of the VMs minimizing the earliest finish
	 * time
	 * 
	 * @param task
	 *            The task to be scheduled
	 * @pre All parent tasks are already scheduled
	 */
	private void allocateTask(Task task, Vm vm) {
//		Log.printLine("allocateTask(" + task + ", " + vm + ")");

		double finishTime;
		double minReadyTime = 0.0;

		for (Task parent : task.getParents()) {
			double readyTime = earliestFinishTimes.get(parent);
			if (parent.getVmId() != vm.getId())
				readyTime += transferCosts.get(parent).get(task);

			minReadyTime = Math.max(minReadyTime, readyTime);
		}

		finishTime = findFinishTime(task, vm, minReadyTime, true);

		earliestFinishTimes.put(task, finishTime);

		task.setVmId(vm.getId());
		task.setOptIndexFreq(indexFreq.get(vm));
	}

	/**
	 * Finds the best time slot available to minimize the finish time of the
	 * given task in the vm with the constraint of not scheduling it before
	 * readyTime. If occupySlot is true, reserves the time slot in the schedule.
	 * 
	 * @param task
	 *            The task to have the time slot reserved
	 * @param vm
	 *            The vm that will execute the task
	 * @param readyTime
	 *            The first moment that the task is available to be scheduled
	 * @param occupySlot
	 *            If true, reserves the time slot in the schedule.
	 * @return The minimal finish time of the task in the vmn
	 */
	private double findFinishTime(Task task, Vm vm, double readyTime, boolean occupySlot) {
//		Log.printLine("findFinishTime(" + task + ", " + vm + ", " + readyTime + ", " + occupySlot
//				+ ")");

		List<Event> sched = schedules.get(vm);
		double computationCost = computationCosts.get(task).get(vm);
		double start, finish;
		int pos;

		if (sched.size() == 0) {
			if (occupySlot) {
				sched.add(new Event(readyTime, readyTime + computationCost, task));
				schedulingTable.get(vm.getId()).add(task);
			}
			return readyTime + computationCost;
		}

		if (sched.size() == 1) {
			if (readyTime >= sched.get(0).finish) {
				pos = 1;
				start = readyTime;
			} else if (readyTime + computationCost <= sched.get(0).start) {
				pos = 0;
				start = readyTime;
			} else {
				pos = 1;
				start = sched.get(0).finish;
			}

			if (occupySlot) {
				sched.add(pos, new Event(start, start + computationCost, task));
				schedulingTable.get(vm.getId()).add(task);
			}
			return start + computationCost;
		}

		// Trivial case: Start after the latest task scheduled
		start = Math.max(readyTime, sched.get(sched.size() - 1).finish);
		finish = start + computationCost;
		int i = sched.size() - 1;
		int j = sched.size() - 2;
		pos = i + 1;
		while (j >= 0) {
			Event current = sched.get(i);
			Event previous = sched.get(j);

			if (readyTime > previous.finish) {
				if (readyTime + computationCost <= current.start) {
					start = readyTime;
					finish = readyTime + computationCost;
				}

				break;
			}

			if (previous.finish + computationCost <= current.start) {
				start = previous.finish;
				finish = previous.finish + computationCost;
				pos = i;
			}

			i--;
			j--;
		}

		if (readyTime + computationCost <= sched.get(0).start) {
			pos = 0;
			start = readyTime;

			if (occupySlot) {
				sched.add(pos, new Event(start, start + computationCost, task));
				schedulingTable.get(vm.getId()).add(task);
			}
			return start + computationCost;
		}
		if (occupySlot) {
			sched.add(pos, new Event(start, finish, task));
			schedulingTable.get(vm.getId()).add(task);
		}
		return finish;
	}

	private void setDataDependencies() {
//		Log.printLine("setDataDependencies");

		for (Task t : tasks) {
			for (DataItem data : t.getDataDependencies()) {
				if (!dataRequiredLocation.containsKey(data.getId())) {
					dataRequiredLocation.put(data.getId(), new HashSet<Integer>());
				}
				dataRequiredLocation.get(data.getId()).add(t.getVmId());
			}

			for (DataItem data : t.getOutput()) {
				if (!dataRequiredLocation.containsKey(data.getId())) {
					dataRequiredLocation.put(data.getId(), new HashSet<Integer>());
				}
			}
		}
	}

	private List<Vm> sortVmOfferList() {
//		Log.printLine("sortVMOfferList()");

		LinkedList<Vm> offers = new LinkedList<Vm>();

		// sorts offers
		LinkedList<Entry<Vm, Integer>> tempList = new LinkedList<Entry<Vm, Integer>>();
		Hashtable<Vm, Integer> table = vmOffers.getVmOffers();

		Iterator<Entry<Vm, Integer>> iter = table.entrySet().iterator();
		while (iter.hasNext()) {
			tempList.add(iter.next());
		}
		Collections.sort(tempList, new OffersComparator());
		for (Entry<Vm, Integer> entry : tempList) {
			offers.add(entry.getKey());
		}

		System.out.println("***********************************************");
		for (Vm vm : offers) {
			System.out.println("** Vm memory:" + vm.getRam() + " vm mips:" + vm.getMips()
					+ " vm price:" + table.get(vm));
		}
		System.out.println("***********************************************");

		return offers;
	}

}

package org.cloudbus.cloudsim.workflow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;

public class PowerHEFTPolicy extends Policy {
	private Map<Task, Map<Vm, Double>> computationCosts;
	private Map<Task, Map<Task, Double>> transferCosts;
	private Map<Task, Double> rank;

	private Map<Vm, List<Event>> schedules;
	private Map<Task, Double> earliestFinishTimes;

	private double averageBandwidth;

	private List<Vm> vmList;
	private int nextVMId = 0;
	private List<Vm> sortedVmOffers;

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
	}

	public PowerHEFTPolicy() {
		computationCosts = new HashMap<>();
		transferCosts = new HashMap<>();
		rank = new HashMap<>();
		earliestFinishTimes = new HashMap<>();
		schedules = new HashMap<>();
		vmList = new ArrayList<>();
	}

	@Override
	public void doScheduling(long availableExecTime, VMOffers vmOffers) {
		Log.printLine("Power HEFT planner running with " + tasks.size() + " tasks.");

		averageBandwidth = calculateAverageBandwidth();

		// VM instantiation phase
		instantiateVM(sortedVmOffers.get(sortedVmOffers.size() - 1), availableExecTime); // Fastest

		// Prioritization phase
		updateComputationCosts();
		updateTransferCosts();
		updateRanks();

		List<TaskRank> taskRank = new ArrayList<>();
		for (Task task : rank.keySet()) {
			taskRank.add(new TaskRank(task));
		}
		
		Task next;
		while(taskRank.size() > 0){
			// Sorting in non-DESCENDING order of rank
			Collections.sort(taskRank);
			
			next = taskRank.remove(taskRank.size() - 1).task;
			Vm bestVm = vmList.get(0);
			boolean isNewVm = false;
			double bestEnergy = Double.MAX_VALUE;
			
			for(Vm vm: vmList){
				allocateTask(next, vm);
				double energy = simulateAllocation(new ArrayList<>(taskRank));
				//deallocateTask(next, vm);
				taskRank.add(new TaskRank(next));
				
//				if(energy < bestEnergy){
//					bestVm = vm;
//					bestEnergy = energy;
//				}
			}
			
			for(Vm option: sortedVmOffers){
				Vm newVm = instantiateVM(option, availableExecTime);
				updateComputationCosts();
				updateTransferCosts();
				Collections.sort(taskRank);
				next = taskRank.remove(taskRank.size() - 1).task;
				allocateTask(next, newVm);
				//double energy = simulateAllocation();
				//deallocateTask(next, newVm);
				//deinstantiateVM(newVm);
				//taskRank.add(new TaskRank(next));
				
//				if(energy < bestEnergy){
//					bestVm = option;
//					bestEnergy = energy;
//					isNewVm = true;
//				}
			}
			
			if(isNewVm){
				Vm newVm = instantiateVM(bestVm, availableExecTime);
				updateComputationCosts();
				updateTransferCosts();
				Collections.sort(taskRank);
				next = taskRank.remove(taskRank.size() - 1).task;
				allocateTask(next, newVm);
			}
		}

	}

	private Vm instantiateVM(Vm instance, long availableExecTime) {
		Vm newVm = new Vm(nextVMId++, ownerId, instance.getMips(), instance.getNumberOfPes(),
				instance.getRam(), instance.getBw(), instance.getSize(), "",
				new CloudletSchedulerTimeShared());
		int cost = vmOffers.getCost(newVm.getMips(), newVm.getRam(), newVm.getBw());

		provisioningInfo.add(new ProvisionedVm(newVm, 0, availableExecTime, cost));

		schedulingTable.put(newVm.getId(), new ArrayList<Task>());
		vmList.add(newVm);
		return newVm;
	}

	private int calculateNumberOfPaths(Task task, Map<Task, Integer> memory) {
		int numberPaths = 0;

		if (memory.containsKey(task))
			return memory.get(task);

		if (task.getChildren().size() == 0)
			numberPaths = 1;

		for (Task child : task.getChildren())
			numberPaths += calculateNumberOfPaths(child, memory);

		memory.put(task, numberPaths);
		return numberPaths;
	}

	/**
	 * Calculates the average available bandwidth among all VMs in Mbit/s
	 * 
	 * @return Average available bandwidth in Mbit/s
	 */
	private double calculateAverageBandwidth() {
		double avg = 0.0;
		for (Vm vm : vmList) {
			avg += vm.getBw();
		}
		return avg / vmList.size();
	}

	/**
	 * Populates the computationCosts field with the time in seconds to compute
	 * a task in a vm.
	 */
	private void updateComputationCosts() {
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
	}

	/**
	 * Populates the transferCosts map with the time in seconds to transfer all
	 * files from each parent to each child
	 */
	private void updateTransferCosts() {
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

	/**
	 * Allocates all tasks to be scheduled in non-ascending order of schedule.
	 */
	private Double simulateAllocation(List<TaskRank> toSchedule) {
		Task next;
		for(int i = toSchedule.size() - 1; i >= 0; i--){
			next = toSchedule.get(i).task;
			
			allocateTask(next);
		}
		
//		double energy = estimateEnergyConsumption();
		
		for(TaskRank tr : toSchedule){
//			deallocateTask(tr.task);
		}
		
		return 0.0;
	}

	private void allocateTask(Task task) {
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
		List<Event> sched = schedules.get(vm);
		double computationCost = computationCosts.get(task).get(vm);
		double start, finish;
		int pos;

		if (sched.size() == 0) {
			if (occupySlot)
				sched.add(new Event(readyTime, readyTime + computationCost, task));
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

			if (occupySlot)
				sched.add(pos, new Event(start, start + computationCost, task));
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

			if (occupySlot)
				sched.add(pos, new Event(start, start + computationCost, task));
			return start + computationCost;
		}
		if (occupySlot)
			sched.add(pos, new Event(start, finish, task));
		return finish;
	}

	private List<Vm> sortVmOfferList() {
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

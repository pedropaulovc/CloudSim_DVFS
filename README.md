CloudSim_DVFS
=============

Experiments with the DVFS implementation of CloudSim

The code was provided by: http://www.cloudbus.org/cloudsim/.
This simulator was created by Tom Guerout et al.:
Tom Guerout, Thierry Monteil, Georges Da Costa, Rodrigo N. Calheiros, Rajkumar Buyya, Mihai Alexandru. Energy-aware simulation with DVFS. Simulation Modelling Practice and Theory, Volume 39, pages 76-91, December 2013.

We extended the algorithms for energy-aware scheduling and presented the results in:
Watanabe, E.N.; Campos, P.P.V.; Braghetto, K.R.; Macedo Batista, D., "Energy Saving Algorithms for Workflow Scheduling in Cloud Computing", The 2014 Brazilian Symposium on Computer Networks and Distributed Systems (SBRC) , pp.9,16, 5-9 May 2014.

Instructions to run the simulations:

1. You can import the code for a java project. We used the Eclipse IDE.
2. Select the project
3. Go in the menu: File > Properties > Java Build Path (or go to the context menu > properties)
4. In the tab Source, check if the folders src and tcc are included. 
5. In the tab Libraries, check if all jar files in the folder lib are included too.
6. Close the properties window.
7. In the src folder, go to the package org.cloudbus.cloudsim.workflow and open the Simulation.java file.
8. Change the line 87, passing as an argument the name of a valid directory + the name of the file where the data will be stored.
9. In the config folder, you find the simulation.properties. 

simulation.rounds -> indicate how many times the simulation will be repeated.
vm.offers -> indicate the file where the types of VMs will be used
scheduling.policy -> indicate the class of the scheduling algorithm.
dag.file -> indicate the structure of the workflow, in the DAG format (according to the model language used in the Pegasus WMS). You can find other examples in the Pegasus site.

The following properties are important to configure the proposed algorithms from Tom Guérout et al. and the Task Clustering strategy using these strategies.

dag.optimize -> true or false ( if it is true, the scheduler will set "fast" VMs for the critical path and "slow" VMs for the other paths. You can check it in the code or the Tom Guérout's paper).
dag.modedvfs -> ondemand or optimal (if it is set as ondemand, the algorithm will apply the DVFS strategy. Otherwise, it will try to find the optimal VM for the task.)

dag.deadline doesn't work in the simulator. I don't know why.


10. Run the workflow/Simulation.java file as a Java application.

11. The log file will have this structure:
(name of the used scheduler),(name of the DAG file),(value of the dag.optimize), (value of the dag.modedvfs), (consumed energy), (spent time of the task)

Each line has a round of the simulation.





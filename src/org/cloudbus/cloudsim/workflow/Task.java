package org.cloudbus.cloudsim.workflow;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Log;

/**
 * This class encapsulates a DAG Task. The task contains the actual
 * Cloudlet to be executed, information about its dependencies
 * (data and execution), and scheduling information.
 * 
 */
public class Task {
	Cloudlet cloudlet;
	
	List<Task> parents;
	List<Task> replicas;
	List<Task> children;
	List<DataItem> dataDependencies;
	List<DataItem> output;

  
	HashSet<Task> antecessors;
	HashSet<Task> successors;

  
        
        boolean critical = false;
        double earliestStart, latestEnd;

    
        double slackTime;
        double ratioSlackExec=0;
        int optIndexFreq;


    
        double optExecTime;
        double BasicExecTime ;
  
        boolean defined = false;


	public Task(Cloudlet cl, int ownerId, double deadlineDag_){
		this.cloudlet = cl;
		if (cl!=null) this.cloudlet.setUserId(ownerId);
		parents = new LinkedList<Task>();
		replicas = new LinkedList<Task>();
		children = new LinkedList<Task>();
		dataDependencies = new LinkedList<DataItem>();
		output = new LinkedList<DataItem>();
		successors = new HashSet<Task>();
		antecessors = new HashSet<Task>();
                
               // latestEnd=deadlineDag_;
                BasicExecTime = getExecTime();
                optExecTime = BasicExecTime;
	}
	
        public void computeEarlieststart()
        {
            double timeEarliestStart = 0;
            double tmptime ;
            for(Task tpred : getParents())
            {
              //  Log.printLine("tache " + this.getId() + " pred Estart : " + tpred.getEarliestStart()+"  , tpred execTime : "+tpred.getOptExecTime());
                tmptime = tpred.getEarliestStart()+tpred.getOptExecTime();
                if(tmptime > timeEarliestStart)
                    timeEarliestStart = tmptime;
            }
            earliestStart = timeEarliestStart;
            
                   
        }
        
        public void computeLatestEnd(double deadline)
        {     
            latestEnd=deadline;
            if(!getChildren().isEmpty())
            {
                boolean best = false;
                double bestTimeLatestEnd = 0;
                double tmptime ;
                for(Task tsucc : getChildren())
                {
                    if(!best)
                        bestTimeLatestEnd = tsucc.getLatestEnd();
                    
                    tmptime = tsucc.getLatestEnd()-tsucc.getOptExecTime();
                    
      //              Log.printLine("tache " + this.getId() + " succ Lend : " + tsucc.getLatestEnd()+"  , tsucc execTime : "+tsucc.getOptExecTime() + " tmpLastEnd = " + tmptime);
                    if(tmptime < bestTimeLatestEnd)
                    {
                        best=true;
                        bestTimeLatestEnd = tmptime;
        //                Log.printLine("timelatestEnd" + bestTimeLatestEnd);
                    }
                }
                latestEnd = bestTimeLatestEnd;
            }
          //  setDefined(true);
        }
        public void computeSlackTime()
        {
            
            slackTime = latestEnd - ( earliestStart + getOptExecTime() ) ;
            /*if(critical)
                ratioSlackExec=0;
            else    */
            if(!critical)
                ratioSlackExec = slackTime / optExecTime;
            
           // if(slackTime==0)
                defined = true ;
        }
        
        
     //public Task longestParent(double mips, double bandwith)
     public Task longestParent()
     {
         Task longestParent=null;
         double maxtime=-1;
         double commTime=0;
         double tmp_t;
         for(Task tp: getParents())
         {
             tmp_t = tp.getCloudlet().getCloudletLength() ; ///mips;
             //for(DataItem di : dataDependencies)
               //  commTime+=di.getSize();
             //commTime/=bandwith;
             //tmp_t+=commTime;
             
             if(tmp_t > maxtime)
             {
                maxtime=tmp_t;
                longestParent=tp;
             }
         }
         return longestParent;
     }
     
     public Task longestChild()
     {
         Task longestChild=null;
         double maxtime=-1;
         double tmp_t;
         for(Task tp: getChildren())
         {
             tmp_t = tp.getCloudlet().getCloudletLength();
             if(tmp_t > maxtime)
             {
                maxtime=tmp_t;
                longestChild=tp;
             }
         }
         return longestChild;
     }
        
	public int getId(){
		if (cloudlet==null) return -1; 
		return cloudlet.getCloudletId();
	}
	
	public void addParent(Task parent){
		parents.add(parent);
	}
	
	public List<Task> getParents(){
		return parents;
	}
	
	public void addChild(Task parent){
		children.add(parent);
	}
	
	public List<Task> getChildren(){
		return children;
	}
	
	
	public HashSet<Task> getAntecessors() {
		return antecessors;
	}

	public void addAntecessors(HashSet<Task> newAntecessors) {
		this.antecessors.addAll(newAntecessors);
	}
	
	public void addAntecessors(Task task) {
		this.antecessors.add(task);
	}

	public HashSet<Task> getSuccessors() {
		return successors;
	}
	
	public void addSuccessors(HashSet<Task> newSuccessors) {
		this.successors.addAll(newSuccessors);
	}
	
	public void addSuccessors(Task task) {
		this.successors.add(task);
	}
	
	public void addDataDependency(DataItem data){
		dataDependencies.add(data);
	}
	
	public List<DataItem> getDataDependencies(){
		return dataDependencies;
	}
	
	public Cloudlet getCloudlet(){
		return cloudlet;
	}
	
	public void addOutput(DataItem data){
		output.add(data);
	}
	
	public List<DataItem> getOutput(){
		return output;
	}
		
	public void setVmId(int vmId){
		if(cloudlet!=null) cloudlet.setVmId(vmId);
	}
	
	public int getVmId(){
		if(cloudlet==null) return -1;
		return cloudlet.getVmId();
	}
	
	public boolean isReady(){	
		//while all parents are not done, it can't run
		for(Task parent:parents){
			if (!parent.isFinished()) return false;
		}
				
		return true;
	}
	
	public boolean hasReplicas(){
		return (replicas.size()>0);
	}
	
	public void addReplica(Task task){
		replicas.add(task);
	}
	
	public List<Task> getReplicas(){
		return replicas;
	}
	
	/**
	 * Communicates to the Task that its Cloudlet completed.
	 * It means that the dataItem is now available in the VM
	 * where it run.
	 *
	 */
	public void hasFinished(){
		if(cloudlet!=null){
			for (DataItem data:output){
				data.addLocation(cloudlet.getVmId());
			}
		}
	}
	
	public boolean isFinished(){
		if(cloudlet==null) return true;
		return cloudlet.isFinished();
	}
        
        public double getExecTime()
        {
            return cloudlet.getCloudletLength();
        }
        
       public boolean isCritical() {
        return critical;
    }

    public void setCritical(boolean critical) {
        this.critical = critical;
    }
    
      public double getEarliestStart() {
        return earliestStart;
    }

    public void setEarliestStart(double earliestStart) {
        this.earliestStart = earliestStart;
    }

    public double getLatestEnd() {
        return latestEnd;
    }

    public void setLatestEnd(double latestEnd) {
        this.latestEnd = latestEnd;
    }
    public double getSlackTime() {
        return slackTime;
    }

    public void setSlackTime(double slackTime) {
        this.slackTime = slackTime;
    }
    public double getOptExecTime() {
        return optExecTime;
    }

    public void setOptExecTime(double optExecTime) {
        this.optExecTime = optExecTime;
    }
        public boolean isDefined() {
        return defined;
    }

    public void setDefined(boolean defined) {
        this.defined = defined;
 }
    
    
      public double getRatioSlackExec() {
        return ratioSlackExec;
    }
	public int getOptIndexFreq() {
        return optIndexFreq;
    }

    public void setOptIndexFreq(int optIndexFreq) {
        this.optIndexFreq = optIndexFreq;
    }
    
    public double getBasicExecTime() {
        return BasicExecTime;
    }
}

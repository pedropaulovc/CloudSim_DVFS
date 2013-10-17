/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */
package org.cloudbus.cloudsim.xml;

import java.util.ArrayList;

/**
 *
 * * This class contains all Datacenter parameters
 * 
 * @author Guérout Tom, Monteil Thierry, Da Costa Georges  |  LAAS - IRIT , Toulouse, France
 ** Please cite:
 * T. Guérout et al., Energy-aware simulation with DVFS, Simulat. Modell. Pract. Theory (2013), http://dx.doi.org/10.1016/j.simpat.2013.04.007
 */


public class DatacenterDatas 
{


    ArrayList<HostDatas> vect_hosts;
    
//datacenter
String arch;
String os;
String vmm;
double timezone;
double cost;
double costPerMem;
double costPerStorage;
double costPerbW;

 

    public DatacenterDatas(){vect_hosts=new ArrayList<>();}


    public String getArch() {
        return arch;
    }

    public void setArch(String arch) {
        this.arch = arch;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public double getCostPerMem() {
        return costPerMem;
    }

    public void setCostPerMem(double costPerMem) {
        this.costPerMem = costPerMem;
    }

    public double getCostPerStorage() {
        return costPerStorage;
    }

    public void setCostPerStorage(double costPerStorage) {
        this.costPerStorage = costPerStorage;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public double getTimezone() {
        return timezone;
    }

    public void setTimezone(double timezone) {
        this.timezone = timezone;
    }

    public String getVmm() {
        return vmm;
    }

    public void setVmm(String vmm) {
        this.vmm = vmm;
    }
       public double getCostPerbW() {
        return costPerbW;
    }

    public void setCostPerbW(double costPerbW) {
        this.costPerbW = costPerbW;
    }
    
    
    public void addHost(HostDatas h){vect_hosts.add(h);}
    public HostDatas getHost(int index){return vect_hosts.get(index);}
    
    
    public ArrayList<HostDatas> getArrayListHosts() {
        return vect_hosts;
    }
    
}


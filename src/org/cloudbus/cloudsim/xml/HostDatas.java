/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */
package org.cloudbus.cloudsim.xml;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * This class contains all Host parameters
 * 
 * @author Guérout Tom, Monteil Thierry, Da Costa Georges  |  LAAS - IRIT , Toulouse, France
 * Please cite:
 * T. Guérout et al., Energy-aware simulation with DVFS, Simulat. Modell. Pract. Theory (2013), http://dx.doi.org/10.1016/j.simpat.2013.04.007
 */
public class HostDatas {
    
    private int ram;
    private int storage;
    private int bw;
    private int maxP;
    private double staticPP;
    private int cpus;
    private int mips;
    private HashMap<Integer,String> ht_gov;
    private boolean onoff_enable;
    private boolean dvfs_enable;

    
    private ArrayList<Double> cpu_frequencies;
    
    private DvfsDatas DvfsDatas;

   
   
    
    
     public HostDatas()
    {
        ht_gov = new HashMap<>();
        
        cpu_frequencies = new ArrayList<>();
    }
    
     
    public DvfsDatas getDvfsDatas() {
        return DvfsDatas;
    }

    public void setDvfsDatas(DvfsDatas DvfsDatas) {
        this.DvfsDatas = DvfsDatas;
    }
    
    public void setCpuFrequencies(ArrayList<Double> v)
    {
        cpu_frequencies=v;
    }
    public ArrayList<Double> getCpuFrequencies()
    {
        return cpu_frequencies;
    }
    
    public boolean isOnoffEnable() {
        return onoff_enable;
    }

    public void setOnoffEnable(boolean onoff) {
        this.onoff_enable = onoff;
    }
    
    public boolean isDvfsEnable() {
        return dvfs_enable;
    }

    public void setDvfsEnable(boolean dvfs_enable) {
        this.dvfs_enable = dvfs_enable;
    }
    
    public HashMap<Integer,String> getHTGovs()
    {
        return ht_gov;
    }
  
    public void putHtGovKeys(int num_cpu , String gov)
    {
        ht_gov.put(num_cpu, gov );
    }
    public String getHtGovValue(int num_cpu)
    {
        return ht_gov.get(num_cpu);
                
    }
    
    public int getBw() {
        return bw;
    }

    public void setBw(int bw) {
        this.bw = bw;
    }

    public int getCpus() {
        return cpus;
    }

    public void setCpus(int cpus) {
        this.cpus = cpus;
    }

    public int getMaxP() {
        return maxP;
    }

    public void setMaxP(int maxP) {
        this.maxP = maxP;
    }

    public int getMips() {
        return mips;
    }

    public void setMips(int mips) {
        this.mips = mips;
    }

    public int getRam() {
        return ram;
    }

    public void setRam(int ram) {
        this.ram = ram;
    }

    public double getStaticPP() {
        return staticPP;
    }

    public void setStaticPP(double staticPP) {
        this.staticPP = staticPP;
    }

    public int getStorage() {
        return storage;
    }

    public void setStorage(int storage) {
        this.storage = storage;
    }

    
}

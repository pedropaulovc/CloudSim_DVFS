/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */
package org.cloudbus.cloudsim.xml;

import java.io.File;
import java.io.IOException;
import java.util.*;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

/**
 *
 * @author Guérout Tom, Monteil Thierry, Da Costa Georges  |  LAAS - IRIT , Toulouse, France
 * Please cite:
 * T. Guérout et al., Energy-aware simulation with DVFS, Simulat. Modell. Pract. Theory (2013), http://dx.doi.org/10.1016/j.simpat.2013.04.007
 *
 */
public class SimulationXMLParse {

    
    ArrayList<DatacenterDatas> vect_dcs ;
    ArrayList<VmDatas> vect_vms ;
    ArrayList<CloudletDatas> vect_cls ;

    
    Document document;
    Element Root;
    List listRoot;
    
    public SimulationXMLParse(String path_file)
    {
        vect_dcs = new ArrayList<>();
        
        SAXBuilder sxb = new SAXBuilder();
	System.out.println("XML file used is : " + path_file);

        try {
            document = sxb.build(new File(path_file));
	}

	catch (JDOMException | IOException e) {}
        
        
         Root = document.getRootElement();
         listRoot = Root.getChildren();
         //PrintList(listracine, "root");
         
         DC_Parse((Element)listRoot.get(0));
         VM_Parse((Element)listRoot.get(1));
         CLOUDLET_Parse((Element)listRoot.get(2));
      
    }
    
    
    
    private void CLOUDLET_Parse(Element cls)
    {
        vect_cls = new ArrayList();
        ArrayList<String> atts = new ArrayList<>(Arrays.asList("length","pes", "filesize", "outputsize"));
        List liste_cl = cls.getChildren();
        int nb_cl = liste_cl.size();
        System.out.println("There is "+ nb_cl + " CLOUDLET(s)" );
        
        for(int i = 0 ; i < nb_cl ; i++)
        {
            CloudletDatas tmp_cl_datas = new CloudletDatas();
            Element One_cl = (Element)liste_cl.get(i);
            Iterator iterator = One_cl.getChildren().iterator();
            
            while(iterator.hasNext())
            {
                Element tmp_e = (Element)iterator.next();
               
                String tmp_name =tmp_e.getName();
                String tmp_value = tmp_e.getValue();
                int ind = atts.indexOf(tmp_name);
                System.out.println(tmp_name + " "+  tmp_value + " // " + ind);
                switch(ind)
                {
                     case 0: tmp_cl_datas.setLength(Integer.valueOf(tmp_value));break;
                     case 1: tmp_cl_datas.setPes(Integer.valueOf(tmp_value));break;
                     case 2: tmp_cl_datas.setFilesize(Integer.valueOf(tmp_value));break;
                     case 3: tmp_cl_datas.setOutputsize(Integer.valueOf(tmp_value));break;
                }
            }
            vect_cls.add(tmp_cl_datas);
        }
    
    }
    
    
    
    private void VM_Parse(Element vms)
    {
        vect_vms = new ArrayList();
        ArrayList<String> atts = new ArrayList<>(Arrays.asList("mips","cpu","ram", "bw", "size", "vmm"));
        List liste_vm = vms.getChildren();
        int nb_vm = liste_vm.size();
        System.out.println("There is"+ nb_vm + " VM(s)" );
        
        for(int i = 0 ; i < nb_vm ; i++)
        {
            VmDatas tmp_vm_datas = new VmDatas();
            Element One_vm = (Element)liste_vm.get(i);
            Iterator iterator = One_vm.getChildren().iterator();
            
            while(iterator.hasNext())
            {
                Element tmp_e = (Element)iterator.next();
               
                String tmp_name =tmp_e.getName();
                String tmp_value = tmp_e.getValue();
                int ind = atts.indexOf(tmp_name);
                System.out.println(tmp_name + " "+  tmp_value + " // " + ind);
                switch(ind)
                {
                     case 0: tmp_vm_datas.setMips(Integer.valueOf(tmp_value));break;
                     case 1: tmp_vm_datas.setCpu(Integer.valueOf(tmp_value));break;
                     case 2: tmp_vm_datas.setRam(Integer.valueOf(tmp_value));break;
                     case 3: tmp_vm_datas.setBw(Integer.valueOf(tmp_value));break;
                     case 4: tmp_vm_datas.setSize(Integer.valueOf(tmp_value));break;
                     case 5: tmp_vm_datas.setVmm(tmp_value);break;
                }
            }
            vect_vms.add(tmp_vm_datas);
        }
    
    }
    
    private void DC_Parse(Element dcs)
    {
        ArrayList<String> atts = new ArrayList<>(Arrays.asList("arch","os","vmm", "timezone", "cost", "costPerMem", "costPerStorage", "costPerBw"));
        
        List liste_dc = dcs.getChildren();
        int nb_dc = liste_dc.size();
        System.out.println("There is "+ nb_dc + " datacenter(s)" );
        
        
        DatacenterDatas tmp_dc_datas = new DatacenterDatas();
        
        for(int i = 0 ; i < nb_dc ; i++ )
        {
            Element One_dc = (Element)liste_dc.get(i);
            Iterator iterator = One_dc.getChildren().iterator();
            
            while(iterator.hasNext())
            {
                Element tmp_e = (Element)iterator.next();
                if(!tmp_e.getName().equalsIgnoreCase("host"))
                {
                    
                    String tmp_name =tmp_e.getName();
                    String tmp_value = tmp_e.getValue();
                    int ind = atts.indexOf(tmp_name);
                    System.out.println(tmp_name + " "+  tmp_value + " // " + ind);
                    switch(ind)
                    {
                         case 0: tmp_dc_datas.setArch(tmp_value);break;
                         case 1: tmp_dc_datas.setOs(tmp_value);break;
                         case 2: tmp_dc_datas.setVmm(tmp_value);break;
                         case 3: tmp_dc_datas.setTimezone(Double.parseDouble(tmp_value));break;
                         case 4: tmp_dc_datas.setCost(Double.parseDouble(tmp_value));break;
                         case 5: tmp_dc_datas.setCostPerMem(Double.parseDouble(tmp_value));break;
                         case 6: tmp_dc_datas.setCostPerStorage(Double.parseDouble(tmp_value));break;
                         case 7: tmp_dc_datas.setCostPerbW(Double.parseDouble(tmp_value));break;
                    }
                }
                else
                {
                    tmp_dc_datas.addHost(HOSTS_Parse(tmp_e));  
                }
            }
            vect_dcs.add(tmp_dc_datas);
            System.out.println("number of hosts :" + vect_dcs.get(0).getArrayListHosts().size());
        }
    
    }
    
    public HostDatas HOSTS_Parse(Element Host)
    {
        ArrayList<String> atts = new ArrayList<String>(Arrays.asList("ram","storage","bw", "maxP", "staticPP", "cpus", "mips","onoff","dvfsenable","cpufrequencies","dvfs"));
        HostDatas tmp_host_datas = new HostDatas();
        Iterator iterator = Host.getChildren().iterator();
            
            while(iterator.hasNext())
            {
                Element tmp_e = (Element)iterator.next();
                String tmp_name =tmp_e.getName();
                String tmp_value = tmp_e.getValue();
                int ind = atts.indexOf(tmp_name);
                System.out.println(tmp_name + " "+  tmp_value + " // " + ind);
                switch(ind)
                {
                     case 0: tmp_host_datas.setRam(Integer.valueOf(tmp_value));break;
                     case 1: tmp_host_datas.setStorage(Integer.valueOf(tmp_value));break;
                     case 2: tmp_host_datas.setBw(Integer.valueOf(tmp_value));break;
                     case 3: tmp_host_datas.setMaxP(Integer.valueOf(tmp_value));break;
                     case 4: tmp_host_datas.setStaticPP(Double.valueOf(tmp_value));break;
                     case 5: List l_cpus_child = tmp_e.getChildren();
                            Iterator it_cpus_child = l_cpus_child.iterator();
                            int nb_cpus = l_cpus_child.size();
                            tmp_host_datas.setCpus(nb_cpus);
                            while(it_cpus_child.hasNext())
                            {
                                Element OneCpu = (Element)it_cpus_child.next();
                                
                                Element num_ = OneCpu.getChild("num");
                                Element gov_ = OneCpu.getChild("governor");
                                tmp_host_datas.putHtGovKeys(Integer.parseInt(num_.getValue())-1, gov_.getValue());
                                System.out.println("cpu" +num_.getValue() + " , gov = " + gov_.getValue());
                            }
                            
                         
                         
                         break;
                     case 6: tmp_host_datas.setMips(Integer.valueOf(tmp_value));break;
                     case 7: tmp_host_datas.setOnoffEnable(Boolean.parseBoolean(tmp_value)); break;
                     case 8: tmp_host_datas.setDvfsEnable(Boolean.parseBoolean(tmp_value)); break;
                     case 9: tmp_host_datas.setCpuFrequencies(CPUFrequencies_PARSE(tmp_e)); break;
                     case 10: tmp_host_datas.setDvfsDatas(DVFSModes_PARSE(tmp_e));
                }
            }
            
            return tmp_host_datas;
    }
    
    
     public ArrayList<Double> CPUFrequencies_PARSE(Element cpufrequencies)
    {
        ArrayList<Double> tmp_cpufreqs = new ArrayList<>();
        
        ArrayList<String> atts = new ArrayList<>();
        
     //   PrintList(cpufrequencies.getChildren(), "cpufrequencies");
        
        Iterator iterator = cpufrequencies.getChildren().iterator();
        int nb_cpufrequencies = cpufrequencies.getChildren().size();
        for(int i = 1 ; i <= nb_cpufrequencies ; i++)
            atts.add("f"+i);
            
            while(iterator.hasNext())
            {
                Element tmp_e = (Element)iterator.next();
                String tmp_name =tmp_e.getName();
                String tmp_value = tmp_e.getValue();
                int ind = atts.indexOf(tmp_name);
                System.out.println(tmp_name + " "+  tmp_value + " // " + ind);
                
                tmp_cpufreqs.add(0,Double.parseDouble(tmp_value));
            }
    
        return tmp_cpufreqs;
    }
    
     
     public DvfsDatas DVFSModes_PARSE(Element dvfsmodes)
    {
        
        DvfsDatas tmp_Dvfs_Datas = new DvfsDatas();
        List liste_Mode = dvfsmodes.getChildren();
        
                
        int nb_Mode = liste_Mode.size(); // == 2
        
        System.out.println("nb mode == " + nb_Mode);
        for(int i = 0 ; i < nb_Mode ; i++)
        {
         
            
            Element One_Mode = (Element)liste_Mode.get(i);
            Iterator iterator = One_Mode.getChildren().iterator();
            
            Element e_name_mode = (Element)iterator.next();
            System.out.println("Name mode : " + e_name_mode.getValue());
            
            if(e_name_mode.getValue().equalsIgnoreCase("ondemand"))
            {
                
                while(iterator.hasNext())
                {
                    Element tmp_e = (Element)iterator.next();

                    String tmp_name =tmp_e.getName();
                    String tmp_value = tmp_e.getValue();
                    
                    System.out.println(tmp_name + " "+  tmp_value + " // " );
                    
                    tmp_Dvfs_Datas.getHashMapOnDemand().put(tmp_name, Integer.parseInt(tmp_value));
                    
                }
            }
            else if(e_name_mode.getValue().equalsIgnoreCase("conservative"))
            {
            while(iterator.hasNext())
                {
                    Element tmp_e = (Element)iterator.next();

                    String tmp_name =tmp_e.getName();
                    String tmp_value = tmp_e.getValue();
                    
                    System.out.println(tmp_name + " "+  tmp_value + " // " );
                    
                    tmp_Dvfs_Datas.getHashMapConservative().put(tmp_name, Integer.parseInt(tmp_value));           
                }
            }
            else if(e_name_mode.getValue().equalsIgnoreCase("userspace"))
            {
                 while(iterator.hasNext())
                {
                    Element tmp_e = (Element)iterator.next();

                    String tmp_name =tmp_e.getName();
                    String tmp_value = tmp_e.getValue();
                    
                    System.out.println(tmp_name + " "+  tmp_value + " // " );
                    
                    tmp_Dvfs_Datas.getHashMapUserSpace().put(tmp_name, Integer.parseInt(tmp_value));           
                }
            }
        }
        return tmp_Dvfs_Datas;
    }
    
    
    
    public void PrintList(List liste, String name)
    {
        System.out.println("--- list " + name + " ---"); 
        for(int i = 0  ; i < liste.size() ; i++)
         {            
            Element tmp_e = (Element)liste.get(i);
            System.out.println(tmp_e.getName());                  
        }
     //   System.out.println("------------"); 
    }
    
    
    
    
    public void ReturnError(String location)
    {
        System.out.println("Error during parsing file, verify : "+ location);
    }
    
    
    
    
    public ArrayList<CloudletDatas> getArrayListCLS() {
        return vect_cls;
    }

    
    public ArrayList<DatacenterDatas> getArrayListDCS() {
        return vect_dcs;
    }

    
    public ArrayList<VmDatas> getArrayListVMS() {
        return vect_vms;
    }

    
    
    
      public static void main(String[] args) {
        // TODO code application logic here       
        
          
          new SimulationXMLParse(args[0]);
        
    }
}


/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.examples.Individual;
import org.cloudbus.cloudsim.examples.PSOProcess;
import org.cloudbus.cloudsim.examples.Phase1;
import org.cloudbus.cloudsim.examples.Population;
import org.cloudbus.cloudsim.lists.CloudletList;
import org.cloudbus.cloudsim.lists.VmList;

/**
 * DatacentreBroker represents a broker acting on behalf of a user. It hides VM management, as vm
 * creation, sumbission of cloudlets to this VMs and destruction of VMs.
 * 
 * @author Rodrigo N. Calheiros
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 1.0
 */
public class DatacenterBroker extends SimEntity {

	List<? extends Vm> GlobalVMList=new ArrayList();
	List<? extends Cloudlet> GlobalCloudletList=new ArrayList();
	Datacenter datacenter;
	
	/** The vm list. */
	protected List<? extends Vm> vmList;

	/** The vms created list. */
	protected List<? extends Vm> vmsCreatedList;

	/** The cloudlet list. */
	protected List<? extends Cloudlet> cloudletList;

	/** The cloudlet submitted list. */
	protected List<? extends Cloudlet> cloudletSubmittedList;

	/** The cloudlet received list. */
	protected List<? extends Cloudlet> cloudletReceivedList;

	/** The cloudlets submitted. */
	protected int cloudletsSubmitted;

	/** The vms requested. */
	protected int vmsRequested;

	/** The vms acks. */
	protected int vmsAcks;

	/** The vms destroyed. */
	protected int vmsDestroyed;

	/** The datacenter ids list. */
	protected List<Integer> datacenterIdsList;

	/** The datacenter requested ids list. */
	protected List<Integer> datacenterRequestedIdsList;

	/** The vms to datacenters map. */
	protected Map<Integer, Integer> vmsToDatacentersMap;

	/** The datacenter characteristics list. */
	protected Map<Integer, DatacenterCharacteristics> datacenterCharacteristicsList;

	/**
	 * Created a new DatacenterBroker object.
	 * 
	 * @param name name to be associated with this entity (as required by Sim_entity class from
	 *            simjava package)
	 * @throws Exception the exception
	 * @pre name != null
	 * @post $none
	 */
	
	
	public DatacenterBroker(String name) throws Exception {
		super(name);

		setVmList(new ArrayList<Vm>());
		setVmsCreatedList(new ArrayList<Vm>());
		setCloudletList(new ArrayList<Cloudlet>());
		setCloudletSubmittedList(new ArrayList<Cloudlet>());
		setCloudletReceivedList(new ArrayList<Cloudlet>());

		cloudletsSubmitted = 0;
		setVmsRequested(0);
		setVmsAcks(0);
		setVmsDestroyed(0);

		setDatacenterIdsList(new LinkedList<Integer>());
		setDatacenterRequestedIdsList(new ArrayList<Integer>());
		setVmsToDatacentersMap(new HashMap<Integer, Integer>());
		setDatacenterCharacteristicsList(new HashMap<Integer, DatacenterCharacteristics>());
	}

	/**
	 * This method is used to send to the broker the list with virtual machines that must be
	 * created.
	 * 
	 * @param list the list
	 * @pre list !=null
	 * @post $none
	 */
	public void submitVmList(List<? extends Vm> list) {
		getVmList().addAll(list);
	}

	/**
	 * This method is used to send to the broker the list of cloudlets.
	 * 
	 * @param list the list
	 * @pre list !=null
	 * @post $none
	 */
	public void submitCloudletList(List<? extends Cloudlet> list) {
		getCloudletList().addAll(list);
	}

	/**
	 * Specifies that a given cloudlet must run in a specific virtual machine.
	 * 
	 * @param cloudletId ID of the cloudlet being bount to a vm
	 * @param vmId the vm id
	 * @pre cloudletId > 0
	 * @pre id > 0
	 * @post $none
	 */
	public void bindCloudletToVm(int cloudletId, int vmId) {
		CloudletList.getById(getCloudletList(), cloudletId).setVmId(vmId);
		//CloudletList.getById(getCloudletList(), 2).setVmId(1);
	}

	/**
	 * Processes events available for this Broker.
	 * 
	 * @param ev a SimEvent object
	 * @pre ev != null
	 * @post $none
	 */
	@Override
	public void processEvent(SimEvent ev) {
		switch (ev.getTag()) {
		// Resource characteristics request
			case CloudSimTags.RESOURCE_CHARACTERISTICS_REQUEST:
				processResourceCharacteristicsRequest(ev);
				break;
			// Resource characteristics answer
			case CloudSimTags.RESOURCE_CHARACTERISTICS:
				processResourceCharacteristics(ev);
				break;
			// VM Creation answer
			case CloudSimTags.VM_CREATE_ACK:
				processVmCreate(ev);
				break;
			// A finished cloudlet returned
			case CloudSimTags.CLOUDLET_RETURN:
				processCloudletReturn(ev);
				break;
			// if the simulation finishes
			case CloudSimTags.END_OF_SIMULATION:
				shutdownEntity();
				break;
			// other unknown tags are processed by this method
			default:
				processOtherEvent(ev);
				break;
		}
	}

	/**
	 * Process the return of a request for the characteristics of a PowerDatacenter.
	 * 
	 * @param ev a SimEvent object
	 * @pre ev != $null
	 * @post $none
	 */
	protected void processResourceCharacteristics(SimEvent ev) {
		DatacenterCharacteristics characteristics = (DatacenterCharacteristics) ev.getData();
		getDatacenterCharacteristicsList().put(characteristics.getId(), characteristics);

		if (getDatacenterCharacteristicsList().size() == getDatacenterIdsList().size()) {
			setDatacenterRequestedIdsList(new ArrayList<Integer>());
			createVmsInDatacenter(getDatacenterIdsList().get(0));
		}
	}

	/**
	 * Process a request for the characteristics of a PowerDatacenter.
	 * 
	 * @param ev a SimEvent object
	 * @pre ev != $null
	 * @post $none
	 */
	protected void processResourceCharacteristicsRequest(SimEvent ev) {
		setDatacenterIdsList(CloudSim.getCloudResourceList());
		setDatacenterCharacteristicsList(new HashMap<Integer, DatacenterCharacteristics>());

		Log.printLine(CloudSim.clock() + ": " + getName() + ": Cloud Resource List received with "
				+ getDatacenterIdsList().size() + " resource(s)");

		for (Integer datacenterId : getDatacenterIdsList()) {
			sendNow(datacenterId, CloudSimTags.RESOURCE_CHARACTERISTICS, getId());
		}
	}

	/**
	 * Process the ack received due to a request for VM creation.
	 * 
	 * @param ev a SimEvent object
	 * @pre ev != null
	 * @post $none
	 */
	protected void processVmCreate(SimEvent ev) {
		int[] data = (int[]) ev.getData();
		int datacenterId = data[0];
		int vmId = data[1];
		int result = data[2];

		if (result == CloudSimTags.TRUE) {
			getVmsToDatacentersMap().put(vmId, datacenterId);
			getVmsCreatedList().add(VmList.getById(getVmList(), vmId));
			Log.printLine(CloudSim.clock() + ": " + getName() + ": VM #" + vmId
					+ " has been created in Datacenter #" + datacenterId + ", Host #"
					+ VmList.getById(getVmsCreatedList(), vmId).getHost().getId());
		} else {
			Log.printLine(CloudSim.clock() + ": " + getName() + ": Creation of VM #" + vmId
					+ " failed in Datacenter #" + datacenterId);
		}

		incrementVmsAcks();

		// all the requested VMs have been created
		if (getVmsCreatedList().size() == getVmList().size() - getVmsDestroyed()) {
			submitCloudlets();
		} else {
			// all the acks received, but some VMs were not created
			if (getVmsRequested() == getVmsAcks()) {
				// find id of the next datacenter that has not been tried
				for (int nextDatacenterId : getDatacenterIdsList()) {
					if (!getDatacenterRequestedIdsList().contains(nextDatacenterId)) {
						createVmsInDatacenter(nextDatacenterId);
						return;
					}
				}

				// all datacenters already queried
				if (getVmsCreatedList().size() > 0) { // if some vm were created
					submitCloudlets();
				} else { // no vms created. abort
					Log.printLine(CloudSim.clock() + ": " + getName()
							+ ": none of the required VMs could be created. Aborting");
					finishExecution();
				}
			}
		}
	}

	/**
	 * Process a cloudlet return event.
	 * 
	 * @param ev a SimEvent object
	 * @pre ev != $null
	 * @post $none
	 */
	protected void processCloudletReturn(SimEvent ev) {
		Cloudlet cloudlet = (Cloudlet) ev.getData();
		getCloudletReceivedList().add(cloudlet);
		Log.printLine(CloudSim.clock() + ": " + getName() + ": Cloudlet " + cloudlet.getCloudletId()
				+ " received");
		cloudletsSubmitted--;
		if (getCloudletList().size() == 0 && cloudletsSubmitted == 0) { // all cloudlets executed
			Log.printLine(CloudSim.clock() + ": " + getName() + ": All Cloudlets executed. Finishing...");
			clearDatacenters();
			finishExecution();
		} else { // some cloudlets haven't finished yet
			if (getCloudletList().size() > 0 && cloudletsSubmitted == 0) {
				// all the cloudlets sent finished. It means that some bount
				// cloudlet is waiting its VM be created
				clearDatacenters();
				createVmsInDatacenter(0);
			}

		}
	}

	/**
	 * Overrides this method when making a new and different type of Broker. This method is called
	 * by {@link #body()} for incoming unknown tags.
	 * 
	 * @param ev a SimEvent object
	 * @pre ev != null
	 * @post $none
	 */
	protected void processOtherEvent(SimEvent ev) {
		if (ev == null) {
			Log.printLine(getName() + ".processOtherEvent(): " + "Error - an event is null.");
			return;
		}

		Log.printLine(getName() + ".processOtherEvent(): "
				+ "Error - event unknown by this DatacenterBroker.");
	}

	/**
	 * Create the virtual machines in a datacenter.
	 * 
	 * @param datacenterId Id of the chosen PowerDatacenter
	 * @pre $none
	 * @post $none
	 */
	protected void createVmsInDatacenter(int datacenterId) {
		// send as much vms as possible for this datacenter before trying the next one
		int requestedVms = 0;
		String datacenterName = CloudSim.getEntityName(datacenterId);
		for (Vm vm : getVmList()) {
			if (!getVmsToDatacentersMap().containsKey(vm.getId())) {
				Log.printLine(CloudSim.clock() + ": " + getName() + ": Trying to Create VM #" + vm.getId()
						+ " in " + datacenterName);
				sendNow(datacenterId, CloudSimTags.VM_CREATE_ACK, vm);
				requestedVms++;
			}
		}

		getDatacenterRequestedIdsList().add(datacenterId);

		setVmsRequested(requestedVms);
		setVmsAcks(0);
	}

	/**
	 * Submit cloudlets to the created VMs.
	 * 
	 * @pre $none
	 * @post $none
	 */
	protected void submitCloudlets() {
		int vmIndex = 0;
		for (Cloudlet cloudlet : getCloudletList()) {
			Vm vm;
			// if user didn't bind this cloudlet and it has not been executed yet
			if (cloudlet.getVmId() == -1) {
				vm = getVmsCreatedList().get(vmIndex);
			} else { // submit to the specific vm
				vm = VmList.getById(getVmsCreatedList(), cloudlet.getVmId());
				if (vm == null) { // vm was not created
					Log.printLine(CloudSim.clock() + ": " + getName() + ": Postponing execution of cloudlet "
							+ cloudlet.getCloudletId() + ": bount VM not available");
					continue;
				}
			}

			Log.printLine(CloudSim.clock() + ": " + getName() + ": Sending cloudlet "
					+ cloudlet.getCloudletId() + " to VM #" + vm.getId());
			cloudlet.setVmId(vm.getId());
			sendNow(getVmsToDatacentersMap().get(vm.getId()), CloudSimTags.CLOUDLET_SUBMIT, cloudlet);
			cloudletsSubmitted++;
			vmIndex = (vmIndex + 1) % getVmsCreatedList().size();
			getCloudletSubmittedList().add(cloudlet);
		}

		// remove submitted cloudlets from waiting list
		for (Cloudlet cloudlet : getCloudletSubmittedList()) {
			getCloudletList().remove(cloudlet);
		}
	}

	/**
	 * Destroy the virtual machines running in datacenters.
	 * 
	 * @pre $none
	 * @post $none
	 */
	protected void clearDatacenters() {
		for (Vm vm : getVmsCreatedList()) {
			Log.printLine(CloudSim.clock() + ": " + getName() + ": Destroying VM #" + vm.getId());
			sendNow(getVmsToDatacentersMap().get(vm.getId()), CloudSimTags.VM_DESTROY, vm);
		}

		getVmsCreatedList().clear();
	}

	/**
	 * Send an internal event communicating the end of the simulation.
	 * 
	 * @pre $none
	 * @post $none
	 */
	protected void finishExecution() {
		sendNow(getId(), CloudSimTags.END_OF_SIMULATION);
	}

	/*
	 * (non-Javadoc)
	 * @see cloudsim.core.SimEntity#shutdownEntity()
	 */
	@Override
	public void shutdownEntity() {
		Log.printLine(getName() + " is shutting down...");
	}

	/*
	 * (non-Javadoc)
	 * @see cloudsim.core.SimEntity#startEntity()
	 */
	@Override
	public void startEntity() {
		Log.printLine(getName() + " is starting...");
		schedule(getId(), 0, CloudSimTags.RESOURCE_CHARACTERISTICS_REQUEST);
	}

	/**
	 * Gets the vm list.
	 * 
	 * @param <T> the generic type
	 * @return the vm list
	 */
	@SuppressWarnings("unchecked")
	public <T extends Vm> List<T> getVmList() {
		return (List<T>) vmList;
	}

	/**
	 * Sets the vm list.
	 * 
	 * @param <T> the generic type
	 * @param vmList the new vm list
	 */
	protected <T extends Vm> void setVmList(List<T> vmList) {
		this.vmList = vmList;
	}

	/**
	 * Gets the cloudlet list.
	 * 
	 * @param <T> the generic type
	 * @return the cloudlet list
	 */
	@SuppressWarnings("unchecked")
	public <T extends Cloudlet> List<T> getCloudletList() {
		return (List<T>) cloudletList;
	}

	/**
	 * Sets the cloudlet list.
	 * 
	 * @param <T> the generic type
	 * @param cloudletList the new cloudlet list
	 */
	protected <T extends Cloudlet> void setCloudletList(List<T> cloudletList) {
		this.cloudletList = cloudletList;
	}

	/**
	 * Gets the cloudlet submitted list.
	 * 
	 * @param <T> the generic type
	 * @return the cloudlet submitted list
	 */
	@SuppressWarnings("unchecked")
	public <T extends Cloudlet> List<T> getCloudletSubmittedList() {
		return (List<T>) cloudletSubmittedList;
	}

	/**
	 * Sets the cloudlet submitted list.
	 * 
	 * @param <T> the generic type
	 * @param cloudletSubmittedList the new cloudlet submitted list
	 */
	protected <T extends Cloudlet> void setCloudletSubmittedList(List<T> cloudletSubmittedList) {
		this.cloudletSubmittedList = cloudletSubmittedList;
	}

	/**
	 * Gets the cloudlet received list.
	 * 
	 * @param <T> the generic type
	 * @return the cloudlet received list
	 */
	@SuppressWarnings("unchecked")
	public <T extends Cloudlet> List<T> getCloudletReceivedList() {
		return (List<T>) cloudletReceivedList;
	}

	/**
	 * Sets the cloudlet received list.
	 * 
	 * @param <T> the generic type
	 * @param cloudletReceivedList the new cloudlet received list
	 */
	protected <T extends Cloudlet> void setCloudletReceivedList(List<T> cloudletReceivedList) {
		this.cloudletReceivedList = cloudletReceivedList;
	}

	/**
	 * Gets the vm list.
	 * 
	 * @param <T> the generic type
	 * @return the vm list
	 */
	@SuppressWarnings("unchecked")
	public <T extends Vm> List<T> getVmsCreatedList() {
		return (List<T>) vmsCreatedList;
	}

	/**
	 * Sets the vm list.
	 * 
	 * @param <T> the generic type
	 * @param vmsCreatedList the vms created list
	 */
	protected <T extends Vm> void setVmsCreatedList(List<T> vmsCreatedList) {
		this.vmsCreatedList = vmsCreatedList;
	}

	/**
	 * Gets the vms requested.
	 * 
	 * @return the vms requested
	 */
	protected int getVmsRequested() {
		return vmsRequested;
	}

	/**
	 * Sets the vms requested.
	 * 
	 * @param vmsRequested the new vms requested
	 */
	protected void setVmsRequested(int vmsRequested) {
		this.vmsRequested = vmsRequested;
	}

	/**
	 * Gets the vms acks.
	 * 
	 * @return the vms acks
	 */
	protected int getVmsAcks() {
		return vmsAcks;
	}

	/**
	 * Sets the vms acks.
	 * 
	 * @param vmsAcks the new vms acks
	 */
	protected void setVmsAcks(int vmsAcks) {
		this.vmsAcks = vmsAcks;
	}

	/**
	 * Increment vms acks.
	 */
	protected void incrementVmsAcks() {
		vmsAcks++;
	}

	/**
	 * Gets the vms destroyed.
	 * 
	 * @return the vms destroyed
	 */
	protected int getVmsDestroyed() {
		return vmsDestroyed;
	}

	/**
	 * Sets the vms destroyed.
	 * 
	 * @param vmsDestroyed the new vms destroyed
	 */
	protected void setVmsDestroyed(int vmsDestroyed) {
		this.vmsDestroyed = vmsDestroyed;
	}

	/**
	 * Gets the datacenter ids list.
	 * 
	 * @return the datacenter ids list
	 */
	protected List<Integer> getDatacenterIdsList() {
		return datacenterIdsList;
	}

	/**
	 * Sets the datacenter ids list.
	 * 
	 * @param datacenterIdsList the new datacenter ids list
	 */
	protected void setDatacenterIdsList(List<Integer> datacenterIdsList) {
		this.datacenterIdsList = datacenterIdsList;
	}

	/**
	 * Gets the vms to datacenters map.
	 * 
	 * @return the vms to datacenters map
	 */
	protected Map<Integer, Integer> getVmsToDatacentersMap() {
		return vmsToDatacentersMap;
	}

	/**
	 * Sets the vms to datacenters map.
	 * 
	 * @param vmsToDatacentersMap the vms to datacenters map
	 */
	protected void setVmsToDatacentersMap(Map<Integer, Integer> vmsToDatacentersMap) {
		this.vmsToDatacentersMap = vmsToDatacentersMap;
	}

	/**
	 * Gets the datacenter characteristics list.
	 * 
	 * @return the datacenter characteristics list
	 */
	protected Map<Integer, DatacenterCharacteristics> getDatacenterCharacteristicsList() {
		return datacenterCharacteristicsList;
	}

	/**
	 * Sets the datacenter characteristics list.
	 * 
	 * @param datacenterCharacteristicsList the datacenter characteristics list
	 */
	protected void setDatacenterCharacteristicsList(
			Map<Integer, DatacenterCharacteristics> datacenterCharacteristicsList) {
		this.datacenterCharacteristicsList = datacenterCharacteristicsList;
	}

	/**
	 * Gets the datacenter requested ids list.
	 * 
	 * @return the datacenter requested ids list
	 */
	protected List<Integer> getDatacenterRequestedIdsList() {
		return datacenterRequestedIdsList;
	} 

	/**
	 * Sets the datacenter requested ids list.
	 * 
	 * @param datacenterRequestedIdsList the new datacenter requested ids list
	 */
	protected void setDatacenterRequestedIdsList(List<Integer> datacenterRequestedIdsList) {
		this.datacenterRequestedIdsList = datacenterRequestedIdsList;
	}
	
	//30-Sep-Sun   Start...  2nd part
 	
	public int[][] assignCloudletToVM(List<? extends Vm> vmlist,List<? extends Cloudlet> cloudletList, Datacenter datacenter2) throws Exception
	{
		GlobalVMList=vmlist;
		GlobalCloudletList=cloudletList;
		
		datacenter=datacenter2;
		int tot=(vmlist.size()+cloudletList.size());
		//System.out.println("List values "+GlobalVMList.size()+" "+GlobalCloudletList.size()+" "+tot);
		int a[][]=new int[tot+1][tot+1];
		List<String> encodedarray=new ArrayList();
		int[] chromosome=new int[tot];
		encodedarray=generateRandomCombinations(tot-1,vmlist,cloudletList);
	
		int newPopSize=100; //better to have a fixed value 100 or 20 pop size
		
		List<String> Population=new ArrayList();
		Individual ind=new Individual();
		for(int i=0;i<newPopSize;i++)
		{
			Population.add(encodedarray.get(i));
		}
		Population popn = new Population();
		//popn.setPopulation(Population);
		ind=popn.popmain(Population,vmlist,cloudletList,datacenter2);
		
		String s=ind.getGene();
		
		
		List<Integer> delimiter=new ArrayList();
		int tot1=vmlist.size()+cloudletList.size();
		
		for(int i=vmlist.size()+1;i<=((vmlist.size()+cloudletList.size())-1);i++) //668
			delimiter.add(i);
		
		//int a[][]=new int[tot][tot];
		int[] tempArray=new int[s.length()];
		char[] ct=new char[s.length()];
		ct=s.toCharArray();
		for(int i=0;i<s.length();i++)
		{
			
			tempArray[i]=Integer.parseInt(String.valueOf(ct[i]));
		}
		
		//String fitChromosome=Integer.toString(ind.)
	    //String fitChromosome=getFitAssignmentString(Population,vmlist,cloudletList,datacenter2);
		
		//a[3][2]=1;
		
		int cloud=1;
		for(int i=0;i<tempArray.length;i++)
		{
			if(delimiter.contains(tempArray[i]))
			{
				cloud++;
				continue;
			}
			else
			{
				a[cloud][tempArray[i]]=1;
			}
		}
		
		return a;
	}

	private String getFitAssignmentString(List<String> encodedarray, List<? extends Vm> vmlist2,
			List<? extends Cloudlet> cloudletList2, Datacenter datacenter2) {
		// TODO Auto-generated method stub
		String fitChromosome;
		List<Float> minEnergy=new ArrayList();
		for(String s:encodedarray)
		{
		//	minEnergy.add(evaluatePower(s));
		}
		//float tMinEnergy=Collections.min(minEnergy);
		int index=minEnergy.indexOf(Collections.min(minEnergy));  //668
		fitChromosome=encodedarray.get(index);
		return fitChromosome;
	}

	public Float evaluatePower(String s, List<? extends Vm> vmlist2, List<? extends Cloudlet> cloudletList2, Datacenter datacenter2) {
		// TODO Auto-generated method stub	
		if(s==null)
			return 0.0f;
		
		
		
		float power=0.0f;
		List<Integer> delimiter=new ArrayList();
		
		int tot=vmlist2.size()+cloudletList2.size();
		//System.out.println("List values "+vmlist2.size()+" "+cloudletList2.size()+" "+tot);
		
		for(int i=vmlist2.size()+1;i<=((vmlist2.size()+cloudletList2.size())-1);i++)
			delimiter.add(i);
        		
		int a[][]=new int[tot][tot];
		int[] tempArray=new int[s.length()];
		char[] ct=new char[s.length()];
		ct=s.toCharArray();
		for(int i=0;i<s.length();i++)
		{
			
			tempArray[i]=Integer.parseInt(String.valueOf(ct[i]));
		}
		
		int cloud=1;
		for(int i=0;i<tempArray.length;i++)
		{
			if(delimiter.contains(tempArray[i]))
			{
				cloud++;
				continue;
			}
			else
			{
				a[cloud][tempArray[i]]=1;
			}
		//	power=calculatePower(a,cloudletList2,vmlist2,datacenter2);
		}
		
	//	System.out.println("For each...");
	/*	for(int i=0;i<tot;i++)
		{
			System.out.println(" ");
			for(int j=0;j<tot;j++)
			{
				System.out.print(a[i][j]+" ");
			}
		}*/
		
		power=calculatePower(a,vmlist2,cloudletList2,datacenter2);
		//method to calculate power
		
		
		System.out.println("String  "+s+"         "+power);
		return power;
	}

	public float calculatePower(int[][] a, List<? extends Vm> vmlist2, List<? extends Cloudlet> cloudletList2, Datacenter datacenter2) {
		// TODO Auto-generated method stub
		float beta=0.8f;
		float power=0.0f;
		float vt=0.0f;
		
		float vCPU=0;
		float vMemory = 0;
		
		float vDisk=0;
		
		List<Float> vCPUList=new ArrayList();
		List<Float> vMemoryList=new ArrayList();
		List<Float> vDiskList=new ArrayList();
		
		DatacenterCharacteristics dc=datacenter2.getCharacteristics();
		
		//dc=datacenter2.getCharacteristics();
		
		for(int cloud=1;cloud<=cloudletList2.size();cloud++)
		{
			for(int vm=1;vm<=vmlist2.size();vm++)
			{
				if(a[cloud][vm]==1)
				{
					long length=cloudletList2.get(cloud-1).getCloudletLength();
					double load=cloudletList2.get(cloud-1).getCloudletFileSize();
					vCPU=(float) ((cloudletList2.get(cloud-1).getActualCPUTime()/dc.getCpuTime(length, load))-1);
					
					vMemory=(float) (((cloudletList2.get(cloud-1).getUtilizationOfRam(100.00))/dc.getCostPerMem())-1);
					
					vDisk=(float) (((cloudletList2.get(cloud-1).getNumberOfPes())/(dc.getNumberOfPes())-1));
					//System.out.println(load);
					
					vCPUList.add(vCPU);
					vMemoryList.add(vMemory);
					vDiskList.add(vDisk);
					
				}
			}
		
		}
		vCPU=Collections.max(vCPUList);
		vMemory=Collections.max(vMemoryList);
		vDisk=Collections.max(vDiskList);
		
	
		vt=(vCPU+vMemory+vDisk);
		
		
		float f1,f2;
		f2=0.0f;
		
		for(int cloud=1;cloud<=cloudletList2.size();cloud++)
		{
			for(int vm=1;vm<=vmlist2.size();vm++)
			{
				if(a[cloud][vm]==1)
				{
					float tf;
					tf=(float) ((dc.getCostPerSecond())*60*60); //60*60 for finding cost for one hour
					f2=f2+tf;
					
				}	
			}
		}

		power=(float) ((0.5*f2) + (1+beta*vt));
		
	//	System.out.println("power"+power);
		
	/*	System.out.println(vCPU);
		System.out.println(vMemory);
		System.out.println(vDisk);
		
		System.out.println("datacenter2"+datacenter2.getId());*/
		
		return power;
	}

	private List<String> generateRandomCombinations(int tot, List<? extends Vm> vmlist2,
			List<? extends Cloudlet> cloudletList2) {
		
		int arr[]=new int[tot];
		String ss = "";
		List<String> permutedString=new ArrayList(); 
		Set<String> tt=new HashSet();
		for(int i=1;i<=tot;i++)
		{
			ss=ss+""+Integer.toString(i);  
		}
		tt=permutationFinder(ss);
		for(String t:tt)
		{
			permutedString.add(t);
		}
		
	//	System.out.println(permutedString);
	
		// TODO Auto-generated method stub
		
		return permutedString;
	}
	
	 public static Set<String> permutationFinder(String str) {
	        Set<String> perm = new HashSet<String>();
	        //Handling error scenarios
	        if (str == null) {
	            return null;
	        } else if (str.length() == 0) {
	            perm.add("");
	            return perm;
	        }
	        char initial = str.charAt(0); // first character
	        String rem = str.substring(1); // Full string without first character
	        Set<String> words = permutationFinder(rem);
	        for (String strNew : words) {
	            for (int i = 0;i<=strNew.length();i++){
	                perm.add(charInsert(strNew, initial, i));
	            }
	        }
	        return perm;
	 }

	 public static String charInsert(String str, char c, int j) {
	        String begin = str.substring(0, j);
	        String end = str.substring(j);
	        return begin + c + end;
	    }

	public int[][] assignCloudletUsingPSO(List<Vm> vmlist2, List<Cloudlet> cloudletList2, Datacenter datacenter2) {
		// TODO Auto-generated method stub
		
		PSOProcess pso=new PSOProcess();
		//int[][] a = null;
		
		int cloud_size = cloudletList2.size();
		int vmlist_size = vmlist2.size();
		
		int a[][]=new int[cloud_size][vmlist_size];
		//check
		for(int i=0; i<cloudletList2.size(); i++)
		{
			for(int j=0; j<vmlist2.size();j++)
			{
				a[i][j]=0;
			}
		}
		
		System.out.println("Before Datacentre broker assignment");
		
	/*	for(int i=0; i<cloudletList2.size(); i++)
		{
			for(int j=0; j<vmlist2.size();j++)
			{
				System.out.println("  "+a[i][j]);
			}
		}   */
		
		
		a=pso.execute(vmlist2,cloudletList2,datacenter2);
		
		System.out.println("after Datacentre broker assignment");
		
		
		return a;
	}


	//power function 
	
}

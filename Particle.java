package org.cloudbus.cloudsim.examples;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Vm;

/* public class Particle {
	
int[][] position;
int velocity;
int[][] localBestPosition;
double localBest;

DatacenterBroker broker;  */

public class Particle {
	private double fitnessValue;
	private Velocity velocity;
	private Location location;
	//DatacentreBroker bb;
	
	public Particle() {
		super();
	}

	public Particle(double fitnessValue, Velocity velocity, Location location) {
		super();
		this.fitnessValue = fitnessValue;
		this.velocity = velocity;
		this.location = location;
	}

	public Velocity getVelocity() {
		return velocity;
	}

	public void setVelocity(Velocity velocity) {
		this.velocity = velocity;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public double getFitnessValue(List<Vm> vmlist2, List<Cloudlet> cloudletList2, Datacenter datacenter2) {
		int a[][]=location.getLoc();
		fitnessValue = calculatePower(a,vmlist2,cloudletList2,datacenter2);
		return fitnessValue;
	}

	private double calculatePower(int[][] a, List<Vm> vmlist2, List<Cloudlet> cloudletList2,
			Datacenter datacenter2) {
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
		
		for(int cloud=1;cloud<cloudletList2.size();cloud++)
		{
			for(int vm=1;vm</*5*/vmlist2.size();vm++)
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
		
		for(int cloud=1;cloud<cloudletList2.size();cloud++)
		{
			for(int vm=1;vm<vmlist2.size();vm++)
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

	
}



/*public double evaluatePower(List<? extends Vm> vmlist2, List<? extends Cloudlet> cloudletList2, Datacenter datacenter2)
{
	return broker.calculatePower(position, vmlist2, cloudletList2, datacenter2);
}




}
     */ 
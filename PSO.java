package org.cloudbus.cloudsim;

import java.util.List;

import org.cloudbus.cloudsim.examples.Particle;

public class PSO {

	Particle[] swarm=new Particle[5]; // Give swarm size...
	double GlobalBestPower=0.0;
	int[][] GlobalBest;
	
	public int[][] start(List<Vm> vmlist2, List<Cloudlet> cloudletList2, Datacenter datacenter2) {
		// TODO Auto-generated method stub
		
		Particle particle=new Particle();
	swarm=intializeSwarm(swarm.length); // implement in PSO
	
	for(int i=0;i<swarm.length;i++)
	{
		findLocalBest(swarm[i]);
	}
	
	
	
	GlobalBestPower= swarm[0].evaluatePower(vmlist2, cloudletList2, datacenter2);

	
	for(int i=1;i<swarm.length;i++)
	{
		if(GlobalBestPower>swarm[i].evaluatePower(vmlist2, cloudletList2, datacenter2))
		{
			GlobalBestPower=swarm[i].evaluatePower(vmlist2, cloudletList2, datacenter2);
			GlobalBest=swarm[i].getPosition();
		}
	}
		return GlobalBest;
	}
	
	// end of start()...

	private Particle[] intializeSwarm(int length) {
		// TODO Auto-generated method stub
		Particle[] p=new Particle[length];
		
		//Permute and get some strings 
		//set them to each P[].position

		return p;
	}

	private void findLocalBest(Particle particle) {
		// TODO Auto-generated method stub
		
		
		//Do all updation and shuffling work and calculate best of local best and set it to 
		//particle.localBest and particle.localPsotion
		
	}
}

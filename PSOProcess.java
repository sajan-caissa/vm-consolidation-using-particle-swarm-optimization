package org.cloudbus.cloudsim.examples;

import java.util.List;
import java.util.Random;
import java.util.Vector;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.Vm;

public class PSOProcess {
	
	int SWARM_SIZE = 5;
	int MAX_ITERATION = 10;
	double C1 = 0.25;
	double C2 = 0.25;
	double W_UPPERBOUND = 1.0;
	double W_LOWERBOUND = 0.0;
	
	private Vector<Particle> swarm = new Vector<Particle>();
	private double[] pBest = new double[SWARM_SIZE];
	private Vector<Location> pBestLocation = new Vector<Location>();
	private double gBest;
	private Location gBestLocation;
	private double[] fitnessValueList = new double[SWARM_SIZE];
	
    int VEL_LOW = 0;
	int VEL_HIGH = 5;
	
	Random generator = new Random();
	
	public int[][] execute(List<Vm> vmlist2, List<Cloudlet> cloudletList2, Datacenter datacenter2) {
		initializeSwarm(vmlist2,cloudletList2,datacenter2);
		updateFitnessList(vmlist2,cloudletList2,datacenter2);
		
		for(int i=0; i<SWARM_SIZE; i++) {
			pBest[i] = fitnessValueList[i];
			pBestLocation.add(swarm.get(i).getLocation());
			System.out.println("fitness value copied to pbest");
		}
		
		int t = 0;
		double w;
	
		
		while(t < MAX_ITERATION ) {
			// step 1 - update pBest
			for(int i=0; i<SWARM_SIZE; i++) {
				if(fitnessValueList[i] < pBest[i]) {
					pBest[i] = fitnessValueList[i];
					pBestLocation.set(i, swarm.get(i).getLocation());
				}
				
	     System.out.println("pbest value updated");
			}
				
			// step 2 - update gBest
			int bestParticleIndex = PSOUtility.getMinPos(fitnessValueList);
			if(t == 0 || fitnessValueList[bestParticleIndex] < gBest) {
				gBest = fitnessValueList[bestParticleIndex];
				gBestLocation = swarm.get(bestParticleIndex).getLocation();
				System.out.println("gbest updated and the value is "+gBest);
			}
			
			w = W_UPPERBOUND - (((double) t) / MAX_ITERATION) * (W_UPPERBOUND - W_LOWERBOUND);
			
			for(int i=0; i<SWARM_SIZE; i++) {
				double r1 = generator.nextDouble();
				double r2 = generator.nextDouble();
				
				Particle p = swarm.get(i);
				
				// step 3 - update velocity
		
				double newVel;
				newVel = (w * p.getVelocity().getPos()) + 
						(r1 * C1) * (pBest[i] - p.getFitnessValue(vmlist2,cloudletList2,datacenter2)) +
						(r2 * C2) * (gBest - p.getFitnessValue(vmlist2,cloudletList2,datacenter2));
				
				int velo = (int) Math.round(newVel);
				Velocity vel = new Velocity(velo);
				p.setVelocity(vel);
				System.out.println("velocity value updated and the value is "+velo);
				
				
				// step 4 - update location
	
				int[][] loc1,newLoc;
				loc1=p.getLocation().getLoc();
				newLoc = shuffle(loc1,velo);
				Location loc = new Location(newLoc);
				p.setLocation(loc);
				//System.out.println("location updated and the value is "+newLoc);
				
			}
			
		
			
			
			System.out.println("ITERATION " + t + ": ");
	
			
		//	System.out.println("     global Best location :"+ gBestLocation.getLoc());
			System.out.println("     Global Best Location fitness Value :"+ gBest);
			t++;
			updateFitnessList(vmlist2,cloudletList2,datacenter2);
		}
		
		System.out.println("\nSolution found at iteration " + (t - 1) + ", the solutions is:");
	
	
	    System.out.println("    "+gBestLocation.getLoc());
	    return gBestLocation.getLoc();
	
	
	
	}
	
	private int[][] shuffle(int[][] loc1, int velo) {
		
		for(int k=0;k<velo;k++){
		    Random random = new Random();

		    for (int i = loc1.length - 1; i > 0; i--) {
		        for (int j = loc1[i].length - 1; j > 0; j--) {
		            int m = random.nextInt(i + 1);
		            int n = random.nextInt(j + 1);

		            int temp = loc1[i][j];
		            loc1[i][j] = loc1[m][n];
		            loc1[m][n] = temp;
		        }
		    }
		}
		// TODO Auto-generated method stub
		return loc1;
	}

	public void initializeSwarm(List<Vm> vmlist2, List<Cloudlet> cloudletList2, Datacenter datacenter2) {
		
		System.out.println("initialize Swarm is working");
		Particle p;
		int cloud_size = cloudletList2.size();
		int vmlist_size = vmlist2.size();
		for(int i=0; i<SWARM_SIZE; i++) {
			p = new Particle();
			
		
			
			// intitialization of location
		//	int[][] loc=new int[cloud_size][vmlist_size];
			
		/*	for(int x=0;x<cloud_size;x++)
			{
				for(int y=0;y<vmlist_size;y++)
				{
					loc[x][y]=0;
				}
			}
			
			if((i%2)==0)
			{
				for(int x=0;x<cloud_size;x++)
				{
					for(int y=0;y<vmlist_size;y++)
					{
						if(x==y)
						loc[x][y]=1;
					}
				}
			}
			
			if((i%2)!=0)
			{
				for(int x=0;x<cloud_size;x++)
				{
					for(int y=0;y<vmlist_size;y++)
					{
						if((x==y)&&(x>1))
						loc[x-1][y]=1;
					}
				}
			}
			
			*/
			int[][] loc = {{1,0,0,0,0,0},{0,0,1,0,0,0},{0,0,0,1,0,0},{0,0,0,0,1,0}};
			
			Location location = new Location(loc);
			
			
			
			//intialization of velocity
			int vel = (VEL_LOW + generator.nextInt(4)) * (VEL_HIGH - VEL_LOW);
			Velocity velocity = new Velocity(vel);
			
			p.setLocation(location);
			p.setVelocity(velocity);
			swarm.add(p);
			System.out.println("velocity and position of particle "+i+" is");
			System.out.println("Velocity: "+vel);
			System.out.println("Position: "+swarm.get(i).getLocation());
			
		}
		System.out.println("Initialize swarm is completed");
	}
	
	public void updateFitnessList(List<Vm> vmlist2, List<Cloudlet> cloudletList2, Datacenter datacenter2) {
		System.out.println("update fitness started");
		for(int i=0; i<SWARM_SIZE; i++) {
			fitnessValueList[i] = swarm.get(i).getFitnessValue(vmlist2,cloudletList2,datacenter2);
			System.out.println("fitness value of particle "+i+" is");
            System.out.println(fitnessValueList[i]);
		}
		System.out.println("Update fitness completed");
	}
}
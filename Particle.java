package org.cloudbus.cloudsim.examples;

import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.Vm;

public class Particle {
	
int[][] position;
int velocity;
int[][] localBestPosition;
double localBest;

DatacenterBroker broker;

public Particle()
{
	
	
}

public int[][] getPosition() {
	return position;
}
public void setPosition(int[][] position) {
	this.position = position;
}
public int getVelocity() {
	return velocity;
}
public void setVelocity(int velocity) {
	this.velocity = velocity;
}
public int[][] getLocalBestPosition() {
	return localBestPosition;
}
public void setLocalBestPosition(int[][] localBestPosition) {
	this.localBestPosition = localBestPosition;
}
public double getLocalBest() {
	return localBest;
}
public void setLocalBest(double localBest) {
	this.localBest = localBest;
}

public double evaluatePower(List<? extends Vm> vmlist2, List<? extends Cloudlet> cloudletList2, Datacenter datacenter2)
{
	return broker.calculatePower(position, vmlist2, cloudletList2, datacenter2);
}




}

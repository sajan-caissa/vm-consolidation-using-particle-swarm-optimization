package org.cloudbus.cloudsim.examples;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Vm;

public class Location {
	// store the Location in an array to accommodate multi-dimensional problem space
		private int[][] loc;
		private double fitnessValue;
		
		public Location(int[][] loc) {
			super();
			this.loc = loc;
		}

		public int[][] getLoc() {
			return loc;
		}

		public void setLoc(int[][] loc) {
			this.loc = loc;
		}
		
		
}

/*
 * FIXME LICENCE
 *
 * This file is part of AMES-TS.
 */
package util;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

import amesmarket.Support;

/**
 * Read the split load scenarios and dump them to a text.
 *
 * Supports Milestone1C for the ARPAe project.
 *
 * @author Sean L. Mooney
 *
 */
public class SplitLoadScenarioReader {

	String formatAsCSV(double[][][] loadscenarios) {
		StringBuilder sb = new StringBuilder();
		for (double[][] loadscenario : loadscenarios) {
			for (int hour = 0; hour < loadscenario.length; hour++) {
				int numZones = loadscenario[hour].length;
				for (int z = 0; z < numZones; z++) {
					sb.append(Double.toString(loadscenario[hour][z]));
					if (z < (numZones - 1)) {
						sb.append(", ");
					}
				}
				sb.append("\n");
			}
		}

		return sb.toString();
	}

	/**
	 * Read a load scenario contained completely in a single file.
	 * @throws FileNotFoundException
	 */
	double[][][] readAllIn1Scenario(File loadScenario) throws FileNotFoundException{
		class LPStruct {
			int h, d;
			double[] lp;
			LPStruct(int d, int h, double[] lp){
				this.h = h;
				this.d = d;
				this.lp = lp;
			}
		}
		Scanner s = null;
		final int dIdx = 0;
		final int hIdx = 1;
		final int zOffset = 2;
		final int hoursPerDay = 24;
		int maxDay = 0;
		ArrayList<LPStruct> allProfiles = new ArrayList<LPStruct>();
		double[][][] resProfs = null;
		try{
			s = new Scanner(loadScenario);
			boolean foundHeader = false;
			while(s.hasNextLine()) {
				String l = s.nextLine();
				if(foundHeader){
					String[] elems = l.split("\\s+");
					amesmarket.Support.trimAllStrings(elems);
					int day = Integer.parseInt(elems[dIdx]);
					int hour = Integer.parseInt(elems[hIdx]);
					double[] lp = new double[elems.length - zOffset];
					for(int z = zOffset ; z < elems.length; z++){
						lp[z - zOffset] = Support.parseDouble(elems[z]);
					}
					allProfiles.add(new LPStruct(day, hour, lp));

					if (day > maxDay) {
						maxDay = day;
					}
				} else {
					if (l.startsWith("Day")) {
						foundHeader = true;
					}
				}
			}

			resProfs = new double[maxDay][hoursPerDay][];
			for(LPStruct lps : allProfiles){
				resProfs[lps.d - 1][lps.h - 1] = lps.lp;
			}
		}
		finally{
			if (s != null) {
				s.close();
			}
		}

		return resProfs;
	}

	/**
	 * Read a LoadScenario split across several files.
	 * @param baseFile
	 * @return
	 * @throws Exception
	 */
	double[][][] readSplitDataScenario(File baseFile) throws Exception {
		int d = 1;
		int dmax = 364;
		double[][][] data = new double[dmax][][];
		while (d <= dmax) {
			File dayFile = new File(String.format("%s_%d.dat",
					baseFile.getPath(), d));
			try {
				data[d - 1] = this.readSingleDay(dayFile);
			} catch (Exception e) {
				System.err.println(String.format("Error reading %s",
						dayFile.getPath()));
				throw e;
			}
			d = d + 1;
		}

		return data;
	}

	double[][] readSingleDay(File inputFile) throws Exception {
		final int HOUR_IDX = 0;
		final int NUM_HOURS = 24;
		final int NUM_ZONES = 8;
		double[][] data = new double[NUM_HOURS][NUM_ZONES];
		System.err.print(String.format("reading %s\n", inputFile.getPath()));
		Scanner f = new Scanner(inputFile);
		//read until 'header' line
		int day = -1;
		boolean foundHeader = false;
		while (f.hasNextLine()) {
			String line = f.nextLine();
			line = line.trim();
			if ((line == null) || "".equals(line)) {
				continue;
			}
			if (!foundHeader) {
				if (line.startsWith("Day")) {
					String[] p = line.split(":");
					day = Integer.parseInt(p[1].trim());
				} else if (line.startsWith("Hour")) {
					foundHeader = true;
				}
			} else {
				//nothing like duplicating code...
				if (day < 1) {
					throw new Exception(String.format("Invalid day %d.", day));
				}
				String[] elems = line.split("\\s+");
				int hour = Integer.parseInt((elems[HOUR_IDX]));
				hour = hour - 1; // data file hours a 1 based.
				for (int z = HOUR_IDX + 1; z < elems.length; z++) {
					//off by one, to account for the hour column
					//                    System.err.println(String.format("Processing h:%d, z:%d",
					//                            hour, z));
					data[hour][z - 1] = Support.parseDouble(elems[z]);
				}
			}
		}

		return data;
	}

	void scaleData(double[][][] loadscenarios, double scalefactor) {
		for (int d = 0; d < loadscenarios.length; d++) {
			for (int hour = 0; hour < loadscenarios[d].length; hour++) {
				int numZones = loadscenarios[d][hour].length;
				for (int z = 0; z < numZones; z++) {
					loadscenarios[d][hour][z] *= scalefactor;
				}
			}
		}
	}

	public static void main(String[] args) throws Exception {
		double[][][] ls = null;
		double scaleFactor = 0;
		SplitLoadScenarioReader slr = new SplitLoadScenarioReader();

		if(args.length > 0) {
			//params for all in 1 file.
			if(args[0].equals("-1")){
				File f = new File("TEST-DATA/5BusBaseScenario1.dat");
				ls = slr.readAllIn1Scenario(f);
				scaleFactor = 0.3831897415347626;
			}
		} else { //do the defaults.

			File baseFile = new File("TEST-DATA/Ames_scenarios/AMESscen1");
			scaleFactor = 0.02616777936897704;

			ls = slr.readSplitDataScenario(baseFile);

		}

		slr.scaleData(ls, scaleFactor);
		System.out.println(slr.formatAsCSV(ls));
	}
}

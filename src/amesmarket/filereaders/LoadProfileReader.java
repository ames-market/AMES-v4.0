/* ============================================================================
 * AMES Wholesale Power Market Test Bed (Java): A Free Open-Source Test-Bed
 *         for the Agent-based Modeling of Electricity Systems
 * ============================================================================
 *
 * (C) Copyright 2008, by Hongyan Li, Junjie Sun, and Leigh Tesfatsion
 *
 *    Homepage: http://www.econ.iastate.edu/tesfatsi/AMESMarketHome.htm
 *
 * LICENSING TERMS
 * The AMES Market Package is licensed by the copyright holders (Junjie Sun,
 * Hongyan Li, and Leigh Tesfatsion) as free open-source software under the
 * terms of the GNU General Public License (GPL). Anyone who is interested is
 * allowed to view, modify, and/or improve upon the code used to produce this
 * package, but any software generated using all or part of this code must be
 * released as free open-source software in turn. The GNU GPL can be viewed in
 * its entirety as in the following site: http://www.gnu.org/licenses/gpl.html
 */

package amesmarket.filereaders;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;

import amesmarket.DailyLoadProfile;
import amesmarket.LoadProfileCollection;
import amesmarket.Support;

/**
 * Read in a data file represting the load profile for each our of each day.
 *
 * Usage protocol: Instantiate, read, close
 *
 * Reading a loadProfile is non reentrant. Once the profile is read
 * it cannot be re-read.
 *
 * Client is responsible for closing the underlying input stream, if appropriate.
 *
 * <p>
 * Note: This class is not an AbstractConfigFileReader for historical reasons.
 * I could probably be refactor as extending that class. But it's not really
 * worth the effort.
 *
 * @author Sean L. Mooney
 *
 *
 *
 *
 */
public class LoadProfileReader {

	private final Scanner loadProfileReader;
	private String currentLine = null;

	/**
	 *
	 * @param loadProfileFile
	 * @throws FileNotFoundException
	 */
	public LoadProfileReader(final File loadProfileFile) throws FileNotFoundException {
		this.loadProfileReader = new Scanner(loadProfileFile);
	}

	/**
	 *
	 * @param loadProfileReader, may not be null.
	 * @throws IllegalArgumentException if loadProfileReader is null.
	 */
	public LoadProfileReader(final InputStream loadProfileStream) {
		if(loadProfileStream == null) {
			throw new IllegalArgumentException("Reader may not be null");
		}
		this.loadProfileReader = new Scanner(loadProfileStream);
	}

	/**
	 * Read in load profiles until the end of the file/reader stream.
	 *
	 * @param expectedEntries expected number of entries in each line of the data stream.
	 * @return a list of load profiles.
	 */
	public LoadProfileCollection readLoadProfileData(int expectedEntries) {
		LoadProfileCollection slp = new LoadProfileCollection();

		this.move();

		//if not day, error. FIXME
		while(this.currentLine != null) {
			int currentDay = this.getDay(this.currentLine);
			DailyLoadProfile lp = this.readDayLoadProfile(expectedEntries);
			lp.setDayNumber(currentDay);
			slp.put(lp);
		}

		return slp;
	}

	/**
	 * Read in the load profile for a single day.
	 *
	 * @param expectedEntries expected number of entries in the load profile
	 * @return
	 */
	protected DailyLoadProfile readDayLoadProfile(int expectedEntries) {
		ArrayList<double[]> loads = new ArrayList<double[]>();

		this.move();
		while( (this.currentLine != null) && !this.isDayMarker(this.currentLine)) {
			//if null, or Day <int> return

			//else, split the line in to its parts, create double array and add it
			String[] lineParts = this.currentLine.split("\\s+");

			if(lineParts.length == expectedEntries) {
				double[] hourlyLoadProfileByLSE = new double[expectedEntries];
				for(int i = 0; i < expectedEntries; i++) {
					hourlyLoadProfileByLSE[i] = Support.parseDouble(lineParts[i]);
				}

				loads.add(hourlyLoadProfileByLSE);
			} else {
				//FIXME: Report an error for the line.
			}

			this.move();
		}
		//done with day
		return new DailyLoadProfile(loads);
	}

	protected boolean isDayMarker(String s) {
		return s.startsWith("Day");
	}

	protected int getDay(String s) {
		String[] elems = s.split("\\s+"); //split on whitespace.

		//if not 2 indexes error. FIXME
		return Integer.parseInt(elems[1]);
	}

	/**
	 * Move to the next line in the file.
	 *
	 * Trims the whitespace off of the read in line.
	 */
	protected void move() {
		//TODO-X: Protocol for unexpected end of file?
		//FIXME-X: Strip Comments from the end of the line.
		if(this.loadProfileReader.hasNextLine()) {
			this.currentLine = this.loadProfileReader.nextLine().trim();
		} else {
			this.currentLine = null;
		}
	}

	/**
	 * Closes the underlying {@link InputStream}.
	 * @throws IOException
	 */
	public void close() throws IOException {
		this.loadProfileReader.close();
	}
}

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

package amesmarket;

import java.util.Arrays;
import java.util.List;

/**
 * Model the load profile for a particular day.
 *
 * Hours are 0 indexed. A standard day goes from 0 to 23.
 *
 * @author Sean L. Mooney
 *
 */
public class DailyLoadProfile {

    /**
     * Load profiles for each hour of a single day.
     */
    final private double[][] loadProfiles;

    /**
     * The day this object represents.
     */
    private int dayNumber = 0;

    private LoadType loadType = LoadType.LOAD;

    /**
     * Create a new LoadProfile with the default size of 24.
     *
     * Produces one slot for each hour of a standard day.
     */
    public DailyLoadProfile() {
        this(24); //Determines default size of daily load profile 
    }

    /**
     * Copy Constructor.
     * @param other
     */
    public DailyLoadProfile(DailyLoadProfile other){
        this(other.loadProfiles.length);
        for(int p = 0; p < other.loadProfiles.length; p++){
            double[] otherProfiles = other.loadProfiles[p];
            loadProfiles[p] = Arrays.copyOf(otherProfiles, otherProfiles.length);
        }
        this.dayNumber = other.dayNumber;
    }

    /**
     *
     * @param size - number of load profiles to store.
     */
    public DailyLoadProfile(int size) {
        loadProfiles = new double[size][];
    }

    /**
     * Create a load profile from the given list.
     * @param profiles - must not be null.
     */
    public DailyLoadProfile(List<double[]> profiles) {
        this(profiles.size());
        int size = profiles.size();
        for(int h = 0; h < size; h++){
            //Default/implied behavior was alias not copy. Maintain that semantic for this constructor.
            setLoadByHour(h, profiles.get(h), false); //TODO-X: does this need to copy the memory?
        }
    }

    /**
     *
     * @param profiles
     * @param copy
     */
    public DailyLoadProfile(double[][] profiles, boolean copy){
        this(profiles.length);
        for(int h = 0; h < profiles.length; h++){
            setLoadByHour(h, profiles[h], copy);
        }
    }

    /**
     * Get the load profile for hour h.
     * @param h range: [0, size-1]
     * @return
     */
    public double[] getLoadByHour(int h) {
        if(checkHourBound(h)) {
            return loadProfiles[h];
        } else {
            return null;
        }
    }

    /**
     * Set the load profile for hour h, without copying the array.
     * See: {@link #setLoadByHour(int, double[], boolean)}.
     *
     * TODO-X: Is not copy loadProfile the sensible semantic? It matches all the current
     * uses when this method was added. Look at users to see if copying would cause a problem.
     * @param h
     * @param loadProfile
     */
    public void setLoadByHour(int h, double[] loadProfile){
        setLoadByHour(h, loadProfile, false);
    }

    /**
     * Set the load profile for hour h.
     * @param h range: [0, size-1]
     * @param loadProfile load profile, each element represents a single zone's load.
     * @param copy whether or not make a copy of the array.
     * @return
     * @throws IllegalArgumentException if h is out of bounds
     */
    public void setLoadByHour(int h, double[] loadProfile, boolean copy) {
        if(checkHourBound(h))
            if(copy)
                loadProfiles[h] = Arrays.copyOf(loadProfile, loadProfile.length);
            else
                loadProfiles[h] = loadProfile;
        else
            throw new IllegalArgumentException("Hour " + h + " out of bounds");
    }



    /**
     * @return the dayNumber
     */
    public int getDayNumber() {
        return dayNumber;
    }

    /**
     * @return number of hours this day represents, or 0 if
     */
    public int getNumHours() {
        return (loadProfiles != null) ? loadProfiles.length : 0;
    }

    /**
     * @param dayNumber the dayNumber to set
     * @throws IllegalArgumentException if dayNumber < 1
     */
    public void setDayNumber(int dayNumber) {
        if(dayNumber < 1){
            throw new IllegalArgumentException("dayNumber may not be less than 1");
        }
        this.dayNumber = dayNumber;
    }

    private boolean checkHourBound(int h) {
        return (h >= 0) && (h < loadProfiles.length);
    }

    private boolean checkZoneBounds(int zNum) {
        //assumes that each loadProfile[i] has the same length
        return (zNum >= 0) && (zNum < loadProfiles[0].length);
    }

    /**
     * Get the load at zone/LSE zNum for EACH hour of the day.
     *
     * @param zNum zNum >= 0 && zNum < total_zones.
     * @return and array represent the load for each hour of the day for zone zNum.
     * @throws IllegalArgumentException if zNum out of bounds.
     */
    public double[] getZoneLoad(int zNum) {
        if(!checkZoneBounds(zNum))
            throw new IllegalArgumentException("Invalid zone: " + zNum);

        double[] zoneLoad = new double[loadProfiles.length];
        for(int h = 0; h<zoneLoad.length; h++){
            zoneLoad[h] = loadProfiles[h][zNum];
        }

        return zoneLoad;
    }

    /**
     * Find the peak load for all hours in the LoadProfile.
     *
     * @return
     */
    public double peakHourLoad() {
        double peakTotalLoad = Double.MIN_NORMAL;
        int numHours = loadProfiles.length;
        for (int h = 0; h < numHours; h++) {
            double localTotalLoad = Support.sumArray(loadProfiles[h]);
            peakTotalLoad = (localTotalLoad > peakTotalLoad) ? localTotalLoad : peakTotalLoad;
        }
        return peakTotalLoad;
    }

    /**
     * Multiply each load (hour/zone) by the scale factor.
     *
     * @param scaleFactor
     */
    public void scaleProfile(double scaleFactor) {
        for(int h = 0; h < loadProfiles.length ; h++)
            for(int z = 0; z < loadProfiles[h].length ; z++)
                loadProfiles[h][z] = loadProfiles[h][z]*scaleFactor;
    }
    
    public void subtractFromLoad(DailyLoadProfile other) {
        //aliases for local access.
        //TODO-XXX: Check same length!
        final double[][] myLoads = loadProfiles;
        final double[][] otherLoads = other.loadProfiles;
        
        for(int h = 0; h < myLoads.length ; h++)
            for(int z = 0; z < myLoads[h].length ; z++) {
                myLoads[h][z] = myLoads[h][z] - otherLoads[h][z];
                if(myLoads[h][z] < 0) {
                    System.err.println("WARNING: Allowing Negative load hour " + (h+1) + ", zone " + (z+1) );
                }
            }
    }

    /**
     * @return the loadType
     */
    public LoadType getLoadType() {
        return loadType;
    }

    /**
     * @param loadType the loadType to set
     */
    public void setLoadType(LoadType loadType) {
        this.loadType = loadType;
    }



    /**
     * What 'type' of load this profile models.
     * Marker for 'regular' load, or wind/photovoltaic that
     * is effectively negative load.
     *
     * @author Sean L. Mooney
     *
     */
    public static enum LoadType {
        /**
         * 'Base' load
         */
        LOAD,
        /**
         * Negative load from wind generation
         */
        WIND,
        /**
         * Negative load from Photovoltaics
         */
        PV
    }
}

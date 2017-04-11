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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import amesmarket.filereaders.BadDataFileFormatException;
import amesmarket.probability.IChoice;


/**
 * Model the entire collection of load profiles for a simulation.
 *
 * @author Sean L. Mooney
 *
 */
public class LoadProfileCollection implements IChoice{


    private String caseName = null;
    private int scenarioNumber = -1;
    private double scenarioProb = 0.0d;

    /**
     * A map from a day, d,  to the load profile for the day.
     */
    private final HashMap<Integer, DailyLoadProfile> loadProfiles;

    /**
     *
     */
    public LoadProfileCollection() {
        loadProfiles = new HashMap<Integer, DailyLoadProfile>();
    }

    /**
     * Construct a full load scenario (all hours of all days in the scenario).
     *
     * @param caseName
     * @param scenarioNumber
     * @param scenarioProb
     * @param loadScenarioData day x hour x zone loads. Assumes data is arranged from day 1 to DAY_MAX.
     * @param copyProfiles if true, will copy each array. See: {@link DailyLoadProfile#setLoadByHour(int, double[], boolean)}.
     *
     * This constructor <em>does not</em> validate the data. Call {@link #validate(String, int, int)},
     * if need to make sure LoadScenario is correct.
     */
    public LoadProfileCollection(String caseName, int scenarioNumber, double scenarioProb,
            double[][][] loadScenarioData, boolean copyProfiles){
        this();
        this.caseName = caseName;
        this.scenarioNumber = scenarioNumber;
        this.scenarioProb = scenarioProb;

        int dc = 0;
        for(double[][] dayProfiles : Arrays.asList(loadScenarioData)){
            ++dc; //start by incrementing the day counter.
            DailyLoadProfile dlp = new DailyLoadProfile(dayProfiles, copyProfiles);
            dlp.setDayNumber(dc); //increment the day and set the result.
            put(dlp);
        }
    }

    /**
     * Copy Constructor.
     *
     * @param other
     */
    public LoadProfileCollection(LoadProfileCollection other) {
        caseName = other.caseName;
        scenarioNumber = other.scenarioNumber;
        scenarioProb = other.scenarioProb;
        loadProfiles = new HashMap<Integer, DailyLoadProfile>(other.loadProfiles.size());
        for(Integer key : other.loadProfiles.keySet()){
            loadProfiles.put(key, new DailyLoadProfile(other.loadProfiles.get(key)));
        }
    }

    public double peakTotalLoad() {
        double peakTotalLoad = Double.MIN_NORMAL;
        // TODO MOVE UP
        for(DailyLoadProfile dlp : loadProfiles.values()) {
            double localTotalLoad = dlp.peakHourLoad();
            peakTotalLoad = (localTotalLoad > peakTotalLoad) ? localTotalLoad : peakTotalLoad;
        }

        return peakTotalLoad;
    }

    public void scaleScenario(double scaleFactor) {
        for(DailyLoadProfile dlp : loadProfiles.values())
            dlp.scaleProfile(scaleFactor);
    }

    public void subtractLoad(LoadProfileCollection other) {
        if(other == null) return;

        final HashMap<Integer, DailyLoadProfile> otherLoadProfiles = other.loadProfiles;
        for(Integer k :  loadProfiles.keySet()) {
            DailyLoadProfile l = loadProfiles.get(k);
            DailyLoadProfile o = otherLoadProfiles.get(k);
            l.subtractFromLoad(o);
        }
    }

    /**
     * Store a {@link DailyLoadProfile}.
     *
     * Stores the DailyLoadProfile at the value of {@link DailyLoadProfile#getDayNumber()}.
     *
     * If the loadProfile for the currentDay exists, this will replace the
     * old profile with the current one.
     *
     * @param currentDay
     */
    public void put(DailyLoadProfile lp) {
        loadProfiles.put(lp.getDayNumber(), lp);
    }

    /**
     * Get the {@link DailyLoadProfile} for the day.
     *
     * @param day
     * @return a DailyLoadProfile, or null if no profile exists for the day.
     */
    public DailyLoadProfile get(int day) {
        return loadProfiles.get(day);
    }

    /**
     * @return the caseName
     */
    public String getCaseName() {
        return caseName;
    }

    /**
     * @param caseName the caseName to set
     * @throws IllegalArgumentException if caseName is null.
     */
    public void setCaseName(String caseName) {
        if(caseName == null)
            throw new IllegalArgumentException();

        this.caseName = caseName;
    }

    /**
     * @return the scenarioNumber
     */
    public int getScenarioNumber() {
        return scenarioNumber;
    }

    /**
     * @param scenarioNumber the scenarioNumber to set
     * @throws IllegalArgumentException if scenarioNumber < 1 and not the expected load marker.
     */
    public void setScenarioNumber(int scenarioNumber) {
        if(scenarioNumber < 1
                && scenarioNumber != LoadCaseControl.EXPECTED_LOAD_SCEN_NUM
                && scenarioNumber != LoadCaseControl.ACTUAL_LOAD_SCEN_NUM
                )
            throw new IllegalArgumentException();

        this.scenarioNumber = scenarioNumber;
    }

    /**
     * Get all the profiles, sort by hour.
     * @return
     */
    public List<DailyLoadProfile> getAllProfiles() {
        List<DailyLoadProfile> dlps = new ArrayList<DailyLoadProfile>(loadProfiles.values());

        Collections.sort(dlps, new Comparator<DailyLoadProfile>() {
            @Override
            public int compare(DailyLoadProfile o1, DailyLoadProfile o2) {
                int dn1, dn2;
                dn1 = o1.getDayNumber();
                dn2 = o2.getDayNumber();
                return (dn1 < dn2) ? -1 : (dn1 == dn2) ? 0 : 1;
            }
        });

        return dlps;
    }

    /**
     * @return the scenarioProb
     */
    @Override
    public double probability() {
        return scenarioProb;
    }

    /**
     * @param scenarioProb the scenarioProb to set
     * @throws IllegalArgumentException if scenarioProb < 0 || scenarioProb > 1.
     */
    public void setScenarioProb(double scenarioProb) {
        if(scenarioProb < 0 || scenarioProb > 1){
            throw new IllegalArgumentException();
        }

        this.scenarioProb = scenarioProb;
    }

    /**
     * Check to make sure this LoadScenario respects the
     * specifications of the loadCaseControl.
     *
     * @param exCaseName expected CaseName
     * @param exScenNum expected Scenario Number
     * @param exDays expected number of days in the scenario
     * @throws BadDataFileFormatException if the scenario does not validate.
     * Throwing an exception allows more information about the reason the
     * validation failed to be sent back.
     */
    public void validate(String exCaseName, int exScenNum, int exDays)
            throws BadDataFileFormatException {
        //CaseName matches
        if (!exCaseName.equals(caseName)) {
            throw new BadDataFileFormatException("Expected CaseName: "
                    + exCaseName + " but found " + caseName);
        }

        //Scenario number matches
        if (exScenNum != scenarioNumber) {
            String expectedScenNum = (scenarioNumber == LoadCaseControl.EXPECTED_LOAD_SCEN_NUM) ? "ExpectedLoad" : Integer.toString(scenarioNumber);
            throw new BadDataFileFormatException("Expected Scenario "
                    + exScenNum + " but found Scenario " + expectedScenNum);
        }

        //correct number of days
        if (exDays != loadProfiles.size()) {
            String scenarioDesc = "";
            //adjust for ExpectedLoad
            if(scenarioNumber == LoadCaseControl.EXPECTED_LOAD_SCEN_NUM){
                scenarioDesc = "ExpectedLoad";
            }else{
                scenarioDesc = "scenario " + scenarioNumber;
            }

            throw new BadDataFileFormatException(
                    "CaseName: " + caseName + ", " + scenarioDesc
                    + "-- Expected " + exDays
                    + " days but found " + loadProfiles.size() + " days.");
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        toString(sb);
        return sb.toString();
    }

    public void toString(StringBuilder sb) {
        double[] loads;
        for(DailyLoadProfile dlp : getAllProfiles()) {
            sb.append(String.format("Day %d\n", dlp.getDayNumber()));
            for(int h = 0; h<dlp.getNumHours(); h++) { //FIXME: Hardcoded 24
                sb.append(String.format("H-%d ", h+1));
                loads = dlp.getLoadByHour(h);
                if(loads == null){
                    sb.append("NO LOADS!!!\n");
                    continue;
                }
                int m = loads.length;
                for(int i = 0; i < m; i++){
                    sb.append(loads[i]);
                    if(i < m-1)
                        sb.append(", ");
                }
                sb.append("\n");
            }
        }
    }
}

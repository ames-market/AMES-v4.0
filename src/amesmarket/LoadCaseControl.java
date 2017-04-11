//FIXME-XXX LICENSE DECLARATION

package amesmarket;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import amesmarket.DailyLoadProfile.LoadType;
import amesmarket.filereaders.BadDataFileFormatException;
import amesmarket.filereaders.IZoneIndexProvider;
import amesmarket.filereaders.LoadCaseScenarioReader;

/**
 * Model of the LoadCase Control File.
 *
 * Scenarios are referenced by their 'userland' number.
 * Scenario 1 in the file is Scenario 1 in the getters/setters.
 *
 * The design of this class assumes 'file-based' scenarios. It should
 * be relatively easy, but is not implemented, to use other input sources
 * instead of files (e.g. a stream from the somewhere.)
 *
 * @author Sean L. Mooney
 */
public class LoadCaseControl {

    /**
     * A marker/constant to mark a scenario number as the ExpectedLoad
     * scenario.
     */
    public static final int EXPECTED_LOAD_SCEN_NUM = -100;

    public static final int ACTUAL_LOAD_SCEN_NUM = -200;
    
    

    private String caseName;
    private int numZones;
    private int numLoadScenarios;
    private int numIntervalsInSim = 24;
    /**
     * First day of data. Assume we always start at day 1 right now.
     * TODO-X: find all loops over days and use this as initial value.
     * TODO: add getter/setter for outside world.
     */
    private int startDay = 1;
    private int maxDay;

    /**
     * The scenarioInfo map acts as a cache for the LoadScenario data.
     * unless iterating through the elements in the map, LoadScenarioInstances
     * should ONLY be accessed via the getLoadScenario method. This method
     * ensures the LoadScenarioInstance objects are set up correctly.
     *
     * The scenarioInfo map stores both the 'real' LoadScenarios and
     * the ExpectedLoadScenarion information. The Expected load is
     * keyed with the {@link LoadCaseControl#EXPECTED_LOAD_SCEN_NUM}, and in a
     * few cases is treated specially, but mostly acts as another LoadScenario.
     */
    private Map<Integer, LoadScenarioInstance> scenarioInfo;

    private IZoneIndexProvider zoneNameIndexes;

    /**
     * Default constructor.
     *
     * Initialize all fields to 'unusable' data for AMES.
     * (e.g. negative numbers and nulls).
     */
    public LoadCaseControl(){
        this(null, -1, -1, -1, null, 24);
    }

    /**
     * @param caseName
     * @param numZones
     * @param numLoadScenarios
     * @param maxDay
     */
    public LoadCaseControl(String caseName, int numZones, int numLoadScenarios,
            int maxDay, IZoneIndexProvider zoneNameIndexes, int numIntervalsInSim) {
        this.caseName = caseName;
        this.numZones = numZones;
        this.numLoadScenarios = numLoadScenarios;
        this.maxDay = maxDay;
        this.scenarioInfo = new TreeMap<Integer, LoadCaseControl.LoadScenarioInstance>();
        this.zoneNameIndexes = zoneNameIndexes;
        this.numIntervalsInSim = numIntervalsInSim;
    }

    /**
     * Default version of validate, uses 1e-6 as the tolerance.
     * @throws BadDataFileFormatException
     */
    public void validate() throws BadDataFileFormatException {
        validate(1e-6);
    }

    //TODO: Error messages!
    /**
     * Validate the data set in the constructor.
     * @throws BadDataFileFormatException
     */
    public void validate(double probTol) throws BadDataFileFormatException{
        //Case names may not be null and may not be empty.
        if (caseName == null || "".equals(caseName))
            throw new BadDataFileFormatException("CaseName may not be empty");

        //TODO-X: validation? 0-9* or > 0?
        if(numZones < 1){
            throw new BadDataFileFormatException("NumZones may not be less than 1");
        }

        //TODO-X: Is this true? At least 1?
        if(numLoadScenarios < 1){
            throw new BadDataFileFormatException("NumLoadScenarios may not be less than 1");
        }

        //Make sure there is something (file/prob) that was read for each Scenario.
        for(int sn = 1; sn <= numLoadScenarios; sn++){
            if(!scenarioInfo.containsKey(sn)) {
                throw new BadDataFileFormatException(
                        "Scenario " + sn + " not found in the control file.\n"
                        + "Must specifiy probability and scenario file for each "
                        + "scenario from 1 to " + Integer.toString(numLoadScenarios)
                        );
            }
        }

        if(maxDay < 1){
            throw new BadDataFileFormatException("MaxDay must be at least 1");
        }

        Set<Integer> siKeys = scenarioInfo.keySet();

        System.out.println( "Checking daily scenario probability sum with tolerance " + probTol );
        for(int day = 1; day < maxDay; day++) {
            //now check the probs. sum to 1. And that there are no negative probs.
            double totalProb = 0.0;
            for(Integer i : siKeys){
                if (i == LoadCaseControl.EXPECTED_LOAD_SCEN_NUM
                        || i == LoadCaseControl.ACTUAL_LOAD_SCEN_NUM) {
                    continue; //no probability for the Expected or Actual Load File
                }

                String scenDesc = lpcollectionDesc(i);
                LoadScenarioInstance lsi = getLoadScenarioInstance(i);
                double scenarioProb = lsi.getScenarioProb(day);
                if( !lsi.isScenPropSet() ){
                    throw new BadDataFileFormatException(
                            "No probability set for " + scenDesc);
                }else if(scenarioProb < 0){
                    //TODO-X: Decimal format on scenario prob.
                    throw new BadDataFileFormatException(
                            "Invalid probability of " + scenarioProb +
                            " for " + scenDesc +
                            " may not be less than 0.0");
                }else if (scenarioProb > 1){
                    throw new BadDataFileFormatException(
                            "Invalid probability of " + scenarioProb +
                            " for " + scenDesc +
                            " may not be greater than 1.0");
                }
                totalProb += scenarioProb;
            }
            //TODO-X: Decimal format. Follow conventions used in AMESGUIFrame.
            if (Support.doubleIsDifferent(1.0, totalProb, probTol)) {
                throw new BadDataFileFormatException(
                        "Expected all scenario probabilities to sum to 1.0, but the"
                                + " sum on day " + day + " is actually " + totalProb);
            } else {
                System.out.println("Total probability for day " + day + ": " + totalProb);
                //FIXME: Delete this once Woodruff's group is generate better data.
                double actualDiff = totalProb - 1.0;
                if( Math.abs(actualDiff) > 1e-6 ) { //1e-6 is the tolerance we have, even if the data is less good than that
                    System.err.println( "WARNING Total probability is not with 1e6 of 1.0. Adjusting scenario 1 by " + actualDiff );
                    LoadScenarioInstance scenToAdj = getLoadScenarioInstance(1);
                    double s1Prob = scenToAdj.getScenarioProb(day);
                    double s1ProbAdj = s1Prob - actualDiff;
                    scenToAdj.setScenarioProb(day, s1ProbAdj);
                    System.err.println("Adjusted scenario1 probability for day " + day
                            + " from " + s1Prob + " to " + s1ProbAdj + ".");
                }
            }
        }

        //And make sure all of the files exist.
        for(Integer i : siKeys){
//            if(i == LoadCaseControl.EXPECTED_LOAD_SCEN_NUM){
//                continue; //Check on the expected load later.
//            }
            String scenDesc;
            scenDesc = lpcollectionDesc(i);

            LoadScenarioInstance lsi = getLoadScenarioInstance(i);

            for( LoadScenarioDay lsd : lsi.dailyParams.values()) {
                File scenarioFile = lsd.scenarioLoadFile;
                File windFile = lsd.scenarioWindFile;
                if(scenarioFile == null){
                    throw new BadDataFileFormatException(
                            "No file specified for scenario " + scenDesc
                            + " day " + lsd.scenarioDay );
                }

                if( !scenarioFile.exists() ){
                    throw new BadDataFileFormatException("Scenario " + scenDesc
                            + "load file " + scenarioFile.getPath() + " does not exist");
                }

                if( !scenarioFile.canRead()) {
                    throw new BadDataFileFormatException("Scenario " + scenDesc
                            + "load file " + scenarioFile.getPath() + " is not readable.");
                }

                if( windFile != null ) {
                    if( !windFile.exists() ){
                        throw new BadDataFileFormatException("Scenario " + scenDesc
                                + "wind file " + scenarioFile.getPath() + " does not exist");
                    }

                    if( !windFile.canRead()) {
                        throw new BadDataFileFormatException("Scenario " + scenDesc
                                + " wind file " + scenarioFile.getPath() + " is not readable.");
                    }
                }
            }
        }
    }

    /**
     * Get the load scenario info for scenario i.
     *
     * If we have seen this scenario before, return it.
     * Otherwise, create it, put it in the list and return it.
     *
     * @param i
     * @return scenario instance for i. Will not be null!
     */
    private LoadScenarioInstance getLoadScenarioInstance(int i){
        LoadScenarioInstance si = scenarioInfo.get(i);

        if(si == null){
            si = new LoadScenarioInstance(i);
            scenarioInfo.put(i, si);
        }

        return si;
    }

    /**
     * @param caseName the caseName to set
     */
    public void setCaseName(String caseName) {
        this.caseName = caseName;
    }

    /**
     * @return the caseName
     */
    public String getCaseName() {
        return caseName;
    }

    /**
     * @param numZones the numZones to set
     */
    public void setNumZones(int numZones) {
        this.numZones = numZones;
    }

    /**
     * @return the numZones
     */
    public int getNumZones() {
        return numZones;
    }

    /**
     * @param numLoadScenarios the numLoadScenarios to set
     */
    public void setNumLoadScenarios(int numLoadScenarios) {
        this.numLoadScenarios = numLoadScenarios;
    }

    /**
     * @return the numLoadScenarios
     */
    public int getNumLoadScenarios() {
        return numLoadScenarios;
    }

    /**
     * @param maxDay the maxDay to set
     */
    public void setMaxDay(int maxDay) {
        this.maxDay = maxDay;
    }

    /**
     * @return the maxDay
     */
    public int getMaxDay() {
        return maxDay;
    }
    
    public void setNumIntervalsInSim(int numIntervalsInSim) {
        this.numIntervalsInSim = numIntervalsInSim;    
    }

    public int getNumIntervalsInSim() {
        return numIntervalsInSim;
    }

    public void setZoneNameIndexes(IZoneIndexProvider zip) {
        this.zoneNameIndexes = zip;
    }

    public IZoneIndexProvider getZoneNameIndexes() {
        return this.zoneNameIndexes;
    }

    /**
     * Set the same scenario file name for all days in this scenario.
     * @param sn
     * @param scenarioFileName
     */
    public void setAllScenarioFilePaths(int sn, String scenarioFileName) {
        for(int day = startDay; day <= maxDay; day++){
            setScenarioDayFilePath(day, sn, scenarioFileName);
        }
    }

    /**
     * Set the same wind file name for all days in this scenario.
     * @param sn
     * @param scenarioFileName
     */
    public void setAllScenarioWindFilePaths(int sn, String scenarioFileName) {
        for(int day = startDay; day <= maxDay; day++){
            setScenarioDayWindFilePath(day, sn, scenarioFileName);
        }
    }

    /**
     * Set the name of scenario file used for day d, scenario i.
     * @param day scenario day
     * @param sn scenario number User level indexed Valid range is 1 to numLoadScenarios.
     * @param scenarioFileNames the scenarioFileNames to set
     * @throws IllegalArgumentException if i is out of bounds.
     */
    public void setScenarioDayFilePath(int day, int sn, String scenarioFileName) {
        if(isInvalidScenarioNumber(sn) || isInvalidDayNumber(day))
            throw new IllegalArgumentException();

        getLoadScenarioInstance(sn).getLoadScenarioDay(day).scenarioLoadFile = new File(scenarioFileName);
    }

    /**
     * Set the name of scenario file used for day d, scenario i.
     * @param day scenario day
     * @param sn scenario number User level indexed Valid range is 1 to numLoadScenarios.
     * @param scenarioFileNames the scenarioFileNames to set
     * @throws IllegalArgumentException if i is out of bounds.
     */
    public void setScenarioDayWindFilePath(int day, int sn, String scenarioFileName) {
        if(isInvalidScenarioNumber(sn) || isInvalidDayNumber(day))
            throw new IllegalArgumentException();

        getLoadScenarioInstance(sn).getLoadScenarioDay(day).scenarioWindFile = new File(scenarioFileName);
    }

    /**
     * Get the name of a single scenario file.
     *
     * This method is intended for testing/error messages. Use the method
     * {@link #getLoadScenarioInstance(int)} to get an individual load scenario.
     * @param day day number
     * @param i scenario number. 1 indexed. Valid range is 1 to numLoadScenarios.
     * @return the scenarioFileNames
     * @throws IllegalArgumentException if i is out of bounds.
     */
    public String getScenarioFilePath(int day, int i) {
        if(isInvalidScenarioNumber(i) || isInvalidDayNumber(day))
            throw new IllegalArgumentException();

        File scenFile = getLoadScenarioInstance(i).getLoadScenarioDay(day).scenarioLoadFile;
        return scenFile == null ? null : scenFile.getPath();
    }

    /**
     * Get the name of a single scenario file.
     *
     * This method is intended for testing/error messages. Use the method
     * {@link #getWindScenarioInstance(int)} to get an individual load scenario.
     * @param day day number
     * @param i scenario number. 1 indexed. Valid range is 1 to numLoadScenarios.
     * @return the scenarioFileNames
     * @throws IllegalArgumentException if i is out of bounds.
     */
    public String getScenarioWindFilePath(int day, int i) {
        if(isInvalidScenarioNumber(i) || isInvalidDayNumber(day))
            throw new IllegalArgumentException();

        File scenFile = getLoadScenarioInstance(i).getLoadScenarioDay(day).scenarioWindFile;
        return scenFile == null ? null : scenFile.getPath();
    }

    /**
     * Get the load scenario
     * @param i
     * @return
     * @throws BadDataFileFormatException
     */
    public LoadProfileCollection getLoadScenario(int i) throws BadDataFileFormatException{
        if(isInvalidScenarioNumber(i))
            throw new IllegalArgumentException();

        return getLoadScenarioInstance(i).getScenario();
    }

    /**
     * Returns an list with all the scenarios.
     * Does not include the expected load in the list.
     * @return
     * @throws BadDataFileFormatException
     */
    public List<LoadProfileCollection> getAllLoadScenarios() throws BadDataFileFormatException {
        ArrayList<LoadProfileCollection> l = new ArrayList<LoadProfileCollection>();
        for(int sn = 1; sn <= numLoadScenarios; sn++)
            l.add(getLoadScenario(sn));

        return l;
    }

    /**
     * Set the same probability for all days of the scenario
     * @param sn scenario number
     * @param scenarioProbability
     * @throws IllegalArgumentException {@link #setScenarioDayProbability}
     */
    public void setAllScenarioProbabilities(int sn, double scenarioProbability) {
        for(int day = startDay; day <= maxDay; day++){
            setScenarioDayProbability(day, sn, scenarioProbability);
        }
    }

    /**
     * @param scenario day
     * @param sn see {@link #getScenarioFilePath(int) } for range restriction
     * @param scenarioProbabilities set the probability of choosing scenario sn as the 'true' load for the run.
     * @throws IllegalArgumentException
     */
    public void setScenarioDayProbability(int day, int sn, double scenarioProbability) {
        if(isInvalidProbability(scenarioProbability)){
            throw new IllegalArgumentException("Invalid probability of " + scenarioProbability);
        }
        if(isInvalidScenarioNumber(sn)) {
            throw new IllegalArgumentException(
                    "Scenario " + sn + " out of bounds. Must be between 1 and " + numLoadScenarios
                    );
        }
        if(isInvalidDayNumber(day)){
            throw new IllegalArgumentException("Day " + day + " out of bounds. " +
                    "Must be bewteen 1 and " + maxDay);
        }
        getLoadScenarioInstance(sn).getLoadScenarioDay(day).scenarioProb = scenarioProbability;
    }

    /**
     *
     * @param sn see {@link #getScenarioFilePath(int) } for range restriction
     * @return
     * @throws IllegalArgumentException
     */
    public double getScenarioProbability(int sn){
        if(isInvalidScenarioNumber(sn)) {
            throw new IllegalArgumentException();
        }

        return getLoadScenarioInstance(sn).getScenarioProb();
    }

    /**
     *
     * @param day day number
     * @param scenario number - see {@link #getScenarioFilePath(int) } for range restriction
     * @return
     * @throws IllegalArgumentException
     */
    public double getScenarioProbability(int day, int sn){
        if(isInvalidScenarioNumber(sn) || isInvalidDayNumber(day)) {
            throw new IllegalArgumentException();
        }
        return getLoadScenarioInstance(sn).getLoadScenarioDay(day).scenarioProb;
    }

    /**
     * @param sn see {@link #getScenarioFilePath(int) } for range restriction
     * @return
     */
    public boolean hasScenarioProbality(int sn) {
        return hasScenarioProbality(1, sn);
    }

    /**
     * @param day must be between 1 and MaxDay
     * @param sn scenario number - see {@link #getScenarioFilePath(int) } for range restriction
     * @return true a probability has been assigned to the day.
     */
    public boolean hasScenarioProbality(int day, int sn) {
        if (isInvalidScenarioNumber(sn)) {
            throw new IllegalArgumentException( "Invalid scenario number " + lpcollectionDesc(sn) );
        }
        if ( isInvalidDayNumber(day) ) {
            throw new IllegalArgumentException( "Invalid day " + day );
        }
        return getLoadScenarioInstance(sn).getLoadScenarioDay(day).isScenProbSet();
    }


    /**
     * Inject a LoadScenario, instead of reading it from a case file.
     *
     * Useful for things like unit tests.
     * @param sn
     * @param scenProb
     * @param scenario
     * @throws IllegalArgumentException
     */
    protected void putLoadScenario(int sn, double scenProb, LoadProfileCollection scenario) {
        if(isInvalidScenarioNumber(sn))
            throw new IllegalArgumentException();

        LoadScenarioInstance instance = getLoadScenarioInstance(sn);
        instance.setScenario(scenario);
        instance.setScenarioProb(scenProb);
    }

    /**
     * Inject the ExpectedLoadScenario.
     *
     * Useful for things like unit tests.
     * @param expectedLoad
     */
    protected void putExpectedLoad(LoadProfileCollection expectedLoad) {
        //Use Double.NaN because probability of choosing means nothing
        //for the expectation.
        putLoadScenario(EXPECTED_LOAD_SCEN_NUM, Double.NaN, expectedLoad);
    }

    /**
     * @return true a file path has been set for the ExpectedLoad profile, else false.
     */
    public boolean hasExternalExpectedLoadProfile() {
        //TODO-X: Magic number of day 1 expected load scenario.
        return getScenarioFilePath(1, LoadCaseControl.EXPECTED_LOAD_SCEN_NUM) != null;
    }

    public boolean hasExternalActualLoadProfiles() {
        //TODO-X: Magic number of day 1 actual load scenario.
        return getScenarioFilePath(1, LoadCaseControl.ACTUAL_LOAD_SCEN_NUM) != null;
    }

    /**
     * Get the LoadScenario for the ExpectedLoad definition.
     *
     * Will compute the 'true' expected load if the scenario informations
     * does not exist and a file name for the ExpectedLoad was not given.
     *
     * @throws BadDataFileFormatException
     */
    public LoadProfileCollection getExpectedLoadProfiles() throws BadDataFileFormatException{
        LoadScenarioInstance lsi = getLoadScenarioInstance(EXPECTED_LOAD_SCEN_NUM);
        if(lsi.scenario == null){ //it is possible the expected scenario was set directly.
            if(!hasExternalExpectedLoadProfile()){
                LoadProfileCollection etl = computeExpectedTrueLoad();
                putExpectedLoad(etl);
            }
        }

        return getLoadScenario(EXPECTED_LOAD_SCEN_NUM);
    }

    /**
     * Get the LoadScenario for the ActualLoad definition.
     * @returns The load scenario object representing the "actual load" or null, if an 'actual' load was not in the LoadCase definition.
     * @throws BadDataFileFormatException
     */
    public LoadProfileCollection getActualLoadProfiles() throws BadDataFileFormatException{
        if(hasExternalActualLoadProfiles())
            return getLoadScenario(ACTUAL_LOAD_SCEN_NUM);
        else
            return null;
    }

    /**
     * Compute the expected true load.
     *
     * Currently we simply define the expectation as the weight average
     * of all the scenarios, with respect to the scenario probabilities.
     * @return
     * @throws BadDataFileFormatException
     */
    private LoadProfileCollection computeExpectedTrueLoad() throws BadDataFileFormatException {
        LoadProfileCollection expectedScenario = null;
//        int hoursPerDay = 24;//FIXME hardcoded 24.
        int hoursPerDay = numIntervalsInSim;
        System.out.println("The number of intervals in the simulation are " + hoursPerDay);
        double[][][] expectedTrueLoad = new double[maxDay][hoursPerDay][numZones];
        Iterator<LoadProfileCollection> scenarios = getAllLoadScenarios().iterator();
        while(scenarios.hasNext()){
            LoadProfileCollection ls = scenarios.next();
            double p = ls.probability();

            for(int day = 0; day<maxDay; day++){
                DailyLoadProfile dlp = ls.get(day+1); //day 1 is in array idx 0
                for(int hour = 0; hour < hoursPerDay; hour++){
                    double[] loadProfile = dlp.getLoadByHour(hour);
                    for(int z = 0 ; z<numZones; z++){
                        expectedTrueLoad[day][hour][z] += p * loadProfile[z];
                    }
                }
            }
        }

        //no need to copy, the array is 'fresh' and can't be aliased.
        expectedScenario = new LoadProfileCollection(caseName, EXPECTED_LOAD_SCEN_NUM, 1.0, expectedTrueLoad, false);


        return expectedScenario;
    }

    /**
     * Check if i is a valid scenario number.
     *
     * Has public visability for quick-testing.
     * @param i
     * @return true if i is not between [1,numLoadScenarios) and i is not the EXPECTED_LOAD_MARKER.
     */
    public boolean isInvalidScenarioNumber(int i) {

        return (i != LoadCaseControl.EXPECTED_LOAD_SCEN_NUM &&
                i != LoadCaseControl.ACTUAL_LOAD_SCEN_NUM &&
                i <= 0 || i > numLoadScenarios);
    }

    /**
     * Make sure p is between 0 and 1.
     * @param p
     * @return 0 &le; p &le; 1.0
     */
    private boolean isInvalidProbability(double p) {
        return p < 0 && p > 1.0;
    }

    /**
     * Check if day is between 1 and daymax, inclusive
     * @param day
     * @return
     */
    private boolean isInvalidDayNumber(int day) {
        return day < 1 || day > maxDay;
    }

    /**
     * Get a descriptive string for the index.
     * @param i
     * @return Scenario i for any scenario, or ExpectedLoad or
     * ActualLoad for if i is {@link #EXPECTED_LOAD_SCEN_NUM} or {@link #ACTUAL_LOAD_SCEN_NUM}
     * respectively.
     */
    public String lpcollectionDesc(int i) {
        if(i == EXPECTED_LOAD_SCEN_NUM)
            return "ExpectedLoad";
        else if(i == ACTUAL_LOAD_SCEN_NUM)
            return "ActualLoad";
        else
            return "Scenario " + Integer.toString(i) ;
    }

    /**
     * Struct to the information for a single day of a single load scenario.
     *
     * @author Sean L. Mooney
     */
    private static class LoadScenarioDay {

        double scenarioProb = Double.NaN;
        private File scenarioLoadFile = null;
        private File scenarioWindFile = null;
        private int scenarioDay = Integer.MIN_VALUE;

        boolean isScenProbSet() {
            return !Double.isNaN(scenarioProb);
        }
    }

    /**
     * Struct to store common information about scenarios.
     * @author Sean L. Mooney
     *
     */
    private class LoadScenarioInstance {
        TreeMap<Integer, LoadScenarioDay> dailyParams = new TreeMap<Integer, LoadScenarioDay>();
        /**Make sure to call {@link #getScenario} to instantiate object file.*/
        private LoadProfileCollection scenario = null;
        private final int scenarioNumber;

        /**
         *
         * @param scenDay
         * @param scenNumber
         */
        public LoadScenarioInstance(int scenNumber){
            this.scenarioNumber = scenNumber;
        }

        /**
         * Check the probability of each day is set.
         * @return
         */
        public final boolean isScenPropSet() {
            for (LoadScenarioDay lsd : dailyParams.values()) {
                if (!lsd.isScenProbSet()) {
                    return false;
                }
            }
            return true;
        }

        /**
         * Get the probability of the scenario.
         *
         * Assumes each scenario's day has the same probability,
         * and returns the probability of the first day.
         * @return
         */
        final double getScenarioProb() {
            return dailyParams.firstEntry().getValue().scenarioProb;
        }

        /**
         * Get the scenario probability for day d.
         * @param day
         * @return probability for day d, or {@link Double#NaN}
         */
        final double getScenarioProb(int day) {
            if(dailyParams.containsKey(day)){
                return dailyParams.get(day).scenarioProb;
            } else {
                return Double.NaN;
            }
        }

        /**
         * Get the object representing the information for scenario day d.
         *
         * Creates the object if it does not exist.
         * @param day
         * @return
         */
        final LoadScenarioDay getLoadScenarioDay(int day){
            if(dailyParams.containsKey(day)){
                return dailyParams.get(day);
            } else {
                LoadScenarioDay lsd = new LoadScenarioDay();
                lsd.scenarioDay = day;
                dailyParams.put(day, lsd);
                return lsd;
            }
        }

        /**
         * Get the scenario object.
         *
         * Load the scenario if it has not been loaded already.
         * @return
         * @throws BadDataFileFormatException
         */
        public final LoadProfileCollection getScenario() throws BadDataFileFormatException{
            if(scenario == null){
                setupLoadScenario();
            }

            return scenario;
        }

        /**
         * Set the scenario directly, instead of reading from a file.
         * @param scenario
         */
        public final void setScenario(LoadProfileCollection scenario) {
            this.scenario = scenario;

            //make sure there is at least 1 load scenario day object
            //We need at least 1 to store the probability of choosing
            //this scenario in. Bit of a hack, but get's the job done.
            if(dailyParams.isEmpty()){
                LoadScenarioDay lsd = new LoadScenarioDay();
                lsd.scenarioDay = 1;
                dailyParams.put(1, lsd);
            }
        }

        /**
         * Set the probability for each day in the scenario.
         * @param probability
         */
        final void setScenarioProb(double probability) {
            if(isInvalidProbability(probability)){
                throw new IllegalArgumentException("Invalid probability " + probability);
            }
            for(LoadScenarioDay lsd : dailyParams.values())
                lsd.scenarioProb = probability;
        }

        final void setScenarioProb(int day, double probability) {
            if(isInvalidProbability(probability)){
                throw new IllegalArgumentException("Invalid probability " + probability);
            }
            if( dailyParams.containsKey(day) ) {
                dailyParams.get(day).scenarioProb = probability;
            }
        }

        /**
         * Use the {@link #scenarioFile} to read in the scenario.
         *
         * @throws BadDataFileFormatException
         */
        public final void setupLoadScenario() throws BadDataFileFormatException{
            LoadCaseScenarioReader lcsr = new LoadCaseScenarioReader(LoadCaseControl.this);
            lcsr.setExpectedScenarioNumber(scenarioNumber);

            LoadProfileCollection wind = null;

            //read in the base load


            if(hasMultipleDayFiles()){
                boolean hasWindData = false;
                Map<Integer, File> loadFiles = new TreeMap<Integer, File>();
                Map<Integer, File> windFiles = new TreeMap<Integer, File>();

                for( Entry<Integer, LoadScenarioDay> dlpe : dailyParams.entrySet() ) {
                    LoadScenarioDay v = dlpe.getValue();
                    Integer k = dlpe.getKey();
                    loadFiles.put(k, v.scenarioLoadFile);
                    if(v.scenarioWindFile != null) {
                        hasWindData = true;
                        windFiles.put(k, v.scenarioWindFile);
                    }
                }

                //load the files
                lcsr.setLoadType(DailyLoadProfile.LoadType.LOAD);
                scenario = lcsr.readCompositScenario(loadFiles);

                if(hasWindData){
                    lcsr.setLoadType(LoadType.WIND);
                    wind = lcsr.readCompositScenario(windFiles);
                }
                //FIXME-X: Read wind, plumb in photo. Adjust to net load.
            } else {
                //There should be at least 1 day, the first day should have
                //the name of the file to read everything from.
                LoadScenarioDay lsd = dailyParams.get(1);
                if(lsd == null){
                    throw new BadDataFileFormatException("No data file for " +
                            lpcollectionDesc(scenarioNumber));
                }
                lcsr.setLoadType(DailyLoadProfile.LoadType.LOAD);
                scenario = lcsr.read(lsd.scenarioLoadFile);
                if(lsd.scenarioWindFile !=null ){
                    lcsr.setLoadType(LoadType.WIND);
                    wind = lcsr.read(lsd.scenarioWindFile);
                }
            }

            computeNetLoad(scenario, wind);

            //TODO-X: There is still some disagreement between
            // the program logic for scenario probability and scenario day probability.
            scenario.setScenarioProb(getScenarioProb()); // make sure to copy the prob. over.

        }

        private void computeNetLoad(LoadProfileCollection load, LoadProfileCollection wind) {
            if (wind != null) {
                load.subtractLoad(wind);
            }
        }

        private boolean hasMultipleDayFiles() {
            File first = null;
            for(LoadScenarioDay lsd : dailyParams.values()){
                if(first == null) {
                    first = lsd.scenarioLoadFile;
                } else { //as soon as as a different file is found, we can return false.
                    if(!first.equals(lsd.scenarioLoadFile))
                        return true;
                }
            }
            return false;
        }
    }
}

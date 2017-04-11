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

import static amesmarket.Support.trimAllStrings;


import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
//Use the common tokens between the Control file and the Scenario.
import static amesmarket.filereaders.LoadCaseLabelInfo.*;

import amesmarket.DailyLoadProfile;
import amesmarket.DailyLoadProfile.LoadType;
import amesmarket.LoadCaseControl;
import amesmarket.LoadProfileCollection;
import amesmarket.Support;


// TODO-X Error Messages for parsing files are not user friendly.
// TODO:    Externalize strings
/**
 * Read in a data file representing a single LoadProfile scenario.
 *
 * The reader fails fast. As soon as any problem is encountered in the data
 * file format, an excpetion is thrown.
 *
 * TODO: Describe the expected file format, once it solidifies.
 *
 * @author Sean L. Mooney
 *
 */
//TODO: Refactor to use error/warning methods instead of throwing exceptions all over the place.
//TODO: Lack of actual Tokenizing is making this rather complicated.
// when infinite time exists, replace the line-based reader
// with a proper lexer/parser.
public class LoadCaseScenarioReader extends AbstractConfigFileReader<LoadProfileCollection> {

    /**
     * Whether or not we are reading a file that contains a complete
     * Load Scenario, or a file that only contains a single day's worth
     * of profiles.
     */
    private boolean isSplitScenarioDays = false;

    /**
     * A link to the LoadControl representation for the files being loaded.
     * Needed to properly validate data.
     */
    private final LoadCaseControl loadControl;

    /**
     * Check the expected scenario number. Must be set
     * before each read.
     */
    private int expectedScenarioNumber;

     /**
     * Number of columns dedicated to describing 'when' the load profile is.
     *
     * Defaults to 2, but will be one if the file only represents 1 day's worth of load profiles.
     */
    private int numTimeCols = 2;

    /**
     * Index we expect to find the day in
     */
    private final int dayIdx = 0;
    /**
     * Index we expect to find the hour in. May be changed by {@link #oneDayOnly()}
     */
    private int hourIdx = 1;

    /**
     * Store the day number, if it is global to the file.
     */
    private int dayNumber = -1;

    //TODO : Find the common definition of HOURS_PER_DAY in the wider system.
    private final int HOURS_PER_DAY = 24;

    /**
     * Index mapping the 'correct' order of zone names to a index.
     */
    private final IZoneIndexProvider zoneIndexOrder;

    /**
     * List of zone names, as they appear in the LoadProfileCollection file being read.
     */
    private List<String> zoneNameList;

    private LoadType expectedLoadType;

    /**
     * Create a load scenario reader using the giving lsc's information to validate
     * the scenario file. The lsc must not be null.
     * Multiple scenarios files may be read with a single LoadScenarioReader.
     *
     * @param lcc
     * @throws IllegalArgumentException if lsc is null
     */
    public LoadCaseScenarioReader(LoadCaseControl lcc){
        super();
        if(lcc == null){
            throw new IllegalArgumentException();
        }
        loadControl = lcc;
        zoneIndexOrder = lcc.getZoneNameIndexes();
        zoneNameList = new ArrayList<String>();
        //Default to load. Most of the files we read, especially in tests
        //are load.
        expectedLoadType = LoadType.LOAD;
    }

    /**
     * Set the type of 'load' we are expecting to read.
     * @param load
     */
    public void setLoadType(LoadType load) {
        expectedLoadType = load;
    }

    /**
     * Read in load profiles until the end of the file/reader stream.
     *
     * The LoadControl object must be set before calling this method.
     * The return object has not been checked for consistency. The client is
     * responsible for checking the object when it is appropriate to do so!
     *
     * Before calling this method (or any of the other read methods) the
     * expected scenario number must be set with {@link #setExpectedScenarioNumber(int)}.
     * The value of the expected scenario will be used to validate the scenario
     * data at the end of the method.
     *
     * Breaks the general protocol for {@link #move(boolean)}. This is because
     * of the loop and-a-half issue, coupled with needing to look ahead in
     * the stream to check for the end of loop and no push-back into the input stream.
     *
     *
     * @param expectedEntries expected number of entries in each line of the data stream.
     * @return a list of load profiles.
     * @throws BadDataFileFormatException
     */
    @Override
    protected LoadProfileCollection read() throws BadDataFileFormatException {
        LoadProfileCollection slp = new LoadProfileCollection();

        readScenarioInfo(slp);

        //loop and half
        while( true ) {
            move(false);

            //now that have moved, check the condition. currentLine will be
            //null if the file ended.
            if(currentLine == null)
                break;

            readDayLoadProfile(slp);
        }

        try{
            if( isSplitScenarioDays ){
                //only expect 1 day's worth of date if split days.
                //TODO-XX: Check this is the 'correct/expected' day number.
                slp.validate(loadControl.getCaseName(), expectedScenarioNumber, 1);
            } else {
                slp.validate(loadControl.getCaseName(), expectedScenarioNumber, loadControl.getMaxDay());
            }
        }catch(BadDataFileFormatException ex) {
            //I don't think we actually care about the cause. It is more an issue of which line
            //in what file and the message.
            throw new BadDataFileFormatException( sourceFile, lineNum, ex.getMessage(), ex );
        }
        return slp;
    }

    /**
     * Read multiple profile files to construct a single load scenario.
     * @param files
     * @return
     * @throws BadDataFileFormatException
     */
    public LoadProfileCollection readCompositScenario(Map<Integer, File> files) throws BadDataFileFormatException {
        LoadProfileCollection compositScenario = null;
        oneDayOnly();
        for(Entry<Integer, File> entry : files.entrySet()){
            LoadProfileCollection ls = read(entry.getValue());
            //start by copying the load scenario we read.
            if(compositScenario == null){
                compositScenario = new LoadProfileCollection(ls);
            } else { //now just copy in the load profile for the day this file represents.
                int day = entry.getKey();
                DailyLoadProfile dlp = ls.get(day);
                if (dlp == null) {
                    throw new BadDataFileFormatException(
                            String.format(
                                    "Did not find a load profile for day %d in scenario %d.",
                                    dayNumber, expectedScenarioNumber));
                }
                compositScenario.put(dlp);
            }
        }

        return compositScenario;
    }

    /**
     * Read in the load profile for a single day.
     *
     * When finished reading the day, the {@link DailyLoadProfile} will
     * be added to the slp.
     *
     * @param slp
     * @return
     * @throws BadDataFileFormatException
     *
     * Breaks the {@link #move(boolean)} protocol with {@link #readLoadProfileData}.
     */
    private void readDayLoadProfile(LoadProfileCollection slp)
            throws BadDataFileFormatException {
        final int NUM_ZONES = loadControl.getNumZones();
        final int LAST_ZONE_IDX = numTimeCols + NUM_ZONES;


        int expectedDay = -1; //Day we should be reading for

        final DailyLoadProfile dlp = new DailyLoadProfile(HOURS_PER_DAY);

        for(int h = 0; h < HOURS_PER_DAY; h++){
            String[] lpElements = currentLine.split("\\s+");
            trimAllStrings(lpElements);

            if( !checkCorrectZoneCount(lpElements) ){
                //TODO: Error message reported expected/found.
                throw new BadDataFileFormatException(sourceFile, lineNum, "Wrong number elements in \"" + currentLine + "\"");
            }

            int day;
            int hour;

            if(!isSplitScenarioDays)
                day = stoi(lpElements[dayIdx]);
            else
                day = this.dayNumber;

            hour = stoi(lpElements[hourIdx]);

            if(expectedDay == -1){
                expectedDay = day; //set the expected day the first time through
            }else{
                if (expectedDay != day) { //Make sure we're always on the same day!
                    throw new BadDataFileFormatException(sourceFile, lineNum,
                            "Expected day " + expectedDay + ", found day"
                                    + day);
                }
            }

            double[] zoneLoads = new double[NUM_ZONES];

            //Convert each zone's load to a double.
            for(int zi = numTimeCols; zi < LAST_ZONE_IDX; zi++){
                try{
                    zoneLoads[zi - numTimeCols] //adjust for time coordinate columns
                            = Support.parseDouble(lpElements[zi]);
                }catch(NumberFormatException nfe){
                    throw new BadDataFileFormatException(sourceFile, lineNum, lpElements[zi] + " must be a valid double");
                }
            }

            zoneLoads = adjustZoneLoadOrder(zoneLoads);

            //Adjust to 0 indexed hours.
            //TODO-XXX: Does the rest of AMES expect 0 or 1 indexed hours.
            // I think it is 0, but need to verify/document somewhere.
            dlp.setLoadByHour(hour - 1, zoneLoads);

            // Do not move on the last hour of the day.
            // The loop in readLoadProfileData will move as its fist operation.
            // readLoadProfileData runs move to catch cases where there
            // is an empty line at the end of the file and prevent
            // trying to read a day when there isn't another in the file.
            if( h < (HOURS_PER_DAY - 1))
                move(true);
        }

        //Mark what day we just processed
        dlp.setDayNumber(expectedDay);
        slp.put(dlp);
    }

    /**
     * Reorder the zoneLoads according to the expected order in
     * the TestCase file.
     * @param zoneLoads
     */
    private double[] adjustZoneLoadOrder(double[] zoneLoads) {

        double[] sorted = new double[zoneLoads.length];

        for(int i = 0; i<zoneLoads.length; i++) {
            String zName = zoneColumnName(i);
            int destIdx = zoneIndexOrder.get(zName);

            sorted[destIdx-1] = zoneLoads[i];
        }

        return sorted;
    }

    /**
     * Get the name at element i. I must be in the bounds of the list.
     * @param i
     * @return
     */
    private String zoneColumnName(int i) {
        return zoneNameList.get(i);
    }

    /**
     * Check a the line contains as many columns as it should.
     *
     * @param expLength expected length of line
     * @param line list of strings to check for the correct length
     * @return
     */
    private boolean checkCorrectZoneCount(String[] line){
        return line.length == numTimeCols + loadControl.getNumZones();
    }

    /**
     * Read/Validate the scenario info in the header of the ScenarioFile.
     *
     * @param dest for read data
     * @throws BadDataFileFormatException
     */
    private void readScenarioInfo(LoadProfileCollection slp) throws BadDataFileFormatException{

        boolean hasCaseName = false;
        boolean hasScenarioNumber = false;
        boolean foundDataHeader = false;

        //The scenario file must start with
        //[AllOf | PartOf] : <name> [Scenario <int> [Load | Wind] | ExpectedLoad | ActualLoad]
        // Day : <day_number> (optional)
        //(Day)? Hour (Z[1-9][0-9]*)+
        do { //read
            move(true);

            //This quick and dirty but should be sufficient
            //If the has a colon, it must be a label and a value.
            if(!currentLine.contains(":")) {
                //this should be the data description line.
                validateColumnDescriptions(currentLine);
                foundDataHeader = true;
                continue; //skip the loop condition
            }

            final LoadCaseLabelInfo decl = new LoadCaseLabelInfo(currentLine);

            if ( decl.isPartOfDecl() || decl.isAllOfDecl() ){

                //check to make sure the AllOf/PartOf decl matches
                //what is expected, given what was declared in the control file.
                if( !isSplitScenarioDays && decl.isPartOfDecl() ) {
                    throw new BadDataFileFormatException(sourceFile, lineNum,
                            String.format("Found %s, expected %s", PART_OF, ALL_OF)
                            );
                }
                if( isSplitScenarioDays && decl.isAllOfDecl() ) {
                    throw new BadDataFileFormatException(sourceFile, lineNum,
                            String.format("Found %s, expected %s", ALL_OF, PART_OF)
                            );
                }

                //Split at a , Index 0 is the CaseName, index 1 is the type
                String[] lpmem = splitLPMemValue(decl.value());

                slp.setCaseName(lpmem[0]);
                hasCaseName = true;

                //Tokenize the collection decl
                if(lpmem.length > 1 && lpmem[1] == null) {
                    throw new BadDataFileFormatException(sourceFile, lineNum,
                            String.format("No load profile collection type found in %s", currentLine)
                            );
                }
                String[] scenParts = lpmem[1].split("\\s+");
                Support.trimAllStrings(scenParts);

                if( EXP_LOAD.equals(scenParts[0]) ){ //check for the ExpectedLoad marker.
                    slp.setScenarioNumber(LoadCaseControl.EXPECTED_LOAD_SCEN_NUM);
                    //checkLoadType(slp.getScenarioNumber(), lpmem);
                }else if( ACT_LOAD.equals(scenParts[0]) ){ //check for the ActualLoad marker
                    slp.setScenarioNumber(LoadCaseControl.ACTUAL_LOAD_SCEN_NUM);
                    //checkLoadType(slp.getScenarioNumber(), lpmem);
                }else{

                    if(scenParts.length != 3) {
                        throw new BadDataFileFormatException(sourceFile, lineNum,
                                String.format("Expected '<CASE_NAME>, %s <INT> [%s | %s]', found '%s'.",
                                        SCENARIO, LOAD, WIND, lpmem[1])
                                );

                    }if(!SCENARIO.equals(scenParts[0])) {
                        throw new BadDataFileFormatException(sourceFile, lineNum,
                                String.format("Unknown token '%s'.", scenParts[0])
                                );
                    }
                    slp.setScenarioNumber(stoi(scenParts[1]));
                    checkLoadType(slp.getScenarioNumber(), scenParts);
                }

                hasScenarioNumber = true;
            } else if ( decl.hasDay() ){
                if( !isSplitScenarioDays )
                    throw new BadDataFileFormatException(sourceFile, lineNum,
                            String.format(
                            "Found invalid day declaration '%1$s'" +
                            " in a complete scenario file.%n" +
                            "Day declarations may only be used in split load profeil collection files."
                            , currentLine)
                            );
                dayNumber = stoi(decl.value());
            } else {
                throw new BadDataFileFormatException(sourceFile, lineNum,
                        "Unknown declaration \"" + currentLine
                                + "\" in scenario description");

            }
        } while(!foundDataHeader);

        //Check that both the case name and scenario number were listed
        //Common warning message.
        final String missingScenDecl = "Scenario file %s does not contain a %s declaration.";

        if(!hasCaseName){
            throw new BadDataFileFormatException(
                    String.format(missingScenDecl, sourceFile.getPath(), CASE_NAME) );
        }

        if(!hasScenarioNumber){
            throw new BadDataFileFormatException(
                    String.format(missingScenDecl, sourceFile.getPath(), SCENARIO) );
        }
    }

    /**
     * Parse the values of a membership decl.
     *
     * <p>
     * A membership decl looks like:
     * <p>
     * [PartOf | AllOf] : &ltCASE_NAME&gt, [Scenario &ltINT&gt [Wind | Load] | ExpectedLoad | ActualLoad]
     * @param v -- not null
     * @return String[] {casename, load-profile-type}
     * @throws BadDataFileFormatException
     */
    private String[] splitLPMemValue(String v) throws BadDataFileFormatException {
        if(v == null) {
            return new String[]{"", ""}; //fail safe
        }

        if(v.contains(",")) {
            String[] elems = v.split(",");
            if(elems.length == 2) {
                Support.trimAllStrings(elems);
                return elems;
            } else {
                throw new BadDataFileFormatException(sourceFile, lineNum,
                        String.format("Failed to find two elements in '%s'.", v)
                        );

            }
        } else {
            throw new BadDataFileFormatException(sourceFile, lineNum,
                    String.format("No ',' found in %s.%n" +
                            "Expected <CASE_NAME>, [Scenario <INT> | ExpectedLoad | ActualLoad]", v));
        }
    }

    private void checkLoadType(int scenNum, String[] v) throws BadDataFileFormatException {
        //Load index is always at the end
        final int i = v.length - 1; //Turns out the Load label will be in the same place regardless.

        //TODO: Refactor. There's a common case here.
        final String errTmplt = "Incorrect load type. Expected '%s', found '%s'";
        switch (expectedLoadType) {
        case LOAD:
            if (!LoadCaseLabelInfo.LOAD.equals(v[i])) {
                throw new BadDataFileFormatException(sourceFile, lineNum,
                        String.format(errTmplt, LoadCaseLabelInfo.LOAD,
                                v[i]));
            }
            break;
        case WIND:
            if (!LoadCaseLabelInfo.WIND.equals(v[i])) {
                throw new BadDataFileFormatException(sourceFile, lineNum,
                        String.format(errTmplt, LoadCaseLabelInfo.WIND,
                                v[i]));
            }
            break;
        default:
            throw new BadDataFileFormatException(sourceFile, lineNum,
                    "Unknown load type " + v[i]);
        }

    }

    /**
     * Validate the data column descriptions. If there is a problem, an error
     * will be thrown. If it terminates then the descriptions is valid.
     * @param si
     * @throws BadDataFileFormatException
     */
    private void validateColumnDescriptions(String dataDesc) throws BadDataFileFormatException{

        //split on whitespace. Will handle not explicitly tab deliminated columns
        String[]  columnDescs = dataDesc.split("\\s+");
        trimAllStrings(columnDescs);

        if( !checkCorrectZoneCount(columnDescs) ){
            int expZones = loadControl.getNumZones();
            int numActZones = columnDescs.length - numTimeCols;
            throw new BadDataFileFormatException(sourceFile, lineNum,
                    "Incorrect column count in '" + dataDesc + "'\n" +
                    "Expected " + expZones +
                    ", found " + numActZones + "."
                    );
        }

        String unexpectedMarker = "Expected %s, found %s at column %d";

        //Check for Day for full scenarios
        if( !isSplitScenarioDays ) { //only check for day if not single day in file.
            //Day at 0
            if( !DAY.equals(columnDescs[dayIdx]) ){
                throw new BadDataFileFormatException(sourceFile, lineNum,
                        String.format(unexpectedMarker, DAY, columnDescs[0], 0) );
            }
        }

        //Check for hour
        if( !HOUR.equals(columnDescs[hourIdx]) ){
            throw new BadDataFileFormatException(sourceFile, lineNum,
                    String.format(unexpectedMarker, HOUR, columnDescs[1], 1) );
        }

        //Remaining elements are ZONE markers
        final int numZones = loadControl.getNumZones();
        for(int zNum = 1; zNum <= numZones; zNum++){

            int zCol = zNum + numTimeCols - 1; //subtract 1 to account for 0 index

            if ( zoneIndexOrder.hasIndexForName(columnDescs[zCol]) ) {
                zoneNameList.add(columnDescs[zCol]);
            } else {
                throw new BadDataFileFormatException(sourceFile, lineNum,
                        "Unknown zone name '" + columnDescs[zCol] + "' at column " + zCol );
            }
        }
    }

    /**
     * Mutate appropriate fields to mark the input file as containing only
     * 1 days worth of load profiles.
     */
    private void oneDayOnly() {
        isSplitScenarioDays = true;
        numTimeCols = 1;
        hourIdx = 0;
    }

    /**
     * @return the expectedScenarioNumber
     */
    public int getExpectedScenarioNumber() {
        return expectedScenarioNumber;
    }

    /**
     * Set the scenario number we expect to see.
     *
     * Should be either a positive integer or {@link LoadCaseControl#EXPECTED_LOAD_SCEN_NUM}
     * @param expectedScenarioNumber the expectedScenarioNumber to set
     */
    public void setExpectedScenarioNumber(int expectedScenarioNumber) {
        this.expectedScenarioNumber = expectedScenarioNumber;
    }

    /////////////////////KEYWORDS IN SCENARIO FILE ///////////////////////
    public static final String DAY         = "Day";
    public static final String HOUR        = "Hour";
    public static final String ZONE_PREFIX = "Z";
    /////////////////////END KEYWORDS/////////////////////////////////////

}

/* ============================================================================
 * AMES Wholesale Power Market Test Bed (Java): A Free Open-Source Test-Bed
 *         for the Agent-based Modeling of Electricity Systems
 * ============================================================================
 *
 * (C) Copyright 2008, by Hongyan Li, Junjie Sun, and Leigh Tesfatsion
 *
 *    Homepage: http://www.econ.iastate.edu/tesfatsi/testMarketHome.htm
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

// AMESMarket.java - main class for AMESMarket project
//
// Reference: DynTestAMES Working Paper
// Available online at http://www.econ.iastate.edu/tesfatsi/DynTestAMES.JSLT.pdf
package testsupport;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import org.junit.After;
import org.junit.Before;

import uchicago.src.sim.engine.BasicAction;

import AMESGUIFrame.AMESFrame;
import amesmarket.AMESMarket;
import amesmarket.AMESMarketException;
import amesmarket.CaseFileData;
import amesmarket.DAMarket;
import amesmarket.DailyLoadProfile;
import amesmarket.ISO;
import amesmarket.LoadProfileCollection;
import amesmarket.Support;
import amesmarket.filereaders.BadDataFileFormatException;
import amesmarket.filereaders.CaseFileReader;


/**
 * Base for AMES market test classes.
 *
 * The BaseAMESTest sets up the market and loads the 5 bus test case.
 *
 * Other classes testing specific aspects of AMES should extend this class.
 *
 * @author Sean L. Mooney, Dheepak Krishnamurthy
 *
 */
public abstract class AbstractTestAMES {

    /**
     * Keep a reference to the 'console' system out. AMESFrame
     * changes system.out when it  runs.
     */
    protected PrintStream origSysOut;
    /**
     * Reference to another {@link PrintStream} to be used for
     * see err. Like {@link #origSysOut}.
     */
    protected PrintStream origSysErr;

    public static final TestCaseParams FIVE_BUS_TEST_CASE =
           new TestCaseParams(
                   "TEST-DATA/5BusTestCase.dat",
                   "TEST-DATA/5BusTestCase_temp.out",
                   "TEST-DATA/5BusTestCase_expected.out" //TODO: no expected data file yet.
                   );

    /**
     * Test case with 5 zones/LSES. A set of 5 generators and 5 canary generators.
     * Includes SCUC data.
     */
    public static final TestCaseParams FIVE_BUS_5_GEN_TEST_CASE =
            new TestCaseParams(new File(TestConstants.TEST_FILE_DIR, "5BusSCEDComparison/5BusTestCase_5gen.dat"),
                    null, null);
    
    public static final TestCaseParams WINDLOAD_GEN_TEST_CASE1 =
            new TestCaseParams(new File(Test_DirectoryVariables.TEST_FILE_DIR, "Test_TwoTestCases/test1.dat"),
                    null, null);
    
    public static final TestCaseParams WINDLOAD_GEN_TEST_CASE2 =
            new TestCaseParams(new File(Test_DirectoryVariables.TEST_FILE_DIR, "Test_TwoTestCases/test2.dat"),
                    null, null);
    
    public static final TestCaseParams EIGHT_BUS_MILESTONE_TEST_CASE =
            new TestCaseParams(
                    "TEST-DATA/8BusMilestone1C.dat",
                    "TEST-DATA/8BusMilestone1C_temp.dat",
                    "TEST-DATA/8BusTestCase_expected.out" //TODO: no expected data file yet.
                    );

    @Before
    public void setUp() {
        saveOutStreams(); //keep a copy to the console PrintStreams before an AMESFrame is constructed.
    }

    @After
    public void tearDown() {
    }

    private void saveOutStreams() {
        origSysOut = System.out;
        origSysErr = System.err;
    }

    /**
     * Set the stdout and stderr to the saved values.
     *
     * Must call {@link #saveOutStreams()} before using this.
     */
    protected void restoreOutStreams() {
        if(origSysOut == null)
            fail("Must origSysOut may not be null");
        if(origSysErr == null)
            fail("Must origSysErr may not be null");

        System.setOut(origSysOut);
        System.setErr(origSysErr);


    }

    /**
     * Run the initialized market for the maximum number of days,
     * as specified in the testConf.
     * @param numDays number of days to run the market for. Generally this will be
     * the max number of days from the test market.
     */
    public void runTestMarket(AMESMarket testMarket, int numDays){
        testMarket.Start();


        for(int i = 0; i<numDays; i++){
            testMarket.Step();
        }

        testMarket.Stop();
    }

    /**
     * Run the market until a the simulation ends.
     */
    public void runTestMarket(AMESMarket testMarket){
        testMarket.Start();
        while(!testMarket.IfCalculationEnd()){
            testMarket.Step();
        }
    }

    /**
     * Load and initialize the market with the test case parameters.
     * @param testCaseParam
     * @return
     */
    public AMESMarket loadAndSetupTestCase(TestCaseParams testCaseParam){
        return loadAndSetupTestCase(testCaseParam.getTestCaseFile());
    }

    /**
     * Load the test case file.
     * @param tcp
     * @return
     * @throws BadDataFileFormatException
     * @throws FileNotFoundException
     * @throws IOException
     */
    public CaseFileData loadCaseFileData(final TestCaseParams tcp) throws FileNotFoundException, BadDataFileFormatException, IOException{
        return loadCaseFileData(tcp.getTestCaseFile());
    }

    public CaseFileData loadCaseFileData(final Test_TestCaseParameters tcp) throws FileNotFoundException, BadDataFileFormatException, IOException{
        return loadCaseFileData(tcp.getTestCaseFile());
    }
    
    /**
     * Read results file in as a String.
     *
     * @param tcp
     * @return
     */
    public String readResults(TestCaseParams tcp) {
        File f = tcp.getTestCaseOutput();
        String contents = "";
        try {
            contents = readFileAsString(f);
        } catch (FileNotFoundException e) {
            fail("File " + f.getName() + " not found.\n" + e.getMessage());
        }

        return contents;
    }

    /**
     * Assert that the SCEDs computed in the two markets are, in fact,
     * the same.
     *
     * Supports ARPe milestone Q3 1C.
     * @param expected
     * @param actual
     *
     * TODO-XX: Review test with Auswin.
     */
    public void assertEqualSCED(AMESMarket expected, AMESMarket actual, double tol){
        //general places to store the elements we need to check between
        //the two versions.
        ArrayList<int[]> expIA, actIA;
        int numDays = expected.DAY_MAX;

        //check the hasSolutions
        expIA = expected.getHasSolutionByDay();
        assertNotNull("Expected solution by day null");
        actIA = actual.getHasSolutionByDay();
        assertNotNull("Actual solution by day null");
        assertEquals("Expected SCED: Wrong number of solutions by day.", numDays, expIA.size());
        assertEquals("Actual SCED: Wrong number of solutions by day.", numDays, actIA.size());

        for(int d = 0; d < numDays; d++) {
            assertArrayEquals("Different 'has solutions' for day " + d,
                    expIA.get(d), actIA.get(d));
        }

        //TODO-XX: Check correct number of days for each element here.
        //check the genco commitments
        //TODO-XXX: is gen data correct?
        assertEquals("Expected SCED: Wrong number of solutions by day.", numDays, expIA.size());
        assertEquals("Actual SCED: Wrong number of solutions by day.", numDays, actIA.size());

        assertEqualList("GenCo commitment by day.",
                expected.getGenAgentCommitmentByDay(),
                actual.getGenAgentCommitmentByDay(), tol);

        assertEquals("Expected SCED: Wrong number of solutions by day.", numDays, expIA.size());
        assertEquals("Actual SCED: Wrong number of solutions by day.", numDays, actIA.size());

        assertEqualList("GenCo rt commitment by day.",
                expected.getGenAgentRealTimeCommitmentByDay(),
                actual.getGenAgentRealTimeCommitmentByDay(), tol);

        //price sensitive demand
        assertEqualList("Price Sensitive Demand",
                expected.getLSEAgenPriceSensitiveDemandByDay(),
                actual.getLSEAgenPriceSensitiveDemandByDay(), tol);

        //check the branch power flow
        assertEqualList("Branch power flow", expected.getBranchFlowByDay(),
                actual.getBranchFlowByDay(), tol);
        assertEqualList("RT Branch power flow", expected.getRealTimeBranchFlowByDay(),
                actual.getRealTimeBranchFlowByDay(), tol);

        //check the lmp's
        assertEqualList("LMP", expected.getLMPByDay(), actual.getLMPByDay(), tol);
        assertEqualList("RT LMP", expected.getRealTimeLMPByDay(), actual.getRealTimeLMPByDay(), tol);

        //TODO-XXX: check the voltage angles
        //fail("No volt angle check");
//        assertSameList("Voltage Angles",
//                expected.get, TestConstants.DOUBLE_EQ);
    }

    /**
     *
     * @param lbl
     * @param exp
     * @param act
     * @param tol comparison tolerance
     */
    public void assertEqualLoadScenarios(String lbl, LoadProfileCollection exp, LoadProfileCollection act, double tol) {
        List<DailyLoadProfile> edlps = exp.getAllProfiles();
        List<DailyLoadProfile> adlps = act.getAllProfiles();

        assertEquals(lbl + " Different number of daily load profiles.", edlps.size(), adlps.size());

        for(int i = 0; i < edlps.size(); i++) {
            DailyLoadProfile edlp, adlp;
            edlp = edlps.get(i);
            adlp = adlps.get(i);
            assertEquals(lbl + " Found different days.", edlp.getDayNumber(), adlp.getDayNumber());
            assertEquals(lbl + " Found diffent number of hours.", edlp.getNumHours(), adlp.getNumHours());
            for(int h = 0; h < edlp.getNumHours(); h++){
                assertArrayEquals(
                        lbl + " Different load profiles at hour " + h,
                        edlp.getLoadByHour(h), adlp.getLoadByHour(h), tol);
            }
        }
    }

    /**
     *
     * @param exp
     * @param act
     */
    public void assertEqualList(String lbl, List<double[][]> exp, List<double[][]> act, double tol){
        assertEquals(lbl + " size of lists.", exp.size(), act.size());
        int m = exp.size();
        for(int d = 0; d < m; d++) {
            double[][] expDD, actDD;
            expDD = exp.get(d);
            actDD = act.get(d);
            for(int i =0 ; i < expDD.length; i++)
                assertArrayEquals(lbl + " d:" + d + " i:" + i,
                        expDD[i], actDD[i], tol );
        }
    }
    
    public void assertEqualList(String lbl, List<int[]> exp, List<int[]> act){
        assertEquals(lbl + " size of lists.", exp.size(), act.size());
        int m = exp.size();
        for(int d = 0; d < m; d++) {
            int[] expI, actI;
            expI = exp.get(d);
            actI = act.get(d);
            assertArrayEquals(lbl + " d:" + d , expI, actI);
        }
    }

    /**
    * Wrapper for {@link AbstractTestAMES#assertEqualList(String, List, List, double)}.
    * Packs the first dimension into a List<double[][]>.
    * @param exp
    * @param act
    */
    public void assertEqualArrays(String msg, double[][][] exp, double[][][] act, double tol){
       assertEqualList(msg,
               Arrays.asList(exp),
               Arrays.asList(act), tol);
   }

    public void assertEqualArrays(String msg, int[][] exp, int[][] act,
            double tol) {

        assertEquals(msg + "Different array lengths", exp.length, act.length);
        for (int i = 0; i < exp.length; i++) {
            assertArrayEquals(msg + "Row: " + i, exp[i], act[i]);
        }
    }

   public void assertEqualArrays(String msg, double[][] exp, double[][] act, double tol){
       assertEquals(msg + " Different array lengths", exp.length, act.length);
       for(int i = 0; i<exp.length; i++){
           assertArrayEquals(msg + "Row: " + i, exp[i], act[i], tol);
       }
   }

   /**
    * Assert the stop conditions of an AMES market executions.
    *
    * See  WPMarket in {@link AMESMarket#buildSchedule()} for what these codes are.
    * @param am
    * @param expMaxDay expected {@link AMESMarket#bMaximumDay} value.
    * @param expHasError whether or not an error should have occured
    */
   public void assertStopCodes(AMESMarket am, boolean expMaxDay, boolean expHasStopError) {
       assertEquals("Incorrect stop code.", expHasStopError, am.hasStopCodeError());
       assertEquals("Incorrect maxDay", expMaxDay, am.bMaximumDay);
   }

    /**
     * Read the expected results as a String.
     *
     * @param tcp
     * @return
     */
    public String readExpected(TestCaseParams tcp) {
        File f = tcp.getTestCaseExpected();
        String contents = "";
        try {
            contents = readFileAsString(f);
        } catch (FileNotFoundException e) {
            fail("File " + f.getName() + " not found.\n" + e.getMessage());
        }

        return contents;
    }

    /**
     * Load and setup a test case from a data file.
     * @param caseFile
     */
    private AMESMarket loadAndSetupTestCase(File caseFile){
        AMESMarket testMarket = null;

        AMESFrame af = new AMESFrame();

        af.setCaseFile(caseFile);

        boolean success = af.openCaseFileForSimulation();
        assertTrue("Failed to setup market for simulation.", success);

        testMarket = af.getAMESMarket();
        assertNotNull("AMESMarket is null!", testMarket);

        return testMarket;
    }



    /**
     * Read in/load the test file configuration.
     *
     * Internal worker for loading the case file.
     * TODO: Refactor.
     * @param testCaseFile
     * @throws BadDataFileFormatException
     */
    private CaseFileData loadCaseFileData(final File testCaseFile) throws FileNotFoundException, IOException, BadDataFileFormatException {
        CaseFileReader casefileReader = new CaseFileReader();
        return casefileReader.loadCaseFileData(testCaseFile);
    }

    /**
     * Utility method to read the entire contents of a file as a string.
     * @param file
     * @return
     * @throws FileNotFoundException
     */
    private String readFileAsString(File file) throws FileNotFoundException{
        StringBuilder fileContents = new StringBuilder((int)file.length());
        Scanner scanner = new Scanner(file);
        String lineSeparator = System.getProperty("line.separator");
        try {
            while(scanner.hasNextLine()){
                fileContents.append(scanner.nextLine());
                fileContents.append(lineSeparator); //and the line sep back in. Scanner trims it off.
            }
        } finally{
            scanner.close();
        }

        return fileContents.toString();
    }

    /**
     * Load a {@link DailyLoadProfile} from a an array of profiles.
     * @param dayNumber
     * @param profiles
     * @param dlp
     */
    protected void loadProfile(int dayNumber, double[][] profiles, DailyLoadProfile dlp){
        dlp.setDayNumber(dayNumber);
        for(int h = 0; h < profiles.length; h++){
            //don't alias the original. This will cause problems
            //if the DailyLoadProfiles changes a load somewhere in the profile.
            double [] pCopy = Arrays.copyOf(profiles[h], profiles[h].length);
            dlp.setLoadByHour(h, pCopy);
        }
    }

    protected double[][][] read8BusCSV() throws FileNotFoundException {
        return readLoadScenarioCSV(new File("test/amesmarket/8BusMilestone1C.csv"), 364);
    }

    protected double[][][] read5BusCSV() throws FileNotFoundException {
        return readLoadScenarioCSV(new File("test/amesmarket/5BusMilestone1C.csv"), 5);
    }

    /**
     * Read in the data from a CSV representing a LoadScenario. @return
     * @throws FileNotFoundException
     */
    protected double[][][] readLoadScenarioCSV(File inputFile, int numDays) throws FileNotFoundException {
        double[][][] data = new double[numDays][][]; //364 days in the test.
        final int numHours = 24;
        assertTrue(inputFile.getAbsolutePath() + " does not exist.", inputFile.exists());

        Scanner s = new Scanner(inputFile);
        try{
            for(int day = 0; day < data.length; day++){
                data[day] = new double[numHours][];
                //read 24 lines
                for(int hour =  0; hour < numHours; hour++){
                    String line = s.nextLine();
                    //split the line, and convert to doubles
                    String[] elems = line.split(",");
                    int numZones = elems.length;
                    Support.trimAllStrings(elems);
                    data[day][hour] = new double[numZones];
                    assertEquals("Not enough elements", numZones, elems.length);
                    for(int z = 0; z < numZones; z++){
                        data[day][hour][z] = Support.parseDouble(elems[z]);
                    }
                }
            }
        } finally {
            if(s!=null){
                s.close();
            }
        }
        return data;
    }



    /**
     * 'Manually' operate the day ahead market for two instances of
     * the AMESMarket and assert the results are the same.
     *
     * Both m1 and m2 should be completely setup and started.
     *
     * @param m1
     * @param m2
     * @throws IOException
     * @throws BadDataFileFormatException
     */
    protected void compareDayAheadOperation(AMESMarket m1, AMESMarket m2) throws AMESMarketException, IOException {
        int d = 1;
        int h = 0;
        m1.getISO().wholesalePowerMarketOperation(h, d);
        m2.getISO().wholesalePowerMarketOperation(h, d);
        double tol = TestConstants.DOUBLE_EQ;

        compareHour0(m1, m2, tol);
    }

    /**
     * Compare two markets after operating hour 0.
     * @param m1
     * @param m2
     * @param tol
     */
    protected void compareHour0(AMESMarket m1, AMESMarket m2, double tol) {
        DAMarket dam1 = m1.getISO().getDAMarket();
        DAMarket dam2 = m2.getISO().getDAMarket();

        //compare load profiles
        dam1.getLoadProfileByLSE();

        //compare demand bids
        assertEqualArrays("DAM demand bid",
                dam1.getDemandBidByLSE(), dam2.getDemandBidByLSE(), tol);

        //compare hybrid demand bids
        int[][] hb1 = dam1.getDemandHybridByLSE();
        int[][] hb2 = dam2.getDemandHybridByLSE();
        assertEqualArrays("Hybrid demand", hb1, hb2, tol);
    }

    protected void compareHour12(AMESMarket m1, AMESMarket m2, double tol) {
        //is this method comparing enough for hour 12?
        ISO iso1, iso2;
        iso1 = m1.getISO();
        iso2 = m2.getISO();
        DAMarket dam1 = iso1.getDAMarket();
        DAMarket dam2 = iso2.getDAMarket();

        assertEqualArrays("supplyOfferByGen", dam1.getSupplyOfferByGen(), dam2.getSupplyOfferByGen(), tol);
        assertEqualArrays("loadProfileByLSE",  dam1.getLoadProfileByLSE(), dam2.getLoadProfileByLSE(), tol);
        assertEqualArrays("demandBidByLSE", dam1.getDemandBidByLSE(), dam2.getDemandBidByLSE(), tol);
        assertEqualArrays("demandHybridByLSE", dam1.getDemandHybridByLSE(), dam2.getDemandHybridByLSE(), tol);

        assertEqualList("lse price sens. demand by day",
                m1.getLSEAgenPriceSensitiveDemandByDay(),
                m2.getLSEAgenPriceSensitiveDemandByDay(), tol);

        assertEqualList("branch flow by day", m1.getBranchFlowByDay(),
                m2.getBranchFlowByDay(), tol);
        assertEqualList("gen commitment by day",
                m1.getGenAgentCommitmentByDay(),
                m2.getGenAgentCommitmentByDay(), tol);
        assertEqualList("lmp by day", m1.getLMPByDay(), m2.getLMPByDay(), tol);
        assertEqualList("hasSolution by day", m1.getHasSolutionByDay(), m2.getHasSolutionByDay());
    }

    protected void compareHour17(AMESMarket m1, AMESMarket m2, double tol) {
        fail("Not sure what to test from posting");
    }

    /**
     * Operate the first day of the market through hour 17.
     * @param m1
     * @param m2
     * @throws BadDataFileFormatException
     * @throws IOException
     */
    protected void operateThroughH17(AMESMarket m1, AMESMarket m2) throws AMESMarketException, IOException {
        int d = 1;
        double tol = TestConstants.DOUBLE_EQ;

        for(int h = 0; h<=17; h++){
            m1.getISO().wholesalePowerMarketOperation(h, d);
            m2.getISO().wholesalePowerMarketOperation(h, d);
            if(h == 0) {
                compareHour0(m1, m2, tol);
            } else if (h == 12) {
                compareHour12(m1, m2, tol);
            } else if (h == 17) {
                compareHour17(m1, m2, tol);
            }
        }
    }
    
    /**
     * Operate the first day of the market entirely and compare unit commitment vectors.
     * @param m
     * @param day
     * @throws AMESMarketException
     * @throws BadDataFileFormatException
     * @throws IOException
     */
    
    protected void operateThroughDayNumber(AMESMarket m, int day) throws AMESMarketException, IOException {
        for(int h = 0; h<=23; h++){
            m.getISO().wholesalePowerMarketOperation(h, day);
        }
        
    }

}
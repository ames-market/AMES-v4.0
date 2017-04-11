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
package amesmarket;

import static org.junit.Assert.*;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import static testsupport.TestConstants.DOUBLE_EQ;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.internal.ArrayComparisonFailure;

//AMES
import amesmarket.AMESMarket;
import amesmarket.CaseFileData;
import amesmarket.filereaders.BadDataFileFormatException;
import amesmarket.filereaders.CaseFileReader;
import testsupport.AbstractTestAMES;
import testsupport.TestCaseParams;
import testsupport.TestConstants;

/**
 *
 * @author Sean L. Mooney
 */
public class AMESTest extends AbstractTestAMES {

    @Test (expected=AssertionError.class)
    public void ensureAssertionsEnabled() {
        //Make sure jvm assertions are enabled for the
        //tests. Keeps us from missing any assertions that
        //are in the main code.
        assert false;
    }

    /**
     * Make sure the {@link AbstractTestAMES#assertEqualArrays(String, double[][], double[][], double)}
     * is catching errors.
     */
    @Test(expected = ArrayComparisonFailure.class)
    public void doubledoubleArryEqualsSmokeTest1() {
        double[][] d1 = {{3, 4, 5}, {1, 2, 3}};
        double[][] d2 = {{3, 4, 5}, {1, 2, 3.1}};

        assertEqualArrays("Test", d1, d2, 0.001);
    }

    /**
     * Make sure the {@link AbstractTestAMES#assertEqualArrays(String, double[][], double[][], double)}
     * is catching errors.
     */
    @Test
    public void doubledoubleArryEqualsSmokeTest2() {
        double[][] d1 = {{3, 4, 5}, {1, 2, 3}};
        double[][] d2 = {{3, 4, 5}, {1, 2, 3}};

        assertEqualArrays("Test", d1, d2, 0.001);
    }


    @Test
    /**
     * Just test loading the 5 bus example.
     */
    public void testLoad5BusConfig() throws FileNotFoundException, BadDataFileFormatException, IOException{
        CaseFileData testConf = loadCaseFileData(FIVE_BUS_TEST_CASE);

        assertNotNull("Case File Data is null", testConf);
        //Check that the max day was read correctly -- bug in stop condition.
        assertEquals("Incorrect max day.", 5, testConf.iMaxDay);
    }

    @Test(timeout=5000) @Ignore(value="Broken : File format of FIVE_BUS_5_GEN_TEST_CASE needs to be changed")
    public void test5BusExample(){
        AMESMarket testMarket;

        testMarket = loadAndSetupTestCase(FIVE_BUS_TEST_CASE);
        restoreOutStreams();
        runTestMarket(testMarket);

        assertFalse("Error stopped market operation.", testMarket.hasStopCodeError());

        ArrayList<int[]> solsByDay = testMarket.getHasSolutionByDay();
        assertNotNull("No solution by day found", solsByDay);

        int numSols = solsByDay.size();
        int expectedSols = 5;
        assertEquals("Wrong number of solutions by day", expectedSols, numSols);
    }

    @Test
    @Ignore("Need to get AMES_Market writting a data file, and then actually create the expected file.")
    /**
     * Test to ensure we keep getting the same results
     * from the 5 bus example.
     */
    public void test5BusRegession(){
      AMESMarket testMarket;

      testMarket = loadAndSetupTestCase(FIVE_BUS_TEST_CASE);
      runTestMarket(testMarket);

      assertFalse("Error stopped market operation.", testMarket.hasStopCodeError());

      String actualOutput = readResults(FIVE_BUS_TEST_CASE);
      String expectOutput = readExpected(FIVE_BUS_TEST_CASE);

      assertEquals(expectOutput, actualOutput);
    }

    @Test
    public void testFindPeakFromAllScenarios(){

        final double expPeak = 42;
        LoadProfileCollection ls = new LoadProfileCollection();
        DailyLoadProfile dl1 = new DailyLoadProfile(3);
        DailyLoadProfile dl2 = new DailyLoadProfile(3);
        DailyLoadProfile dl3 = new DailyLoadProfile(3);

        double[][] dl1Profiles = {  //peak : 24
                new double[]{1,2,3}
              , new double[]{4, 5, 6}
              , new double[]{7, 8, 9} };
        double[][] dl2Profiles = {
                new double[]{1,2,3}
              , new double[]{10, 11, 12} //peak : 33
              , new double[]{7, 8, 9} };
        double[][] dl3Profiles = {
                new double[]{1,2,3}
              , new double[]{13, 14, 15} //peak : 42
              , new double[]{7, 8, 9} };

        //set up the profiles
        loadProfile(1, dl1Profiles, dl1);
        loadProfile(2, dl2Profiles, dl2);
        loadProfile(3, dl3Profiles, dl3);
        ls.put(dl1); ls.put(dl2); ls.put(dl3);

        assertEquals("Wrong peak load", expPeak, ls.peakTotalLoad(), DOUBLE_EQ);

        //set up the profiles
        //peak in first dlp
        dl1 = new DailyLoadProfile(3);
        dl2 = new DailyLoadProfile(3);
        dl3 = new DailyLoadProfile(3);
        loadProfile(3, dl1Profiles, dl1);
        loadProfile(2, dl2Profiles, dl2);
        loadProfile(1, dl3Profiles, dl3);
        ls.put(dl1); ls.put(dl2); ls.put(dl3);

        assertEquals("Wrong peak load", expPeak, ls.peakTotalLoad(), DOUBLE_EQ);

        //set up the profiles
        //peak in middle dlp
        dl1 = new DailyLoadProfile(3);
        dl2 = new DailyLoadProfile(3);
        dl3 = new DailyLoadProfile(3);
        loadProfile(1, dl1Profiles, dl1);
        loadProfile(3, dl2Profiles, dl2);
        loadProfile(2, dl3Profiles, dl3);
        ls.put(dl1); ls.put(dl2); ls.put(dl3);

        assertEquals("Wrong peak load", expPeak, ls.peakTotalLoad(), DOUBLE_EQ);
    }

    /**
     * Check that the 'AlertGenCo' or Canaries are properly excluded
     * from the generation capacity.
     *
     * TODO-XX Fix testGenCapWithCanary
     * Broken by commit 077ac48. Needs new input data that
     * has the correct number (5) of zones/lses in LoadCase files.
     *
     * @throws IOException
     * @throws BadDataFileFormatException
     */
    @Test @Ignore(value="Broken by commit: 077ac48")
    public void testGenCapWithCanary() throws IOException, AMESMarketException {
        AMESMarket m = loadAndSetupTestCase(new TestCaseParams(
                    new File(TestConstants.TEST_FILE_DIR, "5BusTestCase_5Gen-CanaryTest.dat"), null, null));
        m.buildModel();

        assertEquals(1000.0, m.getMaxGenCapacity(), TestConstants.DOUBLE_EQ);
    }

    @Test
    public void testNumberParser() {
        NumberRecognizer np = new NumberRecognizer();

        assertTrue(np.isInt("+1"));
        assertTrue(np.isInt("1"));
        assertTrue(np.isInt("-1"));
        assertTrue(np.isInt("123456789"));
        assertFalse(np.isInt("3.5"));
        assertFalse(np.isInt("hello"));

        assertEquals(1, np.stoi("1"));
        assertEquals(-1, np.stoi("-1"));
        assertEquals(123456789, np.stoi("123456789"));
        assertEquals(3, np.stoi("3.5"));

        assertTrue(np.isFloatingPoint("+1"));
        assertTrue(np.isFloatingPoint("1"));
        assertTrue(np.isFloatingPoint("-1"));
        assertTrue(np.isFloatingPoint("123456789"));
        assertTrue(np.isFloatingPoint("3.5"));
        assertFalse(np.isFloatingPoint("hello"));

        assertEquals(1, np.stod("+1"), TestConstants.DOUBLE_EQ);
        assertEquals(1, np.stod("1"), TestConstants.DOUBLE_EQ);
        assertEquals(-1, np.stod("-1"), TestConstants.DOUBLE_EQ);
        assertEquals(123456789, np.stod("123456789"), TestConstants.DOUBLE_EQ);
        assertEquals(3.5, np.stod("3.5"), TestConstants.DOUBLE_EQ);

    }

    /**
     * Ensure we can read the data file for the 8bus, 583TestCase datafile.
     *
     * @throws IOException
     * @throws BadDataFileFormatException
     * @throws FileNotFoundException
     */
    @Test
    public void testRead8Bus538GenDataFile() throws FileNotFoundException,
            BadDataFileFormatException, IOException {
        CaseFileReader cfr = new CaseFileReader();
        CaseFileData cfd = cfr.loadCaseFileData(new File(
                TestConstants.TEST_FILE_DIR,
                "8bus538gentestcase/8BusTestCase_538gen.dat"));

        assertNotNull(cfd);

        assertEquals("Incorrect number of generators.", 320, cfd.genData.length);
        assertEquals("Expected LSEDemandSource to be LoadCase.",
                CaseFileData.LSE_DEMAND_LOAD_CASE, cfd.getLSEDemandSource());

        for (int i = 0; i < cfd.lseHybridDemand.length; i++) {
            final String LSE_NAME = "LSE" + (i+1);
            assertEquals(LSE_NAME, cfd.lseHybridDemand[i][0]);
            //27 elements -- 3 datafields + 24 hours worth of flags.
            assertEquals("Wrong number of data elements for " + LSE_NAME, 27, cfd.lseHybridDemand[i].length);
            for (int k = 3; k < cfd.lseHybridDemand[i].length; k++) {
                assertEquals(LSE_NAME + "Hour " + k, 1, cfd.lseHybridDemand[i][k]);
            }
        }
    }

    @Test @Ignore(value="Broken : File format of FIVE_BUS_5_GEN_TEST_CASE needs to be changed")
    public void test5BusSCEDCheckCommitRecord() {
        DecimalFormat df = new DecimalFormat("#,##0");
        AMESMarket m = loadAndSetupTestCase(FIVE_BUS_5_GEN_TEST_CASE);
        restoreOutStreams();

        while (m.getDay() <= 3)
            m.Step();

        int[] commits = m.getGenAgentList().get(0).getCommitmentsForDay(2);
        assertArrayEquals(new int[] { 0, 1, 0, 1, 1, 0, 1, 1, 0, 1, 0, 1, 1, 1,
                0, 0, 0, 0, 0, 0, 0, 1, 0, 1 }, commits);

        double[] dispatches = m.getGenAgentList().get(0).getDispatchesForDay(2);
        assertNotNull(dispatches);


        for (GenAgent ga : m.getGenAgentList()) {

            commits = ga.getCommitmentsForDay(2);
            dispatches = ga.getDispatchesForDay(2);

            StringBuilder cs = new StringBuilder();
            StringBuilder ds = new StringBuilder();

            cs.append(ga.getID() + "\t\t");
            ds.append(ga.getID() + "\t\t");
            for (int i = 0; i < commits.length; i++) {
                cs.append(commits[i]);
                cs.append("\t");
                ds.append(df.format(dispatches[i]));
                ds.append("\t");
            }

            System.out.println(cs.toString());
            System.out.println(ds.toString());
            System.out.println();
        }
    }
}

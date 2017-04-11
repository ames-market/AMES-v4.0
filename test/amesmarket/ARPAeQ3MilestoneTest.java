//TODO: LICENCE

package amesmarket;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import testsupport.AbstractTestAMES;
import testsupport.SCEDWriter;
import testsupport.TestCaseParams;
import testsupport.TestConstants;
import amesmarket.filereaders.BadDataFileFormatException;

/**
 * Test methods for the validation tasks for the Jan 15 (Dec 31) milestone.
 *
 * Milestone 1C description:
 * <p>
 * <b>TASK 1C:</b>
 * </p>
 * <p>
 * <blockquote>
 * <b>Integrate the (variable) load model</b> into the wholesale portion of the
 * IRW test bed (AMES), and <b>verify</b> the extended test bed is working
 * properly.
 * </blockquote>
 * </p>
 *
 *
 * <p>
 * <blockquote>
 * <b>Task 1C Milestones:</b> With stochastic features turned off, the test bed
 * generates the same commitment schedule as baseline deterministic SCED. 30-bus
 * reduced ISO-NE data will be used to test the algorithm. The data will have
 * hourly resolution for one year. The (sampled) distributions of stochastic
 * process model match historical data within a 10% on the first two
 * distributional moments
 * </blockquote>
 * </p>
 *
 * Task 1C has been split into two parts. For the AMES test part of the
 * milestone, the goal is now stated as:
 * <p>
 * <blockquote>
 * <b>Task 1C-II Milestones (IRW Block):</b> Using a single deterministic load
 * scenario S, check that the wholesale part of the IRW test bed (i.e., AMES-
 * TS) generates the same SCED solutions whether S is input into the test bed as
 * a (degenerate) stochastic Load Case through a Load Interface or is input
 * directly “by hand” using alternative means. 30-bus reduced ISO-NE data will
 * be used to test the algorithm. The data will have hourly resolution for one
 * year. The (sampled) distributions of stochastic process model match
 * historical data within a 10% on the first two distributional moments.
 * </blockquote>
 * </p>
 *
 * @author Sean L. Mooney
 */
public class ARPAeQ3MilestoneTest extends AbstractTestAMES{

    /**
     * Implements the validation task on a small test case.
     * @throws BadDataFileFormatException
     * @throws IOException
     */
    @Test  @Ignore("Broken expected data with new SCED.") //FIXME: Plumb in using DCOPFJ SCED/BUC
    public void test5BusMilestone1C() throws BadDataFileFormatException, IOException {
        final double[][][] FIVE_BUS_SCALED_SCENARIO = read5BusCSV();
        assertNotNull(FIVE_BUS_SCALED_SCENARIO);

        run1CIItest(FIVE_BUS_TEST_CASE, FIVE_BUS_SCALED_SCENARIO);
    }

    @Test @Ignore("Broken expected data with new SCED.") //FIXME: Plumb in using DCOPFJ SCED/BUC
    public void test8BusMilestone1C() throws BadDataFileFormatException, IOException {

        double[][][] eightBusScenario = read8BusCSV();
        assertNotNull(eightBusScenario);

        //sanity check for what we read in.
        for(int d = 0; d <364; d++)
            for(int h = 0; h<23; h++){
                assertNotNull(String.format("d:%d, h:%h", d, h), eightBusScenario[d][h]);
                assertEquals(8, eightBusScenario[d][h].length);
            }


        run1CIItest(EIGHT_BUS_MILESTONE_TEST_CASE, eightBusScenario);
    }

    @Test @Ignore("Useful for creating data files, but not for general testing")
    public void testSCEDCheckSanity5Bus() {
        scedSanityCheck(FIVE_BUS_TEST_CASE);
    }

    @Test @Ignore("Useful for creating data files, but not for general testing")
    public void testSCEDCheckSanity8Bus() {
        scedSanityCheck(EIGHT_BUS_MILESTONE_TEST_CASE);
    }

    @Test
    public void testInjectLoadScenarioData() throws FileNotFoundException {
        final double[][][] FIVE_BUS_SCALED_SCENARIO = read5BusCSV();
        final LoadProfileCollection ls = new LoadProfileCollection("TestCase", 1, 1.0,
                FIVE_BUS_SCALED_SCENARIO, true);
        final int MAX_DAY = 5;

        for(int d = 1; d <= MAX_DAY; d++ ) {
            assertNotNull(String.format("Day %d is null", d), ls.get(d));
        }
    }

    /**
     * Log the results from a milestone 1C test by moving the 'usual' ames
     * output file to another name and writing the SCED in its own file.
     *
     * @param am
     * @param testCaseOutput
     * @param testCaseCopy
     * @param scedFile
     * @throws IOException
     */
    private void log1CIItestResults(AMESMarket am, File testCaseOutput, File testCaseCopy, File scedFile) throws IOException {
        //move the output file to one that won't get overwritten.
        try{
            Support.copyFile(testCaseOutput, testCaseCopy); //append an 'r' for read in the load scenarios.
        }catch(FileNotFoundException fne){
            System.err.println("File not found " + fne.getMessage());
        }
        catch(IOException ioe){
            System.err.println("Could not copy " + testCaseCopy.getPath()
                    + " " + ioe.getMessage()
                    );
        } finally {
            testCaseOutput.exists();
            testCaseOutput.delete();
        }
        SCEDWriter scedWriter = new SCEDWriter();
        scedWriter.writeSCEDResult(am, scedFile);
    }

    /**
     * Run a test for the 1c milestone.
     *
     * Similar to {@link #loadAndSetupTestCase(TestCaseParams)}, but
     * uses a different setup mechanism.
     *
     * @param testCaseInfo  base test case information.
     * @param expectedLoadScenarios load scenario to plug in.
     * @throws IOException
     * @throws BadDataFileFormatException 
     */
    private void run1CIItest(TestCaseParams testCaseInfo,
           double[][][] loadScenario) throws IOException, BadDataFileFormatException  {

        AMESMarket[] ams = prepareLoadScenarioInjectMarkets(testCaseInfo, loadScenario);

        AMESMarket amAct, amExp; //actual and 'expected' markets.

        amAct = ams[0];
        amExp = ams[1];

        restoreOutStreams();

        //read in load scenarios
        runTestMarket(amAct);
        log1CIItestResults(amAct, testCaseInfo.getTestCaseOutput(),
                new File(testCaseInfo.getTestCaseOutput().getPath()+"r"),
                new File(testCaseInfo.getTestCaseOutput().getPath()+"r-sced") );
        
        //inject load scenarios
        runTestMarket(amExp);
        log1CIItestResults(amAct, testCaseInfo.getTestCaseOutput(),
                new File(testCaseInfo.getTestCaseOutput().getPath()+"i"),
                new File(testCaseInfo.getTestCaseOutput().getPath()+"i-sced") );

        assertEqualSCED(amExp, amAct, TestConstants.DOUBLE_EQ);

        final boolean expMaxDay = true, expError = false;
        assertStopCodes(amAct, expMaxDay, expError);
        assertStopCodes(amExp, expMaxDay, expError);
    }

    /**
     * Make sure the SCED check method is working by running
     * the market twice, with the exact same TestCase.
     *
     * If these are not the same, we have an instability issue
     * somewhere.
     */
    private void scedSanityCheck(TestCaseParams testCase) {
        AMESMarket am1, am2;

        am1 = loadAndSetupTestCase(testCase);
        restoreOutStreams();
        runTestMarket(am1);

        am2 = loadAndSetupTestCase(testCase);
        restoreOutStreams();
        runTestMarket(am2);
    }

    /**
     * 
     * @param testCaseInfo
     * @param loadScenarioData
     * @return index 0 -> read in sceanrio market. 1 -> injected sceanrio market.
     * @throws BadDataFileFormatException
     * @throws IOException
     * @throws FileNotFoundException
     */
    private AMESMarket[] prepareLoadScenarioInjectMarkets(TestCaseParams testCaseInfo,
            double[][][] loadScenarioData) throws BadDataFileFormatException, FileNotFoundException, IOException {
        AMESMarket mr, mi;
        LoadProfileCollection ls1;
        LoadProfileCollection lsExp;

        //reference to the testcase being used
        final CaseFileData caseFileData = loadCaseFileData(testCaseInfo);
        final int numZones = caseFileData.lseData.length;
        final int numDays = caseFileData.iMaxDay;

        //setup the 'standard' market
        mr = loadAndSetupTestCase(testCaseInfo);
        restoreOutStreams();

        ls1 = new LoadProfileCollection("Direct Value Load Scenarios", 1, 1.0, loadScenarioData, true);

        //make sure that scenario is valid
        ls1.validate("Direct Value Load Scenarios", 1, numDays);

        //There is only 1 scenario. The expected load is the same as the scenario.
        lsExp = new LoadProfileCollection(
                "Direct Value Load Scenarios",
                LoadCaseControl.EXPECTED_LOAD_SCEN_NUM, 1.0,
                loadScenarioData, true);
        lsExp.validate("Direct Value Load Scenarios",
                LoadCaseControl.EXPECTED_LOAD_SCEN_NUM, numDays);

        //Create a load scenario provider that does not scale.
        DefaultLPCProvider manScenProv = new NonScalingLoadScenarioProvider();
        manScenProv.initializeEmpty(numZones, 1, caseFileData.iMaxDay);
        manScenProv.putLoadScenario(1, 1.0, ls1);
        manScenProv.putExpectedLoad(lsExp);

        mi = loadAndSetupTestCase(testCaseInfo);
        mi.setLoadScenarioProvider(manScenProv);
        restoreOutStreams();

        mr.begin();
        mi.begin();

        return new AMESMarket[]{mr, mi};
    }
}

/**
 * A load scenario provider that DOES NOT APPLY SCALING.
 */
class NonScalingLoadScenarioProvider extends DefaultLPCProvider{
    boolean verbose;

    public NonScalingLoadScenarioProvider(){
        this(true);
    }

    public NonScalingLoadScenarioProvider(boolean isVerbose){
        this.verbose = isVerbose;
    }

    /**
     * Override default {@link DefaultLPCProvider#scaleLoadProfileCollections(double, double)}
     * and leave the values alone.
     */
    @Override
    public void scaleLoadProfileCollections(double maxGenCap, double resMargin) throws BadDataFileFormatException{
        if(verbose)
            System.out.println("IGNORING SCALING REQUEST!");
        fail("POKE");
    }
}

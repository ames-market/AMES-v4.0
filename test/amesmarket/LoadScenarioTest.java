//FIXME LICENCE
package amesmarket;

import static org.junit.Assert.*;
import static testsupport.TestConstants.DOUBLE_EQ;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

import org.junit.Ignore;
import org.junit.Test;

import amesmarket.DailyLoadProfile.LoadType;
import amesmarket.filereaders.BadDataFileFormatException;
import amesmarket.filereaders.IZoneIndexProvider;
import amesmarket.filereaders.IZoneIndexProvider.AutomaticIndexProvider;
import amesmarket.filereaders.IZoneIndexProvider.NamedIndexProvider;
import amesmarket.filereaders.LoadCaseScenarioReader;
import amesmarket.filereaders.LoadCaseControlReader;
import amesmarket.probability.IChoice;
import amesmarket.probability.RouletteWheelSelector;
import testsupport.AbstractTestAMES;
import testsupport.TestConstants;

/**
 * 
 * @author Sean L. Mooney
 *
 */
public class LoadScenarioTest extends AbstractTestAMES {

    @Test
    public void test8Bus8GenScenario() throws BadDataFileFormatException {
        LoadCaseControl lcc = new LoadCaseControlReader().read(new File(
                TestConstants.TEST_FILE_DIR, "8BusControlFile.dat"));

        lcc.setZoneNameIndexes(
                new IZoneIndexProvider.DefaultIndexProvider(false));

        lcc.validate();
        lcc.getExpectedLoadProfiles();
        lcc.getAllLoadScenarios();
    }

    /**
     * Check the probability assignment to scenarios with the 'all in 1' format.
     * @throws BadDataFileFormatException 
     */
    @Test
    public void testLoadScenProbAllIn1() throws BadDataFileFormatException {
        LoadCaseControl lcc = new LoadCaseControlReader().read(TestConstants.SIMPLE_CTRL_FILE);
        lcc.setZoneNameIndexes(new IZoneIndexProvider.DefaultIndexProvider());

        LoadProfileCollection ls;
        //check scenario 1, which should have a prob. of .2
        ls = lcc.getLoadScenario(1);
        assertEquals("Load Scenario 1", .2, ls.probability(), TestConstants.DOUBLE_EQ);

        //check scenario 2, which should have a prob of .8
        ls = lcc.getLoadScenario(2);
        assertEquals("Load Scenario 2", .8, ls.probability(), TestConstants.DOUBLE_EQ);

    }

    @Test
    public void testLoadSplitScenProp() throws BadDataFileFormatException {
        LoadCaseControl lcc = new LoadCaseControlReader().read(TestConstants.IND_LOAD_PROF_CONTROL_FILE);
        lcc.setZoneNameIndexes(new IZoneIndexProvider.DefaultIndexProvider());

        LoadProfileCollection ls;
        //check scenario 1, which should have a prob. of .2
        ls = lcc.getLoadScenario(1);
        assertEquals("Load Scenario 1", .2, ls.probability(), TestConstants.DOUBLE_EQ);

        //check scenario 2, which should have a prob of .8
        ls = lcc.getLoadScenario(2);
        assertEquals("Load Scenario 2", .8, ls.probability(), TestConstants.DOUBLE_EQ);

    }
    
    @Test
    public void testComputeExectedTrueLoad() throws BadDataFileFormatException {
        DefaultLPCProvider lsp = new DefaultLPCProvider();
        lsp.initializeFromControlFile(
                TestConstants.COMP_EXP_LOAD_CONTROL,
                new IZoneIndexProvider.DefaultIndexProvider()
                );

        //values expected to have in the computed expected true. 
        double[][][] expectedLoadProfiles = new double[][][] { new double[][] {
                new double[] { 136.25, 231.75, 327.25 },
                new double[] { 137.45, 232.95, 328.45 },
                new double[] { 138.65, 234.15, 329.65 },
                new double[] { 139.85, 235.35, 330.85 },
                new double[] { 141.05, 236.55, 332.05 },
                new double[] { 142.25, 237.75, 333.25 },
                new double[] { 143.45, 238.95, 334.45 },
                new double[] { 144.65, 240.15, 335.65 },
                new double[] { 145.85, 241.35, 336.85 },
                new double[] { 147.05, 242.55, 338.05 },
                new double[] { 148.25, 243.75, 339.25 },
                new double[] { 149.45, 244.95, 340.45 },
                new double[] { 150.65, 246.15, 341.65 },
                new double[] { 151.85, 247.35, 342.85 },
                new double[] { 153.05, 248.55, 344.05 },
                new double[] { 154.25, 249.75, 345.25 },
                new double[] { 155.45, 250.95, 346.45 },
                new double[] { 156.65, 252.15, 347.65 },
                new double[] { 157.85, 253.35, 348.85 },
                new double[] { 159.05, 254.55, 350.05 },
                new double[] { 160.25, 255.75, 351.25 },
                new double[] { 161.45, 256.95, 352.45 },
                new double[] { 162.65, 258.15, 353.65 },
                new double[] { 163.85, 259.35, 354.85 } } };
        LoadProfileCollection expectedExpectedLoad = new LoadProfileCollection(
                "Expected Default", LoadCaseControl.EXPECTED_LOAD_SCEN_NUM,
                Double.NaN, expectedLoadProfiles, true);

        LoadProfileCollection computedExpectedLoad = lsp.getExpectedLoadProfiles();

        assertEqualLoadScenarios("computed expected loaded",
                expectedExpectedLoad, computedExpectedLoad,
                TestConstants.DOUBLE_EQ);
    }

    @Test
    public void testComputeCapacityMarginFactor() throws BadDataFileFormatException {
        final double[][] initialLoads = new double[][]{
                new double[]{15, 100, 35}
        };

        final double[][] expectedLoads = {
                new double[]{15,100,35}
                //new double[]{9.0909,60.6061,21.2121}
        };

        DailyLoadProfile dlp = new DailyLoadProfile(1);
        LoadProfileCollection ls = new LoadProfileCollection();
        loadProfile(1, initialLoads, dlp);
        ls.put(dlp);

        DefaultLPCProvider testLPCProvider = new DefaultLPCProvider();
        testLPCProvider.initializeEmpty(3, 1, 1);
        testLPCProvider.putLoadScenario(1, 1, ls);
        testLPCProvider.putExpectedLoad(new LoadProfileCollection(ls));

        testLPCProvider.scaleLoadProfileCollections(100, .10);

        DailyLoadProfile loadProfs = ls.get(1);

        for(int h = 0; h < 1; h++) {
            assertArrayEquals("Load Profile adjustment is wrong at hour " + h,
                    //The entered values are rounded aggressively -- to 4 decimals.
                    //Use a big tolerance to account for the rounding.
                    expectedLoads[h], loadProfs.getLoadByHour(h), 0.0001);
        }
    }

    @Test
    public void testScaleLoadScenario() {
        final double[][] initialLoads = new double[][]{
                new double[]{20, 30, 40}
                , new double[]{50, 50, 50}
                , new double[]{25, 35, 45}
        };

        final double[][] expectedLoads = {
                new double[]{11.5942, 17.3913, 23.1884}
                , new double[]{28.9855, 28.9855, 28.9855}
                , new double[]{14.4928, 20.2899, 26.087}
        };

        DailyLoadProfile dlp = new DailyLoadProfile(3);
        LoadProfileCollection ls = new LoadProfileCollection();
        loadProfile(1, initialLoads, dlp);
        ls.put(dlp);

        double scaleFactor = .57971;
        ls.scaleScenario(scaleFactor);

        DailyLoadProfile loadProfs = ls.get(1);

        //make sure the profiles where initialized correctly.
        for(int h = 0; h < 3; h++) {
            assertArrayEquals("Load Profile adjustment is wrong",
                    //The entered values are rounded aggressively -- to 4 decimals.
                    //Use a big tolerance to account for the rounding.
                    expectedLoads[h], loadProfs.getLoadByHour(h), .0001);
        }
    }

    @Test
    public void testScaleLoadScenarioProvider() throws AMESMarketException{
        final double genCap = 100; //100MW generation capacity.
        final double resMargin = .15; //15% generation capacity.
        final double tol = .0001; //big tolerance because of rounding in expected.

        final double[][] initialLoads = new double[][]{
                new double[]{20, 30, 40}
                , new double[]{50, 50, 50}
                , new double[]{25, 35, 45}
        };

//        final double[][] expectedLoads = {
//                new double[]{11.5942, 17.3913, 23.1884}
//                , new double[]{28.9855, 28.9855, 28.9855}
//                , new double[]{14.4928, 20.2899, 26.087}
//        };
        
        final double[][] expectedLoads = {
                new double[]{20, 30, 40}
                , new double[]{50, 50, 50}
                , new double[]{25, 35, 45}
        };

        DefaultLPCProvider lsp = new DefaultLPCProvider();
        lsp.initializeEmpty(3, 1, 1); //3 zones, 1 scenario, 1 day

        DailyLoadProfile dlp = new DailyLoadProfile(3);
        LoadProfileCollection ls = new LoadProfileCollection();

        loadProfile(1, initialLoads, dlp);

        ls.put(dlp);

        lsp.putLoadScenario(1, 1.0, ls);
        lsp.putExpectedLoad(new LoadProfileCollection(ls));//expected is the same as the daily, since there is only 1 profile.
        lsp.determineActualScenario(1); //it's the only one we've got.

        DailyLoadProfile loadProfs = lsp.getActualScenario().get(1);
        DailyLoadProfile expectedProfs = lsp.getExpectedLoadProfiles().get(1);

        //make sure the profiles where initialized correctly.
        for(int h = 0; h < 3; h++) {
            assertArrayEquals("Actual Load Profile adjustment is wrong",
                    initialLoads[h], loadProfs.getLoadByHour(h), tol);
            assertArrayEquals("Expected Load Profile adjustment is wrong",
                    initialLoads[h], expectedProfs.getLoadByHour(h), tol);
        }

        lsp.scaleLoadProfileCollections(genCap, resMargin);

        for(int h = 0; h < 3; h++) {
            assertArrayEquals("Actual Load Profile adjustment is wrong",
                    expectedLoads[h], loadProfs.getLoadByHour(h), tol);
            assertArrayEquals("Expected Load Profile adjustment is wrong",
                    expectedLoads[h], expectedProfs.getLoadByHour(h), tol);
        }
    }

    @Test
    public void testFindPeakHour(){
        final double expPeak = 24;
        DailyLoadProfile dlp = new DailyLoadProfile(3);
        dlp.setDayNumber(1);
        dlp.setLoadByHour(0, new double[]{1, 2, 3}); //Total Load 6
        dlp.setLoadByHour(1, new double[]{4, 5, 6}); //Total Load 15
        dlp.setLoadByHour(2, new double[]{7, 8, 9}); //Total Load 24

        assertEquals("Wrong peak load", expPeak, dlp.peakHourLoad(), DOUBLE_EQ);

        //try it with the peak hour not in the middle.
        dlp = new DailyLoadProfile(3);
        dlp.setDayNumber(1);
        dlp.setLoadByHour(0, new double[]{1, 2, 3}); //Total Load 6
        dlp.setLoadByHour(1, new double[]{7, 8, 9}); //Total Load 24
        dlp.setLoadByHour(2, new double[]{4, 5, 6}); //Total Load 15

        assertEquals("Wrong peak load", expPeak, dlp.peakHourLoad(), DOUBLE_EQ);

        //try it with the peak hour at the start.
        dlp = new DailyLoadProfile(3);
        dlp.setDayNumber(1);
        dlp.setLoadByHour(0, new double[]{7, 8, 9}); //Total Load 24
        dlp.setLoadByHour(1, new double[]{1, 2, 3}); //Total Load 6
        dlp.setLoadByHour(2, new double[]{4, 5, 6}); //Total Load 15

        assertEquals("Wrong peak load", expPeak, dlp.peakHourLoad(), DOUBLE_EQ);
    }

    /**
     * Check the equality between two {@link ILoadProfileCollectionProvider}'s.
     *
     * Two providers are considered equal if they provider both the same
     * 'actual' scenario and expected scenario.
     * @param lsp1
     * @param lsp2
     * @param tol tolerance for double equality
     * @throws BadDataFileFormatException
     */
    private void assertLoadProvidersEqual(ILoadProfileCollectionProvider lsp1, ILoadProfileCollectionProvider lsp2, double tol) throws AMESMarketException {
        assertEqualLoadScenarios("", lsp1.getActualScenario(),
                lsp2.getActualScenario(), tol);
        assertEqualLoadScenarios("", lsp1.getExpectedLoadProfiles(),
                lsp2.getExpectedLoadProfiles(), tol);
    }


    @Test @Ignore(value="Broken : FIVE_BUS_TEST_CASE format need to be updated")
    public void testLoadProvider5Bus() throws AMESMarketException, IOException {
        final double tol = TestConstants.DOUBLE_EQ;
        DefaultLPCProvider lsp1, lsp2;
        CaseFileData caseFileData = loadCaseFileData(FIVE_BUS_TEST_CASE);

        lsp1 = new DefaultLPCProvider();
        lsp1.initializeFromControlFile(new File(
                "TEST-DATA/5BusControlFile.dat"),
                new IZoneIndexProvider.DefaultIndexProvider());

        lsp2 = new NonScalingLoadScenarioProvider();
        lsp2.initializeEmpty(3, 1, caseFileData.iMaxDay);
        lsp2.putLoadScenario(1, 1,
                new LoadProfileCollection("foo", 1, 1, read5BusCSV(), true)
                );

        lsp1.determineActualScenario(1);
        //1530.0 apriori calculated max gen capacity for the Test Case.
        lsp1.scaleLoadProfileCollections(1530.0, .15);

        lsp2.determineActualScenario(1);

        assertLoadProvidersEqual(lsp1, lsp2, tol);
    }

    @Test
    //TODO: Fix expected data for this test.
    @Ignore("Commit 3bb1e11 updated input data without changing the expected data file. Expected data now borked.")
    public void testLoadProvider8Bus() throws AMESMarketException, IOException{
        final double tol = 0.0000001;
        DefaultLPCProvider lsp1, lsp2;
        CaseFileData caseFileData = loadCaseFileData(EIGHT_BUS_MILESTONE_TEST_CASE);

        lsp1 = new DefaultLPCProvider();
        lsp1.initializeFromControlFile(new File(
                "TEST-DATA/Ames_scenarios/8BusARPAe1Ccontrol.dat"),
                new IZoneIndexProvider.DefaultIndexProvider());

        lsp2 = new NonScalingLoadScenarioProvider();
        lsp2.initializeEmpty(8, 1, caseFileData.iMaxDay);
        lsp2.putLoadScenario(1, 1,
                new LoadProfileCollection("foo", 1, 1, read8BusCSV(), true)
                );

        lsp1.determineActualScenario(1);
        lsp1.scaleLoadProfileCollections(34145.8, .15);

        lsp2.determineActualScenario(1);

        assertLoadProvidersEqual(lsp1, lsp2, tol);
    }

    /**
     * Test reading a load case file with the Actual LoadProfiles
     * specified.
     * @throws BadDataFileFormatException
     */
    @Test
    public void testReadLoadCaseWithActualProfiles() throws BadDataFileFormatException {
        LoadCaseControlReader lccr = new LoadCaseControlReader();
        LoadCaseControl lcc = lccr.read(new File(TestConstants.TEST_FILE_DIR,
                "TestActualLoadProfiles/ActualLPControl.dat"));

        assertTrue(lcc.hasExternalActualLoadProfiles());
    }

    @Test
    public void testReadLoadCaseActualProfiles2() throws BadDataFileFormatException {
        LoadCaseControlReader lccr = new LoadCaseControlReader();
        LoadCaseControl lcc = lccr.read(new File(TestConstants.TEST_FILE_DIR,
                "8BusTestCase/loadcase/master_control_file.dat"));

        assertTrue(lcc.hasExternalActualLoadProfiles());
    }

    @Test
    public void testReadWindCase1() throws BadDataFileFormatException {
        LoadCaseControlReader lccr = new LoadCaseControlReader();
        lccr.setValidateAfterRead(false);
        LoadCaseControl lcc = lccr.read(TestConstants.EIGHT_ZONE_538GEN_MASTER);

        assertEquals(
                "TEST-DATA/8busNgentestcase/loadcase/2011-06-01/AMESscenLoad1_1.dat",
                lcc.getScenarioFilePath(1, 1)
                );
        assertEquals(
                "TEST-DATA/8busNgentestcase/loadcase/2011-06-01/AMESWind1_1.dat",
                lcc.getScenarioWindFilePath(1, 1)
                );
    }

    /**
     * Test reading the LoadCase from Woodruff's group
     * for the March 31, 2013 deadlines.
     * @throws BadDataFileFormatException
     */
    @Test
    public void testRead8bus538genLoadScen1() throws BadDataFileFormatException {
        LoadCaseControlReader lccr = new LoadCaseControlReader();
        LoadCaseControl lcc = lccr.read(TestConstants.EIGHT_ZONE_538GEN_MASTER);
        NamedIndexProvider zidx = new AutomaticIndexProvider();
        lcc.setZoneNameIndexes(zidx);

        LoadProfileCollection s1 = lcc.getLoadScenario(1);

        assertNotNull("LoadScenario, day 1 null", s1);
    }

    @Test
    public void roullettWheelTest() {
        class c implements IChoice {

            double p;
            int v;

            public c(int i, double p) {
                this.v = i;
                this.p = p;
            }

            @Override
            public double probability() {
                return p;
            }

            public String toString() {
                return "i: " + v + " p: " + p;
            }
        }

        RouletteWheelSelector<c> rws = new RouletteWheelSelector<c>(
                Arrays.asList(new c(0, .5), new c(1, .25), new c(2, .125),
                        new c(3, .125)), new Random(1));

        int numChoices = 10000;

        double[] counts = new double[4];

        for (int i = 0; i < numChoices; i++) {
            c c = (rws.selectElement());
            counts[c.v] += 1.0;
        }

        assertEquals(.5, counts[0] / (double) numChoices, .01);
        assertEquals(.25, counts[1] / (double) numChoices, .01);
        assertEquals(.125, counts[2] / (double) numChoices, .01);
        assertEquals(.125, counts[3] / (double) numChoices, .01);
    }

    /**
     * Make sure the data is being reordered correctly
     * from a LoadProfile collection.
     * @throws BadDataFileFormatException
     */
    @Test
    public void testZoneReorderCorrect() throws BadDataFileFormatException {
        NamedIndexProvider nip = new NamedIndexProvider();
        nip.put("z3", 1);
        nip.put("z2", 3);
        nip.put("z1", 2);


        //Assemble the data string
        //needs 24 hours, even though we only care about the first hour for this test.
        String loads = "";
        for(int h = 1; h<=24; h++)
            loads += String.format(" 1   %d    100  200 300\n", h);

        String scenDesc =
                " AllOf : AMES_3, Scenario 1 Load\n" +
                " Day Hour z1   z2  z3\n" +
                loads;


        LoadCaseControl lcc = new LoadCaseControl("AMES_3", 3, 1, 1, nip, 30);

        LoadCaseScenarioReader lcsr = new LoadCaseScenarioReader(lcc);
        lcsr.setExpectedScenarioNumber(1);
        lcsr.setLoadType(LoadType.LOAD);

        LoadProfileCollection scen = lcsr.read(new StringReader(scenDesc));
        DailyLoadProfile dlp = scen.get(1);
        assertNotNull(dlp);

        double[] hour0 = dlp.getLoadByHour(0);

        assertEquals(300, hour0[0], TestConstants.DOUBLE_EQ);
        assertEquals(100, hour0[1], TestConstants.DOUBLE_EQ);
        assertEquals(200, hour0[2], TestConstants.DOUBLE_EQ);
    }

    @Test
    public void testNetLoad1() throws BadDataFileFormatException {
        //CaseControl
        LoadCaseControl lcc = new LoadCaseControlReader().read(
                new File(TestConstants.TEST_FILE_DIR,
                        "NetLoad/Test1/control.dat")
                );

        lcc.setZoneNameIndexes(new  AutomaticIndexProvider());

        //Get a load
        LoadProfileCollection s = lcc.getLoadScenario(1);

        //assert values are what I want.
        DailyLoadProfile d1 = s.get(1);
        final double[][] expectedNetLoad = new double[][]
                { { 10, 20, 30 }
                 ,{ 40, 50, 60 }
                 , { 70, 80, 90 } };

        for (int i = 0; i < 3; i++) {
            for (int z = 0; z < 3; z++) {
                assertArrayEquals(expectedNetLoad[i], d1.getLoadByHour(i), TestConstants.DOUBLE_EQ);
            }
        }
    }

    @Test
    public void testNetLoad2() throws BadDataFileFormatException {
        //CaseControl
        LoadCaseControl lcc = new LoadCaseControlReader().read(
                new File(TestConstants.TEST_FILE_DIR,
                        "NetLoad/Test2/control.dat")
                );

        lcc.setZoneNameIndexes(new  AutomaticIndexProvider());

        //Get a load
        LoadProfileCollection s = lcc.getLoadScenario(1);

        //assert values are what I want.
        DailyLoadProfile d1 = s.get(1);
        DailyLoadProfile d2 = s.get(2);
        final double[][] expectedNetLoad1 = new double[][]
                { { 10, 20, 30 }
                 ,{ 40, 50, 60 }
                 , { 70, 80, 90 } };
        final double[][] expectedNetLoad2 = new double[][]
                { { 30, 20, 30 }
                 ,{ 40, 60, 60 }
                 , { 70, 80, 100 } };

        for (int i = 0; i < 3; i++) {
            for (int z = 0; z < 3; z++) {
                assertArrayEquals(expectedNetLoad1[i], d1.getLoadByHour(i), TestConstants.DOUBLE_EQ);
            }
        }

        for (int i = 0; i < 3; i++) {
            for (int z = 0; z < 3; z++) {
                assertArrayEquals(expectedNetLoad2[i], d2.getLoadByHour(i), TestConstants.DOUBLE_EQ);
            }
        }

    }

    @Test
    public void testPeakNetLoad() throws BadDataFileFormatException {
        LoadCaseControl lcc = new LoadCaseControlReader().read(
                new File(TestConstants.TEST_FILE_DIR,
                        "NetLoad/Test2/control.dat")
                );

        lcc.setZoneNameIndexes(new  AutomaticIndexProvider());

        //Get a load
        LoadProfileCollection s = lcc.getLoadScenario(1);

        final double p = s.peakTotalLoad();
        final double expLoad = 250; //day 2, hour 3

        assertEquals(expLoad, p, TestConstants.DOUBLE_EQ);
    }

    @Test
    public void testScalePeakNetLoad() throws BadDataFileFormatException {

        DefaultLPCProvider lp = new DefaultLPCProvider();
        lp.initializeFromControlFile(new File(TestConstants.TEST_FILE_DIR,
                "NetLoad/Test2/control.dat"), new AutomaticIndexProvider());

        lp.scaleLoadProfileCollections(100, .1);

        double expPeak = 250;//90.909090909090909090909;
        assertEquals(expPeak, lp.getPeakLoad(), TestConstants.DOUBLE_EQ);
    }

    @Ignore
    public void testCapacityMarginOutOfBounds() throws BadDataFileFormatException {
        DefaultLPCProvider lp = new DefaultLPCProvider();
        double[] outOfBounds = new double[]{-1.0, -2.0, 1.1};

        for (double cm : outOfBounds) {
            try {
                lp.scaleLoadProfileCollections(100, cm);
                fail("Missed IllegalArgument " + cm);
            } catch (IllegalArgumentException iae) {} //ignore, this was supposed to happen.
        }
    }
}

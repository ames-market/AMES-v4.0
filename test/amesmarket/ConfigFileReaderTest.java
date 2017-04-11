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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import amesmarket.LoadCaseControl;
import amesmarket.LoadProfileCollection;
import amesmarket.DailyLoadProfile.LoadType;
import amesmarket.filereaders.AbstractConfigFileReader;
import amesmarket.filereaders.BadDataFileFormatException;
import amesmarket.filereaders.CaseFileReader;
import amesmarket.filereaders.DictionaryReader;
import amesmarket.filereaders.IZoneIndexProvider;
import amesmarket.filereaders.IZoneIndexProvider.DefaultIndexProvider;
import amesmarket.filereaders.IZoneIndexProvider.NamedIndexProvider;
import amesmarket.filereaders.LoadCaseControlReader;
import amesmarket.filereaders.LoadCaseLabelInfo;
import amesmarket.filereaders.LoadCaseScenarioReader;

import testsupport.TestConstants;

public class ConfigFileReaderTest {

    @Test
    public void testValidateAllScenarios() throws BadDataFileFormatException{
        File controlFile = TestConstants.SIMPLE_CTRL_FILE;
        LoadCaseControlReader lccr = new LoadCaseControlReader();

        //Read the control
        LoadCaseControl lcc = lccr.read(controlFile);
        lcc.setZoneNameIndexes(new DefaultIndexProvider());

        lcc.validate();
        assertNotNull("Null load case control", lcc);

        for(int sn = 1; sn < lcc.getNumLoadScenarios(); sn++){
            LoadProfileCollection lscen = lcc.getLoadScenario(sn);
            assertNotNull("Scenario " + sn + " is null", lscen);
            assertEquals("Wrong load scenario!", sn, lscen.getScenarioNumber());
        }

        //use the control file to read the expected load
        LoadProfileCollection exLoad = lcc.getExpectedLoadProfiles();
        assertNotNull("Expected Load Scenario is null", exLoad);
        assertEquals("Not the expected load scenario",
                LoadCaseControl.EXPECTED_LOAD_SCEN_NUM,
                exLoad.getScenarioNumber()
                );
    }


    @Test
    public void testRemoveComments() throws BadDataFileFormatException{
        String ts = "//Comment 1\n" +
                "Line 1\n" +
                "Line 2 //comment2\n" +
                "\n\n\n"; //and some blank lines at EOF for good measure.

        Reader in = new StringReader(ts);

        TestableConfigFileReader r = new TestableConfigFileReader();

        List<String> res = r.read(in);

        assertEquals("Wrong number lines read.", 2, res.size());
        assertEquals("Line 1", res.get(0));
        assertEquals("Line 2", res.get(1));

    }

    @Test
    public void testReadEmptyFile() throws BadDataFileFormatException {
        File f = createTmpFile();

        class EmptyReader extends AbstractConfigFileReader<Void> {
            @Override
            protected Void read() {
                try {
                    move(true);
                } catch (BadDataFileFormatException e) {
                    assertTrue(e.getMessage().contains(UNEXPECTED_EOF));
                    return null;
                }

                //the reader should have failed on the first move.
                fail("Did not catch the eof in an empty file.");
                return null;
            }

        }

        new EmptyReader().read(f);
    }

    @Test
    public void testReadLSEDemandSource() throws BadDataFileFormatException {
        //Test for the TestCase demand source
        String input = "LSEDemandSource TestCase";
        TestableCaseFileReader lfr = new TestableCaseFileReader();
        CaseFileData data = lfr.loadCaseFileData(new StringReader(input), false);

        assertEquals(CaseFileData.LSE_DEMAND_TEST_CASE, data.getLSEDemandSource());

        //Test for the LoadCase demand source
        data = null;
        input = "LSEDemandSource LoadCase";
        data = lfr.loadCaseFileData(new StringReader(input), false);


        assertEquals(CaseFileData.LSE_DEMAND_LOAD_CASE, data.getLSEDemandSource());
    }

    @Test
    public void testReadScucType() throws BadDataFileFormatException {
        //Test for the TestCase demand source
        String input = "SCUC_Type Deterministic";
        CaseFileReader lfr = new CaseFileReader();
        CaseFileData data = lfr.loadCaseFileData(new StringReader(input));

        assertEquals(SCUC.SCUC_DETERM, data.getSCUCType());

        //Test for the LoadCase demand source
        input = "SCUC_Type Stochastic";
        data = lfr.loadCaseFileData(new StringReader(input));

        assertEquals(SCUC.SCUC_STOC, data.getSCUCType());
    }
    
    /**
     * Test that an exception gets thrown if the lse demand source is unknown.
     * @throws BadDataFileFormatException
     */
    @Test(expected=BadDataFileFormatException.class)
    public void testUnknownLSEDemandSource() throws BadDataFileFormatException {
        String input = "LSEDemandSource TestCASE";

        CaseFileReader lfr = new CaseFileReader();
        lfr.loadCaseFileData(new StringReader(input));
    }

    @Test
    public void testReadNoLoadSection() throws BadDataFileFormatException {
        String input =
                "#ZoneNamesStart\n" + "z1\nz2\n" + "#ZoneNamesEnd\n"
                + "#GenCostStart\n" +
                        "z1 5.5 10 20 30\n " +
                        "z2 10 15 20 35\n"
                + "#GenCostEnd\n";

        CaseFileReader lfr = new CaseFileReader();
        CaseFileData cfd = lfr.loadCaseFileData(new StringReader(input));

        assertNotNull(cfd);

        assertEquals(5.5, cfd.getNoLoadCostForGen("z1"), TestConstants.DOUBLE_EQ);
        assertEquals(10, cfd.getNoLoadCostForGen("z2"), TestConstants.DOUBLE_EQ);
    }

    @Test
    public void testReadNoLoadCosts() throws BadDataFileFormatException, FileNotFoundException, IOException {
        CaseFileReader lfr = new CaseFileReader();
        CaseFileData cfd = lfr.loadCaseFileData(
                new File(TestConstants.TEST_FILE_DIR, "8BusTestCase_18gen.dat"));

        for (int i = 1; i <= 18; i++) {
            String genName = "GenCo" + i;
            assertEquals(i*5, cfd.getNoLoadCostForGen(genName), TestConstants.DOUBLE_EQ);
            assertEquals(i*1.1, cfd.getColdStartUpCostForGen(genName), TestConstants.DOUBLE_EQ);
            assertEquals(i*2, cfd.getHotStartUpCostForGen(genName), TestConstants.DOUBLE_EQ);
            assertEquals(i*3, cfd.getShutDownCostForGen(genName), TestConstants.DOUBLE_EQ);
        }
    }

    @Test
    public void testReadSampleScenarioControlFile() throws BadDataFileFormatException{
        String input =
                "CaseName : Example 1\n" +
                "NumZones : 2\n" +
                "NumScenarios : 3\n" +
                "MaxDay : 3\n" +
                "Scenario 1 Prob : .2\n" +
                "Scenario 2 Prob : .3\n" +
                "Scenario 3 Prob : .5\n" +
                "Scenario 1 File : foo1.dat\n" +
                "Scenario 2 File : foo2.dat\n" +
                "Scenario 3 File : foo3.dat\n" ;

        LoadCaseControlReader lscr = new LoadCaseControlReader();
        //do not validate. It won't pass (because of missing files),
        //but we want to test the rest of the properties.
        lscr.setValidateAfterRead(false);

        LoadCaseControl lsc = lscr.read(new StringReader(input));

        assertNotNull(lsc);
        assertEquals("Wrong case name", "Example 1", lsc.getCaseName());
        assertEquals("Wrong number of zones", 2, lsc.getNumZones());
        assertEquals("Wrong number of load scenarios", 3, lsc.getNumLoadScenarios());
        assertEquals("Wrong number max day", 3, lsc.getMaxDay());
        assertEquals("Wrong scen 1 prob", 0.2, lsc.getScenarioProbability(1), TestConstants.DOUBLE_EQ);
        assertEquals("Wrong scen 2 prob", 0.3, lsc.getScenarioProbability(2), TestConstants.DOUBLE_EQ);
        assertEquals("Wrong scen 3 prob", 0.5, lsc.getScenarioProbability(3), TestConstants.DOUBLE_EQ);
        assertEquals("Wrong scen 1 file", "foo1.dat", lsc.getScenarioFilePath(1, 1));
        assertEquals("Wrong scen 2 file", "foo2.dat", lsc.getScenarioFilePath(1, 2));
        assertEquals("Wrong scen 3 file", "foo3.dat", lsc.getScenarioFilePath(1, 3));

        //finally, make sure we fail the validation on
        //the first data file not existing (which is absolutely true)
        try{
            lsc.validate();
            fail("Expected a validation exception!");
        }catch(BadDataFileFormatException bfe){
            assertTrue("Actual Exception Message: " + bfe.getMessage(),
                    bfe.getMessage().contains("foo1.dat does not exist"));
        }
    }

    @Test
    /**
     * Read the version of the control file with each scenario/day listed explicitly.
     *
     * @throws BadDataFileFormatException
     */
    public void testReadSampleScenarioControlFile2() throws BadDataFileFormatException{
        String input =
                "CaseName : Example 1\n" +
                "NumZones : 2\n" +
                "NumScenarios : 3\n" +
                "MaxDay : 1\n" +
                "Day 1 Scenario 1 Prob : .2\n" +
                "Day 1 Scenario 2 Prob : .3\n" +
                "Day 1 Scenario 3 Prob : .5\n" +
                "Day 1 Scenario 1 File : foo1.dat\n" +
                "Day 1 Scenario 2 File : foo2.dat\n" +
                "Day 1 Scenario 3 File : foo3.dat\n" ;

        LoadCaseControlReader lscr = new LoadCaseControlReader();
        //do not validate. It won't pass (because of missing files),
        //but we want to test the rest of the properties.
        lscr.setValidateAfterRead(false);

        LoadCaseControl lsc = lscr.read(new StringReader(input));

        assertNotNull(lsc);
        assertEquals("Wrong case name", "Example 1", lsc.getCaseName());
        assertEquals("Wrong number of zones", 2, lsc.getNumZones());
        assertEquals("Wrong number of load scenarios", 3, lsc.getNumLoadScenarios());
        assertEquals("Wrong number max day", 1, lsc.getMaxDay());
        assertEquals("Wrong scen 1 prob", 0.2, lsc.getScenarioProbability(1, 1), TestConstants.DOUBLE_EQ);
        assertEquals("Wrong scen 2 prob", 0.3, lsc.getScenarioProbability(1, 2), TestConstants.DOUBLE_EQ);
        assertEquals("Wrong scen 3 prob", 0.5, lsc.getScenarioProbability(1, 3), TestConstants.DOUBLE_EQ);
        assertEquals("Wrong scen 1 file", "foo1.dat", lsc.getScenarioFilePath(1, 1));
        assertEquals("Wrong scen 2 file", "foo2.dat", lsc.getScenarioFilePath(1, 2));
        assertEquals("Wrong scen 3 file", "foo3.dat", lsc.getScenarioFilePath(1, 3));

        //finally, make sure we fail the validation on
        //the first data file not existing (which is absolutely true)
        try{
            lsc.validate();
            fail("Expected a validation exception!");
        }catch(BadDataFileFormatException bfe){
            assertTrue("Actual Exception Message: " + bfe.getMessage(),
                    bfe.getMessage().contains("foo1.dat does not exist"));
        }
    }

    @Test
    public void testReadLoadScenario() throws FileNotFoundException, BadDataFileFormatException{
        File scenFile = TestConstants.LOAD_SCEN_SAMPLE;
        assertTrue(scenFile.getName() + " does not exist.", scenFile.exists());

        final int EXP_SCEN_NUM = 2;
        final int EXP_NUM_ZONES = 8;

        LoadCaseControl lsc = new LoadCaseControl();
        lsc.setNumLoadScenarios(1);
        lsc.setCaseName("TestCase1");
        lsc.setMaxDay(28);
        lsc.setNumZones(EXP_NUM_ZONES);
        lsc.setZoneNameIndexes(new DefaultIndexProvider());
        
        LoadCaseScenarioReader lsr = new LoadCaseScenarioReader(lsc);
        lsr.setExpectedScenarioNumber(EXP_SCEN_NUM);
        LoadProfileCollection ls  = lsr.read(scenFile);

        assertNotNull(ls);

        assertEquals("Wrong scenario number.", EXP_SCEN_NUM, ls.getScenarioNumber());
    }

    @Test
    public void testReadMultilpleProfileFiles() throws BadDataFileFormatException{
        File scenFile = TestConstants.IND_LOAD_PROF_CONTROL_FILE;

        assertTrue(scenFile.getName() + " does not exist.", scenFile.exists());

        final int EXP_NUM_ZONES = 8;
        final int MAX_DAY = 2;

        LoadCaseControlReader lscr = new LoadCaseControlReader();

        LoadCaseControl lsc = lscr.read(scenFile);
        lsc.setZoneNameIndexes(new DefaultIndexProvider());

        assertEquals("Wrong number of scenarios", 2, lsc.getNumLoadScenarios());
        assertEquals("Wrong case name", "IndProfileFiles", lsc.getCaseName());
        assertEquals("Wrong max day", 2, lsc.getMaxDay());
        assertEquals("Wrong number of zones", 2, lsc.getNumZones());

        LoadProfileCollection ls;

        //check scenario 1
        ls = lsc.getLoadScenario(1);
        assertNotNull("Scenario 1 is null", ls);
        assertEquals("Scenario number wrong.", 1, ls.getScenarioNumber());
        assertEquals("Scenario prob wrong.", .2, ls.probability(), TestConstants.DOUBLE_EQ);
        for(int day = 1; day <= MAX_DAY; day++){
            DailyLoadProfile dlp = ls.get(day);
            assertNotNull("Scenario 1, day " + day, dlp);
        }

        //check scenario 2
        ls = lsc.getLoadScenario(2);
        assertNotNull("Scenario 2 is null", ls);
        assertEquals("Scenario number wrong.", 2, ls.getScenarioNumber());
        assertEquals("Scenario 2 prob wrong.", .8, ls.probability(), TestConstants.DOUBLE_EQ);
        for(int day = 1; day <= MAX_DAY; day++){
            DailyLoadProfile dlp = ls.get(day);
            assertNotNull("Scenario 2, day " + day, dlp);
        }
    }

    @Test
    public void testInvalidZoneNumber(){
        final int EXP_NUM_ZONES = 2;

        NamedIndexProvider zip = new NamedIndexProvider();
        zip.put("z1", 0);
        zip.put("z2", 1);

        LoadCaseControl lsc = new LoadCaseControl();
        lsc.setNumLoadScenarios(1);
        lsc.setCaseName("TestCase1");
        lsc.setMaxDay(50);
        lsc.setNumZones(EXP_NUM_ZONES);
        lsc.setZoneNameIndexes(zip);

        String exampleScenario =
                "AllOf : TestCase1, Scenario 1 Load\n" +
                "Day    Hour    z1  z3\n"
                ;

        LoadCaseScenarioReader lsr = new LoadCaseScenarioReader(lsc);
        lsr.setLoadType(LoadType.LOAD);
        try {
            lsr.read(new StringReader(exampleScenario));
        } catch (BadDataFileFormatException e) {
            assertEquals("Wrong exception msg.",
                    "LINE 2: Unknown zone name 'z3' at column 3", e.getMessage());
        }
    }

    @Test
    public void testFixupControlFileLocation(){
        final String loadCaseControlName = "LCF.dat";
        CaseFileData cfd = new CaseFileData();
        cfd.loadCaseControlFile = loadCaseControlName;

        //null
        cfd.adjustLoadControlFilePath(null);
        assertEquals(loadCaseControlName, cfd.loadCaseControlFile);

        //path
        cfd.loadCaseControlFile = loadCaseControlName;
        String rootPath = "../Foo";
        cfd.adjustLoadControlFilePath(new File(rootPath));
        assertEquals("../"+loadCaseControlName, cfd.loadCaseControlFile);

        //given absolute path
        String absFileName = "/home/sean/ames/LCF.dat";
        cfd.loadCaseControlFile = absFileName;
        cfd.adjustLoadControlFilePath(new File(rootPath));
        assertEquals(absFileName, cfd.loadCaseControlFile);
    }

    @Test
    public void testAuxControlFiles() throws BadDataFileFormatException {
        LoadCaseControlReader lccr = new LoadCaseControlReader();
        LoadCaseControl lcc = lccr.read(TestConstants.AUX_CONTROL_MASTER1);

        assertNotNull(lcc);
    }

    @Test
    public void testLSRValidateHeadersBug () throws BadDataFileFormatException, IOException {
        String input =
        " //Expected Loads for day 3 \n" +
        " PartOf : AMES_3, ExpectedLoad \n" +
        " Day : 3 \n" +
        " Hour  Z1   Z2   Z3   Z4   Z5   Z6   Z7   Z8   \n" +
        " 1 2964.49692892 1054.44603797 1014.1855367 521.331185422 810.742001905 1570.3692958 1565.1264611 2654.71040134  \n" +
        " 2 2758.67425494 995.245050525 994.972897833 510.789631568 775.450580436 1442.44639036 1604.4860441 2456.31646972  \n" +
        " 3 2547.24216503 970.150267728 966.263281692 498.306097479 748.506989212 1372.06008543 1558.57344471 2409.26610208  \n" +
        " 4 2321.63903891 970.740590423 983.729487164 494.309218414 733.194204011 1321.68620466 1527.50296801 2371.3507997  \n" +
        " 5 2266.06774428 995.513403653 1011.94558416 504.013816134 739.742494329 1388.11358988 1577.07285441 2468.71476316  \n" +
        " 6 2389.08431449 1103.53533577 1085.88349148 548.556767727 773.463997412 1445.41606473 1660.25263747 2545.82913597  \n" +
        " 7 3227.4334755 1380.33512239 1205.03043668 656.199809619 865.773306209 1617.2168751 1836.62066687 2871.0461783  \n" +
        " 8 3622.58751298 1578.11502851 1373.5927986 730.575296369 998.803694776 1838.53471778 2063.33151986 3233.1159706  \n" +
        " 9 3743.51974708 1650.6737812 1420.00133556 749.760655655 1074.03113035 1988.13361052 2175.39657807 3432.26397639  \n" +
        " 10 4227.71357229 1724.60667284 1523.37994072 763.958269505 1139.77717729 2111.53724202 2244.95598907 3604.71326889  \n" +
        " 11 4265.3357686 1773.01917877 1513.9663972 774.907902273 1190.5607682 2195.47343928 2318.52974258 3696.59791483  \n" +
        " 12 4358.1021959 1810.28532286 1521.97268622 779.682964519 1208.82127156 2247.79578008 2364.60313275 3716.38127687  \n" +
        " 13 4627.25504655 1808.59196361 1537.71394346 777.757004171 1222.88392835 2294.79877674 2386.49821996 3727.3844028  \n" +
        " 14 4649.36210743 1830.00985978 1476.3951894 779.57073882 1217.03387708 2275.38377184 2439.67199964 3769.24407723  \n" +
        " 15 4748.8705502 1739.53079124 1442.07615892 773.583865548 1232.55373831 2322.27119176 2453.55349518 3814.95687387  \n" +
        " 16 4755.74886647 1742.62655136 1435.83479714 764.867885665 1231.77048819 2291.66915682 2454.84619165 3799.28456769  \n" +
        " 17 4703.54423127 1589.41184654 1419.52834881 768.609790585 1216.30500093 2416.87335834 2501.39529087 3743.56435216  \n" +
        " 18 4684.60519606 1577.01974471 1362.55684996 763.693098574 1172.92938077 2066.37890283 2487.83215493 3850.35540487  \n" +
        " 19 4384.28754665 1568.30006976 1304.40701901 768.433576005 1172.63028506 1746.09930234 2429.91885683 3633.94861939  \n" +
        " 20 4229.24006736 1563.67165062 1248.31117309 741.73613389 1147.58370306 1750.14741673 2394.55441269 3502.60379992  \n" +
        " 21 4243.11178949 1567.15287084 956.656646104 753.50220955 1149.95729159 1803.84237898 2406.43962139 3452.4560265  \n" +
        " 22 4194.26624162 1423.38277837 967.136101249 724.704026719 1098.85334096 2196.61527974 2266.76358872 3315.84278823  \n" +
        " 23 3848.10820955 1265.17884862 1410.81198477 661.991304537 982.561684476 2140.52061002 2085.23323316 3094.74988831  \n" +
        " 24 3447.8822092 -132.812768627 1342.80252035 -19.5868488119 515.503814517 1136.29095039 -115.743364662 -439.841010438";

        File tempDataFile = createTmpFile();
        tempDataFile.deleteOnExit();
        BufferedWriter tmpWriter = new BufferedWriter(new FileWriter(tempDataFile));
        tmpWriter.write(input);
        tmpWriter.close();

        LoadCaseControl lcc = new LoadCaseControl("AMES_3", 8, 3, 1, new IZoneIndexProvider.DefaultIndexProvider(),24);

        LoadCaseScenarioReader lcsr = new LoadCaseScenarioReader(lcc);
        lcsr.setExpectedScenarioNumber(LoadCaseControl.EXPECTED_LOAD_SCEN_NUM);
        HashMap<Integer, File> scenPieces = new HashMap<Integer, File>();
        scenPieces.put(LoadCaseControl.EXPECTED_LOAD_SCEN_NUM, tempDataFile);
        lcsr.readCompositScenario(scenPieces);
    }

    @Test
    public void testMultipleScenProbDecl() throws BadDataFileFormatException {
        String input =
                "CaseName : Foo\n" +
                "MaxDay : 1 \n" +
                "NumScenarios : 1\n" +
                "NumZones : 1\n" +
                "Scenario 1 Day 1 Prob : 0.2\n" +
                "Scenario 1 Day 1 Prob : 0.3";
        LoadCaseControlReader lccr = new LoadCaseControlReader();

        try{
            lccr.read(new StringReader(input));
        }catch(BadDataFileFormatException ex) {
            assertTrue(ex.getMessage().contains("Probability already set"));
            return;
        }
        fail("Expected a BadDataFileFormatException");
    }

    @Test
    public void testReadSCUCFromTestCase() throws FileNotFoundException, BadDataFileFormatException, IOException {
        CaseFileReader scucDataReader = new CaseFileReader();
        CaseFileData cfd = scucDataReader.loadCaseFileData(new File(TestConstants.TEST_FILE_DIR,
                "SCUCDefinitionsTestCase.dat"));

        //PowerGenTO
        assertEquals(1000, cfd.getPowGenT0("GenCo1"), TestConstants.DOUBLE_EQ);
        assertEquals(2000, cfd.getPowGenT0("GenCo2"), TestConstants.DOUBLE_EQ);

        //UnitOn
        assertEquals(1, cfd.unitOnT0("GenCo1"));
        assertEquals(0, cfd.unitOnT0("GenCo2"));

        //MinUpTime
        assertEquals(1, cfd.minUpTime("GenCo1"));
        assertEquals(2, cfd.minUpTime("GenCo2"));

        //MinDownTime
        assertEquals(3, cfd.minDownTime("GenCo1"), TestConstants.DOUBLE_EQ);
        assertEquals(4, cfd.minDownTime("GenCo2"), TestConstants.DOUBLE_EQ);

        //NominalRampUp
        assertEquals(520, cfd.nominalRampUp("GenCo1"), TestConstants.DOUBLE_EQ);
        assertEquals(300, cfd.nominalRampUp("GenCo2"), TestConstants.DOUBLE_EQ);

        //NominalRampDown
        assertEquals(620, cfd.nominalRampDown("GenCo1"), TestConstants.DOUBLE_EQ);
        assertEquals(400, cfd.nominalRampDown("GenCo2"), TestConstants.DOUBLE_EQ);

        //StatupRampLim
        assertEquals(500, cfd.startupRampLim("GenCo1"), TestConstants.DOUBLE_EQ);
        assertEquals(100, cfd.startupRampLim("GenCo2"), TestConstants.DOUBLE_EQ);

        //ShutdownRampLim
        assertEquals(550, cfd.shutdownRampLim("GenCo1"), TestConstants.DOUBLE_EQ);
        assertEquals(110, cfd.shutdownRampLim("GenCo2"), TestConstants.DOUBLE_EQ);

        //Reserve Req
        assertEquals(0, cfd.reserveReq("GenCo1"));
        assertEquals(0, cfd.reserveReq("GenCo2"));

        //Schedule
        assertEquals(1, cfd.scucSchedule1("GenCo1"));
        assertEquals(0, cfd.scucSchedule1("GenCo2"));

        //Schedule2
        assertEquals(0, cfd.scucSchedule2("GenCo1"));
        assertEquals(1, cfd.scucSchedule2("GenCo2"));

    }

    @Test
    public void testAlertGenCo() throws IOException, BadDataFileFormatException {
        String testCaseDesc = "#ZoneNamesStart\n" +
                        "1\n2\n3\n4\n" +
                        "#ZoneNamesEnd\n" +
                        "#GenDataStart\n" +
                        "//    Name          ID       atBus            SCost               a               b            capL            capU       InitMoney\n" +
                        "    GenCo1  1   1   56.9    14  0.005   0   110 1000000\n" +
                        "    GenCo2  2   1   0.11    15  0.006   0   100 1000000\n" +
                        "    GenCo3  3   3   2267.53 23  0.01    0   520 1000000\n" +
                        "    GenCo4  4   4   5.19    30  0.012   0   200 1000000\n" +
                        "#GenDataEnd\n" +
                        "#AlertGenCoStart\n" +
                        "GenCo3\n" +
                        "#AlertGenCoEnd\n";
        File testCaseFile = createTmpFile();
        FileWriter tcfr = new FileWriter(testCaseFile);
        tcfr.write(testCaseDesc);
        tcfr.close();

        CaseFileReader cfr = new CaseFileReader();
        CaseFileData readData = cfr.loadCaseFileData(testCaseFile);

        assertFalse("GenCo1 should not be a canary", readData.genData[0].isCanary);
        assertFalse("GenCo2 should not be a canary", readData.genData[1].isCanary);
        assertTrue("GenCo3 should be a canary", readData.genData[2].isCanary);
        assertFalse("GenCo4 should not be a canary", readData.genData[3].isCanary);
    }

    @Test
    public void testNoFindAlertGenCo() throws IOException, BadDataFileFormatException {
        String testCaseDesc = "#ZoneNamesStart\n" +
                        "1\n2\n3\n4\n" +
                        "#ZoneNamesEnd\n" +
                        "#GenDataStart\n" +
                        "//    Name          ID       atBus            SCost               a               b            capL            capU       InitMoney\n" +
                        "    GenCo1  1   1   56.9    14  0.005   0   110 1000000\n" +
                        "    GenCo2  2   1   0.11    15  0.006   0   100 1000000\n" +
                        "    GenCo3  3   3   2267.53 23  0.01    0   520 1000000\n" +
                        "    GenCo4  4   4   5.19    30  0.012   0   200 1000000\n" +
                        "#GenDataEnd\n" +
                        "#AlertGenCoStart\n" +
                        "GenCO3\n" +
                        "#AlertGenCoEnd\n";
        File testCaseFile = createTmpFile();
        FileWriter tcfr = new FileWriter(testCaseFile);
        tcfr.write(testCaseDesc);
        tcfr.close();

        PrintStream stderr = System.err;

        //intercept stderr to catch the expected warning message.
        final ByteArrayOutputStream myOut = new ByteArrayOutputStream();
        System.setErr(new PrintStream(myOut));

        CaseFileReader cfr = new CaseFileReader();
        cfr.loadCaseFileData(testCaseFile);

        System.setErr(stderr); //restore the err stream, just in case something else gets printed there.

        assertEquals("[WARNING] AlertGen GenCO3 Not Found\n", myOut.toString());
    }

    @Test
    /**
     * Test that the zones indexes are set up correctly.
     */
    public void readNamedZonesTestCase() throws FileNotFoundException, BadDataFileFormatException, IOException {
        CaseFileReader cfr = new CaseFileReader();
        CaseFileData cfd =cfr.loadCaseFileData(new File(
                TestConstants.TEST_FILE_DIR, "5BusNamedZones/5BusTestCase_5gen.dat"
                ));

        IZoneIndexProvider zi = cfd.getZoneNames();

        assertEquals(1, zi.get("z1"));
        assertEquals(2, zi.get("z2"));
        assertEquals(3, zi.get("z3"));
        assertEquals(4, zi.get("z4"));
        assertEquals(5, zi.get("z5"));
    }

    @Test
    public void testDefaultZoneIndexProvider(){
        IZoneIndexProvider zip = new IZoneIndexProvider.DefaultIndexProvider(false);

        assertEquals(1, zip.get("Zone1"));
        assertEquals(12, zip.get("z12"));
        assertEquals(6, zip.get("6"));
        assertEquals(-1, zip.get("zone"));
    }

    @Test
    public void testLabelInfoWind() throws BadDataFileFormatException {
        LoadCaseLabelInfo info = new LoadCaseLabelInfo("Scenario 10 Wind Day 202 : foo/bar/baz.dat");

        assertTrue(info.hasDay());
        assertTrue(info.hasWindDecl());
        assertFalse(info.hasLoadDecl());
        assertTrue(info.isScenario());

    }

    @Test
    public void testLabelInfoLoad() throws BadDataFileFormatException {
        LoadCaseLabelInfo info = new LoadCaseLabelInfo("Scenario 10 Load Day 202 : foo/bar/baz.dat");

        assertTrue(info.hasDay());
        assertTrue(info.hasLoadDecl());
        assertFalse(info.hasWindDecl());
        assertTrue(info.isScenario());

    }

    @Test
    public void testDictParser1() {
        String dict1 = "{ foo : bar}";
        DictionaryReader dr = new DictionaryReader(new StringReader(dict1));

        Map<String, String> dict = dr.read();


        assertFalse(dr.getErrorMessage(), dr.readFailed());
        assertFalse(dr.getWarningMessage(), dr.hasWarning());
        assertEquals("bar", dict.get("foo"));
    }

    @Test
    public void testDictParser2() {
        String dict1 = "{foo:bar,foo1:bar1,foo2:bar2}";
        DictionaryReader dr = new DictionaryReader(new StringReader(dict1));

        Map<String, String> dict = dr.read();


        assertFalse(dr.getErrorMessage(), dr.readFailed());
        assertFalse(dr.getWarningMessage(), dr.hasWarning());
        assertEquals("bar", dict.get("foo"));
        assertEquals("bar1", dict.get("foo1"));
        assertEquals("bar2", dict.get("foo2"));
    }

    @Test
    public void testDictParserEOLUnix() {
        String dict1 = "{foo:bar,foo1:bar1,\nfoo2:bar2}";
        DictionaryReader dr = new DictionaryReader(new StringReader(dict1));

        Map<String, String> dict = dr.read();


        assertFalse(dr.getErrorMessage(), dr.readFailed());
        assertFalse(dr.getWarningMessage(), dr.hasWarning());
        assertEquals("bar", dict.get("foo"));
        assertEquals("bar1", dict.get("foo1"));
        assertEquals("bar2", dict.get("foo2"));
    }

    @Test
    public void testDictParserEOLWindows() {
        String dict1 = "{foo:bar,foo1:bar1,\r\nfoo2:bar2}";
        DictionaryReader dr = new DictionaryReader(new StringReader(dict1));

        Map<String, String> dict = dr.read();


        assertFalse(dr.getErrorMessage(), dr.readFailed());
        assertFalse(dr.getWarningMessage(), dr.hasWarning());
        assertEquals("bar", dict.get("foo"));
        assertEquals("bar1", dict.get("foo1"));
        assertEquals("bar2", dict.get("foo2"));
    }

    /**
     * Create temporary files with consistent prefix/suffix.
     * @return a fresh temp file.
     */
    static File createTmpFile() {
        File tmp = null;
        try{
            tmp = File.createTempFile("ames-ts-unittest", ".tmp");
        } catch(IOException ioe) { //we're just done if there's a problem.
            fail(ioe.getMessage());
        }

        return tmp;
    }

    /**
     * A simple little reader that returns the list of lines read in.
     *
     * @author Sean L. Mooney
     *
     */
    static class TestableConfigFileReader
    extends AbstractConfigFileReader<List<String>>{

        @Override
        protected List<String> read() throws BadDataFileFormatException {
            ArrayList<String> l = new ArrayList<String>();

            while(true){
                move(false);
                if(currentLine == null)
                    break;

                l.add(currentLine);
            }

            return l;
        }
    }

    static class TestableCaseFileReader extends CaseFileReader {
        @Override
        //Expose the method for testing.
        public CaseFileData loadCaseFileData(final Reader testCaseInput, boolean doFinishActions) throws BadDataFileFormatException {
            return super.loadCaseFileData(testCaseInput, doFinishActions);
        }
    }
}

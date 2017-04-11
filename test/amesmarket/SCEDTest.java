package amesmarket;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import amesmarket.extern.psst.DataFileWriter;
import amesmarket.filereaders.BadDataFileFormatException;
import org.junit.Ignore;

import testsupport.AbstractTestAMES;
import testsupport.TestCaseParams;

/**
 * Unit tests for the external scuc.
 * @author Sean L. Mooney
 *
 */
public class SCEDTest extends AbstractTestAMES{

    @Test
    public void testWritePyomoScenDat() throws IOException, AMESMarketException {
        AMESMarket tm = loadAndSetupTestCase(FIVE_BUS_5_GEN_TEST_CASE);
        final int numLSEAgents = tm.getNumLSEAgents();
        final int numHours = 24;

        DataFileWriter dfw = new DataFileWriter();

        File f = File.createTempFile("ames", "-tmp");

        try{
            tm.Initialize();
            ILoadProfileCollectionProvider lcp = tm.getLoadScenarioProvider();
            LoadProfileCollection scenario = lcp.getLoadCaseControl().getLoadScenario(1);

            double[][] loadProfileLSE = new double[numLSEAgents][numHours];

            for (int j = 0; j < numLSEAgents; j++) {
                for (int h = 0; h < numHours; h++) {
                    loadProfileLSE[j][h] = scenario.get(1).getLoadByHour(h)[j];
                }

            }

            dfw.writeScenDatFile(f, tm, 1, loadProfileLSE, numHours);
        }
        finally {
            f.delete();
        }
    }

    @Test @Ignore("Broken : SCED.dat missing")
    public void testReadSced() throws AMESMarketException, IOException{
        //Don't delete the generated files. This test reads them
        //outside of the normal lifetime of the files.
        System.setProperty("DEL_INTER_FILES", "false");
        AMESMarket testScedMarket = loadAndSetupTestCase(new TestCaseParams(
                new File("TEST-DATA/8BusTestCase_8gen.dat"), null, null));

        restoreOutStreams();

        testScedMarket.buildModel();
        ISO iso = testScedMarket.getISO();
        iso.wholesalePowerMarketOperation(0, 1);
        iso.wholesalePowerMarketOperation(1, 1);
        iso.getSCED().solveOPF();
    }
}

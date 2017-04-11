package amesmarket;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import amesmarket.extern.common.CommitmentDecision;
import amesmarket.extern.psst.DataFileWriter;
import amesmarket.filereaders.BadDataFileFormatException;
import amesmarket.filereaders.IZoneIndexProvider;
import amesmarket.filereaders.LoadCaseControlReader;
import org.junit.Ignore;

import testsupport.AbstractTestAMES;
import testsupport.TestConstants;
import testsupport.Test_DirectoryVariables;
import testsupport.Test_TestCaseParameters;


/**
 * 
 * @author Dheepak Krishnamurthy
 *
 */

public class LoadAndWindTest extends AbstractTestAMES {

    @Test @Ignore(value="Broken : Market fails to be setup using WINDLOAD_GEN_TEST_CASE1")
    public void testLoadAndWindWithDifferentReserves() throws FileNotFoundException, IOException, AMESMarketException {
       
        AMESMarket m1 = loadAndSetupTestCase(WINDLOAD_GEN_TEST_CASE1);
        AMESMarket m2 = loadAndSetupTestCase(WINDLOAD_GEN_TEST_CASE2);
        ArrayList<Integer> resultOfComparison;

        
        String lbl = "gen commitment by day";
        List<double[][]> exp;
        List<double[][]> act;
               
        double tol = TestConstants.DOUBLE_EQ;
        
        String scucTypeString;

        int numLSEAgents = m1.getNumLSEAgents();
        int numGenAgents = m1.getNumGenAgents();
        double rR = m1.getReserveRequirements();
        int scucType = m1.getSCUCType();
        
        origSysOut.println("Total number of LSEAgents in market 1 : " + numLSEAgents);
        origSysOut.println("Total number of GenAgents in market 1 : " + numGenAgents);
        origSysOut.println("The reserve requirement in market 1 : " + rR);
        if(scucType==1) {
            scucTypeString = "Stochastic" ;
            }
        else {
            scucTypeString = "Deterministic";
        }
        origSysOut.println("SCUC in market 1 : " + scucTypeString);
        
        numLSEAgents = m2.getNumLSEAgents();
        numGenAgents = m2.getNumGenAgents();
        rR = m2.getReserveRequirements();
        scucType = m2.getSCUCType();

        origSysOut.println("Total number of LSEAgents in market 2 : " + numLSEAgents);
        origSysOut.println("Total number of GenAgents in market 2 : " + numGenAgents);
        origSysOut.println("The reserve requirement in market 2 : " + rR);
        if(scucType==1) {
            scucTypeString = "Stochastic" ;
            }
        else {
            scucTypeString = "Deterministic";
        }
        origSysOut.println("SCUC in market 2 : " + scucTypeString + "\n");
        
        origSysOut.println("Initializing markets... \n");

            m1.Initialize();
            m2.Initialize();
            
            //restoreOutStreams();
            
            origSysOut.println("Starting simulation... \n");
            
            for ( int d=1; d<=2; d++ ) {
            
            origSysOut.println("Running Market 1 Day " + d);
            
            operateThroughDayNumber(m1,d);
            List<CommitmentDecision> commitmentSchedule1 = m1.getISO().getGenSchedule();
            //origSysOut.println("The size of the commitmentschedule1 variable is : " + commitmentSchedule1.size());
            
            origSysOut.println("Running Market 2 Day " + d);

            operateThroughDayNumber(m2,d);
            List<CommitmentDecision> commitmentSchedule2 = m2.getISO().getGenSchedule();
            //origSysOut.println("The size of the commitmentschedule2 variable is : " + commitmentSchedule2.size());
            
            resultOfComparison= new ArrayList<Integer>();

            origSysOut.println("\nSimulation complete\n\nComparing results...\n");

            for( int generator=0 ; generator < commitmentSchedule1.size() ; generator++) {
                if(commitmentSchedule2.get(generator).toString().equals(commitmentSchedule1.get(generator).toString())) {
                    resultOfComparison.add(1);
                } else {
                    resultOfComparison.add(0);
                    origSysOut.println("Market 1 results : \n" + commitmentSchedule1.get(generator).toString());
                    origSysOut.println("Market 2 results : \n" + commitmentSchedule2.get(generator).toString());
                }
            }
            
            if(resultOfComparison.contains(0)) {
                origSysOut.println("Comparison Result : \n" + resultOfComparison.toString());
            }
            else {
                origSysOut.println("Both markets give identical results\n");
            }
            
            }
            
            origSysOut.println("Test Complete");
    }

}

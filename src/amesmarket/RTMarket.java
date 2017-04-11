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

// RTMarket.java
// Real-time market

package amesmarket;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import amesmarket.extern.common.CommitmentDecision;
import amesmarket.extern.psst.DataFileWriter;
import amesmarket.extern.psst.PSSTSCED;
import amesmarket.filereaders.BadDataFileFormatException;

/**
 * Real-time market.
 */
public class RTMarket {

    //Real time market's data
    private AMESMarket ames;
    private ISO iso;

    /*
     * The values of these arrays come from
     * the SCED. The PSSTSced allocates fresh arrays
     * each time it is called. Which means we do not
     * need to worry about aliasing issues with these
     * arrays and can just return the references.
     */
    private int[] hasSolution;
    private double[][] rtDispatches;
    private double[][] rtBranchFlow;
    private double[][] rtLMPs;
    private double[][] rtProductionCost;
    private double[][] rtStartupCost;
    private double[][] rtShutdownCost;

    private double[][] supplyOfferByGen;
    private double[][] priceSensitiveDispatch;

    private int numGenAgents;
    private int numLSEAgents;
    private int numSupplyOfferParams;
    private int numHoursPerDay;

    BUC buc;
    private final PSSTSCED sced;

    //TODO-X : Parameterize file paths. Probably should be accessible
    //from the AMESMarket instance.
    private final File scedOutFile = new File("RTSCED.dat");
    private final File rtRefModelFile = new File("SCUCresources/ScenarioData/RTRefernceModel.dat");
    private final File unitCommitmentFile = new File("rt-unitcommitments.dat");


    // constructor
    public RTMarket(ISO iso, AMESMarket model) {

        //System.out.println("Created a RTMarket objecct");
        ames = model;
        this.iso=iso;
        numGenAgents = ames.getNumGenAgents();
        numLSEAgents = ames.getNumLSEAgents();
        numHoursPerDay = ames.getNumHoursPerDay();
        numSupplyOfferParams = 4;

        supplyOfferByGen = new double[numGenAgents][numSupplyOfferParams];

        priceSensitiveDispatch = new double[numHoursPerDay][numLSEAgents];

        sced = new PSSTSCED(model, model.getBaseS(),
                unitCommitmentFile,
                rtRefModelFile,
                scedOutFile);
    }

    public void realTimeOperation(int h, int d) {
        System.out.println("Hour " + h + " Day " + d +": Real Time Market operation.");
        supplyOfferByGen=iso.getSupplyOfferByGenRT();
        priceSensitiveDispatch=iso.getPriceSensitiveDispatchRT();
    }

    /**
     *
     * @param h
     * @param d
     * @throws AMESMarketException
     */
    public void evaluateRealTimeBidsOffers(
            List<CommitmentDecision> genCoCommitments,
            //double[][] supplyOfferRT, double[] dailyPriceSensitiveDispatchRT,
            double[][] rtDemand,
            int h, int d) throws AMESMarketException {


        DataFileWriter dfw = new DataFileWriter();

        //write the correct data files.
        dfw.writeGenCommitments(genCoCommitments, unitCommitmentFile);
        dfw.writeScenDatFile(rtRefModelFile, ames, d, rtDemand, ames.NUM_HOURS_PER_DAY);


        //
        sced.solveOPF();

        //pull the data back out
        hasSolution  = sced.getHasSolution();
        //power
        rtDispatches = sced.getDailyCommitment();
        rtBranchFlow = sced.getDailyBranchFlow();
        //costs
        rtLMPs = sced.getDailyLMP();
        //sced.getDailyPriceSensitiveDemand();
        rtProductionCost = sced.getProductionCost();
        rtStartupCost = sced.getStartupCost();
        rtShutdownCost = sced.getShutdownCost();
    }

//    public double[][] getSupplyOfferByGen() {
//        return supplyOfferByGen;
//    }
//
//    public double[][] getPriceSensitiveDispatch() {
//        return priceSensitiveDispatch;
//    }

    public void setBUC(BUC buc) {
        this.buc = buc;
    }

    public int[] hasSolution() {
        return hasSolution;
    }

    /**
     * @return the rtDispatches
     */
    public double[][] getRtDispatches() {
        return rtDispatches;
    }

    /**
     * @return the rtBranchFlow
     */
    public double[][] getRtBranchFlow() {
        return rtBranchFlow;
    }

    /**
     * @return the rtLMPs
     */
    public double[][] getRtLMPs() {
        return rtLMPs;
    }

    /**
     * @return the rtProductionCost
     */
    public double[][] getRtProductionCost() {
        return rtProductionCost;
    }

    /**
     * @return the rtStartupCost
     */
    public double[][] getRtStartupCost() {
        return rtStartupCost;
    }

    /**
     * @return the rtShutdownCost
     */
    public double[][] getRtShutdownCost() {
        return rtShutdownCost;
    }

    /**
     * @return the rtRefModelFile
     */
    public File getRtRefModelFile() {
        return rtRefModelFile;
    }

}

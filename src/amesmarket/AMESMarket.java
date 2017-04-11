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

// AMESMarket.java - main class for AMESMarket project
//
// Reference: DynTestAMES Working Paper
// Available online at http://www.econ.iastate.edu/tesfatsi/DynTestAMES.JSLT.pdf



package amesmarket;

// Repast
import uchicago.src.sim.engine.SimModelImpl;
import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.engine.SimInit;
import uchicago.src.sim.engine.BasicAction;
import uchicago.src.sim.gui.DisplaySurface;
import uchicago.src.sim.util.Random;
import uchicago.src.sim.engine.Controller;

// JReLM
import edu.iastate.jrelm.rl.rotherev.variant.VREParameters;
import edu.iastate.jrelm.core.BasicLearnerManager;
import edu.iastate.jrelm.gui.BasicSettingsEditor;

// Java
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import amesmarket.filereaders.BadDataFileFormatException;
import amesmarket.filereaders.IZoneIndexProvider.NamedIndexProvider;
import amesmarket.probability.RouletteWheelSelector;
import amesmarket.CaseFileData.SCUCInputData;
import amesmarket.SimulationStatusListener.StatusEvent;


public class AMESMarket extends SimModelImpl {

    /**
     * Logger for extra printing that doesn't need to go through System.out
     */
    public static Logger LOGGER = Logger.getLogger("amesmarket.AMESMarket");

    public static final int GRID_X_SIZE = 40;
    public static final int GRID_Y_SIZE = 40;
    public static final int NUM_HOURS_PER_DAY = 24;
    public static final int NUM_HOURS_PER_DAY_UC = 30; // Number of hours for the rolling horizon. 

    public double COOLING;
    public double EXPERIMENTATION;
    public double INIT_PROPENSITY;
    public double RECENCY;

    public int M1;
    public int M2;
    public int M3;
    public double RIMAX_L;
    public double RIMAX_U;
    public double RIMIN_C;
    public double SLOPE_START;

    public double [][]genLearningParameters;

    /**
     * max days of model simulation
     */
    public int DAY_MAX;
    public boolean bMaximumDay;
    public boolean bThreshold;

    public int RANDOM_SEED;

    /**
     * foe stop number
     */
    private int stopCode;

    /**
     * Marker for error causing the stop of market operation.
     */
    private static final int STOP_CODE_ERROR = 0x1000000; //BIT 25


    private int gridXSize = GRID_X_SIZE;
    private int gridYSize = GRID_Y_SIZE;
    private int numHoursPerDay = NUM_HOURS_PER_DAY;
    private int numIntervalsInSim = NUM_HOURS_PER_DAY_UC;
    private int numNodes;      //K
    private int numBranches;   //N
    private int numGenAgents;  //I
    private int numLSEAgents;  //J
    private double reserveRequirements;
    private double[][] nodeData;
    private double[][] branchData;
    private double[][] genData;
    private boolean[]  alertGenMarkers;
    private Map<String, SCUCInputData> extraGenCoParams;
    private double[][] lseData;
    private double[][][] lsePriceSensitiveDemand;
    private int   [][] lseHybridDemand;
    private double baseS;
    private double baseV;

    /**
     * A storage class. Acts as an intermediary
     * storage location between moving data in from the main control
     * GUI and finishing the model setup, as a callback from RePast.
     */
    private INIT init;

// Model time
    private int hour;
    private int day;
    private boolean isConverged; // used to determine if the model needs to be stopped
    private boolean isGenActionProbabilityConverged; // used to determine if the model needs to be stopped
    private boolean isGenLearningResultConverged; // another stopping rule, check is gen's learning results are stable
    private boolean isGenDailyNetEarningConverged;
    private int dayMax;
    private double dThresholdProbability;
    private double dGenPriceCap;
    private double dLSEPriceCap;
    private int iStartDay;
    private int iCheckDayLength;
    private double dActionProbability;
    private boolean bActionProbabilityCheck;
    private int iLearningCheckStartDay;
    private int iLearningCheckDayLength;
    private double dLearningCheckDifference;
    private boolean bLearningCheck;
    private double dDailyNetEarningThreshold;
    private boolean bDailyNetEarningThreshold;
    private int iDailyNetEarningStartDay;
    private int iDailyNetEarningDayLength;
    /**
     * Name of the file where the LoadCaseControl is stored.
     */
    //private String loadCaseControlFile;
    
    /**
     * Margin of generation capacity reserved for reliability.
     */
    //private double capacityMargin;

    private CaseFileData testcaseConfig;

/// Repast required variables
    private Schedule schedule;
    private DisplaySurface displaySurf;

// Model variables
    private TransGrid transGrid;
    private ISO iso;
    private ArrayList<GenAgent> genAgentList;
    private Map<String, GenAgent> genAgentsByName;
    private ArrayList<LSEAgent> lseAgentList;

    private ILoadProfileCollectionProvider loadScenarioProvider;
   

//  Learning variables
    private ActionDomain ad;
    int numLowerRIs;
    int numUpperRIs;
    int numUpperCaps;
    double RIMaxL;
    double RIMaxU;
    double RIMinC;
    double slopeStart;

    // Learning parameters
    private double[] coolingOfGen;
    private double[] initPropensityOfGen;
    private double[] experimentationOfGen;
    private double[] recencyOfGen;
    private int[] learningRandomSeedsOfGen;

    private ArrayList<double[][]> genAgentSupplyOfferByDay;
    private ArrayList<int[]> hasSolutionByDay;
    private ArrayList<double[][]> lseAgentPriceSensitiveDemandByDay;
    private ArrayList<double[][]> lseAgentSurplusByDay;
    private ArrayList<double[][]> lseAgentSurplusWithTrueCost;
    private ArrayList<double[][]> lseAgentPriceSensitiveDemandWithTrueCost;
    private ArrayList<double[][]> genAgentProfitAndNetGainByDay;
    private ArrayList<double[][]> genAgentActionPropensityAndProbilityByDay;
    private ArrayList<double[][]> genAgentProfitAndNetGainWithTrueCost;
    private ArrayList<double[][]> genAgentCommitmentWithTrueCost;
    private ArrayList<double[][]> genAgentCommitmentByDay, genAgentRealTimeCommitmentByDay;

    private ArrayList<double[][]> branchFlowByDay;
    private ArrayList<double[][]> LMPByDay,realTimeLMPByDay,realTimeBranchFlowByDay;
    private ArrayList<double[][]> LMPWithTrueCost;
    private ArrayList<double[][]> productionCostsByDay, startupCostsByDay, shutdownCostsByDay;

    private ArrayList<double[][]> lastDayGenActions;

    private Controller ModelController=null;
    private boolean bCalculationEnd=false;

    private final boolean deleteIntermediateFiles;

// RePast required methods

    public String getName() {
        return "AMES Market";
    }

    public String[] getInitParam() {
        String[] initParams= {"NumNodes","NumBranches","NumGenAgents","NumLSEAgents"};
        return initParams;
    }

    /**
     * The first method SimModelImpl calls
     */
    @Override
    public void setup() {
        //System.out.println("Running setup");

        // Tear down any objects created over the course of the run to null.
        transGrid = null;
        iso = null;
        genAgentList = new ArrayList<GenAgent>();
        genAgentsByName = new HashMap<String, GenAgent>();
        lseAgentList = new ArrayList<LSEAgent>();
        schedule = new Schedule(1);

        setInitialStructuralParameters(init);
        setInitialLearningParameters();
        setInitialActionDomainParameters();
    }

//maybe this is related with input output method
    private void setInitialStructuralParameters(INIT init) {

        setNumNodes((int) init.getNodeData()[0][0]);
        setNumBranches(init.getBranchData().length);
        setNumGenAgents(init.getGenData().length);
        setNumLSEAgents(init.getLSEDataFixedDemand().length);
        setBaseS(init.getBaseS());
        setBaseV(init.getBaseV());
        setReserveRequirements(init.getReserveRequirements());

        setNodeData(init.getNodeData());
        setBranchData(init.getBranchData());
        setGenData(init.getGenData());
        setAlertGenMarkers(init.getAlertGenMarker());
        setExtraGenData(init.getExtraGenCoParams());
        setLSEData(init.getLSEDataFixedDemand());
        setLSEPriceSensitiveData(init.getLSEDataPriceSensitiveDemand());
        setLSEHybridData(init.getLSEDataHybridDemand());
    }

    private void setInitialLearningParameters() {
        coolingOfGen = new double[numGenAgents];
        experimentationOfGen = new double[numGenAgents];
        initPropensityOfGen = new double[numGenAgents];
        recencyOfGen = new double[numGenAgents];
        for (int i=0; i<numGenAgents; i++) {
            coolingOfGen[i] = genLearningParameters[i][1];
            experimentationOfGen[i] = genLearningParameters[i][3];
            initPropensityOfGen[i] = genLearningParameters[i][0];
            recencyOfGen[i] = genLearningParameters[i][2];
        }
        learningRandomSeedsOfGen = new int[numGenAgents];
    }

    private void setInitialActionDomainParameters() {
        numLowerRIs = M1;
        numUpperRIs = M2;
        numUpperCaps = M3;
        RIMaxL = RIMAX_L;
        RIMaxU = RIMAX_U;
        RIMinC = RIMIN_C;
        slopeStart = SLOPE_START;
    }

    @Override
    public void begin() {
        try{ //TODO-XX: Is this how the error handling should be done?
            buildModel();
            buildSchedule();
            Random.createUniform();  // Create random uniform\
        }catch(AMESMarketException bdff){
            System.err.println("Error building the model. " +
                    "The simulation cannot run!\n" + bdff.getMessage());
            stop();
        }
    }

    public void buildModel() throws AMESMarketException {
        LOGGER.log(Level.FINE, "Building the Model");

        Date sysDate = new Date();
        System.out.println("Simulation Start time: "+sysDate.toString()+"\n");
        System.out.println("Print the user-specified random seed: " + RANDOM_SEED + "\n");

        System.out.println("Print structural parameters\n");
        printStructuralParameters();


        setRngSeed(RANDOM_SEED);

        hour  = 0;
        day   = 1;
        isConverged = false;
        dayMax = DAY_MAX;

        transGrid = new TransGrid(nodeData, branchData, gridXSize,gridYSize, init);

        java.util.Random randomSeed = new java.util.Random((int)this.getRngSeed());
        addGenAgents(randomSeed);


        //only create the load scenario provider if one doesn't exist
        if(loadScenarioProvider == null){
            createLoadScenarios();
        }
        //Actualize a single LoadScenario, if one was not provided
        //in the LoadCase.
        if(!loadScenarioProvider.hasExternalActualLoadProfiles()){
            loadScenarioProvider.determineActualScenario(randomSeed);
        } else {
            System.out.println("LoadCase specified the actual load. Using it instead of choosing of a scenario for the simulated true load.");
        }

        addLSEAgents();
        try {
            iso = new ISO(this, init);
        } catch (IOException e) {
            throw new AMESMarketException(e);
        }

        String strTemp=String.format("%1$15.2f", dGenPriceCap);
        System.out.println("Supply-Offer Price Cap:  "+strTemp+" ($/MWh) \n");

        strTemp=String.format("%1$15.2f", dLSEPriceCap);
        System.out.println("Demand-Bid Price Floor:  "+strTemp+" ($/MWh) \n");

        System.out.println("Print the Simulation Controls: \n");
        //System.out.println("Print the derived learning random seed for each GenAgent:" + "\n");
        //printLearningRandomSeeds();

        System.out.println("Print the Stopping Rule:");
        if(bMaximumDay)
            System.out.println("\t   (1) Maximum Day Check. The user-specified maximum day: " + DAY_MAX );

        if(bThreshold)
            System.out.println("\t   (2) Threshold Probability Check. The user-specified threshold probability: " + getThresholdProbability());

        if(bActionProbabilityCheck)
            System.out.println("\t   (3) GenCo Action Probability Check. The user-specified start day:"+ iStartDay+"\t Consecutive day length:"+iCheckDayLength+"\t Probability difference:" + dActionProbability);

        if(bLearningCheck)
            System.out.println("\t   (4) GenCo Action Stability Check. The user-specified start day:"+ iLearningCheckStartDay+"\t Consecutive day length:"+iLearningCheckDayLength+"\t Difference:" + dLearningCheckDifference);

        if(bDailyNetEarningThreshold)
            System.out.println("\t   (5) Daily Net Earnings Threshold Check. The user-specified start day:"+ iDailyNetEarningStartDay+"\t Consecutive day length:"+iDailyNetEarningDayLength+"\t Threshold:" + dDailyNetEarningThreshold+"\n");

        //Bootstrap D1
        iso.computeCompetitiveEquilibriumResults();
    }

    private void printStructuralParameters() {
        System.out.println("Penalty Weight in DC-OPF Objective Function: " + nodeData[0][1]);
        System.out.println("\nGridData: ");
        int iNode=(int)nodeData[0][0];
        System.out.println("Number of Buses: " + iNode);
        System.out.println("Base Apparent Power: " + init.getBaseS());
        System.out.println("Base Voltage: " + init.getBaseV());

        System.out.println(String.format("Capacity Margin: %.0f%%",
                testcaseConfig.capacityMargin * 100));
        System.out.println("LoadCase Control File: " +
                (testcaseConfig.loadCaseControlFile == null ? "Not specified" : testcaseConfig.loadCaseControlFile));

        System.out.println("\nNumber of Branches: " + numBranches);
        System.out.println("BranchData: ");
        String strTemp=String.format("\t%1$15s\t%2$15s\t%3$15s\t%4$15s\t%5$15s",
                                     "ID",init.getParamNames()[1][0],init.getParamNames()[1][1],
                                     init.getParamNames()[1][2],init.getParamNames()[1][3]);
        System.out.println(strTemp);
        for(int i=0; i<branchData.length; i++) {
            strTemp=String.format("\t%1$15d\t%2$15d\t%3$15d\t%4$15.4f\t%5$15.4f",
                                  i+1, (int)branchData[i][0], (int)branchData[i][1], branchData[i][2], branchData[i][3]);
            System.out.println(strTemp);
        }

        System.out.println("Number of GenCo Agents: " + numGenAgents);
        System.out.println("GenCo Data: ");
        strTemp=String.format("\t%1$15s\t%2$15s\t%3$15s\t%4$15s\t%5$15s\t%6$15s\t%7$15s\t%8$15s",
                              init.getParamNames()[2][0],init.getParamNames()[2][1],
                              init.getParamNames()[2][2],init.getParamNames()[2][3],
                              init.getParamNames()[2][4],init.getParamNames()[2][5],
                              init.getParamNames()[2][6],init.getParamNames()[2][7]);
        System.out.println(strTemp);
        //Support.printArray(init.getParamNames()[2]);
        for(int i=0; i<genData.length; i++) {
            strTemp=String.format("\t%1$15d\t%2$15d\t%3$15.4f\t%4$15.4f\t%5$15.4f\t%6$15.4f\t%7$15.4f\t%8$15.4f",
                                  (int)genData[i][0], (int)genData[i][1], genData[i][2], genData[i][3],
                                  genData[i][4], genData[i][5], genData[i][6], genData[i][7]);
            System.out.println(strTemp);
        }

        {//New scope to limit the life of alertGenBuilder and hasAlertGen
            StringBuilder alertGenBuilder = new StringBuilder();
            boolean hasAlertGen = false;
            for(int i = 0; i < alertGenMarkers.length; i++){
                if(alertGenMarkers[i]){
                    hasAlertGen = true;
                    alertGenBuilder.append("\t");
                    alertGenBuilder.append((int)genData[i][0]);
                    alertGenBuilder.append("\n");
                }
            }

            System.out.println("\nAlert Generators:");
            if (hasAlertGen) {
                System.out.println(alertGenBuilder.toString());
            } else {
                System.out.println("\tNONE");
            }
        }//END AlertGen printing scope

        System.out.println("\nGenCos' Action Domain Parameters \n");
        printActionDomainParameters();

        System.out.println("GenCos' Learning Parameters \n");
        printLearningParameters();

        System.out.println("Number of LSE Agents: " + numLSEAgents);
        System.out.println("LSE Fixed Demand by Hour: ");
        strTemp=String.format("\t%1$15s\t%2$15s\t%3$15s\t%4$15s\t%5$15s\t%6$15s\t%7$15s\t%8$15s\t%9$15s\t%10$15s",
                              init.getParamNames()[4][0],init.getParamNames()[4][1],
                              init.getParamNames()[4][2],init.getParamNames()[4][3],
                              init.getParamNames()[4][4],init.getParamNames()[4][5],
                              init.getParamNames()[4][6],init.getParamNames()[4][7],
                              init.getParamNames()[4][8],init.getParamNames()[4][9]);
        System.out.println(strTemp);

        for(int i=0; i<lseData.length; i++) {
            strTemp=String.format("\t%1$15d\t%2$15d\t%3$15.4f\t%4$15.4f\t%5$15.4f\t%6$15.4f\t%7$15.4f\t%8$15.4f\t%9$15.4f\t%10$15.4f",
                                  (int)init.getLSESec1Data()[i][0], (int)init.getLSESec1Data()[i][1],
                                  init.getLSESec1Data()[i][2],init.getLSESec1Data()[i][3],
                                  init.getLSESec1Data()[i][4],init.getLSESec1Data()[i][5],
                                  init.getLSESec1Data()[i][6],init.getLSESec1Data()[i][7],
                                  init.getLSESec1Data()[i][8],init.getLSESec1Data()[i][9]);
            System.out.println(strTemp);
        }
        strTemp=String.format("\t%1$15s\t%2$15s\t%3$15s\t%4$15s\t%5$15s\t%6$15s\t%7$15s\t%8$15s\t%9$15s\t%10$15s",
                              init.getParamNames()[5][0],init.getParamNames()[5][1],
                              init.getParamNames()[5][2],init.getParamNames()[5][3],
                              init.getParamNames()[5][4],init.getParamNames()[5][5],
                              init.getParamNames()[5][6],init.getParamNames()[5][7],
                              init.getParamNames()[5][8],init.getParamNames()[5][9]);
        System.out.println(strTemp);

        for(int i=0; i<lseData.length; i++) {
            strTemp=String.format("\t%1$15d\t%2$15d\t%3$15.4f\t%4$15.4f\t%5$15.4f\t%6$15.4f\t%7$15.4f\t%8$15.4f\t%9$15.4f\t%10$15.4f",
                                  (int)init.getLSESec2Data()[i][0], (int)init.getLSESec2Data()[i][1],
                                  init.getLSESec2Data()[i][2],init.getLSESec2Data()[i][3],
                                  init.getLSESec2Data()[i][4],init.getLSESec2Data()[i][5],
                                  init.getLSESec2Data()[i][6],init.getLSESec2Data()[i][7],
                                  init.getLSESec2Data()[i][8],init.getLSESec2Data()[i][9]);
            System.out.println(strTemp);
        }
        strTemp=String.format("\t%1$15s\t%2$15s\t%3$15s\t%4$15s\t%5$15s\t%6$15s\t%7$15s\t%8$15s\t%9$15s\t%10$15s",
                              init.getParamNames()[6][0],init.getParamNames()[6][1],
                              init.getParamNames()[6][2],init.getParamNames()[6][3],
                              init.getParamNames()[6][4],init.getParamNames()[6][5],
                              init.getParamNames()[6][6],init.getParamNames()[6][7],
                              init.getParamNames()[6][8],init.getParamNames()[6][9]);
        System.out.println(strTemp);

        for(int i=0; i<lseData.length; i++) {
            strTemp=String.format("\t%1$15d\t%2$15d\t%3$15.4f\t%4$15.4f\t%5$15.4f\t%6$15.4f\t%7$15.4f\t%8$15.4f\t%9$15.4f\t%10$15.4f",
                                  (int)init.getLSESec3Data()[i][0], (int)init.getLSESec3Data()[i][1],
                                  init.getLSESec3Data()[i][2],init.getLSESec3Data()[i][3],
                                  init.getLSESec3Data()[i][4],init.getLSESec3Data()[i][5],
                                  init.getLSESec3Data()[i][6],init.getLSESec3Data()[i][7],
                                  init.getLSESec3Data()[i][8],init.getLSESec3Data()[i][9]);
            System.out.println(strTemp);
        }

        StringBuffer lseDataBuffer = new StringBuffer();
        lseDataBuffer.append("\nLSE Price-Sensitive Demand Function Parameters by Hour: \n");
        strTemp=String.format("\t%1$10s\t%2$10s\t%3$10s\t%4$15s\t%5$15s\t%6$15s\n",
                              "ID", "atBus", "hourIndex", "c", "d", "SLMax");
        lseDataBuffer.append(strTemp);

        for(int i=0; i<lseData.length; i++) {
            for(int h=0; h<24; h++) {
                strTemp="";

                for(int j=0; j<6; j++) {
                    if(j<3) {
                        int iTemp=(int)lsePriceSensitiveDemand[i][h][j];
                        strTemp=strTemp+"\t"+String.format("%1$10d", iTemp);
                    }
                    else {
                        double dTemp=lsePriceSensitiveDemand[i][h][j];
                        strTemp=strTemp+"\t"+String.format("%1$15.4f", dTemp);
                    }
                }

                lseDataBuffer.append(strTemp);
                lseDataBuffer.append('\n');
            }
        }
        System.out.println(lseDataBuffer.toString());

        String scucTypeDesc = "";
        switch(testcaseConfig.getSCUCType()) {
        case SCUC.SCUC_DETERM:
            scucTypeDesc = "Deterministic.";
        break;
        case SCUC.SCUC_STOC:
            scucTypeDesc = "Stochastic.";
        break;
        default:
            scucTypeDesc = "Unknown.  ERROR!!!";
        }
        System.out.println("SCUC: " + scucTypeDesc);
    }


    private void printActionDomainParameters() {
        StringBuffer adpInfo = new StringBuffer();
        String temp=String.format("\t%1$10s\t%2$10s\t%3$10s\t%4$10s\t%5$10s\t%6$10S\t%7$10s\t%8$10s",
                                  "ID","M1","M2","M3","RIMaxL","RIMaxU","RIMinC","SS");
        adpInfo.append(temp).append('\n');

        for(int i=0; i<numGenAgents; i++) {
            temp=String.format("\t%1$10d\t%2$10d\t%3$10d\t%4$10d\t%5$10.4f\t%6$10.4f\t%7$10.4f\t%8$10.4f",
                               (i+1),(int)genLearningParameters[i][4],
                               (int)genLearningParameters[i][5],
                               (int)genLearningParameters[i][6],
                               genLearningParameters[i][7],
                               genLearningParameters[i][8],
                               genLearningParameters[i][9],
                               genLearningParameters[i][10]);
            adpInfo.append(temp).append('\n');
        }

        System.out.println(adpInfo.toString());

    }
    private void printLearningParameters() {
        StringBuffer lpInfo = new StringBuffer();
        String temp=String.format("\t%1$10s\t%2$10s\t%3$10s\t%4$10s\t%5$10s",
                                  "ID","q(1)","T","r","e");
        lpInfo.append(temp).append('\n');
        for(int i=0; i<numGenAgents; i++) {
            temp=String.format("\t%1$10d\t%2$10.4f\t%3$10.4f\t%4$10.4f\t%5$10.4f",
                               (i+1),initPropensityOfGen[i],coolingOfGen[i], recencyOfGen[i],experimentationOfGen[i]);
            lpInfo.append(temp).append('\n');
        }
        System.out.println(lpInfo.toString());
    }


    private void printLearningRandomSeeds() {
        String temp=String.format("\t%1$10s\t%2$15s", "ID","randomSeed");
        System.out.println(temp);
        for(int i=0; i<numGenAgents; i++) {
            temp=String.format("\t%1$10d\t%2$15d", (i+1),learningRandomSeedsOfGen[i]);
            System.out.println(temp);
        }
        System.out.println();
    }


    private void addGenAgents(java.util.Random rn) {

        for(int i=0; i<numGenAgents; i++) {
            int randomNumber = rn.nextInt();
            learningRandomSeedsOfGen[i] = randomNumber;
            VREParameters learningParams = new VREParameters(genLearningParameters[i][1],
                    genLearningParameters[i][3], genLearningParameters[i][0], genLearningParameters[i][2], randomNumber);

            ad = new ActionDomain((int)genLearningParameters[i][4],
                                  (int)genLearningParameters[i][5],
                                  (int)genLearningParameters[i][6],
                                  genLearningParameters[i][7],
                                  genLearningParameters[i][8],
                                  genLearningParameters[i][9]);

            GenAgent gen = new GenAgent(genData[i], learningParams, ad.getActionDomain(),
                                        genLearningParameters[i][10], dGenPriceCap, randomNumber, iStartDay, iCheckDayLength, dActionProbability, bActionProbabilityCheck,
                                        iLearningCheckStartDay, iLearningCheckDayLength, dLearningCheckDifference, bLearningCheck, dDailyNetEarningThreshold,
                                        bDailyNetEarningThreshold, iDailyNetEarningStartDay, iDailyNetEarningDayLength, (int)genLearningParameters[i][11],
                                        alertGenMarkers[i],
                                        i/*assume the order we create the agents in is the index order.*/
                    );

            //update the name to one in the case files.
            gen.setID(testcaseConfig.genData[i].name);

            final String gcName = gen.getID();
            SCUCInputData sid = extraGenCoParams.get(gcName);
            if(sid != null) {
                gen.addExtraData(sid);
            } else {
                System.err.println("Could not find the extra GenCo data for " + gcName);
            }
            
            
            
            if(testcaseConfig.hasFuelType(gcName)) {
                gen.setFuelType(testcaseConfig.getFuelTypeForGen(gcName));
            }

            if(testcaseConfig.hasNoLoadCost(gcName)) {
                gen.setNoLoadCost(testcaseConfig.getNoLoadCostForGen(gen.getID()));
            }
            if(testcaseConfig.hasColdStartUpCost(gcName)) {
                gen.setColdStartUpCost(testcaseConfig.getColdStartUpCostForGen(gen.getID()));
            }
            if(testcaseConfig.hasHotStartUpCost(gcName)) {
                gen.setHotStartUpCost(testcaseConfig.getHotStartUpCostForGen(gen.getID()));
            }
            if(testcaseConfig.hasShutDownCost(gcName)) {
                gen.setShutDownCost(testcaseConfig.getShutDownCostForGen(gen.getID()));
            }

            transGrid.addGenAgentAtNodeK(gen,(i+1));
            genAgentList.add(gen);
        }

        rebuildGenAgentNameCache();
    }

    /**
     * Iterate over the list of genagents and build a map
     * which associates agent names with the object representing
     * the genagent.
     *
     * Should be called anytime a new agent is added to the list.
     *
     * The objects stored in the map are aliases to the objects
     * in {@link #genAgentList}.
     */
    private void rebuildGenAgentNameCache() {
        genAgentsByName.clear(); //dump the list, just in case.

        for(GenAgent ga : genAgentList) {
            genAgentsByName.put(ga.getID(), ga);
        }
    }

    private void addLSEAgents() throws BadDataFileFormatException {

        /*
         * Decide what demand source was specified.
         * If the data source was a TestCase, use the default test case data
         * If the data was LoadCase, use the loadCase ExpectedLoad and for the
         * hybridDemand Flag to 1.
         */


        for(int j=0; j<numLSEAgents; j++) {

            LSEAgent lse = new LSEAgent(
                    lseData[j], 
                    lsePriceSensitiveDemand[j], 
                    lseHybridDemand[j],
                    //FIXME: Where should LSE expected load really come from? 
                    //Currently it is the LoadScenarioProvider 'Expected' scenario.
                    loadScenarioProvider.getExpectedLoadProfiles()
                    );
            transGrid.addLSEAgentAtNodeK(lse,(j+1));
            lseAgentList.add(lse);
        }
    }

    /**
     * Create a new load scenario provider.
     *
     * Will replace the object currently being used (if it exists) as the load
     * provider.
     *
     * @throws BadDataFileFormatException
     * FIXME: Fail the initialization if an exception is thrown!
     */
    protected void createLoadScenarios() throws AMESMarketException {
        loadScenarioProvider = new DefaultLPCProvider();

        String loadCaseFile = testcaseConfig.loadCaseControlFile;
        
        if(loadCaseFile == null){
            //TODO: Is this the best place to check that a file was specified?
            throw new BadDataFileFormatException("No LoadCaseControl file specified.");//TODO-X : Configuration Exception!
        }

        File loadCaseControl = new File(loadCaseFile);

        loadScenarioProvider.initializeFromControlFile(loadCaseControl, testcaseConfig.getZoneNames());

        {//make sure the load case control object defines the correct
         //number of zones.
            LoadCaseControl lcc = ((DefaultLPCProvider) loadScenarioProvider)
                    .getLoadCaseControl();
            int expNumZones = testcaseConfig.getZoneNames().getNumZones();
            int actualNumZones = lcc.getNumZones();
            if (expNumZones != actualNumZones) {
                throw new AMESMarketException(
                        "The LoadCase control file declares the wrong number of zones"
                                + " Expected " + expNumZones + " but found "
                                + actualNumZones);
            }
        }

        loadScenarioProvider.scaleLoadProfileCollections(getMaxGenCapacity(), testcaseConfig.capacityMargin);
    }

    public void buildSchedule() {
        //System.out.println("\n\n\t*******************************************************************************");
        //System.out.println("\t********* Wholesale power market with learning traders is now running *********");
        //System.out.println("\t*******************************************************************************\n\n");

        class WPMarket extends BasicAction {

            @Override
            public final void execute() {

                stopCode=0;

                try {
                    announceStatusEvent(new StatusEvent(StatusEvent.UPDATE_DAY, day, AMESMarket.this));
                    announceStatusEvent(new StatusEvent(StatusEvent.UPDATE_HOUR, hour, AMESMarket.this));

                    iso.wholesalePowerMarketOperation(hour, day);

                    announceStatusEvent(new StatusEvent(StatusEvent.UPDATE_LMPS,
                        iso.getHourlyLMP(hour), AMESMarket.this));
                    announceStatusEvent(new StatusEvent(StatusEvent.UPDATE_BRANCH_FLOW,
                            iso.getHourlyRTBranchFlow(hour, true), AMESMarket.this
                            ));
                    announceStatusEvent(new StatusEvent(StatusEvent.UPDATE_COMMITMENTS,
                            iso.getHourlyRTCommitment(hour, true), AMESMarket.this
                            ));
                    announceStatusEvent(new StatusEvent(StatusEvent.UPDATE_HAS_SOLUTION,
                            iso.getHourlyHasSolution(hour), AMESMarket.this
                            ));
                    announceStatusEvent(new StatusEvent(StatusEvent.UPDATE_COSTS,
                            iso.getHourlyRTCosts(hour), AMESMarket.this
                            ));

                } catch (FileNotFoundException e) {
                    System.err.println("File not found: " + e.getMessage());
                    logException(e, "File Not Found");
                    stopCode |= STOP_CODE_ERROR;
                    stop();
                } catch (IOException e) {
                    System.err.println("An I/O exception during wholesale market operation\n" + e.getMessage());
                    logException(e, "I/O Exceptions");
                    stopCode |= STOP_CODE_ERROR;
                    stop();
                } catch (Exception e) {
                    System.err.println("An exception occuring during market operation " +
                                       "at hour " + hour + " on day " + day +
                                       "\nError Message: " + e.getMessage() +
                                       "\nStopping the market!!!!"
                                      );
                    logException(e, "Exception during market operation");
                    stopCode |= STOP_CODE_ERROR;
                    stop();
                }

                if(bMaximumDay) {
                    if(bThreshold) { // Both dayMax has been reached and all GenCos are selecting a single action with probability
                        if(isConverged == true) {// Only all GenCos are selecting a single action with probability
                            stop();
                            stopCode=stopCode|0x10; // second bit
                        }
                    }

                    if((hour==23)&&(day==dayMax)) { // Only dayMax has been reached
                        stop();
                        stopCode=stopCode|0x1;   // first bit
                    }
                }

                if(bActionProbabilityCheck) {
                    if(isGenActionProbabilityConverged) {
                        stop();
                        stopCode=stopCode|0x100;   // third bit
                    }
                }

                if(bLearningCheck) {
                    if(isGenLearningResultConverged) {
                        stop();
                        stopCode=stopCode|0x1000;   // fotrh bit;
                    }
                }

                if(bDailyNetEarningThreshold) {
                    if(isGenDailyNetEarningConverged) {
                        stop();
                        stopCode=stopCode|0x10000;   // fifth bit;
                    }
                }

                //TODO-XXX REFACTOR: Add a listener interface to notify when the calculation is finished
                // Can stop actively polling for calculation finished in the GUI that way.
                if(stopCode>0) {

                    bCalculationEnd=true;
                    iso.DayAheadMarketCheckLastDayAction();

                    Date sysDate = new Date();
                    System.out.println("Simulation End time: "+sysDate.toString()+"\n");

                    if (hasStopCodeError()) {
                        System.out.println("\n\nThe current simulation run concluded on day " + day +
                                           " in response to error during the simulation\n\n");
                    } else {

                        String stopStr="";
                        int iStopNumber=0;
                        int iFirstIndex=-1;
                        int iLastIndex=0;
                        int iTemp=stopCode;
                        for(int i=0; i<5; i++) {
                            if((stopCode&0x1)==0x1) {
                                if(iFirstIndex==-1)
                                    iFirstIndex=i;

                                iStopNumber++;
                                iLastIndex=i;
                            }

                            stopCode/=2;
                        }

                        stopCode=iTemp;
                        for(int i=0; i<5; i++) {
                            if((stopCode&0x1)==0x1) {

                                if(iFirstIndex==i)
                                    stopStr+=(i+1);
                                else if((iLastIndex==i)&&(iLastIndex!=iFirstIndex))
                                    stopStr+=", and "+(i+1);
                                else
                                    stopStr+=", "+(i+1);
                            }

                            stopCode/=2;
                        }


                        System.out.println("\n\nThe current simulation run concluded on day "+day+ " in response to the activation of the following \nstopping rule:("+stopStr+")\n\n");

                        System.out.println("Customizable table and chart output displays for the competitive (no learning) benchmark pre-run and the actual \nmarket simulation run can be accessed through the \"View\" screen on the menu bar.\n");
                    }
                }



                // Updating world time by one hour for each "tick count" in RePast
                hour++;
                if(hour==24) {
                    hour = 0;
                    day++;
                }

            }

            /**
             * Log the exception as SEVERE.
             * @param t
             * @param msg
             */
            private final void logException(Throwable t, String msg) {
                java.util.logging.Logger logger
                = java.util.logging.Logger.getLogger("amesmarket");
                logger.log(java.util.logging.Level.SEVERE, msg, t);

                t.printStackTrace();
            }
        }

        //isoRealTime=iso;
        // System.out.println(hour);
        schedule.scheduleActionBeginning(0,new WPMarket());
    }

    public void buildDisplay() {
    }

    @Override
    public Schedule getSchedule() {
        return schedule;
    }
    
   

    public ArrayList<GenAgent> getGenAgentList() {
        return genAgentList;
    }

    /**
     * Find a {@link GenAgent} by name.
     * @param name
     * @return the genagent named name or null, if the name is unknown.
     */
    public GenAgent getGenAgentByName(String name) {
        return genAgentsByName.get(name);
    }

    public ArrayList<LSEAgent> getLSEAgentList() {
        return lseAgentList;
    }

    public TransGrid getTransGrid() {
        return transGrid;
    }
    
    public ISO getISO() {
        return iso;
    }

    public int getHour() {
        return hour;
    }
    public int getDay() {
        return day;
    }

    /**
     * Get the object responsible for managing/
     * actualizing a scenario for the real-time market.
     * @return
     */
    public ILoadProfileCollectionProvider getLoadScenarioProvider(){
        return loadScenarioProvider;
    }

    /**
     * Set the {@link DefaultLPCProvider} to use.
     *
     * This provider will be used instead of the one created automatically by
     * the system from the LoadCase control file specified in the TestCase.
     *
     * @param lsp
     */
    public void setLoadScenarioProvider(ILoadProfileCollectionProvider lsp){
        this.loadScenarioProvider = lsp;
    }

    public void setIsConverged(boolean ic) {
        isConverged = ic;
    }

    public void setIsGenActionProbabilityConverged(boolean ic) {
        isGenActionProbabilityConverged = ic;
    }

    public void setIsGenLearningResultConverged(boolean ic) {
        isGenLearningResultConverged = ic;
    }

    public void setIsGenDailyNetEarningConverged(boolean ic) {
        isGenDailyNetEarningConverged = ic;
    }

    // Get and set methods for user-setting parameters

    public int getNumNodes() {
        return numNodes;
    }
    public void setNumNodes(int nn) {
        numNodes = nn;
    }

    public int getNumBranches() {
        return numBranches;
    }
    public void setNumBranches(int nb) {
        numBranches = nb;
    }

    public int getNumGenAgents() {
        return numGenAgents;
    }
    public void setNumGenAgents(int ngen) {
        numGenAgents = ngen;
    }

    public int getNumLSEAgents() {
        return numLSEAgents;
    }
    public void setNumLSEAgents(int nlse) {
        numLSEAgents = nlse;
    }

    public double[][] getNodeData() {
        return nodeData;
    }
    public void setNodeData(double[][] nd) {
        nodeData = nd;
    }

    public double[][] getBranchData() {
        return branchData;
    }
    
    public double getReserveRequirements() {
        return reserveRequirements;
    }    
    
    public void setReserveRequirements(double rR) {
        reserveRequirements = rR;
    }
    
    public void setBranchData(double[][] bd) {
        branchData = bd;
    }

    public double[][] getGenData() {
        return genData;
    }
    public void setGenData(double[][] gd) {
        genData = gd;
    }

    /**
     * Get the list of flags marking each generator
     * as an alert gen or not.
     * @return
     */
    public boolean[] getAlertGenMarkers() {
        return alertGenMarkers;
    }

    public void setAlertGenMarkers(boolean[] alertGenMarkers) {
        this.alertGenMarkers = alertGenMarkers;
    }

    public void setExtraGenData(Map<String, SCUCInputData> extraGenCoParams) {
        this.extraGenCoParams = extraGenCoParams;
    }

    public double[][] getLSEData() {
        return lseData;
    }
    public void setLSEData(double[][] ld) {
        lseData = ld;
    }

    public double[][][] getLSEPriceSensitiveData() {
        return lsePriceSensitiveDemand;
    }
    public void setLSEPriceSensitiveData(double[][][] ld) {
        lsePriceSensitiveDemand = ld;
    }

    public int[][] getLSEHybridData() {
        return lseHybridDemand;
    }
    public void setLSEHybridData(int[][] ld) {
        lseHybridDemand = ld;
    }

    public int getNumHoursPerDay() {
        return numHoursPerDay;
    }

    public int getNumIntervalsInSim() {
        return numIntervalsInSim;
    }

    public void setNumHoursPerDay(int nhpd) {
        numHoursPerDay  = nhpd;
    }

    public double getBaseS() {
        return baseS;
    }
    public void setBaseS(double bs) {
        baseS = bs;
    }

    public double getBaseV() {
        return baseV;
    }
    public void setBaseV(double bv) {
        baseV = bv;
    }

    public int getWorldXSize() {
        return gridXSize;
    }
    public void setWorldXSize(int wxs) {
        gridXSize = wxs;
    }

    public int getWorldYSize() {
        return gridYSize;
    }
    public void setWorldYSize(int wys) {
        gridYSize = wys;
    }

    public double getThresholdProbability() {
        return dThresholdProbability;
    }

    /**
     * Sum of all the generators' total generation, for non
     * 'canary' (reserve only) generators.
     * @return
     */
    public double getMaxGenCapacity(){
        double acc = 0;
        for(GenAgent genco : getGenAgentList()){
            if(!genco.isCanary()){
                acc += genco.getCapacityMax();
            }
        }
        return acc;
    }

    /**
     *
     * @param baseS
     * @param baseV
     * @param nodeData
     * @param branchDataData
     * @param genData
     * @param lseData
     * @param lsePriceData
     * @param lseHybridData
     * @param gencoAlertMarkers
     * @param extraGenCoParams 'new' genco params that don't easily fit in with the existing model.
     */
    public  void AMESMarketSetupFromGUI(double baseS, double baseV, double [][] nodeData, double [][] branchDataData, double [][] genData, double [][] lseData, double [][][] lsePriceData, int [][] lseHybridData, boolean[] gencoAlertMarkers,
            Map<String, SCUCInputData> extraGenCoParams, double reserveReq) {
        /*
         * Ran into a fundamental data inconsistency trying to add a boolean flag to
         * the generators at this stage.
         *
         * There are some very baked-in assumptions that a genco is represented by
         * a single array of doubles. Added a better type abstraction will solve
         * the problem, but will have deep repercussions on the rest of the program.
         * Notably, lots of places that set/read an array index. Semantically it
         * would be the same to have an object and just call methods. Practically,
         * its a little more complicated than that.
         *
         * The extraGenCoParams element side steps the above issue and gives us
         * a type-safe way to store any other data we need to plugin about the gencos.
         */
        InitDataFromGUI(baseS, baseV, nodeData, branchDataData, genData,
                lseData, lsePriceData, lseHybridData, gencoAlertMarkers,
                extraGenCoParams, reserveReq);

        ModelController=new Controller();
        this.setController(ModelController);
        ModelController.setModel(this);
        this.addSimEventListener(ModelController);

        ModelController.setConsoleOut(false);
        ModelController.setConsoleErr(false);
    }

    public void Setup() {
        ModelController.setup();
    }

    public void Start() {
        LOGGER.log(Level.FINE, "Start Sim");
        stopCode=-1;
        bCalculationEnd=false;

        ModelController.startSim();
    }

    public void Step() {
        LOGGER.log(Level.FINE, "Step Sim");
        ModelController.stepSim();
    }

    public void Stop() {
        LOGGER.log(Level.FINE, "Stop Sim");
        ModelController.stopSim();
    }

    public void Pause() {
        LOGGER.log(Level.FINE, "Pause Sim");
        ModelController.pauseSim();
    }

    public void Initialize() {
        LOGGER.log(Level.FINE, "Initialize Sim");
        ModelController.beginModel();
    }

    public void ViewSettings() {
        ModelController.showSettings();
    }


    public void InitDataFromGUI(double s, double v, double [][] nodeData, double [][] branchDataData, double [][] genData, double [][] lseData, double [][][] lsePriceData, int [][] lseHybridData, boolean[] gencoAlertMarkers,
            Map<String, SCUCInputData> extraGenCoParams, double rR) {
        init = new INIT();

        init.setBaseS(s);
        init.setBaseV(v);
        init.setNodeDataFromGUI(nodeData);
        init.setBranchDataFromGUI(branchDataData);
        init.setGenDataFromGUI(genData);
        init.setAlertGenIdsFromGUI(gencoAlertMarkers);
        init.setLSEDataFromGUI(lseData);
        init.setLSEPriceDataFromGUI(lsePriceData);
        init.setLSEHybridDataFromGUI(lseHybridData);
        init.setExtraGenCoParams(extraGenCoParams);
        init.setReserveRequirements(rR);
    }

    public void InitLearningParameters(double [][] learningData) {
        int iGen=learningData.length;
        genLearningParameters=new double[iGen][12];

        for(int i=0; i<iGen; i++) {
            for(int j=0; j<12; j++) {
                genLearningParameters[i][j]=learningData[i][j];
            }
        }
    }

    /**
     * Set the simulation parameters to use.
     * TODO: Refactor to just pass the config object in.
     * @param iMax
     * @param bMax
     * @param dThreshold
     * @param bThresh
     * @param dEarningThreshold
     * @param bEarningThresh
     * @param iEarningStart
     * @param iEarningLength
     * @param iStart
     * @param iLength
     * @param dCheck
     * @param bCheck
     * @param iLearnStart
     * @param iLearnLength
     * @param dLearnCheck
     * @param bLearnCheck
     * @param dGCap
     * @param dLseCap
     * @param lRandom
     * @param cfd casefile data. Handle the rest of the config. Makes it MUCH easier to plumb new data in.
     */
    public void InitSimulationParameters(int iMax, boolean bMax,
            double dThreshold, boolean bThresh, double dEarningThreshold,
            boolean bEarningThresh, int iEarningStart, int iEarningLength,
            int iStart, int iLength, double dCheck, boolean bCheck,
            int iLearnStart, int iLearnLength, double dLearnCheck,
            boolean bLearnCheck, double dGCap, double dLseCap, long lRandom,
            CaseFileData cfd) {
        DAY_MAX= iMax;
        bMaximumDay=bMax;
        dThresholdProbability=dThreshold;
        bThreshold=bThresh;
        dDailyNetEarningThreshold=dEarningThreshold;
        bDailyNetEarningThreshold=bEarningThresh;
        iDailyNetEarningStartDay=iEarningStart;
        iDailyNetEarningDayLength=iEarningLength;
        iStartDay=iStart;
        iCheckDayLength=iLength;
        dActionProbability=dCheck;
        bActionProbabilityCheck=bCheck;
        iLearningCheckStartDay=iLearnStart;
        iLearningCheckDayLength=iLearnLength;
        dLearningCheckDifference=dLearnCheck;
        bLearningCheck=bLearnCheck;
        dGenPriceCap=dGCap;
        dLSEPriceCap=dLseCap;

        RANDOM_SEED=(int)lRandom;

        testcaseConfig = cfd;
    }

    public void SetRandomSeed(long lSeed) {
        RANDOM_SEED=(int)lSeed;
    }

    /**
     * Get the scuc type to use.
     * Must not be called before {@link #InitSimulationParameters(int, boolean, double, boolean, double, boolean, int, int, int, int, double, boolean, int, int, double, boolean, double, double, long, CaseFileData)}.
     * @return the type of scuc to use.
     */
    public int getSCUCType() {
        return this.testcaseConfig.getSCUCType();
    }

    public void addHasSolutionByDay(int[] hasSolution) {
        int iRow=hasSolution.length;
        int [] newObject=new int[iRow];
        for(int i=0; i<iRow; i++)
            newObject[i]=hasSolution[i];

        hasSolutionByDay.add(newObject);
    }

    public ArrayList<int[]> getHasSolutionByDay() {
        return hasSolutionByDay;
    }

    public void addGenAgentSupplyOfferByDay(double[][] supplyOffer) {
        int iRow=supplyOffer.length;
        int iCol=supplyOffer[0].length;
        double [][] newObject=new double[iRow][iCol];
        for(int i=0; i<iRow; i++)
            for(int j=0; j<iCol; j++)
                newObject[i][j]=supplyOffer[i][j];

        genAgentSupplyOfferByDay.add(newObject);
    }

    public ArrayList<double[][]> getGenAgentSupplyOfferByDay() {
        return genAgentSupplyOfferByDay;
    }

    public void addLSEAgenPriceSensitiveDemandByDay(double[][] LSEPDemand) {
        int iRow=LSEPDemand.length;
        int iCol=LSEPDemand[0].length;
        for(int i=0; i<iRow; i++) {
            if(iCol>LSEPDemand[i].length)
                iCol=LSEPDemand[i].length;
        }

        double [][] newObject=new double[iRow][iCol];
        for(int i=0; i<iRow; i++)
            for(int j=0; j<iCol; j++) {
                newObject[i][j]=LSEPDemand[i][j];
            }

        lseAgentPriceSensitiveDemandByDay.add(newObject);
    }

    public ArrayList<double[][]> getLSEAgenPriceSensitiveDemandByDay() {
        return lseAgentPriceSensitiveDemandByDay;
    }


    public void addLSEAgentSurplusByDay(double[][] object) {
        int iRow=object.length;
        int iCol=object[0].length;
        double [][] newObject=new double[iRow][iCol];
        for(int i=0; i<iRow; i++)
            for(int j=0; j<iCol; j++)
                newObject[i][j]=object[i][j];

        lseAgentSurplusByDay.add(newObject);
    }

    public ArrayList<double[][]> getLSEAgentSurplusByDay() {
        return lseAgentSurplusByDay;
    }

    public void addGenAgentProfitAndNetGainByDay(double[][] object) {
        int iRow=object.length;
        int iCol=object[0].length;
        double [][] newObject=new double[iRow][iCol];
        for(int i=0; i<iRow; i++)
            for(int j=0; j<iCol; j++)
                newObject[i][j]=object[i][j];

        genAgentProfitAndNetGainByDay.add(newObject);
    }

    public ArrayList<double[][]> getGenAgentProfitAndNetGainByDay() {
        return genAgentProfitAndNetGainByDay;
    }

    public void addGenAgentActionPropensityAndProbilityByDay(double[][] object) {
        int iRow=object.length;
        int iCol=object[0].length;
        double [][] newObject=new double[iRow][iCol];
        for(int i=0; i<iRow; i++)
            for(int j=0; j<iCol; j++)
                newObject[i][j]=object[i][j];

        genAgentActionPropensityAndProbilityByDay.add(newObject);
    }

    public ArrayList<double[][]> getGenAgentActionPropensityAndProbilityByDay() {
        return genAgentActionPropensityAndProbilityByDay;
    }

    public void addLSEAgentPriceSensitiveDemandWithTrueCost(double[][] object) {
        int iRow=object.length;
        int iCol=object[0].length;
        double [][] newObject=new double[iRow][iCol];
        for(int i=0; i<iRow; i++)
            for(int j=0; j<iCol; j++)
                newObject[i][j]=object[i][j];

        lseAgentPriceSensitiveDemandWithTrueCost.add(newObject);
    }

    public ArrayList<double[][]> getLSEAgentPriceSensitiveDemandWithTrueCost() {
        return lseAgentPriceSensitiveDemandWithTrueCost;
    }

    public void addGenAgentCommitmentWithTrueCost(double[][] object) {
        int iRow=object.length;
        int iCol=object[0].length;
        double [][] newObject=new double[iRow][iCol];
        for(int i=0; i<iRow; i++)
            for(int j=0; j<iCol; j++)
                newObject[i][j]=object[i][j];

        genAgentCommitmentWithTrueCost.add(newObject);
    }

    public ArrayList<double[][]> getGenAgentCommitmentWithTrueCost() {
        return genAgentCommitmentWithTrueCost;
    }

    public void addGenAgentProfitAndNetGainWithTrueCost(double[][] object) {
        int iRow=object.length;
        int iCol=object[0].length;
        double [][] newObject=new double[iRow][iCol];
        for(int i=0; i<iRow; i++)
            for(int j=0; j<iCol; j++)
                newObject[i][j]=object[i][j];

        genAgentProfitAndNetGainWithTrueCost.add(newObject);
    }

    public ArrayList<double[][]> getGenAgentProfitAndNetGainWithTrueCost() {
        return genAgentProfitAndNetGainWithTrueCost;
    }

    public void addLSEAgentSurplusWithTrueCost(double[][] object) {
        int iRow=object.length;
        int iCol=object[0].length;
        double [][] newObject=new double[iRow][iCol];
        for(int i=0; i<iRow; i++)
            for(int j=0; j<iCol; j++)
                newObject[i][j]=object[i][j];

        lseAgentSurplusWithTrueCost.add(newObject);
    }

    public ArrayList<double[][]> getLSEAgentSurplusWithTrueCost() {
        return lseAgentSurplusWithTrueCost;
    }

    public void addGenAgentCommitmentByDay(double[][] object) {
        int iRow=object.length;
        int iCol=object[0].length;
        double [][] newObject=new double[iRow][iCol];
        for(int i=0; i<iRow; i++)
            for(int j=0; j<iCol; j++)
                newObject[i][j]=object[i][j];

        genAgentCommitmentByDay.add(newObject);
    }

    public ArrayList<double[][]> getGenAgentCommitmentByDay() {
        return genAgentCommitmentByDay;
    }

    public void addGenAgentRealTimeCommitmentByDay(double[][] object) {
        int iRow=object.length;
        int iCol=object[0].length;
        double [][] newObject=new double[iRow][iCol];
        for(int i=0; i<iRow; i++)
            for(int j=0; j<iCol; j++)
                newObject[i][j]=object[i][j];

        genAgentRealTimeCommitmentByDay.add(newObject);
    }

    public ArrayList<double[][]> getGenAgentRealTimeCommitmentByDay() {
        return genAgentRealTimeCommitmentByDay;
    }

    public void addBranchFlowByDay(double[][] object) {
        int iRow=object.length;
        int iCol=object[0].length;
        double [][] newObject=new double[iRow][iCol];
        for(int i=0; i<iRow; i++)
            for(int j=0; j<iCol; j++)
                newObject[i][j]=object[i][j];

        branchFlowByDay.add(newObject);
    }

    public ArrayList<double[][]> getBranchFlowByDay() {
        return branchFlowByDay;
    }

    public void addLMPByDay(double[][] object) {
        int iRow=object.length;
        int iCol=object[0].length;
        double [][] newObject=new double[iRow][iCol];
        for(int i=0; i<iRow; i++)
            for(int j=0; j<iCol; j++)
                newObject[i][j]=object[i][j];

        LMPByDay.add(newObject);
    }

    public ArrayList<double[][]> getLMPByDay() {
        return LMPByDay;
    }

    public void addActualProductionCostsByDay(double[][] costs) {
        if(costs == null) throw new IllegalArgumentException();
        double[][] costCopy = new double[costs.length][];
        for(int i = 0; i<costs.length; i++) {
            costCopy[i] = Arrays.copyOf(costs[i], costs[i].length);
        }

        productionCostsByDay.add(costCopy);
    }

    public ArrayList<double[][]> getActualProductionCostsByDay(){
        return productionCostsByDay;
    }

    public void addActualStartupCostsByDay(double[][] costs) {
        if(costs == null) throw new IllegalArgumentException();
        double[][] costCopy = new double[costs.length][];
        for(int i = 0; i<costs.length; i++) {
            costCopy[i] = Arrays.copyOf(costs[i], costs[i].length);
        }

        startupCostsByDay.add(costCopy);
    }

    public ArrayList<double[][]> getActualShutdownCostsByDay(){
        return shutdownCostsByDay;
    }

    public void addActualShutdownCostsByDay(double[][] costs) {
        if(costs == null) throw new IllegalArgumentException();
        double[][] costCopy = new double[costs.length][];
        for(int i = 0; i<costs.length; i++) {
            costCopy[i] = Arrays.copyOf(costs[i], costs[i].length);
        }

        shutdownCostsByDay.add(costCopy);
    }

    public ArrayList<double[][]> getActualStartupCostsByDay(){
        return startupCostsByDay;
    }

    public void addRealTimeLMPByDay(double[][] object) {
        int iRow=object.length;
        int iCol=object[0].length;
        double [][] newObject=new double[iRow][iCol];
        for(int i=0; i<iRow; i++)
            for(int j=0; j<iCol; j++)
                newObject[i][j]=object[i][j];

        realTimeLMPByDay.add(newObject);
    }

    public ArrayList<double[][]> getRealTimeLMPByDay() {
        return realTimeLMPByDay;
    }

    public void addRealTimeBranchFlowByDay(double[][] object) {
        int iRow=object.length;
        int iCol=object[0].length;
        double [][] newObject=new double[iRow][iCol];
        for(int i=0; i<iRow; i++)
            for(int j=0; j<iCol; j++)
                newObject[i][j]=object[i][j];

        realTimeBranchFlowByDay.add(newObject);
    }

    public ArrayList<double[][]> getRealTimeBranchFlowByDay() {
        return realTimeBranchFlowByDay;
    }

    public void addLMPWithTrueCost(double[][] object) {
        int iRow=object.length;
        int iCol=object[0].length;
        double [][] newObject=new double[iRow][iCol];
        for(int i=0; i<iRow; i++)
            for(int j=0; j<iCol; j++)
                newObject[i][j]=object[i][j];

        LMPWithTrueCost.add(newObject);
    }

    public ArrayList<double[][]> getLMPWithTrueCost() {
        return LMPWithTrueCost;
    }

    public void addGenActions(double[][] object) {
        int iRow=object.length;

        double [][] newObject=new double[iRow][ ];
        for(int i=0; i<iRow; i++) {
            int iCol=object[i].length;

            double[] newCol=new double[iCol];
            for(int j=0; j<iCol; j++)
                newCol[j]=object[i][j];

            newObject[i]=newCol;
        }

        lastDayGenActions.add(newObject);
    }

    public ArrayList<double[][]> getGenActions() {
        return lastDayGenActions;
    }

    public boolean isDeleteIntermediateFiles() {
        return deleteIntermediateFiles;
    }

    public boolean IfCalculationEnd() {
        return bCalculationEnd;
    }

    public int getStopCode() {
        return stopCode;
    }

    /**
     * Check if the parameter has the {@link #STOP_CODE_ERROR} bit set.
     *
     * @return
     */
    public boolean hasStopCodeError() {
        return (stopCode & STOP_CODE_ERROR) >> 24 == 1;
    }


    /////////////////////STATUS LISTENER CODE/////////////////////////
    private ArrayList<SimulationStatusListener> simStatListeners = new ArrayList<SimulationStatusListener>();
    public void addStatusListener(SimulationStatusListener l) {
        simStatListeners.add(l);
    }

    public void announceStatusEvent(StatusEvent se){
        for (SimulationStatusListener l : simStatListeners) {
            l.receiveStatusEvent(se);
        }
    }
    /////////////////////END STATUS LISTENER//////////////////////////

    /**
     * Constructor
     */
    public AMESMarket(boolean deleteIntermediateFiles) {
        genAgentSupplyOfferByDay=new ArrayList<double[][]>();
        lseAgentPriceSensitiveDemandByDay=new ArrayList<double[][]>();
        lseAgentPriceSensitiveDemandWithTrueCost=new ArrayList<double[][]>();
        lseAgentSurplusByDay=new ArrayList<double[][]>();
        lseAgentSurplusWithTrueCost=new ArrayList<double[][]>();
        genAgentProfitAndNetGainByDay=new ArrayList<double[][]>();
        genAgentActionPropensityAndProbilityByDay=new ArrayList<double[][]>();
        genAgentProfitAndNetGainWithTrueCost=new ArrayList<double[][]>();
        genAgentCommitmentWithTrueCost=new ArrayList<double[][]>();
        genAgentCommitmentByDay=new ArrayList<double[][]>();
        genAgentRealTimeCommitmentByDay=new ArrayList<double[][]>();
        branchFlowByDay=new ArrayList<double[][]>();
        LMPByDay=new ArrayList<double[][]>();
        productionCostsByDay = new ArrayList<double[][]>();
        startupCostsByDay = new ArrayList<double[][]>();
        shutdownCostsByDay = new ArrayList<double[][]>();
        realTimeLMPByDay=new ArrayList<double[][]>();
        realTimeBranchFlowByDay=new ArrayList<double[][]>();
        LMPWithTrueCost=new ArrayList<double[][]>();
        LMPWithTrueCost=new ArrayList<double[][]>();
        lastDayGenActions=new ArrayList<double[][]>();
        hasSolutionByDay=new ArrayList<int[]>();
        this.deleteIntermediateFiles = deleteIntermediateFiles;
    }


}




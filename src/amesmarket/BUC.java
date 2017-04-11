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

// BUC.java


package amesmarket;


/**
 * Bid-based Unit Commitment.
 *
 */
public class BUC implements SCED {

    // BUC's data

    /**numNodes*/
    private int K;
    /**numBranches*/
    private int N;
    /**numGenAgents*/
    private int I;
    /**numLSEAgents*/
    private int J;
    /**numBranches*/
    private int M;
    /**numHoursPerDay = # of load profiles for each LSE*/
    private int H;

    private boolean check;

    /**daily commitments (24 hours by row)*/
    private double[][] dailyDispatch,dailyRealTimeCommitment,dailyRealTimeBranchFlow;
    /**daily voltage angles (delta)*/
    private double[][] dailyVoltAngle;
    /**daily LMPs */
    private double[][] dailyLMP, dailyRealTimeLMP;
    private double[] dailyMinTVC;
    private double[][] dailyBranchFlow;
    private double[][] dailyPriceSensitiveDemand;

    private boolean [] bDCOPFHasSolution;

    private AMESMarket ames;
    private ISO iso;
    private DCOPFJ opf;

    private double[] ineqMultiplier;
    private String [] ineqMultiplierName;
    private int[][] genSchedule, genScheduleRT;

    // Index for supplyOfferByGen parameters, i.e., in the form of {A,B,CapMin,CapMax}
    private static final int A_INDEX    = 0;
    private static final int B_INDEX    = 1;
    private static final int CAP_MIN    = 2;
    private static final int CAP_MAX    = 3;
    private double[][] supplyOfferByGen;
    private double[] priceSensitiveDispatchRT;
    private double[][] supplyOfferByGenRT;
    // Index for psDemandBidByLSE parameters, i.e., in the form of {C,D,DemandMax}
    private static final int C_INDEX    = 0;
    private static final int D_INDEX    = 1;
    private static final int DEMAND_MAX    = 2;
    private double[][] psDemandBidByLSE;

    private double[][] dailySLoad = new double[H][J]; // in price-sensitive demand case only

    private final INIT init;

    // constructor
    public BUC(ISO independentSystemOperator, AMESMarket model, INIT init) {
        if(init == null) throw new IllegalArgumentException("Null INIT parameter");

        ames = model;
        iso = independentSystemOperator;
        this.init = init;

        K = ames.getNumNodes();
        N = ames.getNumBranches();
        I = ames.getNumGenAgents();
        J = ames.getNumLSEAgents();
        H = ames.NUM_HOURS_PER_DAY;  // H=24

        check=false;

        dailyDispatch = new double[H][I];
        dailyRealTimeCommitment = new double[H][I];
        dailyRealTimeBranchFlow = new double[H][N];
        dailyVoltAngle = new double[H][K];
        dailyLMP        = new double[H][K];
        dailyRealTimeLMP        = new double[H][K];
        dailyMinTVC = new double[H];
        dailyBranchFlow = new double[H][N];
        dailyPriceSensitiveDemand = new double [H][J];

        bDCOPFHasSolution = new boolean[24];
    }

    /**
     * Solve OPF DC approximation problem by invoking DCOPFJ.
     */
    public void solveOPF() {
        
        genSchedule=iso.getGenScheduleAsArray();
        
        double [] dLoad=new double[24]; // Total Demand
        for (int h=0; h<H; h++) {

        supplyOfferByGen = iso.getSupplyOfferByGen();

        // Store supplyOfferByGen to dSupplyOfferByGen for later check
        int iRow=supplyOfferByGen.length;
        int iCol=supplyOfferByGen[0].length;
        
        int[] atNodeByGen = new int[I];
        for(int i=0; i<I; i++) {
            GenAgent gen = ames.getGenAgentList().get(i);
            atNodeByGen[i] = gen.getAtNode();
        }

        int[] atNodeByLSE = new int[J];
        for(int j=0; j<J; j++) {
            LSEAgent lse = ames.getLSEAgentList().get(j);
            atNodeByLSE[j] = lse.getAtNode();
        }

        double [][] dSupplyOfferByGen=new double[iRow][iCol];
        for(int i=0; i<iRow; i++)
            for(int j=0; j<iCol; j++)
                dSupplyOfferByGen[i][j]=supplyOfferByGen[i][j];

        double dMinGenCapacity=0.0;
        double dMaxGenCapacity=0.0;
        
        for (int i=0;i<I;i++)
     {
         if (genSchedule[i][h]==0)
             dSupplyOfferByGen[i][CAP_MAX]=0.0;
     }

        // SI to PU conversion for supply offer and load profile

        for(int i=0; i<supplyOfferByGen.length; i++) {
            dMinGenCapacity+=supplyOfferByGen[i][CAP_MIN];
            dMaxGenCapacity+=supplyOfferByGen[i][CAP_MAX];
            //System.out.println("\n supplyOfferByGen-> dMaxGenCapacity "+supplyOfferByGen[i][CAP_MAX]+" for GenCo :"+i);

            /*// Convert A from SI to PU-adjusted
            supplyOfferByGen[i][A_INDEX] = supplyOfferByGen[i][A_INDEX]*INIT.getBaseS();

            // Convert B from SI to PU-adjusted
            supplyOfferByGen[i][B_INDEX] = supplyOfferByGen[i][B_INDEX]*INIT.getBaseS()*INIT.getBaseS();

            // Convert CapMin from SI to PU
            supplyOfferByGen[i][CAP_MIN] = supplyOfferByGen[i][CAP_MIN]/INIT.getBaseS();

            // Convert CapMax from SI to PU
            supplyOfferByGen[i][CAP_MAX] = supplyOfferByGen[i][CAP_MAX]/INIT.getBaseS();*/

        }

        



        //psDemandBidByLSE = Support.correctRoundingError(psDemandBidByLSE);

        
            //NOTE: phaseAngle is assumed to be zero at first bus, i.e. phaseAngle[0]=0

            double[] hourlyLoadProfileByLSE = new double[J];
            int[] hourlyLoadHybridFlagByLSE = new int[J];

            dLoad[h] = 0.0;
            for(int j=0; j<J; j++) {
                hourlyLoadProfileByLSE[j] = iso.getLoadProfileByLSE()[j][h];
                // Calculate total demand
                hourlyLoadHybridFlagByLSE[j] = iso.getDemandHybridByLSE()[j][h];

                if((hourlyLoadHybridFlagByLSE[j]&1)==1) {
                    dLoad[h] += hourlyLoadProfileByLSE[j];
                }

            }

            //for(int i=0;i<hourlyLoadProfileByLSE.length;i++)
              //      System.out.println("LSE Load: "+hourlyLoadProfileByLSE[i]+" MW"+"for hour: "+h);

            double [][][] priceSensitiveDemandBidByLSE = iso.getDemandBidByLSE();
            iRow=priceSensitiveDemandBidByLSE.length;
            iCol=priceSensitiveDemandBidByLSE[0][0].length;
            psDemandBidByLSE=new double[iRow][iCol];
            for(int i=0; i<iRow; i++)
                for(int j=0; j<iCol; j++)
                    psDemandBidByLSE[i][j]=priceSensitiveDemandBidByLSE[i][h][j];


            boolean bCheckMinMaxGenCapacityOK=true;
            if(dMinGenCapacity>dLoad[h]) {
                System.out.println("GenCo total reported lower required operating capacity is greater than total fixed demand at hour "+h+" \n");
                bCheckMinMaxGenCapacityOK=false;
            }

            if(dMaxGenCapacity<dLoad[h]) {
                System.out.println("GenCo total reported upper operating capacity under supply-offer price cap is less than total fixed demand at hour "+h+"\n");
                bCheckMinMaxGenCapacityOK=false;
            }



            if(bCheckMinMaxGenCapacityOK) {
                //System.out.println("Check DAM:"+supplyOfferByGen[0][3]);

                opf = new DCOPFJ(dSupplyOfferByGen, psDemandBidByLSE, hourlyLoadProfileByLSE, hourlyLoadHybridFlagByLSE,
                                 atNodeByGen, atNodeByLSE, ames.getTransGrid(),h,false, init);
                // System.out.println("Check DAM:"+supplyOfferByGen[0][3]);

                bDCOPFHasSolution[h] = opf.getIsSolutionFeasibleAndOptimal();
                dailyDispatch[h] = opf.getCommitment();
                /*for(int i=0;i<dailyCommitment[h].length;i++)
                            System.out.println("DAM Commit: "+h+" "+dailyCommitment[h][i]);*/

                dailyVoltAngle[h] = opf.getVoltAngle();
                dailyLMP[h]        = opf.getLMP();
                /*for(int i=0;i<dailyLMP[h].length;i++)
                            System.out.println("DAM Commit: "+h+" "+dailyLMP[h][i]);*/
                dailyMinTVC[h] = opf.getMinTVC();
                dailyBranchFlow[h] = opf.getBranchFlow();
                dailyPriceSensitiveDemand[h] = opf.getSLoad();
                dailyPriceSensitiveDemand[h]=Support.correctRoundingError(dailyPriceSensitiveDemand[h]);


                /*for(int i=0;i<dailyPriceSensitiveDemand[h].length;i++)
                            System.out.println("DAM PS: "+dailyPriceSensitiveDemand[h][i]+" MW"+" for hour: "+h);
                for(int i=0;i<dailyCommitment[h].length;i++)
                            System.out.println("DAM Commit: "+dailyCommitment[h][i]+" MW"+" for hour: "+h);
                for(int i=0;i<dailyLMP[h].length;i++)
                            System.out.println("DAM LMP: "+dailyLMP[h][i]+" $/MWh"+" for hour: "+h);
                for(int i=0;i<dailyBranchFlow[h].length;i++)
                            System.out.println("DAM Branch Flow: "+dailyBranchFlow[h][i]+" MW"+" for hour: "+h);
                for(int i=0;i<opf.getIneqMultiplier().length;i++)
                            System.out.println("DAM Ineq Multiplier: "+opf.getIneqMultiplier()[i]+" for hour: "+h+"Name"+opf.getIneqMultiplierName()[i]);
                          System.out.println(dailyMinTVC[h]);*/
                if(h==17) {// get inequality multiplier
                    ineqMultiplier=opf.getIneqMultiplier();
                    ineqMultiplierName=opf.getIneqMultiplierName();
                }
            }
            else {
                bDCOPFHasSolution[h]=false;
                double[] dispatch = new double[I];   // in MWs
                double[] voltAngle = new double[K-1];  // in radians
                double[] lmp        = new double[K];
                double[] branchFlow = new double[N]; //in MWs
                double[] psLoad = new double[J]; //in MWs

                dailyDispatch[h] = dispatch;


                dailyVoltAngle[h] = voltAngle;
                dailyLMP[h]        = lmp;
                dailyMinTVC[h] = 0.0;
                dailyBranchFlow[h] = branchFlow;
                dailyPriceSensitiveDemand[h] = psLoad;
            }
        }

        for (int h=0; h<H; h++) {
            if(!bDCOPFHasSolution[h])
                System.out.println("  At hour "+h+" DCOPF has no solution!");
        }

        System.gc();
    }

    //Real time OPF

    public void solveRTOPF(double [][] dc,double [] psd,int hour,int day) throws AMESMarketException{
        //FIXME: This is an alias that gets modified. Only place this field gets used. Could drop the field.
        
       supplyOfferByGenRT = iso.getSupplyOfferByGenRT();

      genScheduleRT = iso.getGenScheduleRTAsArray();
      
        int iRow=supplyOfferByGenRT.length;
        int iCol=supplyOfferByGenRT[0].length;

      double [][] dSupplyOfferByGen=new double[iRow][iCol];
        for(int i=0; i<iRow; i++)
        for(int j=0; j<iCol; j++)
            dSupplyOfferByGen[i][j]=supplyOfferByGenRT[i][j];

      String strTemp=null;

        double dMinGenCapacity=0.0;
     double dMaxGenCapacity=0.0;

     // SI to PU conversion for supply offer and load profile

     for (int i=0;i<I;i++)
     {
         if (genSchedule[i][hour]==0)
             dSupplyOfferByGen[i][CAP_MAX]=0.0;
     }

     for(int i=0; i<dSupplyOfferByGen.length; i++){
      dMinGenCapacity+=dSupplyOfferByGen[i][CAP_MIN];
      dMaxGenCapacity+=dSupplyOfferByGen[i][CAP_MAX];
      //System.out.println("\n supplyOfferByGen-> dMaxGenCapacity "+dSupplyOfferByGen[i][CAP_MAX]+" for GenCo :"+i);

         }

        int[] atNodeByGen = new int[I];
        for(int i=0; i<I; i++) {
            GenAgent gen = ames.getGenAgentList().get(i);
            atNodeByGen[i] = gen.getAtNode();
        }

        int[] atNodeByLSE = new int[J];
        for(int j=0; j<J; j++) {
            LSEAgent lse = (LSEAgent) ames.getLSEAgentList().get(j);
            atNodeByLSE[j] = lse.getAtNode();
        }

        double [][][] priceSensitiveDemandBidByLSE = iso.getDemandBidByLSE();
        iRow=priceSensitiveDemandBidByLSE.length;
        iCol=priceSensitiveDemandBidByLSE[0][0].length;
        psDemandBidByLSE=new double[iRow][iCol];
        for(int i=0; i<iRow; i++)
            for(int j=0; j<iCol; j++)
            {
                psDemandBidByLSE[i][j]=priceSensitiveDemandBidByLSE[i][hour][j];
                if(j==2)
                    psDemandBidByLSE[i][j]=0.0;
            }



        double dLoad; // Total Demand


        double[] hourlyLoadProfileByLSE = new double[J];
        int[] hourlyLoadHybridFlagByLSE = new int[J];

        dLoad = 0.0;

        iso.getActualLoadProfile(hour, day, hourlyLoadProfileByLSE);
        
        //for(int i=0;i<hourlyLoadProfileByLSE.length;i++)
                  //  System.out.println("LSE Load: "+hourlyLoadProfileByLSE[i]+" MW"+"for hour: "+hour);

        /*for(int j=0; j<J; j++) {
            //hourlyLoadProfileByLSE[j] = iso.getLoadProfileByLSE()[j][hour];
            hourlyLoadHybridFlagByLSE[j] = 1;
            hourlyLoadProfileByLSE[j]+=priceSensitiveDispatchRT[j];
            hourlyLoadProfileByLSE[j]=Support.correctRoundingError(hourlyLoadProfileByLSE[j]);
            dLoad+= hourlyLoadProfileByLSE[j];
        }*/

        boolean bCheckMinMaxGenCapacityOK=true;
        if(dMinGenCapacity>dLoad) {
            System.out.println("GenCo total reported lower required operating capacity is greater than total fixed demand at hour "+hour+" \n");
            bCheckMinMaxGenCapacityOK=false;
        }

        if(dMaxGenCapacity<dLoad) {
            System.out.println("GenCo total reported upper operating capacity under supply-offer price cap is less than total fixed demand at hour "+hour+"\n");
            bCheckMinMaxGenCapacityOK=false;
        }

        if(bCheckMinMaxGenCapacityOK) {

            if(hour ==10)
            {
                check=true;
            }
            else
                check=false;

            opf = new DCOPFJ(dSupplyOfferByGen, psDemandBidByLSE, hourlyLoadProfileByLSE, hourlyLoadHybridFlagByLSE,
                             atNodeByGen, atNodeByLSE, ames.getTransGrid(),0,check, init);

            boolean check = opf.getIsSolutionFeasibleAndOptimal();
            dailyRealTimeCommitment[hour]=opf.getCommitment();
            dailyRealTimeLMP[hour]=opf.getLMP();
            dailyRealTimeBranchFlow[hour]=opf.getBranchFlow();

            /*for(int i=0;i<dailyRealTimeCommitment[hour].length;i++)
                            System.out.println("RT Commitment: "+dailyRealTimeCommitment[hour][i]+" MW"+" for hour: "+hour);
                        for(int i=0;i<opf.getLMP().length;i++)
                            System.out.println("RT LMP: "+opf.getLMP()[i]+" $/MWh"+" for hour: "+hour);
                        for(int i=0;i<opf.getBranchFlow().length;i++)
                            System.out.println("RT Branch Flow: "+opf.getBranchFlow()[i]+" MW"+" for hour: "+hour);
                        for(int i=0;i<opf.getIneqMultiplier().length;i++)
                            System.out.println("RT Ineq Multiplier: "+opf.getIneqMultiplier()[i]+" for hour: "+hour+"Name"+opf.getIneqMultiplierName()[i]);
                              System.out.println(opf.getMinTVC());*/

            /*if(h==17) {// get inequality multiplier
                    ineqMultiplier=opf.getIneqMultiplier();
                    ineqMultiplierName=opf.getIneqMultiplierName();
                }*/
        }
        else {


        }

    }


    /**
     *  dailyCommitment: Hour-by-Node
     * @return
     */
    public double[][] getDailyBranchFlow() {
        return dailyBranchFlow;
    }

    /**
     * real time branch flow
     * @return
     */
    public double[][] getDailyRealTimeBranchFlow() {
        return dailyRealTimeBranchFlow;
    }

    /**
     * dailyCommitment: Hour-by-GenC
     */
    public double[][] getDailyCommitment() {
        return dailyDispatch;
    }

    /**
     * @return
     */
    public double[][] getDailyRealTimeCommitment() {
        return dailyRealTimeCommitment;
    }

    /**
     * dailyPriceSensitiveDemand: Hour-by-LSE
     * @return
     */
    public double[][] getDailyPriceSensitiveDemand() {
        return dailyPriceSensitiveDemand;
    }
    /**
     * dailyPhaseAngle: Hour-by-Node (excluding Node 1)
     */
    public double[][] getDailyVoltAngle() {
        return dailyVoltAngle;
    }
    /**
     *  dailyLMP: Hour-by-Node
     * @return
     */
    public double[][] getDailyLMP() {
        return dailyLMP;
    }

    public double[][] getDailyRealTimeLMP() {
        return dailyRealTimeLMP;
    }

    public int [] getHasSolution() {
        int [] hasSolution=new int[H];

        for(int i=0; i<H; i++) {
            if(bDCOPFHasSolution[i])
                hasSolution[i]=1;
        }

        return hasSolution;
    }
}

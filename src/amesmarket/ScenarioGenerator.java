/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package amesmarket;
import java.util.*;
import java.io.*;


/**
 *
 * @author David
 */
public class ScenarioGenerator {

    private AMESMarket ames;
    private ISO iso;
    private double[][] loadProfileByLSE;
    private int numLSEAgents, numNodes, numBranches,numHours;
    private double[][][] demandBidByLSE;
    private int   [][] demandHybridByLSE;

    private int randomSeed;
    private Random randomGenerator;

    public ScenarioGenerator(ISO independentSystemOperator, AMESMarket model) {
        ames = model;
        iso = independentSystemOperator;
        numLSEAgents=ames.getNumLSEAgents();
        numNodes = ames.getNumNodes();
        numBranches=ames.getNumBranches();
        numHours = ames.NUM_HOURS_PER_DAY;
        loadProfileByLSE = new double[numLSEAgents][numHours];
        randomSeed=ames.RANDOM_SEED;
        randomGenerator = new Random(randomSeed);

    }

    public void generateScenarios(int day) throws IOException {

        loadProfileByLSE = iso.getLoadProfileByLSE();

        double[][] loadProfileScenario=new double[numLSEAgents][numHours];

        for (int s=0; s<10; s++)
        {

            for (int i=0; i<numLSEAgents; i++)
                for (int j=0; j<numHours; j++)
                {
                    loadProfileScenario[i][j]=loadProfileByLSE[i][j];
                }



            for (int i=0; i<numLSEAgents; i++)
                for (int j=0; j<numHours; j++)
                {
                    double randUniform=(randomGenerator.nextDouble()-0.5)*10.0;
                    loadProfileScenario[i][j]=loadProfileScenario[i][j]*(1+randUniform*0.01);

                }

            File fileObj=new File("DATA/Scenarios/Scenario"+s+".dat");

            ensureParentDirExists(fileObj);

            FileWriter ScenFileWriter = new FileWriter(fileObj,true);
            BufferedWriter ScenBufferWriter = new BufferedWriter(ScenFileWriter);
            PrintWriter FilePrintWriter = new PrintWriter(ScenBufferWriter);

            if(day==1)
            {
                FilePrintWriter.format("%-12s\t%-12s\t%-12s\t%-12s\t%-12s\t%-12s\t%-12s\t%-12s\t%-12s\t%-12s\t%-12s\t%-12s\t%-12s\t%-12s\t%-12s\t","Day","Hour","Zone 1","Zone 2","Zone 3","Zone 4","Zone 5","Zone 6","Zone 7","Zone 8","Zone 9","Zone 10","Zone 11","Zone 12","Zone 13");
                FilePrintWriter.write("\n");
            }

            for(int i=0; i<numHours; i++)
            {
                FilePrintWriter.format("%-12d\t%-12d\t",day,(i+1));
                for(int j=0; j<numLSEAgents; j++)
                    FilePrintWriter.format("%-12.2f\t",loadProfileScenario[j][i]);
                FilePrintWriter.write("\n");
            }


            FilePrintWriter.close();

        }

    }

    private void ensureParentDirExists(File fileObj) throws IOException {
        File parent = fileObj.getParentFile();
        if (parent != null && !parent.exists()){
            boolean success = parent.mkdirs();
            if(!success){
                throw new IOException("Unable to create directory " + parent.getName());
            }
        }
    }
}

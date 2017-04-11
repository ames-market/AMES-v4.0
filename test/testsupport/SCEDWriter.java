package testsupport;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import amesmarket.AMESMarket;

public class SCEDWriter {

    public void writeSCEDResult(AMESMarket amesMarket, File dest) throws IOException{

        StringBuilder sb = new StringBuilder();

        //check the hasSolutions
        printHasSolutionByDay(amesMarket.getHasSolutionByDay(), sb);


        //check the genco commitments
        //TODO-XXX: is gen data correct?
        sb.append("#Commitments by day\n");
        toString(amesMarket.getGenAgentCommitmentByDay(), sb);
        sb.append("#End Commitments by day\n");
        sb.append("#RT Commitments by day\n");
        toString(amesMarket.getGenAgentRealTimeCommitmentByDay(), sb);
        sb.append("#End RT Commitments by day\n");


        sb.append("#LSE Price Sensitive Demand by day\n");
        toString(amesMarket.getLSEAgenPriceSensitiveDemandByDay(), sb);
        sb.append("#End LSE Price Sensitive Demand by day\n");

        //check the branch power flow
        sb.append("#BranchFlowByDay\n");
        toString(amesMarket.getBranchFlowByDay(), sb);
        sb.append("#End BranchFlowByDay\n");
        sb.append("#RealTimeBranchFlowByDay\n");
        toString(amesMarket.getRealTimeBranchFlowByDay(), sb);
        sb.append("#EndRealTimeBranchFlowByDay\n");

        //check the lmp's
        sb.append("#LMPByDay\n");
        toString(amesMarket.getLMPByDay(), sb);
        sb.append("#End LMPByDay\n");
        sb.append("#RealTimeLMPByDay\n");
        toString(amesMarket.getRealTimeLMPByDay(), sb);
        sb.append("#End RealTimeLMPByDay\n");


        //TODO-XXX: check the voltage angles
        //fail("No volt angle check");
//        assertSameList("Voltage Angles",
//                expected.get, TestConstants.DOUBLE_EQ);
        sb.append("#Voltage Angles\n");
        sb.append("What field do these come from?\n");
        sb.append("#End Voltage Angles\n");

        FileWriter fw = null;
        try {
            fw = new FileWriter(dest);
            fw.write(sb.toString());

        } finally {
            if(fw != null)
                fw.close();
        }
    }

    /**
     * @param valList [in]
     * @param sb [out]
     */
    void toString(ArrayList<double[][]> valList, StringBuilder sb) {
        if(valList == null){
            sb.append("null");
            return;
        }
        int size = valList.size();
        String strTemp;
        for(int i=0; i<size; i++) {
            double [][] vals=valList.get(i);

            for(int h=0; h<24; h++) {
                strTemp=String.format("%1$5d\t%2$5d", i+1, h);

                int jEnd = vals[h].length;
                for(int j=0; j<jEnd; j++) {
                    strTemp+=String.format("\t%1$15f", vals[h][j]);
                }

                strTemp+="\n";
                sb.append(strTemp);
            }
        }
    }

    void printHasSolutionByDay(List<int[]>hasSolutionByDay, StringBuilder sb) {
        if(hasSolutionByDay == null){
            sb.append("null");
            return;
        }

        String strTemp;
        sb.append("#HasSolutionDataStart\n");
        strTemp=String.format("//%1$5s\t%2$4s\t%3$4s\t%4$4s\t%5$4s\t%6$4s\t%7$4s\t%8$4s\t%9$4s\t%10$4s\t%11$4s\t%12$4s\t%13$4s\t%14$4s\t%15$4s\t%16$4s\t%17$4s\t%18$4s\t%19$4s\t%20$4s\t%21$4s\t%22$4s\t%23$4s\t%24$4s\t%25$4s\n",
                              "Day", "H-00", "H-01", "H-02", "H-03", "H-04", "H-05", "H-06", "H-07",
                              "H-08", "H-09", "H-10", "H-11", "H-12", "H-13", "H-14", "H-15",
                              "H-16", "H-17", "H-18", "H-19", "H-20", "H-21", "H-22", "H-23");
        sb.append(strTemp);

        int numSolsByDays = hasSolutionByDay.size();
        for (int i = 0; i < numSolsByDays; i++) {
            int[] hasSolution = hasSolutionByDay.get(i);

            strTemp = String.format("%1$7d\t", i + 1);
            for (int j = 0; j < 24; j++)
                strTemp += String.format("%1$4d\t", hasSolution[j]);

            strTemp += "\n";

            sb.append(strTemp);
        }

        sb.append("#HasSolutionDataEnd\n\n");
    }

}

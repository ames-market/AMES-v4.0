/*
 * FIXME <LICENCE>
 */
package amesmarket;

/**
 * Defines the methods for running a SCED computation
 * and getting the computed data back to the AMES simulation.
 *
 * @author Sean L. Mooney
 *
 */
public interface SCED {
    
    /**
     * Run the solver.
     */
    public void solveOPF() throws AMESMarketException;
    
    /**
     * 
     * @return
     */
    public double[][] getDailyCommitment();
    
    /**
     * 
     * @return
     */
    public double[][] getDailyLMP();
    
    /**
     * 
     * @return
     */
    public double[][] getDailyBranchFlow();
    
    /**
     * 
     * @return
     */
    public double[][] getDailyPriceSensitiveDemand();
    
    /**
     * 
     * @return
     */
    public int[] getHasSolution();
}

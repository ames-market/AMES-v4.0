//TODO: LICENCE
package amesmarket;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import amesmarket.filereaders.BadDataFileFormatException;
import amesmarket.filereaders.IZoneIndexProvider;
import amesmarket.filereaders.LoadCaseControlReader;
import amesmarket.probability.RouletteWheelSelector;


/**
 * Default LoadProfileCollection provider. Probably the only one that
 * will ever be used, other than for testing.
 * @author Sean L. Mooney, Dheepak Krishnamurthy
 *
 */
public class DefaultLPCProvider implements ILoadProfileCollectionProvider {

    private LoadCaseControl loadCaseControl;
    private int actualScenarioIdx = Integer.MIN_VALUE;

    /**
     * Field instead of local for traceablity. Get this value later
     * to log what scale factor was used.
     */
    private double scaleFactor = Double.NaN;

    /**
     * Track was value was used as the generation capacity.
     */
    private double peakLoad = Double.NaN;

    //TODO: DOCUMENTATION
    /* (non-Javadoc)
     * @see amesmarket.ILoadScenarioProvider#initializeFromControlFile(java.io.File)
     */
    @Override
    public void initializeFromControlFile(File controlFile, IZoneIndexProvider zoneNames) throws BadDataFileFormatException{
        LoadCaseControlReader lccr = new LoadCaseControlReader();
        System.out.println("Using master load case control file " + controlFile.getAbsolutePath());
        loadCaseControl = lccr.read(controlFile);
        loadCaseControl.setZoneNameIndexes(zoneNames);
    }

    /**
     * Set up the loadprovider with an 'empty' loadCaseControl file.
     *
     * Useful for unit testing.
     */
    protected void initializeEmpty(int numZones, int numLoadScenarios, int maxDay) {
        loadCaseControl = new LoadCaseControl(null, numZones, numLoadScenarios, maxDay, null,24);
    }

    /* (non-Javadoc)
     * @see amesmarket.ILoadScenarioProvider#putLoadScenario(int, double, amesmarket.LoadScenario)
     */
    @Override
    public void putLoadScenario(int sn, double scenProb, LoadProfileCollection scenario) {
        loadCaseControl.putLoadScenario(sn, scenProb, scenario);
    }

    /* (non-Javadoc)
     * @see amesmarket.ILoadScenarioProvider#putExpectedLoad(amesmarket.LoadScenario)
     */
    @Override
    public void putExpectedLoad(LoadProfileCollection expectedLoad) {
        loadCaseControl.putExpectedLoad(expectedLoad);
    }

    /* (non-Javadoc)
     * @see amesmarket.ILoadScenarioProvider#getActualScenario()
     */
    @Override
    public LoadProfileCollection getActualScenario() throws AMESMarketException{
        if(loadCaseControl.hasExternalActualLoadProfiles()){
            return loadCaseControl.getActualLoadProfiles();
        } else if(actualScenarioIdx == Integer.MIN_VALUE) {
            //TODO: Exception type. Generical AMES problem, not BadData...
            throw new AMESMarketException("A load scenario has not been actualized.");
        } else {
            return loadCaseControl.getLoadScenario(actualScenarioIdx);
        }
    }

    /* (non-Javadoc)
     * @see amesmarket.ILoadScenarioProvider#getExpectedScenario()
     */
    @Override
    public LoadProfileCollection getExpectedLoadProfiles() throws BadDataFileFormatException {
        return loadCaseControl.getExpectedLoadProfiles();
    }

    /* (non-Javadoc)
     * @see amesmarket.ILoadProfileCollectionProvider#hasExternalActualLoadProfiles()
     */
    @Override
    public boolean hasExternalActualLoadProfiles() {
        return loadCaseControl.hasExternalActualLoadProfiles();
    }

    //TODO: DOCS
    /* (non-Javadoc)
     * @see amesmarket.ILoadScenarioProvider#chooseActualScenario(int)
     */
    public  void determineActualScenario(int scenarioNumber) {
        if(loadCaseControl.isInvalidScenarioNumber(scenarioNumber))
            throw new IllegalArgumentException();

        System.out.println(String.format("Actualizing load scenario %d", scenarioNumber));
        actualScenarioIdx = scenarioNumber;
    }

    /**
     * Determine which of LoadScenarios will be used as the 'simulated true load'.
     * The supplied random number generate will be used to choose one of the
     * scenarios wrt the probability of choosing each scenario.
     *
     * @see amesmarket.ILoadProfileCollectionProvider#determineActualScenario(int)
     *
     * @param random
     * @throws unchecked runtime exception.
     */
    @Override
   public  void determineActualScenario(Random random) {
        //TODO-X use the 'provided' actual load scenario if specified.
        try {
            List<LoadProfileCollection> ls = loadCaseControl.getAllLoadScenarios();

            RouletteWheelSelector<LoadProfileCollection> rw =
                    new RouletteWheelSelector<LoadProfileCollection>(ls, random);

            int scenarioNumber = rw.selectElement().getScenarioNumber();

            System.out.println(String.format("Actualizing load scenario %d", scenarioNumber));
            actualScenarioIdx = scenarioNumber;
        } catch (BadDataFileFormatException e) {
            System.err.println("Failed to actualize a scenario");
            throw new RuntimeException(e);
        }
    }

    /* (non-Javadoc)
     * @see amesmarket.ILoadScenarioProvider#scaleLoadScenarios(double, double)
     */
    @Override
    public void scaleLoadProfileCollections(double maxGenCap, double capMargin) throws BadDataFileFormatException{
        if(capMargin <= -1.0)
            throw new IllegalArgumentException(
                    String.format(
                    "Invalid capacity margin %1.2f%%" + capMargin * 100)
                    );
        if(maxGenCap <= 0)
            throw new IllegalArgumentException("Generating capacity must be > 0");

        peakLoad = findPeakLoad();
        if(peakLoad < 0) {
            //TODO: format the peakLoad as a decimal.
            throw new BadDataFileFormatException("PeakLoad of " + peakLoad + " is less than 0.");
        }

        System.out.println(String.format(
                "\nGeneration Capacity: %.2f MW\nPeak load before scaling: %.2f MW. ",
                maxGenCap, peakLoad));

        scaleFactor = computeScaleFactor(maxGenCap, peakLoad, capMargin);
        
        //if(scaleFactor>1){
            scaleFactor = 1;
        //}
            
        
        String capMarginDesc = String.format("%1$4s%%", capMargin * 100);
        System.out.println("Capacity Margin: " + capMarginDesc);
        System.out.println("Apply scaling factor " + scaleFactor);

        scaleAllScenarios(scaleFactor);
        //Reset the peakLoad to the scaled peak.
        peakLoad = findPeakLoad();

        System.out.println(String.format(
                "Scaled Peak load %.2f MW.", peakLoad)
                );
    }

    /**
     * Compute the scaling factor to get the peak load below the generation capacity
     * by capMargin.
     *
     * Let s be the scale factor. Then from the definition of capacity margin,
     * {@link #computeCapMargin(double, double)},
     *
     * cm = (genCap - s*maxLoad)/(s*maxLoad).
     *
     * @param genCap
     * @param peakLoad
     * @param capMargin
     * @return
     */
    private double computeScaleFactor(double genCap, double peakLoad, double capMargin){
        return genCap / ( (capMargin + 1) * peakLoad );
    }

    private double findPeakLoad() throws BadDataFileFormatException{
        double peak = 0;
        double localPeak;

        //Find the peak from the scenarios
        Iterator<LoadProfileCollection> lsi = loadCaseControl.getAllLoadScenarios().iterator();
        while(lsi.hasNext()){
            localPeak = lsi.next().peakTotalLoad();
            if(localPeak > peak)
                peak = localPeak;
        }

        //Check if the expected load has a larger peak
        localPeak = loadCaseControl.getExpectedLoadProfiles().peakTotalLoad();
        if(localPeak > peak)
            peak = localPeak;

        //If an actual load profile collection was specified, check its peak.
        if (loadCaseControl.hasExternalActualLoadProfiles()) {
            localPeak = loadCaseControl.getActualLoadProfiles().peakTotalLoad();
            if (localPeak > peak) {
                peak = localPeak;
            }
        }

        return peak;
    }

    private void scaleAllScenarios(double scaleFactor) throws BadDataFileFormatException{
        Iterator<LoadProfileCollection> lsi = loadCaseControl.getAllLoadScenarios().iterator();
        while(lsi.hasNext())
            lsi.next().scaleScenario(scaleFactor);

        loadCaseControl.getExpectedLoadProfiles().scaleScenario(scaleFactor);

        if (loadCaseControl.hasExternalActualLoadProfiles())
            loadCaseControl.getActualLoadProfiles().scaleScenario(scaleFactor);
    }

    /*
     * (non-Javadoc)
     * @see amesmarket.ILoadScenarioProvider#getLoadCaseControl()
     */
    public LoadCaseControl getLoadCaseControl() {
        return loadCaseControl;
    }

    /**
     * @return the peakLoad
     */
    public double getPeakLoad() {
        return peakLoad;
    }
    
}

//FIXME: LICENCE
package amesmarket;

import java.io.File;
import java.util.Random;

import amesmarket.filereaders.BadDataFileFormatException;
import amesmarket.filereaders.IZoneIndexProvider;

/**
 * Defines an interface to provide load scenarios to AMES.
 *
 * Defined as an interface to make it possible to have different
 * types of load scenario providers. (e.g. one provides a 'different'
 * true load scenario than any of the scenarios there were actually
 * defined in the LoadCase control.)
 * @author Sean L. Mooney
 *
 */
public interface ILoadProfileCollectionProvider {

    /**
     * Initialize LoadCase provider with data from a control file.
     * @param controlFile
     * @throws BadDataFileFormatException
     */
    public abstract void initializeFromControlFile(File controlFile , IZoneIndexProvider zoneNames)
            throws BadDataFileFormatException;

    /**
     * @param sn
     * @param scenProb
     * @param scenario
     * @see amesmarket.LoadCaseControl#putLoadScenario(int, amesmarket.LoadProfileCollection)
     */
    public abstract void putLoadScenario(int sn, double scenProb,
            LoadProfileCollection scenario);

    /**
     * @param expectedLoad
     */
    public abstract void putExpectedLoad(LoadProfileCollection expectedLoad);

    /**
     * @return whether or not the LoadCase specified an specific LoadCase file.
     */
    public abstract boolean hasExternalActualLoadProfiles();

    /**
     * Get the LoadScenario that was marked as the actual scenario.
     * pre: {@link #determineActualScenario(Random)} has been called,
     *      {@link #initializeFromControlFile(File)} has been called.
     * @return the LoadScenario to use as the 'actual' LoadProfile data.
     * @throws BadDataFileFormatException
     */
    public abstract LoadProfileCollection getActualScenario()
            throws AMESMarketException;

    /**
     * Get the load scenario that is 'expected' as opposed to the
     * one that actually happens. LSE's use this information for forecasting load.
     * @return
     * @throws BadDataFileFormatException
     */
    public abstract LoadProfileCollection getExpectedLoadProfiles()
            throws BadDataFileFormatException;

    /**
     * Select one of the scenarios as the 'true' load.
     * @param random number generator to use.
     * @throws IllegalArgumentException if invalid scenario number.
     */
    public abstract void determineActualScenario(Random random);

    /**
     * Adjust the loads in the scenarios to be a certain margin below
     * the generation capacity.
     * @param maxGenCap
     * @param capMargin as decimal representation of the percentage. e.g. 0.15 not 15%. between -1 and 1.
     * @throws BadDataFileFormatException
     */
    public abstract void scaleLoadProfileCollections(double maxGenCap, double capMargin)
            throws BadDataFileFormatException;


    /**
     * @return The object representing the 'control' information for the load case.
     */
    public LoadCaseControl getLoadCaseControl();

}

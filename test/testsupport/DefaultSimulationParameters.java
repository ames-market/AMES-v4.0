/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package testsupport;

/**
 *
 * Default simulation parameters.
 *
 * A struct-type class to group the default parameters together to make it easy
 * to pass them around. Public visibility to make is simpler to hook into
 * existing code.
 *
 * TODO: Can remove the final modifier on parameters if needed.
 * TODO: Make this a "static final" type constant declaration class?
 *
 * Adapted from the {@link  AMESGUIFrame.AMESFrame}
 *
 * @author Sean L. Mooney
 */
public class DefaultSimulationParameters {

    /*
     * Initialize all of the static final variables.
     *
     * The only reason the values are not declared in-line with the definition of
     * is because of legacy issues. These constants were copied in from
     * AMESFrame, where the values where assigned in the class constructor. It's not
     * worth the time to put it back together.
     */
    static {

        Default_Cooling = 1000.0;
        Default_Experimentation = 0.96;
        Default_InitPropensity = 6000.0;
        Default_Recency = 0.04;

        Default_M1 = 10;
        Default_M2 = 10;
        Default_M3 = 1;
        Default_RI_MAX_Lower = 0.75;
        Default_RI_MAX_Upper = 0.75;
        Default_RI_MIN_C = 1.0;
        Default_SlopeStart = 0.001;
        Default_iRewardSelection = 1;


        Default_RandomSeed = 695672061;
        Default_iMaxDay = 50;
        Default_dThresholdProbability = 0.999;
        Default_dDailyNetEarningThreshold = 10.0;
        Default_dGenPriceCap = 1000.0;
        Default_dLSEPriceCap = 0.0;
        Default_iStartDay = 1;
        Default_iCheckDayLength = 5;
        Default_dActionProbability = 0.001;
        Default_iLearningCheckStartDay = 1;
        Default_iLearningCheckDayLength = 5;
        Default_dLearningCheckDifference = 0.001;
        Default_iDailyNetEarningStartDay = 1;
        Default_iDailyNetEarningDayLength = 5;
    }

    public static final long Default_RandomSeed;
    public static final int Default_iMaxDay;
    public static final double Default_dThresholdProbability;
    public static final double Default_dDailyNetEarningThreshold;
    public static final double Default_dGenPriceCap;
    public static final double Default_dLSEPriceCap;
    public static final int Default_iStartDay;
    public static final int Default_iCheckDayLength;
    public static final double Default_dActionProbability;
    public static final int Default_iLearningCheckStartDay;
    public static final int Default_iLearningCheckDayLength;
    public static final double Default_dLearningCheckDifference;
    public static final int Default_iDailyNetEarningStartDay;
    public static final int Default_iDailyNetEarningDayLength;
    public static final boolean Default_bMaximumDay = true;
    public static final boolean Default_bThreshold = true;
    public static final boolean Default_bDailyNetEarningThreshold = false;
    public static final boolean Default_bActionProbabilityCheck = false;
    public static final boolean Default_bLearningCheck = false;

    // Learning and action domain parameters
    public static final double Default_Cooling;
    public static final double Default_Experimentation;
    public static final double Default_InitPropensity;
    public static final int Default_M1;
    public static final int Default_M2;
    public static final int Default_M3;
    public static final double Default_RI_MAX_Lower;
    public static final double Default_RI_MAX_Upper;
    public static final double Default_RI_MIN_C;
    public static final double Default_Recency;
    public static final double Default_SlopeStart;
    public static final int Default_iRewardSelection;


}

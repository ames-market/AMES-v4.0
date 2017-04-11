package AMESGUIFrame.datacontrols;

import javax.swing.JFrame;

import AMESGUIFrame.AMESFrame;
import AMESGUIFrame.LearnOption1;
import AMESGUIFrame.PowerGridConfigure1;
import AMESGUIFrame.PowerGridConfigure2;
import AMESGUIFrame.PowerGridConfigure4;
import AMESGUIFrame.PowerGridConfigure5;
import amesmarket.CaseFileData;

/**
 * A builder to centralize choices about what type of panel we show, based on
 * the TestCase being constructed.
 *
 * Consider all of the collections of JFrames the AMES gui shows as the
 * 'TestCase Wizard'
 *
 * @author Sean L. Mooney
 *
 */
public class TestCaseWizardBuilder {

    //The frames we will build.
    PowerGridConfigure1 globalParams = null;
    PowerGridConfigure2 branchParams = null;
    PowerGridConfigure4 gencoParams = null;
    LSEDemandConfig lseParams = null;
    LearnOption1 learningParams = null;

    /**
     * Choose what type of an LSEDemandConfig panel we will show
     *
     * @param demandSource either {@link CaseFileData#LSE_DEMAND_LOAD_CASE} or
     *            {@link CaseFileData#LSE_DEMAND_TEST_CASE}.
     * @param amesframe
     */
    public void buildLSEConfig(int demandSource, AMESFrame amesframe) {
        switch (demandSource) {
        case CaseFileData.LSE_DEMAND_TEST_CASE:
            lseParams = new PowerGridConfigure5(amesframe);
            break;
        case CaseFileData.LSE_DEMAND_LOAD_CASE:
            lseParams = new LoadCaseLSEDemand(amesframe);
            break;
        default: //do nothing in the default case
            break;
        }
    }

    public void buildGlobalParameters(AMESFrame frame) {
        globalParams = new PowerGridConfigure1(frame);
    }

    public void buildGridBranchParameters(AMESFrame amesFrame) {
        branchParams = new PowerGridConfigure2(amesFrame);
    }

    public void buildGenCoParameters(AMESFrame amesFrame) {
        gencoParams = new PowerGridConfigure4(amesFrame);
    }

    public void buildLearningParameters(AMESFrame amesFrame, boolean bShow) {
        learningParams = new LearnOption1(amesFrame, bShow);
    }

    /**
     * @return the globalParams
     */
    public PowerGridConfigure1 getGlobalParams() {
        return globalParams;
    }

    /**
     * @return the branchParams
     */
    public PowerGridConfigure2 getBranchParams() {
        return branchParams;
    }

    /**
     * @return the gencoParams
     */
    public PowerGridConfigure4 getGencoParams() {
        return gencoParams;
    }

    /**
     * @return the lseParams
     */
    public LSEDemandConfig getLseParams() {
        return lseParams;
    }

    /**
     * @return the learningParams
     */
    public LearnOption1 getLearningParams() {
        return learningParams;
    }
}

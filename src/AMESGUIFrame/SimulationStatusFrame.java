/*
 * FIXME: LICENCE
 */
package AMESGUIFrame;

import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JFrame;

import amesmarket.SimulationStatusListener;

/**
 * A simple frame to display some of the status events.
 * Implements the {@link SimulationStatusListener}
 * interface and uses the events to update its display of the information.
 *
 * @author Sean L. Mooney
 *
 */
@SuppressWarnings("serial")
public class SimulationStatusFrame extends JFrame implements SimulationStatusListener {
    public SimulationStatusFrame() {
        super("Market Conditions");
        getContentPane().add(mip);

        Toolkit theKit = getToolkit();
        Dimension wndSize = theKit.getScreenSize();

        // Set the position to screen center & size to half screen size
        setBounds(wndSize.width / 6, wndSize.height / 6, wndSize.width * 2 / 3,
                wndSize.height * 2 / 3);
    }

    @Override
    public void receiveStatusEvent(StatusEvent evt) {
        switch (evt.eventType) {
        case StatusEvent.UPDATE_DAY:
            mip.setDay((Integer)evt.value);
            break;
        case StatusEvent.UPDATE_HOUR:
            mip.setHour((Integer)evt.value);
            break;
        case StatusEvent.UPDATE_GENCO_COUNT:
            mip.setNumGenCos((Integer)evt.value);
            break;
        case StatusEvent.UPDATE_LSE_COUNT:
            mip.setNumLSEs((Integer)evt.value);
            break;
        case StatusEvent.UPDATE_ZONE_COUNT:
            mip.setNumBuses((Integer)evt.value);
            break;
        case StatusEvent.UPDATE_LMPS:
            mip.setLMPs((double[])evt.value);
            break;
        case StatusEvent.UPDATE_BRANCH_FLOW:
            mip.setBranchFlow((double[])evt.value);
            break;
        case StatusEvent.UPDATE_HAS_SOLUTION:
            mip.setHasSolution((Integer)evt.value);
            break;
        case StatusEvent.UPDATE_COMMITMENTS:
            mip.setCommitments((double[])evt.value);
            break;
        case StatusEvent.UPDATE_LSE_DEMAND:
            mip.setLSEDemand((double[])evt.value);
        case StatusEvent.UPDATE_RT_LOAD:
            mip.setRTLoads((double[])evt.value);
            break;
        case StatusEvent.UPDATE_COSTS:
            mip.setRTCosts((double[][])evt.value);
            break;
        default:
            break;
        }
    }

    private MarketInformationPanel mip = new MarketInformationPanel();
}

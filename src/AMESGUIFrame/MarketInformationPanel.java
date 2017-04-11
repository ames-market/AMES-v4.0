/*
 * FIXME: LICENCE
 */
package AMESGUIFrame;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;


/**
 * A panel to display the current market conditions
 * @author Sean L. Mooney
 *
 */
@SuppressWarnings("serial")
public class MarketInformationPanel extends JPanel{

    private static final String LMP_PROPERTY = "lmp";
    private static final String BRANCH_FLOW_PROPERTY = "branch_flow";
    private static final String DISPATCH_PROPERTY = "dispatch";
    private static final String SOL_PROPERTY = "sol";
    private static final String LSE_DEMAND_PROPERTY = "lsedemand";
    private static final String RT_LOAD_PROPERTY = "rtload";
    private static final String GEN_CO_COSTS_PROPERTY = "gencocosts";

    MarketTimeDisplay mtd = new MarketTimeDisplay();
    MarketInformationPanel2 mp2 = new MarketInformationPanel2();

    int numBuses;
    int numGenCos;
    int numLSEs;
    int hour, day;

    public MarketInformationPanel() {
        super(new BorderLayout());


        add(mtd, BorderLayout.PAGE_START);
        add(mp2, BorderLayout.CENTER);

        addPropertyChangeListener(mtd);
        addPropertyChangeListener(mp2);
    }



    /**
     * @param displayH the displayH to set
     */
    public void setHour(int hour) {
        firePropertyChange(MarketTimeDisplay.HOUR_PROPERTY, -1, hour);
    }

    /**
     * @param displayD the displayD to set
     */
    public void setDay(int day) {
        firePropertyChange(MarketTimeDisplay.DAY_PROPERTY, -1, day);
    }

    /**
     * @param numBuses the numBuses to set
     */
    public void setNumBuses(int numBuses) {
        this.numBuses = numBuses;
        repaint();
    }

    /**
     * @param numGenCos the numGenCos to set
     */
    public void setNumGenCos(int numGenCos) {
        this.numGenCos = numGenCos;
        repaint();
    }

    /**
     * @param numLSEs the numLSEs to set
     */
    public void setNumLSEs(int numLSEs) {
        this.numLSEs = numLSEs;
        repaint();
    }

    public void setLMPs(double[] lmps){
        firePropertyChange(LMP_PROPERTY, null, lmps);
    }

    public void setBranchFlow(double[] flow){
        firePropertyChange(BRANCH_FLOW_PROPERTY, null, flow);
    }

    public void setCommitments(double[] commitments){
        firePropertyChange(DISPATCH_PROPERTY, null, commitments);
    }

    public void setHasSolution(int sol){
        firePropertyChange(SOL_PROPERTY, null, sol);
    }

    public void setLSEDemand(double[] demand) {
        firePropertyChange(LSE_DEMAND_PROPERTY, null, demand);
    }

    public void setRTLoads(double[] rtload) {
        firePropertyChange(RT_LOAD_PROPERTY, null, rtload);
    }

    public void setRTCosts(double[][] costs) {
        firePropertyChange(GEN_CO_COSTS_PROPERTY, null, costs);
    }

    //FIXME: NAME
    private class MarketInformationPanel2 extends JPanel implements PropertyChangeListener {

        JTextArea txtLMPs;
        JTextArea txtCommitments;
        JTextArea txtBranchFlow;
        JTextArea txtHasSolutions;
        JTextArea txtLSEDemand;
        JTextArea txtRTLoad;
        JTable tblCosts;
        DefaultTableModel costTblModel;

        public MarketInformationPanel2() {

            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.BOTH;
            c.ipady = 300;
            c.ipadx = 200;
            setLayout(new GridBagLayout());

            txtLMPs = new JTextArea();
            c.gridx = 0; c.gridy = 0;
            addPanel(txtLMPs, "LMPS", c);
            txtLMPs.setEditable(false);


            txtCommitments = new JTextArea();
            c.gridx = 1; c.gridy = 0;
            addPanel(txtCommitments, "Dispatches", c);
            txtCommitments.setEditable(false);

            txtBranchFlow = new JTextArea();
            c.gridx = 2; c.gridy = 0;
            addPanel(txtBranchFlow, "Branch Flow", c);
            txtBranchFlow.setEditable(false);

            txtHasSolutions = new JTextArea();
            c.gridx = 3; c.gridy = 0;
            addPanel(txtHasSolutions, "Has Solutions", c);
            txtHasSolutions.setEditable(false);

            txtLSEDemand = new JTextArea();
            c.gridx = 0; c.gridy = 1;
            addPanel(txtLSEDemand, "LSE Demand - Day Ahead", c);
            txtLSEDemand.setEditable(false);

            txtRTLoad = new JTextArea();
            c.gridx = 1; c.gridy = 1;
            addPanel(txtRTLoad, "Real Time Load", c);
            txtRTLoad.setEditable(false);


            tblCosts = new JTable();
            costTblModel = new DefaultTableModel(new String[]{"Name", "Startup", "Production", "Shutdown"}, 0);
            tblCosts.setModel(costTblModel);
            c.gridx = 2; c.gridy = 1;
            c.gridwidth = 2;
            addPanel(tblCosts, "Costs", c);
            tblCosts.setFillsViewportHeight(true);
        }

        /**
         * Common add txtBox to the panel function.
         * @param p
         * @param title
         */
        private void addPanel(Component p, String title, GridBagConstraints c) {
            JScrollPane sp = new JScrollPane(p);
            sp.setPreferredSize(new Dimension(400, 400));
            sp.setBorder(new TitledBorder(title));
            this.add(sp, c);
        }


        private void showStatus(String lbl, double[] vs, JTextArea dest) {
            synchronized (dest) {
                try {
                    Document doc = dest.getDocument();
                    doc.remove(0, doc.getLength());
                } catch (BadLocationException e) { //TODO-XX: Error Handling.
                    final String clsName = e.getClass().getName();
                    System.err.println(clsName + ": " + e.getMessage());
                }

                StringBuilder sb = new StringBuilder();
                if (vs != null) {
                    for (int i = 0; i < vs.length; i++) {
                        String l = String.format("%s %d: %3$15.2f\n", lbl, (i+1), vs[i]);
                        sb.append(l);
                    }
                    dest.append(sb.toString());
                }
            }
        }

        /*
         * setXXX have separate methods to separate property
         * change handling from the actual method invoked when the
         * property changes.
         */

        public void setLMPs(double[] lmps) {
            showStatus("Zone ", lmps, txtLMPs);
        }

        public void setBranchFlow(double[] flow) {
            showStatus("Zone ", flow, txtBranchFlow);
        }

        public void setCommitments(double[] commitments) {
            showStatus("GenCo ", commitments, txtCommitments);

            double total = 0;
            for(int i = 0; i<commitments.length; i++){
                total += commitments[i];
            }
            txtCommitments.append(String.format(
                    "\nTotal Dispatch: %1$15.2f MW",total
                    ));
        }

        public void setLSEDemand(double[] demand) {
            showStatus("LSE@Zone", demand, txtLSEDemand);

            double total = 0;
            for(int i = 0; i<demand.length; i++){
                total += demand[i];
            }
            txtLSEDemand.append(String.format(
                    "\nTotal Demand: %1$15.2f MW",total
                    ));
        }

        public void setRealTimeLoad(double[] rtl) {
            showStatus("LSE@Zone", rtl, txtRTLoad);

            double total = 0;
            for(int i = 0; i<rtl.length; i++){
                total += rtl[i];
            }
            txtRTLoad.append(String.format(
                    "\nTotal Demand: %1$15.2f MW",total
                    ));
        }

        public void setCosts(double[][] costs) {
            DefaultTableModel m = costTblModel; //shorter name for local reference.

            int rowCount = 0;
            for(int i = 0; i<costs.length; i++) {
                String gcName = "GenCo" + (i+1);
                rowCount = m.getRowCount();
                if( i >= rowCount) { //add a row if we need one
                    Object[] newRow = new Object[costs[i].length + 1];
                    newRow[0] = gcName;
                    for(int c = 0; c < costs[i].length; c++)
                        newRow[c+1] = costs[i][c];
                    m.addRow(newRow);
                } else{ //replace if the right size
                    m.setValueAt(gcName, i, 0);
                    for(int col = 0; col < costs[i].length; col++){
                        m.setValueAt(costs[i][col], i, col+1);
                    }
                }
            }

            //check to see if we need to trim off old rows.
            //first we update in case new rows were added
            rowCount = m.getRowCount();
            if( costs.length > rowCount) {
                for(int r = costs.length; r<rowCount; r++) {
                    m.removeRow(r);
                }
            }
        }

        /**
         * Use a slightly different form of the showStatus method. Has solution
         * only shows a Yes/No for each hour, instead of a list of things.
         * @param hasSol
         */
        public void setHasSolution(int hasSol) {
            JTextArea dest = txtHasSolutions; //local alias.
            synchronized (dest) {
                try {
                    Document doc = dest.getDocument();
                    doc.remove(0, doc.getLength());
                } catch (BadLocationException e) { //TODO-XX: Error Handling.
                    final String clsName = e.getClass().getName();
                    System.err.println(clsName + ": " + e.getMessage());
                }

                dest.append(String.format(
                        "Has Solution: %s", (hasSol == 1 ? "Yes" : "No")
                        ));
            }
        }

        @Override
        /**
         * Handle a property change. Decide what the change event
         * was and dispatch to correct method to deal with the
         * new value of the property.
         */
        public void propertyChange(PropertyChangeEvent evt) {
            String pName = evt.getPropertyName();
            Object val = evt.getNewValue();
            if (LMP_PROPERTY.equals(pName)) {
                if (val instanceof double[]) {
                    setLMPs((double[]) val);
                }
            } else if (DISPATCH_PROPERTY.equals(pName)) {
                if (val instanceof double[]) {
                    setCommitments((double[]) val);
                }
            } else if (BRANCH_FLOW_PROPERTY.equals(pName)) {
                if (val instanceof double[]) {
                    setBranchFlow((double[]) val);
                }
            } else if (SOL_PROPERTY.equals(pName)) {
                if (val instanceof Integer) {
                    setHasSolution(((Integer) val).intValue());
                }
            } else if (LSE_DEMAND_PROPERTY.equals(pName)) {
                if (val instanceof double[]) {
                    setLSEDemand((double[]) val);
                }
            } else if (RT_LOAD_PROPERTY.equals(pName)) {
                if (val instanceof double[]) {
                    setRealTimeLoad((double[]) val);
                }
            } else if (GEN_CO_COSTS_PROPERTY.equals(pName)) {
                if (val instanceof double[][]) {
                    setCosts((double[][]) val);
                }
            }
        }
    }

    /**
     * A simple panel to display the current day/hour of the market.
     *
     * @author Sean L. Mooney
     *
     */
    @SuppressWarnings("serial")
    public static class MarketTimeDisplay extends JPanel implements
            PropertyChangeListener {
        public static final String HOUR_PROPERTY = "hour";
        public static final String DAY_PROPERTY = "day";

        private JLabel displayH = new JLabel("-"), displayD = new JLabel("-");

        public MarketTimeDisplay() {
            super(new FlowLayout());
            add(new JLabel("Day:"));
            add(displayD);
            add(new JLabel("Hour:"));
            add(displayH);
        }

        public String getHour() {
            return this.displayH.getText();
        }

        public void setHour(String h) {
            this.displayH.setText(h);
        }

        public String getDay() {
            return this.displayD.getText();
        }

        public void setDay(String d) {
            this.displayD.setText(d);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            String pName = evt.getPropertyName();
            //use String.valueOf in case the new value is new.
            if (HOUR_PROPERTY.equals(pName)) {
                setHour(String.valueOf(evt.getNewValue()));
            } else if (DAY_PROPERTY.equals(pName)) {
                setDay(String.valueOf(evt.getNewValue()));
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            final MarketInformationPanel mip = new MarketInformationPanel();

            @Override
            public void run() {
                JFrame frame = new JFrame("Market Conditions");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.getContentPane().add(mip);

                populateCostData();

                Toolkit theKit = frame.getToolkit();
                Dimension wndSize = theKit.getScreenSize();

                // Set the position to screen center & size to half screen size
                frame.setBounds(wndSize.width/6, wndSize.height/6,
                        wndSize.width*2/3, wndSize.height*2/3);

                frame.setVisible(true);
            }

            void populateCostData() {
                //dummy data to make the panel long.
                double[][] data = new double[100][4];
                mip.setRTCosts(data);
            }
        });
    }
}

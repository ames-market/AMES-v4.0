/*
 * FIXME: LICENCE
 */
package AMESGUIFrame.datacontrols;

import java.awt.event.ActionEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import AMESGUIFrame.AMESFrame;
import amesmarket.AMESMain;
import amesmarket.CaseFileData;

/**
 * A gui control for the LSEDemand data when the
 * test case parameter is set to {@link CaseFileData#LSE_DEMAND_LOAD_CASE}.
 *
 * Ignores any data for demand source, fixed demand and price sensitive demand.
 * @author Sean L. Mooney
 *
 */
@SuppressWarnings("serial")
public class LoadCaseLSEDemand extends LSEDemandConfig{

    private final AMESFrame mainFrame;

    public LoadCaseLSEDemand(AMESFrame mainFrame) {
        this.mainFrame = mainFrame;
        initComponents();
    }

    private void initComponents() {
        JPanel jPanel1 = new JPanel();
        JButton Prev = new javax.swing.JButton();
        JButton NextButton = new javax.swing.JButton();
        JButton CancelButton = new javax.swing.JButton();
        JLabel jLabel1 = new JLabel();

        jPanel1.setBorder(BorderFactory.createEtchedBorder());
        jPanel1.add(jLabel1);

        jLabel1.setFont(new java.awt.Font("Arial", 1, 12));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("No configurable options.\n" +
              "LSE demand source set to LoadCase in the configuration file.\n");
        jLabel1.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));


        Prev.setFont(new java.awt.Font("Arial", 0, 12));
        Prev.setText("<< Prev");
        Prev.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        Prev.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                PrevActionPerformed(evt);
            }
        });


        NextButton.setFont(new java.awt.Font("Arial", 0, 12));
        NextButton.setText("Next >>");
        NextButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        NextButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                NextButtonActionPerformed(evt);
            }
        });

        CancelButton.setFont(new java.awt.Font("Arial", 0, 12));
        CancelButton.setText("Cancel");
        CancelButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        CancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CancelButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                      .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                      .addComponent(CancelButton, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                      .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                      .addComponent(Prev, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE)
                      .addGap(15, 15, 15)
                      .addComponent(NextButton, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)
                      .addGap(38, 38, 38))
            .addGroup(layout.createSequentialGroup()
                      .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                      .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                      .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                      .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                      .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(NextButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(Prev)
                                .addComponent(CancelButton))
                      .addContainerGap())
        );

        pack();
    }

    protected void CancelButtonActionPerformed(ActionEvent evt) {
        setVisible(false);
    }

    protected void NextButtonActionPerformed(ActionEvent evt) {
        this.setVisible(false);
        mainFrame.activeSimulationControl();
    }

    protected void PrevActionPerformed(ActionEvent evt) {
        this.setVisible(false);
        mainFrame.activeLearnOption1();
    }

    @Override
    public void loadBlankData() {}

    @Override
    public void loadData(Object[][] lseFixedData,
            Object[][][] lsePriceSensitiveData, Object[][] lseHybridData) {
        //nothing to be done
    }

    @Override
    public void addRowsBlankData(int iRow) {
        //nothing to be done.
    }

    @Override
    public void saveTableDataToList() {
        // TODO Auto-generated method stub

    }

    @Override
    public void saveData() {
        //nothing to be done
    }

    @Override
    public String DataVerify() {
        return "";
    }

    @Override
    public String PriceCapVerify(double dLSEPriceCap) {
        return "";
    }
}

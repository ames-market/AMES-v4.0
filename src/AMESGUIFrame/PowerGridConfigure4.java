/* ============================================================================
 * AMES Wholesale Power Market Test Bed (Java): A Free Open-Source Test-Bed
 *         for the Agent-based Modeling of Electricity Systems
 * ============================================================================
 *
 * (C) Copyright 2008, by Hongyan Li, Junjie Sun, and Leigh Tesfatsion
 *
 *    Homepage: http://www.econ.iastate.edu/tesfatsi/AMESMarketHome.htm
 *
 * LICENSING TERMS
 * The AMES Market Package is licensed by the copyright holders (Junjie Sun,
 * Hongyan Li, and Leigh Tesfatsion) as free open-source software under the
 * terms of the GNU General Public License (GPL). Anyone who is interested is
 * allowed to view, modify, and/or improve upon the code used to produce this
 * package, but any software generated using all or part of this code must be
 * released as free open-source software in turn. The GNU GPL can be viewed in
 * its entirety as in the following site: http://www.gnu.org/licenses/gpl.html
 */

/*
 * PowerGridConfigure4.java
 *
 * Created on June 5, 2007, 9:56 PM
 */

package AMESGUIFrame;


import java.awt.Component;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import amesmarket.Support;

public class PowerGridConfigure4 extends javax.swing.JFrame {

	/** Creates new form PowerGridConfigure4
	 * @param frame
	 */
	public PowerGridConfigure4(AMESFrame frame) {
		this.mainFrame = frame;
		this.initComponents();

		this.addRowItem = this.popupMenu.add("Add A Row");
		this.addRowItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				PowerGridConfigure4.this.addRowItemActionPerformed(evt);
			}
		});

		this.popupMenu.addSeparator();

		this.copyRowItem = this.popupMenu.add("Copy A Row");
		this.copyRowItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				PowerGridConfigure4.this.copyRowItemActionPerformed(evt);
			}
		});

		this.popupMenu.addSeparator();

		this.pasteRowItem = this.popupMenu.add("Paste A Row");
		this.pasteRowItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				PowerGridConfigure4.this.pasteRowItemActionPerformed(evt);
			}
		});

		this.popupMenu.addSeparator();

		this.deleteRowItem = this.popupMenu.add("Delete A Row");
		this.deleteRowItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				PowerGridConfigure4.this.deleteRowItemActionPerformed(evt);
			}
		});

		this.setTitle("Step 3: Input GenCo parameters");

		DefaultTableModel dataModel = new DefaultTableModel(this.data,  this.names);
		// Create the table
		this.jTable1 = new JTable(dataModel);

		TableColumn column = null;
		this.jTable1.setAutoscrolls(true);
		this.jScrollPane1.setViewportView(this.jTable1);

		DefaultTableCellRenderer   renderer   =   new   DefaultTableCellRenderer();
		renderer.setHorizontalAlignment(JLabel.CENTER);

		this.jTable1.setDefaultRenderer(Object.class,   renderer);
		this.jTable1.setToolTipText("GenCo Parameters Table");

		this.iconHeaderRender = new DefaultTableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value,
					boolean isSelected, boolean hasFocus, int row, int column) {
				// Inherit the colors and font from the header component
				if (table != null) {
					JTableHeader header = table.getTableHeader();
					if (header != null) {
						this.setForeground(header.getForeground());
						this.setBackground(header.getBackground());
						this.setFont(header.getFont());
					}
				}

				if (value instanceof ImageIcon) {
					this.setIcon((ImageIcon)value);
				} else {
					this.setText((value == null) ? "" : value.toString());
				}
				this.setBorder(UIManager.getBorder("TableHeader.cellBorder"));
				this.setHorizontalAlignment(JLabel.CENTER);
				return this;
			}
		};

		column=this.jTable1.getColumnModel().getColumn(this.CONSTB_COLUMN_INDEX);
		column.setHeaderRenderer(this.iconHeaderRender);
		this.constbIcon=new javax.swing.ImageIcon(AMESFrame.class.getResource("/resources/constb.gif"));
		column.setHeaderValue(this.constbIcon);

		column=this.jTable1.getColumnModel().getColumn(this.CAPU_COLUMN_INDEX);
		column.setHeaderRenderer(this.iconHeaderRender);
		this.capuIcon=new javax.swing.ImageIcon(AMESFrame.class.getResource("/resources/capu.gif"));
		column.setHeaderValue(this.capuIcon);

		column=this.jTable1.getColumnModel().getColumn(this.CAPI_COLUMN_INDEX);
		column.setHeaderRenderer(this.iconHeaderRender);
		this.caplIcon=new javax.swing.ImageIcon(AMESFrame.class.getResource("/resources/capl.gif"));
		column.setHeaderValue(this.caplIcon);

		// Set the component to show the popup menu
		this.jTable1.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent evt) {
				if (evt.isPopupTrigger()) {
					PowerGridConfigure4.this.popupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
				}
			}
			@Override
			public void mouseReleased(MouseEvent evt) {
				if (evt.isPopupTrigger()) {
					PowerGridConfigure4.this.popupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
				}
			}
		});

	}


	private void addRowItemActionPerformed(java.awt.event.ActionEvent evt) {
		DefaultTableModel tableModel = (DefaultTableModel) this.jTable1.getModel();
		int iSelectRow =  this.jTable1.getSelectedRow();
		tableModel.insertRow(iSelectRow, this.blankRowData );

		this.jTable1.repaint();

		this.mainFrame.addGenNumber();
	}

	private void copyRowItemActionPerformed(java.awt.event.ActionEvent evt) {
		DefaultTableModel tableModel = (DefaultTableModel) this.jTable1.getModel();
		Vector data = tableModel.getDataVector();

		int iSelectRow =  this.jTable1.getSelectedRow();

		Vector row = (Vector)data.elementAt(iSelectRow);
		this.copyRowVector  = (Vector)row.clone();

		this.jTable1.repaint();
	}

	private void pasteRowItemActionPerformed(java.awt.event.ActionEvent evt) {
		DefaultTableModel tableModel = (DefaultTableModel) this.jTable1.getModel();
		Vector data = tableModel.getDataVector();
		int iSelectRow =  this.jTable1.getSelectedRow();

		Vector selectRow = (Vector)data.elementAt(iSelectRow);
		for (int i=0; i<selectRow.size(); i++) {
			selectRow.set(i, this.copyRowVector.get(i));
		}

		this.jTable1.repaint();
	}

	private void deleteRowItemActionPerformed(java.awt.event.ActionEvent evt) {
		DefaultTableModel tableModel = (DefaultTableModel) this.jTable1.getModel();
		int iSelectRow =  this.jTable1.getSelectedRow();
		tableModel.removeRow(iSelectRow);

		this.jTable1.repaint();
		this.mainFrame.deleteGenNumber();
	}

	public void  loadBlankData( ) {
		DefaultTableModel blankDataModel = new DefaultTableModel(this.blankData,  this.names);

		this.jTable1.setModel(blankDataModel);
	}

	public void  loadData(Object [][] loadData ) {
		DefaultTableModel loadDataModel = new DefaultTableModel(loadData,  this.names);

		this.jTable1.setModel(loadDataModel);

		TableColumn column = null;
		this.jTable1.setAutoscrolls(true);
		this.jScrollPane1.setViewportView(this.jTable1);

		DefaultTableCellRenderer   renderer   =   new   DefaultTableCellRenderer();
		renderer.setHorizontalAlignment(JLabel.CENTER);

		this.jTable1.setDefaultRenderer(Object.class,   renderer);
		this.jTable1.setToolTipText("GenCo Parameters Table");

		JTableHeader header = this.jTable1.getTableHeader();

		column=this.jTable1.getColumnModel().getColumn(this.CONSTB_COLUMN_INDEX);
		column.setHeaderRenderer(this.iconHeaderRender);
		column.setHeaderValue(this.constbIcon);

		column=this.jTable1.getColumnModel().getColumn(this.CAPI_COLUMN_INDEX);
		column.setHeaderRenderer(this.iconHeaderRender);
		column.setHeaderValue(this.caplIcon);

		column=this.jTable1.getColumnModel().getColumn(this.CAPU_COLUMN_INDEX);
		column.setHeaderRenderer(this.iconHeaderRender);
		column.setHeaderValue(this.capuIcon);

		this.jTable1.setAutoscrolls(true);
		this.jTable1.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		for (int i = 0; i < this.names.length; i++) {
			column =this.jTable1.getColumnModel().getColumn(i);

			column.setPreferredWidth(120);
		}
	}

	public void addRowsBlankData(int iRow) {
		DefaultTableModel tableModel = new DefaultTableModel(this.blankData,  this.names);

		for(int i=1; i<iRow; i++) {
			tableModel.insertRow(i-1, this.blankRowData );
		}

		this.jTable1.setModel(tableModel);
		this.jTable1.repaint();

	}


	public Object [][] saveData( ) {
		DefaultTableModel tableModel = (DefaultTableModel) this.jTable1.getModel();
		int iRowCount = tableModel.getRowCount();
		int iColCount = tableModel.getColumnCount();

		Object [][] returnData = new Object [iRowCount][iColCount];

		for(int i=0; i<iRowCount; i++) {
			for(int j=0; j<iColCount; j++) {
				returnData[i][j] = tableModel.getValueAt(i,j);
			}
		}

		this.mainFrame.setdGenNumber(iRowCount);
		return returnData;
	}

	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
	// <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
	private void initComponents() {

		this.jPanel1 = new javax.swing.JPanel();
		this.jScrollPane1 = new javax.swing.JScrollPane();
		this.jTable1 = new javax.swing.JTable();
		this.CancelButton = new javax.swing.JButton();
		this.PrevButton = new javax.swing.JButton();
		this.NextButton = new javax.swing.JButton();
		this.DataVerifyButton = new javax.swing.JButton();

		this.jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

		this.jScrollPane1.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));

		this.jTable1.setFont(new java.awt.Font("Arial", 0, 12));
		this.jTable1.setModel(new javax.swing.table.DefaultTableModel(
				new Object [][] {
					{null, null, null, null},
					{null, null, null, null},
					{null, null, null, null},
					{null, null, null, null}
				},
				new String [] {
						"Title 1", "Title 2", "Title 3", "Title 4"
				}
				));
		this.jScrollPane1.setViewportView(this.jTable1);

		javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(this.jPanel1);
		this.jPanel1.setLayout(jPanel1Layout);
		jPanel1Layout.setHorizontalGroup(
				jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel1Layout.createSequentialGroup()
						.addContainerGap()
						.addComponent(this.jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 641, Short.MAX_VALUE)
						.addContainerGap())
				);
		jPanel1Layout.setVerticalGroup(
				jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel1Layout.createSequentialGroup()
						.addGap(38, 38, 38)
						.addComponent(this.jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 162, javax.swing.GroupLayout.PREFERRED_SIZE)
						.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				);

		this.CancelButton.setFont(new java.awt.Font("Arial", 0, 12));
		this.CancelButton.setText("Cancel");
		this.CancelButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
		this.CancelButton.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				PowerGridConfigure4.this.CancelButtonActionPerformed(evt);
			}
		});

		this.PrevButton.setFont(new java.awt.Font("Arial", 0, 12));
		this.PrevButton.setText("<< Prev");
		this.PrevButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
		this.PrevButton.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				PowerGridConfigure4.this.PrevButtonActionPerformed(evt);
			}
		});

		this.NextButton.setFont(new java.awt.Font("Arial", 0, 12));
		this.NextButton.setText("Next >>");
		this.NextButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
		this.NextButton.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				PowerGridConfigure4.this.NextButtonActionPerformed(evt);
			}
		});

		this.DataVerifyButton.setFont(new java.awt.Font("Arial", 0, 12));
		this.DataVerifyButton.setText("Data Verification");
		this.DataVerifyButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
		this.DataVerifyButton.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				PowerGridConfigure4.this.DataVerifyButtonActionPerformed(evt);
			}
		});

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this.getContentPane());
		this.getContentPane().setLayout(layout);
		layout.setHorizontalGroup(
				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addComponent(this.jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
						.addGap(74, 74, 74)
						.addComponent(this.DataVerifyButton, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 265, Short.MAX_VALUE)
						.addComponent(this.CancelButton, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(this.PrevButton, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(this.NextButton, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)
						.addContainerGap())
				);
		layout.setVerticalGroup(
				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
						.addComponent(this.jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(this.NextButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(this.PrevButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(this.CancelButton)
								.addComponent(this.DataVerifyButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
						.addContainerGap())
				);

		this.pack();
	}// </editor-fold>//GEN-END:initComponents
	public String DataVerify() {
		String strMessage="";

		// Verify each parameter is greater than 0
		// Step4: GenCo parameters
		//  "GenCo Name", "ID", "atBus", "FCost ($/H)", "a ($/MWh)", "b ($/MW2h)", "CapL (MW)", "CapU (MW)", "InitMoney ($)"
		DefaultTableModel tableModel = (DefaultTableModel) this.jTable1.getModel();
		int iRowCount = tableModel.getRowCount();
		int iColCount = tableModel.getColumnCount();

		Object [][] verifyData = new Object [iRowCount][iColCount];

		for(int i=0; i<iRowCount; i++) {
			for(int j=0; j<iColCount; j++) {
				verifyData[i][j] = tableModel.getValueAt(i,j);
			}
		}

		for(int i=0; i<verifyData.length; i++) {
			if(Integer.parseInt(verifyData[i][1].toString())<0) {
				strMessage+="The "+i+"th generator in ID column is not bigger than 0\n";
			}

			if(Integer.parseInt(verifyData[i][2].toString())<0) {
				strMessage+="The "+i+"th generator in atBus column is not bigger than 0\n";
			}

			if(Support.parseDouble(verifyData[i][3].toString())<0.0) {
				strMessage+="The "+i+"th generator in FCost column is not bigger than 0.0\n";
			}

			if(Support.parseDouble(verifyData[i][5].toString())<0.0) {
				strMessage+="The "+i+"th generator in b column is not bigger than 0.0\n";
			}

			if(Support.parseDouble(verifyData[i][6].toString())<0.0) {
				strMessage+="The "+i+"th generator in CapL column is not bigger than 0.0\n";
			}

			if(Support.parseDouble(verifyData[i][7].toString())<0.0) {
				strMessage+="The "+i+"th generator in CapU column is not bigger than 0.0\n";
			}

			if(Support.parseDouble(verifyData[i][8].toString())<0.0) {
				strMessage+="The "+i+"th generator in InitMoney column is not bigger than 0.0\n";
			}

			String isAlertValue = verifyData[i][9].toString();
			if (!"false".equalsIgnoreCase(isAlertValue)
					&& !"true".equalsIgnoreCase(isAlertValue)) {
				strMessage+="The "+i+"th generator in Alert Generator column must be true or false, but is " + isAlertValue + ".";
			}

			double da=Support.parseDouble(verifyData[i][4].toString());
			double db=Support.parseDouble(verifyData[i][5].toString());
			double dcapl=Support.parseDouble(verifyData[i][6].toString());

			if((da+(2.0*db*dcapl))<0.0) {
				//    strMessage+="The "+i+"th generator a+2*b*capL is not bigger than 0.0\n";
				System.err.println("The "+i+"th generator a+2*b*capL is not bigger than 0.0");
			}
		}

		return strMessage;
	}

	private void DataVerifyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DataVerifyButtonActionPerformed
		String strErrorMessage=this.DataVerify();
		if(!strErrorMessage.isEmpty()) {
			JOptionPane.showMessageDialog(this, strErrorMessage, "Case Data Verification Message", JOptionPane.ERROR_MESSAGE);
		} else {
			String strMessage="Case data verify ok!";
			JOptionPane.showMessageDialog(this, strMessage, "Case Data Verification Message", JOptionPane.INFORMATION_MESSAGE);
		}
	}//GEN-LAST:event_DataVerifyButtonActionPerformed

	private void NextButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_NextButtonActionPerformed
		this.setVisible(false);
		this.mainFrame.activeLearnOption1();
	}//GEN-LAST:event_NextButtonActionPerformed

	private void PrevButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_PrevButtonActionPerformed
		this.setVisible(false);
		this.mainFrame.activeConfig2();
	}//GEN-LAST:event_PrevButtonActionPerformed

	private void CancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CancelButtonActionPerformed
		this.setVisible(false);
	}//GEN-LAST:event_CancelButtonActionPerformed


	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JButton CancelButton;
	private javax.swing.JButton DataVerifyButton;
	private javax.swing.JButton NextButton;
	private javax.swing.JButton PrevButton;
	private javax.swing.JPanel jPanel1;
	private javax.swing.JScrollPane jScrollPane1;
	private javax.swing.JTable jTable1;
	// End of variables declaration//GEN-END:variables

	private TableCellRenderer iconHeaderRender;
	private ImageIcon caplIcon;
	private ImageIcon capuIcon;
	private ImageIcon constbIcon;
	private final int CONSTB_COLUMN_INDEX=5;
	private final int CAPI_COLUMN_INDEX=6;
	private final int CAPU_COLUMN_INDEX=7;

	private Vector copyRowVector;
	private javax.swing.JPopupMenu popupMenu = new JPopupMenu();
	private JMenuItem addRowItem,  copyRowItem, pasteRowItem, deleteRowItem;

	private AMESFrame mainFrame;

	Object [][] data =  {
			{"Gen1", 1, 1, 1600, 14, 0.005, 0, 110, 1000000, false},
			{"Gen2", 2, 1, 1200, 15, 0.006, 0, 100, 1000000, false},
			{"Gen3", 3, 3, 8500, 25, 0.010, 0, 520, 1000000, false},
			{"Gen4", 4, 4, 1000, 30, 0.012, 0, 200, 1000000, false},
			{"Gen5", 5, 5, 5400, 10, 0.007, 0, 600, 1000000, false}
	};
	final Object [] blankRowData = {"GenCo Name", 0, 0, 0, 0, 0, 0, 0, 0, false};

	final Object [][] blankData = { {"GenCo Name", 0, 0, 0, 0, 0, 0, 0, 0, false} };

	String [] names =  {
			"GenCo Name", "ID", "atBus", "FCost ($/H)", "a ($/MWh)", "b ($/MW2h)", "CapL (MW)", "CapU (MW)", "InitMoney ($)", "Alert Generator(?)"
	};
}

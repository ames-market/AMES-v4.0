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
 * PowerGridConfigure5.java
 *
 * Created on May 28, 2007, 10:39 PM
 */

package AMESGUIFrame;

import java.awt.Component;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import AMESGUIFrame.datacontrols.LSEDemandConfig;
import amesmarket.Support;

public class PowerGridConfigure5 extends LSEDemandConfig {

	/** Creates new form PowerGridConfigure5
	 * @param frame
	 */
	public PowerGridConfigure5(AMESFrame frame) {
		this.mainFrame=frame;
		this.initComponents();

		this.CheckAllFixedLoadItem = this.popupMenuFlagTable.add("Check all fixed load");
		this.CheckAllFixedLoadItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				PowerGridConfigure5.this.CheckAllFixedLoaItemActionPerformed(evt);
			}
		});

		this.popupMenuFlagTable.addSeparator();

		this.unCheckAllFixedLoadItem = this.popupMenuFlagTable.add("UnCheck all fixed load");
		this.unCheckAllFixedLoadItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				PowerGridConfigure5.this.unCheckAllFixedLoaItemActionPerformed(evt);
			}
		});

		this.popupMenuFlagTable.addSeparator();

		this.CheckAllPriceSensitiveItem = this.popupMenuFlagTable.add("Check all price-sensitive demand");
		this.CheckAllPriceSensitiveItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				PowerGridConfigure5.this.CheckAllPriceSensitiveItemActionPerformed(evt);
			}
		});

		this.popupMenuFlagTable.addSeparator();

		this.unCheckAllPriceSensitiveItem = this.popupMenuFlagTable.add("UnCheck all price-sensitive demand");
		this.unCheckAllPriceSensitiveItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				PowerGridConfigure5.this.unCheckAllPriceSensitiveItemActionPerformed(evt);
			}
		});

		this.popupMenuFlagTable.addSeparator();

		this.copyRowFixedLoadItem = this.popupMenuFixedLoadTable.add("Copy A Row");
		this.copyRowFixedLoadItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				PowerGridConfigure5.this.copyRowItemFixedLoadActionPerformed(evt);
			}
		});

		this.popupMenuFixedLoadTable.addSeparator();

		this.pasteRowFixedLoadItem = this.popupMenuFixedLoadTable.add("Paste A Row");
		this.pasteRowFixedLoadItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				PowerGridConfigure5.this.pasteRowItemFixedLoadActionPerformed(evt);
			}
		});

		this.popupMenuFixedLoadTable.addSeparator();

		this.copyRowPriceSensitiveItem = this.popupMenuPriceSensitiveTable.add("Copy A Row");
		this.copyRowPriceSensitiveItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				PowerGridConfigure5.this.copyRowItemPriceSensitiveActionPerformed(evt);
			}
		});

		this.popupMenuPriceSensitiveTable.addSeparator();

		this.pasteRowPriceSensitiveItem = this.popupMenuPriceSensitiveTable.add("Paste A Row");
		this.pasteRowPriceSensitiveItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				PowerGridConfigure5.this.pasteRowItemPriceSensitiveActionPerformed(evt);
			}
		});

		this.popupMenuPriceSensitiveTable.addSeparator();

		this.setTitle("Step 5: Input LSE parameters");

		DefaultTableModel dataModel = new DefaultTableModel(this.blankData,  this.names);
		// Create the table
		this.FixedLoadTable = new JTable(dataModel);

		TableColumn column = null;
		this.FixedLoadTable.setAutoscrolls(true);
		this.FixedLoadTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

		DefaultTableCellRenderer renderer=new DefaultTableCellRenderer();
		renderer.setHorizontalAlignment(JLabel.CENTER);
		this.FixedLoadTable.setDefaultRenderer(Object.class, renderer);

		this.FixedLoadTable.setToolTipText("LSE Parameters Table");
		this.jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		this.jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		this.jScrollPane1.setViewportView(this.FixedLoadTable);

		dataModel = new DefaultTableModel(this.blankPriceSensitiveData,  this.priceSensitiveNames);
		// Create the table
		this.PriceSensitiveTable = new JTable(dataModel);

		this.PriceSensitiveTable.setAutoscrolls(true);
		this.PriceSensitiveTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

		this.PriceSensitiveTable.setDefaultRenderer(Object.class, renderer);

		this.PriceSensitiveTable.setToolTipText("LSE Parameters Table");
		this.jScrollPane2.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		this.jScrollPane2.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		this.jScrollPane2.setViewportView(this.PriceSensitiveTable);

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

		column=this.PriceSensitiveTable.getColumnModel().getColumn(this.CONSTD_COLUMN_INDEX);
		column.setHeaderRenderer(this.iconHeaderRender);
		this.constdIcon=new javax.swing.ImageIcon(AMESFrame.class.getResource("/resources/constd.gif"));
		column.setHeaderValue(this.constdIcon);

		this.FlagTable.setToolTipText("First row is for fixed demand and second row is for price-sensitive demand.");
		// Set the component to show the popup menu
		this.FlagTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent evt) {
				if (evt.isPopupTrigger()) {
					PowerGridConfigure5.this.popupMenuFlagTable.show(evt.getComponent(), evt.getX(), evt.getY());
				}
			}
			@Override
			public void mouseReleased(MouseEvent evt) {
				if (evt.isPopupTrigger()) {
					PowerGridConfigure5.this.popupMenuFlagTable.show(evt.getComponent(), evt.getX(), evt.getY());
				}
			}
		});

		// Set the component to show the popup menu
		this.PriceSensitiveTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent evt) {
				if (evt.isPopupTrigger()) {
					PowerGridConfigure5.this.popupMenuPriceSensitiveTable.show(evt.getComponent(), evt.getX(), evt.getY());
				}
			}
			@Override
			public void mouseReleased(MouseEvent evt) {
				if (evt.isPopupTrigger()) {
					PowerGridConfigure5.this.popupMenuPriceSensitiveTable.show(evt.getComponent(), evt.getX(), evt.getY());
				}
			}
		});

		this.FixedLoadTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent evt) {
				if (evt.isPopupTrigger()) {
					PowerGridConfigure5.this.popupMenuFixedLoadTable.show(evt.getComponent(), evt.getX(), evt.getY());
				}
			}
			@Override
			public void mouseReleased(MouseEvent evt) {
				if (evt.isPopupTrigger()) {
					PowerGridConfigure5.this.popupMenuFixedLoadTable.show(evt.getComponent(), evt.getX(), evt.getY());
				}
			}
		});

		this.iCurrentLSEIndex=0;
	}


	private void CheckAllFixedLoaItemActionPerformed(java.awt.event.ActionEvent evt) {
		for(int j=0; j<24; j++) {
			this.lseDemandFlag[0][j]=true;
		}

		this.FlagTable.repaint();
	}

	private void unCheckAllFixedLoaItemActionPerformed(java.awt.event.ActionEvent evt) {
		for(int j=0; j<24; j++) {
			this.lseDemandFlag[0][j]=false;
		}

		this.FlagTable.repaint();
	}

	private void CheckAllPriceSensitiveItemActionPerformed(java.awt.event.ActionEvent evt) {
		for(int j=0; j<24; j++) {
			this.lseDemandFlag[1][j]=true;
		}

		this.FlagTable.repaint();
	}

	private void unCheckAllPriceSensitiveItemActionPerformed(java.awt.event.ActionEvent evt) {
		for(int j=0; j<24; j++) {
			this.lseDemandFlag[1][j]=false;
		}

		this.FlagTable.repaint();
	}


	private void copyRowItemPriceSensitiveActionPerformed(java.awt.event.ActionEvent evt) {
		DefaultTableModel tableModel = (DefaultTableModel) this.PriceSensitiveTable.getModel();
		Vector dataConstantLoad = tableModel.getDataVector();

		int iSelectRow =  this.PriceSensitiveTable.getSelectedRow();

		Vector row = (Vector)dataConstantLoad.elementAt(iSelectRow);
		this.copyRowVector  = (Vector)row.clone();

		this.PriceSensitiveTable.repaint();
	}

	private void pasteRowItemPriceSensitiveActionPerformed(java.awt.event.ActionEvent evt) {
		DefaultTableModel tableModel = (DefaultTableModel) this.PriceSensitiveTable.getModel();
		Vector dataConstantLoad = tableModel.getDataVector();
		int iSelectRow =  this.PriceSensitiveTable.getSelectedRow();

		Vector selectRow = (Vector)dataConstantLoad.elementAt(iSelectRow);
		for (int i=0; i<selectRow.size(); i++) {
			selectRow.set(i, this.copyRowVector.get(i));
		}

		this.PriceSensitiveTable.repaint();
	}

	private void copyRowItemFixedLoadActionPerformed(java.awt.event.ActionEvent evt) {
		DefaultTableModel tableModel = (DefaultTableModel) this.FixedLoadTable.getModel();
		Vector dataConstantLoad = tableModel.getDataVector();

		int iSelectRow =  this.FixedLoadTable.getSelectedRow();

		Vector row = (Vector)dataConstantLoad.elementAt(iSelectRow);
		this.copyRowVector  = (Vector)row.clone();

		this.FixedLoadTable.repaint();
	}

	private void pasteRowItemFixedLoadActionPerformed(java.awt.event.ActionEvent evt) {
		DefaultTableModel tableModel = (DefaultTableModel) this.FixedLoadTable.getModel();
		Vector dataConstantLoad = tableModel.getDataVector();
		int iSelectRow =  this.FixedLoadTable.getSelectedRow();

		Vector selectRow = (Vector)dataConstantLoad.elementAt(iSelectRow);
		for (int i=0; i<selectRow.size(); i++) {
			selectRow.set(i, this.copyRowVector.get(i));
		}

		this.FixedLoadTable.repaint();
	}


	/* (non-Javadoc)
	 * @see AMESGUIFrame.LSEDemandConfig#loadBlankData()
	 */
	@Override
	public void  loadBlankData( ) {
	}

	class HybridTableModel extends AbstractTableModel {
		@Override
		public int getColumnCount() {
			return PowerGridConfigure5.this.priceHybridNames.length;
		}

		@Override
		public int getRowCount() {
			return PowerGridConfigure5.this.lseDemandFlag.length;
		}

		@Override
		public String getColumnName(int col) {
			return PowerGridConfigure5.this.priceHybridNames[col];
		}

		@Override
		public Object getValueAt(int row, int col) {
			return PowerGridConfigure5.this.lseDemandFlag[row][col];
		}

		/*
		 * JTable uses this method to determine the default renderer/
		 * editor for each cell.  If we didn't implement this method,
		 * then the last column would contain text ("true"/"false"),
		 * rather than a check box.
		 */
		@Override
		public Class getColumnClass(int c) {
			return this.getValueAt(0, c).getClass();
		}

		/*
		 * Don't need to implement this method unless your table's
		 * editable.
		 */
		@Override
		public boolean isCellEditable(int row, int col) {
			return true;
		}

		/*
		 * Don't need to implement this method unless your table's
		 * data can change.
		 */
		@Override
		public void setValueAt(Object value, int row, int col) {
			PowerGridConfigure5.this.lseDemandFlag[row][col] = value;
			this.fireTableCellUpdated(row, col);
		}

	};

	/* (non-Javadoc)
	 * @see AMESGUIFrame.LSEDemandConfig#loadData(java.lang.Object[][], java.lang.Object[][][], java.lang.Object[][])
	 */
	@Override
	public void  loadData(Object [][] lseFixedData, Object[][][] lsePriceSensitiveData, Object[][] lseHybridData) {

		this.bDataLoad=false;

		this.LSENameComboBox.removeAllItems();
		this.lseFixedDemandList.clear();
		this.lsePriceSensitiveDemandList.clear();
		this.lseHybridDemandList.clear();


		int iRow=lseFixedData.length;
		int iCol=lseFixedData[0].length;

		for(int i=0; i<iRow; i++) {
			Object[] FixedDemandArray=new Object[iCol];

			for(int j=0; j<iCol; j++) {
				FixedDemandArray[j]=lseFixedData[i][j];
			}

			this.lseFixedDemandList.add(FixedDemandArray);
		}

		if(lsePriceSensitiveData!=null) {
			iRow=lsePriceSensitiveData.length;
			iCol=lsePriceSensitiveData[0][0].length;
			for(int i=0; i<iRow; i++) {
				Object[][] PriceDemandArray=new Object[24][iCol];

				for(int h=0; h<24; h++) {
					for(int j=0; j<iCol; j++) {
						PriceDemandArray[h][j]=lsePriceSensitiveData[i][h][j];
					}
				}

				this.lsePriceSensitiveDemandList.add(PriceDemandArray);
			}
		}

		if(lseHybridData!=null) {
			iRow=lseHybridData.length;
			iCol=lseHybridData[0].length;
			for(int i=0; i<iRow; i++) {
				Object[] HybridDemandArray=new Object[iCol];

				for(int j=0; j<iCol; j++) {
					HybridDemandArray[j]=lseHybridData[i][j];
				}
				this.lseHybridDemandList.add(HybridDemandArray);
				this.LSENameComboBox.addItem(lseHybridData[i][0].toString());
			}
		}

		if(this.LSENameComboBox.getItemCount()<1) {
			return;
		}
		this.LSENameComboBox.setSelectedIndex(0);
		this.lseName = (String)this.LSENameComboBox.getItemAt(0);
		this.SelectLSE(this.lseName);

		this.bDataLoad=true;
	}

	/* (non-Javadoc)
	 * @see AMESGUIFrame.LSEDemandConfig#addRowsBlankData(int)
	 */
	@Override
	public void addRowsBlankData(int iRow) {
	}

	/* (non-Javadoc)
	 * @see AMESGUIFrame.LSEDemandConfig#saveTableDataToList()
	 */
	@Override
	public void saveTableDataToList() {
		String selectedName=(String)this.LSENameComboBox.getSelectedItem();

		for(int i=0; i<this.lseHybridDemandList.size(); i++) {
			Object [] temp=(Object [])this.lseHybridDemandList.get(i);
			if(selectedName.equalsIgnoreCase(temp[0].toString())) {
				String strTemp=this.IDTextField.getText();
				this.iID=Integer.parseInt(strTemp);
				temp[1]=this.iID;
				strTemp=this.AtNodeTextField.getText();
				this.iAtNode=Integer.parseInt(strTemp);
				temp[2]=this.iAtNode;

				for(int j=0; j<24; j++) {
					int Flag=0;
					if(Boolean.parseBoolean(this.lseDemandFlag[0][j].toString())) {
						Flag+=1;
					}
					if(Boolean.parseBoolean(this.lseDemandFlag[1][j].toString())) {
						Flag+=2;
					}

					temp[j+3]=Flag;
				}

				for (int j=0; j<this.lseFixedDemandList.size(); j++) {
					Object [] tempFixed=(Object [])this.lseFixedDemandList.get(j);
					if(selectedName.equalsIgnoreCase(tempFixed[0].toString())) {
						tempFixed[1]=this.iID;
						tempFixed[2]=this.iAtNode;
						DefaultTableModel tableModelFixedLoad = (DefaultTableModel) this.FixedLoadTable.getModel();
						for(int k=0; k<24; k++) {
							tempFixed[k+3]=tableModelFixedLoad.getValueAt(0,k);
						}
					}
				}

				for (int j=0; j<this.lsePriceSensitiveDemandList.size(); j++) {
					Object [][] tempPrice=(Object [][])this.lsePriceSensitiveDemandList.get(j);
					if(selectedName.equalsIgnoreCase(tempPrice[0][0].toString())) {
						DefaultTableModel tableModelPriceDemand = (DefaultTableModel) this.PriceSensitiveTable.getModel();

						for(int h=0; h<24; h++) {
							tempPrice[h][1]=this.iID;
							tempPrice[h][2]=this.iAtNode;
							for(int k=0; k<4; k++) {
								tempPrice[h][k+3]=tableModelPriceDemand.getValueAt(h,k);
							}
						}
					}
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see AMESGUIFrame.LSEDemandConfig#saveData()
	 */
	@Override
	public void saveData() {
		this.saveTableDataToList();

		this.mainFrame.lseData=this.TransListToArray(this.lseFixedDemandList);
		this.mainFrame.lsePriceSensitiveDemand=this.TransPriceSensitiveListToArray(this.lsePriceSensitiveDemandList);
		this.mainFrame.lseHybridDemand=this.TransListToArray(this.lseHybridDemandList);
		this.mainFrame.setLSENumber(this.lseHybridDemandList.size());

	}

	private Object [][] TransListToArray(ArrayList list) {
		int iRow=list.size();
		Object [][] obj;
		int iCol=0;

		if(iRow<1) {
			obj=null;
		} else {
			iCol=((Object [])list.get(0)).length;
			obj=new Object[iRow][iCol];
		}

		for(int i=0; i<iRow; i++) {
			Object [] temp=(Object [])list.get(i);

			for(int j=0; j<iCol; j++) {
				obj[i][j]=temp[j];
			}
		}

		return obj;
	}

	private Object [][][] TransPriceSensitiveListToArray(ArrayList list) {
		int iRow=list.size();
		Object [][][] obj;
		int iCol=0;

		if(iRow<1) {
			obj=null;
		} else {
			iCol=((Object [][])list.get(0))[0].length;
			obj=new Object[iRow][24][iCol];
		}

		for(int i=0; i<iRow; i++) {
			Object [][] temp=(Object [][])list.get(i);

			for(int h=0; h<24; h++) {
				for(int j=0; j<iCol; j++) {
					obj[i][h][j]=temp[h][j];
				}
			}
		}

		return obj;
	}
	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
	// <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
	private void initComponents() {

		this.jPanel1 = new javax.swing.JPanel();
		this.jLabel1 = new javax.swing.JLabel();
		this.LSENameComboBox = new javax.swing.JComboBox();
		this.AddlButton = new javax.swing.JButton();
		this.DeletelButton = new javax.swing.JButton();
		this.jLabel2 = new javax.swing.JLabel();
		this.IDTextField = new javax.swing.JTextField();
		this.jLabel3 = new javax.swing.JLabel();
		this.AtNodeTextField = new javax.swing.JTextField();
		this.jPanel2 = new javax.swing.JPanel();
		this.jScrollPane1 = new javax.swing.JScrollPane();
		this.FixedLoadTable = new javax.swing.JTable();
		this.jLabel4 = new javax.swing.JLabel();
		this.jPanel4 = new javax.swing.JPanel();
		this.jScrollPane3 = new javax.swing.JScrollPane();
		this.FlagTable = new javax.swing.JTable();
		this.jLabel6 = new javax.swing.JLabel();
		this.jPanel3 = new javax.swing.JPanel();
		this.jScrollPane2 = new javax.swing.JScrollPane();
		this.PriceSensitiveTable = new javax.swing.JTable();
		this.jLabel5 = new javax.swing.JLabel();
		this.Prev = new javax.swing.JButton();
		this.DataVerifyButton = new javax.swing.JButton();
		this.NextButton = new javax.swing.JButton();
		this.CancelButton = new javax.swing.JButton();

		this.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

		this.jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

		this.jLabel1.setFont(new java.awt.Font("Arial", 1, 12));
		this.jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		this.jLabel1.setText("LSE name:");
		this.jLabel1.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));

		this.LSENameComboBox.setEditable(true);
		this.LSENameComboBox.setFont(new java.awt.Font("Arial", 0, 12));
		this.LSENameComboBox.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				PowerGridConfigure5.this.LSENameComboBoxActionPerformed(evt);
			}
		});

		this.AddlButton.setFont(new java.awt.Font("Arial", 0, 12));
		this.AddlButton.setText("Add");
		this.AddlButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
		this.AddlButton.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				PowerGridConfigure5.this.AddlButtonActionPerformed(evt);
			}
		});

		this.DeletelButton.setFont(new java.awt.Font("Arial", 0, 12));
		this.DeletelButton.setText("Delete");
		this.DeletelButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
		this.DeletelButton.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				PowerGridConfigure5.this.DeletelButtonActionPerformed(evt);
			}
		});

		this.jLabel2.setFont(new java.awt.Font("Arial", 1, 12));
		this.jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		this.jLabel2.setText("ID No:");
		this.jLabel2.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));

		this.IDTextField.setFont(new java.awt.Font("Tahoma", 0, 12));
		this.IDTextField.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
		this.IDTextField.setText("0");

		this.jLabel3.setFont(new java.awt.Font("Arial", 1, 12));
		this.jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		this.jLabel3.setText("Bus No:");
		this.jLabel3.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));

		this.AtNodeTextField.setFont(new java.awt.Font("Tahoma", 0, 12));
		this.AtNodeTextField.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
		this.AtNodeTextField.setText("0");

		this.jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED)));

		this.FixedLoadTable.setFont(new java.awt.Font("Arial", 0, 12));
		this.FixedLoadTable.setModel(new javax.swing.table.DefaultTableModel(
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
		this.jScrollPane1.setViewportView(this.FixedLoadTable);

		this.jLabel4.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
		this.jLabel4.setText("Fixed Demand Values by Hour");
		this.jLabel4.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));

		javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(this.jPanel2);
		this.jPanel2.setLayout(jPanel2Layout);
		jPanel2Layout.setHorizontalGroup(
				jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel2Layout.createSequentialGroup()
						.addContainerGap()
						.addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addComponent(this.jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 507, Short.MAX_VALUE)
								.addComponent(this.jLabel4))
						.addContainerGap())
				);
		jPanel2Layout.setVerticalGroup(
				jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
						.addComponent(this.jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, 21, Short.MAX_VALUE)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(this.jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE))
				);

		this.jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED)));

		this.FlagTable.setFont(new java.awt.Font("Arial", 0, 12));
		this.FlagTable.setModel(new javax.swing.table.DefaultTableModel(
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
		this.jScrollPane3.setViewportView(this.FlagTable);

		this.jLabel6.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
		this.jLabel6.setText("Flag Selection for Existence of Fixed and/or Price-Sensitive Demand by Hour");
		this.jLabel6.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));

		javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(this.jPanel4);
		this.jPanel4.setLayout(jPanel4Layout);
		jPanel4Layout.setHorizontalGroup(
				jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel4Layout.createSequentialGroup()
						.addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addGroup(jPanel4Layout.createSequentialGroup()
										.addGap(10, 10, 10)
										.addComponent(this.jLabel6))
								.addGroup(jPanel4Layout.createSequentialGroup()
										.addContainerGap()
										.addComponent(this.jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 507, Short.MAX_VALUE)))
						.addContainerGap())
				);
		jPanel4Layout.setVerticalGroup(
				jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
						.addComponent(this.jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, 21, Short.MAX_VALUE)
						.addGap(11, 11, 11)
						.addComponent(this.jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE))
				);

		this.jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED)));

		this.PriceSensitiveTable.setFont(new java.awt.Font("Arial", 0, 12));
		this.PriceSensitiveTable.setModel(new javax.swing.table.DefaultTableModel(
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
		this.jScrollPane2.setViewportView(this.PriceSensitiveTable);

		this.jLabel5.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
		this.jLabel5.setText("Price-Sensitive Demand Function Parameters by Hour");
		this.jLabel5.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));

		javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(this.jPanel3);
		this.jPanel3.setLayout(jPanel3Layout);
		jPanel3Layout.setHorizontalGroup(
				jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel3Layout.createSequentialGroup()
						.addContainerGap()
						.addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addComponent(this.jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 507, Short.MAX_VALUE)
								.addComponent(this.jLabel5))
						.addContainerGap())
				);
		jPanel3Layout.setVerticalGroup(
				jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel3Layout.createSequentialGroup()
						.addComponent(this.jLabel5)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
						.addComponent(this.jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 118, Short.MAX_VALUE)
						.addContainerGap())
				);

		javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(this.jPanel1);
		this.jPanel1.setLayout(jPanel1Layout);
		jPanel1Layout.setHorizontalGroup(
				jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel1Layout.createSequentialGroup()
						.addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addGroup(jPanel1Layout.createSequentialGroup()
										.addGap(22, 22, 22)
										.addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
												.addComponent(this.jLabel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 101, Short.MAX_VALUE)
												.addComponent(this.jLabel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE))
										.addGap(26, 26, 26)
										.addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
												.addGroup(jPanel1Layout.createSequentialGroup()
														.addComponent(this.LSENameComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 149, javax.swing.GroupLayout.PREFERRED_SIZE)
														.addGap(57, 57, 57)
														.addComponent(this.AddlButton, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
														.addGap(26, 26, 26)
														.addComponent(this.DeletelButton, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE))
												.addGroup(jPanel1Layout.createSequentialGroup()
														.addComponent(this.IDTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)
														.addGap(26, 26, 26)
														.addComponent(this.jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)
														.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
														.addComponent(this.AtNodeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE))))
								.addGroup(jPanel1Layout.createSequentialGroup()
										.addContainerGap()
										.addComponent(this.jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
								.addGroup(jPanel1Layout.createSequentialGroup()
										.addContainerGap()
										.addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
												.addComponent(this.jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
												.addComponent(this.jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
						.addContainerGap())
				);
		jPanel1Layout.setVerticalGroup(
				jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel1Layout.createSequentialGroup()
						.addContainerGap()
						.addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(this.jLabel1)
								.addComponent(this.LSENameComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
								.addComponent(this.AddlButton)
								.addComponent(this.DeletelButton))
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(this.jLabel2)
								.addComponent(this.jLabel3)
								.addComponent(this.IDTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
								.addComponent(this.AtNodeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(this.jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(this.jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(this.jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
						.addContainerGap())
				);

		this.Prev.setFont(new java.awt.Font("Arial", 0, 12));
		this.Prev.setText("<< Prev");
		this.Prev.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
		this.Prev.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				PowerGridConfigure5.this.PrevActionPerformed(evt);
			}
		});

		this.DataVerifyButton.setFont(new java.awt.Font("Arial", 0, 12));
		this.DataVerifyButton.setText("Data Verification");
		this.DataVerifyButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
		this.DataVerifyButton.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				PowerGridConfigure5.this.DataVerifyButtonActionPerformed(evt);
			}
		});

		this.NextButton.setFont(new java.awt.Font("Arial", 0, 12));
		this.NextButton.setText("Next >>");
		this.NextButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
		this.NextButton.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				PowerGridConfigure5.this.NextButtonActionPerformed(evt);
			}
		});

		this.CancelButton.setFont(new java.awt.Font("Arial", 0, 12));
		this.CancelButton.setText("Cancel");
		this.CancelButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
		this.CancelButton.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				PowerGridConfigure5.this.CancelButtonActionPerformed(evt);
			}
		});

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this.getContentPane());
		this.getContentPane().setLayout(layout);
		layout.setHorizontalGroup(
				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
						.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(this.DataVerifyButton, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE)
						.addGap(191, 191, 191)
						.addComponent(this.CancelButton, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(this.Prev, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE)
						.addGap(15, 15, 15)
						.addComponent(this.NextButton, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)
						.addGap(38, 38, 38))
				.addGroup(layout.createSequentialGroup()
						.addComponent(this.jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addContainerGap())
				);
		layout.setVerticalGroup(
				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
						.addComponent(this.jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
						.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(this.DataVerifyButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(this.NextButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(this.Prev)
								.addComponent(this.CancelButton))
						.addContainerGap())
				);

		this.pack();
	}// </editor-fold>//GEN-END:initComponents

	private void LSENameComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_LSENameComboBoxActionPerformed
		// TODO add your handling code here:
		if(!this.bDataLoad) {
			return;
		}
		int iSelectedIndex=this.LSENameComboBox.getSelectedIndex();
		// nothing left in ComboBox
		if(this.LSENameComboBox.getSelectedItem()==null) {
			return;
		}

		String selectedName=(String)this.LSENameComboBox.getSelectedItem();

		if(this.lseName.equalsIgnoreCase(selectedName)) {
			return;
		}

		//System.out.println("lseName="+lseName+" selectedName="+selectedName+" iCurrentLSEIndex="+iCurrentLSEIndex+" iSelectedIndex="+iSelectedIndex);
		if(iSelectedIndex<0) {
			this.LSENameComboBox.removeItem(this.lseName);
			this.LSENameComboBox.insertItemAt(selectedName, this.iCurrentLSEIndex);
			this.lseName=selectedName;

			Object [] temp=(Object [])this.lseHybridDemandList.get(this.iCurrentLSEIndex);
			temp[0]=selectedName;
			Object [] tempFixed=(Object [])this.lseFixedDemandList.get(this.iCurrentLSEFixedDemandIndex);
			tempFixed[0]=selectedName;
			Object [][] tempPrice=(Object [][])this.lsePriceSensitiveDemandList.get(this.iCurrentLSEPriceDemandIndex);

			for(int h=0; h<24; h++) {
				tempPrice[h][0]=selectedName;
			}

			return;
		} else {
			this.iCurrentLSEIndex=iSelectedIndex;
		}

		for(int i=0; i<this.lseHybridDemandList.size(); i++) {
			Object [] temp=(Object [])this.lseHybridDemandList.get(i);
			if(this.lseName.equalsIgnoreCase(temp[0].toString())) {
				String strTemp=this.IDTextField.getText();
				this.iID=Integer.parseInt(strTemp);
				temp[1]=this.iID;
				strTemp=this.AtNodeTextField.getText();
				this.iAtNode=Integer.parseInt(strTemp);
				temp[2]=this.iAtNode;

				for(int j=0; j<24; j++) {
					int Flag=0;
					if(Boolean.parseBoolean(this.lseDemandFlag[0][j].toString())) {
						Flag+=1;
					}
					if(Boolean.parseBoolean(this.lseDemandFlag[1][j].toString())) {
						Flag+=2;
					}

					temp[j+3]=Flag;
				}

				for (int j=0; j<this.lseFixedDemandList.size(); j++) {
					Object [] tempFixed=(Object [])this.lseFixedDemandList.get(j);
					if(this.lseName.equalsIgnoreCase(tempFixed[0].toString())) {
						tempFixed[1]=this.iID;
						tempFixed[2]=this.iAtNode;
						DefaultTableModel tableModelFixedLoad = (DefaultTableModel) this.FixedLoadTable.getModel();
						for(int k=0; k<24; k++) {
							tempFixed[k+3]=tableModelFixedLoad.getValueAt(0,k);
						}
					}
				}

				for (int j=0; j<this.lsePriceSensitiveDemandList.size(); j++) {
					Object [][] tempPrice=(Object [][])this.lsePriceSensitiveDemandList.get(j);
					if(this.lseName.equalsIgnoreCase(tempPrice[0][0].toString())) {
						for(int h=0; h<24; h++) {
							tempPrice[h][1]=this.iID;
							tempPrice[h][2]=this.iAtNode;
							DefaultTableModel tableModelPriceDemand = (DefaultTableModel) this.PriceSensitiveTable.getModel();
							for(int k=0; k<4; k++) {
								tempPrice[h][k+3]=tableModelPriceDemand.getValueAt(h,k);
							}
						}
					}
				}
			}
		}

		this.lseName=selectedName;

		this.SelectLSE(this.lseName);
	}//GEN-LAST:event_LSENameComboBoxActionPerformed

	private void CancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CancelButtonActionPerformed
		this.setVisible(false);
	}//GEN-LAST:event_CancelButtonActionPerformed

	private void NextButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_NextButtonActionPerformed
		this.setVisible(false);
		this.mainFrame.activeSimulationControl();
	}//GEN-LAST:event_NextButtonActionPerformed

	/* (non-Javadoc)
	 * @see AMESGUIFrame.LSEDemandConfig#DataVerify()
	 */
	@Override
	public String DataVerify() {
		String strMessage="";

		this.saveTableDataToList();

		String name;
		for(int i=0; i<this.lseHybridDemandList.size(); i++) {
			Object [] temp=(Object [])this.lseHybridDemandList.get(i);
			name=temp[0].toString();

			int ID=Integer.parseInt(temp[1].toString());
			int AtNode=Integer.parseInt(temp[2].toString());

			if(ID<0) {
				strMessage+=name+"'s ID number is not bigger than 0\n";
			}

			if(AtNode<0) {
				strMessage+=name+"'s atBus number is not bigger than 0\n";
			}

			boolean bFixedDemandFound=false;
			Object [] tempFixed=null;
			for (int k=0; k<this.lseFixedDemandList.size(); k++) {
				tempFixed=(Object [])this.lseFixedDemandList.get(k);
				if(name.equalsIgnoreCase(tempFixed[0].toString())) {
					bFixedDemandFound=true;
					break;
				}
			}

			boolean bPriceSensitiveDemandFound=false;
			Object [][] tempPrice=null;
			for (int k=0; k<this.lsePriceSensitiveDemandList.size(); k++) {
				tempPrice=(Object [][])this.lsePriceSensitiveDemandList.get(k);
				if(name.equalsIgnoreCase(tempPrice[0][0].toString())) {
					bPriceSensitiveDemandFound=true;
					break;
				}
			}

			boolean bUsePriceSensitiveDemand=false;
			for(int j=0; j<24; j++) {
				int Flag=Integer.parseInt(temp[j+3].toString());

				if((Flag&1)==1) {
					if(!bFixedDemandFound) {
						strMessage+=name+"'s fixed demand is not found!\n";
					} else {
						if(Support.parseDouble(tempFixed[j+3].toString())<0.0) {
							strMessage+=name+"'s in "+this.names[j+3]+" column is not bigger than 0.0\n";
						}
					}
				}
				// Check price sensitive demand once if needed
				if(((Flag&2)==2)&&!bUsePriceSensitiveDemand) {
					if(!bPriceSensitiveDemandFound) {
						strMessage+=name+"'s price sensitive demand is not found!\n";
					} else {
					}

					bUsePriceSensitiveDemand=true;
				}
			}
		}

		for (int k=0; k<this.lsePriceSensitiveDemandList.size(); k++) {
			Object [][] tempPrice=(Object [][])this.lsePriceSensitiveDemandList.get(k);
			name=tempPrice[0][0].toString();
			for(int h=0; h<24; h++) {
				double c=Support.parseDouble(tempPrice[h][4].toString());
				double d=Support.parseDouble(tempPrice[h][5].toString());
				double slMax=Support.parseDouble(tempPrice[h][6].toString());

				if(d<0.0) {
					strMessage+=name+"'s at hour "+ h+" in d column is not bigger than 0.0\n";
				}

				if(slMax<0.0) {
					strMessage+=name+"'s at hour "+ h+" in SLMax column is not bigger than 0.0\n";
				}

				if((c-(d*2.0*slMax))<0.0) {
					strMessage+=name+"'s at hour "+ h+" c-d*2.0*SLMax is not bigger than 0.0\n";
				}
			}
		}

		return strMessage;
	}

	/* (non-Javadoc)
	 * @see AMESGUIFrame.LSEDemandConfig#PriceCapVerify(double)
	 */
	@Override
	public String PriceCapVerify(double dLSEPriceCap) {
		String strMessage="";

		String name;
		for (int k=0; k<this.lsePriceSensitiveDemandList.size(); k++) {
			Object [][] tempPrice=(Object [][])this.lsePriceSensitiveDemandList.get(k);
			name=tempPrice[0][0].toString();
			for(int h=0; h<24; h++) {
				double c=Support.parseDouble(tempPrice[h][4].toString());
				double d=Support.parseDouble(tempPrice[h][5].toString());
				double slMax=Support.parseDouble(tempPrice[h][6].toString());

				if((c-(d*2.0*slMax))<dLSEPriceCap) {
					strMessage+=name+"'s at hour "+ h+ " c-d*2.0*SLMax is not bigger than "+dLSEPriceCap+"\n";
				}
			}
		}

		return strMessage;
	}

	private void DataVerifyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DataVerifyButtonActionPerformed
		String strErrorMessage=this.DataVerify();
		if(!strErrorMessage.isEmpty()) {
			JOptionPane.showMessageDialog(this, strErrorMessage, "Case Data Verification Message", JOptionPane.ERROR_MESSAGE);
		} else { //GEN-LAST:event_DataVerifyButtonActionPerformed
			String strMessage="Case data verify ok!";
			JOptionPane.showMessageDialog(this, strMessage, "Case Data Verification Message", JOptionPane.INFORMATION_MESSAGE);
		}

	}

	private void PrevActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_PrevActionPerformed
		this.setVisible(false);
		this.mainFrame.activeLearnOption1();

	}//GEN-LAST:event_PrevActionPerformed

	private void AddlButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AddlButtonActionPerformed
		String lseNewName="LSE00";

		for(int i=0; i<this.lseHybridDemandList.size(); i++) {
			Object [] temp=(Object [])this.lseHybridDemandList.get(i);
			if(lseNewName.equalsIgnoreCase(temp[0].toString())) {
				this.SelectLSE("LSE00");
				return;
			}
		}

		Object[] FixedDemandArray=new Object[27];
		FixedDemandArray[0]="LSE00";
		for(int i=1; i<27; i++) {
			FixedDemandArray[i]=0;
		}

		this.lseFixedDemandList.add(FixedDemandArray);

		Object[][] PriceDemandArray=new Object[24][7];
		for(int h=0; h<24; h++) {
			PriceDemandArray[h][0]="LSE00";
			for(int i=1; i<7; i++) {
				PriceDemandArray[h][i]=0;
			}
		}

		this.lsePriceSensitiveDemandList.add(PriceDemandArray);

		Object[] HybridDemandArray=new Object[27];
		HybridDemandArray[0]="LSE00";
		for(int i=1; i<27; i++) {
			HybridDemandArray[i]=0;
		}

		this.lseHybridDemandList.add(HybridDemandArray);

		this.LSENameComboBox.addItem("LSE00");
		this.SelectLSE("LSE00");

	}//GEN-LAST:event_AddlButtonActionPerformed

	private void DeletelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DeletelButtonActionPerformed
		int iSelectedIndex=this.LSENameComboBox.getSelectedIndex();
		String selectedName=(String)this.LSENameComboBox.getSelectedItem();
		if(selectedName.isEmpty()) {
			return;
		}

		this.LSENameComboBox.removeItem(selectedName);

		for(int i=0; i<this.lseHybridDemandList.size(); i++) {
			Object [] temp=(Object [])this.lseHybridDemandList.get(i);
			if(selectedName.equalsIgnoreCase(temp[0].toString())) {
				this.lseHybridDemandList.remove(i);
				break;
			}
		}


		for (int j=0; j<this.lseFixedDemandList.size(); j++) {
			Object [] tempFixed=(Object [])this.lseFixedDemandList.get(j);
			if(selectedName.equalsIgnoreCase(tempFixed[0].toString())) {
				this.lseFixedDemandList.remove(j);
				break;
			}
		}

		for (int j=0; j<this.lsePriceSensitiveDemandList.size(); j++) {
			Object [][] tempPrice=(Object [][])this.lsePriceSensitiveDemandList.get(j);
			if(selectedName.equalsIgnoreCase(tempPrice[0][0].toString())) {
				this.lsePriceSensitiveDemandList.remove(j);
				break;
			}
		}

		if(this.lseHybridDemandList.size()<1) {
			this.AddlButtonActionPerformed(evt);
		} else {
			if(iSelectedIndex<1) {
				iSelectedIndex=0;
			} else {
				iSelectedIndex--;
			}

			Object [] temp=(Object [])this.lseHybridDemandList.get(iSelectedIndex);
			this.lseName=temp[0].toString();
			this.SelectLSE(this.lseName);
		}


	}//GEN-LAST:event_DeletelButtonActionPerformed

	private void SelectLSE(String name) {
		this.LSENameComboBox.setSelectedItem(name);

		for(int i=0; i<this.lseHybridDemandList.size(); i++) {
			Object [] temp=(Object [])this.lseHybridDemandList.get(i);
			if(name.equalsIgnoreCase(temp[0].toString())) {
				this.iID=Integer.parseInt(temp[1].toString());
				this.iAtNode=Integer.parseInt(temp[2].toString());
				this.IDTextField.setText(String.valueOf(this.iID));
				this.AtNodeTextField.setText(String.valueOf(this.iAtNode));

				for(int j=0; j<24; j++) {
					int Flag=Integer.parseInt(temp[j+3].toString());
					this.lseDemandFlag[0][j]=false;
					this.lseDemandFlag[1][j]=false;

					if((Flag&1)==1) {
						this.lseDemandFlag[0][j]=true;
					}
					if((Flag&2)==2) {
						this.lseDemandFlag[1][j]=true;
					}
				}

				HybridTableModel hybridModel=new HybridTableModel();
				this.FlagTable.setModel(hybridModel);

				TableColumn column = null;
				this.FlagTable.setAutoscrolls(true);
				this.FlagTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
				for (int j=0; j<this.priceHybridNames.length; j++) {
					column=this.FlagTable.getColumnModel().getColumn(j);

					column.setPreferredWidth(60);
				}

				for (int j=0; j<this.lseFixedDemandList.size(); j++) {
					Object [] tempFixed=(Object [])this.lseFixedDemandList.get(j);
					if(name.equalsIgnoreCase(tempFixed[0].toString())) {
						this.iCurrentLSEFixedDemandIndex=j;
						Object [][] fixedDemand=new Object [1][24];
						for(int k=0; k<24; k++) {
							fixedDemand[0][k]=tempFixed[k+3];
						}

						DefaultTableModel loadDataModel = new DefaultTableModel(fixedDemand, this.names);
						this.FixedLoadTable.setModel(loadDataModel);

						this.FixedLoadTable.setAutoscrolls(true);
						this.FixedLoadTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
						for (int k=0; k<this.names.length; k++) {
							column =this.FixedLoadTable.getColumnModel().getColumn(k);

							column.setPreferredWidth(120);
						}
					}
				}

				for (int j=0; j<this.lsePriceSensitiveDemandList.size(); j++) {
					Object [][] tempPrice=(Object [][])this.lsePriceSensitiveDemandList.get(j);
					if(name.equalsIgnoreCase(tempPrice[0][0].toString())) {
						this.iCurrentLSEPriceDemandIndex=j;
						Object [][] priceDemand=new Object [24][4];
						for(int h=0; h<24; h++) {
							for(int k=0; k<4; k++) {
								priceDemand[h][k]=tempPrice[h][k+3];
							}
						}

						DefaultTableModel loadDataModel = new DefaultTableModel(priceDemand, this.priceSensitiveNames);
						this.PriceSensitiveTable.setModel(loadDataModel);

						this.PriceSensitiveTable.setAutoscrolls(true);
						this.PriceSensitiveTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

						column=this.PriceSensitiveTable.getColumnModel().getColumn(this.CONSTD_COLUMN_INDEX);
						column.setHeaderRenderer(this.iconHeaderRender);
						column.setHeaderValue(this.constdIcon);

						for (int k=0; k<this.priceSensitiveNames.length; k++) {
							column =this.PriceSensitiveTable.getColumnModel().getColumn(k);

							column.setPreferredWidth(120);
						}
					}
				}
			}
		}

		this.FlagTable.repaint();
		this.FixedLoadTable.repaint();
		this.PriceSensitiveTable.repaint();

	}


	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JButton AddlButton;
	private javax.swing.JTextField AtNodeTextField;
	private javax.swing.JButton CancelButton;
	private javax.swing.JButton DataVerifyButton;
	private javax.swing.JButton DeletelButton;
	private javax.swing.JTable FixedLoadTable;
	private javax.swing.JTable FlagTable;
	private javax.swing.JTextField IDTextField;
	private javax.swing.JComboBox LSENameComboBox;
	private javax.swing.JButton NextButton;
	private javax.swing.JButton Prev;
	private javax.swing.JTable PriceSensitiveTable;
	private javax.swing.JLabel jLabel1;
	private javax.swing.JLabel jLabel2;
	private javax.swing.JLabel jLabel3;
	private javax.swing.JLabel jLabel4;
	private javax.swing.JLabel jLabel5;
	private javax.swing.JLabel jLabel6;
	private javax.swing.JPanel jPanel1;
	private javax.swing.JPanel jPanel2;
	private javax.swing.JPanel jPanel3;
	private javax.swing.JPanel jPanel4;
	private javax.swing.JScrollPane jScrollPane1;
	private javax.swing.JScrollPane jScrollPane2;
	private javax.swing.JScrollPane jScrollPane3;
	// End of variables declaration//GEN-END:variables
	private Vector copyRowVector;
	private javax.swing.JPopupMenu popupMenuFixedLoadTable = new JPopupMenu();
	private javax.swing.JPopupMenu popupMenuPriceSensitiveTable = new JPopupMenu();
	private javax.swing.JPopupMenu popupMenuFlagTable = new JPopupMenu();
	private JMenuItem CheckAllFixedLoadItem, unCheckAllFixedLoadItem, CheckAllPriceSensitiveItem, unCheckAllPriceSensitiveItem;
	private JMenuItem copyRowFixedLoadItem, pasteRowFixedLoadItem;
	private JMenuItem copyRowPriceSensitiveItem, pasteRowPriceSensitiveItem;
	private boolean bDataLoad=false;

	private TableCellRenderer iconHeaderRender;
	private ImageIcon constdIcon;
	private final int CONSTD_COLUMN_INDEX=2;

	private ArrayList lseFixedDemandList = new ArrayList(); // LSEName, H-00, ... H-23
	private ArrayList lsePriceSensitiveDemandList = new ArrayList();    // LSEName, c, d, min, max
	private ArrayList lseHybridDemandList = new ArrayList();    // LSEName, ID, AtNode, H-00 Flag, ... H-23 Flag
	// 1 = bFixedDemand,
	// 2 = bPriceSensitiveDemand
	// 3 = bFixedDemand & bPriceSensitiveDemand

	//private Object[][] lseFixedDemand; // LSEName, H-00, ... H-23
	//private Object[][] lsePriceSensitiveDemand; // LSEName, c, d, min, max
	//private Object[][] lseHybridDemand;  // LSEName, ID, AtNode, H-00 Flag, ... H-23 Flag
	// 1 = bFixedDemand,
	// 2 = bPriceSensitiveDemand
	// 3 = bFixedDemand & bPriceSensitiveDemand

	private Object [][] lseDemandFlag=new Object[2][24];

	private int iCurrentLSEIndex;
	private int iCurrentLSEFixedDemandIndex;
	private int iCurrentLSEPriceDemandIndex;
	private int iID;
	private int iAtNode;
	private String lseName;

	private AMESFrame mainFrame;

	Object [][] dataLoad;
	Object [][] data = {
			{   "LSE1", 1, 2, 350, 322.93, 305.04, 296.02, 287.16, 291.59, 296.02, 314.07,
				358.86, 394.80, 403.82,	408.25,	403.82, 394.80,	390.37,	390.37,
				408.25,	448.62,	430.73,	426.14,	421.71,	412.69,	390.37,	363.46
			},
			{   "LSE2", 2, 3, 300, 276.80, 261.47, 253.73, 246.13, 249.93, 253.73, 269.20,
				307.60,	338.40,	346.13,	349.93,	346.13,	338.40,	334.60,	334.60,
				349.93,	384.53,	369.20,	365.26,	361.47,	353.73,	334.60,	311.53
			},
			{   "LSE3", 3, 4, 250, 230.66, 217.89, 211.44, 205.11, 208.28, 211.44,224.33,
				256.33,	282.00,	288.44,	291.61,	288.44,	282.00,	278.83,	278.83,
				291.61,	320.44,	307.67,	304.39,	301.22,	294.78,	278.83,	259.61
			}
	};

	final Object [] blankRowData = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

	final Object [][] blankData = { { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0} };
	final Object [] blankPriceSensitiveRowData = { 0, 0, 0};

	final Object [][] blankPriceSensitiveData = { { 0, 0, 0} };

	public String [] names =  {
			"H-00 (MW)", "H-01 (MW)", "H-02 (MW)", "H-03 (MW)", "H-04 (MW)", "H-05 (MW)",
			"H-06 (MW)", "H-07 (MW)", "H-08 (MW)", "H-09 (MW)", "H-10 (MW)", "H-11 (MW)",
			"H-12 (MW)", "H-13 (MW)", "H-14 (MW)", "H-15 (MW)", "H-16 (MW)", "H-17 (MW)",
			"H-18 (MW)", "H-19 (MW)", "H-20 (MW)", "H-21 (MW)", "H-22 (MW)", "H-23 (MW)"
	};

	public String [] priceSensitiveNames =  {
			"Hour Index", "c ($/MWh)", "d ($/MW2h)", "SLMax (MW)"
	};

	public String [] priceHybridNames =  {
			"FLAG-00", "FLAG-01", "FLAG-02", "FLAG-03", "FLAG-04", "FLAG-05",
			"FLAG-06", "FLAG-07", "FLAG-08", "FLAG-09", "FLAG-10", "FLAG-11",
			"FLAG-12", "FLAG-13", "FLAG-14", "FLAG-15", "FLAG-16", "FLAG-17",
			"FLAG-18", "FLAG-19", "FLAG-20", "FLAG-21", "FLAG-22", "FLAG-23"
	};

}
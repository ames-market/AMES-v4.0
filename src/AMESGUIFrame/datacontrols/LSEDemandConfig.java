/*
 * FIXME: LICENCE
 */
package AMESGUIFrame.datacontrols;

import javax.swing.JFrame;

/**
 * An inteface for a GUI element designed to configure the LSEDemandData.
 * @author Sean L. Mooney
 *
 */
public abstract class LSEDemandConfig extends JFrame {

    public abstract void loadBlankData();

    public abstract void loadData(Object[][] lseFixedData,
            Object[][][] lsePriceSensitiveData, Object[][] lseHybridData);

    public abstract void addRowsBlankData(int iRow);

    public abstract void saveTableDataToList();

    public abstract void saveData();

    public abstract String DataVerify();

    public abstract String PriceCapVerify(double dLSEPriceCap);

}

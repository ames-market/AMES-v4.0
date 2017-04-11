/*
 * FIXME: LICENCE
 */
package AMESGUIFrame.datacontrols;

import amesmarket.AMESMarketException;

/**
 * Type to model a problem building on of the GUI Wizard elements.
 *
 * @author Sean L. Mooney
 *
 */
public class WizardBuilderException extends AMESMarketException {
    /**
     *
     */
    private static final long serialVersionUID = 2543603327904621482L;
    public WizardBuilderException(){super();}
    public WizardBuilderException(String msg){super(msg);}
}

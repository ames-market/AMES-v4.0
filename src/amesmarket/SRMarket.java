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

// SRMarket.java
// Supply re-offer market

package amesmarket;

/**
 * Supply re-offer market.
 *
 */
public class SRMarket {

    //Supply re-offer market's data
    private AMESMarket ames;

    // constructor
    public SRMarket(AMESMarket model) {
        //System.out.println("Created an SRMarket objecct");
        ames = model;

    }

    public void supplyReOfferOperation(int h, int d, int m) {
        //System.out.println("Hour " + h + " Day " + d + " Month " + m +
        //                 ": Supply Re-Offer Market operation.");
    }

}

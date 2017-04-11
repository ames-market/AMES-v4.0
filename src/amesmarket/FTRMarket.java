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

// FTRMarket.java
// Financial transmission rights market

package amesmarket;

/**
 * Financial transmission rights market.
 *
 */
public class FTRMarket {

    // FTR market's data
    private AMESMarket ames;

    // constructor
    public FTRMarket(AMESMarket model) {
        ames = model;

    }


    public void FTRAuction(int m) {
        System.out.println("This is FTR Market Auction for Month " + m);
        System.out.println();
    }

}

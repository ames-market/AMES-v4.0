/* ============================================================================
 * AMES Wholesale Power Market Test Bed (Java): A Free Open-Source Test-Bed
 *         for the Agent-based Modeling of Electricity Systems
 * ============================================================================
 *
 * (C) Copyright 2008, by Hongyan Li, Junjie Sun, and Leigh Tesfatsion
 *
 *    Homepage: http://www.econ.iastate.edu/tesfatsi/testMarketHome.htm
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
package testsupport;

import java.io.File;

/**
 * Defines the test case parameters for an existing case file.
 *
 * @author Dheepak Krishnamurthy
 *
 */
public class Test_TestCaseParameters {

    public Test_TestCaseParameters(String testCaseFile) {
        this(new File(testCaseFile));
    }

    /**
     * @param testCaseFile
     * @param testCaseOutput
     * @param testCaseExpected
     */
    public Test_TestCaseParameters(File testCaseFile) {
        this.testCaseFile = testCaseFile;
    }

    /**
     * @return the testCaseFile
     */
    public File getTestCaseFile() {
        return testCaseFile;
    }

    /**
     * The test case input file.
     */
    private final File testCaseFile;

}

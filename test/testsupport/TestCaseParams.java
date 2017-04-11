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
 * @author Sean L. Mooney
 *
 */
public class TestCaseParams {

    public TestCaseParams(String testCaseFile, String testCaseOutput,
            String testCaseExpected) {
        this(new File(testCaseFile), new File(testCaseOutput),
                new File(testCaseExpected));
    }

    /**
     * @param testCaseFile
     * @param testCaseOutput
     * @param testCaseExpected
     */
    public TestCaseParams(File testCaseFile, File testCaseOutput,
            File testCaseExpected) {
        this.testCaseFile = testCaseFile;
        this.testCaseOutput = testCaseOutput;
        this.testCaseExpected = testCaseExpected;
    }

    /**
     * @return the testCaseFile
     */
    public File getTestCaseFile() {
        return testCaseFile;
    }

    /**
     * @return the testCaseOutput
     */
    public File getTestCaseOutput() {
        return testCaseOutput;
    }

    /**
     * @return the testCaseExpected
     */
    public File getTestCaseExpected() {
        return testCaseExpected;
    }

    /**
     * The test case input file.
     */
    private final File testCaseFile;
    /**
     * The output file for the test case.
     */
    private final File testCaseOutput;
    /**
     * File with the 'expected' results of the test case.
     */
    private final File testCaseExpected;

}

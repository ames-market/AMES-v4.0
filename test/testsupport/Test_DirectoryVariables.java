//FIXME-XXX: LICENSE
package testsupport;

import java.io.File;

/**
 * Contains directory or file paths to files required for various tests 
 *  
 * @author Dheepak Krishnamurthy
 *
 */
public class Test_DirectoryVariables {
    /**
     * Folder where the test files are stored
     */
    public static final File TEST_FILE_DIR = new File("TEST-DATA");

    public static final File TEST_TWO_TEST_CASES_DIR = new File(TEST_FILE_DIR, "Test_TwoTestCases");

    public static final File TEST_TWO_LOAD_CASES_DIR = new File(TEST_FILE_DIR, "Test_TwoWindAndLoadCases");

}

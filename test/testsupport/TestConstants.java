//FIXME-XXX: LICENSE
package testsupport;

import java.io.File;

/**
 * Constant test parameters. Used for definition of items such as
 * test file locations.
 *
 * @author Sean L. Mooney
 *
 */
public class TestConstants {
    /**
     * Folder where the test files are stored
     */
    public static final File TEST_FILE_DIR = new File("TEST-DATA");

    /**
     * A LoadCase control file.
     */
    public static final File SIMPLE_CTRL_FILE = new File(TEST_FILE_DIR, "SimpleControlFile.dat");

    /**
     * A LoadScenario for parsing tests
     */
    public static final File LOAD_SCEN_SAMPLE = new File(TEST_FILE_DIR, "Load_scenario_sample.dat");

    public static final File IND_LOAD_PROF_DIR = new File(TEST_FILE_DIR, "singledayfiles");

    public static final File IND_LOAD_PROF_CONTROL_FILE = new File(IND_LOAD_PROF_DIR, "SimpleControlFile.dat");

    public static final File COMP_EXP_LOAD_CONTROL = new File(TEST_FILE_DIR, "ExpectedTrueLoadExample/ComputeExpectedControl.dat");
    
    /**
     * Directory for test control files with 'aux' control files
     */
    public static final File AUX_CONTROL_DIR = new File(TEST_FILE_DIR, "aux_control_ex");

    /**
     * First master control used to test reading aux control files.
     */
    public static final File AUX_CONTROL_MASTER1 = new File(AUX_CONTROL_DIR, "aux_control1/master_control_file.dat");

    /**
     * Master control file associated with the 8Zone, 538gen TestCase
     */
    public static final File EIGHT_ZONE_538GEN_MASTER = new File(TestConstants.TEST_FILE_DIR,
            "8busNgentestcase/loadcase/master_control_file.dat");

    /**
     * Delta for comparing two doubles. If the absolute value of the difference
     * is than less than this value, consider them the same.
     */
    public static final double DOUBLE_EQ = 1.0E-13;


}

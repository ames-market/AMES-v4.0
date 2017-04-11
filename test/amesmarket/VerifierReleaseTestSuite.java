//TODO: LICENCE
package amesmarket;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Test that must pass before releasing a new version of the load case verifier.
 * @author Sean L. Mooney
 *
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    ConfigFileReaderTest.class,
    LoadScenarioTest.class
})
public class VerifierReleaseTestSuite {}

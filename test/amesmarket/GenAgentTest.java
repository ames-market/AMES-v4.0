/*
 * FIXME: LICENSE
 */

package amesmarket;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Test;

import amesmarket.GenAgent;
import testsupport.AbstractTestAMES;


/**
 * @author Sean L. Mooney
 */
public class GenAgentTest extends AbstractTestAMES {

    @Test
    public void testUnitOnT0State() {
        GenAgent ga = new TestableGenAgent();

        ga.addCommitmentForDay(1, new int[]{0, 0, 0, 0});
        assertEquals(-5, ga.getUnitOnT0State(1));

        ga.addCommitmentForDay(1, new int[]{0, 0, 0, 1});
        assertEquals(2, ga.getUnitOnT0State(1));

        ga.addCommitmentForDay(1, new int[]{0, 0, 1, 1});
        assertEquals(3, ga.getUnitOnT0State(1));

        ga.addCommitmentForDay(1, new int[]{1, 1, 0, 0});
        assertEquals(-3, ga.getUnitOnT0State(1));

        ga.addCommitmentForDay(1, new int[]{1, 0, 0, 1});
        assertEquals(2, ga.getUnitOnT0State(1));
    }

    /**
     * Simple to construct GenAgent.
     */
    static class TestableGenAgent extends GenAgent{
        /**
         * Simple constructor. Doesn't
         * call super() because we do not have all of the data that constructor requires.
         */
        public TestableGenAgent() {
            super(new double[]{1, 1, 0, 0, 0, 0, 100, 10000},
                    null,
                    new ArrayList<double[]>(), 0.0, 0.0, 0, 0, 0, 0.0, false,
                    0, 0, 0.0, false, 0.0, false,
                    0, 0, 0, false, 1
                    );
        }
    }
}

package org.apache.poi.ss.formula.functions;

import org.apache.poi.ss.formula.eval.ValueEval;

/**
 * Interface specifying how an algorithm to be used by {@link DStarRunner} should look like.
 * Each implementing class should correspond to one of the D* functions.
 */
public interface IDStarAlgorithm {
    /**
     * Reset the state of this algorithm.
     * This is called before each run through a database.
     */
    void reset();
    /**
     * Process a match that is found during a run through a database.
     * @param eval ValueEval of the cell in the matching row. References will already be resolved.
     * @return Whether we should continue iterating through the database.
     */
    boolean processMatch(ValueEval eval);
    /**
     * Return a result ValueEval that will be the result of the calculation.
     * This is always called at the end of a run through the database.
     * @return a ValueEval
     */
    ValueEval getResult();
}

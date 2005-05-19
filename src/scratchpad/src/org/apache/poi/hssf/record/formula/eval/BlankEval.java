/*
 * Created on May 9, 2005
 *
 */
package org.apache.poi.hssf.record.formula.eval;

/**
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt; This class is a
 *         marker class. It is a special value for empty cells.
 */
public class BlankEval implements ValueEval {

    public static BlankEval INSTANCE = new BlankEval();

    private BlankEval() {
    }
}

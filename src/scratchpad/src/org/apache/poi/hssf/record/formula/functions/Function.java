/*
 * Created on May 9, 2005
 *
 */
package org.apache.poi.hssf.record.formula.functions;

import org.apache.poi.hssf.record.formula.eval.Eval;

/**
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 * Function serves as a marker interface.
 */
public interface Function {

    public Eval evaluate(Eval[] evals, int srcCellRow, short srcCellCol);

}

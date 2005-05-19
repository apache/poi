/*
 * Created on May 8, 2005
 *
 */
package org.apache.poi.hssf.record.formula.eval;

import org.apache.poi.hssf.record.formula.Ptg;
import org.apache.poi.hssf.record.formula.StringPtg;

/**
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 *  
 */
public class StringEval implements StringValueEval {

    private String value;

    public StringEval(Ptg ptg) {
        this.value = ((StringPtg) ptg).getValue();
    }

    public StringEval(String value) {
        this.value = value;
    }

    public String getStringValue() {
        return value;
    }
}

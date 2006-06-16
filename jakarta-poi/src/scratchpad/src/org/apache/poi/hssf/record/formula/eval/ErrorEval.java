/*
 * Created on May 8, 2005
 *
 */
package org.apache.poi.hssf.record.formula.eval;

/**
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 * 
 */
public class ErrorEval implements ValueEval {

    private int errorCode;


    public static final ErrorEval NAME_INVALID = new ErrorEval(525);

    public static final ErrorEval VALUE_INVALID = new ErrorEval(519);

    
    // Non std error codes
    public static final ErrorEval UNKNOWN_ERROR = new ErrorEval(-20);

    public static final ErrorEval FUNCTION_NOT_IMPLEMENTED = new ErrorEval(-30);

    public static final ErrorEval REF_INVALID = new ErrorEval(-40);

    public static final ErrorEval NA = new ErrorEval(-50);
    
    public static final ErrorEval CIRCULAR_REF_ERROR = new ErrorEval(-60);
    
    public static final ErrorEval DIV_ZERO = new ErrorEval(-70);
    
    public static final ErrorEval NUM_ERROR = new ErrorEval(-80);

    private ErrorEval(int errorCode) {
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public String getStringValue() {
        return "Err:" + Integer.toString(errorCode);
    }

}

/*
 * Created on May 14, 2005
 *
 */
package org.apache.poi.hssf.record.formula.eval;

/**
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 *
 */
public class ValueEvalToNumericXlator {

    public static final short STRING_IS_PARSED = 0x0001;
    public static final short BOOL_IS_PARSED = 0x0002;
    
    public static final short REF_STRING_IS_PARSED = 0x0004;
    public static final short REF_BOOL_IS_PARSED = 0x0008;
    
    public static final short EVALUATED_REF_STRING_IS_PARSED = 0x0010;
    public static final short EVALUATED_REF_BOOL_IS_PARSED = 0x0020;
    
    public static final short STRING_TO_BOOL_IS_PARSED = 0x0040;
    public static final short REF_STRING_TO_BOOL_IS_PARSED = 0x0080;
    
    public static final short STRING_IS_INVALID_VALUE = 0x0100;
    public static final short REF_STRING_IS_INVALID_VALUE = 0x200;
    
    private final short flags;
    
    
    public ValueEvalToNumericXlator(short flags) {
        this.flags = flags;
    }
    
    /**
     * returned value can be either A NumericValueEval, BlankEval or ErrorEval.
     * The params can be either NumberEval, BoolEval, StringEval, or
     * RefEval
     * @param eval
     * @return
     */
    public ValueEval attemptXlateToNumeric(ValueEval eval) {
        ValueEval retval = null;
        
        if (eval == null) {
            retval = BlankEval.INSTANCE;
        }
        
        // most common case - least worries :)
        else if (eval instanceof NumberEval) {
            retval = (NumberEval) eval; 
        }
        
        // booleval
        else if (((flags | BOOL_IS_PARSED) > 0) && eval instanceof BoolEval) {
            retval = (NumericValueEval) eval;
        } 
        
        // stringeval 
        else if (eval instanceof StringEval) {
            retval = handleStringEval((StringEval) eval);
        }
        
        // refeval
        else if (eval instanceof RefEval) {
            retval = handleRefEval((RefEval) eval);
        }
        
        //blankeval
        else if (eval instanceof BlankEval) {
            retval = eval;
        }
        
        // erroreval
        else if (eval instanceof ErrorEval) {
            retval = eval;
        }
        
        // probably AreaEval? then not acceptable.
        else {
            throw new RuntimeException("Invalid ValueEval type passed for conversion: " + eval.getClass());
        }
        return retval;
    }
    
    /**
     * uses the relevant flags to decode the supplied RefVal
     * @param eval
     * @return
     */
    private ValueEval handleRefEval(RefEval reval) {
        ValueEval retval = null;
        ValueEval eval = (ValueEval) reval.getInnerValueEval();
        
        // most common case - least worries :)
        if (eval instanceof NumberEval) {
            retval = (NumberEval) eval; // the cast is correct :)
        }
        
        // booleval
        else if (((flags | REF_BOOL_IS_PARSED) > 0) && eval instanceof BoolEval) {
            retval = (NumericValueEval) eval;
        } 
        
        // stringeval 
        else if (eval instanceof StringEval) {
            retval = handleRefStringEval((StringEval) eval);
        }
        
        //blankeval
        else if (eval instanceof BlankEval) {
            retval = eval;
        }
        
        // erroreval
        else if (eval instanceof ErrorEval) {
            retval = eval;
        }
        
        // probably AreaEval or another RefEval? then not acceptable.
        else {
            throw new RuntimeException("Invalid ValueEval type passed for conversion: " + eval.getClass());
        }
        return retval;
    }
    
    /**
     * uses the relevant flags to decode the StringEval
     * @param eval
     * @return
     */
    private ValueEval handleStringEval(StringEval eval) {
        ValueEval retval = null;
        if ((flags | STRING_IS_PARSED) > 0) {
            StringEval sve = (StringEval) eval;
            String s = sve.getStringValue();
            try { 
                double d = Double.parseDouble(s);
                retval = new NumberEval(d);
            } 
            catch (Exception e) { retval = ErrorEval.VALUE_INVALID; }
        }
        else if ((flags | STRING_TO_BOOL_IS_PARSED) > 0) {
            StringEval sve = (StringEval) eval;
            String s = sve.getStringValue();
            try { 
                boolean b = Boolean.getBoolean(s);
                retval = b ? BoolEval.TRUE : BoolEval.FALSE;
            } 
            catch (Exception e) { retval = ErrorEval.VALUE_INVALID; }
        }
        
        // strings are errors?
        else if ((flags | STRING_IS_INVALID_VALUE) > 0) {
            retval = ErrorEval.VALUE_INVALID;
        }
        
        // ignore strings
        else {
            retval = BlankEval.INSTANCE;
        }
        return retval;
    }
    
    /**
     * uses the relevant flags to decode the StringEval
     * @param eval
     * @return
     */
    private ValueEval handleRefStringEval(StringEval eval) {
        ValueEval retval = null;
        if ((flags | REF_STRING_IS_PARSED) > 0) {
            StringEval sve = (StringEval) eval;
            String s = sve.getStringValue();
            try { 
                double d = Double.parseDouble(s);
                retval = new NumberEval(d);
            } 
            catch (Exception e) { retval = ErrorEval.VALUE_INVALID; }
        }
        else if ((flags | REF_STRING_TO_BOOL_IS_PARSED) > 0) {
            StringEval sve = (StringEval) eval;
            String s = sve.getStringValue();
            try { 
                boolean b = Boolean.getBoolean(s);
                retval = retval = b ? BoolEval.TRUE : BoolEval.FALSE;;
            } 
            catch (Exception e) { retval = ErrorEval.VALUE_INVALID; }
        }
        
        // strings are errors?
        else if ((flags | REF_STRING_IS_INVALID_VALUE) > 0) {
            retval = ErrorEval.VALUE_INVALID;
        }
        
        // ignore strings
        else {
            retval = BlankEval.INSTANCE;
        }
        return retval;
    }
    
}

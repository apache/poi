package org.apache.poi.ss.formula.functions;

import org.apache.poi.ss.formula.eval.*;

/**
 * Implementation for Excel CODE () function.<p/>
 * <p/>
 * <b>Syntax</b>:<br/> <b>CODE   </b>(<b>text</b> )<br/>
 * <p/>
 * Returns a numeric code for the first character in a text string. The returned code corresponds to the character set used by your computer.
 * <p/>
 * text The text for which you want the code of the first character.
 *
 * @author cedric dot walter @ gmail dot com
 */
public class Code extends Fixed1ArgFunction {

    public ValueEval evaluate(int srcRowIndex, int srcColumnIndex, ValueEval textArg) {

        ValueEval veText1;
        try {
            veText1 = OperandResolver.getSingleValue(textArg, srcRowIndex, srcColumnIndex);
        } catch (EvaluationException e) {
            return e.getErrorEval();
        }
        String text = OperandResolver.coerceValueToString(veText1);

        if (text.length() == 0) {
            return ErrorEval.VALUE_INVALID;
        }

        int code = (int)text.charAt(0);

        return new StringEval(String.valueOf(code));
    }
}


package org.apache.poi.ss.formula.functions;

import org.apache.poi.ss.formula.OperationEvaluationContext;
import org.apache.poi.ss.formula.eval.*;

/**
 * Implementation for Excel Bin2Dec() function.<p/>
 * <p/>
 * <b>Syntax</b>:<br/> <b>Bin2Dec  </b>(<b>number</b>)<br/>
 * <p/>
 * Converts a binary number to decimal.
 * <p/>
 * Number is the binary number you want to convert. Number cannot contain more than 10 characters (10 bits).
 * The most significant bit of number is the sign bit. The remaining 9 bits are magnitude bits.
 * Negative numbers are represented using two's-complement notation.
 * <p/>
 * Remark
 * If number is not a valid binary number, or if number contains more than 10 characters (10 bits),
 * BIN2DEC returns the #NUM! error value.
 *
 * @author cedric dot walter @ gmail dot com
 */
public class Bin2Dec extends Fixed1ArgFunction implements FreeRefFunction {

    public static final FreeRefFunction instance = new Bin2Dec();

    public ValueEval evaluate(int srcRowIndex, int srcColumnIndex, ValueEval numberVE) {
        String number = OperandResolver.coerceValueToString(numberVE);
        if (number.length() > 10) {
            return ErrorEval.NUM_ERROR;
        }

        String unsigned;

        //If the leftmost bit is 0 -- number is positive.
        boolean isPositive;
        if (number.length() < 10) {
            unsigned = number;
            isPositive = true;
        } else {
            unsigned = number.substring(1);
            isPositive = number.startsWith("0");
        }

        String value;
        int sum;
        if (isPositive) {
            //bit9*2^8 + bit8*2^7 + bit7*2^6 + bit6*2^5 + bit5*2^4+ bit3*2^2+ bit2*2^1+ bit1*2^0
            sum = getDecimalValue(unsigned);
            value = String.valueOf(sum);
        } else {
            //The leftmost bit is 1 -- this is negative number
            //Inverse bits [1-9]
            String inverted = toggleBits(unsigned);
            // Calculate decimal number
            sum = getDecimalValue(inverted);

            //Add 1 to obtained number
            sum++;

            value = "-" + String.valueOf(sum);
        }

        return new NumberEval(Long.parseLong(value));
    }

    private int getDecimalValue(String unsigned) {
        int sum = 0;
        int numBits = unsigned.length();
        int power = numBits - 1;

        for (int i = 0; i < numBits; i++) {
            int bit = Integer.parseInt(unsigned.substring(i, i + 1));
            int term = (int) (bit * Math.pow(2, power));
            sum += term;
            power--;
        }
        return sum;
    }

    private static String toggleBits(String s) {
        long i = Long.parseLong(s, 2);
        long i2 = i ^ ((1L << s.length()) - 1);
        String s2 = Long.toBinaryString(i2);
        while (s2.length() < s.length()) s2 = '0' + s2;
        return s2;
    }

    public ValueEval evaluate(ValueEval[] args, OperationEvaluationContext ec) {
        if (args.length != 1) {
            return ErrorEval.VALUE_INVALID;
        }
        return evaluate(ec.getRowIndex(), ec.getColumnIndex(), args[0]);
    }
}

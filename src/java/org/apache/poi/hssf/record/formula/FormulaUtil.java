
/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache POI" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache POI", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

/*
 * FormulaUtil.java
 *
 * Created on November 4, 2001, 5:50 AM
 */
package org.apache.poi.hssf.record.formula;

/**
 *
 * @author  andy
 */

public class FormulaUtil
{

    /** Creates new FormulaUtil */

    public FormulaUtil()
    {
    }

    public static Ptg [] parseFormula(String formula)
    {
        Ptg[]        ptg = null;
        StringBuffer f   = new StringBuffer(formula);

        if (isIntAddition(formula))
        {
            int loc = getLoc(formula, '+');

            System.out.println(formula.substring(0, loc).trim() + ","
                               + formula.substring(loc + 1,
                                                   formula.length()).trim());
            ptg = formulaAddTwoInts(Short
                .parseShort(formula.substring(0, loc).trim()), Short
                .parseShort(formula.substring(loc + 1, formula.length())
                    .trim()));
        }
        else if (isIntSubtraction(formula))
        {
            int loc = getLoc(formula, '-');

            ptg = formulaSubtractTwoInts(Short
                .parseShort(formula.substring(0, loc).trim()), Short
                .parseShort(formula.substring(loc + 1, formula.length())
                    .trim()));
        }
        else if (isIntMultiplication(formula))
        {
            int loc = getLoc(formula, '*');

            ptg = formulaMultiplyTwoInts(Short
                .parseShort(formula.substring(0, loc).trim()), Short
                .parseShort(formula.substring(loc + 1, formula.length())
                    .trim()));
        }
        else if (isIntDivision(formula))
        {
            int loc = getLoc(formula, '/');

            ptg = formulaDivideTwoInts(Short
                .parseShort(formula.substring(0, loc).trim()), Short
                .parseShort(formula.substring(loc + 1, formula.length())
                    .trim()));
        }
        else if (isIntPower(formula))
        {
            int loc = getLoc(formula, '^');

            ptg = formulaPowerTwoInts(Short
                .parseShort(formula.substring(0, loc).trim()), Short
                .parseShort(formula.substring(loc + 1, formula.length())
                    .trim()));
        }
        return ptg;
    }

    public static Ptg [] formulaAddTwoInts(short first, short second)
    {
        Ptg[] ptg = new Ptg[ 3 ];

        ptg[ 0 ] = createInteger(first);
        ptg[ 1 ] = createInteger(second);
        ptg[ 2 ] = createAdd();
        return ptg;
    }

    public static Ptg [] formulaSubtractTwoInts(short first, short second)
    {
        Ptg[] ptg = new Ptg[ 3 ];

        ptg[ 0 ] = createInteger(first);
        ptg[ 1 ] = createInteger(second);
        ptg[ 2 ] = createSubtract();
        return ptg;
    }

    public static Ptg [] formulaMultiplyTwoInts(short first, short second)
    {
        Ptg[] ptg = new Ptg[ 3 ];

        ptg[ 0 ] = createInteger(first);
        ptg[ 1 ] = createInteger(second);
        ptg[ 2 ] = createMultiply();
        return ptg;
    }

    public static Ptg [] formulaPowerTwoInts(short first, short second)
    {
        Ptg[] ptg = new Ptg[ 3 ];

        ptg[ 0 ] = createInteger(second);
        ptg[ 1 ] = createInteger(first);
        ptg[ 2 ] = createPower();
        return ptg;
    }

    public static Ptg [] formulaDivideTwoInts(short first, short second)
    {
        Ptg[] ptg = new Ptg[ 3 ];

        ptg[ 0 ] = createInteger(first);
        ptg[ 1 ] = createInteger(second);
        ptg[ 2 ] = createDivide();
        return ptg;
    }

    public static Ptg createInteger(short value)
    {
        IntPtg ptg = new IntPtg();

        ptg.setValue(value);
        return ptg;
    }

    public static Ptg createAdd()
    {
        AddPtg ptg = new AddPtg();

        return ptg;
    }

    public static Ptg createSubtract()
    {
        SubtractPtg ptg = new SubtractPtg();

        return ptg;
    }

    public static Ptg createMultiply()
    {
        MultiplyPtg ptg = new MultiplyPtg();

        return ptg;
    }

    public static Ptg createDivide()
    {
        DividePtg ptg = new DividePtg();

        return ptg;
    }

    public static Ptg createPower()
    {
        PowerPtg ptg = new PowerPtg();

        return ptg;
    }

    private static boolean isIntAddition(String formula)
    {
        StringBuffer buffer = new StringBuffer(formula);

        if (instr(formula, "+"))
        {
            return true;
        }
        return false;
    }

    private static boolean isIntSubtraction(String formula)
    {
        StringBuffer buffer = new StringBuffer(formula);

        if (instr(formula, "-"))
        {
            return true;
        }
        return false;
    }

    private static boolean isIntMultiplication(String formula)
    {
        StringBuffer buffer = new StringBuffer(formula);

        if (instr(formula, "*"))
        {
            return true;
        }
        return false;
    }

    private static boolean isIntDivision(String formula)
    {
        StringBuffer buffer = new StringBuffer(formula);

        if (instr(formula, "/"))
        {
            return true;
        }
        return false;
    }

    private static boolean isIntPower(String formula)
    {
        StringBuffer buffer = new StringBuffer(formula);

        if (instr(formula, "^"))
        {
            return true;
        }
        return false;
    }

    private static boolean instr(String matchin, String matchon)
    {
        int lenmatchin = matchin.length();
        int lenmatchon = matchon.length();
        int pos        = 0;

        if (lenmatchon > lenmatchin)
        {
            return false;
        }
        while (pos + lenmatchon < lenmatchin)
        {
            String sub = matchin.substring(pos, pos + lenmatchon);

            if (sub.equals(matchon))
            {
                return true;
            }
            pos++;
        }
        return false;
    }

    private static int getLoc(String matchin, char matchon)
    {
        int retval = -1;

        for (int pos = 0; pos < matchin.length(); pos++)
        {
            if (matchin.charAt(pos) == matchon)
            {
                retval = pos;
                break;
            }
        }
        return retval;
    }
}

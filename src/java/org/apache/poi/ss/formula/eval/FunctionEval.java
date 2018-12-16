/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package org.apache.poi.ss.formula.eval;

import java.util.Collection;
import java.util.Collections;
import java.util.TreeSet;

import org.apache.poi.ss.formula.atp.AnalysisToolPak;
import org.apache.poi.ss.formula.function.FunctionMetadata;
import org.apache.poi.ss.formula.function.FunctionMetadataRegistry;
import org.apache.poi.ss.formula.functions.*;

/**
 * Mappings from the Excel functions to our evaluation implementations
 *  (where available)
 */
public final class FunctionEval {
    private FunctionEval() {
        // no instances of this class
    }

    /**
     * Some function IDs that require special treatment
     */
    private static final class FunctionID {
        /** 1 */
        public static final int IF = FunctionMetadataRegistry.FUNCTION_INDEX_IF;
        /** 4 */
        public static final int SUM = FunctionMetadataRegistry.FUNCTION_INDEX_SUM;
        /** 78 */
        public static final int OFFSET = 78;
        /** 100 */
        public static final int CHOOSE = FunctionMetadataRegistry.FUNCTION_INDEX_CHOOSE;
        /** 148 */
        public static final int INDIRECT = FunctionMetadataRegistry.FUNCTION_INDEX_INDIRECT;
        /** 255 */
        public static final int EXTERNAL_FUNC = FunctionMetadataRegistry.FUNCTION_INDEX_EXTERNAL;
    }

    /**
     * Array elements corresponding to unimplemented functions are <code>null</code>
     */
    protected static final Function[] functions = produceFunctions();

    /**
     * See <a href="https://www.openoffice.org/sc/excelfileformat.pdf">Apache Open Office Excel File Format,
     * Section 3.11 Built-In Sheet Functions</a>
     */
    private static Function[] produceFunctions() {
        Function[] retval = new Function[368];

        retval[0] = new Count();
        retval[FunctionID.IF] = new IfFunc(); //nominally 1
        retval[2] = LogicalFunction.ISNA;
        retval[3] = LogicalFunction.ISERROR;
        retval[FunctionID.SUM] = AggregateFunction.SUM; //nominally 4
        retval[5] = AggregateFunction.AVERAGE;
        retval[6] = AggregateFunction.MIN;
        retval[7] = AggregateFunction.MAX;
        retval[8] = new RowFunc(); // ROW
        retval[9] = new Column();
        retval[10] = new Na();
        retval[11] = new Npv();
        retval[12] = AggregateFunction.STDEV;
        retval[13] = NumericFunction.DOLLAR;
        retval[14] = new Fixed();
        retval[15] = NumericFunction.SIN;
        retval[16] = NumericFunction.COS;
        retval[17] = NumericFunction.TAN;
        retval[18] = NumericFunction.ATAN;
        retval[19] = NumericFunction.PI;
        retval[20] = NumericFunction.SQRT;
        retval[21] = NumericFunction.EXP;
        retval[22] = NumericFunction.LN;
        retval[23] = NumericFunction.LOG10;
        retval[24] = NumericFunction.ABS;
        retval[25] = NumericFunction.INT;
        retval[26] = NumericFunction.SIGN;
        retval[27] = NumericFunction.ROUND;
        retval[28] = new Lookup();
        retval[29] = new Index();
        retval[30] = new Rept();
        retval[31] = TextFunction.MID;
        retval[32] = TextFunction.LEN;
        retval[33] = new Value();
        retval[34] = BooleanFunction.TRUE;
        retval[35] = BooleanFunction.FALSE;
        retval[36] = BooleanFunction.AND;
        retval[37] = BooleanFunction.OR;
        retval[38] = BooleanFunction.NOT;
        retval[39] = NumericFunction.MOD;
        // 40: DCOUNT
        retval[41] = new DStarRunner(DStarRunner.DStarAlgorithmEnum.DSUM);
        // 42: DAVERAGE
        retval[43] = new DStarRunner(DStarRunner.DStarAlgorithmEnum.DMIN);
        retval[44] = new DStarRunner(DStarRunner.DStarAlgorithmEnum.DMAX);
        // 45: DSTDEV
        retval[46] = AggregateFunction.VAR;
        // 47: DVAR
        retval[48] = TextFunction.TEXT;
        // 49: LINEST
        retval[50] = new Trend();
        // 51: LOGEST
        // 52: GROWTH

        retval[56] = FinanceFunction.PV;
        retval[57] = FinanceFunction.FV;
        retval[58] = FinanceFunction.NPER;
        retval[59] = FinanceFunction.PMT;
        retval[60] = new Rate();
        retval[61] = new Mirr();
        retval[62] = new Irr();
        retval[63] = NumericFunction.RAND;
        retval[64] = new Match();
        retval[65] = DateFunc.instance;
        retval[66] = new TimeFunc();
        retval[67] = CalendarFieldFunction.DAY;
        retval[68] = CalendarFieldFunction.MONTH;
        retval[69] = CalendarFieldFunction.YEAR;
        retval[70] = WeekdayFunc.instance;
        retval[71] = CalendarFieldFunction.HOUR;
        retval[72] = CalendarFieldFunction.MINUTE;
        retval[73] = CalendarFieldFunction.SECOND;
        retval[74] = new Now();
        retval[75] = new Areas();
        retval[76] = new Rows();
        retval[77] = new Columns();
        retval[FunctionID.OFFSET] = new Offset(); //nominally 78

        retval[82] = TextFunction.SEARCH;
        retval[83] = MatrixFunction.TRANSPOSE;

        // 86: TYPE

        retval[97] = NumericFunction.ATAN2;
        retval[98] = NumericFunction.ASIN;
        retval[99] = NumericFunction.ACOS;
        retval[FunctionID.CHOOSE] = new Choose(); //nominally 100
        retval[101] = new Hlookup();
        retval[102] = new Vlookup();

        retval[105] = LogicalFunction.ISREF;

        retval[109] = NumericFunction.LOG;

        retval[111] = TextFunction.CHAR;
        retval[112] = TextFunction.LOWER;
        retval[113] = TextFunction.UPPER;
        retval[114] = TextFunction.PROPER;
        retval[115] = TextFunction.LEFT;
        retval[116] = TextFunction.RIGHT;
        retval[117] = TextFunction.EXACT;
        retval[118] = TextFunction.TRIM;
        retval[119] = new Replace();
        retval[120] = new Substitute();
        retval[121] = new Code();

        retval[124] = TextFunction.FIND;
        // 125: CELL

        retval[126] = LogicalFunction.ISERR;
        retval[127] = LogicalFunction.ISTEXT;
        retval[128] = LogicalFunction.ISNUMBER;
        retval[129] = LogicalFunction.ISBLANK;
        retval[130] = new T();
        // 131: N
        // 140: DATEVALUE
        // 141: TIMEVALUE
        // 142: SLN
        // 143: SYD
        // 144: DDB

        retval[FunctionID.INDIRECT] = null; // Indirect.evaluate has different signature

        retval[162] = TextFunction.CLEAN;
        
        retval[163] = MatrixFunction.MDETERM;
        retval[164] = MatrixFunction.MINVERSE;
        retval[165] = MatrixFunction.MMULT;

        retval[167] = new IPMT();
        retval[168] = new PPMT();
        retval[169] = new Counta();

        retval[183] = AggregateFunction.PRODUCT;
        retval[184] = NumericFunction.FACT;

        // 189: DPRODUCT
        retval[190] = LogicalFunction.ISNONTEXT;

        retval[194] = AggregateFunction.VARP;
        // 195: DSTDEVP
        // 196: DVARP
        retval[197] = NumericFunction.TRUNC;
        retval[198] = LogicalFunction.ISLOGICAL;
        // 199: DCOUNTA

        //204: USDOLLAR (YEN in BIFF3)
        //205: FINDB
        //206: SEARCHB
        //207: REPLACEB
        //208: LEFTB
        //209: RIGHTB
        //210: MIDB
        //211: LENB
        retval[212] = NumericFunction.ROUNDUP;
        retval[213] = NumericFunction.ROUNDDOWN;
        //214: ASC
        //215: DBCS (JIS in BIFF3)
        retval[216] = new Rank();
        retval[219] = new Address();
        retval[220] = new Days360();
        retval[221] = new Today();
        //222: VBD

        retval[227] = AggregateFunction.MEDIAN;
        retval[228] = new Sumproduct();
        retval[229] = NumericFunction.SINH;
        retval[230] = NumericFunction.COSH;
        retval[231] = NumericFunction.TANH;
        retval[232] = NumericFunction.ASINH;
        retval[233] = NumericFunction.ACOSH;
        retval[234] = NumericFunction.ATANH;
        retval[235] = new DStarRunner(DStarRunner.DStarAlgorithmEnum.DGET);

        // 244: INFO

        // 247: DB
        // 252: FEQUENCY
        retval[252] = Frequency.instance;

        retval[FunctionID.EXTERNAL_FUNC] = null; // ExternalFunction is a FreeRefFunction, nominally 255

        retval[261] = new Errortype();

        retval[269] = AggregateFunction.AVEDEV;
        // 270: BETADIST
        // 271: GAMMALN
        // 272: BETAINV
        // 273: BINOMDIST
        // 274: CHIDIST
        // 275: CHIINV
        retval[276] = NumericFunction.COMBIN;
        // 277: CONFIDENCE
        // 278:CRITBINOM
        retval[279] = new Even();
        // 280: EXPONDIST
        // 281: FDIST
        // 282: FINV
        // 283: FISHER
        // 284: FISHERINV
        retval[285] = NumericFunction.FLOOR;
        // 286: GAMMADIST
        // 287: GAMMAINV
        retval[288] = NumericFunction.CEILING;
        // 289: HYPGEOMDIST
        // 290: LOGNORMDIST
        // 291: LOGINV
        // 292: NEGBINOMDIST
        // 293: NORMDIST
        // 294: NORMSDIST
        // 295: NORMINV
        // 296: NORMSINV
        // 297: STANDARDIZE
        retval[298] = new Odd();
        // 299: PERMUT
        retval[300] = NumericFunction.POISSON;
        // 301: TDIST
        // 302: WEIBULL
        retval[303] = new Sumxmy2();
        retval[304] = new Sumx2my2();
        retval[305] = new Sumx2py2();
        // 306: CHITEST
        // 307: CORREL
        // 308: COVAR
        // 309: FORECAST
        // 310: FTEST
        retval[311] = new Intercept();
        // 312: PEARSON
        // 313: RSQ
        // 314: STEYX
        retval[315] = new Slope();
        // 316: TTEST
        // 317: PROB
        retval[318] = AggregateFunction.DEVSQ;
        retval[319] = AggregateFunction.GEOMEAN;
        // 320: HARMEAN
        retval[321] = AggregateFunction.SUMSQ;
        // 322: KURT
        // 323: SKEW
        // 324: ZTEST
        retval[325] = AggregateFunction.LARGE;
        retval[326] = AggregateFunction.SMALL;
        // 327: QUARTILE
        retval[328] = AggregateFunction.PERCENTILE;
        // 329: PERCENTRANK
        retval[330] = new Mode();
        // 331: TRIMMEAN
        // 332: TINV

        retval[336] = TextFunction.CONCATENATE;
        retval[337] = NumericFunction.POWER;

        retval[342] = NumericFunction.RADIANS;
        retval[343] = NumericFunction.DEGREES;
        retval[344] = new Subtotal();
        retval[345] = new Sumif();
        retval[346] = new Countif();
        retval[347] = new Countblank();

        // 350: ISPMT
        // 351: DATEDIF
        // 352: DATESTRING
        // 353: NUMBERSTRING
        retval[354] = new Roman();

        // 358: GETPIVOTDATA
        retval[359] = new Hyperlink();
        // 360: PHONETIC
        // 361: AVERAGEA
        retval[362] = MinaMaxa.MAXA;
        retval[363] = MinaMaxa.MINA;
        // 364: STDEVPA
        // 365: VARPA
        // 366: STDEVA
        // 367: VARA

        for (int i = 0; i < retval.length; i++) {
            Function f = retval[i];
            if (f == null) {
                FunctionMetadata fm = FunctionMetadataRegistry.getFunctionByIndex(i);
                if (fm == null) {
                    continue;
                }
                retval[i] = new NotImplementedFunction(fm.getName());
            }
        }
        return retval;
    }
    /**
     * @return <code>null</code> if the specified functionIndex is for INDIRECT() or any external (add-in) function.
     */
    public static Function getBasicFunction(int functionIndex) {
        // check for 'free ref' functions first
        switch (functionIndex) {
        case FunctionID.INDIRECT:
        case FunctionID.EXTERNAL_FUNC:
            return null;
        }
        // else - must be plain function
        Function result = functions[functionIndex];
        if (result == null) {
            throw new NotImplementedException("FuncIx=" + functionIndex);
        }
        return result;
    }

    /**
     * Register a new function in runtime.
     *
     * @param name  the function name
     * @param func  the functoin to register
     * @throws IllegalArgumentException if the function is unknown or already  registered.
     * @since 3.8 beta6
     */
    public static void registerFunction(String name, Function func){
        FunctionMetadata metaData = FunctionMetadataRegistry.getFunctionByName(name);
        if(metaData == null) {
            if(AnalysisToolPak.isATPFunction(name)) {
                throw new IllegalArgumentException(name + " is a function from the Excel Analysis Toolpack. " +
                        "Use AnalysisToolpack.registerFunction(String name, FreeRefFunction func) instead.");
            }

            throw new IllegalArgumentException("Unknown function: " + name);
        }

        int idx = metaData.getIndex();
        if(functions[idx] instanceof NotImplementedFunction) {
            functions[idx] = func;
        } else {
            throw new IllegalArgumentException("POI already implememts " + name +
                    ". You cannot override POI's implementations of Excel functions");
        }
    }

    /**
     * Returns a collection of function names implemented by POI.
     *
     * @return an array of supported functions
     * @since 3.8 beta6
     */
    public static Collection<String> getSupportedFunctionNames() {
        Collection<String> lst = new TreeSet<>();
        for (int i = 0; i < functions.length; i++) {
            Function func = functions[i];
            FunctionMetadata metaData = FunctionMetadataRegistry.getFunctionByIndex(i);
            if (func != null && !(func instanceof NotImplementedFunction)) {
                lst.add(metaData.getName());
            }
        }
        lst.add("INDIRECT"); // INDIRECT is a special case
        return Collections.unmodifiableCollection(lst);
    }

    /**
     * Returns an array of function names NOT implemented by POI.
     *
     * @return an array of not supported functions
     * @since 3.8 beta6
     */
    public static Collection<String> getNotSupportedFunctionNames() {
        Collection<String> lst = new TreeSet<>();
        for (int i = 0; i < functions.length; i++) {
            Function func = functions[i];
            if (func != null && (func instanceof NotImplementedFunction)) {
                FunctionMetadata metaData = FunctionMetadataRegistry.getFunctionByIndex(i);
                lst.add(metaData.getName());
            }
        }
        lst.remove("INDIRECT"); // INDIRECT is a special case
        return Collections.unmodifiableCollection(lst);
    }
}

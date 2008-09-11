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

package org.apache.poi.hssf.record.formula.eval;

import java.util.HashMap;
import java.util.Map;

import org.apache.poi.hssf.record.formula.functions.*;

/**
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 *  
 */
public abstract class FunctionEval implements OperationEval {
    /**
     * Some function IDs that require special treatment
     */
    private static final class FunctionID {
        /** 78 */
        public static final int OFFSET = 78;
        /** 148 */
        public static final int INDIRECT = 148;
        /** 255 */
        public static final int EXTERNAL_FUNC = 255;
    }
    // convenient access to namespace
    private static final FunctionID ID = null;
    
    protected static Function[] functions = produceFunctions();

    private static Map freeRefFunctionsByIdMap;
     
    static {
        Map m = new HashMap();
        addMapping(m, ID.INDIRECT, new Indirect());
        addMapping(m, ID.EXTERNAL_FUNC, new ExternalFunction());
        freeRefFunctionsByIdMap = m;
    }
    private static void addMapping(Map m, int offset, FreeRefFunction frf) {
        m.put(createFRFKey(offset), frf);
    }
    private static Integer createFRFKey(int functionIndex) {
        return new Integer(functionIndex);
    }
    
    
    public Function getFunction() {
        short fidx = getFunctionIndex();
        return functions[fidx];
    }
    public boolean isFreeRefFunction() {
        return freeRefFunctionsByIdMap.containsKey(createFRFKey(getFunctionIndex()));
    }
    public FreeRefFunction getFreeRefFunction() {
        return (FreeRefFunction) freeRefFunctionsByIdMap.get(createFRFKey(getFunctionIndex()));
    }

    public abstract short getFunctionIndex();

    private static Function[] produceFunctions() {
        Function[] retval = new Function[368];
        retval[0] = new Count(); // COUNT
        retval[1] = new If(); // IF
        retval[2] = new IsNa(); // ISNA
        retval[3] = new IsError(); // ISERROR
        retval[4] = AggregateFunction.SUM;
        retval[5] = AggregateFunction.AVERAGE;
        retval[6] = AggregateFunction.MIN;
        retval[7] = AggregateFunction.MAX;
        retval[8] = new Row(); // ROW
        retval[9] = new Column(); // COLUMN
        retval[10] = new Na(); // NA
        retval[11] = new Npv(); // NPV
        retval[12] = AggregateFunction.STDEV;
        retval[13] = NumericFunction.DOLLAR;
        retval[14] = new Fixed(); // FIXED
        retval[15] = NumericFunction.SIN;
        retval[16] = NumericFunction.COS;
        retval[17] = NumericFunction.TAN;
        retval[18] = NumericFunction.ATAN;
        retval[19] = new Pi(); // PI
        retval[20] = NumericFunction.SQRT;
        retval[21] = NumericFunction.EXP;
        retval[22] = NumericFunction.LN;
        retval[23] = NumericFunction.LOG10;
        retval[24] = NumericFunction.ABS;
        retval[25] = NumericFunction.INT;
        retval[26] = NumericFunction.SIGN;
        retval[27] = NumericFunction.ROUND;
        retval[28] = new Lookup(); // LOOKUP
        retval[29] = new Index(); // INDEX
        retval[30] = new Rept(); // REPT
        retval[31] = new Mid(); // MID
        retval[32] = new Len(); // LEN
        retval[33] = new Value(); // VALUE
        retval[34] = new True(); // TRUE
        retval[35] = new False(); // FALSE
        retval[36] = new And(); // AND
        retval[37] = new Or(); // OR
        retval[38] = new Not(); // NOT
        retval[39] = NumericFunction.MOD;
        retval[40] = new Dcount(); // DCOUNT
        retval[41] = new Dsum(); // DSUM
        retval[42] = new Daverage(); // DAVERAGE
        retval[43] = new Dmin(); // DMIN
        retval[44] = new Dmax(); // DMAX
        retval[45] = new Dstdev(); // DSTDEV
        retval[46] = new Var(); // VAR
        retval[47] = new Dvar(); // DVAR
        retval[48] = new Text(); // TEXT
        retval[49] = new Linest(); // LINEST
        retval[50] = new Trend(); // TREND
        retval[51] = new Logest(); // LOGEST
        retval[52] = new Growth(); // GROWTH
        retval[53] = new Goto(); // GOTO
        retval[54] = new Halt(); // HALT
        retval[56] = FinanceFunction.PV;
        retval[57] = FinanceFunction.FV;
        retval[58] = FinanceFunction.NPER;
        retval[59] = FinanceFunction.PMT;
        retval[60] = new Rate(); // RATE
        retval[61] = new Mirr(); // MIRR
        retval[62] = new Irr(); // IRR
        retval[63] = new Rand(); // RAND
        retval[64] = new Match(); // MATCH
        retval[65] = DateFunc.instance; // DATE
        retval[66] = new Time(); // TIME
        retval[67] = CalendarFieldFunction.DAY; // DAY
        retval[68] = CalendarFieldFunction.MONTH; // MONTH
        retval[69] = CalendarFieldFunction.YEAR; // YEAR
        retval[70] = new Weekday(); // WEEKDAY
        retval[71] = new Hour(); // HOUR
        retval[72] = new Minute(); // MINUTE
        retval[73] = new Second(); // SECOND
        retval[74] = new Now(); // NOW
        retval[75] = new Areas(); // AREAS
        retval[76] = new Rows(); // ROWS
        retval[77] = new Columns(); // COLUMNS
        retval[ID.OFFSET] = new Offset(); // OFFSET
        retval[79] = new Absref(); // ABSREF
        retval[80] = new Relref(); // RELREF
        retval[81] = new Argument(); // ARGUMENT
        retval[82] = new Search(); // SEARCH
        retval[83] = new Transpose(); // TRANSPOSE
        retval[84] = new org.apache.poi.hssf.record.formula.functions.Error(); // ERROR
        retval[85] = new Step(); // STEP
        retval[86] = new Type(); // TYPE
        retval[87] = new Echo(); // ECHO
        retval[88] = new Setname(); // SETNAME
        retval[89] = new Caller(); // CALLER
        retval[90] = new Deref(); // DEREF
        retval[91] = new NotImplementedFunction(); // WINDOWS
        retval[92] = new Series(); // SERIES
        retval[93] = new NotImplementedFunction(); // DOCUMENTS
        retval[94] = new Activecell(); // ACTIVECELL
        retval[95] = new NotImplementedFunction(); // SELECTION
        retval[96] = new Result(); // RESULT
        retval[97] = NumericFunction.ATAN2;
        retval[98] = NumericFunction.ASIN;
        retval[99] = NumericFunction.ACOS;
        retval[100] = new Choose(); // CHOOSE
        retval[101] = new Hlookup(); // HLOOKUP
        retval[102] = new Vlookup(); // VLOOKUP
        retval[103] = new Links(); // LINKS
        retval[104] = new Input(); // INPUT
        retval[105] = new Isref(); // ISREF
        retval[106] = new NotImplementedFunction(); // GETFORMULA
        retval[107] = new NotImplementedFunction(); // GETNAME
        retval[108] = new Setvalue(); // SETVALUE
        retval[109] = NumericFunction.LOG;
        retval[110] = new Exec(); // EXEC
        retval[111] = new Char(); // CHAR
        retval[112] = new Lower(); // LOWER
        retval[113] = new Upper(); // UPPER
        retval[114] = new Proper(); // PROPER
        retval[115] = new Left(); // LEFT
        retval[116] = new Right(); // RIGHT
        retval[117] = new Exact(); // EXACT
        retval[118] = new Trim(); // TRIM
        retval[119] = new Replace(); // REPLACE
        retval[120] = new Substitute(); // SUBSTITUTE
        retval[121] = new Code(); // CODE
        retval[122] = new Names(); // NAMES
        retval[123] = new NotImplementedFunction(); // DIRECTORY
        retval[124] = new Find(); // FIND
        retval[125] = new Cell(); // CELL
        retval[126] = new Iserr(); // ISERR
        retval[127] = new Istext(); // ISTEXT
        retval[128] = new Isnumber(); // ISNUMBER
        retval[129] = new Isblank(); // ISBLANK
        retval[130] = new T(); // T
        retval[131] = new N(); // N
        retval[132] = new NotImplementedFunction(); // FOPEN
        retval[133] = new NotImplementedFunction(); // FCLOSE
        retval[134] = new NotImplementedFunction(); // FSIZE
        retval[135] = new NotImplementedFunction(); // FREADLN
        retval[136] = new NotImplementedFunction(); // FREAD
        retval[137] = new NotImplementedFunction(); // FWRITELN
        retval[138] = new NotImplementedFunction(); // FWRITE
        retval[139] = new Fpos(); // FPOS
        retval[140] = new Datevalue(); // DATEVALUE
        retval[141] = new Timevalue(); // TIMEVALUE
        retval[142] = new Sln(); // SLN
        retval[143] = new Syd(); // SYD
        retval[144] = new Ddb(); // DDB
        retval[145] = new NotImplementedFunction(); // GETDEF
        retval[146] = new Reftext(); // REFTEXT
        retval[147] = new Textref(); // TEXTREF
        retval[ID.INDIRECT] = null; // Indirect.evaluate has different signature
        retval[149] = new NotImplementedFunction(); // REGISTER
        retval[150] = new Call(); // CALL
        retval[151] = new NotImplementedFunction(); // ADDBAR
        retval[152] = new NotImplementedFunction(); // ADDMENU
        retval[153] = new NotImplementedFunction(); // ADDCOMMAND
        retval[154] = new NotImplementedFunction(); // ENABLECOMMAND
        retval[155] = new NotImplementedFunction(); // CHECKCOMMAND
        retval[156] = new NotImplementedFunction(); // RENAMECOMMAND
        retval[157] = new NotImplementedFunction(); // SHOWBAR
        retval[158] = new NotImplementedFunction(); // DELETEMENU
        retval[159] = new NotImplementedFunction(); // DELETECOMMAND
        retval[160] = new NotImplementedFunction(); // GETCHARTITEM
        retval[161] = new NotImplementedFunction(); // DIALOGBOX
        retval[162] = new Clean(); // CLEAN
        retval[163] = new Mdeterm(); // MDETERM
        retval[164] = new Minverse(); // MINVERSE
        retval[165] = new Mmult(); // MMULT
        retval[166] = new Files(); // FILES
        retval[167] = new Ipmt(); // IPMT
        retval[168] = new Ppmt(); // PPMT
        retval[169] = new Counta(); // COUNTA
        retval[170] = new NotImplementedFunction(); // CANCELKEY
        retval[175] = new Initiate(); // INITIATE
        retval[176] = new Request(); // REQUEST
        retval[177] = new NotImplementedFunction(); // POKE
        retval[178] = new NotImplementedFunction(); // EXECUTE
        retval[179] = new NotImplementedFunction(); // TERMINATE
        retval[180] = new NotImplementedFunction(); // RESTART
        retval[181] = new Help(); // HELP
        retval[182] = new NotImplementedFunction(); // GETBAR
        retval[183] = AggregateFunction.PRODUCT;
        retval[184] = NumericFunction.FACT;
        retval[185] = new NotImplementedFunction(); // GETCELL
        retval[186] = new NotImplementedFunction(); // GETWORKSPACE
        retval[187] = new NotImplementedFunction(); // GETWINDOW
        retval[188] = new NotImplementedFunction(); // GETDOCUMENT
        retval[189] = new Dproduct(); // DPRODUCT
        retval[190] = new Isnontext(); // ISNONTEXT
        retval[191] = new NotImplementedFunction(); // GETNOTE
        retval[192] = new Note(); // NOTE
        retval[193] = new Stdevp(); // STDEVP
        retval[194] = new Varp(); // VARP
        retval[195] = new Dstdevp(); // DSTDEVP
        retval[196] = new Dvarp(); // DVARP
        retval[197] = new Trunc(); // TRUNC
        retval[198] = new Islogical(); // ISLOGICAL
        retval[199] = new Dcounta(); // DCOUNTA
        retval[200] = new NotImplementedFunction(); // DELETEBAR
        retval[201] = new NotImplementedFunction(); // UNREGISTER
        retval[204] = new Usdollar(); // USDOLLAR
        retval[205] = new Findb(); // FINDB
        retval[206] = new Searchb(); // SEARCHB
        retval[207] = new Replaceb(); // REPLACEB
        retval[208] = new Leftb(); // LEFTB
        retval[209] = new Rightb(); // RIGHTB
        retval[210] = new Midb(); // MIDB
        retval[211] = new Lenb(); // LENB
        retval[212] = NumericFunction.ROUNDUP;
        retval[213] = NumericFunction.ROUNDDOWN;
        retval[214] = new Asc(); // ASC
        retval[215] = new Dbcs(); // DBCS
        retval[216] = new Rank(); // RANK
        retval[219] = new Address(); // ADDRESS
        retval[220] = new Days360(); // DAYS360
        retval[221] = new Today(); // TODAY
        retval[222] = new Vdb(); // VDB
        retval[227] = AggregateFunction.MEDIAN;
        retval[228] = new Sumproduct(); // SUMPRODUCT
        retval[229] = NumericFunction.SINH;
        retval[230] = NumericFunction.COSH;
        retval[231] = NumericFunction.TANH;
        retval[232] = NumericFunction.ASINH;
        retval[233] = NumericFunction.ACOSH;
        retval[234] = NumericFunction.ATANH;
        retval[235] = new Dget(); // DGET
        retval[236] = new NotImplementedFunction(); // CREATEOBJECT
        retval[237] = new Volatile(); // VOLATILE
        retval[238] = new Lasterror(); // LASTERROR
        retval[239] = new NotImplementedFunction(); // CUSTOMUNDO
        retval[240] = new Customrepeat(); // CUSTOMREPEAT
        retval[241] = new Formulaconvert(); // FORMULACONVERT
        retval[242] = new NotImplementedFunction(); // GETLINKINFO
        retval[243] = new NotImplementedFunction(); // TEXTBOX
        retval[244] = new Info(); // INFO
        retval[245] = new Group(); // GROUP
        retval[246] = new NotImplementedFunction(); // GETOBJECT
        retval[247] = new Db(); // DB
        retval[248] = new NotImplementedFunction(); // PAUSE
        retval[250] = new NotImplementedFunction(); // RESUME
        retval[252] = new Frequency(); // FREQUENCY
        retval[253] = new NotImplementedFunction(); // ADDTOOLBAR
        retval[254] = new NotImplementedFunction(); // DELETETOOLBAR
        retval[ID.EXTERNAL_FUNC] = null; // ExternalFunction is a FreeREfFunction
        retval[256] = new NotImplementedFunction(); // RESETTOOLBAR
        retval[257] = new Evaluate(); // EVALUATE
        retval[258] = new NotImplementedFunction(); // GETTOOLBAR
        retval[259] = new NotImplementedFunction(); // GETTOOL
        retval[260] = new NotImplementedFunction(); // SPELLINGCHECK
        retval[261] = new Errortype(); // ERRORTYPE
        retval[262] = new NotImplementedFunction(); // APPTITLE
        retval[263] = new NotImplementedFunction(); // WINDOWTITLE
        retval[264] = new NotImplementedFunction(); // SAVETOOLBAR
        retval[265] = new NotImplementedFunction(); // ENABLETOOL
        retval[266] = new NotImplementedFunction(); // PRESSTOOL
        retval[267] = new NotImplementedFunction(); // REGISTERID
        retval[268] = new NotImplementedFunction(); // GETWORKBOOK
        retval[269] = AggregateFunction.AVEDEV;
        retval[270] = new Betadist(); // BETADIST
        retval[271] = new Gammaln(); // GAMMALN
        retval[272] = new Betainv(); // BETAINV
        retval[273] = new Binomdist(); // BINOMDIST
        retval[274] = new Chidist(); // CHIDIST
        retval[275] = new Chiinv(); // CHIINV
        retval[276] = NumericFunction.COMBIN;
        retval[277] = new Confidence(); // CONFIDENCE
        retval[278] = new Critbinom(); // CRITBINOM
        retval[279] = new Even(); // EVEN
        retval[280] = new Expondist(); // EXPONDIST
        retval[281] = new Fdist(); // FDIST
        retval[282] = new Finv(); // FINV
        retval[283] = new Fisher(); // FISHER
        retval[284] = new Fisherinv(); // FISHERINV
        retval[285] = NumericFunction.FLOOR;
        retval[286] = new Gammadist(); // GAMMADIST
        retval[287] = new Gammainv(); // GAMMAINV
        retval[288] = NumericFunction.CEILING;
        retval[289] = new Hypgeomdist(); // HYPGEOMDIST
        retval[290] = new Lognormdist(); // LOGNORMDIST
        retval[291] = new Loginv(); // LOGINV
        retval[292] = new Negbinomdist(); // NEGBINOMDIST
        retval[293] = new Normdist(); // NORMDIST
        retval[294] = new Normsdist(); // NORMSDIST
        retval[295] = new Norminv(); // NORMINV
        retval[296] = new Normsinv(); // NORMSINV
        retval[297] = new Standardize(); // STANDARDIZE
        retval[298] = new Odd(); // ODD
        retval[299] = new Permut(); // PERMUT
        retval[300] = new Poisson(); // POISSON
        retval[301] = new Tdist(); // TDIST
        retval[302] = new Weibull(); // WEIBULL
        retval[303] = new Sumxmy2(); // SUMXMY2
        retval[304] = new Sumx2my2(); // SUMX2MY2
        retval[305] = new Sumx2py2(); // SUMX2PY2
        retval[306] = new Chitest(); // CHITEST
        retval[307] = new Correl(); // CORREL
        retval[308] = new Covar(); // COVAR
        retval[309] = new Forecast(); // FORECAST
        retval[310] = new Ftest(); // FTEST
        retval[311] = new Intercept(); // INTERCEPT
        retval[312] = new Pearson(); // PEARSON
        retval[313] = new Rsq(); // RSQ
        retval[314] = new Steyx(); // STEYX
        retval[315] = new Slope(); // SLOPE
        retval[316] = new Ttest(); // TTEST
        retval[317] = new Prob(); // PROB
        retval[318] = AggregateFunction.DEVSQ;
        retval[319] = new Geomean(); // GEOMEAN
        retval[320] = new Harmean(); // HARMEAN
        retval[321] = AggregateFunction.SUMSQ;
        retval[322] = new Kurt(); // KURT
        retval[323] = new Skew(); // SKEW
        retval[324] = new Ztest(); // ZTEST
        retval[325] = AggregateFunction.LARGE;
        retval[326] = AggregateFunction.SMALL;
        retval[327] = new Quartile(); // QUARTILE
        retval[328] = new Percentile(); // PERCENTILE
        retval[329] = new Percentrank(); // PERCENTRANK
        retval[330] = new Mode(); // MODE
        retval[331] = new Trimmean(); // TRIMMEAN
        retval[332] = new Tinv(); // TINV
        retval[334] = new NotImplementedFunction(); // MOVIECOMMAND
        retval[335] = new NotImplementedFunction(); // GETMOVIE
        retval[336] = new Concatenate(); // CONCATENATE
        retval[337] = NumericFunction.POWER;
        retval[338] = new NotImplementedFunction(); // PIVOTADDDATA
        retval[339] = new NotImplementedFunction(); // GETPIVOTTABLE
        retval[340] = new NotImplementedFunction(); // GETPIVOTFIELD
        retval[341] = new NotImplementedFunction(); // GETPIVOTITEM
        retval[342] = NumericFunction.RADIANS;
        retval[343] = NumericFunction.DEGREES;
        retval[344] = new Subtotal(); // SUBTOTAL
        retval[345] = new Sumif(); // SUMIF
        retval[346] = new Countif(); // COUNTIF
        retval[347] = new Countblank(); // COUNTBLANK
        retval[348] = new NotImplementedFunction(); // SCENARIOGET
        retval[349] = new NotImplementedFunction(); // OPTIONSLISTSGET
        retval[350] = new Ispmt(); // ISPMT
        retval[351] = new Datedif(); // DATEDIF
        retval[352] = new Datestring(); // DATESTRING
        retval[353] = new Numberstring(); // NUMBERSTRING
        retval[354] = new Roman(); // ROMAN
        retval[355] = new NotImplementedFunction(); // OPENDIALOG
        retval[356] = new NotImplementedFunction(); // SAVEDIALOG
        retval[357] = new NotImplementedFunction(); // VIEWGET
        retval[358] = new NotImplementedFunction(); // GETPIVOTDATA
        retval[359] = new Hyperlink(); // HYPERLINK
        retval[360] = new NotImplementedFunction(); // PHONETIC
        retval[361] = new Averagea(); // AVERAGEA
        retval[362] = MinaMaxa.MAXA;
        retval[363] = MinaMaxa.MINA;
        retval[364] = new Stdevpa(); // STDEVPA
        retval[365] = new Varpa(); // VARPA
        retval[366] = new Stdeva(); // STDEVA
        retval[367] = new Vara(); // VARA
        return retval;
    }
}

/* ====================================================================
   Copyright 2003-2004   Apache Software Foundation

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package org.apache.poi.hssf.record.formula;

import org.apache.poi.util.BinaryTree;
import org.apache.poi.hssf.model.Workbook;


/**
 * This class provides the base functionality for Excel sheet functions 
 * There are two kinds of function Ptgs - tFunc and tFuncVar
 * Therefore, this class will have ONLY two subclasses
 * @author  Avik Sengupta
 * @author Andrew C. Oliver (acoliver at apache dot org)
 */
public abstract class AbstractFunctionPtg extends OperationPtg {
	//constant used allow a ptgAttr to be mapped properly for its functionPtg
	public static final String ATTR_NAME = "specialflag";
	    
    public static final short INDEX_EXTERNAL = 255;
    
    private static BinaryTree map = produceHash(); 
    protected static Object[][] functionData = produceFunctionData();
    protected byte returnClass;
    protected byte[] paramClass;
    
    protected byte field_1_num_args;
    protected short field_2_fnc_index;
 
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer
        .append("<FunctionPtg>").append("\n")
        .append("   field_1_num_args=").append(field_1_num_args).append("\n")
        .append("      name         =").append(lookupName(field_2_fnc_index)).append("\n")
        .append("   field_2_fnc_index=").append(field_2_fnc_index).append("\n")
        .append("</FunctionPtg>");
        return buffer.toString();
    }
   
    public int getType() {
        return -1;
    }   
    
   
    
    public short getFunctionIndex() {
        return field_2_fnc_index;
    }
    
    public String getName() {
        return lookupName(field_2_fnc_index);
    }
    
    public String toFormulaString(Workbook book) {
        return getName();
    }
    
    public String toFormulaString(String[] operands) {
        StringBuffer buf = new StringBuffer();        
          
          if (field_2_fnc_index != 1) {
              buf.append(getName());
              buf.append('(');
          }
          if (operands.length >0) {
              for (int i=0;i<operands.length;i++) {
                  buf.append(operands[i]);
                  buf.append(',');
              }
              buf.deleteCharAt(buf.length()-1);
          }
          if (field_2_fnc_index != 1) {
            buf.append(")");
          }
        return buf.toString();
    }
    
    public abstract void writeBytes(byte[] array, int offset);
    public abstract int getSize();
    
   
    

    
    protected String lookupName(short index) {
        return ((String)map.get(new Integer(index))); 
    }
    
    protected short lookupIndex(String name) {
        Integer index = (Integer) map.getKeyForValue(name);
        if (index != null) return index.shortValue();
        return INDEX_EXTERNAL;
    }
    
    /**
     * Produces the function table hashmap
     */
    private static BinaryTree produceHash() {
        BinaryTree dmap = new BinaryTree();

        dmap.put(new Integer(0),"COUNT");
        dmap.put(new Integer(1),"specialflag");
        dmap.put(new Integer(2),"ISNA");
        dmap.put(new Integer(3),"ISERROR");
        dmap.put(new Integer(4),"SUM");
        dmap.put(new Integer(5),"AVERAGE");
        dmap.put(new Integer(6),"MIN");
        dmap.put(new Integer(7),"MAX");
        dmap.put(new Integer(8),"ROW");
        dmap.put(new Integer(9),"COLUMN");
        dmap.put(new Integer(10),"NA");
        dmap.put(new Integer(11),"NPV");
        dmap.put(new Integer(12),"STDEV");
        dmap.put(new Integer(13),"DOLLAR");
        dmap.put(new Integer(14),"FIXED");
        dmap.put(new Integer(15),"SIN");
        dmap.put(new Integer(16),"COS");
        dmap.put(new Integer(17),"TAN");
        dmap.put(new Integer(18),"ATAN");
        dmap.put(new Integer(19),"PI");
        dmap.put(new Integer(20),"SQRT");
        dmap.put(new Integer(21),"EXP");
        dmap.put(new Integer(22),"LN");
        dmap.put(new Integer(23),"LOG10");
        dmap.put(new Integer(24),"ABS");
        dmap.put(new Integer(25),"INT");
        dmap.put(new Integer(26),"SIGN");
        dmap.put(new Integer(27),"ROUND");
        dmap.put(new Integer(28),"LOOKUP");
        dmap.put(new Integer(29),"INDEX");
        dmap.put(new Integer(30),"REPT");
        dmap.put(new Integer(31),"MID");
        dmap.put(new Integer(32),"LEN");
        dmap.put(new Integer(33),"VALUE");
        dmap.put(new Integer(34),"TRUE");
        dmap.put(new Integer(35),"FALSE");
        dmap.put(new Integer(36),"AND");
        dmap.put(new Integer(37),"OR");
        dmap.put(new Integer(38),"NOT");
        dmap.put(new Integer(39),"MOD");
        dmap.put(new Integer(40),"DCOUNT");
        dmap.put(new Integer(41),"DSUM");
        dmap.put(new Integer(42),"DAVERAGE");
        dmap.put(new Integer(43),"DMIN");
        dmap.put(new Integer(44),"DMAX");
        dmap.put(new Integer(45),"DSTDEV");
        dmap.put(new Integer(46),"VAR");
        dmap.put(new Integer(47),"DVAR");
        dmap.put(new Integer(48),"TEXT");
        dmap.put(new Integer(49),"LINEST");
        dmap.put(new Integer(50),"TREND");
        dmap.put(new Integer(51),"LOGEST");
        dmap.put(new Integer(52),"GROWTH");
        dmap.put(new Integer(53),"GOTO");
        dmap.put(new Integer(54),"HALT");
        dmap.put(new Integer(56),"PV");
        dmap.put(new Integer(57),"FV");
        dmap.put(new Integer(58),"NPER");
        dmap.put(new Integer(59),"PMT");
        dmap.put(new Integer(60),"RATE");
        dmap.put(new Integer(61),"MIRR");
        dmap.put(new Integer(62),"IRR");
        dmap.put(new Integer(63),"RAND");
        dmap.put(new Integer(64),"MATCH");
        dmap.put(new Integer(65),"DATE");
        dmap.put(new Integer(66),"TIME");
        dmap.put(new Integer(67),"DAY");
        dmap.put(new Integer(68),"MONTH");
        dmap.put(new Integer(69),"YEAR");
        dmap.put(new Integer(70),"WEEKDAY");
        dmap.put(new Integer(71),"HOUR");
        dmap.put(new Integer(72),"MINUTE");
        dmap.put(new Integer(73),"SECOND");
        dmap.put(new Integer(74),"NOW");
        dmap.put(new Integer(75),"AREAS");
        dmap.put(new Integer(76),"ROWS");
        dmap.put(new Integer(77),"COLUMNS");
        dmap.put(new Integer(78),"OFFSET");
        dmap.put(new Integer(79),"ABSREF");
        dmap.put(new Integer(80),"RELREF");
        dmap.put(new Integer(81),"ARGUMENT");
        dmap.put(new Integer(82),"SEARCH");
        dmap.put(new Integer(83),"TRANSPOSE");
        dmap.put(new Integer(84),"ERROR");
        dmap.put(new Integer(85),"STEP");
        dmap.put(new Integer(86),"TYPE");
        dmap.put(new Integer(87),"ECHO");
        dmap.put(new Integer(88),"SETNAME");
        dmap.put(new Integer(89),"CALLER");
        dmap.put(new Integer(90),"DEREF");
        dmap.put(new Integer(91),"WINDOWS");
        dmap.put(new Integer(92),"SERIES");
        dmap.put(new Integer(93),"DOCUMENTS");
        dmap.put(new Integer(94),"ACTIVECELL");
        dmap.put(new Integer(95),"SELECTION");
        dmap.put(new Integer(96),"RESULT");
        dmap.put(new Integer(97),"ATAN2");
        dmap.put(new Integer(98),"ASIN");
        dmap.put(new Integer(99),"ACOS");
        dmap.put(new Integer(100),"CHOOSE");
        dmap.put(new Integer(101),"HLOOKUP");
        dmap.put(new Integer(102),"VLOOKUP");
        dmap.put(new Integer(103),"LINKS");
        dmap.put(new Integer(104),"INPUT");
        dmap.put(new Integer(105),"ISREF");
        dmap.put(new Integer(106),"GETFORMULA");
        dmap.put(new Integer(107),"GETNAME");
        dmap.put(new Integer(108),"SETVALUE");
        dmap.put(new Integer(109),"LOG");
        dmap.put(new Integer(110),"EXEC");
        dmap.put(new Integer(111),"CHAR");
        dmap.put(new Integer(112),"LOWER");
        dmap.put(new Integer(113),"UPPER");
        dmap.put(new Integer(114),"PROPER");
        dmap.put(new Integer(115),"LEFT");
        dmap.put(new Integer(116),"RIGHT");
        dmap.put(new Integer(117),"EXACT");
        dmap.put(new Integer(118),"TRIM");
        dmap.put(new Integer(119),"REPLACE");
        dmap.put(new Integer(120),"SUBSTITUTE");
        dmap.put(new Integer(121),"CODE");
        dmap.put(new Integer(122),"NAMES");
        dmap.put(new Integer(123),"DIRECTORY");
        dmap.put(new Integer(124),"FIND");
        dmap.put(new Integer(125),"CELL");
        dmap.put(new Integer(126),"ISERR");
        dmap.put(new Integer(127),"ISTEXT");
        dmap.put(new Integer(128),"ISNUMBER");
        dmap.put(new Integer(129),"ISBLANK");
        dmap.put(new Integer(130),"T");
        dmap.put(new Integer(131),"N");
        dmap.put(new Integer(132),"FOPEN");
        dmap.put(new Integer(133),"FCLOSE");
        dmap.put(new Integer(134),"FSIZE");
        dmap.put(new Integer(135),"FREADLN");
        dmap.put(new Integer(136),"FREAD");
        dmap.put(new Integer(137),"FWRITELN");
        dmap.put(new Integer(138),"FWRITE");
        dmap.put(new Integer(139),"FPOS");
        dmap.put(new Integer(140),"DATEVALUE");
        dmap.put(new Integer(141),"TIMEVALUE");
        dmap.put(new Integer(142),"SLN");
        dmap.put(new Integer(143),"SYD");
        dmap.put(new Integer(144),"DDB");
        dmap.put(new Integer(145),"GETDEF");
        dmap.put(new Integer(146),"REFTEXT");
        dmap.put(new Integer(147),"TEXTREF");
        dmap.put(new Integer(148),"INDIRECT");
        dmap.put(new Integer(149),"REGISTER");
        dmap.put(new Integer(150),"CALL");
        dmap.put(new Integer(151),"ADDBAR");
        dmap.put(new Integer(152),"ADDMENU");
        dmap.put(new Integer(153),"ADDCOMMAND");
        dmap.put(new Integer(154),"ENABLECOMMAND");
        dmap.put(new Integer(155),"CHECKCOMMAND");
        dmap.put(new Integer(156),"RENAMECOMMAND");
        dmap.put(new Integer(157),"SHOWBAR");
        dmap.put(new Integer(158),"DELETEMENU");
        dmap.put(new Integer(159),"DELETECOMMAND");
        dmap.put(new Integer(160),"GETCHARTITEM");
        dmap.put(new Integer(161),"DIALOGBOX");
        dmap.put(new Integer(162),"CLEAN");
        dmap.put(new Integer(163),"MDETERM");
        dmap.put(new Integer(164),"MINVERSE");
        dmap.put(new Integer(165),"MMULT");
        dmap.put(new Integer(166),"FILES");
        dmap.put(new Integer(167),"IPMT");
        dmap.put(new Integer(168),"PPMT");
        dmap.put(new Integer(169),"COUNTA");
        dmap.put(new Integer(170),"CANCELKEY");
        dmap.put(new Integer(175),"INITIATE");
        dmap.put(new Integer(176),"REQUEST");
        dmap.put(new Integer(177),"POKE");
        dmap.put(new Integer(178),"EXECUTE");
        dmap.put(new Integer(179),"TERMINATE");
        dmap.put(new Integer(180),"RESTART");
        dmap.put(new Integer(181),"HELP");
        dmap.put(new Integer(182),"GETBAR");
        dmap.put(new Integer(183),"PRODUCT");
        dmap.put(new Integer(184),"FACT");
        dmap.put(new Integer(185),"GETCELL");
        dmap.put(new Integer(186),"GETWORKSPACE");
        dmap.put(new Integer(187),"GETWINDOW");
        dmap.put(new Integer(188),"GETDOCUMENT");
        dmap.put(new Integer(189),"DPRODUCT");
        dmap.put(new Integer(190),"ISNONTEXT");
        dmap.put(new Integer(191),"GETNOTE");
        dmap.put(new Integer(192),"NOTE");
        dmap.put(new Integer(193),"STDEVP");
        dmap.put(new Integer(194),"VARP");
        dmap.put(new Integer(195),"DSTDEVP");
        dmap.put(new Integer(196),"DVARP");
        dmap.put(new Integer(197),"TRUNC");
        dmap.put(new Integer(198),"ISLOGICAL");
        dmap.put(new Integer(199),"DCOUNTA");
        dmap.put(new Integer(200),"DELETEBAR");
        dmap.put(new Integer(201),"UNREGISTER");
        dmap.put(new Integer(204),"USDOLLAR");
        dmap.put(new Integer(205),"FINDB");
        dmap.put(new Integer(206),"SEARCHB");
        dmap.put(new Integer(207),"REPLACEB");
        dmap.put(new Integer(208),"LEFTB");
        dmap.put(new Integer(209),"RIGHTB");
        dmap.put(new Integer(210),"MIDB");
        dmap.put(new Integer(211),"LENB");
        dmap.put(new Integer(212),"ROUNDUP");
        dmap.put(new Integer(213),"ROUNDDOWN");
        dmap.put(new Integer(214),"ASC");
        dmap.put(new Integer(215),"DBCS");
        dmap.put(new Integer(216),"RANK");
        dmap.put(new Integer(219),"ADDRESS");
        dmap.put(new Integer(220),"DAYS360");
        dmap.put(new Integer(221),"TODAY");
        dmap.put(new Integer(222),"VDB");
        dmap.put(new Integer(227),"MEDIAN");
        dmap.put(new Integer(228),"SUMPRODUCT");
        dmap.put(new Integer(229),"SINH");
        dmap.put(new Integer(230),"COSH");
        dmap.put(new Integer(231),"TANH");
        dmap.put(new Integer(232),"ASINH");
        dmap.put(new Integer(233),"ACOSH");
        dmap.put(new Integer(234),"ATANH");
        dmap.put(new Integer(235),"DGET");
        dmap.put(new Integer(236),"CREATEOBJECT");
        dmap.put(new Integer(237),"VOLATILE");
        dmap.put(new Integer(238),"LASTERROR");
        dmap.put(new Integer(239),"CUSTOMUNDO");
        dmap.put(new Integer(240),"CUSTOMREPEAT");
        dmap.put(new Integer(241),"FORMULACONVERT");
        dmap.put(new Integer(242),"GETLINKINFO");
        dmap.put(new Integer(243),"TEXTBOX");
        dmap.put(new Integer(244),"INFO");
        dmap.put(new Integer(245),"GROUP");
        dmap.put(new Integer(246),"GETOBJECT");
        dmap.put(new Integer(247),"DB");
        dmap.put(new Integer(248),"PAUSE");
        dmap.put(new Integer(250),"RESUME");
        dmap.put(new Integer(252),"FREQUENCY");
        dmap.put(new Integer(253),"ADDTOOLBAR");
        dmap.put(new Integer(254),"DELETETOOLBAR");
        dmap.put(new Integer(255),"externalflag");
        dmap.put(new Integer(256),"RESETTOOLBAR");
        dmap.put(new Integer(257),"EVALUATE");
        dmap.put(new Integer(258),"GETTOOLBAR");
        dmap.put(new Integer(259),"GETTOOL");
        dmap.put(new Integer(260),"SPELLINGCHECK");
        dmap.put(new Integer(261),"ERRORTYPE");
        dmap.put(new Integer(262),"APPTITLE");
        dmap.put(new Integer(263),"WINDOWTITLE");
        dmap.put(new Integer(264),"SAVETOOLBAR");
        dmap.put(new Integer(265),"ENABLETOOL");
        dmap.put(new Integer(266),"PRESSTOOL");
        dmap.put(new Integer(267),"REGISTERID");
        dmap.put(new Integer(268),"GETWORKBOOK");
        dmap.put(new Integer(269),"AVEDEV");
        dmap.put(new Integer(270),"BETADIST");
        dmap.put(new Integer(271),"GAMMALN");
        dmap.put(new Integer(272),"BETAINV");
        dmap.put(new Integer(273),"BINOMDIST");
        dmap.put(new Integer(274),"CHIDIST");
        dmap.put(new Integer(275),"CHIINV");
        dmap.put(new Integer(276),"COMBIN");
        dmap.put(new Integer(277),"CONFIDENCE");
        dmap.put(new Integer(278),"CRITBINOM");
        dmap.put(new Integer(279),"EVEN");
        dmap.put(new Integer(280),"EXPONDIST");
        dmap.put(new Integer(281),"FDIST");
        dmap.put(new Integer(282),"FINV");
        dmap.put(new Integer(283),"FISHER");
        dmap.put(new Integer(284),"FISHERINV");
        dmap.put(new Integer(285),"FLOOR");
        dmap.put(new Integer(286),"GAMMADIST");
        dmap.put(new Integer(287),"GAMMAINV");
        dmap.put(new Integer(288),"CEILING");
        dmap.put(new Integer(289),"HYPGEOMDIST");
        dmap.put(new Integer(290),"LOGNORMDIST");
        dmap.put(new Integer(291),"LOGINV");
        dmap.put(new Integer(292),"NEGBINOMDIST");
        dmap.put(new Integer(293),"NORMDIST");
        dmap.put(new Integer(294),"NORMSDIST");
        dmap.put(new Integer(295),"NORMINV");
        dmap.put(new Integer(296),"NORMSINV");
        dmap.put(new Integer(297),"STANDARDIZE");
        dmap.put(new Integer(298),"ODD");
        dmap.put(new Integer(299),"PERMUT");
        dmap.put(new Integer(300),"POISSON");
        dmap.put(new Integer(301),"TDIST");
        dmap.put(new Integer(302),"WEIBULL");
        dmap.put(new Integer(303),"SUMXMY2");
        dmap.put(new Integer(304),"SUMX2MY2");
        dmap.put(new Integer(305),"SUMX2PY2");
        dmap.put(new Integer(306),"CHITEST");
        dmap.put(new Integer(307),"CORREL");
        dmap.put(new Integer(308),"COVAR");
        dmap.put(new Integer(309),"FORECAST");
        dmap.put(new Integer(310),"FTEST");
        dmap.put(new Integer(311),"INTERCEPT");
        dmap.put(new Integer(312),"PEARSON");
        dmap.put(new Integer(313),"RSQ");
        dmap.put(new Integer(314),"STEYX");
        dmap.put(new Integer(315),"SLOPE");
        dmap.put(new Integer(316),"TTEST");
        dmap.put(new Integer(317),"PROB");
        dmap.put(new Integer(318),"DEVSQ");
        dmap.put(new Integer(319),"GEOMEAN");
        dmap.put(new Integer(320),"HARMEAN");
        dmap.put(new Integer(321),"SUMSQ");
        dmap.put(new Integer(322),"KURT");
        dmap.put(new Integer(323),"SKEW");
        dmap.put(new Integer(324),"ZTEST");
        dmap.put(new Integer(325),"LARGE");
        dmap.put(new Integer(326),"SMALL");
        dmap.put(new Integer(327),"QUARTILE");
        dmap.put(new Integer(328),"PERCENTILE");
        dmap.put(new Integer(329),"PERCENTRANK");
        dmap.put(new Integer(330),"MODE");
        dmap.put(new Integer(331),"TRIMMEAN");
        dmap.put(new Integer(332),"TINV");
        dmap.put(new Integer(334),"MOVIECOMMAND");
        dmap.put(new Integer(335),"GETMOVIE");
        dmap.put(new Integer(336),"CONCATENATE");
        dmap.put(new Integer(337),"POWER");
        dmap.put(new Integer(338),"PIVOTADDDATA");
        dmap.put(new Integer(339),"GETPIVOTTABLE");
        dmap.put(new Integer(340),"GETPIVOTFIELD");
        dmap.put(new Integer(341),"GETPIVOTITEM");
        dmap.put(new Integer(342),"RADIANS");
        dmap.put(new Integer(343),"DEGREES");
        dmap.put(new Integer(344),"SUBTOTAL");
        dmap.put(new Integer(345),"SUMIF");
        dmap.put(new Integer(346),"COUNTIF");
        dmap.put(new Integer(347),"COUNTBLANK");
        dmap.put(new Integer(348),"SCENARIOGET");
        dmap.put(new Integer(349),"OPTIONSLISTSGET");
        dmap.put(new Integer(350),"ISPMT");
        dmap.put(new Integer(351),"DATEDIF");
        dmap.put(new Integer(352),"DATESTRING");
        dmap.put(new Integer(353),"NUMBERSTRING");
        dmap.put(new Integer(354),"ROMAN");
        dmap.put(new Integer(355),"OPENDIALOG");
        dmap.put(new Integer(356),"SAVEDIALOG");
        dmap.put(new Integer(357),"VIEWGET");
        dmap.put(new Integer(358),"GETPIVOTDATA");
        dmap.put(new Integer(359),"HYPERLINK");
        dmap.put(new Integer(360),"PHONETIC");
        dmap.put(new Integer(361),"AVERAGEA");
        dmap.put(new Integer(362),"MAXA");
        dmap.put(new Integer(363),"MINA");
        dmap.put(new Integer(364),"STDEVPA");
        dmap.put(new Integer(365),"VARPA");
        dmap.put(new Integer(366),"STDEVA");
        dmap.put(new Integer(367),"VARA");

        return dmap;
    }
    
    private static Object[][]  produceFunctionData() {
        Object [][] functionData = new Object[368][3];
                                 //return Class                       // Param Class                               //Num Params 
        functionData[0][0]=new Byte(Ptg.CLASS_VALUE);functionData[0][1]=new byte[] {Ptg.CLASS_REF};functionData[0][2]=new Integer(-1);
        functionData[2][0]=new Byte(Ptg.CLASS_VALUE);functionData[2][1]=new byte[] {Ptg.CLASS_VALUE};functionData[2][2]=new Integer(1);
        functionData[3][0]=new Byte(Ptg.CLASS_VALUE);functionData[3][1]=new byte[] {Ptg.CLASS_VALUE};functionData[3][2]=new Integer(1);
        functionData[4][0]=new Byte(Ptg.CLASS_VALUE);functionData[4][1]=new byte[] {Ptg.CLASS_REF};functionData[4][2]=new Integer(-1);
        functionData[5][0]=new Byte(Ptg.CLASS_VALUE);functionData[5][1]=new byte[] {Ptg.CLASS_REF};functionData[5][2]=new Integer(-1);
        functionData[6][0]=new Byte(Ptg.CLASS_VALUE);functionData[6][1]=new byte[] {Ptg.CLASS_REF};functionData[6][2]=new Integer(-1);
        functionData[7][0]=new Byte(Ptg.CLASS_VALUE);functionData[7][1]=new byte[] {Ptg.CLASS_REF};functionData[7][2]=new Integer(-1);
        functionData[8][0]=new Byte(Ptg.CLASS_VALUE);functionData[8][1]=new byte[] {Ptg.CLASS_REF};functionData[8][2]=new Integer(-1);
        functionData[9][0]=new Byte(Ptg.CLASS_VALUE);functionData[9][1]=new byte[] {Ptg.CLASS_REF};functionData[9][2]=new Integer(-1);
        functionData[10][0]=new Byte(Ptg.CLASS_VALUE);functionData[10][1]=new byte[] {Ptg.CLASS_VALUE};functionData[10][2]=new Integer(0);
        functionData[11][0]=new Byte(Ptg.CLASS_VALUE);functionData[11][1]=new byte[] {Ptg.CLASS_REF};functionData[11][2]=new Integer(-1);
        functionData[12][0]=new Byte(Ptg.CLASS_VALUE);functionData[12][1]=new byte[] {Ptg.CLASS_REF};functionData[12][2]=new Integer(-1);
        functionData[13][0]=new Byte(Ptg.CLASS_VALUE);functionData[13][1]=new byte[] {Ptg.CLASS_VALUE};functionData[13][2]=new Integer(-1);
        functionData[14][0]=new Byte(Ptg.CLASS_VALUE);functionData[14][1]=new byte[] {Ptg.CLASS_VALUE};functionData[14][2]=new Integer(-1);
        functionData[15][0]=new Byte(Ptg.CLASS_VALUE);functionData[15][1]=new byte[] {Ptg.CLASS_VALUE};functionData[15][2]=new Integer(1);
        functionData[16][0]=new Byte(Ptg.CLASS_VALUE);functionData[16][1]=new byte[] {Ptg.CLASS_VALUE};functionData[16][2]=new Integer(1);
        functionData[17][0]=new Byte(Ptg.CLASS_VALUE);functionData[17][1]=new byte[] {Ptg.CLASS_VALUE};functionData[17][2]=new Integer(1);
        functionData[18][0]=new Byte(Ptg.CLASS_VALUE);functionData[18][1]=new byte[] {Ptg.CLASS_VALUE};functionData[18][2]=new Integer(1);
        functionData[19][0]=new Byte(Ptg.CLASS_VALUE);functionData[19][1]=new byte[] {Ptg.CLASS_VALUE};functionData[19][2]=new Integer(0);
        functionData[20][0]=new Byte(Ptg.CLASS_VALUE);functionData[20][1]=new byte[] {Ptg.CLASS_VALUE};functionData[20][2]=new Integer(1);
        functionData[21][0]=new Byte(Ptg.CLASS_VALUE);functionData[21][1]=new byte[] {Ptg.CLASS_VALUE};functionData[21][2]=new Integer(1);
        functionData[22][0]=new Byte(Ptg.CLASS_VALUE);functionData[22][1]=new byte[] {Ptg.CLASS_VALUE};functionData[22][2]=new Integer(1);
        functionData[23][0]=new Byte(Ptg.CLASS_VALUE);functionData[23][1]=new byte[] {Ptg.CLASS_VALUE};functionData[23][2]=new Integer(1);
        functionData[24][0]=new Byte(Ptg.CLASS_VALUE);functionData[24][1]=new byte[] {Ptg.CLASS_VALUE};functionData[24][2]=new Integer(1);
        functionData[25][0]=new Byte(Ptg.CLASS_VALUE);functionData[25][1]=new byte[] {Ptg.CLASS_VALUE};functionData[25][2]=new Integer(1);
        functionData[26][0]=new Byte(Ptg.CLASS_VALUE);functionData[26][1]=new byte[] {Ptg.CLASS_VALUE};functionData[26][2]=new Integer(1);
        functionData[27][0]=new Byte(Ptg.CLASS_VALUE);functionData[27][1]=new byte[] {Ptg.CLASS_VALUE};functionData[27][2]=new Integer(2);
        functionData[28][0]=new Byte(Ptg.CLASS_VALUE);functionData[28][1]=new byte[] {Ptg.CLASS_VALUE, Ptg.CLASS_REF};functionData[28][2]=new Integer(-1);
        functionData[29][0]=new Byte(Ptg.CLASS_VALUE);functionData[29][1]=new byte[] {Ptg.CLASS_REF};functionData[29][2]=new Integer(-1);
        functionData[30][0]=new Byte(Ptg.CLASS_VALUE);functionData[30][1]=new byte[] {Ptg.CLASS_VALUE};functionData[30][2]=new Integer(2);
        functionData[31][0]=new Byte(Ptg.CLASS_VALUE);functionData[31][1]=new byte[] {Ptg.CLASS_VALUE};functionData[31][2]=new Integer(3);
        functionData[32][0]=new Byte(Ptg.CLASS_VALUE);functionData[32][1]=new byte[] {Ptg.CLASS_VALUE};functionData[32][2]=new Integer(1);
        functionData[33][0]=new Byte(Ptg.CLASS_VALUE);functionData[33][1]=new byte[] {Ptg.CLASS_VALUE};functionData[33][2]=new Integer(1);
        functionData[34][0]=new Byte(Ptg.CLASS_VALUE);functionData[34][1]=new byte[] {Ptg.CLASS_VALUE};functionData[34][2]=new Integer(1);
        functionData[35][0]=new Byte(Ptg.CLASS_VALUE);functionData[35][1]=new byte[] {Ptg.CLASS_VALUE};functionData[35][2]=new Integer(1);
        functionData[36][0]=new Byte(Ptg.CLASS_VALUE);functionData[36][1]=new byte[] {Ptg.CLASS_REF};functionData[36][2]=new Integer(-1);
        functionData[37][0]=new Byte(Ptg.CLASS_VALUE);functionData[37][1]=new byte[] {Ptg.CLASS_REF};functionData[37][2]=new Integer(-1);
        functionData[38][0]=new Byte(Ptg.CLASS_VALUE);functionData[38][1]=new byte[] {Ptg.CLASS_VALUE};functionData[38][2]=new Integer(1);
        functionData[39][0]=new Byte(Ptg.CLASS_VALUE);functionData[39][1]=new byte[] {Ptg.CLASS_VALUE};functionData[39][2]=new Integer(2);
        functionData[40][0]=new Byte(Ptg.CLASS_VALUE);functionData[40][1]=new byte[] {Ptg.CLASS_REF};functionData[40][2]=new Integer(3);
        functionData[41][0]=new Byte(Ptg.CLASS_VALUE);functionData[41][1]=new byte[] {Ptg.CLASS_REF};functionData[41][2]=new Integer(3);
        functionData[42][0]=new Byte(Ptg.CLASS_VALUE);functionData[42][1]=new byte[] {Ptg.CLASS_REF};functionData[42][2]=new Integer(3);
        functionData[43][0]=new Byte(Ptg.CLASS_VALUE);functionData[43][1]=new byte[] {Ptg.CLASS_REF};functionData[43][2]=new Integer(3);
        functionData[44][0]=new Byte(Ptg.CLASS_VALUE);functionData[44][1]=new byte[] {Ptg.CLASS_REF};functionData[44][2]=new Integer(3);
        functionData[45][0]=new Byte(Ptg.CLASS_VALUE);functionData[45][1]=new byte[] {Ptg.CLASS_REF};functionData[45][2]=new Integer(3);
        functionData[46][0]=new Byte(Ptg.CLASS_VALUE);functionData[46][1]=new byte[] {Ptg.CLASS_REF};functionData[46][2]=new Integer(-1);
        functionData[47][0]=new Byte(Ptg.CLASS_VALUE);functionData[47][1]=new byte[] {Ptg.CLASS_REF};functionData[47][2]=new Integer(3);
        functionData[48][0]=new Byte(Ptg.CLASS_VALUE);functionData[48][1]=new byte[] {Ptg.CLASS_VALUE};functionData[48][2]=new Integer(2);
        functionData[49][0]=new Byte(Ptg.CLASS_VALUE);functionData[49][1]=new byte[] {Ptg.CLASS_REF};functionData[49][2]=new Integer(-1);
        functionData[50][0]=new Byte(Ptg.CLASS_VALUE);functionData[50][1]=new byte[] {Ptg.CLASS_REF};functionData[50][2]=new Integer(-1);
        functionData[51][0]=new Byte(Ptg.CLASS_VALUE);functionData[51][1]=new byte[] {Ptg.CLASS_REF};functionData[51][2]=new Integer(-1);
        functionData[52][0]=new Byte(Ptg.CLASS_VALUE);functionData[52][1]=new byte[] {Ptg.CLASS_REF};functionData[52][2]=new Integer(-1);
        
        
        functionData[56][0]=new Byte(Ptg.CLASS_VALUE);functionData[56][1]=new byte[] {Ptg.CLASS_VALUE};functionData[56][2]=new Integer(-1);
        functionData[57][0]=new Byte(Ptg.CLASS_VALUE);functionData[57][1]=new byte[] {Ptg.CLASS_VALUE};functionData[57][2]=new Integer(-1);
        functionData[58][0]=new Byte(Ptg.CLASS_VALUE);functionData[58][1]=new byte[] {Ptg.CLASS_VALUE};functionData[58][2]=new Integer(-1);
        functionData[59][0]=new Byte(Ptg.CLASS_VALUE);functionData[59][1]=new byte[] {Ptg.CLASS_VALUE};functionData[59][2]=new Integer(-1);
        functionData[60][0]=new Byte(Ptg.CLASS_VALUE);functionData[60][1]=new byte[] {Ptg.CLASS_VALUE};functionData[60][2]=new Integer(-1);
        functionData[61][0]=new Byte(Ptg.CLASS_VALUE);functionData[61][1]=new byte[] {Ptg.CLASS_VALUE};functionData[61][2]=new Integer(3);
        functionData[62][0]=new Byte(Ptg.CLASS_VALUE);functionData[62][1]=new byte[] {Ptg.CLASS_REF};functionData[62][2]=new Integer(-1);
        functionData[63][0]=new Byte(Ptg.CLASS_VALUE);functionData[63][1]=new byte[] {Ptg.CLASS_REF};functionData[63][2]=new Integer(1);
        functionData[64][0]=new Byte(Ptg.CLASS_VALUE);functionData[64][1]=new byte[] {Ptg.CLASS_VALUE, Ptg.CLASS_REF};functionData[64][2]=new Integer(-1);
        functionData[65][0]=new Byte(Ptg.CLASS_VALUE);functionData[65][1]=new byte[] {Ptg.CLASS_VALUE};functionData[65][2]=new Integer(3);
        functionData[66][0]=new Byte(Ptg.CLASS_VALUE);functionData[66][1]=new byte[] {Ptg.CLASS_VALUE};functionData[66][2]=new Integer(3);
        functionData[67][0]=new Byte(Ptg.CLASS_VALUE);functionData[67][1]=new byte[] {Ptg.CLASS_VALUE};functionData[67][2]=new Integer(1);
        functionData[68][0]=new Byte(Ptg.CLASS_VALUE);functionData[68][1]=new byte[] {Ptg.CLASS_VALUE};functionData[68][2]=new Integer(1);
        functionData[69][0]=new Byte(Ptg.CLASS_VALUE);functionData[69][1]=new byte[] {Ptg.CLASS_VALUE};functionData[69][2]=new Integer(1);
        functionData[70][0]=new Byte(Ptg.CLASS_VALUE);functionData[70][1]=new byte[] {Ptg.CLASS_VALUE};functionData[70][2]=new Integer(-1);
        functionData[71][0]=new Byte(Ptg.CLASS_VALUE);functionData[71][1]=new byte[] {Ptg.CLASS_VALUE};functionData[71][2]=new Integer(1);
        functionData[72][0]=new Byte(Ptg.CLASS_VALUE);functionData[72][1]=new byte[] {Ptg.CLASS_VALUE};functionData[72][2]=new Integer(1);
        functionData[73][0]=new Byte(Ptg.CLASS_VALUE);functionData[73][1]=new byte[] {Ptg.CLASS_VALUE};functionData[73][2]=new Integer(1);
        functionData[74][0]=new Byte(Ptg.CLASS_VALUE);functionData[74][1]=new byte[] {Ptg.CLASS_REF};functionData[74][2]=new Integer(1);
        functionData[75][0]=new Byte(Ptg.CLASS_VALUE);functionData[75][1]=new byte[] {Ptg.CLASS_REF};functionData[75][2]=new Integer(1);
        functionData[76][0]=new Byte(Ptg.CLASS_VALUE);functionData[76][1]=new byte[] {Ptg.CLASS_REF};functionData[76][2]=new Integer(1);
        functionData[77][0]=new Byte(Ptg.CLASS_VALUE);functionData[77][1]=new byte[] {Ptg.CLASS_REF};functionData[77][2]=new Integer(1);
        functionData[78][0]=new Byte(Ptg.CLASS_VALUE);functionData[78][1]=new byte[] {Ptg.CLASS_VALUE};functionData[78][2]=new Integer(-1);
        
        
        
        functionData[82][0]=new Byte(Ptg.CLASS_VALUE);functionData[82][1]=new byte[] {Ptg.CLASS_VALUE};functionData[82][2]=new Integer(-1);
        functionData[83][0]=new Byte(Ptg.CLASS_VALUE);functionData[83][1]=new byte[] {Ptg.CLASS_VALUE};functionData[83][2]=new Integer(1);
        
        
        functionData[86][0]=new Byte(Ptg.CLASS_VALUE);functionData[86][1]=new byte[] {Ptg.CLASS_VALUE};functionData[86][2]=new Integer(1);
        
        
        
        
        
        
        
        
        
        
        functionData[97][0]=new Byte(Ptg.CLASS_VALUE);functionData[97][1]=new byte[] {Ptg.CLASS_VALUE};functionData[97][2]=new Integer(2);
        functionData[98][0]=new Byte(Ptg.CLASS_VALUE);functionData[98][1]=new byte[] {Ptg.CLASS_VALUE};functionData[98][2]=new Integer(1);
        functionData[99][0]=new Byte(Ptg.CLASS_VALUE);functionData[99][1]=new byte[] {Ptg.CLASS_VALUE};functionData[99][2]=new Integer(1);
        
        functionData[101][0]=new Byte(Ptg.CLASS_VALUE);functionData[101][1]=new byte[] {Ptg.CLASS_REF};functionData[101][2]=new Integer(-1);
        functionData[102][0]=new Byte(Ptg.CLASS_VALUE);functionData[102][1]=new byte[] {Ptg.CLASS_REF};functionData[102][2]=new Integer(-1);
        
        
        functionData[105][0]=new Byte(Ptg.CLASS_VALUE);functionData[105][1]=new byte[] {Ptg.CLASS_REF};functionData[105][2]=new Integer(1);
        
        
        
        functionData[109][0]=new Byte(Ptg.CLASS_VALUE);functionData[109][1]=new byte[] {Ptg.CLASS_VALUE};functionData[109][2]=new Integer(-1);
        
        functionData[111][0]=new Byte(Ptg.CLASS_VALUE);functionData[111][1]=new byte[] {Ptg.CLASS_VALUE};functionData[111][2]=new Integer(1);
        functionData[112][0]=new Byte(Ptg.CLASS_VALUE);functionData[112][1]=new byte[] {Ptg.CLASS_VALUE};functionData[112][2]=new Integer(1);
        functionData[113][0]=new Byte(Ptg.CLASS_VALUE);functionData[113][1]=new byte[] {Ptg.CLASS_VALUE};functionData[113][2]=new Integer(1);
        functionData[114][0]=new Byte(Ptg.CLASS_VALUE);functionData[114][1]=new byte[] {Ptg.CLASS_VALUE};functionData[114][2]=new Integer(1);
        functionData[115][0]=new Byte(Ptg.CLASS_VALUE);functionData[115][1]=new byte[] {Ptg.CLASS_VALUE};functionData[115][2]=new Integer(-1);
        functionData[116][0]=new Byte(Ptg.CLASS_VALUE);functionData[116][1]=new byte[] {Ptg.CLASS_VALUE};functionData[116][2]=new Integer(-1);
        functionData[117][0]=new Byte(Ptg.CLASS_VALUE);functionData[117][1]=new byte[] {Ptg.CLASS_VALUE};functionData[117][2]=new Integer(2);
        functionData[118][0]=new Byte(Ptg.CLASS_VALUE);functionData[118][1]=new byte[] {Ptg.CLASS_VALUE};functionData[118][2]=new Integer(1);
        functionData[119][0]=new Byte(Ptg.CLASS_VALUE);functionData[119][1]=new byte[] {Ptg.CLASS_VALUE};functionData[119][2]=new Integer(4);
        functionData[120][0]=new Byte(Ptg.CLASS_VALUE);functionData[120][1]=new byte[] {Ptg.CLASS_VALUE};functionData[120][2]=new Integer(-1);
        functionData[121][0]=new Byte(Ptg.CLASS_VALUE);functionData[121][1]=new byte[] {Ptg.CLASS_VALUE};functionData[121][2]=new Integer(1);
        
        
        functionData[124][0]=new Byte(Ptg.CLASS_VALUE);functionData[124][1]=new byte[] {Ptg.CLASS_VALUE};functionData[124][2]=new Integer(-1);
        functionData[125][0]=new Byte(Ptg.CLASS_VALUE);functionData[125][1]=new byte[] {Ptg.CLASS_VALUE};functionData[125][2]=new Integer(-1);
        functionData[126][0]=new Byte(Ptg.CLASS_VALUE);functionData[126][1]=new byte[] {Ptg.CLASS_VALUE};functionData[126][2]=new Integer(1);
        functionData[127][0]=new Byte(Ptg.CLASS_VALUE);functionData[127][1]=new byte[] {Ptg.CLASS_VALUE};functionData[127][2]=new Integer(1);
        functionData[128][0]=new Byte(Ptg.CLASS_VALUE);functionData[128][1]=new byte[] {Ptg.CLASS_VALUE};functionData[128][2]=new Integer(1);
        functionData[129][0]=new Byte(Ptg.CLASS_VALUE);functionData[129][1]=new byte[] {Ptg.CLASS_VALUE};functionData[129][2]=new Integer(1);
        functionData[130][0]=new Byte(Ptg.CLASS_VALUE);functionData[130][1]=new byte[] {Ptg.CLASS_REF};functionData[130][2]=new Integer(1);
        functionData[131][0]=new Byte(Ptg.CLASS_VALUE);functionData[131][1]=new byte[] {Ptg.CLASS_REF};functionData[131][2]=new Integer(1);
        
        
        
        
        
        
        
        
        functionData[140][0]=new Byte(Ptg.CLASS_VALUE);functionData[140][1]=new byte[] {Ptg.CLASS_VALUE};functionData[140][2]=new Integer(1);
        functionData[141][0]=new Byte(Ptg.CLASS_VALUE);functionData[141][1]=new byte[] {Ptg.CLASS_VALUE};functionData[141][2]=new Integer(1);
        functionData[142][0]=new Byte(Ptg.CLASS_VALUE);functionData[142][1]=new byte[] {Ptg.CLASS_VALUE};functionData[142][2]=new Integer(3);
        
        
        
        
        
        functionData[148][0]=new Byte(Ptg.CLASS_VALUE);functionData[148][1]=new byte[] {Ptg.CLASS_VALUE};functionData[148][2]=new Integer(-1);
        
        functionData[150][0]=new Byte(Ptg.CLASS_VALUE);functionData[150][1]=new byte[] {Ptg.CLASS_VALUE};functionData[150][2]=new Integer(-1);
        
        
        
        
        
        
        
        
        
        
        
        functionData[162][0]=new Byte(Ptg.CLASS_VALUE);functionData[162][1]=new byte[] {Ptg.CLASS_VALUE};functionData[162][2]=new Integer(1);
        functionData[163][0]=new Byte(Ptg.CLASS_VALUE);functionData[163][1]=new byte[] {Ptg.CLASS_ARRAY};functionData[163][2]=new Integer(1);
        functionData[164][0]=new Byte(Ptg.CLASS_VALUE);functionData[164][1]=new byte[] {Ptg.CLASS_ARRAY};functionData[164][2]=new Integer(1);
        functionData[165][0]=new Byte(Ptg.CLASS_VALUE);functionData[165][1]=new byte[] {Ptg.CLASS_ARRAY};functionData[165][2]=new Integer(2);
        functionData[166][0]=new Byte(Ptg.CLASS_VALUE);functionData[166][1]=new byte[] {Ptg.CLASS_VALUE};functionData[166][2]=new Integer(-1);
        functionData[167][0]=new Byte(Ptg.CLASS_VALUE);functionData[167][1]=new byte[] {Ptg.CLASS_VALUE};functionData[167][2]=new Integer(-1);
        functionData[168][0]=new Byte(Ptg.CLASS_VALUE);functionData[168][1]=new byte[] {Ptg.CLASS_REF};functionData[168][2]=new Integer(-1);
        
        
        
        
        
        
        
        
        
        
        functionData[183][0]=new Byte(Ptg.CLASS_VALUE);functionData[183][1]=new byte[] {Ptg.CLASS_REF};functionData[183][2]=new Integer(-1);
        functionData[184][0]=new Byte(Ptg.CLASS_VALUE);functionData[184][1]=new byte[] {Ptg.CLASS_VALUE};functionData[184][2]=new Integer(1);
        
        
        
        
        functionData[189][0]=new Byte(Ptg.CLASS_VALUE);functionData[189][1]=new byte[] {Ptg.CLASS_REF};functionData[189][2]=new Integer(3);
        functionData[190][0]=new Byte(Ptg.CLASS_VALUE);functionData[190][1]=new byte[] {Ptg.CLASS_VALUE};functionData[190][2]=new Integer(1);
        
        
        functionData[193][0]=new Byte(Ptg.CLASS_VALUE);functionData[193][1]=new byte[] {Ptg.CLASS_REF};functionData[193][2]=new Integer(-1);
        functionData[194][0]=new Byte(Ptg.CLASS_VALUE);functionData[194][1]=new byte[] {Ptg.CLASS_REF};functionData[194][2]=new Integer(-1);
        functionData[195][0]=new Byte(Ptg.CLASS_VALUE);functionData[195][1]=new byte[] {Ptg.CLASS_REF};functionData[195][2]=new Integer(3);
        functionData[196][0]=new Byte(Ptg.CLASS_VALUE);functionData[196][1]=new byte[] {Ptg.CLASS_REF};functionData[196][2]=new Integer(3);
        functionData[197][0]=new Byte(Ptg.CLASS_VALUE);functionData[197][1]=new byte[] {Ptg.CLASS_VALUE};functionData[197][2]=new Integer(-1);
        functionData[198][0]=new Byte(Ptg.CLASS_VALUE);functionData[198][1]=new byte[] {Ptg.CLASS_VALUE};functionData[198][2]=new Integer(1);
        functionData[199][0]=new Byte(Ptg.CLASS_VALUE);functionData[199][1]=new byte[] {Ptg.CLASS_REF};functionData[199][2]=new Integer(3);
        
        
        functionData[204][0]=new Byte(Ptg.CLASS_VALUE);functionData[204][1]=new byte[] {Ptg.CLASS_VALUE};functionData[204][2]=new Integer(-1);
        functionData[205][0]=new Byte(Ptg.CLASS_VALUE);functionData[205][1]=new byte[] {Ptg.CLASS_VALUE};functionData[205][2]=new Integer(-1);
        functionData[206][0]=new Byte(Ptg.CLASS_VALUE);functionData[206][1]=new byte[] {Ptg.CLASS_VALUE};functionData[206][2]=new Integer(-1);
        functionData[207][0]=new Byte(Ptg.CLASS_VALUE);functionData[207][1]=new byte[] {Ptg.CLASS_VALUE};functionData[207][2]=new Integer(3);
        functionData[208][0]=new Byte(Ptg.CLASS_VALUE);functionData[208][1]=new byte[] {Ptg.CLASS_VALUE};functionData[208][2]=new Integer(1);
        functionData[209][0]=new Byte(Ptg.CLASS_VALUE);functionData[209][1]=new byte[] {Ptg.CLASS_VALUE};functionData[209][2]=new Integer(2);
        functionData[210][0]=new Byte(Ptg.CLASS_VALUE);functionData[210][1]=new byte[] {Ptg.CLASS_VALUE};functionData[210][2]=new Integer(2);
        functionData[211][0]=new Byte(Ptg.CLASS_VALUE);functionData[211][1]=new byte[] {Ptg.CLASS_VALUE};functionData[211][2]=new Integer(1);
        functionData[212][0]=new Byte(Ptg.CLASS_VALUE);functionData[212][1]=new byte[] {Ptg.CLASS_VALUE};functionData[212][2]=new Integer(1);
        functionData[213][0]=new Byte(Ptg.CLASS_VALUE);functionData[213][1]=new byte[] {Ptg.CLASS_REF};functionData[213][2]=new Integer(-1);
        functionData[214][0]=new Byte(Ptg.CLASS_VALUE);functionData[214][1]=new byte[] {Ptg.CLASS_VALUE};functionData[214][2]=new Integer(-1);
        
        
        
        
        functionData[221][0]=new Byte(Ptg.CLASS_VALUE);functionData[221][1]=new byte[] {Ptg.CLASS_REF};functionData[221][2]=new Integer(1);
        functionData[222][0]=new Byte(Ptg.CLASS_VALUE);functionData[222][1]=new byte[] {Ptg.CLASS_VALUE};functionData[222][2]=new Integer(-1);
        functionData[227][0]=new Byte(Ptg.CLASS_VALUE);functionData[227][1]=new byte[] {Ptg.CLASS_REF};functionData[227][2]=new Integer(-1);
        functionData[228][0]=new Byte(Ptg.CLASS_VALUE);functionData[228][1]=new byte[] {Ptg.CLASS_ARRAY};functionData[228][2]=new Integer(-1);
        functionData[229][0]=new Byte(Ptg.CLASS_VALUE);functionData[229][1]=new byte[] {Ptg.CLASS_VALUE};functionData[229][2]=new Integer(1);
        functionData[230][0]=new Byte(Ptg.CLASS_VALUE);functionData[230][1]=new byte[] {Ptg.CLASS_VALUE};functionData[230][2]=new Integer(1);
        functionData[231][0]=new Byte(Ptg.CLASS_VALUE);functionData[231][1]=new byte[] {Ptg.CLASS_VALUE};functionData[231][2]=new Integer(1);
        functionData[232][0]=new Byte(Ptg.CLASS_VALUE);functionData[232][1]=new byte[] {Ptg.CLASS_VALUE};functionData[232][2]=new Integer(1);
        functionData[233][0]=new Byte(Ptg.CLASS_VALUE);functionData[233][1]=new byte[] {Ptg.CLASS_VALUE};functionData[233][2]=new Integer(1);
        functionData[234][0]=new Byte(Ptg.CLASS_VALUE);functionData[234][1]=new byte[] {Ptg.CLASS_VALUE};functionData[234][2]=new Integer(1);
        functionData[235][0]=new Byte(Ptg.CLASS_VALUE);functionData[235][1]=new byte[] {Ptg.CLASS_REF};functionData[235][2]=new Integer(3);
        
        
        
        
        
        
        
        
        functionData[244][0]=new Byte(Ptg.CLASS_VALUE);functionData[244][1]=new byte[] {Ptg.CLASS_VALUE};functionData[244][2]=new Integer(2);
        
        
        
        
        
        functionData[252][0]=new Byte(Ptg.CLASS_VALUE);functionData[252][1]=new byte[] {Ptg.CLASS_REF};functionData[252][2]=new Integer(2);
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        functionData[269][0]=new Byte(Ptg.CLASS_VALUE);functionData[269][1]=new byte[] {Ptg.CLASS_REF};functionData[269][2]=new Integer(-1);
        functionData[270][0]=new Byte(Ptg.CLASS_VALUE);functionData[270][1]=new byte[] {Ptg.CLASS_VALUE};functionData[270][2]=new Integer(-1);
        functionData[271][0]=new Byte(Ptg.CLASS_VALUE);functionData[271][1]=new byte[] {Ptg.CLASS_VALUE};functionData[271][2]=new Integer(1);
        functionData[272][0]=new Byte(Ptg.CLASS_VALUE);functionData[272][1]=new byte[] {Ptg.CLASS_VALUE};functionData[272][2]=new Integer(-1);
        functionData[273][0]=new Byte(Ptg.CLASS_VALUE);functionData[273][1]=new byte[] {Ptg.CLASS_VALUE};functionData[273][2]=new Integer(4);
        functionData[274][0]=new Byte(Ptg.CLASS_VALUE);functionData[274][1]=new byte[] {Ptg.CLASS_VALUE};functionData[274][2]=new Integer(2);
        functionData[275][0]=new Byte(Ptg.CLASS_VALUE);functionData[275][1]=new byte[] {Ptg.CLASS_VALUE};functionData[275][2]=new Integer(2);
        functionData[276][0]=new Byte(Ptg.CLASS_VALUE);functionData[276][1]=new byte[] {Ptg.CLASS_VALUE};functionData[276][2]=new Integer(2);
        functionData[277][0]=new Byte(Ptg.CLASS_VALUE);functionData[277][1]=new byte[] {Ptg.CLASS_VALUE};functionData[277][2]=new Integer(3);
        functionData[278][0]=new Byte(Ptg.CLASS_VALUE);functionData[278][1]=new byte[] {Ptg.CLASS_VALUE};functionData[278][2]=new Integer(3);
        functionData[279][0]=new Byte(Ptg.CLASS_VALUE);functionData[279][1]=new byte[] {Ptg.CLASS_VALUE};functionData[279][2]=new Integer(1);
        functionData[280][0]=new Byte(Ptg.CLASS_VALUE);functionData[280][1]=new byte[] {Ptg.CLASS_VALUE};functionData[280][2]=new Integer(3);
        functionData[281][0]=new Byte(Ptg.CLASS_VALUE);functionData[281][1]=new byte[] {Ptg.CLASS_VALUE};functionData[281][2]=new Integer(3);
        functionData[282][0]=new Byte(Ptg.CLASS_VALUE);functionData[282][1]=new byte[] {Ptg.CLASS_VALUE};functionData[282][2]=new Integer(3);
        functionData[283][0]=new Byte(Ptg.CLASS_VALUE);functionData[283][1]=new byte[] {Ptg.CLASS_VALUE};functionData[283][2]=new Integer(1);
        functionData[284][0]=new Byte(Ptg.CLASS_VALUE);functionData[284][1]=new byte[] {Ptg.CLASS_VALUE};functionData[284][2]=new Integer(1);
        functionData[285][0]=new Byte(Ptg.CLASS_VALUE);functionData[285][1]=new byte[] {Ptg.CLASS_VALUE};functionData[285][2]=new Integer(2);
        functionData[286][0]=new Byte(Ptg.CLASS_VALUE);functionData[286][1]=new byte[] {Ptg.CLASS_VALUE};functionData[286][2]=new Integer(4);
        functionData[287][0]=new Byte(Ptg.CLASS_VALUE);functionData[287][1]=new byte[] {Ptg.CLASS_VALUE};functionData[287][2]=new Integer(3);
        functionData[288][0]=new Byte(Ptg.CLASS_VALUE);functionData[288][1]=new byte[] {Ptg.CLASS_VALUE};functionData[288][2]=new Integer(2);
        functionData[289][0]=new Byte(Ptg.CLASS_VALUE);functionData[289][1]=new byte[] {Ptg.CLASS_VALUE};functionData[289][2]=new Integer(4);
        functionData[290][0]=new Byte(Ptg.CLASS_VALUE);functionData[290][1]=new byte[] {Ptg.CLASS_VALUE};functionData[290][2]=new Integer(3);
        functionData[291][0]=new Byte(Ptg.CLASS_VALUE);functionData[291][1]=new byte[] {Ptg.CLASS_VALUE};functionData[291][2]=new Integer(3);
        functionData[292][0]=new Byte(Ptg.CLASS_VALUE);functionData[292][1]=new byte[] {Ptg.CLASS_VALUE};functionData[292][2]=new Integer(3);
        functionData[293][0]=new Byte(Ptg.CLASS_VALUE);functionData[293][1]=new byte[] {Ptg.CLASS_VALUE};functionData[293][2]=new Integer(4);
        functionData[294][0]=new Byte(Ptg.CLASS_VALUE);functionData[294][1]=new byte[] {Ptg.CLASS_VALUE};functionData[294][2]=new Integer(1);
        functionData[295][0]=new Byte(Ptg.CLASS_VALUE);functionData[295][1]=new byte[] {Ptg.CLASS_VALUE};functionData[295][2]=new Integer(3);
        functionData[296][0]=new Byte(Ptg.CLASS_VALUE);functionData[296][1]=new byte[] {Ptg.CLASS_VALUE};functionData[296][2]=new Integer(1);
        functionData[297][0]=new Byte(Ptg.CLASS_VALUE);functionData[297][1]=new byte[] {Ptg.CLASS_VALUE};functionData[297][2]=new Integer(3);
        functionData[298][0]=new Byte(Ptg.CLASS_VALUE);functionData[298][1]=new byte[] {Ptg.CLASS_VALUE};functionData[298][2]=new Integer(1);
        functionData[299][0]=new Byte(Ptg.CLASS_VALUE);functionData[299][1]=new byte[] {Ptg.CLASS_VALUE};functionData[299][2]=new Integer(2);
        functionData[300][0]=new Byte(Ptg.CLASS_VALUE);functionData[300][1]=new byte[] {Ptg.CLASS_VALUE};functionData[300][2]=new Integer(3);
        functionData[301][0]=new Byte(Ptg.CLASS_VALUE);functionData[301][1]=new byte[] {Ptg.CLASS_VALUE};functionData[301][2]=new Integer(3);
        functionData[302][0]=new Byte(Ptg.CLASS_VALUE);functionData[302][1]=new byte[] {Ptg.CLASS_VALUE};functionData[302][2]=new Integer(4);
        functionData[303][0]=new Byte(Ptg.CLASS_VALUE);functionData[303][1]=new byte[] {Ptg.CLASS_ARRAY};functionData[303][2]=new Integer(2);
        functionData[304][0]=new Byte(Ptg.CLASS_VALUE);functionData[304][1]=new byte[] {Ptg.CLASS_ARRAY};functionData[304][2]=new Integer(2);
        functionData[305][0]=new Byte(Ptg.CLASS_VALUE);functionData[305][1]=new byte[] {Ptg.CLASS_ARRAY};functionData[305][2]=new Integer(2);
        functionData[306][0]=new Byte(Ptg.CLASS_VALUE);functionData[306][1]=new byte[] {Ptg.CLASS_ARRAY};functionData[306][2]=new Integer(2);
        functionData[307][0]=new Byte(Ptg.CLASS_VALUE);functionData[307][1]=new byte[] {Ptg.CLASS_ARRAY};functionData[307][2]=new Integer(2);
        functionData[308][0]=new Byte(Ptg.CLASS_VALUE);functionData[308][1]=new byte[] {Ptg.CLASS_ARRAY};functionData[308][2]=new Integer(2);
        functionData[309][0]=new Byte(Ptg.CLASS_VALUE);functionData[309][1]=new byte[] {Ptg.CLASS_ARRAY};functionData[309][2]=new Integer(3);
        functionData[310][0]=new Byte(Ptg.CLASS_VALUE);functionData[310][1]=new byte[] {Ptg.CLASS_ARRAY};functionData[310][2]=new Integer(2);
        functionData[311][0]=new Byte(Ptg.CLASS_VALUE);functionData[311][1]=new byte[] {Ptg.CLASS_ARRAY};functionData[311][2]=new Integer(2);
        functionData[312][0]=new Byte(Ptg.CLASS_VALUE);functionData[312][1]=new byte[] {Ptg.CLASS_ARRAY};functionData[312][2]=new Integer(2);
        functionData[313][0]=new Byte(Ptg.CLASS_VALUE);functionData[313][1]=new byte[] {Ptg.CLASS_ARRAY};functionData[313][2]=new Integer(2);
        functionData[314][0]=new Byte(Ptg.CLASS_VALUE);functionData[314][1]=new byte[] {Ptg.CLASS_ARRAY};functionData[314][2]=new Integer(2);
        functionData[315][0]=new Byte(Ptg.CLASS_VALUE);functionData[315][1]=new byte[] {Ptg.CLASS_ARRAY};functionData[315][2]=new Integer(2);
        functionData[316][0]=new Byte(Ptg.CLASS_VALUE);functionData[316][1]=new byte[] {Ptg.CLASS_VALUE};functionData[316][2]=new Integer(4);
        functionData[317][0]=new Byte(Ptg.CLASS_VALUE);functionData[317][1]=new byte[] {Ptg.CLASS_VALUE};functionData[317][2]=new Integer(-1);
        functionData[318][0]=new Byte(Ptg.CLASS_VALUE);functionData[318][1]=new byte[] {Ptg.CLASS_REF};functionData[318][2]=new Integer(-1);
        functionData[319][0]=new Byte(Ptg.CLASS_VALUE);functionData[319][1]=new byte[] {Ptg.CLASS_REF};functionData[319][2]=new Integer(-1);
        functionData[320][0]=new Byte(Ptg.CLASS_VALUE);functionData[320][1]=new byte[] {Ptg.CLASS_REF};functionData[320][2]=new Integer(-1);
        functionData[321][0]=new Byte(Ptg.CLASS_VALUE);functionData[321][1]=new byte[] {Ptg.CLASS_REF};functionData[321][2]=new Integer(-1);
        functionData[322][0]=new Byte(Ptg.CLASS_VALUE);functionData[322][1]=new byte[] {Ptg.CLASS_REF};functionData[322][2]=new Integer(-1);
        functionData[323][0]=new Byte(Ptg.CLASS_VALUE);functionData[323][1]=new byte[] {Ptg.CLASS_REF};functionData[323][2]=new Integer(-1);
        functionData[324][0]=new Byte(Ptg.CLASS_VALUE);functionData[324][1]=new byte[] {Ptg.CLASS_VALUE};functionData[324][2]=new Integer(-1);
        functionData[325][0]=new Byte(Ptg.CLASS_VALUE);functionData[325][1]=new byte[] {Ptg.CLASS_VALUE};functionData[325][2]=new Integer(2);
        functionData[326][0]=new Byte(Ptg.CLASS_VALUE);functionData[326][1]=new byte[] {Ptg.CLASS_VALUE};functionData[326][2]=new Integer(2);
        functionData[327][0]=new Byte(Ptg.CLASS_VALUE);functionData[327][1]=new byte[] {Ptg.CLASS_VALUE};functionData[327][2]=new Integer(2);
        functionData[328][0]=new Byte(Ptg.CLASS_VALUE);functionData[328][1]=new byte[] {Ptg.CLASS_VALUE};functionData[328][2]=new Integer(2);
        functionData[329][0]=new Byte(Ptg.CLASS_VALUE);functionData[329][1]=new byte[] {Ptg.CLASS_VALUE};functionData[329][2]=new Integer(-1);
        functionData[330][0]=new Byte(Ptg.CLASS_VALUE);functionData[330][1]=new byte[] {Ptg.CLASS_ARRAY};functionData[330][2]=new Integer(-1);
        functionData[331][0]=new Byte(Ptg.CLASS_VALUE);functionData[331][1]=new byte[] {Ptg.CLASS_VALUE};functionData[331][2]=new Integer(2);
        functionData[332][0]=new Byte(Ptg.CLASS_VALUE);functionData[332][1]=new byte[] {Ptg.CLASS_VALUE};functionData[332][2]=new Integer(2);
        
        
        functionData[336][0]=new Byte(Ptg.CLASS_VALUE);functionData[336][1]=new byte[] {Ptg.CLASS_VALUE};functionData[336][2]=new Integer(-1);
        functionData[337][0]=new Byte(Ptg.CLASS_VALUE);functionData[337][1]=new byte[] {Ptg.CLASS_VALUE};functionData[337][2]=new Integer(2);
        
        
        
        
        functionData[342][0]=new Byte(Ptg.CLASS_VALUE);functionData[342][1]=new byte[] {Ptg.CLASS_VALUE};functionData[342][2]=new Integer(1);
        functionData[343][0]=new Byte(Ptg.CLASS_VALUE);functionData[343][1]=new byte[] {Ptg.CLASS_VALUE};functionData[343][2]=new Integer(1);
        functionData[344][0]=new Byte(Ptg.CLASS_VALUE);functionData[344][1]=new byte[] {Ptg.CLASS_REF};functionData[344][2]=new Integer(-1);
        functionData[345][0]=new Byte(Ptg.CLASS_VALUE);functionData[345][1]=new byte[] {Ptg.CLASS_REF};functionData[345][2]=new Integer(-1);
        functionData[346][0]=new Byte(Ptg.CLASS_VALUE);functionData[346][1]=new byte[] {Ptg.CLASS_VALUE};functionData[346][2]=new Integer(2);
        functionData[347][0]=new Byte(Ptg.CLASS_VALUE);functionData[347][1]=new byte[] {Ptg.CLASS_REF};functionData[347][2]=new Integer(1);
        
        
        functionData[350][0]=new Byte(Ptg.CLASS_VALUE);functionData[350][1]=new byte[] {Ptg.CLASS_VALUE};functionData[350][2]=new Integer(4);
        
        functionData[352][0]=new Byte(Ptg.CLASS_VALUE);functionData[352][1]=new byte[] {Ptg.CLASS_VALUE};functionData[352][2]=new Integer(1);
        
        functionData[354][0]=new Byte(Ptg.CLASS_VALUE);functionData[354][1]=new byte[] {Ptg.CLASS_VALUE};functionData[354][2]=new Integer(-1);
        
        
        
        functionData[358][0]=new Byte(Ptg.CLASS_VALUE);functionData[358][1]=new byte[] {Ptg.CLASS_VALUE};functionData[358][2]=new Integer(2);
        functionData[359][0]=new Byte(Ptg.CLASS_VALUE);functionData[359][1]=new byte[] {Ptg.CLASS_VALUE};functionData[359][2]=new Integer(-1);
        functionData[360][0]=new Byte(Ptg.CLASS_VALUE);functionData[360][1]=new byte[] {Ptg.CLASS_REF};functionData[360][2]=new Integer(1);
        functionData[361][0]=new Byte(Ptg.CLASS_VALUE);functionData[361][1]=new byte[] {Ptg.CLASS_REF};functionData[361][2]=new Integer(-1);
        functionData[362][0]=new Byte(Ptg.CLASS_VALUE);functionData[362][1]=new byte[] {Ptg.CLASS_REF};functionData[362][2]=new Integer(-1);
        functionData[363][0]=new Byte(Ptg.CLASS_VALUE);functionData[363][1]=new byte[] {Ptg.CLASS_REF};functionData[363][2]=new Integer(-1);
        functionData[364][0]=new Byte(Ptg.CLASS_VALUE);functionData[364][1]=new byte[] {Ptg.CLASS_REF};functionData[364][2]=new Integer(-1);
        functionData[365][0]=new Byte(Ptg.CLASS_VALUE);functionData[365][1]=new byte[] {Ptg.CLASS_REF};functionData[365][2]=new Integer(-1);
        functionData[366][0]=new Byte(Ptg.CLASS_VALUE);functionData[366][1]=new byte[] {Ptg.CLASS_REF};functionData[366][2]=new Integer(-1);
        functionData[367][0]=new Byte(Ptg.CLASS_VALUE);functionData[367][1]=new byte[] {Ptg.CLASS_REF};functionData[367][2]=new Integer(-1);
        
        
        return functionData;
    }

    public byte getDefaultOperandClass() {
        return returnClass;
    }
    
    public byte getParameterClass(int index) {
        try {
            return paramClass[index];
        } catch (ArrayIndexOutOfBoundsException aioobe) {
            return paramClass[paramClass.length - 1];
        }
    }
}

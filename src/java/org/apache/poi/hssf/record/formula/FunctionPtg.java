package org.apache.poi.hssf.record.formula;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.BinaryTree;
/**
 * This class provides functions with variable arguments.  
 * @author  Avik Sengupta
 * @author Andrew C. Oliver (acoliver at apache dot org)
 */
public class FunctionPtg extends OperationPtg {
    public final static short sid  = 0x22;
    private final static int  SIZE = 4;    
    
    private static BinaryTree map = produceHash(); 
    private static Object[][] functionData = produceFunctionData();
    private byte returnClass;
    private byte[] paramClass;
    
    private byte field_1_num_args;
    private short field_2_fnc_index;
    
        
    /**Creates new function pointer from a byte array 
     * usually called while reading an excel file. 
     */
    public FunctionPtg(byte[] data, int offset) {
        offset++;
        field_1_num_args = data[ offset + 0 ];
        field_2_fnc_index  = LittleEndian.getShort(data,offset + 1 );
    }
    
    /**
     * Create a function ptg from a string tokenised by the parser
     */
    protected FunctionPtg(String pName, byte pNumOperands) {
        field_1_num_args = pNumOperands;
        field_2_fnc_index = lookupIndex(pName);
        try{
            returnClass = ( (Byte) functionData[field_2_fnc_index][0]).byteValue();
            paramClass = (byte[]) functionData[field_2_fnc_index][1];
        } catch (NullPointerException npe ) {
            returnClass = Ptg.CLASS_VALUE;
            paramClass = new byte[] {Ptg.CLASS_VALUE};
        }
    }
    
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
    
    public int getNumberOfOperands() {
        return field_1_num_args;
    }
    
    public short getFunctionIndex() {
        return field_2_fnc_index;
    }
    
    public String getName() {
        return lookupName(field_2_fnc_index);
    }
    
    public String toFormulaString() {
        return getName();
    }
    
    public String toFormulaString(String[] operands) {
        StringBuffer buf = new StringBuffer();
        buf.append(getName()+"(");
        if (operands.length >0) {
            for (int i=0;i<operands.length;i++) {
                buf.append(operands[i]);
                buf.append(',');
            }
            buf.deleteCharAt(buf.length()-1);
        }
        buf.append(")");
        return buf.toString();
    }
    
    
    public void writeBytes(byte[] array, int offset) {
        array[offset+0]=(byte) (sid + ptgClass);
        array[offset+1]=field_1_num_args;
        LittleEndian.putShort(array,offset+2,field_2_fnc_index);
    }
    
    public int getSize() {
        return SIZE;
    }
    
    private String lookupName(short index) {
        return ((String)map.get(new Integer(index))); 
    }
    
    private short lookupIndex(String name) {
        return (short)((Integer)map.getKeyForValue(name)).intValue();
    }
    
    /**
     * Produces the function table hashmap
     */
    private static BinaryTree produceHash() {
        BinaryTree dmap = new BinaryTree();

        dmap.put(new Integer(0),"COUNT");
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
        Object [][] functionData = new Object[368][2];
        
functionData[0][0]=new Byte(Ptg.CLASS_VALUE);functionData[0][1]=new byte[] {Ptg.CLASS_VALUE};
functionData[2][0]=new Byte(Ptg.CLASS_VALUE);functionData[2][1]=new byte[] {Ptg.CLASS_VALUE};
functionData[3][0]=new Byte(Ptg.CLASS_VALUE);functionData[3][1]=new byte[] {Ptg.CLASS_VALUE};
functionData[4][0]=new Byte(Ptg.CLASS_VALUE);functionData[4][1]=new byte[] {Ptg.CLASS_VALUE};
functionData[5][0]=new Byte(Ptg.CLASS_VALUE);functionData[5][1]=new byte[] {Ptg.CLASS_VALUE};
functionData[6][0]=new Byte(Ptg.CLASS_VALUE);functionData[6][1]=new byte[] {Ptg.CLASS_VALUE};
functionData[7][0]=new Byte(Ptg.CLASS_VALUE);functionData[7][1]=new byte[] {Ptg.CLASS_VALUE};
functionData[8][0]=new Byte(Ptg.CLASS_VALUE);functionData[8][1]=new byte[] {Ptg.CLASS_REF};
functionData[9][0]=new Byte(Ptg.CLASS_VALUE);functionData[9][1]=new byte[] {Ptg.CLASS_REF};
functionData[10][0]=new Byte(Ptg.CLASS_VALUE);functionData[10][1]=new byte[] {Ptg.CLASS_VALUE};
functionData[11][0]=new Byte(Ptg.CLASS_VALUE);functionData[11][1]=new byte[] {Ptg.CLASS_VALUE};
functionData[12][0]=new Byte(Ptg.CLASS_VALUE);functionData[12][1]=new byte[] {Ptg.CLASS_VALUE};
functionData[13][0]=new Byte(Ptg.CLASS_VALUE);functionData[13][1]=new byte[] {Ptg.CLASS_VALUE};
functionData[14][0]=new Byte(Ptg.CLASS_VALUE);functionData[14][1]=new byte[] {Ptg.CLASS_VALUE};
functionData[15][0]=new Byte(Ptg.CLASS_VALUE);functionData[15][1]=new byte[] {Ptg.CLASS_VALUE};
functionData[16][0]=new Byte(Ptg.CLASS_VALUE);functionData[16][1]=new byte[] {Ptg.CLASS_VALUE};
functionData[17][0]=new Byte(Ptg.CLASS_VALUE);functionData[17][1]=new byte[] {Ptg.CLASS_VALUE};
functionData[18][0]=new Byte(Ptg.CLASS_VALUE);functionData[18][1]=new byte[] {Ptg.CLASS_VALUE};
functionData[19][0]=new Byte(Ptg.CLASS_VALUE);functionData[19][1]=new byte[] {Ptg.CLASS_VALUE};
functionData[20][0]=new Byte(Ptg.CLASS_VALUE);functionData[20][1]=new byte[] {Ptg.CLASS_VALUE};
functionData[21][0]=new Byte(Ptg.CLASS_VALUE);functionData[21][1]=new byte[] {Ptg.CLASS_VALUE};
functionData[22][0]=new Byte(Ptg.CLASS_VALUE);functionData[22][1]=new byte[] {Ptg.CLASS_VALUE};
functionData[23][0]=new Byte(Ptg.CLASS_VALUE);functionData[23][1]=new byte[] {Ptg.CLASS_VALUE};
functionData[24][0]=new Byte(Ptg.CLASS_VALUE);functionData[24][1]=new byte[] {Ptg.CLASS_VALUE};
functionData[25][0]=new Byte(Ptg.CLASS_VALUE);functionData[25][1]=new byte[] {Ptg.CLASS_VALUE};
functionData[26][0]=new Byte(Ptg.CLASS_VALUE);functionData[26][1]=new byte[] {Ptg.CLASS_VALUE};
functionData[27][0]=new Byte(Ptg.CLASS_VALUE);functionData[27][1]=new byte[] {Ptg.CLASS_VALUE};
       
        return functionData;
    }

    public byte getDefaultOperandClass() {
        return returnClass;
    }
    
    protected byte getParameterClass(int index) {
        try {
            return paramClass[index];
        } catch (ArrayIndexOutOfBoundsException aioobe) {
            return paramClass[paramClass.length - 1];
        }
    }
}

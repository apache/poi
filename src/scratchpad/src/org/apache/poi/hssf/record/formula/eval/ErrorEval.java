/*
 * Created on May 8, 2005
 *
 */
package org.apache.poi.hssf.record.formula.eval;

/**
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 * 
 * Error code reference from OpenOffice documentation: <br/><TABLE WIDTH=575
 * BORDER=1 CELLPADDING=2 CELLSPACING=0 BGCOLOR="#ffffff"> <COL WIDTH=42> <COL
 * WIDTH=118> <COL WIDTH=401>
 * <TR VALIGN=TOP>
 * <TD WIDTH=42 BGCOLOR="#ffffff">
 * <P CLASS="tablehead" ALIGN=LEFT>
 * Error Code
 * </P>
 * </TD>
 * <TD WIDTH=118 BGCOLOR="#ffffff">
 * <P CLASS="tablehead" ALIGN=LEFT>
 * Message
 * </P>
 * </TD>
 * <TD WIDTH=401 BGCOLOR="#ffffff">
 * <P CLASS="tablehead" ALIGN=LEFT>
 * Explanation
 * </P>
 * </TD>
 * </TR>
 * <TR VALIGN=TOP>
 * <TD WIDTH=42 BGCOLOR="#ffffff">
 * <P CLASS="textintable" ALIGN=LEFT>
 * 501
 * </P>
 * </TD>
 * <TD WIDTH=118 BGCOLOR="#ffffff">
 * <P CLASS="textintable" ALIGN=LEFT>
 * Invalid character
 * </P>
 * </TD>
 * <TD WIDTH=401 BGCOLOR="#ffffff">
 * <P CLASS="textintable" ALIGN=LEFT>
 * Character in a formula is not valid, for example, &quot;=1Eq&quot; instead of
 * &quot;=1E2&quot;.
 * </P>
 * </TD>
 * </TR>
 * <TR VALIGN=TOP>
 * <TD WIDTH=42 BGCOLOR="#ffffff">
 * <P CLASS="textintable" ALIGN=LEFT>
 * 502
 * </P>
 * </TD>
 * <TD WIDTH=118 BGCOLOR="#ffffff">
 * <P CLASS="textintable" ALIGN=LEFT>
 * Invalid argument
 * </P>
 * </TD>
 * <TD WIDTH=401 BGCOLOR="#ffffff">
 * <P CLASS="textintable" ALIGN=LEFT>
 * Function argument is not valid, for example, a negative number for the root
 * function.
 * </P>
 * </TD>
 * </TR>
 * <TR VALIGN=TOP>
 * <TD WIDTH=42 BGCOLOR="#ffffff">
 * <P CLASS="textintable" ALIGN=LEFT>
 * 503
 * </P>
 * </TD>
 * <TD WIDTH=118 BGCOLOR="#ffffff">
 * <P CLASS="textintable" ALIGN=LEFT>
 * Invalid floating point operation
 * </P>
 * </TD>
 * <TD WIDTH=401 BGCOLOR="#ffffff">
 * <P CLASS="textintable" ALIGN=LEFT>
 * Division by 0, or another calculation that results in an overflow of the
 * defined value range.
 * </P>
 * </TD>
 * </TR>
 * <TR VALIGN=TOP>
 * <TD WIDTH=42 BGCOLOR="#ffffff">
 * <P CLASS="textintable" ALIGN=LEFT>
 * 504
 * </P>
 * </TD>
 * <TD WIDTH=118 BGCOLOR="#ffffff">
 * <P CLASS="textintable" ALIGN=LEFT>
 * Parameter list error
 * </P>
 * </TD>
 * <TD WIDTH=401 BGCOLOR="#ffffff">
 * <P CLASS="textintable" ALIGN=LEFT>
 * Function parameter is not valid, for example, text instead of a number, or a
 * domain reference instead of cell reference.
 * </P>
 * </TD>
 * </TR>
 * <TR VALIGN=TOP>
 * <TD WIDTH=42 BGCOLOR="#ffffff">
 * <P CLASS="textintable" ALIGN=LEFT>
 * 505
 * </P>
 * </TD>
 * <TD WIDTH=118 BGCOLOR="#ffffff">
 * <P CLASS="textintable" ALIGN=LEFT>
 * Internal syntax error
 * </P>
 * </TD>
 * <TD WIDTH=401 BGCOLOR="#ffffff">
 * <P CLASS="textintable" ALIGN=LEFT>
 * Not used
 * </P>
 * </TD>
 * </TR>
 * <TR VALIGN=TOP>
 * <TD WIDTH=42 BGCOLOR="#ffffff">
 * <P CLASS="textintable" ALIGN=LEFT>
 * 506
 * </P>
 * </TD>
 * <TD WIDTH=118 BGCOLOR="#ffffff">
 * <P CLASS="textintable" ALIGN=LEFT>
 * Invalid semicolon
 * </P>
 * </TD>
 * <TD WIDTH=401 BGCOLOR="#ffffff">
 * <P CLASS="textintable" ALIGN=LEFT>
 * Not used
 * </P>
 * </TD>
 * </TR>
 * <TR VALIGN=TOP>
 * <TD WIDTH=42 BGCOLOR="#ffffff">
 * <P CLASS="textintable" ALIGN=LEFT>
 * 507
 * </P>
 * </TD>
 * <TD WIDTH=118 BGCOLOR="#ffffff">
 * <P CLASS="textintable" ALIGN=LEFT>
 * Error: Pair missing
 * </P>
 * </TD>
 * <TD WIDTH=401 BGCOLOR="#ffffff">
 * <P CLASS="textintable" ALIGN=LEFT>
 * Not used
 * </P>
 * </TD>
 * </TR>
 * <TR VALIGN=TOP>
 * <TD WIDTH=42 BGCOLOR="#ffffff">
 * <P CLASS="textintable" ALIGN=LEFT>
 * 508
 * </P>
 * </TD>
 * <TD WIDTH=118 BGCOLOR="#ffffff">
 * <P CLASS="textintable" ALIGN=LEFT>
 * Error: Pair missing
 * </P>
 * </TD>
 * <TD WIDTH=401 BGCOLOR="#ffffff">
 * <P CLASS="textintable" ALIGN=LEFT>
 * Missing bracket, for example, closing brackets, but no opening brackets
 * </P>
 * </TD>
 * </TR>
 * <TR VALIGN=TOP>
 * <TD WIDTH=42 BGCOLOR="#ffffff">
 * <P CLASS="textintable" ALIGN=LEFT>
 * 509
 * </P>
 * </TD>
 * <TD WIDTH=118 BGCOLOR="#ffffff">
 * <P CLASS="textintable" ALIGN=LEFT>
 * Missing operator
 * </P>
 * </TD>
 * <TD WIDTH=401 BGCOLOR="#ffffff">
 * <P CLASS="textintable" ALIGN=LEFT>
 * Operator is missing, for example, &quot;=2(3+4) * &quot;, where the operator
 * between &quot;2&quot; and &quot;(&quot; is missing.
 * </P>
 * </TD>
 * </TR>
 * <TR VALIGN=TOP>
 * <TD WIDTH=42 BGCOLOR="#ffffff">
 * <P CLASS="textintable" ALIGN=LEFT>
 * 510
 * </P>
 * </TD>
 * <TD WIDTH=118 BGCOLOR="#ffffff">
 * <P CLASS="textintable" ALIGN=LEFT>
 * Missing variable
 * </P>
 * </TD>
 * <TD WIDTH=401 BGCOLOR="#ffffff">
 * <P CLASS="textintable" ALIGN=LEFT>
 * Variable is missing, for example when two operators are together
 * &quot;=1+*2&quot;.
 * </P>
 * </TD>
 * </TR>
 * <TR VALIGN=TOP>
 * <TD WIDTH=42 BGCOLOR="#ffffff">
 * <P CLASS="textintable" ALIGN=LEFT>
 * 511
 * </P>
 * </TD>
 * <TD WIDTH=118 BGCOLOR="#ffffff">
 * <P CLASS="textintable" ALIGN=LEFT>
 * Missing variable
 * </P>
 * </TD>
 * <TD WIDTH=401 BGCOLOR="#ffffff">
 * <P CLASS="textintable" ALIGN=LEFT>
 * Function requires more variables than are provided, for example, AND() and
 * OR().
 * </P>
 * </TD>
 * </TR>
 * <TR VALIGN=TOP>
 * <TD WIDTH=42 BGCOLOR="#ffffff">
 * <P CLASS="textintable" ALIGN=LEFT>
 * 512
 * </P>
 * </TD>
 * <TD WIDTH=118 BGCOLOR="#ffffff">
 * <P CLASS="textintable" ALIGN=LEFT>
 * Formula overflow
 * </P>
 * </TD>
 * <TD WIDTH=401 BGCOLOR="#ffffff">
 * <P CLASS="textintable" ALIGN=LEFT>
 * <B>Compiler: </B> the total number of internal tokens, (that is, operators,
 * variables, brackets) in the formula exceeds 512. <B>Interpreter: </B> the
 * total number of matrices that the formula creates exceeds 150. This includes
 * basic functions that receive too large an array as a parameter (max. 0xFFFE,
 * for example, 65534 bytes).
 * </P>
 * </TD>
 * </TR>
 * <TR VALIGN=TOP>
 * <TD WIDTH=42 BGCOLOR="#ffffff">
 * <P CLASS="textintable" ALIGN=LEFT>
 * 513
 * </P>
 * </TD>
 * <TD WIDTH=118 BGCOLOR="#ffffff">
 * <P CLASS="textintable" ALIGN=LEFT>
 * String overflow
 * </P>
 * </TD>
 * <TD WIDTH=401 BGCOLOR="#ffffff">
 * <P CLASS="textintable" ALIGN=LEFT>
 * <B>Compiler: </B> an identifier in the formula exceeds 64 KB in size.
 * <B>Interpreter: </B> a result of a string operation exceeds 64 KB in size.
 * </P>
 * </TD>
 * </TR>
 * <TR VALIGN=TOP>
 * <TD WIDTH=42 BGCOLOR="#ffffff">
 * <P CLASS="textintable" ALIGN=LEFT>
 * 514
 * </P>
 * </TD>
 * <TD WIDTH=118 BGCOLOR="#ffffff">
 * <P CLASS="textintable" ALIGN=LEFT>
 * Internal overflow
 * </P>
 * </TD>
 * <TD WIDTH=401 BGCOLOR="#ffffff">
 * <P CLASS="textintable" ALIGN=LEFT>
 * Sort operation attempted on too much numerical data (max. 100000) or a
 * calculation stack overflow.
 * </P>
 * </TD>
 * </TR>
 * <TR VALIGN=TOP>
 * <TD WIDTH=42 BGCOLOR="#ffffff">
 * <P CLASS="textintable" ALIGN=LEFT>
 * 515
 * </P>
 * </TD>
 * <TD WIDTH=118 BGCOLOR="#ffffff">
 * <P CLASS="textintable" ALIGN=LEFT>
 * Internal syntax error
 * </P>
 * </TD>
 * <TD WIDTH=401 BGCOLOR="#ffffff">
 * <P CLASS="textintable" ALIGN=LEFT>
 * Not used
 * </P>
 * </TD>
 * </TR>
 * <TR VALIGN=TOP>
 * <TD WIDTH=42 BGCOLOR="#ffffff">
 * <P CLASS="textintable" ALIGN=LEFT>
 * 516
 * </P>
 * </TD>
 * <TD WIDTH=118 BGCOLOR="#ffffff">
 * <P CLASS="textintable" ALIGN=LEFT>
 * Internal syntax error
 * </P>
 * </TD>
 * <TD WIDTH=401 BGCOLOR="#ffffff">
 * <P CLASS="textintable" ALIGN=LEFT>
 * Matrix is expected on the calculation stack, but is not available.
 * </P>
 * </TD>
 * </TR>
 * <TR VALIGN=TOP>
 * <TD WIDTH=42 BGCOLOR="#ffffff">
 * <P CLASS="textintable" ALIGN=LEFT>
 * 517
 * </P>
 * </TD>
 * <TD WIDTH=118 BGCOLOR="#ffffff">
 * <P CLASS="textintable" ALIGN=LEFT>
 * Internal syntax error
 * </P>
 * </TD>
 * <TD WIDTH=401 BGCOLOR="#ffffff">
 * <P CLASS="textintable" ALIGN=LEFT>
 * Unknown code, for example, a document with a newer function is loaded in an
 * older version that does not contain the function.
 * </P>
 * </TD>
 * </TR>
 * <TR VALIGN=TOP>
 * <TD WIDTH=42 BGCOLOR="#ffffff">
 * <P CLASS="textintable" ALIGN=LEFT>
 * 518
 * </P>
 * </TD>
 * <TD WIDTH=118 BGCOLOR="#ffffff">
 * <P CLASS="textintable" ALIGN=LEFT>
 * Internal syntax error
 * </P>
 * </TD>
 * <TD WIDTH=401 BGCOLOR="#ffffff">
 * <P CLASS="textintable" ALIGN=LEFT>
 * Variable is not available
 * </P>
 * </TD>
 * </TR>
 * <TR VALIGN=TOP>
 * <TD WIDTH=42 BGCOLOR="#ffffff">
 * <P CLASS="textintable" ALIGN=LEFT>
 * 519
 * </P>
 * </TD>
 * <TD WIDTH=118 BGCOLOR="#ffffff">
 * <P CLASS="textintable" ALIGN=LEFT>
 * No result (#VALUE is in the cell rather than Err:519!)
 * </P>
 * </TD>
 * <TD WIDTH=401 BGCOLOR="#ffffff">
 * <P CLASS="textintable" ALIGN=LEFT>
 * Formula yields a value that does not corresponds to the definition, or a cell
 * that is referenced in the formula contains text instead of a number.
 * </P>
 * </TD>
 * </TR>
 * <TR VALIGN=TOP>
 * <TD WIDTH=42 BGCOLOR="#ffffff">
 * <P CLASS="textintable" ALIGN=LEFT>
 * 520
 * </P>
 * </TD>
 * <TD WIDTH=118 BGCOLOR="#ffffff">
 * <P CLASS="textintable" ALIGN=LEFT>
 * Internal syntax error
 * </P>
 * </TD>
 * <TD WIDTH=401 BGCOLOR="#ffffff">
 * <P CLASS="textintable" ALIGN=LEFT>
 * Compiler creates an unknown compiler code.
 * </P>
 * </TD>
 * </TR>
 * <TR VALIGN=TOP>
 * <TD WIDTH=42 BGCOLOR="#ffffff">
 * <P CLASS="textintable" ALIGN=LEFT>
 * 521
 * </P>
 * </TD>
 * <TD WIDTH=118 BGCOLOR="#ffffff">
 * <P CLASS="textintable" ALIGN=LEFT>
 * Internal syntax error
 * </P>
 * </TD>
 * <TD WIDTH=401 BGCOLOR="#ffffff">
 * <P CLASS="textintable" ALIGN=LEFT>
 * No result.
 * </P>
 * </TD>
 * </TR>
 * <TR VALIGN=TOP>
 * <TD WIDTH=42 BGCOLOR="#ffffff">
 * <P CLASS="textintable" ALIGN=LEFT>
 * 522
 * </P>
 * </TD>
 * <TD WIDTH=118 BGCOLOR="#ffffff">
 * <P CLASS="textintable" ALIGN=LEFT>
 * Circular reference
 * </P>
 * </TD>
 * <TD WIDTH=401 BGCOLOR="#ffffff">
 * <P CLASS="textintable" ALIGN=LEFT>
 * Formula refers directly or indirectly to itself and the iterations option is
 * not selected under Tools - Options - Table Document - Calculate.
 * </P>
 * </TD>
 * </TR>
 * <TR VALIGN=TOP>
 * <TD WIDTH=42 BGCOLOR="#ffffff">
 * <P CLASS="textintable" ALIGN=LEFT>
 * 523
 * </P>
 * </TD>
 * <TD WIDTH=118 BGCOLOR="#ffffff">
 * <P CLASS="textintable" ALIGN=LEFT>
 * The calculation procedure does not converge
 * </P>
 * </TD>
 * <TD WIDTH=401 BGCOLOR="#ffffff">
 * <P CLASS="textintable" ALIGN=LEFT>
 * Financial statistics function missed a targeted value or iterations of
 * circular references do not reach the minimum change within the maximum steps
 * that are set.
 * </P>
 * </TD>
 * </TR>
 * <TR VALIGN=TOP>
 * <TD WIDTH=42 BGCOLOR="#ffffff">
 * <P CLASS="textintable" ALIGN=LEFT>
 * 524
 * </P>
 * </TD>
 * <TD WIDTH=118 BGCOLOR="#ffffff">
 * <P CLASS="textintable" ALIGN=LEFT>
 * <A NAME="kw66944_5"> </A><A NAME="kw66944_4"> </A> invalid references
 * (instead of Err:524 cell contains #REF)
 * </P>
 * </TD>
 * <TD WIDTH=401 BGCOLOR="#ffffff">
 * <P CLASS="textintable" ALIGN=LEFT>
 * <B>Compiler: </B> a column or row description name could not be resolved.
 * <B>Interpreter: </B> in a formula, the column, row, or sheet that contains a
 * referenced cell is missing.
 * </P>
 * </TD>
 * </TR>
 * <TR VALIGN=TOP>
 * <TD WIDTH=42 BGCOLOR="#ffffff">
 * <P CLASS="textintable" ALIGN=LEFT>
 * 525
 * </P>
 * </TD>
 * <TD WIDTH=118 BGCOLOR="#ffffff">
 * <P CLASS="textintable" ALIGN=LEFT>
 * <A NAME="kw66944_3"> </A><A NAME="kw66944_2"> </A> invalid names (instead of
 * Err:525 cell contains #NAME?)
 * </P>
 * </TD>
 * <TD WIDTH=401 BGCOLOR="#ffffff">
 * <P CLASS="textintable" ALIGN=LEFT>
 * An identifier could not be evaluated, for example, no valid reference, no
 * valid domain name, no column/row label, no macro, incorrect decimal divider,
 * add-in not found.
 * </P>
 * </TD>
 * </TR>
 * <TR VALIGN=TOP>
 * <TD WIDTH=42 BGCOLOR="#ffffff">
 * <P CLASS="textintable" ALIGN=LEFT>
 * 526
 * </P>
 * </TD>
 * <TD WIDTH=118 BGCOLOR="#ffffff">
 * <P CLASS="textintable" ALIGN=LEFT>
 * Internal syntax error
 * </P>
 * </TD>
 * <TD WIDTH=401 BGCOLOR="#ffffff">
 * <P CLASS="textintable" ALIGN=LEFT>
 * Obsolete, no longer used, but could come from old documents if the result is
 * a formula from a domain.
 * </P>
 * </TD>
 * </TR>
 * <TR VALIGN=TOP>
 * <TD WIDTH=42 BGCOLOR="#ffffff">
 * <P CLASS="textintable" ALIGN=LEFT>
 * 527
 * </P>
 * </TD>
 * <TD WIDTH=118 BGCOLOR="#ffffff">
 * <P CLASS="textintable" ALIGN=LEFT>
 * Internal overflow
 * </P>
 * </TD>
 * <TD WIDTH=401 BGCOLOR="#ffffff">
 * <P CLASS="textintable" ALIGN=LEFT>
 * <B>Interpreter: </B>References, such as when a cell references a cell, are
 * too encapsulated.
 * </P>
 * </TD>
 * </TR>
 * </TABLE>
 *  
 */
public class ErrorEval implements ValueEval {

    private int errorCode;

    // Oo std error codes
    public static final ErrorEval ERROR_501 = new ErrorEval(501);

    public static final ErrorEval ERROR_502 = new ErrorEval(502);

    public static final ErrorEval ERROR_503 = new ErrorEval(503);

    public static final ErrorEval ERROR_504 = new ErrorEval(504);

    public static final ErrorEval ERROR_505 = new ErrorEval(505);

    public static final ErrorEval ERROR_506 = new ErrorEval(506);

    public static final ErrorEval ERROR_507 = new ErrorEval(507);

    public static final ErrorEval ERROR_508 = new ErrorEval(508);

    public static final ErrorEval ERROR_509 = new ErrorEval(509);

    public static final ErrorEval ERROR_510 = new ErrorEval(510);

    public static final ErrorEval ERROR_511 = new ErrorEval(511);

    public static final ErrorEval ERROR_512 = new ErrorEval(512);

    public static final ErrorEval ERROR_513 = new ErrorEval(513);

    public static final ErrorEval ERROR_514 = new ErrorEval(514);

    public static final ErrorEval ERROR_515 = new ErrorEval(515);

    public static final ErrorEval ERROR_516 = new ErrorEval(516);

    public static final ErrorEval ERROR_517 = new ErrorEval(517);

    public static final ErrorEval ERROR_518 = new ErrorEval(518);

    public static final ErrorEval ERROR_519 = new ErrorEval(519);

    public static final ErrorEval ERROR_520 = new ErrorEval(520);

    public static final ErrorEval ERROR_521 = new ErrorEval(521);

    public static final ErrorEval ERROR_522 = new ErrorEval(522);

    public static final ErrorEval ERROR_523 = new ErrorEval(523);

    public static final ErrorEval ERROR_524 = new ErrorEval(524);

    public static final ErrorEval ERROR_525 = new ErrorEval(525);

    public static final ErrorEval ERROR_526 = new ErrorEval(526);

    public static final ErrorEval ERROR_527 = new ErrorEval(527);

    public static final ErrorEval NAME_INVALID = ERROR_525;

    public static final ErrorEval VALUE_INVALID = ERROR_519;

    
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

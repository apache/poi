/*
 * Created on May 21, 2005
 *
 */
package org.apache.poi.hssf.record.formula.functions;

/**
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 * 
 * 
 * This class is a functon library for common fiscal functions.
 * <b>Glossary of terms/abbreviations:</b>
 * <br/>
 * <ul>
 * <li><em>FV:</em> Future Value</li>
 * <li><em>PV:</em> Present Value</li>
 * <li><em>NPV:</em> Net Present Value</li>
 * <li><em>PMT:</em> (Periodic) Payment</li>
 * 
 * </ul>
 * For more info on the terms/abbreviations please use the references below 
 * (hyperlinks are subject to change):
 * </br>Online References:
 * <ol>
 * <li>GNU Emacs Calc 2.02 Manual: http://theory.uwinnipeg.ca/gnu/calc/calc_203.html</li>
 * <li>Yahoo Financial Glossary: http://biz.yahoo.com/f/g/nn.html#y</li>
 * </ol>
 */
public class FinanceLib {
    
    // constants for default values
    
    
    
    private FinanceLib() {}
    
    /**
     * Future value of an amount given the number of payments, rate, amount
     * of individual payment, present value and boolean value indicating whether
     * payments are due at the beginning of period 
     * (false => payments are due at end of period) 
     * @param r rate
     * @param n num of periods
     * @param y pmt per period
     * @param f future value
     * @param t type (true=pmt at end of period, false=pmt at begining of period)
     * @return
     */
    public static double fv(double r, int n, double y, double p, boolean t) {
        double r1 = r + 1;
        return ((1-Math.pow(r1, n)) * (t ? r1 : 1) * y ) / r  
                  - 
               p*Math.pow(r1, n);
    }
    
    /**
     * Present value of an amount given the number of future payments, rate, amount
     * of individual payment, future value and boolean value indicating whether
     * payments are due at the beginning of period 
     * (false => payments are due at end of period) 
     * @param r
     * @param n
     * @param y
     * @param f
     * @param t
     * @return
     */
    public static double pv(double r, int n, double y, double f, boolean t) {
        double r1 = r + 1;
        return (( ( 1 - Math.pow(r1, n) ) / r ) * (t ? r1 : 1)  * y - f)
                  /
               Math.pow(r1, n);
    }
    
    /**
     * calculates the Net Present Value of a principal amount
     * given the discount rate and a sequence of cash flows 
     * (supplied as an array). If the amounts are income the value should 
     * be positive, else if they are payments and not income, the 
     * value should be negative.
     * @param r
     * @param cfs cashflow amounts
     * @return
     */
    public static double npv(double r, double[] cfs) {
        double npv = 0;
        double r1 = r + 1;
        double trate = r1;
        for (int i=0, iSize=cfs.length; i<iSize; i++) {
            npv += cfs[i] / trate;
            trate *= r1;
        }
        return npv;
    }
    
    /**
     * 
     * @param r
     * @param n
     * @param p
     * @param f
     * @param t
     * @return
     */
    public static double pmt(double r, int n, double p, double f, boolean t) {
        double r1 = r + 1;
        return ( f + p * Math.pow(r1, n) ) * r 
                  / 
               ((t ? r1 : 1) * (1 - Math.pow(r1, n)));
    }
    
    /**
     * 
     * @param r
     * @param n
     * @param p
     * @param f
     * @param t
     * @return
     */
    public static int nper(double r, double y, double p, double f, boolean t) {
        double r1 = r + 1;
        double ryr = (t ? r1 : 1) * y / r;
        double a1 = ((ryr-f) < 0) ? Math.log(f-ryr) : Math.log(ryr-f);
        double a2 = ((ryr-f) < 0) ? Math.log(-p-ryr) : Math.log(p+ryr);
        double a3 = Math.log(r1);
        double dval = ( a1 - a2 ) / a3;
        
        return (int) Math.round(dval);
    }
    

}

/*   Copyright 2004 The Apache Software Foundation
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package tools.inst2xsd.detailed;

import tools.inst2xsd.common.Inst2XsdTestBase;
import org.apache.xmlbeans.XmlObject;

public class DetailedLCDTest extends Inst2XsdTestBase {

    public DetailedLCDTest(String name) {
        super(name);
    }

    // List of precedence for smart simple primitive type determination
    // byte, short, int, long, integer, float, double, decimal,
    // boolean
    // date, dateTime, time, gDuration,
    // QName ?,
    // anyUri ? - triggered only for http:// or www. constructs,
    // list types ?
    // string
    //TODO: Value will become number
    // public void test_gYear() throws Exception {
    //runTypeChecking(getAttrTypeXml("1999"), "gYear");
    //}
    // NOTE: The following features are not supported by the
    // NOTE: inst2xsd tool
    /*public void test_lcd_gYearMonth() throws Exception {
        runLCDTypeCheckTest("abc","1999-05", "string");
        runLCDTypeCheckTest("1998-06", "1999-05", "gYearMonth");

    }

    public void test_lcd_gMonthDay() throws Exception {
        runLCDTypeCheckTest("abc","--02-15", "string");
        runLCDTypeCheckTest("--02-15", "--02-15", "gMonthDay");
    }

    public void test_lcd_gDay() throws Exception {
        runLCDTypeCheckTest("abc", "---15", "string");
        runLCDTypeCheckTest("---14","---15", "gDay");
    }

    public void test_lcd_gMonth() throws Exception {
        runLCDTypeCheckTest("abc", "--02--", "string");
        runLCDTypeCheckTest("--10--","--02--", "gMonth");
    }

    public void test_lcd_duration() throws Exception {
        runLCDTypeCheckTest("abc", "P1347Y", "string");
        runLCDTypeCheckTest("P1347Y", "P1347M", "duration");
        runLCDTypeCheckTest("-P1347M", "P1Y2MT2H", "duration");
        runLCDTypeCheckTest("P1347Y", "P0Y1347M", "duration");
        runLCDTypeCheckTest("P1347Y", "P0Y1347M0D", "duration");
        runLCDTypeCheckTest("P1347Y", "-P1347M", "duration");
    } */

    public void test_lcd_anyuri() throws Exception {
        runLCDTypeCheckTest("abc",
                "http://www.math.uio.no/faq/compression-faq/part1.html", "string");
        runLCDTypeCheckTest("http://www.math.uio.no/faq/compression-faq/part1.html",
                "http://www.example.com/doc.html#ID5", "anyURI");
        runLCDTypeCheckTest("123", "www.math.uio.no/faq/compression-faq/part1.html", "string");
        //runLCDTypeCheckTest("ftp://ftp.is.co.za/rfc/rfc1808.txt", "gopher://spinaltap.micro.umn.edu/00/Weather/California/Los%20Angeles", "anyURI");
    }

    public void test_lcd_byte() throws Exception {
        runLCDTypeCheckTest("122", "123", "byte");
        runLCDTypeCheckTest("-1", "+100", "byte");
        runLCDTypeCheckTest("0", "-1", "byte");
        runLCDTypeCheckTest("-1", "0", "byte");
        runLCDTypeCheckTest("-128", "127", "byte");
        runLCDTypeCheckTest("127", "-128", "byte");

        runLCDTypeCheckTest("-200", "123", "short");
        runLCDTypeCheckTest("128", "+100", "short");
        runLCDTypeCheckTest("32767", "-1", "short");
        runLCDTypeCheckTest("39000", "0", "int");
        runLCDTypeCheckTest("2147483647", "127", "int");
        runLCDTypeCheckTest("-2147483648", "-128", "int");
        runLCDTypeCheckTest("-9223372036854775808", "123", "long");
        runLCDTypeCheckTest("9223372036854775807", "+100", "long");
        runLCDTypeCheckTest("-9223372036854775809", "-1", "integer");
        runLCDTypeCheckTest("9223372036854775808", "0", "integer");
        runLCDTypeCheckTest("2147483647", "127", "int");
        runLCDTypeCheckTest("-2147483648", "-128", "int");
        runLCDTypeCheckTest("-200.034", "123", "float");
        runLCDTypeCheckTest("1267.43233E12", "-1", "float");
        runLCDTypeCheckTest("INF", "0", "float");
        runLCDTypeCheckTest("abc", "127", "string");

    }

    public void test_lcd_short() throws Exception {
        runLCDTypeCheckTest("32767","-129", "short");
        runLCDTypeCheckTest("-32768","128", "short");
        runLCDTypeCheckTest("3000","3000", "short");
        runLCDTypeCheckTest("-32768","-3000", "short");
        runLCDTypeCheckTest("32767","-32768", "short");
        runLCDTypeCheckTest("-32768","32767", "short");

        runLCDTypeCheckTest("39000", "-3000", "int");
        runLCDTypeCheckTest("2147483647", "-3000", "int");
        runLCDTypeCheckTest("-2147483648", "-3000", "int");
        runLCDTypeCheckTest("-9223372036854775808", "-3000", "long");
        runLCDTypeCheckTest("9223372036854775807", "-3000", "long");
        runLCDTypeCheckTest("-9223372036854775809", "-3000", "integer");
        runLCDTypeCheckTest("9223372036854775808", "-3000", "integer");
        runLCDTypeCheckTest("2147483647", "-3000", "int");
        runLCDTypeCheckTest("-2147483648", "-3000", "int");
        runLCDTypeCheckTest("-200.034", "-3000", "float");
        runLCDTypeCheckTest("1267.43233E12", "-3000", "float");
        runLCDTypeCheckTest("INF", "-3000", "float");
        runLCDTypeCheckTest("abc", "-3000", "string");
    }

    public void test_lcd_int() throws Exception {
        runLCDTypeCheckTest("32768","39000", "int");
        runLCDTypeCheckTest("32768","32768", "int");
        runLCDTypeCheckTest("-39000","-32769", "int");
        runLCDTypeCheckTest("2147483647","-39000", "int");
        runLCDTypeCheckTest("39000","126789675", "int");
        runLCDTypeCheckTest("-2147483648","2147483647", "int");
        runLCDTypeCheckTest("2147483647","-2147483648", "int");

        runLCDTypeCheckTest("39000", "32768", "int");
        runLCDTypeCheckTest("2147483647", "32768", "int");
        runLCDTypeCheckTest("-2147483648", "32768", "int");
        runLCDTypeCheckTest("-9223372036854775808", "32768", "long");
        runLCDTypeCheckTest("9223372036854775807", "32768", "long");
        runLCDTypeCheckTest("-9223372036854775809", "32768", "integer");
        runLCDTypeCheckTest("9223372036854775808", "32768", "integer");
        runLCDTypeCheckTest("2147483647", "32768", "int");
        runLCDTypeCheckTest("-2147483648", "32768", "int");
        runLCDTypeCheckTest("-200.034", "32768", "float");
        runLCDTypeCheckTest("1267.43233E12", "32768", "float");
        runLCDTypeCheckTest("INF", "32768", "float");
        runLCDTypeCheckTest("abc", "32768", "string");
    }

    public void test_lcd_long() throws Exception {
        runLCDTypeCheckTest("-9223372036854775808","2147483648", "long");
        runLCDTypeCheckTest("-9223372036854775808","-2147483649", "long");
        runLCDTypeCheckTest("-2150000000","-2150000000", "long");
        runLCDTypeCheckTest("-9223372036854775808","2150000000", "long");
        runLCDTypeCheckTest("9223372036854775807","-9223372036854775808", "long");
        runLCDTypeCheckTest("-9223372036854775808","9223372036854775807", "long");

        runLCDTypeCheckTest("-9223372036854775809", "2150000000", "integer");
        runLCDTypeCheckTest("9223372036854775808", "2150000000", "integer");
        runLCDTypeCheckTest("2147483647", "2150000000", "long");
        runLCDTypeCheckTest("-2147483648", "2150000000", "long");
        runLCDTypeCheckTest("-200.034", "2150000000", "float");
        runLCDTypeCheckTest("1267.43233E12", "2150000000", "float");
        runLCDTypeCheckTest("INF", "2150000000", "float");
        runLCDTypeCheckTest("abc", "2150000000", "string");
    }

    public void test_lcd_integer() throws Exception {
        runLCDTypeCheckTest("9223372036854775808","9300000000000000000", "integer");
        runLCDTypeCheckTest("9223372036854775808","-9300000000000000000", "integer");
        runLCDTypeCheckTest("9223372036854775808","-9223372036854775809", "integer");
        runLCDTypeCheckTest("9223372036854775808","9223372036854775808", "integer");

        runLCDTypeCheckTest("-9223372036854775809", "0", "integer");
        runLCDTypeCheckTest("9223372036854775808", "0", "integer");
        runLCDTypeCheckTest("-200.034", "9223372036854775808", "float");
        runLCDTypeCheckTest("1267.43233E12", "9223372036854775808", "float");
        runLCDTypeCheckTest("INF", "9223372036854775808", "float");
        runLCDTypeCheckTest("abc", "9223372036854775808", "string");
    }

    public void test_lcd_float() throws Exception {
        runLCDTypeCheckTest("+100000.00","12.78e-2", "float");
        runLCDTypeCheckTest("+100000.00","1267.43233E12", "float");
        runLCDTypeCheckTest("12","-1E4", "float");
        runLCDTypeCheckTest("-1.23","INF", "float");
        runLCDTypeCheckTest("-1.23","-INF", "float");
        runLCDTypeCheckTest("12.78e-2","NaN", "float");
        runLCDTypeCheckTest("12678967.543233","-1.23", "float");
        runLCDTypeCheckTest("-1E4","12678967.543233", "float");
        runLCDTypeCheckTest("12.78e-2","+100000.00", "float");

        runLCDTypeCheckTest("INF", "12.78e-2", "float");
        runLCDTypeCheckTest("-1E4", "1267.43233E12", "float");
        runLCDTypeCheckTest("-1.23", "-1E4", "float");
        runLCDTypeCheckTest("-1.23", "INF", "float");
        runLCDTypeCheckTest("-1.23", "-INF", "float");
        runLCDTypeCheckTest("12.78e-2", "NaN", "float");
        runLCDTypeCheckTest("12678967.543233", "-1.23", "float");
        runLCDTypeCheckTest("-1E4", "12678967.543233", "float");
        runLCDTypeCheckTest("12.78e-2", "+100000.00", "float");

        runLCDTypeCheckTest("0", "INF", "float");
        runLCDTypeCheckTest("129", "-INF", "float");
        runLCDTypeCheckTest("2147483647", "NaN", "float");
        runLCDTypeCheckTest("32767", "NaN", "float");
        runLCDTypeCheckTest("2147483647a", "INF", "string");
        runLCDTypeCheckTest("2147483647 abc", "-INF", "string");
        runLCDTypeCheckTest("9223372036854775808", "NaN", "float");
        runLCDTypeCheckTest("abc", "NaN", "string");
    }

    //TODO: NOT COMPLETELY SURE HOW TO GET THESE WITHOUT
    //CAUSING AN Number EXCEPTION
    //public void test_lcd_double() throws Exception {
    //    runLCDTypeCheckTest("",""), "double");
    //}

    //public void test_lcd_decimal() throws Exception {
    //    runLCDTypeCheckTest("",""), "decimal");
    //}
    //Value will become number
    // public void test_lcd_gYear() throws Exception {
    //runLCDTypeCheckTest("","1999"), "gYear");
    //}


    public void test_lcd_date() throws Exception {
        runLCDTypeCheckTest("abc", "1999-05-31", "string");
        runLCDTypeCheckTest("1999-06-15","1999-05-31", "date");
    }

    public void test_lcd_dateTime() throws Exception {
        runLCDTypeCheckTest("abc", "1999-05-31T13:20:00-05:00", "string");
        runLCDTypeCheckTest("1999-05-31T13:20:00-08:00","1999-05-31T13:20:00-05:00", "dateTime");
        runLCDTypeCheckTest("1999-05-31T13:20:00-06:00","2000-03-04T20:00:00Z", "dateTime");
        runLCDTypeCheckTest("1999-05-15T13:20:00-05:00","2000-03-04T23:00:00+03:00", "dateTime");
    }

    public void test_lcd_time() throws Exception {
        runLCDTypeCheckTest("abc", "13:20:00-05:00", "string");
        runLCDTypeCheckTest("00:00:00","13:20:00-05:00", "time");
        runLCDTypeCheckTest("13:20:00Z","00:00:00", "time");
        runLCDTypeCheckTest("13:20:00Z","13:20:00Z", "time");
    }

    /*public void test_lcd_QName() throws Exception {
        XmlObject xsdString = XmlObject.Factory.parse("<a xmlns=\"attrTests\" " +
                "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" a=\"xsd:string\"></a>");

        XmlObject foobaz = XmlObject.Factory.parse("<a xmlns=\"attrTests\" " +
                "xmlns:foo=\"http://foobaz\" a=\"foo:baz\" />");

        runLCDTypeCheckTest("abc", "1999-05-31", "string");
        runAttrTypeChecking(xsdString, "QName");
        runAttrTypeChecking(foobaz, "QName");
    }*/



}

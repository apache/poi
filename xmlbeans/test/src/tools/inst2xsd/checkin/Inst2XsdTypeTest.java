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
package tools.inst2xsd.checkin;

import tools.inst2xsd.common.Inst2XsdTestBase;

public class Inst2XsdTypeTest extends Inst2XsdTestBase {

    public Inst2XsdTypeTest(String name) {
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
    public void test_string() throws Exception {
        runTypeChecking(getTypeXml("a"), "string");
        runTypeChecking(getTypeXml("a2a"), "string");
        runTypeChecking(getTypeXml("a b c\n hello\t from\n\txmlbeans"), "string");
    }

    public void test_byte() throws Exception {
        runTypeChecking(getTypeXml("123"), "byte");
        runTypeChecking(getTypeXml("+100"), "byte");
        runTypeChecking(getTypeXml("-1"), "byte");
        runTypeChecking(getTypeXml("0"), "byte");
        runTypeChecking(getTypeXml("127"), "byte");
        runTypeChecking(getTypeXml("-128"), "byte");
    }

    public void test_short() throws Exception {
        runTypeChecking(getTypeXml("-129"), "short");
        runTypeChecking(getTypeXml("128"), "short");
        runTypeChecking(getTypeXml("3000"), "short");
        runTypeChecking(getTypeXml("-3000"), "short");
        runTypeChecking(getTypeXml("-32768"), "short");
        runTypeChecking(getTypeXml("32767"), "short");
    }

    public void test_int() throws Exception {
        runTypeChecking(getTypeXml("39000"), "int");
        runTypeChecking(getTypeXml("32768"), "int");
        runTypeChecking(getTypeXml("-32769"), "int");
        runTypeChecking(getTypeXml("-39000"), "int");
        runTypeChecking(getTypeXml("126789675"), "int");
        runTypeChecking(getTypeXml("2147483647"), "int");
        runTypeChecking(getTypeXml("-2147483648"), "int");
    }

    public void test_long() throws Exception {
        runTypeChecking(getTypeXml("2147483648"), "long");
        runTypeChecking(getTypeXml("-2147483649"), "long");
        runTypeChecking(getTypeXml("-2150000000"), "long");
        runTypeChecking(getTypeXml("2150000000"), "long");
        runTypeChecking(getTypeXml("-9223372036854775808"), "long");
        runTypeChecking(getTypeXml("9223372036854775807"), "long");
    }

    public void test_integer() throws Exception {
        runTypeChecking(getTypeXml("9300000000000000000"), "integer");
        runTypeChecking(getTypeXml("-9300000000000000000"), "integer");
        runTypeChecking(getTypeXml("-9223372036854775809"), "integer");
        runTypeChecking(getTypeXml("9223372036854775808"), "integer");
    }

    public void test_float() throws Exception {
        runTypeChecking(getTypeXml("12.78e-2"), "float");
        runTypeChecking(getTypeXml("1267.43233E12"), "float");
        runTypeChecking(getTypeXml("-1E4"), "float");
        runTypeChecking(getTypeXml("INF"), "float");
        runTypeChecking(getTypeXml("-INF"), "float");
        runTypeChecking(getTypeXml("NaN"), "float");
        runTypeChecking(getTypeXml("-1.23"), "float");
        runTypeChecking(getTypeXml("12678967.543233"), "float");
        runTypeChecking(getTypeXml("+100000.00"), "float");
     }

    //TODO: NOT COMPLETELY SURE HOW TO GET THESE WITHOUT
    //CAUSING AN Number EXCEPTION
    //public void test_double() throws Exception {
    //    runTypeChecking(getTypeXml(""), "double");
    //}

    //public void test_decimal() throws Exception {
    //    runTypeChecking(getTypeXml(""), "decimal");
    //}
    //Value will become number
    // public void test_gYear() throws Exception {
    //runTypeChecking(getTypeXml("1999"), "gYear");
    //}


    public void test_date() throws Exception {
        runTypeChecking(getTypeXml("1999-05-31"), "date");
    }

    public void test_dateTime() throws Exception {
        runTypeChecking(getTypeXml("1999-05-31T13:20:00-05:00"), "dateTime");
        runTypeChecking(getTypeXml("2000-03-04T20:00:00Z"), "dateTime");
        runTypeChecking(getTypeXml("2000-03-04T23:00:00+03:00"), "dateTime");
    }
    public void test_time() throws Exception {
        runTypeChecking(getTypeXml("13:20:00-05:00"), "time");
        runTypeChecking(getTypeXml("00:00:00"), "time");
        runTypeChecking(getTypeXml("13:20:00Z"), "time");
    }
    
    public void test_CDATA() throws Exception {
        runTypeChecking(getTypeXml("<![CDATA[ " +
                                   "function matchwo(a, b) {" +
                                   "if (a < b && a < 0) " +
                                   "    then { " +
                                   "        return 1 " +
                                   "    } else { " +
                                   "        return 0 " +
                                   "    } " +
                                   "} ]]>"), "string");
    }

    //public void test_() throws Exception {
    //    runTypeChecking(getTypeXml(""), "");
    //}

}

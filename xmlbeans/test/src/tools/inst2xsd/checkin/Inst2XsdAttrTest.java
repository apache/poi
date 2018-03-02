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

public class Inst2XsdAttrTest extends Inst2XsdTestBase {

    public Inst2XsdAttrTest(String name) {
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
    public void test_attrstring() throws Exception {
        runAttrTypeChecking(getAttrTypeXml("a"), "string");
        runAttrTypeChecking(getAttrTypeXml("a2a"), "string");
        runAttrTypeChecking(getAttrTypeXml("a b c\n hello\t from\n\txmlbeans"), "string");
    }

    public void test_attrshort() throws Exception {
        runAttrTypeChecking(getAttrTypeXml("-129"), "short");
        runAttrTypeChecking(getAttrTypeXml("128"), "short");
        runAttrTypeChecking(getAttrTypeXml("3000"), "short");
        runAttrTypeChecking(getAttrTypeXml("-3000"), "short");
        runAttrTypeChecking(getAttrTypeXml("-32768"), "short");
        runAttrTypeChecking(getAttrTypeXml("32767"), "short");
    }

    public void test_attrint() throws Exception {
        runAttrTypeChecking(getAttrTypeXml("39000"), "int");
        runAttrTypeChecking(getAttrTypeXml("32768"), "int");
        runAttrTypeChecking(getAttrTypeXml("-32769"), "int");
        runAttrTypeChecking(getAttrTypeXml("-39000"), "int");
        runAttrTypeChecking(getAttrTypeXml("126789675"), "int");
        runAttrTypeChecking(getAttrTypeXml("2147483647"), "int");
        runAttrTypeChecking(getAttrTypeXml("-2147483648"), "int");
    }

    public void test_attrlong() throws Exception {
        runAttrTypeChecking(getAttrTypeXml("2147483648"), "long");
        runAttrTypeChecking(getAttrTypeXml("-2147483649"), "long");
        runAttrTypeChecking(getAttrTypeXml("-2150000000"), "long");
        runAttrTypeChecking(getAttrTypeXml("2150000000"), "long");
        runAttrTypeChecking(getAttrTypeXml("-9223372036854775808"), "long");
        runAttrTypeChecking(getAttrTypeXml("9223372036854775807"), "long");
    }

    public void test_attrinteger() throws Exception {
        runAttrTypeChecking(getAttrTypeXml("9300000000000000000"), "integer");
        runAttrTypeChecking(getAttrTypeXml("-9300000000000000000"), "integer");
        runAttrTypeChecking(getAttrTypeXml("-9223372036854775809"), "integer");
        runAttrTypeChecking(getAttrTypeXml("9223372036854775808"), "integer");
    }

    public void test_attrfloat() throws Exception {
        runAttrTypeChecking(getAttrTypeXml("12.78e-2"), "float");
        runAttrTypeChecking(getAttrTypeXml("1267.43233E12"), "float");
        runAttrTypeChecking(getAttrTypeXml("-1E4"), "float");
        runAttrTypeChecking(getAttrTypeXml("INF"), "float");
        runAttrTypeChecking(getAttrTypeXml("-INF"), "float");
        runAttrTypeChecking(getAttrTypeXml("NaN"), "float");
        runAttrTypeChecking(getAttrTypeXml("-1.23"), "float");
        runAttrTypeChecking(getAttrTypeXml("12678967.543233"), "float");
        runAttrTypeChecking(getAttrTypeXml("+100000.00"), "float");
    }


    public void test_attrdate() throws Exception {
        runAttrTypeChecking(getAttrTypeXml("1999-05-31"), "date");
    }

    public void test_attrdateTime() throws Exception {
        runAttrTypeChecking(getAttrTypeXml("1999-05-31T13:20:00-05:00"), "dateTime");
        runAttrTypeChecking(getAttrTypeXml("2000-03-04T20:00:00Z"), "dateTime");
        runAttrTypeChecking(getAttrTypeXml("2000-03-04T23:00:00+03:00"), "dateTime");
    }

    public void test_attrtime() throws Exception {
        runAttrTypeChecking(getAttrTypeXml("13:20:00-05:00"), "time");
        runAttrTypeChecking(getAttrTypeXml("00:00:00"), "time");
        runAttrTypeChecking(getAttrTypeXml("13:20:00Z"), "time");
    }

    public void test_attrbyte() throws Exception {
        runAttrTypeChecking(getAttrTypeXml("123"), "byte");
        runAttrTypeChecking(getAttrTypeXml("+100"), "byte");
        runAttrTypeChecking(getAttrTypeXml("-1"), "byte");
        runAttrTypeChecking(getAttrTypeXml("0"), "byte");
        runAttrTypeChecking(getAttrTypeXml("127"), "byte");
        runAttrTypeChecking(getAttrTypeXml("-128"), "byte");
    }

    public void test_attrduration() throws Exception {
        runAttrTypeChecking(getAttrTypeXml("P1347Y"), "duration");
        runAttrTypeChecking(getAttrTypeXml("P1347M"), "duration");
        runAttrTypeChecking(getAttrTypeXml("P1Y2MT2H"), "duration");
        runAttrTypeChecking(getAttrTypeXml("P0Y1347M"), "duration");
        runAttrTypeChecking(getAttrTypeXml("P0Y1347M0D"), "duration");
        runAttrTypeChecking(getAttrTypeXml("-P1347M"), "duration");
    }

    //public void test_attr() throws Exception {
    //    runAttrTypeChecking(getAttrTypeXml(""), "");
    //}

}

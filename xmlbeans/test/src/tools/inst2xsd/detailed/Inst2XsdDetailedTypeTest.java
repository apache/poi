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

public class Inst2XsdDetailedTypeTest extends Inst2XsdTestBase {

    public Inst2XsdDetailedTypeTest(String name) {
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
    //runTypeChecking(getTypeXml("1999"), "gYear");
    //}
    // NOTE: The following features are not supported by the
    // NOTE: inst2xsd tool
    /*public void test_gYearMonth() throws Exception {
        runTypeChecking(getTypeXml("1999-05"), "gYearMonth");
    }

    public void test_gMonthDay() throws Exception {
        runTypeChecking(getTypeXml("--02-15"), "gMonthDay");
    }

    public void test_gDay() throws Exception {
        runTypeChecking(getTypeXml("---15"), "gDay");
    }

    public void test_gMonth() throws Exception {
        runTypeChecking(getTypeXml("--02--"), "gMonth");
    }

    public void test_duration() throws Exception {
        runTypeChecking(getTypeXml("P1347Y"), "duration");
        runTypeChecking(getTypeXml("P1347M"), "duration");
        runTypeChecking(getTypeXml("P1Y2MT2H"), "duration");
        runTypeChecking(getTypeXml("P0Y1347M"), "duration");
        runTypeChecking(getTypeXml("P0Y1347M0D"), "duration");
        runTypeChecking(getTypeXml("-P1347M"), "duration");
    } */

    //THIS becomes string as expected
    //public void test_hexBinary() throws Exception {
    //    runTypeChecking(getTypeXml("0FB7"), "hexBinary");
    //}

    public void test_anyuri() throws Exception {
        runTypeChecking(getTypeXml("http://www.math.uio.no/faq/compression-faq/part1.html"), "anyURI");
        runTypeChecking(getTypeXml("http://www.example.com/doc.html#ID5"), "anyURI");
        runTypeChecking(getTypeXml("www.math.uio.no/faq/compression-faq/part1.html"), "anyURI");
        //commenting uri per accepted tool uri
        //runTypeChecking(getTypeXml("gopher://spinaltap.micro.umn.edu/00/Weather/California/Los%20Angeles"), "anyURI");
        //runTypeChecking(getTypeXml("ftp://ftp.is.co.za/rfc/rfc1808.txt"), "anyURI");
        //runTypeChecking(getTypeXml("mailto:mduerst@ifi.unizh.ch"), "string");
        //runTypeChecking(getTypeXml("news:comp.infosystems.www.servers.unix"), "anyURI");
        //runTypeChecking(getTypeXml("telnet://melvyl.ucop.edu/"), "anyURI");
        //runTypeChecking(getTypeXml("./this:that"), "anyURI");
    }

    /**
     * TODO: FEATURE REQUEST: recognition of type
     * 0, and 1 get picked up by byte
     * true and false are strings
     *
     */
    public void test_boolean() throws Exception {
        //runTypeChecking(getTypeXml("true"), "boolean");
        //runTypeChecking(getTypeXml("false"), "boolean");
        runTypeChecking(getTypeXml("true"), "string");
        runTypeChecking(getTypeXml("false"), "string");

    }

    public void test_QName() throws Exception {
        XmlObject xsdString = XmlObject.Factory.parse("<a xmlns=\"typeTests\" " +
                        "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" >xsd:string</a>");
        XmlObject xsiint = XmlObject.Factory.parse("<a xmlns=\"typeTests\" " +
                "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" " +
                "xsi:type=\"xsd:QName\">xsi:type</a>");

        XmlObject foobaz = XmlObject.Factory.parse("<a xmlns=\"typeTests\" " +
                "xmlns:foo=\"http://foobaz\" >foo:baz</a>");

        runTypeChecking(xsdString, "QName");
        runTypeChecking(xsiint, "QName");
        runTypeChecking(foobaz, "QName");
    }




}

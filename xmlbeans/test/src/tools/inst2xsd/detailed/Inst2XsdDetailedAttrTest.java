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

public class Inst2XsdDetailedAttrTest extends Inst2XsdTestBase {

    public Inst2XsdDetailedAttrTest(String name) {
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
    /*public void test_attrgYearMonth() throws Exception {
        runAttrTypeChecking(getAttrTypeXml("1999-05"), "gYearMonth");
    }

    public void test_attrgMonthDay() throws Exception {
        runAttrTypeChecking(getAttrTypeXml("--02-15"), "gMonthDay");
    }

    public void test_attrgDay() throws Exception {
        runAttrTypeChecking(getAttrTypeXml("---15"), "gDay");
    }

    public void test_attrgMonth() throws Exception {
        runAttrTypeChecking(getAttrTypeXml("--02--"), "gMonth");
    } */

    //THIS becomes string as expected
    //public void test_attrhexBinary() throws Exception {
    //    runAttrTypeChecking(getAttrTypeXml("0FB7"), "hexBinary");
    //}

    //TODO: NOT COMPLETELY SURE HOW TO GET THESE WITHOUT
    //CAUSING AN Number EXCEPTION
    //public void test_attrdouble() throws Exception {
    //    runAttrTypeChecking(getAttrTypeXml(""), "double");
    //}

    //public void test_attrdecimal() throws Exception {
    //    runAttrTypeChecking(getAttrTypeXml(""), "decimal");
    //}
    //Value will become number
    // public void test_attrgYear() throws Exception {
    //runAttrTypeChecking(getAttrTypeXml("1999"), "gYear");
    //}

    public void test_attranyuri() throws Exception {
        runAttrTypeChecking(getAttrTypeXml("http://www.math.uio.no/faq/compression-faq/part1.html"), "anyURI");
        runAttrTypeChecking(getAttrTypeXml("http://www.example.com/doc.html#ID5"), "anyURI");
        runAttrTypeChecking(getAttrTypeXml("www.math.uio.no/faq/compression-faq/part1.html"), "anyURI");
        //runAttrTypeChecking(getAttrTypeXml("gopher://spinaltap.micro.umn.edu/00/Weather/California/Los%20Angeles"), "anyURI");
        //runAttrTypeChecking(getAttrTypeXml("ftp://ftp.is.co.za/rfc/rfc1808.txt"), "anyURI");
        //runAttrTypeChecking(getAttrTypeXml("mailto:mduerst@ifi.unizh.ch"), "string");
        //runAttrTypeChecking(getAttrTypeXml("news:comp.infosystems.www.servers.unix"), "anyURI");
        //runAttrTypeChecking(getAttrTypeXml("telnet://melvyl.ucop.edu/"), "anyURI");
        //runAttrTypeChecking(getAttrTypeXml("./this:that"), "anyURI");
    }

    /**
     * 0, and 1 get picked up by byte
     * true, false are strings
     */
    public void test_attrboolean() throws Exception {
        runAttrTypeChecking(getAttrTypeXml("true"), "string");
        runAttrTypeChecking(getAttrTypeXml("false"), "string");

        //runAttrTypeChecking(getAttrTypeXml("true"), "boolean");
        //runAttrTypeChecking(getAttrTypeXml("false"), "boolean");
    }






    public void test_attrQName() throws Exception {
        XmlObject xsdString = XmlObject.Factory.parse("<a xmlns=\"attrTests\" " +
                "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" a=\"xsd:string\"></a>");

        XmlObject foobaz = XmlObject.Factory.parse("<a xmlns=\"attrTests\" " +
                "xmlns:foo=\"http://foobaz\" a=\"foo:baz\" />");

        runAttrTypeChecking(xsdString, "QName");
        runAttrTypeChecking(foobaz, "QName");
    }



}

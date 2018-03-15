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

package xmlobject.detailed;

import junit.framework.TestCase;

import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlError;

import javax.xml.namespace.QName;

import org.xmlsoap.schemas.soap.envelope.Fault;
//import org.xmlsoap.schemas.soap.envelope.FaultDocument;
import org.xmlsoap.schemas.soap.envelope.Detail;

import xmlobjecttest.soapfaults.FirstFaultType;
import xmlobjecttest.soapfaults.FirstFaultDocument;
import xmlobjecttest.soapfaults.SecondFaultType;
import xmlobjecttest.soapfaults.SecondFaultDocument;

import java.util.ArrayList;


/**
 *
 */
public class SoapFaultTest extends TestCase {
    public static final String soapenc = "http://schemas.xmlsoap.org/soap/encoding/";
    public static final String soapenv = "http://schemas.xmlsoap.org/soap/envelope/";

    public SoapFaultTest(String name) {
        super(name);
    }

    /** Regression test for Radar bug #25114 */

    /**
     *

    public void testSetDetail() throws Exception {
        Fault fault = Fault.Factory.newInstance();
        fault.setDetail(Detail.Factory.parse(XmlObject.Factory.parse("<foo/>").newXMLInputStream()));

        assertEquals("<detail><foo/></detail>", fault.xmlText());
        assertEquals("<xml-fragment><foo/></xml-fragment>", fault.getDetail().xmlText());
    }
       */

    /** Regression test for Radar bug #25119 */

    /**
     *
     */
    public void testAddNewDetail() throws Exception {
        Fault fault = Fault.Factory.newInstance();

        fault.setFaultcode(new QName(soapenv, "foo"));
        fault.setFaultstring("Undefined");
        fault.addNewDetail().set(
                XmlObject.Factory.parse("<foo/>").changeType(Detail.type));

        String expect = "<xml-fragment>" +
                "<faultcode xmlns:soapenv=\"" + soapenv + "\">soapenv:foo</faultcode>" +
                "<faultstring>Undefined</faultstring>" +
                "<detail><foo/></detail>" +
                "</xml-fragment>";
        assertEquals(expect, fault.xmlText());
        assertEquals(new QName(soapenv, "foo"), fault.getFaultcode());
        assertEquals("Undefined", fault.getFaultstring());
        assertEquals("<foo/>", fault.getDetail().xmlText());
    }

    /** Regression test for Radar bug #25409 */

    /**
     *
     * @status inactive

    public void testSetFaultDetail() throws Exception {
        String soapFault =
                "<soapenv:Fault xmlns:soapenv=\"" + soapenv + "\">" +
                "<faultcode>soapenv:Server</faultcode>" +
                "<faultstring>Undefined</faultstring>" +
                "<detail>" +
                "    <soap:a-string xmlns:soap=\"http://xmlobjecttest/soapfaults\">" +
                "        The First Fault" +
                "    </soap:a-string>" +
                "    <soap:a-int xmlns:soap=\"http://xmlobjecttest/soapfaults\">" +
                "        1" +
                "    </soap:a-int>" +
                "    <soap:a-date xmlns:soap=\"http://xmlobjecttest/soapfaults\">" +
                "       2003-03-28" +
                "    </soap:a-date>" +
                "</detail>" +
                "</soapenv:Fault>";

        Fault faultDoc = Fault.Factory.parse(soapFault);
        XmlOptions opt=new XmlOptions();
        ArrayList errors=new ArrayList();
        opt.setErrorListener(errors);
        try{
        assertTrue(faultDoc.validate(opt));
        }catch (Throwable t){
             for (int i = 0; i < errors.size(); i++) {
                XmlError error = (XmlError) errors.get(i);
                System.out.println("\n");
                System.out.println("Message: " + error.getMessage() + "\n");
                if (error.getCursorLocation() != null)
                    System.out.println("Location of invalid XML: " +
                            error.getCursorLocation().xmlText() + "\n");
            }
        }
        assertEquals(new QName(soapenv, "Server"), faultDoc.getFaultcode());
        assertEquals("Undefined", faultDoc.getFaultstring());


        FirstFaultType firstFault = FirstFaultType.Factory.newInstance();
        System.out.println("firstFault = " + firstFault.xmlText());
        firstFault.set(faultDoc.getDetail());

        assertEquals("The First Fault", firstFault.getAString().trim());
        assertEquals(1, firstFault.getAInt());
        assertEquals(new org.apache.xmlbeans.XmlCalendar("2003-03-28"),
                     firstFault.getADate());
    }
      */
}

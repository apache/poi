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

package scomp.namespace.checkin;

import junit.framework.TestCase;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlCursor;
import org.xmlsoap.schemas.soap.envelope.EnvelopeDocument;
import tools.xml.XmlComparator;
import javax.xml.namespace.QName;


public class PreserveNamespaces extends TestCase
{
    public static XmlOptions options;
    public static final String EOL = System.getProperty("line.separator");

    public void setUp()
    {
        options = new XmlOptions().setSavePrettyPrint().setSaveOuter();
    }

    //tests for preserving/copying namespace declarations when doing an XmlObject.set()
    public void testDroppedXsdNSDecl() throws Exception
    {
        // Test for XSD namespace declaration dropped
        EnvelopeDocument env1 = EnvelopeDocument.Factory.parse("<soap:Envelope \n" +
                "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \n" +
                "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" \n" +
                "xmlns:tns=\"http://Walkthrough/XmlWebServices/\" \n" +
                "xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
                "  <soap:Body>\n" +
                "    <tns:ConvertTemperature>\n" +
                "      <dFahrenheit xsi:type=\"xsd:double\">88</dFahrenheit>\n" +
                "    </tns:ConvertTemperature>\n" +
                "  </soap:Body>\n" +
                "</soap:Envelope>");

        EnvelopeDocument env2 = EnvelopeDocument.Factory.newInstance();
        env2.addNewEnvelope().setBody(env1.getEnvelope().getBody());

        // compare the 2 body elements using the Xml Comparator. This uses a cursor to walk thro the docs and compare elements and attributes
        tools.xml.XmlComparator.Diagnostic diag = new tools.xml.XmlComparator.Diagnostic();
        assertTrue(XmlComparator.lenientlyCompareTwoXmlStrings(env1.getEnvelope().getBody().xmlText(), env2.getEnvelope().getBody().xmlText(), diag));

        // navigate to the dFahrenhiet element and check for the XSD namespace
        XmlCursor env2Cursor = env2.newCursor();
        assertTrue(env2Cursor.toFirstChild());      // <Envelope>
        assertTrue(env2Cursor.toFirstChild());      // <Body>
        assertTrue(env2Cursor.toFirstChild());      // <ConvertTemperature>
        if (env2Cursor.toFirstChild())               // <dFahrenheit>
        {
            assertTrue("Element name mismatch!", env2Cursor.getName().equals(new QName("","dFahrenheit")));
            assertEquals("Element val mismatch!", "88", env2Cursor.getTextValue());
            assertEquals("XSD Namespace has been dropped", "http://www.w3.org/2001/XMLSchema", env2Cursor.namespaceForPrefix("xsd"));
        }

    }

    public void testsModifiedXsdNSPrefix() throws Exception
    {
        // XSD namespace used in QName values and elements
        EnvelopeDocument env1 = EnvelopeDocument.Factory.parse("<soap:Envelope \n" +
                "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \n" +
                "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" \n" +
                "xmlns:tns=\"http://Walkthrough/XmlWebServices/\" \n" +
                "xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
                "  <soap:Body>\n" +
                "      <xsd:element name=\"myname\" type=\"xsd:string\"/>\n" +
                "  </soap:Body>\n" +
                "</soap:Envelope>");

        EnvelopeDocument env2 = EnvelopeDocument.Factory.newInstance();
        env2.addNewEnvelope().setBody(env1.getEnvelope().getBody());

        // compare the 2 body elements using the Xml Comparator. This uses a cursor to walk thro the docs and compare elements and attributes
        tools.xml.XmlComparator.Diagnostic diag = new tools.xml.XmlComparator.Diagnostic();
        assertTrue("new envelope has missing XSD namespace declaration", XmlComparator.lenientlyCompareTwoXmlStrings(env1.getEnvelope().getBody().xmlText(), env2.getEnvelope().getBody().xmlText(), diag));

        // navigate to the 'element' element and check for the XSD namespace
        XmlCursor env2Cursor = env2.newCursor();
        assertTrue(env2Cursor.toFirstChild());      // <Envelope>
        assertTrue(env2Cursor.toFirstChild());      // <Body>
        if (env2Cursor.toFirstChild())              // <element>
        {
            assertTrue("Element name mismatch!", env2Cursor.getName().equals(new QName("http://www.w3.org/2001/XMLSchema","element")));
            assertEquals("XSD Namespace has been dropped", "http://www.w3.org/2001/XMLSchema", env2Cursor.namespaceForPrefix("xsd"));
        }

    }

    public void testsFaultCodeNSUpdate() throws Exception
    {
        EnvelopeDocument env1 = EnvelopeDocument.Factory.parse("<soap:Envelope \n" +
                "xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
                "  <soap:Body>\n" +
                "      <soap:Fault>\n" +
                "           <faultcode>soap:Server</faultcode>\n" +
                "           <faultstring>my error message</faultstring>\n" +
                "      </soap:Fault>\n" +
                "  </soap:Body>\n" +
                "</soap:Envelope>");

        // Test for NS of the faultcode element
        EnvelopeDocument env2 = EnvelopeDocument.Factory.newInstance();
        env2.addNewEnvelope().setBody(env1.getEnvelope().getBody());

        // compare the 2 body elements using the Xml Comparator. This uses a cursor to walk thro the docs and compare elements and attributes
        tools.xml.XmlComparator.Diagnostic diag = new tools.xml.XmlComparator.Diagnostic();
        assertTrue("new envelope has missing XSD namespace declaration", XmlComparator.lenientlyCompareTwoXmlStrings(env1.getEnvelope().getBody().xmlText(), env2.getEnvelope().getBody().xmlText(), diag));

        // navigate to the soap element and check for the 'soap' namespace
        XmlCursor env2Cursor = env2.newCursor();
        assertTrue(env2Cursor.toFirstChild());      // <Envelope>
        assertTrue(env2Cursor.toFirstChild());      // <Body>
        assertTrue(env2Cursor.toFirstChild());      // <Fault>
        if (env2Cursor.toFirstChild())              // <faultcode>
        {
            assertTrue("Element name mismatch!", env2Cursor.getName().equals(new QName("","faultcode")));
            assertEquals("soap Namespace has been dropped", "http://schemas.xmlsoap.org/soap/envelope/", env2Cursor.namespaceForPrefix("soap"));
        }

    }

}

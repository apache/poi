/*   Copyright 2006 The Apache Software Foundation
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
 *   limitations under the License.
 */
package misc.detailed;

import java.io.InputStream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;

import junit.framework.TestCase;

/**
 * Adapted from testcase submitted by Brian Bonner for JIRA issue
 * XMLBEANS-222, based on comments by Cezar Andrei.
 */
public class XMLStreamReaderTest extends TestCase {
    private static final String soapMsg = "<SOAP:Envelope xmlns:SOAP=\"http://schemas.xmlsoap.org/soap/envelope/\" "
            + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
            + "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">"
            + "<SOAP:Body>"
            + "<PriceandAvailabilityResponse xmlns=\"http://www.foobar.com\">"
            + "<Header/>"
            + "<Body>"
            + "<UPC xsi:nil=\"true\"/>"
            + "</Body>"
            + "</PriceandAvailabilityResponse>"
            + "</SOAP:Body>"
            + "</SOAP:Envelope>";

    public void testXmlStreamReader1() throws Exception {
        XmlObject object = XmlObject.Factory.parse(soapMsg);

        XmlOptions opts = new XmlOptions().setSaveOuter();
        XMLStreamReader reader = object.newXMLStreamReader(opts);

        boolean foundXsiNamespace = false;
        while (reader.hasNext()) {
            int event = reader.next();

            if (event == XMLStreamReader.START_ELEMENT) {
                System.out.println("namespace count: " + reader.getNamespaceCount());
                for (int i = 0; i < reader.getNamespaceCount(); i++) {
                    if (reader.getNamespacePrefix(i).equals("xsi")) {
                        foundXsiNamespace = true;
                    }
                    System.out.println("Namespace "
                            + reader.getNamespacePrefix(i) + ": "
                            + reader.getNamespaceURI(i));
                }
            }
        }
        assertTrue("xsi namespace is not found", foundXsiNamespace);
    }
    
    public void testXmlStreamReader2() throws Exception {
        XmlObject object = XmlObject.Factory.parse(soapMsg);

        XMLStreamReader reader = object.newXMLStreamReader();

        boolean foundXsiNamespace = false;
        int event = reader.getEventType();
        do
        {
            if (event == XMLStreamReader.START_ELEMENT)
            {
                System.out.println("namespace count: " + reader.getNamespaceCount());
                for (int i = 0; i < reader.getNamespaceCount(); i++)
                {
                    if (reader.getNamespacePrefix(i).equals("xsi"))
                    {
                        foundXsiNamespace = true;
                    }
                    System.out.println("Namespace "
                            + reader.getNamespacePrefix(i) + ": "
                            + reader.getNamespaceURI(i));
                }
            }
            event = reader.next();
        }
        while (reader.hasNext());
        assertTrue("xsi namespace is not found", foundXsiNamespace);
    }

}

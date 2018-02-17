/*
 *   Copyright 2004 The Apache Software Foundation
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

package misc.common;

import junit.framework.TestCase;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.StringReader;


public class ParsersBase extends TestCase {

    private static final String outputEncoding = "UTF-8";

    // for reference  - the values for System Properties to switch between different parser implementaion for JAXP
    // ----------------------------------------------------------------------------------------------------------
    // System Property                               Parser                        Value
    // ----------------------------------------------------------------------------------------------------------
    // javax.xml.parsers.DocumentBuilderFactory     Xerces              org.apache.xerces.jaxp.DocumentBuilderFactoryImpl
    //                                              Crimson             org.apache.crimson.jaxp.DocumentBuilderFactoryImpl
    //                                              Piccolo             NA
    //
    // org.xml.sax.driver                           Xerces              org.apache.xerces.parsers.SAXParser
    //                                              Crimson             org.apache.crimson.parser.XmlReaderImpl
    //                                              Piccolo (Xbeans)    org.apache.xmlbeans.impl.piccolo.xml.Piccolo
    //
    // javax.xml.parsers.SAXParserFactory           Xerces              org.apache.xerces.jaxp.SAXParserFactoryImpl
    //                                              Crimson             org.apache.crimson.jaxp.SAXParserFactoryImpl
    //                                              Piccolo (XBeans)    org.apache.xmlbeans.impl.piccolo.xml.JAXPSAXParserFactory
    // ----------------------------------------------------------------------------------------------------------



    // This method parsers the input xml string using the DOM API with the parser specified using the 
    // "javax.xml.parsers.DocumentBuilderFactory" system property
    public void parseXmlWithDOMAPI(String xmlInput, String parserName, String docbuilderfactory) {

        System.setProperty("javax.xml.parsers.DocumentBuilderFactory", docbuilderfactory);
        try {
            // Step 1: create a DocumentBuilderFactory and configure it
            DocumentBuilderFactory dbf =
                    DocumentBuilderFactory.newInstance();

            dbf.setNamespaceAware(true);

            // Set the validation mode to either: no validation, DTD
            // validation, or XSD validation
            dbf.setValidating(false);

            // Step 2: create a DocumentBuilder that satisfies the constraints
            // specified by the DocumentBuilBderFactory
            DocumentBuilder db = dbf.newDocumentBuilder();

            // Step 3: parse the input string
            Document doc = db.parse(new InputSource(new StringReader(xmlInput)));
        }
        catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        }
        catch (SAXParseException spe) {
            System.out.println("parsing error with " + parserName + " (DOM) for xml input string :'" + xmlInput + "''");
            spe.printStackTrace();
            fail("parsing error with " + parserName + " (DOM) for xml input string :'" + xmlInput + "''");
        }
        catch (SAXException se) {
            se.printStackTrace();
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    // This method parsers the input xml string using the SAX API with the parser specified using the 
    // "javax.xml.parsers.SAXParserFactory" and "org.xml.sax.driver" system properties
    public void parseXmlWithSAXAPI(String xmlInput, String parserName, String saxdriverprop, String saxparserfactoryprop) {
        try {

            // Set the system props to pick the appropriate parser implementation
            System.setProperty("org.xml.sax.driver", saxdriverprop);
            System.setProperty("javax.xml.parsers.SAXParserFactory", saxparserfactoryprop);

            SAXParserFactory saxparserfactory = SAXParserFactory.newInstance();
            saxparserfactory.setNamespaceAware(false);

            XMLReader xmlreader = saxparserfactory.newSAXParser().getXMLReader();
            xmlreader.parse(new InputSource(new StringReader(xmlInput)));
        }
        catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        }
        catch (SAXParseException spe) {
            System.out.println("parsing error with " + parserName + " (SAX) for xml input string :'" + xmlInput + "'");
            spe.printStackTrace();
            fail("parsing error with " + parserName + " for xml input string :'" + xmlInput + "'");
        }
        catch (SAXException se) {
            se.printStackTrace();
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }


}


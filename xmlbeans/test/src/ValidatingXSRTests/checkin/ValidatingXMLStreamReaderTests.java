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
package ValidatingXSRTests.checkin;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.Assert;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.Location;
import javax.xml.stream.events.XMLEvent;
import javax.xml.namespace.QName;
import javax.xml.namespace.NamespaceContext;
import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.xmlbeans.impl.validator.ValidatingXMLStreamReader;
import org.apache.xmlbeans.impl.xb.xsdschema.SchemaDocument;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlException;

import org.openuri.testNumerals.DocDocument;


import tools.util.JarUtil;

/**
 *
 */
public class ValidatingXMLStreamReaderTests extends TestCase
{
    public ValidatingXMLStreamReaderTests(String name) { super(name); }

    public static Test suite() { return new TestSuite(ValidatingXMLStreamReaderTests.class); }

    private final static String URI_NUMERALS = "http://openuri.org/testNumerals";

    public void testValidateDoc() throws FileNotFoundException, XMLStreamException,Exception
    {
        File[] files = new File[] {
             JarUtil.getResourceFromJarasFile("xbean/xmlobject/enumtest.xml") ,
             JarUtil.getResourceFromJarasFile("xbean/xmlobject/nameworld.xml") ,
             JarUtil.getResourceFromJarasFile("xbean/misc/xmldsig-core-schema.xsd") ,
            //TestEnv.xbeanCase("xbean/misc/signature-example.xml")
        };

        ValidatingXMLStreamReader valXsr = new ValidatingXMLStreamReader();

        for( int i = 0; i<files.length; i++)
        {
            validate(valXsr, files[i]);
        }
    }

    private static void validate(ValidatingXMLStreamReader valXsr, File file) throws XMLStreamException, IOException
    {
        Collection errors = new ArrayList();
        XMLStreamReader xsr = null;
        try {
            xsr = XmlObject.Factory.
                        parse(new FileInputStream(file)).newXMLStreamReader();
        } catch (XmlException e) {
            throw new XMLStreamException(e);
        }

        valXsr.init(xsr, true, null /* validate an entire document */ ,
            XmlBeans.typeLoaderForClassLoader(ValidatingXMLStreamReader.class.getClassLoader()),
            null,
            errors);

        while( valXsr.hasNext() )
        {
            valXsr.next();
        }

        if (!valXsr.isValid())
            System.out.println("File '" + file + "' is: " + (valXsr.isValid() ? "Valid" : "INVALID"));

        for (Iterator i = errors.iterator(); i.hasNext(); )
        {
            XmlError err = (XmlError)i.next();

            String sev = (err.getSeverity()==XmlError.SEVERITY_WARNING ? "WARNING" :
                (err.getSeverity()==XmlError.SEVERITY_INFO ? "INFO" : "ERROR"));

            System.out.println(sev + " " + err.getLine() + ":" + err.getColumn() + " " + err.getMessage() + " ");
        }

        if (!valXsr.isValid())
            System.out.println("---------------\n");

        Assert.assertTrue("File '" + file.getName() +"' is invalid.", valXsr.isValid());
    }

    public void testValidateGlobalAtt1() throws XMLStreamException
    {
        XmlObject xo = XmlObject.Factory.newInstance();
        XmlCursor xc = xo.newCursor();
        xc.toNextToken();

        xc.insertAttributeWithValue("price", URI_NUMERALS, "23.5");

        XMLStreamReader xsr = xo.newXMLStreamReader(new XmlOptions().setSaveOuter());
        Collection errors = new ArrayList();

        ValidatingXMLStreamReader valXsr = new ValidatingXMLStreamReader();
        valXsr.init(xsr, false, null,
            XmlBeans.typeLoaderForClassLoader(ValidatingXMLStreamReader.class.getClassLoader()),
            null, errors);

        while(valXsr.hasNext())
        {
            valXsr.next();
        }

        if (!valXsr.isValid())
        {
            System.out.println("---------------\n");
            Iterator i = errors.iterator();
            while (i.hasNext())
            {
                XmlError xmlError = (XmlError) i.next();
                System.out.println(xmlError.getSeverity() + " " + xmlError.getMessage());
            }
        }

        Assert.assertTrue("Global attribute validation is broken.", valXsr.isValid());

    }

    public void testValidateGlobalAtt2() throws XMLStreamException
    {

        XMLStreamReader xsr = new TestXSR();
        Collection errors = new ArrayList();

        ValidatingXMLStreamReader valXsr = new ValidatingXMLStreamReader();
        valXsr.init(xsr, false, null,
            XmlBeans.typeLoaderForClassLoader(ValidatingXMLStreamReader.class.getClassLoader()),
            null, errors);

        while(valXsr.hasNext())
        {
            valXsr.next();
        }

        for (Iterator i = errors.iterator(); i.hasNext(); )
        {
            XmlError err = (XmlError)i.next();

            String sev = (err.getSeverity()==XmlError.SEVERITY_WARNING ? "WARNING" :
                (err.getSeverity()==XmlError.SEVERITY_INFO ? "INFO" : "ERROR"));

            System.out.println(sev + " " + err.getLine() + ":" + err.getColumn() + " " + err.getMessage() + " ");
        }

        Assert.assertTrue("Global attribute validation 2 is broken.", valXsr.isValid());
    }

    private static class TestXSR implements XMLStreamReader
    {
        private int _state = 0;

        public Object getProperty(String name) throws IllegalArgumentException
        {
            return null;
        }

        public int next() throws XMLStreamException
        {
            int state = _state;
            _state++;

            switch(state)
            {
                case 0:
                    return XMLEvent.START_DOCUMENT;

                case 1:
                    return XMLEvent.ATTRIBUTE;
            }
            return XMLEvent.END_DOCUMENT;
        }

        public void require(int type, String namespaceURI, String localName) throws XMLStreamException
        {}

        public String getElementText() throws XMLStreamException
        {
            return null;
        }

        public int nextTag() throws XMLStreamException
        {
            return next();
        }

        public boolean hasNext() throws XMLStreamException
        {
            return _state<3;
        }

        public void close() throws XMLStreamException
        {}

        public String getNamespaceURI(String prefix)
        {
            if ("".equals(prefix))
                return URI_NUMERALS;
            return null;
        }

        public boolean isStartElement()
        {
            return false;
        }

        public boolean isEndElement()
        {
            return false;
        }

        public boolean isCharacters()
        {
            return false;
        }

        public boolean isWhiteSpace()
        {
            return false;
        }

        public String getAttributeValue(String namespaceURI, String localName)
        {
            if (URI_NUMERALS.equals(namespaceURI) && "price".equals(localName))
                return "5";
            throw new IllegalStateException();
        }

        public int getAttributeCount()
        {
            return 1;
        }

        public QName getAttributeName(int index)
        {
            if (index==0)
                return new QName(URI_NUMERALS, "price");
            throw new IllegalStateException();
        }

        public String getAttributeNamespace(int index)
        {
            if (index==0)
                return URI_NUMERALS;
            throw new IllegalStateException();
        }

        public String getAttributeLocalName(int index)
        {
            if (index==0)
                return "price";
            throw new IllegalStateException();
        }

        public String getAttributePrefix(int index)
        {
            if (index==0)
                return "";
            throw new IllegalStateException();
        }

        public String getAttributeType(int index)
        {
            if (index==0)
                return  "CDATA";
            throw new IllegalStateException();
        }

        public String getAttributeValue(int index)
        {
            if (index==0)
                return "8.7654321";
            throw new IllegalStateException();
        }

        public boolean isAttributeSpecified(int index)
        {
            if (index==0)
                return true;
            throw new IllegalStateException();
        }

        public int getNamespaceCount()
        {
            return 0;
        }

        public String getNamespacePrefix(int index)
        {
            return null;
        }

        public String getNamespaceURI(int index)
        {
            return null;
        }

        public NamespaceContext getNamespaceContext()
        {
            return null;
        }

        public int getEventType()
        {
            return XMLEvent.ATTRIBUTE;
        }

        public String getText()
        {
            return null;
        }

        public char[] getTextCharacters()
        {
            return new char[0];
        }

        public int getTextCharacters(int sourceStart, char[] target, int targetStart, int length) throws XMLStreamException
        {
            return 0;
        }

        public int getTextStart()
        {
            return 0;
        }

        public int getTextLength()
        {
            return 0;
        }

        public String getEncoding()
        {
            return null;
        }

        public boolean hasText()
        {
            return false;
        }

        public Location getLocation()
        {
            return null;
        }

        public QName getName()
        {
            return null;
        }

        public String getLocalName()
        {
            return null;
        }

        public boolean hasName()
        {
            return false;
        }

        public String getNamespaceURI()
        {
            return null;
        }

        public String getPrefix()
        {
            return null;
        }

        public String getVersion()
        {
            return null;
        }

        public boolean isStandalone()
        {
            return false;
        }

        public boolean standaloneSet()
        {
            return false;
        }

        public String getCharacterEncodingScheme()
        {
            return null;
        }

        public String getPITarget()
        {
            return null;
        }

        public String getPIData()
        {
            return null;
        }

    }

    public void testValidateContent1() throws XMLStreamException
    {
        XmlObject xo = XmlObject.Factory.newInstance();
        XmlCursor xc = xo.newCursor();
        xc.toNextToken();

        xc.insertElementWithText("int", URI_NUMERALS, "5");
        xc.insertElementWithText("float", URI_NUMERALS, "7.654321");

        XMLStreamReader xsr = xo.newXMLStreamReader();
        Collection errors = new ArrayList();

        ValidatingXMLStreamReader valXsr = new ValidatingXMLStreamReader();
        valXsr.init(xsr, true, DocDocument.Doc.type,
            XmlBeans.typeLoaderForClassLoader(ValidatingXMLStreamReader.class.getClassLoader()),
            null, errors);

        int depth = 0;

        loop:   while(valXsr.hasNext())
        {
            int evType = valXsr.getEventType();
//            printState(valXsr);
            if (evType==XMLEvent.END_ELEMENT)
            {
                depth++;
                if (depth>=2)
                {
                    break loop;
                }
            }
            valXsr.next();
        }
        Assert.assertTrue("Content1 validation is broken.", valXsr.isValid());
    }

    private static void printState(XMLStreamReader vxsr)
    {
        int et = vxsr.getEventType();
        switch(et)
        {
            case XMLEvent.START_ELEMENT:
                System.out.println("  SE " + vxsr.getName() + " atts: " + vxsr.getAttributeCount());
                for (int i = 0; i < vxsr.getAttributeCount(); i++)
                {
                    System.out.println("    AT " + vxsr.getAttributeName(i) + " = '" + vxsr.getAttributeValue(i) + "'");
                }
                break;
            case XMLEvent.END_ELEMENT:
                System.out.println("  EE");
                break;
            case XMLEvent.START_DOCUMENT:
                System.out.println("  SD");
                break;
            case XMLEvent.END_DOCUMENT:
                System.out.println("  ED");
                break;
            case XMLEvent.CHARACTERS:
            case XMLEvent.CDATA:
            case XMLEvent.SPACE:
                System.out.println("  TX " + vxsr.hasText() + " '" + (vxsr.hasText() ? vxsr.getText() : ""));
                break;
            case XMLEvent.ATTRIBUTE:
            case XMLEvent.NAMESPACE:
                System.out.println("  ATT " + vxsr.getName() + " '" + vxsr.getText() + "'");
                break;
            default:
                System.out.println("  OTHER " + et + " '" + (vxsr.hasText() ? vxsr.getText() : ""));
                break;
        }
    }


    public void testValidateContent2() throws XMLStreamException
    {
        String doc = "<doc xmlns='" + URI_NUMERALS + "'><int>5</int><float>7.654321</float></doc>";

        XMLStreamReader xsr = null;
        try {
            xsr = XmlObject.Factory.parse(doc).newXMLStreamReader();
        } catch (XmlException e) {
            throw new XMLStreamException(e);
        }
        xsr.nextTag();

        Collection errors = new ArrayList();

        ValidatingXMLStreamReader valXsr = new ValidatingXMLStreamReader();
        valXsr.init(xsr, false, DocDocument.Doc.type,
            XmlBeans.typeLoaderForClassLoader(ValidatingXMLStreamReader.class.getClassLoader()),
            null, errors);

        int depth = 0;

loop:   while(valXsr.hasNext())
        {
            int evType = valXsr.next();

            switch (evType)
            {
            case XMLEvent.END_ELEMENT:
                depth++;
                if (depth>=2)
                {
                    break loop;
                }
            }
        }

        for (Iterator i = errors.iterator(); i.hasNext(); )
        {
            XmlError err = (XmlError)i.next();

            String sev = (err.getSeverity()==XmlError.SEVERITY_WARNING ? "WARNING" :
                (err.getSeverity()==XmlError.SEVERITY_INFO ? "INFO" : "ERROR"));

            System.out.println(sev + " " + err.getLine() + ":" + err.getColumn() + " " + err.getMessage() + " ");
        }

        Assert.assertTrue("Content2 validation is broken.", valXsr.isValid());
    }
}

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
package org.apache.xmlbeans.test.performance.parsers;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;

import javax.xml.namespace.QName;
import java.util.Collection;

/**
 * @author Cezar Andrei (cezar.andrei at bea.com)
 *         Date: Aug 8, 2005
 */
public class BaseSaxPerfTest
{
    public static class CopyAndStoreDataSaxHandler
        extends DoNothingSaxHandler
        implements ContentHandler, LexicalHandler, ErrorHandler
    {
        private Collection _store;

        CopyAndStoreDataSaxHandler(XMLReader r, Collection c)
        {
            super(r);
            if (c==null)
                throw new IllegalArgumentException();
            _store = c;
        }

        public void startPrefixMapping(String prefix, String uri)
            throws SAXException
        {
//            System.out.println("  PM: " + prefix + "=" + uri);
        }

        public void startElement(String uri, String localName, String qName, Attributes atts)
            throws SAXException
        {
            QName elName = new QName(uri, localName, qName);
            _store.add(elName);
//            System.out.println("  SE: " + elName);
            int l = atts.getLength();
            for (int i = 0; i < l; i++)
            {
                QName attName = new QName(atts.getURI(i), atts.getLocalName(i), atts.getQName(i));
                String val = atts.getValue(i);
                _store.add(attName);
                _store.add(val);
//                System.out.println("    AT: " + attName + "='" + val + "' " + atts.getType(i));
            }
        }

        public void endElement(String uri, String localName, String qName)
            throws SAXException
        {
            QName elName = new QName(uri, localName, qName);
            _store.add(elName);
//            System.out.println("  EE: " + elName);
        }

        public void characters(char ch[], int start, int length)
            throws SAXException
        {
            char[] buf = new char[length];
            System.arraycopy(ch, start, buf, 0, length);
            _store.add(buf);
//            System.out.println("  CH: '" + new String(buf).replace("\n", "\\n") + "'");
        }

        public void ignorableWhitespace(char ch[], int start, int length)
            throws SAXException
        {
            char[] buf = new char[length];
            System.arraycopy(ch, start, buf, 0, length);
            _store.add(buf);
//            System.out.println("  SP: '" + new String(buf).replace("\n", "\\n") + "'");
        }

        public void comment(char ch[], int start, int length)
            throws SAXException
        {
            char[] buf = new char[length];
            System.arraycopy(ch, start, buf, 0, length);
            _store.add(buf);
        }
    }

    public static class CopyDataSaxHandler extends DoNothingSaxHandler
        implements ContentHandler, LexicalHandler, ErrorHandler
    {
        CopyDataSaxHandler(XMLReader r)
        {
            super(r);
        }

        public void startPrefixMapping(String prefix, String uri)
            throws SAXException
        {
//            System.out.println("  PM: " + prefix + "=" + uri);
        }

        public void startElement(String uri, String localName, String qName, Attributes atts)
            throws SAXException
        {
            QName elName = new QName(uri, localName, qName);
//            System.out.println("  SE: " + elName);
            int l = atts.getLength();
            for (int i = 0; i < l; i++)
            {
                QName attName = new QName(atts.getURI(i), atts.getLocalName(i), atts.getQName(i));
                String val = atts.getValue(i);
//                System.out.println("    AT: " + attName + "='" + val + "' " + atts.getType(i));
            }
        }

        public void endElement(String uri, String localName, String qName)
            throws SAXException
        {
            QName elName = new QName(uri, localName, qName);
//            System.out.println("  EE: " + elName);
        }

        public void characters(char ch[], int start, int length)
            throws SAXException
        {
            char[] buf = new char[length];
            System.arraycopy(ch, start, buf, 0, length);
//            System.out.println("  CH: '" + new String(buf).replace("\n", "\\n") + "'");
        }

        public void ignorableWhitespace(char ch[], int start, int length)
            throws SAXException
        {
            char[] buf = new char[length];
            System.arraycopy(ch, start, buf, 0, length);
//            System.out.println("  SP: '" + new String(buf).replace("\n", "\\n") + "'");
        }

        public void comment(char ch[], int start, int length)
            throws SAXException
        {
            char[] buf = new char[length];
            System.arraycopy(ch, start, buf, 0, length);
        }
    }

    public static class DoNothingSaxHandler
        implements ContentHandler, LexicalHandler, ErrorHandler
    {
        private XMLReader _xr;

        DoNothingSaxHandler(XMLReader r)
        {
            _xr = r;

            try
            {
                _xr.setFeature(
                    "http://xml.org/sax/features/namespace-prefixes", true);
                _xr.setFeature("http://xml.org/sax/features/namespaces", true);
                _xr.setFeature("http://xml.org/sax/features/validation", false);
                _xr.setProperty(
                    "http://xml.org/sax/properties/lexical-handler", this);
                _xr.setContentHandler(this);
                _xr.setErrorHandler(this);
            }
            catch (Throwable e)
            {
                throw new RuntimeException(e.getMessage(), e);
            }
        }

        public void setDocumentLocator(Locator locator)
        {
        }

        public void startDocument()
            throws SAXException
        {
        }

        public void endDocument()
            throws SAXException
        {
        }

        public void startPrefixMapping(String prefix, String uri)
            throws SAXException
        {
//            System.out.println("  PM: " + prefix + "=" + uri);
        }

        public void endPrefixMapping(String prefix)
            throws SAXException
        {
        }

        public void startElement(String uri, String localName, String qName, Attributes atts)
            throws SAXException
        {
        }

        public void endElement(String uri, String localName, String qName)
            throws SAXException
        {
        }

        public void characters(char ch[], int start, int length)
            throws SAXException
        {
        }

        public void ignorableWhitespace(char ch[], int start, int length)
            throws SAXException
        {
        }

        public void processingInstruction(String target, String data)
            throws SAXException
        {
        }

        public void skippedEntity(String name)
            throws SAXException
        {
        }

        public void startDTD(String name, String publicId, String systemId)
            throws SAXException
        {
        }

        public void endDTD()
            throws SAXException
        {
        }

        public void startEntity(String name)
            throws SAXException
        {
        }

        public void endEntity(String name)
            throws SAXException
        {
        }

        public void startCDATA()
            throws SAXException
        {
//            System.out.println("CDATA start");
        }

        public void endCDATA()
            throws SAXException
        {
//            System.out.println("CDATA start");
        }

        public void comment(char ch[], int start, int length)
            throws SAXException
        {
        }

        public void warning(SAXParseException exception)
            throws SAXException
        {
            exception.printStackTrace(System.out);
        }

        public void error(SAXParseException exception)
            throws SAXException
        {
            exception.printStackTrace(System.out);
        }

        public void fatalError(SAXParseException exception)
            throws SAXException
        {
            exception.printStackTrace(System.out);
        }
    }
}

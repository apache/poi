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

/**
 * Author: Cezar Andrei ( cezar.andrei at bea.com )
 * Date: Nov 11, 2003
 */
package org.apache.xmlbeans.impl.common;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;


public class LoadSaveUtils
{
    public static Document xmlText2GenericDom(InputStream is, Document emptyDoc)
            throws SAXException, ParserConfigurationException, IOException
    {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);

        SAXParser parser = factory.newSAXParser();

        Sax2Dom handler = new Sax2Dom(emptyDoc);

        parser.setProperty("http://xml.org/sax/properties/lexical-handler", handler);
        parser.parse(is, handler);

        return (Document) handler.getDOM();
    }

    public static void xmlStreamReader2XmlText(XMLStreamReader xsr, OutputStream os)
            throws XMLStreamException
    {
        //REVIEW (zieg): I think we can cache this factory
        XMLStreamWriter xsw = XMLOutputFactory.newInstance().createXMLStreamWriter(os);

        while (xsr.hasNext())
        {
            switch( xsr.getEventType() )
            {
                case XMLStreamReader.ATTRIBUTE:
                    xsw.writeAttribute(xsr.getPrefix(), xsr.getNamespaceURI(), xsr.getLocalName(), xsr.getText());
                    break;

                case XMLStreamReader.CDATA:
                    xsw.writeCData(xsr.getText());
                    break;

                case XMLStreamReader.CHARACTERS:
                    xsw.writeCharacters(xsr.getText());
                    break;

                case XMLStreamReader.COMMENT:
                    xsw.writeComment(xsr.getText());
                    break;

                case XMLStreamReader.DTD:
                    xsw.writeDTD(xsr.getText());
                    break;

                case XMLStreamReader.END_DOCUMENT:
                    xsw.writeEndDocument();
                    break;

                case XMLStreamReader.END_ELEMENT:
                    xsw.writeEndElement();
                    break;

                case XMLStreamReader.ENTITY_DECLARATION:
                    break;

                case XMLStreamReader.ENTITY_REFERENCE:
                    xsw.writeEntityRef(xsr.getText());
                    break;

                case XMLStreamReader.NAMESPACE:
                    xsw.writeNamespace(xsr.getPrefix(), xsr.getNamespaceURI());
                    break;

                case XMLStreamReader.NOTATION_DECLARATION:
                    break;

                case XMLStreamReader.PROCESSING_INSTRUCTION:
                    xsw.writeProcessingInstruction(xsr.getPITarget(), xsr.getPIData());
                    break;

                case XMLStreamReader.SPACE:
                    xsw.writeCharacters(xsr.getText());
                    break;

                case XMLStreamReader.START_DOCUMENT:
                    xsw.writeStartDocument();
                    break;

                case XMLStreamReader.START_ELEMENT:
                    xsw.writeStartElement(xsr.getPrefix()==null ? "" : xsr.getPrefix(), xsr.getLocalName(), xsr.getNamespaceURI());

                    int attrs = xsr.getAttributeCount();
                    for ( int i = attrs-1; i>=0; i--)
                    {
                        xsw.writeAttribute(xsr.getAttributePrefix(i)==null ? "" : xsr.getAttributePrefix(i), xsr.getAttributeNamespace(i), xsr.getAttributeLocalName(i), xsr.getAttributeValue(i));
                    }

                    int nses = xsr.getNamespaceCount();
                    for ( int i = 0; i<nses; i++)
                    {
                        xsw.writeNamespace(xsr.getNamespacePrefix(i), xsr.getNamespaceURI(i));
                    }
                    break;
            }
            xsr.next();
        }
        xsw.flush();
    }
}

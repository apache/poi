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

import org.xml.sax.XMLReader;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.FileInputStream;
import java.util.Collection;
import java.util.ArrayList;

/**
 * @author Cezar Andrei (cezar.andrei at bea.com)
 *         Date: Jul 18, 2005
 */
public class XercesSaxParserPerfTests
{
    static XMLReader _xercesSaxParser;
    static
    {
        try
        {
            _xercesSaxParser = org.apache.xerces.jaxp.SAXParserFactoryImpl.newInstance().newSAXParser().getXMLReader();
            System.out.println("Xerces SAX parser: " + _xercesSaxParser + " " + org.apache.xerces.impl.Version.getVersion());
        }
        catch (SAXException e)
        {
            e.printStackTrace();
        }
        catch (ParserConfigurationException e)
        {
            e.printStackTrace();
        }
    }

    public static class DoNothingParseFile
        extends Utils.ParseFile
    {
        public void execute(String file)
            throws IOException, SAXException
        {
            new BaseSaxPerfTest.DoNothingSaxHandler(_xercesSaxParser);
            _xercesSaxParser.parse(new InputSource(new FileInputStream(file)));
        }
    }

    public static class CopyDataParseFile
        extends Utils.ParseFile
    {
        public void execute(String file)
            throws IOException, SAXException
        {
            new BaseSaxPerfTest.CopyDataSaxHandler(_xercesSaxParser);
            _xercesSaxParser.parse(new InputSource(new FileInputStream(file)));
        }
    }

    public static class CopyAndStoreDataParseFile
        extends Utils.ParseFile
    {
        public void execute(String file)
            throws IOException, SAXException
        {
            Collection store = new ArrayList();
            new BaseSaxPerfTest.CopyAndStoreDataSaxHandler(_xercesSaxParser, store);
            _xercesSaxParser.parse(new InputSource(new FileInputStream(file)));
        }
    }
}

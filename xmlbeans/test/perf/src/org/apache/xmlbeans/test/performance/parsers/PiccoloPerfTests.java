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

import java.io.IOException;
import java.io.FileInputStream;
import java.util.Collection;
import java.util.ArrayList;

/**
 * @author Cezar Andrei (cezar.andrei at bea.com)
 *         Date: Jul 12, 2005
 */
public class PiccoloPerfTests
{
    static XMLReader picollo = new org.apache.xmlbeans.impl.piccolo.xml.Piccolo();

    public static class DoNothingParseFile
        extends Utils.ParseFile
    {
        public void execute(String file)
            throws IOException, SAXException
        {
            new BaseSaxPerfTest.DoNothingSaxHandler(PiccoloPerfTests.picollo);
            PiccoloPerfTests.picollo.parse(new InputSource(new FileInputStream(file)));
        }
    }

    public static class CopyDataParseFile
        extends Utils.ParseFile
    {
        public void execute(String file)
            throws IOException, SAXException
        {
            new BaseSaxPerfTest.CopyDataSaxHandler(PiccoloPerfTests.picollo);
            PiccoloPerfTests.picollo.parse(new InputSource(new FileInputStream(file)));
        }
    }

    public static class CopyAndStoreDataParseFile
        extends Utils.ParseFile
    {
        public void execute(String file)
            throws IOException, SAXException
        {
            Collection store = new ArrayList();
            new BaseSaxPerfTest.CopyAndStoreDataSaxHandler(PiccoloPerfTests.picollo, store);
            PiccoloPerfTests.picollo.parse(new InputSource(new FileInputStream(file)));
        }
    }
}

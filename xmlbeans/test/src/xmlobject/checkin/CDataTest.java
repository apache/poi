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

package xmlobject.checkin;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.SystemProperties;
import org.apache.xmlbeans.XmlException;

import dumbNS.RootDocument.Root;
import dumbNS.RootDocument;

import tools.util.Util;
import tools.util.ResourceUtil;
import tools.util.JarUtil;
import xmlcursor.common.Common;


/**
 * Test for finner CData control feature.
 */
public class CDataTest
    extends TestCase
{
    static final String NL = SystemProperties.getProperty("line.separator")!=null ?
        SystemProperties.getProperty("line.separator") :
        (System.getProperty("line.separator") != null ? System.getProperty("line.separator") : "\n");

    public CDataTest(String name)
    {
        super(name);
    }

    public void testCData1()
            throws Exception
    {
        String xmlText = "<a><![CDATA[cdata text]]></a>";

        checkCData(xmlText, xmlText, xmlText);
    }

    public void testCData2()
            throws Exception
    {
        String xmlText = "<a>" + NL +
                "<b><![CDATA[cdata text]]> regular text</b>" + NL +
                "</a>";
        String expected1 = "<a>\n" +
                           "<b><![CDATA[cdata text regular text]]></b>\n" +
                           "</a>";
        String expected2 = "<a>" + NL +
                           "  <b><![CDATA[cdata text regular text]]></b>" + NL +
                           "</a>";

        checkCData(xmlText, expected1, expected2);
    }

    public void testCData3()
            throws Exception
    {
        String xmlText = "<a>\n" +
                "<c>text <![CDATA[cdata text]]></c>\n" +
                "</a>";
        String expected1 = "<a>\n" +
                           "<c>text cdata text</c>\n" +
                           "</a>";
        String expected2 = "<a>" + NL +
                           "  <c>text cdata text</c>" + NL +
                           "</a>";

        checkCData(xmlText, expected1, expected2);
    }

    private void checkCData(String xmlText, String expected1, String expected2)
            throws XmlException
    {
        System.out.println("\ninput:\n" + xmlText);

        XmlOptions opts = new XmlOptions();
        opts.setUseCDataBookmarks();

        XmlObject xo = XmlObject.Factory.parse( xmlText , opts);

        String result1 = xo.xmlText(opts);
        System.out.println("result xmlText:\n" + result1);
        assertEquals("xmlText", expected1, result1);

        opts.setSavePrettyPrint();
        String result2 = xo.xmlText(opts);
        System.out.println("result prettyPrint:\n" + result2);
        assertEquals("prettyPrint", expected2, result2);
    }
}

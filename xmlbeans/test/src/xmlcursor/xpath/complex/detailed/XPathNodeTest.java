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
package xmlcursor.xpath.complex.detailed;

import javax.xml.namespace.QName;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import junit.framework.TestCase;
import xmlcursor.common.Common;

/**
 *
 */
public class XPathNodeTest
    extends TestCase
{
    public void testNodeEquality()
        throws Exception
    {
        XmlCursor c = XmlObject.Factory.parse("<root>" +
            "<book isbn='012345' id='09876'/></root>")
            .newCursor();
        c.selectPath("//book[@isbn='012345'] is //book[@id='09876']");
        assertEquals(1, c.getSelectionCount());
        c.toNextSelection();
        assertEquals(Common.wrapInXmlFrag("true"), c.xmlText());
    }

    public void testNodeOrder()
        throws Exception
    {
        XmlCursor c = XmlObject.Factory.parse("<root>" +
            "<book isbn='012345'/><book id='09876'/></root>")
            .newCursor();
        c.selectPath("//book[@isbn='012345'] << //book[@id='09876']");
        assertEquals(1, c.getSelectionCount());
        c.toNextSelection();
        assertEquals(Common.wrapInXmlFrag("true"), c.xmlText());

        c.selectPath("//book[@isbn='012345'] >> //book[@id='09876']");
        assertEquals(1, c.getSelectionCount());
        c.toNextSelection();
        assertEquals(Common.wrapInXmlFrag("false"), c.xmlText());
    }

    public void testParent()
        throws Exception
    {
        String input = "<A><B><C></C></B></A>";
        XmlObject o;
        XmlCursor c = XmlObject.Factory.parse(input).newCursor();
        c.toFirstContentToken();
        c.toFirstChild();
        c.toFirstChild();
        o = c.getObject();
        assertEquals("<C/>", o.newCursor().xmlText());
        XmlObject[] res = o.selectPath("..");
        assertEquals(1, res.length);
        assertEquals("<B><C/></B>", res[0].newCursor().xmlText());
    }

    public void testParent1()
        throws Exception
    {
        String input = 
            "<AttributeCertificate " +
            "xmlns=\"http://www.eurecom.fr/security/xac#\" " +
            "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" +
            "<Content>" +
            "<Validity>" +
            "<ValidityFrom>2005-02-10T11:02:57.590+01:00</ValidityFrom>" +
            "<ValidityTo>2006-02-10T11:02:57.590+01:00</ValidityTo>" +
            "</Validity></Content></AttributeCertificate>";

        XmlObject o;
        XmlCursor c = XmlObject.Factory.parse(input).newCursor();
        c.toFirstContentToken();
        c.toFirstChild();
        c.toFirstChild();
        o = c.getObject();
        QName qn = o.newCursor().getName();
        assertEquals("http://www.eurecom.fr/security/xac#", qn.getNamespaceURI());
        assertEquals("Validity", qn.getLocalPart());
        XmlObject[] res = o.selectPath("..");
        assertEquals(1, res.length);
        qn = res[0].newCursor().getName();
        assertEquals("http://www.eurecom.fr/security/xac#", qn.getNamespaceURI());
        assertEquals("Content", qn.getLocalPart());
    }

}

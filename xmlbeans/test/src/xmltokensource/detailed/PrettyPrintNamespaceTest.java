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

package xmltokensource.detailed;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import com.mtest.SubInfo;
import com.mtest.TestDocument;
import junit.framework.TestCase;


public class PrettyPrintNamespaceTest extends TestCase {

    public PrettyPrintNamespaceTest(String name) {
        super(name);
    }

    public void testWithNewInstance()
            throws Exception {
        XmlObject x = XmlObject.Factory.newInstance();
        XmlCursor c = x.newCursor();

        c.toNextToken();
        c.beginElement("a", "aaaa");
        c.insertAttribute("a", "aaaa");
        c.insertNamespace("", "aaaa");

        String str =
                "<a aaaa:a=\"\" xmlns=\"aaaa\" xmlns:aaaa=\"aaaa\"/>";

        assertTrue("XmlText() Failed.", x.xmlText().equals(str));
        assertTrue("toString() Failed.", x.toString().trim().equals(str));
    }

    public void testWithInstanceFromSchema()
            throws Exception {
        String xml = "<mt:Test xmlns:mt=\"http://www.mtest.com\"> <mt:desc/> </mt:Test>";
        TestDocument doc = TestDocument.Factory.parse(xml);
        SubInfo subInfo = SubInfo.Factory.newInstance();
        subInfo.setDesc("hi");
        subInfo.setSubdesc("there");
        doc.setTest(subInfo);
        String lnSep = System.getProperty("line.separator");
        String str1 = "<mtes:Test xsi:type=\"mtes:SubInfo\" xmlns:mtes=\"http://www.mtest.com\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" + lnSep +
                "  <mtes:desc>hi</mtes:desc>" + lnSep +
                "  <mtes:subdesc>there</mtes:subdesc>" + lnSep +
                "</mtes:Test>";
        String str2 = "<mtes:Test xsi:type=\"mtes:SubInfo\" xmlns:mtes=\"http://www.mtest.com\" " +
                "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" +
                "<mtes:desc>hi</mtes:desc>" +
                "<mtes:subdesc>there</mtes:subdesc>" +
                "</mtes:Test>";

        assertTrue("XmlText() Failed.", doc.xmlText().equals(str2));
        assertTrue("toString() Failed.", doc.toString().trim().equals(str1));
    }
}

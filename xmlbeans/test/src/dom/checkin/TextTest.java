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


package dom.checkin;


import dom.common.CharacterDataTest;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import xmlcursor.common.Common;


/**
 *
 *
 */

public class TextTest extends CharacterDataTest {

    public TextTest(String s) {
        super(s);
        sXml = Common.XML_FOO_BAR_TEXT_EXT;
    }

    public static Test suite() {
        return new TestSuite(TextTest.class);
    }


    public void testNodeName() {
        assertEquals("#text", m_node.getNodeName());
    }

    public void testNodeType() {
        assertEquals(Node.TEXT_NODE, m_node.getNodeType());
    }


    public void testNodeValue() {
        assertEquals("extended", m_node.getNodeValue());
    }


    public void testNextSibling() {
        Node nxtSibling = m_node.getNextSibling();
        assertEquals(null, nxtSibling);
    }

    public void testPreviousSibling() {
        Node prSibling = m_node.getPreviousSibling();
        assertEquals("bar", prSibling.getLocalName());
        assertEquals("text", ((Text) prSibling.getFirstChild()).getData());
    }

    public void testParent() {
        Node parent = m_node.getParentNode();
        assertEquals(m_doc.getFirstChild(), parent);
        assertEquals("foo", parent.getLocalName());
    }


    public void testSplitTextNegative() {
        try {
            ((Text) m_node).splitText(-1);
            fail("Deleting OOB chars");
        }
        catch (DOMException de) {
            assertEquals(de.code, DOMException.INDEX_SIZE_ERR);
        }
    }

    public void testSplitTextLarge() {
        try {
            ((Text) m_node).splitText(((Text) m_node).getLength() + 1);
            fail("Deleting OOB chars");
        }
        catch (DOMException de) {
            assertEquals(de.code, DOMException.INDEX_SIZE_ERR);
        }
    }

    public void testSplitText() {
        Node parent = m_node.getParentNode();
        int nChildCount = parent.getChildNodes().getLength();
        ((Text) m_node).splitText(2);
        assertEquals(nChildCount + 1, parent.getChildNodes().getLength());
    }

    public void testSplitTextBorder() {
        Node parent = m_node.getParentNode();
        int nChildCount = parent.getChildNodes().getLength();
        ((Text) m_node).splitText(((Text) m_node).getLength());
        assertEquals(nChildCount + 1, parent.getChildNodes().getLength());
        ((Text) m_node).splitText(0);
        assertEquals(nChildCount + 2, parent.getChildNodes().getLength());
    }

    //code coverage case; not sure why it's important
    public void testSplitTextNoParent() {
        m_node = m_doc.createTextNode("foobar");
        ((Text) m_node).splitText(3);
        assertEquals("foo", m_node.getNodeValue());
    }

    public void testSetNodeValue() {
        m_node.setNodeValue("new text value");
        assertEquals("new text value", m_node.getNodeValue());
    }

    public void moveToNode() {
        m_node = m_doc.getFirstChild().getChildNodes().item(1);//"extended"
    }

    public void setUp() throws Exception {
        super.setUp();
        moveToNode();
    }

    public void testDelete()throws Exception{
        org.apache.xmlbeans.XmlObject o= org.apache.xmlbeans.XmlObject.Factory.parse("<foo/>");
        Node d = o.newDomNode();
        assertEquals("foo",d.getFirstChild().getLocalName());
    }
}

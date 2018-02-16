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

import dom.common.NodeWithChildrenTest;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.w3c.dom.Node;


/**
 *
 *  */

public class DocumentFragmentTest extends NodeWithChildrenTest {


    public DocumentFragmentTest(String s) {
        super(s);
        sXml =
                "<foo at0=\"val0\" a" +
            "t1=\"val1\" at2=\"val2\" at3=\"val3\" at4=\"val4\"><bar bat0=\"val0\"/></foo>";

        sXmlNS =
                "<foo xmlns:myns=\"uri:foo\" at0=\"val0\" myns:at0=\"val01\" at2=\"val2\" at3=\"val3\" at4=\"val4\"/>";
    }

    public static Test suite() {
        return new TestSuite(DocumentFragmentTest.class);
    }


    public void testNodeName() {
        assertEquals("#document-fragment", m_node.getNodeName());
    }

    public void testNodeType() {
        assertEquals(Node.DOCUMENT_FRAGMENT_NODE, m_node.getNodeType());
    }


    public void testNodeValue() {
        assertEquals(null, m_node.getNodeValue());
    }


    public void testNextSibling() {
        assertEquals(null, m_node.getNextSibling());
    }

    public void testPreviousSibling() {
        assertFalse(m_node == null);
        assertEquals(null, m_node.getPreviousSibling());
    }

    public void testParent() {
        assertEquals(null, m_node.getParentNode());
    }

    public void testGetChildNodes() {
        assertEquals(1, m_node.getChildNodes().getLength());
    }

    public void testFirstChild() {
        assertEquals("foo", m_node.getFirstChild().getNodeName());
    }


    public void testLastChild() {
        assertEquals("foo", m_node.getLastChild().getNodeName());
    }

    public void testInsertExisitingNode() {
        Node child = m_doc.getFirstChild().getFirstChild();//some text
        if (child == m_node)
            child = m_doc.getLastChild();
        super.testInsertExistingNode(child);
    }

    public void testAppendChildExisting() {
        Node child = m_doc.getFirstChild().getFirstChild();//some text
        if (child == m_node)
            child = m_doc.getLastChild();
        //if still the same, SOL
        super.testAppendChildExisting(child);
    }
        
    public void moveToNode() {
        m_node = m_doc.createDocumentFragment();
        m_node.appendChild(m_doc.createElement("foo"));

    }

    public void setUp() throws Exception {
        super.setUp();
        moveToNode();
    }


}

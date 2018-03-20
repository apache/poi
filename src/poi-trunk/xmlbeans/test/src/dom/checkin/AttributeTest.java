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


import dom.common.DomUtils;
import dom.common.NodeWithChildrenTest;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.w3c.dom.*;


/**
 *
 *
 */

public class AttributeTest extends NodeWithChildrenTest {

    public AttributeTest(String s) {
        super(s);
        String sDTD = "<?xml version=\"1.0\"?>" +
                "<!DOCTYPE foodoc [" +
                "<!ELEMENT foo>" +
                "<!ATTLIST foo at_spec CDATA \"0\">" +
                "]>";

        sXml =
                "<foo xmlns:extra=\"bea.org\" xmlns:myns=\"uri:foo\" at0=\"val0\" myns:at0=\"val01\" at2=\"val2\" at3=\"val3\" at4=\"val4\">some text</foo>";
        if (bDTD)
            sXml = sDTD + sXml;
        sXmlNS =
                "<foo xmlns:myns=\"uri:foo\" at0=\"val0\" myns:at0=\"val01\" at2=\"val2\" at3=\"val3\" at4=\"val4\"/>";
    }

    public static Test suite() {
        return new TestSuite(AttributeTest.class);
    }


    public void testNodeName() {

        String sExpected = "myns:at0";
        assertEquals(sExpected, m_node.getNodeName());
    }

    public void testGetName() {

        String sExpected = "myns:at0";
        assertEquals(sExpected, ((Attr) m_node).getName());
    }

    public void testNodeType() {
        assertEquals(Node.ATTRIBUTE_NODE, m_node.getNodeType());
    }


    public void testNodeValue() {
        assertEquals("val01", m_node.getNodeValue());
    }


    //following are null here

    public void testNextSibling() {
        assertEquals(null, m_node.getNextSibling());
    }

    public void testPreviousSibling() {
        assertEquals(null, m_node.getPreviousSibling());
    }

    public void testParent() {
        assertEquals(null, m_node.getParentNode());
    }


    public void testPrefix() {
        assertEquals("myns", m_node.getPrefix());
    }

    public void testNamespaceUri() {
        assertEquals("uri:foo", m_node.getNamespaceURI());
    }

    public void testLocalName() {
        assertEquals("at0", m_node.getLocalName());
    }


    public void testAppendChild() {
        //elt
        Node newChild = m_doc.createElement("foo");
        try {
            m_node.appendChild(newChild);
            fail(
                    "Cannot append an element children to attributes " +
                    m_node.getChildNodes().getLength());
        }
        catch (DOMException de) {
            assertEquals(DOMException.HIERARCHY_REQUEST_ERR, de.code);
        }

        newChild = m_doc.createTextNode("foobar");
        m_node.appendChild(newChild);
        assertEquals(2, m_node.getChildNodes().getLength());

//TODO
        m_node.normalize();
        assertEquals(1, m_node.getChildNodes().getLength());
        assertEquals("val01foobar", ((Text) m_node.getFirstChild()).getData());

    }

    public void testCloneNode() {
        Attr cloned,
                cloned1;
        cloned = (Attr) m_node.cloneNode(true);


        assertEquals(true, DomUtils.compareNodesDeep(m_node, cloned));
        assertEquals(false, m_node == cloned);


//TODO
        cloned1 = (Attr) m_node.cloneNode(false);
        //    assertEquals(m_node.getChildNodes(), );
        assertEquals(true, DomUtils.compareNodesShallow(m_node, cloned));

        if (bDTD) {
            assertEquals(false, cloned.getSpecified());
            assertEquals(false, ((Attr) m_node).getSpecified());
            m_node = m_doc.getAttributes().getNamedItem("at_spec");
            cloned = (Attr) m_node.cloneNode(true);
            cloned1 = (Attr) m_node.cloneNode(false);
            assertEquals(cloned, cloned1);
            assertEquals(true, m_node.equals(cloned));
            assertEquals(false, m_node == cloned);
            assertEquals(true, cloned.getSpecified());
            assertEquals(false, ((Attr) m_node).getSpecified());
        }
    }

    /**
     * public void testXercesClone()throws Exception{
     * org.apache.xerces.parsers.DOMParser parser = new org.apache.xerces.parsers.DOMParser();
     * parser.parse(new InputSource(new StringReader(sXml)));
     * Document xercesDocument = parser.getDocument();
     * assertFalse (xercesDocument==null);
     * <p/>
     * Node test = m_doc.getFirstChild();
     * assertTrue(test.hasChildNodes());
     * test = ((Element) test).getAttributeNodeNS("uri:foo", "at0");
     * Node clone=test.cloneNode(false);
     * assertTrue(clone.hasChildNodes());
     * <p/>
     * }
     */

    public void testGetChildNodes() {
        assertEquals(1, m_node.getChildNodes().getLength());
    }

    public void testFirstChild() {
        assertEquals("val01", ((Text) m_node.getFirstChild()).getData());
    }

    public void testLastChild() {
        assertEquals("val01", ((Text) m_node.getLastChild()).getData());
    }

    public void testInsertBefore() {
        Node newChild = m_doc.createElement("foo");
        assertEquals(1, m_node.getChildNodes().getLength());

        Node textNode = m_node.getFirstChild();

        try {
            m_node.insertBefore(newChild, textNode);
        }
        catch (DOMException de) {
            assertEquals(DOMException.HIERARCHY_REQUEST_ERR, de.code);
        }

        newChild = m_doc.createTextNode("foo");
        m_node.insertBefore(newChild, textNode);
        assertEquals("foo", m_node.getFirstChild().getNodeValue());
        assertEquals("val01", m_node.getLastChild().getNodeValue());
    }


    public void testRemoveChild() {
        //attr w/o a value
        Element owner = (Element) ((Attr) m_node).getOwnerElement();
        m_node.removeChild(m_node.getFirstChild());
        assertEquals("", ((Attr) m_node).getValue());
        //  assertEquals(false,owner.hasAttributeNS("uri:foo","at0"));
    }

    public void testReplaceChild() {

        //assertFalse(m_node.hasChildNodes());
        Node newChild = m_doc.createElement("foo");
        assertEquals(1, m_node.getChildNodes().getLength());
        try {
            m_node.replaceChild(newChild, m_node.getFirstChild());
            fail("can not put an element under an attr");
        }
        catch (DOMException de) {
            assertEquals(DOMException.HIERARCHY_REQUEST_ERR, de.code);
        }
        newChild = m_doc.createTextNode("realnewval");
        assertEquals(1, m_node.getChildNodes().getLength());
        m_node.replaceChild(newChild, m_node.getFirstChild());
        if (!"realnewval".equals(((Attr) m_node).getValue()))
            fail(" Expected realnewval but got " + ((Attr) m_node).getValue());

    }


    public void testGetOwnerElement() {
        assertEquals("foo", ((Attr) m_node).getOwnerElement().getLocalName());
        Node newNode = m_doc.createAttributeNS("foo1:org", "name");
        assertEquals(null, ((Attr) newNode).getOwnerElement());
        newNode = m_doc.createAttribute("name");
        assertEquals(null, ((Attr) newNode).getOwnerElement());
    }

    /*Not implem
    //TODO
    public void testGetSpecified(){
	if (bDTD){
	    Attr at_true=(Attr)m_doc.getAttributes().getNamedItem("at_spec");
	    assertEquals(false,at_true.getSpecified());
	}
	assertEquals(true,((Attr)m_node).getSpecified());
    }
    */
    public void testSetValue() {
        String newVal = "new<spec\u042Fchar";
        ((Attr) m_node).setValue(newVal);
        assertEquals(newVal, ((Attr) m_node).getValue());
    }

    public void testSetValueNull() {
        ((Attr) m_node).setValue("foo");
        String newVal = "";
        ((Attr) m_node).setValue(newVal);
        assertEquals(true, ((Attr) m_node).hasChildNodes());

        newVal = null;
        ((Attr) m_node).setValue(newVal);
        assertEquals(true, ((Attr) m_node).hasChildNodes());
    }


    public void testGetValue() {
        assertEquals("val01", ((Attr) m_node).getValue());
    }


    public void testInsertBeforeDocFrag() {
        DocumentFragment child = m_doc.createDocumentFragment();
        child.appendChild(m_doc.createTextNode("foo1"));
        Node target = m_node.getFirstChild();
        super.testInsertBefore(child, target);
    }

    public void testAppendChildDocFrag() {
        DocumentFragment child = m_doc.createDocumentFragment();
        child.appendChild(m_doc.createTextNode("foo"));
        super.testAppendChild(child);

    }

    public void testReplaceChildDocFrag() {

        DocumentFragment child = m_doc.createDocumentFragment();
        child.appendChild(m_doc.createElement("foo"));
        child.appendChild(m_doc.createElement("foobar"));
        Node toReplace = m_node.getFirstChild();
        try {
            super.testReplaceChild(child, toReplace);
            fail("cannot insert element in attr");
        }
        catch (DOMException de) {
            assertEquals(DOMException.HIERARCHY_REQUEST_ERR, de.code);
        }

    }

    public void testInsertBeforeNullTarget() {
        Node child = m_doc.createElementNS("org.foo.www", "foonode");
        try {
            super.testInsertBefore(child, null);
            fail("cannot insert element in attr");
        }
        catch (DOMException de) {
            assertEquals(DOMException.HIERARCHY_REQUEST_ERR, de.code);
        }
        child = m_doc.createTextNode("foonode");
        super.testInsertBefore(child, null);
    }

    public void testInsertExistingNode() {
        Node toInsert=m_doc.getFirstChild();
        //elt under attr
        try {
            super.testInsertExistingNode(toInsert);
            fail("Shouldn't work for attrs");
        }
        catch (DOMException de) {
            assertEquals(DOMException.HIERARCHY_REQUEST_ERR, de.code);
        }
        toInsert = m_doc.getFirstChild().getFirstChild(); //some text

      super.testInsertBefore(toInsert, m_node.getFirstChild());
        assertEquals("some text", m_node.getFirstChild().getNodeValue());
        assertEquals(2, m_node.getChildNodes().getLength());
    }

    public void testSetNodeValue() {
        m_node.appendChild(m_doc.createTextNode("bar"));//attr w/ two values
        int nCount = m_node.getChildNodes().getLength();
        m_node.setNodeValue("blah");
        //assertEquals(1,m_node.getChildNodes().getLength());
        assertEquals("blah", m_node.getFirstChild().getNodeValue());
    }

    public void testAppendChildExisting() {
        Node child = m_doc.getFirstChild().getFirstChild();//some text
        if (child == m_node)
            child = m_doc.getLastChild();
        //if still the same, SOL
        super.testAppendChild(child);
    }


    public void testSetPrefix() {
        String newPrefix = "yana"; //should clear it
        m_node.setPrefix(newPrefix);
        assertEquals("yana:at0", m_node.getNodeName());
        newPrefix = "extra";
        m_node.setPrefix(newPrefix);
        assertEquals("uri:foo", m_node.getNamespaceURI());//URI never changes

        newPrefix = null;
        m_node.setPrefix(newPrefix);
        assertEquals("", m_node.getPrefix());

    }


    public void testInsertBeforeInvalidRefNode() {
        Node child = m_doc.createTextNode("foonode");
        Node target = m_doc.createElement("foo");
        try {
            super.testInsertBefore(child, target);
            fail("Insert cannot happen");
        }
        catch (DOMException de) {
            System.err.println(de.getMessage() + " " + de.code);
            assertEquals(DOMException.NOT_FOUND_ERR, de.code);
        }
    }

    public void testDomLevel1() {
        Attr at = m_doc.createAttribute("foobar");
        assertNull("L1 prefix null", at.getPrefix());
        assertNull("L1 LocalName null", at.getLocalName());
        assertNull("L1 Uri null", at.getNamespaceURI());
        try
        {
            at.setPrefix("foo");
            fail("L1 prefix null");
        }
        catch (DOMException de)
        {
            assertEquals(DOMException.NAMESPACE_ERR, de.code);
        }
    }

    public void moveToNode() {
        m_node = m_doc.getFirstChild();
        m_node = ((Element) m_node).getAttributeNodeNS("uri:foo", "at0");
        assertEquals("val01", m_node.getNodeValue());
        assertTrue(m_node instanceof Attr);


    }

    public void setUp() throws Exception {
        super.setUp();
        moveToNode();
    }

}







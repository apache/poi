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
import org.w3c.dom.*;
import xmlcursor.common.Common;


/**
 *
 *
 */

public class ElementTest extends NodeWithChildrenTest {


    public ElementTest(String s) {
        super(s);
        String sDTD = "<?xml version=\"1.0\"?>" +
                "<!DOCTYPE bardoc [" +
                "<!ELEMENT bar>" +
                "<!ELEMENT foo>" +
                "<!ATTLIST bar at_spec CDATA \"0\">" +
                "]>";
        sXmlNS = "<bar xmlns:other=\"uri:other\" xmlns:myns=\"uri:foo\">" +
                "<foo  myns:at0=\"val01\" myns:at2=\"at2\" at2=\"val2\" myns:at3=\"val3\" at4=\"val4\">" +
                "txt0<foo>nestedfoo</foo><myns:yana/>" +
                "</foo>" +
                "<myns:foo>nstext<ZeD/></myns:foo>" +
                "</bar>";
        if (bDTD)
            sXmlNS = sDTD + sXmlNS;
        sXml = Common.XML_FOO_BAR_NESTED_SIBLINGS;
    }

    public static Test suite() {
        return new TestSuite(ElementTest.class);
    }


    public void testNodeName() {
        assertEquals("zed", m_node.getNodeName());
    }

    public void testNodeType() {
        assertEquals(Node.ELEMENT_NODE, m_node.getNodeType());
    }


    public void testNodeValue() {
        assertEquals(null, m_node.getNodeValue());
    }


    public void testNextSibling() {
        assertEquals(null, m_node.getNextSibling());
    }

    public void testPreviousSibling() {
        Node prSib = m_node.getPreviousSibling();
        assertEquals("text0", prSib.getNodeValue());
    }

    public void testParent() {
        Node parent = m_node.getParentNode();
        assertEquals("bar", parent.getLocalName());
        assertEquals(m_doc.getFirstChild().getFirstChild(), parent);
    }

    public void testPrefix() {
        assertEquals("", m_node.getPrefix());

        m_node = m_docNS.getDocumentElement().getChildNodes().item(1);
        assertEquals("myns:foo", m_node.getNodeName());
        assertEquals("myns", m_node.getPrefix());
    }

    public void testNamespaceUri() {
        assertEquals("",m_node.getNamespaceURI());
    }
    
    public void testCloneNode() {
        super.testCloneNode();
    }

    /**
     * Clone node with atts
     */
    public void testCloneNodeAttrs() {
        Node toClone = m_docNS.getFirstChild(); //the foo elt
        /* Node clone1=toClone.cloneNode(false);

         NamedNodeMap attrSet1=toClone.getAttributes();
         assertEquals(true,DomUtils.compareNamedNodeMaps(attrSet1,clone1.getAttributes()));
        */
        Node clone2 = toClone.cloneNode(true);
    }

    public void testHasAttributes() {
        super.testHasAttributes();
        m_node = m_doc.getFirstChild();
        assertEquals(true, ((Element) m_node).hasAttributes());
    }

    public void testGetAttribute() {
        m_node = m_docNS.getFirstChild();
        if (bDTD)
            assertEquals("0", ((Element) m_node).getAttribute("at_spec"));
        assertEquals("val2",
                ((Element) m_node.getFirstChild()).getAttribute("at2"));
    }

    public void testGetAttributeDNE() {
        m_node = m_docNS.getFirstChild();
        assertEquals("", ((Element) m_node).getAttribute("at3"));
        assertEquals("", ((Element) m_node).getAttribute("foobar"));
        String sNull = null;
        assertEquals("", ((Element) m_node).getAttribute(sNull));
    }

    public void testGetAttributeNode() {
        m_node = m_docNS.getFirstChild();
        assertEquals("bar", ((Element) m_node).getTagName());
        //assertEquals("uri:foo",((Attr)((Element)m_node).getAttributeNodeNS("xmlns","myns")).getNodeValue());
        m_node = m_node.getFirstChild();
        assertEquals("val2",
                ((Attr) ((Element) m_node).getAttributeNode("at2")).getNodeValue());
        if (bDTD)
            assertEquals("0",
                    ((Attr) ((Element) m_node).getAttributeNode("at_spec")).getNodeValue());
    }

    public void testGetAttributeNodeDNE() {
        m_node = m_docNS.getFirstChild();
        assertEquals(null, ((Element) m_node).getAttributeNode("at3"));
        assertEquals(null, ((Element) m_node).getAttributeNode("foobar"));
        String sNull = null;
        assertEquals("", ((Element) m_node).getAttribute(sNull));
    }

    public void getAttributeNodeNS() {
        m_node = m_docNS.getFirstChild();
        assertEquals("0",
                ((Attr) ((Element) m_node).getAttributeNodeNS("", "at_spec")).getNodeValue());
        assertEquals("val01",
                ((Attr) ((Element) m_node).getAttributeNodeNS("uri:foo", "at0")).getNodeValue());
        assertEquals("val0",
                ((Attr) ((Element) m_node).getAttributeNodeNS(null, "at0")).getNodeValue());
        assertEquals("val3",
                ((Attr) ((Element) m_node).getAttributeNodeNS("uri:foo", "at3")).getNodeValue());
    }

    public void testGetAttributeNodeNS_DNE() {
        m_node = m_docNS.getFirstChild();
        assertEquals(null, ((Element) m_node).getAttributeNodeNS("", "at3"));
        assertEquals(null,
                ((Element) m_node).getAttributeNodeNS("uri:foo", "at1"));
        String sNull = null;
        assertEquals(null,
                ((Element) m_node).getAttributeNodeNS("uri:foo", sNull));
    }

    public void testGetAttributeNS() {
        m_node = m_docNS.getFirstChild().getFirstChild();
        if (bDTD)
            assertEquals("0",
                    ((Element) m_node).getAttributeNS(null, "at_spec"));
        assertEquals("val01",
                ((Element) m_node).getAttributeNS("uri:foo", "at0"));
        assertEquals("val2", ((Element) m_node).getAttributeNS("", "at2"));
    }

    public void testGetAttributeNS_DNE() {
        m_node = m_docNS.getFirstChild();
        assertEquals("", ((Element) m_node).getAttributeNS("", "at3"));
        assertEquals("", ((Element) m_node).getAttributeNS("uri:foo", "at1"));
        String sNull = null;
        assertEquals("", ((Element) m_node).getAttributeNS("uri:foo", sNull));
    }

    public void testGetElementsByTagName() {
        //move node @ foo
        m_node = m_node.getParentNode().getParentNode();
        NodeList result = ((Element) m_node).getElementsByTagName("*");
        int nEltCount = 5;//num elts in the XML
        assertEquals(nEltCount - 1, result.getLength());

        result = ((Element) m_node).getElementsByTagName("zed");
        assertEquals(2, result.getLength());
        assertEquals("nested0", result.item(0).getFirstChild().getNodeValue());
        assertEquals("nested1", result.item(1).getFirstChild().getNodeValue());
    }

    public void testGetElementsByTagNameDNE() {
        NodeList result = ((Element) m_node.getParentNode()).getElementsByTagName(
                "foobar");
        assertEquals(0, result.getLength());
    }

    //elts need to come out in preorder order
    public void testGetElementsByTagNamePreorder() {
        m_node = m_docNS.getFirstChild();
        NodeList result = ((Element) m_node).getElementsByTagName("foo");
        assertEquals(2, result.getLength());
        assertEquals("txt0", result.item(0).getFirstChild().getNodeValue());
        assertEquals("nestedfoo",
                result.item(1).getFirstChild().getNodeValue());
    }

    public void testGetElementsByTagNameDescendant() {
        m_node = m_docNS.getFirstChild().getFirstChild();
        NodeList result = ((Element) m_node).getElementsByTagName("foo");//self should not be selected
        assertEquals(1, result.getLength());
        assertEquals("nestedfoo",
                result.item(0).getFirstChild().getNodeValue());
    }


    public void testGetElementsByTagNameNS() {
        m_node = m_docNS.getFirstChild();
        NodeList result = ((Element) m_node).getElementsByTagNameNS("*", "*");
        int nEltCount = 6;
        assertEquals(nEltCount - 1, result.getLength());

        result = ((Element) m_node).getElementsByTagNameNS("*", "foo");
        nEltCount = 3;
        assertEquals(nEltCount, result.getLength());
        assertEquals("txt0", result.item(0).getFirstChild().getNodeValue());
        assertEquals("nestedfoo",
                result.item(1).getFirstChild().getNodeValue());
        assertEquals("nstext", result.item(2).getFirstChild().getNodeValue());


        result = ((Element) m_node).getElementsByTagNameNS("uri:foo", "foo");
        assertEquals(1, result.getLength());
        assertEquals("nstext", result.item(0).getFirstChild().getNodeValue());

        result = ((Element) m_node).getElementsByTagNameNS(null, "foo");
        assertEquals("txt0", result.item(0).getFirstChild().getNodeValue());
        assertEquals("nestedfoo",
                result.item(1).getFirstChild().getNodeValue());
        NodeList result1 = ((Element) m_node).getElementsByTagNameNS("", "foo");
        assertEquals(true, compareNodeList(result, result1));


        result = ((Element) m_node).getElementsByTagNameNS(null, "*");
        assertEquals(3, result.getLength());
        assertEquals("ZeD", ((Element) result.item(2)).getTagName());
    }

    public void testGetElementsByTagNameNS_DNE() {
        m_node = m_docNS.getFirstChild();
        NodeList result = ((Element) m_node).getElementsByTagNameNS("uri:foo",
                "zed");
        assertEquals(0, result.getLength());

        result =
                ((Element) m_node).getElementsByTagNameNS("foo:uri_DNE", "foo");
        assertEquals(0, result.getLength());

    }

    public void testGetTagName() {
        m_node =
                m_docNS.getFirstChild().getChildNodes().item(1).getChildNodes()
                .item(1);
        assertEquals("ZeD", ((Element) m_node).getTagName());

    }


    public void testHasAttribute() {
        m_node = m_docNS.getFirstChild();
        if (bDTD)
            assertEquals(true, ((Element) m_node).hasAttribute("at_spec"));

        m_node = m_docNS.getFirstChild();
        assertEquals(false, ((Element) m_node).hasAttribute("at3"));
        assertEquals(false, ((Element) m_node).hasAttribute("at0"));
    }

    public void testHasAttributeNS() {
        m_node = m_docNS.getFirstChild();
        if (bDTD)
            assertEquals(true,
                    ((Element) m_node).hasAttributeNS(null, "at_spec"));

        m_node = m_node.getFirstChild();
        assertEquals(true, ((Element) m_node).hasAttributeNS("uri:foo", "at3"));
        assertEquals(false,
                ((Element) m_node).hasAttributeNS("uri:foo:org", "at0"));
        assertEquals(false, ((Element) m_node).hasAttributeNS("uri:foo", null));
    }

    public void testRemoveAttribute() {
        m_node = m_docNS.getFirstChild();
        //remove default
        if (bDTD) {

            ((Element) m_node).removeAttribute("at_spec");
            assertEquals(1, m_node.getAttributes().getLength());
        }

        m_node = m_node.getFirstChild();
        assertEquals("foo", m_node.getNodeName());
        assertEquals(5, m_node.getAttributes().getLength());
        ((Element) m_node).removeAttribute("at2");
        assertEquals(4, m_node.getAttributes().getLength());

        //DNE
        ((Element) m_node).removeAttribute("at3");
        assertEquals(4, m_node.getAttributes().getLength());

    }

    public void testRemoveAttributeNode() {
        Node removed;
        //remove default
        m_node = m_docNS.getFirstChild();
        if (bDTD) {
            ((Element) m_node).removeAttributeNode(
                    ((Element) m_node).getAttributeNode("at_spec"));
            assertEquals(1, m_node.getAttributes().getLength());
        }
        m_node = m_node.getFirstChild();
        assertEquals("foo", m_node.getNodeName());
        assertEquals(5, m_node.getAttributes().getLength());
        Attr remove = ((Element) m_node).getAttributeNode("at2");
        removed = ((Element) m_node).removeAttributeNode(remove);
        assertFalse(removed == null);
        assertEquals(4, m_node.getAttributes().getLength());
        assertEquals(removed, remove);
    }

    public void testRemoveAttributeNode_DNE() {
        //DNE
        Node removed;
        Attr remove = ((Element) m_node).getAttributeNode("at3");
        try {
            removed = ((Element) m_node).removeAttributeNode(remove);
            fail("removing Non existing attr");
        }
        catch (DOMException de) {
            assertEquals(DOMException.NOT_FOUND_ERR, de.code);
        }

        remove = null;
        try {
            removed = ((Element) m_node).removeAttributeNode(remove);
            fail("removing Non existing attr");
        }
        catch (DOMException de) {
            assertEquals(DOMException.NOT_FOUND_ERR, de.code);
        }

        //differentParent
        remove = m_doc.getDocumentElement().getAttributeNode("attr0");
        try {
            removed = ((Element) m_node).removeAttributeNode(remove);
            fail("removing Non existing attr");
        }
        catch (DOMException de) {
            assertEquals(DOMException.NOT_FOUND_ERR, de.code);
        }
    }

    public void testRemoveAttributeNS() {
        //remove default
        m_node = m_docNS.getFirstChild();
        if (bDTD) {
            ((Element) m_node).removeAttributeNS(null, "at_spec");
            assertEquals(1, m_node.getAttributes().getLength());
        }
        m_node = ((Element) m_node).getFirstChild();
        ((Element) m_node).removeAttributeNS("uri:foo", "at0");
        assertEquals(4, m_node.getAttributes().getLength());

        //DNE
        ((Element) m_node).removeAttributeNS(null, "at3");
        assertEquals(4, m_node.getAttributes().getLength());

        ((Element) m_node).removeAttributeNS("uri:foo", null);
        assertEquals(4, m_node.getAttributes().getLength());
    }


    public void testSetAttribute() {
        m_node = m_doc.getDocumentElement();

        try {
            ((Element) m_node).setAttribute("invalid<", "0");
            fail("Invalid attr name");
        }
        catch (DOMException de) {
            assertEquals(DOMException.INVALID_CHARACTER_ERR, de.code);
        }

        ((Element) m_node).setAttribute("attr0", "newval");
        assertEquals("newval", ((Element) m_node).getAttribute("attr0"));


        ((Element) m_node).setAttribute("attr1", "newval");
        assertEquals("newval", ((Element) m_node).getAttribute("attr1"));
        assertEquals(2, m_node.getAttributes().getLength());
    }

    public void testSetAttributeNode() {
        Attr result;
        Attr newAttr = m_doc.createAttribute("attr0");
        Attr oldAttr = ((Element) m_node).getAttributeNode("attr0");
        newAttr.setValue("newval");
        result = ((Element) m_node).setAttributeNode(newAttr);
        assertEquals(oldAttr, result);
        assertEquals("newval",
                ((Element) m_node).getAttributeNode("attr0").getNodeValue());

        //insert self
        try {
            Attr at0 = ((Element) m_node).getAttributeNode("attr0");
            String v1 = at0.getNodeValue();
            ((Element) m_node).setAttributeNode(at0);
            assertEquals(v1, ((Element) m_node).getAttribute("attr0"));
        }
        catch (DOMException de) {
            assertEquals(de.code, DOMException.INUSE_ATTRIBUTE_ERR);
        }

        //insert new
        newAttr = m_doc.createAttribute("attr1");
        newAttr.setValue("newval");
        result = ((Element) m_node).setAttributeNode(newAttr);
        assertEquals(null, result);
        assertEquals("newval",
                ((Element) m_node).getAttributeNode("attr1").getNodeValue());
        assertEquals(2, m_node.getAttributes().getLength());
    }

    public void testSetAttributeNodeDiffDoc() {
        Attr result;
        Attr newAttr = m_docNS.createAttribute("attr0");
        try {
            result = ((Element) m_node).setAttributeNode(newAttr);
            fail("Attr Node diff doc in use");
        }
        catch (DOMException de) {
            assertEquals(DOMException.WRONG_DOCUMENT_ERR, de.code);
        }
    }

    public void testSetAttributeNodeInUse() {
        //insert new
        m_node = m_node.getParentNode().getParentNode();
        Attr newAttr = ((Element) m_node).getAttributeNode("attr0");
        m_node = m_node.getFirstChild();
        try {
            ((Element) m_node).setAttributeNode(newAttr);
            fail("Attr Node in use");
        }
        catch (DOMException de) {
            assertEquals(DOMException.INUSE_ATTRIBUTE_ERR, de.code);
        }
    }

    public void testSetAttributeNodeNS() {
        m_node = m_docNS.getFirstChild().getFirstChild();
        Attr result;
        Attr newAttr = m_docNS.createAttributeNS("uri:foo", "at0");
        Attr oldAttr = ((Element) m_node).getAttributeNodeNS("uri:foo", "at0");
        assertFalse(oldAttr == null);
        newAttr.setValue("newval");
        result = ((Element) m_node).setAttributeNodeNS(newAttr);
        assertEquals(oldAttr, result);
        Attr insertedAtt = ((Element) m_node).getAttributeNodeNS("uri:foo",
                "at0");
        assertFalse(insertedAtt == null);
        assertEquals("newval", insertedAtt.getNodeValue());

        //insert new
        int nAttrCnt = m_node.getAttributes().getLength();
        newAttr = m_docNS.createAttributeNS("uri:foo", "attr1");
        newAttr.setValue("newval");
        result = ((Element) m_node).setAttributeNode(newAttr);
        assertEquals(null, result);
        assertEquals("newval",
                ((Element) m_node).getAttributeNS("uri:foo", "attr1"));
        assertEquals(nAttrCnt + 1, m_node.getAttributes().getLength());

        //insert new
        newAttr = m_docNS.createAttributeNS("uri:foo:org", "attr1");
        newAttr.setValue("newURIval");
        result = ((Element) m_node).setAttributeNodeNS(newAttr);

        assertEquals(null, result);
        assertEquals("newURIval",
                ((Element) m_node).getAttributeNS("uri:foo:org", "attr1"));
        assertEquals(nAttrCnt + 2, m_node.getAttributes().getLength());

    }

    public void testSetAttributeNS() {
        m_node = m_docNS.getFirstChild().getFirstChild();
        //overwrite
        ((Element) m_node).setAttributeNS("uri:foo", "at0", "newval");
        assertEquals("newval",
                ((Element) m_node).getAttributeNS("uri:foo", "at0"));


        ((Element) m_node).setAttributeNS("uri:foo:org", "attr1", "newval");
        assertEquals("newval",
                ((Element) m_node).getAttributeNS("uri:foo:org", "attr1"));
        assertEquals(6, m_node.getAttributes().getLength());

    }

    public void testSetAttributeNSBadNS() {
        //qualifiedName is malformed
        try {
            ((Element) m_node).setAttributeNS("foo:org", "invalid<", "0");
            fail("Invalid attr name");
        }
        catch (DOMException de) {
            assertEquals(DOMException.INVALID_CHARACTER_ERR, de.code);
        }

        //the qualifiedName has a prefix and the namespaceURI is null
        try {
            String sNull = null;
            ((Element) m_node).setAttributeNS(sNull, "myfoo:at", "0");
            fail("Invalid attr name");
        }
        catch (DOMException de) {
            assertEquals(DOMException.NAMESPACE_ERR, de.code);
        }
    }

    public void testSetAttributeNSBadNS_xmlns() {
        //the qualifiedName, or its prefix, is "xmlns" and the namespaceURI is different from " http://www.w3.org/2000/xmlns/".
        try {
            ((Element) m_node).setAttributeNS("foo:org:uri", "xmlns", "0");
            fail("Invalid attr name");
        }
        catch (DOMException de) {
            assertEquals(DOMException.NAMESPACE_ERR, de.code);
        }

        try {
            ((Element) m_node).setAttributeNS("foo:org:uri", "xmlns:foo", "0");
            fail("Invalid attr name");
        }
        catch (DOMException de) {
            assertEquals(DOMException.NAMESPACE_ERR, de.code);
        }
    }

    public void testSetAttributeNSBadNS_xml() {
        //if the qualifiedName has a prefix that is "xml"
        // and the namespaceURI is different from " http://www.w3.org/XML/1998/namespace"
        try {
            ((Element) m_node).setAttributeNS("foo:org:uri", "xml:foo", "0");
            fail("Invalid attr name");
        }
        catch (DOMException de) {
            assertEquals(DOMException.NAMESPACE_ERR, de.code);
        }
    }

    public void testGetChildNodes() {
        m_node = m_node.getParentNode();
        assertEquals(2, m_node.getChildNodes().getLength());
    }

    public void testFirstChild() {
        assertEquals("nested0", m_node.getFirstChild().getNodeValue());
    }

    public void testLastChild() {
        assertEquals("nested0", m_node.getLastChild().getNodeValue());
    }

    //code coverage: need a node with penultimate elt and last text
    public void testLastChildMixedContent() {
        Node prevSibling = m_doc.createElement("penultimateNode");
        m_node.insertBefore(prevSibling, m_node.getFirstChild());
        assertEquals("nested0", m_node.getLastChild().getNodeValue());
    }


    public void testGetAttributes() {
        assertEquals(0, m_node.getAttributes().getLength());
    }

    public void testLocalName() {
        assertEquals("zed", m_node.getLocalName());
    }

    public void testSetPrefix() {
        //set a null prefix
        m_node =
                m_docNS.getFirstChild().getFirstChild().getChildNodes().item(2);//<myns:yana/>
        assertFalse(m_node == null);
        m_node.setPrefix(null);
        assertEquals("", m_node.getPrefix());

        m_node.setPrefix("other");

        assertEquals("other:yana", m_node.getNodeName());
        assertEquals("other:yana", ((Element) m_node).getTagName());
        // assertEquals("uri:other",m_node.getNamespaceURI());--this is the URI @ creation--never changes
        assertEquals(1,
                ((Element) m_docNS.getDocumentElement()).getElementsByTagName(
                        "other:yana")
                .getLength());


    }

    public void testNormalizeNode() throws Exception {
        m_node = m_node.getParentNode();
        m_node.replaceChild(m_doc.createTextNode("txt1"),
                m_node.getLastChild());
        assertEquals(2, m_node.getChildNodes().getLength());

        m_node.normalize();
        assertEquals(1, m_node.getChildNodes().getLength());


    }

    public void testNormalizeNodeNoChildren() throws Exception {
        m_node = m_doc.createElement("foobar");
        assertEquals(0, m_node.getChildNodes().getLength());
        m_node.normalize();
        assertEquals(0, m_node.getChildNodes().getLength());
    }

    public void testNormalizeNodeOneChild() throws Exception {
        m_node = m_doc.createElement("foobar");
        m_node.appendChild(m_doc.createElement("foobar"));
        assertEquals(1, m_node.getChildNodes().getLength());
        m_node.normalize();
        assertEquals(1, m_node.getChildNodes().getLength());
    }

    public void testAppendChildExisting() {
        m_node = m_docNS.getFirstChild().getLastChild();
        Node child = m_docNS.getFirstChild().getFirstChild();
        super.testAppendChildExisting(child);
    }

    public void testInsertExisitingNode() {
        m_node = m_docNS.getFirstChild().getLastChild();
        Node child = m_docNS.getFirstChild().getFirstChild();
        super.testAppendChildExisting(child);

    }


    public void testDomLevel1() {
           Element elt = m_doc.createElement("foobar");
           assertNull("L1 prefix null", elt.getPrefix());
           assertNull("L1 LocalName null", elt.getLocalName());
           assertNull("L1 Uri null", elt.getNamespaceURI());
            try
            {
                elt.setPrefix("foo");
                fail("L1 prefix null");
            }
            catch (DOMException de)
            {
                assertEquals(DOMException.NAMESPACE_ERR, de.code);
            }
       }

    public void moveToNode() {
        m_node = m_doc.getFirstChild().getFirstChild().getChildNodes().item(1);//zed node;
        assertFalse(m_node == null);
    }

    public void setUp() throws Exception {
        super.setUp();
        moveToNode();
    }


}

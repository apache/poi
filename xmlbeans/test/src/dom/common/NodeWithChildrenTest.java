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

package dom.common;

import org.w3c.dom.*;




public abstract class NodeWithChildrenTest extends NodeTest {


    public NodeWithChildrenTest(String s) {
        super(s);
    }

    public void testRemoveChildEnd() {
        Node node = m_node.getLastChild();
        super.testRemoveChild(node);

    }

    public void testRemoveChild() {
        NodeList children = m_node.getChildNodes();
        int pos = children.getLength() / 2;
        Node node = children.item(pos);
        super.testRemoveChild(node);

    }


    public void testRemoveChildDiffImpl() throws Exception {

        Node toRemove=NodeTest.getApacheNode(sXml,true,'E');
        try {
             super.testRemoveChild(toRemove);
            fail("Removing node from a different impl");
        } catch (DOMException de) {
            assertEquals(de.code, DOMException.WRONG_DOCUMENT_ERR);
        }

    }

    public void testRemoveChildDiffDoc() throws Exception {
        Node toRemove=m_docNS.getDocumentElement();
        try {
            super.testRemoveChild(toRemove);
            fail("Removing node from a different doc");
        } catch (DOMException de) {
            assertEquals(de.code, DOMException.WRONG_DOCUMENT_ERR);
        }

    }



    public void testRemoveChildFront() {
        Node node = m_node.getFirstChild();
        super.testRemoveChild(node);

    }

    public void testRemoveChildNull() {
        super.testRemoveChild(null);
    }

    public void testReplaceChild() {
        NodeList children = m_node.getChildNodes();
        int pos = children.getLength() / 2;
        Node newNode;
        if (m_node instanceof Document)
               newNode= m_doc.createElement("fooBAR");
        else
                 newNode=m_doc.createTextNode("fooBAR");
        Node node = children.item(pos);
        super.testReplaceChild(newNode, node);

    }

    public void testReplaceChildEnd() {
        Node node = m_node.getLastChild();
        Node newNode = m_doc.createTextNode("fooBAR");
        super.testReplaceChild(newNode, node);
    }

    public void testReplaceChildFront() {
        Node node = m_node.getFirstChild();
        Node newNode = m_doc.createTextNode("fooBAR");
        super.testReplaceChild(newNode, node);
    }

    public void testReplaceChildNullChild() {
        Node node = m_node.getChildNodes().item(0);
        Node newNode = null;
        assertFalse(null == node);
        super.testReplaceChild(newNode, node);
    }

    public void testReplaceChildNull() {
        Node node = null;
        Node newNode;
        if (m_node instanceof Document)
            newNode = ((Document) m_node).createElement("fooBAR");
        else
            newNode = m_node.getOwnerDocument().createElement("fooBAR");
        super.testReplaceChild(newNode, node);
    }

    public void testReplaceChildDNE() {

        if (!(m_doc instanceof Document))
            assertEquals(m_doc, m_node.getOwnerDocument());

        //node to replace is not a child
        Node node =m_doc.createElement("foobar");
        Node newNode = m_doc.createElement("fooBAR");
        try {
            super.testReplaceChild(newNode, node);
        } catch (DOMException de) {
            assertEquals(DOMException.NOT_FOUND_ERR, de.code);//Raised if oldChild is not a child of this node.
        }

         //newChild was created from a different document than the one that created this node

        newNode = m_docNS.createElement("fooBAR");
        assertFalse(m_docNS.equals(m_node.getOwnerDocument()));
        try {
            super.testReplaceChild(newNode, node);
            fail("Node is from the wrong document");
        } catch (DOMException de) {
            assertEquals(DOMException.WRONG_DOCUMENT_ERR, de.code);
        }
       //refChild was created from a different document than the one that created this node

          node = m_docNS.createElement("fooBAR");
            newNode=m_doc.createElement("fooBAR");
        try {
            super.testReplaceChild(newNode, node);
            fail("Node is from the wrong document");
        } catch (DOMException de) {
            assertTrue(
                          (DOMException.WRONG_DOCUMENT_ERR == de.code)
                           || (DOMException.NOT_FOUND_ERR == de.code)
                    );
        }

    }


    // public void testInsertBeforeDiffDoc(){}:done above

    public void testReplace_replacement_DiffImpl() throws Exception {
        Node node = m_node.getFirstChild();
        Node newnode=NodeTest.getApacheNode(sXml,true,'T');
        try {
            super.testReplaceChild(newnode, node);
            fail("Inserting node created from a different impl");
        } catch (DOMException de) {
            assertEquals(de.code, DOMException.WRONG_DOCUMENT_ERR);
        }

    }

    //ref child is diff impl
    public void testReplace_target_DiffImpl() throws Exception {
        Node node =NodeTest.getApacheNode(sXml,true,'E');
        Node newnode=m_node.getFirstChild();
        try {
            super.testReplaceChild(newnode, node);
            fail("Inserting node created from a different impl");
        } catch (DOMException de) {
            assertEquals(de.code, DOMException.WRONG_DOCUMENT_ERR);
        }

    }

    public void testReplaceChildDocFrag() {
        DocumentFragment child = m_doc.createDocumentFragment();
        child.appendChild(m_doc.createElement("foo"));
        child.appendChild(m_doc.createElement("foobar"));
        Node toReplace = m_node.getFirstChild();
        super.testReplaceChild(child, toReplace);
    }

    public void testInsertBefore() {
        Node target = m_node.getFirstChild();
        Node child = m_doc.createElementNS("org.foo.www", "foonode");
        assertFalse(target == null);
        super.testInsertBefore(child, target);
    }

    public void testInsertBeforeNullTarget() {
        Node child = m_doc.createElementNS("org.foo.www", "foonode");
        super.testInsertBefore(child, null);
    }

    public void testInsertBeforeInvalidRefNode() {
        Node child = m_doc.createElementNS("org.foo.www", "foonode");
        Node target = m_doc.createElement("foo");
        try {
            super.testInsertBefore(child, target);
            fail("Insert cannot happen");
        } catch (DOMException de) {
            System.err.println(de.getMessage() + " " + de.code);
            assertEquals(DOMException.NOT_FOUND_ERR, de.code);
        }
    }

    public void testInsertBeforeNewChildDiffDoc(){
        Node target = m_node.getFirstChild();
        Node toInsert=m_docNS.getDocumentElement();
         try {
             super.testInsertBefore(toInsert, target);
            fail("Inserting node created from a different doc");
        } catch (DOMException de) {
            assertEquals(de.code, DOMException.WRONG_DOCUMENT_ERR);
        }


    }

    public void testInsertBeforeNewChildDiffImpl() throws Exception {
        Node target = m_node.getFirstChild();
        Node toInsert=NodeTest.getApacheNode(sXml,true,'T');
        try {
            super.testInsertBefore(toInsert, target);
            fail("Inserting node created from a different impl");
        } catch (DOMException de) {
            assertEquals(de.code, DOMException.WRONG_DOCUMENT_ERR);
        }

    }

    public void testInsertBeforeRefChildDiffDoc(){
            Node target = m_docNS.getDocumentElement();
            Node toInsert= m_node.getFirstChild();;
             try {
                 super.testInsertBefore(toInsert, target);
                fail("Ref Child from a different doc");
            } catch (DOMException de) {
                assertEquals(de.code, DOMException.WRONG_DOCUMENT_ERR);
            }


        }

        public void testInsertBeforeRefChildDiffImpl() throws Exception {
            Node target = NodeTest.getApacheNode(sXml,true,'T');
            Node toInsert=m_node.getFirstChild();;
            try {
                super.testInsertBefore(toInsert, target);
                fail("Inserting node created from a different impl");
            } catch (DOMException de) {
                assertEquals(de.code, DOMException.WRONG_DOCUMENT_ERR);
            }

        }


    public void testInsertBeforeNullChild() {
        Node target = m_doc.createElement("foo");
        super.testInsertBefore(null, target);
    }

    /**
     *  pre: child is not a parent ancestor
     */
    public void testAppendChildExisting(Node child) {

        if (child == m_node)
            child = m_doc.getLastChild();
        //if still the same, too bad
        super.testAppendChild(child);
    }

    /**
     * pre: child cannot be an ancestor of m_node
     * @param child
   */
    public void testInsertExistingNode(Node child) {
        Node target = m_node.getFirstChild();
        if (target.getParentNode()==child.getParentNode())
            child=child.getParentNode();
        assertFalse(target == null || child == null);
        super.testInsertBefore(child, target);
    }
       
    public void testInsertBeforeDocFrag() {
        DocumentFragment child = m_doc.createDocumentFragment();
        child.appendChild(m_doc.createElement("foo1"));
        Node target = m_node.getFirstChild();
        super.testInsertBefore(child, target);
    }

    public void testAppendChild() {
        Node newNode = m_doc.createElement("foo");
        super.testAppendChild(newNode);
    }

    //try to append the parent
    public void testAppendChildIllegal0() {
        Node parent = m_node.getFirstChild();
        m_node = m_node.getFirstChild();
        try {
            super.testAppendChild(parent);
            fail("Appending parent ");
        } catch (DOMException de) {
            assertEquals(DOMException.HIERARCHY_REQUEST_ERR, de.code);
        }
    }

    //try to insert diff doc
    public void testAppendChildIllegal1() {
        Node newNode = m_docNS.createElement("newNode");
        try {
            super.testAppendChild(newNode);
            fail("Appending wrong doc");
        } catch (DOMException de) {
            assertEquals(DOMException.WRONG_DOCUMENT_ERR, de.code);
        }
    }

    //append doc frag
    public void testAppendChildDocFrag() {
        DocumentFragment child = m_doc.createDocumentFragment();
        child.appendChild(m_doc.createElement("foo"));
        super.testAppendChild(child);
    }

    //TODO  : not implemented
    public void testNormalize() {

        int nCount=m_node.getChildNodes().getLength();
        String value="";
        if (m_node.getLastChild() instanceof Text)
            value=((Text)m_node.getLastChild()).getNodeValue();

        int nExistingText=0;
        for (int i=nCount-1; i > -1; i--)
            if (m_node.getChildNodes().item(i) instanceof CharacterData)
                   nExistingText++;
        Node txt=m_doc.createTextNode("foo");
        m_node.appendChild(txt);

        txt=m_doc.createTextNode("");
        m_node.appendChild(txt);
        txt=m_doc.createTextNode(" bar");
        m_node.appendChild(txt);

        assertEquals(nCount+3,m_node.getChildNodes().getLength());

        m_node.normalize();

        assertEquals(true,(m_node.getLastChild() instanceof Text));
       // if (value.length()==0)nCount++;//if last node was a text nCount stays the same
        assertEquals(nCount-nExistingText+1,m_node.getChildNodes().getLength());

        value+="foo bar";
        assertEquals(value,m_node.getLastChild().getNodeValue());


    }


    public void testSetPrefixInvalid() {
        //test only applies to Attrs and Elems
        if (!(m_node.getNodeType() == Node.ATTRIBUTE_NODE
            || m_node.getNodeType() == Node.ELEMENT_NODE))
            return;

        //qualifiedName is malformed
        try {
            m_node.setPrefix("invalid<");
            fail("Invalid prefix name--see http://www.w3.org/TR/REC-xml#NT-BaseChar");
        } catch (DOMException de) {
            assertEquals(DOMException.INVALID_CHARACTER_ERR, de.code);
        }

        //the qualifiedName has a prefix and the namespaceURI is null
        try {

            if (m_node.getNamespaceURI() == null) {
                m_node.setPrefix("foo");
                fail("Can not set prefix here");
            } else {
                m_node.setPrefix("xml");
                fail("Xml is not a valid prefix here");
            }
        } catch (DOMException de) {
            assertEquals(DOMException.NAMESPACE_ERR, de.code);
        }

    }

    public void testSetNodeValue() {
        int nCount = m_node.getChildNodes().getLength();
        m_node.setNodeValue("blah");
        assertEquals(nCount, m_node.getChildNodes().getLength());
        for (int i = 0; i < nCount; i++)
            assertEquals(false, ("blah".equals(m_node.getChildNodes().item(i).getNodeValue())));
    }
}

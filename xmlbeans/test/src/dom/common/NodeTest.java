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

import junit.framework.TestCase;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.StringReader;


public abstract class NodeTest extends TestCase implements
       TestSetup {
    protected Node m_node;

    protected Document m_doc;
    protected String sXml;

    protected Document m_docNS;
    protected String sXmlNS;
    //attributes

    public NodeTest(String s) {
        super(s);
    }

    public final void testInherited() {
    };
    public abstract void testNodeName();

    public abstract void testNodeType();

    public abstract void testNodeValue();

    public abstract void testParent();

    public abstract void testPreviousSibling();

    public abstract void testNextSibling();

    public void testOwnerDocument() {
        assertEquals(m_doc, m_node.getOwnerDocument());
    }

    public void testPrefix() {
        assertFalse(m_node == null);
        assertEquals(null,m_node.getPrefix());
       // assertEquals("", m_node.getPrefix());
    }

    public void testNamespaceUri() {
        assertFalse(m_node == null);
        assertEquals(null,m_node.getNamespaceURI());
        //assertEquals("", m_node.getNamespaceURI());
    }

    public void testLocalName() {
        assertFalse(m_node == null);
        assertEquals(null,m_node.getLocalName());
       // assertEquals("", m_node.getLocalName());
    }

    //0 length list as of API
    public void testGetChildNodes() {
        assertEquals(0, m_node.getChildNodes().getLength());
    }

    public void testFirstChild() {
        assertEquals(null, m_node.getFirstChild());
    }

    public void testLastChild() {
        assertEquals(null, m_node.getLastChild());
    }

    public abstract void testAppendChild();

    /**
     * pathologic cases: newChild is m_node or an ancestor
     * newChild is from a different document
     * newChild is not allowed at this pos
     */
    public void testAppendChild(Node newChild) {
        Node inserted = m_node.appendChild(newChild);
        if (newChild.getNodeType() == Node.DOCUMENT_FRAGMENT_NODE)
            assertEquals(true, compareNodeListPrefix(newChild.getChildNodes(), m_node.getChildNodes()));
        else
            assertEquals(inserted, m_node.getLastChild());
        if (isInTree(m_node, newChild)) //new child is in the tree
        //$NOTE: assert the child is removed first
            ;
    }

    /**
     * $NOTE:
     * override for Element;
     * override for Attribute
     * $TODO: ER results in a mutable copy
     */
    public void testCloneNode() {

        Node m_clone;
        /**not implemented : Eric */
         m_clone=m_node.cloneNode(false);
         assertEquals(true, DomUtils.compareNodesShallow(m_node, m_clone));
//         assertEquals(true, DomUtils.compareNodeTreePtr(m_clone.getChildNodes(),m_node.getChildNodes())); //ptr eq for ch.
         assertEquals(false,m_clone==m_node);


        m_clone = m_node.cloneNode(true);
        assertEquals(true, DomUtils.compareNodesDeep(m_node, m_clone)); //deep clone: should do for whole tree, not just ch.
        assertEquals(false, m_clone == m_node);

        assertEquals(null, m_clone.getParentNode());
    }

    public abstract void testInsertBefore();



    public void testInsertBefore(Node newChild, Node refChild) {

        int newChPos = getChildPos(m_node, newChild);
        int pos = getChildPos(m_node, refChild);
        Node prevParent = null;
        if (newChPos > -1)
            prevParent = newChild.getParentNode();
        NodeList childNodes = m_node.getChildNodes();
        int nOrigChildNum = childNodes.getLength(); //get it now, List is live


        if (newChild == null) {
            try {
                m_node.insertBefore(newChild, refChild);
                fail("Inserting null");
            } catch (IllegalArgumentException e) {
                return;
            }
        }
        Node inserted = m_node.insertBefore(newChild, refChild);


        if (refChild == null)
            assertEquals(inserted, m_node.getLastChild());
        else if (pos == -1)//would have thrown exc
            fail("Inserting after fake child");
        else if (newChild.getNodeType() == Node.DOCUMENT_FRAGMENT_NODE)
            assertEquals(true, compareNodeListPrefix(newChild.getChildNodes(), m_node.getChildNodes()));
        else if (newChPos != -1) //new child is in the tree
        //assert the child is removed first
            assertEquals(false, inserted.getParentNode().equals(prevParent));
        else {
            assertEquals(newChild, childNodes.item(pos));
            assertEquals(nOrigChildNum + 1, m_node.getChildNodes().getLength());
        }

    }

    /**
     * $NOTE: override for element
     */
    public void testGetAttributes() {
        assertEquals(null, m_node.getAttributes());
    }


    public void testHasChildNodes() {
        int i = m_node.getChildNodes().getLength();
        if (i > 0)
            assertEquals(true, m_node.hasChildNodes());
        else
            assertEquals(false, m_node.hasChildNodes());
    }


    //Override for Element
    public void testHasAttributes() {
        assertEquals(false, m_node.hasAttributes());
    }


    public void testIsSupported() {
        String[] features=new String[]{
            "Core","XML","Events","MutationEvents","Range","Traversal","HTML","Views","StyleSheets","CSS","CSS2","UIEvents","HTMLEvents"
        };
        boolean bResult=true;
        for (int i=0;i<features.length;i++){
            if (i>1) bResult=false;
            System.out.println("============== "+features[i]+" ============="+bResult);
            assertEquals(bResult,m_node.isSupported(features[i],"2.0"));
        }

    }

    public void testNormalize() {
        fail("Test Not implemented");
    }


    /**
     * pathological cases:
     * node is read-only
     * node is not a child of this node
     */
    public abstract void testRemoveChild();

    public void testRemoveChild(Node removed) {
        int pos = getChildPos(m_node, removed);
        int len = m_node.getChildNodes().getLength();
        if (removed == null)
            try {
                m_node.removeChild(removed);
                fail("Should not be Removing non-existing node");
            } catch (DOMException de) {
                assertEquals(DOMException.NOT_FOUND_ERR, de.code);
            }
        else if (pos == -1)
            try {
                m_node.removeChild(removed);
                fail("Removing non-existing node");
            } catch (DOMException de) {
                throw de;
            }
        else {
            m_node.removeChild(removed);
            assertEquals(len - 1, m_node.getChildNodes().getLength());
        }

    }


    public abstract void testReplaceChild();

    /**
     * pathological cases:
     * node is DocFrag
     * node is already in tree
     */
    public void testReplaceChild(Node newChild, Node oldChild) {
        int pos = getChildPos(m_node, oldChild);
        boolean existing = isInTree(m_doc.getDocumentElement(), newChild); //new Child has a parent

        int len = m_node.getChildNodes().getLength();


        if (newChild == null) {
            try {
                m_node.replaceChild(newChild, oldChild);
                fail("Inserting null");
            } catch (IllegalArgumentException e) {
            }
        } else if (pos == -1) {
            try {
                m_node.replaceChild(newChild, oldChild);
                fail("Replacing non-existing node");
            } catch (DOMException de) {
                if (DOMException.NOT_FOUND_ERR != de.code)
                    throw de;
            }
        } else if (existing) {
            Node oldParent = newChild.getParentNode();
            NodeList old = m_node.getChildNodes();
            assertEquals(oldChild, m_node.replaceChild(newChild, oldChild));
            assertFalse(newChild.getParentNode().equals(oldParent));
        } else if (newChild == null) { //is this equivalent to deletion?
            m_node.replaceChild(newChild, oldChild);
            assertEquals(len - 1, m_node.getChildNodes().getLength());
        } else if (newChild.getNodeType() == Node.DOCUMENT_FRAGMENT_NODE) {
            int new_len = newChild.getChildNodes().getLength();
            assertEquals(oldChild, m_node.replaceChild(newChild, oldChild));
            assertEquals(new_len + len - 1, m_node.getChildNodes().getLength());//new+old-one replaced
        } else
            m_node.replaceChild(newChild, oldChild);


    }

    public abstract void testSetNodeValue();

    //$NOTE:override for element and attribute
    public void testSetPrefix()
    {
        //any prefix here is invalid
        String val = null;
        val = "blah"; //Eric's default
        try
        {
            m_node.setPrefix(val);
            fail(" set prefix only works for at/elt");
        }
        catch (DOMException de)
        {
            assertEquals(DOMException.NAMESPACE_ERR, de.code);
        }
    }

    public static int getChildPos(Node node, Node child) {
        if (child == null) return -1;
        NodeList ch = node.getChildNodes();
        for (int i = 0; i < ch.getLength(); i++)
            if (ch.item(i) == child)
                return i;
        return -1;
    }

    public static boolean isInTree(Node root, Node find) {
        if (find == null) return false;
        if (root == null) return false;
        if (root == find) return true;
        NodeList ch = root.getChildNodes();
        boolean temp_res = false;
        for (int i = 0; i < ch.getLength(); i++)
            temp_res = temp_res || isInTree(ch.item(i), find);
        return temp_res;
    }

    public static boolean compareNodeList(NodeList l1, NodeList l2) {
        if (l1.getLength() != l2.getLength()) return false;
        for (int i = 0; i < l1.getLength(); i++)
            if (l1.item(i) != l2.item(i)) //pointer eq
                return false;
        return true;
    }

    //l1 is a prefix of l2
    public static boolean compareNodeListPrefix(NodeList l1, NodeList l2) {
        if (l1.getLength() > l2.getLength()) return false;
        for (int i = 0; i < l1.getLength(); i++)
            if (l1.item(i) != l2.item(i)) //pointer eq
                return false;
        return true;
    }

    public void loadSync() throws Exception {
        _loader = Loader.getLoader();

        if (sXml == null && sXmlNS == null) throw new IllegalArgumentException("Test bug : Initialize xml strings");
        m_doc = (org.w3c.dom.Document) _loader.loadSync(sXml);
        if (sXmlNS != null && sXmlNS.length() > 0)
            m_docNS = (org.w3c.dom.Document) _loader.loadSync(sXmlNS);

    }

    
     public static Node getApacheNode(String sXml,boolean namespace,char type)
            throws Exception{
        org.apache.xerces.parsers.DOMParser parser = new org.apache.xerces.parsers.DOMParser();
        parser.parse(new InputSource(new StringReader(sXml)));
	Document doc = parser.getDocument();

        String name="apache_node";
        String nsname="pre:apache_node";
        String uri="uri:apache:test";

        switch(type){
        case 'A':
            if (namespace)
                return doc.createAttributeNS(uri,nsname);
            else
                return doc.createAttribute(name);
        case 'E':
             if (namespace)
                 return  doc.createElementNS(uri,nsname);
             else  return  doc.createElement(name);
        default: return  doc.createTextNode(name);

        }
   
    }


    public abstract void moveToNode();

    //exposing a node for other tests...saver in particular
    public Node getNode(){
        return m_node;
    }
    public void setUp() throws Exception {
        //m_doc=(org.w3c.dom.Document)org.apache.xmlbeans.XmlObject.Factory.parse(xml).newDomNode();
        _loader = Loader.getLoader();

        if (sXml == null && sXmlNS == null) throw new IllegalArgumentException("Test bug : Initialize xml strings");
        m_doc = (org.w3c.dom.Document) _loader.load(sXml);
        if (sXmlNS != null && sXmlNS.length() > 0)
            m_docNS = (org.w3c.dom.Document) _loader.load(sXmlNS);
    }

    private Loader _loader;
}


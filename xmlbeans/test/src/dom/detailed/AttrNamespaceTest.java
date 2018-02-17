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

package dom.detailed;

import dom.common.Loader;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.apache.xmlbeans.XmlObject;

import java.io.StringReader;
import java.io.IOException;


/**
 * Tests conversion of regular attributes to namespaces and vv
 */

public class AttrNamespaceTest extends TestCase {
    String sXml = "<foo at0=\"nonsattr\"></foo>";
    String sXmlNS = "<foo xmlns:myns=\"http://foo.org\"><myns:bar/></foo>";
    Document m_doc,
    m_docNS;
    Node m_node;


    public AttrNamespaceTest(String name) {
        super(name);
    }

    public static Test suite() {
        return new TestSuite(AttrNamespaceTest.class);
    }

    public void testDefaultNamespace() {
        //assertEquals(true,((Element)m_node).hasAttribute("xmlns"));
    }

    public void testAttr2Namespace() {
        Attr at = (Attr) ((Element) m_node).getAttributeNode("at0");
        String namespaceURI = "http://foo.org";
        String qualifiedName = "xmlns:bar";
        at.setValue(namespaceURI);
        m_node.appendChild(m_doc.createElementNS(namespaceURI, qualifiedName));
        Element bar = (Element) ((Element) m_node).getElementsByTagNameNS(namespaceURI, "bar").item(0);
        assertFalse(null == bar);
        assertEquals(namespaceURI, bar.getNamespaceURI());
        assertEquals(qualifiedName, bar.getNodeName());
        /*
	org.apache.xmlbeans.XmlCursor cur=Public2.getCursor(m_node);
	assertFalse(cur.isAttr());
	assertTrue(cur.isNamespace());
	assertTrue(cur.isAnyAttr());
	*/
    }

    public void testNamespace2Attr() {
        m_node = m_docNS.getFirstChild();

        int nAttrCount = ((Element) m_node).getAttributes().getLength();
        ((Element) m_node).removeAttribute("xmlns:myns");
        assertEquals(nAttrCount - 1, ((Element) m_node).getAttributes().getLength());//assertRemoved
        ((Element) m_node).setAttribute("myns", "reg_attr_val");
        Attr at = (Attr) ((Element) m_node).getAttributeNode("myns");

        assertEquals("reg_attr_val", at.getValue());
        assertEquals("myns:bar", m_node.getFirstChild().getNodeName());

        /*

        if(!(m_node instanceof DomImpl))
            fail(m_node.getClass().toString());

        org.apache.xmlbeans.XmlCursor  cur=Public2.getCursor(m_node);

        assertFalse(cur.isNamespace());
        assertTrue(cur.isAnyAttr());
        assertTrue(cur.isAttr());
        */
    }

    /**
     * Insert a namespace attribute and set it's value to
     * ""/NULL...Do we get an error since now there is a prefix
     * with NULL URI?
     */
    public void testInsertBadAttribute() throws Exception{
        String sER="<foo/>";
        org.apache.xerces.parsers.DOMParser parser = new org.apache.xerces.parsers.DOMParser();
        parser.parse(new InputSource(new StringReader(sER)));
        Document xercesDocument = parser.getDocument();
        Document beanDocument=(Document)XmlObject.Factory.parse(sER)
                .getDomNode();
        Attr at_xerces=xercesDocument.createAttributeNS("http://www.w3.org/2000/xmlns/","xmlns:ns");
        Attr at_bean=beanDocument.createAttributeNS("http://www.w3.org/2000/xmlns/","xmlns:ns");

        beanDocument.getDocumentElement().setAttributeNode(at_bean);
        xercesDocument.getDocumentElement().setAttributeNode(at_xerces);
        at_bean.setValue(null);
        at_xerces.setValue(null);
         at_bean.setValue("");
        at_xerces.setValue("");
    }


    public void setUp() throws Exception {

        if (sXml == null && sXmlNS == null) throw new IllegalArgumentException("Test bug : Initialize xml strings");
        Loader loader = Loader.getLoader();
        m_doc = (org.w3c.dom.Document) loader.load(sXml);
        if (sXmlNS != null && sXmlNS.length() > 0)
            m_docNS = (org.w3c.dom.Document) loader.load(sXmlNS);
        m_node = m_doc.getFirstChild();
    }
}

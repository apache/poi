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
import junit.framework.TestCase;
import org.w3c.dom.*;
import org.apache.xmlbeans.XmlObject;


/**
 *
 *
 */
public class TextInsertDeleteTest extends TestCase {
    String sXml = "<foo>txt0<bar/>txt1<baz/>txt2</foo>";
    String sXmlNS = "<foo/>";
    Document m_doc,
    m_docNS;
    Node m_node;

    public void testBuildMixedContent(){
        DOMImplementation domImpl = XmlObject.Factory.newDomImplementation( null );
        m_doc=domImpl.createDocument("foobar","val",null);
        Element root=m_doc.getDocumentElement();
        //m_doc.appendChild(root);
        Element n=(Element)m_doc.createElement("foo");
        Text txt1=m_doc.createTextNode("foobar");
        Text txt2=m_doc.createTextNode("baz");
       root.appendChild(txt1);
         root.appendChild(n);
         root.appendChild(txt2);
         System.out.println(m_doc.toString()) ;
    }

    public void testAdjacent() {
        NodeList ch = m_node.getChildNodes();
        m_node.removeChild(ch.item(1));
        assertEquals(ch.getLength(), 4);
        assertEquals("txt0", ch.item(0).getNodeValue());

        m_node.removeChild(ch.item(2));
        assertEquals(ch.getLength(), 3);
        assertEquals("txt2", ch.item(2).getNodeValue());

    }

    public void testInsertDelete() {
        //eric test
        // TODO: three children delete middle--verify length
        m_node = m_docNS.getFirstChild();

        Text txt1 = m_docNS.createTextNode("bar");
        Text txt2 = m_docNS.createTextNode("baz");
        m_node.appendChild(txt1);
        m_node.appendChild(txt2);
        assertEquals("baz", m_node.getLastChild().getNodeValue());
        assertEquals("bar", m_node.getFirstChild().getNodeValue());
        m_node.removeChild(m_node.getChildNodes().item(1));
        assertEquals("bar", m_node.getLastChild().getNodeValue());

        NodeList ch = m_node.getChildNodes();

        //another test
        Text txt3 = m_docNS.createTextNode("boom");
        m_node.appendChild(txt2);
        m_node.appendChild(txt3);
        Node remove = m_node.getChildNodes().item(1);
        m_node.removeChild(remove);
        assertEquals("boom", m_node.getLastChild().getNodeValue());
        assertEquals("bar", m_node.getFirstChild().getNodeValue());
        assertEquals(2, ch.getLength());

        m_node.removeChild(m_node.getChildNodes().item(1));
        assertEquals(1, ch.getLength());
        assertEquals("bar", m_node.getLastChild().getNodeValue());
        assertEquals("bar", m_node.getFirstChild().getNodeValue());


    }

    public void testInsertDeleteBulk() {

        int nNodeCnt = 16;
        m_node = m_docNS.getDocumentElement();
        Text[] nodes = new Text[nNodeCnt];
        NodeList ch = m_node.getChildNodes();

        for (int i = 0; i < nNodeCnt; i++) {
            nodes[i] = m_docNS.createTextNode("bar" + i);
            m_node.appendChild(nodes[i]);
        }

        assertEquals(nNodeCnt, ch.getLength());

        //delete all odd entries;go back
        for (int i = nNodeCnt - 1; i > -1; i--) {
            if (i % 2 != 0)
                m_node.removeChild(nodes[i]);
        }

        assertEquals(nNodeCnt/2 , ch.getLength());

        //split all remaining nodes

        for (int i = 0; i < nNodeCnt; i++) {
            ((Text)ch.item(i++)).splitText(2);
        }
        System.out.println();
        //delete all even entries;go fwd
        for (int i = 0; i < nodes.length; i++) {
            if (i % 2 == 0)
                m_node.removeChild(nodes[i]);
        }
          System.out.println();
        for (int i = 0; i < nNodeCnt / 2; i++) {

            assertEquals("r" + 2 * i, ch.item(i).getNodeValue());
        }

    }

    public void setUp() throws Exception {

        Loader loader = Loader.getLoader();
        if (sXml == null && sXmlNS == null) throw new IllegalArgumentException("Test bug : Initialize xml strings");
        m_doc = (org.w3c.dom.Document) loader.load(sXml);
        if (sXmlNS != null && sXmlNS.length() > 0)
            m_docNS = (org.w3c.dom.Document) loader.load(sXmlNS);

        m_node = m_doc.getFirstChild();
    }
}

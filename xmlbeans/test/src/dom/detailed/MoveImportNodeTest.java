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
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import java.io.StringReader;


/**
 *
 *
 *
 */
public class MoveImportNodeTest extends TestCase{
    String sXmlNS="<foo><foobar  xmlns:myns=\"http://foo.org\" xmlns:other=\"http://other.org\"><myns:bar/></foobar></foo>";

     String sXml="<foo at0=\"no_ns_attr\"><bar/></foo>";
    Document m_doc,
	m_docNS;
    Node m_node;



    public  MoveImportNodeTest(String name){
	super(name);
    }

    //insert a node from a ns into a non-ns: node will move "as is"
    //even though ns is not in scope as DOM does no prefix resolution
    public void testMoveNodeNStoNoNS(){
	Node toMove=m_docNS.getFirstChild().getFirstChild().getFirstChild(); //bar
        assertEquals("myns:bar",toMove.getNodeName());
	Element newParent=(Element)m_docNS.getFirstChild();
        assertEquals("foo",newParent.getNodeName());
	newParent.insertBefore(toMove,newParent.getFirstChild());

	assertEquals(2,newParent.getChildNodes().getLength());
    assertEquals(toMove,newParent.getElementsByTagNameNS("http://foo.org","bar").item(0));
	assertEquals(newParent.getElementsByTagName("bar").item(0),
        newParent.getElementsByTagNameNS(null,"bar").item(0));

    }

    //move node to a different namespace
    //namespace of node should be unchanged -- DOM does not care
    public void testMoveDiffNS(){
	Node toMove=m_docNS.getFirstChild().getFirstChild().getFirstChild(); //bar
	Element newParent=(Element)m_docNS.getFirstChild();
	newParent.insertBefore(toMove,newParent.getFirstChild());
	newParent.getFirstChild().setPrefix("other");
	assertEquals(2,newParent.getChildNodes().getLength());
	assertEquals(0,(newParent).getElementsByTagNameNS(null,"bar").getLength());
	assertEquals(true,(toMove==newParent.getElementsByTagNameNS("http://foo.org","bar").item(0)));
    }


    //import to a doc where the given ns DNE
    public void testMoveDiffDoc(){
	Node toMove=m_docNS.getFirstChild().getFirstChild().getFirstChild(); //bar
	try{
	    m_node.insertBefore(toMove,m_node.getFirstChild());
	    fail(" Cannot move nodes across docs");
	}catch(DOMException de){
	    assertEquals(DOMException.WRONG_DOCUMENT_ERR,de.code);
	}

    }

    public void testMoveDiffImplementations() throws Exception{
	org.apache.xerces.parsers.DOMParser parser =
            new org.apache.xerces.parsers.DOMParser();


	parser.parse(new InputSource(new StringReader(sXmlNS)));

	Document xercesDocument = parser.getDocument();
	assertFalse (xercesDocument==null);
	Node toMove=xercesDocument.getFirstChild().getFirstChild().getFirstChild(); //bar
	System.out.println("=======Source Impl "+xercesDocument.getImplementation());
	System.out.println("=======Dest Impl "+m_doc.getImplementation());

	try{
	    m_node.insertBefore(toMove,m_node.getFirstChild());
	    fail(" Cannot move nodes across implementations");
	}catch(DOMException de){
	    assertEquals(DOMException.WRONG_DOCUMENT_ERR,de.code);
	}

	Node imported=m_doc.importNode(toMove,true);//deep would be the same here
	m_node.appendChild(imported);

	assertEquals(2,m_node.getChildNodes().getLength());
        //node should be imported w/ prefix and all
	assertEquals(imported,((Element)m_node).getElementsByTagNameNS("http://foo.org","bar").item(0));
        //	assertEquals(((Element)m_node).getElementsByTagName("bar").item(1),imported);
	assertEquals(((Element)m_node).getElementsByTagName("bar").item(0),((Element)m_node).getElementsByTagNameNS(null,"bar").item(0));
    }


   // public void 
    public void testImportSameDoc(){
	//inspired by nist documentimportnode10?

	Node toImport=m_doc.createElement("foobar");
	toImport=m_doc.importNode(toImport,true);

        toImport=m_doc.createDocumentFragment();
        toImport.appendChild(m_doc.getDocumentElement().getFirstChild());

        m_doc.importNode(toImport,true);
    }

    public static Test suite() {
	return new TestSuite(MoveImportNodeTest.class);
    }

    public void setUp() throws Exception{

        Loader loader=Loader.getLoader();
	if (sXml==null && sXmlNS==null) throw new IllegalArgumentException("Test bug : Initialize xml strings");
	m_doc=(org.w3c.dom.Document)loader.load(sXml);
	if(sXmlNS!=null && sXmlNS.length()>0)
	    m_docNS=(org.w3c.dom.Document)loader.load(sXmlNS);

	m_node=m_doc.getFirstChild();
    }
}

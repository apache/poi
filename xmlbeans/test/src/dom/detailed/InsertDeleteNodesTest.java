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
import org.w3c.dom.*;


/**
 * Insertion and deletion of nodes, normalization of text children
 *
 *
 *
 */

public class InsertDeleteNodesTest extends TestCase{
    String sXml="<foo at0=\"no_ns_attr\"></foo>";
    String sXmlNS="<foo xmlns:myns=\"http://foo.org\"><myns:bar/></foo>";
    Document m_doc,
	m_docNS;
    Node m_node;




    public InsertDeleteNodesTest(String name){
	super(name);
    }
    public static Test suite() {
	return new TestSuite(InsertDeleteNodesTest.class);
    }

    public void testInsertNodes(){
	//insert one of each type of node:
	Text txt0=m_doc.createTextNode("foo");
	Text txt1=m_doc.createTextNode(" ");
	Text txt2=m_doc.createTextNode("bar");
	Element elt1=m_doc.createElement("elt1");

	Attr attr0=m_doc.createAttributeNS("xmlns","attr0");

	Comment comment=m_doc.createComment("A comment");
	DocumentFragment doc_frag=m_doc.createDocumentFragment();
	//Document new_doc: what to do with this...
	//CDATASection cdata=m_doc.createCDATASection("<CDATA Section>");--Not impl
	//ProcessingInstruction pi=m_doc.createProcessingInstruction("xml","version 1.0"); --Not impl
	// DocumentType--TODO
	//Entity
	//EntityReference
	//Notation

	Text nested_txt=m_doc.createTextNode("foo");


	//m_doc.appendChild(m_doc.createElement("root"));

	m_doc.getDocumentElement().appendChild(txt2);
	m_doc.getDocumentElement().insertBefore(elt1,txt2);
	m_doc.getDocumentElement().insertBefore(txt1,elt1);
	m_doc.getDocumentElement().insertBefore(txt0,txt1);

	//insert level 1
	System.out.println("=======Basic inserts passed ===================");
        int nAttrs=20;
	for (int i=0;i<nAttrs;i++){
	    Attr insertAttr=(Attr)attr0.cloneNode(true);
	    insertAttr.setValue(i+"");
	    elt1.setAttributeNode(insertAttr);
	}

	assertEquals(1,elt1.getAttributes().getLength());
	assertEquals((nAttrs-1)+"",elt1.getAttributes().getNamedItemNS("xmlns","attr0").getNodeValue());


	//elt1.appendChild(cdata);
	elt1.appendChild(comment);
	//elt1.appendChild(pi);
	elt1.appendChild(nested_txt);

        Element childElt=m_doc.createElement("childElt");
        childElt.setAttributeNode(attr0);
         attr0.setValue("Attr0Value");

        //a chain of depth 100 under doc frag of elt w/ attr
	doc_frag.appendChild((Element)childElt.cloneNode(true));

	System.out.println("======= Overwriting same attr ===================");
	Element last=(Element)doc_frag.getFirstChild();


        int nMaxTries=100;




	for (int i=0;i<nMaxTries;i++){
	    Element deep_nested=(Element)childElt.cloneNode(true);
	    last.appendChild(deep_nested);
	    last=(Element)last.getFirstChild();
	}

	elt1.appendChild(doc_frag);

	System.out.println("======= Inserted deep chain  ===================");
	NodeList deepChain=m_doc.getDocumentElement().getElementsByTagName("childElt");
	assertEquals(nMaxTries+1,deepChain.getLength());//newly inserted + 1 original

	for (int i=0;i<nMaxTries;i++)
	    assertEquals("Attr0Value",deepChain.item(i).getAttributes().getNamedItemNS("xmlns","attr0").getNodeValue());

	//check doc frag isn't there
	assertEquals("childElt",elt1.getLastChild().getNodeName());



	/////Done inserting: begin deletion:

	//1. delete the deep tree at depth 50
	Node toRemove=deepChain.item(nMaxTries / 2);
	toRemove.removeChild(toRemove.getFirstChild());

	assertEquals(nMaxTries / 2 + 1,deepChain.getLength());

	//test normalization with Elt node
	Element root=m_doc.getDocumentElement();
	assertEquals(4,root.getChildNodes().getLength());
	root.removeChild(root.getChildNodes().item(2));

	assertEquals(0,deepChain.getLength());
        //TODO: normalize
	//root.normalize();
//	assertEquals(1,root.getChildNodes().getLength());
//	assertEquals("foo bar",root.getFirstChild());


	//insert stuff under doc node: should be able to insert comments and PI here....


	m_doc.insertBefore(comment,root);
	//m_doc.insertBefore(pi,root);
	//m_doc.insertBefore(cdata,root);

	try{
	    m_doc.insertBefore(root,doc_frag.getLastChild());
            fail("Should except here");
	}catch (DOMException de){
	    assertEquals(de.code,DOMException.HIERARCHY_REQUEST_ERR);
	}
    }
    //TODO: insert nodes at all illegal places:


    public void setUp() throws Exception{
	Loader loader=Loader.getLoader();
	if (sXml==null && sXmlNS==null) throw new IllegalArgumentException("Test bug : Initialize xml strings");
	m_doc=(org.w3c.dom.Document)loader.load(sXml);
	if(sXmlNS!=null && sXmlNS.length()>0)
	    m_docNS=(org.w3c.dom.Document)loader.load(sXmlNS);
	m_doc=m_doc.getImplementation().createDocument(null,"root",null);
	m_node=m_doc.getFirstChild();
    }

}

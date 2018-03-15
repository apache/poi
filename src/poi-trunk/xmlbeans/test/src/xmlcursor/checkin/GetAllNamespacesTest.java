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


package xmlcursor.checkin;

import junit.framework.*;

import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlCursor;
import xmlcursor.common.*;


import java.util.*;

/**
 *
 *
 */
public class GetAllNamespacesTest extends BasicCursorTestCase {

    static String sTestXml = "<bk:book xmlns:bk='urn:loc.gov:books'" +
            " xmlns:isbn='urn:ISBN:0-395-36341-6'>" +
            "<bk:title>Cheaper by the Dozen</bk:title>" +
            "<isbn:number>1568491379</isbn:number>" +
            "<nestedInfo xmlns:bk='urn:loc.gov:booksOverridden'>" +
            "nestedText</nestedInfo>" +
            "</bk:book>";


    public GetAllNamespacesTest(String sName) {
        super(sName);
    }

    public static Test suite() {
        return new TestSuite(GetAllNamespacesTest.class);
    }


    public void testCursorNotContainer() {
        //lousy message
        toNextTokenOfType(m_xc, XmlCursor.TokenType.TEXT);
        Map myHash = new HashMap();

        try {
            m_xc.getAllNamespaces(myHash);
            fail("Cursor not on a container");
        }
        catch (Exception e) {
            System.err.println(e.getMessage());
        }
        /*
          Iterator it=myHash.values().iterator();
          while(it.hasNext()){
          System.out.println(it.next());
          }
        */

    }


    public void testGetAllNamespaces() {
        //parse in setUp
        int nExpectedNamespaces = 2;//2 distinct namespaces but 3
        Map namespaceMap = new HashMap();
        toNextTokenOfType(m_xc, XmlCursor.TokenType.START);
        m_xc.getAllNamespaces(namespaceMap);
        assertEquals(namespaceMap.entrySet().size(), nExpectedNamespaces);
        assertEquals((String) namespaceMap.get("bk"), "urn:loc.gov:books");
        //assertEquals((String)namespaceMap.get("bk"),"urn:loc.gov:booksOverridden");
        assertEquals((String) namespaceMap.get("isbn"),
                "urn:ISBN:0-395-36341-6");
    }

    public void testGetAllNamespacesIllegalCursorPos() {
        int nExpectedNamespaces = 0;
        Map namespaceMap = new HashMap();
        m_xc.getAllNamespaces(namespaceMap);
        assertEquals(namespaceMap.entrySet().size(), nExpectedNamespaces);
    }

    public void testGetAllNamespacesNull() {

        toNextTokenOfType(m_xc, XmlCursor.TokenType.START);

            m_xc.getAllNamespaces(null);
    }

    /**
     * cursor is positioned below the namespace declaration but in its scope
     */
    public void testGetAllNamespacesInternal() {
        int nExpectedNamespaces = 2;
        Map namespaceMap = new HashMap();
        m_xc.toFirstChild();
        m_xc.toChild(2);//nestedInfo
        m_xc.getAllNamespaces(namespaceMap);
        assertEquals(namespaceMap.entrySet().size(), nExpectedNamespaces);

        assertEquals((String) namespaceMap.get("bk"),
                "urn:loc.gov:booksOverridden");
        assertEquals((String) namespaceMap.get("isbn"),
                "urn:ISBN:0-395-36341-6");

    }

    public void setUp() throws Exception {
        m_xc = XmlObject.Factory.parse(sTestXml).newCursor();
    }

    public static void main(String[] rgs) {
        try {
            GetAllNamespacesTest t = (new GetAllNamespacesTest(""));
            t.setUp();
            t.testGetAllNamespacesNull();
        }
        catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }


}

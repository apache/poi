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

import org.apache.xmlbeans.XmlOptions;
import junit.framework.*;
import junit.framework.Assert.*;

import java.io.*;

import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlCursor.TokenType;
import org.apache.xmlbeans.XmlDocumentProperties;
import org.apache.xmlbeans.XmlCursor.XmlBookmark;

import javax.xml.namespace.QName;

import xmlcursor.common.BasicCursorTestCase;

import java.net.URL;


/**
 *
 * 
 */
public class AddToSelectionTest extends BasicCursorTestCase
{

    static String sXml = "<foo><b>0</b><b>1</b><b>2</b><b attr=\"a3\">3</b><b>4</b><b>5</b><b>6</b></foo>";

    public AddToSelectionTest(String sName)
    {
        super(sName);
    }

    public static Test suite()
    {
        return new TestSuite(AddToSelectionTest.class);
    }

    public void testAddToSelectionEnd()
    {
        m_xc.toEndDoc();
        m_xc.addToSelection();
        assertEquals(1, m_xc.getSelectionCount());
    }

    public void testAddToSelectionStart()
    {
        m_xc.toStartDoc();
        m_xc.addToSelection();
        assertEquals(1, m_xc.getSelectionCount());
    }

    public void testAddToSelectionAll() throws Exception
    {
        sXml = "<foo></foo>";
        m_xc = XmlObject.Factory.parse(sXml).newCursor();
        XmlCursor.TokenType tok;
        m_xc.addToSelection();
        while ((tok = m_xc.toNextToken()) != XmlCursor.TokenType.NONE) {
            System.err.println(tok);
            m_xc.addToSelection();
        }
        assertEquals(4, m_xc.getSelectionCount());

        //check results
        XmlCursor m_xc1 = XmlObject.Factory.parse(sXml).newCursor();
        m_xc.toSelection(0); //reset cursor
        int i = m_xc.getSelectionCount();
        while ((tok = m_xc1.toNextToken()) != XmlCursor.TokenType.NONE) {
            //assertEquals(true,m_xc.hasNextSelection());
            assertEquals(m_xc.toNextToken(), tok);
            m_xc.toNextSelection();
        }
        //second cursor should be at the end of selections too...
        assertEquals(false, m_xc.toNextSelection());
        m_xc1.dispose();
    }

    public void testAddToSelectionSet()
    {
        //not set but bag semantics
        int expRes = 100;

        m_xc.clearSelections();
        for (int i = 0; i < 100; i++) {
            m_xc.toStartDoc();
            m_xc.addToSelection();
        }
        assertEquals(expRes, m_xc.getSelectionCount());
    }

    public void testAddAfterDispose()
    {
        m_xc.dispose();
        boolean error = false;
        try {
            m_xc.addToSelection();
        } catch (Throwable e) {
            error = true;
        }
        assertEquals(true, error);

    }


    public void setUp() throws Exception
    {
        m_xc = XmlObject.Factory.parse(sXml).newCursor();
        super.setUp();
    }

    public void tearDown()
    {
        if (m_xc == null) return;
        try {
            m_xc.clearSelections();
            super.tearDown();
        } catch (IllegalStateException e) { //cursor disposed
        } catch (Exception e) {

        }
    }
}


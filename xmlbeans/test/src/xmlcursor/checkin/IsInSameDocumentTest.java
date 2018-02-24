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

import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlCursor.TokenType;
import org.apache.xmlbeans.XmlObject;
import xmlcursor.common.BasicCursorTestCase;
import xmlcursor.common.Common;


/**
 *
 *
 */
public class IsInSameDocumentTest extends BasicCursorTestCase {
    public IsInSameDocumentTest(String sName) {
        super(sName);
    }

    public static Test suite() {
        return new TestSuite(IsInSameDocumentTest.class);
    }

    public void testSameDocSTARTDOCandENDDOC() throws Exception {
        m_xc = XmlObject.Factory.parse(Common.XML_FOO_1ATTR_TEXT).newCursor();
        XmlCursor xc0 = m_xc.newCursor();
        xc0.toEndDoc();
        try {
            assertEquals(true, m_xc.isInSameDocument(xc0));
            assertEquals(true, xc0.isInSameDocument(m_xc));
        } finally {
            xc0.dispose();
        }
    }

    public void testSameDocNAMESPACEandATTR() throws Exception {
        m_xc = XmlObject.Factory.parse(Common.XML_FOO_DIGITS).newCursor();
        XmlCursor xc0 = m_xc.newCursor();
        try {
            toNextTokenOfType(m_xc, TokenType.NAMESPACE);
            toNextTokenOfType(xc0, TokenType.ATTR);
            assertEquals(true, m_xc.isInSameDocument(xc0));
            assertEquals(true, xc0.isInSameDocument(m_xc));
        } finally {
            xc0.dispose();
        }
    }

    public void testSameDocNull() throws Exception {
        m_xc = XmlObject.Factory.parse(Common.XML_FOO_1ATTR_TEXT).newCursor();
        assertEquals(false, m_xc.isInSameDocument(null));
    }

    public void testSameDocDifferentDocs() throws Exception {
        m_xc = XmlObject.Factory.parse(Common.XML_FOO_1ATTR_TEXT).newCursor();
        XmlCursor xc0 = XmlObject.Factory.parse(Common.XML_FOO_1ATTR_TEXT).newCursor();
        toNextTokenOfType(m_xc, TokenType.TEXT);
        toNextTokenOfType(xc0, TokenType.TEXT);
        try {
            assertEquals(false, m_xc.isInSameDocument(xc0));
            assertEquals(false, xc0.isInSameDocument(m_xc));
        } finally {
            xc0.dispose();
        }
    }

    public void testSameDocTEXTpositional() throws Exception {
        m_xc = XmlObject.Factory.parse(Common.XML_FOO_1ATTR_TEXT).newCursor();
        XmlCursor xc0 = m_xc.newCursor();
        toNextTokenOfType(m_xc, TokenType.TEXT);
        toNextTokenOfType(xc0, TokenType.TEXT);
        xc0.toNextChar(2);
        try {
            assertEquals(true, m_xc.isInSameDocument(xc0));
            assertEquals(true, xc0.isInSameDocument(m_xc));
        } finally {
            xc0.dispose();
        }
    }

}


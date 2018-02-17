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

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import tools.util.JarUtil;
import xmlcursor.common.BasicCursorTestCase;
import xmlcursor.common.Common;
import junit.framework.Test;
import junit.framework.TestSuite;


/**
 *
 *
 */
public class GetDocChangeStampTest extends BasicCursorTestCase {
    public GetDocChangeStampTest(String sName) {
        super(sName);
    }

    public static Test suite() {
        return new TestSuite(GetDocChangeStampTest.class);
    }

    public void testGetDocChangeStampHasChanged() throws Exception {
        m_xo = XmlObject.Factory.parse(
                  JarUtil.getResourceFromJar(Common.TRANXML_FILE_XMLCURSOR_PO));
        m_xc = m_xo.newCursor();
        String ns="declare namespace po=\"http://xbean.test/xmlcursor/PurchaseOrder\";";

        m_xc.selectPath(ns+" $this//po:city");
        m_xc.toNextSelection();
        assertEquals("Mill Valley", m_xc.getTextValue());
        XmlCursor.ChangeStamp cs0 = m_xc.getDocChangeStamp();
        m_xc.setTextValue("Mowed Valley");
        assertEquals("Mowed Valley", m_xc.getTextValue());
        assertEquals(true, cs0.hasChanged());
    }

    public void testGetDocChangeStampNotChanged() throws Exception {
        m_xo = XmlObject.Factory.parse(
                  JarUtil.getResourceFromJar(Common.TRANXML_FILE_XMLCURSOR_PO));
        String ns="declare namespace po=\"http://xbean.test/xmlcursor/PurchaseOrder\";";

        m_xc = m_xo.newCursor();
        m_xc.selectPath(ns+" $this//po:city");
        XmlCursor.ChangeStamp cs0 = m_xc.getDocChangeStamp();
        m_xc.toEndDoc();
        assertEquals(false, cs0.hasChanged());
        System.out.println(cs0);
    }
}


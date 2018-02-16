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
public class ClearSelectionTest extends BasicCursorTestCase {
    public ClearSelectionTest(String sName) {
        super(sName);
    }

    public static Test suite() {
        return new TestSuite(ClearSelectionTest.class);
    }

    public void testClearSelection() throws Exception {
        //m_xo = XmlObject.Factory.parse(Common.XML_PURCHASEORDER);
        m_xo = XmlObject.Factory.parse(
                JarUtil.getResourceFromJar(Common.TRANXML_FILE_XMLCURSOR_PO));
        m_xc = m_xo.newCursor();
        String ns="declare namespace po=\"http://xbean.test/xmlcursor/PurchaseOrder\";";
        String exp_ns="xmlns:po=\"http://xbean.test/xmlcursor/PurchaseOrder\"";
        m_xc.selectPath(ns+" $this//po:city");
        m_xc.toNextSelection();
        assertEquals("Mill Valley", m_xc.getTextValue());
        assertEquals("<po:city "+exp_ns+">Mill Valley</po:city>", m_xc.xmlText());
        m_xc.clearSelections();
        assertEquals(false, m_xc.toNextSelection());
        assertEquals("Mill Valley", m_xc.getTextValue());
        assertEquals("<po:city "+exp_ns+">Mill Valley</po:city>", m_xc.xmlText());
    }

    public void testClearSelectionNoSelection() throws Exception {
        m_xo = XmlObject.Factory.parse(
               JarUtil.getResourceFromJar(Common.TRANXML_FILE_XMLCURSOR_PO));
        m_xc = m_xo.newCursor();
        m_xc.clearSelections();
    }

}


/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package org.apache.poi.xssf.usermodel.extensions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.apache.poi.xssf.usermodel.XSSFOddHeader;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTHeaderFooter;

public class TestXSSFHeaderFooter {
    
    private XSSFWorkbook wb;
    private XSSFSheet sheet;
    private XSSFHeaderFooter hO;
    private XSSFHeaderFooter hE;
    private XSSFHeaderFooter hF;
    private XSSFHeaderFooter fO;
    private XSSFHeaderFooter fE;
    private XSSFHeaderFooter fF;
    
    @Before
    public void before() {
        wb = new XSSFWorkbook();
        sheet = wb.createSheet();
        hO = (XSSFHeaderFooter) sheet.getOddHeader();
        hE = (XSSFHeaderFooter) sheet.getEvenHeader();
        hF = (XSSFHeaderFooter) sheet.getFirstHeader();
        fO = (XSSFHeaderFooter) sheet.getOddFooter();
        fE = (XSSFHeaderFooter) sheet.getEvenFooter();
        fF = (XSSFHeaderFooter) sheet.getFirstFooter();
    }
    
    @After
    public void after() throws Exception {
        wb.close();
    }

    @Test
    public void testGetHeaderFooter() {
        CTHeaderFooter ctHf; 
        ctHf = hO.getHeaderFooter();
        assertNotNull(ctHf);
        ctHf = hE.getHeaderFooter();
        assertNotNull(ctHf);
        ctHf = hF.getHeaderFooter();
        assertNotNull(ctHf);
        ctHf = fO.getHeaderFooter();
        assertNotNull(ctHf);
        ctHf = fE.getHeaderFooter();
        assertNotNull(ctHf);
        ctHf = fF.getHeaderFooter();
        assertNotNull(ctHf);
    }

    @Test
    public void testGetValue() {
        assertEquals("", hO.getValue());
        assertEquals("", hE.getValue());
        assertEquals("", hF.getValue());
        assertEquals("", fO.getValue());
        assertEquals("", fE.getValue());
        assertEquals("", fF.getValue());
        hO.setLeft("Left value");
        hO.setCenter("Center value");
        hO.setRight("Right value");
        hE.setLeft("LeftEvalue");
        hE.setCenter("CenterEvalue");
        hE.setRight("RightEvalue");
        hF.setLeft("LeftFvalue");
        hF.setCenter("CenterFvalue");
        hF.setRight("RightFvalue");
        assertEquals("&CCenter value&LLeft value&RRight value", hO.getValue());
        assertEquals("&CCenterEvalue&LLeftEvalue&RRightEvalue", hE.getValue());
        assertEquals("&CCenterFvalue&LLeftFvalue&RRightFvalue", hF.getValue());
        fO.setLeft("Left value1");
        fO.setCenter("Center value1");
        fO.setRight("Right value1");
        fE.setLeft("LeftEvalue1");
        fE.setCenter("CenterEvalue1");
        fE.setRight("RightEvalue1");
        fF.setLeft("LeftFvalue1");
        fF.setCenter("CenterFvalue1");
        fF.setRight("RightFvalue1");
        assertEquals("&CCenter value1&LLeft value1&RRight value1", fO.getValue());
        assertEquals("&CCenterEvalue1&LLeftEvalue1&RRightEvalue1", fE.getValue());
        assertEquals("&CCenterFvalue1&LLeftFvalue1&RRightFvalue1", fF.getValue());
    }

    @Ignore("Test not yet created")
    public void testAreFieldsStripped() {
        fail("Not yet implemented");
    }

    @Ignore("Test not yet created")
    public void testSetAreFieldsStripped() {
        fail("Not yet implemented");
    }

    @Test
    public void testStripFields() {
        String simple = "I am a test header";
        String withPage = "I am a&P test header";
        String withLots = "I&A am&N a&P test&T header&U";
        String withFont = "I&22 am a&\"Arial,bold\" test header";
        String withOtherAnds = "I am a&P test header&&";
        String withOtherAnds2 = "I am a&P test header&a&b";
        
        assertEquals(simple, XSSFOddHeader.stripFields(simple));
        assertEquals(simple, XSSFOddHeader.stripFields(withPage));
        assertEquals(simple, XSSFOddHeader.stripFields(withLots));
        assertEquals(simple, XSSFOddHeader.stripFields(withFont));
        assertEquals(simple + "&&", XSSFOddHeader.stripFields(withOtherAnds));
        assertEquals(simple + "&a&b", XSSFOddHeader.stripFields(withOtherAnds2));
        
        // Now test the default strip flag
        hE.setCenter("Center");
        hE.setLeft("In the left");
    
        assertEquals("In the left", hE.getLeft());
        assertEquals("Center", hE.getCenter());
        assertEquals("", hE.getRight());
        
        hE.setLeft("Top &P&F&D Left");
        assertEquals("Top &P&F&D Left", hE.getLeft());
        assertFalse(hE.areFieldsStripped());
        
        hE.setAreFieldsStripped(true);
        assertEquals("Top  Left", hE.getLeft());
        assertTrue(hE.areFieldsStripped());
        
        // Now even more complex
        hE.setCenter("HEADER TEXT &P&N&D&T&Z&F&F&A&V");
        assertEquals("HEADER TEXT &V", hE.getCenter());
    }

    @Test
    public void testGetCenter() {
        assertEquals("", hO.getCenter());
        assertEquals("", hE.getCenter());
        assertEquals("", hF.getCenter());
        assertEquals("", fO.getCenter());
        assertEquals("", fE.getCenter());
        assertEquals("", fF.getCenter());
        hO.setCenter("Center value");
        hE.setCenter("CenterEvalue");
        hF.setCenter("CenterFvalue");
        assertEquals("Center value", hO.getCenter());
        assertEquals("CenterEvalue", hE.getCenter());
        assertEquals("CenterFvalue", hF.getCenter());
        fO.setCenter("Center value1");
        fE.setCenter("CenterEvalue1");
        fF.setCenter("CenterFvalue1");
        assertEquals("Center value1", fO.getCenter());
        assertEquals("CenterEvalue1", fE.getCenter());
        assertEquals("CenterFvalue1", fF.getCenter());
    }

    @Test
    public void testGetLeft() {
        assertEquals("", hO.getLeft());
        assertEquals("", hE.getLeft());
        assertEquals("", hF.getLeft());
        assertEquals("", fO.getLeft());
        assertEquals("", fE.getLeft());
        assertEquals("", fF.getLeft());
        hO.setLeft("Left value");
        hE.setLeft("LeftEvalue");
        hF.setLeft("LeftFvalue");
        assertEquals("Left value", hO.getLeft());
        assertEquals("LeftEvalue", hE.getLeft());
        assertEquals("LeftFvalue", hF.getLeft());
        fO.setLeft("Left value1");
        fE.setLeft("LeftEvalue1");
        fF.setLeft("LeftFvalue1");
        assertEquals("Left value1", fO.getLeft());
        assertEquals("LeftEvalue1", fE.getLeft());
        assertEquals("LeftFvalue1", fF.getLeft());
    }

    @Test
    public void testGetRight() {
        assertEquals("", hO.getValue());
        assertEquals("", hE.getValue());
        assertEquals("", hF.getValue());
        assertEquals("", fO.getValue());
        assertEquals("", fE.getValue());
        assertEquals("", fF.getValue());
        hO.setRight("Right value");
        hE.setRight("RightEvalue");
        hF.setRight("RightFvalue");
        assertEquals("Right value", hO.getRight());
        assertEquals("RightEvalue", hE.getRight());
        assertEquals("RightFvalue", hF.getRight());
        fO.setRight("Right value1");
        fE.setRight("RightEvalue1");
        fF.setRight("RightFvalue1");
        assertEquals("Right value1", fO.getRight());
        assertEquals("RightEvalue1", fE.getRight());
        assertEquals("RightFvalue1", fF.getRight());
    }

    @Test
    public void testSetCenter() {
        assertEquals("", hO.getValue());
        assertEquals("", hE.getValue());
        assertEquals("", hF.getValue());
        assertEquals("", fO.getValue());
        assertEquals("", fE.getValue());
        assertEquals("", fF.getValue());
        hO.setCenter("Center value");
        hE.setCenter("CenterEvalue");
        hF.setCenter("CenterFvalue");
        assertEquals("&CCenter value", hO.getValue());
        assertEquals("&CCenterEvalue", hE.getValue());
        assertEquals("&CCenterFvalue", hF.getValue());
        fO.setCenter("Center value1");
        fE.setCenter("CenterEvalue1");
        fF.setCenter("CenterFvalue1");
        assertEquals("&CCenter value1", fO.getValue());
        assertEquals("&CCenterEvalue1", fE.getValue());
        assertEquals("&CCenterFvalue1", fF.getValue());
    }

    @Test
    public void testSetLeft() {
        assertEquals("", hO.getValue());
        assertEquals("", hE.getValue());
        assertEquals("", hF.getValue());
        assertEquals("", fO.getValue());
        assertEquals("", fE.getValue());
        assertEquals("", fF.getValue());
        hO.setLeft("Left value");
        hE.setLeft("LeftEvalue");
        hF.setLeft("LeftFvalue");
        assertEquals("&LLeft value", hO.getValue());
        assertEquals("&LLeftEvalue", hE.getValue());
        assertEquals("&LLeftFvalue", hF.getValue());
        fO.setLeft("Left value1");
        fE.setLeft("LeftEvalue1");
        fF.setLeft("LeftFvalue1");
        assertEquals("&LLeft value1", fO.getValue());
        assertEquals("&LLeftEvalue1", fE.getValue());
        assertEquals("&LLeftFvalue1", fF.getValue());
    }

    @Test
    public void testSetRight() {
        assertEquals("", hO.getValue());
        assertEquals("", hE.getValue());
        assertEquals("", hF.getValue());
        assertEquals("", fO.getValue());
        assertEquals("", fE.getValue());
        assertEquals("", fF.getValue());
        hO.setRight("Right value");
        hE.setRight("RightEvalue");
        hF.setRight("RightFvalue");
        assertEquals("&RRight value", hO.getValue());
        assertEquals("&RRightEvalue", hE.getValue());
        assertEquals("&RRightFvalue", hF.getValue());
        fO.setRight("Right value1");
        fE.setRight("RightEvalue1");
        fF.setRight("RightFvalue1");
        assertEquals("&RRight value1", fO.getValue());
        assertEquals("&RRightEvalue1", fE.getValue());
        assertEquals("&RRightFvalue1", fF.getValue());
    }
    
 

    @Test
    public void testGetSetCenterLeftRight() {
        
        assertEquals("", fO.getCenter());
        fO.setCenter("My first center section");
        assertEquals("My first center section", fO.getCenter());
        fO.setCenter("No, let's update the center section");
        assertEquals("No, let's update the center section", fO.getCenter());
        fO.setLeft("And add a left one");
        fO.setRight("Finally the right section is added");
        assertEquals("And add a left one", fO.getLeft());
        assertEquals("Finally the right section is added", fO.getRight());
        
        // Test changing the three sections value
        fO.setCenter("Second center version");
        fO.setLeft("Second left version");
        fO.setRight("Second right version");
        assertEquals("Second center version", fO.getCenter());
        assertEquals("Second left version", fO.getLeft());
        assertEquals("Second right version", fO.getRight());
        
    }    
}

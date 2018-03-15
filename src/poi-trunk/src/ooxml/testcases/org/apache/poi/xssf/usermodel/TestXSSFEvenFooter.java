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

package org.apache.poi.xssf.usermodel;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestXSSFEvenFooter {
    
    private XSSFWorkbook wb;
    private XSSFSheet sheet;
    
    @Before
    public void before() {
        wb = new XSSFWorkbook();
        sheet = wb.createSheet();
    }
    
    @After
    public void after() throws Exception {
        wb.close();
    }

    @Test
    public void testSetGetText() {
        XSSFEvenFooter footer = (XSSFEvenFooter) sheet.getEvenFooter();
        assertNotNull(footer);
        assertNull(footer.getText());
        footer.setText("this is a test");
        assertEquals("this is a test", footer.getText());
    }
}
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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TestXSSFHeaderFooterProperties {

    private XSSFWorkbook wb;
    private XSSFSheet sheet;
    private XSSFHeaderFooterProperties hfProp;

    @BeforeEach
    void before() {
        wb = new XSSFWorkbook();
        sheet = wb.createSheet();
        hfProp = sheet.getHeaderFooterProperties();
    }

    @AfterEach
    void after() throws Exception {
        wb.close();
    }

    @Test
    void testGetAlignWithMargins() {
        assertFalse(hfProp.getAlignWithMargins());
        hfProp.setAlignWithMargins(true);
        assertTrue(hfProp.getAlignWithMargins());
    }

    @Test
    void testGetDifferentFirst() {
        assertFalse(hfProp.getDifferentFirst());
        hfProp.setDifferentFirst(true);
        assertTrue(hfProp.getDifferentFirst());
        hfProp.setDifferentFirst(false);
        assertFalse(hfProp.getDifferentFirst());
    }

    @Test
    void testGetDifferentOddEven() {
        assertFalse(hfProp.getDifferentOddEven());
        hfProp.setDifferentOddEven(true);
        assertTrue(hfProp.getDifferentOddEven());
        hfProp.setDifferentOddEven(false);
        assertFalse(hfProp.getDifferentOddEven());
    }

    @Test
    void testGetScaleWithDoc() {
        assertFalse(hfProp.getScaleWithDoc());
        hfProp.setScaleWithDoc(true);
        assertTrue(hfProp.getScaleWithDoc());
    }

    @Test
    void testRemoveAlignWithMargins() {
        hfProp.setAlignWithMargins(true);
        assertTrue(hfProp.getHeaderFooter().isSetAlignWithMargins());
        hfProp.removeAlignWithMargins();
        assertFalse(hfProp.getHeaderFooter().isSetAlignWithMargins());
    }

    @Test
    void testRemoveDifferentFirst() {
        hfProp.setDifferentFirst(true);
        assertTrue(hfProp.getHeaderFooter().isSetDifferentFirst());
        hfProp.removeDifferentFirst();
        assertFalse(hfProp.getHeaderFooter().isSetDifferentFirst());
    }

    @Test
    void testRemoveDifferentOddEven() {
        hfProp.setDifferentOddEven(true);
        assertTrue(hfProp.getHeaderFooter().isSetDifferentOddEven());
        hfProp.removeDifferentOddEven();
        assertFalse(hfProp.getHeaderFooter().isSetDifferentOddEven());
    }

    @Test
    void testRemoveScaleWithDoc() {
        hfProp.setScaleWithDoc(true);
        assertTrue(hfProp.getHeaderFooter().isSetScaleWithDoc());
        hfProp.removeScaleWithDoc();
        assertFalse(hfProp.getHeaderFooter().isSetScaleWithDoc());
    }

}

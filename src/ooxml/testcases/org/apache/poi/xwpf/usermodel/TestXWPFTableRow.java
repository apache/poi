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

package org.apache.poi.xwpf.usermodel;

import junit.framework.TestCase;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTbl;

public class TestXWPFTableRow extends TestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testCreateRow() throws Exception {
        CTRow ctRow = CTRow.Factory.newInstance();
        assertNotNull(ctRow);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testSetGetCantSplitRow() {
        // create a table
        XWPFDocument doc = new XWPFDocument();
        CTTbl ctTable = CTTbl.Factory.newInstance();
        XWPFTable table = new XWPFTable(ctTable, doc);
        // table has a single row by default; grab it
        XWPFTableRow tr = table.getRow(0);
        assertNotNull(tr);

        tr.setCantSplitRow(true);
        boolean isCant = tr.isCantSplitRow();
        assert (isCant);
    }

    public void testSetGetRepeatHeader() {
        // create a table
        XWPFDocument doc = new XWPFDocument();
        CTTbl ctTable = CTTbl.Factory.newInstance();
        XWPFTable table = new XWPFTable(ctTable, doc);
        // table has a single row by default; grab it
        XWPFTableRow tr = table.getRow(0);
        assertNotNull(tr);

        tr.setRepeatHeader(true);
        boolean isRpt = tr.isRepeatHeader();
        assert (isRpt);
    }
}

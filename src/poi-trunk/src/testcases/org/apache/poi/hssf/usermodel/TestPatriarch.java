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

package org.apache.poi.hssf.usermodel;

import junit.framework.TestCase;
import org.apache.poi.ddf.EscherDgRecord;
import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.record.EscherAggregate;

/**
 * @author Evgeniy Berlog
 * @date 01.08.12
 */
public class TestPatriarch extends TestCase {

    public void testGetPatriarch(){
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sh = wb.createSheet();
        assertNull(sh.getDrawingPatriarch());

        HSSFPatriarch patriarch = sh.createDrawingPatriarch();
        assertNotNull(patriarch);
        patriarch.createSimpleShape(new HSSFClientAnchor());
        patriarch.createSimpleShape(new HSSFClientAnchor());

        assertSame(patriarch, sh.getDrawingPatriarch());

        EscherAggregate agg = patriarch.getBoundAggregate();

        EscherDgRecord dg = agg.getEscherContainer().getChildById(EscherDgRecord.RECORD_ID);
        int lastId = dg.getLastMSOSPID();
        
        assertSame(patriarch, sh.createDrawingPatriarch());
        
        wb = HSSFTestDataSamples.writeOutAndReadBack(wb);
        sh = wb.getSheetAt(0);
        patriarch = sh.createDrawingPatriarch();
        dg = patriarch.getBoundAggregate().getEscherContainer().getChildById(EscherDgRecord.RECORD_ID);

        assertEquals(lastId, dg.getLastMSOSPID());
    }
}

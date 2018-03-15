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

package org.apache.poi.xssf.binary;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.apache.poi.POIDataSamples;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.xssf.eventusermodel.XSSFBReader;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.junit.Test;

public class TestXSSFBSheetHyperlinkManager {

    private static POIDataSamples _ssTests = POIDataSamples.getSpreadSheetInstance();

    @Test
    public void testBasic() throws Exception {

        OPCPackage pkg = OPCPackage.open(_ssTests.openResourceAsStream("hyperlink.xlsb"));
        XSSFBReader reader = new XSSFBReader(pkg);
        XSSFReader.SheetIterator it = (XSSFReader.SheetIterator) reader.getSheetsData();
        it.next();
        XSSFBHyperlinksTable manager = new XSSFBHyperlinksTable(it.getSheetPart());
        List<XSSFHyperlinkRecord> records = manager.getHyperLinks().get(new CellAddress(0, 0));
        assertNotNull(records);
        assertEquals(1, records.size());
        XSSFHyperlinkRecord record = records.get(0);
        assertEquals("http://tika.apache.org/", record.getLocation());
        assertEquals("rId2", record.getRelId());

    }


}

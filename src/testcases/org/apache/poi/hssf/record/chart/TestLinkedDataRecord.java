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

package org.apache.poi.hssf.record.chart;


import junit.framework.TestCase;

import org.apache.poi.hssf.record.TestcaseRecordInputStream;
import org.apache.poi.hssf.record.formula.Area3DPtg;
import org.apache.poi.hssf.record.formula.Ptg;

/**
 * Tests the serialization and deserialization of the LinkedDataRecord
 * class works correctly.  Test data taken directly from a real
 * Excel file.
 *
 * @author Glen Stampoultzis (glens at apache.org)
 */
public final class TestLinkedDataRecord extends TestCase {

/*
    The records below are records that would appear in a simple bar chart

    The first record links to the series title (linkType = 0).   It's
    reference type is 1 which means that it links directly to data entered
    into the forumula bar.  There seems to be no reference to any data
    however.  The formulaOfLink field contains two 0 bytes.  This probably
    means that there is no particular heading set.


============================================
Offset 0xf9c (3996)
rectype = 0x1051, recsize = 0x8
-BEGIN DUMP---------------------------------
00000000 00 01 00 00 00 00 00 00                         ........
-END DUMP-----------------------------------
recordid = 0x1051, size =8
[AI]
.linkType             = 0x00 (0 )
.referenceType        = 0x01 (1 )
.options              = 0x0000 (0 )
    .customNumberFormat       = false
.indexNumberFmtRecord = 0x0000 (0 )
.formulaOfLink        =  (org.apache.poi.hssf.record.LinkedDataFormulaField@95fd19 )
[/AI]


    The second record links to the series data (linkType=1).  The
    referenceType = 2 which means it's linked to the worksheet.
    It links using a formula.  The formula value is
    0B 00 3B 00 00 00 00 1E 00 01 00 01 00.

    0B 00   11 bytes length
    3B (tArea3d) Rectangular area
        00 00 index to REF entry in extern sheet
        00 00 index to first row
        1E 00 index to last row
        01 00 index to first column and relative flags
        01 00 index to last column and relative flags

============================================
Offset 0xfa8 (4008)
rectype = 0x1051, recsize = 0x13
-BEGIN DUMP---------------------------------
00000000 01 02 00 00 00 00 0B 00 3B 00 00 00 00 1E 00 01 ........;.......
00000010 00 01 00                                        ...
-END DUMP-----------------------------------
recordid = 0x1051, size =19
[AI]
.linkType             = 0x01 (1 )
.referenceType        = 0x02 (2 )
.options              = 0x0000 (0 )
    .customNumberFormat       = false
.indexNumberFmtRecord = 0x0000 (0 )
.formulaOfLink        =  (org.apache.poi.hssf.record.LinkedDataFormulaField@11b9fb1 )
[/AI]

    The third record links to the series categories (linkType=2).  The
    reference type of 2 means that it's linked to the worksheet.
    It links using a formula.  The formula value is
    0B 00 3B 00 00 00 00 1E 00 01 00 01 00

    0B 00   11 bytes in length
        3B (tArea3d) Rectangular area
        00 00 index to REF entry in extern sheet
        00 00  index to first row
        00 1F  index to last row
        00 00 index to first column and relative flags
        00 00 index to last column and relative flags


============================================
Offset 0xfbf (4031)
rectype = 0x1051, recsize = 0x13
-BEGIN DUMP---------------------------------
00000000 02 02 00 00 69 01 0B 00 3B 00 00 00 00 1F 00 00 ....i...;.......
00000010 00 00 00                                        ...
-END DUMP-----------------------------------
recordid = 0x1051, size =19
[AI]
.linkType             = 0x02 (2 )
.referenceType        = 0x02 (2 )
.options              = 0x0000 (0 )
    .customNumberFormat       = false
.indexNumberFmtRecord = 0x0169 (361 )
.formulaOfLink        =  (org.apache.poi.hssf.record.LinkedDataFormulaField@913fe2 )
[/AI]

This third link type does not seem to be documented and does not appear to
contain any useful information anyway.

============================================
Offset 0xfd6 (4054)
rectype = 0x1051, recsize = 0x8
-BEGIN DUMP---------------------------------
00000000 03 01 00 00 00 00 00 00                         ........
-END DUMP-----------------------------------
recordid = 0x1051, size =8
[AI]
.linkType             = 0x03 (3 )
.referenceType        = 0x01 (1 )
.options              = 0x0000 (0 )
    .customNumberFormat       = false
.indexNumberFmtRecord = 0x0000 (0 )
.formulaOfLink        =  (org.apache.poi.hssf.record.LinkedDataFormulaField@1f934ad )
[/AI]

*/

    byte[] data = new byte[]{
        (byte)0x01,                 // link type
        (byte)0x02,                 // reference type
        (byte)0x00,(byte)0x00,      // options
        (byte)0x00,(byte)0x00,      // index number format record
        (byte)0x0B,(byte)0x00,      // 11 bytes length
        (byte)0x3B,                 // formula of link
        (byte)0x00,(byte)0x00,          // index to ref entry in extern sheet
        (byte)0x00,(byte)0x00,          // index to first row
        (byte)0x00,(byte)0x1F,          // index to last row
        (byte)0x00,(byte)0x00,          // index to first column and relative flags
        (byte)0x00,(byte)0x00,          // index to last column and relative flags
    };

    public void testLoad() {

        LinkedDataRecord record = new LinkedDataRecord(TestcaseRecordInputStream.create(0x1051, data));
        assertEquals( LinkedDataRecord.LINK_TYPE_VALUES, record.getLinkType());
        assertEquals( LinkedDataRecord.REFERENCE_TYPE_WORKSHEET, record.getReferenceType());
        assertEquals( 0, record.getOptions());
        assertEquals( false, record.isCustomNumberFormat() );
        assertEquals( 0, record.getIndexNumberFmtRecord());

        Area3DPtg ptgExpected = new Area3DPtg(0, 7936, 0, 0,
                false, false, false, false, 0);
        
        Ptg ptgActual = record.getFormulaOfLink()[0];
        assertEquals(ptgExpected.toString(),  ptgActual.toString());

        assertEquals( data.length + 4, record.getRecordSize() );
    }

    public void testStore() {
        LinkedDataRecord record = new LinkedDataRecord();
        record.setLinkType( LinkedDataRecord.LINK_TYPE_VALUES );
        record.setReferenceType( LinkedDataRecord.REFERENCE_TYPE_WORKSHEET );
        record.setOptions( (short)0 );
        record.setCustomNumberFormat( false );
        record.setIndexNumberFmtRecord( (short)0 );
        Area3DPtg ptg = new Area3DPtg(0, 7936, 0, 0,
        		false, false, false, false, 0);
        record.setFormulaOfLink(new Ptg[] { ptg, } );

        byte [] recordBytes = record.serialize();
        assertEquals(recordBytes.length - 4, data.length);
        for (int i = 0; i < data.length; i++)
            assertEquals("At offset " + i, data[i], recordBytes[i+4]);
    }
}

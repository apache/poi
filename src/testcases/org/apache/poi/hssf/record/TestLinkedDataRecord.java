
/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache POI" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache POI", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */


package org.apache.poi.hssf.record;


import junit.framework.TestCase;

/**
 * Tests the serialization and deserialization of the LinkedDataRecord
 * class works correctly.  Test data taken directly from a real
 * Excel file.
 *
 * @author Glen Stampoultzis (glens at apache.org)
 */
public class TestLinkedDataRecord
        extends TestCase
{

/*
    Records that would appear in a simple bar chart

    The first record links to the series title (linkType = 0).   It's
    reference type is 1 which means that it links directly to data entered
    into the forumula bar.  There seems to be no reference to any data
    however.  The formulaOfLink field contains two 0 bytes.

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
        (byte) 0x00,
        (byte) 0x01,
        (byte) 0x00, (byte) 0x00,
        (byte)0x00,(byte)0x00,
        (byte)0x00,(byte)0x00          // not supported
    };

    public TestLinkedDataRecord(String name)
    {
        super(name);
    }

    public void testLoad()
            throws Exception
    {

        LinkedDataRecord record = new LinkedDataRecord((short)0x1051, (short)data.length, data);
        assertEquals( LinkedDataRecord.LINK_TYPE_TITLE_OR_TEXT, record.getLinkType());
        assertEquals( LinkedDataRecord.REFERENCE_TYPE_DIRECT, record.getReferenceType());
        assertEquals( 0, record.getOptions());
        assertEquals( false, record.isCustomNumberFormat() );
        assertEquals( 0, record.getIndexNumberFmtRecord());


        assertEquals( 12, record.getRecordSize() );

        record.validateSid((short)0x1051);

    }

    public void testStore()
    {
        LinkedDataRecord record = new LinkedDataRecord();
        record.setLinkType( LinkedDataRecord.LINK_TYPE_TITLE_OR_TEXT );
        record.setReferenceType( LinkedDataRecord.REFERENCE_TYPE_DIRECT );
        record.setOptions( (short)0 );
        record.setCustomNumberFormat( false );
        record.setIndexNumberFmtRecord( (short)0 );


        byte [] recordBytes = record.serialize();
        assertEquals(recordBytes.length - 4, data.length);
        for (int i = 0; i < data.length; i++)
            assertEquals("At offset " + i, data[i], recordBytes[i+4]);
    }
}

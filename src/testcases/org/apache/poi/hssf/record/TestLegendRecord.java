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
 * Tests the serialization and deserialization of the LegendRecord
 * class works correctly.  Test data taken directly from a real
 * Excel file.
 *
 * @author Glen Stampoultzis (glens at apache.org)
 */
public class TestLegendRecord
        extends TestCase
{
    byte[] data = new byte[] {
        (byte)0xB2,(byte)0x0D,(byte)0x00,(byte)0x00,  //field_1_xPosition
        (byte)0x39,(byte)0x06,(byte)0x00,(byte)0x00,  //field_2_yPosition
        (byte)0xD9,(byte)0x01,(byte)0x00,(byte)0x00,  //field_3_xSize
        (byte)0x34,(byte)0x02,(byte)0x00,(byte)0x00,  //field_4_ySize
        (byte)0x03,                                   //field_5_type
        (byte)0x01,                                   //field_6_spacing
        (byte)0x1F,(byte)0x00                         //field_7_options
    };

    public TestLegendRecord(String name)
    {
        super(name);
    }

    public void testLoad()
            throws Exception
    {

        LegendRecord legendRecord = new LegendRecord((short)0x1015, (short)data.length, data);
        assertEquals(3506, legendRecord.getXPosition());
        assertEquals(1593, legendRecord.getYPosition());
        assertEquals(473, legendRecord.getXSize());
        assertEquals(564, legendRecord.getYSize());
        assertEquals(LegendRecord.TYPE_RIGHT, legendRecord.getType());
        assertEquals(LegendRecord.SPACING_MEDIUM, legendRecord.getSpacing());
        assertEquals(31, legendRecord.getOptions());
        assertEquals(true, legendRecord.isAutoPosition());
        assertEquals(true, legendRecord.isAutoSeries());
        assertEquals(true, legendRecord.isAutoPosX());
        assertEquals(true, legendRecord.isAutoPosY());
        assertEquals(true, legendRecord.isVert());
        assertEquals(false, legendRecord.isContainsDataTable());

        assertEquals(24, legendRecord.getRecordSize());

        legendRecord.validateSid((short)0x1015);
    }

    public void testStore()
    {
        LegendRecord legendRecord = new LegendRecord();
        legendRecord.setXPosition(3506);
        legendRecord.setYPosition(1593);
        legendRecord.setXSize(473);
        legendRecord.setYSize(564);
        legendRecord.setType(LegendRecord.TYPE_RIGHT);
        legendRecord.setSpacing(LegendRecord.SPACING_MEDIUM);
        legendRecord.setAutoPosition(true);
        legendRecord.setAutoSeries(true);
        legendRecord.setAutoPosX(true);
        legendRecord.setAutoPosY(true);
        legendRecord.setVert(true);
        legendRecord.setContainsDataTable(false);

        byte [] recordBytes = legendRecord.serialize();
        assertEquals(recordBytes.length - 4, data.length);
        for (int i = 0; i < data.length; i++)
            assertEquals("At offset " + i, data[i], recordBytes[i+4]);
    }
}

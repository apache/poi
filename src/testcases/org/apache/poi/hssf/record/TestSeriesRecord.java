
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
 * Tests the serialization and deserialization of the SeriesRecord
 * class works correctly.  Test data taken directly from a real
 * Excel file.
 *
 * @author Glen Stampoultzis (glens at apache.org)
 */
public class TestSeriesRecord
        extends TestCase
{
    byte[] data = new byte[] {
        (byte)0x01,(byte)0x00,      // category data type
        (byte)0x01,(byte)0x00,      // values data type
        (byte)0x1B,(byte)0x00,      // num categories
        (byte)0x1B,(byte)0x00,      // num values
        (byte)0x01,(byte)0x00,      // bubble series type
        (byte)0x00,(byte)0x00       // num bubble values
    };

    public TestSeriesRecord(String name)
    {
        super(name);
    }

    public void testLoad()
            throws Exception
    {

        SeriesRecord record = new SeriesRecord((short)0x1003, (short)data.length, data);
        assertEquals( SeriesRecord.CATEGORY_DATA_TYPE_NUMERIC, record.getCategoryDataType());
        assertEquals( SeriesRecord.VALUES_DATA_TYPE_NUMERIC, record.getValuesDataType());
        assertEquals( 27, record.getNumCategories());
        assertEquals( 27, record.getNumValues());
        assertEquals( SeriesRecord.BUBBLE_SERIES_TYPE_NUMERIC, record.getBubbleSeriesType());
        assertEquals( 0, record.getNumBubbleValues());


        assertEquals( 16, record.getRecordSize() );

        record.validateSid((short)0x1003);
    }

    public void testStore()
    {
        SeriesRecord record = new SeriesRecord();
        record.setCategoryDataType( SeriesRecord.CATEGORY_DATA_TYPE_NUMERIC );
        record.setValuesDataType( SeriesRecord.VALUES_DATA_TYPE_NUMERIC );
        record.setNumCategories( (short)27 );
        record.setNumValues( (short)27 );
        record.setBubbleSeriesType( SeriesRecord.BUBBLE_SERIES_TYPE_NUMERIC );
        record.setNumBubbleValues( (short)0 );

        byte [] recordBytes = record.serialize();
        assertEquals(recordBytes.length - 4, data.length);
        for (int i = 0; i < data.length; i++)
            assertEquals("At offset " + i, data[i], recordBytes[i+4]);
    }
}


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
 * Tests the serialization and deserialization of the ValueRangeRecord
 * class works correctly.  Test data taken directly from a real
 * Excel file.
 *
 *
 * @author Glen Stampoultzis (glens at apache.org)
 */
public class TestValueRangeRecord
        extends TestCase
{
    byte[] data = new byte[] {
        (byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,     // min axis value
        (byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,     // max axis value
        (byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,     // major increment
        (byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,     // minor increment
        (byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,     // cross over
        (byte)0x1F,(byte)0x01                                    // options
    };

    public TestValueRangeRecord(String name)
    {
        super(name);
    }

    public void testLoad()
            throws Exception
    {

        ValueRangeRecord record = new ValueRangeRecord((short)0x101f, (short)data.length, data);
        assertEquals( 0.0, record.getMinimumAxisValue(), 0.001);
        assertEquals( 0.0, record.getMaximumAxisValue(), 0.001);
        assertEquals( 0.0, record.getMajorIncrement(), 0.001);
        assertEquals( 0.0, record.getMinorIncrement(), 0.001);
        assertEquals( 0.0, record.getCategoryAxisCross(), 0.001);
        assertEquals( 0x011f, record.getOptions());
        assertEquals( true, record.isAutomaticMinimum() );
        assertEquals( true, record.isAutomaticMaximum() );
        assertEquals( true, record.isAutomaticMajor() );
        assertEquals( true, record.isAutomaticMinor() );
        assertEquals( true, record.isAutomaticCategoryCrossing() );
        assertEquals( false, record.isLogarithmicScale() );
        assertEquals( false, record.isValuesInReverse() );
        assertEquals( false, record.isCrossCategoryAxisAtMaximum() );
        assertEquals( true, record.isReserved() );

        assertEquals( 42+4, record.getRecordSize() );

        record.validateSid((short)0x101f);
    }

    public void testStore()
    {
        ValueRangeRecord record = new ValueRangeRecord();
        record.setMinimumAxisValue( 0 );
        record.setMaximumAxisValue( 0 );
        record.setMajorIncrement( 0 );
        record.setMinorIncrement( 0 );
        record.setCategoryAxisCross( 0 );
        record.setAutomaticMinimum( true );
        record.setAutomaticMaximum( true );
        record.setAutomaticMajor( true );
        record.setAutomaticMinor( true );
        record.setAutomaticCategoryCrossing( true );
        record.setLogarithmicScale( false );
        record.setValuesInReverse( false );
        record.setCrossCategoryAxisAtMaximum( false );
        record.setReserved( true );

        byte [] recordBytes = record.serialize();
        assertEquals(recordBytes.length - 4, data.length);
        for (int i = 0; i < data.length; i++)
            assertEquals("At offset " + i, data[i], recordBytes[i+4]);
    }
}

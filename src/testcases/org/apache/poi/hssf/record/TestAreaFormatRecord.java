
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
 * Tests the serialization and deserialization of the AreaFormatRecord
 * class works correctly.  Test data taken directly from a real
 * Excel file.
 *

 * @author Glen Stampoultzis (glens at apache.org)
 */
public class TestAreaFormatRecord
        extends TestCase
{
    byte[] data = new byte[] {
        (byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0x00,    // forecolor
        (byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,    // backcolor
        (byte)0x01,(byte)0x00,                          // pattern
        (byte)0x01,(byte)0x00,                          // format
        (byte)0x4E,(byte)0x00,                          // forecolor index
        (byte)0x4D,(byte)0x00                           // backcolor index

    };

    public TestAreaFormatRecord(String name)
    {
        super(name);
    }

    public void testLoad()
            throws Exception
    {

        AreaFormatRecord record = new AreaFormatRecord((short)0x100a, (short)data.length, data);
        assertEquals( 0xFFFFFF, record.getForegroundColor());
        assertEquals( 0x000000, record.getBackgroundColor());
        assertEquals( 1, record.getPattern());
        assertEquals( 1, record.getFormatFlags());
        assertEquals( true, record.isAutomatic() );
        assertEquals( false, record.isInvert() );
        assertEquals( 0x4e, record.getForecolorIndex());
        assertEquals( 0x4d, record.getBackcolorIndex());


        assertEquals( 20, record.getRecordSize() );

        record.validateSid((short)0x100a);
    }

    public void testStore()
    {
        AreaFormatRecord record = new AreaFormatRecord();
        record.setForegroundColor( 0xFFFFFF );
        record.setBackgroundColor( 0x000000 );
        record.setPattern( (short)1 );
        record.setAutomatic( true );
        record.setInvert( false );
        record.setForecolorIndex( (short)0x4e );
        record.setBackcolorIndex( (short)0x4d );


        byte [] recordBytes = record.serialize();
        assertEquals(recordBytes.length - 4, data.length);
        for (int i = 0; i < data.length; i++)
            assertEquals("At offset " + i, data[i], recordBytes[i+4]);
    }
}

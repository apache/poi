
/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
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
 * Tests the serialization and deserialization of the CommonObjectDataSubRecord
 * class works correctly.  Test data taken directly from a real
 * Excel file.
 *

 * @author Glen Stampoultzis (glens at apache.org)
 */
public class TestCommonObjectDataSubRecord
        extends TestCase
{
    byte[] data = new byte[] {
        (byte)0x12,(byte)0x00,(byte)0x01,(byte)0x00,
        (byte)0x01,(byte)0x00,(byte)0x11,(byte)0x60,
        (byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,
        (byte)0x00,(byte)0x0D,(byte)0x26,(byte)0x01,
        (byte)0x00,(byte)0x00,
    };

    public TestCommonObjectDataSubRecord(String name)
    {
        super(name);
    }

    public void testLoad()
            throws Exception
    {
        CommonObjectDataSubRecord record = new CommonObjectDataSubRecord((short)0x15, (short)data.length, data);
        

        assertEquals( CommonObjectDataSubRecord.OBJECT_TYPE_LIST_BOX, record.getObjectType());
        assertEquals( (short)1, record.getObjectId());
        assertEquals( (short)1, record.getOption());
        assertEquals( true , record.isLocked() );
        assertEquals( false, record.isPrintable() );
        assertEquals( false, record.isAutofill() );
        assertEquals( false, record.isAutoline() );
        assertEquals( (int)24593, record.getReserved1());
        assertEquals( (int)218103808, record.getReserved2());
        assertEquals( (int)294, record.getReserved3());
        assertEquals( 22 , record.getRecordSize() );

        record.validateSid((short)0x15);
    }

    public void testStore()
    {
        CommonObjectDataSubRecord record = new CommonObjectDataSubRecord();

        record.setObjectType( CommonObjectDataSubRecord.OBJECT_TYPE_LIST_BOX );
        record.setObjectId( (short) 1);
        record.setOption( (short) 1);
        record.setLocked( true );
        record.setPrintable( false );
        record.setAutofill( false );
        record.setAutoline( false );
        record.setReserved1( (int) 24593);
        record.setReserved2( (int) 218103808);
        record.setReserved3( (int) 294);

        byte [] recordBytes = record.serialize();
        assertEquals(recordBytes.length - 4, data.length);
        for (int i = 0; i < data.length; i++)
            assertEquals("At offset " + i, data[i], recordBytes[i+4]);
    }
}


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
 * Tests the serialization and deserialization of the TickRecord
 * class works correctly.  Test data taken directly from a real
 * Excel file.
 *

 * @author Andrew C. Oliver(acoliver at apache.org)
 */
public class TestTickRecord
        extends TestCase
{
    byte[] data = new byte[] {
	(byte)0x02, (byte)0x00, (byte)0x03, (byte)0x01, 
        (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
        (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, 
	(byte)0x00, (byte)0x00, (byte)0x00,
	(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, 
	(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x23, (byte)0x00, 
	(byte)0x4D, (byte)0x00, (byte)0x00, (byte)0x00
    };

    public TestTickRecord(String name)
    {
        super(name);
    }

    public void testLoad()
            throws Exception
    {
        TickRecord record = new TickRecord((short)0x101e, (short)data.length, data);
        assertEquals( (byte)2, record.getMajorTickType());
        assertEquals( (byte)0, record.getMinorTickType());
        assertEquals( (byte)3, record.getLabelPosition());
        assertEquals( (short)1, record.getBackground());
        assertEquals( 0, record.getLabelColorRgb());
        assertEquals( (short)0, record.getZero1());
        assertEquals( (short)0, record.getZero2());
        assertEquals( (short)35, record.getOptions());
        assertEquals( true, record.isAutoTextColor() );
        assertEquals( true, record.isAutoTextBackground() );
        assertEquals( (short)0x0, record.getRotation() );
        assertEquals( true, record.isAutorotate() );
        assertEquals( (short)77, record.getTickColor());
        assertEquals( (short)0x0, record.getZero3());


        assertEquals( 34, record.getRecordSize() );

        record.validateSid((short)0x101e);
    }

    public void testStore()
    {
        TickRecord record = new TickRecord();
        record.setMajorTickType( (byte)2 );
        record.setMinorTickType( (byte)0 );
        record.setLabelPosition( (byte)3 );
        record.setBackground( (byte)1 );
        record.setLabelColorRgb( 0 );
        record.setZero1( (short)0 );
        record.setZero2( (short)0 );
        record.setOptions( (short)35 );
        record.setAutoTextColor( true );
        record.setAutoTextBackground( true );
        record.setRotation( (short)0 );
        record.setAutorotate( true );
        record.setTickColor( (short)77 );
        record.setZero3( (short)0 );


        byte [] recordBytes = record.serialize();
        assertEquals(recordBytes.length - 4, data.length);
        for (int i = 0; i < data.length; i++)
            assertEquals("At offset " + i, data[i], recordBytes[i+4]);
    }
    
    
    /**
     *  The main program for the TestTickRecord class
     *
     *@param  args  The command line arguments
     */
    public static void main(String[] args) {
        System.out.println("Testing org.apache.poi.hssf.record.TickRecord");
        junit.textui.TestRunner.run(TestTickRecord.class);
    }
    
}

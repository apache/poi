
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
 * Tests the record factory
 * @author Glen Stampoultzis (glens at apache.org)
 * @author Andrew C. Oliver (acoliver at apache dot org)
 */

public class TestRecordFactory
    extends TestCase
{

    /**
     * Creates new TestRecordFactory
     * @param testCaseName
     */

    public TestRecordFactory(String testCaseName)
    {
        super(testCaseName);
    }

    /**
     * TEST NAME:  Test Basic Record Construction <P>
     * OBJECTIVE:  Test that the RecordFactory given the required parameters for know
     *             record types can construct the proper record w/values.<P>
     * SUCCESS:    Record factory creates the records with the expected values.<P>
     * FAILURE:    The wrong records are creates or contain the wrong values <P>
     *
     */

    public void testBasicRecordConstruction()
        throws Exception
    {
        short    recType = BOFRecord.sid;
        byte[]   data    = new byte[]
        {
            0, 6, 5, 0, -2, 28, -51, 7, -55, 64, 0, 0, 6, 1, 0, 0
        };
        short    size    = 16;
        Record[] record  = RecordFactory.createRecord(recType, size, data);

        assertEquals(BOFRecord.class.getName(),
                     record[ 0 ].getClass().getName());
        BOFRecord bofRecord = ( BOFRecord ) record[ 0 ];

        assertEquals(7422, bofRecord.getBuild());
        assertEquals(1997, bofRecord.getBuildYear());
        assertEquals(16585, bofRecord.getHistoryBitMask());
        assertEquals(20, bofRecord.getRecordSize());
        assertEquals(262, bofRecord.getRequiredVersion());
        assertEquals(2057, bofRecord.getSid());
        assertEquals(5, bofRecord.getType());
        assertEquals(1536, bofRecord.getVersion());
        recType = MMSRecord.sid;
        size    = 2;
        data    = new byte[]
        {
            0, 0
        };
        record  = RecordFactory.createRecord(recType, size, data);
        assertEquals(MMSRecord.class.getName(),
                     record[ 0 ].getClass().getName());
        MMSRecord mmsRecord = ( MMSRecord ) record[ 0 ];

        assertEquals(0, mmsRecord.getAddMenuCount());
        assertEquals(0, mmsRecord.getDelMenuCount());
        assertEquals(6, mmsRecord.getRecordSize());
        assertEquals(193, mmsRecord.getSid());
    }

    /**
     * TEST NAME:  Test Special Record Construction <P>
     * OBJECTIVE:  Test that the RecordFactory given the required parameters for
     *             constructing a RKRecord will return a NumberRecord.<P>
     * SUCCESS:    Record factory creates the Number record with the expected values.<P>
     * FAILURE:    The wrong records are created or contain the wrong values <P>
     *
     */

    public void testSpecial()
        throws Exception
    {
        short    recType = RKRecord.sid;
        byte[]   data    = new byte[]
        {
            0, 0, 0, 0, 21, 0, 0, 0, 0, 0
        };
        short    size    = 10;
        Record[] record  = RecordFactory.createRecord(recType, size, data);

        assertEquals(NumberRecord.class.getName(),
                     record[ 0 ].getClass().getName());
        NumberRecord numberRecord = ( NumberRecord ) record[ 0 ];

        assertEquals(0, numberRecord.getColumn());
        assertEquals(18, numberRecord.getRecordSize());
        assertEquals(0, numberRecord.getRow());
        assertEquals(515, numberRecord.getSid());
        assertEquals(0.0, numberRecord.getValue(), 0.001);
        assertEquals(21, numberRecord.getXFIndex());
    }

    public static void main(String [] ignored_args)
    {
        System.out
            .println("Testing org.apache.poi.hssf.record.TestRecordFactory");
        junit.textui.TestRunner.run(TestRecordFactory.class);
    }
}

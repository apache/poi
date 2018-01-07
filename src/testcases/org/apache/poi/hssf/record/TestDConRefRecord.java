/*
 *  ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ====================================================================
 */

package org.apache.poi.hssf.record;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import junit.framework.TestCase;
import org.apache.poi.util.LittleEndianOutputStream;

/**
 * Unit tests for DConRefRecord class.
 *
 * @author Niklas Rehfeld
 */
public class TestDConRefRecord extends TestCase
{
    /**
     * record of a proper single-byte external 'volume'-style path with multiple parts and a sheet
     * name.
     */
    final byte[] volumeString = new byte[]
    {
        0, 0, 0, 0, 0, 0, //ref (6 bytes) not used...
        17, 0,//cchFile (2 bytes)
        0, //char type
        1, 1, 'c', '[', 'f', 'o', 'o', 0x3,
        'b', 'a', 'r', ']', 's', 'h', 'e', 'e',
        't'
    };
    /**
     * record of a proper single-byte external 'unc-volume'-style path with multiple parts and a
     * sheet name.
     */
    final byte[] uncVolumeString = new byte[]
    {
        0, 0, 0, 0, 0, 0, //ref (6 bytes) not used...
        34, 0,//cchFile (2 bytes)
        0, //char type
        1, 1, '@', '[', 'c', 'o', 'm', 'p',
        0x3, 's', 'h', 'a', 'r', 'e', 'd', 0x3,
        'r', 'e', 'l', 'a', 't', 'i', 'v', 'e',
        0x3, 'f', 'o', 'o', ']', 's', 'h', 'e',
        'e', 't'
    };
    /**
     * record of a proper single-byte external 'simple-file-path-dcon' style path with a sheet name.
     */
    final byte[] simpleFilePathDconString = new byte[]
    {
        0, 0, 0, 0, 0, 0, //ref (6 bytes) not used...
        16, 0,//cchFile (2 bytes)
        0, //char type
        1, 'c', '[', 'f', 'o', 'o', 0x3, 'b',
        'a', 'r', ']', 's', 'h', 'e', 'e', 't'
    };
    /**
     * record of a proper 'transfer-protocol'-style path. This one has a sheet name at the end, and
     * another one inside the file path. The spec doesn't seem to care about what they are.
     */
    final byte[] transferProtocolString = new byte[]
    {
        0, 0, 0, 0, 0, 0, //ref (6 bytes) not used...
        33, 0,//cchFile (2 bytes)
        0, //char type
        0x1, 0x5, 30, //count = 30
        '[', 'h', 't', 't', 'p', ':', '/', '/',
        '[', 'f', 'o', 'o', 0x3, 'b', 'a', 'r',
        ']', 's', 'h', 'e', 'e', 't', '1', ']',
        's', 'h', 'e', 'e', 't', 'x'
    };
    /**
     * startup-type path.
     */
    final byte[] relVolumeString = new byte[]
    {
        0, 0, 0, 0, 0, 0, //ref (6 bytes) not used...
        16, 0,//cchFile (2 bytes)
        0, //char type
        0x1, 0x2, '[', 'f', 'o', 'o', 0x3, 'b',
        'a', 'r', ']', 's', 'h', 'e', 'e', 't'
    };
    /**
     * startup-type path.
     */
    final byte[] startupString = new byte[]
    {
        0, 0, 0, 0, 0, 0, //ref (6 bytes) not used...
        16, 0,//cchFile (2 bytes)
        0, //char type
        0x1, 0x6, '[', 'f', 'o', 'o', 0x3, 'b',
        'a', 'r', ']', 's', 'h', 'e', 'e', 't'
    };
    /**
     * alt-startup-type path.
     */
    final byte[] altStartupString = new byte[]
    {
        0, 0, 0, 0, 0, 0, //ref (6 bytes) not used...
        16, 0,//cchFile (2 bytes)
        0, //char type
        0x1, 0x7, '[', 'f', 'o', 'o', 0x3, 'b',
        'a', 'r', ']', 's', 'h', 'e', 'e', 't'
    };
    /**
     * library-style path.
     */
    final byte[] libraryString = new byte[]
    {
        0, 0, 0, 0, 0, 0, //ref (6 bytes) not used...
        16, 0,//cchFile (2 bytes)
        0, //char type
        0x1, 0x8, '[', 'f', 'o', 'o', 0x3, 'b',
        'a', 'r', ']', 's', 'h', 'e', 'e', 't'
    };
    /**
     * record of single-byte string, external, volume path.
     */
    final byte[] data1 = new byte[]
    {
        0, 0, 0, 0, 0, 0, //ref (6 bytes) not used...
        10, 0,//cchFile (2 bytes)
        0, //char type
        1, 1, (byte) 'b', (byte) 'l', (byte) 'a', (byte) ' ', (byte) 't',
        (byte) 'e', (byte) 's', (byte) 't'
    //unused doesn't exist as stFile[1] != 2
    };
    /**
     * record of double-byte string, self-reference.
     */
    final byte[] data2 = new byte[]
    {
        0, 0, 0, 0, 0, 0, //ref (6 bytes) not used...
        9, 0,//cchFile (2 bytes)
        1, //char type = unicode
        2, 0, (byte) 'b', 0, (byte) 'l', 0, (byte) 'a', 0, (byte) ' ', 0, (byte) 't', 0,
        (byte) 'e', 0, (byte) 's', (byte) 't', 0,//stFile
        0, 0 //unused (2 bytes as we're using double-byte chars)
    };
    /**
     * record of single-byte string, self-reference.
     */
    final byte[] data3 = new byte[]
    {
        0, 0, 0, 0, 0, 0, //ref (6 bytes) not used...
        9, 0,//cchFile (2 bytes)
        0, //char type = ansi
        2, (byte) 'b', (byte) 'l', (byte) 'a', (byte) ' ', (byte) 't', (byte) 'e', (byte) 's',
        (byte) 't',//stFile
        0 //unused (1 byte as we're using single byes)
    };
    /**
     * double-byte string, external reference, unc-volume.
     */
    final byte[] data4 = new byte[]
    {
        0, 0, 0, 0, 0, 0, //ref (6 bytes) not used...
        16, 0,//cchFile (2 bytes)
        //stFile starts here:
        1, //char type = unicode
        1, 0, 1, 0, 0x40, 0, (byte) 'c', 0, (byte) 'o', 0, (byte) 'm', 0, (byte) 'p', 0, 0x03, 0,
        (byte) 'b', 0, (byte) 'l', 0, (byte) 'a', 0, 0x03, 0, (byte) 't', 0, (byte) 'e', 0,
        (byte) 's', 0, (byte) 't', 0,
    //unused doesn't exist as stFile[1] != 2
    };

    /**
     * test read-constructor-then-serialize for a single-byte external reference strings of
     * various flavours. This uses the RecordInputStream constructor.
     * @throws IOException
     */
    public void testReadWriteSBExtRef() throws IOException
    {
        testReadWrite(data1, "read-write single-byte external reference, volume type path");
        testReadWrite(volumeString,
                "read-write properly formed single-byte external reference, volume type path");
        testReadWrite(uncVolumeString,
                "read-write properly formed single-byte external reference, UNC volume type path");
        testReadWrite(relVolumeString,
                "read-write properly formed single-byte external reference, rel-volume type path");
        testReadWrite(simpleFilePathDconString,
                "read-write properly formed single-byte external reference, simple-file-path-dcon type path");
        testReadWrite(transferProtocolString,
                "read-write properly formed single-byte external reference, transfer-protocol type path");
        testReadWrite(startupString,
                "read-write properly formed single-byte external reference, startup type path");
        testReadWrite(altStartupString,
                "read-write properly formed single-byte external reference, alt-startup type path");
        testReadWrite(libraryString,
                "read-write properly formed single-byte external reference, library type path");
    }

    /**
     * test read-constructor-then-serialize for a double-byte external reference 'UNC-Volume' style
     * string
     * <p>
     * @throws IOException
     */
    public void testReadWriteDBExtRefUncVol() throws IOException
    {
        testReadWrite(data4, "read-write double-byte external reference, UNC volume type path");
    }

    private void testReadWrite(byte[] data, String message) throws IOException
    {
        RecordInputStream is = TestcaseRecordInputStream.create(81, data);
        DConRefRecord d = new DConRefRecord(is);
        ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length);
        LittleEndianOutputStream o = new LittleEndianOutputStream(bos);
        d.serialize(o);
        o.flush();

        assertTrue(message, Arrays.equals(data,
                bos.toByteArray()));
    }

    /**
     * test read-constructor-then-serialize for a double-byte self-reference style string
     * <p>
     * @throws IOException
     */
    public void testReadWriteDBSelfRef() throws IOException
    {
        testReadWrite(data2, "read-write double-byte self reference");
    }

    /**
     * test read-constructor-then-serialize for a single-byte self-reference style string
     * <p>
     * @throws IOException
     */
    public void testReadWriteSBSelfRef() throws IOException
    {
        testReadWrite(data3, "read-write single byte self reference");
    }

    /**
     * Test of getDataSize method, of class DConRefRecord.
     */
    public void testGetDataSize()
    {
        DConRefRecord instance = new DConRefRecord(TestcaseRecordInputStream.create(81, data1));
        int expResult = data1.length;
        int result = instance.getDataSize();
        assertEquals("single byte external reference, volume type path data size", expResult, result);
        instance = new DConRefRecord(TestcaseRecordInputStream.create(81, data2));
        assertEquals("double byte self reference data size", data2.length, instance.getDataSize());
        instance = new DConRefRecord(TestcaseRecordInputStream.create(81, data3));
        assertEquals("single byte self reference data size", data3.length, instance.getDataSize());
        instance = new DConRefRecord(TestcaseRecordInputStream.create(81, data4));
        assertEquals("double byte external reference, UNC volume type path data size", data4.length,
                instance.getDataSize());
    }

    /**
     * Test of getSid method, of class DConRefRecord.
     */
    public void testGetSid()
    {
        DConRefRecord instance = new DConRefRecord(TestcaseRecordInputStream.create(81, data1));
        short expResult = 81;
        short result = instance.getSid();
        assertEquals("SID", expResult, result);
    }

    /**
     * Test of getPath method, of class DConRefRecord.
     * @todo different types of paths.
     */
    public void testGetPath()
    {
        DConRefRecord instance = new DConRefRecord(TestcaseRecordInputStream.create(81, data1));
        byte[] expResult = Arrays.copyOfRange(data1, 9, data1.length);
        byte[] result = instance.getPath();
        assertTrue("get path", Arrays.equals(expResult, result));
    }

    /**
     * Test of isExternalRef method, of class DConRefRecord.
     */
    public void testIsExternalRef()
    {
        DConRefRecord instance = new DConRefRecord(TestcaseRecordInputStream.create(81, data1));
        assertTrue("external reference", instance.isExternalRef());
        instance = new DConRefRecord(TestcaseRecordInputStream.create(81, data2));
        assertFalse("internal reference", instance.isExternalRef());
    }
}

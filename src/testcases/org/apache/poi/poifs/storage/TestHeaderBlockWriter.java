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

package org.apache.poi.poifs.storage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import junit.framework.TestCase;

import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.LittleEndianConsts;

/**
 * Class to test HeaderBlockWriter functionality
 *
 * @author Marc Johnson
 */
public final class TestHeaderBlockWriter extends TestCase {

    /**
     * Test creating a HeaderBlockWriter
     */
    public void testConstructors() throws IOException {
        HeaderBlockWriter     block  = new HeaderBlockWriter();
        ByteArrayOutputStream output = new ByteArrayOutputStream(512);

        block.writeBlocks(output);
        byte[] copy     = output.toByteArray();
        byte[] expected =
        {   // TODO - put this raw data in a better format
            ( byte ) 0xD0, ( byte ) 0xCF, ( byte ) 0x11, ( byte ) 0xE0,
            ( byte ) 0xA1, ( byte ) 0xB1, ( byte ) 0x1A, ( byte ) 0xE1,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x3B, ( byte ) 0x00, ( byte ) 0x03, ( byte ) 0x00,
            ( byte ) 0xFE, ( byte ) 0xFF, ( byte ) 0x09, ( byte ) 0x00,
            ( byte ) 0x06, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0xFE, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x10, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0xFE, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0xFE, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF
        };

        assertEquals(expected.length, copy.length);
        for (int j = 0; j < 512; j++)
        {
            assertEquals("testing byte " + j, expected[ j ], copy[ j ]);
        }

        // verify we can read a 'good' HeaderBlockWriter (also test
        // getPropertyStart)
        block.setPropertyStart(0x87654321);
        output = new ByteArrayOutputStream(512);
        block.writeBlocks(output);
        assertEquals(0x87654321,
                     new HeaderBlockReader(new ByteArrayInputStream(output
                         .toByteArray())).getPropertyStart());
    }

    /**
     * Test setting the SBAT start block
     */
    public void testSetSBATStart() throws IOException {
        HeaderBlockWriter block = new HeaderBlockWriter();

        block.setSBATStart(0x01234567);
        ByteArrayOutputStream output = new ByteArrayOutputStream(512);

        block.writeBlocks(output);
        byte[] copy     = output.toByteArray();
        byte[] expected =
        {
            ( byte ) 0xD0, ( byte ) 0xCF, ( byte ) 0x11, ( byte ) 0xE0,
            ( byte ) 0xA1, ( byte ) 0xB1, ( byte ) 0x1A, ( byte ) 0xE1,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x3B, ( byte ) 0x00, ( byte ) 0x03, ( byte ) 0x00,
            ( byte ) 0xFE, ( byte ) 0xFF, ( byte ) 0x09, ( byte ) 0x00,
            ( byte ) 0x06, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0xFE, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x10, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x67, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0xFE, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF
        };

        assertEquals(expected.length, copy.length);
        for (int j = 0; j < 512; j++)
        {
            assertEquals("testing byte " + j, expected[ j ], copy[ j ]);
        }
    }

    /**
     * test setPropertyStart and getPropertyStart
     */
    public void testSetPropertyStart() throws IOException {
        HeaderBlockWriter block = new HeaderBlockWriter();

        block.setPropertyStart(0x01234567);
        ByteArrayOutputStream output = new ByteArrayOutputStream(512);

        block.writeBlocks(output);
        byte[] copy     = output.toByteArray();
        byte[] expected =
        {
            ( byte ) 0xD0, ( byte ) 0xCF, ( byte ) 0x11, ( byte ) 0xE0,
            ( byte ) 0xA1, ( byte ) 0xB1, ( byte ) 0x1A, ( byte ) 0xE1,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x3B, ( byte ) 0x00, ( byte ) 0x03, ( byte ) 0x00,
            ( byte ) 0xFE, ( byte ) 0xFF, ( byte ) 0x09, ( byte ) 0x00,
            ( byte ) 0x06, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x67, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x10, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0xFE, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0xFE, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF
        };

        assertEquals(expected.length, copy.length);
        for (int j = 0; j < 512; j++)
        {
            assertEquals("testing byte " + j, expected[ j ], copy[ j ]);
        }
    }

    /**
     * test setting the BAT blocks; also tests getBATCount,
     * getBATArray, getXBATCount
     */
    public void testSetBATBlocks() throws IOException {

        // first, a small set of blocks
        HeaderBlockWriter block = new HeaderBlockWriter();
        BATBlock[]        xbats = block.setBATBlocks(5, 0x01234567);

        assertEquals(0, xbats.length);
        assertEquals(0, HeaderBlockWriter
            .calculateXBATStorageRequirements(5));
        ByteArrayOutputStream output = new ByteArrayOutputStream(512);

        block.writeBlocks(output);
        byte[] copy     = output.toByteArray();
        byte[] expected =
        {
            ( byte ) 0xD0, ( byte ) 0xCF, ( byte ) 0x11, ( byte ) 0xE0,
            ( byte ) 0xA1, ( byte ) 0xB1, ( byte ) 0x1A, ( byte ) 0xE1,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x3B, ( byte ) 0x00, ( byte ) 0x03, ( byte ) 0x00,
            ( byte ) 0xFE, ( byte ) 0xFF, ( byte ) 0x09, ( byte ) 0x00,
            ( byte ) 0x06, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x05, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0xFE, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x10, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0xFE, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0xFE, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x67, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x68, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x69, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x6A, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x6B, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF
        };

        assertEquals(expected.length, copy.length);
        for (int j = 0; j < 512; j++)
        {
            assertEquals("testing byte " + j, expected[ j ], copy[ j ]);
        }

        // second, a full set of blocks (109 blocks)
        block = new HeaderBlockWriter();
        xbats = block.setBATBlocks(109, 0x01234567);
        assertEquals(0, xbats.length);
        assertEquals(0, HeaderBlockWriter
            .calculateXBATStorageRequirements(109));
        output = new ByteArrayOutputStream(512);
        block.writeBlocks(output);
        copy = output.toByteArray();
        byte[] expected2 =
        {
            ( byte ) 0xD0, ( byte ) 0xCF, ( byte ) 0x11, ( byte ) 0xE0,
            ( byte ) 0xA1, ( byte ) 0xB1, ( byte ) 0x1A, ( byte ) 0xE1,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x3B, ( byte ) 0x00, ( byte ) 0x03, ( byte ) 0x00,
            ( byte ) 0xFE, ( byte ) 0xFF, ( byte ) 0x09, ( byte ) 0x00,
            ( byte ) 0x06, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x6D, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0xFE, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x10, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0xFE, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0xFE, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x67, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x68, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x69, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x6A, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x6B, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x6C, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x6D, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x6E, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x6F, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x70, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x71, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x72, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x73, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x74, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x75, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x76, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x77, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x78, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x79, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x7A, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x7B, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x7C, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x7D, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x7E, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x7F, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x80, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x81, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x82, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x83, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x84, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x85, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x86, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x87, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x88, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x89, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x8A, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x8B, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x8C, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x8D, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x8E, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x8F, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x90, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x91, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x92, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x93, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x94, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x95, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x96, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x97, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x98, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x99, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x9A, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x9B, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x9C, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x9D, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x9E, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x9F, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0xA0, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0xA1, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0xA2, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0xA3, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0xA4, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0xA5, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0xA6, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0xA7, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0xA8, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0xA9, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0xAA, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0xAB, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0xAC, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0xAD, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0xAE, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0xAF, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0xB0, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0xB1, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0xB2, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0xB3, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0xB4, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0xB5, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0xB6, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0xB7, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0xB8, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0xB9, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0xBA, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0xBB, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0xBC, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0xBD, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0xBE, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0xBF, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0xC0, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0xC1, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0xC2, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0xC3, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0xC4, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0xC5, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0xC6, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0xC7, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0xC8, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0xC9, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0xCA, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0xCB, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0xCC, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0xCD, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0xCE, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0xCF, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0xD0, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0xD1, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0xD2, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0xD3, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01
        };

        assertEquals(expected2.length, copy.length);
        for (int j = 0; j < 512; j++)
        {
            assertEquals("testing byte " + j, expected2[ j ], copy[ j ]);
        }

        // finally, a really large set of blocks (256 blocks)
        block = new HeaderBlockWriter();
        xbats = block.setBATBlocks(256, 0x01234567);
        assertEquals(2, xbats.length);
        assertEquals(2, HeaderBlockWriter
            .calculateXBATStorageRequirements(256));
        output = new ByteArrayOutputStream(512);
        block.writeBlocks(output);
        copy = output.toByteArray();
        byte[] expected3 =
        {
            ( byte ) 0xD0, ( byte ) 0xCF, ( byte ) 0x11, ( byte ) 0xE0,
            ( byte ) 0xA1, ( byte ) 0xB1, ( byte ) 0x1A, ( byte ) 0xE1,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x3B, ( byte ) 0x00, ( byte ) 0x03, ( byte ) 0x00,
            ( byte ) 0xFE, ( byte ) 0xFF, ( byte ) 0x09, ( byte ) 0x00,
            ( byte ) 0x06, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x01, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0xFE, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x10, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0xFE, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x67, ( byte ) 0x46, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x02, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x67, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x68, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x69, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x6A, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x6B, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x6C, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x6D, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x6E, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x6F, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x70, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x71, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x72, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x73, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x74, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x75, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x76, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x77, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x78, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x79, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x7A, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x7B, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x7C, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x7D, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x7E, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x7F, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x80, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x81, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x82, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x83, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x84, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x85, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x86, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x87, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x88, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x89, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x8A, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x8B, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x8C, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x8D, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x8E, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x8F, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x90, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x91, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x92, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x93, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x94, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x95, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x96, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x97, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x98, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x99, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x9A, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x9B, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x9C, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x9D, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x9E, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0x9F, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0xA0, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0xA1, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0xA2, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0xA3, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0xA4, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0xA5, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0xA6, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0xA7, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0xA8, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0xA9, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0xAA, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0xAB, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0xAC, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0xAD, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0xAE, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0xAF, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0xB0, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0xB1, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0xB2, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0xB3, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0xB4, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0xB5, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0xB6, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0xB7, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0xB8, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0xB9, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0xBA, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0xBB, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0xBC, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0xBD, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0xBE, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0xBF, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0xC0, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0xC1, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0xC2, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0xC3, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0xC4, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0xC5, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0xC6, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0xC7, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0xC8, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0xC9, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0xCA, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0xCB, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0xCC, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0xCD, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0xCE, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0xCF, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0xD0, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0xD1, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0xD2, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01,
            ( byte ) 0xD3, ( byte ) 0x45, ( byte ) 0x23, ( byte ) 0x01
        };

        assertEquals(expected3.length, copy.length);
        for (int j = 0; j < 512; j++)
        {
            assertEquals("testing byte " + j, expected3[ j ], copy[ j ]);
        }
        output = new ByteArrayOutputStream(1028);
        xbats[ 0 ].writeBlocks(output);
        xbats[ 1 ].writeBlocks(output);
        copy = output.toByteArray();
        int correct = 0x012345D4;
        int offset  = 0;
        int k       = 0;

        for (; k < 127; k++)
        {
            assertEquals("XBAT entry " + k, correct,
                         LittleEndian.getInt(copy, offset));
            correct++;
            offset += LittleEndianConsts.INT_SIZE;
        }
        assertEquals("XBAT Chain", 0x01234567 + 257,
                     LittleEndian.getInt(copy, offset));
        offset += LittleEndianConsts.INT_SIZE;
        k++;
        for (; k < 148; k++)
        {
            assertEquals("XBAT entry " + k, correct,
                         LittleEndian.getInt(copy, offset));
            correct++;
            offset += LittleEndianConsts.INT_SIZE;
        }
        for (; k < 255; k++)
        {
            assertEquals("XBAT entry " + k, -1,
                         LittleEndian.getInt(copy, offset));
            offset += LittleEndianConsts.INT_SIZE;
        }
        assertEquals("XBAT End of chain", -2,
                     LittleEndian.getInt(copy, offset));
    }
}

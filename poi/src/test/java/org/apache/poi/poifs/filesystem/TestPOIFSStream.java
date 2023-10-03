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

package org.apache.poi.poifs.filesystem;

import static org.apache.poi.POIDataSamples.writeOutAndReadBack;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Function;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.output.UnsynchronizedByteArrayOutputStream;
import org.apache.poi.POIDataSamples;
import org.apache.poi.hpsf.DocumentSummaryInformation;
import org.apache.poi.hpsf.NoPropertySetStreamException;
import org.apache.poi.hpsf.PropertySet;
import org.apache.poi.hpsf.PropertySetFactory;
import org.apache.poi.hpsf.SummaryInformation;
import org.apache.poi.poifs.common.POIFSConstants;
import org.apache.poi.poifs.property.DirectoryProperty;
import org.apache.poi.poifs.property.Property;
import org.apache.poi.poifs.property.PropertyTable;
import org.apache.poi.poifs.property.RootProperty;
import org.apache.poi.poifs.storage.BATBlock;
import org.apache.poi.poifs.storage.HeaderBlock;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.TempFile;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests {@link POIFSStream}
 */
final class TestPOIFSStream {
    private static final POIDataSamples _inst = POIDataSamples.getPOIFSInstance();

    /**
     * Read a single block stream
     */
    @Test
    void testReadTinyStream() throws Exception {
        try (POIFSFileSystem fs = new POIFSFileSystem(_inst.getFile("BlockSize512.zvi"))) {

            // 98 is actually the last block in a two block stream...
            POIFSStream stream = new POIFSStream(fs, 98);
            Iterator<ByteBuffer> i = stream.getBlockIterator();
            assertTrue(i.hasNext());
            ByteBuffer b = i.next();
            assertFalse(i.hasNext());

            // Check the contents
            assertEquals((byte) 0x81, b.get());
            assertEquals((byte) 0x00, b.get());
            assertEquals((byte) 0x00, b.get());
            assertEquals((byte) 0x00, b.get());
            assertEquals((byte) 0x82, b.get());
            assertEquals((byte) 0x00, b.get());
            assertEquals((byte) 0x00, b.get());
            assertEquals((byte) 0x00, b.get());
        }
    }

    /**
     * Read a stream with only two blocks in it
     */
    @Test
    void testReadShortStream() throws Exception {
        try (POIFSFileSystem fs = new POIFSFileSystem(_inst.getFile("BlockSize512.zvi"))) {

            // 97 -> 98 -> end
            POIFSStream stream = new POIFSStream(fs, 97);
            Iterator<ByteBuffer> i = stream.getBlockIterator();
            assertTrue(i.hasNext());
            ByteBuffer b97 = i.next();
            assertTrue(i.hasNext());
            ByteBuffer b98 = i.next();
            assertFalse(i.hasNext());

            // Check the contents of the 1st block
            assertEquals((byte) 0x01, b97.get());
            assertEquals((byte) 0x00, b97.get());
            assertEquals((byte) 0x00, b97.get());
            assertEquals((byte) 0x00, b97.get());
            assertEquals((byte) 0x02, b97.get());
            assertEquals((byte) 0x00, b97.get());
            assertEquals((byte) 0x00, b97.get());
            assertEquals((byte) 0x00, b97.get());

            // Check the contents of the 2nd block
            assertEquals((byte) 0x81, b98.get());
            assertEquals((byte) 0x00, b98.get());
            assertEquals((byte) 0x00, b98.get());
            assertEquals((byte) 0x00, b98.get());
            assertEquals((byte) 0x82, b98.get());
            assertEquals((byte) 0x00, b98.get());
            assertEquals((byte) 0x00, b98.get());
            assertEquals((byte) 0x00, b98.get());
        }
    }

    /**
     * Read a stream with many blocks
     */
    @Test
    void testReadLongerStream() throws Exception {
        try (POIFSFileSystem fs = new POIFSFileSystem(_inst.getFile("BlockSize512.zvi"))) {

            ByteBuffer b0 = null;
            ByteBuffer b1 = null;
            ByteBuffer b22 = null;

            // The stream at 0 has 23 blocks in it
            POIFSStream stream = new POIFSStream(fs, 0);
            Iterator<ByteBuffer> i = stream.getBlockIterator();
            int count = 0;
            while (i.hasNext()) {
                ByteBuffer b = i.next();
                if (count == 0) {
                    b0 = b;
                }
                if (count == 1) {
                    b1 = b;
                }
                if (count == 22) {
                    b22 = b;
                }

                count++;
            }
            assertEquals(23, count);

            // Check the contents
            //  1st block is at 0
            assertNotNull(b0);
            assertEquals((byte) 0x9e, b0.get());
            assertEquals((byte) 0x75, b0.get());
            assertEquals((byte) 0x97, b0.get());
            assertEquals((byte) 0xf6, b0.get());

            //  2nd block is at 1
            assertNotNull(b1);
            assertEquals((byte) 0x86, b1.get());
            assertEquals((byte) 0x09, b1.get());
            assertEquals((byte) 0x22, b1.get());
            assertEquals((byte) 0xfb, b1.get());

            //  last block is at 89
            assertNotNull(b22);
            assertEquals((byte) 0xfe, b22.get());
            assertEquals((byte) 0xff, b22.get());
            assertEquals((byte) 0x00, b22.get());
            assertEquals((byte) 0x00, b22.get());
            assertEquals((byte) 0x05, b22.get());
            assertEquals((byte) 0x01, b22.get());
            assertEquals((byte) 0x02, b22.get());
            assertEquals((byte) 0x00, b22.get());
        }
    }

    /**
     * Read a stream with several blocks in a 4096 byte block file
     */
    @Test
    void testReadStream4096() throws Exception {
        try (POIFSFileSystem fs = new POIFSFileSystem(_inst.getFile("BlockSize4096.zvi"))) {
            // 0 -> 1 -> 2 -> end
            POIFSStream stream = new POIFSStream(fs, 0);
            Iterator<ByteBuffer> i = stream.getBlockIterator();
            assertTrue(i.hasNext());
            ByteBuffer b0 = i.next();
            assertTrue(i.hasNext());
            ByteBuffer b1 = i.next();
            assertTrue(i.hasNext());
            ByteBuffer b2 = i.next();
            assertFalse(i.hasNext());

            // Check the contents of the 1st block
            assertEquals((byte) 0x9E, b0.get());
            assertEquals((byte) 0x75, b0.get());
            assertEquals((byte) 0x97, b0.get());
            assertEquals((byte) 0xF6, b0.get());
            assertEquals((byte) 0xFF, b0.get());
            assertEquals((byte) 0x21, b0.get());
            assertEquals((byte) 0xD2, b0.get());
            assertEquals((byte) 0x11, b0.get());

            // Check the contents of the 2nd block
            assertEquals((byte) 0x00, b1.get());
            assertEquals((byte) 0x00, b1.get());
            assertEquals((byte) 0x03, b1.get());
            assertEquals((byte) 0x00, b1.get());
            assertEquals((byte) 0x00, b1.get());
            assertEquals((byte) 0x00, b1.get());
            assertEquals((byte) 0x00, b1.get());
            assertEquals((byte) 0x00, b1.get());

            // Check the contents of the 3rd block
            assertEquals((byte) 0x6D, b2.get());
            assertEquals((byte) 0x00, b2.get());
            assertEquals((byte) 0x00, b2.get());
            assertEquals((byte) 0x00, b2.get());
            assertEquals((byte) 0x03, b2.get());
            assertEquals((byte) 0x00, b2.get());
            assertEquals((byte) 0x46, b2.get());
            assertEquals((byte) 0x00, b2.get());
        }
    }

    /**
     * Craft a nasty file with a loop, and ensure we don't get stuck
     */
    @Test
    void testReadFailsOnLoop() throws Exception {
        try (POIFSFileSystem fs = new POIFSFileSystem(_inst.getFile("BlockSize512.zvi"))) {
            // Hack the FAT so that it goes 0->1->2->0
            fs.setNextBlock(0, 1);
            fs.setNextBlock(1, 2);
            fs.setNextBlock(2, 0);

            // Now try to read
            POIFSStream stream = new POIFSStream(fs, 0);
            Iterator<ByteBuffer> i = stream.getBlockIterator();
            assertTrue(i.hasNext());

            // 1st read works
            i.next();
            assertTrue(i.hasNext());

            // 2nd read works
            i.next();
            assertTrue(i.hasNext());

            // 3rd read works
            i.next();
            assertTrue(i.hasNext());

            // 4th read blows up as it loops back to 0
            assertThrows(RuntimeException.class, i::next, "Loop should have been detected but wasn't!");
            assertTrue(i.hasNext());
        }
    }

    /**
     * Tests that we can load some streams that are
     * stored in the mini stream.
     */
    @Test
    void testReadMiniStreams() throws Exception {
        try (POIFSFileSystem fs = new POIFSFileSystem(_inst.openResourceAsStream("BlockSize512.zvi"))) {
            POIFSMiniStore ministore = fs.getMiniStore();

            // 178 -> 179 -> 180 -> end
            POIFSStream stream = new POIFSStream(ministore, 178);
            Iterator<ByteBuffer> i = stream.getBlockIterator();
            assertTrue(i.hasNext());
            ByteBuffer b178 = i.next();
            assertTrue(i.hasNext());
            ByteBuffer b179 = i.next();
            assertTrue(i.hasNext());
            ByteBuffer b180 = i.next();
            assertFalse(i.hasNext());

            // Check the contents of the 1st block
            assertEquals((byte) 0xfe, b178.get());
            assertEquals((byte) 0xff, b178.get());
            assertEquals((byte) 0x00, b178.get());
            assertEquals((byte) 0x00, b178.get());
            assertEquals((byte) 0x05, b178.get());
            assertEquals((byte) 0x01, b178.get());
            assertEquals((byte) 0x02, b178.get());
            assertEquals((byte) 0x00, b178.get());

            // And the 2nd
            assertEquals((byte) 0x6c, b179.get());
            assertEquals((byte) 0x00, b179.get());
            assertEquals((byte) 0x00, b179.get());
            assertEquals((byte) 0x00, b179.get());
            assertEquals((byte) 0x28, b179.get());
            assertEquals((byte) 0x00, b179.get());
            assertEquals((byte) 0x00, b179.get());
            assertEquals((byte) 0x00, b179.get());

            // And the 3rd
            assertEquals((byte) 0x30, b180.get());
            assertEquals((byte) 0x00, b180.get());
            assertEquals((byte) 0x00, b180.get());
            assertEquals((byte) 0x00, b180.get());
            assertEquals((byte) 0x00, b180.get());
            assertEquals((byte) 0x00, b180.get());
            assertEquals((byte) 0x00, b180.get());
            assertEquals((byte) 0x80, b180.get());
        }
    }

    /**
     * Writing the same amount of data as before
     */
    @Test
    void testReplaceStream() throws Exception {
        try (POIFSFileSystem fs = new POIFSFileSystem(_inst.openResourceAsStream("BlockSize512.zvi"))) {

            byte[] data = new byte[512];
            for (int i = 0; i < data.length; i++) {
                data[i] = (byte) (i % 256);
            }

            // 98 is actually the last block in a two block stream...
            POIFSStream stream = new POIFSStream(fs, 98);
            stream.updateContents(data);

            // Check the reading of blocks
            Iterator<ByteBuffer> it = stream.getBlockIterator();
            assertTrue(it.hasNext());
            ByteBuffer b = it.next();
            assertFalse(it.hasNext());

            // Now check the contents
            data = new byte[512];
            b.get(data);
            for (int i = 0; i < data.length; i++) {
                byte exp = (byte) (i % 256);
                assertEquals(exp, data[i]);
            }
        }
    }

    /**
     * Writes less data than before, some blocks will need
     * to be freed
     */
    @Test
    void testReplaceStreamWithLess() throws Exception {
        try (InputStream is = _inst.openResourceAsStream("BlockSize512.zvi");
             POIFSFileSystem fs = new POIFSFileSystem(is)) {

            byte[] data = new byte[512];
            for (int i = 0; i < data.length; i++) {
                data[i] = (byte) (i % 256);
            }

            // 97 -> 98 -> end
            assertEquals(98, fs.getNextBlock(97));
            assertEquals(POIFSConstants.END_OF_CHAIN, fs.getNextBlock(98));

            // Create a 2 block stream, will become a 1 block one
            POIFSStream stream = new POIFSStream(fs, 97);
            stream.updateContents(data);

            // 97 should now be the end, and 98 free
            assertEquals(POIFSConstants.END_OF_CHAIN, fs.getNextBlock(97));
            assertEquals(POIFSConstants.UNUSED_BLOCK, fs.getNextBlock(98));

            // Check the reading of blocks
            Iterator<ByteBuffer> it = stream.getBlockIterator();
            assertTrue(it.hasNext());
            ByteBuffer b = it.next();
            assertFalse(it.hasNext());

            // Now check the contents
            data = new byte[512];
            b.get(data);
            for (int i = 0; i < data.length; i++) {
                byte exp = (byte) (i % 256);
                assertEquals(exp, data[i]);
            }
        }
    }

    /**
     * Writes more data than before, new blocks will be needed
     */
    @Test
    void testReplaceStreamWithMore() throws Exception {
        try (InputStream is = _inst.openResourceAsStream("BlockSize512.zvi");
             POIFSFileSystem fs = new POIFSFileSystem(is)) {

            byte[] data = new byte[512 * 3];
            for (int i = 0; i < data.length; i++) {
                data[i] = (byte) (i % 256);
            }

            // 97 -> 98 -> end
            assertEquals(98, fs.getNextBlock(97));
            assertEquals(POIFSConstants.END_OF_CHAIN, fs.getNextBlock(98));

            // 100 is our first free one
            assertEquals(POIFSConstants.FAT_SECTOR_BLOCK, fs.getNextBlock(99));
            assertEquals(POIFSConstants.UNUSED_BLOCK, fs.getNextBlock(100));

            // Create a 2 block stream, will become a 3 block one
            POIFSStream stream = new POIFSStream(fs, 97);
            stream.updateContents(data);

            // 97 -> 98 -> 100 -> end
            assertEquals(98, fs.getNextBlock(97));
            assertEquals(100, fs.getNextBlock(98));
            assertEquals(POIFSConstants.END_OF_CHAIN, fs.getNextBlock(100));

            // Check the reading of blocks
            Iterator<ByteBuffer> it = stream.getBlockIterator();
            int count = 0;
            while (it.hasNext()) {
                ByteBuffer b = it.next();
                data = new byte[512];
                b.get(data);
                for (int i = 0; i < data.length; i++) {
                    byte exp = (byte) (i % 256);
                    assertEquals(exp, data[i]);
                }
                count++;
            }
            assertEquals(3, count);
        }
    }

    /**
     * Writes to a new stream in the file
     */
    @Test
    void testWriteNewStream() throws Exception {
        try (POIFSFileSystem fs = new POIFSFileSystem(_inst.openResourceAsStream("BlockSize512.zvi"))) {

            // 100 is our first free one
            assertEquals(POIFSConstants.FAT_SECTOR_BLOCK, fs.getNextBlock(99));
            assertEquals(POIFSConstants.UNUSED_BLOCK, fs.getNextBlock(100));
            assertEquals(POIFSConstants.UNUSED_BLOCK, fs.getNextBlock(101));
            assertEquals(POIFSConstants.UNUSED_BLOCK, fs.getNextBlock(102));
            assertEquals(POIFSConstants.UNUSED_BLOCK, fs.getNextBlock(103));
            assertEquals(POIFSConstants.UNUSED_BLOCK, fs.getNextBlock(104));


            // Add a single block one
            byte[] data = new byte[512];
            for (int i = 0; i < data.length; i++) {
                data[i] = (byte) (i % 256);
            }

            POIFSStream stream = new POIFSStream(fs);
            stream.updateContents(data);

            // Check it was allocated properly
            assertEquals(POIFSConstants.FAT_SECTOR_BLOCK, fs.getNextBlock(99));
            assertEquals(POIFSConstants.END_OF_CHAIN, fs.getNextBlock(100));
            assertEquals(POIFSConstants.UNUSED_BLOCK, fs.getNextBlock(101));
            assertEquals(POIFSConstants.UNUSED_BLOCK, fs.getNextBlock(102));
            assertEquals(POIFSConstants.UNUSED_BLOCK, fs.getNextBlock(103));
            assertEquals(POIFSConstants.UNUSED_BLOCK, fs.getNextBlock(104));

            // And check the contents
            Iterator<ByteBuffer> it = stream.getBlockIterator();
            int count = 0;
            while (it.hasNext()) {
                ByteBuffer b = it.next();
                data = new byte[512];
                b.get(data);
                for (int i = 0; i < data.length; i++) {
                    byte exp = (byte) (i % 256);
                    assertEquals(exp, data[i]);
                }
                count++;
            }
            assertEquals(1, count);


            // And a multi block one
            data = new byte[512 * 3];
            for (int i = 0; i < data.length; i++) {
                data[i] = (byte) (i % 256);
            }

            stream = new POIFSStream(fs);
            stream.updateContents(data);

            // Check it was allocated properly
            assertEquals(POIFSConstants.FAT_SECTOR_BLOCK, fs.getNextBlock(99));
            assertEquals(POIFSConstants.END_OF_CHAIN, fs.getNextBlock(100));
            assertEquals(102, fs.getNextBlock(101));
            assertEquals(103, fs.getNextBlock(102));
            assertEquals(POIFSConstants.END_OF_CHAIN, fs.getNextBlock(103));
            assertEquals(POIFSConstants.UNUSED_BLOCK, fs.getNextBlock(104));

            // And check the contents
            it = stream.getBlockIterator();
            count = 0;
            while (it.hasNext()) {
                ByteBuffer b = it.next();
                data = new byte[512];
                b.get(data);
                for (int i = 0; i < data.length; i++) {
                    byte exp = (byte) (i % 256);
                    assertEquals(exp, data[i]);
                }
                count++;
            }
            assertEquals(3, count);

            // Free it
            stream.free();
            assertEquals(POIFSConstants.FAT_SECTOR_BLOCK, fs.getNextBlock(99));
            assertEquals(POIFSConstants.END_OF_CHAIN, fs.getNextBlock(100));
            assertEquals(POIFSConstants.UNUSED_BLOCK, fs.getNextBlock(101));
            assertEquals(POIFSConstants.UNUSED_BLOCK, fs.getNextBlock(102));
            assertEquals(POIFSConstants.UNUSED_BLOCK, fs.getNextBlock(103));
            assertEquals(POIFSConstants.UNUSED_BLOCK, fs.getNextBlock(104));
        }
    }

    /**
     * Writes to a new stream in the file, where we've not enough
     * free blocks so new FAT segments will need to be allocated
     * to support this
     */
    @Test
    void testWriteNewStreamExtraFATs() throws Exception {
        try (POIFSFileSystem fs = new POIFSFileSystem(_inst.openResourceAsStream("BlockSize512.zvi"))) {

            // Allocate almost all the blocks
            assertEquals(POIFSConstants.FAT_SECTOR_BLOCK, fs.getNextBlock(99));
            assertEquals(POIFSConstants.UNUSED_BLOCK, fs.getNextBlock(100));
            assertEquals(POIFSConstants.UNUSED_BLOCK, fs.getNextBlock(127));
            for (int i = 100; i < 127; i++) {
                fs.setNextBlock(i, POIFSConstants.END_OF_CHAIN);
            }
            assertEquals(POIFSConstants.UNUSED_BLOCK, fs.getNextBlock(127));
            assertTrue(fs.getBATBlockAndIndex(0).getBlock().hasFreeSectors());


            // Write a 3 block stream
            byte[] data = new byte[512 * 3];
            for (int i = 0; i < data.length; i++) {
                data[i] = (byte) (i % 256);
            }
            POIFSStream stream = new POIFSStream(fs);
            stream.updateContents(data);

            // Check we got another BAT
            assertFalse(fs.getBATBlockAndIndex(0).getBlock().hasFreeSectors());
            assertTrue(fs.getBATBlockAndIndex(128).getBlock().hasFreeSectors());

            // the BAT will be in the first spot of the new block
            assertEquals(POIFSConstants.END_OF_CHAIN, fs.getNextBlock(126));
            assertEquals(129, fs.getNextBlock(127));
            assertEquals(POIFSConstants.FAT_SECTOR_BLOCK, fs.getNextBlock(128));
            assertEquals(130, fs.getNextBlock(129));
            assertEquals(POIFSConstants.END_OF_CHAIN, fs.getNextBlock(130));
            assertEquals(POIFSConstants.UNUSED_BLOCK, fs.getNextBlock(131));
        }
    }

    /**
     * Replaces data in an existing stream, with a bit
     * more data than before, in a 4096 byte block file
     */
    @Test
    void testWriteStream4096() throws Exception {
        try (POIFSFileSystem fs = new POIFSFileSystem(_inst.openResourceAsStream("BlockSize4096.zvi"))) {

            // 0 -> 1 -> 2 -> end
            assertEquals(1, fs.getNextBlock(0));
            assertEquals(2, fs.getNextBlock(1));
            assertEquals(POIFSConstants.END_OF_CHAIN, fs.getNextBlock(2));
            assertEquals(4, fs.getNextBlock(3));

            // First free one is at 15
            assertEquals(POIFSConstants.FAT_SECTOR_BLOCK, fs.getNextBlock(14));
            assertEquals(POIFSConstants.UNUSED_BLOCK, fs.getNextBlock(15));


            // Write a 5 block file
            byte[] data = new byte[4096 * 5];
            for (int i = 0; i < data.length; i++) {
                data[i] = (byte) (i % 256);
            }
            POIFSStream stream = new POIFSStream(fs, 0);
            stream.updateContents(data);


            // Check it
            assertEquals(1, fs.getNextBlock(0));
            assertEquals(2, fs.getNextBlock(1));
            assertEquals(15, fs.getNextBlock(2)); // Jumps
            assertEquals(4, fs.getNextBlock(3));  // Next stream
            assertEquals(POIFSConstants.FAT_SECTOR_BLOCK, fs.getNextBlock(14));
            assertEquals(16, fs.getNextBlock(15)); // Continues
            assertEquals(POIFSConstants.END_OF_CHAIN, fs.getNextBlock(16)); // Ends
            assertEquals(POIFSConstants.UNUSED_BLOCK, fs.getNextBlock(17)); // Free

            // Check the contents too
            Iterator<ByteBuffer> it = stream.getBlockIterator();
            int count = 0;
            while (it.hasNext()) {
                ByteBuffer b = it.next();
                data = new byte[512];
                b.get(data);
                for (int i = 0; i < data.length; i++) {
                    byte exp = (byte) (i % 256);
                    assertEquals(exp, data[i]);
                }
                count++;
            }
            assertEquals(5, count);
        }
    }

    /**
     * Tests that we can write into the mini stream
     */
    @Test
    void testWriteMiniStreams() throws Exception {
        try (InputStream is = _inst.openResourceAsStream("BlockSize512.zvi");
             POIFSFileSystem fs = new POIFSFileSystem(is)) {

            POIFSMiniStore ministore = fs.getMiniStore();

            // 178 -> 179 -> 180 -> end
            assertEquals(179, ministore.getNextBlock(178));
            assertEquals(180, ministore.getNextBlock(179));
            assertEquals(POIFSConstants.END_OF_CHAIN, ministore.getNextBlock(180));


            // Try writing 3 full blocks worth
            byte[] data = new byte[64 * 3];
            for (int i = 0; i < data.length; i++) {
                data[i] = (byte) i;
            }
            POIFSStream stream = new POIFSStream(ministore, 178);
            stream.updateContents(data);

            // Check
            assertEquals(179, ministore.getNextBlock(178));
            assertEquals(180, ministore.getNextBlock(179));
            assertEquals(POIFSConstants.END_OF_CHAIN, ministore.getNextBlock(180));

            stream = new POIFSStream(ministore, 178);
            Iterator<ByteBuffer> it = stream.getBlockIterator();
            ByteBuffer b178 = it.next();
            ByteBuffer b179 = it.next();
            ByteBuffer b180 = it.next();
            assertFalse(it.hasNext());

            assertEquals((byte) 0x00, b178.get());
            assertEquals((byte) 0x01, b178.get());
            assertEquals((byte) 0x40, b179.get());
            assertEquals((byte) 0x41, b179.get());
            assertEquals((byte) 0x80, b180.get());
            assertEquals((byte) 0x81, b180.get());


            // Try writing just into 3 blocks worth
            data = new byte[64 * 2 + 12];
            for (int i = 0; i < data.length; i++) {
                data[i] = (byte) (i + 4);
            }
            stream = new POIFSStream(ministore, 178);
            stream.updateContents(data);

            // Check
            assertEquals(179, ministore.getNextBlock(178));
            assertEquals(180, ministore.getNextBlock(179));
            assertEquals(POIFSConstants.END_OF_CHAIN, ministore.getNextBlock(180));

            stream = new POIFSStream(ministore, 178);
            it = stream.getBlockIterator();
            b178 = it.next();
            b179 = it.next();
            b180 = it.next();
            assertFalse(it.hasNext());

            assertEquals((byte) 0x04, b178.get(0));
            assertEquals((byte) 0x05, b178.get(1));
            assertEquals((byte) 0x44, b179.get(0));
            assertEquals((byte) 0x45, b179.get(1));
            assertEquals((byte) 0x84, b180.get(0));
            assertEquals((byte) 0x85, b180.get(1));


            // Try writing 1, should truncate
            data = new byte[12];
            for (int i = 0; i < data.length; i++) {
                data[i] = (byte) (i + 9);
            }
            stream = new POIFSStream(ministore, 178);
            stream.updateContents(data);

            assertEquals(POIFSConstants.END_OF_CHAIN, ministore.getNextBlock(178));
            assertEquals(POIFSConstants.UNUSED_BLOCK, ministore.getNextBlock(179));
            assertEquals(POIFSConstants.UNUSED_BLOCK, ministore.getNextBlock(180));

            stream = new POIFSStream(ministore, 178);
            it = stream.getBlockIterator();
            b178 = it.next();
            assertFalse(it.hasNext());

            assertEquals((byte) 0x09, b178.get(0));
            assertEquals((byte) 0x0a, b178.get(1));


            // Try writing 5, should extend
            assertEquals(POIFSConstants.END_OF_CHAIN, ministore.getNextBlock(178));
            assertEquals(POIFSConstants.UNUSED_BLOCK, ministore.getNextBlock(179));
            assertEquals(POIFSConstants.UNUSED_BLOCK, ministore.getNextBlock(180));
            assertEquals(POIFSConstants.UNUSED_BLOCK, ministore.getNextBlock(181));
            assertEquals(POIFSConstants.UNUSED_BLOCK, ministore.getNextBlock(182));
            assertEquals(POIFSConstants.UNUSED_BLOCK, ministore.getNextBlock(183));

            data = new byte[64 * 4 + 12];
            for (int i = 0; i < data.length; i++) {
                data[i] = (byte) (i + 3);
            }
            stream = new POIFSStream(ministore, 178);
            stream.updateContents(data);

            assertEquals(179, ministore.getNextBlock(178));
            assertEquals(180, ministore.getNextBlock(179));
            assertEquals(181, ministore.getNextBlock(180));
            assertEquals(182, ministore.getNextBlock(181));
            assertEquals(POIFSConstants.END_OF_CHAIN, ministore.getNextBlock(182));

            stream = new POIFSStream(ministore, 178);
            it = stream.getBlockIterator();
            b178 = it.next();
            b179 = it.next();
            b180 = it.next();
            ByteBuffer b181 = it.next();
            ByteBuffer b182 = it.next();
            assertFalse(it.hasNext());

            assertEquals((byte) 0x03, b178.get(0));
            assertEquals((byte) 0x04, b178.get(1));
            assertEquals((byte) 0x43, b179.get(0));
            assertEquals((byte) 0x44, b179.get(1));
            assertEquals((byte) 0x83, b180.get(0));
            assertEquals((byte) 0x84, b180.get(1));
            assertEquals((byte) 0xc3, b181.get(0));
            assertEquals((byte) 0xc4, b181.get(1));
            assertEquals((byte) 0x03, b182.get(0));
            assertEquals((byte) 0x04, b182.get(1));


            // Write lots, so it needs another big block
            ministore.getBlockAt(183);
            assertThrows(NoSuchElementException.class, () -> ministore.getBlockAt(184), "Block 184 should be off the end of the list");

            data = new byte[64 * 6 + 12];
            for (int i = 0; i < data.length; i++) {
                data[i] = (byte) (i + 1);
            }
            stream = new POIFSStream(ministore, 178);
            stream.updateContents(data);

            // Should have added 2 more blocks to the chain
            assertEquals(179, ministore.getNextBlock(178));
            assertEquals(180, ministore.getNextBlock(179));
            assertEquals(181, ministore.getNextBlock(180));
            assertEquals(182, ministore.getNextBlock(181));
            assertEquals(183, ministore.getNextBlock(182));
            assertEquals(184, ministore.getNextBlock(183));
            assertEquals(POIFSConstants.END_OF_CHAIN, ministore.getNextBlock(184));
            assertEquals(POIFSConstants.UNUSED_BLOCK, ministore.getNextBlock(185));

            // Block 184 should exist
            ministore.getBlockAt(183);
            ministore.getBlockAt(184);
            ministore.getBlockAt(185);

            // Check contents
            stream = new POIFSStream(ministore, 178);
            it = stream.getBlockIterator();
            b178 = it.next();
            b179 = it.next();
            b180 = it.next();
            b181 = it.next();
            b182 = it.next();
            ByteBuffer b183 = it.next();
            ByteBuffer b184 = it.next();
            assertFalse(it.hasNext());

            assertEquals((byte) 0x01, b178.get(0));
            assertEquals((byte) 0x02, b178.get(1));
            assertEquals((byte) 0x41, b179.get(0));
            assertEquals((byte) 0x42, b179.get(1));
            assertEquals((byte) 0x81, b180.get(0));
            assertEquals((byte) 0x82, b180.get(1));
            assertEquals((byte) 0xc1, b181.get(0));
            assertEquals((byte) 0xc2, b181.get(1));
            assertEquals((byte) 0x01, b182.get(0));
            assertEquals((byte) 0x02, b182.get(1));
            assertEquals((byte) 0x41, b183.get(0));
            assertEquals((byte) 0x42, b183.get(1));
            assertEquals((byte) 0x81, b184.get(0));
            assertEquals((byte) 0x82, b184.get(1));

        }
    }

    /**
     * Craft a nasty file with a loop, and ensure we don't get stuck
     */
    @Test
    void testWriteFailsOnLoop() throws Exception {
        try (POIFSFileSystem fs = new POIFSFileSystem(_inst.getFile("BlockSize512.zvi"))) {

            // Hack the FAT so that it goes 0->1->2->0
            fs.setNextBlock(0, 1);
            fs.setNextBlock(1, 2);
            fs.setNextBlock(2, 0);

            // Try to write a large amount, should fail on the write
            POIFSStream stream1 = new POIFSStream(fs, 0);
            assertThrows(IllegalStateException.class,
                () -> stream1.updateContents(new byte[512 * 4]), "Loop should have been detected but wasn't!");

            // Now reset, and try on a small bit
            // Should fail during the freeing set
            fs.setNextBlock(0, 1);
            fs.setNextBlock(1, 2);
            fs.setNextBlock(2, 0);

            POIFSStream stream2 = new POIFSStream(fs, 0);
            assertThrows(IllegalStateException.class,
                () -> stream2.updateContents(new byte[512]), "Loop should have been detected but wasn't!");
        }
    }

    /**
     * Tests adding a new stream, writing and reading it.
     */
    @Test
    void testReadWriteNewStream() throws Exception {
        try (POIFSFileSystem fs = new POIFSFileSystem()) {
            POIFSStream stream = new POIFSStream(fs);

            // Check our filesystem has Properties then BAT
            assertEquals(2, fs.getFreeBlock());
            BATBlock bat = fs.getBATBlockAndIndex(0).getBlock();
            assertEquals(POIFSConstants.END_OF_CHAIN, bat.getValueAt(0));
            assertEquals(POIFSConstants.FAT_SECTOR_BLOCK, bat.getValueAt(1));
            assertEquals(POIFSConstants.UNUSED_BLOCK, bat.getValueAt(2));

            // Check the stream as-is
            assertEquals(POIFSConstants.END_OF_CHAIN, stream.getStartBlock());
            assertThrows(IllegalStateException.class, stream::getBlockIterator,
                "Shouldn't be able to get an iterator before writing");

            // Write in two blocks
            byte[] data = new byte[512 + 20];
            for (int i = 0; i < 512; i++) {
                data[i] = (byte) (i % 256);
            }
            for (int i = 512; i < data.length; i++) {
                data[i] = (byte) (i % 256 + 100);
            }
            stream.updateContents(data);

            // Check now
            assertEquals(4, fs.getFreeBlock());
            bat = fs.getBATBlockAndIndex(0).getBlock();
            assertEquals(POIFSConstants.END_OF_CHAIN, bat.getValueAt(0));
            assertEquals(POIFSConstants.FAT_SECTOR_BLOCK, bat.getValueAt(1));
            assertEquals(3, bat.getValueAt(2));
            assertEquals(POIFSConstants.END_OF_CHAIN, bat.getValueAt(3));
            assertEquals(POIFSConstants.UNUSED_BLOCK, bat.getValueAt(4));


            Iterator<ByteBuffer> it = stream.getBlockIterator();
            assertTrue(it.hasNext());
            ByteBuffer b = it.next();

            byte[] read = new byte[512];
            b.get(read);
            for (int i = 0; i < read.length; i++) {
                assertEquals(data[i], read[i], "Wrong value at " + i);
            }

            assertTrue(it.hasNext());
            b = it.next();

            read = new byte[512];
            b.get(read);
            for (int i = 0; i < 20; i++) {
                assertEquals(data[i + 512], read[i]);
            }
            for (int i = 20; i < read.length; i++) {
                assertEquals(0, read[i]);
            }

            assertFalse(it.hasNext());
        }
    }

    /**
     * Writes a stream, then replaces it
     */
    @Test
    void testWriteThenReplace() throws Exception {
        try (POIFSFileSystem fs1 = new POIFSFileSystem()) {

            // Starts empty, other that Properties and BAT
            BATBlock bat = fs1.getBATBlockAndIndex(0).getBlock();
            assertEquals(POIFSConstants.END_OF_CHAIN, bat.getValueAt(0));
            assertEquals(POIFSConstants.FAT_SECTOR_BLOCK, bat.getValueAt(1));
            assertEquals(POIFSConstants.UNUSED_BLOCK, bat.getValueAt(2));

            // Write something that uses a main stream
            byte[] main4106 = new byte[4106];
            main4106[0] = -10;
            main4106[4105] = -11;
            fs1.getRoot().createDocument("Normal", new ByteArrayInputStream(main4106));

            // Should have used 9 blocks
            assertEquals(POIFSConstants.END_OF_CHAIN, bat.getValueAt(0));
            assertEquals(POIFSConstants.FAT_SECTOR_BLOCK, bat.getValueAt(1));
            assertEquals(3, bat.getValueAt(2));
            assertEquals(4, bat.getValueAt(3));
            assertEquals(5, bat.getValueAt(4));
            assertEquals(6, bat.getValueAt(5));
            assertEquals(7, bat.getValueAt(6));
            assertEquals(8, bat.getValueAt(7));
            assertEquals(9, bat.getValueAt(8));
            assertEquals(10, bat.getValueAt(9));
            assertEquals(POIFSConstants.END_OF_CHAIN, bat.getValueAt(10));
            assertEquals(POIFSConstants.UNUSED_BLOCK, bat.getValueAt(11));

            DocumentEntry normal = (DocumentEntry) fs1.getRoot().getEntryCaseInsensitive("Normal");
            assertEquals(4106, normal.getSize());
            assertEquals(4106, ((DocumentNode) normal).getProperty().getSize());


            // Replace with one still big enough for a main stream, but one block smaller
            byte[] main4096 = new byte[4096];
            main4096[0] = -10;
            main4096[4095] = -11;

            try (DocumentOutputStream nout = new DocumentOutputStream(normal)) {
                nout.write(main4096);
            }

            // Will have dropped to 8
            assertEquals(POIFSConstants.END_OF_CHAIN, bat.getValueAt(0));
            assertEquals(POIFSConstants.FAT_SECTOR_BLOCK, bat.getValueAt(1));
            assertEquals(3, bat.getValueAt(2));
            assertEquals(4, bat.getValueAt(3));
            assertEquals(5, bat.getValueAt(4));
            assertEquals(6, bat.getValueAt(5));
            assertEquals(7, bat.getValueAt(6));
            assertEquals(8, bat.getValueAt(7));
            assertEquals(9, bat.getValueAt(8));
            assertEquals(POIFSConstants.END_OF_CHAIN, bat.getValueAt(9));
            assertEquals(POIFSConstants.UNUSED_BLOCK, bat.getValueAt(10));
            assertEquals(POIFSConstants.UNUSED_BLOCK, bat.getValueAt(11));

            normal = (DocumentEntry) fs1.getRoot().getEntryCaseInsensitive("Normal");
            assertEquals(4096, normal.getSize());
            assertEquals(4096, ((DocumentNode) normal).getProperty().getSize());


            // Write and check
            try (POIFSFileSystem fs2 = writeOutAndReadBack(fs1)) {
                bat = fs2.getBATBlockAndIndex(0).getBlock();

                // No change after write
                assertEquals(POIFSConstants.END_OF_CHAIN, bat.getValueAt(0)); // Properties
                assertEquals(POIFSConstants.FAT_SECTOR_BLOCK, bat.getValueAt(1));
                assertEquals(3, bat.getValueAt(2));
                assertEquals(4, bat.getValueAt(3));
                assertEquals(5, bat.getValueAt(4));
                assertEquals(6, bat.getValueAt(5));
                assertEquals(7, bat.getValueAt(6));
                assertEquals(8, bat.getValueAt(7));
                assertEquals(9, bat.getValueAt(8));
                assertEquals(POIFSConstants.END_OF_CHAIN, bat.getValueAt(9)); // End of Normal
                assertEquals(POIFSConstants.UNUSED_BLOCK, bat.getValueAt(10));
                assertEquals(POIFSConstants.UNUSED_BLOCK, bat.getValueAt(11));

                normal = (DocumentEntry) fs2.getRoot().getEntryCaseInsensitive("Normal");
                assertEquals(4096, normal.getSize());
                assertEquals(4096, ((DocumentNode) normal).getProperty().getSize());


                // Make longer, take 1 block at the end
                normal = (DocumentEntry) fs2.getRoot().getEntryCaseInsensitive("Normal");
                try (DocumentOutputStream nout = new DocumentOutputStream(normal)) {
                    nout.write(main4106);
                }

                assertEquals(POIFSConstants.END_OF_CHAIN, bat.getValueAt(0));
                assertEquals(POIFSConstants.FAT_SECTOR_BLOCK, bat.getValueAt(1));
                assertEquals(3, bat.getValueAt(2));
                assertEquals(4, bat.getValueAt(3));
                assertEquals(5, bat.getValueAt(4));
                assertEquals(6, bat.getValueAt(5));
                assertEquals(7, bat.getValueAt(6));
                assertEquals(8, bat.getValueAt(7));
                assertEquals(9, bat.getValueAt(8));
                assertEquals(10, bat.getValueAt(9));
                assertEquals(POIFSConstants.END_OF_CHAIN, bat.getValueAt(10)); // Normal
                assertEquals(POIFSConstants.UNUSED_BLOCK, bat.getValueAt(11));
                assertEquals(POIFSConstants.UNUSED_BLOCK, bat.getValueAt(12));

                normal = (DocumentEntry) fs2.getRoot().getEntryCaseInsensitive("Normal");
                assertEquals(4106, normal.getSize());
                assertEquals(4106, ((DocumentNode) normal).getProperty().getSize());


                // Make it small, will trigger the SBAT stream and free lots up
                byte[] mini = new byte[]{42, 0, 1, 2, 3, 4, 42};
                normal = (DocumentEntry) fs2.getRoot().getEntryCaseInsensitive("Normal");
                try (DocumentOutputStream nout = new DocumentOutputStream(normal)) {
                    nout.write(mini);
                }

                assertEquals(POIFSConstants.END_OF_CHAIN, bat.getValueAt(0));
                assertEquals(POIFSConstants.FAT_SECTOR_BLOCK, bat.getValueAt(1));
                assertEquals(POIFSConstants.END_OF_CHAIN, bat.getValueAt(2)); // SBAT
                assertEquals(POIFSConstants.END_OF_CHAIN, bat.getValueAt(3)); // Mini Stream
                assertEquals(POIFSConstants.UNUSED_BLOCK, bat.getValueAt(4));
                assertEquals(POIFSConstants.UNUSED_BLOCK, bat.getValueAt(5));
                assertEquals(POIFSConstants.UNUSED_BLOCK, bat.getValueAt(6));
                assertEquals(POIFSConstants.UNUSED_BLOCK, bat.getValueAt(7));
                assertEquals(POIFSConstants.UNUSED_BLOCK, bat.getValueAt(8));
                assertEquals(POIFSConstants.UNUSED_BLOCK, bat.getValueAt(9));
                assertEquals(POIFSConstants.UNUSED_BLOCK, bat.getValueAt(10));
                assertEquals(POIFSConstants.UNUSED_BLOCK, bat.getValueAt(11));
                assertEquals(POIFSConstants.UNUSED_BLOCK, bat.getValueAt(12));

                normal = (DocumentEntry) fs2.getRoot().getEntryCaseInsensitive("Normal");
                assertEquals(7, normal.getSize());
                assertEquals(7, ((DocumentNode) normal).getProperty().getSize());


                // Finally back to big again
                try (DocumentOutputStream nout = new DocumentOutputStream(normal)) {
                    nout.write(main4096);
                }

                // Will keep the mini stream, now empty
                assertEquals(POIFSConstants.END_OF_CHAIN, bat.getValueAt(0));
                assertEquals(POIFSConstants.FAT_SECTOR_BLOCK, bat.getValueAt(1));
                assertEquals(POIFSConstants.END_OF_CHAIN, bat.getValueAt(2)); // SBAT
                assertEquals(POIFSConstants.END_OF_CHAIN, bat.getValueAt(3)); // Mini Stream
                assertEquals(5, bat.getValueAt(4));
                assertEquals(6, bat.getValueAt(5));
                assertEquals(7, bat.getValueAt(6));
                assertEquals(8, bat.getValueAt(7));
                assertEquals(9, bat.getValueAt(8));
                assertEquals(10, bat.getValueAt(9));
                assertEquals(11, bat.getValueAt(10));
                assertEquals(POIFSConstants.END_OF_CHAIN, bat.getValueAt(11));
                assertEquals(POIFSConstants.UNUSED_BLOCK, bat.getValueAt(12));
                assertEquals(POIFSConstants.UNUSED_BLOCK, bat.getValueAt(13));

                normal = (DocumentEntry) fs2.getRoot().getEntryCaseInsensitive("Normal");
                assertEquals(4096, normal.getSize());
                assertEquals(4096, ((DocumentNode) normal).getProperty().getSize());


                // Save, re-load, re-check
                try (POIFSFileSystem fs3 = writeOutAndReadBack(fs2)) {
                    bat = fs3.getBATBlockAndIndex(0).getBlock();

                    assertEquals(POIFSConstants.END_OF_CHAIN, bat.getValueAt(0));
                    assertEquals(POIFSConstants.FAT_SECTOR_BLOCK, bat.getValueAt(1));
                    assertEquals(POIFSConstants.END_OF_CHAIN, bat.getValueAt(2)); // SBAT
                    assertEquals(POIFSConstants.END_OF_CHAIN, bat.getValueAt(3)); // Mini Stream
                    assertEquals(5, bat.getValueAt(4));
                    assertEquals(6, bat.getValueAt(5));
                    assertEquals(7, bat.getValueAt(6));
                    assertEquals(8, bat.getValueAt(7));
                    assertEquals(9, bat.getValueAt(8));
                    assertEquals(10, bat.getValueAt(9));
                    assertEquals(11, bat.getValueAt(10));
                    assertEquals(POIFSConstants.END_OF_CHAIN, bat.getValueAt(11));
                    assertEquals(POIFSConstants.UNUSED_BLOCK, bat.getValueAt(12));
                    assertEquals(POIFSConstants.UNUSED_BLOCK, bat.getValueAt(13));

                    normal = (DocumentEntry) fs3.getRoot().getEntryCaseInsensitive("Normal");
                    assertEquals(4096, normal.getSize());
                    assertEquals(4096, ((DocumentNode) normal).getProperty().getSize());
                }
            }
        }
    }


    /**
     * Returns test files with 512 byte and 4k block sizes, loaded
     * both from InputStreams and Files
     */
    public static Collection<Arguments> get512and4kFileAndInput() {
        return CollectionUtils.union(get512FileAndInput(), get4kFileAndInput());
    }

    public static List<Arguments> get512FileAndInput() {
        return Arrays.asList(
            Arguments.of("BlockSize512.zvi", (Function<String,POIFSFileSystem>)TestPOIFSStream::openAsFile),
            Arguments.of("BlockSize512.zvi", (Function<String,POIFSFileSystem>)TestPOIFSStream::openAsStream)
        );
    }

    public static List<Arguments> get4kFileAndInput() {
        return Arrays.asList(
            Arguments.of("BlockSize4096.zvi", (Function<String,POIFSFileSystem>)TestPOIFSStream::openAsFile),
            Arguments.of("BlockSize4096.zvi", (Function<String,POIFSFileSystem>)TestPOIFSStream::openAsStream)
        );
    }

    private static POIFSFileSystem openAsFile(String fileName) {
        try {
            return new POIFSFileSystem(_inst.getFile(fileName));
        } catch (IOException e) {
            fail(e);
            return null;
        }
    }

    private static POIFSFileSystem openAsStream(String fileName) {
        try {
            return new POIFSFileSystem(_inst.openResourceAsStream(fileName));
        } catch (IOException e) {
            fail(e);
            return null;
        }
    }


    private static void assertBATCount(POIFSFileSystem fs, int expectedBAT, int expectedXBAT) throws IOException {
        int foundBAT = 0;
        int foundXBAT = 0;
        int sz = (int) (fs.size() / fs.getBigBlockSize());
        for (int i = 0; i < sz; i++) {
            if (fs.getNextBlock(i) == POIFSConstants.FAT_SECTOR_BLOCK) {
                foundBAT++;
            }
            if (fs.getNextBlock(i) == POIFSConstants.DIFAT_SECTOR_BLOCK) {
                foundXBAT++;
            }
        }
        assertEquals(expectedBAT, foundBAT, "Wrong number of BATs");
        assertEquals(expectedXBAT, foundXBAT, "Wrong number of XBATs with " + expectedBAT + " BATs");
    }

    private void assertContentsMatches(byte[] expected, DocumentEntry doc) throws IOException {
        DocumentInputStream inp = new DocumentInputStream(doc);
        byte[] contents = new byte[doc.getSize()];
        assertEquals(doc.getSize(), inp.read(contents));
        inp.close();

        if (expected != null) {
            assertThat(expected, equalTo(contents));
        }
    }

    private static HeaderBlock writeOutAndReadHeader(POIFSFileSystem fs) throws IOException {
        UnsynchronizedByteArrayOutputStream baos = UnsynchronizedByteArrayOutputStream.builder().get();
        fs.writeFilesystem(baos);
        return new HeaderBlock(baos.toInputStream());
    }

    private static POIFSFileSystem writeOutFileAndReadBack(POIFSFileSystem original) throws IOException {
        final File file = TempFile.createTempFile("TestPOIFS", ".ole2");
        try (OutputStream fout = new FileOutputStream(file)) {
            original.writeFilesystem(fout);
        }
        return new POIFSFileSystem(file, false);
    }

    @ParameterizedTest()
    @MethodSource("get512FileAndInput")
    void basicOpen512(String file, Function<String,POIFSFileSystem> opener) throws IOException {
        // With a simple 512 block file
        try (POIFSFileSystem fs = opener.apply(file)) {
            assertEquals(512, fs.getBigBlockSize());
        }
    }

    @ParameterizedTest()
    @MethodSource("get4kFileAndInput")
    void basicOpen4k(String file, Function<String,POIFSFileSystem> opener) throws IOException {
        // Now with a simple 4096 block file
        try (POIFSFileSystem fs = opener.apply(file)) {
            assertEquals(4096, fs.getBigBlockSize());
        }
    }

    @ParameterizedTest()
    @MethodSource("get512FileAndInput")
    void propertiesAndFatOnRead512(String file, Function<String,POIFSFileSystem> opener) throws IOException {
        // With a simple 512 block file
        try (POIFSFileSystem fs = opener.apply(file)) {
            // Check the FAT was properly processed:
            // Verify we only got one block
            fs.getBATBlockAndIndex(0);
            fs.getBATBlockAndIndex(1);
            assertThrows(IndexOutOfBoundsException.class, () -> fs.getBATBlockAndIndex(140),
                "Should only be one BAT, but a 2nd was found");

            // Verify a few next offsets
            // 97 -> 98 -> END
            assertEquals(98, fs.getNextBlock(97));
            assertEquals(POIFSConstants.END_OF_CHAIN, fs.getNextBlock(98));


            // Check the properties
            PropertyTable props = fs._get_property_table();
            assertEquals(90, props.getStartBlock());
            assertEquals(7, props.countBlocks());

            // Root property tells us about the Mini Stream
            RootProperty root = props.getRoot();
            assertEquals("Root Entry", root.getName());
            assertEquals(11564, root.getSize());
            assertEquals(0, root.getStartBlock());

            // Check its children too
            Property prop;
            Iterator<Property> pi = root.getChildren();
            prop = pi.next();
            assertEquals("Thumbnail", prop.getName());
            prop = pi.next();
            assertEquals("\u0005DocumentSummaryInformation", prop.getName());
            prop = pi.next();
            assertEquals("\u0005SummaryInformation", prop.getName());
            prop = pi.next();
            assertEquals("Image", prop.getName());
            prop = pi.next();
            assertEquals("Tags", prop.getName());
            assertFalse(pi.hasNext());


            // Check the SBAT (Small Blocks FAT) was properly processed
            POIFSMiniStore ministore = fs.getMiniStore();

            // Verify we only got two SBAT blocks
            ministore.getBATBlockAndIndex(0);
            ministore.getBATBlockAndIndex(128);
            assertThrows(IndexOutOfBoundsException.class, () -> ministore.getBATBlockAndIndex(256),
                "Should only be two SBATs, but a 3rd was found");

            // Verify a few offsets: 0->50 is a stream
            for (int i = 0; i < 50; i++) {
                assertEquals(i + 1, ministore.getNextBlock(i));
            }
            assertEquals(POIFSConstants.END_OF_CHAIN, ministore.getNextBlock(50));
        }
    }

    @ParameterizedTest()
    @MethodSource("get4kFileAndInput")
    void propertiesAndFatOnRead4k(String file, Function<String,POIFSFileSystem> opener) throws IOException {
        // Now with a simple 4096 block file
        try (POIFSFileSystem fs = opener.apply(file)) {
            // Check the FAT was properly processed
            // Verify we only got one block
            fs.getBATBlockAndIndex(0);
            fs.getBATBlockAndIndex(1);
            assertThrows(IndexOutOfBoundsException.class, () -> fs.getBATBlockAndIndex(1040),
                "Should only be one BAT, but a 2nd was found");

            // Verify a few next offsets
            // 0 -> 1 -> 2 -> END
            assertEquals(1, fs.getNextBlock(0));
            assertEquals(2, fs.getNextBlock(1));
            assertEquals(POIFSConstants.END_OF_CHAIN, fs.getNextBlock(2));


            // Check the properties
            PropertyTable props = fs._get_property_table();
            assertEquals(12, props.getStartBlock());
            assertEquals(1, props.countBlocks());

            // Root property tells us about the Mini Stream
            RootProperty root = props.getRoot();
            assertEquals("Root Entry", root.getName());
            assertEquals(11564, root.getSize());
            assertEquals(0, root.getStartBlock());

            // Check its children too
            Property prop;
            Iterator<Property> pi = root.getChildren();
            prop = pi.next();
            assertEquals("Thumbnail", prop.getName());
            prop = pi.next();
            assertEquals("\u0005DocumentSummaryInformation", prop.getName());
            prop = pi.next();
            assertEquals("\u0005SummaryInformation", prop.getName());
            prop = pi.next();
            assertEquals("Image", prop.getName());
            prop = pi.next();
            assertEquals("Tags", prop.getName());
            assertFalse(pi.hasNext());


            // Check the SBAT (Small Blocks FAT) was properly processed
            POIFSMiniStore ministore = fs.getMiniStore();

            // Verify we only got one SBAT block
            ministore.getBATBlockAndIndex(0);
            ministore.getBATBlockAndIndex(128);
            ministore.getBATBlockAndIndex(1023);
            assertThrows(IndexOutOfBoundsException.class, () -> ministore.getBATBlockAndIndex(1024),
                "Should only be one SBAT, but a 2nd was found");

            // Verify a few offsets: 0->50 is a stream
            for (int i = 0; i < 50; i++) {
                assertEquals(i + 1, ministore.getNextBlock(i));
            }
            assertEquals(POIFSConstants.END_OF_CHAIN, ministore.getNextBlock(50));
        }
    }

    /**
     * Check that for a given block, we can correctly figure
     * out what the next one is
     */
    @ParameterizedTest()
    @MethodSource("get512FileAndInput")
    void nextBlock512(String file, Function<String,POIFSFileSystem> opener) throws IOException {
        try (POIFSFileSystem fs = opener.apply(file)) {
            // 0 -> 21 are simple
            for (int i = 0; i < 21; i++) {
                assertEquals(i + 1, fs.getNextBlock(i));
            }
            // 21 jumps to 89, then ends
            assertEquals(89, fs.getNextBlock(21));
            assertEquals(POIFSConstants.END_OF_CHAIN, fs.getNextBlock(89));

            // 22 -> 88 simple sequential stream
            for (int i = 22; i < 88; i++) {
                assertEquals(i + 1, fs.getNextBlock(i));
            }
            assertEquals(POIFSConstants.END_OF_CHAIN, fs.getNextBlock(88));

            // 90 -> 96 is another stream
            for (int i = 90; i < 96; i++) {
                assertEquals(i + 1, fs.getNextBlock(i));
            }
            assertEquals(POIFSConstants.END_OF_CHAIN, fs.getNextBlock(96));

            // 97+98 is another
            assertEquals(98, fs.getNextBlock(97));
            assertEquals(POIFSConstants.END_OF_CHAIN, fs.getNextBlock(98));

            // 99 is our FAT block
            assertEquals(POIFSConstants.FAT_SECTOR_BLOCK, fs.getNextBlock(99));

            // 100 onwards is free
            for (int i = 100; i < fs.getBigBlockSizeDetails().getBATEntriesPerBlock(); i++) {
                assertEquals(POIFSConstants.UNUSED_BLOCK, fs.getNextBlock(i));
            }
        }
    }

    @ParameterizedTest()
    @MethodSource("get4kFileAndInput")
    void nextBlock4k(String file, Function<String,POIFSFileSystem> opener) throws IOException {
        // Quick check on 4096 byte blocks too
        try (POIFSFileSystem fs = opener.apply(file)) {
            // 0 -> 1 -> 2 -> end
            assertEquals(1, fs.getNextBlock(0));
            assertEquals(2, fs.getNextBlock(1));
            assertEquals(POIFSConstants.END_OF_CHAIN, fs.getNextBlock(2));

            // 4 -> 11 then end
            for (int i = 4; i < 11; i++) {
                assertEquals(i + 1, fs.getNextBlock(i));
            }
            assertEquals(POIFSConstants.END_OF_CHAIN, fs.getNextBlock(11));
        }
    }

    /**
     * Check we get the right data back for each block
     */
    @ParameterizedTest()
    @MethodSource("get512FileAndInput")
    void getBlock512(String file, Function<String,POIFSFileSystem> opener) throws IOException {
        try (POIFSFileSystem fs = opener.apply(file)) {
            // The 0th block is the first data block
            ByteBuffer b = fs.getBlockAt(0);
            assertEquals((byte) 0x9e, b.get());
            assertEquals((byte) 0x75, b.get());
            assertEquals((byte) 0x97, b.get());
            assertEquals((byte) 0xf6, b.get());

            // And the next block
            b = fs.getBlockAt(1);
            assertEquals((byte) 0x86, b.get());
            assertEquals((byte) 0x09, b.get());
            assertEquals((byte) 0x22, b.get());
            assertEquals((byte) 0xfb, b.get());

            // Check the final block too
            b = fs.getBlockAt(99);
            assertEquals((byte) 0x01, b.get());
            assertEquals((byte) 0x00, b.get());
            assertEquals((byte) 0x00, b.get());
            assertEquals((byte) 0x00, b.get());
            assertEquals((byte) 0x02, b.get());
            assertEquals((byte) 0x00, b.get());
            assertEquals((byte) 0x00, b.get());
            assertEquals((byte) 0x00, b.get());
        }
    }

    @ParameterizedTest()
    @MethodSource("get4kFileAndInput")
    void getBlock4k(String file, Function<String,POIFSFileSystem> opener) throws IOException {
        // Quick check on 4096 byte blocks too
        try (POIFSFileSystem fs = opener.apply(file)) {
            // The 0th block is the first data block
            ByteBuffer b = fs.getBlockAt(0);
            assertEquals((byte) 0x9e, b.get());
            assertEquals((byte) 0x75, b.get());
            assertEquals((byte) 0x97, b.get());
            assertEquals((byte) 0xf6, b.get());

            // And the next block
            b = fs.getBlockAt(1);
            assertEquals((byte) 0x00, b.get());
            assertEquals((byte) 0x00, b.get());
            assertEquals((byte) 0x03, b.get());
            assertEquals((byte) 0x00, b.get());

            // The 14th block is the FAT
            b = fs.getBlockAt(14);
            assertEquals((byte) 0x01, b.get());
            assertEquals((byte) 0x00, b.get());
            assertEquals((byte) 0x00, b.get());
            assertEquals((byte) 0x00, b.get());
            assertEquals((byte) 0x02, b.get());
            assertEquals((byte) 0x00, b.get());
            assertEquals((byte) 0x00, b.get());
            assertEquals((byte) 0x00, b.get());
        }
    }

    /**
     * Ask for free blocks where there are some already
     * to be had from the FAT
     */
    @Test
    void getFreeBlockWithSpare() throws IOException {
        try (POIFSFileSystem fs = new POIFSFileSystem(_inst.getFile("BlockSize512.zvi"))) {
            // Our first BAT block has spares
            assertTrue(fs.getBATBlockAndIndex(0).getBlock().hasFreeSectors());

            // First free one is 100
            assertEquals(POIFSConstants.UNUSED_BLOCK, fs.getNextBlock(100));
            assertEquals(POIFSConstants.UNUSED_BLOCK, fs.getNextBlock(101));
            assertEquals(POIFSConstants.UNUSED_BLOCK, fs.getNextBlock(102));
            assertEquals(POIFSConstants.UNUSED_BLOCK, fs.getNextBlock(103));

            // Ask, will get 100
            assertEquals(100, fs.getFreeBlock());

            // Ask again, will still get 100 as not written to
            assertEquals(100, fs.getFreeBlock());

            // Allocate it, then ask again
            fs.setNextBlock(100, POIFSConstants.END_OF_CHAIN);
            assertEquals(101, fs.getFreeBlock());
        }
    }

    /**
     * Ask for free blocks where no free ones exist, and so the
     * file needs to be extended and another BAT/XBAT added
     */
    @Test
    void getFreeBlockWithNoneSpare() throws IOException {
        try (POIFSFileSystem fs1 = new POIFSFileSystem(_inst.openResourceAsStream("BlockSize512.zvi"))) {
            int free;

            // We have one BAT at block 99
            assertEquals(POIFSConstants.FAT_SECTOR_BLOCK, fs1.getNextBlock(99));
            assertBATCount(fs1, 1, 0);

            // We've spare ones from 100 to 128
            for (int i = 100; i < 128; i++) {
                assertEquals(POIFSConstants.UNUSED_BLOCK, fs1.getNextBlock(i));
            }

            // Check our BAT knows it's free
            assertTrue(fs1.getBATBlockAndIndex(0).getBlock().hasFreeSectors());

            // Allocate all the spare ones
            for (int i = 100; i < 128; i++) {
                fs1.setNextBlock(i, POIFSConstants.END_OF_CHAIN);
            }

            // BAT is now full, but there's only the one
            assertFalse(fs1.getBATBlockAndIndex(0).getBlock().hasFreeSectors());
            assertThrows(IndexOutOfBoundsException.class, () -> fs1.getBATBlockAndIndex(128), "Should only be one BAT");
            assertBATCount(fs1, 1, 0);


            // Now ask for a free one, will need to extend the file
            assertEquals(129, fs1.getFreeBlock());

            assertFalse(fs1.getBATBlockAndIndex(0).getBlock().hasFreeSectors());
            assertTrue(fs1.getBATBlockAndIndex(128).getBlock().hasFreeSectors());
            assertEquals(POIFSConstants.FAT_SECTOR_BLOCK, fs1.getNextBlock(128));
            assertEquals(POIFSConstants.UNUSED_BLOCK, fs1.getNextBlock(129));

            // We now have 2 BATs, but no XBATs
            assertBATCount(fs1, 2, 0);


            // Fill up to hold 109 BAT blocks
            for (int i = 0; i < 109; i++) {
                fs1.getFreeBlock();
                int startOffset = i * 128;
                while (fs1.getBATBlockAndIndex(startOffset).getBlock().hasFreeSectors()) {
                    free = fs1.getFreeBlock();
                    fs1.setNextBlock(free, POIFSConstants.END_OF_CHAIN);
                }
            }

            assertFalse(fs1.getBATBlockAndIndex(109 * 128 - 1).getBlock().hasFreeSectors());
            assertThrows(IndexOutOfBoundsException.class, () -> fs1.getBATBlockAndIndex(109 * 128), "Should only be 109 BATs");

            // We now have 109 BATs, but no XBATs
            assertBATCount(fs1, 109, 0);


            // Ask for it to be written out, and check the header
            HeaderBlock header = writeOutAndReadHeader(fs1);
            assertEquals(109, header.getBATCount());
            assertEquals(0, header.getXBATCount());


            // Ask for another, will get our first XBAT
            free = fs1.getFreeBlock();
            assertTrue(free > 0, "Had: " + free);

            assertFalse(fs1.getBATBlockAndIndex(109 * 128 - 1).getBlock().hasFreeSectors());
            assertTrue(fs1.getBATBlockAndIndex(110 * 128 - 1).getBlock().hasFreeSectors());
            assertThrows(IndexOutOfBoundsException.class, () -> fs1.getBATBlockAndIndex(110 * 128), "Should only be 110 BATs");
            assertBATCount(fs1, 110, 1);

            header = writeOutAndReadHeader(fs1);
            assertEquals(110, header.getBATCount());
            assertEquals(1, header.getXBATCount());


            // Fill the XBAT, which means filling 127 BATs
            for (int i = 109; i < 109 + 127; i++) {
                fs1.getFreeBlock();
                int startOffset = i * 128;
                while (fs1.getBATBlockAndIndex(startOffset).getBlock().hasFreeSectors()) {
                    free = fs1.getFreeBlock();
                    fs1.setNextBlock(free, POIFSConstants.END_OF_CHAIN);
                }
                assertBATCount(fs1, i + 1, 1);
            }

            // Should now have 109+127 = 236 BATs
            assertFalse(fs1.getBATBlockAndIndex(236 * 128 - 1).getBlock().hasFreeSectors());
            assertThrows(IndexOutOfBoundsException.class, () -> fs1.getBATBlockAndIndex(236 * 128), "Should only be 236 BATs");
            assertBATCount(fs1, 236, 1);


            // Ask for another, will get our 2nd XBAT
            free = fs1.getFreeBlock();
            assertTrue(free > 0, "Had: " + free);

            assertFalse(fs1.getBATBlockAndIndex(236 * 128 - 1).getBlock().hasFreeSectors());
            assertTrue(fs1.getBATBlockAndIndex(237 * 128 - 1).getBlock().hasFreeSectors());
            assertThrows(IndexOutOfBoundsException.class, () -> fs1.getBATBlockAndIndex(237 * 128), "Should only be 237 BATs");

            // Check the counts now
            assertBATCount(fs1, 237, 2);

            // Check the header
            header = writeOutAndReadHeader(fs1);
            assertNotNull(header);

            // Now, write it out, and read it back in again fully
            try (POIFSFileSystem fs2 = writeOutAndReadBack(fs1)) {

                // Check that it is seen correctly
                assertBATCount(fs2, 237, 2);

                assertFalse(fs2.getBATBlockAndIndex(236 * 128 - 1).getBlock().hasFreeSectors());
                assertTrue(fs2.getBATBlockAndIndex(237 * 128 - 1).getBlock().hasFreeSectors());
                assertThrows(IndexOutOfBoundsException.class, () -> fs2.getBATBlockAndIndex(237 * 128), "Should only be 237 BATs");
            }
        }
    }

    /**
     * Test that we can correctly get the list of directory
     * entries, and the details on the files in them
     */
    @ParameterizedTest
    @MethodSource("get512and4kFileAndInput")
    void listEntries(String file, Function<String,POIFSFileSystem> opener) throws IOException {
        try (POIFSFileSystem fs = opener.apply(file)) {
            DirectoryEntry root = fs.getRoot();
            assertEquals(5, root.getEntryCount());

            // Check by the names
            Entry thumbnail = root.getEntryCaseInsensitive("Thumbnail");
            Entry dsi = root.getEntryCaseInsensitive("\u0005DocumentSummaryInformation");
            Entry si = root.getEntryCaseInsensitive("\u0005SummaryInformation");
            Entry image = root.getEntryCaseInsensitive("Image");
            Entry tags = root.getEntryCaseInsensitive("Tags");

            assertFalse(thumbnail.isDirectoryEntry());
            assertFalse(dsi.isDirectoryEntry());
            assertFalse(si.isDirectoryEntry());
            assertTrue(image.isDirectoryEntry());
            assertFalse(tags.isDirectoryEntry());

            // Check via the iterator
            Iterator<Entry> it = root.getEntries();
            assertEquals(thumbnail.getName(), it.next().getName());
            assertEquals(dsi.getName(), it.next().getName());
            assertEquals(si.getName(), it.next().getName());
            assertEquals(image.getName(), it.next().getName());
            assertEquals(tags.getName(), it.next().getName());

            // Look inside another
            DirectoryEntry imageD = (DirectoryEntry) image;
            assertEquals(7, imageD.getEntryCount());
        }
    }

    /**
     * Tests that we can get the correct contents for
     * a document in the filesystem
     */
    @ParameterizedTest
    @MethodSource("get512and4kFileAndInput")
    void getDocumentEntry(String file, Function<String,POIFSFileSystem> opener)
    throws IOException, NoPropertySetStreamException {
        try (POIFSFileSystem fs = opener.apply(file)) {
            DirectoryEntry root = fs.getRoot();
            Entry si = root.getEntryCaseInsensitive("\u0005SummaryInformation");

            assertTrue(si.isDocumentEntry());
            DocumentNode doc = (DocumentNode) si;

            // Check we can read it
            assertContentsMatches(null, doc);

            // Now try to build the property set
            try (DocumentInputStream inp = new DocumentInputStream(doc)) {
                PropertySet ps = PropertySetFactory.create(inp);
                SummaryInformation inf = (SummaryInformation) ps;

                // Check some bits in it
                assertNull(inf.getApplicationName());
                assertNull(inf.getAuthor());
                assertNull(inf.getSubject());
                assertEquals(131333, inf.getOSVersion());
            }


            // Try the other summary information
            si = root.getEntryCaseInsensitive("\u0005DocumentSummaryInformation");
            assertTrue(si.isDocumentEntry());
            doc = (DocumentNode) si;
            assertContentsMatches(null, doc);

            try (DocumentInputStream inp = new DocumentInputStream(doc)) {
                PropertySet ps = PropertySetFactory.create(inp);
                DocumentSummaryInformation dinf = (DocumentSummaryInformation) ps;
                assertEquals(131333, dinf.getOSVersion());
            }
        }
    }

    /**
     * Read a file, write it and read it again.
     * Then, alter+add some streams, write and read
     */
    @ParameterizedTest
    @MethodSource("get512and4kFileAndInput")
    void readWriteRead(String file, Function<String,POIFSFileSystem> opener) throws IOException, NoPropertySetStreamException {
        SummaryInformation sinf;
        DocumentSummaryInformation dinf;
        DirectoryEntry root, testDir;

        try (POIFSFileSystem fs1 = opener.apply(file)) {
            // Check we can find the entries we expect
            root = fs1.getRoot();
            assertEquals(5, root.getEntryCount());
            assertThat(root.getEntryNames(), hasItem("Thumbnail"));
            assertThat(root.getEntryNames(), hasItem("Image"));
            assertThat(root.getEntryNames(), hasItem("Tags"));
            assertThat(root.getEntryNames(), hasItem("\u0005DocumentSummaryInformation"));
            assertThat(root.getEntryNames(), hasItem("\u0005SummaryInformation"));

            // Write out, re-load
            try (POIFSFileSystem fs2 = writeOutAndReadBack(fs1)) {
                // Check they're still there
                root = fs2.getRoot();
                assertEquals(5, root.getEntryCount());
                assertThat(root.getEntryNames(), hasItem("Thumbnail"));
                assertThat(root.getEntryNames(), hasItem("Image"));
                assertThat(root.getEntryNames(), hasItem("Tags"));
                assertThat(root.getEntryNames(), hasItem("\u0005DocumentSummaryInformation"));
                assertThat(root.getEntryNames(), hasItem("\u0005SummaryInformation"));


                // Check the contents of them - parse the summary block and check
                sinf = (SummaryInformation) PropertySetFactory.create(new DocumentInputStream(
                    (DocumentEntry) root.getEntryCaseInsensitive(SummaryInformation.DEFAULT_STREAM_NAME)));
                assertEquals(131333, sinf.getOSVersion());

                dinf = (DocumentSummaryInformation) PropertySetFactory.create(new DocumentInputStream(
                    (DocumentEntry) root.getEntryCaseInsensitive(DocumentSummaryInformation.DEFAULT_STREAM_NAME)));
                assertEquals(131333, dinf.getOSVersion());


                // Add a test mini stream
                testDir = root.createDirectory("Testing 123");
                testDir.createDirectory("Testing 456");
                testDir.createDirectory("Testing 789");
                byte[] mini = new byte[]{42, 0, 1, 2, 3, 4, 42};
                testDir.createDocument("Mini", new ByteArrayInputStream(mini));


                // Write out, re-load
                try (POIFSFileSystem fs3 = writeOutAndReadBack(fs2)) {

                    root = fs3.getRoot();
                    testDir = (DirectoryEntry) root.getEntryCaseInsensitive("Testing 123");
                    assertEquals(6, root.getEntryCount());
                    assertThat(root.getEntryNames(), hasItem("Thumbnail"));
                    assertThat(root.getEntryNames(), hasItem("Image"));
                    assertThat(root.getEntryNames(), hasItem("Tags"));
                    assertThat(root.getEntryNames(), hasItem("Testing 123"));
                    assertThat(root.getEntryNames(), hasItem("\u0005DocumentSummaryInformation"));
                    assertThat(root.getEntryNames(), hasItem("\u0005SummaryInformation"));


                    // Check old and new are there
                    sinf = (SummaryInformation) PropertySetFactory.create(new DocumentInputStream(
                        (DocumentEntry) root.getEntryCaseInsensitive(SummaryInformation.DEFAULT_STREAM_NAME)));
                    assertEquals(131333, sinf.getOSVersion());

                    dinf = (DocumentSummaryInformation) PropertySetFactory.create(new DocumentInputStream(
                        (DocumentEntry) root.getEntryCaseInsensitive(DocumentSummaryInformation.DEFAULT_STREAM_NAME)));
                    assertEquals(131333, dinf.getOSVersion());

                    assertContentsMatches(mini, (DocumentEntry) testDir.getEntryCaseInsensitive("Mini"));


                    // Write out and read once more, just to be sure
                    try (POIFSFileSystem fs4 = writeOutAndReadBack(fs3)) {

                        root = fs4.getRoot();
                        testDir = (DirectoryEntry) root.getEntryCaseInsensitive("Testing 123");
                        assertEquals(6, root.getEntryCount());
                        assertThat(root.getEntryNames(), hasItem("Thumbnail"));
                        assertThat(root.getEntryNames(), hasItem("Image"));
                        assertThat(root.getEntryNames(), hasItem("Tags"));
                        assertThat(root.getEntryNames(), hasItem("Testing 123"));
                        assertThat(root.getEntryNames(), hasItem("\u0005DocumentSummaryInformation"));
                        assertThat(root.getEntryNames(), hasItem("\u0005SummaryInformation"));

                        sinf = (SummaryInformation) PropertySetFactory.create(new DocumentInputStream(
                            (DocumentEntry) root.getEntryCaseInsensitive(SummaryInformation.DEFAULT_STREAM_NAME)));
                        assertEquals(131333, sinf.getOSVersion());

                        dinf = (DocumentSummaryInformation) PropertySetFactory.create(new DocumentInputStream(
                            (DocumentEntry) root.getEntryCaseInsensitive(DocumentSummaryInformation.DEFAULT_STREAM_NAME)));
                        assertEquals(131333, dinf.getOSVersion());

                        assertContentsMatches(mini, (DocumentEntry) testDir.getEntryCaseInsensitive("Mini"));


                        // Add a full stream, delete a full stream
                        byte[] main4096 = new byte[4096];
                        main4096[0] = -10;
                        main4096[4095] = -11;
                        testDir.createDocument("Normal4096", new ByteArrayInputStream(main4096));

                        assertTrue(root.getEntryCaseInsensitive("Tags").delete());


                        // Write out, re-load
                        try (POIFSFileSystem fs5 = writeOutAndReadBack(fs4)) {

                            // Check it's all there
                            root = fs5.getRoot();
                            testDir = (DirectoryEntry) root.getEntryCaseInsensitive("Testing 123");
                            assertEquals(5, root.getEntryCount());
                            assertThat(root.getEntryNames(), hasItem("Thumbnail"));
                            assertThat(root.getEntryNames(), hasItem("Image"));
                            assertThat(root.getEntryNames(), hasItem("Testing 123"));
                            assertThat(root.getEntryNames(), hasItem("\u0005DocumentSummaryInformation"));
                            assertThat(root.getEntryNames(), hasItem("\u0005SummaryInformation"));


                            // Check old and new are there
                            sinf = (SummaryInformation) PropertySetFactory.create(new DocumentInputStream(
                                (DocumentEntry) root.getEntryCaseInsensitive(SummaryInformation.DEFAULT_STREAM_NAME)));
                            assertEquals(131333, sinf.getOSVersion());

                            dinf = (DocumentSummaryInformation) PropertySetFactory.create(new DocumentInputStream(
                                (DocumentEntry) root.getEntryCaseInsensitive(DocumentSummaryInformation.DEFAULT_STREAM_NAME)));
                            assertEquals(131333, dinf.getOSVersion());

                            assertContentsMatches(mini, (DocumentEntry) testDir.getEntryCaseInsensitive("Mini"));
                            assertContentsMatches(main4096, (DocumentEntry) testDir.getEntryCaseInsensitive("Normal4096"));


                            // Delete a directory, and add one more
                            assertTrue(testDir.getEntryCaseInsensitive("Testing 456").delete());
                            testDir.createDirectory("Testing ABC");


                            // Save
                            try (POIFSFileSystem fs6 = writeOutAndReadBack(fs5)) {

                                // Check
                                root = fs6.getRoot();
                                testDir = (DirectoryEntry) root.getEntryCaseInsensitive("Testing 123");

                                assertEquals(5, root.getEntryCount());
                                assertThat(root.getEntryNames(), hasItem("Thumbnail"));
                                assertThat(root.getEntryNames(), hasItem("Image"));
                                assertThat(root.getEntryNames(), hasItem("Testing 123"));
                                assertThat(root.getEntryNames(), hasItem("\u0005DocumentSummaryInformation"));
                                assertThat(root.getEntryNames(), hasItem("\u0005SummaryInformation"));

                                assertEquals(4, testDir.getEntryCount());
                                assertThat(testDir.getEntryNames(), hasItem("Mini"));
                                assertThat(testDir.getEntryNames(), hasItem("Normal4096"));
                                assertThat(testDir.getEntryNames(), hasItem("Testing 789"));
                                assertThat(testDir.getEntryNames(), hasItem("Testing ABC"));


                                // Add another mini stream
                                byte[] mini2 = new byte[]{-42, 0, -1, -2, -3, -4, -42};
                                testDir.createDocument("Mini2", new ByteArrayInputStream(mini2));

                                // Save, load, check
                                try (POIFSFileSystem fs7 = writeOutAndReadBack(fs6)) {

                                    root = fs7.getRoot();
                                    testDir = (DirectoryEntry) root.getEntryCaseInsensitive("Testing 123");

                                    assertEquals(5, root.getEntryCount());
                                    assertThat(root.getEntryNames(), hasItem("Thumbnail"));
                                    assertThat(root.getEntryNames(), hasItem("Image"));
                                    assertThat(root.getEntryNames(), hasItem("Testing 123"));
                                    assertThat(root.getEntryNames(), hasItem("\u0005DocumentSummaryInformation"));
                                    assertThat(root.getEntryNames(), hasItem("\u0005SummaryInformation"));

                                    assertEquals(5, testDir.getEntryCount());
                                    assertThat(testDir.getEntryNames(), hasItem("Mini"));
                                    assertThat(testDir.getEntryNames(), hasItem("Mini2"));
                                    assertThat(testDir.getEntryNames(), hasItem("Normal4096"));
                                    assertThat(testDir.getEntryNames(), hasItem("Testing 789"));
                                    assertThat(testDir.getEntryNames(), hasItem("Testing ABC"));

                                    assertContentsMatches(mini, (DocumentEntry) testDir.getEntryCaseInsensitive("Mini"));
                                    assertContentsMatches(mini2, (DocumentEntry) testDir.getEntryCaseInsensitive("Mini2"));
                                    assertContentsMatches(main4096, (DocumentEntry) testDir.getEntryCaseInsensitive("Normal4096"));


                                    // Delete a mini stream, add one more
                                    assertTrue(testDir.getEntryCaseInsensitive("Mini").delete());

                                    byte[] mini3 = new byte[]{42, 0, 42, 0, 42, 0, 42};
                                    testDir.createDocument("Mini3", new ByteArrayInputStream(mini3));


                                    // Save, load, check
                                    try (POIFSFileSystem fs8 = writeOutAndReadBack(fs7)) {

                                        root = fs8.getRoot();
                                        testDir = (DirectoryEntry) root.getEntryCaseInsensitive("Testing 123");

                                        assertEquals(5, root.getEntryCount());
                                        assertThat(root.getEntryNames(), hasItem("Thumbnail"));
                                        assertThat(root.getEntryNames(), hasItem("Image"));
                                        assertThat(root.getEntryNames(), hasItem("Testing 123"));
                                        assertThat(root.getEntryNames(), hasItem("\u0005DocumentSummaryInformation"));
                                        assertThat(root.getEntryNames(), hasItem("\u0005SummaryInformation"));

                                        assertEquals(5, testDir.getEntryCount());
                                        assertThat(testDir.getEntryNames(), hasItem("Mini2"));
                                        assertThat(testDir.getEntryNames(), hasItem("Mini3"));
                                        assertThat(testDir.getEntryNames(), hasItem("Normal4096"));
                                        assertThat(testDir.getEntryNames(), hasItem("Testing 789"));
                                        assertThat(testDir.getEntryNames(), hasItem("Testing ABC"));

                                        assertContentsMatches(mini2, (DocumentEntry) testDir.getEntryCaseInsensitive("Mini2"));
                                        assertContentsMatches(mini3, (DocumentEntry) testDir.getEntryCaseInsensitive("Mini3"));
                                        assertContentsMatches(main4096, (DocumentEntry) testDir.getEntryCaseInsensitive("Normal4096"));


                                        // Change some existing streams
                                        POIFSDocument mini2Doc = new POIFSDocument((DocumentNode) testDir.getEntryCaseInsensitive("Mini2"));
                                        mini2Doc.replaceContents(new ByteArrayInputStream(mini));

                                        byte[] main4106 = new byte[4106];
                                        main4106[0] = 41;
                                        main4106[4105] = 42;
                                        POIFSDocument mainDoc = new POIFSDocument((DocumentNode) testDir.getEntryCaseInsensitive("Normal4096"));
                                        mainDoc.replaceContents(new ByteArrayInputStream(main4106));


                                        // Re-check
                                        try (POIFSFileSystem fs9 = writeOutAndReadBack(fs8)) {

                                            root = fs9.getRoot();
                                            testDir = (DirectoryEntry) root.getEntryCaseInsensitive("Testing 123");

                                            assertEquals(5, root.getEntryCount());
                                            assertThat(root.getEntryNames(), hasItem("Thumbnail"));
                                            assertThat(root.getEntryNames(), hasItem("Image"));
                                            assertThat(root.getEntryNames(), hasItem("Testing 123"));
                                            assertThat(root.getEntryNames(), hasItem("\u0005DocumentSummaryInformation"));
                                            assertThat(root.getEntryNames(), hasItem("\u0005SummaryInformation"));

                                            assertEquals(5, testDir.getEntryCount());
                                            assertThat(testDir.getEntryNames(), hasItem("Mini2"));
                                            assertThat(testDir.getEntryNames(), hasItem("Mini3"));
                                            assertThat(testDir.getEntryNames(), hasItem("Normal4096"));
                                            assertThat(testDir.getEntryNames(), hasItem("Testing 789"));
                                            assertThat(testDir.getEntryNames(), hasItem("Testing ABC"));

                                            assertContentsMatches(mini, (DocumentEntry) testDir.getEntryCaseInsensitive("Mini2"));
                                            assertContentsMatches(mini3, (DocumentEntry) testDir.getEntryCaseInsensitive("Mini3"));
                                            assertContentsMatches(main4106, (DocumentEntry) testDir.getEntryCaseInsensitive("Normal4096"));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Create a new file, write it and read it again
     * Then, add some streams, write and read
     */
    @Test
    void createWriteRead() throws IOException {
        try (POIFSFileSystem fs1 = new POIFSFileSystem()) {
            // Initially has Properties + BAT but not SBAT
            assertEquals(POIFSConstants.END_OF_CHAIN, fs1.getNextBlock(0));
            assertEquals(POIFSConstants.FAT_SECTOR_BLOCK, fs1.getNextBlock(1));
            assertEquals(POIFSConstants.UNUSED_BLOCK, fs1.getNextBlock(2));

            // Check that the SBAT is empty
            assertEquals(POIFSConstants.END_OF_CHAIN, fs1.getRoot().getProperty().getStartBlock());

            // Check that properties table was given block 0
            assertEquals(0, fs1._get_property_table().getStartBlock());

            // Write and read it
            try (POIFSFileSystem fs2 = writeOutAndReadBack(fs1)) {

                // No change, SBAT remains empty
                assertEquals(POIFSConstants.END_OF_CHAIN, fs2.getNextBlock(0));
                assertEquals(POIFSConstants.FAT_SECTOR_BLOCK, fs2.getNextBlock(1));
                assertEquals(POIFSConstants.UNUSED_BLOCK, fs2.getNextBlock(2));
                assertEquals(POIFSConstants.UNUSED_BLOCK, fs2.getNextBlock(3));
                assertEquals(POIFSConstants.END_OF_CHAIN, fs2.getRoot().getProperty().getStartBlock());
                assertEquals(0, fs2._get_property_table().getStartBlock());
            }
        }

        // Check the same but with saving to a file
        try (POIFSFileSystem fs3 = new POIFSFileSystem();
            POIFSFileSystem fs4 = writeOutFileAndReadBack(fs3)) {

            // Same, no change, SBAT remains empty
            assertEquals(POIFSConstants.END_OF_CHAIN, fs4.getNextBlock(0));
            assertEquals(POIFSConstants.FAT_SECTOR_BLOCK, fs4.getNextBlock(1));
            assertEquals(POIFSConstants.UNUSED_BLOCK, fs4.getNextBlock(2));
            assertEquals(POIFSConstants.UNUSED_BLOCK, fs4.getNextBlock(3));
            assertEquals(POIFSConstants.END_OF_CHAIN, fs4.getRoot().getProperty().getStartBlock());
            assertEquals(0, fs4._get_property_table().getStartBlock());


            // Put everything within a new directory
            DirectoryEntry testDir = fs4.createDirectory("Test Directory");

            // Add a new Normal Stream (Normal Streams minimum 4096 bytes)
            byte[] main4096 = new byte[4096];
            main4096[0] = -10;
            main4096[4095] = -11;
            testDir.createDocument("Normal4096", new ByteArrayInputStream(main4096));

            assertEquals(POIFSConstants.END_OF_CHAIN, fs4.getNextBlock(0));
            assertEquals(POIFSConstants.FAT_SECTOR_BLOCK, fs4.getNextBlock(1));
            assertEquals(3, fs4.getNextBlock(2));
            assertEquals(4, fs4.getNextBlock(3));
            assertEquals(5, fs4.getNextBlock(4));
            assertEquals(6, fs4.getNextBlock(5));
            assertEquals(7, fs4.getNextBlock(6));
            assertEquals(8, fs4.getNextBlock(7));
            assertEquals(9, fs4.getNextBlock(8));
            assertEquals(POIFSConstants.END_OF_CHAIN, fs4.getNextBlock(9));
            assertEquals(POIFSConstants.UNUSED_BLOCK, fs4.getNextBlock(10));
            assertEquals(POIFSConstants.UNUSED_BLOCK, fs4.getNextBlock(11));
            // SBAT still unused
            assertEquals(POIFSConstants.END_OF_CHAIN, fs4.getRoot().getProperty().getStartBlock());


            // Add a bigger Normal Stream
            byte[] main5124 = new byte[5124];
            main5124[0] = -22;
            main5124[5123] = -33;
            testDir.createDocument("Normal5124", new ByteArrayInputStream(main5124));

            assertEquals(POIFSConstants.END_OF_CHAIN, fs4.getNextBlock(0));
            assertEquals(POIFSConstants.FAT_SECTOR_BLOCK, fs4.getNextBlock(1));
            assertEquals(3, fs4.getNextBlock(2));
            assertEquals(4, fs4.getNextBlock(3));
            assertEquals(5, fs4.getNextBlock(4));
            assertEquals(6, fs4.getNextBlock(5));
            assertEquals(7, fs4.getNextBlock(6));
            assertEquals(8, fs4.getNextBlock(7));
            assertEquals(9, fs4.getNextBlock(8));
            assertEquals(POIFSConstants.END_OF_CHAIN, fs4.getNextBlock(9));

            assertEquals(11, fs4.getNextBlock(10));
            assertEquals(12, fs4.getNextBlock(11));
            assertEquals(13, fs4.getNextBlock(12));
            assertEquals(14, fs4.getNextBlock(13));
            assertEquals(15, fs4.getNextBlock(14));
            assertEquals(16, fs4.getNextBlock(15));
            assertEquals(17, fs4.getNextBlock(16));
            assertEquals(18, fs4.getNextBlock(17));
            assertEquals(19, fs4.getNextBlock(18));
            assertEquals(20, fs4.getNextBlock(19));
            assertEquals(POIFSConstants.END_OF_CHAIN, fs4.getNextBlock(20));
            assertEquals(POIFSConstants.UNUSED_BLOCK, fs4.getNextBlock(21));
            assertEquals(POIFSConstants.UNUSED_BLOCK, fs4.getNextBlock(22));

            assertEquals(POIFSConstants.END_OF_CHAIN, fs4.getRoot().getProperty().getStartBlock());


            // Now Add a mini stream
            byte[] mini = new byte[]{42, 0, 1, 2, 3, 4, 42};
            testDir.createDocument("Mini", new ByteArrayInputStream(mini));

            // Mini stream will get one block for fat + one block for data
            assertEquals(POIFSConstants.END_OF_CHAIN, fs4.getNextBlock(0));
            assertEquals(POIFSConstants.FAT_SECTOR_BLOCK, fs4.getNextBlock(1));
            assertEquals(3, fs4.getNextBlock(2));
            assertEquals(4, fs4.getNextBlock(3));
            assertEquals(5, fs4.getNextBlock(4));
            assertEquals(6, fs4.getNextBlock(5));
            assertEquals(7, fs4.getNextBlock(6));
            assertEquals(8, fs4.getNextBlock(7));
            assertEquals(9, fs4.getNextBlock(8));
            assertEquals(POIFSConstants.END_OF_CHAIN, fs4.getNextBlock(9));

            assertEquals(11, fs4.getNextBlock(10));
            assertEquals(12, fs4.getNextBlock(11));
            assertEquals(13, fs4.getNextBlock(12));
            assertEquals(14, fs4.getNextBlock(13));
            assertEquals(15, fs4.getNextBlock(14));
            assertEquals(16, fs4.getNextBlock(15));
            assertEquals(17, fs4.getNextBlock(16));
            assertEquals(18, fs4.getNextBlock(17));
            assertEquals(19, fs4.getNextBlock(18));
            assertEquals(20, fs4.getNextBlock(19));
            assertEquals(POIFSConstants.END_OF_CHAIN, fs4.getNextBlock(20));
            assertEquals(POIFSConstants.END_OF_CHAIN, fs4.getNextBlock(21));
            assertEquals(POIFSConstants.END_OF_CHAIN, fs4.getNextBlock(22));
            assertEquals(POIFSConstants.UNUSED_BLOCK, fs4.getNextBlock(23));

            // Check the mini stream location was set
            // (21 is mini fat, 22 is first mini stream block)
            assertEquals(22, fs4.getRoot().getProperty().getStartBlock());


            // Write and read back
            try (POIFSFileSystem fs5 = writeOutAndReadBack(fs4)) {
                HeaderBlock header = writeOutAndReadHeader(fs5);

                // Check the header has the right points in it
                assertEquals(1, header.getBATCount());
                assertEquals(1, header.getBATArray()[0]);
                assertEquals(0, header.getPropertyStart());
                assertEquals(1, header.getSBATCount());
                assertEquals(21, header.getSBATStart());
                assertEquals(22, fs5._get_property_table().getRoot().getStartBlock());

                // Block use should be almost the same, except the properties
                //  stream will have grown out to cover 2 blocks
                // Check the block use is all unchanged
                assertEquals(23, fs5.getNextBlock(0)); // Properties now extends over 2 blocks
                assertEquals(POIFSConstants.FAT_SECTOR_BLOCK, fs5.getNextBlock(1));

                assertEquals(3, fs5.getNextBlock(2));
                assertEquals(4, fs5.getNextBlock(3));
                assertEquals(5, fs5.getNextBlock(4));
                assertEquals(6, fs5.getNextBlock(5));
                assertEquals(7, fs5.getNextBlock(6));
                assertEquals(8, fs5.getNextBlock(7));
                assertEquals(9, fs5.getNextBlock(8));
                assertEquals(POIFSConstants.END_OF_CHAIN, fs5.getNextBlock(9)); // End of normal4096

                assertEquals(11, fs5.getNextBlock(10));
                assertEquals(12, fs5.getNextBlock(11));
                assertEquals(13, fs5.getNextBlock(12));
                assertEquals(14, fs5.getNextBlock(13));
                assertEquals(15, fs5.getNextBlock(14));
                assertEquals(16, fs5.getNextBlock(15));
                assertEquals(17, fs5.getNextBlock(16));
                assertEquals(18, fs5.getNextBlock(17));
                assertEquals(19, fs5.getNextBlock(18));
                assertEquals(20, fs5.getNextBlock(19));
                assertEquals(POIFSConstants.END_OF_CHAIN, fs5.getNextBlock(20)); // End of normal5124

                assertEquals(POIFSConstants.END_OF_CHAIN, fs5.getNextBlock(21)); // Mini Stream FAT
                assertEquals(POIFSConstants.END_OF_CHAIN, fs5.getNextBlock(22)); // Mini Stream data
                assertEquals(POIFSConstants.END_OF_CHAIN, fs5.getNextBlock(23)); // Properties #2
                assertEquals(POIFSConstants.UNUSED_BLOCK, fs5.getNextBlock(24));


                // Check some data
                assertEquals(1, fs5.getRoot().getEntryCount());
                testDir = (DirectoryEntry) fs5.getRoot().getEntryCaseInsensitive("Test Directory");
                assertEquals(3, testDir.getEntryCount());

                DocumentEntry miniDoc = (DocumentEntry) testDir.getEntryCaseInsensitive("Mini");
                assertContentsMatches(mini, miniDoc);

                DocumentEntry normDoc = (DocumentEntry) testDir.getEntryCaseInsensitive("Normal4096");
                assertContentsMatches(main4096, normDoc);

                normDoc = (DocumentEntry) testDir.getEntryCaseInsensitive("Normal5124");
                assertContentsMatches(main5124, normDoc);


                // Delete a couple of streams
                assertTrue(miniDoc.delete());
                assertTrue(normDoc.delete());


                // Check - will have un-used sectors now
                try (POIFSFileSystem fs6 = writeOutAndReadBack(fs5)) {

                    assertEquals(POIFSConstants.END_OF_CHAIN, fs6.getNextBlock(0)); // Props back in 1 block
                    assertEquals(POIFSConstants.FAT_SECTOR_BLOCK, fs6.getNextBlock(1));

                    assertEquals(3, fs6.getNextBlock(2));
                    assertEquals(4, fs6.getNextBlock(3));
                    assertEquals(5, fs6.getNextBlock(4));
                    assertEquals(6, fs6.getNextBlock(5));
                    assertEquals(7, fs6.getNextBlock(6));
                    assertEquals(8, fs6.getNextBlock(7));
                    assertEquals(9, fs6.getNextBlock(8));
                    assertEquals(POIFSConstants.END_OF_CHAIN, fs6.getNextBlock(9)); // End of normal4096

                    assertEquals(POIFSConstants.UNUSED_BLOCK, fs6.getNextBlock(10));
                    assertEquals(POIFSConstants.UNUSED_BLOCK, fs6.getNextBlock(11));
                    assertEquals(POIFSConstants.UNUSED_BLOCK, fs6.getNextBlock(12));
                    assertEquals(POIFSConstants.UNUSED_BLOCK, fs6.getNextBlock(13));
                    assertEquals(POIFSConstants.UNUSED_BLOCK, fs6.getNextBlock(14));
                    assertEquals(POIFSConstants.UNUSED_BLOCK, fs6.getNextBlock(15));
                    assertEquals(POIFSConstants.UNUSED_BLOCK, fs6.getNextBlock(16));
                    assertEquals(POIFSConstants.UNUSED_BLOCK, fs6.getNextBlock(17));
                    assertEquals(POIFSConstants.UNUSED_BLOCK, fs6.getNextBlock(18));
                    assertEquals(POIFSConstants.UNUSED_BLOCK, fs6.getNextBlock(19));
                    assertEquals(POIFSConstants.UNUSED_BLOCK, fs6.getNextBlock(20));

                    assertEquals(POIFSConstants.END_OF_CHAIN, fs6.getNextBlock(21)); // Mini Stream FAT
                    assertEquals(POIFSConstants.END_OF_CHAIN, fs6.getNextBlock(22)); // Mini Stream data
                    assertEquals(POIFSConstants.UNUSED_BLOCK, fs6.getNextBlock(23)); // Properties gone
                    assertEquals(POIFSConstants.UNUSED_BLOCK, fs6.getNextBlock(24));
                    assertEquals(POIFSConstants.UNUSED_BLOCK, fs6.getNextBlock(25));
                }
            }
        }

    }

    @Test
    void addBeforeWrite() throws IOException {
        try (POIFSFileSystem fs1 = new POIFSFileSystem()) {

            // Initially has Properties + BAT but nothing else
            assertEquals(POIFSConstants.END_OF_CHAIN, fs1.getNextBlock(0));
            assertEquals(POIFSConstants.FAT_SECTOR_BLOCK, fs1.getNextBlock(1));
            assertEquals(POIFSConstants.UNUSED_BLOCK, fs1.getNextBlock(2));

            HeaderBlock hdr = writeOutAndReadHeader(fs1);
            // No mini stream, and no xbats
            // Will have fat then properties stream
            assertEquals(1, hdr.getBATCount());
            assertEquals(1, hdr.getBATArray()[0]);
            assertEquals(0, hdr.getPropertyStart());
            assertEquals(POIFSConstants.END_OF_CHAIN, hdr.getSBATStart());
            assertEquals(POIFSConstants.END_OF_CHAIN, hdr.getXBATIndex());
            assertEquals(POIFSConstants.SMALLER_BIG_BLOCK_SIZE * 3, fs1.size());
        }

        // Get a clean filesystem to start with
        try (POIFSFileSystem fs1 = new POIFSFileSystem()) {

            // Put our test files in a non-standard place
            DirectoryEntry parentDir = fs1.createDirectory("Parent Directory");
            DirectoryEntry testDir = parentDir.createDirectory("Test Directory");


            // Add to the mini stream
            byte[] mini = new byte[]{42, 0, 1, 2, 3, 4, 42};
            testDir.createDocument("Mini", new ByteArrayInputStream(mini));

            // Add to the main stream
            byte[] main4096 = new byte[4096];
            main4096[0] = -10;
            main4096[4095] = -11;
            testDir.createDocument("Normal4096", new ByteArrayInputStream(main4096));


            // Check the mini stream was added, then the main stream
            assertEquals(POIFSConstants.END_OF_CHAIN, fs1.getNextBlock(0));
            assertEquals(POIFSConstants.FAT_SECTOR_BLOCK, fs1.getNextBlock(1));
            assertEquals(POIFSConstants.END_OF_CHAIN, fs1.getNextBlock(2)); // Mini Fat
            assertEquals(POIFSConstants.END_OF_CHAIN, fs1.getNextBlock(3)); // Mini Stream
            assertEquals(5, fs1.getNextBlock(4)); // Main Stream
            assertEquals(6, fs1.getNextBlock(5));
            assertEquals(7, fs1.getNextBlock(6));
            assertEquals(8, fs1.getNextBlock(7));
            assertEquals(9, fs1.getNextBlock(8));
            assertEquals(10, fs1.getNextBlock(9));
            assertEquals(11, fs1.getNextBlock(10));
            assertEquals(POIFSConstants.END_OF_CHAIN, fs1.getNextBlock(11));
            assertEquals(POIFSConstants.UNUSED_BLOCK, fs1.getNextBlock(12));
            assertEquals(POIFSConstants.SMALLER_BIG_BLOCK_SIZE * 13, fs1.size());


            // Check that we can read the right data pre-write
            DocumentEntry miniDoc = (DocumentEntry) testDir.getEntryCaseInsensitive("Mini");
            assertContentsMatches(mini, miniDoc);

            DocumentEntry normDoc = (DocumentEntry) testDir.getEntryCaseInsensitive("Normal4096");
            assertContentsMatches(main4096, normDoc);


            // Write, read, check
            HeaderBlock hdr = writeOutAndReadHeader(fs1);
            try (POIFSFileSystem fs2 = writeOutAndReadBack(fs1)) {
                // Check the header details - will have the sbat near the start,
                //  then the properties at the end
                assertEquals(1, hdr.getBATCount());
                assertEquals(1, hdr.getBATArray()[0]);
                assertEquals(2, hdr.getSBATStart());
                assertEquals(0, hdr.getPropertyStart());
                assertEquals(POIFSConstants.END_OF_CHAIN, hdr.getXBATIndex());

                // Check the block allocation is unchanged, other than
                //  the properties stream going in at the end
                assertEquals(12, fs2.getNextBlock(0)); // Properties
                assertEquals(POIFSConstants.FAT_SECTOR_BLOCK, fs2.getNextBlock(1));
                assertEquals(POIFSConstants.END_OF_CHAIN, fs2.getNextBlock(2));
                assertEquals(POIFSConstants.END_OF_CHAIN, fs2.getNextBlock(3));
                assertEquals(5, fs2.getNextBlock(4));
                assertEquals(6, fs2.getNextBlock(5));
                assertEquals(7, fs2.getNextBlock(6));
                assertEquals(8, fs2.getNextBlock(7));
                assertEquals(9, fs2.getNextBlock(8));
                assertEquals(10, fs2.getNextBlock(9));
                assertEquals(11, fs2.getNextBlock(10));
                assertEquals(POIFSConstants.END_OF_CHAIN, fs2.getNextBlock(11));
                assertEquals(POIFSConstants.END_OF_CHAIN, fs2.getNextBlock(12));
                assertEquals(POIFSConstants.UNUSED_BLOCK, fs2.getNextBlock(13));
                assertEquals(POIFSConstants.SMALLER_BIG_BLOCK_SIZE * 14, fs2.size());


                // Check the data
                DirectoryEntry fsRoot = fs2.getRoot();
                assertEquals(1, fsRoot.getEntryCount());

                parentDir = (DirectoryEntry) fsRoot.getEntryCaseInsensitive("Parent Directory");
                assertEquals(1, parentDir.getEntryCount());

                testDir = (DirectoryEntry) parentDir.getEntryCaseInsensitive("Test Directory");
                assertEquals(2, testDir.getEntryCount());

                miniDoc = (DocumentEntry) testDir.getEntryCaseInsensitive("Mini");
                assertContentsMatches(mini, miniDoc);

                normDoc = (DocumentEntry) testDir.getEntryCaseInsensitive("Normal4096");
                assertContentsMatches(main4096, normDoc);


                // Add one more stream to each, then save and re-load
                byte[] mini2 = new byte[]{-42, 0, -1, -2, -3, -4, -42};
                testDir.createDocument("Mini2", new ByteArrayInputStream(mini2));

                // Add to the main stream
                byte[] main4106 = new byte[4106];
                main4106[0] = 41;
                main4106[4105] = 42;
                testDir.createDocument("Normal4106", new ByteArrayInputStream(main4106));

                // Recheck the data in all 4 streams
                try (POIFSFileSystem fs3 = writeOutAndReadBack(fs2)) {
                    fsRoot = fs3.getRoot();
                    assertEquals(1, fsRoot.getEntryCount());

                    parentDir = (DirectoryEntry) fsRoot.getEntryCaseInsensitive("Parent Directory");
                    assertEquals(1, parentDir.getEntryCount());

                    testDir = (DirectoryEntry) parentDir.getEntryCaseInsensitive("Test Directory");
                    assertEquals(4, testDir.getEntryCount());

                    miniDoc = (DocumentEntry) testDir.getEntryCaseInsensitive("Mini");
                    assertContentsMatches(mini, miniDoc);

                    miniDoc = (DocumentEntry) testDir.getEntryCaseInsensitive("Mini2");
                    assertContentsMatches(mini2, miniDoc);

                    normDoc = (DocumentEntry) testDir.getEntryCaseInsensitive("Normal4106");
                    assertContentsMatches(main4106, normDoc);
                }
            }
        }
    }

    @Test
    void readZeroLengthEntries() throws IOException {
        try (POIFSFileSystem fs = new POIFSFileSystem(_inst.getFile("only-zero-byte-streams.ole2"))) {
            DirectoryNode testDir = fs.getRoot();
            assertEquals(3, testDir.getEntryCount());

            DocumentEntry entry = (DocumentEntry) testDir.getEntryCaseInsensitive("test-zero-1");
            assertNotNull(entry);
            assertEquals(0, entry.getSize());

            entry = (DocumentEntry) testDir.getEntryCaseInsensitive("test-zero-2");
            assertNotNull(entry);
            assertEquals(0, entry.getSize());

            entry = (DocumentEntry) testDir.getEntryCaseInsensitive("test-zero-3");
            assertNotNull(entry);
            assertEquals(0, entry.getSize());

            // Check properties, all have zero length, no blocks
            PropertyTable props = fs._get_property_table();
            assertEquals(POIFSConstants.END_OF_CHAIN, props.getRoot().getStartBlock());
            for (Property prop : props.getRoot()) {
                assertEquals("test-zero-", prop.getName().substring(0, 10));
                assertEquals(POIFSConstants.END_OF_CHAIN, prop.getStartBlock());
            }
        }
    }

    @Test
    void writeZeroLengthEntries() throws IOException {
        try (POIFSFileSystem fs1 = new POIFSFileSystem()) {
            DirectoryNode testDir = fs1.getRoot();
            DocumentEntry miniDoc;
            DocumentEntry normDoc;
            DocumentEntry emptyDoc;

            // Add mini and normal sized entries to start
            byte[] mini2 = new byte[]{-42, 0, -1, -2, -3, -4, -42};
            testDir.createDocument("Mini2", new ByteArrayInputStream(mini2));

            // Add to the main stream
            byte[] main4106 = new byte[4106];
            main4106[0] = 41;
            main4106[4105] = 42;
            testDir.createDocument("Normal4106", new ByteArrayInputStream(main4106));

            // Now add some empty ones
            byte[] empty = new byte[0];
            testDir.createDocument("empty-1", new ByteArrayInputStream(empty));
            testDir.createDocument("empty-2", new ByteArrayInputStream(empty));
            testDir.createDocument("empty-3", new ByteArrayInputStream(empty));

            // Check
            miniDoc = (DocumentEntry) testDir.getEntryCaseInsensitive("Mini2");
            assertContentsMatches(mini2, miniDoc);

            normDoc = (DocumentEntry) testDir.getEntryCaseInsensitive("Normal4106");
            assertContentsMatches(main4106, normDoc);

            emptyDoc = (DocumentEntry) testDir.getEntryCaseInsensitive("empty-1");
            assertContentsMatches(empty, emptyDoc);

            emptyDoc = (DocumentEntry) testDir.getEntryCaseInsensitive("empty-2");
            assertContentsMatches(empty, emptyDoc);

            emptyDoc = (DocumentEntry) testDir.getEntryCaseInsensitive("empty-3");
            assertContentsMatches(empty, emptyDoc);

            // Look at the properties entry, and check the empty ones
            //  have zero size and no start block
            PropertyTable props = fs1._get_property_table();
            Iterator<Property> propsIt = props.getRoot().getChildren();

            Property prop = propsIt.next();
            assertEquals("Mini2", prop.getName());
            assertEquals(0, prop.getStartBlock());
            assertEquals(7, prop.getSize());

            prop = propsIt.next();
            assertEquals("Normal4106", prop.getName());
            assertEquals(4, prop.getStartBlock()); // BAT, Props, SBAT, MIni
            assertEquals(4106, prop.getSize());

            prop = propsIt.next();
            assertEquals("empty-1", prop.getName());
            assertEquals(POIFSConstants.END_OF_CHAIN, prop.getStartBlock());
            assertEquals(0, prop.getSize());

            prop = propsIt.next();
            assertEquals("empty-2", prop.getName());
            assertEquals(POIFSConstants.END_OF_CHAIN, prop.getStartBlock());
            assertEquals(0, prop.getSize());

            prop = propsIt.next();
            assertEquals("empty-3", prop.getName());
            assertEquals(POIFSConstants.END_OF_CHAIN, prop.getStartBlock());
            assertEquals(0, prop.getSize());


            // Save and re-check
            try (POIFSFileSystem fs2 = writeOutAndReadBack(fs1)) {
                testDir = fs2.getRoot();

                miniDoc = (DocumentEntry) testDir.getEntryCaseInsensitive("Mini2");
                assertContentsMatches(mini2, miniDoc);

                normDoc = (DocumentEntry) testDir.getEntryCaseInsensitive("Normal4106");
                assertContentsMatches(main4106, normDoc);

                emptyDoc = (DocumentEntry) testDir.getEntryCaseInsensitive("empty-1");
                assertContentsMatches(empty, emptyDoc);

                emptyDoc = (DocumentEntry) testDir.getEntryCaseInsensitive("empty-2");
                assertContentsMatches(empty, emptyDoc);

                emptyDoc = (DocumentEntry) testDir.getEntryCaseInsensitive("empty-3");
                assertContentsMatches(empty, emptyDoc);

                // Check that a mini-stream was assigned, with one block used
                assertEquals(3, testDir.getProperty().getStartBlock());
                assertEquals(64, testDir.getProperty().getSize());
            }
        }
    }

    /**
     * Test that we can read a file with POIFS, create a new POIFS instance,
     * write it out, read it with POIFS, and see the original data
     */
    @Test
    void POIFSReadCopyWritePOIFSRead() throws IOException {
        File testFile = POIDataSamples.getSpreadSheetInstance().getFile("Simple.xls");
        try (POIFSFileSystem src = new POIFSFileSystem(testFile);
             POIFSFileSystem nfs = new POIFSFileSystem()) {
            byte[] wbDataExp = IOUtils.toByteArray(src.createDocumentInputStream("Workbook"));
            EntryUtils.copyNodes(src.getRoot(), nfs.getRoot());

            try (POIFSFileSystem pfs = writeOutFileAndReadBack(nfs)) {
                byte[] wbDataAct = IOUtils.toByteArray(pfs.createDocumentInputStream("Workbook"));
                assertThat(wbDataExp, equalTo(wbDataAct));
            }
        }
    }

    /**
     * Ensure that you can recursively delete directories and their
     * contents
     */
    @Test
    void RecursiveDelete() throws IOException {
        File testFile = POIDataSamples.getSpreadSheetInstance().getFile("SimpleMacro.xls");
        try (POIFSFileSystem src = new POIFSFileSystem(testFile)) {
            // Starts out with 5 entries:
            //  _VBA_PROJECT_CUR
            //  SummaryInformation <(0x05)SummaryInformation>
            //  DocumentSummaryInformation <(0x05)DocumentSummaryInformation>
            //  Workbook
            //  CompObj <(0x01)CompObj>
            assertEquals(5, _countChildren(src._get_property_table().getRoot()));
            assertEquals(5, src.getRoot().getEntryCount());

            // Grab the VBA project root
            DirectoryEntry vbaProj = (DirectoryEntry) src.getRoot().getEntryCaseInsensitive("_VBA_PROJECT_CUR");
            assertEquals(3, vbaProj.getEntryCount());
            // Can't delete yet, has stuff
            assertFalse(vbaProj.delete());
            // Recursively delete
            _recursiveDeletee(vbaProj);

            // Entries gone
            assertEquals(4, _countChildren(src._get_property_table().getRoot()));
            assertEquals(4, src.getRoot().getEntryCount());
        }
    }

    private void _recursiveDeletee(Entry entry) throws IOException {
        if (entry.isDocumentEntry()) {
            assertTrue(entry.delete());
            return;
        }

        DirectoryEntry dir = (DirectoryEntry) entry;
        String[] names = dir.getEntryNames().toArray(new String[dir.getEntryCount()]);
        for (String name : names) {
            Entry ce = dir.getEntryCaseInsensitive(name);
            _recursiveDeletee(ce);
        }
        assertTrue(dir.delete());
    }

    @SuppressWarnings("unused")
    private int _countChildren(DirectoryProperty p) {
        int count = 0;
        for (Property cp : p) {
            count++;
        }
        return count;
    }

    /**
     * To ensure we can create a file >2gb in size, as well as to
     * extend existing files past the 2gb boundary.
     * <p>
     * Note that to run this test, you will require 2.5+gb of free
     * space on your TMP/TEMP partition/disk
     * <p>
     * Note that to run this test, you need to be able to mmap 2.5+gb
     * files, which may need bigger kernel.shmmax and vm.max_map_count
     * settings on Linux.
     * <p>
     * TODO Fix this to work...
     */
    @Test
    @Disabled("Work in progress test for #60670")
    void creationAndExtensionPast2GB() throws Exception {
        File big = TempFile.createTempFile("poi-test-", ".ole2");
        assumeTrue(big.getFreeSpace() > 2.5 * 1024 * 1024 * 1024,
            "2.5gb of free space is required on your tmp/temp partition/disk to run large file tests");
        System.out.println("Slow, memory heavy test in progress....");

        int s100mb = 100 * 1024 * 1024;
        int s512mb = 512 * 1024 * 1024;
        long s2gb = 2L * 1024 * 1024 * 1024;
        DocumentEntry entry;

        // Create a just-sub 2gb file
        try (POIFSFileSystem fs = POIFSFileSystem.create(big)) {
            for (int i = 0; i < 19; i++) {
                fs.createDocument(new DummyDataInputStream(s100mb), "Entry" + i);
            }
            fs.writeFilesystem();
        }

        // Extend it past the 2gb mark
        try (POIFSFileSystem fs = new POIFSFileSystem(big, false)) {
            for (int i = 0; i < 19; i++) {
                entry = (DocumentEntry) fs.getRoot().getEntryCaseInsensitive("Entry" + i);
                assertNotNull(entry);
                assertEquals(s100mb, entry.getSize());
            }

            fs.createDocument(new DummyDataInputStream(s512mb), "Bigger");
            fs.writeFilesystem();
        }

        // Check it still works
        try (POIFSFileSystem fs = new POIFSFileSystem(big, false)) {
            for (int i = 0; i < 19; i++) {
                entry = (DocumentEntry) fs.getRoot().getEntryCaseInsensitive("Entry" + i);
                assertNotNull(entry);
                assertEquals(s100mb, entry.getSize());
            }
            entry = (DocumentEntry) fs.getRoot().getEntryCaseInsensitive("Bigger");
            assertNotNull(entry);
            assertEquals(s512mb, entry.getSize());
        }
        // Tidy
        assertTrue(big.delete());


        // Create a >2gb file
        try (POIFSFileSystem fs = POIFSFileSystem.create(big)) {
            for (int i = 0; i < 4; i++) {
                fs.createDocument(new DummyDataInputStream(s512mb), "Entry" + i);
            }
            fs.writeFilesystem();
        }

        // Read it
        try (POIFSFileSystem fs = new POIFSFileSystem(big, false)) {
            for (int i = 0; i < 4; i++) {
                entry = (DocumentEntry) fs.getRoot().getEntryCaseInsensitive("Entry" + i);
                assertNotNull(entry);
                assertEquals(s512mb, entry.getSize());
            }

            // Extend it
            fs.createDocument(new DummyDataInputStream(s512mb), "Entry4");
            fs.writeFilesystem();
        }

        // Check it worked
        try (POIFSFileSystem fs = new POIFSFileSystem(big, false)) {
            for (int i = 0; i < 5; i++) {
                entry = (DocumentEntry) fs.getRoot().getEntryCaseInsensitive("Entry" + i);
                assertNotNull(entry);
                assertEquals(s512mb, entry.getSize());
            }
        }
        // Tidy
        assertTrue(big.delete());

        // Create a file with a 2gb entry
        try (POIFSFileSystem fs = POIFSFileSystem.create(big)) {
            fs.createDocument(new DummyDataInputStream(s100mb), "Small");
            // TODO Check we get a helpful error about the max size
            fs.createDocument(new DummyDataInputStream(s2gb), "Big");
        }
    }

    private static final class DummyDataInputStream extends InputStream {
        private final long maxSize;
        private long size;

        private DummyDataInputStream(long maxSize) {
            this.maxSize = maxSize;
            this.size = 0;
        }

        @Override
        public int read() {
            if (size >= maxSize) return -1;
            size++;
            return (int) (size % 128);
        }

        @Override
        public int read(byte[] b) {
            return read(b, 0, b.length);
        }

        @Override
        public int read(byte[] b, int offset, int len) {
            if (size >= maxSize) return -1;
            int sz = (int) Math.min(len, maxSize - size);
            for (int i = 0; i < sz; i++) {
                b[i + offset] = (byte) ((size + i) % 128);
            }
            size += sz;
            return sz;
        }
    }

    @Disabled("Takes a long time to run")
    @Test
    void performance() throws Exception {
        int iterations = 200;//1_000;

        long start = System.currentTimeMillis();

        for (int i = 0; i < iterations; i++) {

            try (InputStream inputStream = POIDataSamples.getHSMFInstance().openResourceAsStream("lots-of-recipients.msg");
                 POIFSFileSystem srcFileSystem = new POIFSFileSystem(inputStream);
                 POIFSFileSystem destFileSystem = new POIFSFileSystem()) {

                copyAllEntries(srcFileSystem.getRoot(), destFileSystem.getRoot());

                File file = File.createTempFile("npoi", ".dat");
                try (OutputStream outputStream = new FileOutputStream(file)) {
                    destFileSystem.writeFilesystem(outputStream);
                }

                assertTrue(file.delete());
                if (i % 10 == 0) System.out.print(".");
            }
        }

        System.out.println("NPOI took: " + (System.currentTimeMillis() - start));
    }

    private static void copyAllEntries(DirectoryEntry srcDirectory, DirectoryEntry destDirectory) throws IOException {
        Iterator<Entry> iterator = srcDirectory.getEntries();

        while (iterator.hasNext()) {
            Entry entry = iterator.next();

            if (entry.isDirectoryEntry()) {
                DirectoryEntry childDest = destDirectory.createDirectory(entry.getName());
                copyAllEntries((DirectoryEntry) entry, childDest);

            } else {
                DocumentEntry srcEntry = (DocumentEntry) entry;

                try (InputStream inputStream = new DocumentInputStream(srcEntry)) {
                    destDirectory.createDocument(entry.getName(), inputStream);
                }
            }
        }
    }
}

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

package org.apache.poi.poifs.property;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.output.UnsynchronizedByteArrayOutputStream;
import org.apache.poi.poifs.storage.RawDataUtil;
import org.apache.poi.util.LocaleUtil;
import org.junit.jupiter.api.Test;

/**
 * Class to test DirectoryProperty functionality
 */
final class TestDirectoryProperty {
    private DirectoryProperty _property;
    private byte[]            _testblock;

    /**
     * Test constructing DirectoryProperty
     */
    @Test
    void testConstructor() throws IOException {
        createBasicDirectoryProperty();
        verifyProperty();
    }

    /**
     * Test pre-write functionality
     */
    @Test
    void testPreWrite() throws IOException {
        createBasicDirectoryProperty();
        _property.preWrite();

        // shouldn't change anything at all
        verifyProperty();
        verifyChildren(0);

        // now try adding 1 property
        createBasicDirectoryProperty();
        _property.addChild(new LocalProperty(1));
        _property.preWrite();

        // update children index
        _testblock[ 0x4C ] = 1;
        _testblock[ 0x4D ] = 0;
        _testblock[ 0x4E ] = 0;
        _testblock[ 0x4F ] = 0;
        verifyProperty();
        verifyChildren(1);

        // now try adding 2 properties
        createBasicDirectoryProperty();
        _property.addChild(new LocalProperty(1));
        _property.addChild(new LocalProperty(2));
        _property.preWrite();

        // update children index
        _testblock[ 0x4C ] = 2;
        _testblock[ 0x4D ] = 0;
        _testblock[ 0x4E ] = 0;
        _testblock[ 0x4F ] = 0;
        verifyProperty();
        verifyChildren(2);

        // beat on the children allocation code
        for (int count = 1; count < 100; count++)
        {
            createBasicDirectoryProperty();
            for (int j = 1; j < (count + 1); j++)
            {
                _property.addChild(new LocalProperty(j));
            }
            _property.preWrite();
            verifyChildren(count);
        }
    }

    private void verifyChildren(int count) {
        Iterator<Property> iter = _property.getChildren();
        List<Property> children = new ArrayList<>();

        while (iter.hasNext())
        {
            children.add(iter.next());
        }
        assertEquals(count, children.size());
        assertEquals(count, _property.spliterator().getExactSizeIfKnown());
        if (count != 0)
        {
            boolean[] found = new boolean[ count ];

            found[ _property.getChildIndex() - 1 ] = true;
            int total_found = 1;

            Arrays.fill(found, false);
            iter = children.iterator();
            while (iter.hasNext())
            {
                Property child = iter.next();
                Child    next  = child.getNextChild();

                if (next != null)
                {
                    int index = (( Property ) next).getIndex();

                    if (index != -1)
                    {
                        assertFalse(found[index - 1]);
                        found[ index - 1 ] = true;
                        total_found++;
                    }
                }
                Child previous = child.getPreviousChild();

                if (previous != null)
                {
                    int index = (( Property ) previous).getIndex();

                    if (index != -1)
                    {
                        assertFalse(found[index - 1]);
                        found[ index - 1 ] = true;
                        total_found++;
                    }
                }
            }
            assertEquals(count, total_found);
        }
    }

    private void createBasicDirectoryProperty() {
        final String name = "MyDirectory";

        _property  = new DirectoryProperty(name);
        _testblock = new byte[ 128 ];
        int index = 0;

        for (; index < 0x40; index++)
        {
            _testblock[ index ] = ( byte ) 0;
        }
        int limit = name.length();

        _testblock[ index++ ] = ( byte ) (2 * (limit + 1));
        _testblock[ index++ ] = ( byte ) 0;
        _testblock[ index++ ] = ( byte ) 1;
        _testblock[ index++ ] = ( byte ) 1;
        for (; index < 0x50; index++)
        {
            _testblock[ index ] = ( byte ) 0xff;
        }
        for (; index < 0x80; index++)
        {
            _testblock[ index ] = ( byte ) 0;
        }
        byte[] name_bytes = name.getBytes(LocaleUtil.CHARSET_1252);

        for (index = 0; index < limit; index++)
        {
            _testblock[ index * 2 ] = name_bytes[ index ];
        }
    }

    private void verifyProperty() throws IOException {
        UnsynchronizedByteArrayOutputStream stream = new UnsynchronizedByteArrayOutputStream(512);

        _property.writeData(stream);
        byte[] output = stream.toByteArray();

        assertEquals(_testblock.length, output.length);
        for (int j = 0; j < _testblock.length; j++)
        {
            assertEquals(_testblock[ j ], output[ j ], "mismatch at offset " + j);
        }
    }

    @Test
    void testAddChild() throws IOException {
        createBasicDirectoryProperty();
        _property.addChild(new LocalProperty(1));
        _property.addChild(new LocalProperty(2));
        assertThrows(IOException.class, () -> _property.addChild(new LocalProperty(1)));
        assertThrows(IOException.class, () -> _property.addChild(new LocalProperty(2)));
        _property.addChild(new LocalProperty(3));
    }

    @Test
    void testDeleteChild() throws IOException {
        createBasicDirectoryProperty();
        Property p1 = new LocalProperty(1);

        _property.addChild(p1);
        assertThrows(IOException.class, () -> _property.addChild(new LocalProperty(1)));
        assertTrue(_property.deleteChild(p1));
        assertFalse(_property.deleteChild(p1));
        _property.addChild(new LocalProperty(1));
    }

    @Test
    void testChangeName() throws IOException {
        createBasicDirectoryProperty();
        Property p1           = new LocalProperty(1);
        String   originalName = p1.getName();

        _property.addChild(p1);
        assertTrue(_property.changeName(p1, "foobar"));
        assertEquals("foobar", p1.getName());
        assertFalse(_property.changeName(p1, "foobar"));
        assertEquals("foobar", p1.getName());
        Property p2 = new LocalProperty(1);

        _property.addChild(p2);
        assertFalse(_property.changeName(p1, originalName));
        assertTrue(_property.changeName(p2, "foo"));
        assertTrue(_property.changeName(p1, originalName));
    }

    @Test
    void testReadingConstructor() {
        String[] input = {
            "42 00 6F 00 6F 00 74 00 20 00 45 00 6E 00 74 00 72 00 79 00 00 00 00 00 00 00 00 00 00 00 00 00",
            "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00",
            "16 00 01 01 FF FF FF FF FF FF FF FF 02 00 00 00 20 08 02 00 00 00 00 00 C0 00 00 00 00 00 00 46",
            "00 00 00 00 00 00 00 00 00 00 00 00 C0 5C E8 23 9E 6B C1 01 FE FF FF FF 00 00 00 00 00 00 00 00",
        };
        verifyReadingProperty(0, RawDataUtil.decode(input), 0, "Boot Entry");
    }

    private static void verifyReadingProperty(int index, byte[] input, int offset, String name) {
        DirectoryProperty property = new DirectoryProperty(index, input, offset);
        UnsynchronizedByteArrayOutputStream stream = new UnsynchronizedByteArrayOutputStream(128);
        byte[] expected = Arrays.copyOfRange(input, offset, offset+128);
        try {
            property.writeData(stream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        byte[] output = stream.toByteArray();

        assertEquals(128, output.length);
        for (int j = 0; j < 128; j++) {
            assertEquals(expected[j], output[j], "mismatch at offset " + j);
        }
        assertEquals(index, property.getIndex());
        assertEquals(name, property.getName());
        assertFalse(property.getChildren().hasNext());
    }
}

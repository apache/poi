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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.poifs.storage.RawDataUtil;

import junit.framework.TestCase;

/**
 * Class to test DirectoryProperty functionality
 *
 * @author Marc Johnson
 */
public final class TestDirectoryProperty extends TestCase {
    private DirectoryProperty _property;
    private byte[]            _testblock;

    /**
     * Test constructing DirectoryProperty
     */
    public void testConstructor() throws IOException {
        createBasicDirectoryProperty();
        verifyProperty();
    }

    /**
     * Test pre-write functionality
     */
    public void testPreWrite() throws IOException {
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
        Iterator iter     = _property.getChildren();
        List     children = new ArrayList();

        while (iter.hasNext())
        {
            children.add(iter.next());
        }
        assertEquals(count, children.size());
        if (count != 0)
        {
            boolean[] found = new boolean[ count ];

            found[ _property.getChildIndex() - 1 ] = true;
            int total_found = 1;

            Arrays.fill(found, false);
            iter = children.iterator();
            while (iter.hasNext())
            {
                Property child = ( Property ) iter.next();
                Child    next  = child.getNextChild();

                if (next != null)
                {
                    int index = (( Property ) next).getIndex();

                    if (index != -1)
                    {
                        assertTrue("found index " + index + " twice",
                                   !found[ index - 1 ]);
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
                        assertTrue("found index " + index + " twice",
                                   !found[ index - 1 ]);
                        found[ index - 1 ] = true;
                        total_found++;
                    }
                }
            }
            assertEquals(count, total_found);
        }
    }

    private void createBasicDirectoryProperty() {
        String name = "MyDirectory";

        _property  = new DirectoryProperty(name);
        _testblock = new byte[ 128 ];
        int index = 0;

        for (; index < 0x40; index++)
        {
            _testblock[ index ] = ( byte ) 0;
        }
        int limit = Math.min(31, name.length());

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
        byte[] name_bytes = name.getBytes();

        for (index = 0; index < limit; index++)
        {
            _testblock[ index * 2 ] = name_bytes[ index ];
        }
    }

    private void verifyProperty() throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream(512);

        _property.writeData(stream);
        byte[] output = stream.toByteArray();

        assertEquals(_testblock.length, output.length);
        for (int j = 0; j < _testblock.length; j++)
        {
            assertEquals("mismatch at offset " + j, _testblock[ j ],
                         output[ j ]);
        }
    }

    public void testAddChild() throws IOException {
        createBasicDirectoryProperty();
        _property.addChild(new LocalProperty(1));
        _property.addChild(new LocalProperty(2));
        try
        {
            _property.addChild(new LocalProperty(1));
            fail("should have caught IOException");
        }
        catch (IOException ignored)
        {

            // as expected
        }
        try
        {
            _property.addChild(new LocalProperty(2));
            fail("should have caught IOException");
        }
        catch (IOException ignored)
        {

            // as expected
        }
        _property.addChild(new LocalProperty(3));
    }

    public void testDeleteChild() throws IOException {
        createBasicDirectoryProperty();
        Property p1 = new LocalProperty(1);

        _property.addChild(p1);
        try
        {
            _property.addChild(new LocalProperty(1));
            fail("should have caught IOException");
        }
        catch (IOException ignored)
        {

            // as expected
        }
        assertTrue(_property.deleteChild(p1));
        assertTrue(!_property.deleteChild(p1));
        _property.addChild(new LocalProperty(1));
    }

    public void testChangeName() throws IOException {
        createBasicDirectoryProperty();
        Property p1           = new LocalProperty(1);
        String   originalName = p1.getName();

        _property.addChild(p1);
        assertTrue(_property.changeName(p1, "foobar"));
        assertEquals("foobar", p1.getName());
        assertTrue(!_property.changeName(p1, "foobar"));
        assertEquals("foobar", p1.getName());
        Property p2 = new LocalProperty(1);

        _property.addChild(p2);
        assertTrue(!_property.changeName(p1, originalName));
        assertTrue(_property.changeName(p2, "foo"));
        assertTrue(_property.changeName(p1, originalName));
    }

    public void testReadingConstructor() {
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
        ByteArrayOutputStream stream = new ByteArrayOutputStream(128);
        byte[] expected = new byte[128];

        System.arraycopy(input, offset, expected, 0, 128);
        try {
            property.writeData(stream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        byte[] output = stream.toByteArray();

        assertEquals(128, output.length);
        for (int j = 0; j < 128; j++) {
            assertEquals("mismatch at offset " + j, expected[j], output[j]);
        }
        assertEquals(index, property.getIndex());
        assertEquals(name, property.getName());
        assertTrue(!property.getChildren().hasNext());
    }
}

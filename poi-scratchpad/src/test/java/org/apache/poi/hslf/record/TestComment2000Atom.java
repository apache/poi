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

package org.apache.poi.hslf.record;


import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.commons.io.output.UnsynchronizedByteArrayOutputStream;
import org.apache.poi.util.LocaleUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Tests that Comment2000Atom works properly.
 */
public final class TestComment2000Atom {
    // From a real file
    private final byte[] data_a = new byte[] {
        0, 0, 0xE1-256, 0x2E, 0x1C, 0, 0, 0,
        1, 0, 0, 0, 0xD6-256, 7, 1, 0,
        2, 0, 0x18, 0, 0x0A, 0, 0x1A, 0,
        0x0F, 0, 0xCD-256, 0, 0x92-256, 0,
        0,  0, 0x92-256, 0, 0, 0
    };
    private final byte[] data_b = new byte[] {
        0, 0, 0xE1-256, 0x2E, 0x1C, 0, 0, 0,
        5, 0, 0, 0, 0xD6-256, 0x07, 1, 0,
        2, 0, 0x18, 0, 0x15, 0, 0x19, 0, 3,
        0, 0xD5-256, 2, 0x0A, 0, 0, 0,
        0x0E, 0, 0, 0
        };

    private static SimpleDateFormat sdf;

    @BeforeAll
    public static void initDateFormat() {
        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.ROOT);
        sdf.setTimeZone(LocaleUtil.getUserTimeZone());
    }

    @Test
    void testRecordType() {
        Comment2000Atom ca = new Comment2000Atom(data_a, 0, data_a.length);
        assertEquals(12001L, ca.getRecordType());
    }

    @Test
    void testGetDate() throws Exception {
        Comment2000Atom ca = new Comment2000Atom(data_a, 0, data_a.length);
        Comment2000Atom cb = new Comment2000Atom(data_b, 0, data_b.length);

        // A is 2006-01-24 (2nd day of week) 10:26:15.205
        Date exp_a = sdf.parse("2006-01-24 10:26:15.205");
        // B is 2006-01-24 (2nd day of week) 21:25:03.725
        Date exp_b = sdf.parse("2006-01-24 21:25:03.725");

        assertEquals(exp_a, ca.getDate());
        assertEquals(exp_b, cb.getDate());
    }

    @Test
    void testGetNums() {
        Comment2000Atom ca = new Comment2000Atom(data_a, 0, data_a.length);
        Comment2000Atom cb = new Comment2000Atom(data_b, 0, data_b.length);

        // A is number 1
        assertEquals(1, ca.getNumber());
        // B is number 5
        assertEquals(5, cb.getNumber());
    }

    @Test
    void testGetPos() {
        Comment2000Atom ca = new Comment2000Atom(data_a, 0, data_a.length);
        Comment2000Atom cb = new Comment2000Atom(data_b, 0, data_b.length);

        // A is at 0x92, 0x92
        assertEquals(0x92, ca.getXOffset());
        assertEquals(0x92, ca.getYOffset());

        // B is at 0x0A, 0x0E
        assertEquals(0x0A, cb.getXOffset());
        assertEquals(0x0E, cb.getYOffset());
    }

    @Test
    void testWrite() throws Exception {
        Comment2000Atom ca = new Comment2000Atom(data_a, 0, data_a.length);
        UnsynchronizedByteArrayOutputStream baos = UnsynchronizedByteArrayOutputStream.builder().get();
        ca.writeOut(baos);
        byte[] b = baos.toByteArray();
        assertArrayEquals(data_a, b);
    }

    // Create A from scratch
    @Test
    void testCreate() throws Exception {
        Comment2000Atom a = new Comment2000Atom();

        // Set number, x and y
        a.setNumber(1);
        a.setXOffset(0x92);
        a.setYOffset(0x92);

        // Set the date
        Date date_a = sdf.parse("2006-01-24 10:26:15.205");
        a.setDate(date_a);

        // Check it's now the same as a
        UnsynchronizedByteArrayOutputStream baos = UnsynchronizedByteArrayOutputStream.builder().get();
        a.writeOut(baos);
        byte[] b = baos.toByteArray();
        assertArrayEquals(data_a, b);
    }

    // Try to turn a into b
    @Test
    void testChange() throws Exception {
        Comment2000Atom ca = new Comment2000Atom(data_a, 0, data_a.length);

        // Change the number
        ca.setNumber(5);

        // Change the date
        Date new_date = sdf.parse("2006-01-24 21:25:03.725");
        ca.setDate(new_date);

        // Change the x and y
        ca.setXOffset(0x0A);
        ca.setYOffset(0x0E);

        // Check bytes are now the same
        UnsynchronizedByteArrayOutputStream baos = UnsynchronizedByteArrayOutputStream.builder().get();
        ca.writeOut(baos);
        byte[] b = baos.toByteArray();
        assertArrayEquals(data_b, b);
    }
}

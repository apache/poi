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


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.poi.util.LocaleUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Tests that Comment2000 works properly.
 * TODO: Test Comment200Atom within
 *
 * @author Nick Burch (nick at torchbox dot com)
 */
public final class TestComment2000 {
	// From a real file
	private final byte[] data_a = new byte[] {
		0x0F, 0, 0xE0-256, 0x2E, 0x9C-256, 0, 0, 0,
		0, 0, 0xBA-256, 0x0F, 0x14, 0, 0, 0,
		0x44, 0, 0x75, 0, 0x6D, 0, 0x62, 0,
		0x6C, 0, 0x65, 0, 0x64, 0, 0x6F, 0,
		0x72, 0, 0x65, 0,
		0x10, 0, 0xBA-256, 0x0F, 0x4A, 0, 0, 0,
		0x59, 0, 0x65, 0, 0x73, 0, 0x2C, 0,
		0x20, 0, 0x74, 0, 0x68, 0, 0x65, 0,
		0x79, 0, 0x20, 0, 0x63, 0, 0x65, 0,
		0x72, 0, 0x74, 0,	0x61, 0, 0x69, 0,
		0x6E, 0, 0x6C, 0, 0x79, 0, 0x20, 0,
		0x61, 0, 0x72, 0, 0x65, 0, 0x2C, 0,
		0x20, 0, 0x61, 0, 0x72, 0, 0x65, 0,
		0x6E, 0, 0x27, 0, 0x74, 0, 0x20, 0,
		0x74, 0, 0x68, 0, 0x65, 0, 0x79, 0, 0x21, 0,
		0x20, 0, 0xBA-256, 0x0F, 0x02, 0, 0, 0,
		0x44, 0,
		0, 0, 0xE1-256, 0x2E, 0x1C, 0, 0, 0,
		0x01, 0, 0, 0, 0xD6-256, 0x07, 0x01, 0,
		0x02, 0, 0x18, 0, 0x0A, 0, 0x1A, 0,
		0x0F, 0, 0xCD-256, 0, 0x92-256, 0,
		0,	0, 0x92-256, 0, 0, 0
	};
	private final byte[] data_b = new byte[] {
		0x0F, 0, 0xE0-256, 0x2E, 0xAC-256, 0, 0, 0,
		0, 0, 0xBA-256, 0x0F, 0x10, 0, 0, 0,
		0x48, 0, 0x6F, 0, 0x67, 0, 0x77, 0,
		0x61, 0, 0x72, 0, 0x74, 0, 0x73, 0,
		0x10, 0, 0xBA-256, 0x0F, 0x5E, 0, 0, 0,
		0x43, 0, 0x6F, 0, 0x6D, 0, 0x6D, 0,
		0x65, 0, 0x6E, 0, 0x74, 0, 0x73, 0,
		0x20, 0, 0x61, 0, 0x72, 0, 0x65, 0,
		0x20, 0, 0x66, 0, 0x75, 0, 0x6E, 0,
		0x20, 0, 0x74, 0, 0x68, 0, 0x69, 0,
		0x6E, 0, 0x67, 0, 0x73, 0, 0x20, 0,
		0x74, 0, 0x6F, 0, 0x20, 0, 0x61, 0,
		0x64, 0, 0x64, 0, 0x20, 0, 0x69, 0,
		0x6E, 0, 0x2C, 0, 0x20, 0, 0x61, 0,
		0x72, 0, 0x65, 0, 0x6E, 0, 0x27, 0,
		0x74, 0, 0x20, 0, 0x74, 0, 0x68, 0,
		0x65, 0, 0x79, 0, 0x3F, 0,
		0x20, 0, 0xBA-256, 0x0F, 0x02, 0, 0, 0,
		0x48, 0,
		0, 0, 0xE1-256, 0x2E, 0x1C, 0, 0, 0,
		0x01, 0, 0, 0, 0xD6-256, 0x07, 0x01, 0,
		0x02, 0, 0x18, 0, 0x16, 0, 0x19, 0, 0x03,
		0, 0xD5-256, 0x02, 0x0A, 0, 0, 0,
		0x0A, 0, 0, 0
		};

	private static SimpleDateFormat sdf;

	@BeforeAll
	public static void initDateFormat() {
	    sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.ROOT);
	    sdf.setTimeZone(LocaleUtil.getUserTimeZone());
	}

    @Test
    void testRecordType() {
		Comment2000 ca = new Comment2000(data_a, 0, data_a.length);
		assertEquals(12000L, ca.getRecordType());
	}

    @Test
    void testAuthor() {
		Comment2000 ca = new Comment2000(data_a, 0, data_a.length);
		assertEquals("Dumbledore", ca.getAuthor());
		assertEquals("D", ca.getAuthorInitials());
	}

    @Test
    void testText() {
		Comment2000 ca = new Comment2000(data_a, 0, data_a.length);
		assertEquals("Yes, they certainly are, aren't they!", ca.getText());
	}

    @Test
    void testCommentAtom() throws Exception {
		Comment2000 ca = new Comment2000(data_a, 0, data_a.length);
		Comment2000Atom c2a = ca.getComment2000Atom();

		assertEquals(1, c2a.getNumber());
		assertEquals(0x92, c2a.getXOffset());
		assertEquals(0x92, c2a.getYOffset());
		Date exp_a = sdf.parse("2006-01-24 10:26:15.205");
		assertEquals(exp_a, c2a.getDate());
	}

    @Test
    void testCommentAtomB() throws Exception {
		Comment2000 cb = new Comment2000(data_b, 0, data_b.length);
		Comment2000Atom c2b = cb.getComment2000Atom();

		assertEquals(1, c2b.getNumber());
		assertEquals(0x0a, c2b.getXOffset());
		assertEquals(0x0a, c2b.getYOffset());
		Date exp_b = sdf.parse("2006-01-24 22:25:03.725");
		assertEquals(exp_b, c2b.getDate());
	}

    @Test
    void testWrite() throws Exception {
		Comment2000 ca = new Comment2000(data_a, 0, data_a.length);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ca.writeOut(baos);
		byte[] b = baos.toByteArray();

		assertEquals(data_a.length, b.length);
		for(int i=0; i<data_a.length; i++) {
			assertEquals(data_a[i],b[i]);
		}
	}

	// Change a few things
    @Test
    void testChange() throws Exception {
		Comment2000 ca = new Comment2000(data_a, 0, data_a.length);
		Comment2000 cb = new Comment2000(data_b, 0, data_b.length);
		Comment2000 cn = new Comment2000();
		ca.setAuthor("Hogwarts");
		ca.setAuthorInitials("H");
		ca.setText("Comments are fun things to add in, aren't they?");
		cn.setAuthor("Hogwarts");
		cn.setAuthorInitials("H");
		cn.setText("Comments are fun things to add in, aren't they?");

		// Change the Comment2000Atom
		Comment2000Atom c2a = ca.getComment2000Atom();
		Comment2000Atom c2n = cn.getComment2000Atom();
		c2a.setNumber(1);
		c2a.setXOffset(0x0a);
		c2a.setYOffset(0x0a);
		c2n.setNumber(1);
		c2n.setXOffset(0x0a);
		c2n.setYOffset(0x0a);

		Date new_date = sdf.parse("2006-01-24 22:25:03.725");
		c2a.setDate(new_date);
		c2n.setDate(new_date);

		// Check now the same
		assertEquals(ca.getText(), cb.getText());
		assertEquals(cn.getText(), cb.getText());
		assertEquals(ca.getAuthor(), cb.getAuthor());
		assertEquals(cn.getAuthor(), cb.getAuthor());
		assertEquals(ca.getAuthorInitials(), cb.getAuthorInitials());
		assertEquals(cn.getAuthorInitials(), cb.getAuthorInitials());

		// Check bytes weren't the same
		boolean equals = true;
		for(int i=0; i<data_a.length; i++) {
			if (data_a[i] != data_b[i]) {
				equals = false;
				break;
			}
		}
		assertFalse(equals, "Arrays should not be equals");

		// Check bytes are now the same
		ByteArrayOutputStream baosa = new ByteArrayOutputStream();
		ByteArrayOutputStream baosn = new ByteArrayOutputStream();
		ca.writeOut(baosa);
		cn.writeOut(baosn);
		byte[] ba = baosa.toByteArray();
		byte[] bn = baosn.toByteArray();

		// Should now be the same
		assertEquals(data_b.length, ba.length);
		for(int i=0; i<data_b.length; i++) {
			assertEquals(data_b[i],ba[i]);
		}
		assertEquals(data_b.length, bn.length);
		for(int i=0; i<data_b.length; i++) {
			assertEquals(data_b[i],bn[i]);
		}
	}

    /**
     *  A Comment2000 records with missing commentTextAtom
     */
    @Test
    void testBug44770() {
		byte[] data = {
            0x0F, 0x00, (byte)0xE0, 0x2E, 0x3E, 0x00, 0x00, 0x00, 0x00, 0x00, (byte)0xBA, 0x0F,
            0x08, 0x00, 0x00, 0x00, 0x4E, 0x00, 0x45, 0x00, 0x53, 0x00, 0x53, 0x00, 0x20,
            0x00, (byte)0xBA, 0x0F, 0x02, 0x00, 0x00, 0x00, 0x4E, 0x00, 0x00, 0x00, (byte)0xE1, 0x2E,
            0x1C, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, (byte)0xD9, 0x07, 0x08, 0x00,
            0x01, 0x00, 0x18, 0x00, 0x10, 0x00, 0x1F, 0x00, 0x05, 0x00, (byte)0x80, 0x03,
            0x0A, 0x00, 0x00, 0x00, 0x0A, 0x00, 0x00, 0x00
        };
        Comment2000 ca = new Comment2000(data, 0, data.length);
        Record[] ch = ca.getChildRecords();
        assertEquals(3, ch.length);

        assertTrue(ch[0] instanceof CString);
        assertEquals(0, ((CString)ch[0]).getOptions() >> 4);
        assertTrue(ch[1] instanceof CString);
        assertEquals(2, ((CString)ch[1]).getOptions() >> 4);
        assertTrue(ch[2] instanceof Comment2000Atom);

        assertEquals("NESS", ca.getAuthor());
        assertEquals("N", ca.getAuthorInitials());
        assertNull(ca.getText()); //commentTextAtom is missing
    }

}

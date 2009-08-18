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


import junit.framework.TestCase;
import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Tests that Comment2000Atom works properly.
 *
 * @author Nick Burch (nick at torchbox dot com)
 */
public final class TestComment2000Atom extends TestCase {
	// From a real file
	private byte[] data_a = new byte[] {
		00, 00, 0xE1-256, 0x2E, 0x1C, 00, 00, 00,
		01, 00, 00, 00, 0xD6-256, 07, 01, 00,
		02, 00, 0x18, 00, 0x0A, 00, 0x1A, 00,
		0x0F, 00, 0xCD-256, 00, 0x92-256, 00,
		00,	00, 0x92-256, 00, 00, 00
	};
	private byte[] data_b = new byte[] {
		00, 00, 0xE1-256, 0x2E, 0x1C, 00, 00, 00,
		05, 00, 00, 00, 0xD6-256, 0x07, 01, 00,
		02, 00, 0x18, 00, 0x15, 00, 0x19, 00, 03,
		00, 0xD5-256, 02, 0x0A, 00, 00, 00,
		0x0E, 00, 00, 00
		};

	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

	public void testRecordType() {
		Comment2000Atom ca = new Comment2000Atom(data_a, 0, data_a.length);
		assertEquals(12001l, ca.getRecordType());
	}

	public void testGetDate() throws Exception {
		Comment2000Atom ca = new Comment2000Atom(data_a, 0, data_a.length);
		Comment2000Atom cb = new Comment2000Atom(data_b, 0, data_b.length);

		// A is 2006-01-24 (2nd day of week) 10:26:15.205
		Date exp_a = sdf.parse("2006-01-24 10:26:15.205");
		// B is 2006-01-24 (2nd day of week) 21:25:03.725
		Date exp_b = sdf.parse("2006-01-24 21:25:03.725");

		assertEquals(exp_a, ca.getDate());
		assertEquals(exp_b, cb.getDate());
	}

	public void testGetNums() {
		Comment2000Atom ca = new Comment2000Atom(data_a, 0, data_a.length);
		Comment2000Atom cb = new Comment2000Atom(data_b, 0, data_b.length);

		// A is number 1
		assertEquals(1, ca.getNumber());
		// B is number 5
		assertEquals(5, cb.getNumber());
	}

	public void testGetPos() {
		Comment2000Atom ca = new Comment2000Atom(data_a, 0, data_a.length);
		Comment2000Atom cb = new Comment2000Atom(data_b, 0, data_b.length);

		// A is at 0x92, 0x92
		assertEquals(0x92, ca.getXOffset());
		assertEquals(0x92, ca.getYOffset());

		// B is at 0x0A, 0x0E
		assertEquals(0x0A, cb.getXOffset());
		assertEquals(0x0E, cb.getYOffset());
	}

	public void testWrite() throws Exception {
		Comment2000Atom ca = new Comment2000Atom(data_a, 0, data_a.length);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ca.writeOut(baos);
		byte[] b = baos.toByteArray();

		assertEquals(data_a.length, b.length);
		for(int i=0; i<data_a.length; i++) {
			assertEquals(data_a[i],b[i]);
		}
	}

	// Create A from scratch
	public void testCreate() throws Exception {
		Comment2000Atom a = new Comment2000Atom();

		// Set number, x and y
		a.setNumber(1);
		a.setXOffset(0x92);
		a.setYOffset(0x92);

		// Set the date
		Date date_a = sdf.parse("2006-01-24 10:26:15.205");
		a.setDate(date_a);

		// Check it's now the same as a
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		a.writeOut(baos);
		byte[] b = baos.toByteArray();

		assertEquals(data_a.length, b.length);
		for(int i=0; i<data_a.length; i++) {
			assertEquals(data_a[i],b[i]);
		}
	}

	// Try to turn a into b
	public void testChange() throws Exception {
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
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ca.writeOut(baos);
		byte[] b = baos.toByteArray();

		// Should now be the same
		assertEquals(data_b.length, b.length);
		for(int i=0; i<data_b.length; i++) {
			assertEquals(data_b[i],b[i]);
		}
	}
}

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

package org.apache.poi.hssf.usermodel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.apache.poi.hssf.record.BOFRecord;
import org.apache.poi.hssf.record.BoundSheetRecord;
import org.apache.poi.hssf.record.EOFRecord;
import org.apache.poi.hssf.record.InterfaceHdrRecord;
import org.apache.poi.hssf.record.NameRecord;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.usermodel.SanityChecker.CheckRecord;

/**
 * A Test case for a test utility class.<br/>
 * Okay, this may seem strange but I need to test my test logic.
 *
 * @author Glen Stampoultzis (glens at apache.org)
 */
public final class TestSanityChecker extends TestCase {

	private static BoundSheetRecord createBoundSheetRec() {
		return new BoundSheetRecord("Sheet1");
	}
	public void testCheckRecordOrder() {
		final SanityChecker c = new SanityChecker();
		List records = new ArrayList();
		records.add(new BOFRecord());
		records.add(new InterfaceHdrRecord());
		records.add(createBoundSheetRec());
		records.add(EOFRecord.instance);
		CheckRecord[] check = {
				new CheckRecord(BOFRecord.class, '1'),
				new CheckRecord(InterfaceHdrRecord.class, '0'),
				new CheckRecord(BoundSheetRecord.class, 'M'),
				new CheckRecord(NameRecord.class, '*'),
				new CheckRecord(EOFRecord.class, '1'),
		};
		// check pass
		c.checkRecordOrder(records, check);
		records.add(2, createBoundSheetRec());
		c.checkRecordOrder(records, check);
		records.remove(1);	  // optional record missing
		c.checkRecordOrder(records, check);
		records.add(3, new NameRecord());
		records.add(3, new NameRecord()); // optional multiple record occurs more than one time
		c.checkRecordOrder(records, check);

		// check fail
		confirmBadRecordOrder(check, new Record[] {
				new BOFRecord(),
				createBoundSheetRec(),
				new InterfaceHdrRecord(),
				EOFRecord.instance,
		});

		confirmBadRecordOrder(check, new Record[] {
				new BOFRecord(),
				new InterfaceHdrRecord(),
				createBoundSheetRec(),
				new InterfaceHdrRecord(),
				EOFRecord.instance,
		});

		confirmBadRecordOrder(check, new Record[] {
				new BOFRecord(),
				createBoundSheetRec(),
				new NameRecord(),
				EOFRecord.instance,
				new NameRecord(),
		});

		confirmBadRecordOrder(check, new Record[] {
				new InterfaceHdrRecord(),
				createBoundSheetRec(),
				EOFRecord.instance,
		});

		confirmBadRecordOrder(check, new Record[] {
				new BOFRecord(),
				new InterfaceHdrRecord(),
				EOFRecord.instance,
		});

		confirmBadRecordOrder(check, new Record[] {
				new InterfaceHdrRecord(),
				createBoundSheetRec(),
				new BOFRecord(),
				EOFRecord.instance,
		});

		confirmBadRecordOrder(check, new Record[] {
				new BOFRecord(),
				createBoundSheetRec(),
				new InterfaceHdrRecord(),
				EOFRecord.instance,
		});
	}
	private static void confirmBadRecordOrder(final SanityChecker.CheckRecord[] check, Record[] recs) {
		final SanityChecker c = new SanityChecker();
		final List records = Arrays.asList(recs);
		try {
			new Runnable() {
				public void run() {
					c.checkRecordOrder(records, check);
				}
			}.run();
		} catch (AssertionFailedError pass) {
			// expected during normal test
			return;
		}
		throw new AssertionFailedError("Did not get failure exception as expected");
	}
}

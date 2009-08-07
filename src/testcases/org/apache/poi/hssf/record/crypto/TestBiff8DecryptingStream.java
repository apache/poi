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

package org.apache.poi.hssf.record.crypto;

import java.io.InputStream;
import java.util.Arrays;

import junit.framework.AssertionFailedError;
import junit.framework.ComparisonFailure;
import junit.framework.TestCase;

import org.apache.poi.util.HexDump;
import org.apache.poi.util.HexRead;

/**
 * Tests for {@link Biff8DecryptingStream}
 *
 * @author Josh Micich
 */
public final class TestBiff8DecryptingStream extends TestCase {

	/**
	 * A mock {@link InputStream} that keeps track of position and also produces
	 * slightly interesting data. Each successive data byte value is one greater
	 * than the previous.
	 */
	private static final class MockStream extends InputStream {
		private int _val;
		private int _position;

		public MockStream(int initialValue) {
			_val = initialValue & 0xFF;
		}
		public int read() {
			_position++;
			return _val++ & 0xFF;
		}
		public int getPosition() {
			return _position;
		}
	}

	private static final class StreamTester {
		private static final boolean ONLY_LOG_ERRORS = true;

		private final MockStream _ms;
		private final Biff8DecryptingStream _bds;
		private boolean _errorsOccurred;

		/**
		 * @param expectedFirstInt expected value of the first int read from the decrypted stream
		 */
		public StreamTester(MockStream ms, String keyDigestHex, int expectedFirstInt) {
			_ms = ms;
			byte[] keyDigest = HexRead.readFromString(keyDigestHex);
			_bds = new Biff8DecryptingStream(_ms, 0, new Biff8EncryptionKey(keyDigest));
			assertEquals(expectedFirstInt, _bds.readInt());
			_errorsOccurred = false;
		}

		public Biff8DecryptingStream getBDS() {
			return _bds;
		}

		/**
		 * Used to 'skip over' the uninteresting middle bits of the key blocks.
		 * Also confirms that read position of the underlying stream is aligned.
		 */
		public void rollForward(int fromPosition, int toPosition) {
			assertEquals(fromPosition, _ms.getPosition());
			for (int i = fromPosition; i < toPosition; i++) {
				_bds.readByte();
			}
			assertEquals(toPosition, _ms.getPosition());
		}

		public void confirmByte(int expVal) {
			cmp(HexDump.byteToHex(expVal), HexDump.byteToHex(_bds.readUByte()));
		}

		public void confirmShort(int expVal) {
			cmp(HexDump.shortToHex(expVal), HexDump.shortToHex(_bds.readUShort()));
		}

		public void confirmInt(int expVal) {
			cmp(HexDump.intToHex(expVal), HexDump.intToHex(_bds.readInt()));
		}

		public void confirmLong(long expVal) {
			cmp(HexDump.longToHex(expVal), HexDump.longToHex(_bds.readLong()));
		}

		private void cmp(char[] exp, char[] act) {
			if (Arrays.equals(exp, act)) {
				return;
			}
			_errorsOccurred = true;
			if (ONLY_LOG_ERRORS) {
				logErr(3, "Value mismatch " + new String(exp) + " - " + new String(act));
				return;
			}
			throw new ComparisonFailure("Value mismatch", new String(exp), new String(act));
		}

		public void confirmData(String expHexData) {

			byte[] expData = HexRead.readFromString(expHexData);
			byte[] actData = new byte[expData.length];
			_bds.readFully(actData);
			if (Arrays.equals(expData, actData)) {
				return;
			}
			_errorsOccurred = true;
			if (ONLY_LOG_ERRORS) {
				logErr(2, "Data mismatch " + HexDump.toHex(expData) + " - "
						+ HexDump.toHex(actData));
				return;
			}
			throw new ComparisonFailure("Data mismatch", HexDump.toHex(expData), HexDump.toHex(actData));
		}

		private static void logErr(int stackFrameCount, String msg) {
			StackTraceElement ste = new Exception().getStackTrace()[stackFrameCount];
			System.err.print("(" + ste.getFileName() + ":" + ste.getLineNumber() + ") ");
			System.err.println(msg);
		}

		public void assertNoErrors() {
			assertFalse("Some values decrypted incorrectly", _errorsOccurred);
		}
	}

	/**
	 * Tests reading of 64,32,16 and 8 bit integers aligned with key changing boundaries
	 */
	public void testReadsAlignedWithBoundary() {
		StreamTester st = createStreamTester(0x50, "BA AD F0 0D 00", 0x96C66829);

		st.rollForward(0x0004, 0x03FF);
		st.confirmByte(0x3E);
		st.confirmByte(0x28);
		st.rollForward(0x0401, 0x07FE);
		st.confirmShort(0x76CC);
		st.confirmShort(0xD83E);
		st.rollForward(0x0802, 0x0BFC);
		st.confirmInt(0x25F280EB);
		st.confirmInt(0xB549E99B);
		st.rollForward(0x0C04, 0x0FF8);
		st.confirmLong(0x6AA2D5F6B975D10CL);
		st.confirmLong(0x34248ADF7ED4F029L);
		st.assertNoErrors();
	}

	/**
	 * Tests reading of 64,32 and 16 bit integers <i>across</i> key changing boundaries
	 */
	public void testReadsSpanningBoundary() {
		StreamTester st = createStreamTester(0x50, "BA AD F0 0D 00", 0x96C66829);

		st.rollForward(0x0004, 0x03FC);
		st.confirmLong(0x885243283E2A5EEFL);
		st.rollForward(0x0404, 0x07FE);
		st.confirmInt(0xD83E76CC);
		st.rollForward(0x0802, 0x0BFF);
		st.confirmShort(0x9B25);
		st.assertNoErrors();
	}

	/**
	 * Checks that the BIFF header fields (sid, size) get read without applying decryption,
	 * and that the RC4 stream stays aligned during these calls
	 */
	public void testReadHeaderUShort() {
		StreamTester st = createStreamTester(0x50, "BA AD F0 0D 00", 0x96C66829);

		st.rollForward(0x0004, 0x03FF);

		Biff8DecryptingStream bds = st.getBDS();
		int hval = bds.readDataSize();   // unencrypted
		int nextInt = bds.readInt();
		if (nextInt == 0x8F534029) {
			throw new AssertionFailedError(
					"Indentified bug in key alignment after call to readHeaderUShort()");
		}
		assertEquals(0x16885243, nextInt);
		if (hval == 0x283E) {
			throw new AssertionFailedError("readHeaderUShort() incorrectly decrypted result");
		}
		assertEquals(0x504F, hval);

		// confirm next key change
		st.rollForward(0x0405, 0x07FC);
		st.confirmInt(0x76CC1223);
		st.confirmInt(0x4842D83E);
		st.assertNoErrors();
	}

	/**
	 * Tests reading of byte sequences <i>across</i> and <i>aligned with</i> key changing boundaries
	 */
	public void testReadByteArrays() {
		StreamTester st = createStreamTester(0x50, "BA AD F0 0D 00", 0x96C66829);

		st.rollForward(0x0004, 0x2FFC);
		st.confirmData("66 A1 20 B1 04 A3 35 F5"); // 4 bytes on either side of boundary
		st.rollForward(0x3004, 0x33F8);
		st.confirmData("F8 97 59 36");  // last 4 bytes in block
		st.confirmData("01 C2 4E 55");  // first 4 bytes in next block
		st.assertNoErrors();
	}

	private static StreamTester createStreamTester(int mockStreamStartVal, String keyDigestHex, int expectedFirstInt) {
		return new StreamTester(new MockStream(mockStreamStartVal), keyDigestHex, expectedFirstInt);
	}
}

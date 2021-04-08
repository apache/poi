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

package org.apache.poi.poifs.crypt;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.io.InputStream;

import javax.crypto.spec.SecretKeySpec;

import org.apache.poi.hssf.record.crypto.Biff8DecryptingStream;
import org.apache.poi.util.HexRead;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link Biff8DecryptingStream}
 */
final class TestBiff8DecryptingStream {

	/**
	 * A mock {@link InputStream} that keeps track of position and also produces
	 * slightly interesting data. Each successive data byte value is one greater
	 * than the previous.
	 */
	private static final class MockStream extends InputStream {
		private final int _initialValue;
		private int _position;

		public MockStream(int initialValue) {
			_initialValue = initialValue;
		}

		@Override
        public int read() {
			return (_initialValue+_position++) & 0xFF;
		}
	}

	private static final class StreamTester {
		private final Biff8DecryptingStream _bds;

		/**
		 * @param expectedFirstInt expected value of the first int read from the decrypted stream
		 */
		public StreamTester(MockStream ms, String keyDigestHex, int expectedFirstInt) {
			byte[] keyDigest = HexRead.readFromString(keyDigestHex);
			EncryptionInfo ei = new EncryptionInfo(EncryptionMode.binaryRC4);
			Decryptor dec = ei.getDecryptor();
			dec.setSecretKey(new SecretKeySpec(keyDigest, "RC4"));

			_bds = new Biff8DecryptingStream(ms, 0, ei);
			assertEquals(expectedFirstInt, _bds.readInt());
		}

		public Biff8DecryptingStream getBDS() {
			return _bds;
		}

		/**
		 * Used to 'skip over' the uninteresting middle bits of the key blocks.
		 * Also confirms that read position of the underlying stream is aligned.
		 */
		void rollForward(int fromPosition, int toPosition) {
			assertEquals(fromPosition, _bds.getPosition());
			for (int i = fromPosition; i < toPosition; i++) {
				_bds.readByte();
			}
			assertEquals(toPosition, _bds.getPosition());
		}

		void confirmByte(int expVal) {
		    assertEquals(expVal, _bds.readUByte());
		}

		void confirmShort(int expVal) {
		    assertEquals((short)expVal, _bds.readShort());
		}

        void confirmUShort(int expVal) {
            assertEquals(expVal, _bds.readUShort());
        }

        public short readShort() {
            return _bds.readShort();
        }

        public int readUShort() {
            return _bds.readUShort();
        }

		void confirmInt(int expVal) {
			assertEquals(expVal, _bds.readInt());
		}

		void confirmLong(long expVal) {
		    assertEquals(expVal, _bds.readLong());
		}

		void confirmData(String expHexData) {

			byte[] expData = HexRead.readFromString(expHexData);
			byte[] actData = new byte[expData.length];
			_bds.readFully(actData);
			assertArrayEquals(expData, actData, "Data mismatch");
		}
	}

	/**
	 * Tests reading of 64,32,16 and 8 bit integers aligned with key changing boundaries
	 */
	@Test
	void readsAlignedWithBoundary() {
		StreamTester st = createStreamTester();

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
		// check for signed/unsigned shorts #58069
		st.rollForward(0x1008, 0x7213);
		st.confirmUShort(0xFFFF);
		st.rollForward(0x7215, 0x1B9AD);
        st.confirmShort(-1);
        st.rollForward(0x1B9AF, 0x37D99);
        assertEquals(0xFFFF, st.readUShort());
        st.rollForward(0x37D9B, 0x4A6F2);
        assertEquals(-1, st.readShort());
	}

	/**
	 * Tests reading of 64,32 and 16 bit integers <i>across</i> key changing boundaries
	 */
    @Test
	void readsSpanningBoundary() {
		StreamTester st = createStreamTester();

		st.rollForward(0x0004, 0x03FC);
		st.confirmLong(0x885243283E2A5EEFL);
		st.rollForward(0x0404, 0x07FE);
		st.confirmInt(0xD83E76CC);
		st.rollForward(0x0802, 0x0BFF);
		st.confirmShort(0x9B25);
	}

	/**
	 * Checks that the BIFF header fields (sid, size) get read without applying decryption,
	 * and that the RC4 stream stays aligned during these calls
	 */
    @Test
	void readHeaderUShort() {
		StreamTester st = createStreamTester();

		st.rollForward(0x0004, 0x03FF);

		Biff8DecryptingStream bds = st.getBDS();
		int hval = bds.readDataSize();   // unencrypted
		int nextInt = bds.readInt();
		assertNotEquals(0x8F534029, nextInt, "Indentified bug in key alignment after call to readHeaderUShort()");
		assertEquals(0x16885243, nextInt);
		assertNotEquals(0x283E, hval, "readHeaderUShort() incorrectly decrypted result");
		assertEquals(0x504F, hval);

		// confirm next key change
		st.rollForward(0x0405, 0x07FC);
		st.confirmInt(0x76CC1223);
		st.confirmInt(0x4842D83E);
	}

	/**
	 * Tests reading of byte sequences <i>across</i> and <i>aligned with</i> key changing boundaries
	 */
    @Test
	void readByteArrays() {
		StreamTester st = createStreamTester();

		st.rollForward(0x0004, 0x2FFC);
		st.confirmData("66 A1 20 B1 04 A3 35 F5"); // 4 bytes on either side of boundary
		st.rollForward(0x3004, 0x33F8);
		st.confirmData("F8 97 59 36");  // last 4 bytes in block
		st.confirmData("01 C2 4E 55");  // first 4 bytes in next block
	}

	private static StreamTester createStreamTester() {
		return new StreamTester(new MockStream(0x50), "BA AD F0 0D 00", 0x96C66829);
	}
}

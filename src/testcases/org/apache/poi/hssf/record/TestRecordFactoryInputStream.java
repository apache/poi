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

package org.apache.poi.hssf.record;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.hssf.record.crypto.Biff8EncryptionKey;
import org.apache.poi.util.HexRead;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Tests for {@link RecordFactoryInputStream}
 *
 * @author Josh Micich
 */
public final class TestRecordFactoryInputStream {
	/**
	 * Hex dump of a BOF record and most of a FILEPASS record.
	 * A 16 byte saltHash should be added to complete the second record
	 */
	private static final String COMMON_HEX_DATA = ""
		// BOF
		+ "09 08 10 00"
		+ "00 06  05 00  D3 10  CC 07  01 00 00 00  00 06 00 00"
		// FILEPASS
		+ "2F 00 36 00"
		+ "01 00  01 00  01 00"
		+ "BAADF00D BAADF00D BAADF00D BAADF00D" // docId
		+ "DEADBEEF DEADBEEF DEADBEEF DEADBEEF" // saltData
		;

	/**
	 * Hex dump of a sample WINDOW1 record
	 */
	private static final String SAMPLE_WINDOW1 = "3D 00 12 00"
		+ "00 00 00 00 40 38 55 23 38 00 00 00 00 00 01 00 58 02";

	@Rule
	public ExpectedException expectedEx = ExpectedException.none();

	
	/**
	 * Makes sure that a default password mismatch condition is represented with {@link EncryptedDocumentException}
	 */
	@Test
	public void defaultPasswordWrong() {
		// This encodng depends on docId, password and stream position
		final String SAMPLE_WINDOW1_ENCR1 = "3D 00 12 00"
			+ "C4, 9B, 02, 50, 86, E0, DF, 34, FB, 57, 0E, 8C, CE, 25, 45, E3, 80, 01";

		byte[] dataWrongDefault = HexRead.readFromString(""
				+ COMMON_HEX_DATA
				+ "00000000 00000000 00000000 00000001"
				+ SAMPLE_WINDOW1_ENCR1
		);

	    expectedEx.expect(EncryptedDocumentException.class);
	    expectedEx.expectMessage("Default password is invalid for salt/verifier/verifierHash");
		createRFIS(dataWrongDefault);
	}
	
    @Test
    public void defaultPasswordOK() {
        // This encodng depends on docId, password and stream position
        final String SAMPLE_WINDOW1_ENCR1 = "3D 00 12 00"
            + "C4, 9B, 02, 50, 86, E0, DF, 34, FB, 57, 0E, 8C, CE, 25, 45, E3, 80, 01";

        byte[] dataCorrectDefault = HexRead.readFromString(""
                + COMMON_HEX_DATA
                + "137BEF04 969A200B 306329DE 52254005" // correct saltHash for default password (and docId/saltHash)
                + SAMPLE_WINDOW1_ENCR1
        );

        RecordFactoryInputStream rfis = createRFIS(dataCorrectDefault);
        confirmReadInitialRecords(rfis);
    }
	

	/**
	 * Makes sure that an incorrect user supplied password condition is represented with {@link EncryptedDocumentException}
	 */
	@Test
	public void suppliedPasswordWrong() {
		// This encoding depends on docId, password and stream position
		final String SAMPLE_WINDOW1_ENCR2 = "3D 00 12 00"
			+ "45, B9, 90, FE, B6, C6, EC, 73, EE, 3F, 52, 45, 97, DB, E3, C1, D6, FE";

		byte[] dataWrongDefault = HexRead.readFromString(""
				+ COMMON_HEX_DATA
				+ "00000000 00000000 00000000 00000000"
				+ SAMPLE_WINDOW1_ENCR2
		);

		expectedEx.expect(EncryptedDocumentException.class);
		expectedEx.expectMessage("Supplied password is invalid for salt/verifier/verifierHash");

		Biff8EncryptionKey.setCurrentUserPassword("passw0rd");
		try {
			createRFIS(dataWrongDefault);
		} finally {
			Biff8EncryptionKey.setCurrentUserPassword(null);
		}
	}

    @Test
    public void suppliedPasswordOK() {
        // This encoding depends on docId, password and stream position
        final String SAMPLE_WINDOW1_ENCR2 = "3D 00 12 00"
            + "45, B9, 90, FE, B6, C6, EC, 73, EE, 3F, 52, 45, 97, DB, E3, C1, D6, FE";

        byte[] dataCorrectDefault = HexRead.readFromString(""
                + COMMON_HEX_DATA
                + "C728659A C38E35E0 568A338F C3FC9D70" // correct saltHash for supplied password (and docId/saltHash)
                + SAMPLE_WINDOW1_ENCR2
        );

		Biff8EncryptionKey.setCurrentUserPassword("passw0rd");
		try {
			RecordFactoryInputStream rfis = createRFIS(dataCorrectDefault);
			confirmReadInitialRecords(rfis);
		} finally {
			Biff8EncryptionKey.setCurrentUserPassword(null);
		}
    }
	
	
	/**
	 * makes sure the record stream starts with {@link BOFRecord}, {@link FilePassRecord} and then {@link WindowOneRecord}
	 * The third record is decrypted so this method also checks its content.
	 */
	private void confirmReadInitialRecords(RecordFactoryInputStream rfis) {
		assertEquals(BOFRecord.class, rfis.nextRecord().getClass());
		FilePassRecord recFP = (FilePassRecord) rfis.nextRecord();
		WindowOneRecord rec1 = (WindowOneRecord) rfis.nextRecord();
		assertArrayEquals(HexRead.readFromString(SAMPLE_WINDOW1),rec1.serialize());
	}

	private static RecordFactoryInputStream createRFIS(byte[] data) {
		return new RecordFactoryInputStream(new ByteArrayInputStream(data), true);
	}
}

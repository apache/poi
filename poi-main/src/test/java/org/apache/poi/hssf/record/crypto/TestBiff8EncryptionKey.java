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

import java.util.Arrays;

import org.apache.poi.util.HexDump;
import org.apache.poi.util.HexRead;

import junit.framework.ComparisonFailure;
import junit.framework.TestCase;

/**
 * Tests for {@link Biff8EncryptionKey}
 *
 * @author Josh Micich
 */
public final class TestBiff8EncryptionKey extends TestCase {

	private static byte[] fromHex(String hexString) {
		return HexRead.readFromString(hexString);
	}
	public void testCreateKeyDigest() {
		byte[] docIdData = fromHex("17 F6 D1 6B 09 B1 5F 7B 4C 9D 03 B4 81 B5 B4 4A");
		byte[] keyDigest = Biff8EncryptionKey.createKeyDigest("MoneyForNothing", docIdData);
		byte[] expResult = fromHex("C2 D9 56 B2 6B");
		if (!Arrays.equals(expResult, keyDigest)) {
			throw new ComparisonFailure("keyDigest mismatch", HexDump.toHex(expResult), HexDump.toHex(keyDigest));
		}
	}


	public void testValidateWithDefaultPassword() {

		String docIdSuffixA = "F 35 52 38 0D 75 4A E6 85 C2 FD 78 CE 3D D1 B6"; // valid prefix is 'D'
		String saltHashA = "30 38 BE 5E 93 C5 7E B4 5F 52 CD A1 C6 8F B6 2A";
		String saltDataA = "D4 04 43 EC B7 A7 6F 6A D2 68 C7 DF CF A8 80 68";

		String docIdB = "39 D7 80 41 DA E4 74 2C 8C 84 F9 4D 39 9A 19 2D";
		String saltDataSuffixB = "3 EA 8D 52 11 11 37 D2 BD 55 4C 01 0A 47 6E EB"; // valid prefix is 'C'
		String saltHashB = "96 19 F5 D0 F1 63 08 F1 3E 09 40 1E 87 F0 4E 16";

		confirmValid(true, "D" + docIdSuffixA, saltDataA, saltHashA);
		confirmValid(true, docIdB, "C" + saltDataSuffixB, saltHashB);
		confirmValid(false, "E" + docIdSuffixA, saltDataA, saltHashA);
		confirmValid(false, docIdB, "B" + saltDataSuffixB, saltHashB);
	}

	public void testValidateWithSuppliedPassword() {

		String docId = "DF 35 52 38 0D 75 4A E6 85 C2 FD 78 CE 3D D1 B6";
		String saltData = "D4 04 43 EC B7 A7 6F 6A D2 68 C7 DF CF A8 80 68";
		String saltHashA = "8D C2 63 CC E1 1D E0 05 20 16 96 AF 48 59 94 64"; // for password '5ecret'
		String saltHashB = "31 0B 0D A4 69 55 8E 27 A1 03 AD C9 AE F8 09 04"; // for password '5ecret'

		confirmValid(true, docId, saltData, saltHashA, "5ecret");
		confirmValid(false, docId, saltData, saltHashA, "Secret");
		confirmValid(true, docId, saltData, saltHashB, "Secret");
		confirmValid(false, docId, saltData, saltHashB, "secret");
	}


	private static void confirmValid(boolean expectedResult,
			String docIdHex, String saltDataHex, String saltHashHex) {
		confirmValid(expectedResult, docIdHex, saltDataHex, saltHashHex, null);
	}
	private static void confirmValid(boolean expectedResult,
			String docIdHex, String saltDataHex, String saltHashHex, String password) {
		byte[] docId = fromHex(docIdHex);
		byte[] saltData = fromHex(saltDataHex);
		byte[] saltHash = fromHex(saltHashHex);


		Biff8EncryptionKey key;
		if (password == null) {
			key = Biff8EncryptionKey.create(docId);
		} else {
			key = Biff8EncryptionKey.create(password, docId);
		}
		boolean actResult = key.validate(saltData, saltHash);
		if (expectedResult) {
			assertTrue("validate failed", actResult);
		} else {
			assertFalse("validate succeeded unexpectedly", actResult);
		}
	}
}

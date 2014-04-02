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

import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import junit.framework.ComparisonFailure;
import junit.framework.TestCase;

import org.apache.poi.util.HexDump;
import org.apache.poi.util.HexRead;

/**
 * Tests for {@link RC4}
 *
 * @author Josh Micich
 */
public class TestRC4 extends TestCase {
	public void testSimple() {
		confirmRC4("Key", "Plaintext", "BBF316E8D940AF0AD3");
		confirmRC4("Wiki", "pedia", "1021BF0420");
		confirmRC4("Secret", "Attack at dawn", "45A01F645FC35B383552544B9BF5");

	}

	private static void confirmRC4(String k, String origText, String expEncrHex) {
		byte[] actEncr = origText.getBytes();
		new RC4(k.getBytes()).encrypt(actEncr);
		byte[] expEncr = HexRead.readFromString(expEncrHex);

		if (!Arrays.equals(expEncr, actEncr)) {
			throw new ComparisonFailure("Data mismatch", HexDump.toHex(expEncr), HexDump.toHex(actEncr));
		}


		Cipher cipher;
		try {
			cipher = Cipher.getInstance("RC4");
		} catch (GeneralSecurityException e) {
			throw new RuntimeException(e);
		}
		String k2 = k+k; // Sun has minimum of 5 bytes for key
		SecretKeySpec skeySpec = new SecretKeySpec(k2.getBytes(), "RC4");

		try {
			cipher.init(Cipher.DECRYPT_MODE, skeySpec);
		} catch (InvalidKeyException e) {
			throw new RuntimeException(e);
		}
		byte[] origData = origText.getBytes();
		byte[] altEncr = cipher.update(origData);
		if (!Arrays.equals(expEncr, altEncr)) {
			throw new RuntimeException("Mismatch from jdk provider");
		}
	}
}

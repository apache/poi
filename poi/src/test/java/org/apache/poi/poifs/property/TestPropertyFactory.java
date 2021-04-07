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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.poi.poifs.storage.RawDataUtil;
import org.junit.jupiter.api.Test;

/**
 * Class to test PropertyFactory functionality
 */
final class TestPropertyFactory {

    @Test
    void testConvertToProperties() throws IOException {

		// real data from a real file!
		String hexData =
			"H4sIAAAAAAAAANWZ624TRxTHxymXcm2ahjRNUrJAoCGESxFCCPEB22lSqoRECbGE+slxNvEqcYx2bUQ/lUfphz5BnwBegw88" +
			"Ac8A/M/ZWe/E3svMbKjEseIdbyz/zpk5cy6z66KNV0c44jdxgKsv/hImMiaOi09SzuV8983Sv+8/uG9L32D8+Gx4bwH0huiK" +
			"lnCZ7+COi2tdeGJfBLn8y0KUPimS9J1//r7+7fPa29Ib51e+qv+rwmIXtA54bWjgiKf4RNroyZQGv18+4nvzqfwF/vSl+afl" +
			"eBm0gOd9A6OX4G4b6eAU5EckRyzyihPfRMYK8/v9r4aRjzkJ1yNPdPwviX9Mjiuwv4FXEzoE0vvrmAfyQ9Jqi7VJl9mC/EH7" +
			"l/nOnuZKOEfOj2fgGWLRixwvvGbJP5HKL+PTNla/o/NT4qIGP4o7r39/OBB/NrHqtMIqlyz3ZQTME1v/q8hxlb28w7wGs5d4" +
			"Jly+E0elJ3jfwbhf7mrwI7uT7I9XOyL4WIuYnG9/qcf/KeU7Pf5/6xl8GgWYAx/kFwb8IYpB5IdCd/4p9pyS4w2mu7z3yzIX" +
			"OLwq25rxd6g0guucAf8M/uL9F9lfhf/5rMEBZkG3CpgCf5L10OdT6j8px6ugdhDl2rgecO4JfZ8y0b6SidIqgXnwr+L6iwGf" +
			"6pRLcryC33+FtW5xDKAsSLWHfg00Af4orsMG/PP4O57Dd8Qa70GPPSFdZuF/47heMeB/J5LWXyfaDsoo+BdYD33+sMLfgN1b" +
			"StQ3lRHM/y1cpw343yt82mktvDx4WNCLdjXWpasxG9j/xvF3ROEvguRz/WM//6b8Hw7xNzH3FPXJ18Laz5PZMJqPrCp81sL+" +
			"0Uy+WR6YA5/8eULor/9H5XsLHHm2OAbHXuiBuCt1oZzcYE3aCZXYXfDJny4Z8C8o9le47vM44wacBcz8YMpi/ccU/ibXmD5H" +
			"233OPcuszR7rUpcxeY27hIC9YlfWx6E8suCr81/m36MKJDDuvUjGLfg/KvarVbaDFd7JtHZQ5iz44wq/jPmuKhk/v+M9LDb7" +
			"X53/qtzh5Nu01+qGujiF+U2uc7d7Ga8h/aHOcx/dbXFl3BnoSu5j/80IqgP09x/VidH8JzNDP3gOpsu6pcushf0TQvU/l6vu" +
			"dVxbsvrPtniAX7ouuA/Qtn9S4YfRtt7rvTyugcNqTEeXe+DflGxd/pQBPy8TU/2HHkzcNrD/Z4X/DDNfwy607z+GSneEmf0X" +
			"RVb8/4PvEH+nl3nSdbllkX+nxeH6y+fzB6pDdm3qjxLFU5pTXb4jVP8n+7qyBgr3XY118bRWwWb/Ua5ek+NVMJoy+tMe3FH6" +
			"EBeVed4pwAzsp3qeaipdPtXqcf1Z534ryr9xx72Ie25KVIzlgYX9M0Z8Opd7Jc8FB3fjQ9h/Q4R7Wpd/1Yif3Zfes7CfevWo" +
			"/wzjLvnbnnHuJRkumP9U/6uyHj5nHZ97QZfPZNoZFci8BZ965Tj/+fz70Sls1A9FNVmeXC5oP+W/XX4C4Ymk86a8aHxH5/xJ" +
			"nvsknf+sc9zt8Kw3ZIbrXwmKytdkb97fDd0veP5ZBi889QstjM5idFeh6Pkv2f+SOV1e/xXej2GUic9E0/V58L/ww8js9qKA" +
			"Gn+K8Vc49xY5/ynGj5//hJ5XMX7+ZseflONV3m0V0Jvse5R/V/GuK0Xtj8+f1nrVd5nPBJvKs4is/suOPyzHSxz/uui4Y26b" +
			"d35wdOffMu48fvfnQPyJn7894fqvK/1A1SvrSZAOP8n+6PlHGkc3F9o+f9T8eS0x5R+1fM38zxmfK1AAIAAA";

		final byte[] testdata = RawDataUtil.decompress(hexData);
		final ByteArrayInputStream stream = new ByteArrayInputStream(testdata);
		final List<Property> properties = new ArrayList<>();

		final byte[] buf = new byte[512];
		for (int readBytes; (readBytes = stream.read(buf)) != -1; ) {
			byte[] bbuf = buf;
			if (readBytes < 512) {
				bbuf = Arrays.copyOf(buf, readBytes);
			}

			PropertyFactory.convertToProperties(bbuf, properties);
		}

		assertEquals(64, properties.size());
		String[] names = {
			"Root Entry", null, null, null, null, null, null, null, null,
			null, null, null, null, "Deal Information", "Deal Description",
			"Sales Area Code", "Deal Currency", "Outbound Travel Dates",
			"Maximum Stay", "Maximum Stay Period", "Deal Type", "Sub Deal",
			"Commission Value", "Fare Type", "FUD Grid Dimensions",
			"FUD Grid Information", "Double Dealing Indicator",
			"Business Type", "Umbrella Links and Passengers", "Agents Name",
			"Number of Passengers", "ALC Codes", "Consortia Codes",
			"Child Percentage Permitted", "Percentage of Yield",
			"Net Remit Permitted", "Infant Discount Permitted",
			"Infant Discount Value", "TRVA Information",
			"Business Justification", "Surcharge", "Nature of Variation",
			"Other Refund Text", "Cancellation Fee Percentage",
			"Cancellation Fee Fixed Value", "Cancellation Fee Currency",
			"Remarks", "Other Carrier Sectors", "Prorate Comments", null,
			null, null, null, null, null, null, null, null, null, null, null,
			null, null, null
		};
		assertEquals(64, names.length);

		boolean[] isRoot = {
			true, false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false, false,
			false
		};
		assertEquals(64, isRoot.length);

		boolean[] isDocument = {
			false, false, false, false, false, false, false, false, false,
			false, false, false, false, false, true, true, true, true, true,
			true, true, true, true, true, true, true, true, true, true, true,
			true, true, true, true, true, true, true, true, true, true, true,
			true, true, true, true, true, true, true, true, false, false,
			false, false, false, false, false, false, false, false, false,
			false, false, false, false
		};
		assertEquals(64, isDocument.length);

		boolean[] isDirectory = {
			false, false, false, false, false, false, false, false, false,
			false, false, false, false, true, false, false, false, false,
			false, false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false, false,
			false
		};
		assertEquals(64, isDirectory.length);

		boolean[] isNull = {
			false, true, true, true, true, true, true, true, true, true, true,
			true, true, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false, false,
			false, false, true, true, true, true, true, true, true, true,
			true, true, true, true, true, true, true
		};
		assertEquals(64, isNull.length);

		for (int j = 0; j < 64; j++) {
			if (isNull[j]) {
				assertNull(properties.get(j), "Checking property " + j);
			} else {
				assertNotNull(properties.get(j), "Checking property " + j);
				if (isRoot[j]) {
					assertTrue(properties.get(j) instanceof RootProperty, "Checking property " + j);
				}
				if (isDirectory[j]) {
					assertTrue(properties.get(j) instanceof DirectoryProperty, "Checking property " + j);
				}
				if (isDocument[j]) {
					assertTrue(properties.get(j) instanceof DocumentProperty, "Checking property " + j);
				}
				assertEquals(names[j], properties.get(j).getName(), "Checking property " + j);
			}
		}
	}
}

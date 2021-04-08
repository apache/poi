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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hpsf.DocumentSummaryInformation;
import org.apache.poi.hpsf.SummaryInformation;
import org.apache.poi.poifs.common.POIFSConstants;
import org.apache.poi.poifs.filesystem.POIFSStream;
import org.apache.poi.poifs.storage.HeaderBlock;
import org.apache.poi.poifs.storage.RawDataUtil;
import org.junit.jupiter.api.Test;

/**
 * Class to test PropertyTable functionality
 */
final class TestPropertyTable {

	private static void confirmBlockEncoding(String expectedDataStr, PropertyTable table) throws IOException {
		final ByteArrayOutputStream bos = new ByteArrayOutputStream();
		byte[] expectedData = RawDataUtil.decompress(expectedDataStr);

		POIFSStream stream = new POIFSStream(null) {
			@Override
			public OutputStream getOutputStream() {
				return bos;
			}
		};

		table.write(stream);

		assertArrayEquals(expectedData, bos.toByteArray());
	}

	/**
	 * Test PropertyTable
	 * <p>
	 * Running individual tests of the PropertyTable methods, which is the
	 * traditional way to write unit tests (at least for me), seems somewhat
	 * useless in this case. Of greater relevance: if one follows the normal
	 * steps of creating a PropertyTable, and then checking the output, does it
	 * make sense? In other words, more of an integration test.
	 * <p>
	 * So, the test consists of creating a PropertyTable instance, adding three
	 * DocumentProperty instances to it, and then getting the output (including
	 * the preWrite phase first), and comparing it against a real property table
	 * extracted from a file known to be acceptable to Excel.
	 */
	@Test
	void testWriterPropertyTable() throws IOException {

		// create the PropertyTable
	   	HeaderBlock   headerBlock = new HeaderBlock(POIFSConstants.SMALLER_BIG_BLOCK_SIZE_DETAILS);
		PropertyTable table = new PropertyTable(headerBlock);

		// create three DocumentProperty instances and add them to the
		// PropertyTable
		DocumentProperty workbook = new DocumentProperty("Workbook", 0x00046777);

		workbook.setStartBlock(0);
		DocumentProperty summary1 = new DocumentProperty(SummaryInformation.DEFAULT_STREAM_NAME, 0x00001000);

		summary1.setStartBlock(0x00000234);
		DocumentProperty summary2 = new DocumentProperty(DocumentSummaryInformation.DEFAULT_STREAM_NAME, 0x00001000);

		summary2.setStartBlock(0x0000023C);
		table.addProperty(workbook);
		RootProperty root = table.getRoot();

		root.addChild(workbook);
		table.addProperty(summary1);
		root = table.getRoot();
		root.addChild(summary1);
		table.addProperty(summary2);
		root = table.getRoot();
		root.addChild(summary2);
		table.preWrite();

		final String testblock =
			"H4sIAAAAAAAAAAtiyAfCEgYFBleGPCBdxFDJQAoQY2Bl/A8FTETq+QdUC2OHA20vYshmSAK7I5sku0FAiIEJbv9/JHMJgfJ0FjDN" +
			"yhDMUMqQC4SJYL97AkMhDewmkEgJQyaQnYfHHA2g/YxAmhmIibXfBBRQAgxQ+12ANiSD3ZAKjgHS3GNBhv9tkOwHAFGXmbcAAgAA";
		confirmBlockEncoding(testblock, table);

		table.removeProperty(summary1);
		root = table.getRoot();
		root.deleteChild(summary1);
		table.preWrite();

		final String testblock2 =
			"H4sIAAAAAAAAAAtiyAfCEgYFBleGPCBdxFDJQAoQY2Bl/A8FTETq+QdUC2OHA20vYshmSAK7I5sku0FAiIEJbv9/JHMJ" +
			"gfJ0FjDNyuACtDeZoZQhlyEVHALBYHYuQyI4LDyBYmlgN4JEShgygew8JHMsgPYzAmlS7LcBBZQAhA0Ae5Y5UIABAAA=";
		// (N)POIFS only returns 384 bytes here, instead of 512
		confirmBlockEncoding(testblock2, table);

		table.addProperty(summary1);
		root = table.getRoot();
		root.addChild(summary1);
		table.preWrite();

		final String testblock3 =
			"H4sIAAAAAAAAAAtiyAfCEgYFBleGPCBdxFDJQAoQY2Bl/A8FzETq+QdUC2OHA20vYshmSAK7I5sku0FAiIEJbv9/JHMJgfJ0FjDNyu" +
			"ACtDeZoZQhlyEVHALBYHYuQyI4LDyBYmlgN4JEShgygew8JHMsyLDfhglICDBA7SfNPnSgAbSfEUiDjCTWfhMk+wEk2TJjAAIAAA==";
		confirmBlockEncoding(testblock3, table);
	}

	@Test
	void testReadingConstructor() throws IOException {

		// first, we need the raw data blocks
		String raw_data_array =
			"H4sIAAAAAAAAAO2Z608TQRDA5wqVlreIyFMP5AMaMUAMMcYvQIVoUAivxI9HOeCkpaS9GvjmX67OzO61S1930zOpJJ2muUd" +
			"u9zfv3V4PoIAfH2z4BNd4LMIdSGQCktYfLVbEMf34/dWnzjPgggM55H9G/jnqUoQ83vHBw/Pr0LkWwKrwn4o0V7INx6iDDT" +
			"vI9eBMrMdrSDB/GM/pKOVncPYynKIHXGQH3vCQeKF1OcOrLGtCOtXKmuanhfxefdyCS5w/x5bvI72ILJczwUEN3MrdPD7l4" +
			"8fFJ01Z1/w+Ad+6x3eQRswcfqr+tjEyLvO38c4tc204Ye+U8SqQD5r/SMBPGPxjtOwU7Qv4Nuyy96+ghOcO+5984OB1iT1z" +
			"wf4o6fEfNT+QKHxTwu1vFJWqvNf8pMD+HsN+le0Oz03556FlWc5Jdad19AHeaX6vgN8LkvhvoS5FjhA9V9udAn5KwCdf6fY" +
			"Dezi7jxmgLKZYHyHLgZ+sEXnEYbtLTeZ6o/kDAj7l6rw+30RuiTPO5Qyz4QvfIT+cVyq/eQ96q/k9Aj7ZHjX+9RXX2P4hAT" +
			"9l8PeQcsk5ZnMuFLkPq+tDPGZ13wvzf7+Anzb439A26gCKWEBftKr2egn6/6CA32/wD9m/Lkd+g7PcYU8UMBeb+dwUG/mzm" +
			"h2VPwCN/X+Ax3OjDlzsu37IXIvIfybkDxr8r2jvLUY8z3GgmFOPu6t0Ho890VyWtP/HIbr/h8CMv8t5TrarLhfe8xrxnwj4" +
			"wwa/8Zqr8vA7V0IuEj8h4I+AaT/1lzJnXsA94Tr0Iu3CAv6YgD/Kdiup339lOBvIHyVNb159ik/zPRbwSdfA/ur+M8NVmGU" +
			"9bgT7z4Q1BbL8p1xJ6/MjzLwTrPz2978Ja1LIp1qp5l+RmWqVU50nr/3vt/R8lT8h5JsS8DzuuMHaH7bq3OePCPn0OyGs/0" +
			"SVaeTbQj75K6nPq/nXep/TTGaRPyfkU78O9j9busIoB3yu+erqEx59tf7MCPmm1O9/jtD2m0hrHwnZPy3kU73U17+MG8g48" +
			"l8K+VNgrv9l7v+X3HMv2uLPC/nTBn+DmarWbV4N8iIdJpH/QsifMfjbbLcby//PhfxZuO//U+OXt1TGkL8o5M+B6f9drDdZ" +
			"zZlC9i8I+aaofQ/F4ErMJhmN+fs3rgT7ni6/PX7teKnEHZ/q8Pj4+vfAzuZ+jPFzsLTxanV9eS/rL6+trKzafsE2LkPHP3T" +
			"/Pezx8evH6rj+Kd0H/sVRzp+MaX8Sfjh5t9Tm+M7nrwVhNd56fNz+063/OOPj2t9p+R3zS+9d2hnXlf9DLN27g/+E6L0E/T" +
			"/Rp/t5WseX3hnTe9uhmnh35WHLX544XEIAIAAA";

		// Fake up a header
		HeaderBlock header_block = new HeaderBlock(POIFSConstants.SMALLER_BIG_BLOCK_SIZE_DETAILS);
		header_block.setPropertyStart(0);

		List<ByteBuffer> data_blocks = new ArrayList<>();
		try (InputStream is = new ByteArrayInputStream(RawDataUtil.decompress(raw_data_array))) {
			byte[] buf = new byte[header_block.getBigBlockSize().getBigBlockSize()];

			for (int readBytes; (readBytes = is.read(buf)) != -1; ) {
				data_blocks.add(ByteBuffer.wrap(buf.clone(), 0, readBytes));
			}
		}


		// get property table from the document
		PropertyTable table = new PropertyTable(header_block, data_blocks);

		assertEquals(30 * 64, table.getRoot().getSize());
		int count = 0;
		Property lastChild = null;
		for (Property p : table.getRoot()) {
			assertNotNull(p);
			lastChild = p;
			++count;
		}

		assertNotNull(lastChild, "no children found");
		assertEquals(1, count);
		assertTrue(lastChild.isDirectory());
		count = 0;
		for (Property p : (DirectoryProperty) lastChild) {
			assertNotNull(p);
			++count;
		}
		assertEquals(35, count);
	}
}

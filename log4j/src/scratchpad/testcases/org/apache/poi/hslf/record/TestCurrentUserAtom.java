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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.apache.poi.POIDataSamples;
import org.apache.poi.hslf.exceptions.EncryptedPowerPointFileException;
import org.apache.poi.hslf.usermodel.HSLFSlideShowImpl;
import org.apache.poi.poifs.filesystem.DocumentEntry;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.junit.jupiter.api.Test;

/**
 * Tests that CurrentUserAtom works properly.
 *
 * @author Nick Burch (nick at torchbox dot com)
 */
public final class TestCurrentUserAtom {
    private static final POIDataSamples _slTests = POIDataSamples.getSlideShowInstance();
	/** Not encrypted */
	private static final String normalFile = "basic_test_ppt_file.ppt";
	/** Encrypted */
	private static final String encFile = "Password_Protected-hello.ppt";

	@Test
	void readNormal() throws Exception {
		POIFSFileSystem fs = new POIFSFileSystem(_slTests.getFile(normalFile));

		CurrentUserAtom cu = new CurrentUserAtom(fs.getRoot());
		fs.close();

		// Check the contents
		assertEquals("Hogwarts", cu.getLastEditUsername());
		assertEquals(0x2942, cu.getCurrentEditOffset());

		// Round trip
		POIFSFileSystem poifs = new POIFSFileSystem();
		cu.writeToFS(poifs);

		CurrentUserAtom cu2 = new CurrentUserAtom(poifs.getRoot());
		assertEquals("Hogwarts", cu2.getLastEditUsername());
		assertEquals(0x2942, cu2.getCurrentEditOffset());

		poifs.close();
	}

	@Test
	void readEnc() throws Exception {
        try (POIFSFileSystem fs = new POIFSFileSystem(_slTests.getFile(encFile))) {
            new CurrentUserAtom(fs.getRoot());
			assertThrows(EncryptedPowerPointFileException.class, () -> new HSLFSlideShowImpl(fs).close());
        }
	}

	@Test
	void writeNormal() throws Exception {
		// Get raw contents from a known file
		byte[] contents;
		try (POIFSFileSystem fs = new POIFSFileSystem(_slTests.getFile(normalFile))) {
			DocumentEntry docProps = (DocumentEntry) fs.getRoot().getEntry("Current User");
			contents = new byte[docProps.getSize()];
			try (InputStream in = fs.getRoot().createDocumentInputStream("Current User")) {
				in.read(contents);
			}
		}

		// Now build up a new one
		CurrentUserAtom cu = new CurrentUserAtom();
		cu.setLastEditUsername("Hogwarts");
		cu.setCurrentEditOffset(0x2942);

		// Check it matches
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		cu.writeOut(baos);
		byte[] out = baos.toByteArray();

		assertArrayEquals(contents, out);
	}
}

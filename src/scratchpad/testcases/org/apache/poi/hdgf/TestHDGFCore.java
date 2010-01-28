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

package org.apache.poi.hdgf;

import java.io.FileInputStream;

import org.apache.poi.hdgf.streams.PointerContainingStream;
import org.apache.poi.hdgf.streams.TrailerStream;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.POIDataSamples;

import junit.framework.TestCase;

public final class TestHDGFCore extends TestCase {
    private static POIDataSamples _dgTests = POIDataSamples.getDiagramInstance();

	private POIFSFileSystem fs;

	protected void setUp() throws Exception {
		fs = new POIFSFileSystem(_dgTests.openResourceAsStream("Test_Visio-Some_Random_Text.vsd"));
	}

	public void testCreate() throws Exception {
		new HDGFDiagram(fs);
	}

	public void testTrailer() throws Exception {
		HDGFDiagram hdgf = new HDGFDiagram(fs);
		assertNotNull(hdgf);
		assertNotNull(hdgf.getTrailerStream());

		// Check it has what we'd expect
		TrailerStream trailer = hdgf.getTrailerStream();
		assertEquals(0x8a94, trailer.getPointer().getOffset());

		assertNotNull(trailer.getPointedToStreams());
		assertEquals(20, trailer.getPointedToStreams().length);

		assertEquals(20, hdgf.getTopLevelStreams().length);

		// 9th one should have children
		assertNotNull(trailer.getPointedToStreams()[8]);
		assertNotNull(trailer.getPointedToStreams()[8].getPointer());
		PointerContainingStream ps8 = (PointerContainingStream)
			trailer.getPointedToStreams()[8];
		assertNotNull(ps8.getPointedToStreams());
		assertEquals(8, ps8.getPointedToStreams().length);
	}

	/**
	 * Tests that we can open a problematic file, that used to
	 *  break with a negative chunk length
	 */
	public void testNegativeChunkLength() throws Exception {
		fs = new POIFSFileSystem(_dgTests.openResourceAsStream("NegativeChunkLength.vsd"));

		HDGFDiagram hdgf = new HDGFDiagram(fs);
		assertNotNull(hdgf);
		
		// And another file
		fs = new POIFSFileSystem(_dgTests.openResourceAsStream("NegativeChunkLength2.vsd"));
		hdgf = new HDGFDiagram(fs);
		assertNotNull(hdgf);
	}
	
	/**
	 * Tests that we can open a problematic file that triggers
	 *  an ArrayIndexOutOfBoundsException when processing the
	 *  chunk commands.
	 * @throws Exception
	 */
	public void DISABLEDtestAIOOB() throws Exception {
      fs = new POIFSFileSystem(_dgTests.openResourceAsStream("44501.vsd"));

      HDGFDiagram hdgf = new HDGFDiagram(fs);
      assertNotNull(hdgf);
	}
}

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

package org.apache.poi.hdgf.streams;

import static org.apache.poi.poifs.storage.RawDataUtil.decompress;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.apache.poi.hdgf.pointers.Pointer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public final class TestStreamBasics extends StreamTest {
	private static byte[] compressedStream, uncompressedStream;

	@BeforeAll
	public static void init() throws IOException {
        compressedStream = decompress(
            "H4sIAAAAAAAAAAFTAaz+e8QC6/ABAAC48/BO4PsBAAPr8AoFBOvwFQnr8Gfr8CLc/zQPRg94WA/5/u"+
            "T3hRUALIYB/GgP6PMoniBOZr3//uJAKL2Pt3SeqwJCewltqwKnDsi7rbHe/XhuS/f2FPrn9Bbr8PSv"+
            "QwGAuuvwVOvwRgAX6/BML9hPAdTr8CADEgwR1bwREPgVFv/r8Kz/3U/39mAALuvwLNnXTwF38/CW8/"+
            "BUAH0a6/BE2k8BEQoAnzIKAADWlA92HwD9HevwnOdPAe5h3EwQ6/BWACT7AftPPwGEYgAAHAMU3v19"+
            "IevwZPxPAaSlEOoYEynr8NTFEGxkAOsAR5cSJ1UR/U8BX5RxAABoAxIxMRH/QFUBAHIAAKPc6/BkHw"+
            "AA2OvwpEJ/VQFidwAA0E8S/TLvAUNVAVGBANcADgYEET/BEURVvwEeiAAAKk8SRH7r8LRFVQFmiUgl"+
            "AGEhwtTYaVMBAAA="
        );

        uncompressedStream = decompress(
            "H4sIAAAAAAAAAGNgZGDYAcSogJGBGUjCMAsQcwJxOhAroSulEkB2Qqsogw4I41KrMU/BL23vv0cOGn"+
            "v7t5eAGU7VnLlgBobUibUb0fVX5HnDrROB0mJA/GW9M2MDkA4BYjcGcSDpc8Of8QqQVgCLgkT2AEV+"+
            "wEX+A8k1//3hpiUw6AFJnZv+jOVAsWmMIDVSQBGXW/6MgsCgNOJiYLhGVHjIAvGc5/6M7xKB5gDZYQ"+
            "wqIBf+9mdsSWJgkIG6Eh0oAnHKH3/GJUkwF2oCyStAkZwUBgZ3sDnqIPf89WecUsjAkAFWYwjyhUMo"+
            "I0MRA8NiBuwuvAHES5xCGZPKGRgugP1lBAo951DGwEYGBj42kIg9yHaXUEa5DgYGLbAaF6DIFtdQxr"+
            "ROmAgIAAD6SJPAdAIAAA=="
        );
	}


	@Test
	void testCompressedStream() {
		// Create a fake pointer
		Pointer ptr = new TestPointer(true, 0, compressedStream.length, -1, (short)-1);
		// Now the stream
		Stream stream = Stream.createStream(ptr, compressedStream, null, null);

		// Check
		assertNotNull(stream.getPointer());
		assertNotNull(stream.getStore());
		assertTrue(stream.getStore() instanceof CompressedStreamStore);
		assertTrue(stream instanceof UnknownStream);

		// Check the stream store
		CompressedStreamStore ss = (CompressedStreamStore)stream.getStore();
		assertEquals(4, ss._getBlockHeader().length);
		assertEquals(compressedStream.length, ss._getCompressedContents().length);
		assertEquals(uncompressedStream.length, ss.getContents().length);

		for(int i=0; i<uncompressedStream.length; i++) {
			assertEquals(uncompressedStream[i], ss.getContents()[i]);
		}
	}

	@Test
    void testUncompressedStream() {
		// Create a fake pointer
		Pointer ptr = new TestPointer(false, 0, uncompressedStream.length, -1, (short)-1);
		// Now the stream
		Stream stream = Stream.createStream(ptr, uncompressedStream, null, null);

		// Check
		assertNotNull(stream.getPointer());
		assertNotNull(stream.getStore());
		assertFalse(stream.getStore() instanceof CompressedStreamStore);
		assertTrue(stream instanceof UnknownStream);
	}
}

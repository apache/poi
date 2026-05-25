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

package org.apache.poi.fuzz;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.util.NoSuchElementException;

import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.poi.hdgf.extractor.VisioTextExtractor;
import org.apache.poi.ooxml.POIXMLException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JRuntimeException;
import org.apache.poi.util.RecordFormatException;
import org.apache.poi.xdgf.usermodel.XmlVisioDocument;

public class POIVisioFuzzer {
	public static void fuzzerInitialize() {
		POIFuzzer.adjustLimits();
	}

	public static void fuzzerTestOneInput(byte[] input) {
		try (XmlVisioDocument visio = new XmlVisioDocument(new ByteArrayInputStream(input))) {
			visio.write(NullOutputStream.INSTANCE);
		} catch (IOException | POIXMLException |
				 BufferUnderflowException | RecordFormatException | OpenXML4JRuntimeException |
				 IllegalArgumentException | IndexOutOfBoundsException | IllegalStateException e) {
			// expected here
		} catch (NullPointerException e) {
			// can be removed when commons-compress is > 1.28.0 and changes from COMPRESS-598 are applied
			if (!ExceptionUtils.getStackTrace(e).contains("ZipArchiveInputStream.read")) {
				throw e;
			}
		}

		try (VisioTextExtractor extractor = new VisioTextExtractor(new ByteArrayInputStream(input))) {
			POIFuzzer.checkExtractor(extractor);
		} catch (IOException | POIXMLException | IllegalArgumentException | BufferUnderflowException |
				 RecordFormatException | IndexOutOfBoundsException | ArithmeticException | IllegalStateException |
				 NoSuchElementException e) {
			// expected
		}
	}
}

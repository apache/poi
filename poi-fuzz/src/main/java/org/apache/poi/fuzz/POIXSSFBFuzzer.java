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

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.poi.ooxml.POIXMLException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.util.RecordFormatException;
import org.apache.poi.xssf.binary.XSSFBParseException;
import org.apache.poi.xssf.extractor.XSSFBEventBasedExcelExtractor;
import org.apache.xmlbeans.XmlException;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class POIXSSFBFuzzer {
	public static void fuzzerInitialize() {
		POIFuzzer.adjustLimits();
	}

	public static void fuzzerTestOneInput(byte[] input) {
		if (input.length == 0) {
			return;
		}

		try (OPCPackage pkg = OPCPackage.open(new ByteArrayInputStream(input), true)) {
			XSSFBEventBasedExcelExtractor ex = new XSSFBEventBasedExcelExtractor(pkg);
			ex.getText();

			/* Not supported:
			ex.setFormulasNotResults(true);
			ex.getText();*/

			ex.setHandleHyperlinksInCells(true);
			ex.getText();
		} catch (IOException | XmlException | OpenXML4JException | POIXMLException |
                 RecordFormatException | IndexOutOfBoundsException | IllegalArgumentException |
				 IllegalStateException | ArithmeticException | XSSFBParseException ex) {
			// expected
		} catch (NullPointerException e) {
			// can be removed when commons-compress is > 1.28.0 and changes from COMPRESS-598 are applied
			if (!ExceptionUtils.getStackTrace(e).contains("org.apache.commons.compress.archivers.zip.ZipArchiveInputStream.read")) {
				throw e;
			}
        }
	}
}

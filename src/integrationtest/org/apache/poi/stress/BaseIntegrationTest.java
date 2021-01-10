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
package org.apache.poi.stress;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipException;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.OldFileFormatException;
import org.apache.poi.poifs.filesystem.OfficeXmlFileException;
import org.junit.jupiter.api.Test;

/**
 * This class is used for mass-regression testing via a
 * separate project, this class provides functionality to
 * run integration tests on one file and handle some
 * types of files/exceptions, e.g. old file formats.
 *
 */
public class BaseIntegrationTest {
	private final File rootDir;
	private final String file;
	private FileHandler handler;

	public BaseIntegrationTest(File rootDir, String file, FileHandler handler) {
		this.rootDir = rootDir;
		this.file = file;
		this.handler = handler;
	}

	void test() throws Exception {
        assertNotNull( handler, "Unknown file extension for file: " + file + ": " + TestAllFiles.getExtension(file) );
		testOneFile(new File(rootDir, file));
	}

	protected void testOneFile(File inputFile) throws Exception {
		try {
			handleFile(inputFile);
		} catch (OfficeXmlFileException e) {
			// switch XWPF and HWPF and so forth depending on the error message
			handleWrongOLE2XMLExtension(inputFile, e);
		} catch (OldFileFormatException e) {
			// Not even text extraction is supported for these: handler.handleExtracting(inputFile);
			assumeFalse( true, "File " + file + " excluded because it is an unsupported old format" );
		} catch (EncryptedDocumentException e) {
			// Do not try to read encrypted files
			assumeFalse( true, "File " + file + " excluded because it is password-encrypted" );
		} catch (ZipException e) {
			// some files are corrupted
			if (e.getMessage().equals("unexpected EOF") || e.getMessage().equals("Truncated ZIP file")) {
				assumeFalse( true, "File " + file + " excluded because the Zip file is incomplete" );
			}

			throw e;
		} catch (IOException e) {
			// ignore some other ways of corrupted files
			String message = e.getMessage();
			if(message != null && message.contains("Truncated ZIP file")) {
				assumeFalse( true, "File " + file + " excluded because the Zip file is incomplete" );
			}

			// sometimes binary format has XML-format-extension...
			if(message != null && message.contains("rong file format or file extension for OO XML file")) {
				handleWrongOLE2XMLExtension(inputFile, e);
				return;
			}

			throw e;
		} catch (IllegalArgumentException e) {
			// ignore errors for documents with incorrect extension
			String message = e.getMessage();
			if(message != null && (message.equals("The document is really a RTF file") ||
					message.equals("The document is really a PDF file") ||
					message.equals("The document is really a HTML file"))) {
				assumeFalse( true, "File " + file + " excluded because it is actually a PDF/RTF/HTML file" );
			}

			if(message != null && message.equals("The document is really a OOXML file")) {
				handleWrongOLE2XMLExtension(inputFile, e);
				return;
			}

			throw e;
		}

		try {
			handler.handleExtracting(inputFile);
		} catch (EncryptedDocumentException e) {
			// Do not try to read encrypted files
			assumeFalse( true, "File " + file + " excluded because it is password-encrypted" );
		}
	}

    void handleWrongOLE2XMLExtension(File inputFile, Exception e) throws Exception {
		// we sometimes have wrong extensions, so for some exceptions we try to handle it
		// with the correct FileHandler instead
		String message = e.getMessage();

		// ignore some file-types that we do not want to handle here
		assumeFalse( message != null && (message.equals("The document is really a RTF file") ||
					message.equals("The document is really a PDF file") ||
					message.equals("The document is really a HTML file")), "File " + file + " excluded because it is actually a PDF/RTF/HTML file" );

		if(message != null && (message.equals("The document is really a XLS file"))) {
			handler = TestAllFiles.HANDLERS.get(".xls");
		} else if(message != null && (message.equals("The document is really a PPT file"))) {
			handler = TestAllFiles.HANDLERS.get(".ppt");
		} else if(message != null && (message.equals("The document is really a DOC file"))) {
			handler = TestAllFiles.HANDLERS.get(".doc");
		} else if(message != null && (message.equals("The document is really a VSD file"))) {
			handler = TestAllFiles.HANDLERS.get(".vsd");

		// use XWPF instead of HWPF and XSSF instead of HSSF as the file seems to have the wrong extension
		} else if (handler instanceof HWPFFileHandler) {
            handler = TestAllFiles.HANDLERS.get(".docx");
        } else if (handler instanceof HSSFFileHandler) {
            handler = TestAllFiles.HANDLERS.get(".xlsx");
        } else if (handler instanceof HSLFFileHandler) {
			handler = TestAllFiles.HANDLERS.get(".pptx");

		// and the other way around, use HWPF instead of XWPF and so forth
		} else if(handler instanceof XWPFFileHandler) {
			handler = TestAllFiles.HANDLERS.get(".doc");
		} else if(handler instanceof XSSFFileHandler) {
			handler = TestAllFiles.HANDLERS.get(".xls");
		} else if(handler instanceof XSLFFileHandler) {
			handler = TestAllFiles.HANDLERS.get(".ppt");
        } else {
			// nothing matched => throw the exception to the outside
			throw e;
		}

		// we found a different handler to try processing again
		handleFile(inputFile);
	}

	private void handleFile(File inputFile) throws Exception {
		try (InputStream newStream = new BufferedInputStream(new FileInputStream(inputFile), 64*1024)) {
			handler.handleFile(newStream, inputFile.getAbsolutePath());
		}
	}
}

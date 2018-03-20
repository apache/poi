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
package org.apache.poi;

import org.apache.poi.hslf.exceptions.OldPowerPointFormatException;
import org.apache.poi.hssf.OldExcelFormatException;
import org.apache.poi.hwpf.OldWordFileFormatException;
import org.apache.poi.poifs.filesystem.OfficeXmlFileException;
import org.apache.poi.stress.*;
import org.junit.Assume;

import java.io.*;
import java.util.zip.ZipException;

import static org.junit.Assert.assertNotNull;

public class BaseIntegrationTest {
	private final File rootDir;
	private String file;
	private FileHandler handler;

	public BaseIntegrationTest(File rootDir, String file, FileHandler handler) {
		this.rootDir = rootDir;
		this.file = file;
		this.handler = handler;
	}

	public void test() throws Exception {
        assertNotNull("Unknown file extension for file: " + file + ": " + TestAllFiles.getExtension(file), handler);

        File inputFile = new File(rootDir, file);
        try {
            handleFile(inputFile);
        } catch (OfficeXmlFileException e) {
        	// check if the file-extension is wrong
        	if(!e.getMessage().contains("data appears to be in the Office 2007")) {
        		throw e;
        	}

        	// use XWPF instead of HWPF and XSSF instead of HSSF as the file seems to have the wrong extension
			handleWrongExtension(inputFile, e);
		} catch (OldWordFileFormatException | OldExcelFormatException | OldPowerPointFormatException e) {
        	// at least perform extracting tests on these old files
        } catch (OldFileFormatException e) {
            // Not even text extraction is supported for these: handler.handleExtracting(inputFile);
			//noinspection ConstantConditions
			Assume.assumeFalse("File " + file + " excluded because it is unsupported old Excel format", true);
        } catch (EncryptedDocumentException e) {
        	// Do not try to read encrypted files
			//noinspection ConstantConditions
			Assume.assumeFalse("File " + file + " excluded because it is password-encrypted", true);
        } catch (ZipException e) {
			// some files are corrupted
			if (e.getMessage().equals("unexpected EOF")) {
				//noinspection ConstantConditions
				Assume.assumeFalse("File " + file + " excluded because the Zip file is incomplete", true);
			}

			throw e;
		} catch (IOException e) {
			// sometimes binary format has XML-format-extension...
			if(e.getMessage().contains("rong file format or file extension for OO XML file")) {
				handleWrongExtension(inputFile, e);
			} else {
				throw e;
			}
        } catch (IllegalArgumentException e) {
        	// ignore errors for documents with incorrect extension
        	String message = e.getMessage();
			if(message != null && (message.equals("The document is really a RTF file") ||
        			message.equals("The document is really a PDF file") ||
					message.equals("The document is really a HTML file"))) {
				//noinspection ConstantConditions
				Assume.assumeFalse("File " + file + " excluded because it is actually a PDF/RTF file", true);
			}

			if(e.getMessage().equals("The document is really a OOXML file")) {
				handleWrongExtension(inputFile, e);
			} else {
				throw e;
			}
        }

        try {
        	handler.handleExtracting(inputFile);
		} catch (EncryptedDocumentException e) {
			// Do not try to read encrypted files
			//noinspection ConstantConditions
			Assume.assumeFalse("File " + file + " excluded because it is password-encrypted", true);
		}
	}

	void handleWrongExtension(File inputFile, Exception e) throws Exception {
		// use XWPF instead of HWPF and XSSF instead of HSSF as the file seems to have the wrong extension
		if (handler instanceof HWPFFileHandler) {
            handler = TestAllFiles.HANDLERS.get(".docx");
            handleFile(inputFile);
        } else if (handler instanceof HSSFFileHandler) {
            handler = TestAllFiles.HANDLERS.get(".xlsx");
            handleFile(inputFile);
        } else if (handler instanceof HSLFFileHandler) {
			handler = TestAllFiles.HANDLERS.get(".pptx");
			handleFile(inputFile);
		// and the other way around, use HWPF instead of XWPF and so forth
		} else if(handler instanceof XWPFFileHandler) {
			handler = TestAllFiles.HANDLERS.get(".doc");
			handleFile(inputFile);
		} else if(handler instanceof XSSFFileHandler) {
			handler = TestAllFiles.HANDLERS.get(".xls");
			handleFile(inputFile);
		} else if(handler instanceof XSLFFileHandler) {
			handler = TestAllFiles.HANDLERS.get(".ppt");
			handleFile(inputFile);
        } else {
            throw e;
        }
	}

	private void handleFile(File inputFile) throws Exception {
		try (InputStream newStream = new BufferedInputStream(new FileInputStream(inputFile), 64*1024)) {
			handler.handleFile(newStream, inputFile.getAbsolutePath());
		}
	}
}

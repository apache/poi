/*
 *  ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ====================================================================
 */

package org.apache.poi.stress;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.poifs.filesystem.FileMagic;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.util.SuppressForbidden;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.tools.ant.DirectoryScanner;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Helper class to scan a folder for files and return a collection of
 * found files together with the matching {@link FileHandler}.
 *
 * Can also be used to get the appropriate FileHandler for a single file.
 */
class POIFileScanner {
    private final static File ROOT_DIR;
    static {
        // when running in Gradle, current directory might be "build/integrationtest"
        if(new File("../../test-data").exists()) {
            ROOT_DIR = new File("../../test-data");
        } else {
            ROOT_DIR = new File("test-data");
        }
    }

    /**
     * Scan a folder for files and return a collection of
     * found files together with the matching {@link FileHandler}.
     *
     * Note: unknown files will be assigned to {@link TestAllFiles.NullFileHandler}
     *
     * @param rootDir The directory to scan
     * @return A collection with file-FileHandler pairs which can be used for running tests on that file
     * @throws IOException If determining the file-type fails
     */
    public static Collection<Map.Entry<String, FileHandler>> scan(File rootDir) throws IOException {
        DirectoryScanner scanner = new DirectoryScanner();
        scanner.setBasedir(rootDir);

        scanner.setExcludes(TestAllFiles.SCAN_EXCLUDES);

        System.out.println("Scanning for files in " + rootDir);

        scanner.scan();

        String[] includedFiles = scanner.getIncludedFiles();
        System.out.println("Handling " + includedFiles.length + " files");

        List<Map.Entry<String, FileHandler>> files = new ArrayList<>();
        for(String file : includedFiles) {
            // breaks files with slash in their name on Linux:
            // file = file.replace('\\', '/'); // ... failures/handlers lookup doesn't work on windows otherwise

            FileHandler fileHandler = getFileHandler(rootDir, file);

            files.add(new AbstractMap.SimpleImmutableEntry<>(file, fileHandler));

            if(files.size() % 100 == 0) {
                System.out.print(".");
                if(files.size() % 100_000 == 0) {
                    System.out.println(file);
                }
            }
        }
        System.out.println();

        return files;
    }

    /**
     * Get the FileHandler for a single file
     *
     * @param rootDir The directory where the file resides
     * @param file The name of the file without directory
     * @return The matching {@link FileHandler}, A {@link TestAllFiles.NullFileHandler}
     *          is returned if no match is found
     * @throws IOException If determining the file-type fails
     */
    protected static FileHandler getFileHandler(File rootDir, String file) throws IOException {
        FileHandler fileHandler = TestAllFiles.HANDLERS.get(TestAllFiles.getExtension(file));
        if(fileHandler == null) {
            // we could not detect a type of file based on the extension, so we
            // need to take a close look at the file
            fileHandler = detectUnnamedFile(rootDir, file);
        }
        return fileHandler;
    }

    private static FileHandler detectUnnamedFile(File rootDir, String file) throws IOException {
        File testFile = new File(rootDir, file);

        // find out if it looks like OLE2 (HSSF, HSLF, HWPF, ...) or OOXML (XSSF, XSLF, XWPF, ...)
        // and then determine the file type accordingly
        FileMagic magic = FileMagic.valueOf(testFile);
        switch (magic) {
            case OLE2: {
                try {
                    try (POIFSFileSystem fs = new POIFSFileSystem(testFile, true)) {
                        HSSFWorkbook.getWorkbookDirEntryName(fs.getRoot());
                    }

                    // we did not get an exception, so it seems this is a HSSFWorkbook
                    return TestAllFiles.HANDLERS.get(".xls");
                } catch (IOException | RuntimeException e) {
                    try {
                        try (FileInputStream istream = new FileInputStream(testFile)) {
                            try (HWPFDocument ignored = new HWPFDocument(istream)) {
                                // seems to be a valid document
                                return TestAllFiles.HANDLERS.get(".doc");
                            }
                        }
                    } catch (IOException | RuntimeException e2) {
                        System.out.println("Could not open POIFSFileSystem for OLE2 file " + testFile + ": " + e + " and " + e2);
                        return TestAllFiles.NullFileHandler.instance;
                    }
                }
            }
            case OOXML: {
                try {
                    try (Workbook ignored = WorkbookFactory.create(testFile, null, true)) {
                        // seems to be a valid workbook
                        return TestAllFiles.HANDLERS.get(".xlsx");
                    }
                } catch (IOException | RuntimeException e) {
                    try {
                        try (FileInputStream is = new FileInputStream(testFile)) {
                            try (XWPFDocument ignored = new XWPFDocument(is)) {
                                // seems to be a valid document
                                return TestAllFiles.HANDLERS.get(".docx");
                            }
                        }
                    } catch (IOException | RuntimeException e2) {
                        System.out.println("Could not open POIFSFileSystem for OOXML file " + testFile + ": " + e + " and " + e2);
                        return TestAllFiles.NullFileHandler.instance;
                    }
                }
            }

            // do not warn about a few detected file types
            case RTF:
            case PDF:
            case HTML:
            case XML:
            case JPEG:
            case GIF:
            case TIFF:
            case WMF:
            case EMF:
            case BMP:
                return TestAllFiles.NullFileHandler.instance;
        }

        System.out.println("Did not get a handler for extension " + TestAllFiles.getExtension(file) +
                " of file " + file + ": " + magic);
        return TestAllFiles.NullFileHandler.instance;
    }

    @Disabled
    @Test
    @SuppressForbidden("Just an ignored test")
    void testInvalidFile() throws IOException, InterruptedException {
        FileHandler fileHandler = POIFileScanner.getFileHandler(new File("/usbc/CommonCrawl"),
                "www.bgs.ac.uk_downloads_directdownload.cfm_id=2362&noexcl=true&t=west_20sussex_20-_20building_20stone_20quarries");

        assertEquals(XSSFFileHandler.class, fileHandler.getClass());

        // to show the output from ZipFile() from commons-compress
        // although I did not find out yet why the ZipFile is not closed here
        System.gc();
        Thread.sleep(1000);
        System.gc();
        Thread.sleep(1000);
    }

    @Test
    void testDetectUnnamedFile() throws IOException {
        File root = new File(ROOT_DIR, "spreadsheet");
        assertDoesNotThrow(() -> POIFileScanner.detectUnnamedFile(root, "49156.xlsx"));
    }

    @Test
    void test() throws IOException {
        assertDoesNotThrow(() -> POIFileScanner.scan(ROOT_DIR));
    }
}

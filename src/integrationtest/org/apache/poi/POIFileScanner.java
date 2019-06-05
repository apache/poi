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

package org.apache.poi;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.poifs.filesystem.FileMagic;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.stress.FileHandler;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.tools.ant.DirectoryScanner;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Helper class to scan a folder for files and return a collection of
 * found files together with the matching {@link FileHandler}.
 *
 * Can also be used to get the appropriate FileHandler for a single file.
 */
public class POIFileScanner {
    /**
     * Scan a folder for files and return a collection of
     * found files together with the matching {@link FileHandler}.
     *
     * Note: unknown files will be assigned to {@link org.apache.poi.TestAllFiles.NullFileHandler}
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

        System.out.println("Handling " + scanner.getIncludedFiles().length + " files");

        List<Map.Entry<String, FileHandler>> files = new ArrayList<>();
        for(String file : scanner.getIncludedFiles()) {
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
     * @return The matching {@link FileHandler}, A {@link org.apache.poi.TestAllFiles.NullFileHandler}
     *          is returned if no match is found
     * @throws IOException If determining the file-type fails
     */
    protected static FileHandler getFileHandler(File rootDir, String file) throws IOException {
        FileHandler fileHandler = TestAllFiles.HANDLERS.get(TestAllFiles.getExtension(file));
        if(fileHandler == null) {
            File testFile = new File(rootDir, file);
            FileMagic magic = FileMagic.valueOf(testFile);
            // if we have a file-type that we can read, but no extension, we try to determine the
            // file type manually

            switch(magic) {
                case OLE2: {
                    try {
                        try (POIFSFileSystem fs = new POIFSFileSystem(testFile, true)) {
                            HSSFWorkbook.getWorkbookDirEntryName(fs.getRoot());
                        }

                        // we did not get an exception, so it seems this is a HSSFWorkbook
                        fileHandler = TestAllFiles.HANDLERS.get(".xls");
                    } catch (IOException | RuntimeException e) {
                        try {
                            try (FileInputStream istream = new FileInputStream(testFile)) {
                                try (HWPFDocument ignored = new HWPFDocument(istream)) {
                                    // seems to be a valid document
                                    fileHandler = TestAllFiles.HANDLERS.get(".doc");
                                }
                            }
                        } catch (IOException | RuntimeException e2) {
                            System.out.println("Could not open POIFSFileSystem for OLE2 file " + testFile + ": " + e + " and " + e2);
                            fileHandler = new TestAllFiles.NullFileHandler();
                        }
                    }
                    break;
                }
                case OOXML: {
                    try {
                        WorkbookFactory.create(testFile);

                        // seems to be a valid workbook
                        fileHandler = TestAllFiles.HANDLERS.get(".xlsx");
                    } catch (IOException | RuntimeException e) {
                        try {
                            try (FileInputStream is = new FileInputStream(testFile)) {
                                try (XWPFDocument ignored = new XWPFDocument(is)) {
                                    // seems to be a valid document
                                    fileHandler = TestAllFiles.HANDLERS.get(".docx");
                                }
                            }
                        } catch (IOException | RuntimeException e2) {
                            System.out.println("Could not open POIFSFileSystem for OOXML file " + testFile + ": " + e + " and " + e2);
                            fileHandler = new TestAllFiles.NullFileHandler();
                        }
                    }
                    break;
                }

                // do not warn about a few detected file types
                case RTF:
                case PDF:
                case HTML:
                    fileHandler = new TestAllFiles.NullFileHandler();
                    break;
            }

            if(fileHandler == null) {
                System.out.println("Did not get a handler for extension " + TestAllFiles.getExtension(file) +
                        " of file " + file + ": " + magic);
                fileHandler = new TestAllFiles.NullFileHandler();
            }
        }
        return fileHandler;
    }
}

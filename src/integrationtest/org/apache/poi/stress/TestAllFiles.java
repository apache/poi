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


import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.poi.OldFileFormatException;
import org.apache.poi.hssf.record.crypto.Biff8EncryptionKey;
import org.apache.poi.poifs.crypt.Decryptor;
import org.apache.tools.ant.DirectoryScanner;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.opentest4j.TestAbortedException;

/**
 *  This is an integration test which performs various actions on all stored test-files and tries
 *  to reveal problems which are introduced, but not covered (yet) by unit tests.
 *
 *  This test looks for any file under the test-data directory and tries to do some useful
 *  processing with it based on it's type.
 *
 *  The test is implemented as a junit {@link ParameterizedTest} test, which leads
 *  to one test-method call for each file (currently around 950 files are handled).
 *
 *  There is a a mapping of extension to implementations of the interface
 *  {@link FileHandler} which defines how the file is loaded and which actions are
 *  tried with the file.
 *
 *  The test can be expanded by adding more actions to the FileHandlers, this automatically
 *  applies the action to any such file in our test-data repository.
 *
 *  There is also a list of files that should actually fail.
 *
 *  Note: It is also a test-failure if a file that is expected to fail now actually works,
 *  i.e. if a bug was fixed in POI itself, the file should be removed from the expected-failures
 *  here as well! This is to ensure that files that should not work really do not work, e.g.
 *  that we do not remove expected sanity checks.
 */
class TestAllFiles {
    private static final File ROOT_DIR = new File("test-data");
    private static final boolean IGNORE_SCRATCHPAD = Boolean.getBoolean("scratchpad.ignore");

    public static final String[] SCAN_EXCLUDES = new String[] { "**/.svn/**", "lost+found", "**/.git/**" };

    private static final Map<String,String> FILE_PASSWORD;


    // map file extensions to the actual mappers
    public static final Map<String, FileHandler> HANDLERS = new HashMap<>();

    static {
        // Excel
        HANDLERS.put(".xls", new HSSFFileHandler());
        HANDLERS.put(".xlsx", new XSSFFileHandler());
        HANDLERS.put(".xlsm", new XSSFFileHandler());
        HANDLERS.put(".xltx", new XSSFFileHandler());
        HANDLERS.put(".xlsb", new XSSFBFileHandler());

        // Word
        HANDLERS.put(".doc", IGNORE_SCRATCHPAD ? new HPSFFileHandler() : new HWPFFileHandler());
        HANDLERS.put(".docx", new XWPFFileHandler());
        HANDLERS.put(".dotx", new XWPFFileHandler());
        HANDLERS.put(".docm", new XWPFFileHandler());

        // OpenXML4J files
        HANDLERS.put(".ooxml", new OPCFileHandler());
        HANDLERS.put(".zip", new OPCFileHandler());

        // Powerpoint
        HANDLERS.put(".ppt", IGNORE_SCRATCHPAD ? new HPSFFileHandler() : new HSLFFileHandler());
        HANDLERS.put(".pot", IGNORE_SCRATCHPAD ? new HPSFFileHandler() : new HSLFFileHandler());
        HANDLERS.put(".pptx", new XSLFFileHandler());
        HANDLERS.put(".pptm", new XSLFFileHandler());
        HANDLERS.put(".ppsm", new XSLFFileHandler());
        HANDLERS.put(".ppsx", new XSLFFileHandler());
        HANDLERS.put(".thmx", new XSLFFileHandler());
        HANDLERS.put(".potx", new XSLFFileHandler());

        // Outlook
        HANDLERS.put(".msg", IGNORE_SCRATCHPAD ? new HPSFFileHandler() : new HSMFFileHandler());

        // Publisher
        HANDLERS.put(".pub", IGNORE_SCRATCHPAD ? new HPSFFileHandler() : new HPBFFileHandler());

        // Visio - binary
        HANDLERS.put(".vsd", IGNORE_SCRATCHPAD ? new HPSFFileHandler() : new HDGFFileHandler());

        // Visio - ooxml
        HANDLERS.put(".vsdm", new XDGFFileHandler());
        HANDLERS.put(".vsdx", new XDGFFileHandler());
        HANDLERS.put(".vssm", new XDGFFileHandler());
        HANDLERS.put(".vssx", new XDGFFileHandler());
        HANDLERS.put(".vstm", new XDGFFileHandler());
        HANDLERS.put(".vstx", new XDGFFileHandler());

        // Visio - not handled yet
        HANDLERS.put(".vst", NullFileHandler.instance);
        HANDLERS.put(".vss", NullFileHandler.instance);

        // POIFS
        HANDLERS.put(".ole2", new POIFSFileHandler());

        // Microsoft Admin Template?
        HANDLERS.put(".adm", new HPSFFileHandler());

        // Microsoft TNEF
        HANDLERS.put(".dat", IGNORE_SCRATCHPAD ? new HPSFFileHandler() : new HMEFFileHandler());

        // TODO: are these readable by some of the formats?
        HANDLERS.put(".wri", NullFileHandler.instance);
        HANDLERS.put(".shw", NullFileHandler.instance);
        HANDLERS.put(".zvi", NullFileHandler.instance);
        HANDLERS.put(".mpp", NullFileHandler.instance);
        HANDLERS.put(".qwp", NullFileHandler.instance);
        HANDLERS.put(".wps", NullFileHandler.instance);
        HANDLERS.put(".bin", NullFileHandler.instance);
        HANDLERS.put(".xps", NullFileHandler.instance);
        HANDLERS.put(".sldprt", NullFileHandler.instance);
        HANDLERS.put(".mdb", NullFileHandler.instance);
        HANDLERS.put(".vml", NullFileHandler.instance);

        // ignore some file types, images, other formats, ...
        HANDLERS.put(".txt", NullFileHandler.instance);
        HANDLERS.put(".pdf", NullFileHandler.instance);
        HANDLERS.put(".rtf", NullFileHandler.instance);
        HANDLERS.put(".gif", NullFileHandler.instance);
        HANDLERS.put(".html", NullFileHandler.instance);
        HANDLERS.put(".png", NullFileHandler.instance);
        HANDLERS.put(".wmf", NullFileHandler.instance);
        HANDLERS.put(".emf", NullFileHandler.instance);
        HANDLERS.put(".dib", NullFileHandler.instance);
        HANDLERS.put(".svg", NullFileHandler.instance);
        HANDLERS.put(".pict", NullFileHandler.instance);
        HANDLERS.put(".jpg", NullFileHandler.instance);
        HANDLERS.put(".jpeg", NullFileHandler.instance);
        HANDLERS.put(".tif", NullFileHandler.instance);
        HANDLERS.put(".tiff", NullFileHandler.instance);
        HANDLERS.put(".wav", NullFileHandler.instance);
        HANDLERS.put(".xml", NullFileHandler.instance);
        HANDLERS.put(".csv", NullFileHandler.instance);
        HANDLERS.put(".ods", NullFileHandler.instance);
        HANDLERS.put(".ttf", NullFileHandler.instance);
        HANDLERS.put(".fntdata", NullFileHandler.instance);
        // VBA source files
        HANDLERS.put(".vba", NullFileHandler.instance);
        HANDLERS.put(".bas", NullFileHandler.instance);
        HANDLERS.put(".frm", NullFileHandler.instance);
        HANDLERS.put(".frx", NullFileHandler.instance); //binary
        HANDLERS.put(".cls", NullFileHandler.instance);

        // map some files without extension
        HANDLERS.put("spreadsheet/BigSSTRecord", NullFileHandler.instance);
        HANDLERS.put("spreadsheet/BigSSTRecord2", NullFileHandler.instance);
        HANDLERS.put("spreadsheet/BigSSTRecord2CR1", NullFileHandler.instance);
        HANDLERS.put("spreadsheet/BigSSTRecord2CR2", NullFileHandler.instance);
        HANDLERS.put("spreadsheet/BigSSTRecord2CR3", NullFileHandler.instance);
        HANDLERS.put("spreadsheet/BigSSTRecord2CR4", NullFileHandler.instance);
        HANDLERS.put("spreadsheet/BigSSTRecord2CR5", NullFileHandler.instance);
        HANDLERS.put("spreadsheet/BigSSTRecord2CR6", NullFileHandler.instance);
        HANDLERS.put("spreadsheet/BigSSTRecord2CR7", NullFileHandler.instance);
        HANDLERS.put("spreadsheet/BigSSTRecordCR", NullFileHandler.instance);
        HANDLERS.put("spreadsheet/test_properties1", NullFileHandler.instance);

        // keystore files
        HANDLERS.put(".pfx", NullFileHandler.instance);
        HANDLERS.put(".pem", NullFileHandler.instance);
        HANDLERS.put(".jks", NullFileHandler.instance);
        HANDLERS.put(".pkcs12", NullFileHandler.instance);

        Map<String,String> passmap = new HashMap<>();
        passmap.put("slideshow/Password_Protected-hello.ppt", "hello");
        passmap.put("slideshow/Password_Protected-56-hello.ppt", "hello");
        passmap.put("slideshow/Password_Protected-np-hello.ppt", "hello");
        passmap.put("slideshow/cryptoapi-proc2356.ppt", "crypto");
        passmap.put("spreadsheet/xor-encryption-abc.xls", "abc");
        passmap.put("spreadsheet/35897-type4.xls", "freedom");
        passmap.put("spreadsheet/58616.xlsx", Decryptor.DEFAULT_PASSWORD);
        passmap.put("spreadsheet/password.xls", "password");
        passmap.put("spreadsheet/protected_passtika.xlsx", "tika");
        passmap.put("document/bug53475-password-is-pass.docx", "pass");
        passmap.put("document/bug53475-password-is-solrcell.docx", "solrcell");
        passmap.put("document/password_password_cryptoapi.doc", "password");
        passmap.put("document/password_tika_binaryrc4.doc", "tika");
        passmap.put("poifs/protect.xlsx", Decryptor.DEFAULT_PASSWORD);
        passmap.put("poifs/extenxls_pwd123.xlsx", "pwd123");
        passmap.put("poifs/protected_agile.docx", Decryptor.DEFAULT_PASSWORD);
        passmap.put("poifs/60320-protected.xlsx", "Test001!!");
        passmap.put("poifs/protected_sha512.xlsx", "this is a test");

        FILE_PASSWORD = Collections.unmodifiableMap(passmap);
    }

    private static Set<String> unmodifiableHashSet(String... a) {
        return Collections.unmodifiableSet(hashSet(a));
    }
    private static Set<String> hashSet(String... a) {
        return new HashSet<>(Arrays.asList(a));
    }

    // Old Word Documents where we can at least extract some text
    private static final Set<String> OLD_FILES_HWPF = unmodifiableHashSet(
        "document/Bug49933.doc",
        "document/Bug51944.doc",
        "document/Word6.doc",
        "document/Word6_sections.doc",
        "document/Word6_sections2.doc",
        "document/Word95.doc",
        "document/word95err.doc",
        "document/Bug60936.doc",
        "document/Bug60942.doc",
        "document/Bug60942b.doc",
        "document/cn.orthodox.www_divenbog_APRIL_30-APRIL.DOC",
        "hpsf/TestMickey.doc",
        "document/52117.doc",
        "hpsf/TestInvertedClassID.doc",
        "hpsf/TestBug52117.doc"
    );

    private static final Set<String> EXPECTED_FAILURES = unmodifiableHashSet(
        // password protected files without known password
        "spreadsheet/51832.xls",
        "spreadsheet/64759.xlsx",
        "document/PasswordProtected.doc",

        // TODO: fails XMLExportTest, is this ok?
        "spreadsheet/CustomXMLMapping-singleattributenamespace.xlsx",
        "spreadsheet/55864.xlsx",
        "spreadsheet/57890.xlsx",
        "spreadsheet/xxe_in_schema.xlsx",

        // TODO: these fail now with some NPE/file read error because we now try to compute every value via Cell.toString()!
        "spreadsheet/44958.xls",
        "spreadsheet/44958_1.xls",
        "spreadsheet/testArraysAndTables.xls",

        // TODO: good to ignore?
        "spreadsheet/sample-beta.xlsx",
        "document/cpansearch.perl.org_src_tobyink_acme-rundoc-0.001_word-lib_hello_world.docm",

        // This is actually a spreadsheet!
        "hpsf/TestRobert_Flaherty.doc",

        // some files that are broken, eg Word 95, ...
        "spreadsheet/43493.xls",
        "spreadsheet/46904.xls",
        "document/Bug50955.doc",
        "document/57843.doc",
        "slideshow/PPT95.ppt",
        "slideshow/pp40only.ppt",
        "slideshow/Divino_Revelado.pptx",
        "openxml4j/OPCCompliance_CoreProperties_DCTermsNamespaceLimitedUseFAIL.docx",
        "openxml4j/OPCCompliance_CoreProperties_DoNotUseCompatibilityMarkupFAIL.docx",
        "openxml4j/OPCCompliance_CoreProperties_LimitedXSITypeAttribute_NotPresentFAIL.docx",
        "openxml4j/OPCCompliance_CoreProperties_LimitedXSITypeAttribute_PresentWithUnauthorizedValueFAIL.docx",
        "openxml4j/OPCCompliance_CoreProperties_OnlyOneCorePropertiesPartFAIL.docx",
        "openxml4j/OPCCompliance_CoreProperties_UnauthorizedXMLLangAttributeFAIL.docx",
        "openxml4j/OPCCompliance_DerivedPartNameFAIL.docx",
        "openxml4j/invalid.xlsx",
        "openxml4j/62592.thmx",
        "spreadsheet/54764-2.xlsx",   // see TestXSSFBugs.bug54764()
        "spreadsheet/54764.xlsx",     // see TestXSSFBugs.bug54764()
        "poifs/unknown_properties.msg", // POIFS properties corrupted
        "poifs/only-zero-byte-streams.ole2", // No actual contents
        "spreadsheet/poc-xmlbomb.xlsx",  // contains xml-entity-expansion
        "spreadsheet/poc-xmlbomb-empty.xlsx",  // contains xml-entity-expansion
        "spreadsheet/poc-shared-strings.xlsx",  // contains shared-string-entity-expansion
        "document/61612a.docx",
        "document/word2.doc",
        "spreadsheet/xlsx-corrupted.xlsx",
        "integration/stress025.docx",

        // old Excel files, which we only support simple text extraction of
        "spreadsheet/testEXCEL_2.xls",
        "spreadsheet/testEXCEL_3.xls",
        "spreadsheet/testEXCEL_4.xls",
        "spreadsheet/testEXCEL_5.xls",
        "spreadsheet/testEXCEL_95.xls",
        "spreadsheet/59074.xls",
        "spreadsheet/60284.xls",
        "spreadsheet/64130.xls",

        // OOXML Strict is not yet supported, see bug #57699
        "spreadsheet/SampleSS.strict.xlsx",
        "spreadsheet/SimpleStrict.xlsx",
        "spreadsheet/sample.strict.xlsx",
        "spreadsheet/57914.xlsx",

        // files with XML entities
        "openxml4j/ContentTypeHasEntities.ooxml",

        // non-TNEF files
        "ddf/Container.dat",
        "ddf/47143.dat",

        // sheet cloning errors
        "spreadsheet/56450.xls",
        // "spreadsheet/OddStyleRecord.xls",

        // msg files with non-standard encodings
        "hsmf/ASCII_CP1251_LCID1049.msg",
        "hsmf/ASCII_UTF-8_CP1252_LCID1031.msg",
        "hsmf/ASCII_UTF-8_CP1252_LCID1031_HTML.msg",
        "hsmf/HTMLBodyBinary_CP1251.msg",
        "hsmf/HTMLBodyBinary_UTF-8.msg"
    );

    private static final Set<String> IGNORED = unmodifiableHashSet(
        // OPC handler works / XSSF handler fails
        "spreadsheet/57181.xlsm",
        "spreadsheet/61300.xls"//intentionally fuzzed -- used to cause infinite loop
    );

    public static Stream<Arguments> files() {
        DirectoryScanner scanner = new DirectoryScanner();
        scanner.setBasedir(ROOT_DIR);
        scanner.setExcludes(SCAN_EXCLUDES);

        scanner.scan();

        System.out.println("Handling " + scanner.getIncludedFiles().length + " files");

        List<Arguments> files = new ArrayList<>();
        for(String file : scanner.getIncludedFiles()) {
            file = file.replace('\\', '/'); // ... failures/handlers lookup doesn't work on windows otherwise
            if (IGNORED.contains(file)) {
                System.out.println("Ignoring " + file);
                continue;
            }
            FileHandler handler = HANDLERS.get(getExtension(file));
            files.add(Arguments.of( file, handler ));

            // for some file-types also run OPCFileHandler
            if(handler instanceof XSSFFileHandler ||
                handler instanceof XWPFFileHandler ||
                handler instanceof XSLFFileHandler ||
                handler instanceof XDGFFileHandler) {
                files.add(Arguments.of( file, new OPCFileHandler() ));
            }

            if (handler instanceof HSSFFileHandler ||
                handler instanceof HSLFFileHandler ||
                handler instanceof HWPFFileHandler ||
                handler instanceof HDGFFileHandler) {
                files.add(Arguments.of( file, new HPSFFileHandler() ));
            }
        }

        return files.stream();
    }

    // the display name annotation is ignored by ants junitlauncher listeners :(
    // ... even when using a custom display name generator
    @ParameterizedTest(name = "#{index} {0}" )
    @MethodSource("files")
    void testAllFiles(String file, FileHandler handler) throws Exception {
        assertNotNull(handler, "Did not find a handler for file " + file);

        // this also removes the password for non encrypted files
        String pass = TestAllFiles.FILE_PASSWORD.get(file);
        Biff8EncryptionKey.setCurrentUserPassword(pass);


        System.out.println("Reading " + file + " with " + handler.getClass().getSimpleName());
        assertNotNull( handler, "Unknown file extension for file: " + file + ": " + getExtension(file) );
        File inputFile = new File(ROOT_DIR, file);

        // special cases where docx-handling breaks, but OPCPackage handling works
        boolean ignoredOPC = (file.endsWith(".docx") || file.endsWith(".xlsx") ||
                    file.endsWith(".xlsb") || file.endsWith(".pptx")) &&
                handler instanceof OPCFileHandler;
        boolean ignoreHPSF = (handler instanceof HPSFFileHandler);

        try {
            try (InputStream stream = new BufferedInputStream(new FileInputStream(inputFile), 64 * 1024)) {
                handler.handleFile(stream, file);
                assertFalse( OLD_FILES_HWPF.contains(file) && !ignoreHPSF, "Expected to fail for file " + file + " and handler " + handler + ", but did not fail!" );
            }

            handler.handleExtracting(inputFile);

            assertFalse( EXPECTED_FAILURES.contains(file) && !ignoredOPC && !ignoreHPSF, "Expected to fail for file " + file + " and handler " + handler + ", but did not fail!" );
        } catch (OldFileFormatException e) {
            // for old word files we should still support extracting text
            if(OLD_FILES_HWPF.contains(file)) {
                handler.handleExtracting(inputFile);
            } else {
                // check if we expect failure for this file
                if(!EXPECTED_FAILURES.contains(file) && !AbstractFileHandler.EXPECTED_EXTRACTOR_FAILURES.contains(file)) {
                    System.out.println("Failed: " + file);
                    throw new Exception("While handling " + file, e);
                }
            }
        } catch (TestAbortedException e) {
            // file handler ignored this file
        } catch (Exception e) {
            // check if we expect failure for this file
            if(!EXPECTED_FAILURES.contains(file) && !AbstractFileHandler.EXPECTED_EXTRACTOR_FAILURES.contains(file)) {
                System.out.println("Failed: " + file);
                throw new Exception("While handling " + file, e);
            }
        }

        try {
            // let some file handlers do additional stuff
            handler.handleAdditional(inputFile);
        } catch (TestAbortedException e) {
            // file handler ignored this file
        } catch (Exception e) {
            if(!EXPECTED_FAILURES.contains(file) && !AbstractFileHandler.EXPECTED_EXTRACTOR_FAILURES.contains(file)) {
                System.out.println("Failed: " + file);
                throw new Exception("While handling " + file, e);
            }
        }
    }

    public static String getExtension(String file) {
        int pos = file.lastIndexOf('.');
        if(pos == -1 || pos == file.length()-1) {
            return file;
        }

        return file.substring(pos).toLowerCase(Locale.ROOT);
    }

    public static class NullFileHandler implements FileHandler {
        public static final FileHandler instance = new NullFileHandler();

        @Override
        public void handleFile(InputStream stream, String path) {
        }

        @Override
        public void handleExtracting(File file) {
        }

        @Override
        public void handleAdditional(File file) {
        }
    }
}

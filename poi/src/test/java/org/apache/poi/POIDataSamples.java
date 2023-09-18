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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;

import org.apache.commons.io.output.UnsynchronizedByteArrayOutputStream;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.util.IOUtils;

/**
 * Centralises logic for finding/opening sample files
 */
public final class POIDataSamples {

    /**
     * Name of the system property that defined path to the test data.
     */
    public static final String TEST_PROPERTY = "POI.testdata.path";

    private static POIDataSamples _instSlideshow;
    private static POIDataSamples _instSpreadsheet;
    private static POIDataSamples _instDocument;
    private static POIDataSamples _instDiagram;
    private static POIDataSamples _instOpenxml4j;
    private static POIDataSamples _instPOIFS;
    private static POIDataSamples _instDDF;
    private static POIDataSamples _instHMEF;
    private static POIDataSamples _instHPSF;
    private static POIDataSamples _instHPBF;
    private static POIDataSamples _instHSMF;
    private static POIDataSamples _instXmlDSign;

    private File _resolvedDataDir;
    /** {@code true} if standard system property is not set,
     * but the data is available on the test runtime classpath */
    private boolean _sampleDataIsAvaliableOnClassPath;
    private final String _moduleDir;

    /**
     *
     * @param moduleDir   the name of the directory containing the test files
     */
    private POIDataSamples(String moduleDir){
        _moduleDir = moduleDir;
        initialise();
    }

    public static POIDataSamples getSpreadSheetInstance(){
        if(_instSpreadsheet == null) _instSpreadsheet = new POIDataSamples("spreadsheet");
        return _instSpreadsheet;
    }

    public static POIDataSamples getDocumentInstance(){
        if(_instDocument == null) _instDocument = new POIDataSamples("document");
        return _instDocument;
    }

    public static POIDataSamples getSlideShowInstance(){
        if(_instSlideshow == null) _instSlideshow = new POIDataSamples("slideshow");
        return _instSlideshow;
    }

    public static POIDataSamples getDiagramInstance(){
        if(_instOpenxml4j == null) _instOpenxml4j = new POIDataSamples("diagram");
        return _instOpenxml4j;
    }

    public static POIDataSamples getOpenXML4JInstance(){
        if(_instDiagram == null) _instDiagram = new POIDataSamples("openxml4j");
        return _instDiagram;
    }

    public static POIDataSamples getPOIFSInstance(){
        if(_instPOIFS == null) _instPOIFS = new POIDataSamples("poifs");
        return _instPOIFS;
    }

    public static POIDataSamples getDDFInstance(){
        if(_instDDF == null) _instDDF = new POIDataSamples("ddf");
        return _instDDF;
    }

    public static POIDataSamples getHPSFInstance(){
        if(_instHPSF == null) _instHPSF = new POIDataSamples("hpsf");
        return _instHPSF;
    }

    public static POIDataSamples getPublisherInstance(){
        if(_instHPBF == null) _instHPBF = new POIDataSamples("publisher");
        return _instHPBF;
    }

    public static POIDataSamples getHMEFInstance(){
        if(_instHMEF == null) _instHMEF = new POIDataSamples("hmef");
        return _instHMEF;
    }

    public static POIDataSamples getHSMFInstance(){
        if(_instHSMF == null) _instHSMF = new POIDataSamples("hsmf");
        return _instHSMF;
    }

    public static POIDataSamples getXmlDSignInstance() {
        if(_instXmlDSign == null) _instXmlDSign = new POIDataSamples("xmldsign");
        return _instXmlDSign;
    }

    /**
     * Opens a sample file from the test data directory
     *
     * @param  sampleFileName the file to open
     * @return an open {@code InputStream} for the specified sample file
     */
    public InputStream openResourceAsStream(String sampleFileName) {

        if (_sampleDataIsAvaliableOnClassPath) {
            InputStream result = sampleFileName == null ? null :
                    openClasspathResource(sampleFileName);
            if(result == null) {
                throw new RuntimeException("specified test sample file '" + sampleFileName
                        + "' not found on the classpath");
            }
            // wrap to avoid temp warning method about auto-closing input stream
            return new NonSeekableInputStream(result);
        }
        if (_resolvedDataDir == null) {
            throw new RuntimeException("Must set system property '"
                    + TEST_PROPERTY
                    + "' properly before running tests");
        }

        File f = getFile(sampleFileName);
        try {
            return Files.newInputStream(f.toPath());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     *
     * @param sampleFileName    the name of the test file
     * @return Verifies that the file with the given name exists in the test-data directory
     * @throws IllegalStateException if the file was not found
     */
    public File getFile(String sampleFileName) {
        File f = new File(_resolvedDataDir, sampleFileName);
        if (!f.exists()) {
            throw new IllegalStateException("Sample file '" + sampleFileName
                    + "' not found in data dir '" + _resolvedDataDir.getAbsolutePath() + "'");
        }
        try {
            if(sampleFileName.length() > 0) {
               String fn = sampleFileName;
               if(fn.indexOf('/') > 0) {
                  fn = fn.substring(fn.indexOf('/')+1);
               }
               if(!fn.equals(f.getCanonicalFile().getName())){
                   throw new IllegalStateException("File name is case-sensitive: requested '" + fn
                        + "' but actual file is '" + f.getCanonicalFile().getName() + "'");
               }
            }
        } catch (IOException e){
            throw new IllegalStateException(e);
        }
        return f;
    }

    private void initialise() {
        String dataDirName = System.getProperty(TEST_PROPERTY);
        if (dataDirName == null) {
            // check to see if we can just get the resources from the classpath
            InputStream is = openClasspathResource("");
            if (is != null) {
                try {
                    is.close(); // be nice
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                _sampleDataIsAvaliableOnClassPath = true;
                return;
            }

            if (new File("test-data").exists()) {
                dataDirName = "test-data";
            } else if (new File("../test-data").exists()) {
               dataDirName = "../test-data";
            } else {
               throw new RuntimeException("Must set system property '" +
                       TEST_PROPERTY + "' before running tests");
            }
        }
        File dataDir = new File(dataDirName, _moduleDir);
        if (!dataDir.exists()) {
            throw new RuntimeException("Data dir '" + dataDir + "' does not exist");
        }
        // convert to canonical file, to make any subsequent error messages
        // clearer.
        try {
            _resolvedDataDir = dataDir.getCanonicalFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Opens a test sample file from the 'data' sub-package of this class's package.
     *
     * @param  sampleFileName the file to open
     * @return {@code null} if the sample file is not deployed on the classpath.
     */
    private InputStream openClasspathResource(String sampleFileName) {
        return getClass().getResourceAsStream("/" + _moduleDir + "/" + sampleFileName);
    }

    private static final class NonSeekableInputStream extends InputStream {

        private final InputStream _is;

        public NonSeekableInputStream(InputStream is) {
            _is = is;
        }

        @Override
        public int read() throws IOException {
            return _is.read();
        }
        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            return _is.read(b, off, len);
        }
        @Override
        public boolean markSupported() {
            return false;
        }
        @Override
        public void close() throws IOException {
            _is.close();
        }
    }

    /**
     * @param  fileName the file to open
     * @return byte array of sample file content from file found in standard test-data directory
     */
    public byte[] readFile(String fileName) {
        try (InputStream fis = openResourceAsStream(fileName);
             UnsynchronizedByteArrayOutputStream bos = UnsynchronizedByteArrayOutputStream.builder().get()) {
            IOUtils.copy(fis, bos);
            return bos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static POIFSFileSystem writeOutAndReadBack(POIFSFileSystem original) throws IOException {
        try (UnsynchronizedByteArrayOutputStream baos = UnsynchronizedByteArrayOutputStream.builder().get()) {
            original.writeFilesystem(baos);
            return new POIFSFileSystem(baos.toInputStream());
        }
    }
}

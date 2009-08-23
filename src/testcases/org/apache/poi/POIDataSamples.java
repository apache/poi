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

import java.io.*;

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
    private static POIDataSamples _instHPSF;
    private static POIDataSamples _instHPBF;
    private static POIDataSamples _instHSMF;

    private File _resolvedDataDir;
    /** <code>true</code> if standard system propery is not set,
     * but the data is available on the test runtime classpath */
    private boolean _sampleDataIsAvaliableOnClassPath;
    private String _moduleDir;

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

    public static POIDataSamples getHSMFInstance(){
        if(_instHSMF == null) _instHSMF = new POIDataSamples("hsmf");
        return _instHSMF;
    }
    /**
     * Opens a sample file from the test data directory
     *
     * @param  sampleFileName the file to open
     * @return an open <tt>InputStream</tt> for the specified sample file
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
            return new FileInputStream(f);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     *
     * @param sampleFileName    the name of the test file
     * @return
     * @throws RuntimeException if the file was not found
     */
    public File getFile(String sampleFileName) {
        File f = new File(_resolvedDataDir, sampleFileName);
        if (!f.exists()) {
            throw new RuntimeException("Sample file '" + sampleFileName
                    + "' not found in data dir '" + _resolvedDataDir.getAbsolutePath() + "'");
        }
        try {
            if(sampleFileName.length() > 0 && !sampleFileName.equals(f.getCanonicalFile().getName())){
                throw new RuntimeException("File name is case-sensitive: requested '" + sampleFileName
                        + "' but actual file is '" + f.getCanonicalFile().getName() + "'");
            }
        } catch (IOException e){
            throw new RuntimeException(e);
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

            throw new RuntimeException("Must set system property '" +
                    TEST_PROPERTY + "' before running tests");
        }
        File dataDir = new File(dataDirName, _moduleDir);
        if (!dataDir.exists()) {
            throw new RuntimeException("Data dir '" + _moduleDir + " does not exist");
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
     * @return <code>null</code> if the sample file is not deployed on the classpath.
     */
    private InputStream openClasspathResource(String sampleFileName) {
        return getClass().getResourceAsStream("/" + _moduleDir + "/" + sampleFileName);
    }

    private static final class NonSeekableInputStream extends InputStream {

        private final InputStream _is;

        public NonSeekableInputStream(InputStream is) {
            _is = is;
        }

        public int read() throws IOException {
            return _is.read();
        }
        public int read(byte[] b, int off, int len) throws IOException {
            return _is.read(b, off, len);
        }
        public boolean markSupported() {
            return false;
        }
        public void close() throws IOException {
            _is.close();
        }
    }

    /**
     * @param  fileName the file to open
     * @return byte array of sample file content from file found in standard hssf test data dir
     */
    public byte[] readFile(String fileName) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        try {
            InputStream fis = openResourceAsStream(fileName);

            byte[] buf = new byte[512];
            while (true) {
                int bytesRead = fis.read(buf);
                if (bytesRead < 1) {
                    break;
                }
                bos.write(buf, 0, bytesRead);
            }
            fis.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return bos.toByteArray();
    }

}

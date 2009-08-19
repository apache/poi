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
 *
 */
public abstract class POIDataSamples {

    private File _resolvedDataDir;
    /** <code>true</code> if standard system propery is not set,
     * but the data is available on the test runtime classpath */
    private boolean _sampleDataIsAvaliableOnClassPath;
    private String _testDataDir;

    /**
     *
     * @param dir   the name of the system property that defines  path to the test files
     * @param classPathTestFile the name of the test file to check if resources are available from the classpath
     */
    public POIDataSamples(String dir, String classPathTestFile){
        _testDataDir = dir;
        initialise(classPathTestFile);
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
                    + _testDataDir
                    + "' properly before running tests");
        }

        File f = new File(_resolvedDataDir, sampleFileName);
        if (!f.exists()) {
            throw new RuntimeException("Sample file '" + sampleFileName
                    + "' not found in data dir '" + _resolvedDataDir.getAbsolutePath() + "'");
        }
        try {
            if(!sampleFileName.equals(f.getCanonicalFile().getName())){
                throw new RuntimeException("File name is case-sensitive: requested '" + sampleFileName
                        + "' but actual file is '" + f.getCanonicalFile().getName() + "'");
            }
        } catch (IOException e){
            throw new RuntimeException(e);
        }

        try {
            return new FileInputStream(f);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     *
     * @param classPathTest test file to check if the resources are avaiable from the classpath
     */
    private void initialise(String classPathTest) {
        String dataDirName = System.getProperty(_testDataDir);
        if (dataDirName == null) {
            // check to see if we can just get the resources from the classpath
            InputStream is = openClasspathResource(classPathTest);
            if (is != null) {
                try {
                    is.close(); // be nice
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                _sampleDataIsAvaliableOnClassPath = true;
                return;
            }

            throw new RuntimeException("Must set system property '"
                    + _testDataDir + "' before running tests");
        }
        File dataDir = new File(dataDirName);
        if (!dataDir.exists()) {
            throw new RuntimeException("Data dir '" + dataDirName
                    + "' specified by system property '" + _testDataDir
                    + "' does not exist");
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
        return getClass().getResourceAsStream("data/" + sampleFileName);
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

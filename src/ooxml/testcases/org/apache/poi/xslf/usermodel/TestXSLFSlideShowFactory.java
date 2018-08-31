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

package org.apache.poi.xslf.usermodel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;

import org.apache.poi.POIDataSamples;
import org.apache.poi.poifs.crypt.EncryptionInfo;
import org.apache.poi.poifs.crypt.EncryptionMode;
import org.apache.poi.poifs.crypt.Encryptor;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.sl.usermodel.BaseTestSlideShowFactory;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.TempFile;
import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

public final class TestXSLFSlideShowFactory extends BaseTestSlideShowFactory {
    private static POIDataSamples _slTests = POIDataSamples.getSlideShowInstance();
    private static final String filename = "SampleShow.pptx";
    private static final String password = "opensesame";
    private static final String removeExpectedExceptionMsg =
            "This functionality this unit test is trying to test is now passing. " +
            "The unit test needs to be updated by deleting the expected exception code. Status and close any related bugs.";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testFactoryFromFile() throws Exception {
        // Remove thrown.* when bug 58779 is resolved
        // In the mean time, this function will modify SampleShow.pptx on disk.
        thrown.expect(AssertionError.class);
        // thrown.expectCause(Matcher<ArrayComparisonFailure>);
        thrown.expectMessage("SampleShow.pptx sample file was modified as a result of closing the slideshow");
        thrown.reportMissingExceptionWithMessage("Bug 58779: " + removeExpectedExceptionMsg);
    
        testFactoryFromFile(filename);
    }

    @Test
    public void testFactoryFromStream() throws Exception {
        testFactoryFromStream(filename);
    }

    @Test
    public void testFactoryFromNative() throws Exception {
        // Remove thrown.* when unit test for XSLF SlideShowFactory.create(OPCPackage) is implemented
        thrown.expect(UnsupportedOperationException.class);
        thrown.expectMessage("Test not implemented");
        thrown.reportMissingExceptionWithMessage(removeExpectedExceptionMsg);

        testFactoryFromNative(filename);
    }

    @Test
    public void testFactoryFromProtectedFile() throws Exception {
        File pFile = createProtected();
        testFactoryFromProtectedFile(pFile.getAbsolutePath(), password);
    }

    @Test
    public void testFactoryFromProtectedStream() throws Exception {
        File pFile = createProtected();
        testFactoryFromProtectedStream(pFile.getAbsolutePath(), password);
    }

    @Test
    public void testFactoryFromProtectedNative() throws Exception {
        File pFile = createProtected();
        testFactoryFromProtectedNative(pFile.getAbsolutePath(), password);
    }

    private static File createProtected() throws IOException, GeneralSecurityException {
        try (POIFSFileSystem fs = new POIFSFileSystem()) {
            EncryptionInfo info = new EncryptionInfo(EncryptionMode.agile);
            Encryptor enc = info.getEncryptor();
            enc.confirmPassword(password);
            try (InputStream fis = _slTests.openResourceAsStream(filename);
                 OutputStream os = enc.getDataStream(fs)) {
                IOUtils.copy(fis, os);
            }

            File tf = TempFile.createTempFile("test-xslf-slidefactory", ".pptx");
            try (FileOutputStream fos = new FileOutputStream(tf)) {
                fs.writeFilesystem(fos);
            }
            return tf;
        }
    }
}

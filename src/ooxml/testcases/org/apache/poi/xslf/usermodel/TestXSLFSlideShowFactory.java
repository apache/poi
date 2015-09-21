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
import org.apache.poi.poifs.filesystem.NPOIFSFileSystem;
import org.apache.poi.sl.usermodel.BaseTestSlideShowFactory;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.TempFile;
import org.junit.Test;

public final class TestXSLFSlideShowFactory extends BaseTestSlideShowFactory {
    private static POIDataSamples _slTests = POIDataSamples.getSlideShowInstance();
    
    @Test
    public void testFactory() throws Exception {
        File pFile = createProtected("SampleShow.pptx", "foobaa");
        testFactory("SampleShow.pptx", pFile.getAbsolutePath(), "foobaa");
    }
    
    private static File createProtected(String basefile, String password)
    throws IOException, GeneralSecurityException {
        NPOIFSFileSystem fs = new NPOIFSFileSystem();
        EncryptionInfo info = new EncryptionInfo(EncryptionMode.agile);
        Encryptor enc = info.getEncryptor();
        enc.confirmPassword(password);
        InputStream fis = _slTests.openResourceAsStream("SampleShow.pptx");
        OutputStream os = enc.getDataStream(fs);
        IOUtils.copy(fis, os);
        os.close();
        fis.close();
        
        File tf = TempFile.createTempFile("test-xslf-slidefactory", "pptx");
        FileOutputStream fos = new FileOutputStream(tf);
        fs.writeFilesystem(fos);
        fos.close();
        fs.close();

        return tf;
    }
}

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
package org.apache.poi.poifs.crypt;

import org.apache.poi.poifs.filesystem.DocumentInputStream;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import java.io.IOException;

/**
 *  @author Maxim Valyanskiy
 */
public class EncryptionInfo {
    private final int versionMajor;
    private final int versionMinor;
    private final int encryptionFlags;

    private final EncryptionHeader header;
    private final EncryptionVerifier verifier;

    public EncryptionInfo(POIFSFileSystem fs) throws IOException {
        DocumentInputStream dis = fs.createDocumentInputStream("EncryptionInfo");

        versionMajor = dis.readShort();
        versionMinor = dis.readShort();
        encryptionFlags = dis.readInt();

        int hSize = dis.readInt();

        header = new EncryptionHeader(dis);

        if (header.getAlgorithm()==EncryptionHeader.ALGORITHM_RC4) {
            verifier = new EncryptionVerifier(dis, 20);
        } else {
            verifier = new EncryptionVerifier(dis, 32);            
        }
    }

    public int getVersionMajor() {
        return versionMajor;
    }

    public int getVersionMinor() {
        return versionMinor;
    }

    public int getEncryptionFlags() {
        return encryptionFlags;
    }

    public EncryptionHeader getHeader() {
        return header;
    }

    public EncryptionVerifier getVerifier() {
        return verifier;
    }
}

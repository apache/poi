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

package org.apache.poi.poifs.filesystem;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.poifs.crypt.Decryptor;
import org.apache.poi.poifs.crypt.EncryptionInfo;
import org.apache.poi.util.Internal;
import org.apache.poi.util.Removal;

/**
 * A small base class for the various factories, e.g. WorkbookFactory,
 * SlideShowFactory to combine common code here.
 */
@Internal
public final class DocumentFactoryHelper {
    private DocumentFactoryHelper() {
    }

    /**
     * Wrap the OLE2 data in the {@link POIFSFileSystem} into a decrypted stream by using
     * the given password.
     *
     * @param fs The OLE2 stream for the document
     * @param password The password, null if the default password should be used
     * @return A stream for reading the decrypted data
     * @throws IOException If an error occurs while decrypting or if the password does not match
     */
    public static InputStream getDecryptedStream(final POIFSFileSystem fs, String password)
    throws IOException {
        // wrap the stream in a FilterInputStream to close the POIFSFileSystem
        // as well when the resulting OPCPackage is closed
        return new FilterInputStream(getDecryptedStream(fs.getRoot(), password)) {
            @Override
            public void close() throws IOException {
                fs.close();
                super.close();
            }
        };
    }

    /**
     * Wrap the OLE2 data of the DirectoryNode into a decrypted stream by using
     * the given password.
     *
     * @param root The OLE2 directory node for the document
     * @param password The password, null if the default password should be used
     * @return A stream for reading the decrypted data
     * @throws IOException If an error occurs while decrypting or if the password does not match
     */
    public static InputStream getDecryptedStream(final DirectoryNode root, String password)
            throws IOException {
        EncryptionInfo info = new EncryptionInfo(root);
        Decryptor d = Decryptor.getInstance(info);

        try {
            boolean passwordCorrect = false;
            if (password != null && d.verifyPassword(password)) {
                passwordCorrect = true;
            }
            if (!passwordCorrect && d.verifyPassword(Decryptor.DEFAULT_PASSWORD)) {
                passwordCorrect = true;
            }

            if (passwordCorrect) {
                return d.getDataStream(root);
            } else if (password != null) {
                throw new EncryptedDocumentException("Password incorrect");
            } else {
                throw new EncryptedDocumentException("The supplied spreadsheet is protected, but no password was supplied");
            }
        } catch (GeneralSecurityException e) {
            throw new IOException(e);
        }
    }

    /**
     * Checks that the supplied InputStream (which MUST
     *  support mark and reset) has a OOXML (zip) header at the start of it.<p>
     *  
     * If unsure if your InputStream does support mark / reset,
     *  use {@link FileMagic#prepareToCheckMagic(InputStream)} to wrap it and make
     *  sure to always use that, and not the original!
     *  
     * @param inp An InputStream which supports either mark/reset
     *
     * @deprecated in 3.17-beta2, use {@link FileMagic#valueOf(InputStream)} == FileMagic.OOXML instead
     */
    @Deprecated
    @Removal(version="4.0")
    public static boolean hasOOXMLHeader(InputStream inp) throws IOException {
        return FileMagic.valueOf(inp) == FileMagic.OOXML;
    }
}

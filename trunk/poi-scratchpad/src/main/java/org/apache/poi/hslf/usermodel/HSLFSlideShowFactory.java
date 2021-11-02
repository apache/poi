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

package org.apache.poi.hslf.usermodel;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.hssf.record.crypto.Biff8EncryptionKey;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.FileMagic;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.sl.usermodel.SlideShowFactory;
import org.apache.poi.sl.usermodel.SlideShowProvider;
import org.apache.poi.util.Internal;

/**
 * Helper class which is instantiated by reflection from
 * {@link SlideShowFactory#create(java.io.File)} and similar
 */
@SuppressWarnings("unused")
@Internal
public class HSLFSlideShowFactory implements SlideShowProvider<HSLFShape,HSLFTextParagraph> {

    @Override
    public boolean accepts(FileMagic fm) {
        return FileMagic.OLE2 == fm;
    }

    /**
     * Create a new empty SlideShow
     *
     * @return The created SlideShow
     */
    @Override
    public HSLFSlideShow create() {
        return new HSLFSlideShow();
    }

    /**
     * Creates a HSLFSlideShow from the given {@link POIFSFileSystem}<p>
     * Note that in order to properly release resources the
     * SlideShow should be closed after use.
     */
    public static HSLFSlideShow createSlideShow(final POIFSFileSystem fs) throws IOException {
        return new HSLFSlideShow(fs);
    }

    /**
     * Creates a HSLFSlideShow from the given DirectoryNode<p>
     * Note that in order to properly release resources the
     * SlideShow should be closed after use.
     */
    @Override
    @SuppressWarnings("java:S2093")
    public HSLFSlideShow create(final DirectoryNode root, String password) throws IOException {
        boolean passwordSet = false;
        if (password != null) {
            Biff8EncryptionKey.setCurrentUserPassword(password);
            passwordSet = true;
        }
        try {
            return new HSLFSlideShow(root);
        } finally {
            if (passwordSet) {
                Biff8EncryptionKey.setCurrentUserPassword(null);
            }
        }
    }

    @Override
    public HSLFSlideShow create(InputStream inp) throws IOException {
        return create(inp, null);
    }

    @Override
    public HSLFSlideShow create(InputStream inp, String password) throws IOException {
        POIFSFileSystem fs = new POIFSFileSystem(inp);
        return create(fs.getRoot(), password);
    }

    @SuppressWarnings("java:S2093")
    @Override
    public HSLFSlideShow create(File file, String password, boolean readOnly) throws IOException {
        boolean passwordSet = false;
        if (password != null) {
            Biff8EncryptionKey.setCurrentUserPassword(password);
            passwordSet = true;
        }
        try {
            return new HSLFSlideShow(new POIFSFileSystem(file, readOnly));
        } finally {
            if (passwordSet) {
                Biff8EncryptionKey.setCurrentUserPassword(null);
            }
        }
    }
}

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

package org.apache.poi.sl.usermodel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.FileMagic;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.sl.usermodel.ObjectMetaData.Application;
import org.apache.poi.util.IOUtils;

/**
 * An shape which references an embedded OLE object
 *
 * @since POI 4.0.0
 */
public interface ObjectShape<
    S extends Shape<S,P>,
    P extends TextParagraph<S,P,? extends TextRun>
> extends Shape<S,P>, PlaceableShape<S,P>  {

    /**
     * Returns the picture data for this picture.
     *
     * @return the picture data for this picture.
     */
    PictureData getPictureData();

    /**
     * Returns the ProgID that stores the OLE Programmatic Identifier.
     * A ProgID is a string that uniquely identifies a given object, for example,
     * "Word.Document.8" or "Excel.Sheet.8".
     *
     * @return the ProgID
     */
    String getProgId();
    
    /**
     * Returns the full name of the embedded object,
     *  e.g. "Microsoft Word Document" or "Microsoft Office Excel Worksheet".
     *
     * @return the full name of the embedded object
     */
    String getFullName();
    
    /**
     * Updates the ole data. If there wasn't an object registered before, a new
     * ole embedding is registered in the parent slideshow.<p>
     * 
     * For HSLF this needs to be a {@link POIFSFileSystem} stream.
     *
     * @param application a preset application enum
     * @param metaData or a custom metaData object, can be {@code null} if the application has been set
     *
     * @return an {@link OutputStream} which receives the new data, the data will be persisted on {@code close()}
     *
     * @throws IOException if the linked object data couldn't be found or a new object data couldn't be initialized
     */
    OutputStream updateObjectData(ObjectMetaData.Application application, ObjectMetaData metaData) throws IOException;

    /**
     * Reads the ole data as stream - the application specific stream is served
     * The {@link #readObjectDataRaw() raw data} serves the outer/wrapped object, which is usually a
     * {@link POIFSFileSystem} stream, whereas this method return the unwrapped entry 
     *
     * @return an {@link InputStream} which serves the object data
     * 
     * @throws IOException if the linked object data couldn't be found
     */
    default InputStream readObjectData() throws IOException {
        final String progId = getProgId();
        if (progId == null) {
            throw new IllegalStateException(
                "Ole object hasn't been initialized or provided in the source xml. " +
                "use updateObjectData() first or check the corresponding slideXXX.xml");
        }

        final Application app = Application.lookup(progId);

        final ByteArrayOutputStream bos = new ByteArrayOutputStream(50000);
        try (final InputStream is = FileMagic.prepareToCheckMagic(readObjectDataRaw())) {
            final FileMagic fm = FileMagic.valueOf(is);
            if (fm == FileMagic.OLE2) {
                try (final POIFSFileSystem poifs = new POIFSFileSystem(is)) {
                    String[] names = {
                        (app == null) ? null : app.getMetaData().getOleEntry(),
                        // fallback to the usual suspects
                        "Package",
                        "Contents",
                        "CONTENTS",
                        "CONTENTSV30",
                    };
                    final DirectoryNode root = poifs.getRoot();
                    String entryName = null;
                    for (String n : names) {
                        if (root.hasEntry(n)) {
                            entryName = n;
                            break;
                        }
                    }
                    if (entryName == null) {
                        poifs.writeFilesystem(bos);
                    } else {
                        try (final InputStream is2 = poifs.createDocumentInputStream(entryName)) {
                            IOUtils.copy(is2, bos);
                        }
                    }
                }
            } else {
                IOUtils.copy(is, bos);
            }
        }

        return new ByteArrayInputStream(bos.toByteArray());
    }
    
    /**
     * Convenience method to return the raw data as {@code InputStream}
     *
     * @return the raw data stream
     *
     * @throws IOException if the data couldn't be retrieved
     */
    default InputStream readObjectDataRaw() throws IOException {
        return getObjectData().getInputStream();
    }

    /**
     * @return the data object
     */
    ObjectData getObjectData();
}

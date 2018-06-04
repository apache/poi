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
import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.sl.usermodel.SlideShowFactory;
import org.apache.poi.util.Internal;

@Internal
public class XSLFSlideShowFactory extends SlideShowFactory {

    /**
     * Creates a XMLSlideShow from the given OOXML Package.
     * This is a convenience method to go along the create-methods of the super class.
     *
     * <p>Note that in order to properly release resources the
     *  SlideShow should be closed after use.</p>
     *
     *  @param pkg The {@link OPCPackage} opened for reading data.
     *
     *  @return The created SlideShow
     *
     *  @throws IOException if an error occurs while reading the data
     * @throws InvalidFormatException
     */
    public static XMLSlideShow create(OPCPackage pkg) throws IOException {
        try {
            return new XMLSlideShow(pkg);
        } catch (IllegalArgumentException ioe) {
            // ensure that file handles are closed (use revert() to not re-write the file)
            pkg.revert();
            //pkg.close();

            // rethrow exception
            throw ioe;
        }
    }

    /**
     * Creates a XMLSlideShow from the given OOXML Package
     *
     * <p>Note that in order to properly release resources the
     *  SlideShow should be closed after use.</p>
     *
     *  @param pkg The {@link OPCPackage} opened for reading data.
     *
     *  @return The created SlideShow
     *
     *  @throws IOException if an error occurs while reading the data
     * @throws InvalidFormatException 
     */
    public static XMLSlideShow createSlideShow(OPCPackage pkg) throws IOException {
        try {
            return new XMLSlideShow(pkg);
        } catch (IllegalArgumentException ioe) {
            // ensure that file handles are closed (use revert() to not re-write the file)
            pkg.revert();
            //pkg.close();

            // rethrow exception
            throw ioe;
        }
    }
    
    /**
     * Creates the XMLSlideShow from the given File, which must exist and be readable.
     * <p>Note that in order to properly release resources theSlideShow should be closed after use.
     *
     *  @param file The file to read data from.
     *  @param readOnly If the SlideShow should be opened in read-only mode to avoid writing back
     *      changes when the document is closed.
     *
     *  @return The created SlideShow
     *
     *  @throws IOException if an error occurs while reading the data
     *  @throws EncryptedDocumentException If the wrong password is given for a protected file
     */
    @SuppressWarnings("resource")
    public static XMLSlideShow createSlideShow(File file, boolean readOnly)
    throws IOException, InvalidFormatException {
        OPCPackage pkg = OPCPackage.open(file, readOnly ? PackageAccess.READ : PackageAccess.READ_WRITE);
        return createSlideShow(pkg);
    }

    /**
     * Creates a XMLSlideShow from the given InputStream
     *
     * <p>Note that in order to properly release resources the
     * SlideShow should be closed after use.</p>
     *
     * @param stream The {@link InputStream} to read data from.
     *
     * @return The created SlideShow
     *
     * @throws IOException if an error occurs while reading the data
     * @throws InvalidFormatException 
     */
    @SuppressWarnings("resource")
    public static XMLSlideShow createSlideShow(InputStream stream) throws IOException, InvalidFormatException {
        OPCPackage pkg = OPCPackage.open(stream);
        return createSlideShow(pkg);
    }

}

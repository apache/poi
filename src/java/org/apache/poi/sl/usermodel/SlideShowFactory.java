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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.OldFileFormatException;
import org.apache.poi.hssf.record.crypto.Biff8EncryptionKey;
import org.apache.poi.poifs.crypt.Decryptor;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.DocumentFactoryHelper;
import org.apache.poi.poifs.filesystem.FileMagic;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.poifs.filesystem.OfficeXmlFileException;
import org.apache.poi.util.IOUtils;

public class SlideShowFactory {
    /**
     * Creates a SlideShow from the given POIFSFileSystem.
     *
     * @param fs The {@link POIFSFileSystem} to read the document from
     *
     * @return The created SlideShow
     *
     * @throws IOException if an error occurs while reading the data
     */
    public static <
        S extends Shape<S,P>,
        P extends TextParagraph<S,P,? extends TextRun>
    > SlideShow<S,P> create(POIFSFileSystem fs) throws IOException {
        return create(fs, null);
    }

    /**
     * Creates a SlideShow from the given POIFSFileSystem, which may
     * be password protected
     *
     * @param fs The {@link POIFSFileSystem} to read the document from
     * @param password The password that should be used or null if no password is necessary.
     *
     * @return The created SlideShow
     *
     * @throws IOException if an error occurs while reading the data
     */
    public static <
        S extends Shape<S,P>,
        P extends TextParagraph<S,P,? extends TextRun>
    > SlideShow<S,P> create(final POIFSFileSystem fs, String password) throws IOException {
        return create(fs.getRoot(), password);
    }

    /**
     * Creates a SlideShow from the given DirectoryNode.
     *
     * @param root The {@link DirectoryNode} to start reading the document from
     *
     * @return The created SlideShow
     *
     * @throws IOException if an error occurs while reading the data
     */
    public static <
        S extends Shape<S,P>,
        P extends TextParagraph<S,P,? extends TextRun>
    > SlideShow<S,P> create(final DirectoryNode root) throws IOException {
        return create(root, null);
    }


    /**
     * Creates a SlideShow from the given DirectoryNode, which may
     * be password protected
     *
     * @param root The {@link DirectoryNode} to start reading the document from
     * @param password The password that should be used or null if no password is necessary.
     *
     * @return The created SlideShow
     *
     * @throws IOException if an error occurs while reading the data
     */
    public static <
        S extends Shape<S,P>,
        P extends TextParagraph<S,P,? extends TextRun>
    > SlideShow<S,P> create(final DirectoryNode root, String password) throws IOException {
        // Encrypted OOXML files go inside OLE2 containers, is this one?
        if (root.hasEntry(Decryptor.DEFAULT_POIFS_ENTRY)) {
            InputStream stream = null;
            try {
                stream = DocumentFactoryHelper.getDecryptedStream(root, password);

                return createXSLFSlideShow(stream);
            } finally {
                IOUtils.closeQuietly(stream);

                // as we processed the full stream already, we can close the filesystem here
                // otherwise file handles are leaked
                root.getFileSystem().close();
            }
        }

        // If we get here, it isn't an encrypted PPTX file
        // So, treat it as a regular HSLF PPT one
        boolean passwordSet = false;
        if (password != null) {
            Biff8EncryptionKey.setCurrentUserPassword(password);
            passwordSet = true;
        }
        try {
            return createHSLFSlideShow(root);
        } finally {
            if (passwordSet) {
                Biff8EncryptionKey.setCurrentUserPassword(null);
            }
        }
    }

    /**
     * Creates the appropriate HSLFSlideShow / XMLSlideShow from
     *  the given InputStream.
     *
     * <p>Note that using an {@link InputStream} has a higher memory footprint
     *  than using a {@link File}.</p>
     *
     * <p>Note that in order to properly release resources the
     *  SlideShow should be closed after use. Note also that loading
     *  from an InputStream requires more memory than loading
     *  from a File, so prefer {@link #create(File)} where possible.
     *
     *  @param inp The {@link InputStream} to read data from.
     *
     *  @return The created SlideShow
     *
     *  @throws IOException if an error occurs while reading the data
     *  @throws EncryptedDocumentException If the SlideShow given is password protected
     */
    public static <
        S extends Shape<S,P>,
        P extends TextParagraph<S,P,? extends TextRun>
    > SlideShow<S,P> create(InputStream inp) throws IOException, EncryptedDocumentException {
        return create(inp, null);
    }

    /**
     * Creates the appropriate HSLFSlideShow / XMLSlideShow from
     *  the given InputStream, which may be password protected.
     *  
     * <p>Note that using an {@link InputStream} has a higher memory footprint
     *  than using a {@link File}.</p>
     *
     * <p>Note that in order to properly release resources the
     *  SlideShow should be closed after use. Note also that loading
     *  from an InputStream requires more memory than loading
     *  from a File, so prefer {@link #create(File)} where possible.</p>
     *
     *  @param inp The {@link InputStream} to read data from.
     *  @param password The password that should be used or null if no password is necessary.
     *
     *  @return The created SlideShow
     *
     *  @throws IOException if an error occurs while reading the data
     *  @throws EncryptedDocumentException If the wrong password is given for a protected file
     */
    public static <
        S extends Shape<S,P>,
        P extends TextParagraph<S,P,? extends TextRun>
    > SlideShow<S,P> create(InputStream inp, String password) throws IOException, EncryptedDocumentException {
        InputStream is = FileMagic.prepareToCheckMagic(inp);
        FileMagic fm = FileMagic.valueOf(is);
        
        switch (fm) {
        case OLE2:
            POIFSFileSystem fs = new POIFSFileSystem(is);
            return create(fs, password);
        case OOXML:
            return createXSLFSlideShow(is);
        default:
            throw new IOException("Your InputStream was neither an OLE2 stream, nor an OOXML stream");
        }
    }

    /**
     * Creates the appropriate HSLFSlideShow / XMLSlideShow from
     *  the given File, which must exist and be readable.
     * <p>Note that in order to properly release resources the
     *  SlideShow should be closed after use.
     *
     *  @param file The file to read data from.
     *
     *  @return The created SlideShow
     *
     *  @throws IOException if an error occurs while reading the data
     *  @throws EncryptedDocumentException If the SlideShow given is password protected
     */
    public static <
        S extends Shape<S,P>,
        P extends TextParagraph<S,P,? extends TextRun>
    > SlideShow<S,P> create(File file) throws IOException, EncryptedDocumentException {
        return create(file, null);
    }

    /**
     * Creates the appropriate HSLFSlideShow / XMLSlideShow from
     *  the given File, which must exist and be readable, and
     *  may be password protected
     * <p>Note that in order to properly release resources the
     *  SlideShow should be closed after use.
     *
     *  @param file The file to read data from.
     *  @param password The password that should be used or null if no password is necessary.
     *
     *  @return The created SlideShow
     *
     *  @throws IOException if an error occurs while reading the data
     *  @throws EncryptedDocumentException If the wrong password is given for a protected file
     */
    public static <
        S extends Shape<S,P>,
        P extends TextParagraph<S,P,? extends TextRun>
    > SlideShow<S,P> create(File file, String password) throws IOException, EncryptedDocumentException {
        return create(file, password, false);
    }

    /**
     * Creates the appropriate HSLFSlideShow / XMLSlideShow from
     *  the given File, which must exist and be readable, and
     *  may be password protected
     * <p>Note that in order to properly release resources the
     *  SlideShow should be closed after use.
     *
     *  @param file The file to read data from.
     *  @param password The password that should be used or null if no password is necessary.
     *  @param readOnly If the SlideShow should be opened in read-only mode to avoid writing back
     *      changes when the document is closed.
     *
     *  @return The created SlideShow
     *
     *  @throws IOException if an error occurs while reading the data
     *  @throws EncryptedDocumentException If the wrong password is given for a protected file
     */
    public static <
        S extends Shape<S,P>,
        P extends TextParagraph<S,P,? extends TextRun>
    > SlideShow<S,P> create(File file, String password, boolean readOnly) throws IOException, EncryptedDocumentException {
        if (!file.exists()) {
            throw new FileNotFoundException(file.toString());
        }

        POIFSFileSystem fs = null;
        try {
            fs = new POIFSFileSystem(file, readOnly);
            return create(fs, password);
        } catch(OfficeXmlFileException e) {
            IOUtils.closeQuietly(fs);
            return createXSLFSlideShow(file, readOnly);
        } catch(RuntimeException e) {
            IOUtils.closeQuietly(fs);
            throw e;
        }
    }
    
    private static <
        S extends Shape<S,P>,
        P extends TextParagraph<S,P,? extends TextRun>
    > SlideShow<S,P> createHSLFSlideShow(Object... args) throws IOException, EncryptedDocumentException {
        return createSlideShow("org.apache.poi.hslf.usermodel.HSLFSlideShowFactory", args);
    }
    
    private static <
        S extends Shape<S,P>,
        P extends TextParagraph<S,P,? extends TextRun>
    > SlideShow<S,P> createXSLFSlideShow(Object... args) throws IOException, EncryptedDocumentException {
        return createSlideShow("org.apache.poi.xslf.usermodel.XSLFSlideShowFactory", args);
    }

    private static <
        S extends Shape<S,P>,
        P extends TextParagraph<S,P,? extends TextRun>
    > SlideShow<S,P> createSlideShow(String factoryClass, Object[] args) throws IOException, EncryptedDocumentException {
        try {
            Class<?> clazz = SlideShowFactory.class.getClassLoader().loadClass(factoryClass);
            Class<?>[] argsClz = new Class<?>[args.length];
            int i=0;
            for (Object o : args) {
                Class<?> c = o.getClass();
                if (Boolean.class.isAssignableFrom(c)) {
                    c = boolean.class;
                } else if (InputStream.class.isAssignableFrom(c)) {
                    c = InputStream.class;
                }
                argsClz[i++] = c;
            }
            Method m = clazz.getMethod("createSlideShow", argsClz);
            return (SlideShow<S,P>)m.invoke(null, args);
        } catch (InvocationTargetException e) {
            Throwable t = e.getCause();
            if (t instanceof IOException) {
                throw (IOException)t;
            } else if (t instanceof EncryptedDocumentException) {
                throw (EncryptedDocumentException)t;
            } else if (t instanceof OldFileFormatException) {
                throw (OldFileFormatException)t;
            } else if (t instanceof RuntimeException) {
                throw (RuntimeException)t;
            } else {
                throw new IOException(t);
            }
        } catch (Exception e) {
            throw new IOException(e);
        }
    }
}

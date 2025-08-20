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
package org.apache.poi.xwpf;

import java.io.IOException;
import java.io.InputStream;
import java.io.File;

import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.util.ExceptionUtil;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

/**
 * Methods that wrap {@link XWPFDocument} parsing functionality.
 * One difference is that the methods in this class try to
 * throw {@link XWPFReadException} or {@link IOException} instead of {@link RuntimeException}.
 * You can still get an {@link Error}s like an {@link OutOfMemoryError}.
 *
 * @since POI 5.5.0
 */
public final class XWPFParser {

    private XWPFParser() {
        // Prevent instantiation
    }

    /**
     * Parse the given InputStream and return a new {@link XWPFDocument} instance.
     *
     * @param stream the data to parse (will be closed after parsing)
     * @return a new {@link XWPFDocument} instance
     * @throws XWPFReadException if an error occurs while reading the file
     * @throws IOException if an I/O error occurs while reading the file
     */
    public static XWPFDocument parse(InputStream stream) throws XWPFReadException, IOException {
        try {
            return new XWPFDocument(stream);
        } catch (IOException e) {
            throw e;
        } catch (Error | RuntimeException e) {
            if (ExceptionUtil.isFatal(e)) {
                throw e;
            }
            throw new XWPFReadException("Exception reading XWPFDocument", e);
        } catch (Exception e) {
            throw new XWPFReadException("Exception reading XWPFDocument", e);
        }
    }

    /**
     * Parse the given InputStream and return a new {@link XWPFDocument} instance.
     *
     * @param stream the data to parse
     * @param closeStream whether to close the InputStream after parsing
     * @return a new {@link XWPFDocument} instance
     * @throws XWPFReadException if an error occurs while reading the file
     * @throws IOException if an I/O error occurs while reading the file
     */
    public static XWPFDocument parse(InputStream stream, boolean closeStream) throws XWPFReadException, IOException {
        try {
            return new XWPFDocument(stream, closeStream);
        } catch (IOException e) {
            throw e;
        } catch (Error | RuntimeException e) {
            if (ExceptionUtil.isFatal(e)) {
                throw e;
            }
            throw new XWPFReadException("Exception reading XWPFDocument", e);
        } catch (Exception e) {
            throw new XWPFReadException("Exception reading XWPFDocument", e);
        }
    }

    /**
     * Parse the given InputStream and return a new {@link XWPFDocument} instance.
     *
     * @param file to parse
     * @return a new {@link XWPFDocument} instance
     * @throws XWPFReadException if an error occurs while reading the file
     * @throws IOException if an I/O error occurs while reading the file
     */
    public static XWPFDocument parse(File file) throws XWPFReadException, IOException {
        OPCPackage pkg = null;
        try {
            pkg = OPCPackage.open(file);
            return new XWPFDocument(pkg);
        } catch (IOException e) {
            if (pkg != null) {
                IOUtils.closeQuietly(pkg);
            }
            throw e;
        } catch (Error | RuntimeException e) {
            if (pkg != null) {
                IOUtils.closeQuietly(pkg);
            }
            if (ExceptionUtil.isFatal(e)) {
                throw e;
            }
            throw new XWPFReadException("Exception reading XWPFDocument", e);
        } catch (Exception e) {
            if (pkg != null) {
                IOUtils.closeQuietly(pkg);
            }
            throw new XWPFReadException("Exception reading XWPFDocument", e);
        }
    }

    /**
     * Parse the given {@link OPCPackage} and return a new {@link XWPFDocument} instance.
     *
     * @param pkg to parse
     * @return a new {@link XWPFDocument} instance
     * @throws XWPFReadException if an error occurs while reading the file
     * @throws IOException if an I/O error occurs while reading the file
     */
    public static XWPFDocument parse(OPCPackage pkg) throws XWPFReadException, IOException {
        try {
            return new XWPFDocument(pkg);
        } catch (IOException e) {
            throw e;
        } catch (Error | RuntimeException e) {
            if (ExceptionUtil.isFatal(e)) {
                throw e;
            }
            throw new XWPFReadException("Exception reading XWPFDocument", e);
        } catch (Exception e) {
            throw new XWPFReadException("Exception reading XWPFDocument", e);
        }
    }

    /**
     * Parse the given {@link PackagePart} and return a new {@link XWPFDocument} instance.
     *
     * @param packagePart to parse
     * @return a new {@link XWPFDocument} instance
     * @throws XWPFReadException if an error occurs while reading the file
     * @throws IOException if an I/O error occurs while reading the file
     */
    public static XWPFDocument parse(PackagePart packagePart) throws XWPFReadException, IOException {
        try (InputStream is = packagePart.getInputStream()) {
            return new XWPFDocument(is);
        } catch (IOException e) {
            throw e;
        } catch (Error | RuntimeException e) {
            if (ExceptionUtil.isFatal(e)) {
                throw e;
            }
            throw new XWPFReadException("Exception reading XWPFDocument", e);
        } catch (Exception e) {
            throw new XWPFReadException("Exception reading XWPFDocument", e);
        }
    }
}

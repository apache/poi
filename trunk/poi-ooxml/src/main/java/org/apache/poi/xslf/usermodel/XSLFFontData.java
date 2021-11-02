/*
 *  ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ====================================================================
 */

package org.apache.poi.xslf.usermodel;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.poi.ooxml.POIXMLDocumentPart;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.util.Beta;

/**
 * A container for fontdata files, i.e. MTX fonts derived from
 * true (TTF) or open (OTF) type fonts.
 *
 * @since POI 4.1.0
 */
@Beta
public class XSLFFontData extends POIXMLDocumentPart {
    /**
     * Create a new XSLFFontData node
     */
    @SuppressWarnings("unused")
    protected XSLFFontData() {
        super();
    }

    /**
     * Construct XSLFFontData from a package part
     *
     * @param part the package part holding the ole data
     */
    @SuppressWarnings("unused")
    public XSLFFontData(final PackagePart part) {
        super(part);
    }

    public InputStream getInputStream() throws IOException {
        return getPackagePart().getInputStream();
    }

    public OutputStream getOutputStream() throws IOException {
        final PackagePart pp = getPackagePart();
        pp.clear();
        return pp.getOutputStream();
    }

    /**
     * XSLFFontData objects store the actual content in the part directly without keeping a
     * copy like all others therefore we need to handle them differently.
     */
    @Override
    protected void prepareForCommit() {
        // do not clear the part here
    }


    public void setData(final byte[] data) throws IOException {
        try (final OutputStream os = getPackagePart().getOutputStream()) {
            os.write(data);
        }
    }



}

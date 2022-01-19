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

/* ====================================================================
   This product contains an ASLv2 licensed version of the OOXML signer
   package from the eID Applet project
   http://code.google.com/p/eid-applet/source/browse/trunk/README.txt
   Copyright (C) 2008-2014 FedICT.
   ================================================================= */

package org.apache.poi.poifs.crypt.dsig;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.crypto.Data;
import javax.xml.crypto.OctetStreamData;
import javax.xml.crypto.URIDereferencer;
import javax.xml.crypto.URIReference;
import javax.xml.crypto.URIReferenceException;
import javax.xml.crypto.XMLCryptoContext;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.UnsynchronizedByteArrayOutputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackagePartName;
import org.apache.poi.openxml4j.opc.PackagingURIHelper;

/**
 * JSR105 URI dereferencer for Office Open XML documents.
 */
public class OOXMLURIDereferencer implements URIDereferencer {

    private static final Logger LOG = LogManager.getLogger(OOXMLURIDereferencer.class);

    private SignatureInfo signatureInfo;
    private URIDereferencer baseUriDereferencer;

    public void setSignatureInfo(SignatureInfo signatureInfo) {
        this.signatureInfo = signatureInfo;
        baseUriDereferencer = signatureInfo.getSignatureFactory().getURIDereferencer();
    }

    @Override
    public Data dereference(URIReference uriReference, XMLCryptoContext context) throws URIReferenceException {
        if (uriReference == null) {
            throw new NullPointerException("URIReference cannot be null - call setSignatureInfo(...) before");
        }
        if (context == null) {
            throw new NullPointerException("XMLCryptoContext cannot be null");
        }

        URI uri;
        try {
            uri = new URI(uriReference.getURI());
        } catch (URISyntaxException e) {
            throw new URIReferenceException("could not URL decode the uri: "+uriReference.getURI(), e);
        }

        PackagePart part = findPart(uri);
        if (part == null) {
            LOG.atDebug().log("cannot resolve {}, delegating to base DOM URI dereferencer", uri);
            return baseUriDereferencer.dereference(uriReference, context);
        }

        InputStream dataStream = null;
        try {
            dataStream = part.getInputStream();

            // workaround for office 2007 pretty-printed .rels files
            if (part.getPartName().toString().endsWith(".rels")) {
                // although xmlsec has an option to ignore line breaks, currently this
                // only affects .rels files, so we only modify these
                try (UnsynchronizedByteArrayOutputStream bos = new UnsynchronizedByteArrayOutputStream()) {
                    for (int ch; (ch = dataStream.read()) != -1; ) {
                        if (ch == 10 || ch == 13) continue;
                        bos.write(ch);
                    }
                    dataStream = bos.toInputStream();
                }
            }
        } catch (IOException e) {
            IOUtils.closeQuietly(dataStream);
            throw new URIReferenceException("I/O error: " + e.getMessage(), e);
        }

        return new OctetStreamData(dataStream, uri.toString(), null);
    }

    private PackagePart findPart(URI uri) {
        LOG.atDebug().log("dereference: {}", uri);

        String path = uri.getPath();
        if (path == null || path.isEmpty()) {
            LOG.atDebug().log("illegal part name (expected): {}", uri);
            return null;
        }

        PackagePartName ppn;
        try {
            ppn = PackagingURIHelper.createPartName(path);
            return signatureInfo.getOpcPackage().getPart(ppn);
        } catch (InvalidFormatException e) {
            LOG.atWarn().log("illegal part name (not expected) in {}", uri);
            return null;
        }
    }
}

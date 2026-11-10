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
import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.crypto.Data;
import javax.xml.crypto.OctetStreamData;
import javax.xml.crypto.URIDereferencer;
import javax.xml.crypto.URIReference;
import javax.xml.crypto.URIReferenceException;
import javax.xml.crypto.XMLCryptoContext;

import org.apache.logging.log4j.Logger;
import org.apache.poi.logging.PoiLogManager;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackagePartName;
import org.apache.poi.openxml4j.opc.PackagingURIHelper;

/**
 * JSR105 URI dereferencer for Office Open XML documents.
 */
public class OOXMLURIDereferencer implements URIDereferencer {

    private static final Logger LOG = PoiLogManager.getLogger(OOXMLURIDereferencer.class);
    private static final String CONTENT_TYPE_QUERY = "ContentType=";

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
            // Signature references are expected to point at parts within the package (or be
            // same-document references). A URI that carries a scheme (i.e. an absolute URI)
            // refers to something outside the package and is not resolved here.
            if (uri.isAbsolute()) {
                throw new URIReferenceException("only in-package references are supported: " + uri);
            }
            LOG.atDebug().log("cannot resolve {}, delegating to base DOM URI dereferencer", uri);
            return baseUriDereferencer.dereference(uriReference, context);
        }

        // [Content_Types].xml is not signed, so a signed reference carries the content type of
        // the part in its query - the signature only covers the part if the content type matches.
        // Office and POI write the content type as the only query parameter
        String query = uri.getQuery();
        if (query != null && query.startsWith(CONTENT_TYPE_QUERY)) {
            String signedContentType = query.substring(CONTENT_TYPE_QUERY.length());
            if (!signedContentType.equalsIgnoreCase(part.getContentType())) {
                throw new ContentTypeMismatchException("content type of " + part.getPartName() + " is " +
                    part.getContentType() + ", but the signature references " + signedContentType);
            }
        }

        // .rels parts are not modified: the relationship transform drops the whitespace between
        // relationships (e.g. of office 2007 pretty-printed .rels files), and removing line breaks
        // inside an attribute would digest another value than the package reads
        try {
            return new OctetStreamData(part.getInputStream(), uri.toString(), null);
        } catch (IOException e) {
            throw new URIReferenceException("I/O error: " + e.getMessage(), e);
        }
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

    /**
     * The content type of a part differs from the one the signature references, i.e. the signature is invalid
     */
    static final class ContentTypeMismatchException extends URIReferenceException {
        private static final long serialVersionUID = 1L;

        ContentTypeMismatchException(String message) {
            super(message);
        }
    }
}

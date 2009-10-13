
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


/*
 * Based on the eID Applet Project code.
 * Original Copyright (C) 2008-2009 FedICT.
 */

package org.apache.poi.ooxml.signature.service.signer.ooxml;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;

import javax.xml.crypto.Data;
import javax.xml.crypto.OctetStreamData;
import javax.xml.crypto.URIDereferencer;
import javax.xml.crypto.URIReference;
import javax.xml.crypto.URIReferenceException;
import javax.xml.crypto.XMLCryptoContext;
import javax.xml.crypto.dsig.XMLSignatureFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.POIXMLDocument;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackagePart;

/**
 * JSR105 URI dereferencer for Office Open XML documents.
 */
public class OOXMLURIDereferencer implements URIDereferencer {

    private static final Log LOG = LogFactory.getLog(OOXMLURIDereferencer.class);

    private final URL ooxmlUrl;

    private final URIDereferencer baseUriDereferencer;

    public OOXMLURIDereferencer(URL ooxmlUrl) {
        if (null == ooxmlUrl) {
            throw new IllegalArgumentException("ooxmlUrl is null");
        }
        this.ooxmlUrl = ooxmlUrl;
        XMLSignatureFactory xmlSignatureFactory = XMLSignatureFactory.getInstance();
        this.baseUriDereferencer = xmlSignatureFactory.getURIDereferencer();
    }

    public Data dereference(URIReference uriReference, XMLCryptoContext context) throws URIReferenceException {
        if (null == uriReference) {
            throw new NullPointerException("URIReference cannot be null");
        }
        if (null == context) {
            throw new NullPointerException("XMLCrytoContext cannot be null");
        }

        String uri = uriReference.getURI();
        try {
            uri = URLDecoder.decode(uri, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            LOG.warn("could not URL decode the uri: " + uri);
        }
        LOG.debug("dereference: " + uri);
        try {
            InputStream dataInputStream = findDataInputStream(uri);
            if (null == dataInputStream) {
                LOG.debug("cannot resolve, delegating to base DOM URI dereferencer: " + uri);
                return this.baseUriDereferencer.dereference(uriReference, context);
            }
            return new OctetStreamData(dataInputStream, uri, null);
        } catch (IOException e) {
            throw new URIReferenceException("I/O error: " + e.getMessage(), e);
        } catch (InvalidFormatException e) {
            throw new URIReferenceException("Invalid format error: " + e.getMessage(), e);
        }
    }

    private InputStream findDataInputStream(String uri) throws IOException, InvalidFormatException {
        if (-1 != uri.indexOf("?")) {
            uri = uri.substring(0, uri.indexOf("?"));
        }
        OPCPackage pkg = POIXMLDocument.openPackage(this.ooxmlUrl.getPath());
        for (PackagePart part : pkg.getParts()) {
            if (uri.equals(part.getPartName().getURI().toString())) {
                LOG.debug("Part name: " + part.getPartName());
                return part.getInputStream();
            }
        }
        LOG.info("No part found for URI: " + uri);
        return null;
    }
}

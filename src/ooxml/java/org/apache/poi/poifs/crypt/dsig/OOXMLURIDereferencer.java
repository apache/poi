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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackagePartName;
import org.apache.poi.openxml4j.opc.PackagingURIHelper;
import org.apache.poi.poifs.crypt.dsig.SignatureConfig.SignatureConfigurable;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;

/**
 * JSR105 URI dereferencer for Office Open XML documents.
 */
public class OOXMLURIDereferencer implements URIDereferencer, SignatureConfigurable {

    private static final POILogger LOG = POILogFactory.getLogger(OOXMLURIDereferencer.class);

    private SignatureConfig signatureConfig;
    private URIDereferencer baseUriDereferencer;

    public void setSignatureConfig(SignatureConfig signatureConfig) {
        this.signatureConfig = signatureConfig;
    }

    public Data dereference(URIReference uriReference, XMLCryptoContext context) throws URIReferenceException {
        if (baseUriDereferencer == null) {
            baseUriDereferencer = signatureConfig.getSignatureFactory().getURIDereferencer();
        }
        
        if (null == uriReference) {
            throw new NullPointerException("URIReference cannot be null");
        }
        if (null == context) {
            throw new NullPointerException("XMLCrytoContext cannot be null");
        }

        URI uri;
        try {
            uri = new URI(uriReference.getURI());
        } catch (URISyntaxException e) {
            throw new URIReferenceException("could not URL decode the uri: "+uriReference.getURI(), e);
        }

        PackagePart part = findPart(uri);
        if (part == null) {
            LOG.log(POILogger.DEBUG, "cannot resolve, delegating to base DOM URI dereferencer", uri);
            return this.baseUriDereferencer.dereference(uriReference, context);
        }

        InputStream dataStream;
        try {
            dataStream = part.getInputStream();

            // workaround for office 2007 pretty-printed .rels files
            if (part.getPartName().toString().endsWith(".rels")) {
                // although xmlsec has an option to ignore line breaks, currently this
                // only affects .rels files, so we only modify these
                // http://stackoverflow.com/questions/4728300
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                for (int ch; (ch = dataStream.read()) != -1; ) {
                    if (ch == 10 || ch == 13) continue;
                    bos.write(ch);
                }
                dataStream = new ByteArrayInputStream(bos.toByteArray());
            }
        } catch (IOException e) {
            throw new URIReferenceException("I/O error: " + e.getMessage(), e);
        }
        
        return new OctetStreamData(dataStream, uri.toString(), null);
    }

    private PackagePart findPart(URI uri) {
        LOG.log(POILogger.DEBUG, "dereference", uri);

        String path = uri.getPath();
        if (path == null || path.isEmpty()) {
            LOG.log(POILogger.DEBUG, "illegal part name (expected)", uri);
            return null;
        }
        
        PackagePartName ppn;
        try {
            ppn = PackagingURIHelper.createPartName(path);
        } catch (InvalidFormatException e) {
            LOG.log(POILogger.WARN, "illegal part name (not expected)", uri);
            return null;
        }
        
        return signatureConfig.getOpcPackage().getPart(ppn);
    }
}

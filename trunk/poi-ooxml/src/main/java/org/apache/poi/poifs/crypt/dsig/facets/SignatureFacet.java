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

package org.apache.poi.poifs.crypt.dsig.facets;

import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.XMLObject;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureException;

import org.apache.poi.openxml4j.opc.PackageNamespaces;
import org.apache.poi.poifs.crypt.dsig.SignatureInfo;
import org.apache.poi.util.Internal;
import org.w3c.dom.Document;

/**
 * JSR105 Signature Facet base class.
 */
@Internal
public interface SignatureFacet {

    String XML_NS = XMLConstants.XMLNS_ATTRIBUTE_NS_URI;
    String XML_DIGSIG_NS = XMLSignature.XMLNS;
    String OO_DIGSIG_NS = PackageNamespaces.DIGITAL_SIGNATURE;
    String MS_DIGSIG_NS = "http://schemas.microsoft.com/office/2006/digsig";
    String XADES_132_NS = "http://uri.etsi.org/01903/v1.3.2#";
    String XADES_141_NS = "http://uri.etsi.org/01903/v1.4.1#";

    /**
     * This method is being invoked by the XML signature service engine during
     * pre-sign phase. Via this method a signature facet implementation can add
     * signature facets to an XML signature.
     *
     * @param signatureInfo the signature info object holding the OPCPackage and other document related data
     * @param document the signature document to be used for imports
     * @param references list of reference definitions
     * @param objects objects to be signed/included in the signature document
     */
    default void preSign(
          SignatureInfo signatureInfo
        , Document document
        , List<Reference> references
        , List<XMLObject> objects
    ) throws XMLSignatureException {

    }

    /**
     * This method is being invoked by the XML signature service engine during
     * the post-sign phase. Via this method a signature facet can extend the XML
     * signatures with for example key information.
     *
     * @param signatureInfo the signature info object holding the OPCPackage and other document related data
     * @param document the signature document to be modified
     */
    default void postSign(SignatureInfo signatureInfo, Document document) throws MarshalException {

    }

}
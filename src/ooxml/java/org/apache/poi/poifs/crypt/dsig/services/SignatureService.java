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

package org.apache.poi.poifs.crypt.dsig.services;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.xml.crypto.MarshalException;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.poi.poifs.crypt.dsig.spi.DigestInfo;
import org.apache.xmlbeans.XmlException;
import org.w3c.dom.Document;

/**
 * Interface for signature service component.
 * 
 * @author Frank Cornelis
 * 
 */
public interface SignatureService {

    /**
     * Pre-sign callback method. Depending on the configuration some parameters
     * are passed. The returned value will be signed by the eID Applet.
     * 
     * @param digestInfos
     *            the optional list of digest infos.
     * @return the digest to be signed.
     * @throws NoSuchAlgorithmException
     */
    DigestInfo preSign(Document document, List<DigestInfo> digestInfos)
    throws NoSuchAlgorithmException;

    /**
     * Post-sign callback method. Received the signature value. Depending on the
     * configuration the signing certificate chain is also obtained.
     * 
     * @param signatureValue
     * @param signingCertificateChain
     *            the optional chain of signing certificates.
     */
    void postSign(Document document, byte[] signatureValue)
    throws IOException, MarshalException, ParserConfigurationException, XmlException;
}

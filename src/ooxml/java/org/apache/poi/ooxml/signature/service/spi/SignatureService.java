
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

package org.apache.poi.ooxml.signature.service.spi;

import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.List;

/**
 * Interface for signature service component.
 */
public interface SignatureService {

    /**
     * Gives back the digest algorithm to be used for construction of the digest
     * infos of the preSign method. Return a digest algorithm here if you want
     * to let the client sign some locally stored files. Return
     * <code>null</code> if no pre-sign digest infos are required.
     * 
     * @return
     * @see #preSign(List, List)
     */
    String getFilesDigestAlgorithm();

    /**
     * Pre-sign callback method. Depending on the configuration some parameters
     * are passed. The returned value will be signed by the eID Applet.
     * 
     * <p>
     * TODO: service must be able to throw some exception on failure.
     * </p>
     * 
     * @param digestInfos
     *            the optional list of digest infos.
     * @param signingCertificateChain
     *            the optional list of certificates.
     * @return the digest to be signed.
     * @throws NoSuchAlgorithmException
     */
    DigestInfo preSign(List<DigestInfo> digestInfos, List<X509Certificate> signingCertificateChain) throws NoSuchAlgorithmException;

    /**
     * Post-sign callback method. Received the signature value. Depending on the
     * configuration the signing certificate chain is also obtained.
     * 
     * <p>
     * TODO: service must be able to throw some exception on failure.
     * </p>
     * 
     * @param signatureValue
     * @param signingCertificateChain
     *            the optional chain of signing certificates.
     */
    void postSign(byte[] signatureValue, List<X509Certificate> signingCertificateChain);
}

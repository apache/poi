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

import java.security.cert.CRLException;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Container class for PKI revocation data.
 */
public class RevocationData {

    private final List<byte[]> crls = new ArrayList<>();
    private final List<byte[]> ocsps = new ArrayList<>();
    private final List<X509Certificate> x509chain = new ArrayList<>();

    /**
     * Adds a CRL to this revocation data set.
     */
    public void addCRL(byte[] encodedCrl) {
        if (this.crls.stream().noneMatch(by -> Arrays.equals(by, encodedCrl))) {
            this.crls.add(encodedCrl);
        }
    }

    /**
     * Adds a CRL to this revocation data set.
     */
    public void addCRL(X509CRL crl) {
        try {
            addCRL(crl.getEncoded());
        } catch (CRLException e) {
            throw new IllegalArgumentException("CRL coding error: " + e.getMessage(), e);
        }
    }

    /**
     * Adds an OCSP response to this revocation data set.
     */
    public void addOCSP(byte[] encodedOcsp) {
        this.ocsps.add(encodedOcsp);
    }

    public void addCertificate(X509Certificate x509) {
        x509chain.add(x509);
    }

    /**
     * Gives back a list of all CRLs.
     *
     * @return a list of all CRLs
     */
    public List<byte[]> getCRLs() {
        return this.crls;
    }

    /**
     * Gives back a list of all OCSP responses.
     *
     * @return a list of all OCSP response
     */
    public List<byte[]> getOCSPs() {
        return this.ocsps;
    }

    /**
     * Returns {@code true} if this revocation data set holds OCSP
     * responses.
     *
     * @return {@code true} if this revocation data set holds OCSP
     * responses.
     */
    public boolean hasOCSPs() {
        return !this.ocsps.isEmpty();
    }

    /**
     * Returns {@code true} if this revocation data set holds CRLs.
     *
     * @return {@code true} if this revocation data set holds CRLs.
     */
    public boolean hasCRLs() {
        return !this.crls.isEmpty();
    }

    /**
     * Returns {@code true} if this revocation data is not empty.
     *
     * @return {@code true} if this revocation data is not empty.
     */
    public boolean hasRevocationDataEntries() {
        return hasOCSPs() || hasCRLs();
    }

    public List<X509Certificate> getX509chain() {
        return x509chain;
    }
}

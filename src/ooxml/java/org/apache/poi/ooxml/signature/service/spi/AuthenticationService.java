
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

import java.security.cert.X509Certificate;
import java.util.List;

/**
 * Interface for authentication service components.
 */
public interface AuthenticationService {

    /**
     * Validates the given certificate chain. After the client has
     * verified the authentication signature, it will invoke this method on your
     * authentication service component. The implementation of this method
     * should validate the given certificate chain. This validation could be
     * based on PKI validation, or could be based on simply trusting the
     * incoming public key. The actual implementation is very dependent on your
     * type of application. This method should only be used for certificate
     * validation.
     * 
     * <p>
     * Check out <a href="http://code.google.com/p/jtrust/">jTrust</a> for an
     * implementation of a PKI validation framework.
     * </p>
     * 
     * @param certificateChain
     *            the X509 authentication certificate chain of the citizen.
     * @throws SecurityException
     *             in case the certificate chain is invalid/not accepted.
     */
    void validateCertificateChain(List<X509Certificate> certificateChain) throws SecurityException;
}

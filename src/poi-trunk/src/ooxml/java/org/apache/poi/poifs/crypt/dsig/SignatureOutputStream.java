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

package org.apache.poi.poifs.crypt.dsig;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;

import org.apache.poi.poifs.crypt.HashAlgorithm;

/* package */ class SignatureOutputStream extends DigestOutputStream {
    Signature signature;
    
    SignatureOutputStream(final HashAlgorithm algo, PrivateKey key) {
        super(algo, key);
    }
    
    @Override
    public void init() throws GeneralSecurityException {
        final String provider = isMSCapi(key) ? "SunMSCAPI" : "SunRsaSign";
        signature = Signature.getInstance(algo.ecmaString+"withRSA", provider);
        signature.initSign(key);
    }

    @Override
    public byte[] sign() throws SignatureException {
        return signature.sign();
    }
    

    @Override
    public void write(final int b) throws IOException {
        try {
            signature.update((byte)b);
        } catch (final SignatureException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void write(final byte[] data, final int off, final int len) throws IOException {
        try {
            signature.update(data, off, len);
        } catch (final SignatureException e) {
            throw new IOException(e);
        }
    }
}
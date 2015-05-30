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

package org.apache.poi.hssf.record.crypto;

import javax.crypto.spec.SecretKeySpec;

import org.apache.poi.poifs.crypt.CryptoFunctions;


public class Biff8XORKey extends Biff8EncryptionKey {
    final int _xorKey;
    
    public Biff8XORKey(String password, int xorKey) {
        _xorKey = xorKey;
        byte xorArray[] = CryptoFunctions.createXorArray1(password);
        _secretKey = new SecretKeySpec(xorArray, "XOR");
    }
    
    public static Biff8XORKey create(String password, int xorKey) {
        return new Biff8XORKey(password, xorKey);
    }

    public boolean validate(String password, int verifier) {
        int keyComp = CryptoFunctions.createXorKey1(password);
        int verifierComp = CryptoFunctions.createXorVerifier1(password);

        return (_xorKey == keyComp && verifierComp == verifier);
    }
}

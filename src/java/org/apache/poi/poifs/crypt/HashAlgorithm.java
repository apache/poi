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

package org.apache.poi.poifs.crypt;

import org.apache.poi.EncryptedDocumentException;

public enum HashAlgorithm {
    none     (         "", 0x0000,           "",  0,               "", false, ""),
    sha1     (    "SHA-1", 0x8004,       "SHA1", 20,       "HmacSHA1", false, "1.3.14.3.2.26"),
    sha256   (  "SHA-256", 0x800C,     "SHA256", 32,     "HmacSHA256", false, "2.16.840.1.101.3.4.2.1"),
    sha384   (  "SHA-384", 0x800D,     "SHA384", 48,     "HmacSHA384", false, "2.16.840.1.101.3.4.2.2"),
    sha512   (  "SHA-512", 0x800E,     "SHA512", 64,     "HmacSHA512", false, "2.16.840.1.101.3.4.2.3"),
    /* only for agile encryption */
    md5      (      "MD5",     -1,        "MD5", 16,        "HmacMD5", false, "1.2.840.113549.2.5" ),
    // although sunjc2 supports md2, hmac-md2 is only supported by bouncycastle
    md2      (      "MD2",     -1,        "MD2", 16,       "Hmac-MD2", true, "1.2.840.113549.2.2" ),
    md4      (      "MD4",     -1,        "MD4", 16,       "Hmac-MD4", true, "1.2.840.113549.2.4" ),
    ripemd128("RipeMD128",     -1, "RIPEMD-128", 16, "HMac-RipeMD128", true, "1.3.36.3.2.2"),
    ripemd160("RipeMD160",     -1, "RIPEMD-160", 20, "HMac-RipeMD160", true, "1.3.36.3.2.1"),
    whirlpool("Whirlpool",     -1,  "WHIRLPOOL", 64, "HMac-Whirlpool", true, "1.0.10118.3.0.55"),
    // only for xml signing
    sha224   (  "SHA-224",     -1,     "SHA224", 28,     "HmacSHA224", true, "2.16.840.1.101.3.4.2.4"),
    ripemd256("RipeMD256",     -1, "RIPEMD-256", 32, "HMac-RipeMD256", true, "1.3.36.3.2.3")
    ;

    /** the id used for initializing the JCE message digest **/
	public final String jceId;
	/** the id used for the BIFF encryption info header **/
	public final int ecmaId;
	/** the id used for OOXML encryption info header **/ 
    public final String ecmaString;
    /** the length of the digest byte array **/
    public final int hashSize;
    /** the id used for the integrity algorithm in agile encryption **/
    public final String jceHmacId;
    /** is bouncycastle necessary for calculating the digest **/ 
    public final boolean needsBouncyCastle;
    /** ASN1 object identifier of the digest value in combination with the RSA cipher */  
    public final String rsaOid;
    
    HashAlgorithm(String jceId, int ecmaId, String ecmaString, int hashSize, String jceHmacId, boolean needsBouncyCastle, String rsaOid) {
        this.jceId = jceId;
        this.ecmaId = ecmaId;
        this.ecmaString = ecmaString;
        this.hashSize = hashSize;
        this.jceHmacId = jceHmacId;
        this.needsBouncyCastle = needsBouncyCastle;
        this.rsaOid = rsaOid;
    }
    
    public static HashAlgorithm fromEcmaId(int ecmaId) {
        for (HashAlgorithm ha : values()) {
            if (ha.ecmaId == ecmaId) {
                return ha;
            }
        }
        throw new EncryptedDocumentException("hash algorithm not found");
    }    
    
    public static HashAlgorithm fromEcmaId(String ecmaString) {
        for (HashAlgorithm ha : values()) {
            if (ha.ecmaString.equals(ecmaString)) {
                return ha;
            }
        }
        throw new EncryptedDocumentException("hash algorithm not found");
    }
    
    public static HashAlgorithm fromString(String string) {
        for (HashAlgorithm ha : values()) {
            if (ha.ecmaString.equalsIgnoreCase(string) || ha.jceId.equalsIgnoreCase(string)) {
                return ha;
            }
        }
        throw new EncryptedDocumentException("hash algorithm not found");
    }
}
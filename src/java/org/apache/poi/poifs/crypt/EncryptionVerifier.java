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

import java.io.ByteArrayInputStream;

import org.apache.commons.codec.binary.Base64;

import org.apache.poi.poifs.filesystem.DocumentInputStream;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.NamedNodeMap;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.poi.EncryptedDocumentException;

/**
 *  @author Maxim Valyanskiy
 *  @author Gary King
 */
public class EncryptionVerifier {
    private final byte[] salt;
    private final byte[] verifier;
    private final byte[] verifierHash;
    private final byte[] encryptedKey;
    private final int verifierHashSize;
    private final int spinCount;
    private final int algorithm;
    private final int cipherMode;

    public EncryptionVerifier(String descriptor) {
        NamedNodeMap keyData = null;
        try {
            ByteArrayInputStream is;
            is = new ByteArrayInputStream(descriptor.getBytes());
            NodeList keyEncryptor = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder().parse(is)
                .getElementsByTagName("keyEncryptor").item(0).getChildNodes();
            for (int i = 0; i < keyEncryptor.getLength(); i++) {
                Node node = keyEncryptor.item(i);
                if (node.getNodeName().equals("p:encryptedKey")) {
                    keyData = node.getAttributes();
                    break;
                }
            }
            if (keyData == null)
                throw new EncryptedDocumentException("");
        } catch (Exception e) {
            throw new EncryptedDocumentException("Unable to parse keyEncryptor");
        }

        spinCount = Integer.parseInt(keyData.getNamedItem("spinCount")
                                     .getNodeValue());
        verifier = Base64.decodeBase64(keyData
                                       .getNamedItem("encryptedVerifierHashInput")
                                       .getNodeValue().getBytes());
        salt = Base64.decodeBase64(keyData.getNamedItem("saltValue")
                                   .getNodeValue().getBytes());

        encryptedKey = Base64.decodeBase64(keyData
                                           .getNamedItem("encryptedKeyValue")
                                           .getNodeValue().getBytes());

        int saltSize = Integer.parseInt(keyData.getNamedItem("saltSize")
                                        .getNodeValue());
        if (saltSize != salt.length)
            throw new EncryptedDocumentException("Invalid salt size");

        verifierHash = Base64.decodeBase64(keyData
                                           .getNamedItem("encryptedVerifierHashValue")
                                           .getNodeValue().getBytes());

        int blockSize = Integer.parseInt(keyData.getNamedItem("blockSize")
                                         .getNodeValue());

        String alg = keyData.getNamedItem("cipherAlgorithm").getNodeValue();

        if ("AES".equals(alg)) {
            if (blockSize == 16)
                algorithm = EncryptionHeader.ALGORITHM_AES_128;
            else if (blockSize == 24)
                algorithm = EncryptionHeader.ALGORITHM_AES_192;
            else if (blockSize == 32)
                algorithm = EncryptionHeader.ALGORITHM_AES_256;
            else
                throw new EncryptedDocumentException("Unsupported block size");
        } else {
            throw new EncryptedDocumentException("Unsupported cipher");
        }

        String chain = keyData.getNamedItem("cipherChaining").getNodeValue();
        if ("ChainingModeCBC".equals(chain))
            cipherMode = EncryptionHeader.MODE_CBC;
        else if ("ChainingModeCFB".equals(chain))
            cipherMode = EncryptionHeader.MODE_CFB;
        else
            throw new EncryptedDocumentException("Unsupported chaining mode");

        verifierHashSize = Integer.parseInt(keyData.getNamedItem("hashSize")
                                            .getNodeValue());
    }

    public EncryptionVerifier(DocumentInputStream is, int encryptedLength) {
        int saltSize = is.readInt();

        if (saltSize!=16) {
            throw new RuntimeException("Salt size != 16 !?");
        }

        salt = new byte[16];
        is.readFully(salt);
        verifier = new byte[16];
        is.readFully(verifier);

        verifierHashSize = is.readInt();

        verifierHash = new byte[encryptedLength];
        is.readFully(verifierHash);

        spinCount = 50000;
        algorithm = EncryptionHeader.ALGORITHM_AES_128;
        cipherMode = EncryptionHeader.MODE_ECB;
        encryptedKey = null;
    }

    public byte[] getSalt() {
        return salt;
    }

    public byte[] getVerifier() {
        return verifier;
    }

    public byte[] getVerifierHash() {
        return verifierHash;
    }

    public int getSpinCount() {
        return spinCount;
    }

    public int getCipherMode() {
        return cipherMode;
    }

    public int getAlgorithm() {
        return algorithm;
    }

    public byte[] getEncryptedKey() {
        return encryptedKey;
    }
}

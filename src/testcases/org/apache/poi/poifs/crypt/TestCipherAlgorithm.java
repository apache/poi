package org.apache.poi.poifs.crypt;

import static org.junit.Assert.*;

import org.apache.poi.EncryptedDocumentException;
import org.junit.Test;

public class TestCipherAlgorithm {
    @Test
    public void test() {
        assertEquals(128, CipherAlgorithm.aes128.defaultKeySize);
        
        for(CipherAlgorithm alg : CipherAlgorithm.values()) {
            assertEquals(alg, CipherAlgorithm.valueOf(alg.toString()));
        }

        assertEquals(CipherAlgorithm.aes128, CipherAlgorithm.fromEcmaId(0x660E));
        assertEquals(CipherAlgorithm.aes192, CipherAlgorithm.fromXmlId("AES", 192));
        
        try {
            CipherAlgorithm.fromEcmaId(0);
            fail("Should throw exception");
        } catch (EncryptedDocumentException e) {
            // expected
        }

        try {
            CipherAlgorithm.fromXmlId("AES", 1);
            fail("Should throw exception");
        } catch (EncryptedDocumentException e) {
            // expected
        }

        try {
            CipherAlgorithm.fromXmlId("RC1", 0x40);
            fail("Should throw exception");
        } catch (EncryptedDocumentException e) {
            // expected
        }
    }

}

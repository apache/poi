package org.apache.poi.xwpf;

import java.io.File;
import java.io.InputStream;

import junit.framework.TestCase;

import org.apache.poi.POIDataSamples;
import org.apache.poi.hssf.record.crypto.Biff8EncryptionKey;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.poifs.crypt.Decryptor;
import org.apache.poi.poifs.crypt.EncryptionHeader;
import org.apache.poi.poifs.crypt.EncryptionInfo;
import org.apache.poi.poifs.filesystem.NPOIFSFileSystem;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

public class TestXWPFBugs extends TestCase {
	/**
	 * A word document that's encrypted with non-standard
	 *  Encryption options, and no cspname section. See bug 53475
	 */
	public void test53475() throws Exception {
		try {
    		Biff8EncryptionKey.setCurrentUserPassword("solrcell");
    		File file = POIDataSamples.getDocumentInstance().getFile("bug53475-password-is-solrcell.docx");
    		NPOIFSFileSystem filesystem = new NPOIFSFileSystem(file, true);
    		
    		// Check the encryption details
    		EncryptionInfo info = new EncryptionInfo(filesystem);
    		assertEquals(128, info.getHeader().getKeySize());
    		assertEquals(EncryptionHeader.ALGORITHM_AES_128, info.getHeader().getAlgorithm());
    		assertEquals(EncryptionHeader.HASH_SHA1, info.getHeader().getHashAlgorithm());
    		
    		// Check it can be decoded
    		Decryptor d = Decryptor.getInstance(info);		
    		assertTrue("Unable to process: document is encrypted", d.verifyPassword("solrcell"));
    
    		// Check we can read the word document in that
    	    InputStream dataStream = d.getDataStream(filesystem);
    	    OPCPackage opc = OPCPackage.open(dataStream);
    	    XWPFDocument doc = new XWPFDocument(opc);
    	    XWPFWordExtractor ex = new XWPFWordExtractor(doc);
    	    String text = ex.getText();
    	    assertNotNull(text);
    	    assertEquals("This is password protected Word document.", text.trim());
    	    ex.close();
		} finally {
			Biff8EncryptionKey.setCurrentUserPassword(null);
		}
	}
}

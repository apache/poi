package org.apache.poi.xwpf.usermodel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import junit.framework.TestCase;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.xwpf.XWPFTestDataSamples;
import org.apache.xmlbeans.XmlException;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSdtBlock;

public class TestXWPFHeadings extends TestCase{
	private static final String HEADING1 = "Heading1";
	
	public void testSetParagraphStyle() throws IOException, XmlException, InvalidFormatException {
        //new clean instance of paragraph
		XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("heading123.docx");
        XWPFParagraph p = doc.createParagraph();
        XWPFRun run = p.createRun();
        run.setText("Heading 1");
        
        CTSdtBlock block = doc.getDocument().getBody().addNewSdt();
        
        assertNull(p.getStyle());
        p.setStyle(HEADING1);
        assertEquals(HEADING1, p.getCTP().getPPr().getPStyle().getVal());
        
        doc.createTOC();
        
//        CTStyles styles = doc.getStyle();
//        CTStyle style = styles.addNewStyle();
//        style.setType(STStyleType.PARAGRAPH);
//        style.setStyleId("Heading1");
        
        File file = new File("/Users/paolomoz/Desktop/testHeaders.docx");
        OutputStream out = new FileOutputStream(file);
        doc.write(out);
        out.close();
    }

}

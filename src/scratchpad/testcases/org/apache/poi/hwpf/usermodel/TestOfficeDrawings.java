package org.apache.poi.hwpf.usermodel;

import java.io.UnsupportedEncodingException;

import junit.framework.TestCase;

import org.apache.poi.ddf.EscherComplexProperty;
import org.apache.poi.ddf.EscherContainerRecord;
import org.apache.poi.ddf.EscherOptRecord;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.HWPFTestDataSamples;

public class TestOfficeDrawings extends TestCase
{
    public void testWatermark() throws UnsupportedEncodingException
    {
        HWPFDocument hwpfDocument = HWPFTestDataSamples
                .openSampleFile( "watermark.doc" );
        OfficeDrawing drawing = hwpfDocument.getOfficeDrawingsHeaders()
                .getOfficeDrawings().iterator().next();
        EscherContainerRecord escherContainerRecord = drawing
                .getOfficeArtSpContainer();

        EscherOptRecord officeArtFOPT = escherContainerRecord
                .getChildById( (short) 0xF00B );
        EscherComplexProperty gtextUNICODE = (EscherComplexProperty) officeArtFOPT
                .lookup( 0x00c0 );

        String text = new String( gtextUNICODE.getComplexData(), "UTF-16LE" );
        assertEquals( "DRAFT CONTRACT\0", text );
    }
}

package org.apache.poi.hwpf.converter;

import junit.framework.TestCase;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.HWPFTestDataSamples;

public class TestWordToTextConverter extends TestCase
{

    /**
     * [FAILING] Bug 47731 - Word Extractor considers text copied from some
     * website as an embedded object
     */
    public void testBug47731() throws Exception
    {
        HWPFDocument doc = HWPFTestDataSamples.openSampleFile( "Bug47731.doc" );
        String foundText = WordToTextConverter.getText( doc );

        assertTrue( foundText
                .contains( "Soak the rice in water for three to four hours" ) );
    }
}

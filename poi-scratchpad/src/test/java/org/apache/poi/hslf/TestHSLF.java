package org.apache.poi.hslf;

import org.apache.poi.hslf.usermodel.*;
import org.junit.jupiter.api.Test;

import java.io.FileOutputStream;

public class TestHSLF {
    @Test
    public void testHSLF() throws Exception {
        HSLFSlideShow ppt = new HSLFSlideShow();
        HSLFSlide slide = ppt.createSlide();
        //HSLFTextBox title = slide.createTextBox();
        HSLFTextBox title = slide.addTitle();
        title.setText("Hello, World!");
// save changes
        FileOutputStream out = new FileOutputStream("slideshow.ppt");
        ppt.write(out);
        out.close();
    }
}
package org.apache.poi.xslf.usermodel;

import junit.framework.TestCase;

import java.awt.*;

public class TestXSLFSlideLayout extends TestCase {
    public void testInsertPlaceholderInLayout() {
        XMLSlideShow ppt = new XMLSlideShow();
        XSLFSlideMaster slideMaster = ppt.getSlideMasters()[0];
        XSLFSlideLayout layout = slideMaster.createLayout("new Layout");

        assertEquals("new Layout", layout.getName());
        assertEquals(0, layout.getShapes().length);
        assertEquals(0, layout.getPlaceholders().length);

        XSLFAutoShape placeholder = layout.insertPlaceholder(Placeholder.TITLE);
        Rectangle anchor = new Rectangle(50, 50, 500, 100);
        placeholder.setAnchor(anchor);

        assertEquals(1, layout.getShapes().length);
        assertEquals(1, layout.getPlaceholders().length);


        XSLFSlide slide = ppt.createSlide(layout);
        assertEquals(1, slide.getShapes().length);
        assertEquals(1, slide.getPlaceholders().length);

        assertEquals(layout, slide.getSlideLayout());

        XSLFTextShape slidePlaceholder = slide.getPlaceholders()[0];
        assertEquals(anchor, slidePlaceholder.getAnchor());
    }
}

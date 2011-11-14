package org.apache.poi.xslf.usermodel;

import junit.framework.TestCase;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;

/**
 * Created by IntelliJ IDEA.
 * User: yegor
 * Date: Nov 10, 2011
 * Time: 1:43:25 PM
 * To change this template use File | Settings | File Templates.
 */
public class TestXSLFTextParagraph extends TestCase {

    public void testWrappingWidth() throws Exception {
        XMLSlideShow ppt = new XMLSlideShow();
        XSLFSlide slide = ppt.createSlide();
        XSLFTextShape sh = slide.createAutoShape();
        sh.setLineColor(Color.black);

        XSLFTextParagraph p = sh.addNewTextParagraph();
        p.addNewTextRun().setText(
                "Paragraph formatting allows for more granular control " +
                "of text within a shape. Properties here apply to all text " +
                "residing within the corresponding paragraph.");

        Rectangle2D anchor = new Rectangle(50, 50, 300, 200);
        sh.setAnchor(anchor);

        double leftInset = sh.getLeftInset();
        double rightInset = sh.getRightInset();
        assertEquals(7.2, leftInset);
        assertEquals(7.2, rightInset);

        double leftMargin = p.getLeftMargin();
        assertEquals(0.0, leftMargin);

        double indent = p.getIndent();
        assertEquals(0.0, indent); // default

        double expectedWidth;

        // Case 1: bullet=false, leftMargin=0, indent=0.
        expectedWidth = anchor.getWidth() - leftInset - rightInset - leftMargin;
        assertEquals(285.6, expectedWidth); // 300 - 7.2 - 7.2 - 0
        assertEquals(expectedWidth, p.getWrappingWidth(true));
        assertEquals(expectedWidth, p.getWrappingWidth(false));

        p.setLeftMargin(36); // 0.5"
        leftMargin = p.getLeftMargin();
        assertEquals(36.0, leftMargin);
        expectedWidth = anchor.getWidth() - leftInset - rightInset - leftMargin;
        assertEquals(249.6, expectedWidth, 1E-5); // 300 - 7.2 - 7.2 - 36
        assertEquals(expectedWidth, p.getWrappingWidth(true));
        assertEquals(expectedWidth, p.getWrappingWidth(false));

        // increase insets, the wrapping width should get smaller
        sh.setLeftInset(10);
        sh.setRightInset(10);
        leftInset = sh.getLeftInset();
        rightInset = sh.getRightInset();
        assertEquals(10.0, leftInset);
        assertEquals(10.0, rightInset);
        expectedWidth = anchor.getWidth() - leftInset - rightInset - leftMargin;
        assertEquals(244.0, expectedWidth); // 300 - 10 - 10 - 36
        assertEquals(expectedWidth, p.getWrappingWidth(true));
        assertEquals(expectedWidth, p.getWrappingWidth(false));

        // set a positive indent of a 0.5 inch. This means "First Line" indentation:
        // |<---  indent -->|Here goes first line of the text
        // Here go other lines (second and subsequent)

        p.setIndent(36.0);  // 0.5"
        indent = p.getIndent();
        assertEquals(36.0, indent);
        expectedWidth = anchor.getWidth() - leftInset - rightInset - leftMargin - indent;
        assertEquals(208.0, expectedWidth); // 300 - 10 - 10 - 36 - 6.4
        assertEquals(expectedWidth, p.getWrappingWidth(true)); // first line is indented
        // other lines are not indented
        expectedWidth = anchor.getWidth() - leftInset - rightInset - leftMargin;
        assertEquals(244.0, expectedWidth); // 300 - 10 - 10 - 36
        assertEquals(expectedWidth, p.getWrappingWidth(false));

        // set a negative indent of a 1 inch. This means "Hanging" indentation:
        // Here goes first line of the text
        // |<---  indent -->|Here go other lines (second and subsequent)
        p.setIndent(-72.0);  // 1"
        indent = p.getIndent();
        assertEquals(-72.0, indent);
        expectedWidth = anchor.getWidth() - leftInset - rightInset;
        assertEquals(280.0, expectedWidth); // 300 - 10 - 10 
        assertEquals(expectedWidth, p.getWrappingWidth(true)); // first line is NOT indented
        // other lines are indented by leftMargin (the value of indent is not used)
        expectedWidth = anchor.getWidth() - leftInset - rightInset - leftMargin;
        assertEquals(244.0, expectedWidth); // 300 - 10 - 10 - 36 
        assertEquals(expectedWidth, p.getWrappingWidth(false));
     }
}

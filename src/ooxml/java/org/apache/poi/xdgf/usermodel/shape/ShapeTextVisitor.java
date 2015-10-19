package org.apache.poi.xdgf.usermodel.shape;

import java.awt.geom.AffineTransform;

import org.apache.poi.xdgf.usermodel.XDGFShape;

/**
 * Only visits text nodes, accumulates text content into a string
 * 
 * The text is returned in arbitrary order, with no regards to
 * the location of the text on the page. This may change in the
 * future.
 */
public class ShapeTextVisitor extends ShapeVisitor {

    protected StringBuilder text = new StringBuilder();
    
    public static class TextAcceptor implements ShapeVisitorAcceptor {
        public boolean accept(XDGFShape shape) {
            return shape.hasText();
        }
    }
    
    protected ShapeVisitorAcceptor getAcceptor() {
        return new TextAcceptor();
    }

    public void visit(XDGFShape shape, AffineTransform globalTransform,
            int level) {
        text.append(shape.getText().getTextContent().trim());
        text.append('\n');
    }

    /**
     * Call this after visitation has completed
     */
    public String getText() {
        return text.toString();
    }

}

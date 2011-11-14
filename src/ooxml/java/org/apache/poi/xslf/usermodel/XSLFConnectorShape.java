/*
 *  ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ====================================================================
 */

package org.apache.poi.xslf.usermodel;

import org.apache.poi.util.Beta;
import org.apache.poi.xslf.model.geom.Outline;
import org.apache.poi.xslf.model.geom.Path;
import org.openxmlformats.schemas.drawingml.x2006.main.CTLineEndProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTLineProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTNonVisualDrawingProps;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPresetGeometry2D;
import org.openxmlformats.schemas.drawingml.x2006.main.CTShapeProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.STLineEndLength;
import org.openxmlformats.schemas.drawingml.x2006.main.STLineEndType;
import org.openxmlformats.schemas.drawingml.x2006.main.STLineEndWidth;
import org.openxmlformats.schemas.drawingml.x2006.main.STShapeType;
import org.openxmlformats.schemas.presentationml.x2006.main.CTConnector;
import org.openxmlformats.schemas.presentationml.x2006.main.CTConnectorNonVisual;

import java.awt.Shape;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Specifies a connection shape.
 *
 * @author Yegor Kozlov
 */
@Beta
public class XSLFConnectorShape extends XSLFSimpleShape {

    /*package*/ XSLFConnectorShape(CTConnector shape, XSLFSheet sheet) {
        super(shape, sheet);
    }

    /**
     * @param shapeId 1-based shapeId
     */
    static CTConnector prototype(int shapeId) {
        CTConnector ct = CTConnector.Factory.newInstance();
        CTConnectorNonVisual nvSpPr = ct.addNewNvCxnSpPr();
        CTNonVisualDrawingProps cnv = nvSpPr.addNewCNvPr();
        cnv.setName("Connector " + shapeId);
        cnv.setId(shapeId + 1);
        nvSpPr.addNewCNvCxnSpPr();
        nvSpPr.addNewNvPr();
        CTShapeProperties spPr = ct.addNewSpPr();
        CTPresetGeometry2D prst = spPr.addNewPrstGeom();
        prst.setPrst(STShapeType.LINE);
        prst.addNewAvLst();
        CTLineProperties ln = spPr.addNewLn();
        return ct;
    }

    /**
     * Specifies the line end decoration, such as a triangle or arrowhead.
     */
    public void setLineHeadDecoration(LineDecoration style) {
        CTLineProperties ln = getSpPr().getLn();
        CTLineEndProperties lnEnd = ln.isSetHeadEnd() ? ln.getHeadEnd() : ln.addNewHeadEnd();
        if (style == null) {
            if (lnEnd.isSetType()) lnEnd.unsetType();
        } else {
            lnEnd.setType(STLineEndType.Enum.forInt(style.ordinal() + 1));
        }
    }

    public LineDecoration getLineHeadDecoration() {
        CTLineProperties ln = getSpPr().getLn();
        if (ln == null || !ln.isSetHeadEnd()) return LineDecoration.NONE;

        STLineEndType.Enum end = ln.getHeadEnd().getType();
        return end == null ? LineDecoration.NONE : LineDecoration.values()[end.intValue() - 1];
    }

    /**
     * specifies decorations which can be added to the head of a line.
     */
    public void setLineHeadWidth(LineEndWidth style) {
        CTLineProperties ln = getSpPr().getLn();
        CTLineEndProperties lnEnd = ln.isSetHeadEnd() ? ln.getHeadEnd() : ln.addNewHeadEnd();
        if (style == null) {
            if (lnEnd.isSetW()) lnEnd.unsetW();
        } else {
            lnEnd.setW(STLineEndWidth.Enum.forInt(style.ordinal() + 1));
        }
    }

    public LineEndWidth getLineHeadWidth() {
        CTLineProperties ln = getSpPr().getLn();
        if (ln == null || !ln.isSetHeadEnd()) return LineEndWidth.MEDIUM;

        STLineEndWidth.Enum w = ln.getHeadEnd().getW();
        return w == null ? LineEndWidth.MEDIUM : LineEndWidth.values()[w.intValue() - 1];
    }

    /**
     * Specifies the line end width in relation to the line width.
     */
    public void setLineHeadLength(LineEndLength style) {
        CTLineProperties ln = getSpPr().getLn();
        CTLineEndProperties lnEnd = ln.isSetHeadEnd() ? ln.getHeadEnd() : ln.addNewHeadEnd();

        if (style == null) {
            if (lnEnd.isSetLen()) lnEnd.unsetLen();
        } else {
            lnEnd.setLen(STLineEndLength.Enum.forInt(style.ordinal() + 1));
        }
    }

    public LineEndLength getLineHeadLength() {
        CTLineProperties ln = getSpPr().getLn();
        if (ln == null || !ln.isSetHeadEnd()) return LineEndLength.MEDIUM;

        STLineEndLength.Enum len = ln.getHeadEnd().getLen();
        return len == null ? LineEndLength.MEDIUM : LineEndLength.values()[len.intValue() - 1];
    }

    /**
     * Specifies the line end decoration, such as a triangle or arrowhead.
     */
    public void setLineTailDecoration(LineDecoration style) {
        CTLineProperties ln = getSpPr().getLn();
        CTLineEndProperties lnEnd = ln.isSetTailEnd() ? ln.getTailEnd() : ln.addNewTailEnd();
        if (style == null) {
            if (lnEnd.isSetType()) lnEnd.unsetType();
        } else {
            lnEnd.setType(STLineEndType.Enum.forInt(style.ordinal() + 1));
        }
    }

    public LineDecoration getLineTailDecoration() {
        CTLineProperties ln = getSpPr().getLn();
        if (ln == null || !ln.isSetTailEnd()) return LineDecoration.NONE;

        STLineEndType.Enum end = ln.getTailEnd().getType();
        return end == null ? LineDecoration.NONE : LineDecoration.values()[end.intValue() - 1];
    }

    /**
     * specifies decorations which can be added to the tail of a line.
     */
    public void setLineTailWidth(LineEndWidth style) {
        CTLineProperties ln = getSpPr().getLn();
        CTLineEndProperties lnEnd = ln.isSetTailEnd() ? ln.getTailEnd() : ln.addNewTailEnd();
        if (style == null) {
            if (lnEnd.isSetW()) lnEnd.unsetW();
        } else {
            lnEnd.setW(STLineEndWidth.Enum.forInt(style.ordinal() + 1));
        }
    }

    public LineEndWidth getLineTailWidth() {
        CTLineProperties ln = getSpPr().getLn();
        if (ln == null || !ln.isSetTailEnd()) return LineEndWidth.MEDIUM;

        STLineEndWidth.Enum w = ln.getTailEnd().getW();
        return w == null ? LineEndWidth.MEDIUM : LineEndWidth.values()[w.intValue() - 1];
    }

    /**
     * Specifies the line end width in relation to the line width.
     */
    public void setLineTailLength(LineEndLength style) {
        CTLineProperties ln = getSpPr().getLn();
        CTLineEndProperties lnEnd = ln.isSetTailEnd() ? ln.getTailEnd() : ln.addNewTailEnd();

        if (style == null) {
            if (lnEnd.isSetLen()) lnEnd.unsetLen();
        } else {
            lnEnd.setLen(STLineEndLength.Enum.forInt(style.ordinal() + 1));
        }
    }

    public LineEndLength getLineTailLength() {
        CTLineProperties ln = getSpPr().getLn();
        if (ln == null || !ln.isSetTailEnd()) return LineEndLength.MEDIUM;

        STLineEndLength.Enum len = ln.getTailEnd().getLen();
        return len == null ? LineEndLength.MEDIUM : LineEndLength.values()[len.intValue() - 1];
    }

    Outline getTailDecoration() {
        LineEndLength tailLength = getLineTailLength();
        LineEndWidth tailWidth = getLineTailWidth();

        double lineWidth = Math.max(2.5, getLineWidth());

        Rectangle2D anchor = getAnchor();
        double x2 = anchor.getX() + anchor.getWidth(),
                y2 = anchor.getY() + anchor.getHeight();

        double alpha = Math.atan(anchor.getHeight() / anchor.getWidth());

        AffineTransform at = new AffineTransform();
        Shape shape = null;
        Path p = null;
        Rectangle2D bounds;
        double scaleY = Math.pow(2, tailWidth.ordinal());
        double scaleX = Math.pow(2, tailLength.ordinal());
        switch (getLineTailDecoration()) {
            case OVAL:
                p = new Path();
                shape = new Ellipse2D.Double(0, 0, lineWidth * scaleX, lineWidth * scaleY);
                bounds = shape.getBounds2D();
                at.translate(x2 - bounds.getWidth() / 2, y2 - bounds.getHeight() / 2);
                at.rotate(alpha, bounds.getX() + bounds.getWidth() / 2, bounds.getY() + bounds.getHeight() / 2);
                break;
            case ARROW:
                p = new Path();
                GeneralPath arrow = new GeneralPath();
                arrow.moveTo((float) (-lineWidth * 3), (float) (-lineWidth * 2));
                arrow.lineTo(0, 0);
                arrow.lineTo((float) (-lineWidth * 3), (float) (lineWidth * 2));
                shape = arrow;
                at.translate(x2, y2);
                at.rotate(alpha);
                break;
            case TRIANGLE:
                p = new Path();
                scaleY = tailWidth.ordinal() + 1;
                scaleX = tailLength.ordinal() + 1;
                GeneralPath triangle = new GeneralPath();
                triangle.moveTo((float) (-lineWidth * scaleX), (float) (-lineWidth * scaleY / 2));
                triangle.lineTo(0, 0);
                triangle.lineTo((float) (-lineWidth * scaleX), (float) (lineWidth * scaleY / 2));
                triangle.closePath();
                shape = triangle;
                at.translate(x2, y2);
                at.rotate(alpha);
                break;
            default:
                break;
        }

        if (shape != null) {
            shape = at.createTransformedShape(shape);
        }
        return shape == null ? null : new Outline(shape, p);
    }

    Outline getHeadDecoration() {
        LineEndLength headLength = getLineHeadLength();
        LineEndWidth headWidth = getLineHeadWidth();

        double lineWidth = Math.max(2.5, getLineWidth());
        Rectangle2D anchor = getAnchor();
        double x1 = anchor.getX(),
                y1 = anchor.getY();

        double alpha = Math.atan(anchor.getHeight() / anchor.getWidth());

        AffineTransform at = new AffineTransform();
        Shape shape = null;
        Path p = null;
        Rectangle2D bounds;
        double scaleY = 1;
        double scaleX = 1;
        switch (getLineHeadDecoration()) {
            case OVAL:
                p = new Path();
                shape = new Ellipse2D.Double(0, 0, lineWidth * scaleX, lineWidth * scaleY);
                bounds = shape.getBounds2D();
                at.translate(x1 - bounds.getWidth() / 2, y1 - bounds.getHeight() / 2);
                at.rotate(alpha, bounds.getX() + bounds.getWidth() / 2, bounds.getY() + bounds.getHeight() / 2);
                break;
            case STEALTH:
            case ARROW:
                p = new Path(false, true);
                GeneralPath arrow = new GeneralPath();
                arrow.moveTo((float) (lineWidth * 3 * scaleX), (float) (-lineWidth * scaleY * 2));
                arrow.lineTo(0, 0);
                arrow.lineTo((float) (lineWidth * 3 * scaleX), (float) (lineWidth * scaleY * 2));
                shape = arrow;
                at.translate(x1, y1);
                at.rotate(alpha);
                break;
            case TRIANGLE:
                p = new Path();
                scaleY = headWidth.ordinal() + 1;
                scaleX = headLength.ordinal() + 1;
                GeneralPath triangle = new GeneralPath();
                triangle.moveTo((float) (lineWidth * scaleX), (float) (-lineWidth * scaleY / 2));
                triangle.lineTo(0, 0);
                triangle.lineTo((float) (lineWidth * scaleX), (float) (lineWidth * scaleY / 2));
                triangle.closePath();
                shape = triangle;
                at.translate(x1, y1);
                at.rotate(alpha);
                break;
            default:
                break;
        }

        if (shape != null) {
            shape = at.createTransformedShape(shape);
        }
        return shape == null ? null : new Outline(shape, p);
    }

    private List<Outline> getDecorationOutlines(){
        List<Outline> lst = new ArrayList<Outline>();

        Outline head = getHeadDecoration();
        if(head != null) lst.add(head);

        Outline tail = getTailDecoration();
        if(tail != null) lst.add(tail);
        return lst;
    }

    /**
     * YK: dashing of lines is suppressed for now.
     * @return
     */
    @Override
    public XSLFShadow getShadow() {
        return null;
    }

    @Override
    public void draw(Graphics2D graphics){
        super.draw(graphics);

        // draw line decorations
        Color lineColor = getLineColor();
        if(lineColor != null) {
            graphics.setPaint(lineColor);
            for(Outline o : getDecorationOutlines()){
                if(o.getPath().isFilled()){
                    graphics.fill(o.getOutline());
                }            
                if(o.getPath().isStroked()){
                    graphics.draw(o.getOutline());
                }
            }
        }
    }
}
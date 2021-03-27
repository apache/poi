/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package org.apache.poi.xdgf.usermodel;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;

import com.microsoft.schemas.office.visio.x2012.main.ShapeSheetType;
import com.microsoft.schemas.office.visio.x2012.main.TextType;
import org.apache.poi.ooxml.POIXMLException;
import org.apache.poi.util.Internal;
import org.apache.poi.xdgf.exceptions.XDGFException;
import org.apache.poi.xdgf.usermodel.section.CombinedIterable;
import org.apache.poi.xdgf.usermodel.section.GeometrySection;
import org.apache.poi.xdgf.usermodel.section.XDGFSection;
import org.apache.poi.xdgf.usermodel.shape.ShapeVisitor;
import org.apache.poi.xdgf.usermodel.shape.exceptions.StopVisitingThisBranch;

/**
 * A shape is a collection of Geometry Visualization, Format, Text, Images, and
 * Shape Data in a Drawing Page.
 */
public class XDGFShape extends XDGFSheet {

    XDGFBaseContents _parentPage;
    XDGFShape _parent; // only non-null if a subshape

    XDGFMaster _master;
    XDGFShape _masterShape;

    XDGFText _text;

    // subshapes if they exist
    List<XDGFShape> _shapes;

    // properties specific to shapes

    // center of rotation relative to origin of parent
    Double _pinX;
    Double _pinY;

    Double _width;
    Double _height;

    // center of rotation relative to self
    Double _locPinX;
    Double _locPinY;

    // start x coordinate, relative to parent
    // -> one dimensional shapes only
    Double _beginX;
    Double _beginY;

    // end x coordinate, relative to parent
    // -> one dimensional shapes only
    Double _endX;
    Double _endY;

    Double _angle;
    Double _rotationXAngle;
    Double _rotationYAngle;
    Double _rotationZAngle;

    // end x coordinate, relative to parent
    Boolean _flipX;
    Boolean _flipY;

    // center of text relative to this shape
    Double _txtPinX;
    Double _txtPinY;

    // center of text relative to text block
    Double _txtLocPinX;
    Double _txtLocPinY;

    Double _txtAngle;

    Double _txtWidth;
    Double _txtHeight;

    public XDGFShape(ShapeSheetType shapeSheet, XDGFBaseContents parentPage,
            XDGFDocument document) {
        this(null, shapeSheet, parentPage, document);
    }

    public XDGFShape(XDGFShape parent, ShapeSheetType shapeSheet,
            XDGFBaseContents parentPage, XDGFDocument document) {

        super(shapeSheet, document);

        _parent = parent;
        _parentPage = parentPage;

        TextType text = shapeSheet.getText();
        if (text != null) {
            _text = new XDGFText(text, this);
        }

        if (shapeSheet.isSetShapes()) {
            _shapes = new ArrayList<>();
            for (ShapeSheetType shape : shapeSheet.getShapes().getShapeArray()) {
                _shapes.add(new XDGFShape(this, shape, parentPage, document));
            }
        }

        readProperties();
    }

    @Override
    public String toString() {
        if (_parentPage instanceof XDGFMasterContents) {
            return _parentPage + ": <Shape ID=\"" + getID() + "\">";
        } else {
            return "<Shape ID=\"" + getID() + "\">";
        }
    }

    protected void readProperties() {

        _pinX = XDGFCell.maybeGetDouble(_cells, "PinX");
        _pinY = XDGFCell.maybeGetDouble(_cells, "PinY");
        _width = XDGFCell.maybeGetDouble(_cells, "Width");
        _height = XDGFCell.maybeGetDouble(_cells, "Height");
        _locPinX = XDGFCell.maybeGetDouble(_cells, "LocPinX");
        _locPinY = XDGFCell.maybeGetDouble(_cells, "LocPinY");
        _beginX = XDGFCell.maybeGetDouble(_cells, "BeginX");
        _beginY = XDGFCell.maybeGetDouble(_cells, "BeginY");
        _endX = XDGFCell.maybeGetDouble(_cells, "EndX");
        _endY = XDGFCell.maybeGetDouble(_cells, "EndY");

        _angle = XDGFCell.maybeGetDouble(_cells, "Angle");
        _rotationXAngle = XDGFCell.maybeGetDouble(_cells, "RotationXAngle");
        _rotationYAngle = XDGFCell.maybeGetDouble(_cells, "RotationYAngle");
        _rotationZAngle = XDGFCell.maybeGetDouble(_cells, "RotationZAngle");

        _flipX = XDGFCell.maybeGetBoolean(_cells, "FlipX");
        _flipY = XDGFCell.maybeGetBoolean(_cells, "FlipY");

        _txtPinX = XDGFCell.maybeGetDouble(_cells, "TxtPinX");
        _txtPinY = XDGFCell.maybeGetDouble(_cells, "TxtPinY");
        _txtLocPinX = XDGFCell.maybeGetDouble(_cells, "TxtLocPinX");
        _txtLocPinY = XDGFCell.maybeGetDouble(_cells, "TxtLocPinY");
        _txtWidth = XDGFCell.maybeGetDouble(_cells, "TxtWidth");
        _txtHeight = XDGFCell.maybeGetDouble(_cells, "TxtHeight");

        _txtAngle = XDGFCell.maybeGetDouble(_cells, "TxtAngle");
    }

    /**
     * Setup top level shapes
     *
     * Shapes that have a 'Master' attribute refer to a specific master in the
     * page, whereas shapes with a 'MasterShape' attribute refer to a subshape
     * of a Master.
     */
    protected void setupMaster(XDGFPageContents pageContents,
            XDGFMasterContents master) {

        ShapeSheetType obj = getXmlObject();

        if (obj.isSetMaster()) {
            _master = pageContents.getMasterById(obj.getMaster());
            if (_master == null) {
                throw XDGFException.error("refers to non-existant master "
                        + obj.getMaster(), this);
            }

            /*
             * If a master has one top-level shape, a shape that inherits from
             * that master inherits the descendant elements of that master
             * shape. If a master has more than one master shape, a shape that
             * inherits from that master inherits those master shapes as
             * subshapes.
             */

            Collection<XDGFShape> masterShapes = _master.getContent()
                    .getTopLevelShapes();

            switch (masterShapes.size()) {
            case 0:
                throw XDGFException
                .error("Could not retrieve master shape from "
                        + _master, this);
            case 1:
                _masterShape = masterShapes.iterator().next();
                break;
            default:
                break;
            }

        } else if (obj.isSetMasterShape()) {
            _masterShape = (master == null) ? null : master.getShapeById(obj.getMasterShape());
            if (_masterShape == null) {
                throw XDGFException.error(
                        "refers to non-existant master shape "
                                + obj.getMasterShape(), this);
            }

        }

        setupSectionMasters();

        if (_shapes != null) {
            for (XDGFShape shape : _shapes) {
                shape.setupMaster(pageContents, _master == null ? master
                        : _master.getContent());
            }
        }
    }

    protected void setupSectionMasters() {

        if (_masterShape == null) {
            return;
        }

        try {
            for (Entry<String, XDGFSection> section : _sections.entrySet()) {
                XDGFSection master = _masterShape.getSection(section.getKey());
                if (master != null) {
                    section.getValue().setupMaster(master);
                }
            }

            for (Entry<Long, GeometrySection> section : _geometry.entrySet()) {
                GeometrySection master = _masterShape.getGeometryByIdx(section
                        .getKey());
                if (master != null) {
                    section.getValue().setupMaster(master);
                }
            }
        } catch (POIXMLException e) {
            throw XDGFException.wrap(this.toString(), e);
        }
    }

    @Override
    @Internal
    public ShapeSheetType getXmlObject() {
        return (ShapeSheetType) _sheet;
    }

    public long getID() {
        return getXmlObject().getID();
    }

    public String getType() {
        return getXmlObject().getType();
    }

    public String getTextAsString() {
        XDGFText text = getText();
        if (text == null) {
            return "";
        }

        return text.getTextContent();
    }

    public boolean hasText() {
        return _text != null
                || (_masterShape != null && _masterShape._text != null);
    }

    @Override
    public XDGFCell getCell(String cellName) {
        XDGFCell _cell = super.getCell(cellName);

        // if not found, ask the master
        if (_cell == null && _masterShape != null) {
            _cell = _masterShape.getCell(cellName);
        }

        return _cell;
    }

    public GeometrySection getGeometryByIdx(long idx) {
        return _geometry.get(idx);
    }

    /**
     * Only available if this shape is a shape group, may be null
     */
    // -> May be null
    public List<XDGFShape> getShapes() {
        return _shapes;
    }

    // unique to this shape on the page?
    public String getName() {
        String name = getXmlObject().getName();
        if (name == null) {
            return "";
        }
        return name;
    }

    // unique to this shape on the page?
    public String getShapeType() {
        String type = getXmlObject().getType();
        if (type == null) {
            return "";
        }
        return type;
    }

    // name of the symbol that this was derived from
    public String getSymbolName() {

        if (_master == null) {
            return "";
        }

        String name = _master.getName();
        if (name == null) {
            return "";
        }

        return name;
    }

    public XDGFShape getMasterShape() {
        return _masterShape;
    }

    /**
     * @return The parent shape if this is a subshape, null otherwise
     */
    public XDGFShape getParentShape() {
        return _parent;
    }

    public XDGFShape getTopmostParentShape() {
        XDGFShape top = null;
        if (_parent != null) {
            top = _parent.getTopmostParentShape();
            if (top == null) {
                top = _parent;
            }
        }

        return top;
    }

    public boolean hasMaster() {
        return _master != null;
    }

    public boolean hasMasterShape() {
        return _masterShape != null;
    }

    public boolean hasParent() {
        return _parent != null;
    }

    public boolean hasShapes() {
        return _shapes != null;
    }

    public boolean isTopmost() {
        return _parent == null;
    }

    public boolean isShape1D() {
        return getBeginX() != null;
    }

    public boolean isDeleted() {
        return getXmlObject().isSetDel() ? getXmlObject().getDel() : false;
    }

    public XDGFText getText() {
        if (_text == null && _masterShape != null) {
            return _masterShape.getText();
        }

        return _text;
    }

    public Double getPinX() {
        if (_pinX == null && _masterShape != null) {
            return _masterShape.getPinX();
        }

        if (_pinX == null) {
            throw XDGFException.error("PinX not set!", this);
        }

        return _pinX;
    }

    public Double getPinY() {
        if (_pinY == null && _masterShape != null) {
            return _masterShape.getPinY();
        }

        if (_pinY == null) {
            throw XDGFException.error("PinY not specified!", this);
        }

        return _pinY;
    }

    public Double getWidth() {
        if (_width == null && _masterShape != null) {
            return _masterShape.getWidth();
        }

        if (_width == null) {
            throw XDGFException.error("Width not specified!", this);
        }

        return _width;
    }

    public Double getHeight() {
        if (_height == null && _masterShape != null) {
            return _masterShape.getHeight();
        }

        if (_height == null) {
            throw XDGFException.error("Height not specified!", this);
        }

        return _height;
    }

    public Double getLocPinX() {
        if (_locPinX == null && _masterShape != null) {
            return _masterShape.getLocPinX();
        }

        if (_locPinX == null) {
            throw XDGFException.error("LocPinX not specified!", this);
        }

        return _locPinX;
    }

    public Double getLocPinY() {
        if (_locPinY == null && _masterShape != null) {
            return _masterShape.getLocPinY();
        }

        if (_locPinY == null) {
            throw XDGFException.error("LocPinY not specified!", this);
        }

        return _locPinY;
    }

    public Double getBeginX() {
        if (_beginX == null && _masterShape != null) {
            return _masterShape.getBeginX();
        }

        return _beginX;
    }

    public Double getBeginY() {
        if (_beginY == null && _masterShape != null) {
            return _masterShape.getBeginY();
        }

        return _beginY;
    }

    public Double getEndX() {
        if (_endX == null && _masterShape != null) {
            return _masterShape.getEndX();
        }

        return _endX;
    }

    public Double getEndY() {
        if (_endY == null && _masterShape != null) {
            return _masterShape.getEndY();
        }

        return _endY;
    }

    public Double getAngle() {
        if (_angle == null && _masterShape != null) {
            return _masterShape.getAngle();
        }

        return _angle;
    }

    public Boolean getFlipX() {
        if (_flipX == null && _masterShape != null) {
            return _masterShape.getFlipX();
        }

        return _flipX;
    }

    public Boolean getFlipY() {
        if (_flipY == null && _masterShape != null) {
            return _masterShape.getFlipY();
        }

        return _flipY;
    }

    public Double getTxtPinX() {
        if (_txtPinX == null && _masterShape != null
                && _masterShape._txtPinX != null) {
            return _masterShape._txtPinX;
        }

        if (_txtPinX == null) {
            return getWidth() * 0.5;
        }

        return _txtPinX;
    }

    public Double getTxtPinY() {
        if (_txtLocPinY == null && _masterShape != null
                && _masterShape._txtLocPinY != null) {
            return _masterShape._txtLocPinY;
        }

        if (_txtPinY == null) {
            return getHeight() * 0.5;
        }

        return _txtPinY;
    }

    public Double getTxtLocPinX() {
        if (_txtLocPinX == null && _masterShape != null
                && _masterShape._txtLocPinX != null) {
            return _masterShape._txtLocPinX;
        }

        if (_txtLocPinX == null) {
            return getTxtWidth() * 0.5;
        }

        return _txtLocPinX;
    }

    public Double getTxtLocPinY() {
        if (_txtLocPinY == null && _masterShape != null
                && _masterShape._txtLocPinY != null) {
            return _masterShape._txtLocPinY;
        }

        if (_txtLocPinY == null) {
            return getTxtHeight() * 0.5;
        }

        return _txtLocPinY;
    }

    public Double getTxtAngle() {
        if (_txtAngle == null && _masterShape != null) {
            return _masterShape.getTxtAngle();
        }

        return _txtAngle;
    }

    public Double getTxtWidth() {
        if (_txtWidth == null && _masterShape != null
                && _masterShape._txtWidth != null) {
            return _masterShape._txtWidth;
        }

        if (_txtWidth == null) {
            return getWidth();
        }

        return _txtWidth;
    }

    public Double getTxtHeight() {
        if (_txtHeight == null && _masterShape != null
                && _masterShape._txtHeight != null) {
            return _masterShape._txtHeight;
        }

        if (_txtHeight == null) {
            return getHeight();
        }

        return _txtHeight;
    }

    @Override
    public Integer getLineCap() {

        Integer lineCap = super.getLineCap();
        if (lineCap != null) {
            return lineCap;
        }

        // get from master
        if (_masterShape != null) {
            return _masterShape.getLineCap();
        }

        // get default
        return _document.getDefaultLineStyle().getLineCap();
    }

    @Override
    public Color getLineColor() {

        Color lineColor = super.getLineColor();
        if (lineColor != null) {
            return lineColor;
        }

        // get from master
        if (_masterShape != null) {
            return _masterShape.getLineColor();
        }

        // get default
        return _document.getDefaultLineStyle().getLineColor();
    }

    @Override
    public Integer getLinePattern() {

        Integer linePattern = super.getLinePattern();
        if (linePattern != null) {
            return linePattern;
        }

        // get from master
        if (_masterShape != null) {
            return _masterShape.getLinePattern();
        }

        // get default
        return _document.getDefaultLineStyle().getLinePattern();
    }

    @Override
    public Double getLineWeight() {

        Double lineWeight = super.getLineWeight();
        if (lineWeight != null) {
            return lineWeight;
        }

        // get from master
        if (_masterShape != null) {
            return _masterShape.getLineWeight();
        }

        // get default
        return _document.getDefaultLineStyle().getLineWeight();
    }

    @Override
    public Color getFontColor() {

        Color fontColor = super.getFontColor();
        if (fontColor != null) {
            return fontColor;
        }

        // get from master
        if (_masterShape != null) {
            return _masterShape.getFontColor();
        }

        // get default
        return _document.getDefaultTextStyle().getFontColor();
    }

    @Override
    public Double getFontSize() {

        Double fontSize = super.getFontSize();
        if (fontSize != null) {
            return fontSize;
        }

        // get from master
        if (_masterShape != null) {
            return _masterShape.getFontSize();
        }

        // get default
        return _document.getDefaultTextStyle().getFontSize();
    }

    public Stroke getStroke() {

        float lineWeight = getLineWeight().floatValue();
        int cap;
        int join = BasicStroke.JOIN_MITER;

        switch (getLineCap()) {
        case 0:
            cap = BasicStroke.CAP_ROUND;
            break;
        case 1:
            cap = BasicStroke.CAP_SQUARE;
            break;
        case 2:
            cap = BasicStroke.CAP_BUTT; // TODO: what does extended mean?
            break;
        default:
            throw new POIXMLException("Invalid line cap specified");
        }

        float[] dash = null;

        // these line patterns are just approximations
        switch (getLinePattern()) {
        case 0: // transparent
            break;
        case 1: // solid
            break;
        case 2:
            dash = new float[] { 5, 3 };
            break;
        case 3:
            dash = new float[] { 1, 4 };
            break;
        case 4:
            dash = new float[] { 6, 3, 1, 3 };
            break;
        case 5:
            dash = new float[] { 6, 3, 1, 3, 1, 3 };
            break;
        case 6:
            dash = new float[] { 1, 3, 6, 3, 6, 3 };
            break;
        case 7:
            dash = new float[] { 15, 3, 6, 3 };
            break;
        case 8:
            dash = new float[] { 6, 3, 6, 3 };
            break;
        case 9:
            dash = new float[] { 3, 2 };
            break;
        case 10:
            dash = new float[] { 1, 2 };
            break;
        case 11:
            dash = new float[] { 3, 2, 1, 2 };
            break;
        case 12:
            dash = new float[] { 3, 2, 1, 2, 1 };
            break;
        case 13:
            dash = new float[] { 1, 2, 3, 2, 3, 2 };
            break;
        case 14:
            dash = new float[] { 3, 2, 7, 2 };
            break;
        case 15:
            dash = new float[] { 7, 2, 3, 2, 3, 2 };
            break;
        case 16:
            dash = new float[] { 12, 6 };
            break;
        case 17:
            dash = new float[] { 1, 6 };
            break;
        case 18:
            dash = new float[] { 1, 6, 12, 6 };
            break;
        case 19:
            dash = new float[] { 1, 6, 1, 6, 12, 6 };
            break;
        case 20:
            dash = new float[] { 1, 6, 12, 6, 12, 6 };
            break;
        case 21:
            dash = new float[] { 30, 6, 12, 6 };
            break;
        case 22:
            dash = new float[] { 30, 6, 12, 6, 12, 6 };
            break;
        case 23:
            dash = new float[] { 1 };
            break;
        case 254:
            throw new POIXMLException("Unsupported line pattern value");
        default:
            throw new POIXMLException("Invalid line pattern value");
        }

        // dashes are in units of line width
        if (dash != null) {
            for (int i = 0; i < dash.length; i++) {
                dash[i] *= lineWeight;
            }
        }

        return new BasicStroke(lineWeight, cap, join, 10, dash, 0);
    }

    //
    // Geometry
    //

    public Iterable<GeometrySection> getGeometrySections() {
        return new CombinedIterable<>(_geometry,
                _masterShape != null ? _masterShape._geometry : null);
    }

    /**
     * @return rectangle in local coordinates
     */
    public Rectangle2D.Double getBounds() {
        return new Rectangle2D.Double(0, 0, getWidth(), getHeight());
    }

    /**
     * @return returns bounds as a path in local coordinates, which is
     *         userful if you need to transform to global coordinates
     *
     * Warning: Don't use this for 1d objects, and will fail for
     *          infinite line objects
     */
    public Path2D.Double getBoundsAsPath() {

        Double w = getWidth();
        Double h = getHeight();

        Path2D.Double bounds = new Path2D.Double();
        bounds.moveTo(0, 0);
        bounds.lineTo(w, 0);
        bounds.lineTo(w, h);
        bounds.lineTo(0, h);
        bounds.lineTo(0, 0);

        return bounds;
    }

    /**
     * @return The outline of the shape in local coordinates
     */
    public Path2D.Double getPath() {
        for (GeometrySection geoSection : getGeometrySections()) {
            if (geoSection.getNoShow()) {
                continue;
            }

            return geoSection.getPath(this);
        }

        return null;
    }

    /*
     * Returns true if the shape has a drawable geometry associated with it
     */
    public boolean hasGeometry() {
        for (GeometrySection geoSection : getGeometrySections()) {
            if (!geoSection.getNoShow()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns a transform that can translate shape-local coordinates to the
     * coordinates of its parent shape
     */
    protected AffineTransform getParentTransform() {
        // TODO: There's probably a better way to do this
        AffineTransform tr = new AffineTransform();

        Double locX = getLocPinX();
        Double locY = getLocPinY();
        Boolean flipX = getFlipX();
        Boolean flipY = getFlipY();
        Double angle = getAngle();

        tr.translate(-locX, -locY);

        tr.translate(getPinX(), getPinY());

        // rotate about the origin
        if (angle != null && Math.abs(angle) > 0.001) {
            tr.rotate(angle, locX, locY);
        }

        // flip if necessary

        if (flipX != null && flipX) {
            tr.scale(-1, 1);
            tr.translate(-getWidth(), 0);
        }

        if (flipY != null && flipY) {
            tr.scale(1, -1);
            tr.translate(0, -getHeight());
        }

        return tr;
    }

    /**
     * The visitor will first visit this shape, then it's children
     *
     * This is useful because exceptions will be marked with the shapes as it
     * propagates up the shape hierarchy.
     */
    public void visitShapes(ShapeVisitor visitor, AffineTransform tr, int level) {

        tr = (AffineTransform) tr.clone();
        tr.concatenate(getParentTransform());

        try {
            if (visitor.accept(this)) {
                visitor.visit(this, tr, level);
            }

            if (_shapes != null) {
                for (XDGFShape shape : _shapes) {
                    shape.visitShapes(visitor, tr, level + 1);
                }
            }
        } catch (StopVisitingThisBranch e) {
            // intentionally empty
        } catch (POIXMLException e) {
            throw XDGFException.wrap(this.toString(), e);
        }
    }

    /**
     * The visitor will first visit this shape, then it's children. No transform
     * is calculated for this visit
     *
     * This is useful because exceptions will be marked with the shapes as it
     * propagates up the shape hierarchy.
     */
    public void visitShapes(ShapeVisitor visitor, int level) {

        try {
            if (visitor.accept(this)) {
                visitor.visit(this, null, level);
            }

            if (_shapes != null) {
                for (XDGFShape shape : _shapes) {
                    shape.visitShapes(visitor, level + 1);
                }
            }
        } catch (StopVisitingThisBranch e) {
            // intentionally empty
        } catch (POIXMLException e) {
            throw XDGFException.wrap(this.toString(), e);
        }
    }

}

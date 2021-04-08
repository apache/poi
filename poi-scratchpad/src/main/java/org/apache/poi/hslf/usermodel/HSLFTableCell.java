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

package org.apache.poi.hslf.usermodel;

import java.awt.Color;
import java.awt.geom.Rectangle2D;

import org.apache.poi.ddf.AbstractEscherOptRecord;
import org.apache.poi.ddf.EscherContainerRecord;
import org.apache.poi.ddf.EscherPropertyTypes;
import org.apache.poi.sl.draw.DrawPaint;
import org.apache.poi.sl.usermodel.PaintStyle;
import org.apache.poi.sl.usermodel.ShapeType;
import org.apache.poi.sl.usermodel.StrokeStyle;
import org.apache.poi.sl.usermodel.StrokeStyle.LineCompound;
import org.apache.poi.sl.usermodel.StrokeStyle.LineDash;
import org.apache.poi.sl.usermodel.TableCell;

/**
 * Represents a cell in a ppt table
 */
public final class HSLFTableCell extends HSLFTextBox implements TableCell<HSLFShape,HSLFTextParagraph> {
    protected static final int DEFAULT_WIDTH = 100;
    protected static final int DEFAULT_HEIGHT = 40;

    /* package */ HSLFLine borderLeft;
    /* package */ HSLFLine borderRight;
    /* package */ HSLFLine borderTop;
    /* package */ HSLFLine borderBottom;

    /**
     * The number of columns to be spanned/merged
     */
    private int gridSpan = 1;

    /**
     * The number of columns to be spanned/merged
     */
    private int rowSpan = 1;

    /**
     * Create a TableCell object and initialize it from the supplied Record container.
     *
     * @param escherRecord       EscherSpContainer which holds information about this shape
     * @param parent    the parent of the shape
     */
   protected HSLFTableCell(EscherContainerRecord escherRecord, HSLFTable parent){
        super(escherRecord, parent);
    }

    /**
     * Create a new TableCell. This constructor is used when a new shape is created.
     *
     * @param parent    the parent of this Shape. For example, if this text box is a cell
     * in a table then the parent is Table.
     */
    public HSLFTableCell(HSLFTable parent){
        super(parent);

        setShapeType(ShapeType.RECT);
        //_txtrun.setRunType(TextHeaderAtom.HALF_BODY_TYPE);
        //_txtrun.getRichTextRuns()[0].setFlag(false, 0, false);
    }

    @Override
    protected EscherContainerRecord createSpContainer(boolean isChild){
        EscherContainerRecord ecr = super.createSpContainer(isChild);
        AbstractEscherOptRecord opt = getEscherOptRecord();
        setEscherProperty(opt, EscherPropertyTypes.TEXT__TEXTID, 0);
        setEscherProperty(opt, EscherPropertyTypes.TEXT__SIZE_TEXT_TO_FIT_SHAPE, 0x20000);
        setEscherProperty(opt, EscherPropertyTypes.FILL__NOFILLHITTEST, 0x150001);
        setEscherProperty(opt, EscherPropertyTypes.SHADOWSTYLE__SHADOWOBSURED, 0x20000);
        setEscherProperty(opt, EscherPropertyTypes.PROTECTION__LOCKAGAINSTGROUPING, 0x40000);

        return ecr;
    }

    private void anchorBorder(BorderEdge edge, final HSLFLine line) {
        if (line == null) {
            return;
        }
        Rectangle2D cellAnchor = getAnchor();
        double x,y,w,h;
        switch(edge){
            case top:
                x = cellAnchor.getX();
                y = cellAnchor.getY();
                w = cellAnchor.getWidth();
                h = 0;
                break;
            case right:
                x = cellAnchor.getX() + cellAnchor.getWidth();
                y = cellAnchor.getY();
                w = 0;
                h = cellAnchor.getHeight();
                break;
            case bottom:
                x = cellAnchor.getX();
                y = cellAnchor.getY() + cellAnchor.getHeight();
                w = cellAnchor.getWidth();
                h = 0;
                break;
            case left:
                x = cellAnchor.getX();
                y = cellAnchor.getY();
                w = 0;
                h = cellAnchor.getHeight();
                break;
            default:
                throw new IllegalArgumentException();
        }
        line.setAnchor(new Rectangle2D.Double(x,y,w,h));
    }

    @Override
    public void setAnchor(Rectangle2D anchor){
        super.setAnchor(anchor);

        anchorBorder(BorderEdge.top, borderTop);
        anchorBorder(BorderEdge.right, borderRight);
        anchorBorder(BorderEdge.bottom, borderBottom);
        anchorBorder(BorderEdge.left, borderLeft);
    }

    @Override
    public StrokeStyle getBorderStyle(final BorderEdge edge) {
        final Double width = getBorderWidth(edge);
        return (width == null) ? null : new StrokeStyle() {
            @Override
            public PaintStyle getPaint() {
                return DrawPaint.createSolidPaint(getBorderColor(edge));
            }

            @Override
            public LineCap getLineCap() {
                return null;
            }

            @Override
            public LineDash getLineDash() {
                return getBorderDash(edge);
            }

            @Override
            public LineCompound getLineCompound() {
                return getBorderCompound(edge);
            }

            @Override
            public double getLineWidth() {
                return width;
            }
        };
    }

    @Override
    public void setBorderStyle(BorderEdge edge, StrokeStyle style) {
        if (style == null) {
            throw new IllegalArgumentException("StrokeStyle needs to be specified.");
        }

        // setting the line cap is not implemented, as the border lines aren't connected

        LineCompound compound = style.getLineCompound();
        if (compound != null) {
            setBorderCompound(edge, compound);
        }

        LineDash dash = style.getLineDash();
        if (dash != null) {
            setBorderDash(edge, dash);
        }

        double width = style.getLineWidth();
        setBorderWidth(edge, width);
    }


    public Double getBorderWidth(BorderEdge edge) {
        HSLFLine l;
        switch (edge) {
            case bottom: l = borderBottom; break;
            case top: l = borderTop; break;
            case right: l = borderRight; break;
            case left: l = borderLeft; break;
            default: throw new IllegalArgumentException();
        }
        return (l == null) ? null : l.getLineWidth();
    }

    @Override
    public void setBorderWidth(BorderEdge edge, double width) {
        HSLFLine l = addLine(edge);
        l.setLineWidth(width);
    }

    public Color getBorderColor(BorderEdge edge) {
        HSLFLine l;
        switch (edge) {
            case bottom: l = borderBottom; break;
            case top: l = borderTop; break;
            case right: l = borderRight; break;
            case left: l = borderLeft; break;
            default: throw new IllegalArgumentException();
        }
        return (l == null) ? null : l.getLineColor();
    }

    @Override
    public void setBorderColor(BorderEdge edge, Color color) {
        if (edge == null || color == null) {
            throw new IllegalArgumentException("BorderEdge and/or Color need to be specified.");
        }

        HSLFLine l = addLine(edge);
        l.setLineColor(color);
    }

    public LineDash getBorderDash(BorderEdge edge) {
        HSLFLine l;
        switch (edge) {
            case bottom: l = borderBottom; break;
            case top: l = borderTop; break;
            case right: l = borderRight; break;
            case left: l = borderLeft; break;
            default: throw new IllegalArgumentException();
        }
        return (l == null) ? null : l.getLineDash();
    }

    @Override
    public void setBorderDash(BorderEdge edge, LineDash dash) {
        if (edge == null || dash == null) {
            throw new IllegalArgumentException("BorderEdge and/or LineDash need to be specified.");
        }

        HSLFLine l = addLine(edge);
        l.setLineDash(dash);
    }

    public LineCompound getBorderCompound(BorderEdge edge) {
        HSLFLine l;
        switch (edge) {
            case bottom: l = borderBottom; break;
            case top: l = borderTop; break;
            case right: l = borderRight; break;
            case left: l = borderLeft; break;
            default: throw new IllegalArgumentException();
        }
        return (l == null) ? null : l.getLineCompound();
    }

    @Override
    public void setBorderCompound(BorderEdge edge, LineCompound compound) {
        if (edge == null || compound == null) {
            throw new IllegalArgumentException("BorderEdge and/or LineCompound need to be specified.");
        }

        HSLFLine l = addLine(edge);
        l.setLineCompound(compound);
    }


    protected HSLFLine addLine(BorderEdge edge) {
        switch (edge) {
            case bottom: {
                if (borderBottom == null) {
                    borderBottom = createBorder(edge);
                    HSLFTableCell c = getSiblingCell(1,0);
                    if (c != null) {
                        assert(c.borderTop == null);
                        c.borderTop = borderBottom;
                    }
                }
                return borderBottom;
            }
            case top: {
                if (borderTop == null) {
                    borderTop = createBorder(edge);
                    HSLFTableCell c = getSiblingCell(-1,0);
                    if (c != null) {
                        assert(c.borderBottom == null);
                        c.borderBottom = borderTop;
                    }
                }
                return borderTop;
            }
            case right: {
                if (borderRight == null) {
                    borderRight = createBorder(edge);
                    HSLFTableCell c = getSiblingCell(0,1);
                    if (c != null) {
                        assert(c.borderLeft == null);
                        c.borderLeft = borderRight;
                    }
                }
                return borderRight;
            }
            case left: {
                if (borderLeft == null) {
                    borderLeft = createBorder(edge);
                    HSLFTableCell c = getSiblingCell(0,-1);
                    if (c != null) {
                        assert(c.borderRight == null);
                        c.borderRight = borderLeft;
                    }
                }
                return borderLeft;
            }
            default:
                throw new IllegalArgumentException();
        }
    }

    @Override
    public void removeBorder(BorderEdge edge) {
        switch (edge) {
            case bottom: {
                if (borderBottom == null) break;
                getParent().removeShape(borderBottom);
                borderBottom = null;
                HSLFTableCell c = getSiblingCell(1,0);
                if (c != null) {
                    c.borderTop = null;
                }
                break;
            }
            case top: {
                if (borderTop == null) break;
                getParent().removeShape(borderTop);
                borderTop = null;
                HSLFTableCell c = getSiblingCell(-1,0);
                if (c != null) {
                    c.borderBottom = null;
                }
                break;
            }
            case right: {
                if (borderRight == null) break;
                getParent().removeShape(borderRight);
                borderRight = null;
                HSLFTableCell c = getSiblingCell(0,1);
                if (c != null) {
                    c.borderLeft = null;
                }
                break;
            }
            case left: {
                if (borderLeft == null) break;
                getParent().removeShape(borderLeft);
                borderLeft = null;
                HSLFTableCell c = getSiblingCell(0,-1);
                if (c != null) {
                    c.borderRight = null;
                }
                break;
            }
            default:
                throw new IllegalArgumentException();
        }
    }

    protected HSLFTableCell getSiblingCell(int row, int col) {
        return getParent().getRelativeCell(this, row, col);
    }

    /**
     * Create a border to format this table
     *
     * @return the created border
     */
    private HSLFLine createBorder(BorderEdge edge) {
        HSLFTable table = getParent();
        HSLFLine line = new HSLFLine(table);
        table.addShape(line);

        AbstractEscherOptRecord opt = getEscherOptRecord();
        setEscherProperty(opt, EscherPropertyTypes.GEOMETRY__SHAPEPATH, -1);
        setEscherProperty(opt, EscherPropertyTypes.GEOMETRY__FILLOK, -1);
        setEscherProperty(opt, EscherPropertyTypes.SHADOWSTYLE__SHADOWOBSURED, 0x20000);
        setEscherProperty(opt, EscherPropertyTypes.THREED__LIGHTFACE, 0x80000);

        anchorBorder(edge, line);

        return line;
    }

    protected void applyLineProperties(BorderEdge edge, HSLFLine other) {
        HSLFLine line = addLine(edge);
        line.setLineWidth(other.getLineWidth());
        line.setLineColor(other.getLineColor());
        // line.setLineCompound(other.getLineCompound());
        // line.setLineDashing(other.getLineDashing());
    }

    @Override
    public HSLFTable getParent() {
        return (HSLFTable)super.getParent();
    }

    /**
     * Set the gridSpan (aka col-span)
     *
     * @param gridSpan the number of columns to be spanned/merged
     *
     * @since POI 3.15-beta2
     */
    protected void setGridSpan(int gridSpan) {
        this.gridSpan = gridSpan;
    }

    /**
     * Set the rowSpan
     *
     * @param rowSpan the number of rows to be spanned/merged
     *
     * @since POI 3.15-beta2
     */
    protected void setRowSpan(int rowSpan) {
        this.rowSpan = rowSpan;
    }

    @Override
    public int getGridSpan() {
        return gridSpan;
    }

    @Override
    public int getRowSpan() {
        return rowSpan;
    }

    @Override
    public boolean isMerged() {
        // if a hslf cell is merged, it won't appear in the cell matrix, i.e. it doesn't exist
        // therefore this is always false 
        return false;
    }
}

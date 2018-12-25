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

package org.apache.poi.sl.draw;

import static org.apache.poi.sl.draw.DrawPaint.fillPaintWorkaround;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

import org.apache.poi.sl.usermodel.GroupShape;
import org.apache.poi.sl.usermodel.StrokeStyle;
import org.apache.poi.sl.usermodel.StrokeStyle.LineCompound;
import org.apache.poi.sl.usermodel.StrokeStyle.LineDash;
import org.apache.poi.sl.usermodel.TableCell;
import org.apache.poi.sl.usermodel.TableCell.BorderEdge;
import org.apache.poi.util.Internal;
import org.apache.poi.sl.usermodel.TableShape;

public class DrawTableShape extends DrawShape {
    /**
     * Additional spacing between cells
     */
    @Internal
    public static final int borderSize = 2;
    
    public DrawTableShape(TableShape<?,?> shape) {
        super(shape);
    }

    protected Drawable getGroupShape(Graphics2D graphics) {
        if (shape instanceof GroupShape) {
            DrawFactory df = DrawFactory.getInstance(graphics);
            return df.getDrawable((GroupShape<?,?>)shape);
        }
        return null;
    }

    public void applyTransform(Graphics2D graphics) {
        Drawable d = getGroupShape(graphics);
        if (d != null) {
            d.applyTransform(graphics);
        } else {
            super.applyTransform(graphics);
        }
    }

    public void draw(Graphics2D graphics) {
        Drawable d = getGroupShape(graphics);
        if (d != null) {
            d.draw(graphics);
            return;
        }

        TableShape<?,?> ts = getShape();
        DrawPaint drawPaint = DrawFactory.getInstance(graphics).getPaint(ts);
        final int rows = ts.getNumberOfRows();
        final int cols = ts.getNumberOfColumns();
        
        // draw background boxes
        for (int row=0; row<rows; row++) {
            for (int col=0; col<cols; col++) {
                TableCell<?,?> tc = ts.getCell(row, col);
                if (tc == null || tc.isMerged()) {
                    continue;
                }

                Paint fillPaint = drawPaint.getPaint(graphics, tc.getFillStyle().getPaint());
                graphics.setPaint(fillPaint);
                Rectangle2D cellAnc = tc.getAnchor();
                fillPaintWorkaround(graphics, cellAnc);

                for (BorderEdge edge : BorderEdge.values()) {
                    StrokeStyle stroke = tc.getBorderStyle(edge);
                    if (stroke == null) {
                        continue;
                    }
                    graphics.setStroke(getStroke(stroke));
                    Paint linePaint = drawPaint.getPaint(graphics, stroke.getPaint());
                    graphics.setPaint(linePaint);

                    double x=cellAnc.getX(), y=cellAnc.getY(), w=cellAnc.getWidth(), h=cellAnc.getHeight();
                    Line2D line;
                    switch (edge) {
                        default:
                        case bottom:
                            line = new Line2D.Double(x-borderSize, y+h, x+w+borderSize, y+h);
                            break;
                        case left:
                            line = new Line2D.Double(x, y, x, y+h+borderSize);
                            break;
                        case right:
                            line = new Line2D.Double(x+w, y, x+w, y+h+borderSize);
                            break;
                        case top:
                            line = new Line2D.Double(x-borderSize, y, x+w+borderSize, y);
                            break;
                    }

                    graphics.draw(line);
                }
            }
        }

        // draw text
        drawContent(graphics);
    }

    public void drawContent(Graphics2D graphics) {
        Drawable d = getGroupShape(graphics);
        if (d != null) {
            d.drawContent(graphics);
            return;
        }
        
        TableShape<?,?> ts = getShape();
        DrawFactory df = DrawFactory.getInstance(graphics);

        final int rows = ts.getNumberOfRows();
        final int cols = ts.getNumberOfColumns();
        
        for (int row=0; row<rows; row++) {
            for (int col=0; col<cols; col++) {
                TableCell<?,?> tc = ts.getCell(row, col);
                if (tc != null) {
                    DrawTextShape dts = df.getDrawable(tc);
                    dts.drawContent(graphics);
                }
            }
        }
    }

    @Override
    protected TableShape<?,?> getShape() {
        return (TableShape<?,?>)shape;
    }

    /**
     * Format the table and apply the specified Line to all cell boundaries,
     * both outside and inside.
     * An empty args parameter removes the affected border.
     *
     * @param args a varargs array possible containing {@link Double} (width),
     * {@link LineCompound}, {@link Color}, {@link LineDash}
     */
    public void setAllBorders(Object... args) {
        TableShape<?,?> table = getShape();
        final int rows = table.getNumberOfRows();
        final int cols = table.getNumberOfColumns();

        BorderEdge[] edges = {BorderEdge.top, BorderEdge.left, null, null};
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                edges[2] = (col == cols - 1) ? BorderEdge.right : null;
                edges[3] = (row == rows - 1) ? BorderEdge.bottom : null;
                setEdges(table.getCell(row, col), edges, args);
            }
        }
    }

    /**
     * Format the outside border using the specified Line object
     * An empty args parameter removes the affected border.
     *
     * @param args a varargs array possible containing {@link Double} (width),
     * {@link LineCompound}, {@link Color}, {@link LineDash}
     */
    public void setOutsideBorders(Object... args){
        if (args.length == 0) return;

        TableShape<?,?> table = getShape();
        final int rows = table.getNumberOfRows();
        final int cols = table.getNumberOfColumns();

        BorderEdge[] edges = new BorderEdge[4];
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                edges[0] = (col == 0) ? BorderEdge.left : null;
                edges[1] = (col == cols - 1) ? BorderEdge.right : null;
                edges[2] = (row == 0) ? BorderEdge.top : null;
                edges[3] = (row == rows - 1) ? BorderEdge.bottom : null;
                setEdges(table.getCell(row, col), edges, args);
            }
        }
    }

    /**
     * Format the inside border using the specified Line object
     * An empty args parameter removes the affected border.
     *
     * @param args a varargs array possible containing {@link Double} (width),
     * {@link LineCompound}, {@link Color}, {@link LineDash}
     */
    public void setInsideBorders(Object... args) {
        if (args.length == 0) return;

        TableShape<?,?> table = getShape();
        final int rows = table.getNumberOfRows();
        final int cols = table.getNumberOfColumns();

        BorderEdge[] edges = new BorderEdge[2];
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                edges[0] = (col > 0 && col < cols - 1) ? BorderEdge.right : null;
                edges[1] = (row > 0 && row < rows - 1) ? BorderEdge.bottom : null;
                setEdges(table.getCell(row, col), edges, args);
            }
        }
    }

    /**
     * Apply the border attributes (args) to the given cell and edges
     *
     * @param cell the cell
     * @param edges the border edges
     * @param args the border attributes
     */
    private static void setEdges(TableCell<?,?> cell, BorderEdge[] edges, Object... args) {
        if (cell == null) {
            return;
        }
        for (BorderEdge be : edges) {
            if (be != null) {
                if (args.length == 0) {
                    cell.removeBorder(be);
                } else {
                    for (Object o : args) {
                        if (o instanceof Double) {
                            cell.setBorderWidth(be, (Double)o);
                        } else if (o instanceof Color) {
                            cell.setBorderColor(be, (Color)o);
                        } else if (o instanceof LineDash) {
                            cell.setBorderDash(be, (LineDash)o);
                        } else if (o instanceof LineCompound) {
                            cell.setBorderCompound(be, (LineCompound)o);
                        }
                    }
                }
            }
        }
    }
}

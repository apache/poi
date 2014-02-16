package org.apache.poi.hwmf.record;

import java.io.IOException;

import org.apache.poi.util.LittleEndianConsts;
import org.apache.poi.util.LittleEndianInputStream;

public class WmfDraw {
    /**
     * The META_MOVETO record sets the output position in the playback device context to a specified
     * point.
     */
    public static class WmfMoveTo implements WmfRecord {
        
        /**
         * A 16-bit signed integer that defines the y-coordinate, in logical units.
         */
        int y;
        
        /**
         * A 16-bit signed integer that defines the x-coordinate, in logical units.
         */
        int x;
        
        public WmfRecordType getRecordType() {
            return WmfRecordType.moveTo;
        }
        
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            y = leis.readShort();
            x = leis.readShort();
            return 2*LittleEndianConsts.SHORT_SIZE;
        }
    }
    
    /**
     * The META_LINETO record draws a line from the drawing position that is defined in the playback
     * device context up to, but not including, the specified point.
     */
    public static class WmfLineTo implements WmfRecord {
        
        /**
         * A 16-bit signed integer that defines the vertical component of the drawing
         * destination position, in logical units.
         */
        int y;
        
        /**
         * A 16-bit signed integer that defines the horizontal component of the drawing
         * destination position, in logical units.
         */
        int x;
        
        public WmfRecordType getRecordType() {
            return WmfRecordType.lineTo;
        }
        
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            y = leis.readShort();
            x = leis.readShort();
            return 2*LittleEndianConsts.SHORT_SIZE;
        }
    }
    
    /**
     * The META_POLYGON record paints a polygon consisting of two or more vertices connected by
     * straight lines. The polygon is outlined by using the pen and filled by using the brush and polygon fill
     * mode that are defined in the playback device context.
     */
    public static class WmfPolygon implements WmfRecord {
        
        /**
         * A 16-bit signed integer that defines the number of points in the array.
         */
        int numberofPoints;
        
        short xPoints[], yPoints[];
        
        public WmfRecordType getRecordType() {
            return WmfRecordType.polygon;
        }
        
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            numberofPoints = leis.readShort();
            xPoints = new short[numberofPoints];
            yPoints = new short[numberofPoints];
            
            for (int i=0; i<numberofPoints; i++) {
                // A 16-bit signed integer that defines the horizontal (x) coordinate of the point.
                xPoints[i] = leis.readShort();
                // A 16-bit signed integer that defines the vertical (y) coordinate of the point.
                yPoints[i] = leis.readShort();
            }
            
            return LittleEndianConsts.SHORT_SIZE+numberofPoints*LittleEndianConsts.INT_SIZE;
        }
    }
    
    /**
     * The META_POLYLINE record draws a series of line segments by connecting the points in the
     * specified array.
     */
    public static class WmfPolyline extends WmfPolygon {
        
        public WmfRecordType getRecordType() {
            return WmfRecordType.polyline;
        }
    }
    
    /**
     * The META_ELLIPSE record draws an ellipse. The center of the ellipse is the center of the specified
     * bounding rectangle. The ellipse is outlined by using the pen and is filled by using the brush; these
     * are defined in the playback device context.
     */
    public static class WmfEllipse implements WmfRecord {
        /**
         * A 16-bit signed integer that defines the y-coordinate, in logical units, of
         * the lower-right corner of the bounding rectangle.
         */
        int bottomRect;
        /**
         * A 16-bit signed integer that defines the x-coordinate, in logical units, of
         * the lower-right corner of the bounding rectangle.
         */
        int rightRect;
        /**
         * A 16-bit signed integer that defines the y-coordinate, in logical units, of the
         * upper-left corner of the bounding rectangle.
         */
        int topRect;
        /**
         * A 16-bit signed integer that defines the x-coordinate, in logical units, of
         * the upper-left corner of the bounding rectangle.
         */
        int leftRect;
        
        public WmfRecordType getRecordType() {
            return WmfRecordType.ellipse;
        }
        
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            bottomRect = leis.readShort();
            rightRect = leis.readShort();
            topRect = leis.readShort();
            leftRect = leis.readShort();
            return 4*LittleEndianConsts.SHORT_SIZE;
        }
    }


    /**
     * The META_FRAMEREGION record draws a border around a specified region using a specified brush.
     */
    public static class WmfFrameRegion implements WmfRecord {
        /**
         * A 16-bit unsigned integer used to index into the WMF Object Table to get
         * the region to be framed.
         */
        int region;  
        /**
         * A 16-bit unsigned integer used to index into the WMF Object Table to get the
         * Brush to use for filling the region.
         */
        int brush;
        /**
         * A 16-bit signed integer that defines the height, in logical units, of the
         * region frame.
         */
        int height;
        /**
         * A 16-bit signed integer that defines the width, in logical units, of the
         * region frame.
         */
        int width;
        
        public WmfRecordType getRecordType() {
            return WmfRecordType.frameRegion;
        }
        
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            region = leis.readUShort();
            brush = leis.readUShort();
            height = leis.readShort();
            width = leis.readShort();
            return 4*LittleEndianConsts.SHORT_SIZE;
        }
    }

    /**
     * The META_POLYPOLYGON record paints a series of closed polygons. Each polygon is outlined by
     * using the pen and filled by using the brush and polygon fill mode; these are defined in the playback
     * device context. The polygons drawn by this function can overlap.
     */
    public static class WmfPolyPolygon implements WmfRecord {
        
        /**
         * A 16-bit unsigned integer that defines the number of polygons in the object.
         */
        int numberOfPolygons;
        
        /**
         * A NumberOfPolygons array of 16-bit unsigned integers that define the number of
         * points for each polygon in the object.
         */
        int pointsPerPolygon[];
        
        /**
         * An array of 16-bit unsigned integers that define the coordinates of the polygons.
         */
        int xPoints[][];

        /**
         * An array of 16-bit unsigned integers that define the coordinates of the polygons.
         */
        int yPoints[][];
        
        
        public WmfRecordType getRecordType() {
            return WmfRecordType.polyPolygon;
        }
        
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            // see http://secunia.com/gfx/pdf/SA31675_BA.pdf ;)
            numberOfPolygons = leis.readUShort();
            pointsPerPolygon = new int[numberOfPolygons];
            xPoints = new int[numberOfPolygons][];
            yPoints = new int[numberOfPolygons][];

            int size = LittleEndianConsts.SHORT_SIZE;
            
            for (int i=0; i<numberOfPolygons; i++) {
                pointsPerPolygon[i] = leis.readUShort();
                size += LittleEndianConsts.SHORT_SIZE;
            }
            
            for (int i=0; i<numberOfPolygons; i++) {
                
                xPoints[i] = new int[pointsPerPolygon[i]];
                yPoints[i] = new int[pointsPerPolygon[i]];
                
                for (int j=0; j<pointsPerPolygon[i]; j++) {
                    xPoints[i][j] = leis.readUShort();
                    yPoints[i][j] = leis.readUShort();
                    size += 2*LittleEndianConsts.SHORT_SIZE;
                }
            }
            
            return size;
        }
    }

    /**
     * The META_RECTANGLE record paints a rectangle. The rectangle is outlined by using the pen and
     * filled by using the brush that are defined in the playback device context.
     */
    public static class WmfRectangle implements WmfRecord {
        /**
         * A 16-bit signed integer that defines the y-coordinate, in logical units, of
         * the lower-right corner of the rectangle.
         */
        int bottomRect;
        /**
         * A 16-bit signed integer that defines the x-coordinate, in logical units, of
         * the lower-right corner of the rectangle.
         */
        int rightRect;
        /**
         * A 16-bit signed integer that defines the y-coordinate, in logical units, of the
         * upper-left corner of the rectangle.
         */
        int topRect;
        /**
         * A 16-bit signed integer that defines the x-coordinate, in logical units, of
         * the upper-left corner of the rectangle.
         */
        int leftRect;
        
        public WmfRecordType getRecordType() {
            return WmfRecordType.frameRegion;
        }
        
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            bottomRect = leis.readShort();
            rightRect = leis.readShort();
            topRect = leis.readShort();
            leftRect = leis.readShort();
            return 4*LittleEndianConsts.SHORT_SIZE;
        }
    }

    /**
     * The META_RECTANGLE record paints a rectangle. The rectangle is outlined by using the pen and
     * filled by using the brush that are defined in the playback device context.
     */
    public static class WmfSetPixel implements WmfRecord {
        /**
         * A ColorRef Object that defines the color value.
         */
        WmfColorRef colorRef; 

        /**
         * A 16-bit signed integer that defines the y-coordinate, in logical units, of the point
         * to be set.
         */
        int y;
        
        /**
         * A 16-bit signed integer that defines the x-coordinate, in logical units, of the point
         * to be set.
         */
        int x;
        
        
        public WmfRecordType getRecordType() {
            return WmfRecordType.setPixel;
        }
        
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            colorRef = new WmfColorRef();
            int size = colorRef.init(leis);
            y = leis.readShort();
            x = leis.readShort();
            return 2*LittleEndianConsts.SHORT_SIZE+size;
        }
    }

    /**
     * The META_ROUNDRECT record paints a rectangle with rounded corners. The rectangle is outlined
     * using the pen and filled using the brush, as defined in the playback device context.
     */
    public static class WmfRoundRect implements WmfRecord {
        /**
         * A 16-bit signed integer that defines the height, in logical coordinates, of the
         * ellipse used to draw the rounded corners.
         */
        int height;
        
        /**
         * A 16-bit signed integer that defines the width, in logical coordinates, of the
         * ellipse used to draw the rounded corners.
         */
        int width;
        
        /**
         * A 16-bit signed integer that defines the y-coordinate, in logical units, of
         * the lower-right corner of the rectangle.
         */
        int bottomRect;
        
        /**
         * A 16-bit signed integer that defines the x-coordinate, in logical units, of
         * the lower-right corner of the rectangle.
         */
        int rightRect;
        
        /**
         * A 16-bit signed integer that defines the y-coordinate, in logical units, of the
         * upper-left corner of the rectangle.
         */
        int topRect;
        
        /**
         * A 16-bit signed integer that defines the x-coordinate, in logical units, of
         * the upper-left corner of the rectangle.
         */
        int leftRect;
        
        
        public WmfRecordType getRecordType() {
            return WmfRecordType.roundRect;
        }
        
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            height = leis.readShort();
            width = leis.readShort();
            bottomRect = leis.readShort();
            rightRect = leis.readShort();
            topRect = leis.readShort();
            leftRect = leis.readShort();
            return 6*LittleEndianConsts.SHORT_SIZE;
        }
    }
    


    /**
     * The META_PIE record draws a pie-shaped wedge bounded by the intersection of an ellipse and two
     * radials. The pie is outlined by using the pen and filled by using the brush that are defined in the
     * playback device context.
     */
    public static class WmfPie implements WmfRecord {
        /**
         * A 16-bit signed integer that defines the y-coordinate, in logical
         * coordinates, of the endpoint of the second radial.
         */
        int yRadial2;
        /**
         * A 16-bit signed integer that defines the x-coordinate, in logical
         * coordinates, of the endpoint of the second radial.
         */
        int xRadial2;
        /**
         * A 16-bit signed integer that defines the y-coordinate, in logical 
         * coordinates, of the endpoint of the first radial.
         */
        int yRadial1;
        /**
         * A 16-bit signed integer that defines the x-coordinate, in logical 
         * coordinates, of the endpoint of the first radial.
         */
        int xRadial1;  
        /**
         * A 16-bit signed integer that defines the y-coordinate, in logical units, of 
         * the lower-right corner of the bounding rectangle.
         */
        int bottomRect;
        /**
         * A 16-bit signed integer that defines the x-coordinate, in logical units, of 
         * the lower-right corner of the bounding rectangle.
         */
        int rightRect;
        /**
         * A 16-bit signed integer that defines the y-coordinate, in logical units, of the 
         * upper-left corner of the bounding rectangle.
         */
        int topRect;
        /**
         * A 16-bit signed integer that defines the x-coordinate, in logical units, of 
         * the upper-left corner of the bounding rectangle.
         */
        int leftRect;
        
        public WmfRecordType getRecordType() {
            return WmfRecordType.pie;
        }
        
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            yRadial2 = leis.readShort();
            xRadial2 = leis.readShort();
            yRadial1 = leis.readShort();
            xRadial1 = leis.readShort();
            bottomRect = leis.readShort();
            rightRect = leis.readShort();
            topRect = leis.readShort();
            leftRect = leis.readShort();
            return 8*LittleEndianConsts.SHORT_SIZE;
        }
    }

    /**
     * The META_ARC record draws an elliptical arc.
     */
    public static class WmfArc implements WmfRecord {
        /**
         * A 16-bit signed integer that defines the y-coordinate, in logical units, of
         * the ending point of the radial line defining the ending point of the arc.
         */
        int yEndArc; 
        /**
         * A 16-bit signed integer that defines the x-coordinate, in logical units, of 
         * the ending point of the radial line defining the ending point of the arc.
         */
        int xEndArc; 
        /**
         * A 16-bit signed integer that defines the y-coordinate, in logical units, of 
         * the ending point of the radial line defining the starting point of the arc.
         */
        int yStartArc;
        /**
         * A 16-bit signed integer that defines the x-coordinate, in logical units, of 
         * the ending point of the radial line defining the starting point of the arc.
         */
        int xStartArc;
        /**
         * A 16-bit signed integer that defines the y-coordinate, in logical units, of 
         * the lower-right corner of the bounding rectangle.
         */
        int bottomRect;
        /**
         * A 16-bit signed integer that defines the x-coordinate, in logical units, of 
         * the lower-right corner of the bounding rectangle.
         */
        int rightRect;
        /**
         * A 16-bit signed integer that defines the y-coordinate, in logical units, of the 
         * upper-left corner of the bounding rectangle.
         */
        int topRect;
        /**
         * A 16-bit signed integer that defines the x-coordinate, in logical units, of 
         * the upper-left corner of the bounding rectangle.
         */
        int leftRect;
        
        public WmfRecordType getRecordType() {
            return WmfRecordType.arc;
        }
        
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            yEndArc = leis.readShort();
            xEndArc = leis.readShort();
            yStartArc = leis.readShort();
            xStartArc = leis.readShort();
            bottomRect = leis.readShort();
            rightRect = leis.readShort();
            topRect = leis.readShort();
            leftRect = leis.readShort();
            return 8*LittleEndianConsts.SHORT_SIZE;
        }
    }

    /**
     * The META_CHORD record draws a chord, which is defined by a region bounded by the intersection of
     * an ellipse with a line segment. The chord is outlined using the pen and filled using the brush
     * that are defined in the playback device context.
     */
    public static class WmfChord implements WmfRecord {
        /**
         * A 16-bit signed integer that defines the y-coordinate, in logical 
         * coordinates, of the endpoint of the second radial.
         */
        int yRadial2;
        /**
         * A 16-bit signed integer that defines the x-coordinate, in logical 
         * coordinates, of the endpoint of the second radial.
         */
        int xRadial2;
        /**
         * A 16-bit signed integer that defines the y-coordinate, in logical 
         * coordinates, of the endpoint of the first radial.
         */
        int yRadial1;
        /**
         * A 16-bit signed integer that defines the x-coordinate, in logical 
         * coordinates, of the endpoint of the first radial.
         */
        int xRadial1;
        /**
         * A 16-bit signed integer that defines the y-coordinate, in logical units, of 
         * the lower-right corner of the bounding rectangle.
         */
        int bottomRect;
        /**
         * A 16-bit signed integer that defines the x-coordinate, in logical units, of 
         * the lower-right corner of the bounding rectangle.
         */
        int rightRect;
        /**
         * A 16-bit signed integer that defines the y-coordinate, in logical units, of the 
         * upper-left corner of the bounding rectangle.
         */
        int topRect;
        /**
         * A 16-bit signed integer that defines the x-coordinate, in logical units, of 
         * the upper-left corner of the bounding rectangle.
         */
        int leftRect;
        
        
        public WmfRecordType getRecordType() {
            return WmfRecordType.chord;
        }
        
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            yRadial2 = leis.readShort();
            xRadial2 = leis.readShort();
            yRadial1 = leis.readShort();
            xRadial1 = leis.readShort();
            bottomRect = leis.readShort();
            rightRect = leis.readShort();
            topRect = leis.readShort();
            leftRect = leis.readShort();
            return 8*LittleEndianConsts.SHORT_SIZE;
        }
    }


    /**
     * The META_SELECTOBJECT record specifies a graphics object for the playback device context. The 
     * new object replaces the previous object of the same type, unless if the previous object is a palette 
     * object. If the previous object is a palette object, then the META_SELECTPALETTE record must be 
     * used instead of the META_SELECTOBJECT record, as the META_SELECTOBJECT record does not 
     * support replacing the palette object type.
     */
    public static class WmfSelectObject implements WmfRecord {
        
        /**
         * A 16-bit unsigned integer used to index into the WMF Object Table to
         * get the object to be selected.
         */
        int objectIndex;
        
        public WmfRecordType getRecordType() {
            return WmfRecordType.selectObject;
        }
        
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            objectIndex = leis.readUShort();
            return LittleEndianConsts.SHORT_SIZE;
        }
    }
 }

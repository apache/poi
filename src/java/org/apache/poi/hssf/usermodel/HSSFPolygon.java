package org.apache.poi.hssf.usermodel;

/**
 * @author Glen Stampoultzis  (glens at superlinksoftware.com)
 */
public class HSSFPolygon
        extends HSSFShape
{
    int[] xPoints;
    int[] yPoints;
    int drawAreaWidth = 100;
    int drawAreaHeight = 100;

    HSSFPolygon( HSSFShape parent, HSSFAnchor anchor )
    {
        super( parent, anchor );
    }

    public int[] getXPoints()
    {
        return xPoints;
    }

    public int[] getYPoints()
    {
        return yPoints;
    }

    public void setPoints(int[] xPoints, int[] yPoints)
    {
        this.xPoints = cloneArray(xPoints);
        this.yPoints = cloneArray(yPoints);
    }

    private int[] cloneArray( int[] a )
    {
        int[] result = new int[a.length];
        for ( int i = 0; i < a.length; i++ )
            result[i] = a[i];

        return result;
    }

    /**
     * Defines the width and height of the points in the polygon
     * @param width
     * @param height
     */
    public void setPolygonDrawArea( int width, int height )
    {
        this.drawAreaWidth = width;
        this.drawAreaHeight = height;
    }

    public int getDrawAreaWidth()
    {
        return drawAreaWidth;
    }

    public int getDrawAreaHeight()
    {
        return drawAreaHeight;
    }


}

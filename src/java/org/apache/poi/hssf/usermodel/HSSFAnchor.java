package org.apache.poi.hssf.usermodel;


/**
 * An anchor is what specifics the position of a shape within a client object
 * or within another containing shape.
 *
 * @author Glen Stampoultzis (glens at apache.org)
 */
public abstract class HSSFAnchor
{
    int dx1;
    int dy1;
    int dx2;
    int dy2;

    public HSSFAnchor()
    {
    }

    public HSSFAnchor( int dx1, int dy1, int dx2, int dy2 )
    {
        this.dx1 = dx1;
        this.dy1 = dy1;
        this.dx2 = dx2;
        this.dy2 = dy2;
    }

    public int getDx1(){ return dx1; }
    public void setDx1( int dx1 ){ this.dx1 = dx1; }
    public int getDy1(){ return dy1; }
    public void setDy1( int dy1 ){ this.dy1 = dy1; }
    public int getDy2(){ return dy2; }
    public void setDy2( int dy2 ){ this.dy2 = dy2; }
    public int getDx2(){ return dx2; }
    public void setDx2( int dx2 ){ this.dx2 = dx2; }

    public abstract boolean isHorizontallyFlipped();
    public abstract boolean isVerticallyFlipped();
}

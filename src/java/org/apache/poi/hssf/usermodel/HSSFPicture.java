package org.apache.poi.hssf.usermodel;

/**
 * Represents a escher picture.  Eg. A GIF, JPEG etc...
 *
 * @author Glen Stampoultzis
 * @version $Id$
 */
public class HSSFPicture
        extends HSSFSimpleShape
{
    public static final int PICTURE_TYPE_EMF = 0;                // Windows Enhanced Metafile
    public static final int PICTURE_TYPE_WMF = 1;                // Windows Metafile
    public static final int PICTURE_TYPE_PICT = 2;               // Macintosh PICT
    public static final int PICTURE_TYPE_JPEG = 3;               // JFIF
    public static final int PICTURE_TYPE_PNG = 4;                // PNG
    public static final int PICTURE_TYPE_DIB = 5;                // Windows DIB

    int pictureIndex;

    /**
     * Constructs a picture object.
     */ 
    HSSFPicture( HSSFShape parent, HSSFAnchor anchor )
    {
        super( parent, anchor );
        setShapeType(OBJECT_TYPE_PICTURE);
    }

    public int getPictureIndex()
    {
        return pictureIndex;
    }

    public void setPictureIndex( int pictureIndex )
    {
        this.pictureIndex = pictureIndex;
    }
}

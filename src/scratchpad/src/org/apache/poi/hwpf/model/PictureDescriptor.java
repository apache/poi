package org.apache.poi.hwpf.model;

import java.util.Arrays;

import org.apache.poi.util.Internal;
import org.apache.poi.util.LittleEndian;

/**
 * Picture Descriptor (on File) (PICF)
 * <p>
 * Based on Microsoft Office Word 97-2007 Binary File Format (.doc)
 * Specification; Page 181 of 210
 * 
 * @author Sergey Vladimirov ( vlsergey {at} gmail {dot} com )
 */
@Internal
public class PictureDescriptor
{
    private static final int LCB_OFFSET = 0x00;
    private static final int CBHEADER_OFFSET = 0x04;

    private static final int MFP_MM_OFFSET = 0x06;
    private static final int MFP_XEXT_OFFSET = 0x08;
    private static final int MFP_YEXT_OFFSET = 0x0A;
    private static final int MFP_HMF_OFFSET = 0x0C;

    private static final int DXAGOAL_OFFSET = 0x1C;
    private static final int DYAGOAL_OFFSET = 0x1E;

    private static final int MX_OFFSET = 0x20;
    private static final int MY_OFFSET = 0x22;

    private static final int DXACROPLEFT_OFFSET = 0x24;
    private static final int DYACROPTOP_OFFSET = 0x26;
    private static final int DXACROPRIGHT_OFFSET = 0x28;
    private static final int DYACROPBOTTOM_OFFSET = 0x2A;

    /**
     * Number of bytes in the PIC structure plus size of following picture data
     * which may be a Window's metafile, a bitmap, or the filename of a TIFF
     * file. In the case of a Macintosh PICT picture, this includes the size of
     * the PIC, the standard "x" metafile, and the Macintosh PICT data. See
     * Appendix B for more information.
     */
    protected int lcb;

    /**
     * Number of bytes in the PIC (to allow for future expansion).
     */
    protected int cbHeader;

    /*
     * Microsoft Office Word 97-2007 Binary File Format (.doc) Specification
     * 
     * Page 181 of 210
     * 
     * If a Windows metafile is stored immediately following the PIC structure,
     * the mfp is a Window's METAFILEPICT structure. See
     * http://msdn2.microsoft.com/en-us/library/ms649017(VS.85).aspx for more
     * information about the METAFILEPICT structure and
     * http://download.microsoft.com/download/0/B/E/0BE8BDD7-E5E8-422A-ABFD-
     * 4342ED7AD886/WindowsMetafileFormat(wmf)Specification.pdf for Windows
     * Metafile Format specification.
     * 
     * When the data immediately following the PIC is a TIFF filename,
     * mfp.mm==98 If a bitmap is stored after the pic, mfp.mm==99.
     * 
     * When the PIC describes a bitmap, mfp.xExt is the width of the bitmap in
     * pixels and mfp.yExt is the height of the bitmap in pixels.
     */

    protected int mfp_mm;
    protected int mfp_xExt;
    protected int mfp_yExt;
    protected int mfp_hMF;

    /**
     * <li>Window's bitmap structure when PIC describes a BITMAP (14 bytes)
     * 
     * <li>Rectangle for window origin and extents when metafile is stored --
     * ignored if 0 (8 bytes)
     */
    protected byte[] offset14 = new byte[14];

    /**
     * Horizontal measurement in twips of the rectangle the picture should be
     * imaged within
     */
    protected short dxaGoal = 0;

    /**
     * Vertical measurement in twips of the rectangle the picture should be
     * imaged within
     */
    protected short dyaGoal = 0;

    /**
     * Horizontal scaling factor supplied by user expressed in .001% units
     */
    protected short mx;

    /**
     * Vertical scaling factor supplied by user expressed in .001% units
     */
    protected short my;

    /**
     * The amount the picture has been cropped on the left in twips
     */
    protected short dxaCropLeft = 0;

    /**
     * The amount the picture has been cropped on the top in twips
     */
    protected short dyaCropTop = 0;

    /**
     * The amount the picture has been cropped on the right in twips
     */
    protected short dxaCropRight = 0;

    /**
     * The amount the picture has been cropped on the bottom in twips
     */
    protected short dyaCropBottom = 0;

    public PictureDescriptor()
    {
    }

    public PictureDescriptor( byte[] _dataStream, int startOffset )
    {
        this.lcb = LittleEndian.getInt( _dataStream, startOffset + LCB_OFFSET );
        this.cbHeader = LittleEndian.getUShort( _dataStream, startOffset
                + CBHEADER_OFFSET );

        this.mfp_mm = LittleEndian.getUShort( _dataStream, startOffset
                + MFP_MM_OFFSET );
        this.mfp_xExt = LittleEndian.getUShort( _dataStream, startOffset
                + MFP_XEXT_OFFSET );
        this.mfp_yExt = LittleEndian.getUShort( _dataStream, startOffset
                + MFP_YEXT_OFFSET );
        this.mfp_hMF = LittleEndian.getUShort( _dataStream, startOffset
                + MFP_HMF_OFFSET );

        this.offset14 = LittleEndian.getByteArray( _dataStream,
                startOffset + 0x0E, 14 );

        this.dxaGoal = LittleEndian.getShort( _dataStream, startOffset
                + DXAGOAL_OFFSET );
        this.dyaGoal = LittleEndian.getShort( _dataStream, startOffset
                + DYAGOAL_OFFSET );

        this.mx = LittleEndian.getShort( _dataStream, startOffset + MX_OFFSET );
        this.my = LittleEndian.getShort( _dataStream, startOffset + MY_OFFSET );

        this.dxaCropLeft = LittleEndian.getShort( _dataStream, startOffset
                + DXACROPLEFT_OFFSET );
        this.dyaCropTop = LittleEndian.getShort( _dataStream, startOffset
                + DYACROPTOP_OFFSET );
        this.dxaCropRight = LittleEndian.getShort( _dataStream, startOffset
                + DXACROPRIGHT_OFFSET );
        this.dyaCropBottom = LittleEndian.getShort( _dataStream, startOffset
                + DYACROPBOTTOM_OFFSET );
    }

    @Override
    public String toString()
    {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append( "[PICF]\n" );
        stringBuilder.append( "        lcb           = " ).append( this.lcb )
                .append( '\n' );
        stringBuilder.append( "        cbHeader      = " )
                .append( this.cbHeader ).append( '\n' );

        stringBuilder.append( "        mfp.mm        = " ).append( this.mfp_mm )
                .append( '\n' );
        stringBuilder.append( "        mfp.xExt      = " )
                .append( this.mfp_xExt ).append( '\n' );
        stringBuilder.append( "        mfp.yExt      = " )
                .append( this.mfp_yExt ).append( '\n' );
        stringBuilder.append( "        mfp.hMF       = " )
                .append( this.mfp_hMF ).append( '\n' );

        stringBuilder.append( "        offset14      = " )
                .append( Arrays.toString( this.offset14 ) ).append( '\n' );
        stringBuilder.append( "        dxaGoal       = " )
                .append( this.dxaGoal ).append( '\n' );
        stringBuilder.append( "        dyaGoal       = " )
                .append( this.dyaGoal ).append( '\n' );

        stringBuilder.append( "        dxaCropLeft   = " )
                .append( this.dxaCropLeft ).append( '\n' );
        stringBuilder.append( "        dyaCropTop    = " )
                .append( this.dyaCropTop ).append( '\n' );
        stringBuilder.append( "        dxaCropRight  = " )
                .append( this.dxaCropRight ).append( '\n' );
        stringBuilder.append( "        dyaCropBottom = " )
                .append( this.dyaCropBottom ).append( '\n' );

        stringBuilder.append( "[/PICF]" );
        return stringBuilder.toString();
    }
}

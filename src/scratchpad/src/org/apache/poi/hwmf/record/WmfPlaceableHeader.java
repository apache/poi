package org.apache.poi.hwmf.record;

import java.io.IOException;

import org.apache.poi.util.LittleEndianConsts;
import org.apache.poi.util.LittleEndianInputStream;

public class WmfPlaceableHeader {
    public static int WMF_HEADER_MAGIC = 0x9AC6CDD7;
    
    protected WmfPlaceableHeader(LittleEndianInputStream leis) throws IOException {
        /*
         * HWmf (2 bytes):  The resource handle to the metafile, when the metafile is in memory. When
         * the metafile is on disk, this field MUST contain 0x0000. This attribute of the metafile is
         * specified in the Type field of the META_HEADER record.
         */
        leis.readShort(); // ignore
        
        /*
         * BoundingBox (8 bytes):  The destination rectangle, measured in logical units, for displaying
         * the metafile. The size of a logical unit is specified by the Inch field.
         */
        int x1 = leis.readShort();
        int y1 = leis.readShort();
        int x2 = leis.readShort();
        int y2 = leis.readShort();
        
        /*
         * Inch (2 bytes):  The number of logical units per inch used to represent the image.
         * This value can be used to scale an image.
         * By convention, an image is considered to be recorded at 1440 logical units (twips) per inch.
         * Thus, a value of 720 specifies that the image SHOULD be rendered at twice its normal size,
         * and a value of 2880 specifies that the image SHOULD be rendered at half its normal size.
         */
        int inch = leis.readShort();
        
        /*
         * Reserved (4 bytes):  A field that is not used and MUST be set to 0x00000000.
         */
        leis.readInt();
        
        /*
         * Checksum (2 bytes):  A checksum for the previous 10 16-bit values in the header.
         * This value can be used to determine whether the metafile has become corrupted.
         */
        leis.readShort();
        
    }
    
    public static WmfPlaceableHeader readHeader(LittleEndianInputStream leis) throws IOException {
        leis.mark(LittleEndianConsts.INT_SIZE);
        int magic = leis.readInt();
        if (magic == WMF_HEADER_MAGIC) {
            return new WmfPlaceableHeader(leis);
        } else {
            leis.reset();
            return null;
        }
    }
}

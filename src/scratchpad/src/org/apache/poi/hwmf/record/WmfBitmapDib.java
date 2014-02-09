package org.apache.poi.hwmf.record;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.util.LittleEndianConsts;
import org.apache.poi.util.LittleEndianInputStream;

/**
 * The DeviceIndependentBitmap Object defines an image in device-independent bitmap (DIB) format.
 */
public class WmfBitmapDib {
    public static enum BitCount {
        /**
         * The image SHOULD be in either JPEG or PNG format. <6> Neither of these formats includes
         *  a color table, so this value specifies that no color table is present. See [JFIF] and [RFC2083]
         *  for more information concerning JPEG and PNG compression formats.
         */
        BI_BITCOUNT_0(0x0000),
        /**
         * Each pixel in the bitmap is represented by a single bit. If the bit is clear, the pixel is displayed
         *  with the color of the first entry in the color table; if the bit is set, the pixel has the color of the
         *  second entry in the table.
         */
        BI_BITCOUNT_1(0x0001),
        /**
         * Each pixel in the bitmap is represented by a 4-bit index into the color table, and each byte
         *  contains 2 pixels.
         */
        BI_BITCOUNT_2(0x0004),
        /**
         * Each pixel in the bitmap is represented by an 8-bit index into the color table, and each byte
         *  contains 1 pixel.
         */
        BI_BITCOUNT_3(0x0008),
        /**
         * Each pixel in the bitmap is represented by a 16-bit value.
         * <br/>
         * If the Compression field of the BitmapInfoHeader Object is BI_RGB, the Colors field of DIB
         *  is NULL. Each WORD in the bitmap array represents a single pixel. The relative intensities of 
         *  red, green, and blue are represented with 5 bits for each color component. The value for blue 
         *  is in the least significant 5 bits, followed by 5 bits each for green and red. The most significant 
         *  bit is not used. The color table is used for optimizing colors on palette-based devices, and 
         *  contains the number of entries specified by the ColorUsed field of the BitmapInfoHeader 
         *  Object.
         * <br/>
         * If the Compression field of the BitmapInfoHeader Object is BI_BITFIELDS, the Colors field 
         *  contains three DWORD color masks that specify the red, green, and blue components,
         *  respectively, of each pixel. Each WORD in the bitmap array represents a single pixel.
         * <br/>
         * When the Compression field is set to BI_BITFIELDS, bits set in each DWORD mask MUST be 
         *  contiguous and SHOULD NOT overlap the bits of another mask.
         */
        BI_BITCOUNT_4(0x0010),
        /**
         * The bitmap has a maximum of 2^24 colors, and the Colors field of DIB is 
         *  NULL. Each 3-byte triplet in the bitmap array represents the relative intensities of blue, green, 
         *  and red, respectively, for a pixel. The Colors color table is used for optimizing colors used on 
         *  palette-based devices, and MUST contain the number of entries specified by the ColorUsed
         *  field of the BitmapInfoHeader Object.
         */
        BI_BITCOUNT_5(0x0018),
        /**
         * The bitmap has a maximum of 2^24 colors.
         * <br/>
         * If the Compression field of the BitmapInfoHeader Object is set to BI_RGB, the Colors field 
         *  of DIB is set to NULL. Each DWORD in the bitmap array represents the relative intensities of 
         *  blue, green, and red, respectively, for a pixel. The high byte in each DWORD is not used. The 
         *  Colors color table is used for optimizing colors used on palette-based devices, and MUST 
         *  contain the number of entries specified by the ColorUsed field of the BitmapInfoHeader 
         *  Object.
         * <br/>
         * If the Compression field of the BitmapInfoHeader Object is set to BI_BITFIELDS, the Colors
         *  field contains three DWORD color masks that specify the red, green, and blue components, 
         *  respectively, of each pixel. Each DWORD in the bitmap array represents a single pixel.
         * <br/>
         * When the Compression field is set to BI_BITFIELDS, bits set in each DWORD mask must be 
         *  contiguous and should not overlap the bits of another mask. All the bits in the pixel do not 
         *  need to be used.
         */
        BI_BITCOUNT_6(0x0020);
        
        int flag;
        BitCount(int flag) {
            this.flag = flag;
        }
        static BitCount valueOf(int flag) {
            for (BitCount bc : values()) {
                if (bc.flag == flag) return bc;
            }
            return null;
        }
    }

    public static enum Compression {
        /**
         * The bitmap is in uncompressed red green blue (RGB) format that is not compressed
         * and does not use color masks.
         */
        BI_RGB(0x0000),
        /**
         * An RGB format that uses run-length encoding (RLE) compression for bitmaps
         * with 8 bits per pixel. The compression uses a 2-byte format consisting of a count byte
         * followed by a byte containing a color index.
         */
        BI_RLE8(0x0001),
        /**
         * An RGB format that uses RLE compression for bitmaps with 4 bits per pixel. The
         * compression uses a 2-byte format consisting of a count byte followed by two word-length
         * color indexes.
         */
        BI_RLE4(0x0002),
        /**
         * The bitmap is not compressed and the color table consists of three DWORD
         * color masks that specify the red, green, and blue components, respectively, of each pixel.
         * This is valid when used with 16 and 32-bits per pixel bitmaps.
         */
        BI_BITFIELDS(0x0003),
        /**
         * The image is a JPEG image, as specified in [JFIF]. This value SHOULD only be used in 
         * certain bitmap operations, such as JPEG pass-through. The application MUST query for the 
         * pass-through support, since not all devices support JPEG pass-through. Using non-RGB 
         * bitmaps MAY limit the portability of the metafile to other devices. For instance, display device 
         * contexts generally do not support this pass-through.
         */
        BI_JPEG(0x0004),
        /**
         * The image is a PNG image, as specified in [RFC2083]. This value SHOULD only be 
         * used certain bitmap operations, such as JPEG/PNG pass-through. The application MUST query 
         * for the pass-through support, because not all devices support JPEG/PNG pass-through. Using 
         * non-RGB bitmaps MAY limit the portability of the metafile to other devices. For instance, 
         * display device contexts generally do not support this pass-through.
         */
        BI_PNG(0x0005),
        /**
         * The image is an uncompressed CMYK format.
         */
        BI_CMYK(0x000B),
        /**
         * A CMYK format that uses RLE compression for bitmaps with 8 bits per pixel.
         * The compression uses a 2-byte format consisting of a count byte followed by a byte containing
         * a color index.
         */
        BI_CMYKRLE8(0x000C),
        /**
         * A CMYK format that uses RLE compression for bitmaps with 4 bits per pixel.
         * The compression uses a 2-byte format consisting of a count byte followed by two word-length
         * color indexes.
         */
        BI_CMYKRLE4(0x000D);
        
        int flag;
        Compression(int flag) {
            this.flag = flag;
        }
        static Compression valueOf(int flag) {
            for (Compression c : values()) {
                if (c.flag == flag) return c;
            }
            return null;
        }        
    }
    
    
    int headerSize;
    int headerWidth;
    int headerHeight;
    int headerPlanes;
    BitCount headerBitCount;
    Compression headerCompression;
    long headerImageSize = -1;
    int headerXPelsPerMeter = -1;
    int headerYPelsPerMeter = -1;
    long headerColorUsed = -1;
    long headerColorImportant = -1;
    
    Color colorTable[];
    int colorMaskRed=0,colorMaskGreen=0,colorMaskBlue=0;
    
    public int init(LittleEndianInputStream leis) throws IOException {
        int size = 0;
        size += readHeader(leis);
        size += readColors(leis);
        int size2;
        switch (headerBitCount) {
            default:
            case BI_BITCOUNT_0:
                throw new RuntimeException("JPG and PNG formats aren't supported yet.");
            case BI_BITCOUNT_1:
            case BI_BITCOUNT_2:
            case BI_BITCOUNT_3:
                size2 = readBitmapIndexed(leis);
                break;
            case BI_BITCOUNT_4:
            case BI_BITCOUNT_5:
            case BI_BITCOUNT_6:
                size2 = readBitmapDirect(leis);
                break;
        }
        
        assert( headerSize != 0x0C || ((((headerWidth * headerPlanes * headerBitCount.flag + 31) & ~31) / 8) * Math.abs(headerHeight)) == size2);
        assert ( headerSize == 0x0C || headerImageSize == size2 );
        
        size += size2;
        
        return size;
    }

    protected int readHeader(LittleEndianInputStream leis) throws IOException {
        int size = 0;
        
        /**
         * DIBHeaderInfo (variable): Either a BitmapCoreHeader Object or a
         * BitmapInfoHeader Object that specifies information about the image.
         * 
         * The first 32 bits of this field is the HeaderSize value.
         * If it is 0x0000000C, then this is a BitmapCoreHeader; otherwise, this is a BitmapInfoHeader.
         */
        headerSize = leis.readInt();
        size += LittleEndianConsts.INT_SIZE;
        
        // BitmapCoreHeader
        // A 16-bit unsigned integer that defines the width of the DIB, in pixels.
        headerWidth = leis.readUShort();
        // A 16-bit unsigned integer that defines the height of the DIB, in pixels.
        headerHeight = leis.readUShort();
        // A 16-bit unsigned integer that defines the number of planes for the target
        // device. This value MUST be 0x0001.
        headerPlanes = leis.readUShort();
        // A 16-bit unsigned integer that defines the format of each pixel, and the
        // maximum number of colors in the DIB.
        headerBitCount = BitCount.valueOf(leis.readUShort());
        size += 4*LittleEndianConsts.SHORT_SIZE;

        if (headerSize > 0x0C) {
            // BitmapInfoHeader
            // A 32-bit unsigned integer that defines the compression mode of the 
            // DIB. 
            // This value MUST NOT specify a compressed format if the DIB is a top-down bitmap,
            // as indicated by the Height value.
            headerCompression = Compression.valueOf((int)leis.readUInt());
            // A 32-bit unsigned integer that defines the size, in bytes, of the image.
            // If the Compression value is BI_RGB, this value SHOULD be zero and MUST be ignored.
            // If the Compression value is BI_JPEG or BI_PNG, this value MUST specify the size of the JPEG 
            // or PNG image buffer, respectively.
            headerImageSize = leis.readUInt();
            // A 32-bit signed integer that defines the horizontal resolution, 
            // in pixels-per-meter, of the target device for the DIB.
            headerXPelsPerMeter = leis.readInt();
            // A 32-bit signed integer that defines the vertical resolution,
            headerYPelsPerMeter = leis.readInt();
            // A 32-bit unsigned integer that specifies the number of indexes in the
            // color table used by the DIB
            // in pixelsper-meter, of the target device for the DIB.
            headerColorUsed = leis.readUInt();
            // A 32-bit unsigned integer that defines the number of color indexes that are
            // required for displaying the DIB. If this value is zero, all color indexes are required.
            headerColorImportant = leis.readUInt();
            size += 6*LittleEndianConsts.INT_SIZE;
        }
        return size;
    }

    protected int readColors(LittleEndianInputStream leis) throws IOException {
        switch (headerBitCount) {
        default:
        case BI_BITCOUNT_0:
            // no table
            return 0;
        case BI_BITCOUNT_1:
            // 2 colors
            return readRGBQuad(leis, 2);
        case BI_BITCOUNT_2:
            // 16 colors
            return readRGBQuad(leis, 16);
        case BI_BITCOUNT_3:
            // 256 colors
            return readRGBQuad(leis, 256);
        case BI_BITCOUNT_5:
            colorMaskRed=0xFF;
            colorMaskGreen=0xFF;
            colorMaskBlue=0xFF;
            return 0;
        case BI_BITCOUNT_4:
            if (headerCompression == Compression.BI_RGB) {
                colorMaskBlue = 0x1F;
                colorMaskGreen = 0x1F<<5;
                colorMaskRed = 0x1F<<10;
                return 0;
            } else {
                assert(headerCompression == Compression.BI_BITFIELDS);
                colorMaskBlue = leis.readInt();
                colorMaskGreen = leis.readInt();
                colorMaskRed = leis.readInt();
                return 3*LittleEndianConsts.INT_SIZE;
            }
        case BI_BITCOUNT_6:    
            if (headerCompression == Compression.BI_RGB) {
                colorMaskBlue = colorMaskGreen = colorMaskRed = 0xFF;
                return 0;
            } else {
                assert(headerCompression == Compression.BI_BITFIELDS);
                colorMaskBlue = leis.readInt();
                colorMaskGreen = leis.readInt();
                colorMaskRed = leis.readInt();
                return 3*LittleEndianConsts.INT_SIZE;
            }
        }
    }
    
    protected int readRGBQuad(LittleEndianInputStream leis, int count) throws IOException {
        int size = 0;
        List<Color> colorList = new ArrayList<Color>();
        for (int i=0; i<count; i++) {
            int blue = leis.readUByte();
            int green = leis.readUByte();
            int red = leis.readUByte();
            @SuppressWarnings("unused")
            int reserved = leis.readUByte();
            Color c = new Color(red, green, blue);
            colorList.add(c);
            size += 4 * LittleEndianConsts.BYTE_SIZE;
        }
        colorTable = colorList.toArray(new Color[colorList.size()]);
        return size;
    }
    
    protected int readBitmapIndexed(LittleEndianInputStream leis) throws IOException {
        assert(colorTable != null);
        byte r[] = new byte[colorTable.length];
        byte g[] = new byte[colorTable.length];
        byte b[] = new byte[colorTable.length];
        for (int i=0; i<colorTable.length; i++) {
            r[i] = (byte)colorTable[i].getRed();
            g[i] = (byte)colorTable[i].getGreen();
            b[i] = (byte)colorTable[i].getBlue();
        }
        int bits = 32-Integer.numberOfLeadingZeros(colorTable.length);
        IndexColorModel cm = new IndexColorModel(bits,colorTable.length,r,g,b);
        
        BufferedImage bi = new BufferedImage(headerWidth, headerHeight, BufferedImage.TYPE_BYTE_INDEXED, cm);
        WritableRaster wr = bi.getRaster();
        
        int pixelCount = headerWidth*headerHeight;
        int size = 0;
        for (int pixel=0; pixel<pixelCount; size++) {
            int v = leis.readUByte();
            switch (headerBitCount) {
            default:
                throw new RuntimeException("invalid bitcount for indexed image");
            case BI_BITCOUNT_1:
                for (int j=0; j<8 && pixel<pixelCount; j++,pixel++) {
                    wr.setSample(pixel/headerWidth,pixel%headerWidth,0,(v>>(7-j))&1);
                }
                break;
            case BI_BITCOUNT_2:
                wr.setSample(pixel/headerWidth, pixel%headerWidth, 0, (v>>4)&15);
                pixel++;
                if (pixel<pixelCount) {
                    wr.setSample(pixel/headerWidth, pixel%headerWidth, 0, v&15);
                    pixel++;
                }
                break;
            case BI_BITCOUNT_3:
                wr.setSample(pixel/headerWidth, pixel%headerWidth, 0, v);
                pixel++;
                break;
            }
        }
        return size;
    }
    
    protected int readBitmapDirect(LittleEndianInputStream leis) throws IOException {
        assert(colorTable == null);
        
        BufferedImage bi = new BufferedImage(headerWidth, headerHeight, BufferedImage.TYPE_INT_RGB);
        WritableRaster wr = bi.getRaster();
        
        int bitShiftRed=0,bitShiftGreen=0,bitShiftBlue=0;
        if (headerCompression == Compression.BI_BITFIELDS) {
            bitShiftGreen = 32-Integer.numberOfLeadingZeros(this.colorMaskBlue);
            bitShiftRed = 32-Integer.numberOfLeadingZeros(this.colorMaskGreen);
        }
        
        int pixelCount = headerWidth*headerHeight;
        int size = 0;
        int rgb[] = new int[3];
        for (int pixel=0; pixel<pixelCount; pixel++) {
            int v;
            switch (headerBitCount) {
            default:
                throw new RuntimeException("invalid bitcount for indexed image");
            case BI_BITCOUNT_4:
                v = leis.readUShort();
                rgb[0] = (v & colorMaskRed) >> bitShiftRed;
                rgb[1] = (v & colorMaskGreen) >> bitShiftGreen;
                rgb[2] = (v & colorMaskBlue) >> bitShiftBlue;
                size += LittleEndianConsts.SHORT_SIZE;
                break;
            case BI_BITCOUNT_5:
                rgb[2] = leis.readUByte();
                rgb[1] = leis.readUByte();
                rgb[0] = leis.readUByte();
                size += 3*LittleEndianConsts.BYTE_SIZE;
                break;
            case BI_BITCOUNT_6:
                v = leis.readInt();
                rgb[0] = (v & colorMaskRed) >> bitShiftRed;
                rgb[1] = (v & colorMaskGreen) >> bitShiftGreen;
                rgb[2] = (v & colorMaskBlue) >> bitShiftBlue;
                size += LittleEndianConsts.INT_SIZE;
                break;
            }
            wr.setPixel(pixel/headerWidth,pixel%headerWidth,rgb);
        }
        
        return size;
    }
}

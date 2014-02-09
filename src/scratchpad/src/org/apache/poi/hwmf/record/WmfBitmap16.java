package org.apache.poi.hwmf.record;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.apache.poi.util.LittleEndianConsts;
import org.apache.poi.util.LittleEndianInputStream;

public class WmfBitmap16 {
    int type;
    int width;
    int height;
    int widthBytes;
    int planes;
    int bitsPixel;
    
    public int init(LittleEndianInputStream leis) throws IOException {
        // A 16-bit signed integer that defines the bitmap type.
        type = leis.readShort();
        
        // A 16-bit signed integer that defines the width of the bitmap in pixels.
        width = leis.readShort();
        
        // A 16-bit signed integer that defines the height of the bitmap in scan lines.
        height = leis.readShort();
        
        // A 16-bit signed integer that defines the number of bytes per scan line.
        widthBytes = leis.readShort();
        
        // An 8-bit unsigned integer that defines the number of color planes in the 
        // bitmap. The value of this field MUST be 0x01.
        planes = leis.readUByte();
        
        // An 8-bit unsigned integer that defines the number of adjacent color bits on 
        // each plane.
        bitsPixel = leis.readUByte();
        
        BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        int size = 2*LittleEndianConsts.BYTE_SIZE+4*LittleEndianConsts.SHORT_SIZE;
        
        int size2 = 0;
        byte buf[] = new byte[widthBytes];
        for (int h=0; h<height; h++) {
            leis.read(buf);
            size2 += widthBytes;

            BitInputStream bis = new  BitInputStream(new ByteArrayInputStream(buf));
            for (int w=0; w<width; w++) {
                int bitsAtPixel = bis.read(bitsPixel);
                // TODO: is bitsPixel a multiple of 3 (r,g,b)
                // which colortable should be used for the various bit sizes???
                
            }
        }
        
        int bytes = (((width * bitsPixel + 15) >> 4) << 1) * height;
        assert (bytes == size2);

        size += size2;
        
        
        return size;
    }
}

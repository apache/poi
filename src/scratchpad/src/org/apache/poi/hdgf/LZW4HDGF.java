/**
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; version 3 of the License.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */
package org.apache.poi.hdgf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * A decoder for the crazy LZW implementation used
 *  in Visio.
 * This is a port of vsd_inflate.c from vsdump
 *  (http://www.gnome.ru/projects/vsdump_en.html)
 */
public class LZW4HDGF {

public byte fromInt(int b) {
	if(b < 128) return (byte)b;
	return (byte)(b - 256);
}

public byte[] decode(InputStream src) throws IOException {
	ByteArrayOutputStream res = new ByteArrayOutputStream();
	int pos = 0;
	int flag;
	byte[] buffer = new byte[4096];
	buffer[0] = 0;
	
	byte data;
	int tmp;
	int addr1, addr2; 
	int len, pntr;

	while ( (flag = src.read()) != -1 ) {
		for (int mask = 1; mask < 0x100 ; mask <<= 1) {
			if ( (flag & mask) > 0) {
				if( (tmp = src.read()) != -1) {
					buffer[(pos&4095)] = fromInt(tmp);
					pos++;
					res.write( new byte[] {fromInt(tmp)} );
				}
			} else {
				tmp = src.read();
				if(tmp == -1) break;
				addr1 = tmp;
                                                            
				tmp = src.read();
				if(tmp == -1) break;
				addr2 = tmp;
				
				len = (addr2 & 15) + 3;
				pntr = (addr2 & 240)*16 + addr1;
                
				if(pntr > 4078) {
					pntr = pntr - 4078;
				} else {
					pntr = pntr + 18;
				}
				
				for(int i=0; i<len; i++) {
					buffer [(pos + i) & 4095] = buffer [(pntr + i) & 4095];
					data = buffer[(pntr + i ) & 4095];
					res.write(new byte[] {data});
				}
                                    
				pos = pos + len;
			}
		}
    }
    return res.toByteArray();
}
}

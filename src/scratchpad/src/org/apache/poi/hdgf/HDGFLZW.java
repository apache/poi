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
package org.apache.poi.hdgf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A decoder for the crazy LZW implementation used
 *  in Visio.
 * According to VSDump, "it's a slightly perverted version of LZW
 *  compression, with inverted meaning of flag byte and 0xFEE as an
 *  'initial shift'". It uses 12 bit codes
 * (http://www.gnome.ru/projects/vsdump_en.html)
 *
 * Two good resources on LZW are:
 *  http://en.wikipedia.org/wiki/LZW
 *  http://marknelson.us/1989/10/01/lzw-data-compression/
 */
public class HDGFLZW {

/**
 * Given an integer, turn it into a java byte, handling
 *  the wrapping.
 * This is a convenience method
 */
public static byte fromInt(int b) {
	if(b < 128) return (byte)b;
	return (byte)(b - 256);
}
/**
 * Given a java byte, turn it into an integer between 0
 *  and 255 (i.e. handle the unwrapping).
 * This is a convenience method
 */
public static int fromByte(byte b) {
	if(b >= 0) {
		return b;
	}
	return b + 256;
}

/**
 * Compress the given input stream, returning the array of bytes
 *  of the compressed input
 */
public byte[] compress(InputStream src) throws IOException {
	ByteArrayOutputStream res = new ByteArrayOutputStream();
	compress(src,res);
    return res.toByteArray();
}

/**
 * Decompresses the given input stream, returning the array of bytes
 *  of the decompressed input.
 */
public byte[] decode(InputStream src) throws IOException {
	ByteArrayOutputStream res = new ByteArrayOutputStream();
	decode(src,res);
    return res.toByteArray();
}
/**
 * Perform a streaming decompression of the input.
 * Works by:
 * 1) Reading a flag byte, the 8 bits of which tell you if the
 *     following 8 codes are compressed our un-compressed
 * 2) Consider the 8 bits in turn
 * 3) If the bit is set, the next code is un-compressed, so
 *     add it to the dictionary and output it
 * 4) If the bit isn't set, then read in the length and start
 *     position in the dictionary, and output the bytes there
 * 5) Loop until we've done all 8 bits, then read in the next
 *     flag byte
 */
public void decode(InputStream src, OutputStream res) throws IOException {
	// We use 12 bit codes:
	// * 0-255 are real bytes
	// * 256-4095 are the substring codes
	// Java handily initialises our buffer / dictionary
	//  to all zeros
	byte[] buffer = new byte[4096];

	// How far through the output we've got
	// (This is normally used &4095, so it nicely wraps)
	int pos = 0;
	// The flag byte is treated as its 8 individual
	//  bits, which tell us if the following 8 codes
	//  are compressed or un-compressed
	int flag;
	// The mask, between 1 and 255, which is used when
	//  processing each bit of the flag byte in turn
	int mask;

	// This is a byte as looked up in the dictionary
	// It needs to be signed, as it'll get passed on to
	//  the output stream
	byte dataB;
	// This is an unsigned byte read from the stream
	// It needs to be unsigned, so that bit stuff works
	int dataI;
	// The compressed code sequence is held over 2 bytes
	int dataIPt1, dataIPt2;
	// How long a code sequence is, and where in the
	//  dictionary to start at
	int len, pntr;

	while( (flag = src.read()) != -1 ) {
		// Compare each bit in our flag byte in turn:
		for(mask = 1; mask < 256 ; mask <<= 1) {
			// Is this a new code (un-compressed), or
			//  the use of existing codes (compressed)?
			if( (flag & mask) > 0 ) {
				// Retrieve the un-compressed code
				if( (dataI = src.read()) != -1) {
					// Save the byte into the dictionary
					buffer[(pos&4095)] = fromInt(dataI);
					pos++;
					// And output the byte
					res.write( new byte[] {fromInt(dataI)} );
				}
			} else {
				// We have a compressed sequence
				// Grab the next 16 bits of data
				dataIPt1 = src.read();
				dataIPt2 = src.read();
				if(dataIPt1 == -1 || dataIPt2 == -1) break;

				// Build up how long the code sequence is, and
				//  what position of the code to start at
				// (The position is the first 12 bits, the
				//  length is the last 4 bits)
				len = (dataIPt2 & 15) + 3;
				pntr = (dataIPt2 & 240)*16 + dataIPt1;

				// If the pointer happens to be passed the end
				//  of our buffer, then wrap around
				if(pntr > 4078) {
					pntr = pntr - 4078;
				} else {
					pntr = pntr + 18;
				}

				// Loop over the codes, outputting what they correspond to
				for(int i=0; i<len; i++) {
					buffer [(pos + i) & 4095] = buffer [(pntr + i) & 4095];
					dataB = buffer[(pntr + i) & 4095];
					res.write(new byte[] {dataB});
				}

				// Record how far along the stream we have moved
				pos = pos + len;
			}
		}
    }
}

/**
 * Performs the Visio compatible streaming LZW compression.
 * TODO - Finish
 */
public void compress(InputStream src, OutputStream res) throws IOException {
	Compressor c = new Compressor();
	c.compress(src, res);
}

/**
 * Helper class to handle the Visio compatible
 *  streaming LZW compression.
 * Need our own class to handle keeping track of the
 *  code buffer, pending bytes to write out etc.
 */
private static final class Compressor {
	// We use 12 bit codes:
	// * 0-255 are real bytes
	// * 256-4095 are the substring codes
	// Java handily initialises our buffer / dictionary
	//  to all zeros
	byte[] dict = new byte[4096];

	// The next block of data to be written out, minus
	//  its mask byte
	byte[] buffer = new byte[16];
	// And how long it is
	// (Un-compressed codes are 1 byte each, compressed codes
	//   are two)
	int bufferLen = 0;

	// The raw length of a code is limited to 4 bits
	byte[] rawCode = new byte[16];
	// And how much we're using
	int rawCodeLen = 0;

	// How far through the input and output streams we are
	int posInp = 0;
	int posOut = 0;

	// What the next mask byte to output will be
	int nextMask = 0;
	// And how many bits we've already set
	int maskBitsSet = 0;

	public Compressor() {
		//
	}
/**
 * Returns the last place that the bytes from rawCode are found
 *  at in the buffer, or -1 if they can't be found
 */
private int findRawCodeInBuffer() {
	// Work our way back from the end
	// (Visio always seems to use the last possible code)
	for(int i=(buffer.length - rawCodeLen); i>=0; i--) {
		boolean matches = true;
		for(int j=0; matches && j<rawCodeLen; j++) {
			if(buffer[i] == rawCode[j]) {
				// Fits
			} else {
				// Doesn't fit, can't be a match
				matches = false;
			}
		}

		// Was this position a match?
		if(matches) {
			return i;
		}
	}

	// Not found
	return -1;
}

/**
 * Output the compressed representation for the bytes
 *  found in rawCode
 */
private void outputCompressed(OutputStream res) throws IOException {
	// It's not worth compressing only 1 or two bytes,
	//  due to the overheads
	// So if asked, just output uncompressed
	if(rawCodeLen < 3) {
		for(int i=0; i<rawCodeLen; i++) {
			outputUncompressed(rawCode[i], res);
		}
		return;
	}

	// Increment the mask bit count, we've done another code
	maskBitsSet++;
	// Add the length+code to the buffer
	// (The position is the first 12 bits, the
	//  length is the last 4 bits)
	// TODO
	posOut += 2;

	// If we're now at 8 codes, output
	if(maskBitsSet == 8) {
		output8Codes(res);
	}
}
/**
 * Output the un-compressed byte
 */
private void outputUncompressed(byte b, OutputStream res) throws IOException {
	// Set the mask bit for us
	nextMask += (1<<maskBitsSet);

	// And add us to the buffer + dictionary
	buffer[bufferLen] = fromInt(b);
	bufferLen++;
	dict[(posOut&4095)] = fromInt(b);
	posOut++;

	// If we're now at 8 codes, output
	if(maskBitsSet == 8) {
		output8Codes(res);
	}
}

/**
 * We've got 8 code worth to write out, so
 *  output along with the header
 */
private void output8Codes(OutputStream res) throws IOException {
	// Output the mask and the data
	res.write(new byte[] { fromInt(nextMask) } );
	res.write(buffer, 0, bufferLen);

	// Reset things
	nextMask = 0;
	maskBitsSet = 0;
	bufferLen = 0;
}

/**
 * Does the compression
 */
public void compress(InputStream src, OutputStream res) throws IOException {
	// Have we hit the end of the file yet?
	boolean going = true;

	// This is a byte as looked up in the dictionary
	// It needs to be signed, as it'll get passed on to
	//  the output stream
	byte dataB;
	// This is an unsigned byte read from the stream
	// It needs to be unsigned, so that bit stuff works
	int dataI;

	while( going ) {
		dataI = src.read();
		posInp++;
		if(dataI == -1) { going = false; }
		dataB = fromInt(dataI);

		// If we've run out of data, output anything that's
		//  pending then finish
		if(!going && rawCodeLen > 0) {
			outputCompressed(res);
			break;
		}

		// Try adding this new byte onto rawCode, and
		//  see if all of that is still found in the
		//  buffer dictionary or not
		rawCode[rawCodeLen] = dataB;
		rawCodeLen++;
		int rawAt = findRawCodeInBuffer();

		// If we found it and are now at 16 bytes,
		//  we need to output our pending code block
		if(rawCodeLen == 16 && rawAt > -1) {
			outputCompressed(res);
			rawCodeLen = 0;
			continue;
		}

		// If we did find all of rawCode with our new
		//  byte added on, we can wait to see what happens
		//  with the next byte
		if(rawAt > -1) {
			continue;
		}

		// If we get here, then the rawCode + this byte weren't
		// found in the dictionary

		// If there was something in rawCode before, then that was
		// found in the dictionary, so output that compressed
		rawCodeLen--;
		if(rawCodeLen > 0) {
			// Output the old rawCode
			outputCompressed(res);

			// Can this byte start a new rawCode, or does
			//  it need outputting itself?
			rawCode[0] = dataB;
			rawCodeLen = 1;
			if(findRawCodeInBuffer() > -1) {
				// Fits in, wait for next byte
				continue;
			}
			// Doesn't fit, output
			outputUncompressed(dataB,res);
			rawCodeLen = 0;
		} else {
			// Nothing in rawCode before, so this byte
			//  isn't in the buffer dictionary
			// Output it un-compressed
			outputUncompressed(dataB,res);
		}
	}
}
}

}

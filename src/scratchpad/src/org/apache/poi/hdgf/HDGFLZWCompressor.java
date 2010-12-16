package org.apache.poi.hdgf;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Helper class to handle the Visio compatible
 *  streaming LZW compression.
 * Need our own class to handle keeping track of the
 *  code buffer, pending bytes to write out etc.
 *  
 * TODO Fix this, as it starts to go wrong on
 *  large streams 
 */
final class HDGFLZWCompressor {
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

	// The raw length of a code is limited to 4 bits + 2
	byte[] rawCode = new byte[18];
	// And how much we're using
	int rawCodeLen = 0;

	// How far through the input and output streams we are
	int posInp = 0;
	int posOut = 0;

	// What the next mask byte to output will be
	int nextMask = 0;
	// And how many bits we've already set
	int maskBitsSet = 0;

	public HDGFLZWCompressor() {}
	
/**
 * Returns the last place that the bytes from rawCode are found
 *  at in the buffer, or -1 if they can't be found
 */
private int findRawCodeInBuffer() {
	// Work our way through all the codes until we
   //  find the right one. Visio starts from the end
	for(int i=4096-rawCodeLen; i>0; i--) {
		boolean matches = true;
		for(int j=0; matches && j<rawCodeLen; j++) {
			if(dict[i+j] == rawCode[j]) {
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
	
	// Grab where the data lives
	int codesAt = findRawCodeInBuffer();
   codesAt -= 18;
	if(codesAt < 0) {
	   codesAt += 4096;
	}

	// Increment the mask bit count, we've done another code
	maskBitsSet++;
	
	// Add the length+code to the buffer
	// (The position is the first 12 bits, the
	//  length is the last 4 bits)
	int bp1 = (codesAt & 255);
	int bp2 = (rawCodeLen-3) + ((codesAt-bp1) >> 4);
	buffer[bufferLen] = HDGFLZW.fromInt(bp1);
	bufferLen++;
   buffer[bufferLen] = HDGFLZW.fromInt(bp2);
   bufferLen++;
   
   // Copy the data to the dictionary in the new place
   for(int i=0; i<rawCodeLen; i++) {
      dict[(posOut&4095)] = rawCode[i];
      posOut++; 
   }

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
	maskBitsSet++;

	// And add us to the buffer + dictionary
	buffer[bufferLen] = b;
	bufferLen++;
	dict[(posOut&4095)] = b;
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
	res.write(new byte[] { HDGFLZW.fromInt(nextMask) } );
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
		dataB = HDGFLZW.fromInt(dataI);

		// If we've run out of data, output anything that's
		//  pending then finish
		if(!going) {
		   if(rawCodeLen > 0) {
	         outputCompressed(res);
	         if(maskBitsSet > 0) {
	            output8Codes(res);
	         }
		   }
			break;
		}

		// Try adding this new byte onto rawCode, and
		//  see if all of that is still found in the
		//  buffer dictionary or not
		rawCode[rawCodeLen] = dataB;
		rawCodeLen++;
		int rawAt = findRawCodeInBuffer();
		
		// If we found it and are now at 18 bytes,
		//  we need to output our pending code block
		if(rawCodeLen == 18 && rawAt > -1) {
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
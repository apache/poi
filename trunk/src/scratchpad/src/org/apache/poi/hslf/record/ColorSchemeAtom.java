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

package org.apache.poi.hslf.record;

import org.apache.poi.util.LittleEndian;
import java.io.IOException;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;

/**
 * A ColorSchemeAtom (type 2032). Holds the 8 RGB values for the different
 *  colours of bits of text, that makes up a given colour scheme.
 * Slides (presumably) link to a given colour scheme atom, and that
 *  defines the colours to be used
 *
 * @author Nick Burch
 */
public final class ColorSchemeAtom extends RecordAtom {
	private byte[] _header;
	private static long _type = 2032l;

	private int backgroundColourRGB;
	private int textAndLinesColourRGB;
	private int shadowsColourRGB;
	private int titleTextColourRGB;
	private int fillsColourRGB;
	private int accentColourRGB;
	private int accentAndHyperlinkColourRGB;
	private int accentAndFollowingHyperlinkColourRGB;

	/** Fetch the RGB value for Background Colour */
	public int getBackgroundColourRGB() { return backgroundColourRGB; }
	/** Set the RGB value for Background Colour */
	public void setBackgroundColourRGB(int rgb) { backgroundColourRGB = rgb; }

	/** Fetch the RGB value for Text And Lines Colour */
	public int getTextAndLinesColourRGB() { return textAndLinesColourRGB; }
	/** Set the RGB value for Text And Lines Colour */
	public void setTextAndLinesColourRGB(int rgb) { textAndLinesColourRGB = rgb; }

	/** Fetch the RGB value for Shadows Colour */
	public int getShadowsColourRGB() { return shadowsColourRGB; }
	/** Set the RGB value for Shadows Colour */
	public void setShadowsColourRGB(int rgb) { shadowsColourRGB = rgb; }

	/** Fetch the RGB value for Title Text Colour */
	public int getTitleTextColourRGB() { return titleTextColourRGB; }
	/** Set the RGB value for Title Text Colour */
	public void setTitleTextColourRGB(int rgb) { titleTextColourRGB = rgb; }

	/** Fetch the RGB value for Fills Colour */
	public int getFillsColourRGB() { return fillsColourRGB; }
	/** Set the RGB value for Fills Colour */
	public void setFillsColourRGB(int rgb) { fillsColourRGB = rgb; }

	/** Fetch the RGB value for Accent Colour */
	public int getAccentColourRGB() { return accentColourRGB; }
	/** Set the RGB value for Accent Colour */
	public void setAccentColourRGB(int rgb) { accentColourRGB = rgb; }

	/** Fetch the RGB value for Accent And Hyperlink Colour */
	public int getAccentAndHyperlinkColourRGB()
		{ return accentAndHyperlinkColourRGB; }
	/** Set the RGB value for Accent And Hyperlink Colour */
	public void setAccentAndHyperlinkColourRGB(int rgb)
			{ accentAndHyperlinkColourRGB = rgb; }

	/** Fetch the RGB value for Accent And Following Hyperlink Colour */
	public int getAccentAndFollowingHyperlinkColourRGB()
		{ return accentAndFollowingHyperlinkColourRGB; }
	/** Set the RGB value for Accent And Following Hyperlink Colour */
	public void setAccentAndFollowingHyperlinkColourRGB(int rgb)
			{ accentAndFollowingHyperlinkColourRGB = rgb; }

	/* *************** record code follows ********************** */

	/**
	 * For the Colour Scheme (ColorSchem) Atom
	 */
	protected ColorSchemeAtom(byte[] source, int start, int len) {
		// Sanity Checking - we're always 40 bytes long
		if(len < 40) {
			len = 40;
			if(source.length - start < 40) {
				throw new RuntimeException("Not enough data to form a ColorSchemeAtom (always 40 bytes long) - found " + (source.length - start));
			}
		}

		// Get the header
		_header = new byte[8];
		System.arraycopy(source,start,_header,0,8);

		// Grab the rgb values
		backgroundColourRGB = LittleEndian.getInt(source,start+8+0);
		textAndLinesColourRGB = LittleEndian.getInt(source,start+8+4);
		shadowsColourRGB = LittleEndian.getInt(source,start+8+8);
		titleTextColourRGB = LittleEndian.getInt(source,start+8+12);
		fillsColourRGB = LittleEndian.getInt(source,start+8+16);
		accentColourRGB = LittleEndian.getInt(source,start+8+20);
		accentAndHyperlinkColourRGB = LittleEndian.getInt(source,start+8+24);
		accentAndFollowingHyperlinkColourRGB = LittleEndian.getInt(source,start+8+28);
	}

	/**
	 * Create a new ColorSchemeAtom, to go with a new Slide
	 */
	public ColorSchemeAtom(){
		_header = new byte[8];
		LittleEndian.putUShort(_header, 0, 16);
		LittleEndian.putUShort(_header, 2, (int)_type);
		LittleEndian.putInt(_header, 4, 32);

		// Setup the default rgb values
		backgroundColourRGB = 16777215;
		textAndLinesColourRGB = 0;
		shadowsColourRGB = 8421504;
		titleTextColourRGB = 0;
		fillsColourRGB = 10079232;
		accentColourRGB = 13382451;
		accentAndHyperlinkColourRGB = 16764108;
		accentAndFollowingHyperlinkColourRGB = 11711154;
	}


	/**
	 * We are of type 3999
	 */
	public long getRecordType() { return _type; }


	/**
	 * Convert from an integer RGB value to individual R, G, B 0-255 values
	 */
	public static byte[] splitRGB(int rgb) {
		byte[] ret = new byte[3];

		// Serialise to bytes, then grab the right ones out
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			writeLittleEndian(rgb,baos);
		} catch(IOException ie) {
			// Should never happen
			throw new RuntimeException(ie);
		}
		byte[] b = baos.toByteArray();
		System.arraycopy(b,0,ret,0,3);

		return ret;
	}

	/**
	 * Convert from split R, G, B values to an integer RGB value
	 */
	public static int joinRGB(byte r, byte g, byte b) {
		return joinRGB(new byte[] { r,g,b });
	}
	/**
	 * Convert from split R, G, B values to an integer RGB value
	 */
	public static int joinRGB(byte[] rgb) {
		if(rgb.length != 3) {
			throw new RuntimeException("joinRGB accepts a byte array of 3 values, but got one of " + rgb.length + " values!");
		}
		byte[] with_zero = new byte[4];
		System.arraycopy(rgb,0,with_zero,0,3);
		with_zero[3] = 0;
		int ret = LittleEndian.getInt(with_zero,0);
		return ret;
	}


	/**
	 * Write the contents of the record back, so it can be written
	 *  to disk
	 */
	public void writeOut(OutputStream out) throws IOException {
		// Header - size or type unchanged
		out.write(_header);

		// Write out the rgb values
		writeLittleEndian(backgroundColourRGB,out);
		writeLittleEndian(textAndLinesColourRGB,out);
		writeLittleEndian(shadowsColourRGB,out);
		writeLittleEndian(titleTextColourRGB,out);
		writeLittleEndian(fillsColourRGB,out);
		writeLittleEndian(accentColourRGB,out);
		writeLittleEndian(accentAndHyperlinkColourRGB,out);
		writeLittleEndian(accentAndFollowingHyperlinkColourRGB,out);
	}

	/**
	 * Returns color by its index
	 *
	 * @param idx 0-based color index
	 * @return color by its index
	 */
	public int getColor(int idx){
		int[] clr = {backgroundColourRGB, textAndLinesColourRGB, shadowsColourRGB, titleTextColourRGB,
				fillsColourRGB, accentColourRGB, accentAndHyperlinkColourRGB, accentAndFollowingHyperlinkColourRGB};
		return clr[idx];
	}
}

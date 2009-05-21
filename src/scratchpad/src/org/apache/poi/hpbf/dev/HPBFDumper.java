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

package org.apache.poi.hpbf.dev;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.ddf.DefaultEscherRecordFactory;
import org.apache.poi.ddf.EscherRecord;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.DocumentEntry;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.StringUtil;

/**
 * For dumping out the contents of HPBF (Publisher)
 *  files, while we try to figure out how they're
 *  constructed.
 */
public final class HPBFDumper {
	private POIFSFileSystem fs;
	public HPBFDumper(POIFSFileSystem fs) {
		this.fs = fs;
	}
	public HPBFDumper(InputStream inp) throws IOException {
		this(new POIFSFileSystem(inp));
	}

	private static byte[] getData(DirectoryNode dir, String name) throws IOException {
		DocumentEntry docProps =
			(DocumentEntry)dir.getEntry(name);

		// Grab the document stream
		byte[] d = new byte[docProps.getSize()];
		dir.createDocumentInputStream(name).read(d);

		// All done
		return d;
	}

	/**
	 * Dumps out the given number of bytes as hex,
	 *  two chars
	 */
	private String dumpBytes(byte[] data, int offset, int len) {
		StringBuffer ret = new StringBuffer();
		for(int i=0; i<len; i++) {
			int j = i + offset;
			int b = data[j];
			if(b < 0) { b += 256; }

			String bs = Integer.toHexString(b);
			if(bs.length() == 1)
				ret.append('0');
			ret.append(bs);
			ret.append(' ');
		}
		return ret.toString();
	}

	public static void main(String[] args) throws Exception {
		if(args.length < 1) {
			System.err.println("Use:");
			System.err.println("  HPBFDumper <filename>");
			System.exit(1);
		}
		HPBFDumper dump = new HPBFDumper(
				new FileInputStream(args[0])
		);

		System.out.println("Dumping " + args[0]);
		dump.dumpContents();
		dump.dumpEnvelope();
		dump.dumpEscher();
		dump.dump001CompObj(dump.fs.getRoot());
		dump.dumpQuill();

		// Still to go:
		//  (0x03)Internal
		//  Objects
	}

	/**
	 * Dump out the escher parts of the file.
	 * Escher -> EscherStm and EscherDelayStm
	 */
	public void dumpEscher() throws IOException {
		DirectoryNode escherDir = (DirectoryNode)
			fs.getRoot().getEntry("Escher");

		dumpEscherStm(escherDir);
		dumpEscherDelayStm(escherDir);
	}
	private void dumpEscherStream(byte[] data) {
		DefaultEscherRecordFactory erf =
			new DefaultEscherRecordFactory();

		// Dump
		int left = data.length;
		while(left > 0) {
			EscherRecord er = erf.createRecord(data, 0);
			er.fillFields(data, 0, erf);
			left -= er.getRecordSize();

			System.out.println(er.toString());
		}
	}
	protected void dumpEscherStm(DirectoryNode escherDir) throws IOException {
		byte[] data = getData(escherDir, "EscherStm");
		System.out.println("");
		System.out.println("EscherStm - " + data.length + " bytes long:");
		if(data.length > 0)
			dumpEscherStream(data);
	}
	protected void dumpEscherDelayStm(DirectoryNode escherDir) throws IOException {
		byte[] data = getData(escherDir, "EscherDelayStm");
		System.out.println("");
		System.out.println("EscherDelayStm - " + data.length + " bytes long:");
		if(data.length > 0)
			dumpEscherStream(data);
	}

	public void dumpEnvelope() throws IOException {
		byte[] data = getData(fs.getRoot(), "Envelope");

		System.out.println("");
		System.out.println("Envelope - " + data.length + " bytes long:");
	}

	public void dumpContents() throws IOException {
		byte[] data = getData(fs.getRoot(), "Contents");

		System.out.println("");
		System.out.println("Contents - " + data.length + " bytes long:");

		// 8 bytes, always seems to be
		// E8 AC 2C 00 E8 03 05 01
		// E8 AC 2C 00 E8 03 05 01

		// 4 bytes - size of contents
		// 13/15 00 00 01

		// ....

	    // E8 03 08 08 0C 20 03 00 00 00 00 88 16 00 00 00 ..... ..........

	    // 01 18 27 00 03 20 00 00 E8 03 08 08 0C 20 03 00 ..'.. ....... ..

		// 01 18 30 00 03 20 00 00
		// E8 03 06 08 07 08 08 08 09 10 01 00 0C 20 04 00
		// 00 00 00 88 1E 00 00 00

		// 01 18 31 00 03 20 00 00
		// E8 03 06 08 07 08 08 08 09 10 01 00 0C 20 04 00
		// 00 00 00 88 1E 00 00 00

		// 01 18 32 00 03 20 00 00
		// E8 03 06 08 07 08 08 08 09 10 01 00 0C 20 04 00
		// 00 00 00 88 1E 00 00 00
	}

	public void dumpCONTENTSraw(DirectoryNode dir) throws IOException {
		byte[] data = getData(dir, "CONTENTS");

		System.out.println("");
		System.out.println("CONTENTS - " + data.length + " bytes long:");

		// Between the start and 0x200 we have
		//  CHNKINK(space) + 24 bytes
		//  0x1800
		//  TEXT + 6 bytes
		//  TEXT + 8 bytes
		//  0x1800
		//  STSH + 6 bytes
		//  STSH + 8 bytes
		//  0x1800
		//  STSH + 6 bytes
		//  STSH + 8 bytes
		// but towards 0x200 the pattern may
		//  break down a little bit

		// After the second of a given type,
		//  it seems to be 4 bytes giving the start,
		//  then 4 bytes giving the length, then
		//  18 00
		System.out.println(
				new String(data, 0, 8) +
				dumpBytes(data, 8, 0x20-8)
		);

		int pos = 0x20;
		boolean sixNotEight = true;
		while(pos < 0x200) {
			if(sixNotEight) {
				System.out.println(
						dumpBytes(data, pos, 2)
				);
				pos += 2;
			}
			String text = new String(data, pos, 4);
			int blen = 8;
			if(sixNotEight)
				blen = 6;
			System.out.println(
					text + " " + dumpBytes(data, pos+4, blen)
			);

			pos += 4 + blen;
			sixNotEight = ! sixNotEight;
		}

		// Text from 0x200 onwards until we get
		//  to \r(00)\n(00)(00)(00)
		int textStop = -1;
		for(int i=0x200; i<data.length-2 && textStop == -1; i++) {
			if(data[i] == 0 && data[i+1] == 0 && data[i+2] == 0) {
				textStop = i;
			}
		}
		if(textStop > 0) {
			int len = (textStop - 0x200) / 2;
			System.out.println("");
			System.out.println(
					StringUtil.getFromUnicodeLE(data, 0x200, len)
			);
		}

		// The font list comes slightly later

		// The hyperlinks may come before the fonts,
		//  or slightly in front
	}
	public void dumpCONTENTSguessed(DirectoryNode dir) throws IOException {
		byte[] data = getData(dir, "CONTENTS");

		System.out.println("");
		System.out.println("CONTENTS - " + data.length + " bytes long:");

		String[] startType = new String[20];
		String[] endType = new String[20];
		int[] optA = new int[20];
		int[] optB = new int[20];
		int[] optC = new int[20];
		int[] from = new int[20];
		int[] len = new int[20];

		for(int i=0; i<20; i++) {
			int offset = 0x20 + i*24;
			if(data[offset] == 0x18 && data[offset+1] == 0x00) {
				// Has data
				startType[i] = new String(data, offset+2, 4);
				optA[i] = LittleEndian.getUShort(data, offset+6);
				optB[i] = LittleEndian.getUShort(data, offset+8);
				optC[i] = LittleEndian.getUShort(data, offset+10);
				endType[i] = new String(data, offset+12, 4);
				from[i] = (int)LittleEndian.getUInt(data, offset+16);
				len[i] = (int)LittleEndian.getUInt(data, offset+20);
			} else {
				// Doesn't have data
			}
		}

		String text = StringUtil.getFromUnicodeLE(
				data, from[0], len[0]/2
		);

		// Dump
		for(int i=0; i<20; i++) {
			String num = Integer.toString(i);
			if(i < 10) {
				num = "0" + i;
			}
			System.out.print(num + " ");

			if(startType[i] == null) {
				System.out.println("(not present)");
			} else {
				System.out.println(
						"\t" +
						startType[i] + " " +
						optA[i] + " " +
						optB[i] + " " +
						optC[i]
				);
				System.out.println(
						"\t" +
						endType[i] + " " +
						"from: " +
						Integer.toHexString(from[i]) +
						" (" + from[i] + ")" +
						", len: " +
						Integer.toHexString(len[i]) +
						" (" + len[i] + ")"
				);
			}
		}

		// Text
		System.out.println("");
		System.out.println("TEXT:");
		System.out.println(text);
		System.out.println("");

		// All the others
		for(int i=0; i<20; i++) {
			if(startType[i] == null) {
				continue;
			}
			int start = from[i];

			System.out.println(
					startType[i] + " -> " + endType[i] +
					" @ " + Integer.toHexString(start) +
					" (" + start + ")"
			);
			System.out.println("\t" + dumpBytes(data, start, 4));
			System.out.println("\t" + dumpBytes(data, start+4, 4));
			System.out.println("\t" + dumpBytes(data, start+8, 4));
			System.out.println("\t(etc)");
		}
	}

	protected void dump001CompObj(DirectoryNode dir) {
		// TODO
	}

	public void dumpQuill() throws IOException {
		DirectoryNode quillDir = (DirectoryNode)
			fs.getRoot().getEntry("Quill");
		DirectoryNode quillSubDir = (DirectoryNode)
			quillDir.getEntry("QuillSub");

		dump001CompObj(quillSubDir);
		dumpCONTENTSraw(quillSubDir);
		dumpCONTENTSguessed(quillSubDir);
	}
}

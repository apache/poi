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

package org.apache.poi.hslf.dev;

import java.io.*;

import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.poifs.filesystem.DocumentEntry;

import org.apache.poi.ddf.*;
import org.apache.poi.hslf.record.RecordTypes;

import org.apache.poi.util.LittleEndian;

/**
 * This class provides a way to "peek" inside a powerpoint file. It
 *  will print out all the types it find, and for those it know aren't
 *  atoms, what they contain
 *
 * To figure out what things are, and if they are atoms or not, used the
 *  list in hslf.record.RecordTypes
 *
 * To peek inside PPDrawings, which hold Escher drawings, we use the
 *  DDF package from POI (but we can fake it by using the Escher listings
 *  from hslf.record.RecordTypes also)
 *
 * @author Nick Burch
 */
public final class SlideShowDumper {
  private InputStream istream;
  private POIFSFileSystem filesystem;

  private byte[] _docstream;

  /** Do we try to use DDF to understand the escher objects? */
  private boolean ddfEscher = false;
  /** Do we use our own built-in basic escher groker to understand the escher objects? */
  private boolean basicEscher = false;

  /**
   *  right now this function takes one parameter: a ppt file, and outputs
   *  a dump of what it contains
   */
  public static void main(String args[]) throws IOException
  {
	if(args.length == 0) {
		System.err.println("Useage: SlideShowDumper [-escher|-basicescher] <filename>");
		return;
	}

	String filename = args[0];
	if(args.length > 1) {
		filename = args[1];
	}

	SlideShowDumper foo = new SlideShowDumper(filename);

	if(args.length > 1) {
		if(args[0].equalsIgnoreCase("-escher")) {
			foo.setDDFEscher(true);
		} else {
			foo.setBasicEscher(true);
		}
	}

	foo.printDump();
	foo.close();
  }


  /**
   * Constructs a Powerpoint dump from fileName. Parses the document
   * and dumps out the contents
   *
   * @param fileName The name of the file to read.
   * @throws IOException if there is a problem while parsing the document.
   */
  public SlideShowDumper(String fileName) throws IOException
  {
  	this(new FileInputStream(fileName));
  }

  /**
   * Constructs a Powerpoint dump from an input stream. Parses the
   * document and dumps out the contents
   *
   * @param inputStream the source of the data
   * @throws IOException if there is a problem while parsing the document.
   */
  public SlideShowDumper(InputStream inputStream) throws IOException
  {
	//do Ole stuff
	this(new POIFSFileSystem(inputStream));
	istream = inputStream;
  }

  /**
   * Constructs a Powerpoint dump from a POIFS Filesystem. Parses the
   * document and dumps out the contents
   *
   * @param filesystem the POIFS FileSystem to read from
   * @throws IOException if there is a problem while parsing the document.
   */
  public SlideShowDumper(POIFSFileSystem filesystem) throws IOException
  {
	this.filesystem = filesystem;

	// Get the main document stream
	DocumentEntry docProps =
		(DocumentEntry)filesystem.getRoot().getEntry("PowerPoint Document");

	// Grab the document stream
	_docstream = new byte[docProps.getSize()];
	filesystem.createDocumentInputStream("PowerPoint Document").read(_docstream);
  }

  /**
   * Control dumping of any Escher records found - should DDF be used?
   */
  public void setDDFEscher(boolean grok) {
	ddfEscher = grok;
	basicEscher = !(grok);
  }

  /**
   * Control dumping of any Escher records found - should our built in
   *  basic groker be used?
   */
  public void setBasicEscher(boolean grok) {
	basicEscher = grok;
	ddfEscher = !(grok);
  }

  /**
   * Shuts things down. Closes underlying streams etc
   *
   * @throws IOException
   */
  public void close() throws IOException
  {
	if(istream != null) {
		istream.close();
	}
	filesystem = null;
  }


  public void printDump() {
	// The format of records in a powerpoint file are:
	//   <little endian 2 byte "info">
	//   <little endian 2 byte "type">
	//   <little endian 4 byte "length">
	// If it has a zero length, following it will be another record
	//		<xx xx yy yy 00 00 00 00> <xx xx yy yy zz zz zz zz>
	// If it has a length, depending on its type it may have children or data
	// If it has children, these will follow straight away
	//		<xx xx yy yy zz zz zz zz <xx xx yy yy zz zz zz zz>>
	// If it has data, this will come straigh after, and run for the length
	//      <xx xx yy yy zz zz zz zz dd dd dd dd dd dd dd>
	// All lengths given exclude the 8 byte record header
	// (Data records are known as Atoms)

	// Document should start with:
	//   0F 00 E8 03 ## ## ## ##
    //     (type 1000 = document, info 00 0f is normal, rest is document length)
	//   01 00 E9 03 28 00 00 00
	//     (type 1001 = document atom, info 00 01 normal, 28 bytes long)

	// When parsing a document, look to see if you know about that type
	//  of the current record. If you know it's a type that has children,
	//  process the record's data area looking for more records
	// If you know about the type and it doesn't have children, either do
	//  something with the data (eg TextRun) or skip over it
	// Otherwise, check the first byte. If you do a BINARY_AND on it with
	//  0x0f (15) and get back 0x0f, you know it has children. Otherwise
	//  it doesn't

	walkTree(0,0,_docstream.length);
}

public String makeHex(short s) {
	String hex = Integer.toHexString(s).toUpperCase();
	if(hex.length() == 1) { return "0" + hex; }
	return hex;
}
public String makeHex(int i) {
	String hex = Integer.toHexString(i).toUpperCase();
	if(hex.length() == 1) { return "000" + hex; }
	if(hex.length() == 2) { return "00" + hex; }
	if(hex.length() == 3) { return "0" + hex; }
	return hex;
}

public void walkTree(int depth, int startPos, int maxLen) {
	int pos = startPos;
	int endPos = startPos + maxLen;
	int indent = depth;
	while(pos <= endPos - 8) {
		long type = LittleEndian.getUShort(_docstream,pos+2);
		long len = LittleEndian.getUInt(_docstream,pos+4);
		byte opt = _docstream[pos];

		String ind = "";
		for(int i=0; i<indent; i++) { ind += " "; }

		System.out.println(ind + "At position " + pos + " (" + makeHex(pos) + "):");
		System.out.println(ind + "Type is " + type + " (" + makeHex((int)type) + "), len is " + len + " (" + makeHex((int)len) + ")");

		// See if we know about the type of it
		String recordName = RecordTypes.recordName((int)type);

		// Jump over header, and think about going on more
		pos += 8;
		if(recordName != null) {
			System.out.println(ind + "That's a " + recordName);

			// Now check if it's a container or not
			int container = opt & 0x0f;

			// BinaryTagData seems to contain records, but it
			//  isn't tagged as doing so. Try stepping in anyway
			if(type == 5003L && opt == 0L) {
				container = 0x0f;
			}

			if(type == 0L || (container != 0x0f)) {
				System.out.println();
			} else if (type == 1035l || type == 1036l) {
				// Special Handling of 1035=PPDrawingGroup and 1036=PPDrawing
				System.out.println();

				if(ddfEscher) {
					// Seems to be:
					walkEscherDDF((indent+3),pos+8,(int)len-8);
				} else if(basicEscher) {
					walkEscherBasic((indent+3),pos+8,(int)len-8);
				}
			} else {
				// General container record handling code
				System.out.println();
				walkTree((indent+2),pos,(int)len);
			}
		} else {
			System.out.println(ind + "** unknown record **");
			System.out.println();
		}
		pos += (int)len;
	}
  }

  /**
   * Use the DDF code to walk the Escher records
   */
  public void walkEscherDDF(int indent, int pos, int len) {
	if(len < 8) { return; }

	String ind = "";
	for(int i=0; i<indent; i++) { ind += " "; }

	byte[] contents = new byte[len];
	System.arraycopy(_docstream,pos,contents,0,len);
	DefaultEscherRecordFactory erf = new DefaultEscherRecordFactory();
	EscherRecord record = erf.createRecord(contents,0);

	// For now, try filling in the fields
	record.fillFields(contents,0,erf);

	long atomType = LittleEndian.getUShort(contents,2);
	// This lacks the 8 byte header size
	long atomLen = LittleEndian.getUShort(contents,4);
	// This (should) include the 8 byte header size
	int recordLen = record.getRecordSize();


	System.out.println(ind + "At position " + pos + " (" + makeHex(pos) + "):");
	System.out.println(ind + "Type is " + atomType + " (" + makeHex((int)atomType) + "), len is " + atomLen + " (" + makeHex((int)atomLen) + ") (" + (atomLen+8) + ") - record claims " + recordLen);

	// Check for corrupt / lying ones
	if(recordLen != 8 && (recordLen != (atomLen+8))) {
		System.out.println(ind + "** Atom length of " + atomLen + " (" + (atomLen+8) + ") doesn't match record length of " + recordLen);
	}

	// Print the record's details
	if(record instanceof EscherContainerRecord) {
		EscherContainerRecord ecr = (EscherContainerRecord)record;
		System.out.println(ind + ecr.toString());
		walkEscherDDF((indent+3), pos + 8, (int)atomLen );
	} else {
		System.out.println(ind + record.toString());
	}

	// Handle records that seem to lie
	if(atomType == 61451l) {
		// Normally claims a size of 8
		recordLen = (int)atomLen + 8;
	}
	if(atomType == 61453l) {
		// Returns EscherContainerRecord, but really msofbtClientTextbox
		recordLen = (int)atomLen + 8;
		record.fillFields( contents, 0, erf );
		if(! (record instanceof EscherTextboxRecord)) {
			System.out.println(ind + "** Really a msofbtClientTextbox !");
		}
	}

	// Decide on what to do, based on how the lenghts match up
	if(recordLen == 8 && atomLen > 8 ) {
		// Assume it has children, rather than being corrupted
		walkEscherDDF((indent+3), pos + 8, (int)atomLen );

		// Wind on our length + our header
		pos += atomLen;
		pos += 8;
		len -= atomLen;
		len -= 8;
	} else {
		// No children, wind on our real length
		pos += atomLen;
		pos += 8;
		len -= atomLen;
		len -= 8;
	}

	// Move on to the next one, if we're not at the end yet
	if(len >= 8) {
		walkEscherDDF(indent, pos, len );
	}
  }

  /**
   * Use the basic record format groking code to walk the Escher records
   */
  public void walkEscherBasic(int indent, int pos, int len) {
	if(len < 8) { return; }

	String ind = "";
	for(int i=0; i<indent; i++) { ind += " "; }

	long type = LittleEndian.getUShort(_docstream,pos+2);
	long atomlen = LittleEndian.getUInt(_docstream,pos+4);
	String typeS = makeHex((int)type);

	System.out.println(ind + "At position " + pos + " (" + makeHex(pos) + "):");
	System.out.println(ind + "Type is " + type + " (" + typeS + "), len is " + atomlen + " (" + makeHex((int)atomlen) + ")");

	String typeName = RecordTypes.recordName((int)type);
	if(typeName != null) {
		System.out.println(ind + "That's an Escher Record: " + typeName);
	} else {
		System.out.println(ind + "(Unknown Escher Record)");
	}


	// Code to print the first 8 bytes
//	System.out.print(ind);
//	for(int i=0; i<8; i++) {
//		short bv = _docstream[i+pos];
//		if(bv < 0) { bv += 256; }
//		System.out.print(i + "=" + bv + " (" + makeHex(bv) + ")  ");
//	}
//	System.out.println("");

	// Record specific dumps
	if(type == 61453l) {
		// Text Box. Print out first 8 bytes of data, then 8 4 later
		System.out.print(ind);
		for(int i=8; i<16; i++) {
			short bv = _docstream[i+pos];
			if(bv < 0) { bv += 256; }
			System.out.print(i + "=" + bv + " (" + makeHex(bv) + ")  ");
		}
		System.out.println("");
		System.out.print(ind);
		for(int i=20; i<28; i++) {
			short bv = _docstream[i+pos];
			if(bv < 0) { bv += 256; }
			System.out.print(i + "=" + bv + " (" + makeHex(bv) + ")  ");
		}
		System.out.println("");
	}


	// Blank line before next entry
	System.out.println("");

	// Look in children if we are a container
	if(type == 61443l || type == 61444l) {
		walkEscherBasic((indent+3), pos+8, (int)atomlen);
	}

	// Keep going if not yet at end
	if(atomlen < len) {
		int atomleni = (int)atomlen;
		walkEscherBasic(indent, pos+atomleni+8, len-atomleni-8);
	}
  }
}

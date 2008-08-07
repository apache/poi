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

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.poi.hslf.HSLFSlideShow;
import org.apache.poi.hslf.record.Record;

/**
 * This class provides a way to view the contents of a powerpoint file.
 * It will use the recored layer to grok the contents of the file, and
 *  will print out what it finds.
 *
 * @author Nick Burch
 */
public final class SlideShowRecordDumper {
  private HSLFSlideShow doc;

  /**
   *  right now this function takes one parameter: a ppt file, and outputs
   *  a dump of what it contains
   */
  public static void main(String args[]) throws IOException
  {
	if(args.length == 0) {
		System.err.println("Useage: SlideShowRecordDumper <filename>");
		return;
	}

	String filename = args[0];

	SlideShowRecordDumper foo = new SlideShowRecordDumper(filename);

	foo.printDump();
  }


  /**
   * Constructs a Powerpoint dump from fileName. Parses the document 
   * and dumps out the contents
   *
   * @param fileName The name of the file to read.
   * @throws IOException if there is a problem while parsing the document.
   */
  public SlideShowRecordDumper(String fileName) throws IOException
  {
	doc = new HSLFSlideShow(fileName);
  }


  public void printDump() throws IOException {
	// Prints out the records in the tree
	walkTree(0,0,doc.getRecords());
  }

  public String makeHex(int number, int padding) {
	String hex = Integer.toHexString(number).toUpperCase();
	while(hex.length() < padding) {
		hex = "0" + hex;
	}
	return hex;
  }

  public String reverseHex(String s) {
	StringBuffer ret = new StringBuffer();

	// Get to a multiple of two
	if((s.length() / 2) * 2 != s.length()) { s = "0" + s; }

	// Break up into blocks
	char[] c = s.toCharArray();
	for(int i=c.length; i>0; i-=2) {
		ret.append(c[i-2]);
		ret.append(c[i-1]);
		if(i != 2) { ret.append(' '); }
	}
	return ret.toString();
  }

  public int getDiskLen(Record r) throws IOException {
	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	r.writeOut(baos);
	byte[] b = baos.toByteArray();
	return b.length;
  }


  public void walkTree(int depth, int pos, Record[] records) throws IOException {
	int indent = depth;
	String ind = "";
	for(int i=0; i<indent; i++) { ind += " "; }

	for(int i=0; i<records.length; i++) {
		Record r = records[i];

		// Figure out how big it is
		int len = getDiskLen(r);

		// Grab the type as hex
		String hexType = makeHex((int)r.getRecordType(),4);
		String rHexType = reverseHex(hexType);

		// Grab the hslf.record type
		Class c = r.getClass();
		String cname = c.toString();
		if(cname.startsWith("class ")) {
			cname = cname.substring(6);
		}
		if(cname.startsWith("org.apache.poi.hslf.record.")) {
			cname = cname.substring(27);
		}

		// Display the record
		System.out.println(ind + "At position " + pos + " (" + makeHex(pos,6) + "):");
		System.out.println(ind + " Record is of type " + cname);
		System.out.println(ind + " Type is " + r.getRecordType() + " (" + hexType + " -> " + rHexType + " )");
		System.out.println(ind + " Len is " + (len-8) + " (" + makeHex((len-8),8) + "), on disk len is " + len );
		System.out.println();

		// If it has children, show them
		if(r.getChildRecords() != null) {
			walkTree((depth+3),pos+8,r.getChildRecords());
		}

		// Wind on the position marker
		pos += len;
	}
  }
}

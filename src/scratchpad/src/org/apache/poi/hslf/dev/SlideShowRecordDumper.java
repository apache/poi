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

import java.util.Iterator;

import org.apache.poi.hslf.HSLFSlideShow;
import org.apache.poi.hslf.record.Record;
import org.apache.poi.util.HexDump;
import org.apache.poi.ddf.DefaultEscherRecordFactory;
import org.apache.poi.ddf.EscherRecord;
import org.apache.poi.ddf.EscherContainerRecord;
import org.apache.poi.ddf.EscherTextboxRecord;
import org.apache.poi.hslf.record.EscherTextboxWrapper;
import org.apache.poi.hslf.record.TextCharsAtom;
import org.apache.poi.hslf.record.TextBytesAtom;
import org.apache.poi.hslf.record.StyleTextPropAtom;

/**
 * This class provides a way to view the contents of a powerpoint file.
 * It will use the recored layer to grok the contents of the file, and
 *  will print out what it finds.
 *
 * @author Nick Burch
 */
public final class SlideShowRecordDumper {
  private boolean optVerbose;
  private boolean optEscher;
  private HSLFSlideShow doc;

  /**
   *  right now this function takes one parameter: a ppt file, and outputs
   *  a dump of what it contains
   */
  public static void main(String args[]) throws IOException
  {
	String filename = "";
	boolean verbose = false;
	boolean escher = false;

	int ndx=0;
	for (; ndx<args.length; ndx++) {
		if (!args[ndx].substring(0,1).equals("-"))
			break;

		if (args[ndx].equals("-escher")) {
			escher = true;
		} else if (args[ndx].equals("-verbose")) {
			verbose = true;
		} else {
			printUsage();
			return;
		}
	}

	// parsed any options, expect exactly one remaining arg (filename)
	if (ndx != args.length-1) {
		printUsage();
		return;
	}

	filename = args[ndx];

	SlideShowRecordDumper foo = new SlideShowRecordDumper(filename, verbose, escher);

	foo.printDump();
  }

  public static void printUsage() {
		System.err.println("Usage: SlideShowRecordDumper [-escher] [-verbose] <filename>");
		System.err.println("Valid Options:");
		System.err.println("-escher\t\t: dump contents of escher records");
		System.err.println("-verbose\t: dump binary contents of each record");
  }


  /**
   * Constructs a Powerpoint dump from fileName. Parses the document
   * and dumps out the contents
   *
   * @param fileName The name of the file to read.
   * @throws IOException if there is a problem while parsing the document.
   */
  public SlideShowRecordDumper(String fileName, boolean verbose, boolean escher) throws IOException
  {
	optVerbose = verbose;
  	optEscher = escher;
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
  	if (r == null) return 0;

	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	r.writeOut(baos);
	byte[] b = baos.toByteArray();
	return b.length;
  }

  public String getPrintableRecordContents(Record r) throws IOException {
  	if (r==null) return "<<null>>";

	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	r.writeOut(baos);
	byte[] b = baos.toByteArray();
	return HexDump.dump(b, 0, 0);
  }

  public String printEscherRecord( EscherRecord er ) {
        String nl = System.getProperty( "line.separator" );
        StringBuffer buf = new StringBuffer();

		if (er instanceof EscherContainerRecord) {
			buf.append(printEscherContainerRecord( (EscherContainerRecord)er ));
		} else if (er instanceof EscherTextboxRecord) {
			buf.append("EscherTextboxRecord:" + nl);

			EscherTextboxWrapper etw = new EscherTextboxWrapper((EscherTextboxRecord)er);
			Record children[] = etw.getChildRecords();
			for (int j=0; j<children.length; j++) {
				if (children[j] instanceof StyleTextPropAtom) {

					// need preceding Text[Chars|Bytes]Atom to initialize the data structure
					if (j > 0 && (children[j-1] instanceof TextCharsAtom ||
								  children[j-1] instanceof TextBytesAtom)) {

						int size = (children[j-1] instanceof TextCharsAtom) ?
										((TextCharsAtom)children[j-1]).getText().length() :
										((TextBytesAtom)children[j-1]).getText().length();

						StyleTextPropAtom tsp = (StyleTextPropAtom)children[j];
						tsp.setParentTextSize(size);

					} else {
						buf.append("Error! Couldn't find preceding TextAtom for style\n");
					}

					buf.append(children[j].toString() + nl );
				} else {
					buf.append(children[j].toString() + nl );
				}
			}
		} else {
			buf.append( er.toString() );
		}
		return buf.toString();
  }

  public String printEscherContainerRecord( EscherContainerRecord ecr ) {
  		String indent = "";

        String nl = System.getProperty( "line.separator" );

        StringBuffer children = new StringBuffer();
        int count = 0;
        for ( Iterator<EscherRecord> iterator = ecr.getChildIterator(); iterator.hasNext(); )
        {
            if (count < 1) {
                children.append( "  children: " + nl );
            }
            String newIndent = "   ";

            EscherRecord record = iterator.next();
            children.append(newIndent + "Child " + count + ":" + nl);

           	children.append( printEscherRecord(record) );

            count++;
        }

        return
        	indent + ecr.getClass().getName() + " (" + ecr.getRecordName() + "):" + nl +
            indent + "  isContainer: " + ecr.isContainerRecord() + nl +
            indent + "  options: 0x" + HexDump.toHex( ecr.getOptions() ) + nl +
            indent + "  recordId: 0x" + HexDump.toHex( ecr.getRecordId() ) + nl +
            indent + "  numchildren: " + ecr.getChildRecords().size() + nl +
            indent + children.toString();
  }


  public void walkTree(int depth, int pos, Record[] records) throws IOException {
	int indent = depth;
	String ind = "";
	for(int i=0; i<indent; i++) { ind += " "; }

	for(int i=0; i<records.length; i++) {
		Record r = records[i];
		if (r == null) {
			System.out.println(ind + "At position " + pos + " (" + makeHex(pos,6) + "):");
			System.out.println(ind + "Warning! Null record found.");
			continue;
		}

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

		// print additional information for drawings and atoms
		if (optEscher && cname.equals("PPDrawing")) {
			DefaultEscherRecordFactory factory = new DefaultEscherRecordFactory();

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			r.writeOut(baos);
			byte[] b = baos.toByteArray();

			EscherRecord er = factory.createRecord(b, 0);
			er.fillFields(b, 0, factory);

			System.out.println( printEscherRecord( er ) );

		} else if(optVerbose && r.getChildRecords() == null) {
			String recData = getPrintableRecordContents(r);
			System.out.println(ind + recData );
		}

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

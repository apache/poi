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
import java.io.PrintStream;
import java.util.List;
import java.util.Locale;

import org.apache.poi.ddf.DefaultEscherRecordFactory;
import org.apache.poi.ddf.EscherContainerRecord;
import org.apache.poi.ddf.EscherRecord;
import org.apache.poi.ddf.EscherTextboxRecord;
import org.apache.poi.hslf.record.EscherTextboxWrapper;
import org.apache.poi.hslf.record.HSLFEscherRecordFactory;
import org.apache.poi.hslf.record.Record;
import org.apache.poi.hslf.record.StyleTextPropAtom;
import org.apache.poi.hslf.record.TextBytesAtom;
import org.apache.poi.hslf.record.TextCharsAtom;
import org.apache.poi.hslf.usermodel.HSLFSlideShowImpl;
import org.apache.poi.util.HexDump;

/**
 * This class provides a way to view the contents of a powerpoint file.
 * It will use the recored layer to grok the contents of the file, and
 *  will print out what it finds.
 *
 * @author Nick Burch
 */
public final class SlideShowRecordDumper {
    final static String tabs = "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t";
    
    private boolean optVerbose;
    private boolean optEscher;
    private HSLFSlideShowImpl doc;
    private final PrintStream ps;

    /**
     * right now this function takes one parameter: a ppt file, and outputs a
     * dump of what it contains
     */
    public static void main(String args[]) throws IOException {
        String filename = "";
        boolean verbose = false;
        boolean escher = false;

        int ndx = 0;
        for (; ndx < args.length; ndx++) {
            if (!args[ndx].substring(0, 1).equals("-"))
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
        if (ndx != args.length - 1) {
            printUsage();
            return;
        }

        filename = args[ndx];

        SlideShowRecordDumper foo = new SlideShowRecordDumper(System.out,
                filename, verbose, escher);

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
    public SlideShowRecordDumper(PrintStream ps, String fileName, boolean verbose, boolean escher)
    throws IOException {
        this.ps = ps;
        optVerbose = verbose;
        optEscher = escher;
        doc = new HSLFSlideShowImpl(fileName);
    }


    public void printDump() throws IOException {
        // Prints out the records in the tree
        walkTree(0, 0, doc.getRecords(), 0);
    }

    public String makeHex(int number, int padding) {
        String hex = Integer.toHexString(number).toUpperCase(Locale.ROOT);
        while (hex.length() < padding) {
            hex = "0" + hex;
        }
        return hex;
    }

    public String reverseHex(String s) {
        StringBuilder ret = new StringBuilder();

        // Get to a multiple of two
        int pos = 0;
        if ((s.length() & 1) == 1) {
            ret.append(0);
            pos++;
        }
        for (char c : s.toCharArray()) {
            if (pos > 0 && (pos & 1) == 0) {
                ret.append(' ');
            }
            ret.append(c);
            pos++;
        }

        return ret.toString();
    }

    public int getDiskLen(Record r) throws IOException {
        int diskLen = 0;
        if (r != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            r.writeOut(baos);
            diskLen = baos.size();
        }
        return diskLen;
    }

    public String getPrintableRecordContents(Record r) throws IOException {
        if (r == null) {
            return "<<null>>";
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        r.writeOut(baos);
        byte[] b = baos.toByteArray();
        return HexDump.dump(b, 0, 0);
    }

    public void printEscherRecord(EscherRecord er, int indent) {
		if (er instanceof EscherContainerRecord) {
			printEscherContainerRecord( (EscherContainerRecord)er, indent );
		} else if (er instanceof EscherTextboxRecord) {
		    printEscherTextBox( (EscherTextboxRecord)er, indent );
		} else {
		    ps.print( tabs.substring(0, indent) );
		    ps.println(er);
		}
    }

    private void printEscherTextBox( EscherTextboxRecord tbRecord, int indent ) {
        String ind = tabs.substring(0, indent);
        ps.println(ind+"EscherTextboxRecord:");

        EscherTextboxWrapper etw = new EscherTextboxWrapper(tbRecord);
        Record prevChild = null;
        for (Record child : etw.getChildRecords()) {
            if (child instanceof StyleTextPropAtom) {
                // need preceding Text[Chars|Bytes]Atom to initialize the data structure
                String text = null;
                if (prevChild instanceof TextCharsAtom) {
                    text = ((TextCharsAtom)prevChild).getText();
                } else if (prevChild instanceof TextBytesAtom) {
                    text = ((TextBytesAtom)prevChild).getText();
                } else {
                    ps.println(ind+"Error! Couldn't find preceding TextAtom for style");
                    continue;
                }

                StyleTextPropAtom tsp = (StyleTextPropAtom)child;
                tsp.setParentTextSize(text.length());
            }
            ps.println(ind+ child);
            prevChild = child;
        }
 
    }
    
    private void printEscherContainerRecord( EscherContainerRecord ecr, int indent ) {
        String ind = tabs.substring(0, indent);
        ps.println(ind + ecr.getClass().getName() + " (" + ecr.getRecordName() + "):");
        ps.println(ind + "  isContainer: " + ecr.isContainerRecord());
        ps.println(ind + "  options: 0x" + HexDump.toHex( ecr.getOptions() ));
        ps.println(ind + "  recordId: 0x" + HexDump.toHex( ecr.getRecordId() ));
        
        List<EscherRecord> childRecords = ecr.getChildRecords();
        ps.println(ind + "  numchildren: " + childRecords.size());
        ps.println(ind + "  children: ");
        int count = 0;
        for ( EscherRecord record : childRecords ) {
            ps.println(ind + "   Child " + count + ":");
            printEscherRecord(record, indent+1);
            count++;
        }
    }


    public void walkTree(int depth, int pos, Record[] records, int indent) throws IOException {
        String ind = tabs.substring(0, indent);

        for (int i = 0; i < records.length; i++) {
            Record r = records[i];
            if (r == null) {
                ps.println(ind + "At position " + pos + " (" + makeHex(pos, 6) + "):");
                ps.println(ind + "Warning! Null record found.");
                continue;
            }

            // Figure out how big it is
            int len = getDiskLen(r);

            // Grab the type as hex
            String hexType = makeHex((int) r.getRecordType(), 4);
            String rHexType = reverseHex(hexType);

		// Grab the hslf.record type
		Class<? extends Record> c = r.getClass();
		String cname = c.toString();
		if(cname.startsWith("class ")) {
			cname = cname.substring(6);
		}
		if(cname.startsWith("org.apache.poi.hslf.record.")) {
			cname = cname.substring(27);
		}

		// Display the record
		ps.println(ind + "At position " + pos + " (" + makeHex(pos,6) + "):");
		ps.println(ind + " Record is of type " + cname);
		ps.println(ind + " Type is " + r.getRecordType() + " (" + hexType + " -> " + rHexType + " )");
		ps.println(ind + " Len is " + (len-8) + " (" + makeHex((len-8),8) + "), on disk len is " + len );

		// print additional information for drawings and atoms
		if (optEscher && cname.equals("PPDrawing")) {
			DefaultEscherRecordFactory factory = new HSLFEscherRecordFactory();

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			r.writeOut(baos);
			byte[] b = baos.toByteArray();

			EscherRecord er = factory.createRecord(b, 0);
			er.fillFields(b, 0, factory);

			printEscherRecord( er, indent+1 );

		} else if(optVerbose && r.getChildRecords() == null) {
			String recData = getPrintableRecordContents(r);
			ps.println(ind + recData );
		}

		ps.println();

		// If it has children, show them
		if(r.getChildRecords() != null) {
			walkTree((depth+3),pos+8,r.getChildRecords(), indent+1);
		}

		// Wind on the position marker
		pos += len;
	}
  }
}

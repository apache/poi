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

package org.apache.poi.hslf.extractor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hslf.record.CString;
import org.apache.poi.hslf.record.Record;
import org.apache.poi.hslf.record.RecordTypes;
import org.apache.poi.hslf.record.TextBytesAtom;
import org.apache.poi.hslf.record.TextCharsAtom;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.hslf.usermodel.HSLFTextParagraph;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.LittleEndian;

/**
 * This class will get all the text from a Powerpoint Document, including
 *  all the bits you didn't want, and in a somewhat random order, but will
 *  do it very fast.
 * The class ignores most of the hslf classes, and doesn't use
 *  HSLFSlideShow. Instead, it just does a very basic scan through the
 *  file, grabbing all the text records as it goes. It then returns the
 *  text, either as a single string, or as a vector of all the individual
 *  strings.
 * Because of how it works, it will return a lot of "crud" text that you
 *  probably didn't want! It will return text from master slides. It will
 *  return duplicate text, and some mangled text (powerpoint files often
 *  have duplicate copies of slide text in them). You don't get any idea
 *  what the text was associated with.
 * Almost everyone will want to use @see PowerPointExtractor instead. There
 *  are only a very small number of cases (eg some performance sensitive
 *  lucene indexers) that would ever want to use this!
 */
public final class QuickButCruddyTextExtractor {
    private POIFSFileSystem fs;
    private InputStream is;
    private byte[] pptContents;

    /**
     * Really basic text extractor, that will also return lots of crud text.
     * Takes a single argument, the file to extract from
     */
    public static void main(String[] args) throws IOException
    {
        if(args.length < 1) {
            System.err.println("Usage:");
            System.err.println("\tQuickButCruddyTextExtractor <file>");
            System.exit(1);
        }

        String file = args[0];

        QuickButCruddyTextExtractor ppe = new QuickButCruddyTextExtractor(file);
        System.out.println(ppe.getTextAsString());
        ppe.close();
    }

    /**
     * Creates an extractor from a given file name
     * @param fileName
     */
    @SuppressWarnings("resource")
    public QuickButCruddyTextExtractor(String fileName) throws IOException {
        this(new POIFSFileSystem(new File(fileName)));
    }

    /**
     * Creates an extractor from a given input stream
     * @param iStream
     */
    @SuppressWarnings("resource")
    public QuickButCruddyTextExtractor(InputStream iStream) throws IOException {
        this(new POIFSFileSystem(iStream));
        is = iStream;
    }

    /**
     * Creates an extractor from a POIFS Filesystem
     * @param poifs
     */
    public QuickButCruddyTextExtractor(POIFSFileSystem poifs) throws IOException {
        fs = poifs;

        // Find the PowerPoint bit, and get out the bytes
        InputStream pptIs = fs.createDocumentInputStream(HSLFSlideShow.POWERPOINT_DOCUMENT);
        pptContents = IOUtils.toByteArray(pptIs);
        pptIs.close();
    }


    /**
     * Shuts down the underlying streams
     */
    public void close() throws IOException {
        if(is != null) { is.close(); }
        fs = null;
    }

    /**
     * Fetches the ALL the text of the powerpoint file, as a single string
     */
    public String getTextAsString() {
        StringBuilder ret = new StringBuilder();
        List<String> textV = getTextAsVector();
        for(String text : textV) {
            ret.append(text);
            if(! text.endsWith("\n")) {
                ret.append('\n');
            }
        }
        return ret.toString();
    }

    /**
     * Fetches the ALL the text of the powerpoint file, in a List of
     *  strings, one per text record
     */
    public List<String> getTextAsVector() {
        List<String> textV = new ArrayList<>();

        // Set to the start of the file
        int walkPos = 0;

        // Start walking the file, looking for the records
        while(walkPos != -1) {
            walkPos = findTextRecords(walkPos,textV);
        }

        // Return what we find
        return textV;
    }

    /**
     * For the given position, look if the record is a text record, and wind
     *  on after.
     * If it is a text record, grabs out the text. Whatever happens, returns
     *  the position of the next record, or -1 if no more.
     */
    public int findTextRecords(int startPos, List<String> textV) {
        // Grab the length, and the first option byte
        // Note that the length doesn't include the 8 byte atom header
        int len = (int)LittleEndian.getUInt(pptContents,startPos+4);
        byte opt = pptContents[startPos];

        // If it's a container, step into it and return
        // (If it's a container, option byte 1 BINARY_AND 0x0f will be 0x0f)
        int container = opt & 0x0f;
        if(container == 0x0f) {
            return (startPos+8);
        }

        // Otherwise, check the type to see if it's text
        int type = LittleEndian.getUShort(pptContents,startPos+2);

        // TextBytesAtom
        if(type == RecordTypes.TextBytesAtom.typeID) {
            TextBytesAtom tba = (TextBytesAtom)Record.createRecordForType(type, pptContents, startPos, len+8);
            String text = HSLFTextParagraph.toExternalString(tba.getText(), -1);
            textV.add(text);
        }
        // TextCharsAtom
        if(type == RecordTypes.TextCharsAtom.typeID) {
            TextCharsAtom tca = (TextCharsAtom)Record.createRecordForType(type, pptContents, startPos, len+8);
            String text = HSLFTextParagraph.toExternalString(tca.getText(), -1);
            textV.add(text);
        }

        // CString (doesn't go via a TextRun)
        if(type == RecordTypes.CString.typeID) {
            CString cs = (CString)Record.createRecordForType(type, pptContents, startPos, len+8);
            String text = cs.getText();

            // Ignore the ones we know to be rubbish
            if(text.equals("___PPT10")) {
            } else if(text.equals("Default Design")) {
            } else {
                textV.add(text);
            }
        }


        // Wind on by the atom length, and check we're not at the end
        int newPos = (startPos + 8 + len);
        if(newPos > (pptContents.length - 8)) {
            newPos = -1;
        }
        return newPos;
    }
}

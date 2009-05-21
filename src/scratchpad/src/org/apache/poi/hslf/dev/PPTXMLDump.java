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

import org.apache.poi.util.LittleEndian;
import org.apache.poi.hslf.record.RecordTypes;
import org.apache.poi.poifs.filesystem.*;
import java.io.*;

/**
 * Utility class which dumps raw contents of a ppt file into XML format
 *
 * @author Yegor Kozlov
 */

public final class PPTXMLDump {
    public static final int HEADER_SIZE = 8; //size of the record header
    public static final int PICT_HEADER_SIZE = 25; //size of the picture header
    public final static String PPDOC_ENTRY = "PowerPoint Document";
    public final static String PICTURES_ENTRY = "Pictures";
    public static String CR = System.getProperty("line.separator");

    protected Writer out;
    protected byte[] docstream;
    protected byte[] pictstream;
    protected boolean hexHeader = true;

    public PPTXMLDump(File ppt) throws IOException {
        FileInputStream fis = new FileInputStream(ppt);
        POIFSFileSystem fs = new POIFSFileSystem(fis);
        fis.close();

        //read the document entry from OLE file system
        DocumentEntry entry = (DocumentEntry)fs.getRoot().getEntry(PPDOC_ENTRY);
        docstream = new byte[entry.getSize()];
        DocumentInputStream is = fs.createDocumentInputStream(PPDOC_ENTRY);
        is.read(docstream);

        try {
            entry = (DocumentEntry)fs.getRoot().getEntry(PICTURES_ENTRY);
            pictstream = new byte[entry.getSize()];
            is = fs.createDocumentInputStream(PICTURES_ENTRY);
            is.read(pictstream);
        } catch(FileNotFoundException e){
            //silently catch errors if the presentation does not contain pictures
        }
    }

    /**
     * Dump the structure of the supplied PPT file into XML
     * @param out <code>Writer</code> to write out
     * @throws java.io.IOException
     */
    public void dump(Writer out) throws IOException {
        this.out = out;

        int padding = 0;
        write(out, "<Presentation>" + CR, padding);
        padding++;
        if (pictstream != null){
            write(out, "<Pictures>" + CR, padding);
            dumpPictures(pictstream, padding);
            write(out, "</Pictures>" + CR, padding);
        }
        //dump the structure of the powerpoint document
        write(out, "<PowerPointDocument>" + CR, padding);
        padding++;
        dump(docstream, 0, docstream.length, padding);
        padding--;
        write(out, "</PowerPointDocument>" + CR, padding);
        padding--;
        write(out, "</Presentation>", padding);
    }

    /**
     * Dump a part of the document stream into XML
     * @param data PPT binary data
     * @param offset offset from the beginning of the document
     * @param length of the document
     * @param padding used for formatting results
     * @throws java.io.IOException
     */
    public void dump(byte[] data, int offset, int length, int padding) throws IOException {
        int pos = offset;
        while (pos <= (offset + length - HEADER_SIZE)){
            if (pos < 0) break;

            //read record header
            int info = LittleEndian.getUShort(data, pos);
            pos += LittleEndian.SHORT_SIZE;
            int type = LittleEndian.getUShort(data, pos);
            pos += LittleEndian.SHORT_SIZE;
            int size = (int)LittleEndian.getUInt(data, pos);
            pos += LittleEndian.INT_SIZE;

            //get name of the record by type
            String recname = RecordTypes.recordName(type);
            write(out, "<"+recname + " info=\""+info+"\" type=\""+type+"\" size=\""+size+"\" offset=\""+(pos-8)+"\"", padding);
            if (hexHeader){
                out.write(" header=\"");
                dump(out, data, pos-8, 8, 0, false);
                out.write("\"");
            }
            out.write(">" + CR);
			padding++;
            //this check works both for Escher and PowerPoint records
            boolean isContainer = (info & 0x000F) == 0x000F;
            if (isContainer) {
                //continue to dump child records
                dump(data, pos, size, padding);
            } else {
                //dump first 100 bytes of the atom data
                dump(out, data, pos, Math.min(size, data.length-pos), padding, true);
            }
			padding--;
            write(out, "</"+recname + ">" + CR, padding);

            pos += size;
        }
    }

    /**
     * Dumps the Pictures OLE stream into XML.
     *
     * @param data from the Pictures OLE data stream
     * @param padding
     * @throws java.io.IOException
     */
    public void dumpPictures(byte[] data, int padding) throws IOException {
        int pos = 0;
        while (pos < data.length) {
            byte[] header = new byte[PICT_HEADER_SIZE];

            System.arraycopy(data, pos, header, 0, header.length);
            int size = LittleEndian.getInt(header, 4) - 17;
            byte[] pictdata = new byte[size];
            System.arraycopy(data, pos + PICT_HEADER_SIZE, pictdata, 0, pictdata.length);
            pos += PICT_HEADER_SIZE + size;

            padding++;
            write(out, "<picture size=\""+size+"\" type=\""+getPictureType(header)+"\">" + CR, padding);
            padding++;
            write(out, "<header>" + CR, padding);
            dump(out, header, 0, header.length, padding, true);
            write(out, "</header>" + CR, padding);
            write(out, "<imgdata>" + CR, padding);
            dump(out, pictdata, 0, Math.min(pictdata.length, 100), padding, true);
            write(out, "</imgdata>" + CR, padding);
            padding--;
            write(out, "</picture>" + CR, padding);
            padding--;

        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length == 0){
            System.out.println(
                "Usage: PPTXMLDump (options) pptfile\n" +
                "Where options include:\n" +
                "    -f     write output to <pptfile>.xml file in the current directory"
            );
            return;
        }
        boolean outFile = false;
        for (int i = 0; i < args.length; i++){

            if (args[i].startsWith("-")) {
                if ("-f".equals(args[i])){
                    //write ouput to a file
                    outFile = true;
                }
            } else {
                File ppt = new File(args[i]);
                PPTXMLDump dump = new PPTXMLDump(ppt);
                System.out.println("Dumping " + args[i]);

                if (outFile){
                    FileWriter out = new FileWriter(ppt.getName() + ".xml");
                    dump.dump(out);
                    out.close();
                } else {
                    StringWriter out = new StringWriter();
                    dump.dump(out);
                    System.out.println(out.toString());
                }
            }

        }
    }


    /**
     *  write a string to <code>out</code> with the specified padding
     */
    private static void write(Writer out, String str, int padding) throws IOException {
        for (int i = 0; i < padding; i++) out.write("  ");
        out.write(str);
    }

    private String getPictureType(byte[] header){
        String type;
        int meta = LittleEndian.getUShort(header, 0);

        switch(meta){
            case 0x46A0: type = "jpeg"; break;
            case 0x2160: type = "wmf"; break;
            case 0x6E00: type = "png"; break;
            default: type = "unknown"; break;
        }
        return type;
    }

    /**
     *  dump binary data to <code>out</code> with the specified padding
     */
    private static void dump(Writer out, byte[] data, int offset, int length, int padding, boolean nl) throws IOException {
        int linesize = 25;
        for (int i = 0; i < padding; i++) out.write("  ");
        int i;
        for (i = offset; i < (offset + length); i++) {
            int c = data[i];
            out.write((char) hexval[(c & 0xF0) >> 4]);
            out.write((char) hexval[(c & 0x0F) >> 0]);
            out.write(' ');
            if((i+1-offset) % linesize == 0 && i != (offset + length-1)) {
                out.write(CR);
                for (int j = 0; j < padding; j++) out.write("  ");
            }
        }
        if(nl && length > 0)out.write(CR);
    }

    private static final byte hexval[] =
        {(byte) '0', (byte) '1', (byte) '2', (byte) '3',
         (byte) '4', (byte) '5', (byte) '6', (byte) '7',
         (byte) '8', (byte) '9', (byte) 'A', (byte) 'B',
         (byte) 'C', (byte) 'D', (byte) 'E', (byte) 'F'};

}

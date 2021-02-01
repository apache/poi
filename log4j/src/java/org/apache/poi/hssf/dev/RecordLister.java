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

package org.apache.poi.hssf.dev;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.hssf.record.ContinueRecord;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.RecordFactory;
import org.apache.poi.hssf.record.RecordInputStream;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

/**
 * This is a low-level debugging class, which simply prints
 *  out what records come in what order.
 * Most people will want to use {@link BiffViewer} or
 *  {@link EFBiffViewer}, but this can be handy when
 *  trying to make sense of {@link ContinueRecord}
 *  special cases.
 * 
 * Output is of the form:
 *  SID - Length - Type (if known)
 *    byte0 byte1 byte2 byte3 .... byte(n-4) byte(n-3) byte(n-2) byte(n-1)
 */
public class RecordLister
{
    String file;

    public RecordLister()
    {
    }

    public void run() throws IOException {
        try (POIFSFileSystem fs = new POIFSFileSystem(new File(file), true);
             InputStream din = BiffViewer.getPOIFSInputStream(fs)) {
            RecordInputStream rinp = new RecordInputStream(din);

            while (rinp.hasNextRecord()) {
                int sid = rinp.getNextSid();
                rinp.nextRecord();

                int size = rinp.available();
                Class<? extends Record> clz = RecordFactory.getRecordClass(sid);

                System.out.print(
                        formatSID(sid) +
                                " - " +
                                formatSize(size) +
                                " bytes"
                );
                if (clz != null) {
                    System.out.print("  \t");
                    System.out.print(clz.getName().replace("org.apache.poi.hssf.record.", ""));
                }
                System.out.println();

                byte[] data = rinp.readRemainder();
                if (data.length > 0) {
                    System.out.print("   ");
                    System.out.println(formatData(data));
                }
            }
        }
    }
    
    private static String formatSID(int sid) {
       String hex = Integer.toHexString(sid);
       String dec = Integer.toString(sid);
       
       StringBuilder s = new StringBuilder();
       s.append("0x");
       for(int i=hex.length(); i<4; i++) {
          s.append('0');
       }
       s.append(hex);
       
       s.append(" (");
       for(int i=dec.length(); i<4; i++) {
          s.append('0');
       }
       s.append(dec);
       s.append(")");
       
       return s.toString();
    }
    private static String formatSize(int size) {
       String hex = Integer.toHexString(size);
       String dec = Integer.toString(size);
       
       StringBuilder s = new StringBuilder();
       for(int i=hex.length(); i<3; i++) {
          s.append('0');
       }
       s.append(hex);
       
       s.append(" (");
       for(int i=dec.length(); i<3; i++) {
          s.append('0');
       }
       s.append(dec);
       s.append(")");
       
       return s.toString();
    }
    private static String formatData(byte[] data) {
       if(data == null || data.length == 0)
          return "";
       
       // If possible, do first 4 and last 4 bytes
       StringBuilder s = new StringBuilder();
       if(data.length > 9) {
          s.append(byteToHex(data[0]));
          s.append(' ');
          s.append(byteToHex(data[1]));
          s.append(' ');
          s.append(byteToHex(data[2]));
          s.append(' ');
          s.append(byteToHex(data[3]));
          s.append(' ');
          
          s.append(" .... ");
          
          s.append(' ');
          s.append(byteToHex(data[data.length-4]));
          s.append(' ');
          s.append(byteToHex(data[data.length-3]));
          s.append(' ');
          s.append(byteToHex(data[data.length-2]));
          s.append(' ');
          s.append(byteToHex(data[data.length-1]));
       } else {
           for (byte aData : data) {
               s.append(byteToHex(aData));
               s.append(' ');
           }
       }
       
       return s.toString();
    }
    private static String byteToHex(byte b) {
       int i = b;
       if(i<0) {
          i += 256;
       }
       String s = Integer.toHexString(i);
       if(i < 16) {
          return "0" + s; 
       }
       return s;
    }

    public void setFile(String file)
    {
        this.file = file;
    }

    public static void main(String [] args) throws IOException
    {
        if ((args.length == 1) && !args[ 0 ].equals("--help"))
        {
            RecordLister viewer = new RecordLister();

            viewer.setFile(args[ 0 ]);
            viewer.run();
        }
        else
        {
            System.out.println("RecordLister");
            System.out.println(
                "Outputs the summary of the records in file order");
            System.out
                .println("usage: java org.apache.poi.hssf.dev.RecordLister "
                         + "filename");
        }
    }
}

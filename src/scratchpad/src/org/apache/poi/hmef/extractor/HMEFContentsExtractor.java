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

package org.apache.poi.hmef.extractor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.poi.hmef.Attachment;
import org.apache.poi.hmef.HMEFMessage;
import org.apache.poi.hmef.attribute.MAPIAttribute;
import org.apache.poi.hmef.attribute.MAPIRtfAttribute;
import org.apache.poi.hmef.attribute.MAPIStringAttribute;
import org.apache.poi.hsmf.datatypes.MAPIProperty;
import org.apache.poi.hsmf.datatypes.Types;
import org.apache.poi.util.StringUtil;

/**
 * A utility for extracting out the message body, and all attachments
 *  from a HMEF/TNEF/winmail.dat file
 */
public final class HMEFContentsExtractor {
    /**
     * Usage: HMEFContentsExtractor &lt;filename&gt; &lt;output dir&gt;
     */
    public static void main(String[] args) throws IOException {
        if(args.length < 2) {
            System.err.println("Use:");
            System.err.println("  HMEFContentsExtractor <filename> <output dir>");
            System.err.println("");
            System.err.println("");
            System.err.println("Where <filename> is the winmail.dat file to extract,");
            System.err.println(" and <output dir> is where to place the extracted files");
            System.exit(2);
        }
        
        final String filename = args[0];
        final String outputDir = args[1];
        
        HMEFContentsExtractor ext = new HMEFContentsExtractor(new File(filename));
        
        File dir = new File(outputDir);
        File rtf = new File(dir, "message.rtf");
        if(! dir.exists()) {
            throw new FileNotFoundException("Output directory " + dir.getName() + " not found");
        }
        
        System.out.println("Extracting...");
        ext.extractMessageBody(rtf);
        ext.extractAttachments(dir);
        System.out.println("Extraction completed");
    }
    
    private final HMEFMessage message;
    public HMEFContentsExtractor(File filename) throws IOException {
        this(new HMEFMessage(new FileInputStream(filename)));
    }
    public HMEFContentsExtractor(HMEFMessage message) {
        this.message = message;
    }
    
    /**
     * Extracts the RTF message body to the supplied file
     */
    public void extractMessageBody(File dest) throws IOException {
        MAPIAttribute body = getBodyAttribute();
        if (body == null) {
            System.err.println("No message body found, " + dest + " not created");
            return;
        }
        if (body instanceof MAPIStringAttribute) {
            String name = dest.toString();
            if (name.endsWith(".rtf")) { 
                name = name.substring(0, name.length()-4);
            }
            dest = new File(name + ".txt");
        }
        
        OutputStream fout = new FileOutputStream(dest);
        try {
            if (body instanceof MAPIStringAttribute) {
                // Save in a predictable encoding, not raw bytes
                String text = ((MAPIStringAttribute)body).getDataString();
                fout.write(text.getBytes(StringUtil.UTF8));
            } else {
                // Save the raw bytes, should be raw RTF
                fout.write(body.getData());
            }
        } finally {
            fout.close();
        }
    }
    
    protected MAPIAttribute getBodyAttribute() {
        MAPIAttribute body = message.getMessageMAPIAttribute(MAPIProperty.RTF_COMPRESSED);
        if (body != null) return body;
        
        // See bug #59786 - we'd really like a test file to confirm if this
        //  is the right properties + if this is truely general or not!
        MAPIProperty uncompressedBody = 
                MAPIProperty.createCustom(0x3fd9, Types.ASCII_STRING, "Uncompressed Body");
        // Return this uncompressed one, or null if that isn't their either
        return message.getMessageMAPIAttribute(uncompressedBody);
    }
    
    /**
     * Extracts the RTF message body to the supplied stream. If there is no
     *  RTF message body, nothing will be written to the stream, but no
     *  errors or exceptions will be raised.
     */
    public void extractMessageBody(OutputStream out) throws IOException {
        MAPIRtfAttribute body = (MAPIRtfAttribute)
                message.getMessageMAPIAttribute(MAPIProperty.RTF_COMPRESSED);
        if (body != null) {
            out.write(body.getData());
        }
    }
    
    /**
     * Extracts all the message attachments to the supplied directory
     */
    public void extractAttachments(File dir) throws IOException {
        int count = 0;
        for(Attachment att : message.getAttachments()) {
            count++;
            
            // Decide what to call it
            String filename = att.getLongFilename();
            if(filename == null || filename.length() == 0) {
                filename = att.getFilename();
            }
            if(filename == null || filename.length() == 0) {
                filename = "attachment" + count;
                if(att.getExtension() != null) {
                    filename += att.getExtension();
                }
            }
            
            // Save it
            File file = new File(dir, filename);
            OutputStream fout = new FileOutputStream(file);
            try {
                fout.write( att.getContents() );
            } finally {
                fout.close();
            }
        }
    }
}

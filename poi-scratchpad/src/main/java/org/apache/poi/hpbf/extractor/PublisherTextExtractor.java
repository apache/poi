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

package org.apache.poi.hpbf.extractor;

import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.extractor.POIOLE2TextExtractor;
import org.apache.poi.hpbf.HPBFDocument;
import org.apache.poi.hpbf.model.qcbits.QCBit;
import org.apache.poi.hpbf.model.qcbits.QCPLCBit.Type12;
import org.apache.poi.hpbf.model.qcbits.QCTextBit;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

/**
 * Extract text from HPBF Publisher files
 */
public final class PublisherTextExtractor implements POIOLE2TextExtractor {
   private final HPBFDocument doc;
   private boolean hyperlinksByDefault;
   private boolean doCloseFilesystem = true;

   public PublisherTextExtractor(HPBFDocument doc) {
      this.doc = doc;
   }

   public PublisherTextExtractor(DirectoryNode dir) throws IOException {
      this(new HPBFDocument(dir));
   }

   public PublisherTextExtractor(POIFSFileSystem fs) throws IOException {
      this(new HPBFDocument(fs));
   }

   public PublisherTextExtractor(InputStream is) throws IOException {
      this(new POIFSFileSystem(is));
   }

    /**
     * Should a call to getText() return hyperlinks inline
     *  with the text?
     * Default is no
     */
    public void setHyperlinksByDefault(boolean hyperlinksByDefault) {
        this.hyperlinksByDefault = hyperlinksByDefault;
    }


    public String getText() {
        StringBuilder text = new StringBuilder();

        // Get the text from the Quill Contents
        QCBit[] bits = doc.getQuillContents().getBits();
        for (QCBit bit1 : bits) {
            if (bit1 instanceof QCTextBit) {
                QCTextBit t = (QCTextBit) bit1;
                text.append(t.getText().replace('\r', '\n'));
            }
        }

        // If requested, add in the hyperlinks
        // Ideally, we'd do these inline, but the hyperlink
        //  positions are relative to the text area the
        //  hyperlink is in, and we have yet to figure out
        //  how to tie that together.
        if(hyperlinksByDefault) {
            for (QCBit bit : bits) {
                if (bit instanceof Type12) {
                    Type12 hyperlinks = (Type12) bit;
                    for (int j = 0; j < hyperlinks.getNumberOfHyperlinks(); j++) {
                        text.append("<");
                        text.append(hyperlinks.getHyperlink(j));
                        text.append(">\n");
                    }
                }
            }
        }

        // Get more text
        // TODO

        return text.toString();
    }

    @Override
    public HPBFDocument getDocument() {
        return doc;
    }

    @Override
    public void setCloseFilesystem(boolean doCloseFilesystem) {
        this.doCloseFilesystem = doCloseFilesystem;
    }

    @Override
    public boolean isCloseFilesystem() {
        return doCloseFilesystem;
    }

    @Override
    public HPBFDocument getFilesystem() {
        return doc;
    }
}

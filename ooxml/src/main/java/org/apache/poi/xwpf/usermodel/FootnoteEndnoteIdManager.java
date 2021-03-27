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
package org.apache.poi.xwpf.usermodel;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages IDs for footnotes and endnotes.
 * <p>Footnotes and endnotes are managed in separate parts but
 * represent a single namespace of IDs.</p>
 */
public class FootnoteEndnoteIdManager {

    private XWPFDocument document;

    public FootnoteEndnoteIdManager(XWPFDocument document) {
        this.document = document;
    }

    /**
     * Gets the next ID number.
     *
     * @return ID number to use.
     */
    public BigInteger nextId() {

        List<BigInteger> ids = new ArrayList<>();
        for (XWPFAbstractFootnoteEndnote note : document.getFootnotes()) {
            ids.add(note.getId());
        }
        for (XWPFAbstractFootnoteEndnote note : document.getEndnotes()) {
            ids.add(note.getId());
        }
        int cand = ids.size();
        BigInteger newId = BigInteger.valueOf(cand);
        while (ids.contains(newId)) {
            cand++;
            newId = BigInteger.valueOf(cand);
        }

        return newId;
    }


}

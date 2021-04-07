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

package org.apache.poi.hwpf.usermodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.model.PicturesTable;

/**
 * Helper class for mapping Pictures to Runs within
 *  a document.
 *  
 * This allows for easy access to Pictures by Run,
 *  as well as a way to find "Escher Floating"
 *  Pictures which don't have the regular \u0001
 *  references in the main text.
 * 
 * Provides access to the pictures by offset, iteration
 *  over the un-claimed, and peeking forward.
 */
public class PictureRunMapper {
    private PicturesTable picturesTable;
    private Set<Picture> claimed = new HashSet<>();
    private Map<Integer, Picture> lookup;
    private List<Picture> nonU1based;
    private List<Picture> all;
    private int pn;

    public PictureRunMapper(HWPFDocument doc) {
        picturesTable = doc.getPicturesTable();
        all = picturesTable.getAllPictures();

        // Build the Offset-Picture lookup map
        lookup = new HashMap<>();
        for (Picture p : all) {
            lookup.put(p.getStartOffset(), p);
        }

        // Work out which Pictures aren't referenced by
        //  a \u0001 in the main text
        // These are \u0008 escher floating ones, ones
        //  found outside the normal text, and who
        //  knows what else...
        nonU1based = new ArrayList<>();
        nonU1based.addAll(all);
        Range r = doc.getRange();
        for (int i = 0; i < r.numCharacterRuns(); i++) {
            CharacterRun cr = r.getCharacterRun(i);
            if (picturesTable.hasPicture(cr)) {
                Picture p = getFor(cr);
                int at = nonU1based.indexOf(p);
                nonU1based.set(at, null);
            }
        }
    }

    /**
     * Does this run have a Picture in it?
     * 
     * @see #getFor(CharacterRun)
     */
    public boolean hasPicture(CharacterRun cr) {
        return picturesTable.hasPicture(cr);
    }

    /**
     * Get the Picture for this run, if any
     */
    public Picture getFor(CharacterRun cr) {
        return lookup.get(cr.getPicOffset());
    }

    /**
     * Mark a Picture as claimed.
     * Used when trying to match up non-Run based pictures 
     */
    public void markAsClaimed(Picture picture) {
        claimed.add(picture);
    }

    /**
     * Has the given Picture been claimed by a non-Run yet?
     */
    public boolean hasBeenClaimed(Picture picture) {
        return claimed.contains(picture);
    }

    /**
     * Which Picture is this one of all the Pictures in
     *  the Document?
     *
     * Useful when trying to extract all Pictures with
     *  unique numbers or references
     */
    public int pictureNumber(Picture picture) {
        return all.indexOf(picture) + 1;
    }

    /**
     * Return the next unclaimed one, used towards
     * the end
     */
    public Picture nextUnclaimed() {
        Picture p = null;
        while (pn < nonU1based.size()) {
            p = nonU1based.get(pn);
            pn++;
            if (p != null) {
                claimed.add(p);
                return p;
            }
        }
        return null;
    }
}

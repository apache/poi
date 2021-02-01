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

package org.apache.poi.sl.usermodel;

public enum Placeholder {
    /**
     * No placeholder shape.
     */
    NONE(0,0,0,0,0),
    /**
     * Title text placeholder shape.
     */
    TITLE(13,1,1,1,1),
    /**
     * Body text placeholder shape.
     */
    BODY(14,2,12,6,2),
    /**
     * Center title text placeholder shape.
     */
    CENTERED_TITLE(15,3,3,3,3),
    /**
     * Sub-title text placeholder shape.
     */
    SUBTITLE(16,4,4,4,4),
    /**
     * Date placeholder shape.
     */
    DATETIME(7,7,7,7,5),
    /**
     * Slide number placeholder shape.
     */
    SLIDE_NUMBER(8,8,8,8,6),
    /**
     * Footer placeholder shape.
     */
    FOOTER(9,9,9,9,7),
    /**
     *  Header placeholder shape.
     */
    HEADER(10,10,10,10,8),
    /**
     * Object placeholder shape.
     */
    CONTENT(19,19,19,19,9),
    /**
     * Graph object placeholder shape.
     */
    CHART(20,20,20,20,10),
    /**
     * Table object placeholder shape.
     */
    TABLE(21,21,21,21,11),
    /**
     * Clipart object placeholder shape.
     */
    CLIP_ART(22,22,22,22,12),
    /**
     * Organization chart object placeholder shape.
     */
    DGM(23,23,23,23,13),
    /**
     * Media object placeholder shape.
     */
    MEDIA(24,24,24,24,14),
    /**
     * Slide image placeholder shape.
     */
    SLIDE_IMAGE(11,11,11,5,15),
    /**
     * Picture object placeholder shape.
     */
    PICTURE(26,26,26,26,16),
    /**
     * Vertical object placeholder shape.
     */
    VERTICAL_OBJECT(25,25,25,25,-2),
    /**
     * Vertical title text placeholder shape.
     */
    VERTICAL_TEXT_TITLE(17,17,17,17,-2),
    /**
     * Vertical body text placeholder shape.
     */
    VERTICAL_TEXT_BODY(18,18,18,18,-2)
    ;
    
    public final int nativeSlideId;
    public final int nativeSlideMasterId;
    public final int nativeNotesId;
    public final int nativeNotesMasterId;
    public final int ooxmlId;
    
    Placeholder(int nativeSlideId, int nativeSlideMasterId, int nativeNotesId, int nativeNotesMasterId, int ooxmlId) {
        this.nativeSlideId = nativeSlideId;
        this.nativeSlideMasterId = nativeSlideMasterId;
        this.nativeNotesId = nativeNotesId;
        this.nativeNotesMasterId = nativeNotesMasterId;
        this.ooxmlId = ooxmlId;
    }
    
    public static Placeholder lookupNativeSlide(int nativeId) {
        return lookupNative(nativeId, 0);
    }

    public static Placeholder lookupNativeSlideMaster(int nativeId) {
        return lookupNative(nativeId, 1);
    }

    public static Placeholder lookupNativeNotes(int nativeId) {
        return lookupNative(nativeId, 2);
    }

    public static Placeholder lookupNativeNotesMaster(int nativeId) {
        return lookupNative(nativeId, 3);
    }

    
    private static Placeholder lookupNative(int nativeId, int type) {
        for (Placeholder ph : values()) {
            if (
                type == 0 && ph.nativeSlideId == nativeId ||
                type == 1 && ph.nativeSlideMasterId == nativeId || 
                type == 2 && ph.nativeNotesId == nativeId ||
                type == 3 && ph.nativeNotesMasterId == nativeId
            ) {
                return ph;
            }
        }
        return null;
    }
    
    public static Placeholder lookupOoxml(int ooxmlId) {
        for (Placeholder ph : values()) {
            if (ph.ooxmlId == ooxmlId) {
                return ph;
            }
        }
        return null;
    }
}
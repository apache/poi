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

public interface LineDecoration {
    /**
     *  Represents the shape decoration that appears at the ends of lines.
     */
    enum DecorationShape {
        NONE(0,1),
        TRIANGLE(1,2),
        STEALTH(2,3),
        DIAMOND(3,4),
        OVAL(4,5),
        ARROW(5,6);

        public final int nativeId;
        public final int ooxmlId;

        DecorationShape(int nativeId, int ooxmlId) {
            this.nativeId = nativeId;
            this.ooxmlId = ooxmlId;
        }

        public static DecorationShape fromNativeId(int nativeId) {
            for (DecorationShape ld : values()) {
                if (ld.nativeId == nativeId) return ld;
            }
            return null;
        }

        public static DecorationShape fromOoxmlId(int ooxmlId) {
            for (DecorationShape ds : values()) {
                if (ds.ooxmlId == ooxmlId) return ds;
            }
            return null;
        }
    }

    enum DecorationSize {
        SMALL(0, 1),
        MEDIUM(1, 2),
        LARGE(2, 3);

        public final int nativeId;
        public final int ooxmlId;

        DecorationSize(int nativeId, int ooxmlId) {
            this.nativeId = nativeId;
            this.ooxmlId = ooxmlId;
        }

        public static DecorationSize fromNativeId(int nativeId) {
            for (DecorationSize ld : values()) {
                if (ld.nativeId == nativeId) return ld;
            }
            return null;
        }

        public static DecorationSize fromOoxmlId(int ooxmlId) {
            for (DecorationSize ds : values()) {
                if (ds.ooxmlId == ooxmlId) return ds;
            }
            return null;
        }
    }

    /**
     * @return the line start shape
     */
    DecorationShape getHeadShape();

    /**
     * @return the width of the start shape
     */
    DecorationSize getHeadWidth();

    /**
     * @return the length of the start shape
     */
    DecorationSize getHeadLength();

    /**
     * @return the line end shape
     */
    DecorationShape getTailShape();

    /**
     * @return the width of the end shape
     */
    DecorationSize getTailWidth();

    /**
     * @return the length of the end shape
     */
    DecorationSize getTailLength();

}

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

public interface TabStop {

    enum TabStopType {
        LEFT(0,1), CENTER(1,2), RIGHT(2,3), DECIMAL(3,4);
        public final int nativeId;
        public final int ooxmlId;
        
        TabStopType(int nativeId, int ooxmlId) {
            this.nativeId = nativeId;
            this.ooxmlId = ooxmlId;
        }
        public static TabStopType fromNativeId(final int nativeId)  {
            for (TabStopType tst : values()) {
                if (tst.nativeId == nativeId) {
                    return tst;
                }
            }
            return null;
        }
        public static TabStopType fromOoxmlId(final int ooxmlId)  {
            for (TabStopType tst : values()) {
                if (tst.ooxmlId == ooxmlId) {
                    return tst;
                }
            }
            return null;
        }
    }

    /**
     * Gets the position in points relative to the left side of the paragraph.
     * 
     * @return position in points
     */
    double getPositionInPoints();

    /**
     * Sets the position in points relative to the left side of the paragraph
     *
     * @param position position in points
     */
    void setPositionInPoints(double position);

    TabStopType getType();

    void setType(TabStopType type);
}

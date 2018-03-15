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

package org.apache.poi.hssf.usermodel;


import org.apache.poi.ddf.EscherChildAnchorRecord;
import org.apache.poi.ddf.EscherClientAnchorRecord;
import org.apache.poi.ddf.EscherContainerRecord;
import org.apache.poi.ddf.EscherRecord;
import org.apache.poi.ss.usermodel.ChildAnchor;

/**
 * An anchor is what specifics the position of a shape within a client object
 * or within another containing shape.
 */
public abstract class HSSFAnchor implements ChildAnchor {

    protected boolean _isHorizontallyFlipped;
    protected boolean _isVerticallyFlipped;

    public HSSFAnchor() {
        createEscherAnchor();
    }

    public HSSFAnchor(int dx1, int dy1, int dx2, int dy2) {
        createEscherAnchor();
        setDx1(dx1);
        setDy1(dy1);
        setDx2(dx2);
        setDy2(dy2);
    }

    public static HSSFAnchor createAnchorFromEscher(EscherContainerRecord container){
        if (null != container.getChildById(EscherChildAnchorRecord.RECORD_ID)){
            return new HSSFChildAnchor(container.getChildById(EscherChildAnchorRecord.RECORD_ID));
        } else {
            if (null != container.getChildById(EscherClientAnchorRecord.RECORD_ID)){
                return new HSSFClientAnchor(container.getChildById(EscherClientAnchorRecord.RECORD_ID));
            }
            return null;
        }
    }

    /**
     * @return whether this shape is horizontally flipped
     */
    public abstract boolean isHorizontallyFlipped();

    /**
     * @return  whether this shape is vertically flipped
     */
    public abstract boolean isVerticallyFlipped();

    protected abstract EscherRecord getEscherAnchor();

    protected abstract void createEscherAnchor();
}

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

package org.apache.poi.xddf.usermodel;

import org.apache.poi.util.Beta;
import org.apache.poi.util.Internal;
import org.openxmlformats.schemas.drawingml.x2006.main.CTRelativeRect;

@Beta
public class XDDFRelativeRectangle {
    private CTRelativeRect rect;

    public XDDFRelativeRectangle() {
        this(CTRelativeRect.Factory.newInstance());
    }

    protected XDDFRelativeRectangle(CTRelativeRect rectangle) {
        this.rect = rectangle;
    }

    @Internal
    protected CTRelativeRect getXmlObject() {
        return rect;
    }

    public Integer getBottom() {
        if (rect.isSetB()) {
            return rect.getB();
        } else {
            return null;
        }
    }

    public void setBottom(Integer bottom) {
        if (bottom == null) {
            if (rect.isSetB()) {
                rect.unsetB();
            }
        } else {
            rect.setB(bottom);
        }
    }

    public Integer getLeft() {
        if (rect.isSetL()) {
            return rect.getL();
        } else {
            return null;
        }
    }

    public void setLeft(Integer left) {
        if (left == null) {
            if (rect.isSetL()) {
                rect.unsetL();
            }
        } else {
            rect.setL(left);
        }
    }

    public Integer getRight() {
        if (rect.isSetR()) {
            return rect.getR();
        } else {
            return null;
        }
    }

    public void setRight(Integer right) {
        if (right == null) {
            if (rect.isSetR()) {
                rect.unsetR();
            }
        } else {
            rect.setR(right);
        }
    }

    public Integer getTop() {
        if (rect.isSetT()) {
            return rect.getT();
        } else {
            return null;
        }
    }

    public void setTop(Integer top) {
        if (top == null) {
            if (rect.isSetT()) {
                rect.unsetT();
            }
        } else {
            rect.setT(top);
        }
    }
}

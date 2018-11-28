/*
 *  ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ====================================================================
 */

package org.apache.poi.xddf.usermodel.chart;

import org.apache.poi.util.Beta;
import org.apache.poi.xddf.usermodel.text.TextContainer;
import org.apache.poi.xddf.usermodel.text.XDDFTextBody;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTTitle;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTTx;

/**
 * @since 4.0.1
 */
@Beta
public class XDDFTitle {
    private final CTTitle title;
    private final TextContainer parent;

    public XDDFTitle(TextContainer parent, CTTitle title) {
        this.parent = parent;
        this.title = title;
    }

    public XDDFTextBody getBody() {
        if (!title.isSetTx()) {
            title.addNewTx();
        }
        CTTx tx = title.getTx();
        if (tx.isSetStrRef()) {
            tx.unsetStrRef();
        }
        if (!tx.isSetRich()) {
            tx.addNewRich();
        }
        return new XDDFTextBody(parent, tx.getRich());
    }

    public void setText(String text) {
        if (!title.isSetLayout()) {
            title.addNewLayout();
        }
        getBody().setText(text);
    }

    public void setOverlay(Boolean overlay) {
        if (overlay == null) {
            if (title.isSetOverlay()) {
                title.unsetOverlay();
            }
        } else {
            if (title.isSetOverlay()) {
                title.getOverlay().setVal(overlay);
            } else {
                title.addNewOverlay().setVal(overlay);
            }
        }
    }

}

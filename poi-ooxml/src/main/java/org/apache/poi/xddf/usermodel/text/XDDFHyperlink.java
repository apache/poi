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

package org.apache.poi.xddf.usermodel.text;

import org.apache.poi.util.Beta;
import org.apache.poi.util.Internal;
import org.apache.poi.xddf.usermodel.XDDFExtensionList;
import org.openxmlformats.schemas.drawingml.x2006.main.CTHyperlink;

@Beta
public class XDDFHyperlink {
    private CTHyperlink link;

    public XDDFHyperlink(String id) {
        this(CTHyperlink.Factory.newInstance());
        this.link.setId(id);
    }

    public XDDFHyperlink(String id, String action) {
        this(id);
        this.link.setAction(action);
    }

    @Internal
    protected XDDFHyperlink(CTHyperlink link) {
        this.link = link;
    }

    @Internal
    protected CTHyperlink getXmlObject() {
        return link;
    }

    public String getAction() {
        if (link.isSetAction()) {
            return link.getAction();
        } else {
            return null;
        }
    }

    public String getId() {
        if (link.isSetId()) {
            return link.getId();
        } else {
            return null;
        }
    }

    public Boolean getEndSound() {
        if (link.isSetEndSnd()) {
            return link.getEndSnd();
        } else {
            return null;
        }
    }

    public void setEndSound(Boolean ends) {
        if (ends == null) {
            if (link.isSetEndSnd()) {
                link.unsetEndSnd();
            }
        } else {
            link.setEndSnd(ends);
        }
    }

    public Boolean getHighlightClick() {
        if (link.isSetHighlightClick()) {
            return link.getHighlightClick();
        } else {
            return null;
        }
    }

    public void setHighlightClick(Boolean highlights) {
        if (highlights == null) {
            if (link.isSetHighlightClick()) {
                link.unsetHighlightClick();
            }
        } else {
            link.setHighlightClick(highlights);
        }
    }

    public Boolean getHistory() {
        if (link.isSetHistory()) {
            return link.getHistory();
        } else {
            return null;
        }
    }

    public void setHistory(Boolean history) {
        if (history == null) {
            if (link.isSetHistory()) {
                link.unsetHistory();
            }
        } else {
            link.setHistory(history);
        }
    }

    public String getInvalidURL() {
        if (link.isSetInvalidUrl()) {
            return link.getInvalidUrl();
        } else {
            return null;
        }
    }

    public void setInvalidURL(String invalid) {
        if (invalid == null) {
            if (link.isSetInvalidUrl()) {
                link.unsetInvalidUrl();
            }
        } else {
            link.setInvalidUrl(invalid);
        }
    }

    public String getTargetFrame() {
        if (link.isSetTgtFrame()) {
            return link.getTgtFrame();
        } else {
            return null;
        }
    }

    public void setTargetFrame(String frame) {
        if (frame == null) {
            if (link.isSetTgtFrame()) {
                link.unsetTgtFrame();
            }
        } else {
            link.setTgtFrame(frame);
        }
    }

    public String getTooltip() {
        if (link.isSetTooltip()) {
            return link.getTooltip();
        } else {
            return null;
        }
    }

    public void setTooltip(String tooltip) {
        if (tooltip == null) {
            if (link.isSetTooltip()) {
                link.unsetTooltip();
            }
        } else {
            link.setTooltip(tooltip);
        }
    }

    public XDDFExtensionList getExtensionList() {
        if (link.isSetExtLst()) {
            return new XDDFExtensionList(link.getExtLst());
        } else {
            return null;
        }
    }

    public void setExtensionList(XDDFExtensionList list) {
        if (list == null) {
            if (link.isSetExtLst()) {
                link.unsetExtLst();
            }
        } else {
            link.setExtLst(list.getXmlObject());
        }
    }
}

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

package org.apache.poi.xslf.usermodel;

import static org.apache.poi.xslf.usermodel.XSLFShape.PML_NS;

import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.poi.sl.usermodel.MasterSheet;
import org.apache.poi.sl.usermodel.Placeholder;
import org.apache.poi.sl.usermodel.PlaceholderDetails;
import org.openxmlformats.schemas.presentationml.x2006.main.CTApplicationNonVisualDrawingProps;
import org.openxmlformats.schemas.presentationml.x2006.main.CTHeaderFooter;
import org.openxmlformats.schemas.presentationml.x2006.main.CTNotesMaster;
import org.openxmlformats.schemas.presentationml.x2006.main.CTPlaceholder;
import org.openxmlformats.schemas.presentationml.x2006.main.CTSlideMaster;
import org.openxmlformats.schemas.presentationml.x2006.main.STPlaceholderSize;
import org.openxmlformats.schemas.presentationml.x2006.main.STPlaceholderType;

/**
 * XSLF Placeholder Details
 *
 * @since POI 4.0.0
 */
public class XSLFPlaceholderDetails implements PlaceholderDetails {

    private final XSLFShape shape;
    private CTPlaceholder _ph;

    XSLFPlaceholderDetails(final XSLFShape shape) {
        this.shape = shape;
    }

    @Override
    public Placeholder getPlaceholder() {
        final CTPlaceholder ph = getCTPlaceholder(false);
        if (ph == null || !(ph.isSetType() || ph.isSetIdx())) {
            return null;
        }
        return Placeholder.lookupOoxml(ph.getType().intValue());
    }

    @Override
    public void setPlaceholder(final Placeholder placeholder) {
        CTPlaceholder ph = getCTPlaceholder(placeholder != null);
        if (ph != null) {
            if (placeholder != null) {
                ph.setType(STPlaceholderType.Enum.forInt(placeholder.ooxmlId));
            } else {
                getNvProps().unsetPh();
            }
        }
    }

    @Override
    public boolean isVisible() {
        final CTPlaceholder ph = getCTPlaceholder(false);
        if (ph == null || !ph.isSetType()) {
            return true;
        }
        final CTHeaderFooter hf = getHeaderFooter(false);
        if (hf == null) {
            return false;
        }

        final Placeholder pl = Placeholder.lookupOoxml(ph.getType().intValue());
        if (pl == null) {
            return true;
        }
        switch (pl) {
            case DATETIME:
                return !hf.isSetDt() || hf.getDt();
            case FOOTER:
                return !hf.isSetFtr() || hf.getFtr();
            case HEADER:
                return !hf.isSetHdr() || hf.getHdr();
            case SLIDE_NUMBER:
                return !hf.isSetSldNum() || hf.getSldNum();
            default:
                return true;
        }
    }

    @Override
    public void setVisible(final boolean isVisible) {
        final Placeholder ph = getPlaceholder();
        if (ph == null) {
            return;
        }
        final Function<CTHeaderFooter,Consumer<Boolean>> fun;
        switch (ph) {
            case DATETIME:
                fun = (hf) -> hf::setDt;
                break;
            case FOOTER:
                fun = (hf) -> hf::setFtr;
                break;
            case HEADER:
                fun = (hf) -> hf::setHdr;
                break;
            case SLIDE_NUMBER:
                fun = (hf) -> hf::setSldNum;
                break;
            default:
                return;
        }
        // only create a header, if we need to, i.e. the placeholder type is eligible
        final CTHeaderFooter hf = getHeaderFooter(true);
        if (hf == null) {
            return;
        }
        fun.apply(hf).accept(isVisible);
    }

    @Override
    public PlaceholderSize getSize() {
        final CTPlaceholder ph = getCTPlaceholder(false);
        if (ph == null || !ph.isSetSz()) {
            return null;
        }
        switch (ph.getSz().intValue()) {
            case STPlaceholderSize.INT_FULL:
                return PlaceholderSize.full;
            case STPlaceholderSize.INT_HALF:
                return PlaceholderSize.half;
            case STPlaceholderSize.INT_QUARTER:
                return PlaceholderSize.quarter;
            default:
                return null;
        }
    }

    @Override
    public void setSize(final PlaceholderSize size) {
        final CTPlaceholder ph = getCTPlaceholder(false);
        if (ph == null) {
            return;
        }
        if (size == null) {
            ph.unsetSz();
            return;
        }
        switch (size) {
            case full:
                ph.setSz(STPlaceholderSize.FULL);
                break;
            case half:
                ph.setSz(STPlaceholderSize.HALF);
                break;
            case quarter:
                ph.setSz(STPlaceholderSize.QUARTER);
                break;
        }
    }

    /**
     * Gets or creates a new placeholder element
     *
     * @param create if {@code true} creates the element if it hasn't existed before
     * @return the placeholder or {@code null} if the shape doesn't support placeholders
     */
    CTPlaceholder getCTPlaceholder(final boolean create) {
        if (_ph != null) {
            return _ph;
        }

        final CTApplicationNonVisualDrawingProps nv = getNvProps();
        if (nv == null) {
            // shape doesn't support CTApplicationNonVisualDrawingProps
            return null;
        }

        _ph = (nv.isSetPh() || !create) ? nv.getPh() : nv.addNewPh();
        return _ph;
    }

    private CTApplicationNonVisualDrawingProps getNvProps() {
        final String xquery = "declare namespace p='" + PML_NS + "' .//*/p:nvPr";
        return shape.selectProperty(CTApplicationNonVisualDrawingProps.class, xquery);
    }

    private CTHeaderFooter getHeaderFooter(final boolean create) {
        final XSLFSheet sheet = shape.getSheet();
        final XSLFSheet master = (sheet instanceof MasterSheet && !(sheet instanceof XSLFSlideLayout)) ? sheet : (XSLFSheet)sheet.getMasterSheet();
        if (master instanceof XSLFSlideMaster) {
            final CTSlideMaster ct = ((XSLFSlideMaster) master).getXmlObject();
            return (ct.isSetHf() || !create) ? ct.getHf() : ct.addNewHf();
        } else if (master instanceof  XSLFNotesMaster) {
            final CTNotesMaster ct = ((XSLFNotesMaster) master).getXmlObject();
            return (ct.isSetHf() || !create) ? ct.getHf() : ct.addNewHf();
        } else {
            return null;
        }
    }

    @Override
    public String getText() {
        return null;
    }

    @Override
    public void setText(String text) {
    }
}

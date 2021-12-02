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

package org.apache.poi.hslf.usermodel;

import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.poi.ddf.EscherPropertyTypes;
import org.apache.poi.ddf.EscherSpRecord;
import org.apache.poi.hslf.exceptions.HSLFException;
import org.apache.poi.hslf.model.HeadersFooters;
import org.apache.poi.hslf.record.CString;
import org.apache.poi.hslf.record.DateTimeMCAtom;
import org.apache.poi.hslf.record.EscherTextboxWrapper;
import org.apache.poi.hslf.record.HSLFEscherClientDataRecord;
import org.apache.poi.hslf.record.HeadersFootersAtom;
import org.apache.poi.hslf.record.OEPlaceholderAtom;
import org.apache.poi.hslf.record.RecordTypes;
import org.apache.poi.hslf.record.RoundTripHFPlaceholder12;
import org.apache.poi.hslf.record.TextSpecInfoAtom;
import org.apache.poi.hslf.record.TextSpecInfoRun;
import org.apache.poi.hslf.util.LocaleDateFormat;
import org.apache.poi.sl.usermodel.MasterSheet;
import org.apache.poi.sl.usermodel.Placeholder;
import org.apache.poi.util.LocaleID;
import org.apache.poi.util.LocaleUtil;

/**
 * Extended placeholder details for HSLF shapes
 *
 * @since POI 4.0.0
 */
public class HSLFShapePlaceholderDetails extends HSLFPlaceholderDetails {
    private enum PlaceholderContainer {
        slide, master, notes, notesMaster
    }

    private final PlaceholderContainer source;
    final HSLFSimpleShape shape;
    private OEPlaceholderAtom oePlaceholderAtom;
    private RoundTripHFPlaceholder12 roundTripHFPlaceholder12;
    private DateTimeMCAtom localDateTime;


    HSLFShapePlaceholderDetails(final HSLFSimpleShape shape) {
        super(shape.getSheet(), null);

        this.shape = shape;

        final HSLFSheet sheet = shape.getSheet();
        if (sheet instanceof HSLFSlideMaster) {
            source = PlaceholderContainer.master;
        } else if (sheet instanceof HSLFNotes) {
            source = PlaceholderContainer.notes;
        } else if (sheet instanceof MasterSheet) {
            // notes master aren't yet supported ...
            source = PlaceholderContainer.notesMaster;
        } else {
            source = PlaceholderContainer.slide;
        }
    }

    @Override
    public Placeholder getPlaceholder() {
        updatePlaceholderAtom(false);
        final int phId;
        if (oePlaceholderAtom != null) {
            phId = oePlaceholderAtom.getPlaceholderId();
        } else if (roundTripHFPlaceholder12 != null) {
            phId = roundTripHFPlaceholder12.getPlaceholderId();
        } else if (localDateTime != null) {
            return Placeholder.DATETIME;
        } else {
            return null;
        }

        switch (source) {
        case slide:
            return Placeholder.lookupNativeSlide(phId);
        default:
        case master:
            return Placeholder.lookupNativeSlideMaster(phId);
        case notes:
            return Placeholder.lookupNativeNotes(phId);
        case notesMaster:
            return Placeholder.lookupNativeNotesMaster(phId);
        }
    }

    @Override
    public void setPlaceholder(final Placeholder placeholder) {
        final EscherSpRecord spRecord = shape.getEscherChild(EscherSpRecord.RECORD_ID);
        int flags = spRecord.getFlags();
        if (placeholder == null) {
            flags ^= EscherSpRecord.FLAG_HAVEMASTER;
        } else {
            flags |= EscherSpRecord.FLAG_HAVEANCHOR | EscherSpRecord.FLAG_HAVEMASTER;
        }
        spRecord.setFlags(flags);

        // Placeholders can't be grouped
        shape.setEscherProperty(EscherPropertyTypes.PROTECTION__LOCKAGAINSTGROUPING, (placeholder == null ? -1 : 262144));

        if (placeholder == null) {
            removePlaceholder();
            return;
        }

        // init client data
        updatePlaceholderAtom(true);

        final byte phId = getPlaceholderId(placeholder);
        oePlaceholderAtom.setPlaceholderId(phId);
        roundTripHFPlaceholder12.setPlaceholderId(phId);
    }

    @Override
    public PlaceholderSize getSize() {
        final Placeholder ph = getPlaceholder();
        if (ph == null) {
            return null;
        }

        final int size = (oePlaceholderAtom != null)
            ? oePlaceholderAtom.getPlaceholderSize()
            : OEPlaceholderAtom.PLACEHOLDER_HALFSIZE;

        switch (size) {
        case OEPlaceholderAtom.PLACEHOLDER_FULLSIZE:
            return PlaceholderSize.full;
        default:
        case OEPlaceholderAtom.PLACEHOLDER_HALFSIZE:
            return PlaceholderSize.half;
        case OEPlaceholderAtom.PLACEHOLDER_QUARTSIZE:
            return PlaceholderSize.quarter;
        }
    }

    @Override
    public void setSize(final PlaceholderSize size) {
        final Placeholder ph = getPlaceholder();
        if (ph == null || size == null) {
            return;
        }
        updatePlaceholderAtom(true);

        final byte ph_size;
        switch (size) {
        case full:
            ph_size = OEPlaceholderAtom.PLACEHOLDER_FULLSIZE;
            break;
        default:
        case half:
            ph_size = OEPlaceholderAtom.PLACEHOLDER_HALFSIZE;
            break;
        case quarter:
            ph_size = OEPlaceholderAtom.PLACEHOLDER_QUARTSIZE;
            break;
        }
        oePlaceholderAtom.setPlaceholderSize(ph_size);
    }

    private byte getPlaceholderId(final Placeholder placeholder) {
        /*
         * Extract from MSDN:
         *
         * There is a special case when the placeholder does not have a position in the layout.
         * This occurs when the user has moved the placeholder from its original position.
         * In this case the placeholder ID is -1.
         */
        final byte phId;
        switch (source) {
            default:
            case slide:
                phId = (byte)placeholder.nativeSlideId;
                break;
            case master:
                phId = (byte)placeholder.nativeSlideMasterId;
                break;
            case notes:
                phId = (byte)placeholder.nativeNotesId;
                break;
            case notesMaster:
                phId = (byte)placeholder.nativeNotesMasterId;
                break;
        }

        if (phId == -2) {
            throw new HSLFException("Placeholder "+placeholder.name()+" not supported for this sheet type ("+shape.getSheet().getClass()+")");
        }

        return phId;
    }

    private void removePlaceholder() {
        final HSLFEscherClientDataRecord clientData = shape.getClientData(false);
        if (clientData != null) {
            clientData.removeChild(OEPlaceholderAtom.class);
            clientData.removeChild(RoundTripHFPlaceholder12.class);
            // remove client data if the placeholder was the only child to be carried
            if (clientData.getChildRecords().isEmpty()) {
                shape.getSpContainer().removeChildRecord(clientData);
            }
        }
        oePlaceholderAtom = null;
        roundTripHFPlaceholder12 = null;
    }

    private void updatePlaceholderAtom(final boolean create) {
        localDateTime = null;
        if (shape instanceof HSLFTextBox) {
            EscherTextboxWrapper txtBox = ((HSLFTextBox)shape).getEscherTextboxWrapper();
            if (txtBox != null) {
                localDateTime = (DateTimeMCAtom)txtBox.findFirstOfType(RecordTypes.DateTimeMCAtom.typeID);
            }
        }

        final HSLFEscherClientDataRecord clientData = shape.getClientData(create);
        if (clientData == null) {
            oePlaceholderAtom = null;
            roundTripHFPlaceholder12 = null;
            if (!create) {
                return;
            }
            throw new HSLFException("Placeholder aren't allowed for shape type: " + shape.getClass().getSimpleName());
        }

        for (org.apache.poi.hslf.record.Record r : clientData.getHSLFChildRecords()) {
            if (r instanceof OEPlaceholderAtom) {
                oePlaceholderAtom = (OEPlaceholderAtom)r;
            } else if (r instanceof RoundTripHFPlaceholder12) {
                //special case for files saved in Office 2007
                roundTripHFPlaceholder12 = (RoundTripHFPlaceholder12)r;
            }
        }

        if (!create) {
            return;
        }

        if (oePlaceholderAtom == null) {
            oePlaceholderAtom = new OEPlaceholderAtom();
            oePlaceholderAtom.setPlaceholderSize((byte)OEPlaceholderAtom.PLACEHOLDER_FULLSIZE);
            // TODO: placement id only "SHOULD" be unique ... check other placeholders on sheet for unique id
            oePlaceholderAtom.setPlacementId(-1);
            clientData.addChild(oePlaceholderAtom);
        }
        if (roundTripHFPlaceholder12 == null) {
            roundTripHFPlaceholder12 = new RoundTripHFPlaceholder12();
            clientData.addChild(roundTripHFPlaceholder12);
        }
    }

    @Override
    public String getUserDate() {
        HeadersFooters hf = shape.getSheet().getHeadersFooters();
        CString uda = hf.getUserDateAtom();
        return hf.isUserDateVisible() && uda != null ? uda.getText() : null;
    }

    @Override
    public DateTimeFormatter getDateFormat() {
        int formatId;
        if (localDateTime != null) {
            formatId = localDateTime.getIndex();
        } else {
            HeadersFootersAtom hfAtom = shape.getSheet().getHeadersFooters().getContainer().getHeadersFootersAtom();
            formatId = hfAtom.getFormatId();
        }

        LocaleID def = LocaleID.lookupByLanguageTag(LocaleUtil.getUserLocale().toLanguageTag());

        // def = LocaleID.EN_US;

        LocaleID lcid =
            Stream.of(((HSLFTextShape)shape).getTextParagraphs().get(0).getRecords())
            .filter(r -> r instanceof TextSpecInfoAtom)
            .findFirst()
            .map(r -> ((TextSpecInfoAtom)r).getTextSpecInfoRuns()[0])
            .map(TextSpecInfoRun::getLangId)
            .flatMap(lid -> Optional.ofNullable(LocaleID.lookupByLcid(lid)))
            .orElse(def != null ? def : LocaleID.EN_US)
        ;

        return LocaleDateFormat.map(lcid, formatId, LocaleDateFormat.MapFormatId.PPT);
    }
}

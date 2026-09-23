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
 * @since 4.0.0
 */
public class HSLFShapePlaceholderDetails extends HSLFPlaceholderDetails {

    static void updateSPRecord(final HSLFSimpleShape shape, final Placeholder placeholder) {
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
    }

    static void removePlaceholder(final HSLFSimpleShape shape) {
        final HSLFEscherClientDataRecord clientData = shape.getClientData(false);
        if (clientData != null) {
            clientData.removeChild(OEPlaceholderAtom.class);
            clientData.removeChild(RoundTripHFPlaceholder12.class);
            // remove client data if the placeholder was the only child to be carried
            if (clientData.getChildRecords().isEmpty()) {
                shape.getSpContainer().removeChildRecord(clientData);
            }
        }
    }

    static OEPlaceholderAtom createOEPlaceholderAtom(final HSLFEscherClientDataRecord clientData) {
        return createOEPlaceholderAtom(clientData, (byte) 0);
    }

    static OEPlaceholderAtom createOEPlaceholderAtom(final HSLFEscherClientDataRecord clientData,
                                                     final byte placeholderId) {
        OEPlaceholderAtom oePlaceholderAtom = new OEPlaceholderAtom();
        oePlaceholderAtom.setPlaceholderSize((byte)OEPlaceholderAtom.PLACEHOLDER_FULLSIZE);
        // TODO: placement id only "SHOULD" be unique ... check other placeholders on sheet for unique id
        oePlaceholderAtom.setPlacementId(-1);
        oePlaceholderAtom.setPlaceholderId(placeholderId);
        clientData.addChild(oePlaceholderAtom);
        return oePlaceholderAtom;
    }

    static byte getPlaceholderId(final HSLFSheet sheet, final Placeholder placeholder) {
        if (placeholder == null) {
            return 0;
        }
        byte phId;
        // TODO: implement/switch NotesMaster
        if (sheet instanceof HSLFSlideMaster) {
            phId = (byte) placeholder.nativeSlideMasterId;
        } else if (sheet instanceof HSLFNotes) {
            phId = (byte) placeholder.nativeNotesId;
        } else {
            phId = (byte) placeholder.nativeSlideId;
        }

        if (phId == -2) {
            throw new HSLFException("Placeholder " + placeholder.name() + " not supported for this sheet type (" + sheet.getClass() + ")");
        }
        return phId;
    }

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
        updatePlaceholderAtom(null, false);
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

        return switch (source) {
            case slide -> Placeholder.lookupNativeSlide(phId);
            case notes -> Placeholder.lookupNativeNotes(phId);
            case notesMaster -> Placeholder.lookupNativeNotesMaster(phId);
            default -> Placeholder.lookupNativeSlideMaster(phId);
        };
    }

    @Override
    public void setPlaceholder(final Placeholder placeholder) {
        updateSPRecord(shape, placeholder);

        if (placeholder == null) {
            removePlaceholder();
            return;
        }

        // init client data
        updatePlaceholderAtom(placeholder, true);

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

        return switch (size) {
            case OEPlaceholderAtom.PLACEHOLDER_FULLSIZE -> PlaceholderSize.full;
            case OEPlaceholderAtom.PLACEHOLDER_QUARTSIZE -> PlaceholderSize.quarter;
            default -> PlaceholderSize.half;
        };
    }

    @Override
    public void setSize(final PlaceholderSize size) {
        final Placeholder ph = getPlaceholder();
        if (ph == null || size == null) {
            return;
        }
        updatePlaceholderAtom(ph, true);

        final byte ph_size = switch (size) {
            case full -> OEPlaceholderAtom.PLACEHOLDER_FULLSIZE;
            case quarter -> OEPlaceholderAtom.PLACEHOLDER_QUARTSIZE;
            default -> OEPlaceholderAtom.PLACEHOLDER_HALFSIZE;
        };
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
        final byte phId = switch (source) {
            case master -> (byte) placeholder.nativeSlideMasterId;
            case notes -> (byte) placeholder.nativeNotesId;
            case notesMaster -> (byte) placeholder.nativeNotesMasterId;
            default -> (byte) placeholder.nativeSlideId;
        };

        if (phId == -2) {
            throw new HSLFException("Placeholder "+placeholder.name()+" not supported for this sheet type ("+shape.getSheet().getClass()+")");
        }

        return phId;
    }

    private void removePlaceholder() {
        removePlaceholder(shape);
        oePlaceholderAtom = null;
        roundTripHFPlaceholder12 = null;
    }

    private void updatePlaceholderAtom(final Placeholder placeholder, final boolean create) {
        localDateTime = null;
        if (shape instanceof HSLFTextBox textBox) {
            EscherTextboxWrapper txtBox = textBox.getEscherTextboxWrapper();
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
            if (r instanceof OEPlaceholderAtom atom) {
                oePlaceholderAtom = atom;
            } else if (r instanceof RoundTripHFPlaceholder12 hfPlaceholder) {
                //special case for files saved in Office 2007
                roundTripHFPlaceholder12 = hfPlaceholder;
            }
        }

        if (!create) {
            return;
        }

        if (oePlaceholderAtom == null) {
            final byte phId = getPlaceholderId(shape.getSheet(), placeholder);
            oePlaceholderAtom = createOEPlaceholderAtom(clientData, phId);
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

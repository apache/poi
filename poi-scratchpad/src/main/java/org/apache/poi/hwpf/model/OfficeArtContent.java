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

package org.apache.poi.hwpf.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ddf.DefaultEscherRecordFactory;
import org.apache.poi.ddf.EscherContainerRecord;
import org.apache.poi.ddf.EscherRecord;
import org.apache.poi.ddf.EscherRecordFactory;
import org.apache.poi.ddf.EscherRecordTypes;
import org.apache.poi.util.Internal;

import static org.apache.logging.log4j.util.Unbox.box;

/**
 * Information about drawings in the document.
 * <p>
 * The {@code delay stream} referenced in {@code [MS-ODRAW]} is the {@code WordDocument} stream.
 */
@Internal
public final class OfficeArtContent {
    protected static final Logger LOG = LogManager.getLogger(OfficeArtContent.class);

    /**
     * {@link EscherRecordTypes#DGG_CONTAINER} containing drawing group information for the document.
     */
    private final EscherContainerRecord drawingGroupData = new EscherContainerRecord();

    /**
     * {@link EscherRecordTypes#DG_CONTAINER} for drawings in the Main Document.
     * <p>
     * {@code null} to indicate that the document does not have a {@link EscherRecordTypes#DG_CONTAINER} for the Main
     * Document.
     */
    private EscherContainerRecord mainDocumentDgContainer;

    /**
     * {@link EscherRecordTypes#DG_CONTAINER} for drawings in the Header Document.
     * <p>
     * {@code null} to indicate that the document does not have a {@link EscherRecordTypes#DG_CONTAINER} for the Header
     * Document.
     */
    private EscherContainerRecord headerDocumentDgContainer;

    public OfficeArtContent(byte[] data, int offset, int size) {
        fillEscherRecords(data, offset, size);
    }

    /**
     * Parses the records out of the given data.
     *
     * The thing to be aware of here is that if {@code size} is {@code 0}, the document does not contain images.
     *
     * @see FileInformationBlock#getLcbDggInfo()
     */
    private void fillEscherRecords(byte[] data, int offset, int size) {
        if (size == 0) return;

        EscherRecordFactory recordFactory = new DefaultEscherRecordFactory();
        int pos = offset;
        pos += drawingGroupData.fillFields(data, pos, recordFactory);
        if (drawingGroupData.getRecordId() == EscherRecordTypes.DGG_CONTAINER.typeID) {
            LOG.atDebug().log("Invalid record-id for filling Escher records: " + drawingGroupData.getRecordId());
        }

        /*
         * After the drawingGroupData there is an array (2 slots max) that has data about drawings. According to the
         * spec, the first slot is for the Main Document, the second for the Header Document. Additionally, the
         * OfficeArtWordDrawing structure has a byte (dgglbl) that indicates whether the structure is for the Main or
         * Header Document. In practice we've seen documents such as 61911.doc where the order of array entries does not
         * match the dgglbl byte. As the byte is more likely to be reliable, we base the parsing off of that rather than
         * array order.
         */

        // This should loop at most twice
        while (pos < offset + size) {

            // Named this way to match section 2.9.172 of [MS-DOC] - v20191119.
            byte dgglbl = data[pos];

            if (dgglbl != 0x00 && dgglbl != 0x01) {
                throw new IllegalArgumentException("Invalid dgglbl when filling Escher records: " + dgglbl);
            }
            pos++;

            EscherContainerRecord dgContainer = new EscherContainerRecord();
            pos+= dgContainer.fillFields(data, pos, recordFactory);
            if (dgContainer.getRecordId() != EscherRecordTypes.DG_CONTAINER.typeID) {
                throw new IllegalArgumentException("Did have an invalid record-type: " + dgContainer.getRecordId() +
                        " when filling Escher records");
            }

            switch (dgglbl) {
                case 0x00:
                    mainDocumentDgContainer = dgContainer;
                    break;
                case 0x01:
                    headerDocumentDgContainer = dgContainer;
                    break;
                default:
                    LogManager.getLogger(OfficeArtContent.class).atWarn()
                            .log("dgglbl {} for OfficeArtWordDrawing is out of bounds [0, 1]", box(dgglbl));
            }
        }

        if (pos != offset + size) {
            throw new IllegalStateException("Did not read all data when filling Escher records: "
                    + "pos: " + pos + ", offset: " + offset + ", size: " + size);
        }
    }

    private List<? extends EscherContainerRecord> getDgContainers() {
        List<EscherContainerRecord> dgContainers = new ArrayList<>(2);
        if (mainDocumentDgContainer != null) {
            dgContainers.add(mainDocumentDgContainer);
        }
        if (headerDocumentDgContainer != null) {
            dgContainers.add(headerDocumentDgContainer);
        }
        return dgContainers;
    }

    /**
     * @return The {@link EscherRecordTypes#BSTORE_CONTAINER} or {@code null} if the document doesn't have one.
     */
    public EscherContainerRecord getBStoreContainer() {
        return drawingGroupData.getChildById(EscherRecordTypes.BSTORE_CONTAINER.typeID);
    }

    public List<? extends EscherContainerRecord> getSpgrContainers()
    {
        List<EscherContainerRecord> spgrContainers = new ArrayList<>(
                1);
        for ( EscherContainerRecord dgContainer : getDgContainers() )
        {
            for ( EscherRecord escherRecord : dgContainer )
            {
                if ( escherRecord.getRecordId() == (short) 0xF003 )
                {
                    spgrContainers.add( (EscherContainerRecord) escherRecord );
                }
            }
        }
        return spgrContainers;
    }

    public List<? extends EscherContainerRecord> getSpContainers()
    {
        List<EscherContainerRecord> spContainers = new ArrayList<>(
                1);
        for ( EscherContainerRecord spgrContainer : getSpgrContainers() )
        {
            for ( EscherRecord escherRecord : spgrContainer )
            {
                if ( escherRecord.getRecordId() == (short) 0xF004 )
                {
                    spContainers.add( (EscherContainerRecord) escherRecord );
                }
            }
        }
        return spContainers;
    }

    @Override
    public String toString() {
        return "OfficeArtContent{" +
                "drawingGroupData=" + drawingGroupData +
                ", mainDocumentDgContainer=" + mainDocumentDgContainer +
                ", headerDocumentDgContainer=" + headerDocumentDgContainer +
                '}';
    }
}

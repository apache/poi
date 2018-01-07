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

package org.apache.poi.hssf.record;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ddf.DefaultEscherRecordFactory;
import org.apache.poi.ddf.EscherClientDataRecord;
import org.apache.poi.ddf.EscherContainerRecord;
import org.apache.poi.ddf.EscherDgRecord;
import org.apache.poi.ddf.EscherRecord;
import org.apache.poi.ddf.EscherRecordFactory;
import org.apache.poi.ddf.EscherSerializationListener;
import org.apache.poi.ddf.EscherSpRecord;
import org.apache.poi.ddf.EscherSpgrRecord;
import org.apache.poi.ddf.EscherTextboxRecord;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.apache.poi.util.RecordFormatException;

/**
 * This class is used to aggregate the MSODRAWING and OBJ record
 * combinations.  This is necessary due to the bizare way in which
 * these records are serialized.  What happens is that you get a
 * combination of MSODRAWING -&gt; OBJ -&gt; MSODRAWING -&gt; OBJ records
 * but the escher records are serialized _across_ the MSODRAWING
 * records.
 * <p>
 * It gets even worse when you start looking at TXO records.
 * <p>
 * So what we do with this class is aggregate lazily.  That is
 * we don't aggregate the MSODRAWING -&gt; OBJ records unless we
 * need to modify them.
 * <p>
 * At first document contains 4 types of records which belong to drawing layer.
 * There are can be such sequence of record:
 * <p>
 * DrawingRecord
 * ContinueRecord
 * ...
 * ContinueRecord
 * ObjRecord | TextObjectRecord
 * .....
 * ContinueRecord
 * ...
 * ContinueRecord
 * ObjRecord | TextObjectRecord
 * NoteRecord
 * ...
 * NoteRecord
 * <p>
 * To work with shapes we have to read data from Drawing and Continue records into single array of bytes and
 * build escher(office art) records tree from this array.
 * Each shape in drawing layer matches corresponding ObjRecord
 * Each textbox matches corresponding TextObjectRecord
 * <p>
 * ObjRecord contains information about shape. Thus each ObjRecord corresponds EscherContainerRecord(SPGR)
 * <p>
 * EscherAggrefate contains also NoteRecords
 * NoteRecords must be serial
 */

public final class EscherAggregate extends AbstractEscherHolderRecord {
    public static final short sid = 9876; // not a real sid - dummy value
    private static POILogger log = POILogFactory.getLogger(EscherAggregate.class);
    //arbitrarily selected; may need to increase
    private static final int MAX_RECORD_LENGTH = 100_000_000;


    public static final short ST_MIN = (short) 0;
    public static final short ST_NOT_PRIMATIVE = ST_MIN;
    public static final short ST_RECTANGLE = (short) 1;
    public static final short ST_ROUNDRECTANGLE = (short) 2;
    public static final short ST_ELLIPSE = (short) 3;
    public static final short ST_DIAMOND = (short) 4;
    public static final short ST_ISOCELESTRIANGLE = (short) 5;
    public static final short ST_RIGHTTRIANGLE = (short) 6;
    public static final short ST_PARALLELOGRAM = (short) 7;
    public static final short ST_TRAPEZOID = (short) 8;
    public static final short ST_HEXAGON = (short) 9;
    public static final short ST_OCTAGON = (short) 10;
    public static final short ST_PLUS = (short) 11;
    public static final short ST_STAR = (short) 12;
    public static final short ST_ARROW = (short) 13;
    public static final short ST_THICKARROW = (short) 14;
    public static final short ST_HOMEPLATE = (short) 15;
    public static final short ST_CUBE = (short) 16;
    public static final short ST_BALLOON = (short) 17;
    public static final short ST_SEAL = (short) 18;
    public static final short ST_ARC = (short) 19;
    public static final short ST_LINE = (short) 20;
    public static final short ST_PLAQUE = (short) 21;
    public static final short ST_CAN = (short) 22;
    public static final short ST_DONUT = (short) 23;
    public static final short ST_TEXTSIMPLE = (short) 24;
    public static final short ST_TEXTOCTAGON = (short) 25;
    public static final short ST_TEXTHEXAGON = (short) 26;
    public static final short ST_TEXTCURVE = (short) 27;
    public static final short ST_TEXTWAVE = (short) 28;
    public static final short ST_TEXTRING = (short) 29;
    public static final short ST_TEXTONCURVE = (short) 30;
    public static final short ST_TEXTONRING = (short) 31;
    public static final short ST_STRAIGHTCONNECTOR1 = (short) 32;
    public static final short ST_BENTCONNECTOR2 = (short) 33;
    public static final short ST_BENTCONNECTOR3 = (short) 34;
    public static final short ST_BENTCONNECTOR4 = (short) 35;
    public static final short ST_BENTCONNECTOR5 = (short) 36;
    public static final short ST_CURVEDCONNECTOR2 = (short) 37;
    public static final short ST_CURVEDCONNECTOR3 = (short) 38;
    public static final short ST_CURVEDCONNECTOR4 = (short) 39;
    public static final short ST_CURVEDCONNECTOR5 = (short) 40;
    public static final short ST_CALLOUT1 = (short) 41;
    public static final short ST_CALLOUT2 = (short) 42;
    public static final short ST_CALLOUT3 = (short) 43;
    public static final short ST_ACCENTCALLOUT1 = (short) 44;
    public static final short ST_ACCENTCALLOUT2 = (short) 45;
    public static final short ST_ACCENTCALLOUT3 = (short) 46;
    public static final short ST_BORDERCALLOUT1 = (short) 47;
    public static final short ST_BORDERCALLOUT2 = (short) 48;
    public static final short ST_BORDERCALLOUT3 = (short) 49;
    public static final short ST_ACCENTBORDERCALLOUT1 = (short) 50;
    public static final short ST_ACCENTBORDERCALLOUT2 = (short) 51;
    public static final short ST_ACCENTBORDERCALLOUT3 = (short) 52;
    public static final short ST_RIBBON = (short) 53;
    public static final short ST_RIBBON2 = (short) 54;
    public static final short ST_CHEVRON = (short) 55;
    public static final short ST_PENTAGON = (short) 56;
    public static final short ST_NOSMOKING = (short) 57;
    public static final short ST_SEAL8 = (short) 58;
    public static final short ST_SEAL16 = (short) 59;
    public static final short ST_SEAL32 = (short) 60;
    public static final short ST_WEDGERECTCALLOUT = (short) 61;
    public static final short ST_WEDGERRECTCALLOUT = (short) 62;
    public static final short ST_WEDGEELLIPSECALLOUT = (short) 63;
    public static final short ST_WAVE = (short) 64;
    public static final short ST_FOLDEDCORNER = (short) 65;
    public static final short ST_LEFTARROW = (short) 66;
    public static final short ST_DOWNARROW = (short) 67;
    public static final short ST_UPARROW = (short) 68;
    public static final short ST_LEFTRIGHTARROW = (short) 69;
    public static final short ST_UPDOWNARROW = (short) 70;
    public static final short ST_IRREGULARSEAL1 = (short) 71;
    public static final short ST_IRREGULARSEAL2 = (short) 72;
    public static final short ST_LIGHTNINGBOLT = (short) 73;
    public static final short ST_HEART = (short) 74;
    public static final short ST_PICTUREFRAME = (short) 75;
    public static final short ST_QUADARROW = (short) 76;
    public static final short ST_LEFTARROWCALLOUT = (short) 77;
    public static final short ST_RIGHTARROWCALLOUT = (short) 78;
    public static final short ST_UPARROWCALLOUT = (short) 79;
    public static final short ST_DOWNARROWCALLOUT = (short) 80;
    public static final short ST_LEFTRIGHTARROWCALLOUT = (short) 81;
    public static final short ST_UPDOWNARROWCALLOUT = (short) 82;
    public static final short ST_QUADARROWCALLOUT = (short) 83;
    public static final short ST_BEVEL = (short) 84;
    public static final short ST_LEFTBRACKET = (short) 85;
    public static final short ST_RIGHTBRACKET = (short) 86;
    public static final short ST_LEFTBRACE = (short) 87;
    public static final short ST_RIGHTBRACE = (short) 88;
    public static final short ST_LEFTUPARROW = (short) 89;
    public static final short ST_BENTUPARROW = (short) 90;
    public static final short ST_BENTARROW = (short) 91;
    public static final short ST_SEAL24 = (short) 92;
    public static final short ST_STRIPEDRIGHTARROW = (short) 93;
    public static final short ST_NOTCHEDRIGHTARROW = (short) 94;
    public static final short ST_BLOCKARC = (short) 95;
    public static final short ST_SMILEYFACE = (short) 96;
    public static final short ST_VERTICALSCROLL = (short) 97;
    public static final short ST_HORIZONTALSCROLL = (short) 98;
    public static final short ST_CIRCULARARROW = (short) 99;
    public static final short ST_NOTCHEDCIRCULARARROW = (short) 100;
    public static final short ST_UTURNARROW = (short) 101;
    public static final short ST_CURVEDRIGHTARROW = (short) 102;
    public static final short ST_CURVEDLEFTARROW = (short) 103;
    public static final short ST_CURVEDUPARROW = (short) 104;
    public static final short ST_CURVEDDOWNARROW = (short) 105;
    public static final short ST_CLOUDCALLOUT = (short) 106;
    public static final short ST_ELLIPSERIBBON = (short) 107;
    public static final short ST_ELLIPSERIBBON2 = (short) 108;
    public static final short ST_FLOWCHARTPROCESS = (short) 109;
    public static final short ST_FLOWCHARTDECISION = (short) 110;
    public static final short ST_FLOWCHARTINPUTOUTPUT = (short) 111;
    public static final short ST_FLOWCHARTPREDEFINEDPROCESS = (short) 112;
    public static final short ST_FLOWCHARTINTERNALSTORAGE = (short) 113;
    public static final short ST_FLOWCHARTDOCUMENT = (short) 114;
    public static final short ST_FLOWCHARTMULTIDOCUMENT = (short) 115;
    public static final short ST_FLOWCHARTTERMINATOR = (short) 116;
    public static final short ST_FLOWCHARTPREPARATION = (short) 117;
    public static final short ST_FLOWCHARTMANUALINPUT = (short) 118;
    public static final short ST_FLOWCHARTMANUALOPERATION = (short) 119;
    public static final short ST_FLOWCHARTCONNECTOR = (short) 120;
    public static final short ST_FLOWCHARTPUNCHEDCARD = (short) 121;
    public static final short ST_FLOWCHARTPUNCHEDTAPE = (short) 122;
    public static final short ST_FLOWCHARTSUMMINGJUNCTION = (short) 123;
    public static final short ST_FLOWCHARTOR = (short) 124;
    public static final short ST_FLOWCHARTCOLLATE = (short) 125;
    public static final short ST_FLOWCHARTSORT = (short) 126;
    public static final short ST_FLOWCHARTEXTRACT = (short) 127;
    public static final short ST_FLOWCHARTMERGE = (short) 128;
    public static final short ST_FLOWCHARTOFFLINESTORAGE = (short) 129;
    public static final short ST_FLOWCHARTONLINESTORAGE = (short) 130;
    public static final short ST_FLOWCHARTMAGNETICTAPE = (short) 131;
    public static final short ST_FLOWCHARTMAGNETICDISK = (short) 132;
    public static final short ST_FLOWCHARTMAGNETICDRUM = (short) 133;
    public static final short ST_FLOWCHARTDISPLAY = (short) 134;
    public static final short ST_FLOWCHARTDELAY = (short) 135;
    public static final short ST_TEXTPLAINTEXT = (short) 136;
    public static final short ST_TEXTSTOP = (short) 137;
    public static final short ST_TEXTTRIANGLE = (short) 138;
    public static final short ST_TEXTTRIANGLEINVERTED = (short) 139;
    public static final short ST_TEXTCHEVRON = (short) 140;
    public static final short ST_TEXTCHEVRONINVERTED = (short) 141;
    public static final short ST_TEXTRINGINSIDE = (short) 142;
    public static final short ST_TEXTRINGOUTSIDE = (short) 143;
    public static final short ST_TEXTARCHUPCURVE = (short) 144;
    public static final short ST_TEXTARCHDOWNCURVE = (short) 145;
    public static final short ST_TEXTCIRCLECURVE = (short) 146;
    public static final short ST_TEXTBUTTONCURVE = (short) 147;
    public static final short ST_TEXTARCHUPPOUR = (short) 148;
    public static final short ST_TEXTARCHDOWNPOUR = (short) 149;
    public static final short ST_TEXTCIRCLEPOUR = (short) 150;
    public static final short ST_TEXTBUTTONPOUR = (short) 151;
    public static final short ST_TEXTCURVEUP = (short) 152;
    public static final short ST_TEXTCURVEDOWN = (short) 153;
    public static final short ST_TEXTCASCADEUP = (short) 154;
    public static final short ST_TEXTCASCADEDOWN = (short) 155;
    public static final short ST_TEXTWAVE1 = (short) 156;
    public static final short ST_TEXTWAVE2 = (short) 157;
    public static final short ST_TEXTWAVE3 = (short) 158;
    public static final short ST_TEXTWAVE4 = (short) 159;
    public static final short ST_TEXTINFLATE = (short) 160;
    public static final short ST_TEXTDEFLATE = (short) 161;
    public static final short ST_TEXTINFLATEBOTTOM = (short) 162;
    public static final short ST_TEXTDEFLATEBOTTOM = (short) 163;
    public static final short ST_TEXTINFLATETOP = (short) 164;
    public static final short ST_TEXTDEFLATETOP = (short) 165;
    public static final short ST_TEXTDEFLATEINFLATE = (short) 166;
    public static final short ST_TEXTDEFLATEINFLATEDEFLATE = (short) 167;
    public static final short ST_TEXTFADERIGHT = (short) 168;
    public static final short ST_TEXTFADELEFT = (short) 169;
    public static final short ST_TEXTFADEUP = (short) 170;
    public static final short ST_TEXTFADEDOWN = (short) 171;
    public static final short ST_TEXTSLANTUP = (short) 172;
    public static final short ST_TEXTSLANTDOWN = (short) 173;
    public static final short ST_TEXTCANUP = (short) 174;
    public static final short ST_TEXTCANDOWN = (short) 175;
    public static final short ST_FLOWCHARTALTERNATEPROCESS = (short) 176;
    public static final short ST_FLOWCHARTOFFPAGECONNECTOR = (short) 177;
    public static final short ST_CALLOUT90 = (short) 178;
    public static final short ST_ACCENTCALLOUT90 = (short) 179;
    public static final short ST_BORDERCALLOUT90 = (short) 180;
    public static final short ST_ACCENTBORDERCALLOUT90 = (short) 181;
    public static final short ST_LEFTRIGHTUPARROW = (short) 182;
    public static final short ST_SUN = (short) 183;
    public static final short ST_MOON = (short) 184;
    public static final short ST_BRACKETPAIR = (short) 185;
    public static final short ST_BRACEPAIR = (short) 186;
    public static final short ST_SEAL4 = (short) 187;
    public static final short ST_DOUBLEWAVE = (short) 188;
    public static final short ST_ACTIONBUTTONBLANK = (short) 189;
    public static final short ST_ACTIONBUTTONHOME = (short) 190;
    public static final short ST_ACTIONBUTTONHELP = (short) 191;
    public static final short ST_ACTIONBUTTONINFORMATION = (short) 192;
    public static final short ST_ACTIONBUTTONFORWARDNEXT = (short) 193;
    public static final short ST_ACTIONBUTTONBACKPREVIOUS = (short) 194;
    public static final short ST_ACTIONBUTTONEND = (short) 195;
    public static final short ST_ACTIONBUTTONBEGINNING = (short) 196;
    public static final short ST_ACTIONBUTTONRETURN = (short) 197;
    public static final short ST_ACTIONBUTTONDOCUMENT = (short) 198;
    public static final short ST_ACTIONBUTTONSOUND = (short) 199;
    public static final short ST_ACTIONBUTTONMOVIE = (short) 200;
    public static final short ST_HOSTCONTROL = (short) 201;
    public static final short ST_TEXTBOX = (short) 202;
    public static final short ST_NIL = (short) 0x0FFF;

    /**
     * Maps shape container objects to their {@link TextObjectRecord} or {@link ObjRecord}
     */
    private final Map<EscherRecord, Record> shapeToObj = new HashMap<>();

    /**
     * list of "tail" records that need to be serialized after all drawing group records
     */
    private final Map<Integer, NoteRecord> tailRec = new LinkedHashMap<>();

    /**
     * create new EscherAggregate
     * @param createDefaultTree if true creates base tree of the escher records, see EscherAggregate.buildBaseTree()
     *                          else return empty escher aggregate
     */
    public EscherAggregate(boolean createDefaultTree) {
        if (createDefaultTree){
            buildBaseTree();
        }
    }

    /**
     * @return Returns the current sid.
     */
    public short getSid() {
        return sid;
    }

    /**
     * Calculates the string representation of this record.  This is
     * simply a dump of all the records.
     */
    public String toString() {
        String nl = System.getProperty("line.separtor");

        StringBuilder result = new StringBuilder();
        result.append('[').append(getRecordName()).append(']').append(nl);
        for (EscherRecord escherRecord : getEscherRecords()) {
            result.append(escherRecord);
        }
        result.append("[/").append(getRecordName()).append(']').append(nl);

        return result.toString();
    }

    /**
     * Calculates the xml representation of this record.  This is
     * simply a dump of all the records.
     * @param tab - string which must be added before each line (used by default '\t')
     * @return xml representation of the all aggregated records
     */
    public String toXml(String tab) {
        StringBuilder builder = new StringBuilder();
        builder.append(tab).append("<").append(getRecordName()).append(">\n");
        for (EscherRecord escherRecord : getEscherRecords()) {
            builder.append(escherRecord.toXml(tab + "\t"));
        }
        builder.append(tab).append("</").append(getRecordName()).append(">\n");
        return builder.toString();
    }

    /**
     * @param sid - record sid we want to check if it belongs to drawing layer
     * @return true if record is instance of DrawingRecord or ContinueRecord or ObjRecord or TextObjRecord
     */
    private static boolean isDrawingLayerRecord(final short sid) {
        return sid == DrawingRecord.sid ||
                sid == ContinueRecord.sid ||
                sid == ObjRecord.sid ||
                sid == TextObjectRecord.sid;
    }

    /**
     * Collapses the drawing records into an aggregate.
     * read Drawing, Obj, TxtObj, Note and Continue records into single byte array,
     * create Escher tree from byte array, create map &lt;EscherRecord, Record&gt;
     *
     * @param records - list of all records inside sheet
     * @param locFirstDrawingRecord - location of the first DrawingRecord inside sheet
     * @return new EscherAggregate create from all aggregated records which belong to drawing layer
     */
    public static EscherAggregate createAggregate(List<RecordBase> records, int locFirstDrawingRecord) {
        // Keep track of any shape records created so we can match them back to the object id's.
        // Textbox objects are also treated as shape objects.
        final List<EscherRecord> shapeRecords = new ArrayList<>();
        EscherRecordFactory recordFactory = new DefaultEscherRecordFactory() {
            public EscherRecord createRecord(byte[] data, int offset) {
                EscherRecord r = super.createRecord(data, offset);
                if (r.getRecordId() == EscherClientDataRecord.RECORD_ID || r.getRecordId() == EscherTextboxRecord.RECORD_ID) {
                    shapeRecords.add(r);
                }
                return r;
            }
        };

        // Create one big buffer
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        EscherAggregate agg = new EscherAggregate(false);
        int loc = locFirstDrawingRecord;
        while (loc + 1 < records.size()
                && (isDrawingLayerRecord(sid(records, loc)))) {
            try {
                if (!(sid(records, loc) == DrawingRecord.sid || sid(records, loc) == ContinueRecord.sid)) {
                    loc++;
                    continue;
                }
                if (sid(records, loc) == DrawingRecord.sid) {
                    buffer.write(((DrawingRecord) records.get(loc)).getRecordData());
                } else {
                    buffer.write(((ContinueRecord) records.get(loc)).getData());
                }
            } catch (IOException e) {
                throw new RuntimeException("Couldn't get data from drawing/continue records", e);
            }
            loc++;
        }

        // Decode the shapes
        // agg.escherRecords = new ArrayList();
        int pos = 0;
        while (pos < buffer.size()) {
            EscherRecord r = recordFactory.createRecord(buffer.toByteArray(), pos);
            int bytesRead = r.fillFields(buffer.toByteArray(), pos, recordFactory);
            agg.addEscherRecord(r);
            pos += bytesRead;
        }

        // Associate the object records with the shapes
        loc = locFirstDrawingRecord + 1;
        int shapeIndex = 0;
        while (loc < records.size()
                && (isDrawingLayerRecord(sid(records, loc)))) {
            if (!isObjectRecord(records, loc)) {
                loc++;
                continue;
            }
            Record objRecord = (Record) records.get(loc);
            agg.shapeToObj.put(shapeRecords.get(shapeIndex++), objRecord);
            loc++;
        }

        // any NoteRecords that follow the drawing block must be aggregated and and saved in the tailRec collection
        while (loc < records.size()) {
            if (sid(records, loc) == NoteRecord.sid) {
                NoteRecord r = (NoteRecord) records.get(loc);
                agg.tailRec.put(r.getShapeId(), r);
            } else {
                break;
            }
            loc++;
        }

        int locLastDrawingRecord = loc;
        // replace drawing block with the created EscherAggregate
        records.subList(locFirstDrawingRecord, locLastDrawingRecord).clear();
        records.add(locFirstDrawingRecord, agg);
        return agg;
    }

    /**
     * Serializes this aggregate to a byte array.  Since this is an aggregate
     * record it will effectively serialize the aggregated records.
     *
     * @param offset The offset into the start of the array.
     * @param data   The byte array to serialize to.
     * @return The number of bytes serialized.
     */
    public int serialize(int offset, byte[] data) {
        // Determine buffer size
        List <EscherRecord>records = getEscherRecords();
        int size = getEscherRecordSize(records);
        byte[] buffer = new byte[size];

        // Serialize escher records into one big data structure and keep note of ending offsets.
        final List <Integer>spEndingOffsets = new ArrayList<>();
        final List <EscherRecord> shapes = new ArrayList<>();
        int pos = 0;
        for (Object record : records) {
            EscherRecord e = (EscherRecord) record;
            pos += e.serialize(pos, buffer, new EscherSerializationListener() {
                public void beforeRecordSerialize(int offset, short recordId, EscherRecord record) {
                }

                public void afterRecordSerialize(int offset, short recordId, int size, EscherRecord record) {
                    if (recordId == EscherClientDataRecord.RECORD_ID || recordId == EscherTextboxRecord.RECORD_ID) {
                        spEndingOffsets.add(offset);
                        shapes.add(record);
                    }
                }
            });
        }
        shapes.add(0, null);
        spEndingOffsets.add(0, 0);

        // Split escher records into separate MSODRAWING and OBJ, TXO records.  (We don't break on
        // the first one because it's the patriach).
        pos = offset;
        int writtenEscherBytes = 0;
        int i;
        for (i = 1; i < shapes.size(); i++) {
            int endOffset = spEndingOffsets.get(i) - 1;
            int startOffset;
            if (i == 1)
                startOffset = 0;
            else
                startOffset = spEndingOffsets.get(i - 1);

            byte[] drawingData = new byte[endOffset - startOffset + 1];
            System.arraycopy(buffer, startOffset, drawingData, 0, drawingData.length);
            pos += writeDataIntoDrawingRecord(drawingData, writtenEscherBytes, pos, data, i);

            writtenEscherBytes += drawingData.length;

            // Write the matching OBJ record
            Record obj = shapeToObj.get(shapes.get(i));
            pos += obj.serialize(pos, data);

            if (i == shapes.size() - 1 && endOffset < buffer.length - 1) {
                drawingData = new byte[buffer.length - endOffset - 1];
                System.arraycopy(buffer, endOffset + 1, drawingData, 0, drawingData.length);
                pos += writeDataIntoDrawingRecord(drawingData, writtenEscherBytes, pos, data, i);
            }
        }
        if ((pos - offset) < buffer.length - 1) {
            byte[] drawingData = new byte[buffer.length - (pos - offset)];
            System.arraycopy(buffer, (pos - offset), drawingData, 0, drawingData.length);
            pos += writeDataIntoDrawingRecord(drawingData, writtenEscherBytes, pos, data, i);
        }

        for (NoteRecord noteRecord : tailRec.values()) {
            pos += noteRecord.serialize(pos, data);
        }
        int bytesWritten = pos - offset;
        if (bytesWritten != getRecordSize())
            throw new RecordFormatException(bytesWritten + " bytes written but getRecordSize() reports " + getRecordSize());
        return bytesWritten;
    }

    /**
     * @param drawingData - escher records saved into single byte array
     * @param writtenEscherBytes - count of bytes already saved into drawing records (we should know it to decide create
     *                           drawing or continue record)
     * @param pos current position of data array
     * @param data - array of bytes where drawing records must be serialized
     * @param i - number of shape, saved into data array
     * @return offset of data array after serialization
     */
    private int writeDataIntoDrawingRecord(byte[] drawingData, int writtenEscherBytes, int pos, byte[] data, int i) {
        int temp = 0;
        //First record in drawing layer MUST be DrawingRecord
        if (writtenEscherBytes + drawingData.length > RecordInputStream.MAX_RECORD_DATA_SIZE && i != 1) {
            for (int j = 0; j < drawingData.length; j += RecordInputStream.MAX_RECORD_DATA_SIZE) {
                byte[] buf = new byte[Math.min(RecordInputStream.MAX_RECORD_DATA_SIZE, drawingData.length - j)];
                System.arraycopy(drawingData, j, buf, 0, Math.min(RecordInputStream.MAX_RECORD_DATA_SIZE, drawingData.length - j));
                ContinueRecord drawing = new ContinueRecord(buf);
                temp += drawing.serialize(pos + temp, data);
            }
        } else {
            for (int j = 0; j < drawingData.length; j += RecordInputStream.MAX_RECORD_DATA_SIZE) {
                if (j == 0) {
                    DrawingRecord drawing = new DrawingRecord();
                    byte[] buf = new byte[Math.min(RecordInputStream.MAX_RECORD_DATA_SIZE, drawingData.length - j)];
                    System.arraycopy(drawingData, j, buf, 0, Math.min(RecordInputStream.MAX_RECORD_DATA_SIZE, drawingData.length - j));
                    drawing.setData(buf);
                    temp += drawing.serialize(pos + temp, data);
                } else {
                    byte[] buf = new byte[Math.min(RecordInputStream.MAX_RECORD_DATA_SIZE, drawingData.length - j)];
                    System.arraycopy(drawingData, j, buf, 0, Math.min(RecordInputStream.MAX_RECORD_DATA_SIZE, drawingData.length - j));
                    ContinueRecord drawing = new ContinueRecord(buf);
                    temp += drawing.serialize(pos + temp, data);
                }
            }
        }
        return temp;
    }

    /**
     * How many bytes do the raw escher records contain.
     *
     * @param records List of escher records
     * @return the number of bytes
     */
    private int getEscherRecordSize(List<EscherRecord> records) {
        int size = 0;
        for (EscherRecord record : records){
            size += record.getRecordSize();
        }
        return size;
    }

    /**
     * @return record size, including header size of obj, text, note, drawing, continue records
     */
    public int getRecordSize() {
        // To determine size of aggregate record we have to know size of each DrawingRecord because if DrawingRecord
        // is split into several continue records we have to add header size to total EscherAggregate size
        int continueRecordsHeadersSize = 0;
        // Determine buffer size
        List<EscherRecord> records = getEscherRecords();
        int rawEscherSize = getEscherRecordSize(records);
        byte[] buffer = IOUtils.safelyAllocate(rawEscherSize, MAX_RECORD_LENGTH);
        final List<Integer> spEndingOffsets = new ArrayList<>();
        int pos = 0;
        for (EscherRecord e : records) {
            pos += e.serialize(pos, buffer, new EscherSerializationListener() {
                public void beforeRecordSerialize(int offset, short recordId, EscherRecord record) {
                }

                public void afterRecordSerialize(int offset, short recordId, int size, EscherRecord record) {
                    if (recordId == EscherClientDataRecord.RECORD_ID || recordId == EscherTextboxRecord.RECORD_ID) {
                        spEndingOffsets.add(offset);
                    }
                }
            });
        }
        spEndingOffsets.add(0, 0);

        for (int i = 1; i < spEndingOffsets.size(); i++) {
            if (i == spEndingOffsets.size() - 1 && spEndingOffsets.get(i) < pos) {
                continueRecordsHeadersSize += 4;
            }
            if (spEndingOffsets.get(i) - spEndingOffsets.get(i - 1) <= RecordInputStream.MAX_RECORD_DATA_SIZE) {
                continue;
            }
            continueRecordsHeadersSize += ((spEndingOffsets.get(i) - spEndingOffsets.get(i - 1)) / RecordInputStream.MAX_RECORD_DATA_SIZE) * 4;
        }

        int drawingRecordSize = rawEscherSize + (shapeToObj.size()) * 4;
        if (rawEscherSize != 0 && spEndingOffsets.size() == 1/**EMPTY**/) {
            continueRecordsHeadersSize += 4;
        }
        int objRecordSize = 0;
        for (Record r : shapeToObj.values()) {
            objRecordSize += r.getRecordSize();
        }
        int tailRecordSize = 0;
        for (NoteRecord noteRecord : tailRec.values()) {
            tailRecordSize += noteRecord.getRecordSize();
        }
        return drawingRecordSize + objRecordSize + tailRecordSize + continueRecordsHeadersSize;
    }

    /**
     * Associates an escher record to an OBJ record or a TXO record.
     * @param r - ClientData or Textbox record
     * @param objRecord - Obj or TextObj record
     */
    public void associateShapeToObjRecord(EscherRecord r, Record objRecord) {
        shapeToObj.put(r, objRecord);
    }

    /**
     * Remove echerRecord and associated to it Obj or TextObj record
     * @param rec - clientData or textbox record to be removed
     */
    public void removeShapeToObjRecord(EscherRecord rec) {
        shapeToObj.remove(rec);
    }

    /**
     * @return "ESCHERAGGREGATE"
     */
    protected String getRecordName() {
        return "ESCHERAGGREGATE";
    }

    // =============== Private methods ========================

    /**
     *
     * @param records list of the record to look inside
     * @param loc location of the checked record
     * @return true if record is instance of ObjRecord or TextObjectRecord
     */
    private static boolean isObjectRecord(List <RecordBase>records, int loc) {
        return sid(records, loc) == ObjRecord.sid || sid(records, loc) == TextObjectRecord.sid;
    }

    /**
     * create base tree with such structure:
     * EscherDgContainer
     * -EscherSpgrContainer
     * --EscherSpContainer
     * ---EscherSpRecord
     * ---EscherSpgrRecord
     * ---EscherSpRecord
     * -EscherDgRecord
     *
     * id of DgRecord and SpRecord are empty and must be set later by HSSFPatriarch
     */
    private void buildBaseTree() {
        EscherContainerRecord dgContainer = new EscherContainerRecord();
        EscherContainerRecord spgrContainer = new EscherContainerRecord();
        EscherContainerRecord spContainer1 = new EscherContainerRecord();
        EscherSpgrRecord spgr = new EscherSpgrRecord();
        EscherSpRecord sp1 = new EscherSpRecord();
        dgContainer.setRecordId(EscherContainerRecord.DG_CONTAINER);
        dgContainer.setOptions((short) 0x000F);
        EscherDgRecord dg = new EscherDgRecord();
        dg.setRecordId(EscherDgRecord.RECORD_ID);
        short dgId = 1;
        dg.setOptions((short) (dgId << 4));
        dg.setNumShapes(0);
        dg.setLastMSOSPID(1024);
        spgrContainer.setRecordId(EscherContainerRecord.SPGR_CONTAINER);
        spgrContainer.setOptions((short) 0x000F);
        spContainer1.setRecordId(EscherContainerRecord.SP_CONTAINER);
        spContainer1.setOptions((short) 0x000F);
        spgr.setRecordId(EscherSpgrRecord.RECORD_ID);
        spgr.setOptions((short) 0x0001);    // version
        spgr.setRectX1(0);
        spgr.setRectY1(0);
        spgr.setRectX2(1023);
        spgr.setRectY2(255);
        sp1.setRecordId(EscherSpRecord.RECORD_ID);

        sp1.setOptions((short) 0x0002);
        sp1.setVersion((short) 0x2);
        sp1.setShapeId(-1);
        sp1.setFlags(EscherSpRecord.FLAG_GROUP | EscherSpRecord.FLAG_PATRIARCH);
        dgContainer.addChildRecord(dg);
        dgContainer.addChildRecord(spgrContainer);
        spgrContainer.addChildRecord(spContainer1);
        spContainer1.addChildRecord(spgr);
        spContainer1.addChildRecord(sp1);
        addEscherRecord(dgContainer);
    }

    /**
     * EscherDgContainer
     * -EscherSpgrContainer
     * -EscherDgRecord - set id for this record
     * set id for DgRecord of DgContainer
     * @param dgId - id which must be set
     */
    public void setDgId(short dgId) {
        EscherContainerRecord dgContainer = getEscherContainer();
        EscherDgRecord dg = dgContainer.getChildById(EscherDgRecord.RECORD_ID);
        dg.setOptions((short) (dgId << 4));
    }

    /**
     * EscherDgContainer
     * -EscherSpgrContainer
     * --EscherSpContainer
     * ---EscherSpRecord -set id for this record
     * ---***
     * --***
     * -EscherDgRecord
     * set id for the sp record of the first spContainer in main spgrConatiner
     * @param shapeId - id which must be set
     */
    public void setMainSpRecordId(int shapeId) {
        EscherContainerRecord dgContainer = getEscherContainer();
        EscherContainerRecord spgrConatiner = dgContainer.getChildById(EscherContainerRecord.SPGR_CONTAINER);
        EscherContainerRecord spContainer = (EscherContainerRecord) spgrConatiner.getChild(0);
        EscherSpRecord sp = spContainer.getChildById(EscherSpRecord.RECORD_ID);
        sp.setShapeId(shapeId);
    }

    /**
     * @param records list of records to look into
     * @param loc - location of the record which sid must be returned
     * @return sid of the record with selected location
     */
    private static short sid(List<RecordBase> records, int loc) {
        RecordBase record = records.get(loc);
        if (record instanceof Record) {
            return ((Record)record).getSid();
        } else {
            // Aggregates don't have a sid
            // We could step into them, but for these needs we don't care
            return -1;
        }
    }

    /**
     * @return unmodifiable copy of the mapping  of {@link EscherClientDataRecord} and {@link EscherTextboxRecord}
     * to their {@link TextObjectRecord} or {@link ObjRecord} .
     * <p>
     * We need to access it outside of EscherAggregate when building shapes
     */
    public Map<EscherRecord, Record> getShapeToObjMapping() {
        return Collections.unmodifiableMap(shapeToObj);
    }

    /**
     * @return unmodifiable copy of tail records. We need to access them when building shapes.
     *         Every HSSFComment shape has a link to a NoteRecord from the tailRec collection.
     */
    public Map<Integer, NoteRecord> getTailRecords() {
        return Collections.unmodifiableMap(tailRec);
    }

    /**
     * @param obj - ObjRecord with id == NoteRecord.id
     * @return null if note record is not found else returns note record with id == obj.id
     */
    public NoteRecord getNoteRecordByObj(ObjRecord obj) {
        CommonObjectDataSubRecord cod = (CommonObjectDataSubRecord) obj.getSubRecords().get(0);
        return tailRec.get(cod.getObjectId());
    }

    /**
     * Add tail record to existing map
     * @param note to be added
     */
    public void addTailRecord(NoteRecord note) {
        tailRec.put(note.getShapeId(), note);
    }

    /**
     * Remove tail record from the existing map
     * @param note to be removed
     */
    public void removeTailRecord(NoteRecord note) {
        tailRec.remove(note.getShapeId());
    }
}

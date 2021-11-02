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

import static org.apache.poi.hssf.record.RecordInputStream.MAX_RECORD_DATA_SIZE;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.commons.io.output.UnsynchronizedByteArrayOutputStream;
import org.apache.poi.ddf.DefaultEscherRecordFactory;
import org.apache.poi.ddf.EscherClientDataRecord;
import org.apache.poi.ddf.EscherContainerRecord;
import org.apache.poi.ddf.EscherDgRecord;
import org.apache.poi.ddf.EscherRecord;
import org.apache.poi.ddf.EscherSerializationListener;
import org.apache.poi.ddf.EscherSpRecord;
import org.apache.poi.ddf.EscherSpgrRecord;
import org.apache.poi.ddf.EscherTextboxRecord;
import org.apache.poi.sl.usermodel.ShapeType;
import org.apache.poi.util.GenericRecordXmlWriter;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.RecordFormatException;
import org.apache.poi.util.Removal;

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
    // not a real sid - dummy value
    public static final short sid = 9876;
    //arbitrarily selected; may need to increase
    private static final int DEFAULT_MAX_RECORD_LENGTH = 100_000_000;
    private static int MAX_RECORD_LENGTH = DEFAULT_MAX_RECORD_LENGTH;

    /**
     * @param length the max record length allowed for EscherAggregate
     */
    public static void setMaxRecordLength(int length) {
        MAX_RECORD_LENGTH = length;
    }

    /**
     * @return the max record length allowed for EscherAggregate
     */
    public static int getMaxRecordLength() {
        return MAX_RECORD_LENGTH;
    }

    /** @deprecated not used */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_MIN = (short) 0;
    /** @deprecated use {@link ShapeType#NOT_PRIMITIVE} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_NOT_PRIMATIVE = (short) 0;
    /** @deprecated use {@link ShapeType#RECT} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_RECTANGLE = (short) 1;
    /** @deprecated use {@link ShapeType#ROUND_RECT} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_ROUNDRECTANGLE = (short) 2;
    /** @deprecated use {@link ShapeType#ELLIPSE} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_ELLIPSE = (short) 3;
    /** @deprecated use {@link ShapeType#DIAMOND} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_DIAMOND = (short) 4;
    /** @deprecated use {@link ShapeType#TRIANGLE} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_ISOCELESTRIANGLE = (short) 5;
    /** @deprecated use {@link ShapeType#RT_TRIANGLE} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_RIGHTTRIANGLE = (short) 6;
    /** @deprecated use {@link ShapeType#PARALLELOGRAM} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_PARALLELOGRAM = (short) 7;
    /** @deprecated use {@link ShapeType#TRAPEZOID} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_TRAPEZOID = (short) 8;
    /** @deprecated use {@link ShapeType#HEXAGON} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_HEXAGON = (short) 9;
    /** @deprecated use {@link ShapeType#OCTAGON} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_OCTAGON = (short) 10;
    /** @deprecated use {@link ShapeType#PLUS} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_PLUS = (short) 11;
    /** @deprecated use {@link ShapeType#STAR_5} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_STAR = (short) 12;
    /** @deprecated use {@link ShapeType#RIGHT_ARROW} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_ARROW = (short) 13;
    /** @deprecated use {@link ShapeType#THICK_ARROW} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_THICKARROW = (short) 14;
    /** @deprecated use {@link ShapeType#HOME_PLATE} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_HOMEPLATE = (short) 15;
    /** @deprecated use {@link ShapeType#CUBE} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_CUBE = (short) 16;
    /** @deprecated use {@link ShapeType#BALLOON} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_BALLOON = (short) 17;
    /** @deprecated use {@link ShapeType#SEAL} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_SEAL = (short) 18;
    /** @deprecated use {@link ShapeType#ARC} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_ARC = (short) 19;
    /** @deprecated use {@link ShapeType#LINE} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_LINE = (short) 20;
    /** @deprecated use {@link ShapeType#PLAQUE} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_PLAQUE = (short) 21;
    /** @deprecated use {@link ShapeType#CAN} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_CAN = (short) 22;
    /** @deprecated use {@link ShapeType#DONUT} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_DONUT = (short) 23;
    /** @deprecated use {@link ShapeType#TEXT_SIMPLE} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_TEXTSIMPLE = (short) 24;
    /** @deprecated use {@link ShapeType#TEXT_OCTAGON} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_TEXTOCTAGON = (short) 25;
    /** @deprecated use {@link ShapeType#TEXT_HEXAGON} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_TEXTHEXAGON = (short) 26;
    /** @deprecated use {@link ShapeType#TEXT_CURVE} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_TEXTCURVE = (short) 27;
    /** @deprecated use {@link ShapeType#TEXT_WAVE} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_TEXTWAVE = (short) 28;
    /** @deprecated use {@link ShapeType#TEXT_RING} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_TEXTRING = (short) 29;
    /** @deprecated use {@link ShapeType#TEXT_ON_CURVE} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_TEXTONCURVE = (short) 30;
    /** @deprecated use {@link ShapeType#TEXT_ON_RING} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_TEXTONRING = (short) 31;
    /** @deprecated use {@link ShapeType#STRAIGHT_CONNECTOR_1} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_STRAIGHTCONNECTOR1 = (short) 32;
    /** @deprecated use {@link ShapeType#BENT_CONNECTOR_2} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_BENTCONNECTOR2 = (short) 33;
    /** @deprecated use {@link ShapeType#BENT_CONNECTOR_3} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_BENTCONNECTOR3 = (short) 34;
    /** @deprecated use {@link ShapeType#BENT_CONNECTOR_4} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_BENTCONNECTOR4 = (short) 35;
    /** @deprecated use {@link ShapeType#BENT_CONNECTOR_5} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_BENTCONNECTOR5 = (short) 36;
    /** @deprecated use {@link ShapeType#CURVED_CONNECTOR_2} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_CURVEDCONNECTOR2 = (short) 37;
    /** @deprecated use {@link ShapeType#CURVED_CONNECTOR_3} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_CURVEDCONNECTOR3 = (short) 38;
    /** @deprecated use {@link ShapeType#CURVED_CONNECTOR_3} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_CURVEDCONNECTOR4 = (short) 39;
    /** @deprecated use {@link ShapeType#CURVED_CONNECTOR_5} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_CURVEDCONNECTOR5 = (short) 40;
    /** @deprecated use {@link ShapeType#CALLOUT_1} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_CALLOUT1 = (short) 41;
    /** @deprecated use {@link ShapeType#CALLOUT_2} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_CALLOUT2 = (short) 42;
    /** @deprecated use {@link ShapeType#CALLOUT_3} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_CALLOUT3 = (short) 43;
    /** @deprecated use {@link ShapeType#ACCENT_CALLOUT_1} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_ACCENTCALLOUT1 = (short) 44;
    /** @deprecated use {@link ShapeType#ACCENT_CALLOUT_2} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_ACCENTCALLOUT2 = (short) 45;
    /** @deprecated use {@link ShapeType#ACCENT_CALLOUT_3} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_ACCENTCALLOUT3 = (short) 46;
    /** @deprecated use {@link ShapeType#BORDER_CALLOUT_1} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_BORDERCALLOUT1 = (short) 47;
    /** @deprecated use {@link ShapeType#BORDER_CALLOUT_2} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_BORDERCALLOUT2 = (short) 48;
    /** @deprecated use {@link ShapeType#BORDER_CALLOUT_3} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_BORDERCALLOUT3 = (short) 49;
    /** @deprecated use {@link ShapeType#ACCENT_BORDER_CALLOUT_1} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_ACCENTBORDERCALLOUT1 = (short) 50;
    /** @deprecated use {@link ShapeType#ACCENT_BORDER_CALLOUT_2} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_ACCENTBORDERCALLOUT2 = (short) 51;
    /** @deprecated use {@link ShapeType#ACCENT_BORDER_CALLOUT_3} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_ACCENTBORDERCALLOUT3 = (short) 52;
    /** @deprecated use {@link ShapeType#RIBBON} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_RIBBON = (short) 53;
    /** @deprecated use {@link ShapeType#RIBBON_2} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_RIBBON2 = (short) 54;
    /** @deprecated use {@link ShapeType#CHEVRON} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_CHEVRON = (short) 55;
    /** @deprecated use {@link ShapeType#PENTAGON} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_PENTAGON = (short) 56;
    /** @deprecated use {@link ShapeType#NO_SMOKING} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_NOSMOKING = (short) 57;
    /** @deprecated use {@link ShapeType#STAR_8} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_SEAL8 = (short) 58;
    /** @deprecated use {@link ShapeType#STAR_16} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_SEAL16 = (short) 59;
    /** @deprecated use {@link ShapeType#STAR_32} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_SEAL32 = (short) 60;
    /** @deprecated use {@link ShapeType#WEDGE_RECT_CALLOUT} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_WEDGERECTCALLOUT = (short) 61;
    /** @deprecated use {@link ShapeType#WEDGE_ROUND_RECT_CALLOUT} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_WEDGERRECTCALLOUT = (short) 62;
    /** @deprecated use {@link ShapeType#WEDGE_ELLIPSE_CALLOUT} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_WEDGEELLIPSECALLOUT = (short) 63;
    /** @deprecated use {@link ShapeType#WAVE} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_WAVE = (short) 64;
    /** @deprecated use {@link ShapeType#FOLDED_CORNER} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_FOLDEDCORNER = (short) 65;
    /** @deprecated use {@link ShapeType#LEFT_ARROW} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_LEFTARROW = (short) 66;
    /** @deprecated use {@link ShapeType#DOWN_ARROW} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_DOWNARROW = (short) 67;
    /** @deprecated use {@link ShapeType#UP_ARROW} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_UPARROW = (short) 68;
    /** @deprecated use {@link ShapeType#LEFT_RIGHT_ARROW} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_LEFTRIGHTARROW = (short) 69;
    /** @deprecated use {@link ShapeType#UP_DOWN_ARROW} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_UPDOWNARROW = (short) 70;
    /** @deprecated use {@link ShapeType#IRREGULAR_SEAL_1} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_IRREGULARSEAL1 = (short) 71;
    /** @deprecated use {@link ShapeType#IRREGULAR_SEAL_2} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_IRREGULARSEAL2 = (short) 72;
    /** @deprecated use {@link ShapeType#LIGHTNING_BOLT} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_LIGHTNINGBOLT = (short) 73;
    /** @deprecated use {@link ShapeType#HEART} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_HEART = (short) 74;
    /** @deprecated use {@link ShapeType#FRAME} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_PICTUREFRAME = (short) 75;
    /** @deprecated use {@link ShapeType#QUAD_ARROW} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_QUADARROW = (short) 76;
    /** @deprecated use {@link ShapeType#LEFT_ARROW_CALLOUT} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_LEFTARROWCALLOUT = (short) 77;
    /** @deprecated use {@link ShapeType#RIGHT_ARROW_CALLOUT} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_RIGHTARROWCALLOUT = (short) 78;
    /** @deprecated use {@link ShapeType#UP_ARROW_CALLOUT} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_UPARROWCALLOUT = (short) 79;
    /** @deprecated use {@link ShapeType#DOWN_ARROW_CALLOUT} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_DOWNARROWCALLOUT = (short) 80;
    /** @deprecated use {@link ShapeType#LEFT_RIGHT_ARROW_CALLOUT} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_LEFTRIGHTARROWCALLOUT = (short) 81;
    /** @deprecated use {@link ShapeType#UP_DOWN_ARROW_CALLOUT} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_UPDOWNARROWCALLOUT = (short) 82;
    /** @deprecated use {@link ShapeType#QUAD_ARROW_CALLOUT} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_QUADARROWCALLOUT = (short) 83;
    /** @deprecated use {@link ShapeType#BEVEL} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_BEVEL = (short) 84;
    /** @deprecated use {@link ShapeType#LEFT_BRACKET} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_LEFTBRACKET = (short) 85;
    /** @deprecated use {@link ShapeType#RIGHT_BRACKET} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_RIGHTBRACKET = (short) 86;
    /** @deprecated use {@link ShapeType#LEFT_BRACE} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_LEFTBRACE = (short) 87;
    /** @deprecated use {@link ShapeType#RIGHT_BRACE} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_RIGHTBRACE = (short) 88;
    /** @deprecated use {@link ShapeType#LEFT_UP_ARROW} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_LEFTUPARROW = (short) 89;
    /** @deprecated use {@link ShapeType#BENT_UP_ARROW} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_BENTUPARROW = (short) 90;
    /** @deprecated use {@link ShapeType#BENT_ARROW} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_BENTARROW = (short) 91;
    /** @deprecated use {@link ShapeType#STAR_24} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_SEAL24 = (short) 92;
    /** @deprecated use {@link ShapeType#STRIPED_RIGHT_ARROW} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_STRIPEDRIGHTARROW = (short) 93;
    /** @deprecated use {@link ShapeType#NOTCHED_RIGHT_ARROW} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_NOTCHEDRIGHTARROW = (short) 94;
    /** @deprecated use {@link ShapeType#BLOCK_ARC} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_BLOCKARC = (short) 95;
    /** @deprecated use {@link ShapeType#SMILEY_FACE} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_SMILEYFACE = (short) 96;
    /** @deprecated use {@link ShapeType#VERTICAL_SCROLL} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_VERTICALSCROLL = (short) 97;
    /** @deprecated use {@link ShapeType#HORIZONTAL_SCROLL} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_HORIZONTALSCROLL = (short) 98;
    /** @deprecated use {@link ShapeType#CIRCULAR_ARROW} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_CIRCULARARROW = (short) 99;
    /** @deprecated use {@link ShapeType#NOTCHED_CIRCULAR_ARROW} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_NOTCHEDCIRCULARARROW = (short) 100;
    /** @deprecated use {@link ShapeType#UTURN_ARROW} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_UTURNARROW = (short) 101;
    /** @deprecated use {@link ShapeType#CURVED_RIGHT_ARROW} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_CURVEDRIGHTARROW = (short) 102;
    /** @deprecated use {@link ShapeType#CURVED_LEFT_ARROW} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_CURVEDLEFTARROW = (short) 103;
    /** @deprecated use {@link ShapeType#CURVED_UP_ARROW} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_CURVEDUPARROW = (short) 104;
    /** @deprecated use {@link ShapeType#CURVED_DOWN_ARROW} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_CURVEDDOWNARROW = (short) 105;
    /** @deprecated use {@link ShapeType#CLOUD_CALLOUT} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_CLOUDCALLOUT = (short) 106;
    /** @deprecated use {@link ShapeType#ELLIPSE_RIBBON} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_ELLIPSERIBBON = (short) 107;
    /** @deprecated use {@link ShapeType#ELLIPSE_RIBBON_2} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_ELLIPSERIBBON2 = (short) 108;
    /** @deprecated use {@link ShapeType#FLOW_CHART_PROCESS} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_FLOWCHARTPROCESS = (short) 109;
    /** @deprecated use {@link ShapeType#FLOW_CHART_DECISION} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_FLOWCHARTDECISION = (short) 110;
    /** @deprecated use {@link ShapeType#FLOW_CHART_INPUT_OUTPUT} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_FLOWCHARTINPUTOUTPUT = (short) 111;
    /** @deprecated use {@link ShapeType#FLOW_CHART_PREDEFINED_PROCESS} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_FLOWCHARTPREDEFINEDPROCESS = (short) 112;
    /** @deprecated use {@link ShapeType#FLOW_CHART_INTERNAL_STORAGE} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_FLOWCHARTINTERNALSTORAGE = (short) 113;
    /** @deprecated use {@link ShapeType#FLOW_CHART_DOCUMENT} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_FLOWCHARTDOCUMENT = (short) 114;
    /** @deprecated use {@link ShapeType#FLOW_CHART_MULTIDOCUMENT} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_FLOWCHARTMULTIDOCUMENT = (short) 115;
    /** @deprecated use {@link ShapeType#FLOW_CHART_TERMINATOR} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_FLOWCHARTTERMINATOR = (short) 116;
    /** @deprecated use {@link ShapeType#FLOW_CHART_PREPARATION} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_FLOWCHARTPREPARATION = (short) 117;
    /** @deprecated use {@link ShapeType#FLOW_CHART_MANUAL_INPUT} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_FLOWCHARTMANUALINPUT = (short) 118;
    /** @deprecated use {@link ShapeType#FLOW_CHART_MANUAL_OPERATION} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_FLOWCHARTMANUALOPERATION = (short) 119;
    /** @deprecated use {@link ShapeType#FLOW_CHART_CONNECTOR} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_FLOWCHARTCONNECTOR = (short) 120;
    /** @deprecated use {@link ShapeType#FLOW_CHART_PUNCHED_CARD} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_FLOWCHARTPUNCHEDCARD = (short) 121;
    /** @deprecated use {@link ShapeType#FLOW_CHART_PUNCHED_TAPE} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_FLOWCHARTPUNCHEDTAPE = (short) 122;
    /** @deprecated use {@link ShapeType#FLOW_CHART_SUMMING_JUNCTION} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_FLOWCHARTSUMMINGJUNCTION = (short) 123;
    /** @deprecated use {@link ShapeType#FLOW_CHART_OR} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_FLOWCHARTOR = (short) 124;
    /** @deprecated use {@link ShapeType#FLOW_CHART_COLLATE} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_FLOWCHARTCOLLATE = (short) 125;
    /** @deprecated use {@link ShapeType#FLOW_CHART_SORT} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_FLOWCHARTSORT = (short) 126;
    /** @deprecated use {@link ShapeType#FLOW_CHART_EXTRACT} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_FLOWCHARTEXTRACT = (short) 127;
    /** @deprecated use {@link ShapeType#FLOW_CHART_MERGE} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_FLOWCHARTMERGE = (short) 128;
    /** @deprecated use {@link ShapeType#FLOW_CHART_OFFLINE_STORAGE} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_FLOWCHARTOFFLINESTORAGE = (short) 129;
    /** @deprecated use {@link ShapeType#FLOW_CHART_ONLINE_STORAGE} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_FLOWCHARTONLINESTORAGE = (short) 130;
    /** @deprecated use {@link ShapeType#FLOW_CHART_MAGNETIC_TAPE} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_FLOWCHARTMAGNETICTAPE = (short) 131;
    /** @deprecated use {@link ShapeType#FLOW_CHART_MAGNETIC_DISK} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_FLOWCHARTMAGNETICDISK = (short) 132;
    /** @deprecated use {@link ShapeType#FLOW_CHART_MAGNETIC_DRUM} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_FLOWCHARTMAGNETICDRUM = (short) 133;
    /** @deprecated use {@link ShapeType#FLOW_CHART_DISPLAY} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_FLOWCHARTDISPLAY = (short) 134;
    /** @deprecated use {@link ShapeType#FLOW_CHART_DELAY} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_FLOWCHARTDELAY = (short) 135;
    /** @deprecated use {@link ShapeType#TEXT_PLAIN_TEXT} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_TEXTPLAINTEXT = (short) 136;
    /** @deprecated use {@link ShapeType#TEXT_STOP} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_TEXTSTOP = (short) 137;
    /** @deprecated use {@link ShapeType#TEXT_TRIANGLE} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_TEXTTRIANGLE = (short) 138;
    /** @deprecated use {@link ShapeType#TEXT_TRIANGLE_INVERTED} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_TEXTTRIANGLEINVERTED = (short) 139;
    /** @deprecated use {@link ShapeType#TEXT_CHEVRON} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_TEXTCHEVRON = (short) 140;
    /** @deprecated use {@link ShapeType#TEXT_CHEVRON_INVERTED} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_TEXTCHEVRONINVERTED = (short) 141;
    /** @deprecated use {@link ShapeType#TEXT_RING_INSIDE} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_TEXTRINGINSIDE = (short) 142;
    /** @deprecated use {@link ShapeType#TEXT_RING_OUTSIDE} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_TEXTRINGOUTSIDE = (short) 143;
    /** @deprecated use {@link ShapeType#TEXT_ARCH_UP_CURVE} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_TEXTARCHUPCURVE = (short) 144;
    /** @deprecated use {@link ShapeType#TEXT_ARCH_DOWN_CURVE} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_TEXTARCHDOWNCURVE = (short) 145;
    /** @deprecated use {@link ShapeType#TEXT_CIRCLE_CURVE} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_TEXTCIRCLECURVE = (short) 146;
    /** @deprecated use {@link ShapeType#TEXT_BUTTON_CURVE} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_TEXTBUTTONCURVE = (short) 147;
    /** @deprecated use {@link ShapeType#TEXT_ARCH_UP_POUR} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_TEXTARCHUPPOUR = (short) 148;
    /** @deprecated use {@link ShapeType#TEXT_ARCH_DOWN_POUR} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_TEXTARCHDOWNPOUR = (short) 149;
    /** @deprecated use {@link ShapeType#TEXT_CIRCLE_POUR} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_TEXTCIRCLEPOUR = (short) 150;
    /** @deprecated use {@link ShapeType#TEXT_BUTTON_POUR} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_TEXTBUTTONPOUR = (short) 151;
    /** @deprecated use {@link ShapeType#TEXT_CURVE_UP} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_TEXTCURVEUP = (short) 152;
    /** @deprecated use {@link ShapeType#TEXT_CURVE_DOWN} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_TEXTCURVEDOWN = (short) 153;
    /** @deprecated use {@link ShapeType#TEXT_CASCADE_UP} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_TEXTCASCADEUP = (short) 154;
    /** @deprecated use {@link ShapeType#TEXT_CASCADE_DOWN} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_TEXTCASCADEDOWN = (short) 155;
    /** @deprecated use {@link ShapeType#TEXT_WAVE_1} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_TEXTWAVE1 = (short) 156;
    /** @deprecated use {@link ShapeType#TEXT_WAVE_2} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_TEXTWAVE2 = (short) 157;
    /** @deprecated use {@link ShapeType#TEXT_WAVE_3} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_TEXTWAVE3 = (short) 158;
    /** @deprecated use {@link ShapeType#TEXT_WAVE_4} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_TEXTWAVE4 = (short) 159;
    /** @deprecated use {@link ShapeType#TEXT_INFLATE} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_TEXTINFLATE = (short) 160;
    /** @deprecated use {@link ShapeType#TEXT_DEFLATE} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_TEXTDEFLATE = (short) 161;
    /** @deprecated use {@link ShapeType#TEXT_INFLATE_BOTTOM} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_TEXTINFLATEBOTTOM = (short) 162;
    /** @deprecated use {@link ShapeType#TEXT_DEFLATE_BOTTOM} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_TEXTDEFLATEBOTTOM = (short) 163;
    /** @deprecated use {@link ShapeType#TEXT_INFLATE_TOP} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_TEXTINFLATETOP = (short) 164;
    /** @deprecated use {@link ShapeType#TEXT_DEFLATE_TOP} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_TEXTDEFLATETOP = (short) 165;
    /** @deprecated use {@link ShapeType#TEXT_DEFLATE_INFLATE} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_TEXTDEFLATEINFLATE = (short) 166;
    /** @deprecated use {@link ShapeType#TEXT_DEFLATE_INFLATE_DEFLATE} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_TEXTDEFLATEINFLATEDEFLATE = (short) 167;
    /** @deprecated use {@link ShapeType#TEXT_FADE_RIGHT} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_TEXTFADERIGHT = (short) 168;
    /** @deprecated use {@link ShapeType#TEXT_FADE_LEFT} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_TEXTFADELEFT = (short) 169;
    /** @deprecated use {@link ShapeType#TEXT_FADE_UP} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_TEXTFADEUP = (short) 170;
    /** @deprecated use {@link ShapeType#TEXT_FADE_DOWN} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_TEXTFADEDOWN = (short) 171;
    /** @deprecated use {@link ShapeType#TEXT_SLANT_UP} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_TEXTSLANTUP = (short) 172;
    /** @deprecated use {@link ShapeType#TEXT_SLANT_DOWN} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_TEXTSLANTDOWN = (short) 173;
    /** @deprecated use {@link ShapeType#TEXT_CAN_UP} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_TEXTCANUP = (short) 174;
    /** @deprecated use {@link ShapeType#TEXT_CAN_DOWN} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_TEXTCANDOWN = (short) 175;
    /** @deprecated use {@link ShapeType#FLOW_CHART_ALTERNATE_PROCESS} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_FLOWCHARTALTERNATEPROCESS = (short) 176;
    /** @deprecated use {@link ShapeType#FLOW_CHART_OFFPAGE_CONNECTOR} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_FLOWCHARTOFFPAGECONNECTOR = (short) 177;
    /** @deprecated use {@link ShapeType#CALLOUT_90} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_CALLOUT90 = (short) 178;
    /** @deprecated use {@link ShapeType#ACCENT_CALLOUT_90} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_ACCENTCALLOUT90 = (short) 179;
    /** @deprecated use {@link ShapeType#BORDER_CALLOUT_90} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_BORDERCALLOUT90 = (short) 180;
    /** @deprecated use {@link ShapeType#ACCENT_BORDER_CALLOUT_90} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_ACCENTBORDERCALLOUT90 = (short) 181;
    /** @deprecated use {@link ShapeType#LEFT_RIGHT_UP_ARROW} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_LEFTRIGHTUPARROW = (short) 182;
    /** @deprecated use {@link ShapeType#SUN} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_SUN = (short) 183;
    /** @deprecated use {@link ShapeType#MOON} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_MOON = (short) 184;
    /** @deprecated use {@link ShapeType#BRACKET_PAIR} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_BRACKETPAIR = (short) 185;
    /** @deprecated use {@link ShapeType#BRACE_PAIR} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_BRACEPAIR = (short) 186;
    /** @deprecated use {@link ShapeType#STAR_4} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_SEAL4 = (short) 187;
    /** @deprecated use {@link ShapeType#DOUBLE_WAVE} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_DOUBLEWAVE = (short) 188;
    /** @deprecated use {@link ShapeType#ACTION_BUTTON_BLANK} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_ACTIONBUTTONBLANK = (short) 189;
    /** @deprecated use {@link ShapeType#ACTION_BUTTON_HOME} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_ACTIONBUTTONHOME = (short) 190;
    /** @deprecated use {@link ShapeType#ACTION_BUTTON_HELP} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_ACTIONBUTTONHELP = (short) 191;
    /** @deprecated use {@link ShapeType#ACTION_BUTTON_INFORMATION} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_ACTIONBUTTONINFORMATION = (short) 192;
    /** @deprecated use {@link ShapeType#ACTION_BUTTON_FORWARD_NEXT} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_ACTIONBUTTONFORWARDNEXT = (short) 193;
    /** @deprecated use {@link ShapeType#ACTION_BUTTON_BACK_PREVIOUS} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_ACTIONBUTTONBACKPREVIOUS = (short) 194;
    /** @deprecated use {@link ShapeType#ACTION_BUTTON_END} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_ACTIONBUTTONEND = (short) 195;
    /** @deprecated use {@link ShapeType#ACTION_BUTTON_BEGINNING} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_ACTIONBUTTONBEGINNING = (short) 196;
    /** @deprecated use {@link ShapeType#ACTION_BUTTON_RETURN} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_ACTIONBUTTONRETURN = (short) 197;
    /** @deprecated use {@link ShapeType#ACTION_BUTTON_DOCUMENT} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_ACTIONBUTTONDOCUMENT = (short) 198;
    /** @deprecated use {@link ShapeType#ACTION_BUTTON_SOUND} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_ACTIONBUTTONSOUND = (short) 199;
    /** @deprecated use {@link ShapeType#ACTION_BUTTON_MOVIE} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_ACTIONBUTTONMOVIE = (short) 200;
    /** @deprecated use {@link ShapeType#HOST_CONTROL} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_HOSTCONTROL = (short) 201;
    /** @deprecated use {@link ShapeType#TEXT_BOX} */
    @Deprecated
    @Removal(version = "5.3")
    public static final short ST_TEXTBOX = (short) 202;
    /** @deprecated not used */
    @Deprecated
    @Removal(version = "5.3")
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

    public EscherAggregate(EscherAggregate other) {
        super(other);
        // shallow copy, because the aggregates doesn't own the records
        shapeToObj.putAll(other.shapeToObj);
        tailRec.putAll(other.tailRec);
    }

    /**
     * @return Returns the current sid.
     */
    @Override
    public short getSid() {
        return sid;
    }

    /**
     * Calculates the xml representation of this record.  This is
     * simply a dump of all the records.
     * @param tab - string which must be added before each line (used by default '\t')
     * @return xml representation of the all aggregated records
     */
    public String toXml(String tab) {
        return GenericRecordXmlWriter.marshal(this);
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
    public static EscherAggregate createAggregate(final List<RecordBase> records, final int locFirstDrawingRecord) {
        EscherAggregate agg = new EscherAggregate(false);

        ShapeCollector recordFactory = new ShapeCollector();
        List<Record> objectRecords = new ArrayList<>();

        int nextIdx = locFirstDrawingRecord;
        for (RecordBase rb : records.subList(locFirstDrawingRecord, records.size())) {
            nextIdx++;
            switch (sid(rb)) {
                case DrawingRecord.sid:
                    recordFactory.addBytes(((DrawingRecord)rb).getRecordData());
                    continue;
                case ContinueRecord.sid:
                    recordFactory.addBytes(((ContinueRecord)rb).getData());
                    continue;
                case ObjRecord.sid:
                case TextObjectRecord.sid:
                    objectRecords.add((org.apache.poi.hssf.record.Record)rb);
                    continue;
                case NoteRecord.sid:
                    // any NoteRecords that follow the drawing block must be aggregated and saved in the tailRec collection
                    NoteRecord r = (NoteRecord)rb;
                    agg.tailRec.put(r.getShapeId(), r);
                    continue;
                default:
                    nextIdx--;
                    break;
            }
            break;
        }

        // replace drawing block with the created EscherAggregate
        records.set(locFirstDrawingRecord, agg);
        if (locFirstDrawingRecord+1 <= nextIdx) {
            records.subList(locFirstDrawingRecord + 1, nextIdx).clear();
        }

        // Decode the shapes
        Iterator<EscherRecord> shapeIter = recordFactory.parse(agg).iterator();

        // Associate the object records with the shapes
        objectRecords.forEach(or -> agg.shapeToObj.put(shapeIter.next(), or));

        return agg;
    }

    private static class ShapeCollector extends DefaultEscherRecordFactory {
        final List<EscherRecord> objShapes = new ArrayList<>();
        final UnsynchronizedByteArrayOutputStream buffer = new UnsynchronizedByteArrayOutputStream();

        void addBytes(byte[] data) {
            try {
                buffer.write(data);
            } catch (IOException e) {
                throw new RuntimeException("Couldn't get data from drawing/continue records", e);
            }
        }

        @Override
        public EscherRecord createRecord(byte[] data, int offset) {
            EscherRecord r = super.createRecord(data, offset);
            short rid = r.getRecordId();
            if (rid == EscherClientDataRecord.RECORD_ID || rid == EscherTextboxRecord.RECORD_ID) {
                objShapes.add(r);
            }
            return r;
        }

        List<EscherRecord> parse(EscherAggregate agg) {
            byte[] buf = buffer.toByteArray();
            int pos = 0;
            while (pos < buf.length) {
                EscherRecord r = createRecord(buf, pos);
                pos += r.fillFields(buf, pos, this);
                agg.addEscherRecord(r);
            }
            return objShapes;
        }
    }

    /**
     * Serializes this aggregate to a byte array.  Since this is an aggregate
     * record it will effectively serialize the aggregated records.
     *
     * @param offset The offset into the start of the array.
     * @param data   The byte array to serialize to.
     * @return The number of bytes serialized.
     */
    @Override
    public int serialize(final int offset, final byte[] data) {
        // Determine buffer size
        List <EscherRecord>records = getEscherRecords();
        int size = getEscherRecordSize(records);
        byte[] buffer = new byte[size];

        // Serialize escher records into one big data structure and keep note of ending offsets.
        final List <Integer>spEndingOffsets = new ArrayList<>();
        final List <EscherRecord> shapes = new ArrayList<>();
        int pos = 0;
        for (EscherRecord record : records) {
            pos += record.serialize(pos, buffer, new EscherSerializationListener() {
                @Override
                public void beforeRecordSerialize(int offset, short recordId, EscherRecord record) {
                }

                @Override
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
        boolean isFirst = true;
        int endOffset = 0;
        for (int i = 1; i < shapes.size(); i++) {
            int startOffset = endOffset;
            endOffset = spEndingOffsets.get(i);

            byte[] drawingData = Arrays.copyOfRange(buffer, startOffset, endOffset);
            pos += writeDataIntoDrawingRecord(drawingData, writtenEscherBytes, pos, data, isFirst);

            writtenEscherBytes += drawingData.length;

            // Write the matching OBJ record
            Record obj = shapeToObj.get(shapes.get(i));
            pos += obj.serialize(pos, data);

            isFirst = false;
        }

        if (endOffset < buffer.length - 1) {
            byte[] drawingData = Arrays.copyOfRange(buffer, endOffset, buffer.length);
            pos += writeDataIntoDrawingRecord(drawingData, writtenEscherBytes, pos, data, isFirst);
        }

        for (NoteRecord noteRecord : tailRec.values()) {
            pos += noteRecord.serialize(pos, data);
        }

        int bytesWritten = pos - offset;
        if (bytesWritten != getRecordSize()) {
            throw new RecordFormatException(bytesWritten + " bytes written but getRecordSize() reports " + getRecordSize());
        }
        return bytesWritten;
    }

    /**
     * @param drawingData - escher records saved into single byte array
     * @param writtenEscherBytes - count of bytes already saved into drawing records (we should know it to decide create
     *                           drawing or continue record)
     * @param pos current position of data array
     * @param data - array of bytes where drawing records must be serialized
     * @param isFirst - is it the first shape, saved into data array
     * @return offset of data array after serialization
     */
    private int writeDataIntoDrawingRecord(final byte[] drawingData, final int writtenEscherBytes, final int pos, final byte[] data, final boolean isFirst) {
        int temp = 0;
        //First record in drawing layer MUST be DrawingRecord
        boolean useDrawingRecord = isFirst || (writtenEscherBytes + drawingData.length) <= MAX_RECORD_DATA_SIZE;

        for (int j = 0; j < drawingData.length; j += MAX_RECORD_DATA_SIZE) {
            byte[] buf = Arrays.copyOfRange(drawingData, j, Math.min(j+MAX_RECORD_DATA_SIZE, drawingData.length));
            Record drawing = (useDrawingRecord) ? new DrawingRecord(buf) : new ContinueRecord(buf);
            temp += drawing.serialize(pos + temp, data);
            useDrawingRecord = false;
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
    @Override
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
                @Override
                public void beforeRecordSerialize(int offset, short recordId, EscherRecord record) {
                }

                @Override
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
            if (spEndingOffsets.get(i) - spEndingOffsets.get(i - 1) <= MAX_RECORD_DATA_SIZE) {
                continue;
            }
            continueRecordsHeadersSize += ((spEndingOffsets.get(i) - spEndingOffsets.get(i - 1)) / MAX_RECORD_DATA_SIZE) * 4;
        }

        int drawingRecordSize = rawEscherSize + (shapeToObj.size()) * 4;
        if (rawEscherSize != 0 && spEndingOffsets.size() == 1) {
            // EMPTY
            continueRecordsHeadersSize += 4;
        }
        int objRecordSize = 0;
        for (org.apache.poi.hssf.record.Record r : shapeToObj.values()) {
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
    @Override
    protected String getRecordName() {
        return "ESCHERAGGREGATE";
    }

    // =============== Private methods ========================

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
        if (dg != null) {
            dg.setOptions((short) (dgId << 4));
        }
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
        EscherContainerRecord spgrContainer = dgContainer.getChildById(EscherContainerRecord.SPGR_CONTAINER);
        if (spgrContainer != null) {
            EscherContainerRecord spContainer = (EscherContainerRecord) spgrContainer.getChild(0);
            EscherSpRecord sp = spContainer.getChildById(EscherSpRecord.RECORD_ID);
            if (sp != null) {
                sp.setShapeId(shapeId);
            }
        }
    }

    /**
     * @param record the record to look into
     * @return sid of the record
     */
    private static short sid(RecordBase record) {
        // Aggregates don't have a sid
        // We could step into them, but for these needs we don't care
        return (record instanceof org.apache.poi.hssf.record.Record)
            ? ((org.apache.poi.hssf.record.Record)record).getSid()
            : -1;
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

    @Override
    public EscherAggregate copy() {
        return new EscherAggregate(this);
    }

    @Override
    public HSSFRecordTypes getGenericRecordType() {
        return HSSFRecordTypes.ESCHER_AGGREGATE;
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return null;
    }
}

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

package org.apache.poi.hslf.record;

import java.util.HashMap;
import java.util.Map;

/**
 * List of all known record types in a PowerPoint document, and the
 *  classes that handle them.
 * There are two categories of records:
 * <li> PowerPoint records: 0 <= info <= 10002 (will carry class info)
 * <li> Escher records: info >= 0xF000 (handled by DDF, so no class info)
 */
public enum RecordTypes {
    Unknown(0,null),
    UnknownRecordPlaceholder(-1, UnknownRecordPlaceholder.class),
    Document(1000,Document.class),
    DocumentAtom(1001,DocumentAtom.class),
    EndDocument(1002,null),
    Slide(1006,Slide.class),
    SlideAtom(1007,SlideAtom.class),
    Notes(1008,Notes.class),
    NotesAtom(1009,NotesAtom.class),
    Environment(1010,Environment.class),
    SlidePersistAtom(1011,SlidePersistAtom.class),
    SSlideLayoutAtom(1015,null),
    MainMaster(1016,MainMaster.class),
    SSSlideInfoAtom(1017,SSSlideInfoAtom.class),
    SlideViewInfo(1018,null),
    GuideAtom(1019,null),
    ViewInfo(1020,null),
    ViewInfoAtom(1021,null),
    SlideViewInfoAtom(1022,null),
    VBAInfo(1023,VBAInfoContainer.class),
    VBAInfoAtom(1024,VBAInfoAtom.class),
    SSDocInfoAtom(1025,null),
    Summary(1026,null),
    DocRoutingSlip(1030,null),
    OutlineViewInfo(1031,null),
    SorterViewInfo(1032,null),
    ExObjList(1033,ExObjList.class),
    ExObjListAtom(1034,ExObjListAtom.class),
    PPDrawingGroup(1035,PPDrawingGroup.class),
    PPDrawing(1036,PPDrawing.class),
    NamedShows(1040,null),
    NamedShow(1041,null),
    NamedShowSlides(1042,null),
    SheetProperties(1044,null),
    RoundTripCustomTableStyles12Atom(1064,null),
    List(2000,DocInfoListContainer.class),
    FontCollection(2005,FontCollection.class),
    BookmarkCollection(2019,null),
    SoundCollection(2020,SoundCollection.class),
    SoundCollAtom(2021,null),
    Sound(2022,Sound.class),
    SoundData(2023,SoundData.class),
    BookmarkSeedAtom(2025,null),
    ColorSchemeAtom(2032,ColorSchemeAtom.class),
    ExObjRefAtom(3009,ExObjRefAtom.class),
    OEPlaceholderAtom(3011,OEPlaceholderAtom.class),
    GPopublicintAtom(3024,null),
    GRatioAtom(3031,null),
    OutlineTextRefAtom(3998,OutlineTextRefAtom.class),
    TextHeaderAtom(3999,TextHeaderAtom.class),
    TextCharsAtom(4000,TextCharsAtom.class),
    StyleTextPropAtom(4001, StyleTextPropAtom.class),//0x0fa1 RT_StyleTextPropAtom
    MasterTextPropAtom(4002, MasterTextPropAtom.class),
    TxMasterStyleAtom(4003,TxMasterStyleAtom.class),
    TxCFStyleAtom(4004,null),
    TxPFStyleAtom(4005,null),
    TextRulerAtom(4006,TextRulerAtom.class),
    TextBookmarkAtom(4007,null),
    TextBytesAtom(4008,TextBytesAtom.class),
    TxSIStyleAtom(4009,null),
    TextSpecInfoAtom(4010, TextSpecInfoAtom.class),
    DefaultRulerAtom(4011,null),
    StyleTextProp9Atom(4012, StyleTextProp9Atom.class), //0x0FAC RT_StyleTextProp9Atom
    FontEntityAtom(4023,FontEntityAtom.class),
    FontEmbeddedData(4024,null),
    CString(4026,CString.class),
    MetaFile(4033,null),
    ExOleObjAtom(4035,ExOleObjAtom.class),
    SrKinsoku(4040,null),
    HandOut(4041,DummyPositionSensitiveRecordWithChildren.class),
    ExEmbed(4044,ExEmbed.class),
    ExEmbedAtom(4045,ExEmbedAtom.class),
    ExLink(4046,null),
    BookmarkEntityAtom(4048,null),
    ExLinkAtom(4049,null),
    SrKinsokuAtom(4050,null),
    ExHyperlinkAtom(4051,ExHyperlinkAtom.class),
    ExHyperlink(4055,ExHyperlink.class),
    SlideNumberMCAtom(4056,null),
    HeadersFooters(4057,HeadersFootersContainer.class),
    HeadersFootersAtom(4058,HeadersFootersAtom.class),
    TxInteractiveInfoAtom(4063,TxInteractiveInfoAtom.class),
    CharFormatAtom(4066,null),
    ParaFormatAtom(4067,null),
    RecolorInfoAtom(4071,null),
    ExQuickTimeMovie(4074,null),
    ExQuickTimeMovieData(4075,null),
    ExControl(4078,ExControl.class),
    SlideListWithText(4080,SlideListWithText.class),
    InteractiveInfo(4082,InteractiveInfo.class),
    InteractiveInfoAtom(4083,InteractiveInfoAtom.class),
    UserEditAtom(4085,UserEditAtom.class),
    CurrentUserAtom(4086,null),
    DateTimeMCAtom(4087,null),
    GenericDateMCAtom(4088,null),
    FooterMCAtom(4090,null),
    ExControlAtom(4091,ExControlAtom.class),
    ExMediaAtom(4100,ExMediaAtom.class),
    ExVideoContainer(4101,ExVideoContainer.class),
    ExAviMovie(4102,ExAviMovie.class),
    ExMCIMovie(4103,ExMCIMovie.class),
    ExMIDIAudio(4109,null),
    ExCDAudio(4110,null),
    ExWAVAudioEmbedded(4111,null),
    ExWAVAudioLink(4112,null),
    ExOleObjStg(4113,ExOleObjStg.class),
    ExCDAudioAtom(4114,null),
    ExWAVAudioEmbeddedAtom(4115,null),
    AnimationInfo(4116,AnimationInfo.class),
    AnimationInfoAtom(4081,AnimationInfoAtom.class),
    RTFDateTimeMCAtom(4117,null),
    ProgTags(5000,DummyPositionSensitiveRecordWithChildren.class),
    ProgStringTag(5001,null),
    ProgBinaryTag(5002,DummyPositionSensitiveRecordWithChildren.class),
    BinaryTagData(5003, BinaryTagDataBlob.class),//0x138b RT_BinaryTagDataBlob
    PrpublicintOptions(6000,null),
    PersistPtrFullBlock(6001,PersistPtrHolder.class),
    PersistPtrIncrementalBlock(6002,PersistPtrHolder.class),
    GScalingAtom(10001,null),
    GRColorAtom(10002,null),

    // Records ~12000 seem to be related to the Comments used in PPT 2000/XP
    // (Comments in PPT97 are normal Escher text boxes)
    Comment2000(12000,Comment2000.class),
    Comment2000Atom(12001,Comment2000Atom.class),
    Comment2000Summary(12004,null),
    Comment2000SummaryAtom(12005,null),

    // Records ~12050 seem to be related to Document Encryption
    DocumentEncryptionAtom(12052,DocumentEncryptionAtom.class),

    OriginalMainMasterId(1052,null),
    CompositeMasterId(1052,null),
    RoundTripContentMasterInfo12(1054,null),
    RoundTripShapeId12(1055,null),
    RoundTripHFPlaceholder12(1056,RoundTripHFPlaceholder12.class),
    RoundTripContentMasterId(1058,null),
    RoundTripOArtTextStyles12(1059,null),
    RoundTripShapeCheckSumForCustomLayouts12(1062,null),
    RoundTripNotesMasterTextStyles12(1063,null),
    RoundTripCustomTableStyles12(1064,null),

    // records greater then 0xF000 belong to with Microsoft Office Drawing format also known as Escher
    EscherDggContainer(0xF000,null),
    EscherDgg(0xf006,null),
    EscherCLSID(0xf016,null),
    EscherOPT(0xf00b,null),
    EscherBStoreContainer(0xf001,null),
    EscherBSE(0xf007,null),
    EscherBlip_START(0xf018,null),
    EscherBlip_END(0xf117,null),
    EscherDgContainer(0xf002,null),
    EscherDg(0xf008,null),
    EscherRegroupItems(0xf118,null),
    EscherColorScheme(0xf120,null),
    EscherSpgrContainer(0xf003,null),
    EscherSpContainer(0xf004,null),
    EscherSpgr(0xf009,null),
    EscherSp(0xf00a,null),
    EscherTextbox(0xf00c,null),
    EscherClientTextbox(0xf00d,null),
    EscherAnchor(0xf00e,null),
    EscherChildAnchor(0xf00f,null),
    EscherClientAnchor(0xf010,null),
    EscherClientData(0xf011,null),
    EscherSolverContainer(0xf005,null),
    EscherConnectorRule(0xf012,null),
    EscherAlignRule(0xf013,null),
    EscherArcRule(0xf014,null),
    EscherClientRule(0xf015,null),
    EscherCalloutRule(0xf017,null),
    EscherSelection(0xf119,null),
    EscherColorMRU(0xf11a,null),
    EscherDeletedPspl(0xf11d,null),
    EscherSplitMenuColors(0xf11e,null),
    EscherOleObject(0xf11f,null),
    EscherUserDefined(0xf122,null);

    private static final Map<Short,RecordTypes> LOOKUP;

    static {
        LOOKUP = new HashMap<Short,RecordTypes>();
        for(RecordTypes s : values()) {
            LOOKUP.put(s.typeID, s);
        }
    }    
    
    public final short typeID;
    public final Class<? extends Record> handlingClass;

    private RecordTypes(int typeID, Class<? extends Record> handlingClass) {
        this.typeID = (short)typeID;
        this.handlingClass = handlingClass;
    }

    public static RecordTypes forTypeID(int typeID) {
        RecordTypes rt = LOOKUP.get((short)typeID);
        return (rt != null) ? rt : UnknownRecordPlaceholder;
    }



    /**
     * Returns name of the record by its type
     *
     * @param type section of the record header
     * @return name of the record
     */
//    public static String recordName(int type) {
//        String name = typeToName.get(Integer.valueOf(type));
//        return (name == null) ? ("Unknown" + type) : name;
//    }

    /**
     * Returns the class handling a record by its type.
	 * If given an un-handled PowerPoint record, will return a dummy
	 *  placeholder class. If given an unknown PowerPoint record, or
	 *  and Escher record, will return null.
     *
     * @param type section of the record header
     * @return class to handle the record, or null if an unknown (eg Escher) record
     */
//	public static Class<? extends Record> recordHandlingClass(int type) {
//		Class<? extends Record> c = typeToClass.get(Integer.valueOf(type));
//		return c;
//	}
//
//    static {
//		typeToName = new HashMap<Integer,String>();
//		typeToClass = new HashMap<Integer,Class<? extends Record>>();
//        try {
//            Field[] f = RecordTypes.class.getFields();
//            for (int i = 0; i < f.length; i++){
//               Object val = f[i].get(null);
//
//               // Escher record, only store ID -> Name
//               if (val instanceof Integer) {
//                  typeToName.put((Integer)val, f[i].getName());
//               }
//               // PowerPoint record, store ID -> Name and ID -> Class
//               if (val instanceof Type) {
//                  Type t = (Type)val;
//                  Class<? extends Record> c = t.handlingClass;
//                  Integer id = Integer.valueOf(t.typeID);
//                  if(c == null) { c = UnknownRecordPlaceholder.class; }
//
//                  typeToName.put(id, f[i].getName());
//                  typeToClass.put(id, c);
//               }
//            }
//        } catch (IllegalAccessException e){
//            throw new HSLFException("Failed to initialize records types");
//        }
//    }


	/**
	 * Wrapper for the details of a PowerPoint or Escher record type.
	 * Contains both the type, and the handling class (if any), and
	 *  offers methods to get either back out.
	 */
//	public static class Type {
//		public final int typeID;
//		public final Class<? extends Record> handlingClass;
//		public Type(int typeID, Class<? extends Record> handlingClass) {
//			this.typeID = typeID;
//			this.handlingClass = handlingClass;
//		}
//	}
}

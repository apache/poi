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
import java.lang.reflect.Field;

/**
 * List of all known record types in a PowerPoint document, and the
 *  classes that handle them.
 * There are two categories of records:
 * <li> PowerPoint records: 0 <= info <= 10002 (will carry class info)
 * <li> Escher records: info >= 0xF000 (handled by DDF, so no class info)
 *
 * @author Yegor Kozlov
 * @author Nick Burch
 */
public final class RecordTypes {
    public static HashMap typeToName;
    public static HashMap typeToClass;

    public static final Type Unknown = new Type(0,null);
    public static final Type Document = new Type(1000,Document.class);
    public static final Type DocumentAtom = new Type(1001,DocumentAtom.class);
    public static final Type EndDocument = new Type(1002,null);
    public static final Type Slide = new Type(1006,Slide.class);
    public static final Type SlideAtom = new Type(1007,SlideAtom.class);
    public static final Type Notes = new Type(1008,Notes.class);
    public static final Type NotesAtom = new Type(1009,NotesAtom.class);
    public static final Type Environment = new Type(1010,Environment.class);
    public static final Type SlidePersistAtom = new Type(1011,SlidePersistAtom.class);
    public static final Type SSlideLayoutAtom = new Type(1015,null);
    public static final Type MainMaster = new Type(1016,MainMaster.class);
    public static final Type SSSlideInfoAtom = new Type(1017,null);
    public static final Type SlideViewInfo = new Type(1018,null);
    public static final Type GuideAtom = new Type(1019,null);
    public static final Type ViewInfo = new Type(1020,null);
    public static final Type ViewInfoAtom = new Type(1021,null);
    public static final Type SlideViewInfoAtom = new Type(1022,null);
    public static final Type VBAInfo = new Type(1023,null);
    public static final Type VBAInfoAtom = new Type(1024,null);
    public static final Type SSDocInfoAtom = new Type(1025,null);
    public static final Type Summary = new Type(1026,null);
    public static final Type DocRoutingSlip = new Type(1030,null);
    public static final Type OutlineViewInfo = new Type(1031,null);
    public static final Type SorterViewInfo = new Type(1032,null);
    public static final Type ExObjList = new Type(1033,ExObjList.class);
    public static final Type ExObjListAtom = new Type(1034,ExObjListAtom.class);
    public static final Type PPDrawingGroup = new Type(1035,PPDrawingGroup.class);
    public static final Type PPDrawing = new Type(1036,PPDrawing.class);
    public static final Type NamedShows = new Type(1040,null);
    public static final Type NamedShow = new Type(1041,null);
    public static final Type NamedShowSlides = new Type(1042,null);
    public static final Type SheetProperties = new Type(1044,null);
    public static final Type List = new Type(2000,null);
    public static final Type FontCollection = new Type(2005,FontCollection.class);
    public static final Type BookmarkCollection = new Type(2019,null);
    public static final Type SoundCollection = new Type(2020,SoundCollection.class);
    public static final Type SoundCollAtom = new Type(2021,null);
    public static final Type Sound = new Type(2022,Sound.class);
    public static final Type SoundData = new Type(2023,SoundData.class);
    public static final Type BookmarkSeedAtom = new Type(2025,null);
    public static final Type ColorSchemeAtom = new Type(2032,ColorSchemeAtom.class);
    public static final Type ExObjRefAtom = new Type(3009,null);
    public static final Type OEShapeAtom = new Type(3009,OEShapeAtom.class);
    public static final Type OEPlaceholderAtom = new Type(3011,OEPlaceholderAtom.class);
    public static final Type GPopublicintAtom = new Type(3024,null);
    public static final Type GRatioAtom = new Type(3031,null);
    public static final Type OutlineTextRefAtom = new Type(3998,OutlineTextRefAtom.class);
    public static final Type TextHeaderAtom = new Type(3999,TextHeaderAtom.class);
    public static final Type TextCharsAtom = new Type(4000,TextCharsAtom.class);
    public static final Type StyleTextPropAtom = new Type(4001,StyleTextPropAtom.class);
    public static final Type BaseTextPropAtom = new Type(4002,null);
    public static final Type TxMasterStyleAtom = new Type(4003,TxMasterStyleAtom.class);
    public static final Type TxCFStyleAtom = new Type(4004,null);
    public static final Type TxPFStyleAtom = new Type(4005,null);
    public static final Type TextRulerAtom = new Type(4006,TextRulerAtom.class);
    public static final Type TextBookmarkAtom = new Type(4007,null);
    public static final Type TextBytesAtom = new Type(4008,TextBytesAtom.class);
    public static final Type TxSIStyleAtom = new Type(4009,null);
    public static final Type TextSpecInfoAtom = new Type(4010, TextSpecInfoAtom.class);
    public static final Type DefaultRulerAtom = new Type(4011,null);
    public static final Type FontEntityAtom = new Type(4023,FontEntityAtom.class);
    public static final Type FontEmbeddedData = new Type(4024,null);
    public static final Type CString = new Type(4026,CString.class);
    public static final Type MetaFile = new Type(4033,null);
    public static final Type ExOleObjAtom = new Type(4035,ExOleObjAtom.class);
    public static final Type SrKinsoku = new Type(4040,null);
    public static final Type HandOut = new Type(4041,DummyPositionSensitiveRecordWithChildren.class);
    public static final Type ExEmbed = new Type(4044,ExEmbed.class);
    public static final Type ExEmbedAtom = new Type(4045,ExEmbedAtom.class);
    public static final Type ExLink = new Type(4046,null);
    public static final Type BookmarkEntityAtom = new Type(4048,null);
    public static final Type ExLinkAtom = new Type(4049,null);
    public static final Type SrKinsokuAtom = new Type(4050,null);
    public static final Type ExHyperlinkAtom = new Type(4051,ExHyperlinkAtom.class);
    public static final Type ExHyperlink = new Type(4055,ExHyperlink.class);
    public static final Type SlideNumberMCAtom = new Type(4056,null);
    public static final Type HeadersFooters = new Type(4057,HeadersFootersContainer.class);
    public static final Type HeadersFootersAtom = new Type(4058,HeadersFootersAtom.class);
    public static final Type TxInteractiveInfoAtom = new Type(4063,TxInteractiveInfoAtom.class);
    public static final Type CharFormatAtom = new Type(4066,null);
    public static final Type ParaFormatAtom = new Type(4067,null);
    public static final Type RecolorInfoAtom = new Type(4071,null);
    public static final Type ExQuickTimeMovie = new Type(4074,null);
    public static final Type ExQuickTimeMovieData = new Type(4075,null);
    public static final Type ExControl = new Type(4078,ExControl.class);
    public static final Type SlideListWithText = new Type(4080,SlideListWithText.class);
    public static final Type InteractiveInfo = new Type(4082,InteractiveInfo.class);
    public static final Type InteractiveInfoAtom = new Type(4083,InteractiveInfoAtom.class);
    public static final Type UserEditAtom = new Type(4085,UserEditAtom.class);
    public static final Type CurrentUserAtom = new Type(4086,null);
    public static final Type DateTimeMCAtom = new Type(4087,null);
    public static final Type GenericDateMCAtom = new Type(4088,null);
    public static final Type FooterMCAtom = new Type(4090,null);
    public static final Type ExControlAtom = new Type(4091,ExControlAtom.class);
    public static final Type ExMediaAtom = new Type(4100,ExMediaAtom.class);
    public static final Type ExVideoContainer = new Type(4101,ExVideoContainer.class);
    public static final Type ExAviMovie = new Type(4102,ExAviMovie.class);
    public static final Type ExMCIMovie = new Type(4103,ExMCIMovie.class);
    public static final Type ExMIDIAudio = new Type(4109,null);
    public static final Type ExCDAudio = new Type(4110,null);
    public static final Type ExWAVAudioEmbedded = new Type(4111,null);
    public static final Type ExWAVAudioLink = new Type(4112,null);
    public static final Type ExOleObjStg = new Type(4113,ExOleObjStg.class);
    public static final Type ExCDAudioAtom = new Type(4114,null);
    public static final Type ExWAVAudioEmbeddedAtom = new Type(4115,null);
    public static final Type AnimationInfo = new Type(4116,AnimationInfo.class);
    public static final Type AnimationInfoAtom = new Type(4081,AnimationInfoAtom.class);
    public static final Type RTFDateTimeMCAtom = new Type(4117,null);
    public static final Type ProgTags = new Type(5000,DummyPositionSensitiveRecordWithChildren.class);
    public static final Type ProgStringTag = new Type(5001,null);
    public static final Type ProgBinaryTag = new Type(5002,DummyPositionSensitiveRecordWithChildren.class);
    public static final Type BinaryTagData = new Type(5003,DummyPositionSensitiveRecordWithChildren.class);
    public static final Type PrpublicintOptions = new Type(6000,null);
    public static final Type PersistPtrFullBlock = new Type(6001,PersistPtrHolder.class);
    public static final Type PersistPtrIncrementalBlock = new Type(6002,PersistPtrHolder.class);
    public static final Type GScalingAtom = new Type(10001,null);
    public static final Type GRColorAtom = new Type(10002,null);

    // Records ~12000 seem to be related to the Comments used in PPT 2000/XP
    // (Comments in PPT97 are normal Escher text boxes)
    public static final Type Comment2000 = new Type(12000,Comment2000.class);
    public static final Type Comment2000Atom = new Type(12001,Comment2000Atom.class);
    public static final Type Comment2000Summary = new Type(12004,null);
    public static final Type Comment2000SummaryAtom = new Type(12005,null);

    // Records ~12050 seem to be related to Document Encryption
    public static final Type DocumentEncryptionAtom = new Type(12052,DocumentEncryptionAtom.class);

    public static final Type OriginalMainMasterId = new Type(1052,null);
    public static final Type CompositeMasterId = new Type(1052,null);
    public static final Type RoundTripContentMasterInfo12 = new Type(1054,null);
    public static final Type RoundTripShapeId12 = new Type(1055,null);
    public static final Type RoundTripHFPlaceholder12 = new Type(1056,RoundTripHFPlaceholder12.class);
    public static final Type RoundTripContentMasterId = new Type(1058,null);
    public static final Type RoundTripOArtTextStyles12 = new Type(1059,null);
    public static final Type RoundTripShapeCheckSumForCustomLayouts12 = new Type(1062,null);
    public static final Type RoundTripNotesMasterTextStyles12 = new Type(1063,null);
    public static final Type RoundTripCustomTableStyles12 = new Type(1064,null);

    //records greater then 0xF000 belong to with Microsoft Office Drawing format also known as Escher
    public static final int EscherDggContainer = 0xf000;
    public static final int EscherDgg = 0xf006;
    public static final int EscherCLSID = 0xf016;
    public static final int EscherOPT = 0xf00b;
    public static final int EscherBStoreContainer = 0xf001;
    public static final int EscherBSE = 0xf007;
    public static final int EscherBlip_START = 0xf018;
    public static final int EscherBlip_END = 0xf117;
    public static final int EscherDgContainer = 0xf002;
    public static final int EscherDg = 0xf008;
    public static final int EscherRegroupItems = 0xf118;
    public static final int EscherColorScheme = 0xf120;
    public static final int EscherSpgrContainer = 0xf003;
    public static final int EscherSpContainer = 0xf004;
    public static final int EscherSpgr = 0xf009;
    public static final int EscherSp = 0xf00a;
    public static final int EscherTextbox = 0xf00c;
    public static final int EscherClientTextbox = 0xf00d;
    public static final int EscherAnchor = 0xf00e;
    public static final int EscherChildAnchor = 0xf00f;
    public static final int EscherClientAnchor = 0xf010;
    public static final int EscherClientData = 0xf011;
    public static final int EscherSolverContainer = 0xf005;
    public static final int EscherConnectorRule = 0xf012;
    public static final int EscherAlignRule = 0xf013;
    public static final int EscherArcRule = 0xf014;
    public static final int EscherClientRule = 0xf015;
    public static final int EscherCalloutRule = 0xf017;
    public static final int EscherSelection = 0xf119;
    public static final int EscherColorMRU = 0xf11a;
    public static final int EscherDeletedPspl = 0xf11d;
    public static final int EscherSplitMenuColors = 0xf11e;
    public static final int EscherOleObject = 0xf11f;
    public static final int EscherUserDefined = 0xf122;

    /**
     * Returns name of the record by its type
     *
     * @param type section of the record header
     * @return name of the record
     */
    public static String recordName(int type) {
        String name = (String)typeToName.get(Integer.valueOf(type));
        if (name == null) name = "Unknown" + type;
        return name;
    }

    /**
     * Returns the class handling a record by its type.
	 * If given an un-handled PowerPoint record, will return a dummy
	 *  placeholder class. If given an unknown PowerPoint record, or
	 *  and Escher record, will return null.
     *
     * @param type section of the record header
     * @return class to handle the record, or null if an unknown (eg Escher) record
     */
	public static Class recordHandlingClass(int type) {
		Class c = (Class)typeToClass.get(Integer.valueOf(type));
		return c;
	}

    static {
		typeToName = new HashMap();
		typeToClass = new HashMap();
        try {
            Field[] f = RecordTypes.class.getFields();
            for (int i = 0; i < f.length; i++){
                Object val = f[i].get(null);

				// Escher record, only store ID -> Name
                if (val instanceof Integer) {
                    typeToName.put(val, f[i].getName());
                }
				// PowerPoint record, store ID -> Name and ID -> Class
				if (val instanceof Type) {
					Type t = (Type)val;
					Class c = t.handlingClass;
					Integer id = Integer.valueOf(t.typeID);
					if(c == null) { c = UnknownRecordPlaceholder.class; }

                    typeToName.put(id, f[i].getName());
                    typeToClass.put(id, c);
				}
            }
        } catch (IllegalAccessException e){
            throw new RuntimeException("Failed to initialize records types");
        }
    }


	/**
	 * Wrapper for the details of a PowerPoint or Escher record type.
	 * Contains both the type, and the handling class (if any), and
	 *  offers methods to get either back out.
	 */
	public static class Type {
		public int typeID;
		public Class handlingClass;
		public Type(int typeID, Class handlingClass) {
			this.typeID = typeID;
			this.handlingClass = handlingClass;
		}
	}
}

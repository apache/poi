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

package org.apache.poi.hssf.record;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.poi.hssf.record.chart.*;
import org.apache.poi.hssf.record.pivottable.DataItemRecord;
import org.apache.poi.hssf.record.pivottable.ExtendedPivotTableViewFieldsRecord;
import org.apache.poi.hssf.record.pivottable.PageItemRecord;
import org.apache.poi.hssf.record.pivottable.StreamIDRecord;
import org.apache.poi.hssf.record.pivottable.ViewDefinitionRecord;
import org.apache.poi.hssf.record.pivottable.ViewFieldsRecord;
import org.apache.poi.hssf.record.pivottable.ViewSourceRecord;

public enum HSSFRecordTypes {
    UNKNOWN(-1, UnknownRecord.class, UnknownRecord::new, false),
    FORMULA(0x0006, FormulaRecord.class, FormulaRecord::new),
    EOF(0x000A, EOFRecord.class, EOFRecord::new),
    CALC_COUNT(0x000C, CalcCountRecord.class, CalcCountRecord::new),
    CALC_MODE(0x000D, CalcModeRecord.class, CalcModeRecord::new),
    PRECISION(0x000E, PrecisionRecord.class, PrecisionRecord::new),
    REF_MODE(0x000F, RefModeRecord.class, RefModeRecord::new),
    DELTA(0x0010, DeltaRecord.class, DeltaRecord::new),
    ITERATION(0x0011, IterationRecord.class, IterationRecord::new),
    PROTECT(0x0012, ProtectRecord.class, ProtectRecord::new),
    PASSWORD(0x0013, PasswordRecord.class, PasswordRecord::new),
    HEADER(0x0014, HeaderRecord.class, HeaderRecord::new),
    FOOTER(0x0015, FooterRecord.class, FooterRecord::new),
    EXTERN_SHEET(0x0017, ExternSheetRecord.class, ExternSheetRecord::new),
    NAME(0x0018, NameRecord.class, NameRecord::new),
    WINDOW_PROTECT(0x0019, WindowProtectRecord.class, WindowProtectRecord::new),
    VERTICAL_PAGE_BREAK(0x001A, VerticalPageBreakRecord.class, VerticalPageBreakRecord::new),
    HORIZONTAL_PAGE_BREAK(0x001B, HorizontalPageBreakRecord.class, HorizontalPageBreakRecord::new),
    NOTE(0x001C, NoteRecord.class, NoteRecord::new),
    SELECTION(0x001D, SelectionRecord.class, SelectionRecord::new),
    DATE_WINDOW_1904(0x0022, DateWindow1904Record.class, DateWindow1904Record::new),
    EXTERNAL_NAME(0x0023, ExternalNameRecord.class, ExternalNameRecord::new),
    LEFT_MARGIN(0x0026, LeftMarginRecord.class, LeftMarginRecord::new),
    RIGHT_MARGIN(0x0027, RightMarginRecord.class, RightMarginRecord::new),
    TOP_MARGIN(0x0028, TopMarginRecord.class, TopMarginRecord::new),
    BOTTOM_MARGIN(0x0029, BottomMarginRecord.class, BottomMarginRecord::new),
    PRINT_HEADERS(0x002A, PrintHeadersRecord.class, PrintHeadersRecord::new),
    PRINT_GRIDLINES(0X002B, PrintGridlinesRecord.class, PrintGridlinesRecord::new),
    FILE_PASS(0x002F, FilePassRecord.class, FilePassRecord::new),
    FONT(0x0031, FontRecord.class, FontRecord::new),
    CONTINUE(0x003C, ContinueRecord.class, ContinueRecord::new),
    WINDOW_ONE(0x003D, WindowOneRecord.class, WindowOneRecord::new),
    BACKUP(0x0040, BackupRecord.class, BackupRecord::new),
    PANE(0x0041, PaneRecord.class, PaneRecord::new),
    CODEPAGE(0x0042, CodepageRecord.class, CodepageRecord::new),
    DCON_REF(0x0051, DConRefRecord.class, DConRefRecord::new),
    DEFAULT_COL_WIDTH(0x0055, DefaultColWidthRecord.class, DefaultColWidthRecord::new),
    CRN_COUNT(0x0059, CRNCountRecord.class, CRNCountRecord::new),
    CRN(0x005A, CRNRecord.class, CRNRecord::new),
    WRITE_ACCESS(0x005C, WriteAccessRecord.class, WriteAccessRecord::new),
    FILE_SHARING(0x005B, FileSharingRecord.class, FileSharingRecord::new),
    OBJ(0x005D, ObjRecord.class, ObjRecord::new),
    UNCALCED(0x005E, UncalcedRecord.class, UncalcedRecord::new),
    SAVE_RECALC(0x005F, SaveRecalcRecord.class, SaveRecalcRecord::new),
    OBJECT_PROTECT(0x0063, ObjectProtectRecord.class, ObjectProtectRecord::new),
    COLUMN_INFO(0x007D, ColumnInfoRecord.class, ColumnInfoRecord::new),
    GUTS(0x0080, GutsRecord.class, GutsRecord::new),
    WS_BOOL(0x0081, WSBoolRecord.class, WSBoolRecord::new),
    GRIDSET(0x0082, GridsetRecord.class, GridsetRecord::new),
    H_CENTER(0x0083, HCenterRecord.class, HCenterRecord::new),
    V_CENTER(0x0084, VCenterRecord.class, VCenterRecord::new),
    BOUND_SHEET(0x0085, BoundSheetRecord.class, BoundSheetRecord::new),
    WRITE_PROTECT(0x0086, WriteProtectRecord.class, WriteProtectRecord::new),
    COUNTRY(0X008C, CountryRecord.class, CountryRecord::new),
    HIDE_OBJ(0x008D, HideObjRecord.class, HideObjRecord::new),
    PALETTE(0x0092, PaletteRecord.class, PaletteRecord::new),
    FN_GROUP_COUNT(0x009c, FnGroupCountRecord.class, FnGroupCountRecord::new),
    AUTO_FILTER_INFO(0x009D, AutoFilterInfoRecord.class, AutoFilterInfoRecord::new),
    SCL(0x00A0, SCLRecord.class, SCLRecord::new, false),
    PRINT_SETUP(0x00A1, PrintSetupRecord.class, PrintSetupRecord::new),
    VIEW_DEFINITION(0x00B0, ViewDefinitionRecord.class, ViewDefinitionRecord::new),
    VIEW_FIELDS(0x00B1, ViewFieldsRecord.class, ViewFieldsRecord::new),
    PAGE_ITEM(0x00B6, PageItemRecord.class, PageItemRecord::new),
    MUL_BLANK(0x00BE, MulBlankRecord.class, MulBlankRecord::new),
    MUL_RK(0x00BD, MulRKRecord.class, MulRKRecord::new),
    MMS(0x00C1, MMSRecord.class, MMSRecord::new),
    DATA_ITEM(0x00C5, DataItemRecord.class, DataItemRecord::new),
    STREAM_ID(0x00D5, StreamIDRecord.class, StreamIDRecord::new),
    DB_CELL(0x00D7, DBCellRecord.class, DBCellRecord::new),
    BOOK_BOOL(0x00DA, BookBoolRecord.class, BookBoolRecord::new),
    SCENARIO_PROTECT(0x00DD, ScenarioProtectRecord.class, ScenarioProtectRecord::new),
    EXTENDED_FORMAT(0x00E0, ExtendedFormatRecord.class, ExtendedFormatRecord::new),
    INTERFACE_HDR(0x00E1, InterfaceHdrRecord.class, InterfaceHdrRecord::new),
    INTERFACE_END(0x00E2, InterfaceEndRecord.class, InterfaceEndRecord::create),
    VIEW_SOURCE(0x00E3, ViewSourceRecord.class, ViewSourceRecord::new),
    MERGE_CELLS(0x00E5, MergeCellsRecord.class, MergeCellsRecord::new),
    DRAWING_GROUP(0x00EB, DrawingGroupRecord.class, DrawingGroupRecord::new),
    DRAWING(0x00EC, DrawingRecord.class, DrawingRecord::new),
    DRAWING_SELECTION(0x00ED, DrawingSelectionRecord.class, DrawingSelectionRecord::new),
    SST(0x00FC, SSTRecord.class, SSTRecord::new),
    LABEL_SST(0X00FD, LabelSSTRecord.class, LabelSSTRecord::new),
    EXT_SST(0x00FF, ExtSSTRecord.class, ExtSSTRecord::new),
    EXTENDED_PIVOT_TABLE_VIEW_FIELDS(0x0100, ExtendedPivotTableViewFieldsRecord.class, ExtendedPivotTableViewFieldsRecord::new),
    TAB_ID(0x013D, TabIdRecord.class, TabIdRecord::new),
    USE_SEL_FS(0x0160, UseSelFSRecord.class, UseSelFSRecord::new),
    DSF(0x0161, DSFRecord.class, DSFRecord::new),
    USER_SVIEW_BEGIN(0x01AA, UserSViewBegin.class, UserSViewBegin::new),
    USER_SVIEW_END(0x01AB, UserSViewEnd.class, UserSViewEnd::new),
    SUP_BOOK(0x01AE, SupBookRecord.class, SupBookRecord::new),
    PROTECTION_REV_4(0x01AF, ProtectionRev4Record.class, ProtectionRev4Record::new),
    CF_HEADER(0x01B0, CFHeaderRecord.class, CFHeaderRecord::new),
    CF_RULE(0x01B1, CFRuleRecord.class, CFRuleRecord::new),
    DVAL(0x01B2, DVALRecord.class, DVALRecord::new),
    TEXT_OBJECT(0x01B6, TextObjectRecord.class, TextObjectRecord::new),
    REFRESH_ALL(0x01B7, RefreshAllRecord.class, RefreshAllRecord::new),
    HYPERLINK(0x01B8, HyperlinkRecord.class, HyperlinkRecord::new),
    PASSWORD_REV_4(0x01BC, PasswordRev4Record.class, PasswordRev4Record::new),
    DV(0x01BE, DVRecord.class, DVRecord::new),
    RECALC_ID(0x01C1, RecalcIdRecord.class, RecalcIdRecord::new),
    DIMENSIONS(0x0200, DimensionsRecord.class, DimensionsRecord::new),
    BLANK(0x0201, BlankRecord.class, BlankRecord::new),
    NUMBER(0x0203, NumberRecord.class, NumberRecord::new),
    LABEL(0x0204, LabelRecord.class, LabelRecord::new),
    BOOL_ERR(0x0205, BoolErrRecord.class, BoolErrRecord::new),
    STRING(0x0207, StringRecord.class, StringRecord::new),
    ROW(0x0208, RowRecord.class, RowRecord::new),
    INDEX(0x020B, IndexRecord.class, IndexRecord::new),
    ARRAY(0x0221, ArrayRecord.class, ArrayRecord::new),
    DEFAULT_ROW_HEIGHT(0x0225, DefaultRowHeightRecord.class, DefaultRowHeightRecord::new),
    TABLE(0x0236, TableRecord.class, TableRecord::new),
    WINDOW_TWO(0x023E, WindowTwoRecord.class, WindowTwoRecord::new),
    RK(0x027E, RKRecord.class, RKRecord::new),
    STYLE(0x0293, StyleRecord.class, StyleRecord::new),
    FORMAT(0x041E, FormatRecord.class, FormatRecord::new),
    SHARED_FORMULA(0x04BC, SharedFormulaRecord.class, SharedFormulaRecord::new),
    BOF(0x0809, BOFRecord.class, BOFRecord::new),
    CHART_FRT_INFO(0x0850, ChartFRTInfoRecord.class, ChartFRTInfoRecord::new),
    CHART_START_BLOCK(0x0852, ChartStartBlockRecord.class, ChartStartBlockRecord::new),
    CHART_END_BLOCK(0x0853, ChartEndBlockRecord.class, ChartEndBlockRecord::new),
    CHART_START_OBJECT(0x0854, ChartStartObjectRecord.class, ChartStartObjectRecord::new),
    CHART_END_OBJECT(0x0855, ChartEndObjectRecord.class, ChartEndObjectRecord::new),
    CAT_LAB(0x0856, CatLabRecord.class, CatLabRecord::new),
    FEAT_HDR(0x0867, FeatHdrRecord.class, FeatHdrRecord::new),
    FEAT(0x0868, FeatRecord.class, FeatRecord::new),
    DATA_LABEL_EXTENSION(0x086A, DataLabelExtensionRecord.class, DataLabelExtensionRecord::new, false),
    CF_HEADER_12(0x0879, CFHeader12Record.class, CFHeader12Record::new),
    CF_RULE_12(0x087A, CFRule12Record.class, CFRule12Record::new),
    TABLE_STYLES(0x088E, TableStylesRecord.class, TableStylesRecord::new),
    NAME_COMMENT(0x0894, NameCommentRecord.class, NameCommentRecord::new),
    HEADER_FOOTER(0x089C, HeaderFooterRecord.class, HeaderFooterRecord::new),
    UNITS(0x1001, UnitsRecord.class, UnitsRecord::new, false),
    CHART(0x1002, ChartRecord.class, ChartRecord::new),
    SERIES(0x1003, SeriesRecord.class, SeriesRecord::new),
    DATA_FORMAT(0x1006, DataFormatRecord.class, DataFormatRecord::new),
    LINE_FORMAT(0x1007, LineFormatRecord.class, LineFormatRecord::new, false),
    AREA_FORMAT(0x100A, AreaFormatRecord.class, AreaFormatRecord::new, false),
    SERIES_LABELS(0x100C, SeriesLabelsRecord.class, SeriesLabelsRecord::new, false),
    SERIES_TEXT(0x100D, SeriesTextRecord.class, SeriesTextRecord::new),
    CHART_FORMAT(0x1014, ChartFormatRecord.class, ChartFormatRecord::new, false),
    LEGEND(0x1015, LegendRecord.class, LegendRecord::new),
    SERIES_LIST(0x1016, SeriesListRecord.class, SeriesListRecord::new, false),
    BAR(0x1017, BarRecord.class, BarRecord::new, false),
    AREA(0x101A, AreaRecord.class, AreaRecord::new),
    AXIS(0x101D, AxisRecord.class, AxisRecord::new, false),
    TICK(0x101E, TickRecord.class, TickRecord::new, false),
    VALUE_RANGE(0x101F, ValueRangeRecord.class, ValueRangeRecord::new),
    CATEGORY_SERIES_AXIS(0x1020, CategorySeriesAxisRecord.class, CategorySeriesAxisRecord::new, false),
    AXIS_LINE_FORMAT(0x1021, AxisLineFormatRecord.class, AxisLineFormatRecord::new, false),
    DEFAULT_DATA_LABEL_TEXT_PROPERTIES(0x1024, DefaultDataLabelTextPropertiesRecord.class, DefaultDataLabelTextPropertiesRecord::new, false),
    TEXT(0x1025, TextRecord.class, TextRecord::new, false),
    FONT_INDEX(0x1026, FontIndexRecord.class, FontIndexRecord::new, false),
    OBJECT_LINK(0x1027, ObjectLinkRecord.class, ObjectLinkRecord::new, false),
    FRAME(0x1032, FrameRecord.class, FrameRecord::new, false),
    BEGIN(0x1033, BeginRecord.class, BeginRecord::new),
    END(0x1034, EndRecord.class, EndRecord::new),
    PLOT_AREA(0x1035, PlotAreaRecord.class, PlotAreaRecord::new, false),
    AXIS_PARENT(0x1041, AxisParentRecord.class, AxisParentRecord::new, false),
    SHEET_PROPERTIES(0x1044, SheetPropertiesRecord.class, SheetPropertiesRecord::new, false),
    SERIES_CHART_GROUP_INDEX(0x1045, SeriesChartGroupIndexRecord.class, SeriesChartGroupIndexRecord::new),
    AXIS_USED(0x1046, AxisUsedRecord.class, AxisUsedRecord::new, false),
    NUMBER_FORMAT_INDEX(0x104E, NumberFormatIndexRecord.class, NumberFormatIndexRecord::new, false),
    CHART_TITLE_FORMAT(0x1050, ChartTitleFormatRecord.class, ChartTitleFormatRecord::new),
    LINKED_DATA(0x1051, LinkedDataRecord.class, LinkedDataRecord::new),
    FONT_BASIS(0x1060, FontBasisRecord.class, FontBasisRecord::new, false),
    AXIS_OPTIONS(0x1062, AxisOptionsRecord.class, AxisOptionsRecord::new, false),
    DAT(0x1063, DatRecord.class, DatRecord::new, false),
    PLOT_GROWTH(0x1064, PlotGrowthRecord.class, PlotGrowthRecord::new, false),
    SERIES_INDEX(0x1065, SeriesIndexRecord.class, SeriesIndexRecord::new, false),
    // Dummy record
    ESCHER_AGGREGATE(9876, EscherAggregate.class, (in) -> new EscherAggregate(true))
    ;

    @FunctionalInterface
    public interface RecordConstructor<T extends Record> {
        T apply(RecordInputStream in);
    }

    private static final Map<Short,HSSFRecordTypes> LOOKUP =
        Arrays.stream(values()).collect(Collectors.toMap(HSSFRecordTypes::getSid, Function.identity()));

    public final short sid;
    public final Class<? extends org.apache.poi.hssf.record.Record> clazz;
    public final RecordConstructor<? extends org.apache.poi.hssf.record.Record> recordConstructor;
    public final boolean parse;

    HSSFRecordTypes(int sid, Class<? extends org.apache.poi.hssf.record.Record> clazz, RecordConstructor<? extends org.apache.poi.hssf.record.Record> recordConstructor) {
        this(sid, clazz, recordConstructor,true);
    }

    HSSFRecordTypes(int sid, Class<? extends org.apache.poi.hssf.record.Record> clazz, RecordConstructor<? extends org.apache.poi.hssf.record.Record> recordConstructor, boolean parse) {
        this.sid = (short)sid;
        this.clazz = clazz;
        this.recordConstructor = recordConstructor;
        this.parse = parse;
    }

    public static HSSFRecordTypes forSID(int sid) {
        return LOOKUP.getOrDefault((short)sid, UNKNOWN);
    }

    public short getSid() {
        return sid;
    }

    public Class<? extends Record> getClazz() {
        return clazz;
    }

    public RecordConstructor<? extends org.apache.poi.hssf.record.Record> getRecordConstructor() {
        return recordConstructor;
    }

    public boolean isParseable() {
        return parse;
    }
}

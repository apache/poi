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

import java.util.HashMap;
import java.util.Map;

import org.apache.poi.hssf.record.chart.*;
import org.apache.poi.hssf.record.pivottable.DataItemRecord;
import org.apache.poi.hssf.record.pivottable.ExtendedPivotTableViewFieldsRecord;
import org.apache.poi.hssf.record.pivottable.PageItemRecord;
import org.apache.poi.hssf.record.pivottable.StreamIDRecord;
import org.apache.poi.hssf.record.pivottable.ViewDefinitionRecord;
import org.apache.poi.hssf.record.pivottable.ViewFieldsRecord;
import org.apache.poi.hssf.record.pivottable.ViewSourceRecord;

public enum HSSFRecordTypes {
    UNKNOWN(-1, UnknownRecord::new),
    FORMULA(0x0006, FormulaRecord::new),
    EOF(0x000A, EOFRecord::new),
    CALC_COUNT(0x000C, CalcCountRecord::new),
    CALC_MODE(0x000D, CalcModeRecord::new),
    PRECISION(0x000E, PrecisionRecord::new),
    REF_MODE(0x000F, RefModeRecord::new),
    DELTA(0x0010, DeltaRecord::new),
    ITERATION(0x0011, IterationRecord::new),
    PROTECT(0x0012, ProtectRecord::new),
    PASSWORD(0x0013, PasswordRecord::new),
    HEADER(0x0014, HeaderRecord::new),
    FOOTER(0x0015, FooterRecord::new),
    EXTERN_SHEET(0x0017, ExternSheetRecord::new),
    NAME(0x0018, NameRecord::new),
    WINDOW_PROTECT(0x0019, WindowProtectRecord::new),
    VERTICAL_PAGE_BREAK(0x001A, VerticalPageBreakRecord::new),
    HORIZONTAL_PAGE_BREAK(0x001B, HorizontalPageBreakRecord::new),
    NOTE(0x001C, NoteRecord::new),
    SELECTION(0x001D, SelectionRecord::new),
    DATE_WINDOW_1904(0x0022, DateWindow1904Record::new),
    EXTERNAL_NAME(0x0023, ExternalNameRecord::new),
    LEFT_MARGIN(0x0026, LeftMarginRecord::new),
    RIGHT_MARGIN(0x0027, RightMarginRecord::new),
    TOP_MARGIN(0x0028, TopMarginRecord::new),
    BOTTOM_MARGIN(0x0029, BottomMarginRecord::new),
    PRINT_HEADERS(0x002A, PrintHeadersRecord::new),
    PRINT_GRIDLINES(0X002B, PrintGridlinesRecord::new),
    FILE_PASS(0x002F, FilePassRecord::new),
    FONT(0x0031, FontRecord::new),
    CONTINUE(0x003C, ContinueRecord::new),
    WINDOW_ONE(0x003D, WindowOneRecord::new),
    BACKUP(0x0040, BackupRecord::new),
    PANE(0x0041, PaneRecord::new),
    CODEPAGE(0x0042, CodepageRecord::new),
    DCON_REF(0x0051, DConRefRecord::new),
    DEFAULT_COL_WIDTH(0x0055, DefaultColWidthRecord::new),
    CRN_COUNT(0x0059, CRNCountRecord::new),
    CRN(0x005A, CRNRecord::new),
    WRITE_ACCESS(0x005C, WriteAccessRecord::new),
    FILE_SHARING(0x005B, FileSharingRecord::new),
    OBJ(0x005D, ObjRecord::new),
    UNCALCED(0x005E, UncalcedRecord::new),
    SAVE_RECALC(0x005F, SaveRecalcRecord::new),
    OBJECT_PROTECT(0x0063, ObjectProtectRecord::new),
    COLUMN_INFO(0x007D, ColumnInfoRecord::new),
    GUTS(0x0080, GutsRecord::new),
    WS_BOOL(0x0081, WSBoolRecord::new),
    GRIDSET(0x0082, GridsetRecord::new),
    H_CENTER(0x0083, HCenterRecord::new),
    V_CENTER(0x0084, VCenterRecord::new),
    BOUND_SHEET(0x0085, BoundSheetRecord::new),
    WRITE_PROTECT(0x0086, WriteProtectRecord::new),
    COUNTRY(0X008C, CountryRecord::new),
    HIDE_OBJ(0x008D, HideObjRecord::new),
    PALETTE(0x0092, PaletteRecord::new),
    FN_GROUP_COUNT(0x009c, FnGroupCountRecord::new),
    AUTO_FILTER_INFO(0x009D, AutoFilterInfoRecord::new),
    SCL(0x00A0, SCLRecord::new),
    PRINT_SETUP(0x00A1, PrintSetupRecord::new),
    VIEW_DEFINITION(0x00B0, ViewDefinitionRecord::new),
    VIEW_FIELDS(0x00B1, ViewFieldsRecord::new),
    PAGE_ITEM(0x00B6, PageItemRecord::new),
    MUL_BLANK(0x00BE, MulBlankRecord::new),
    MUL_RK(0x00BD, MulRKRecord::new),
    MMS(0x00C1, MMSRecord::new),
    DATA_ITEM(0x00C5, DataItemRecord::new),
    STREAM_ID(0x00D5, StreamIDRecord::new),
    DB_CELL(0x00D7, DBCellRecord::new),
    BOOK_BOOL(0x00DA, BookBoolRecord::new),
    SCENARIO_PROTECT(0x00DD, ScenarioProtectRecord::new),
    EXTENDED_FORMAT(0x00E0, ExtendedFormatRecord::new),
    INTERFACE_HDR(0x00E1, InterfaceHdrRecord::new),
    INTERFACE_END(0x00E2, InterfaceEndRecord::create),
    VIEW_SOURCE(0x00E3, ViewSourceRecord::new),
    MERGE_CELLS(0x00E5, MergeCellsRecord::new),
    DRAWING_GROUP(0x00EB, DrawingGroupRecord::new),
    DRAWING(0x00EC, DrawingRecord::new),
    DRAWING_SELECTION(0x00ED, DrawingSelectionRecord::new),
    SST(0x00FC, SSTRecord::new),
    LABEL_SST(0X00FD, LabelSSTRecord::new),
    EXT_SST(0x00FF, ExtSSTRecord::new),
    EXTENDED_PIVOT_TABLE_VIEW_FIELDS(0x0100, ExtendedPivotTableViewFieldsRecord::new),
    TAB_ID(0x013D, TabIdRecord::new),
    USE_SEL_FS(0x0160, UseSelFSRecord::new),
    DSF(0x0161, DSFRecord::new),
    USER_SVIEW_BEGIN(0x01AA, UserSViewBegin::new),
    USER_SVIEW_END(0x01AB, UserSViewEnd::new),
    SUP_BOOK(0x01AE, SupBookRecord::new),
    PROTECTION_REV_4(0x01AF, ProtectionRev4Record::new),
    CF_HEADER(0x01B0, CFHeaderRecord::new),
    CF_RULE(0x01B1, CFRuleRecord::new),
    DVAL(0x01B2, DVALRecord::new),
    TEXT_OBJECT(0x01B6, TextObjectRecord::new),
    REFRESH_ALL(0x01B7, RefreshAllRecord::new),
    HYPERLINK(0x01B8, HyperlinkRecord::new),
    PASSWORD_REV_4(0x01BC, PasswordRev4Record::new),
    DV(0x01BE, DVRecord::new),
    RECALC_ID(0x01C1, RecalcIdRecord::new),
    DIMENSIONS(0x0200, DimensionsRecord::new),
    BLANK(0x0201, BlankRecord::new),
    NUMBER(0x0203, NumberRecord::new),
    LABEL(0x0204, LabelRecord::new),
    BOOL_ERR(0x0205, BoolErrRecord::new),
    STRING(0x0207, StringRecord::new),
    ROW(0x0208, RowRecord::new),
    INDEX(0x020B, IndexRecord::new),
    ARRAY(0x0221, ArrayRecord::new),
    DEFAULT_ROW_HEIGHT(0x0225, DefaultRowHeightRecord::new),
    TABLE(0x0236, TableRecord::new),
    WINDOW_TWO(0x023E, WindowTwoRecord::new),
    RK(0x027E, RKRecord::new),
    STYLE(0x0293, StyleRecord::new),
    FORMAT(0x041E, FormatRecord::new),
    SHARED_FORMULA(0x04BC, SharedFormulaRecord::new),
    BOF(0x0809, BOFRecord::new),
    CHART_FRT_INFO(0x0850, ChartFRTInfoRecord::new),
    CHART_START_BLOCK(0x0852, ChartStartBlockRecord::new),
    CHART_END_BLOCK(0x0853, ChartEndBlockRecord::new),
    CHART_START_OBJECT(0x0854, ChartStartObjectRecord::new),
    CHART_END_OBJECT(0x0855, ChartEndObjectRecord::new),
    CAT_LAB(0x0856, CatLabRecord::new),
    FEAT_HDR(0x0867, FeatHdrRecord::new),
    FEAT(0x0868, FeatRecord::new),
    DATA_LABEL_EXTENSION(0x086A, DataLabelExtensionRecord::new),
    CF_HEADER_12(0x0879, CFHeader12Record::new),
    CF_RULE_12(0x087A, CFRule12Record::new),
    TABLE_STYLES(0x088E, TableStylesRecord::new),
    NAME_COMMENT(0x0894, NameCommentRecord::new),
    HEADER_FOOTER(0x089C, HeaderFooterRecord::new),
    UNITS(0x1001, UnitsRecord::new),
    CHART(0x1002, ChartRecord::new),
    SERIES(0x1003, SeriesRecord::new),
    DATA_FORMAT(0x1006, DataFormatRecord::new),
    LINE_FORMAT(0x1007, LineFormatRecord::new),
    AREA_FORMAT(0x100A, AreaFormatRecord::new),
    SERIES_LABELS(0x100C, SeriesLabelsRecord::new),
    SERIES_TEXT(0x100D, SeriesTextRecord::new),
    CHART_FORMAT(0x1014, ChartFormatRecord::new),
    LEGEND(0x1015, LegendRecord::new),
    SERIES_LIST(0x1016, SeriesListRecord::new),
    BAR(0x1017, BarRecord::new),
    AREA(0x101A, AreaRecord::new),
    AXIS(0x101D, AxisRecord::new),
    TICK(0x101E, TickRecord::new),
    VALUE_RANGE(0x101F, ValueRangeRecord::new),
    CATEGORY_SERIES_AXIS(0x1020, CategorySeriesAxisRecord::new),
    AXIS_LINE_FORMAT(0x1021, AxisLineFormatRecord::new),
    DEFAULT_DATA_LABEL_TEXT_PROPERTIES(0x1024, DefaultDataLabelTextPropertiesRecord::new),
    TEXT(0x1025, TextRecord::new),
    FONT_INDEX(0x1026, FontIndexRecord::new),
    OBJECT_LINK(0x1027, ObjectLinkRecord::new),
    FRAME(0x1032, FrameRecord::new),
    BEGIN(0x1033, BeginRecord::new),
    END(0x1034, EndRecord::new),
    PLOT_AREA(0x1035, PlotAreaRecord::new),
    AXIS_PARENT(0x1041, AxisParentRecord::new),
    SHEET_PROPERTIES(0x1044, SheetPropertiesRecord::new),
    SERIES_CHART_GROUP_INDEX(0x1045, SeriesChartGroupIndexRecord::new),
    AXIS_USED(0x1046, AxisUsedRecord::new),
    NUMBER_FORMAT_INDEX(0x104E, NumberFormatIndexRecord::new),
    CHART_TITLE_FORMAT(0x1050, ChartTitleFormatRecord::new),
    LINKED_DATA(0x1051, LinkedDataRecord::new),
    FONT_BASIS(0x1060, FontBasisRecord::new),
    AXIS_OPTIONS(0x1062, AxisOptionsRecord::new),
    DAT(0x1063, DatRecord::new),
    PLOT_GROWTH(0x1064, PlotGrowthRecord::new),
    SERIES_INDEX(0x1065, SeriesIndexRecord::new),
    // Dummy record
    ESCHER_AGGREGATE(9876, (in) -> new EscherAggregate(true))
    ;

    @FunctionalInterface
    public interface RecordConstructor<T extends Record> {
        T apply(RecordInputStream in);
    }

    private static final Map<Short,HSSFRecordTypes> LOOKUP;

    static {
        LOOKUP = new HashMap<>();
        for(HSSFRecordTypes s : values()) {
            LOOKUP.put(s.sid, s);
        }
    }

    public final short sid;
    public final RecordConstructor<?> recordConstructor;

    HSSFRecordTypes(int sid, RecordConstructor<?> recordConstructor) {
        this.sid = (short)sid;
        this.recordConstructor = recordConstructor;
    }

    public static HSSFRecordTypes forTypeID(int typeID) {
        return LOOKUP.getOrDefault((short)typeID, UNKNOWN);
    }

}

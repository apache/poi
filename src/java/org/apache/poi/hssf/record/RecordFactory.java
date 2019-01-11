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

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.hssf.record.chart.BeginRecord;
import org.apache.poi.hssf.record.chart.CatLabRecord;
import org.apache.poi.hssf.record.chart.ChartEndBlockRecord;
import org.apache.poi.hssf.record.chart.ChartEndObjectRecord;
import org.apache.poi.hssf.record.chart.ChartFRTInfoRecord;
import org.apache.poi.hssf.record.chart.ChartRecord;
import org.apache.poi.hssf.record.chart.ChartStartBlockRecord;
import org.apache.poi.hssf.record.chart.ChartStartObjectRecord;
import org.apache.poi.hssf.record.chart.ChartTitleFormatRecord;
import org.apache.poi.hssf.record.chart.DataFormatRecord;
import org.apache.poi.hssf.record.chart.EndRecord;
import org.apache.poi.hssf.record.chart.LegendRecord;
import org.apache.poi.hssf.record.chart.LinkedDataRecord;
import org.apache.poi.hssf.record.chart.SeriesRecord;
import org.apache.poi.hssf.record.chart.SeriesTextRecord;
import org.apache.poi.hssf.record.chart.SeriesToChartGroupRecord;
import org.apache.poi.hssf.record.chart.ValueRangeRecord;
import org.apache.poi.hssf.record.pivottable.DataItemRecord;
import org.apache.poi.hssf.record.pivottable.ExtendedPivotTableViewFieldsRecord;
import org.apache.poi.hssf.record.pivottable.PageItemRecord;
import org.apache.poi.hssf.record.pivottable.StreamIDRecord;
import org.apache.poi.hssf.record.pivottable.ViewDefinitionRecord;
import org.apache.poi.hssf.record.pivottable.ViewFieldsRecord;
import org.apache.poi.hssf.record.pivottable.ViewSourceRecord;

/**
 * Title:  Record Factory<p>
 * Description:  Takes a stream and outputs an array of Record objects.
 *
 * @see org.apache.poi.hssf.eventmodel.EventRecordFactory
 */
public final class RecordFactory {
    private static final int NUM_RECORDS = 512;

    private interface I_RecordCreator {
        Record create(RecordInputStream in);

        Class<? extends Record> getRecordClass();
    }
    private static final class ReflectionConstructorRecordCreator implements I_RecordCreator {

        private final Constructor<? extends Record> _c;
        public ReflectionConstructorRecordCreator(Constructor<? extends Record> c) {
            _c = c;
        }
        @Override
        public Record create(RecordInputStream in) {
            Object[] args = { in, };
            try {
                return _c.newInstance(args);
            } catch (IllegalArgumentException | IllegalAccessException | InstantiationException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                Throwable t = e.getTargetException();
                if (t instanceof org.apache.poi.util.RecordFormatException) {
                    throw (org.apache.poi.util.RecordFormatException)t;
                } else if (t instanceof EncryptedDocumentException) {
                    throw (EncryptedDocumentException)t;
                } else {
                    throw new org.apache.poi.util.RecordFormatException("Unable to construct record instance" , t);
                }
            }
        }
        @Override
        public Class<? extends Record> getRecordClass() {
            return _c.getDeclaringClass();
        }
    }
    /**
     * A "create" method is used instead of the usual constructor if the created record might
     * be of a different class to the declaring class.
     */
    private static final class ReflectionMethodRecordCreator implements I_RecordCreator {
        private final Method _m;
        public ReflectionMethodRecordCreator(Method m) {
            _m = m;
        }
        @Override
        public Record create(RecordInputStream in) {
            Object[] args = { in, };
            try {
                return (Record) _m.invoke(null, args);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new org.apache.poi.util.RecordFormatException("Unable to construct record instance" , e.getTargetException());
            }
        }
        @Override
        @SuppressWarnings("unchecked")
        public Class<? extends Record> getRecordClass() {
            return (Class<? extends Record>) _m.getDeclaringClass();
        }
    }

    private static final Class<?>[] CONSTRUCTOR_ARGS = { RecordInputStream.class, };

    /**
     * contains the classes for all the records we want to parse.<br>
     * Note - this most but not *every* subclass of Record.
     */
    @SuppressWarnings("unchecked")
    private static final Class<? extends Record>[] recordClasses = new Class[] {
        ArrayRecord.class,
        AutoFilterInfoRecord.class,
        BackupRecord.class,
        BlankRecord.class,
        BOFRecord.class,
        BookBoolRecord.class,
        BoolErrRecord.class,
        BottomMarginRecord.class,
        BoundSheetRecord.class,
        CalcCountRecord.class,
        CalcModeRecord.class,
        CFHeaderRecord.class,
        CFHeader12Record.class,
        CFRuleRecord.class,
        CFRule12Record.class,
        ChartRecord.class,
        ChartTitleFormatRecord.class,
        CodepageRecord.class,
        ColumnInfoRecord.class,
        ContinueRecord.class,
        CountryRecord.class,
        CRNCountRecord.class,
        CRNRecord.class,
        DateWindow1904Record.class,
        DBCellRecord.class,
        DConRefRecord.class,
        DefaultColWidthRecord.class,
        DefaultRowHeightRecord.class,
        DeltaRecord.class,
        DimensionsRecord.class,
        DrawingGroupRecord.class,
        DrawingRecord.class,
        DrawingSelectionRecord.class,
        DSFRecord.class,
        DVALRecord.class,
        DVRecord.class,
        EOFRecord.class,
        ExtendedFormatRecord.class,
        ExternalNameRecord.class,
        ExternSheetRecord.class,
        ExtSSTRecord.class,
        FeatRecord.class,
        FeatHdrRecord.class,
        FilePassRecord.class,
        FileSharingRecord.class,
        FnGroupCountRecord.class,
        FontRecord.class,
        FooterRecord.class,
        FormatRecord.class,
        FormulaRecord.class,
        GridsetRecord.class,
        GutsRecord.class,
        HCenterRecord.class,
        HeaderRecord.class,
        HeaderFooterRecord.class,
        HideObjRecord.class,
        HorizontalPageBreakRecord.class,
        HyperlinkRecord.class,
        IndexRecord.class,
        InterfaceEndRecord.class,
        InterfaceHdrRecord.class,
        IterationRecord.class,
        LabelRecord.class,
        LabelSSTRecord.class,
        LeftMarginRecord.class,
        LegendRecord.class,
        MergeCellsRecord.class,
        MMSRecord.class,
        MulBlankRecord.class,
        MulRKRecord.class,
        NameRecord.class,
        NameCommentRecord.class,
        NoteRecord.class,
        NumberRecord.class,
        ObjectProtectRecord.class,
        ObjRecord.class,
        PaletteRecord.class,
        PaneRecord.class,
        PasswordRecord.class,
        PasswordRev4Record.class,
        PrecisionRecord.class,
        PrintGridlinesRecord.class,
        PrintHeadersRecord.class,
        PrintSetupRecord.class,
        ProtectionRev4Record.class,
        ProtectRecord.class,
        RecalcIdRecord.class,
        RefModeRecord.class,
        RefreshAllRecord.class,
        RightMarginRecord.class,
        RKRecord.class,
        RowRecord.class,
        SaveRecalcRecord.class,
        ScenarioProtectRecord.class,
        SelectionRecord.class,
        SeriesRecord.class,
        SeriesTextRecord.class,
        SharedFormulaRecord.class,
        SSTRecord.class,
        StringRecord.class,
        StyleRecord.class,
        SupBookRecord.class,
        TabIdRecord.class,
        TableRecord.class,
        TableStylesRecord.class,
        TextObjectRecord.class,
        TopMarginRecord.class,
        UncalcedRecord.class,
        UseSelFSRecord.class,
        UserSViewBegin.class,
        UserSViewEnd.class,
        ValueRangeRecord.class,
        VCenterRecord.class,
        VerticalPageBreakRecord.class,
        WindowOneRecord.class,
        WindowProtectRecord.class,
        WindowTwoRecord.class,
        WriteAccessRecord.class,
        WriteProtectRecord.class,
        WSBoolRecord.class,

        // chart records
        BeginRecord.class,
        ChartFRTInfoRecord.class,
        ChartStartBlockRecord.class,
        ChartEndBlockRecord.class,
        // TODO ChartFormatRecord.class,
        ChartStartObjectRecord.class,
        ChartEndObjectRecord.class,
        CatLabRecord.class,
        DataFormatRecord.class,
        EndRecord.class,
        LinkedDataRecord.class,
        SeriesToChartGroupRecord.class,

        // pivot table records
        DataItemRecord.class,
        ExtendedPivotTableViewFieldsRecord.class,
        PageItemRecord.class,
        StreamIDRecord.class,
        ViewDefinitionRecord.class,
        ViewFieldsRecord.class,
        ViewSourceRecord.class,
    };

    /**
     * cache of the recordsToMap();
     */
    private static final Map<Integer, I_RecordCreator> _recordCreatorsById  = recordsToMap(recordClasses);

    private static short[] _allKnownRecordSIDs;

    /**
     * Debug / diagnosis method<p>
     *
     * Gets the POI implementation class for a given {@code sid}.  Only a subset of the BIFF
     * records are actually interpreted by POI.  A few others are known but not interpreted
     * (see {@link UnknownRecord#getBiffName(int)}).
     *
     * @param sid the record sid
     *
     * @return the POI implementation class for the specified record {@code sid}.
     * {@code null} if the specified record is not interpreted by POI.
     */
    public static Class<? extends Record> getRecordClass(int sid) {
        I_RecordCreator rc = _recordCreatorsById.get(Integer.valueOf(sid));
        if (rc == null) {
            return null;
        }
        return rc.getRecordClass();
    }

    /**
     * create a record, if there are MUL records than multiple records
     * are returned digested into the non-mul form.
     *
     * @param in the RecordInputStream to read from
     * @return the extracted records
     */
    public static Record [] createRecord(RecordInputStream in) {
        Record record = createSingleRecord(in);
        if (record instanceof DBCellRecord) {
            // Not needed by POI.  Regenerated from scratch by POI when spreadsheet is written
            return new Record[] { null, };
        }
        if (record instanceof RKRecord) {
            return new Record[] { convertToNumberRecord((RKRecord) record), };
        }
        if (record instanceof MulRKRecord) {
            return convertRKRecords((MulRKRecord)record);
        }
        return new Record[] { record, };
    }

    public static Record createSingleRecord(RecordInputStream in) {
        I_RecordCreator constructor = _recordCreatorsById.get(Integer.valueOf(in.getSid()));

        if (constructor == null) {
            return new UnknownRecord(in);
        }

        return constructor.create(in);
    }

    /**
     * RK record is a slightly smaller alternative to NumberRecord
     * POI likes NumberRecord better
     *
     * @param rk the RK record to convert
     * @return the NumberRecord
     */
    public static NumberRecord convertToNumberRecord(RKRecord rk) {
        NumberRecord num = new NumberRecord();

        num.setColumn(rk.getColumn());
        num.setRow(rk.getRow());
        num.setXFIndex(rk.getXFIndex());
        num.setValue(rk.getRKNumber());
        return num;
    }

    /**
     * Converts a {@link MulRKRecord} into an equivalent array of {@link NumberRecord NumberRecords}
     *
     * @param mrk the MulRKRecord to convert
     * @return the equivalent array of {@link NumberRecord NumberRecords}
     */
    public static NumberRecord[] convertRKRecords(MulRKRecord mrk) {
        NumberRecord[] mulRecs = new NumberRecord[mrk.getNumColumns()];
        for (int k = 0; k < mrk.getNumColumns(); k++) {
            NumberRecord nr = new NumberRecord();

            nr.setColumn((short) (k + mrk.getFirstColumn()));
            nr.setRow(mrk.getRow());
            nr.setXFIndex(mrk.getXFAt(k));
            nr.setValue(mrk.getRKNumberAt(k));
            mulRecs[k] = nr;
        }
        return mulRecs;
    }

    /**
     * Converts a {@link MulBlankRecord} into an equivalent array of {@link BlankRecord BlankRecords}
     *
     * @param mbk the MulBlankRecord to convert
     * @return the equivalent array of {@link BlankRecord BlankRecords}
     */
    public static BlankRecord[] convertBlankRecords(MulBlankRecord mbk) {
        BlankRecord[] mulRecs = new BlankRecord[mbk.getNumColumns()];
        for (int k = 0; k < mbk.getNumColumns(); k++) {
            BlankRecord br = new BlankRecord();

            br.setColumn((short) (k + mbk.getFirstColumn()));
            br.setRow(mbk.getRow());
            br.setXFIndex(mbk.getXFAt(k));
            mulRecs[k] = br;
        }
        return mulRecs;
    }

    /**
     * @return an array of all the SIDS for all known records
     */
    public static short[] getAllKnownRecordSIDs() {
        if (_allKnownRecordSIDs == null) {
            short[] results = new short[ _recordCreatorsById.size() ];
            int i = 0;

            for (Integer sid : _recordCreatorsById.keySet()) {
                results[i++] = sid.shortValue();
            }
            Arrays.sort(results);
            _allKnownRecordSIDs = results;
        }

        return _allKnownRecordSIDs.clone();
    }

    /**
     * gets the record constructors and sticks them in the map by SID
     * @return map of SIDs to short,short,byte[] constructors for Record classes
     * most of org.apache.poi.hssf.record.*
     */
    private static Map<Integer, I_RecordCreator> recordsToMap(Class<? extends Record> [] records) {
        Map<Integer, I_RecordCreator> result = new HashMap<>();
        Set<Class<?>> uniqueRecClasses = new HashSet<>(records.length * 3 / 2);

        for (Class<? extends Record> recClass : records) {
            if(!Record.class.isAssignableFrom(recClass)) {
                throw new RuntimeException("Invalid record sub-class (" + recClass.getName() + ")");
            }
            if(Modifier.isAbstract(recClass.getModifiers())) {
                throw new RuntimeException("Invalid record class (" + recClass.getName() + ") - must not be abstract");
            }
            if(!uniqueRecClasses.add(recClass)) {
                throw new RuntimeException("duplicate record class (" + recClass.getName() + ")");
            }

            int sid;
            try {
                sid = recClass.getField("sid").getShort(null);
            } catch (Exception illegalArgumentException) {
                throw new org.apache.poi.util.RecordFormatException(
                        "Unable to determine record types");
            }
            Integer key = Integer.valueOf(sid);
            if (result.containsKey(key)) {
                Class<?> prevClass = result.get(key).getRecordClass();
                throw new RuntimeException("duplicate record sid 0x" +
                        Integer.toHexString(sid).toUpperCase(Locale.ROOT)
                        + " for classes (" + recClass.getName() + ") and ("
                        + prevClass.getName() + ")");
            }
            result.put(key, getRecordCreator(recClass));
        }
        // result.put(Integer.valueOf(0x0406), result.get(Integer.valueOf(0x06)));
        return result;
    }

    private static I_RecordCreator getRecordCreator(Class<? extends Record> recClass) {
        try {
            Constructor<? extends Record> constructor;
            constructor = recClass.getConstructor(CONSTRUCTOR_ARGS);
            return new ReflectionConstructorRecordCreator(constructor);
        } catch (NoSuchMethodException e) {
            // fall through and look for other construction methods
        }
        try {
            Method m = recClass.getDeclaredMethod("create", CONSTRUCTOR_ARGS);
            return new ReflectionMethodRecordCreator(m);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Failed to find constructor or create method for (" + recClass.getName() + ").");
        }
    }
    /**
     * Create an array of records from an input stream
     *
     * @param in the InputStream from which the records will be obtained
     *
     * @return an array of Records created from the InputStream
     *
     * @exception org.apache.poi.util.RecordFormatException on error processing the InputStream
     */
    public static List<Record> createRecords(InputStream in) throws org.apache.poi.util.RecordFormatException {

        List<Record> records = new ArrayList<>(NUM_RECORDS);

        RecordFactoryInputStream recStream = new RecordFactoryInputStream(in, true);

        Record record;
        while ((record = recStream.nextRecord())!=null) {
            records.add(record);
        }

        return records;
    }
}

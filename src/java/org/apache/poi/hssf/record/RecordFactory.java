
/* ====================================================================
   Copyright 2002-2004   Apache Software Foundation

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */
        

package org.apache.poi.hssf.record;

import org.apache.poi.util.LittleEndian;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.*;

/**
 * Title:  Record Factory<P>
 * Description:  Takes a stream and outputs an array of Record objects.<P>
 *
 * @deprecated use EventRecordFactory instead
 * @see org.apache.poi.hssf.eventmodel.EventRecordFactory
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @author Marc Johnson (mjohnson at apache dot org)
 * @author Glen Stampoultzis (glens at apache.org)
 * @author Csaba Nagy (ncsaba at yahoo dot com)
 * @version 1.0-pre
 */

public class RecordFactory
{
    private static int           NUM_RECORDS = 10000;
    private static final Class[] records;

    static {
        if (FormulaRecord.EXPERIMENTAL_FORMULA_SUPPORT_ENABLED) {
            records = new Class[]
            {
                BOFRecord.class, InterfaceHdrRecord.class, MMSRecord.class,
                InterfaceEndRecord.class, WriteAccessRecord.class,
                CodepageRecord.class, DSFRecord.class, TabIdRecord.class,
                FnGroupCountRecord.class, WindowProtectRecord.class,
                ProtectRecord.class, PasswordRecord.class, ProtectionRev4Record.class,
                PasswordRev4Record.class, WindowOneRecord.class, BackupRecord.class,
                HideObjRecord.class, DateWindow1904Record.class,
                PrecisionRecord.class, RefreshAllRecord.class, BookBoolRecord.class,
                FontRecord.class, FormatRecord.class, ExtendedFormatRecord.class,
                StyleRecord.class, UseSelFSRecord.class, BoundSheetRecord.class,
                CountryRecord.class, SSTRecord.class, ExtSSTRecord.class,
                EOFRecord.class, IndexRecord.class, CalcModeRecord.class,
                CalcCountRecord.class, RefModeRecord.class, IterationRecord.class,
                DeltaRecord.class, SaveRecalcRecord.class, PrintHeadersRecord.class,
                PrintGridlinesRecord.class, GridsetRecord.class, GutsRecord.class,
                DefaultRowHeightRecord.class, WSBoolRecord.class, HeaderRecord.class,
                FooterRecord.class, HCenterRecord.class, VCenterRecord.class,
                PrintSetupRecord.class, DefaultColWidthRecord.class,
                DimensionsRecord.class, RowRecord.class, LabelSSTRecord.class,
                RKRecord.class, NumberRecord.class, DBCellRecord.class,
                WindowTwoRecord.class, SelectionRecord.class, ContinueRecord.class,
                LabelRecord.class, BlankRecord.class, ColumnInfoRecord.class,
                MulRKRecord.class, MulBlankRecord.class, MergeCellsRecord.class,
                FormulaRecord.class, BoolErrRecord.class, ExternSheetRecord.class,
                NameRecord.class, LeftMarginRecord.class, RightMarginRecord.class,
                TopMarginRecord.class, BottomMarginRecord.class,
                DrawingRecord.class, DrawingGroupRecord.class, DrawingSelectionRecord.class,
                ObjRecord.class, TextObjectRecord.class,
                PaletteRecord.class, StringRecord.class, RecalcIdRecord.class, SharedFormulaRecord.class,
                HorizontalPageBreakRecord.class, VerticalPageBreakRecord.class
            };
        } else {
            records = new Class[]
            {
                BOFRecord.class, InterfaceHdrRecord.class, MMSRecord.class,
                InterfaceEndRecord.class, WriteAccessRecord.class,
                CodepageRecord.class, DSFRecord.class, TabIdRecord.class,
                FnGroupCountRecord.class, WindowProtectRecord.class,
                ProtectRecord.class, PasswordRecord.class, ProtectionRev4Record.class,
                PasswordRev4Record.class, WindowOneRecord.class, BackupRecord.class,
                HideObjRecord.class, DateWindow1904Record.class,
                PrecisionRecord.class, RefreshAllRecord.class, BookBoolRecord.class,
                FontRecord.class, FormatRecord.class, ExtendedFormatRecord.class,
                StyleRecord.class, UseSelFSRecord.class, BoundSheetRecord.class,
                CountryRecord.class, SSTRecord.class, ExtSSTRecord.class,
                EOFRecord.class, IndexRecord.class, CalcModeRecord.class,
                CalcCountRecord.class, RefModeRecord.class, IterationRecord.class,
                DeltaRecord.class, SaveRecalcRecord.class, PrintHeadersRecord.class,
                PrintGridlinesRecord.class, GridsetRecord.class, GutsRecord.class,
                DefaultRowHeightRecord.class, WSBoolRecord.class, HeaderRecord.class,
                FooterRecord.class, HCenterRecord.class, VCenterRecord.class,
                PrintSetupRecord.class, DefaultColWidthRecord.class,
                DimensionsRecord.class, RowRecord.class, LabelSSTRecord.class,
                RKRecord.class, NumberRecord.class, DBCellRecord.class,
                WindowTwoRecord.class, SelectionRecord.class, ContinueRecord.class,
                LabelRecord.class, BlankRecord.class, ColumnInfoRecord.class,
                MulRKRecord.class, MulBlankRecord.class, MergeCellsRecord.class,
                BoolErrRecord.class, ExternSheetRecord.class, NameRecord.class,
                LeftMarginRecord.class, RightMarginRecord.class,
                TopMarginRecord.class, BottomMarginRecord.class,
                PaletteRecord.class, StringRecord.class, RecalcIdRecord.class, SharedFormulaRecord.class,
                DrawingRecord.class, DrawingGroupRecord.class, DrawingSelectionRecord.class,
                ObjRecord.class, TextObjectRecord.class,
                HorizontalPageBreakRecord.class, VerticalPageBreakRecord.class
            };

        }
    }
    private static Map           recordsMap  = recordsToMap(records);

    /**
     * changes the default capacity (10000) to handle larger files
     */

    public static void setCapacity(int capacity)
    {
        NUM_RECORDS = capacity;
    }

    /**
     * Create an array of records from an input stream
     *
     * @param in the InputStream from which the records will be
     *           obtained
     *
     * @return an array of Records created from the InputStream
     *
     * @exception RecordFormatException on error processing the
     *            InputStream
     */

    public static List createRecords(InputStream in)
        throws RecordFormatException
    {
        ArrayList records     = new ArrayList(NUM_RECORDS);
        Record    last_record = null;

        try
        {
            short rectype = 0;

            do
            {
                rectype = LittleEndian.readShort(in);
                if (rectype != 0)
                {
                    short  recsize = LittleEndian.readShort(in);
                    byte[] data    = new byte[ ( int ) recsize ];

                    in.read(data);
                    Record[] recs = createRecord(rectype, recsize,
                                                 data);   // handle MulRK records

                    if (recs.length > 1)
                    {
                        for (int k = 0; k < recs.length; k++)
                        {
                            records.add(
                                recs[ k ]);               // these will be number records
                            last_record =
                                recs[ k ];                // do to keep the algorythm homogenous...you can't
                        }                                 // actually continue a number record anyhow.
                    }
                    else
                    {
                        Record record = recs[ 0 ];

                        if (record != null)
                        {
                            if (rectype == ContinueRecord.sid &&
                                ! (last_record instanceof ContinueRecord) && // include continuation records after
                                ! (last_record instanceof UnknownRecord) )   // unknown records or previous continuation records
                            {
                                if (last_record == null)
                                {
                                    throw new RecordFormatException(
                                        "First record is a ContinueRecord??");
                                }
                                last_record.processContinueRecord(data);
                            }
                            else
                            {
                                last_record = record;
                                records.add(record);
                            }
                        }
                    }
                }
            }
            while (rectype != 0);
        }
        catch (IOException e)
        {
            throw new RecordFormatException("Error reading bytes");
        }

        // Record[] retval = new Record[ records.size() ];
        // retval = ( Record [] ) records.toArray(retval);
        return records;
    }

    public static Record [] createRecord(short rectype, short size,
                                         byte [] data)
    {
        Record   retval     = null;
        Record[] realretval = null;

        try
        {
            Constructor constructor =
                ( Constructor ) recordsMap.get(new Short(rectype));

            if (constructor != null)
            {
                retval = ( Record ) constructor.newInstance(new Object[]
                {
                    new Short(rectype), new Short(size), data
                });
            }
            else
            {
                retval = new UnknownRecord(rectype, size, data);
            }
        }
        catch (Exception introspectionException)
        {
            introspectionException.printStackTrace();
            throw new RecordFormatException(
                "Unable to construct record instance, the following exception occured: " + introspectionException.getMessage());
        }
        if (retval instanceof RKRecord)
        {
            RKRecord     rk  = ( RKRecord ) retval;
            NumberRecord num = new NumberRecord();

            num.setColumn(rk.getColumn());
            num.setRow(rk.getRow());
            num.setXFIndex(rk.getXFIndex());
            num.setValue(rk.getRKNumber());
            retval = num;
        }
        else if (retval instanceof DBCellRecord)
        {
            retval = null;
        }
        else if (retval instanceof MulRKRecord)
        {
            MulRKRecord mrk = ( MulRKRecord ) retval;

            realretval = new Record[ mrk.getNumColumns() ];
            for (int k = 0; k < mrk.getNumColumns(); k++)
            {
                NumberRecord nr = new NumberRecord();

                nr.setColumn(( short ) (k + mrk.getFirstColumn()));
                nr.setRow(mrk.getRow());
                nr.setXFIndex(mrk.getXFAt(k));
                nr.setValue(mrk.getRKNumberAt(k));
                realretval[ k ] = nr;
            }
        }
        else if (retval instanceof MulBlankRecord)
        {
            MulBlankRecord mb = ( MulBlankRecord ) retval;

            realretval = new Record[ mb.getNumColumns() ];
            for (int k = 0; k < mb.getNumColumns(); k++)
            {
                BlankRecord br = new BlankRecord();

                br.setColumn(( short ) (k + mb.getFirstColumn()));
                br.setRow(mb.getRow());
                br.setXFIndex(mb.getXFAt(k));
                realretval[ k ] = br;
            }
        }
        if (realretval == null)
        {
            realretval      = new Record[ 1 ];
            realretval[ 0 ] = retval;
        }
        return realretval;
    }

    public static short [] getAllKnownRecordSIDs()
    {
        short[] results = new short[ recordsMap.size() ];
        int     i       = 0;

        for (Iterator iterator = recordsMap.keySet().iterator();
                iterator.hasNext(); )
        {
            Short sid = ( Short ) iterator.next();

            results[ i++ ] = sid.shortValue();
        }
        return results;
    }

    private static Map recordsToMap(Class [] records)
    {
        Map         result = new HashMap();
        Constructor constructor;

        for (int i = 0; i < records.length; i++)
        {
            Class record = null;
            short sid    = 0;

            record = records[ i ];
            try
            {
                sid         = record.getField("sid").getShort(null);
                constructor = record.getConstructor(new Class[]
                {
                    short.class, short.class, byte [].class
                });
            }
            catch (Exception illegalArgumentException)
            {
                illegalArgumentException.printStackTrace();
                throw new RecordFormatException(
                    "Unable to determine record types");
            }
            result.put(new Short(sid), constructor);
        }
        return result;
    }
}

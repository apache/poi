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

/*
 *  BiffViewer.java
 *
 *  Created on November 13, 2001, 9:23 AM
 */
package org.apache.poi.hssf.dev;

import org.apache.poi.hssf.record.*;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.util.HexDump;
import org.apache.poi.util.LittleEndian;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 *  Utillity for reading in BIFF8 records and displaying data from them.
 *
 *@author     Andrew C. Oliver (acoliver at apache dot org)
 *@author     Glen Stampoultzis (glens at apache.org)
 *@see        #main
 */

public class BiffViewer {
    String filename;
    private boolean dump;


    /**
     *  Creates new BiffViewer
     *
     *@param  args
     */

    public BiffViewer(String[] args) {
        if (args.length > 0) {
            filename = args[0];
        } else {
            System.out.println("BIFFVIEWER REQUIRES A FILENAME***");
        }
    }


    /**
     *  Method run starts up BiffViewer...
     */

    public void run() {
        try {
            POIFSFileSystem fs =
                    new POIFSFileSystem(new FileInputStream(filename));
            InputStream stream =
                    fs.createDocumentInputStream("Workbook");
            createRecords(stream, dump);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     *  Create an array of records from an input stream
     *
     *@param  in                         the InputStream from which the records
     *      will be obtained
     *@param  dump
     *@return                            an array of Records created from the
     *      InputStream
     *@exception  RecordFormatException  on error processing the InputStream
     */

    public static Record[] createRecords(InputStream in, boolean dump)
             throws RecordFormatException {
        ArrayList records = new ArrayList();
//        Record last_record = null;
        int loc = 0;

        RecordDetails activeRecord = null;

        try {
//            long  offset  = 0;
            short rectype = 0;

            do {
                rectype = LittleEndian.readShort(in);
                int startloc = loc;
                loc += 2;
                if (rectype != 0) {
                    short recsize = LittleEndian.readShort(in);

                    loc += 2;
                    byte[] data = new byte[(int) recsize];

                    in.read(data);
                    loc += recsize;
                    Record record = createRecord(rectype, recsize, data );
                    if (record.getSid() != ContinueRecord.sid)
                    {
                        records.add(record);
                        if (activeRecord != null)
                            activeRecord.dump();
                        activeRecord = new RecordDetails(rectype, recsize, startloc, data, record);
                    }
                    else
                    {
                        activeRecord.getRecord().processContinueRecord(data);
                    }
                    if (dump) {
                        dumpRaw(rectype, recsize, data);
                    }
                }
            } while (rectype != 0);

            activeRecord.dump();

        } catch (IOException e) {
            throw new RecordFormatException("Error reading bytes");
        }
        Record[] retval = new Record[records.size()];

        retval = (Record[]) records.toArray(retval);
        return retval;
    }

    private static void dumpNormal(Record record, int startloc, short rectype, short recsize)
    {
        System.out.println("Offset 0x" + Integer.toHexString(startloc) + " (" + startloc + ")");
        System.out.println( "recordid = 0x" + Integer.toHexString( rectype ) + ", size = " + recsize );
        System.out.println( record.toString() );

    }

    private static void dumpContinueRecord(Record last_record, boolean dump, byte[] data) throws IOException {
        if (last_record == null) {
            throw new RecordFormatException(
                    "First record is a ContinueRecord??");
        }
        if (dump) {
            System.out.println(
                    "-----PRECONTINUED LAST RECORD WOULD SERIALIZE LIKE:");
            byte[] lr = last_record.serialize();

            if (lr != null) {
                HexDump.dump(last_record.serialize(),
                        0, System.out, 0);
            }
            System.out.println();
            System.out.println(
                    "-----PRECONTINUED----------------------------------");
        }
        last_record.processContinueRecord(data);
        if (dump) {
            System.out.println(
                    "-----CONTINUED LAST RECORD WOULD SERIALIZE LIKE:");
            HexDump.dump(last_record.serialize(), 0,
                    System.out, 0);
            System.out.println();
            System.out.println(
                    "-----CONTINUED----------------------------------");
        }
    }


    private static void dumpUnknownRecord(byte[] data) throws IOException {
        // record hex dump it!
        System.out.println(
                "-----UNKNOWN----------------------------------");
        if (data.length > 0) {
            HexDump.dump(data, 0, System.out, 0);
        } else {
            System.out.print("**NO RECORD DATA**");
        }
        System.out.println();
        System.out.println(
                "-----UNKNOWN----------------------------------");
    }


    private static void dumpRaw( short rectype, short recsize, byte[] data ) throws IOException
    {
        //                        System.out
        //                            .println("fixing to recordize the following");
        System.out.println("============================================");
        System.out.print( "rectype = 0x"
                + Integer.toHexString( rectype ) );
        System.out.println( ", recsize = 0x"
                + Integer.toHexString( recsize ) );
        System.out.println(
                "-BEGIN DUMP---------------------------------" );
        if ( data.length > 0 )
        {
            HexDump.dump( data, 0, System.out, 0 );
        }
        else
        {
            System.out.println( "**NO RECORD DATA**" );
        }
        //        System.out.println();
        System.out.println(
                "-END DUMP-----------------------------------" );
    }


    /**
     *  Essentially a duplicate of RecordFactory. Kept seperate as not to screw
     *  up non-debug operations.
     *
     */
    private static Record createRecord( short rectype, short size,
                                          byte[] data )
    {
        Record retval = null;

        switch ( rectype )
        {

            case ChartRecord.sid:
                retval = new ChartRecord( rectype, size, data );
                break;
            case ChartFormatRecord.sid:
                retval = new ChartFormatRecord( rectype, size, data );
                break;
            case SeriesRecord.sid:
                retval = new SeriesRecord( rectype, size, data );
                break;
            case BeginRecord.sid:
                retval = new BeginRecord( rectype, size, data );
                break;
            case EndRecord.sid:
                retval = new EndRecord( rectype, size, data );
                break;
            case BOFRecord.sid:
                retval = new BOFRecord( rectype, size, data );
                break;
            case InterfaceHdrRecord.sid:
                retval = new InterfaceHdrRecord( rectype, size, data );
                break;
            case MMSRecord.sid:
                retval = new MMSRecord( rectype, size, data );
                break;
            case InterfaceEndRecord.sid:
                retval = new InterfaceEndRecord( rectype, size, data );
                break;
            case WriteAccessRecord.sid:
                retval = new WriteAccessRecord( rectype, size, data );
                break;
            case CodepageRecord.sid:
                retval = new CodepageRecord( rectype, size, data );
                break;
            case DSFRecord.sid:
                retval = new DSFRecord( rectype, size, data );
                break;
            case TabIdRecord.sid:
                retval = new TabIdRecord( rectype, size, data );
                break;
            case FnGroupCountRecord.sid:
                retval = new FnGroupCountRecord( rectype, size, data );
                break;
            case WindowProtectRecord.sid:
                retval = new WindowProtectRecord( rectype, size, data );
                break;
            case ProtectRecord.sid:
                retval = new ProtectRecord( rectype, size, data );
                break;
            case PasswordRecord.sid:
                retval = new PasswordRecord( rectype, size, data );
                break;
            case ProtectionRev4Record.sid:
                retval = new ProtectionRev4Record( rectype, size, data );
                break;
            case PasswordRev4Record.sid:
                retval = new PasswordRev4Record( rectype, size, data );
                break;
            case WindowOneRecord.sid:
                retval = new WindowOneRecord( rectype, size, data );
                break;
            case BackupRecord.sid:
                retval = new BackupRecord( rectype, size, data );
                break;
            case HideObjRecord.sid:
                retval = new HideObjRecord( rectype, size, data );
                break;
            case DateWindow1904Record.sid:
                retval = new DateWindow1904Record( rectype, size, data );
                break;
            case PrecisionRecord.sid:
                retval = new PrecisionRecord( rectype, size, data );
                break;
            case RefreshAllRecord.sid:
                retval = new RefreshAllRecord( rectype, size, data );
                break;
            case BookBoolRecord.sid:
                retval = new BookBoolRecord( rectype, size, data );
                break;
            case FontRecord.sid:
                retval = new FontRecord( rectype, size, data );
                break;
            case FormatRecord.sid:
                retval = new FormatRecord( rectype, size, data );
                break;
            case ExtendedFormatRecord.sid:
                retval = new ExtendedFormatRecord( rectype, size, data );
                break;
            case StyleRecord.sid:
                retval = new StyleRecord( rectype, size, data );
                break;
            case UseSelFSRecord.sid:
                retval = new UseSelFSRecord( rectype, size, data );
                break;
            case BoundSheetRecord.sid:
                retval = new BoundSheetRecord( rectype, size, data );
                break;
            case CountryRecord.sid:
                retval = new CountryRecord( rectype, size, data );
                break;
            case SSTRecord.sid:
                retval = new SSTRecord( rectype, size, data );
                break;
            case ExtSSTRecord.sid:
                retval = new ExtSSTRecord( rectype, size, data );
                break;
            case EOFRecord.sid:
                retval = new EOFRecord( rectype, size, data );
                break;
            case IndexRecord.sid:
                retval = new IndexRecord( rectype, size, data );
                break;
            case CalcModeRecord.sid:
                retval = new CalcModeRecord( rectype, size, data );
                break;
            case CalcCountRecord.sid:
                retval = new CalcCountRecord( rectype, size, data );
                break;
            case RefModeRecord.sid:
                retval = new RefModeRecord( rectype, size, data );
                break;
            case IterationRecord.sid:
                retval = new IterationRecord( rectype, size, data );
                break;
            case DeltaRecord.sid:
                retval = new DeltaRecord( rectype, size, data );
                break;
            case SaveRecalcRecord.sid:
                retval = new SaveRecalcRecord( rectype, size, data );
                break;
            case PrintHeadersRecord.sid:
                retval = new PrintHeadersRecord( rectype, size, data );
                break;
            case PrintGridlinesRecord.sid:
                retval = new PrintGridlinesRecord( rectype, size, data );
                break;
            case GridsetRecord.sid:
                retval = new GridsetRecord( rectype, size, data );
                break;
            case DrawingGroupRecord.sid:
                retval = new DrawingGroupRecord( rectype, size, data );
                break;
            case DrawingRecordForBiffViewer.sid:
                retval = new DrawingRecordForBiffViewer( rectype, size, data );
                break;
            case DrawingSelectionRecord.sid:
                retval = new DrawingSelectionRecord( rectype, size, data );
                break;
            case GutsRecord.sid:
                retval = new GutsRecord( rectype, size, data );
                break;
            case DefaultRowHeightRecord.sid:
                retval = new DefaultRowHeightRecord( rectype, size, data );
                break;
            case WSBoolRecord.sid:
                retval = new WSBoolRecord( rectype, size, data );
                break;
            case HeaderRecord.sid:
                retval = new HeaderRecord( rectype, size, data );
                break;
            case FooterRecord.sid:
                retval = new FooterRecord( rectype, size, data );
                break;
            case HCenterRecord.sid:
                retval = new HCenterRecord( rectype, size, data );
                break;
            case VCenterRecord.sid:
                retval = new VCenterRecord( rectype, size, data );
                break;
            case PrintSetupRecord.sid:
                retval = new PrintSetupRecord( rectype, size, data );
                break;
            case DefaultColWidthRecord.sid:
                retval = new DefaultColWidthRecord( rectype, size, data );
                break;
            case DimensionsRecord.sid:
                retval = new DimensionsRecord( rectype, size, data );
                break;
            case RowRecord.sid:
                retval = new RowRecord( rectype, size, data );
                break;
            case LabelSSTRecord.sid:
                retval = new LabelSSTRecord( rectype, size, data );
                break;
            case RKRecord.sid:
                retval = new RKRecord( rectype, size, data );
                break;
            case NumberRecord.sid:
                retval = new NumberRecord( rectype, size, data );
                break;
            case DBCellRecord.sid:
                retval = new DBCellRecord( rectype, size, data );
                break;
            case WindowTwoRecord.sid:
                retval = new WindowTwoRecord( rectype, size, data );
                break;
            case SelectionRecord.sid:
                retval = new SelectionRecord( rectype, size, data );
                break;
            case ContinueRecord.sid:
                retval = new ContinueRecord( rectype, size, data );
                break;
            case LabelRecord.sid:
                retval = new LabelRecord( rectype, size, data );
                break;
            case MulRKRecord.sid:
                retval = new MulRKRecord( rectype, size, data );
                break;
            case MulBlankRecord.sid:
                retval = new MulBlankRecord( rectype, size, data );
                break;
            case BlankRecord.sid:
                retval = new BlankRecord( rectype, size, data );
                break;
            case BoolErrRecord.sid:
                retval = new BoolErrRecord( rectype, size, data );
                break;
            case ColumnInfoRecord.sid:
                retval = new ColumnInfoRecord( rectype, size, data );
                break;
            case MergeCellsRecord.sid:
                retval = new MergeCellsRecord( rectype, size, data );
                break;
            case AreaRecord.sid:
                retval = new AreaRecord( rectype, size, data );
                break;
            case DataFormatRecord.sid:
                retval = new DataFormatRecord( rectype, size, data );
                break;
            case BarRecord.sid:
                retval = new BarRecord( rectype, size, data );
                break;
            case DatRecord.sid:
                retval = new DatRecord( rectype, size, data );
                break;
            case PlotGrowthRecord.sid:
                retval = new PlotGrowthRecord( rectype, size, data );
                break;
            case UnitsRecord.sid:
                retval = new UnitsRecord( rectype, size, data );
                break;
            case FrameRecord.sid:
                retval = new FrameRecord( rectype, size, data );
                break;
            case ValueRangeRecord.sid:
                retval = new ValueRangeRecord( rectype, size, data );
                break;
            case SeriesListRecord.sid:
                retval = new SeriesListRecord( rectype, size, data );
                break;
            case FontBasisRecord.sid:
                retval = new FontBasisRecord( rectype, size, data );
                break;
            case FontIndexRecord.sid:
                retval = new FontIndexRecord( rectype, size, data );
                break;
            case LineFormatRecord.sid:
                retval = new LineFormatRecord( rectype, size, data );
                break;
            case AreaFormatRecord.sid:
                retval = new AreaFormatRecord( rectype, size, data );
                break;
            case LinkedDataRecord.sid:
                retval = new LinkedDataRecord( rectype, size, data );
                break;
            case FormulaRecord.sid:
                retval = new FormulaRecord( rectype, size, data );
                break;
            case SheetPropertiesRecord.sid:
                retval = new SheetPropertiesRecord( rectype, size, data );
                break;
            case DefaultDataLabelTextPropertiesRecord.sid:
                retval = new DefaultDataLabelTextPropertiesRecord( rectype, size, data );
                break;
            case TextRecord.sid:
                retval = new TextRecord( rectype, size, data );
                break;
            case AxisParentRecord.sid:
                retval = new AxisParentRecord( rectype, size, data );
                break;
            case AxisLineFormatRecord.sid:
                retval = new AxisLineFormatRecord( rectype, size, data );
                break;
            case SupBookRecord.sid:
                retval = new SupBookRecord( rectype, size, data );
                break;
            case ExternSheetRecord.sid:
                retval = new ExternSheetRecord( rectype, size, data );
                break;
            case SCLRecord.sid:
                retval = new SCLRecord( rectype, size, data );
                break;
            case SeriesToChartGroupRecord.sid:
                retval = new SeriesToChartGroupRecord( rectype, size, data );
                break;
            case AxisUsedRecord.sid:
                retval = new AxisUsedRecord( rectype, size, data );
                break;
            case AxisRecord.sid:
                retval = new AxisRecord( rectype, size, data );
                break;
            case CategorySeriesAxisRecord.sid:
                retval = new CategorySeriesAxisRecord( rectype, size, data );
                break;
            case AxisOptionsRecord.sid:
                retval = new AxisOptionsRecord( rectype, size, data );
                break;
            case TickRecord.sid:
                retval = new TickRecord( rectype, size, data );
                break;
            case SeriesTextRecord.sid:
                retval = new SeriesTextRecord( rectype, size, data );
                break;
            case ObjectLinkRecord.sid:
                retval = new ObjectLinkRecord( rectype, size, data );
                break;
            case PlotAreaRecord.sid:
                retval = new PlotAreaRecord( rectype, size, data );
                break;
            case SeriesIndexRecord.sid:
                retval = new SeriesIndexRecord( rectype, size, data );
                break;
            case LegendRecord.sid:
                retval = new LegendRecord( rectype, size, data );
                break;
            case LeftMarginRecord.sid:
                retval = new LeftMarginRecord( rectype, size, data );
                break;
            case RightMarginRecord.sid:
                retval = new RightMarginRecord( rectype, size, data );
                break;
            case TopMarginRecord.sid:
                retval = new TopMarginRecord( rectype, size, data );
                break;
            case BottomMarginRecord.sid:
                retval = new BottomMarginRecord( rectype, size, data );
                break;
            case PaletteRecord.sid:
                retval = new PaletteRecord( rectype, size, data );
                break;
            case StringRecord.sid:
                retval = new StringRecord( rectype, size, data );
                break;
            case NameRecord.sid:
                retval = new NameRecord( rectype, size, data );
                break;
            case PaneRecord.sid:
                retval = new PaneRecord( rectype, size, data );
                break;
            case SharedFormulaRecord.sid:
            	 retval = new SharedFormulaRecord( rectype, size, data);
            	 break;
            case ObjRecord.sid:
            	 retval = new ObjRecord( rectype, size, data);
            	 break;
            case TextObjectRecord.sid:
            	 retval = new TextObjectRecord( rectype, size, data);
            	 break;
            case HorizontalPageBreakRecord.sid:
                retval = new HorizontalPageBreakRecord( rectype, size, data);
                break;
            case VerticalPageBreakRecord.sid:
                retval = new VerticalPageBreakRecord( rectype, size, data);
                break;
            default:
                retval = new UnknownRecord( rectype, size, data );
        }
        return retval;
    }


    /**
     *  Method setDump - hex dump out data or not.
     *
     *@param  dump
     */

    public void setDump(boolean dump) {
        this.dump = dump;
    }


    /**
     *  Method main with 1 argument just run straight biffview against given
     *  file<P>
     *
     *  with 2 arguments where the second argument is "on" - run biffviewer<P>
     *
     *  with hex dumps of records <P>
     *
     *  with 2 arguments where the second argument is "bfd" just run a big fat
     *  hex dump of the file...don't worry about biffviewing it at all
     *
     *@param  args
     */

    public static void main(String[] args) {
        try {
            System.setProperty("poi.deserialize.escher", "true");
            BiffViewer viewer = new BiffViewer(args);

            if ((args.length > 1) && args[1].equals("on")) {
                viewer.setDump(true);
            }
            if ((args.length > 1) && args[1].equals("bfd")) {
                POIFSFileSystem fs =
                        new POIFSFileSystem(new FileInputStream(args[0]));
                InputStream stream =
                        fs.createDocumentInputStream("Workbook");
                int size = stream.available();
                byte[] data = new byte[size];

                stream.read(data);
                HexDump.dump(data, 0, System.out, 0);
            } else {
                viewer.run();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static class RecordDetails
    {
        short rectype, recsize;
        int startloc;
        byte[] data;
        Record record;

        public RecordDetails( short rectype, short recsize, int startloc, byte[] data, Record record )
        {
            this.rectype = rectype;
            this.recsize = recsize;
            this.startloc = startloc;
            this.data = data;
            this.record = record;
        }

        public short getRectype()
        {
            return rectype;
        }

        public short getRecsize()
        {
            return recsize;
        }

        public byte[] getData()
        {
            return data;
        }

        public Record getRecord()
        {
            return record;
        }

        public void dump() throws IOException
        {
            if (record instanceof UnknownRecord)
                dumpUnknownRecord(data);
            else
                dumpNormal(record, startloc, rectype, recsize);
        }
    }

}


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

/*
 *  BiffViewer.java
 *
 *  Created on November 13, 2001, 9:23 AM
 */
package org.apache.poi.hssf.dev;

import org.apache.poi.hssf.record.*;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.util.HexDump;

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
        RecordDetails activeRecord = null;

        try {
          BiffviewRecordInputStream recStream = new BiffviewRecordInputStream(in);
          while (recStream.hasNextRecord()) {
            recStream.nextRecord();
            if (recStream.getSid() != 0) {
              Record record = createRecord (recStream);
                    if (record.getSid() != ContinueRecord.sid)
                    {
                        records.add(record);
                        if (activeRecord != null)
                            activeRecord.dump();
                  activeRecord = new RecordDetails(recStream.getSid(), recStream.getLength(), (int)recStream.getPos(), record);
                    }
                    if (dump) {
                recStream.dumpBytes();
              }
                    }
                }
            activeRecord.dump();
        } catch (IOException e) {
            throw new RecordFormatException("Error reading bytes", e);
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

    /**
     *  Essentially a duplicate of RecordFactory. Kept seperate as not to screw
     *  up non-debug operations.
     *
     */
    private static Record createRecord( RecordInputStream in )
    {
        Record retval = null;

        switch ( in.getSid() )
        {

            case ChartRecord.sid:
                retval = new ChartRecord( in );
                break;
            case ChartFormatRecord.sid:
                retval = new ChartFormatRecord( in );
                break;
            case SeriesRecord.sid:
                retval = new SeriesRecord( in );
                break;
            case BeginRecord.sid:
                retval = new BeginRecord( in );
                break;
            case EndRecord.sid:
                retval = new EndRecord( in );
                break;
            case BOFRecord.sid:
                retval = new BOFRecord( in );
                break;
            case InterfaceHdrRecord.sid:
                retval = new InterfaceHdrRecord( in );
                break;
            case MMSRecord.sid:
                retval = new MMSRecord( in );
                break;
            case InterfaceEndRecord.sid:
                retval = new InterfaceEndRecord( in );
                break;
            case WriteAccessRecord.sid:
                retval = new WriteAccessRecord( in );
                break;
            case CodepageRecord.sid:
                retval = new CodepageRecord( in );
                break;
            case DSFRecord.sid:
                retval = new DSFRecord( in );
                break;
            case TabIdRecord.sid:
                retval = new TabIdRecord( in );
                break;
            case FnGroupCountRecord.sid:
                retval = new FnGroupCountRecord( in );
                break;
            case WindowProtectRecord.sid:
                retval = new WindowProtectRecord( in );
                break;
            case ProtectRecord.sid:
                retval = new ProtectRecord( in );
                break;
            case PasswordRecord.sid:
                retval = new PasswordRecord( in );
                break;
            case ProtectionRev4Record.sid:
                retval = new ProtectionRev4Record( in );
                break;
            case PasswordRev4Record.sid:
                retval = new PasswordRev4Record( in );
                break;
            case WindowOneRecord.sid:
                retval = new WindowOneRecord( in );
                break;
            case BackupRecord.sid:
                retval = new BackupRecord( in );
                break;
            case HideObjRecord.sid:
                retval = new HideObjRecord( in );
                break;
            case DateWindow1904Record.sid:
                retval = new DateWindow1904Record( in );
                break;
            case PrecisionRecord.sid:
                retval = new PrecisionRecord( in );
                break;
            case RefreshAllRecord.sid:
                retval = new RefreshAllRecord( in );
                break;
            case BookBoolRecord.sid:
                retval = new BookBoolRecord( in );
                break;
            case FontRecord.sid:
                retval = new FontRecord( in );
                break;
            case FormatRecord.sid:
                retval = new FormatRecord( in );
                break;
            case ExtendedFormatRecord.sid:
                retval = new ExtendedFormatRecord( in );
                break;
            case StyleRecord.sid:
                retval = new StyleRecord( in );
                break;
            case UseSelFSRecord.sid:
                retval = new UseSelFSRecord( in );
                break;
            case BoundSheetRecord.sid:
                retval = new BoundSheetRecord( in );
                break;
            case CountryRecord.sid:
                retval = new CountryRecord( in );
                break;
            case SSTRecord.sid:
                retval = new SSTRecord( in );
                break;
            case ExtSSTRecord.sid:
                retval = new ExtSSTRecord( in );
                break;
            case EOFRecord.sid:
                retval = new EOFRecord( in );
                break;
            case IndexRecord.sid:
                retval = new IndexRecord( in );
                break;
            case CalcModeRecord.sid:
                retval = new CalcModeRecord( in );
                break;
            case CalcCountRecord.sid:
                retval = new CalcCountRecord( in );
                break;
            case RefModeRecord.sid:
                retval = new RefModeRecord( in );
                break;
            case IterationRecord.sid:
                retval = new IterationRecord( in );
                break;
            case DeltaRecord.sid:
                retval = new DeltaRecord( in );
                break;
            case SaveRecalcRecord.sid:
                retval = new SaveRecalcRecord( in );
                break;
            case PrintHeadersRecord.sid:
                retval = new PrintHeadersRecord( in );
                break;
            case PrintGridlinesRecord.sid:
                retval = new PrintGridlinesRecord( in );
                break;
            case GridsetRecord.sid:
                retval = new GridsetRecord( in );
                break;
            case DrawingGroupRecord.sid:
                retval = new DrawingGroupRecord( in );
                break;
            case DrawingRecordForBiffViewer.sid:
                retval = new DrawingRecordForBiffViewer( in );
                break;
            case DrawingSelectionRecord.sid:
                retval = new DrawingSelectionRecord( in );
                break;
            case GutsRecord.sid:
                retval = new GutsRecord( in );
                break;
            case DefaultRowHeightRecord.sid:
                retval = new DefaultRowHeightRecord( in );
                break;
            case WSBoolRecord.sid:
                retval = new WSBoolRecord( in );
                break;
            case HeaderRecord.sid:
                retval = new HeaderRecord( in );
                break;
            case FooterRecord.sid:
                retval = new FooterRecord( in );
                break;
            case HCenterRecord.sid:
                retval = new HCenterRecord( in );
                break;
            case VCenterRecord.sid:
                retval = new VCenterRecord( in );
                break;
            case PrintSetupRecord.sid:
                retval = new PrintSetupRecord( in );
                break;
            case DefaultColWidthRecord.sid:
                retval = new DefaultColWidthRecord( in );
                break;
            case DimensionsRecord.sid:
                retval = new DimensionsRecord( in );
                break;
            case RowRecord.sid:
                retval = new RowRecord( in );
                break;
            case LabelSSTRecord.sid:
                retval = new LabelSSTRecord( in );
                break;
            case RKRecord.sid:
                retval = new RKRecord( in );
                break;
            case NumberRecord.sid:
                retval = new NumberRecord( in );
                break;
            case DBCellRecord.sid:
                retval = new DBCellRecord( in );
                break;
            case WindowTwoRecord.sid:
                retval = new WindowTwoRecord( in );
                break;
            case SelectionRecord.sid:
                retval = new SelectionRecord( in );
                break;
            case ContinueRecord.sid:
                retval = new ContinueRecord( in );
                break;
            case LabelRecord.sid:
                retval = new LabelRecord( in );
                break;
            case MulRKRecord.sid:
                retval = new MulRKRecord( in );
                break;
            case MulBlankRecord.sid:
                retval = new MulBlankRecord( in );
                break;
            case BlankRecord.sid:
                retval = new BlankRecord( in );
                break;
            case BoolErrRecord.sid:
                retval = new BoolErrRecord( in );
                break;
            case ColumnInfoRecord.sid:
                retval = new ColumnInfoRecord( in );
                break;
            case MergeCellsRecord.sid:
                retval = new MergeCellsRecord( in );
                break;
            case AreaRecord.sid:
                retval = new AreaRecord( in );
                break;
            case DataFormatRecord.sid:
                retval = new DataFormatRecord( in );
                break;
            case BarRecord.sid:
                retval = new BarRecord( in );
                break;
            case DatRecord.sid:
                retval = new DatRecord( in );
                break;
            case PlotGrowthRecord.sid:
                retval = new PlotGrowthRecord( in );
                break;
            case UnitsRecord.sid:
                retval = new UnitsRecord( in );
                break;
            case FrameRecord.sid:
                retval = new FrameRecord( in );
                break;
            case ValueRangeRecord.sid:
                retval = new ValueRangeRecord( in );
                break;
            case SeriesListRecord.sid:
                retval = new SeriesListRecord( in );
                break;
            case FontBasisRecord.sid:
                retval = new FontBasisRecord( in );
                break;
            case FontIndexRecord.sid:
                retval = new FontIndexRecord( in );
                break;
            case LineFormatRecord.sid:
                retval = new LineFormatRecord( in );
                break;
            case AreaFormatRecord.sid:
                retval = new AreaFormatRecord( in );
                break;
            case LinkedDataRecord.sid:
                retval = new LinkedDataRecord( in );
                break;
            case FormulaRecord.sid:
                retval = new FormulaRecord( in );
                break;
            case SheetPropertiesRecord.sid:
                retval = new SheetPropertiesRecord( in );
                break;
            case DefaultDataLabelTextPropertiesRecord.sid:
                retval = new DefaultDataLabelTextPropertiesRecord( in );
                break;
            case TextRecord.sid:
                retval = new TextRecord( in );
                break;
            case AxisParentRecord.sid:
                retval = new AxisParentRecord( in );
                break;
            case AxisLineFormatRecord.sid:
                retval = new AxisLineFormatRecord( in );
                break;
            case SupBookRecord.sid:
                retval = new SupBookRecord( in );
                break;
            case ExternSheetRecord.sid:
                retval = new ExternSheetRecord( in );
                break;
            case SCLRecord.sid:
                retval = new SCLRecord( in );
                break;
            case SeriesToChartGroupRecord.sid:
                retval = new SeriesToChartGroupRecord( in );
                break;
            case AxisUsedRecord.sid:
                retval = new AxisUsedRecord( in );
                break;
            case AxisRecord.sid:
                retval = new AxisRecord( in );
                break;
            case CategorySeriesAxisRecord.sid:
                retval = new CategorySeriesAxisRecord( in );
                break;
            case AxisOptionsRecord.sid:
                retval = new AxisOptionsRecord( in );
                break;
            case TickRecord.sid:
                retval = new TickRecord( in );
                break;
            case SeriesTextRecord.sid:
                retval = new SeriesTextRecord( in );
                break;
            case ObjectLinkRecord.sid:
                retval = new ObjectLinkRecord( in );
                break;
            case PlotAreaRecord.sid:
                retval = new PlotAreaRecord( in );
                break;
            case SeriesIndexRecord.sid:
                retval = new SeriesIndexRecord( in );
                break;
            case LegendRecord.sid:
                retval = new LegendRecord( in );
                break;
            case LeftMarginRecord.sid:
                retval = new LeftMarginRecord( in );
                break;
            case RightMarginRecord.sid:
                retval = new RightMarginRecord( in );
                break;
            case TopMarginRecord.sid:
                retval = new TopMarginRecord( in );
                break;
            case BottomMarginRecord.sid:
                retval = new BottomMarginRecord( in );
                break;
            case PaletteRecord.sid:
                retval = new PaletteRecord( in );
                break;
            case StringRecord.sid:
                retval = new StringRecord( in );
                break;
            case NameRecord.sid:
                retval = new NameRecord( in );
                break;
            case PaneRecord.sid:
                retval = new PaneRecord( in );
                break;
            case SharedFormulaRecord.sid:
            	 retval = new SharedFormulaRecord( in);
            	 break;
            case ObjRecord.sid:
            	 retval = new ObjRecord( in);
            	 break;
            case TextObjectRecord.sid:
            	 retval = new TextObjectRecord( in);
            	 break;
            case HorizontalPageBreakRecord.sid:
                retval = new HorizontalPageBreakRecord( in);
                break;
            case VerticalPageBreakRecord.sid:
                retval = new VerticalPageBreakRecord( in);
                break;
            case WriteProtectRecord.sid:
            	retval = new WriteProtectRecord( in);
            	break;
            case FilePassRecord.sid:
            	retval = new FilePassRecord(in);
            	break;
            case NoteRecord.sid:
                retval = new NoteRecord( in );
                break;
            case FileSharingRecord.sid:
                retval = new FileSharingRecord( in );
                break;
            case HyperlinkRecord.sid:
                retval = new HyperlinkRecord( in );
                break;
            default:
                retval = new UnknownRecord( in );
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
     * Method main with 1 argument just run straight biffview against given
     * file<P>
     *
     * with 2 arguments where the second argument is "on" - run biffviewer<P>
     *
     * with hex dumps of records <P>
     *
     * with 2 arguments where the second argument is "bfd" just run a big fat
     * hex dump of the file...don't worry about biffviewing it at all
     * <p>
     * Define the system property <code>poi.deserialize.escher</code> to turn on
     * deserialization of escher records.
     *
     */
    public static void main(String[] args) {
        try {
        	System.setProperty("poi.deserialize.escher", "true");

            if (args.length == 0)
            {
                System.out.println( "Biff viewer needs a filename" );
            }
            else
            {
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
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This record supports dumping of completed continue records.
     */
    static class RecordDetails
    {
        short rectype, recsize;
        int startloc;
        Record record;

        public RecordDetails( short rectype, short recsize, int startloc, Record record )
        {
            this.rectype = rectype;
            this.recsize = recsize;
            this.startloc = startloc;
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

        public Record getRecord()
        {
            return record;
        }

        public void dump() throws IOException
        {
                dumpNormal(record, startloc, rectype, recsize);
        }
    }

    static class BiffviewRecordInputStream extends RecordInputStream {
      public BiffviewRecordInputStream(InputStream in) {
        super(in);
      }
      public void dumpBytes() {
        HexDump.dump(this.data, 0, this.currentLength);
      }
    }

}


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

package org.apache.poi.hssf.dev;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

import org.apache.poi.hssf.record.*;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.util.HexDump;

/**
 *  Utillity for reading in BIFF8 records and displaying data from them.
 *
 *@author     Andrew C. Oliver (acoliver at apache dot org)
 *@author     Glen Stampoultzis (glens at apache.org)
 *@see        #main
 */
public final class BiffViewer {
    private final File _inputFile;
    private boolean dump;
    private final PrintStream _ps;


    public BiffViewer(File inFile, PrintStream ps) {
        _inputFile = inFile;
        _ps = ps;
    }


    /**
     *  Method run starts up BiffViewer...
     */
    public void run() {
        try {
            POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(_inputFile));
            InputStream stream = fs.createDocumentInputStream("Workbook");
            createRecords(stream, dump, _ps);
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
    public static Record[] createRecords(InputStream in, boolean dump, PrintStream ps)
             throws RecordFormatException {
        ArrayList records = new ArrayList();
        RecordDetails activeRecord = null;

        BiffviewRecordInputStream recStream = new BiffviewRecordInputStream(in);
        while (recStream.hasNextRecord()) {
            recStream.nextRecord();
            if (recStream.getSid() != 0) {
                Record record = createRecord (recStream);
                    if (record.getSid() != ContinueRecord.sid)
                    {
                        records.add(record);
                        if (activeRecord != null)
                            activeRecord.dump(ps);
                        int startPos = (int)(recStream.getPos()-recStream.getLength() - 4);
                        activeRecord = new RecordDetails(recStream.getSid(), recStream.getLength(), startPos, record);
                    }
                    if (dump) {
                        recStream.dumpBytes(ps);
                    }
              }
        }
        if (activeRecord != null) {
            activeRecord.dump(ps);
        }
        Record[] retval = new Record[records.size()];
        records.toArray(retval);
        return retval;
    }


    /**
     *  Essentially a duplicate of RecordFactory. Kept separate as not to screw
     *  up non-debug operations.
     *
     */
    private static Record createRecord( RecordInputStream in )
    {
        switch ( in.getSid() )
        {
            case ChartRecord.sid:
                return new ChartRecord( in );
            case ChartFormatRecord.sid:
                return new ChartFormatRecord( in );
            case SeriesRecord.sid:
                return new SeriesRecord( in );
            case BeginRecord.sid:
                return new BeginRecord( in );
            case EndRecord.sid:
                return new EndRecord( in );
            case BOFRecord.sid:
                return new BOFRecord( in );
            case InterfaceHdrRecord.sid:
                return new InterfaceHdrRecord( in );
            case MMSRecord.sid:
                return new MMSRecord( in );
            case InterfaceEndRecord.sid:
                return new InterfaceEndRecord( in );
            case WriteAccessRecord.sid:
                return new WriteAccessRecord( in );
            case CodepageRecord.sid:
                return new CodepageRecord( in );
            case DSFRecord.sid:
                return new DSFRecord( in );
            case TabIdRecord.sid:
                return new TabIdRecord( in );
            case FnGroupCountRecord.sid:
                return new FnGroupCountRecord( in );
            case WindowProtectRecord.sid:
                return new WindowProtectRecord( in );
            case ProtectRecord.sid:
                return new ProtectRecord( in );
            case PasswordRecord.sid:
                return new PasswordRecord( in );
            case ProtectionRev4Record.sid:
                return new ProtectionRev4Record( in );
            case PasswordRev4Record.sid:
                return new PasswordRev4Record( in );
            case WindowOneRecord.sid:
                return new WindowOneRecord( in );
            case BackupRecord.sid:
                return new BackupRecord( in );
            case HideObjRecord.sid:
                return new HideObjRecord( in );
            case DateWindow1904Record.sid:
                return new DateWindow1904Record( in );
            case PrecisionRecord.sid:
                return new PrecisionRecord( in );
            case RefreshAllRecord.sid:
                return new RefreshAllRecord( in );
            case BookBoolRecord.sid:
                return new BookBoolRecord( in );
            case FontRecord.sid:
                return new FontRecord( in );
            case FormatRecord.sid:
                return new FormatRecord( in );
            case ExtendedFormatRecord.sid:
                return new ExtendedFormatRecord( in );
            case StyleRecord.sid:
                return new StyleRecord( in );
            case UseSelFSRecord.sid:
                return new UseSelFSRecord( in );
            case BoundSheetRecord.sid:
                return new BoundSheetRecord( in );
            case CountryRecord.sid:
                return new CountryRecord( in );
            case SSTRecord.sid:
                return new SSTRecord( in );
            case ExtSSTRecord.sid:
                return new ExtSSTRecord( in );
            case EOFRecord.sid:
                return new EOFRecord( in );
            case IndexRecord.sid:
                return new IndexRecord( in );
            case CalcModeRecord.sid:
                return new CalcModeRecord( in );
            case CalcCountRecord.sid:
                return new CalcCountRecord( in );
            case RefModeRecord.sid:
                return new RefModeRecord( in );
            case IterationRecord.sid:
                return new IterationRecord( in );
            case DeltaRecord.sid:
                return new DeltaRecord( in );
            case SaveRecalcRecord.sid:
                return new SaveRecalcRecord( in );
            case PrintHeadersRecord.sid:
                return new PrintHeadersRecord( in );
            case PrintGridlinesRecord.sid:
                return new PrintGridlinesRecord( in );
            case GridsetRecord.sid:
                return new GridsetRecord( in );
            case DrawingGroupRecord.sid:
                return new DrawingGroupRecord( in );
            case DrawingRecordForBiffViewer.sid:
                return new DrawingRecordForBiffViewer( in );
            case DrawingSelectionRecord.sid:
                return new DrawingSelectionRecord( in );
            case GutsRecord.sid:
                return new GutsRecord( in );
            case DefaultRowHeightRecord.sid:
                return new DefaultRowHeightRecord( in );
            case WSBoolRecord.sid:
                return new WSBoolRecord( in );
            case HeaderRecord.sid:
                return new HeaderRecord( in );
            case FooterRecord.sid:
                return new FooterRecord( in );
            case HCenterRecord.sid:
                return new HCenterRecord( in );
            case VCenterRecord.sid:
                return new VCenterRecord( in );
            case PrintSetupRecord.sid:
                return new PrintSetupRecord( in );
            case DefaultColWidthRecord.sid:
                return new DefaultColWidthRecord( in );
            case DimensionsRecord.sid:
                return new DimensionsRecord( in );
            case RowRecord.sid:
                return new RowRecord( in );
            case LabelSSTRecord.sid:
                return new LabelSSTRecord( in );
            case RKRecord.sid:
                return new RKRecord( in );
            case NumberRecord.sid:
                return new NumberRecord( in );
            case DBCellRecord.sid:
                return new DBCellRecord( in );
            case WindowTwoRecord.sid:
                return new WindowTwoRecord( in );
            case SelectionRecord.sid:
                return new SelectionRecord( in );
            case ContinueRecord.sid:
                return new ContinueRecord( in );
            case LabelRecord.sid:
                return new LabelRecord( in );
            case MulRKRecord.sid:
                return new MulRKRecord( in );
            case MulBlankRecord.sid:
                return new MulBlankRecord( in );
            case BlankRecord.sid:
                return new BlankRecord( in );
            case BoolErrRecord.sid:
                return new BoolErrRecord( in );
            case ColumnInfoRecord.sid:
                return new ColumnInfoRecord( in );
            case MergeCellsRecord.sid:
                return new MergeCellsRecord( in );
            case AreaRecord.sid:
                return new AreaRecord( in );
            case DataFormatRecord.sid:
                return new DataFormatRecord( in );
            case BarRecord.sid:
                return new BarRecord( in );
            case DatRecord.sid:
                return new DatRecord( in );
            case PlotGrowthRecord.sid:
                return new PlotGrowthRecord( in );
            case UnitsRecord.sid:
                return new UnitsRecord( in );
            case FrameRecord.sid:
                return new FrameRecord( in );
            case ValueRangeRecord.sid:
                return new ValueRangeRecord( in );
            case SeriesListRecord.sid:
                return new SeriesListRecord( in );
            case FontBasisRecord.sid:
                return new FontBasisRecord( in );
            case FontIndexRecord.sid:
                return new FontIndexRecord( in );
            case LineFormatRecord.sid:
                return new LineFormatRecord( in );
            case AreaFormatRecord.sid:
                return new AreaFormatRecord( in );
            case LinkedDataRecord.sid:
                return new LinkedDataRecord( in );
            case FormulaRecord.sid:
                return new FormulaRecord( in );
            case SheetPropertiesRecord.sid:
                return new SheetPropertiesRecord( in );
            case DefaultDataLabelTextPropertiesRecord.sid:
                return new DefaultDataLabelTextPropertiesRecord( in );
            case TextRecord.sid:
                return new TextRecord( in );
            case AxisParentRecord.sid:
                return new AxisParentRecord( in );
            case AxisLineFormatRecord.sid:
                return new AxisLineFormatRecord( in );
            case SupBookRecord.sid:
                return new SupBookRecord( in );
            case ExternSheetRecord.sid:
                return new ExternSheetRecord( in );
            case SCLRecord.sid:
                return new SCLRecord( in );
            case SeriesToChartGroupRecord.sid:
                return new SeriesToChartGroupRecord( in );
            case AxisUsedRecord.sid:
                return new AxisUsedRecord( in );
            case AxisRecord.sid:
                return new AxisRecord( in );
            case CategorySeriesAxisRecord.sid:
                return new CategorySeriesAxisRecord( in );
            case AxisOptionsRecord.sid:
                return new AxisOptionsRecord( in );
            case TickRecord.sid:
                return new TickRecord( in );
            case SeriesTextRecord.sid:
                return new SeriesTextRecord( in );
            case ObjectLinkRecord.sid:
                return new ObjectLinkRecord( in );
            case PlotAreaRecord.sid:
                return new PlotAreaRecord( in );
            case SeriesIndexRecord.sid:
                return new SeriesIndexRecord( in );
            case LegendRecord.sid:
                return new LegendRecord( in );
            case LeftMarginRecord.sid:
                return new LeftMarginRecord( in );
            case RightMarginRecord.sid:
                return new RightMarginRecord( in );
            case TopMarginRecord.sid:
                return new TopMarginRecord( in );
            case BottomMarginRecord.sid:
                return new BottomMarginRecord( in );
            case PaletteRecord.sid:
                return new PaletteRecord( in );
            case StringRecord.sid:
                return new StringRecord( in );
            case NameRecord.sid:
                return new NameRecord( in );
            case PaneRecord.sid:
                return new PaneRecord( in );
            case SharedFormulaRecord.sid:
            	 return new SharedFormulaRecord( in);
            case ObjRecord.sid:
            	 return new ObjRecord( in);
            case TextObjectRecord.sid:
            	 return new TextObjectRecord( in);
            case HorizontalPageBreakRecord.sid:
                return new HorizontalPageBreakRecord( in);
            case VerticalPageBreakRecord.sid:
                return new VerticalPageBreakRecord( in);
            case WriteProtectRecord.sid:
            	return new WriteProtectRecord( in);
            case FilePassRecord.sid:
            	return new FilePassRecord(in);
            case NoteRecord.sid:
                return new NoteRecord( in );
            case FileSharingRecord.sid:
                return new FileSharingRecord( in );
            case HyperlinkRecord.sid:
                return new HyperlinkRecord( in );
            case TableRecord.sid:
            	return new TableRecord( in );
        }
        return new UnknownRecord( in );
    }


    /**
     *  Method setDump - hex dump out data or not.
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

        System.setProperty("poi.deserialize.escher", "true");

        if (args.length == 0) {
            System.out.println( "Biff viewer needs a filename" );
            return;
        }

        try {
            String inFileName = args[0];
            File inputFile = new File(inFileName);
            if(!inputFile.exists()) {
                throw new RuntimeException("specified inputFile '" + inFileName + "' does not exist");
            }
            PrintStream ps;
            if (false) { // set to true to output to file
            	OutputStream os = new FileOutputStream(inFileName + ".out");
            	ps = new PrintStream(os);
            } else {
            	ps = System.out;
            }
            BiffViewer viewer = new BiffViewer(inputFile, ps);

            if (args.length > 1 && args[1].equals("on")) {
                viewer.setDump(true);
            }
            if (args.length > 1 && args[1].equals("bfd")) {
                POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(inputFile));
                InputStream stream = fs.createDocumentInputStream("Workbook");
                int size = stream.available();
                byte[] data = new byte[size];

                stream.read(data);
                HexDump.dump(data, 0, System.out, 0);
            } else {
                viewer.run();
            }
            ps.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This record supports dumping of completed continue records.
     */
    private static final class RecordDetails
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

        public void dump(PrintStream ps) {
            ps.println("Offset 0x" + Integer.toHexString(startloc) + " (" + startloc + ")");
            ps.println( "recordid = 0x" + Integer.toHexString( rectype ) + ", size = " + recsize );
            ps.println( record.toString() );
        }
    }

    private static final class BiffviewRecordInputStream extends RecordInputStream {
      public BiffviewRecordInputStream(InputStream in) {
        super(in);
      }
      public void dumpBytes(PrintStream ps) {
        ps.println(HexDump.dump(this.data, 0, this.currentLength));
      }
    }

}


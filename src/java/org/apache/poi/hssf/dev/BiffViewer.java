/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache POI" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache POI", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

/*
 * BiffViewer.java
 *
 * Created on November 13, 2001, 9:23 AM
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
 * Utillity for reading in BIFF8 records and displaying data from them.
 * @author  Andrew C. Oliver (acoliver at apache dot org)
 * @author Glen Stampoultzis (glens at apache.org)
 * @see #main
 */

public class BiffViewer
{
    String filename;
    private boolean dump;

    /**
     * Creates new BiffViewer
     *
     * @param args
     */

    public BiffViewer(String[] args)
    {
        if (args.length > 0)
        {
            filename = args[0];
        } else
        {
            System.out.println("BIFFVIEWER REQUIRES A FILENAME***");
        }
    }

    /**
     * Method run
     *
     * starts up BiffViewer...
     */

    public void run()
    {
        try
        {
            POIFSFileSystem fs =
                    new POIFSFileSystem(new FileInputStream(filename));
            InputStream stream =
                    fs.createDocumentInputStream("Workbook");
            Record[] records = createRecords(stream, dump);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Create an array of records from an input stream
     *
     * @param in the InputStream from which the records will be
     *           obtained
     * @param dump
     *
     * @return an array of Records created from the InputStream
     *
     * @exception RecordFormatException on error processing the
     *            InputStream
     */

    public static Record[] createRecords(InputStream in, boolean dump)
            throws RecordFormatException
    {
        ArrayList records = new ArrayList();
        Record last_record = null;
        int loc = 0;

        try
        {
//            long  offset  = 0;
            short rectype = 0;

            do
            {
                rectype = LittleEndian.readShort(in);
                System.out.println("============================================");
                System.out.println("Offset 0x" + Integer.toHexString(loc) + " (" + loc + ")");
                loc += 2;
                if (rectype != 0)
                {
                    short recsize = LittleEndian.readShort(in);

                    loc += 2;
                    byte[] data = new byte[(int) recsize];

                    in.read(data);
                    if ((rectype == WSBoolRecord.sid) && (recsize == 0))
                    {
                        System.out.println(loc);
                    }
                    loc += recsize;
//                    offset += 4 + recsize;
                    if (dump)
                    {
                        dump(rectype, recsize, data);
                    }
                    Record[] recs = createRecord(rectype, recsize,
                            data);   // handle MulRK records

                    Record record = recs[0];

                    if ((record instanceof UnknownRecord)
                            && !dump)                 // if we didn't already dump
                    {                                 // just cause dump was on and we're hit an unknow
                        dumpUnknownRecord(data);
                    }
                    if (record != null)
                    {
                        if (rectype == ContinueRecord.sid)
                        {
                            dumpContinueRecord(last_record, dump, data);
                        } else
                        {
                            last_record = record;
                            records.add(record);
                        }
                    }
                }
            } while (rectype != 0);
        } catch (IOException e)
        {
            throw new RecordFormatException("Error reading bytes");
        }
        Record[] retval = new Record[records.size()];

        retval = (Record[]) records.toArray(retval);
        return retval;
    }

    private static void dumpContinueRecord(Record last_record, boolean dump, byte[] data) throws IOException
    {
        if (last_record == null)
        {
            throw new RecordFormatException(
                    "First record is a ContinueRecord??");
        }
        if (dump)
        {
            System.out.println(
                    "-----PRECONTINUED LAST RECORD WOULD SERIALIZE LIKE:");
            byte[] lr = last_record.serialize();

            if (lr != null)
            {
                HexDump.dump(last_record.serialize(),
                        0, System.out, 0);
            }
            System.out.println();
            System.out.println(
                    "-----PRECONTINUED----------------------------------");
        }
        last_record.processContinueRecord(data);
        if (dump)
        {
            System.out.println(
                    "-----CONTINUED LAST RECORD WOULD SERIALIZE LIKE:");
            HexDump.dump(last_record.serialize(), 0,
                    System.out, 0);
            System.out.println();
            System.out.println(
                    "-----CONTINUED----------------------------------");
        }
    }

    private static void dumpUnknownRecord(byte[] data) throws IOException
    {
        // record hex dump it!
        System.out.println(
                "-----UNKNOWN----------------------------------");
        if (data.length > 0)
        {
            HexDump.dump(data, 0, System.out, 0);
        } else
        {
            System.out.print("**NO RECORD DATA**");
        }
        System.out.println();
        System.out.println(
                "-----UNKNOWN----------------------------------");
    }

    private static void dump(short rectype, short recsize, byte[] data) throws IOException
    {
//                        System.out
//                            .println("fixing to recordize the following");
        System.out.print("rectype = 0x"
                + Integer.toHexString(rectype));
        System.out.println(", recsize = 0x"
                + Integer.toHexString(recsize));
        System.out.println(
                "-BEGIN DUMP---------------------------------");
        if (data.length > 0)
        {
            HexDump.dump(data, 0, System.out, 0);
        } else
        {
            System.out.println("**NO RECORD DATA**");
        }
//        System.out.println();
        System.out.println(
                "-END DUMP-----------------------------------");
    }

    /**
     * Essentially a duplicate of RecordFactory.  Kept seperate as not to
     * screw up non-debug operations.
     *
     */

    private static Record[] createRecord(short rectype, short size,
                                         byte[] data)
    {
        Record retval = null;
        Record[] realretval = null;

        // int irectype = rectype;
        switch (rectype)
        {

            case ChartRecord.sid:
                retval = new ChartRecord(rectype, size, data);
                break;

            case ChartFormatRecord.sid:
                retval = new ChartFormatRecord(rectype, size, data);
                break;

            case SeriesRecord.sid:
                retval = new SeriesRecord(rectype, size, data);
                break;

            case BeginRecord.sid:
                retval = new BeginRecord(rectype, size, data);
                break;

            case EndRecord.sid:
                retval = new EndRecord(rectype, size, data);
                break;

            case BOFRecord.sid:
                retval = new BOFRecord(rectype, size, data);
                break;

            case InterfaceHdrRecord.sid:
                retval = new InterfaceHdrRecord(rectype, size, data);
                break;

            case MMSRecord.sid:
                retval = new MMSRecord(rectype, size, data);
                break;

            case InterfaceEndRecord.sid:
                retval = new InterfaceEndRecord(rectype, size, data);
                break;

            case WriteAccessRecord.sid:
                retval = new WriteAccessRecord(rectype, size, data);
                break;

            case CodepageRecord.sid:
                retval = new CodepageRecord(rectype, size, data);
                break;

            case DSFRecord.sid:
                retval = new DSFRecord(rectype, size, data);
                break;

            case TabIdRecord.sid:
                retval = new TabIdRecord(rectype, size, data);
                break;

            case FnGroupCountRecord.sid:
                retval = new FnGroupCountRecord(rectype, size, data);
                break;

            case WindowProtectRecord.sid:
                retval = new WindowProtectRecord(rectype, size, data);
                break;

            case ProtectRecord.sid:
                retval = new ProtectRecord(rectype, size, data);
                break;

            case PasswordRecord.sid:
                retval = new PasswordRecord(rectype, size, data);
                break;

            case ProtectionRev4Record.sid:
                retval = new ProtectionRev4Record(rectype, size, data);
                break;

            case PasswordRev4Record.sid:
                retval = new PasswordRev4Record(rectype, size, data);
                break;

            case WindowOneRecord.sid:
                retval = new WindowOneRecord(rectype, size, data);
                break;

            case BackupRecord.sid:
                retval = new BackupRecord(rectype, size, data);
                break;

            case HideObjRecord.sid:
                retval = new HideObjRecord(rectype, size, data);
                break;

            case DateWindow1904Record.sid:
                retval = new DateWindow1904Record(rectype, size, data);
                break;

            case PrecisionRecord.sid:
                retval = new PrecisionRecord(rectype, size, data);
                break;

            case RefreshAllRecord.sid:
                retval = new RefreshAllRecord(rectype, size, data);
                break;

            case BookBoolRecord.sid:
                retval = new BookBoolRecord(rectype, size, data);
                break;

            case FontRecord.sid:
                retval = new FontRecord(rectype, size, data);
                break;

            case FormatRecord.sid:
                retval = new FormatRecord(rectype, size, data);
                break;

            case ExtendedFormatRecord.sid:
                retval = new ExtendedFormatRecord(rectype, size, data);
                break;

            case StyleRecord.sid:
                retval = new StyleRecord(rectype, size, data);
                break;

            case UseSelFSRecord.sid:
                retval = new UseSelFSRecord(rectype, size, data);
                break;

            case BoundSheetRecord.sid:
                retval = new BoundSheetRecord(rectype, size, data);
                break;

            case CountryRecord.sid:
                retval = new CountryRecord(rectype, size, data);
                break;

            case SSTRecord.sid:
                retval = new SSTRecord(rectype, size, data);
                break;

            case ExtSSTRecord.sid:
                retval = new ExtSSTRecord(rectype, size, data);
                break;

            case EOFRecord.sid:
                retval = new EOFRecord(rectype, size, data);
                break;

            case IndexRecord.sid:
                retval = new IndexRecord(rectype, size, data);
                break;

            case CalcModeRecord.sid:
                retval = new CalcModeRecord(rectype, size, data);
                break;

            case CalcCountRecord.sid:
                retval = new CalcCountRecord(rectype, size, data);
                break;

            case RefModeRecord.sid:
                retval = new RefModeRecord(rectype, size, data);
                break;

            case IterationRecord.sid:
                retval = new IterationRecord(rectype, size, data);
                break;

            case DeltaRecord.sid:
                retval = new DeltaRecord(rectype, size, data);
                break;

            case SaveRecalcRecord.sid:
                retval = new SaveRecalcRecord(rectype, size, data);
                break;

            case PrintHeadersRecord.sid:
                retval = new PrintHeadersRecord(rectype, size, data);
                break;

            case PrintGridlinesRecord.sid:
                retval = new PrintGridlinesRecord(rectype, size, data);
                break;

            case GridsetRecord.sid:
                retval = new GridsetRecord(rectype, size, data);
                break;

            case GutsRecord.sid:
                retval = new GutsRecord(rectype, size, data);
                break;

            case DefaultRowHeightRecord.sid:
                retval = new DefaultRowHeightRecord(rectype, size, data);
                break;

            case WSBoolRecord.sid:
                retval = new WSBoolRecord(rectype, size, data);
                break;

            case HeaderRecord.sid:
                retval = new HeaderRecord(rectype, size, data);
                break;

            case FooterRecord.sid:
                retval = new FooterRecord(rectype, size, data);
                break;

            case HCenterRecord.sid:
                retval = new HCenterRecord(rectype, size, data);
                break;

            case VCenterRecord.sid:
                retval = new VCenterRecord(rectype, size, data);
                break;

            case PrintSetupRecord.sid:
                retval = new PrintSetupRecord(rectype, size, data);
                break;

            case DefaultColWidthRecord.sid:
                retval = new DefaultColWidthRecord(rectype, size, data);
                break;

            case DimensionsRecord.sid:
                retval = new DimensionsRecord(rectype, size, data);
                break;

            case RowRecord.sid:
                retval = new RowRecord(rectype, size, data);
                break;

            case LabelSSTRecord.sid:
                retval = new LabelSSTRecord(rectype, size, data);
                break;

            case RKRecord.sid:
                retval = new RKRecord(rectype, size, data);
                break;

            case NumberRecord.sid:
                retval = new NumberRecord(rectype, size, data);
                break;

            case DBCellRecord.sid:
                retval = new DBCellRecord(rectype, size, data);
                break;

            case WindowTwoRecord.sid:
                retval = new WindowTwoRecord(rectype, size, data);
                break;

            case SelectionRecord.sid:
                retval = new SelectionRecord(rectype, size, data);
                break;

            case ContinueRecord.sid:
                retval = new ContinueRecord(rectype, size, data);
                break;

            case LabelRecord.sid:
                retval = new LabelRecord(rectype, size, data);
                break;

            case MulRKRecord.sid:
                retval = new MulRKRecord(rectype, size, data);
                break;

            case MulBlankRecord.sid:
                retval = new MulBlankRecord(rectype, size, data);
                break;

            case BlankRecord.sid:
                retval = new BlankRecord(rectype, size, data);
                break;

            case BoolErrRecord.sid:
                retval = new BoolErrRecord(rectype, size, data);
                break;

            case ColumnInfoRecord.sid:
                retval = new ColumnInfoRecord(rectype, size, data);
                break;

            case MergeCellsRecord.sid:
                retval = new MergeCellsRecord(rectype, size, data);
                break;

            case AreaRecord.sid:
                retval = new AreaRecord(rectype, size, data);
                break;

            case DataFormatRecord.sid:
                retval = new DataFormatRecord(rectype, size, data);
                break;

            case BarRecord.sid:
                retval = new BarRecord(rectype, size, data);
                break;

            case DatRecord.sid:
                retval = new DatRecord(rectype, size, data);
                break;

            case PlotGrowthRecord.sid:
                retval = new PlotGrowthRecord(rectype, size, data);
                break;

            case UnitsRecord.sid:
                retval = new UnitsRecord(rectype, size, data);
                break;

            case FrameRecord.sid:
                retval = new FrameRecord(rectype, size, data);
                break;

            case ValueRangeRecord.sid:
                retval = new ValueRangeRecord(rectype, size, data);
                break;

            case SeriesListRecord.sid:
                retval = new SeriesListRecord(rectype, size, data);
                break;

            case FontBasisRecord.sid:
                retval = new FontBasisRecord(rectype, size, data);
                break;

            case FontIndexRecord.sid:
                retval = new FontIndexRecord(rectype, size, data);
                break;

            case LineFormatRecord.sid:
                retval = new LineFormatRecord(rectype, size, data);
                break;

            case AreaFormatRecord.sid:
                retval = new AreaFormatRecord(rectype, size, data);
                break;

            case LinkedDataRecord.sid:
                retval = new LinkedDataRecord(rectype, size, data);
                break;
                
//            case FormulaRecord.sid:
//                retval = new FormulaRecord(rectype, size, data);
//                break;
                
            case SheetPropertiesRecord.sid:
                retval = new SheetPropertiesRecord(rectype, size, data);
                break;


            default :
                retval = new UnknownRecord(rectype, size, data);
        }
        if (realretval == null)
        {
            realretval = new Record[1];
            realretval[0] = retval;
            System.out.println("recordid = 0x" + Integer.toHexString(rectype) + ", size =" + size);
            System.out.println(realretval[0].toString());
        }
        return realretval;
    }

    /**
     * Method setDump - hex dump out data or not.
     *
     *
     * @param dump
     *
     */

    public void setDump(boolean dump)
    {
        this.dump = dump;
    }

    /**
     * Method main
     * with 1 argument just run straight biffview against given file<P>
     * with 2 arguments where the second argument is "on" - run biffviewer<P>
     * with hex dumps of records <P>
     *
     * with  2 arguments where the second argument is "bfd" just run a big fat
     * hex dump of the file...don't worry about biffviewing it at all
     *
     *
     * @param args
     *
     */

    public static void main(String[] args)
    {
        try
        {
            BiffViewer viewer = new BiffViewer(args);

            if ((args.length > 1) && args[1].equals("on"))
            {
                viewer.setDump(true);
            }
            if ((args.length > 1) && args[1].equals("bfd"))
            {
                POIFSFileSystem fs =
                        new POIFSFileSystem(new FileInputStream(args[0]));
                InputStream stream =
                        fs.createDocumentInputStream("Workbook");
                int size = stream.available();
                byte[] data = new byte[size];

                stream.read(data);
                HexDump.dump(data, 0, System.out, 0);
            } else
            {
                viewer.run();
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}

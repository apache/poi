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

package org.apache.poi.hssf.eventmodel;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.record.BOFRecord;
import org.apache.poi.hssf.record.BackupRecord;
import org.apache.poi.hssf.record.BlankRecord;
import org.apache.poi.hssf.record.BookBoolRecord;
import org.apache.poi.hssf.record.BoolErrRecord;
import org.apache.poi.hssf.record.BottomMarginRecord;
import org.apache.poi.hssf.record.BoundSheetRecord;
import org.apache.poi.hssf.record.CalcCountRecord;
import org.apache.poi.hssf.record.CalcModeRecord;
import org.apache.poi.hssf.record.CodepageRecord;
import org.apache.poi.hssf.record.ColumnInfoRecord;
import org.apache.poi.hssf.record.ContinueRecord;
import org.apache.poi.hssf.record.CountryRecord;
import org.apache.poi.hssf.record.DBCellRecord;
import org.apache.poi.hssf.record.DSFRecord;
import org.apache.poi.hssf.record.DateWindow1904Record;
import org.apache.poi.hssf.record.DefaultColWidthRecord;
import org.apache.poi.hssf.record.DefaultRowHeightRecord;
import org.apache.poi.hssf.record.DeltaRecord;
import org.apache.poi.hssf.record.DimensionsRecord;
import org.apache.poi.hssf.record.EOFRecord;
import org.apache.poi.hssf.record.ExtSSTRecord;
import org.apache.poi.hssf.record.ExtendedFormatRecord;
import org.apache.poi.hssf.record.ExternSheetRecord;
import org.apache.poi.hssf.record.FnGroupCountRecord;
import org.apache.poi.hssf.record.FontRecord;
import org.apache.poi.hssf.record.FooterRecord;
import org.apache.poi.hssf.record.FormatRecord;
import org.apache.poi.hssf.record.GridsetRecord;
import org.apache.poi.hssf.record.GutsRecord;
import org.apache.poi.hssf.record.HCenterRecord;
import org.apache.poi.hssf.record.HeaderRecord;
import org.apache.poi.hssf.record.HideObjRecord;
import org.apache.poi.hssf.record.IndexRecord;
import org.apache.poi.hssf.record.InterfaceEndRecord;
import org.apache.poi.hssf.record.InterfaceHdrRecord;
import org.apache.poi.hssf.record.IterationRecord;
import org.apache.poi.hssf.record.LabelRecord;
import org.apache.poi.hssf.record.LabelSSTRecord;
import org.apache.poi.hssf.record.LeftMarginRecord;
import org.apache.poi.hssf.record.MMSRecord;
import org.apache.poi.hssf.record.MergeCellsRecord;
import org.apache.poi.hssf.record.MulBlankRecord;
import org.apache.poi.hssf.record.MulRKRecord;
import org.apache.poi.hssf.record.NameRecord;
import org.apache.poi.hssf.record.NumberRecord;
import org.apache.poi.hssf.record.PaletteRecord;
import org.apache.poi.hssf.record.PasswordRecord;
import org.apache.poi.hssf.record.PasswordRev4Record;
import org.apache.poi.hssf.record.PrecisionRecord;
import org.apache.poi.hssf.record.PrintGridlinesRecord;
import org.apache.poi.hssf.record.PrintHeadersRecord;
import org.apache.poi.hssf.record.PrintSetupRecord;
import org.apache.poi.hssf.record.ProtectRecord;
import org.apache.poi.hssf.record.ProtectionRev4Record;
import org.apache.poi.hssf.record.RKRecord;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.RecordFormatException;
import org.apache.poi.hssf.record.RefModeRecord;
import org.apache.poi.hssf.record.RefreshAllRecord;
import org.apache.poi.hssf.record.RightMarginRecord;
import org.apache.poi.hssf.record.RowRecord;
import org.apache.poi.hssf.record.SSTRecord;
import org.apache.poi.hssf.record.SaveRecalcRecord;
import org.apache.poi.hssf.record.SelectionRecord;
import org.apache.poi.hssf.record.SharedFormulaRecord;
import org.apache.poi.hssf.record.StringRecord;
import org.apache.poi.hssf.record.StyleRecord;
import org.apache.poi.hssf.record.TabIdRecord;
import org.apache.poi.hssf.record.TopMarginRecord;
import org.apache.poi.hssf.record.UnknownRecord;
import org.apache.poi.hssf.record.UseSelFSRecord;
import org.apache.poi.hssf.record.VCenterRecord;
import org.apache.poi.hssf.record.WSBoolRecord;
import org.apache.poi.hssf.record.WindowOneRecord;
import org.apache.poi.hssf.record.WindowProtectRecord;
import org.apache.poi.hssf.record.WindowTwoRecord;
import org.apache.poi.hssf.record.WriteAccessRecord;
import org.apache.poi.util.LittleEndian;


/**
 * Event-based record factory.  As opposed to RecordFactory
 * this refactored version throws record events as it comes
 * accross the records.  I throws the "lazily" one record behind
 * to ensure that ContinueRecords are processed first.
 * 
 * @author Andrew C. Oliver (acoliver@apache.org) - probably to blame for the bugs (so yank his chain on the list)
 * @author Marc Johnson (mjohnson at apache dot org) - methods taken from RecordFactory
 * @author Glen Stampoultzis (glens at apache.org) - methods taken from RecordFactory
 * @author Csaba Nagy (ncsaba at yahoo dot com)
 */
public class EventRecordFactory
{
    
    /**
     * contains the classes for all the records we want to parse.
     */
    private static final Class[] records;

    static {
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
                PaletteRecord.class, StringRecord.class, SharedFormulaRecord.class
            };
       
    }
    
    /**
     * cache of the recordsToMap();
     */
    private static Map           recordsMap  = recordsToMap(records);

    /**
     * cache of the return of getAllKnownSids so that we don't have to
     * expensively get them every time.
     */
    private static short[] sidscache;

    /**
     * List of the listners that are registred.  should all be ERFListener
     */    
    private List listeners;

    /**
     * instance is abortable or not
     */
    private boolean abortable;
    
    /**
     * Construct an abortable EventRecordFactory.  
     * The same as calling new EventRecordFactory(true)
     * @see #EventRecordFactory(boolean)
     */
    public EventRecordFactory() {
        this(true);                  
    }
    
    /**
     * Create an EventRecordFactory
     * @param abortable specifies whether the return from the listener 
     * handler functions are obeyed.  False means they are ignored. True
     * means the event loop exits on error.
     */
    public EventRecordFactory(boolean abortable) {
        this.abortable = abortable;
        listeners = new ArrayList(recordsMap.size());    
        
        if (sidscache == null) {
         sidscache = getAllKnownRecordSIDs();   
        }

    }
    
    /**
     * Register a listener for records.  These can be for all records 
     * or just a subset.
     * 
     * @param sids an array of Record.sid values identifying the records
     * the listener will work with.  Alternatively if this is "null" then 
     * all records are passed.
     */
    public void registerListener(ERFListener listener, short[] sids) {
      if (sids == null)
        sids = sidscache;
      ERFListener wrapped = new ListenerWrapper(listener, sids, abortable);    
      listeners.add(wrapped);
    }
    
    /**
     * used for unit tests to test the registration of record listeners.
     * @return Iterator of ERFListeners
     */
    protected Iterator listeners() {
        return listeners.iterator();
    }

    /**
     * sends the record event to all registered listeners.
     * @param record the record to be thrown.
     * @return boolean abort.  If exitability is turned on this aborts
     * out of the event loop should any listener specify to do so.
     */
    private boolean throwRecordEvent(Record record)
    {
        boolean result = true;
        Iterator i = listeners.iterator();
        
        while (i.hasNext()) {
            result = ((ERFListener) i.next()).processRecord(record);  
            if (abortable == true && result == false) {
                break;   
            }
        }
        return result;
    }

    /**
     * Create an array of records from an input stream
     *
     * @param in the InputStream from which the records will be
     *           obtained
     *
     * @exception RecordFormatException on error processing the
     *            InputStream
     */
    public void processRecords(InputStream in)
        throws RecordFormatException
    {
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
                            if ( last_record != null ) {
                                if (throwRecordEvent(last_record) == false && abortable == true) {
                                 last_record = null;
                                 break;   
                                }
                            }
                         //   records.add(
                         //       recs[ k ]);               // these will be number records
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
                                if (last_record != null) {
                                    if (throwRecordEvent(last_record) == false && abortable == true) {
                                        last_record = null;
                                        break;   
                                    }
                                }
                                
                                last_record = record;
                                
                                //records.add(record);
                            }
                        }
                    }
                }
            }
            while (rectype != 0);
            
            if (last_record != null) {
               throwRecordEvent(last_record);               
            }
        }
        catch (IOException e)
        {
            throw new RecordFormatException("Error reading bytes");
        }

        // Record[] retval = new Record[ records.size() ];
        // retval = ( Record [] ) records.toArray(retval);
     
    }

    /**
     * create a record, if there are MUL records than multiple records
     * are returned digested into the non-mul form.
     */
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

    /**
     * @return an array of all the SIDS for all known records
     */
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

    /**
     * gets the record constructors and sticks them in the map by SID
     * @return map of SIDs to short,short,byte[] constructors for Record classes
     * most of org.apache.poi.hssf.record.*
     */
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
                throw new RecordFormatException(
                    "Unable to determine record types");
            }
            result.put(new Short(sid), constructor);
        }
        return result;
    }

}

/**
 * ListenerWrapper just wraps an ERFListener and adds support for throwing
 * the event to multiple SIDs
 */
class ListenerWrapper implements ERFListener {
       private ERFListener listener;
       private short[] sids;
       private boolean abortable;

    ListenerWrapper(ERFListener listener, short[] sids, boolean abortable) {
        this.listener = listener;
        this.sids = sids;   
        this.abortable = abortable;
    }       
    

    public boolean processRecord(Record rec)
    {
        boolean result = true;
        for (int k = 0; k < sids.length; k++) {
            if (sids[k] == rec.getSid()) {
                result = listener.processRecord(rec);
            
                if (abortable == true && result == false) {
                    break;   
                }
            }
        }
        return result;
    }   
}


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

package org.apache.poi.hssf.dev;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;

import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.hssf.record.*;
import org.apache.poi.hssf.eventmodel.*;
import org.apache.poi.hssf.usermodel.*;

/**
 * Event Factory version of HSSF test class.
 * @author  andy
 */

public class EFHSSF
{
    String       infile;
    String       outfile;
    HSSFWorkbook workbook = null;
    HSSFSheet    cursheet = null;

    /** Creates a new instance of EFHSSF */

    public EFHSSF()
    {
    }

    public void setInputFile(String infile)
    {
        this.infile = infile;
    }

    public void setOutputFile(String outfile)
    {
        this.outfile = outfile;
    }

    public void run()
        throws IOException
    {
        FileInputStream fin   = new FileInputStream(infile);
        POIFSFileSystem poifs = new POIFSFileSystem(fin);
        InputStream     din   = poifs.createDocumentInputStream("Workbook");
        HSSFRequest     req   = new HSSFRequest();

        req.addListenerForAllRecords(new EFHSSFListener(this));
        HSSFEventFactory factory = new HSSFEventFactory();

        factory.processEvents(req, din);
        fin.close();
        din.close();
        FileOutputStream fout = new FileOutputStream(outfile);

        workbook.write(fout);
        fout.close();
        System.out.println("done.");
    }

    public void recordHandler(Record record)
    {
        HSSFRow  row      = null;
        HSSFCell cell     = null;
        int      sheetnum = -1;

        switch (record.getSid())
        {

            case BOFRecord.sid :
                BOFRecord bof = ( BOFRecord ) record;

                if (bof.getType() == bof.TYPE_WORKBOOK)
                {
                    workbook = new HSSFWorkbook();
                }
                else if (bof.getType() == bof.TYPE_WORKSHEET)
                {
                    sheetnum++;
                    cursheet = workbook.getSheetAt(sheetnum);
                }
                break;

            case BoundSheetRecord.sid :
                BoundSheetRecord bsr = ( BoundSheetRecord ) record;

                workbook.createSheet(bsr.getSheetname());
                break;

            case RowRecord.sid :
                RowRecord rowrec = ( RowRecord ) record;

                cursheet.createRow(rowrec.getRowNumber());
                break;

            case NumberRecord.sid :
                NumberRecord numrec = ( NumberRecord ) record;

                row  = cursheet.getRow(numrec.getRow());
                cell = row.createCell(numrec.getColumn(),
                                      HSSFCell.CELL_TYPE_NUMERIC);
                cell.setCellValue(numrec.getValue());
                break;

            case SSTRecord.sid :
                SSTRecord sstrec = ( SSTRecord ) record;

                for (int k = 0; k < sstrec.getNumUniqueStrings(); k++)
                {
                    workbook.addSSTString(sstrec.getString(k));
                }
                break;

            case LabelSSTRecord.sid :
                LabelSSTRecord lrec = ( LabelSSTRecord ) record;

                row  = cursheet.getRow(lrec.getRow());
                cell = row.createCell(lrec.getColumn(),
                                      HSSFCell.CELL_TYPE_STRING);
                cell.setCellValue(workbook.getSSTString(lrec.getSSTIndex()));
                break;
        }
    }

    public static void main(String [] args)
    {
        if ((args.length < 2) || !args[ 0 ].equals("--help"))
        {
            try
            {
                EFHSSF viewer = new EFHSSF();

                viewer.setInputFile(args[ 0 ]);
                viewer.setOutputFile(args[ 1 ]);
                viewer.run();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            System.out.println("EFHSSF");
            System.out.println(
                "General testbed for HSSFEventFactory based testing and "
                + "Code examples");
            System.out.println("Usage: java org.apache.poi.hssf.dev.EFHSSF "
                               + "file1 file2");
            System.out.println(
                "   --will rewrite the file reading with the event api");
            System.out.println("and writing with the standard API");
        }
    }
}

class EFHSSFListener
    implements HSSFListener
{
    EFHSSF efhssf;

    public EFHSSFListener(EFHSSF efhssf)
    {
        this.efhssf = efhssf;
    }

    public void processRecord(Record record)
    {
        efhssf.recordHandler(record);
    }
}

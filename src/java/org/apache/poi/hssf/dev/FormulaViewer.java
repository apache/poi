
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
 * FormulaViewer.java - finds formulas in a BIFF8 file and attempts to parse them and
 * display info about them.
 *
 * Created on November 18, 2001, 7:58 AM
 */
package org.apache.poi.hssf.dev;

import java.io.InputStream;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;

//import java.io.*;
import java.util.List;

import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.HexDump;
import org.apache.poi.hssf.record.*;
import org.apache.poi.hssf.record.formula.*;
import org.apache.poi.hssf.model.*;
import org.apache.poi.hssf.usermodel.*;

/**
 * FormulaViewer - finds formulas in a BIFF8 file and attempts to read them/display
 * data from them. Only works if Formulas are enabled in "RecordFactory"
 * @author  andy
 */

public class FormulaViewer
{
    private String file;

    /** Creates new FormulaViewer */

    public FormulaViewer()
    {
    }

    /**
     * Method run
     *
     *
     * @exception Exception
     *
     */

    public void run()
        throws Exception
    {
        POIFSFileSystem fs      =
            new POIFSFileSystem(new FileInputStream(file));
        List            records =
            RecordFactory
                .createRecords(fs.createDocumentInputStream("Workbook"));

        for (int k = 0; k < records.size(); k++)
        {
            Record record = ( Record ) records.get(k);

            if (record.getSid() == FormulaRecord.sid)
            {
                parseFormulaRecord(( FormulaRecord ) record);
            }
        }
    }

    /**
     * Method parseFormulaRecord
     *
     *
     * @param record
     *
     */

    public void parseFormulaRecord(FormulaRecord record)
    {
        System.out.println("In ParseFormula Record");
        System.out.println("row   = " + record.getRow());
        System.out.println("col   = " + record.getColumn());
        System.out.println("value = " + record.getValue());
        System.out.println("xf    = " + record.getXFIndex());
        System.out.println("number of ptgs = "
                           + record.getNumberOfExpressionTokens());
        System.out.println("options = " + record.getOptions());
        System.out.println(composeForumla(record));
    }

    public String composeForumla(FormulaRecord record)
    {
        StringBuffer formula = new StringBuffer("=");
        int          numptgs = record.getNumberOfExpressionTokens();
        List         ptgs    = record.getParsedExpression();

        for (int ptgnum = numptgs - 1; ptgnum > (-1); ptgnum--)
        {
            Ptg          ptg      = ( Ptg ) ptgs.get(ptgnum);
            OperationPtg optg     = ( OperationPtg ) ptg;
            int          numops   = optg.getNumberOfOperands();
            Ptg[]        ops      = new Ptg[ numops ];
            int          opoffset = 1;

            for (int opnum = ops.length - 1; opnum > -1; opnum--)
            {
                ops[ opnum ] = ( Ptg ) ptgs.get(ptgnum - opoffset);
                opoffset++;
            }
            formula.append(optg.toFormulaString(ops));
            ptgnum -= ops.length;
        }
        return formula.toString();
    }

    /**
     * Method setFile
     *
     *
     * @param file
     *
     */

    public void setFile(String file)
    {
        this.file = file;
    }

    /**
     * Method main
     *
     * pass me a filename and I'll try and parse the formulas from it
     *
     * @param args pass one argument with the filename or --help
     *
     */

    public static void main(String args[])
    {
        if ((args == null) || (args.length != 1)
                || args[ 0 ].equals("--help"))
        {
            System.out.println(
                "FormulaViewer .8 proof that the devil lies in the details (or just in BIFF8 files in general)");
            System.out.println("usage: Give me a big fat file name");
        }
        else
        {
            try
            {
                FormulaViewer viewer = new FormulaViewer();

                viewer.setFile(args[ 0 ]);
                viewer.run();
            }
            catch (Exception e)
            {
                System.out.println("Whoops!");
                e.printStackTrace();
            }
        }
    }
}

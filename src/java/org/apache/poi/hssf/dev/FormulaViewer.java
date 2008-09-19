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

import java.io.FileInputStream;
import java.util.List;

import org.apache.poi.hssf.model.HSSFFormulaParser;
import org.apache.poi.hssf.record.FormulaRecord;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.RecordFactory;
import org.apache.poi.hssf.record.formula.ExpPtg;
import org.apache.poi.hssf.record.formula.FuncPtg;
import org.apache.poi.hssf.record.formula.OperationPtg;
import org.apache.poi.hssf.record.formula.Ptg;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

/**
 * FormulaViewer - finds formulas in a BIFF8 file and attempts to read them/display
 * data from them. Only works if Formulas are enabled in "RecordFactory"
 * @author  andy
 * @author Avik
 */

public class FormulaViewer
{
    private String file;
    private boolean list=false;

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
               if (list) {
                    listFormula((FormulaRecord) record);
               }else {
                    parseFormulaRecord(( FormulaRecord ) record);
               }
            }
        }
    }
    
    private void listFormula(FormulaRecord record) {
        String sep="~";
        Ptg[] tokens= record.getParsedExpression();
        Ptg token;
        int numptgs = tokens.length;
        String numArg;
            token = tokens[numptgs-1];
            if (token instanceof FuncPtg) {
                numArg = String.valueOf(numptgs-1);
            } else { 
            	numArg = String.valueOf(-1);
            }
            
            StringBuffer buf = new StringBuffer();
            
            if (token instanceof ExpPtg) return;
            buf.append(((OperationPtg) token).toFormulaString());
            buf.append(sep);
            switch (token.getPtgClass()) {
                case Ptg.CLASS_REF :
                    buf.append("REF");
                    break;
                case Ptg.CLASS_VALUE :
                    buf.append("VALUE");
                    break;
                case Ptg.CLASS_ARRAY :
                    buf.append("ARRAY");
                    break;
            }
            
            buf.append(sep);
            if (numptgs>1) {
                token = tokens[numptgs-2];
                switch (token.getPtgClass()) {
                    case Ptg.CLASS_REF :
                        buf.append("REF");
                        break;
                    case Ptg.CLASS_VALUE :
                        buf.append("VALUE");
                        break;
                    case Ptg.CLASS_ARRAY :
                        buf.append("ARRAY");
                        break;
                }
            }else {
                buf.append("VALUE");
            }
            buf.append(sep);
            buf.append(numArg);
            System.out.println(buf.toString());
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
        System.out.println("==============================");
        System.out.print("row = " + record.getRow());
        System.out.println(", col = " + record.getColumn());
        System.out.println("value = " + record.getValue());
        System.out.print("xf = " + record.getXFIndex());
        System.out.print(", number of ptgs = "
                           + record.getParsedExpression().length);
        System.out.println(", options = " + record.getOptions());
        System.out.println("RPN List = "+formulaString(record));
        System.out.println("Formula text = "+ composeFormula(record));
    }

    private String formulaString(FormulaRecord record) {

        StringBuffer buf = new StringBuffer();
		Ptg[] tokens = record.getParsedExpression();
		for (int i = 0; i < tokens.length; i++) {
			Ptg token = tokens[i];
            buf.append( token.toFormulaString());
            switch (token.getPtgClass()) {
                case Ptg.CLASS_REF :
                    buf.append("(R)");
                    break;
                case Ptg.CLASS_VALUE :
                    buf.append("(V)");
                    break;
                case Ptg.CLASS_ARRAY :
                    buf.append("(A)");
                    break;
            }
            buf.append(' ');
        } 
        return buf.toString();
    }
    
    
    private static String composeFormula(FormulaRecord record)
    {
       return  HSSFFormulaParser.toFormulaString((HSSFWorkbook)null, record.getParsedExpression());
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
    
    public void setList(boolean list) {
        this.list=list;
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
        if ((args == null) || (args.length >2 )
                || args[ 0 ].equals("--help"))
        {
            System.out.println(
                "FormulaViewer .8 proof that the devil lies in the details (or just in BIFF8 files in general)");
            System.out.println("usage: Give me a big fat file name");
        } else if (args[0].equals("--listFunctions")) { // undocumented attribute to research functions!~
            try {
                FormulaViewer viewer = new FormulaViewer();
                viewer.setFile(args[1]);
                viewer.setList(true);
                viewer.run();
            }
            catch (Exception e) {
                System.out.println("Whoops!");
                e.printStackTrace();
            }
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

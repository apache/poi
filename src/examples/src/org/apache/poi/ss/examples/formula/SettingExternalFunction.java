/*
 *  ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ====================================================================
 */

package org.apache.poi.ss.examples.formula;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.poi.ss.formula.OperationEvaluationContext;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.apache.poi.ss.formula.functions.FreeRefFunction;
import org.apache.poi.ss.formula.udf.UDFFinder;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Demonstrates how to use functions provided by third-party add-ins, e.g. Bloomberg Excel Add-in.
 *
 * There can be situations when you are not interested in formula evaluation,
 * you just need to set the formula  and the workbook will be evaluation by the client.
 */
public class SettingExternalFunction {

    /**
     * wrap external functions in a plugin
     */
    public static class BloombergAddIn implements UDFFinder {
        private final Map<String, FreeRefFunction> _functionsByName;

        public BloombergAddIn() {
            // dummy function that returns NA
            // don't care about the implementation, we are not interested in evaluation
            // and this method will never be called
            FreeRefFunction NA = new FreeRefFunction() {
                @Override
                public ValueEval evaluate(ValueEval[] args, OperationEvaluationContext ec) {
                    return ErrorEval.NA;
                }
            };
            _functionsByName = new HashMap<>();
            _functionsByName.put("BDP", NA);
            _functionsByName.put("BDH", NA);
            _functionsByName.put("BDS", NA);
        }

        @Override
        public FreeRefFunction findFunction(String name) {
            return _functionsByName.get(name.toUpperCase(Locale.ROOT));
        }

    }

    public static void main( String[] args ) throws IOException {

        try (Workbook wb = new XSSFWorkbook()) {  // or new HSSFWorkbook()

            // register the add-in
            wb.addToolPack(new BloombergAddIn());

            Sheet sheet = wb.createSheet();
            Row row = sheet.createRow(0);
            row.createCell(0).setCellFormula("BDP(\"GOOG Equity\",\"CHG_PCT_YTD\")/100");
            row.createCell(1).setCellFormula("BDH(\"goog us equity\",\"EBIT\",\"1/1/2005\",\"12/31/2009\",\"per=cy\",\"curr=USD\") ");
            row.createCell(2).setCellFormula("BDS(\"goog us equity\",\"top_20_holders_public_filings\") ");

            try (FileOutputStream out = new FileOutputStream("bloomberg-demo.xlsx")) {
                wb.write(out);
            }
        }
    }
}

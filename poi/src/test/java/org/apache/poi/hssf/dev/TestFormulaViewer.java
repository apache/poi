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
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import org.apache.commons.io.output.NullWriter;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.hssf.model.HSSFFormulaParser;
import org.apache.poi.hssf.record.FormulaRecord;
import org.apache.poi.hssf.record.RecordFactory;
import org.apache.poi.hssf.record.RecordInputStream;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.formula.ptg.FuncPtg;
import org.apache.poi.ss.formula.ptg.Ptg;

class TestFormulaViewer extends BaseTestIteratingXLS {
    @Override
    protected Map<String, Class<? extends Throwable>> getExcludes() {
        Map<String, Class<? extends Throwable>> excludes = super.getExcludes();
        excludes.put("35897-type4.xls", EncryptedDocumentException.class); // unsupported crypto api header
        excludes.put("51832.xls", EncryptedDocumentException.class);
        excludes.put("xor-encryption-abc.xls", EncryptedDocumentException.class);
        excludes.put("password.xls", EncryptedDocumentException.class);
        excludes.put("43493.xls", RecordInputStream.LeftoverDataException.class);  // HSSFWorkbook cannot open it as well
        excludes.put("44958_1.xls", RecordInputStream.LeftoverDataException.class);
        excludes.put("protected_66115.xls", EncryptedDocumentException.class);
        return excludes;
    }

    private final boolean doListFormula = true;

    @Override
    void runOneFile(File fileIn) throws Exception {
        // replace with System.out for manual tests
        PrintWriter out = new PrintWriter(NullWriter.INSTANCE);

        final Function<FormulaRecord, String> lister = (doListFormula) ? this::listFormula : this::parseFormulaRecord;

        try (POIFSFileSystem fs = new POIFSFileSystem(fileIn, true);
             InputStream is = BiffViewer.getPOIFSInputStream(fs)) {
            RecordFactory.createRecords(is).stream()
                .filter(r -> r.getSid() == FormulaRecord.sid)
                .map(FormulaRecord.class::cast)
                .map(lister)
                .map(Objects::nonNull)
                .forEach(out::println);
        }
    }

    private String listFormula(FormulaRecord record) {
        Ptg[] tokens = record.getParsedExpression();
        int numptgs = tokens.length;
        final Ptg lastToken = tokens[numptgs - 1];

        String fmlStr;
        try {
            fmlStr = lastToken.toFormulaString();
        } catch (Exception ignored) {
            return null;
        }

        return String.join("~",
            fmlStr,
            mapToken(lastToken),
            (numptgs > 1 ? mapToken(tokens[numptgs - 2]) : "VALUE"),
            String.valueOf(lastToken instanceof FuncPtg ? numptgs - 1 : -1)
        );
    }

    private static String mapToken(Ptg token) {
        switch (token.getPtgClass()) {
            case Ptg.CLASS_REF:
                return "REF";
            case Ptg.CLASS_VALUE:
                return "VALUE";
            case Ptg.CLASS_ARRAY:
                return "ARRAY";
            default:
                throwInvalidRVAToken(token);
                return "";
        }
    }

    /**
     * Method parseFormulaRecord
     *
     * @param record the record to be parsed
     */
    public String parseFormulaRecord(FormulaRecord record) {
        return String.format(Locale.ROOT,
            "==============================\n" +
                "row = %d, col = %d\n" +
                "value = %f\n" +
                "xf = %d, number of ptgs = %d, options = %d\n" +
                "RPN List = %s\n" +
                "Formula text = %s",
            record.getRow(), record.getColumn(), record.getValue(), record.getXFIndex(),
            record.getParsedExpression().length, record.getOptions(),
            formulaString(record), composeFormula(record));
    }

    private String formulaString(FormulaRecord record) {
        StringBuilder buf = new StringBuilder();
        Ptg[] tokens = record.getParsedExpression();
        for (Ptg token : tokens) {
            buf.append(token.toFormulaString());
            switch (token.getPtgClass()) {
                case Ptg.CLASS_REF:
                    buf.append("(R)");
                    break;
                case Ptg.CLASS_VALUE:
                    buf.append("(V)");
                    break;
                case Ptg.CLASS_ARRAY:
                    buf.append("(A)");
                    break;
                default:
                    throwInvalidRVAToken(token);
            }
            buf.append(' ');
        }
        return buf.toString();
    }

    private static void throwInvalidRVAToken(Ptg token) {
        throw new IllegalStateException("Invalid RVA type (" + token.getPtgClass() + "). This should never happen.");
    }

    private static String composeFormula(FormulaRecord record) {
        return HSSFFormulaParser.toFormulaString(null, record.getParsedExpression());
    }
}

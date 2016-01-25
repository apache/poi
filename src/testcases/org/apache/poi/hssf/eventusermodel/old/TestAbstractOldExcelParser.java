package org.apache.poi.hssf.eventusermodel.old;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.record.OldLabelRecord;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.apache.poi.util.IOUtils.closeQuietly;
import static org.junit.Assert.assertEquals;

public class TestAbstractOldExcelParser {

    private void process(AbstractOldExcelParser p, String fileName) throws IOException {
        OldHSSFEventFactory f = new OldHSSFEventFactory();
        File src = HSSFTestDataSamples.getSampleFile(fileName);
        FileInputStream fis = new FileInputStream(src);
        try {
            f.process(fis, p);
        } finally {
            closeQuietly(fis);
        }
    }

    private void assertRowData1(OldExcelParser1.RowData1 r, String expectedGroupName, long expectedFiled, Object expectedTaxAuditor) {
        assertEquals(expectedGroupName, r.groupName);
        assertEquals(expectedFiled, r.filed);
        assertEquals(expectedTaxAuditor, r.taxAuditor);
    }

    private static final class Column extends AbstractColumn {

        private final String name;
        private final boolean required;

        public Column(String name, boolean required) {
            this.name = name;
            this.required = required;
        }

        @Override
        public boolean isRequired() {
            return required;
        }

        @Override
        public String toString() {
            return "Column{" +
                    "name='" + name + '\'' +
                    ", required=" + required +
                    '}';
        }
    }

    @Test
    public void test1() throws ParseException, IOException {
        OldExcelParser1 p = new OldExcelParser1();
        process(p, "testEXCEL_4.xls");
        assertEquals(34, p.result.size());
        assertRowData1(p.result.get(0), "United States, total [2]", 168184400L, null);
        assertRowData1(p.result.get(12), "$100,000 or more", 272000L, 17L);
        assertRowData1(p.result.get(30), "Employment tax returns", 28866600L, 9L);
        assertRowData1(p.result.get(33), "S corporation returns, Form 1120S [10,15]", 2887100L, null);
    }

    private static final class OldExcelParser1 extends AbstractOldExcelParser<Column, OldExcelParser1.OldExcelParser1Exception> {

        private final Column group = new Column("Group", false);
        private final Column c1 = new Column("(1)", true);// Returns filed in Calendar Year 2000 [1,2]
        private final Column c10 = new Column("(10)", false); // Tax auditor [3,4]
        private final Column[] row = new Column[]{c1, c10};

        private final List<RowData1> result = new ArrayList<RowData1>();

        @Override
        public void onOldLabelRecord(OldLabelRecord lr) {
            if(lr.getColumn() == 0 && "See notes and footnotes at end of table.".equals(lr.getValue())) {
                // Because sheet contains several tables let's reset header when first table parsed
                resetHeader();
            }
            super.onOldLabelRecord(lr);
        }

        @Override
        protected void tryHeaderRecord(short colNum, String value, Map<Short, Column> header) {
            for (Column c : row) {
                if (c.name.equals(value)) {
                    // matches the expected num and name of column
                    header.put(colNum, c);
                    break;
                }
            }
        }

        @Override
        protected boolean tryHeaderEnd(Map<Short, Column> header) {
            boolean b = header.size() == row.length;
            if (b) {
                header.put((short) 0, group);
            }
            return b;
        }


        @Override
        protected void onDataRowEnd() {
            try {
                final String groupName = getString(group);
                if(groupName != null) {
                    final Long filed = getLong(c1);
                    final Long taxAuditor = getLong(c10);
                    result.add(new RowData1(groupName, filed, taxAuditor));
                }
            } catch (OldExcelParser1Exception ex) {
                System.out.println(ex.toString());
            }
        }

        @Override
        protected OldExcelParser1Exception createException(Column column, String message) {
            return new OldExcelParser1Exception(message, getSheetName(), getCurrentRowNum(), column);
        }

        private final class RowData1 {
            private final String groupName;
            private final long filed;
            private final Long taxAuditor;

            RowData1(String groupName, long filed, Long taxAuditor) {
                this.groupName = groupName;
                this.filed = filed;
                this.taxAuditor = taxAuditor;
            }

            @Override
            public String toString() {
                return "RowData{" +
                        "groupName=" + groupName +
                        "filed=" + filed +
                        ", taxAuditor=" + taxAuditor +
                        '}';
            }
        }

        static final class OldExcelParser1Exception extends Exception {

            private final String sheetName;
            private final int rowNum;
            private final Column column;


            OldExcelParser1Exception(String message, String sheetName, int rowNum, Column column) {
                super(message);
                this.sheetName = sheetName;
                this.rowNum = rowNum;
                this.column = column;
            }

            @Override
            public String toString() {
                return "OldExcelParser1Exception{" +
                        "sheetName='" + sheetName + '\'' +
                        ", rowNum=" + rowNum +
                        ", column=" + column +
                        ", message=" + getMessage() +
                        '}';
            }
        }

    }

    @Test
    public void test2() throws ParseException, IOException {
        OldExcelParser2 p = new OldExcelParser2();
        process(p, "testEXCEL_95.xls");
        assertEquals(15, p.result.size());

        assertRowData(p.result.get(0), 1L, 1L, "1.0");
        assertRowData(p.result.get(3), 4L, 16L, null);
        assertRowData(p.result.get(14), 15L, 225L, "1253.82");
    }

    private void assertRowData(OldExcelParser2.RowData2 r, long expectedNumber, long expectedSquare, String expectedFormatted) {
        assertEquals(expectedNumber, r.number);
        assertEquals(expectedSquare, r.square);
        assertEquals(expectedFormatted, r.formatted);
    }

    private static final class OldExcelParser2 extends AbstractOldExcelParser<Column, OldExcelParser2.OldExcelParser2Exception> {

        private final Column number = new Column("Number", false);
        private final Column square = new Column("Square", true);
        private final Column formatted = new Column("Formatted", false);
        private final Column[] row = new Column[]{number, square, formatted};

        private final List<RowData2> result = new ArrayList<RowData2>();

        @Override
        protected void tryHeaderRecord(short colNum, String value, Map<Short, Column> header) {
            for (Column c : row) {
                if (c.name.equals(value)) {
                    // matches name of column
                    header.put(colNum, c);
                    break;
                }
            }
        }

        @Override
        protected boolean tryHeaderEnd(Map<Short, Column> header) {
            boolean b = header.size() == row.length;
            if (b) {
                header.put((short) 0, number);
            }
            return b;
        }


        @Override
        protected void onDataRowEnd() {
            try {
                final Long numberValue = getLong(number);
                if(numberValue != null) {
                    result.add(new RowData2(numberValue, getLong(square), getString(formatted)));
                }
            } catch (OldExcelParser2Exception ex) {
                System.out.println(ex.toString());
            }
        }

        @Override
        protected OldExcelParser2Exception createException(Column column, String message) {
            return new OldExcelParser2Exception(message, getSheetName(), getCurrentRowNum(), column);
        }

        private final class RowData2 {
            private final long number;
            private final long square;
            private final String formatted;

            RowData2(long number, long square, String formatted) {
                this.number = number;
                this.square = square;
                this.formatted = formatted;
            }

            @Override
            public String toString() {
                return "RowData2{" +
                        "number=" + number +
                        ", square=" + square +
                        ", formatted='" + formatted + '\'' +
                        '}';
            }
        }

        static final class OldExcelParser2Exception extends Exception {

            private final String sheetName;
            private final int rowNum;
            private final Column column;


            OldExcelParser2Exception(String message, String sheetName, int rowNum, Column column) {
                super(message);
                this.sheetName = sheetName;
                this.rowNum = rowNum;
                this.column = column;
            }

            @Override
            public String toString() {
                return "OldExcelParser1Exception{" +
                        "sheetName='" + sheetName + '\'' +
                        ", rowNum=" + rowNum +
                        ", column=" + column +
                        ", message=" + getMessage() +
                        '}';
            }
        }

    }

}
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

import org.apache.poi.ss.usermodel.*
import org.apache.poi.ss.util.*
import java.io.File

if (args.length == 0) {
   println "Use:"
   println "   SpreadSheetDemo <excel-file> [output-file]"
   return 1
}

File f = new File(args[0])
DataFormatter formatter = new DataFormatter()
WorkbookFactory.create(f,null,true).withCloseable { workbook ->
   println "Has ${workbook.getNumberOfSheets()} sheets"

   // Dump the contents of the spreadsheet
   (0..<workbook.getNumberOfSheets()).each { sheetNum ->
      println "Sheet ${sheetNum} is called ${workbook.getSheetName(sheetNum)}"

      def sheet = workbook.getSheetAt(sheetNum)
      sheet.each { row ->
         def nonEmptyCells = row.grep { c -> c.getCellType() != Cell.CELL_TYPE_BLANK }
         println " Row ${row.getRowNum()} has ${nonEmptyCells.size()} non-empty cells:"
         nonEmptyCells.each { c ->
            def cRef = [c] as CellReference
            println "  * ${cRef.formatAsString()} = ${formatter.formatCellValue(c)}"
         }
      }
   }

   // Add two new sheets and populate
   CellStyle headerStyle = makeHeaderStyle(workbook)
   Sheet ns1 = workbook.createSheet("Generated 1")
   exportHeader(ns1, headerStyle, null, ["ID","Title","Num"] as String[])
   ns1.createRow(1).createCell(0).setCellValue("TODO - Populate with data")

   Sheet ns2 = workbook.createSheet("Generated 2")
   exportHeader(ns2, headerStyle, "This is a demo sheet", 
                ["ID","Title","Date","Author","Num"] as String[])
   ns2.createRow(2).createCell(0).setCellValue(1)
   ns2.createRow(3).createCell(0).setCellValue(4)
   ns2.createRow(4).createCell(0).setCellValue(1)

   // Save
   File output = File.createTempFile("output-", (f.getName() =~ /(\.\w+$)/)[0][0])
   output.withOutputStream { os -> workbook.write(os) }
   println "Saved as ${output}"
}

CellStyle makeHeaderStyle(Workbook wb) {
   int HEADER_HEIGHT = 18
   CellStyle style = wb.createCellStyle()

   style.setFillForegroundColor(IndexedColors.AQUA.getIndex())
   style.setFillPattern(FillPatternType.SOLID_FOREGROUND)

   Font font = wb.createFont()
   font.setFontHeightInPoints((short)HEADER_HEIGHT)
   font.setBold(true)
   style.setFont(font)

   return style
}
void exportHeader(Sheet s, CellStyle headerStyle, String info, String[] headers) {
   Row r
   int rn = 0
   int HEADER_HEIGHT = 18
   // Do they want an info row at the top?
   if (info != null && !info.isEmpty()) {
      r = s.createRow(rn)
      r.setHeightInPoints(HEADER_HEIGHT+1)
      rn++

      Cell c = r.createCell(0)
      c.setCellValue(info)
      c.setCellStyle(headerStyle)
      s.addMergedRegion(new CellRangeAddress(0,0,0,headers.length-1))
   }
   // Create the header row, of the right size
   r = s.createRow(rn)
   r.setHeightInPoints(HEADER_HEIGHT+1)
   // Add the column headings
   headers.eachWithIndex { col, idx ->
      Cell c = r.createCell(idx)
      c.setCellValue(col)
      c.setCellStyle(headerStyle)
      s.autoSizeColumn(idx)
   }
   // Make all the columns filterable
   s.setAutoFilter(new CellRangeAddress(rn, rn, 0, headers.length-1))
}

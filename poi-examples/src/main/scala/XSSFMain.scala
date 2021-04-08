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

// Import the required classes
import org.apache.poi.ss.usermodel.{WorkbookFactory, DataFormatter}
import java.io.{File, FileOutputStream}

object XSSFMain extends App {

    // Automatically convert Java collections to Scala equivalents
    import scala.collection.JavaConversions._

    // Read the contents of the workbook
    val workbook = WorkbookFactory.create(new File("SampleSS.xlsx"))
    val formatter = new DataFormatter()
    for {
        // Iterate and print the sheets
        (sheet, i) <- workbook.zipWithIndex
        _ = println(s"Sheet $i of ${workbook.getNumberOfSheets}: ${sheet.getSheetName}")

        // Iterate and print the rows
        row <- sheet
        _ = println(s"\tRow ${row.getRowNum}")

        // Iterate and print the cells
        cell <- row
    } {
        println(s"\t\t${cell.getCellAddress}: ${formatter.formatCellValue(cell)}")
    }

    // Add a sheet to the workbook
    val sheet = workbook.createSheet("new sheet")
    val row = sheet.createRow(7)
    val cell = row.createCell(42)
    cell.setAsActiveCell()
    cell.setCellValue("The answer to life, the universe, and everything")

    // Save the updated workbook as a new file
    val fos = new FileOutputStream("SampleSS-updated.xlsx")
    workbook.write(fos)
    workbook.close()
}

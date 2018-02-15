import org.apache.poi.ss.usermodel.*
import java.io.File

if (args.length == 0) {
   println "Use:"
   println "   SpreadSheetDemo [excel-file]"
   return 1
}

File f = new File(args[0]);
WorkbookFactory.create(f,null,true).withCloseable { workbook ->
   println "Has ${workbook.getNumberOfSheets()} sheets"
   0.step workbook.getNumberOfSheets(), 1, { sheetNum ->
     println "Sheet ${sheetNum} is called ${workbook.getSheetName(sheetNum)}"
   }
}


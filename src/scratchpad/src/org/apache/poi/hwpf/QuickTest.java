package org.apache.poi.hwpf;

import java.io.*;

import org.apache.poi.hwpf.usermodel.*;

public class QuickTest
{
  public QuickTest()
  {
  }

  public static void main(String[] args)
  {
    try
    {
      HWPFDocument doc = new HWPFDocument (new FileInputStream (args[0]));
      Range r = doc.getRange();

      System.out.println("Example you supplied:");
      System.out.println("---------------------");
      for (int x = 0; x < r.numSections(); x++)
      {
        Section s = r.getSection(x);
        for (int y = 0; y < s.numParagraphs(); y++)
        {
          Paragraph p = s.getParagraph(y);
          for (int z = 0; z < p.numCharacterRuns(); z++)
          {
            //character run
            CharacterRun run = p.getCharacterRun(z);
            //character run text
            String text = run.text();
            // show us the text
            System.out.print(text);
          }
          // use a new line at the paragraph break
          System.out.println();
        }
      }


//      System.out.println("\n\nExample using new method:");
//      System.out.println("-------------------------");
//      for (int x = 0; x < r.numSections(); x++)
//      {
//        Section s = r.getSection(x);
//        for (int y = 0; y < s.numParagraphs(); y++)
//        {
//          Paragraph p = s.getParagraph(y);
//          for (int z = 0; z < p.numCharacterRuns(); z++)
//          {
//             //character run
//             CharacterRun run = p.getCharacterRun(z);
//             //** get character run/paragraph common text **
//             String text = run.commonText(p);
//             // show us the text
//             System.out.print(text);
//          }
//          // use a new line at the paragraph break
//          System.out.println();
//        }
//      }

    }
    catch (Throwable t)
    {
      t.printStackTrace();
    }
  }

}
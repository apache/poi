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

package org.apache.poi.hwpf;

import java.io.*;

import org.apache.poi.hwpf.usermodel.*;

public final class QuickTest
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

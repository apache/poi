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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import org.apache.poi.hwpf.model.StyleDescription;
import org.apache.poi.hwpf.model.StyleSheet;
import org.apache.poi.hwpf.usermodel.CharacterRun;
import org.apache.poi.hwpf.usermodel.Paragraph;
import org.apache.poi.hwpf.usermodel.Range;

public final class Word2Forrest
{
  Writer _out;
  HWPFDocument _doc;

  @SuppressWarnings("unused")
  public Word2Forrest(HWPFDocument doc, OutputStream stream) throws IOException
  {
      _out = new OutputStreamWriter (stream, StandardCharsets.UTF_8);
    _doc = doc;

    init ();
    openDocument ();
    openBody ();

    Range r = doc.getRange ();
    StyleSheet styleSheet = doc.getStyleSheet ();

    int sectionLevel = 0;
    int lenParagraph = r.numParagraphs ();
    boolean inCode = false;
    for (int x = 0; x < lenParagraph; x++)
    {
      Paragraph p = r.getParagraph (x);
      String text = p.text ();
      if (text.trim ().length () == 0)
      {
        continue;
      }
      StyleDescription paragraphStyle = styleSheet.getStyleDescription (p.
        getStyleIndex ());
      String styleName = paragraphStyle.getName();
      if (styleName.startsWith ("Heading"))
      {
        if (inCode)
        {
          closeSource();
          inCode = false;
        }

        int headerLevel = Integer.parseInt (styleName.substring (8));
        if (headerLevel > sectionLevel)
        {
          openSection ();
        }
        else
        {
          for (int y = 0; y < (sectionLevel - headerLevel) + 1; y++)
          {
            closeSection ();
          }
          openSection ();
        }
        sectionLevel = headerLevel;
        openTitle ();
        writePlainText (text);
        closeTitle ();
      }
      else
      {
        int cruns = p.numCharacterRuns ();
        CharacterRun run = p.getCharacterRun (0);
        String fontName = run.getFontName();
        if (fontName.startsWith ("Courier"))
        {
          if (!inCode)
          {
            openSource ();
            inCode = true;
          }
          writePlainText (p.text());
        }
        else
        {
          if (inCode)
          {
            inCode = false;
            closeSource();
          }
          openParagraph();
          writePlainText(p.text());
          closeParagraph();
        }
      }
    }
    for (int x = 0; x < sectionLevel; x++)
    {
      closeSection();
    }
    closeBody();
    closeDocument();
    _out.flush();

  }

    public void init ()
      throws IOException
    {
      _out.write ("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n");
      _out.write ("<!DOCTYPE document PUBLIC \"-//APACHE//DTD Documentation V1.1//EN\" \"./dtd/document-v11.dtd\">\r\n");
    }

    public void openDocument ()
      throws IOException
    {
      _out.write ("<document>\r\n");
    }
    public void closeDocument ()
      throws IOException
    {
      _out.write ("</document>\r\n");
    }


    public void openBody ()
      throws IOException
    {
      _out.write ("<body>\r\n");
    }

    public void closeBody ()
      throws IOException
    {
      _out.write ("</body>\r\n");
    }


    public void openSection ()
      throws IOException
    {
      _out.write ("<section>");

    }

    public void closeSection ()
      throws IOException
    {
      _out.write ("</section>");

    }

    public void openTitle ()
      throws IOException
    {
      _out.write ("<title>");
    }

    public void closeTitle ()
      throws IOException
    {
      _out.write ("</title>");
    }

    public void writePlainText (String text)
      throws IOException
    {
      _out.write (text);
    }

    public void openParagraph ()
      throws IOException
    {
      _out.write ("<p>");
    }

    public void closeParagraph ()
      throws IOException
    {
      _out.write ("</p>");
    }

    public void openSource ()
      throws IOException
    {
      _out.write ("<source><![CDATA[");
    }
    public void closeSource ()
      throws IOException
    {
      _out.write ("]]></source>");
    }


    public static void main(String[] args) throws IOException {
      try (InputStream is = new FileInputStream(args[0]);
           OutputStream out = new FileOutputStream("test.xml")) {
        new Word2Forrest(new HWPFDocument(is), out);
      }
    }
}

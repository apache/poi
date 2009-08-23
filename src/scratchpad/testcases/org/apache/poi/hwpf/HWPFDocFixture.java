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

import org.apache.poi.hwpf.model.FileInformationBlock;
import org.apache.poi.poifs.filesystem.DocumentEntry;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.POIDataSamples;


public final class HWPFDocFixture
{
  public byte[] _tableStream;
  public byte[] _mainStream;
  public FileInformationBlock _fib;

  public HWPFDocFixture(Object obj)
  {

  }

  public void setUp()
  {
    try
    {
      POIFSFileSystem filesystem = new POIFSFileSystem(
              POIDataSamples.getDocumentInstance().openResourceAsStream("test.doc"));

      DocumentEntry documentProps =
        (DocumentEntry) filesystem.getRoot().getEntry("WordDocument");
      _mainStream = new byte[documentProps.getSize()];
      filesystem.createDocumentInputStream("WordDocument").read(_mainStream);

      // use the fib to determine the name of the table stream.
      _fib = new FileInformationBlock(_mainStream);

      String name = "0Table";
      if (_fib.isFWhichTblStm())
      {
        name = "1Table";
      }

      // read in the table stream.
      DocumentEntry tableProps =
        (DocumentEntry) filesystem.getRoot().getEntry(name);
      _tableStream = new byte[tableProps.getSize()];
      filesystem.createDocumentInputStream(name).read(_tableStream);

      _fib.fillVariableFields(_mainStream, _tableStream);
    }
    catch (Throwable t)
    {
      t.printStackTrace();
    }
  }

  public void tearDown()
  {
  }

}

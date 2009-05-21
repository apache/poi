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

package org.apache.poi.hdf.model.util;

import org.apache.poi.hdf.model.hdftypes.FormattedDiskPage;

public final class ParsingState
{

  //int _numPages;// = charPlcf.length();
  int _currentPageIndex = 0;
  FormattedDiskPage _fkp;// = new CHPFormattedDiskPage(fkp);
  int _currentPropIndex = 0;
  //int _currentArraySize;// = cfkp.size();

  public ParsingState(int firstPage, FormattedDiskPage fkp)
  {
    _fkp = fkp;
  }
  //public int getCurrentPage()
  //{
  //  return _currentPage;
  //}
  //public int getNumPages()
  //{
  //  return _numPages;
  //}
  public int getCurrentPageIndex()
  {
    return _currentPageIndex;
  }
  public FormattedDiskPage getFkp()
  {
    return _fkp;
  }
  public int getCurrentPropIndex()
  {
    return _currentPropIndex;
  }

  public void setState(int currentPageIndex, FormattedDiskPage fkp, int currentPropIndex)
  {

    _currentPageIndex = currentPageIndex;
    _fkp = fkp;
    _currentPropIndex = currentPropIndex;
    //_currentArraySize = currentArraySize;
  }
}

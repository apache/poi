package org.apache.poi.hdf.model.util;

import org.apache.poi.hdf.model.hdftypes.FormattedDiskPage;

public class ParsingState
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
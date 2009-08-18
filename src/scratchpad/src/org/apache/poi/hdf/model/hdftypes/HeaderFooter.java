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

package org.apache.poi.hdf.model.hdftypes;

/**
 * Comment me
 *
 * @author Ryan Ackley
 */

public final class HeaderFooter
{
  public static final int HEADER_EVEN = 1;
  public static final int HEADER_ODD = 2;
  public static final int FOOTER_EVEN = 3;
  public static final int FOOTER_ODD = 4;
  public static final int HEADER_FIRST = 5;
  public static final int FOOTER_FIRST = 6;

  private int _type;
  private int _start;
  private int _end;

  public HeaderFooter(int type, int startFC, int endFC)
  {
    _type = type;
    _start = startFC;
    _end = endFC;
  }
  public int getStart()
  {
    return _start;
  }
  public int getEnd()
  {
    return _end;
  }
  public boolean isEmpty()
  {
    return _start - _end == 0;
  }
}

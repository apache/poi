/*
 *  ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ====================================================================
 */

package org.apache.poi.hwpf.model;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * This class provides access to all the fields Plex.
 * 
 * @author Cedric Bosdonnat <cbosdonnat@novell.com>
 *
 */
public class FieldsTables
{
  public static final int PLCFFLDATN = 0;
  public static final int PLCFFLDEDN = 1;
  public static final int PLCFFLDFTN = 2;
  public static final int PLCFFLDHDR = 3;
  public static final int PLCFFLDHDRTXBX = 4;
  public static final int PLCFFLDMOM = 5;
  public static final int PLCFFLDTXBX = 6;

  // The size in bytes of the FLD data structure
  private static final int FLD_SIZE = 2;

  private HashMap<Integer, ArrayList<PlexOfField>> _tables;

  public FieldsTables(byte[] tableStream, FileInformationBlock fib)
  {
    _tables = new HashMap<Integer, ArrayList<PlexOfField>>();

    for (int i = PLCFFLDATN; i <= PLCFFLDTXBX; i++ )
    {
      _tables.put(i, readPLCF(tableStream, fib, i));
    }
  }
  
  public ArrayList<PlexOfField> getFieldsPLCF( int type )
  {
    return _tables.get(type);
  }

  private ArrayList<PlexOfField> readPLCF(byte[] tableStream, FileInformationBlock fib, int type)
  {
    int start = 0;
    int length = 0;

    switch (type)
    {
    case PLCFFLDATN:
      start = fib.getFcPlcffldAtn();
      length = fib.getLcbPlcffldAtn();
      break;
    case PLCFFLDEDN:
      start = fib.getFcPlcffldEdn();
      length = fib.getLcbPlcffldEdn();
      break;
    case PLCFFLDFTN:
      start = fib.getFcPlcffldFtn();
      length = fib.getLcbPlcffldFtn();
      break;
    case PLCFFLDHDR:
      start = fib.getFcPlcffldHdr();
      length = fib.getLcbPlcffldHdr();
      break;
    case PLCFFLDHDRTXBX:
      start = fib.getFcPlcffldHdrtxbx();
      length = fib.getLcbPlcffldHdrtxbx();
      break;
    case PLCFFLDMOM:
      start = fib.getFcPlcffldMom();
      length = fib.getLcbPlcffldMom();
      break;
    case PLCFFLDTXBX:
      start = fib.getFcPlcffldTxbx();
      length = fib.getLcbPlcffldTxbx();
    default:
      break;
    }

    ArrayList<PlexOfField> fields = new ArrayList<PlexOfField>();

    if (start > 0 && length > 0)
    {
      PlexOfCps plcf = new PlexOfCps(tableStream, start, length, FLD_SIZE);
      fields.ensureCapacity(plcf.length());

      for ( int i = 0; i < plcf.length(); i ++ ) {
        GenericPropertyNode propNode = plcf.getProperty( i );
        PlexOfField plex = new PlexOfField( propNode.getStart(), propNode.getEnd(), propNode.getBytes() );
        fields.add( plex );
      }
    }

    return fields;
  }
}

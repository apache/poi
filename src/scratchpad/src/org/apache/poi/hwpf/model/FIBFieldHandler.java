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

package org.apache.poi.hwpf.model;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.apache.poi.util.Internal;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;

@Internal
public final class FIBFieldHandler
{
    // 154 == 0x009A; 158 == 0x009E
  public static final int STSHFORIG = 0;
  public static final int STSHF = 1;
  public static final int PLCFFNDREF = 2;
  public static final int PLCFFNDTXT = 3;
  public static final int PLCFANDREF = 4;
  public static final int PLCFANDTXT = 5;
  public static final int PLCFSED = 6;
  public static final int PLCFPAD = 7;
  public static final int PLCFPHE = 8;
  public static final int STTBGLSY = 9;
  // 234 == 0x00EA; 238 == 0x00EE
  public static final int PLCFGLSY = 10;
  // 242 == 0200F2; 246 == 0x00F6
  public static final int PLCFHDD = 11;
  public static final int PLCFBTECHPX = 12;
  public static final int PLCFBTEPAPX = 13;
  public static final int PLCFSEA = 14;
  public static final int STTBFFFN = 15;
  public static final int PLCFFLDMOM = 16;
  public static final int PLCFFLDHDR = 17;
  // 298 == 0x12A; 302 == 0x12E
  public static final int PLCFFLDFTN = 18;
  // 306 == 0x132; 310 == 0x0136
  public static final int PLCFFLDATN = 19;
  // 314 == 0x013A; 318 == 0x013E
  public static final int PLCFFLDMCR = 20;
  // 322 == 0x0142; 326 == 0x0146
  public static final int STTBFBKMK = 21;
  // 330 == 0x014A; 334 == 0x014E
  public static final int PLCFBKF = 22;
  public static final int PLCFBKL = 23;
  public static final int CMDS = 24;
  public static final int PLCMCR = 25;
  public static final int STTBFMCR = 26;
  public static final int PRDRVR = 27;
  public static final int PRENVPORT = 28;
  public static final int PRENVLAND = 29;
  public static final int WSS = 30;
  // 402 == 0x0192; 406 == 0x0196
  public static final int DOP = 31;
  public static final int STTBFASSOC = 32;
  public static final int CLX = 33;
  public static final int PLCFPGDFTN = 34;
  public static final int AUTOSAVESOURCE = 35;
  public static final int GRPXSTATNOWNERS = 36;//validated
  public static final int STTBFATNBKMK = 37;
  public static final int PLCFDOAMOM = 38;
  public static final int PLCDOAHDR = 39;
    // 474 == 0x01DA; 478 == 0x01DE
    public static final int PLCSPAMOM = 40;
    // 482 == 0x01E2; 490 == 0x01E6
    public static final int PLCSPAHDR = 41;
    public static final int PLCFATNBKF = 42;
    // 498 == 0x01F2; 502 == 0x01F6
    public static final int PLCFATNBKL = 43;
    // 506 == 0x01FA; 510 == 0x01FE
    public static final int PMS = 44;
    // 514 == 0x0202; 518 == 0x0206
    public static final int FORMFLDSTTBS = 45;
  public static final int PLCFENDREF = 46;
  public static final int PLCFENDTXT = 47;
  public static final int PLCFFLDEDN = 48;
  public static final int PLCFPGDEDN = 49;
    // 554 == 0x022A; 558 == 0x022E -- long
    public static final int DGGINFO = 50;
  public static final int STTBFRMARK = 51;
  public static final int STTBCAPTION = 52;
  public static final int STTBAUTOCAPTION = 53;
  public static final int PLCFWKB = 54;
  public static final int PLCFSPL = 55;
  public static final int PLCFTXBXTXT = 56;
    // 610 -- 0x0262; 614 == 0x0266
    public static final int PLCFFLDTXBX = 57;// validated
  public static final int PLCFHDRTXBXTXT = 58;
  public static final int PLCFFLDHDRTXBX = 59;
  public static final int STWUSER = 60;
  public static final int STTBTTMBD = 61;
  public static final int UNUSED = 62;
  public static final int PGDMOTHER = 63;
  public static final int BKDMOTHER = 64;
  public static final int PGDFTN = 65;
  public static final int BKDFTN = 66;
  public static final int PGDEDN = 67;
  public static final int BKDEDN = 68;
  public static final int STTBFINTFLD = 69;
  public static final int ROUTESLIP = 70;
  public static final int STTBSAVEDBY = 71;
  public static final int STTBFNM = 72;
  public static final int PLFLST = 73;
  @Deprecated
  public static final int PLCFLST = PLFLST;
  public static final int PLFLFO = 74;
  public static final int PLCFTXBXBKD = 75;//validated
  public static final int PLCFTXBXHDRBKD = 76;
  public static final int DOCUNDO = 77;
  public static final int RGBUSE = 78;
  public static final int USP = 79;
  public static final int USKF = 80;
  public static final int PLCUPCRGBUSE = 81;
  public static final int PLCUPCUSP = 82;
  public static final int STTBGLSYSTYLE = 83;
  public static final int PLGOSL = 84;
  public static final int PLCOCX = 85;
  public static final int PLCFBTELVC = 86;
  public static final int MODIFIED = 87;
  public static final int PLCFLVC = 88;
  public static final int PLCASUMY = 89;
  public static final int PLCFGRAM = 90;
  public static final int STTBLISTNAMES = 91;
  public static final int STTBFUSSR = 92;

  private final static POILogger log = POILogFactory.getLogger(FIBFieldHandler.class);

  private static final int FIELD_SIZE = LittleEndian.INT_SIZE * 2;

  private Map<Integer, UnhandledDataStructure> _unknownMap = new HashMap<>();
  private int[] _fields;


  FIBFieldHandler(byte[] mainStream, int offset, int cbRgFcLcb, byte[] tableStream,
                         HashSet<Integer> offsetList, boolean areKnown)
  {
    _fields = new int[cbRgFcLcb * 2];

    for (int x = 0; x < cbRgFcLcb; x++)
    {
      int fieldOffset = (x * FIELD_SIZE) + offset;
      int dsOffset = LittleEndian.getInt(mainStream, fieldOffset);
      fieldOffset += LittleEndian.INT_SIZE;
      int dsSize = LittleEndian.getInt(mainStream, fieldOffset);

      if (offsetList.contains(Integer.valueOf(x)) ^ areKnown)
      {
        if (dsSize > 0)
        {
          if (dsOffset + dsSize > tableStream.length)
          {
              if (log.check(POILogger.WARN)) {
                  log.log(POILogger.WARN, "Unhandled data structure points to outside the buffer. " +
                          "offset = " + dsOffset + ", length = " + dsSize +
                          ", buffer length = " + tableStream.length);
              }
          }
          else
          {
            UnhandledDataStructure unhandled = new UnhandledDataStructure(
              tableStream, dsOffset, dsSize);
            _unknownMap.put(Integer.valueOf(x), unhandled);
          }
        }
      }
      _fields[x*2] = dsOffset;
      _fields[(x*2) + 1] = dsSize;
    }
  }

  public void clearFields()
  {
    Arrays.fill(_fields, 0);
  }

  public int getFieldOffset(int field)
  {
    return _fields[field*2];
  }

  public int getFieldSize(int field)
  {
    return _fields[(field*2) + 1];
  }

  public void setFieldOffset(int field, int offset)
  {
    _fields[field*2] = offset;
  }

  public void setFieldSize(int field, int size)
  {
    _fields[(field*2) + 1] = size;
  }

  public int sizeInBytes()
  {
    return (_fields.length * LittleEndian.INT_SIZE);
  }

  public int getFieldsCount() {
      return _fields.length / 2;
  }
  
  void writeTo(byte[] mainStream, int offset, ByteArrayOutputStream tableStream)
    throws IOException
  {
    for (int x = 0; x < _fields.length/2; x++)
    {
      UnhandledDataStructure ds = _unknownMap.get(Integer.valueOf(x));
      if (ds != null)
      {
        _fields[x * 2] = tableStream.size();
        LittleEndian.putInt(mainStream, offset, tableStream.size());
        offset += LittleEndian.INT_SIZE;

        byte[] buf = ds.getBuf();
        tableStream.write(buf);

        _fields[(x * 2) + 1] = buf.length;
        LittleEndian.putInt(mainStream, offset, buf.length);
        offset += LittleEndian.INT_SIZE;
      }
      else
      {
        LittleEndian.putInt(mainStream, offset, _fields[x * 2]);
        offset += LittleEndian.INT_SIZE;
        LittleEndian.putInt(mainStream, offset, _fields[(x * 2) + 1]);
        offset += LittleEndian.INT_SIZE;
      }
    }
  }

    private static String leftPad( String text, int value, char padChar )
    {
        if ( text.length() >= value )
            return text;

        StringBuilder result = new StringBuilder();
        for ( int i = 0; i < ( value - text.length() ); i++ )
        {
            result.append( padChar );
        }
        result.append( text );
        return result.toString();
    }

    @Override
    public String toString()
    {
        StringBuilder result = new StringBuilder();
        result.append( "[FIBFieldHandler]:\n" );

        result.append( "\tFields:\n" );
        result.append( "\t" );
        result.append( leftPad( "Index", 8, ' ' ) );
        result.append( leftPad( "FIB offset", 15, ' ' ) );
        result.append( leftPad( "Offset", 8, ' ' ) );
        result.append( leftPad( "Size", 8, ' ' ) );
        result.append( '\n' );
        for ( int x = 0; x < _fields.length / 2; x++ )
        {
            result.append( '\t' );
            result.append( leftPad( Integer.toString( x ), 8, ' ' ) );
            result.append( leftPad( Integer.toString( 154 + x * LittleEndian.INT_SIZE * 2 ), 6, ' ' ) );
            result.append( "   0x" );
            result.append( leftPad( Integer.toHexString( 154 + x * LittleEndian.INT_SIZE * 2 ), 4, '0' ) );
            result.append( leftPad( Integer.toString( getFieldOffset( x ) ), 8, ' ' ) );
            result.append( leftPad( Integer.toString( getFieldSize( x ) ), 8, ' ' ) );

            UnhandledDataStructure structure = _unknownMap.get( Integer.valueOf( x ) );
            if ( structure != null )
            {
                result.append( " => Unknown structure of size " );
                result.append( structure.getBuf().length );
            }
            result.append( '\n' );
        }
        return result.toString();
    }
}

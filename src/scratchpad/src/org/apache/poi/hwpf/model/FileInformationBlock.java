/*
 *  ====================================================================
 *  The Apache Software License, Version 1.1
 *
 *  Copyright (c) 2003 The Apache Software Foundation.  All rights
 *  reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions
 *  are met:
 *
 *  1. Redistributions of source code must retain the above copyright
 *  notice, this list of conditions and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright
 *  notice, this list of conditions and the following disclaimer in
 *  the documentation and/or other materials provided with the
 *  distribution.
 *
 *  3. The end-user documentation included with the redistribution,
 *  if any, must include the following acknowledgment:
 *  "This product includes software developed by the
 *  Apache Software Foundation (http://www.apache.org/)."
 *  Alternately, this acknowledgment may appear in the software itself,
 *  if and wherever such third-party acknowledgments normally appear.
 *
 *  4. The names "Apache" and "Apache Software Foundation" and
 *  "Apache POI" must not be used to endorse or promote products
 *  derived from this software without prior written permission. For
 *  written permission, please contact apache@apache.org.
 *
 *  5. Products derived from this software may not be called "Apache",
 *  "Apache POI", nor may "Apache" appear in their name, without
 *  prior written permission of the Apache Software Foundation.
 *
 *  THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 *  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 *  OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 *  ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 *  USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 *  OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 *  OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 *  SUCH DAMAGE.
 *  ====================================================================
 *
 *  This software consists of voluntary contributions made by many
 *  individuals on behalf of the Apache Software Foundation.  For more
 *  information on the Apache Software Foundation, please see
 *  <http://www.apache.org/>.
 */

package org.apache.poi.hwpf.model;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.io.IOException;

import org.apache.poi.util.BitField;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.hwpf.model.io.*;


import org.apache.poi.hwpf.model.types.FIBAbstractType;

/**
 *
 * @author  andy
 */
public class FileInformationBlock extends FIBAbstractType
  implements Cloneable
{

    FIBLongHandler _longHandler;
    FIBShortHandler _shortHandler;
    FIBFieldHandler _fieldHandler;

    /** Creates a new instance of FileInformationBlock */
    public FileInformationBlock(byte[] mainDocument)
    {
        fillFields(mainDocument, 0);
    }

    public void fillVariableFields(byte[] mainDocument, byte[] tableStream)
    {
      HashSet fieldSet = new HashSet();
      fieldSet.add(new Integer(FIBFieldHandler.STSHF));
      fieldSet.add(new Integer(FIBFieldHandler.CLX));
      fieldSet.add(new Integer(FIBFieldHandler.DOP));
      fieldSet.add(new Integer(FIBFieldHandler.PLCFBTECHPX));
      fieldSet.add(new Integer(FIBFieldHandler.PLCFBTEPAPX));
      fieldSet.add(new Integer(FIBFieldHandler.PLCFSED));
      fieldSet.add(new Integer(FIBFieldHandler.PLCFLST));
      fieldSet.add(new Integer(FIBFieldHandler.PLFLFO));
      fieldSet.add(new Integer(FIBFieldHandler.STTBFFFN));
      fieldSet.add(new Integer(FIBFieldHandler.MODIFIED));


      _shortHandler = new FIBShortHandler(mainDocument);
      _longHandler = new FIBLongHandler(mainDocument, _shortHandler.START + _shortHandler.sizeInBytes());
      _fieldHandler = new FIBFieldHandler(mainDocument,
                                          _shortHandler.START + _shortHandler.sizeInBytes() + _longHandler.sizeInBytes(),
                                          tableStream, fieldSet, true);
    }

    public int getFcDop()
    {
      return _fieldHandler.getFieldOffset(FIBFieldHandler.DOP);
    }

    public void setFcDop(int fcDop)
    {
      _fieldHandler.setFieldOffset(FIBFieldHandler.DOP, fcDop);
    }

    public int getLcbDop()
    {
      return _fieldHandler.getFieldSize(FIBFieldHandler.DOP);
    }

    public void setLcbDop(int lcbDop)
    {
      _fieldHandler.setFieldSize(FIBFieldHandler.DOP, lcbDop);
    }

    public int getFcStshf()
    {
      return _fieldHandler.getFieldOffset(FIBFieldHandler.STSHF);
    }

    public int getLcbStshf()
    {
      return _fieldHandler.getFieldSize(FIBFieldHandler.STSHF);
    }

    public void setFcStshf(int fcStshf)
    {
      _fieldHandler.setFieldOffset(FIBFieldHandler.STSHF, fcStshf);
    }

    public void setLcbStshf(int lcbStshf)
    {
      _fieldHandler.setFieldSize(FIBFieldHandler.STSHF, lcbStshf);
    }

    public int getFcClx()
    {
      return _fieldHandler.getFieldOffset(FIBFieldHandler.CLX);
    }

    public int getLcbClx()
    {
      return _fieldHandler.getFieldSize(FIBFieldHandler.CLX);
    }

    public void setFcClx(int fcClx)
    {
      _fieldHandler.setFieldOffset(FIBFieldHandler.CLX, fcClx);
    }

    public void setLcbClx(int lcbClx)
    {
      _fieldHandler.setFieldSize(FIBFieldHandler.CLX, lcbClx);
    }

    public int getFcPlcfbteChpx()
    {
      return _fieldHandler.getFieldOffset(FIBFieldHandler.PLCFBTECHPX);
    }

    public int getLcbPlcfbteChpx()
    {
      return _fieldHandler.getFieldSize(FIBFieldHandler.PLCFBTECHPX);
    }

    public void setFcPlcfbteChpx(int fcPlcfBteChpx)
    {
      _fieldHandler.setFieldOffset(FIBFieldHandler.PLCFBTECHPX, fcPlcfBteChpx);
    }

    public void setLcbPlcfbteChpx(int lcbPlcfBteChpx)
    {
      _fieldHandler.setFieldSize(FIBFieldHandler.PLCFBTECHPX, lcbPlcfBteChpx);
    }

    public int getFcPlcfbtePapx()
    {
      return _fieldHandler.getFieldOffset(FIBFieldHandler.PLCFBTEPAPX);
    }

    public int getLcbPlcfbtePapx()
    {
      return _fieldHandler.getFieldSize(FIBFieldHandler.PLCFBTEPAPX);
    }

    public void setFcPlcfbtePapx(int fcPlcfBtePapx)
    {
      _fieldHandler.setFieldOffset(FIBFieldHandler.PLCFBTEPAPX, fcPlcfBtePapx);
    }

    public void setLcbPlcfbtePapx(int lcbPlcfBtePapx)
    {
      _fieldHandler.setFieldSize(FIBFieldHandler.PLCFBTEPAPX, lcbPlcfBtePapx);
    }

    public int getFcPlcfsed()
    {
      return _fieldHandler.getFieldOffset(FIBFieldHandler.PLCFSED);
    }

    public int getLcbPlcfsed()
    {
      return _fieldHandler.getFieldSize(FIBFieldHandler.PLCFSED);
    }

    public void setFcPlcfsed(int fcPlcfSed)
    {
      _fieldHandler.setFieldOffset(FIBFieldHandler.PLCFSED, fcPlcfSed);
    }

    public void setLcbPlcfsed(int lcbPlcfSed)
    {
      _fieldHandler.setFieldSize(FIBFieldHandler.PLCFSED, lcbPlcfSed);
    }

    public int getFcPlcfLst()
    {
      return _fieldHandler.getFieldOffset(FIBFieldHandler.PLCFLST);
    }

    public int getLcbPlcfLst()
    {
      return _fieldHandler.getFieldSize(FIBFieldHandler.PLCFLST);
    }

    public void setFcPlcfLst(int fcPlcfLst)
    {
      _fieldHandler.setFieldOffset(FIBFieldHandler.PLCFLST, fcPlcfLst);
    }

    public void setLcbPlcfLst(int lcbPlcfLst)
    {
      _fieldHandler.setFieldSize(FIBFieldHandler.PLCFLST, lcbPlcfLst);
    }

    public int getFcPlfLfo()
    {
      return _fieldHandler.getFieldOffset(FIBFieldHandler.PLFLFO);
    }

    public int getLcbPlfLfo()
    {
      return _fieldHandler.getFieldSize(FIBFieldHandler.PLFLFO);
    }

    public void setFcPlfLfo(int fcPlfLfo)
    {
      _fieldHandler.setFieldOffset(FIBFieldHandler.PLFLFO, fcPlfLfo);
    }

    public void setLcbPlfLfo(int lcbPlfLfo)
    {
      _fieldHandler.setFieldSize(FIBFieldHandler.PLFLFO, lcbPlfLfo);
    }

    public int getFcSttbfffn()
    {
      return _fieldHandler.getFieldOffset(FIBFieldHandler.STTBFFFN);
    }

    public int getLcbSttbfffn()
    {
      return _fieldHandler.getFieldSize(FIBFieldHandler.STTBFFFN);
    }

    public void setFcSttbfffn(int fcSttbFffn)
    {
      _fieldHandler.setFieldOffset(FIBFieldHandler.STTBFFFN, fcSttbFffn);
    }

    public void setLcbSttbfffn(int lcbSttbFffn)
    {
      _fieldHandler.setFieldSize(FIBFieldHandler.STTBFFFN, lcbSttbFffn);
    }

    public int getModifiedLow()
    {
      return _fieldHandler.getFieldOffset(FIBFieldHandler.PLFLFO);
    }

    public int getModifiedHigh()
    {
      return _fieldHandler.getFieldSize(FIBFieldHandler.PLFLFO);
    }

    public void setModifiedLow(int modifiedLow)
    {
      _fieldHandler.setFieldOffset(FIBFieldHandler.PLFLFO, modifiedLow);
    }

    public void setModifiedHigh(int modifiedHigh)
    {
      _fieldHandler.setFieldSize(FIBFieldHandler.PLFLFO, modifiedHigh);
    }

    public void setCbMac(int cbMac)
    {
      _longHandler.setLong(FIBLongHandler.CBMAC, cbMac);
    }

    public void clearOffsetsSizes()
    {
      _fieldHandler.clearFields();
    }

    public void writeTo (byte[] mainStream, HWPFOutputStream tableStream)
      throws IOException
    {
      //HWPFOutputStream mainDocument = sys.getStream("WordDocument");
      //HWPFOutputStream tableStream = sys.getStream("1Table");

      super.serialize(mainStream, 0);

      int size = super.getSize();
      _shortHandler.serialize(mainStream);
      _longHandler.serialize(mainStream, size + _shortHandler.sizeInBytes());
      _fieldHandler.writeTo(mainStream,
        super.getSize() + _shortHandler.sizeInBytes() + _longHandler.sizeInBytes(), tableStream);

    }

    public int getSize()
    {
      return super.getSize() + _shortHandler.sizeInBytes() +
        _longHandler.sizeInBytes() + _fieldHandler.sizeInBytes();
    }
//    public Object clone()
//    {
//      try
//      {
//        return super.clone();
//      }
//      catch (CloneNotSupportedException e)
//      {
//        e.printStackTrace();
//        return null;
//      }
//    }
}


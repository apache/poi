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

package org.apache.poi.hwpf.sprm;

import org.apache.poi.hwpf.usermodel.BorderCode;
import org.apache.poi.hwpf.usermodel.SectionProperties;
import org.apache.poi.util.HexDump;
import org.apache.poi.util.Internal;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;

@Internal
public final class SectionSprmUncompressor extends SprmUncompressor
{
  private static final POILogger logger = POILogFactory.getLogger(SectionSprmUncompressor.class);
  
  public SectionSprmUncompressor()
  {
  }
  public static SectionProperties uncompressSEP(byte[] grpprl, int offset)
  {
    SectionProperties newProperties = new SectionProperties();

    SprmIterator sprmIt = new SprmIterator(grpprl, offset);

    while (sprmIt.hasNext())
    {
      SprmOperation sprm = sprmIt.next();
      unCompressSEPOperation(newProperties, sprm);
    }

    return newProperties;
  }

  /**
   * Used in decompression of a sepx. This performs an operation defined by
   * a single sprm.
   *
   * @param newSEP The SectionProperty to perform the operation on.
   * @param operand The operation to perform.
   * @param param The operation's parameter.
   * @param varParam The operation variable length parameter.
   */
  static void unCompressSEPOperation (SectionProperties newSEP, SprmOperation sprm)
  {
    final int operation = sprm.getOperation();
    final int operand = sprm.getOperand();
    switch (operation)
    {
      case 0:
        newSEP.setCnsPgn ((byte) operand);
        break;
      case 0x1:
        newSEP.setIHeadingPgn ((byte) operand);
        break;
      case 0x2:
        byte[] buf = new byte[sprm.size() - 3];
        System.arraycopy(sprm.getGrpprl(), sprm.getGrpprlOffset(), buf, 0, buf.length);
        newSEP.setOlstAnm (buf);
        break;
      case 0x3:
        //not quite sure
        break;
      case 0x4:
        //not quite sure
        break;
      case 0x5:
        newSEP.setFEvenlySpaced (getFlag (operand));
        break;
      case 0x6:
        newSEP.setFUnlocked (getFlag (operand));
        break;
      case 0x7:
        newSEP.setDmBinFirst ((short) operand);
        break;
      case 0x8:
        newSEP.setDmBinOther ((short) operand);
        break;
      case 0x9:
        newSEP.setBkc ((byte) operand);
        break;
      case 0xa:
        newSEP.setFTitlePage (getFlag (operand));
        break;
      case 0xb:
        newSEP.setCcolM1 ((short) operand);
        break;
      case 0xc:
        newSEP.setDxaColumns (operand);
        break;
      case 0xd:
        newSEP.setFAutoPgn (getFlag (operand));
        break;
      case 0xe:
        newSEP.setNfcPgn ((byte) operand);
        break;
      case 0xf:
        newSEP.setDyaPgn ((short) operand);
        break;
      case 0x10:
        newSEP.setDxaPgn ((short) operand);
        break;
      case 0x11:
        newSEP.setFPgnRestart (getFlag (operand));
        break;
      case 0x12:
        newSEP.setFEndNote (getFlag (operand));
        break;
      case 0x13:
        newSEP.setLnc ((byte) operand);
        break;
      case 0x14:
        newSEP.setGrpfIhdt ((byte) operand);
        break;
      case 0x15:
        newSEP.setNLnnMod ((short) operand);
        break;
      case 0x16:
        newSEP.setDxaLnn (operand);
        break;
      case 0x17:
        newSEP.setDyaHdrTop (operand);
        break;
      case 0x18:
        newSEP.setDyaHdrBottom (operand);
        break;
      case 0x19:
        newSEP.setFLBetween (getFlag (operand));
        break;
      case 0x1a:
        newSEP.setVjc ((byte) operand);
        break;
      case 0x1b:
        newSEP.setLnnMin ((short) operand);
        break;
      case 0x1c:
        newSEP.setPgnStart ((short) operand);
        break;
      case 0x1d:
        newSEP.setDmOrientPage( operand != 0 );
        break;
      case 0x1e:

        //nothing
        break;
      case 0x1f:
        newSEP.setXaPage (operand);
        break;
      case 0x20:
        newSEP.setYaPage (operand);
        break;
      case 0x21:
        newSEP.setDxaLeft (operand);
        break;
      case 0x22:
        newSEP.setDxaRight (operand);
        break;
      case 0x23:
        newSEP.setDyaTop (operand);
        break;
      case 0x24:
        newSEP.setDyaBottom (operand);
        break;
      case 0x25:
        newSEP.setDzaGutter (operand);
        break;
      case 0x26:
        newSEP.setDmPaperReq ((short) operand);
        break;
      case 0x27:
        newSEP.setFPropMark (getFlag (operand));
        break;
      case 0x28:
        break;
      case 0x29:
        break;
      case 0x2a:
        break;
      case 0x2b:
        newSEP.setBrcTop(new BorderCode(sprm.getGrpprl(), sprm.getGrpprlOffset()));
        break;
      case 0x2c:
        newSEP.setBrcLeft(new BorderCode(sprm.getGrpprl(), sprm.getGrpprlOffset()));
        break;
      case 0x2d:
        newSEP.setBrcBottom(new BorderCode(sprm.getGrpprl(), sprm.getGrpprlOffset()));
        break;
      case 0x2e:
        newSEP.setBrcRight(new BorderCode(sprm.getGrpprl(), sprm.getGrpprlOffset()));
        break;
      case 0x2f:
        newSEP.setPgbProp (operand);
        break;
      case 0x30:
        newSEP.setDxtCharSpace (operand);
        break;
      case 0x31:
        newSEP.setDyaLinePitch (operand);
        break;
      case 0x33:
        newSEP.setWTextFlow ((short) operand);
        break;
      case 0x3C:
        // [MS-DOC], v20140721, 2.6.4, sprmSRncFtn        
        newSEP.setRncFtn((short) operand);
        break;
      case 0x3E:
        // [MS-DOC], v20140721, 2.6.4, sprmSRncEdn        
        newSEP.setRncEdn((short) operand);
        break;
      case 0x3F:
        // [MS-DOC], v20140721, 2.6.4, sprmSNFtn
        newSEP.setNFtn(operand);
        break;
      case 0x40:
        // [MS-DOC], v20140721, 2.6.4, sprmSNFtnRef
        newSEP.setNfcFtnRef(operand);
        break;
      case 0x41:
        // [MS-DOC], v20140721, 2.6.4, sprmSNEdn
        newSEP.setNEdn(operand);
        break;
      case 0x42:
        // [MS-DOC], v20140721, 2.6.4, sprmSNEdnRef
        newSEP.setNfcEdnRef(operand);
        break;
      default:
        logger.log(POILogger.INFO, "Unsupported Sprm operation: " + operation + " (" + HexDump.byteToHex(operation) + ")");
        break;
    }

  }


}

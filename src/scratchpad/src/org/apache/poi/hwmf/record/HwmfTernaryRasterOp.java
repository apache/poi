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

package org.apache.poi.hwmf.record;

/**
 * Each ternary raster operation code represents a Boolean operation in which the values of the pixels in
 * the source, the selected brush, and the destination are combined. Following are the three operands
 * used in these operations.
 *
 * <table>
 * <tr><th>Operand</th><th>Meaning</th></tr>
 * <tr><td>D</td><td>Destination bitmap</td></tr>
 * <tr><td>P</td><td>Selected brush (also called pattern)</td></tr>
 * <tr><td>S</td><td>Source bitmap</td></tr>
 * </table>
 *
 * Following are the Boolean operators used in these operations.
 * <table>
 * <tr><th>Operand</th><th>Meaning</th></tr>
 * <tr><td>a</td><td>Bitwise AND</td></tr>
 * <tr><td>n</td><td>Bitwise NOT (inverse)</td></tr>
 * <tr><td>o</td><td>Bitwise OR</td></tr>
 * <tr><td>x</td><td>Bitwise exclusive OR (XOR)</td></tr>
 * </table>
 *
 * All Boolean operations are presented in reverse Polish notation. For example, the following operation
 * replaces the values of the pixels in the destination bitmap with a combination of the pixel values of the
 * source and brush: PSo.
 * 
 * The following operation combines the values of the pixels in the source and brush with the pixel values
 * of the destination bitmap: DPSoo (there are alternative spellings of some functions, so although a
 * particular spelling MAY NOT be listed in the enumeration, an equivalent form SHOULD be).
 * 
 * Each raster operation code is a 32-bit integer whose high-order word is a Boolean operation index and
 * whose low-order word is the operation code. The 16-bit operation index is a zero-extended, 8-bit
 * value that represents the result of the Boolean operation on predefined brush, source, and destination
 * values. For example, the operation indexes for the PSo and DPSoo operations are shown in the
 * following list.
 * 
 * <table>
 * <tr><th>P</th><th>S</th><th>D</th><th>DPo</th><th>DPan</th></tr>
 * <tr><td>0</td><td>0</td><td>0</td><td>0</td><td>0</td></tr>
 * <tr><td>0</td><td>0</td><td>1</td><td>0</td><td>1</td></tr>
 * <tr><td>0</td><td>1</td><td>0</td><td>1</td><td>1</td></tr>
 * <tr><td>0</td><td>1</td><td>1</td><td>1</td><td>1</td></tr>
 * <tr><td>1</td><td>0</td><td>0</td><td>1</td><td>1</td></tr>
 * <tr><td>1</td><td>0</td><td>1</td><td>1</td><td>1</td></tr>
 * <tr><td>1</td><td>1</td><td>0</td><td>1</td><td>1</td></tr>
 * <tr><td>1</td><td>1</td><td>1</td><td>1</td><td>1</td></tr>
 * </table>
 * 
 * The operation indexes are determined by reading the binary values in a column of the table from the
 * bottom up. For example, in the PSo column, the binary value is 11111100, which is equivalent to 00FC
 * (hexadecimal is implicit for these values), which is the operation index for PSo.
 * 
 * Using this method, DPSoo can be seen to have the operation index 00FE. Operation indexes define the
 * locations of corresponding raster operation codes in the preceding enumeration. The PSo operation is
 * in line 252 (0x00FC) of the enumeration; DPSoo is in line 254 (0x00FE).
 * 
 * The most commonly used raster operations have been given explicit enumeration names, which
 * SHOULD be used; examples are PATCOPY and WHITENESS.
 * 
 * When the source and destination bitmaps are monochrome, a bit value of 0 represents a black pixel
 * and a bit value of 1 represents a white pixel. When the source and the destination bitmaps are color,
 * those colors are represented with red green blue (RGB) values.
 */
public enum HwmfTernaryRasterOp {
    BLACKNESS(0x0000,0x0042,"0"),
    DPSOON(0x0001,0x0289,"DPSoon"),
    DPSONA(0x0002,0x0C89,"DPSona"),
    PSON(0x0003,0x00AA,"PSon"),
    SDPONA(0x0004,0x0C88,"SDPona"),
    DPON(0x0005,0x00A9,"DPon"),
    PDSXNON(0x0006,0x0865,"PDSxnon"),
    PDSAON(0x0007,0x02C5,"PDSaon"),
    SDPNAA(0x0008,0x0F08,"SDPnaa"),
    PDSXON(0x0009,0x0245,"PDSxon"),
    DPNA(0x000A,0x0329,"DPna"),
    PSDNAON(0x000B,0x0B2A,"PSDnaon"),
    SPNA(0x000C,0x0324,"SPna"),
    PDSNAON(0x000D,0x0B25,"PDSnaon"),
    PDSONON(0x000E,0x08A5,"PDSonon"),
    PN(0x000F,0x0001,"Pn"),
    PDSONA(0x0010,0x0C85,"PDSona"),
    NOTSRCERASE(0x0011,0x00A6,"DSon"),
    SDPXNON(0x0012,0x0868,"SDPxnon"),
    SDPAON(0x0013,0x02C8,"SDPaon"),
    DPSXNON(0x0014,0x0869,"DPSxnon"),
    DPSAON(0x0015,0x02C9,"DPSaon"),
    PSDPSANAXX(0x0016,0x5CCA,"PSDPSanaxx"),
    SSPXDSXAXN(0x0017,0x1D54,"SSPxDSxaxn"),
    SPXPDXA(0x0018,0x0D59,"SPxPDxa"),
    SDPSANAXN(0x0019,0x1CC8,"SDPSanaxn"),
    PDSPAOX(0x001A,0x06C5,"PDSPaox"),
    SDPSXAXN(0x001B,0x0768,"SDPSxaxn"),
    PSDPAOX(0x001C,0x06CA,"PSDPaox"),
    DSPDXAXN(0x001D,0x0766,"DSPDxaxn"),
    PDSOX(0x001E,0x01A5,"PDSox"),
    PDSOAN(0x001F,0x0385,"PDSoan"),
    DPSNAA(0x0020,0x0F09,"DPSnaa"),
    SDPXON(0x0021,0x0248,"SDPxon"),
    DSNA(0x0022,0x0326,"DSna"),
    SPDNAON(0x0023,0x0B24,"SPDnaon"),
    SPXDSXA(0x0024,0x0D55,"SPxDSxa"),
    PDSPANAXN(0x0025,0x1CC5,"PDSPanaxn"),
    SDPSAOX(0x0026,0x06C8,"SDPSaox"),
    SDPSXNOX(0x0027,0x1868,"SDPSxnox"),
    DPSXA(0x0028,0x0369,"DPSxa"),
    PSDPSAOXXN(0x0029,0x16CA,"PSDPSaoxxn"),
    DPSANA(0x002A,0x0CC9,"DPSana"),
    SSPXPDXAXN(0x002B,0x1D58,"SSPxPDxaxn"),
    SPDSOAX(0x002C,0x0784,"SPDSoax"),
    PSDNOX(0x002D,0x060A,"PSDnox"),
    PSDPXOX(0x002E,0x064A,"PSDPxox"),
    PSDNOAN(0x002F,0x0E2A,"PSDnoan"),
    PSNA(0x0030,0x032A,"PSna"),
    SDPNAON(0x0031,0x0B28,"SDPnaon"),
    SDPSOOX(0x0032,0x0688,"SDPSoox"),
    NOTSRCCOPY(0x0033,0x0008,"Sn"),
    SPDSAOX(0x0034,0x06C4,"SPDSaox"),
    SPDSXNOX(0x0035,0x1864,"SPDSxnox"),
    SDPOX(0x0036,0x01A8,"SDPox"),
    SDPOAN(0x0037,0x0388,"SDPoan"),
    PSDPOAX(0x0038,0x078A,"PSDPoax"),
    SPDNOX(0x0390,0x604,"SPDnox"),
    SPDSXOX(0x003A,0x0644,"SPDSxox"),
    SPDNOAN(0x003B,0x0E24,"SPDnoan"),
    PSX(0x003C,0x004A,"PSx"),
    SPDSONOX(0x003D,0x18A4,"SPDSonox"),
    SPDSNAOX(0x003E,0x1B24,"SPDSnaox"),
    PSAN(0x003F,0x00EA,"PSan"),
    PSDNAA(0x0040,0x0F0A,"PSDnaa"),
    DPSXON(0x0041,0x0249,"DPSxon"),
    SDXPDXA(0x0042,0x0D5D,"SDxPDxa"),
    SPDSANAXN(0x0043,0x1CC4,"SPDSanaxn"),
    SRCERASE(0x0044,0x0328,"SDna"),
    DPSNAON(0x0045,0x0B29,"DPSnaon"),
    DSPDAOX(0x0046,0x06C6,"DSPDaox"),
    PSDPXAXN(0x0047,0x076A,"PSDPxaxn"),
    SDPXA(0x0048,0x0368,"SDPxa"),
    PDSPDAOXXN(0x0049,0x16C5,"PDSPDaoxxn"),
    DPSDOAX(0x004A,0x0789,"DPSDoax"),
    PDSNOX(0x004B,0x0605,"PDSnox"),
    SDPANA(0x004C,0x0CC8,"SDPana"),
    SSPXDSXOXN(0x004D,0x1954,"SSPxDSxoxn"),
    PDSPXOX(0x004E,0x0645,"PDSPxox"),
    PDSNOAN(0x004F,0x0E25,"PDSnoan"),
    PDNA(0x0050,0x0325,"PDna"),
    DSPNAON(0x0051,0x0B26,"DSPnaon"),
    DPSDAOX(0x0052,0x06C9,"DPSDaox"),
    SPDSXAXN(0x0053,0x0764,"SPDSxaxn"),
    DPSONON(0x0054,0x08A9,"DPSonon"),
    DSTINVERT(0x0055,0x0009,"Dn"),
    DPSOX(0x0056,0x01A9,"DPSox"),
    DPSOAN(0x0005,0x70389,"DPSoan"),
    PDSPOAX(0x0058,0x0785,"PDSPoax"),
    DPSNOX(0x0059,0x0609,"DPSnox"),
    PATINVERT(0x005A,0x0049,"DPx"),
    DPSDONOX(0x005B,0x18A9,"DPSDonox"),
    DPSDXOX(0x005C,0x0649,"DPSDxox"),
    DPSNOAN(0x005D,0x0E29,"DPSnoan"),
    DPSDNAOX(0x005E,0x1B29,"DPSDnaox"),
    DPAN(0x005F,0x00E9,"DPan"),
    PDSXA(0x0060,0x0365,"PDSxa"),
    DSPDSAOXXN(0x0061,0x16C6,"DSPDSaoxxn"),
    DSPDOAX(0x0062,0x0786,"DSPDoax"),
    SDPNOX(0x0063,0x0608,"SDPnox"),
    SDPSOAX(0x0064,0x0788,"SDPSoax"),
    DSPNOX(0x0065,0x0606,"DSPnox"),
    SRCINVERT(0x0066,0x0046,"DSx"),
    SDPSONOX(0x0067,0x18A8,"SDPSonox"),
    DSPDSONOXXN(0x0068,0x58A6,"DSPDSonoxxn"),
    PDSXXN(0x0069,0x0145,"PDSxxn"),
    DPSAX(0x006A,0x01E9,"DPSax"),
    PSDPSOAXXN(0x006B,0x178A,"PSDPSoaxxn"),
    SDPAX(0x006C,0x01E8,"SDPax"),
    PDSPDOAXXN(0x006D,0x1785,"PDSPDoaxxn"),
    SDPSNOAX(0x006E,0x1E28,"SDPSnoax"),
    // PDXNAN(0x006F,0x0C65,"PDXnan"), // invalid combo
    PDSANA(0x0070,0x0CC5,"PDSana"),
    SSDXPDXAXN(0x0071,0x1D5C,"SSDxPDxaxn"),
    SDPSXOX(0x0072,0x0648,"SDPSxox"),
    SDPNOAN(0x0073,0x0E28,"SDPnoan"),
    DSPDXOX(0x0074,0x0646,"DSPDxox"),
    DSPNOAN(0x0075,0x0E26,"DSPnoan"),
    SDPSNAOX(0x0076,0x1B28,"SDPSnaox"),
    DSAN(0x0077,0x00E6,"DSan"),
    PDSAX(0x0078,0x01E5,"PDSax"),
    DSPDSOAXXN(0x0079,0x1786,"DSPDSoaxxn"),
    DPSDNOAX(0x007A,0x1E29,"DPSDnoax"),
    SDPXNAN(0x007B,0x0C68,"SDPxnan"),
    SPDSNOAX(0x007C,0x1E24,"SPDSnoax"),
    DPSXNAN(0x007D,0x0C69,"DPSxnan"),
    SPXDSXO(0x007E,0x0955,"SPxDSxo"),
    DPSAAN(0x007F,0x03C9,"DPSaan"),
    DPSAA(0x0080,0x03E9,"DPSaa"),
    SPXDSXON(0x0081,0x0975,"SPxDSxon"),
    DPSXNA(0x0082,0x0C49,"DPSxna"),
    SPDSNOAXN(0x0083,0x1E04,"SPDSnoaxn"),
    SDPXNA(0x0084,0x0C48,"SDPxna"),
    PDSPNOAXN(0x0085,0x1E05,"PDSPnoaxn"),
    DSPDSOAXX(0x0086,0x17A6,"DSPDSoaxx"),
    PDSAXN(0x0087,0x01C5,"PDSaxn"),
    SRCAND(0x0088,0x00C6,"DSa"),
    SDPSNAOXN(0x0089,0x1B08,"SDPSnaoxn"),
    DSPNOA(0x008A,0x0E06,"DSPnoa"),
    DSPDXOXN(0x008B,0x0666,"DSPDxoxn"),
    SDPNOA(0x008C,0x0E08,"SDPnoa"),
    SDPSXOXN(0x008D,0x0668,"SDPSxoxn"),
    SSDXPDXAX(0x008E,0x1D7C,"SSDxPDxax"),
    PDSANAN(0x008F,0x0CE5,"PDSanan"),
    PDSXNA(0x0090,0x0C45,"PDSxna"),
    SDPSNOAXN(0x0091,0x1E08,"SDPSnoaxn"),
    DPSDPOAXX(0x0092,0x17A9,"DPSDPoaxx"),
    SPDAXN(0x0093,0x01C4,"SPDaxn"),
    PSDPSOAXX(0x0094,0x17AA,"PSDPSoaxx"),
    DPSAXN(0x0095,0x01C9,"DPSaxn"),
    DPSXX(0x0096,0x0169,"DPSxx"),
    PSDPSONOXX(0x0097,0x588A,"PSDPSonoxx"),
    SDPSONOXN(0x0098,0x1888,"SDPSonoxn"),
    DSXN(0x0099,0x0066,"DSxn"),
    DPSNAX(0x009A,0x0709,"DPSnax"),
    SDPSOAXN(0x009B,0x07A8,"SDPSoaxn"),
    SPDNAX(0x009C,0x0704,"SPDnax"),
    DSPDOAXN(0x009D,0x07A6,"DSPDoaxn"),
    DSPDSAOXX(0x009E,0x16E6,"DSPDSaoxx"),
    PDSXAN(0x009F,0x0345,"PDSxan"),
    DPA(0x00A0,0x00C9,"DPa"),
    PDSPNAOXN(0x00A1,0x1B05,"PDSPnaoxn"),
    DPSNOA(0x00A2,0x0E09,"DPSnoa"),
    DPSDXOXN(0x00A3,0x0669,"DPSDxoxn"),
    PDSPONOXN(0x00A4,0x1885,"PDSPonoxn"),
    PDXN(0x00A5,0x0065,"PDxn"),
    DSPNAX(0x00A6,0x0706,"DSPnax"),
    PDSPOAXN(0x00A7,0x07A5,"PDSPoaxn"),
    DPSOA(0x00A8,0x03A9,"DPSoa"),
    DPSOXN(0x00A9,0x0189,"DPSoxn"),
    D(0x00AA,0x0029,"D"),
    DPSONO(0x00AB,0x0889,"DPSono"),
    SPDSXAX(0x00AC,0x0744,"SPDSxax"),
    DPSDAOXN(0x00AD,0x06E9,"DPSDaoxn"),
    DSPNAO(0x00AE,0x0B06,"DSPnao"),
    DPNO(0x00AF,0x0229,"DPno"),
    PDSNOA(0x00B0,0x0E05,"PDSnoa"),
    PDSPXOXN(0x00B1,0x0665,"PDSPxoxn"),
    SSPXDSXOX(0x00B2,0x1974,"SSPxDSxox"),
    SDPANAN(0x00B3,0x0CE8,"SDPanan"),
    PSDNAX(0x00B4,0x070A,"PSDnax"),
    DPSDOAXN(0x00B5,0x07A9,"DPSDoaxn"),
    DPSDPAOXX(0x00B6,0x16E9,"DPSDPaoxx"),
    SDPXAN(0x00B7,0x0348,"SDPxan"),
    PSDPXAX(0x00B8,0x074A,"PSDPxax"),
    DSPDAOXN(0x00B9,0x06E6,"DSPDaoxn"),
    DPSNAO(0x00BA,0x0B09,"DPSnao"),
    MERGEPAINT(0x00BB,0x0226,"DSno"),
    SPDSANAX(0x00BC,0x1CE4,"SPDSanax"),
    SDXPDXAN(0x00BD,0x0D7D,"SDxPDxan"),
    DPSXO(0x00BE,0x0269,"DPSxo"),
    DPSANO(0x00BF,0x08C9,"DPSano"),
    MERGECOPY(0x00C0,0x00CA,"PSa"),
    SPDSNAOXN(0x00C1,0x1B04,"SPDSnaoxn"),
    SPDSONOXN(0x00C2,0x1884,"SPDSonoxn"),
    PSXN(0x00C3,0x006A,"PSxn"),
    SPDNOA(0x00C4,0x0E04,"SPDnoa"),
    SPDSXOXN(0x00C5,0x0664,"SPDSxoxn"),
    SDPNAX(0x00C6,0x0708,"SDPnax"),
    PSDPOAXN(0x00C7,0x07AA,"PSDPoaxn"),
    SDPOA(0x00C8,0x03A8,"SDPoa"),
    SPDOXN(0x00C9,0x0184,"SPDoxn"),
    DPSDXAX(0x00CA,0x0749,"DPSDxax"),
    SPDSAOXN(0x00CB,0x06E4,"SPDSaoxn"),
    SRCCOPY(0x00CC,0x0020,"S"),
    SDPONO(0x00CD,0x0888,"SDPono"),
    SDPNAO(0x00CE,0x0B08,"SDPnao"),
    SPNO(0x00CF,0x0224,"SPno"),
    PSDNOA(0x00D0,0x0E0A,"PSDnoa"),
    PSDPXOXN(0x00D1,0x066A,"PSDPxoxn"),
    PDSNAX(0x00D2,0x0705,"PDSnax"),
    SPDSOAXN(0x00D3,0x07A4,"SPDSoaxn"),
    SSPXPDXAX(0x00D4,0x1D78,"SSPxPDxax"),
    DPSANAN(0x00D5,0x0CE9,"DPSanan"),
    PSDPSAOXX(0x00D6,0x16EA,"PSDPSaoxx"),
    DPSXAN(0x00D7,0x0349,"DPSxan"),
    PDSPXAX(0x00D8,0x0745,"PDSPxax"),
    SDPSAOXN(0x00D9,0x06E8,"SDPSaoxn"),
    DPSDANAX(0x00DA,0x1CE9,"DPSDanax"),
    SPXDSXAN(0x00DB,0x0D75,"SPxDSxan"),
    SPDNAO(0x00DC,0x0B04,"SPDnao"),
    SDNO(0x00DD,0x0228,"SDno"),
    SDPXO(0x00DE,0x0268,"SDPxo"),
    SDPANO(0x00DF,0x08C8,"SDPano"),
    PDSOA(0x00E0,0x03A5,"PDSoa"),
    PDSOXN(0x00E1,0x0185,"PDSoxn"),
    DSPDXAX(0x00E2,0x0746,"DSPDxax"),
    PSDPAOXN(0x00E3,0x06EA,"PSDPaoxn"),
    SDPSXAX(0x00E4,0x0748,"SDPSxax"),
    PDSPAOXN(0x00E5,0x06E5,"PDSPaoxn"),
    SDPSANAX(0x00E6,0x1CE8,"SDPSanax"),
    SPXPDXAN(0x00E7,0x0D79,"SPxPDxan"),
    SSPXDSXAX(0x00E8,0x1D74,"SSPxDSxax"),
    DSPDSANAXXN(0x00E9,0x5CE6,"DSPDSanaxxn"),
    DPSAO(0x00EA,0x02E9,"DPSao"),
    DPSXNO(0x00EB,0x0849,"DPSxno"),
    SDPAO(0x00EC,0x02E8,"SDPao"),
    SDPXNO(0x00ED,0x0848,"SDPxno"),
    SRCPAINT(0x00EE,0x0086,"DSo"),
    SDPNOO(0x00EF,0x0A08,"SDPnoo"),
    PATCOPY(0x00F0,0x0021,"P"),
    PDSONO(0x00F1,0x0885,"PDSono"),
    PDSNAO(0x00F2,0x0B05,"PDSnao"),
    PSNO(0x00F3,0x022A,"PSno"),
    PSDNAO(0x00F4,0x0B0A,"PSDnao"),
    PDNO(0x00F5,0x0225,"PDno"),
    PDSXO(0x00F6,0x0265,"PDSxo"),
    PDSANO(0x00F7,0x08C5,"PDSano"),
    PDSAO(0x00F8,0x02E5,"PDSao"),
    PDSXNO(0x00F9,0x0845,"PDSxno"),
    DPO(0x00FA,0x0089,"DPo"),
    PATPAINT(0x00FB,0x0A09,"DPSnoo"),
    PSO(0x00FC,0x008A,"PSo"),
    PSDNOO(0x00FD,0x0A0A,"PSDnoo"),
    DPSOO(0x00FE,0x02A9,"DPSoo"),
    WHITENESS(0x00FF,0x0062,"1");

    int opIndex;
    int opCode;
    String opCmd;

    HwmfTernaryRasterOp(int opIndex, int opCode, String opCmd) {
        this.opIndex=opIndex;
        this.opCode=opCode;
        this.opCmd=opCmd;
    }

    public static HwmfTernaryRasterOp valueOf(int opIndex) {
        for (HwmfTernaryRasterOp bb : HwmfTernaryRasterOp.values()) {
            if (bb.opIndex == opIndex) {
                return bb;
            }
        }
        return null;
    }

    public String describeCmd() {
        String stack[] = new String[10];
        int stackPnt = 0;

        for (char c : opCmd.toCharArray()) {
            switch (c) {
                case 'S':
                case 'D':
                case 'P':
                    stack[stackPnt++] = ""+c;
                    break;
                case 'n':
                    stack[stackPnt-1] = "not("+stack[stackPnt-1]+")";
                    break;
                case 'a':
                    stack[stackPnt-2] = "("+stack[stackPnt-1]+" and "+stack[stackPnt-2]+")";
                    stackPnt--;
                    break;
                case 'o':
                    stack[stackPnt-2] = "("+stack[stackPnt-1]+" or "+stack[stackPnt-2]+")";
                    stackPnt--;
                    break;
                case 'x':
                    stack[stackPnt-2] = "("+stack[stackPnt-1]+" xor "+stack[stackPnt-2]+")";
                    stackPnt--;
                    break;
                case '1':
                    stack[stackPnt++] = "all white";
                    break;
                case '0':
                    stack[stackPnt++] = "all black";
                    break;
                default:
                    throw new RuntimeException("unknown cmd '"+c+"'.");
            }
        }

        return stack[--stackPnt];
    }
}

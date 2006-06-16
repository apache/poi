/* ====================================================================
   Copyright 2002-2004   Apache Software Foundation

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */


package org.apache.poi.hssf.util;

import java.util.*;

/**
 * Intends to provide support for the very evil index to triplet issue and
 * will likely replace the color contants interface for HSSF 2.0.
 * This class contains static inner class members for representing colors.
 * Each color has an index (for the standard palette in Excel (tm) ),
 * native (RGB) triplet and string triplet.  The string triplet is as the
 * color would be represented by Gnumeric.  Having (string) this here is a bit of a
 * collusion of function between HSSF and the HSSFSerializer but I think its
 * a reasonable one in this case.
 *
 * @author  Andrew C. Oliver (acoliver at apache dot org)
 * @author  Brian Sanders (bsanders at risklabs dot com) - full default color palette
 */

public class HSSFColor
{
    private final static int PALETTE_SIZE = 56;
    private final static int DISTINCT_COLOR_COUNT = 46;

    /** Creates a new instance of HSSFColor */

    public HSSFColor()
    {
    }

    /**
     * this function returns all colors in a hastable.  Its not implemented as a
     * static member/staticly initialized because that would be dirty in a
     * server environment as it is intended.  This means you'll eat the time
     * it takes to create it once per request but you will not hold onto it
     * if you have none of those requests.
     *
     * @return a hashtable containing all colors mapped to their excel-style 
     * pallette index
     */
    public final static Hashtable getIndexHash() {

        Hashtable hash = new Hashtable(PALETTE_SIZE);

        hash.put(new Integer(HSSFColor.BLACK.index), new HSSFColor.BLACK());
        hash.put(new Integer(HSSFColor.BROWN.index), new HSSFColor.BROWN());
        hash.put(new Integer(HSSFColor.OLIVE_GREEN.index),
                 new HSSFColor.OLIVE_GREEN());
        hash.put(new Integer(HSSFColor.DARK_GREEN.index), new HSSFColor.DARK_GREEN());
        hash.put(new Integer(HSSFColor.DARK_TEAL.index), new HSSFColor.DARK_TEAL());
        hash.put(new Integer(HSSFColor.DARK_BLUE.index), new HSSFColor.DARK_BLUE());
        hash.put(new Integer(HSSFColor.DARK_BLUE.index2), new HSSFColor.DARK_BLUE());
        hash.put(new Integer(HSSFColor.INDIGO.index), new HSSFColor.INDIGO());
        hash.put(new Integer(HSSFColor.GREY_80_PERCENT.index),
                 new HSSFColor.GREY_80_PERCENT());
        hash.put(new Integer(HSSFColor.ORANGE.index), new HSSFColor.ORANGE());
        hash.put(new Integer(HSSFColor.DARK_YELLOW.index),
                 new HSSFColor.DARK_YELLOW());
        hash.put(new Integer(HSSFColor.GREEN.index), new HSSFColor.GREEN());
        hash.put(new Integer(HSSFColor.TEAL.index), new HSSFColor.TEAL());
        hash.put(new Integer(HSSFColor.TEAL.index2), new HSSFColor.TEAL());
        hash.put(new Integer(HSSFColor.BLUE.index), new HSSFColor.BLUE());
        hash.put(new Integer(HSSFColor.BLUE.index2), new HSSFColor.BLUE());
        hash.put(new Integer(HSSFColor.BLUE_GREY.index), new HSSFColor.BLUE_GREY());
        hash.put(new Integer(HSSFColor.GREY_50_PERCENT.index),
                 new HSSFColor.GREY_50_PERCENT());
        hash.put(new Integer(HSSFColor.RED.index), new HSSFColor.RED());
        hash.put(new Integer(HSSFColor.LIGHT_ORANGE.index),
                 new HSSFColor.LIGHT_ORANGE());
        hash.put(new Integer(HSSFColor.LIME.index), new HSSFColor.LIME());
        hash.put(new Integer(HSSFColor.SEA_GREEN.index), new HSSFColor.SEA_GREEN());
        hash.put(new Integer(HSSFColor.AQUA.index), new HSSFColor.AQUA());
        hash.put(new Integer(HSSFColor.LIGHT_BLUE.index), new HSSFColor.LIGHT_BLUE());
        hash.put(new Integer(HSSFColor.VIOLET.index), new HSSFColor.VIOLET());
        hash.put(new Integer(HSSFColor.VIOLET.index2), new HSSFColor.VIOLET());
        hash.put(new Integer(HSSFColor.GREY_40_PERCENT.index),
                 new HSSFColor.GREY_40_PERCENT());
        hash.put(new Integer(HSSFColor.PINK.index), new HSSFColor.PINK());
        hash.put(new Integer(HSSFColor.PINK.index2), new HSSFColor.PINK());
        hash.put(new Integer(HSSFColor.GOLD.index), new HSSFColor.GOLD());
        hash.put(new Integer(HSSFColor.YELLOW.index), new HSSFColor.YELLOW());
        hash.put(new Integer(HSSFColor.YELLOW.index2), new HSSFColor.YELLOW());
        hash.put(new Integer(HSSFColor.BRIGHT_GREEN.index),
                 new HSSFColor.BRIGHT_GREEN());
        hash.put(new Integer(HSSFColor.BRIGHT_GREEN.index2),
                 new HSSFColor.BRIGHT_GREEN());
        hash.put(new Integer(HSSFColor.TURQUOISE.index), new HSSFColor.TURQUOISE());
        hash.put(new Integer(HSSFColor.TURQUOISE.index2), new HSSFColor.TURQUOISE());
        hash.put(new Integer(HSSFColor.DARK_RED.index), new HSSFColor.DARK_RED());
        hash.put(new Integer(HSSFColor.DARK_RED.index2), new HSSFColor.DARK_RED());
        hash.put(new Integer(HSSFColor.SKY_BLUE.index), new HSSFColor.SKY_BLUE());
        hash.put(new Integer(HSSFColor.PLUM.index), new HSSFColor.PLUM());
        hash.put(new Integer(HSSFColor.PLUM.index2), new HSSFColor.PLUM());
        hash.put(new Integer(HSSFColor.GREY_25_PERCENT.index),
                 new HSSFColor.GREY_25_PERCENT());
        hash.put(new Integer(HSSFColor.ROSE.index), new HSSFColor.ROSE());
        hash.put(new Integer(HSSFColor.LIGHT_YELLOW.index),
                 new HSSFColor.LIGHT_YELLOW());
        hash.put(new Integer(HSSFColor.LIGHT_GREEN.index),
                 new HSSFColor.LIGHT_GREEN());
        hash.put(new Integer(HSSFColor.LIGHT_TURQUOISE.index),
                 new HSSFColor.LIGHT_TURQUOISE());
        hash.put(new Integer(HSSFColor.LIGHT_TURQUOISE.index2),
                 new HSSFColor.LIGHT_TURQUOISE());
        hash.put(new Integer(HSSFColor.PALE_BLUE.index), new HSSFColor.PALE_BLUE());
        hash.put(new Integer(HSSFColor.LAVENDER.index), new HSSFColor.LAVENDER());
        hash.put(new Integer(HSSFColor.WHITE.index), new HSSFColor.WHITE());
        hash.put(new Integer(HSSFColor.CORNFLOWER_BLUE.index),
                 new HSSFColor.CORNFLOWER_BLUE());
        hash.put(new Integer(HSSFColor.LEMON_CHIFFON.index),
                 new HSSFColor.LEMON_CHIFFON());
        hash.put(new Integer(HSSFColor.MAROON.index), new HSSFColor.MAROON());
        hash.put(new Integer(HSSFColor.ORCHID.index), new HSSFColor.ORCHID());
        hash.put(new Integer(HSSFColor.CORAL.index), new HSSFColor.CORAL());
        hash.put(new Integer(HSSFColor.ROYAL_BLUE.index), new HSSFColor.ROYAL_BLUE());
        hash.put(new Integer(HSSFColor.LIGHT_CORNFLOWER_BLUE.index),
                 new HSSFColor.LIGHT_CORNFLOWER_BLUE());
	return hash;
    }

    /**
     * this function returns all colors in a hastable.  Its not implemented as a
     * static member/staticly initialized because that would be dirty in a
     * server environment as it is intended.  This means you'll eat the time
     * it takes to create it once per request but you will not hold onto it
     * if you have none of those requests.
     *
     * @return a hashtable containing all colors mapped to their gnumeric-like
     * triplet string
     */

    public final static Hashtable getTripletHash()
    {
        Hashtable hash = new Hashtable(DISTINCT_COLOR_COUNT);

        hash.put(HSSFColor.BLACK.hexString, new HSSFColor.BLACK());
        hash.put(HSSFColor.BROWN.hexString, new HSSFColor.BROWN());
        hash.put(HSSFColor.OLIVE_GREEN.hexString,
                 new HSSFColor.OLIVE_GREEN());
        hash.put(HSSFColor.DARK_GREEN.hexString, new HSSFColor.DARK_GREEN());
        hash.put(HSSFColor.DARK_TEAL.hexString, new HSSFColor.DARK_TEAL());
        hash.put(HSSFColor.DARK_BLUE.hexString, new HSSFColor.DARK_BLUE());
        hash.put(HSSFColor.INDIGO.hexString, new HSSFColor.INDIGO());
        hash.put(HSSFColor.GREY_80_PERCENT.hexString,
                 new HSSFColor.GREY_80_PERCENT());
        hash.put(HSSFColor.ORANGE.hexString, new HSSFColor.ORANGE());
        hash.put(HSSFColor.DARK_YELLOW.hexString,
                 new HSSFColor.DARK_YELLOW());
        hash.put(HSSFColor.GREEN.hexString, new HSSFColor.GREEN());
        hash.put(HSSFColor.TEAL.hexString, new HSSFColor.TEAL());
        hash.put(HSSFColor.BLUE.hexString, new HSSFColor.BLUE());
        hash.put(HSSFColor.BLUE_GREY.hexString, new HSSFColor.BLUE_GREY());
        hash.put(HSSFColor.GREY_50_PERCENT.hexString,
                 new HSSFColor.GREY_50_PERCENT());
        hash.put(HSSFColor.RED.hexString, new HSSFColor.RED());
        hash.put(HSSFColor.LIGHT_ORANGE.hexString,
                 new HSSFColor.LIGHT_ORANGE());
        hash.put(HSSFColor.LIME.hexString, new HSSFColor.LIME());
        hash.put(HSSFColor.SEA_GREEN.hexString, new HSSFColor.SEA_GREEN());
        hash.put(HSSFColor.AQUA.hexString, new HSSFColor.AQUA());
        hash.put(HSSFColor.LIGHT_BLUE.hexString, new HSSFColor.LIGHT_BLUE());
        hash.put(HSSFColor.VIOLET.hexString, new HSSFColor.VIOLET());
        hash.put(HSSFColor.GREY_40_PERCENT.hexString,
                 new HSSFColor.GREY_40_PERCENT());
        hash.put(HSSFColor.PINK.hexString, new HSSFColor.PINK());
        hash.put(HSSFColor.GOLD.hexString, new HSSFColor.GOLD());
        hash.put(HSSFColor.YELLOW.hexString, new HSSFColor.YELLOW());
        hash.put(HSSFColor.BRIGHT_GREEN.hexString,
                 new HSSFColor.BRIGHT_GREEN());
        hash.put(HSSFColor.BRIGHT_GREEN.hexString, new HSSFColor.TURQUOISE());
        hash.put(HSSFColor.DARK_RED.hexString, new HSSFColor.DARK_RED());
        hash.put(HSSFColor.SKY_BLUE.hexString, new HSSFColor.SKY_BLUE());
        hash.put(HSSFColor.PLUM.hexString, new HSSFColor.PLUM());
        hash.put(HSSFColor.GREY_25_PERCENT.hexString,
                 new HSSFColor.GREY_25_PERCENT());
        hash.put(HSSFColor.ROSE.hexString, new HSSFColor.ROSE());
        hash.put(HSSFColor.LIGHT_YELLOW.hexString,
                 new HSSFColor.LIGHT_YELLOW());
        hash.put(HSSFColor.LIGHT_GREEN.hexString,
                 new HSSFColor.LIGHT_GREEN());
        hash.put(HSSFColor.LIGHT_TURQUOISE.hexString,
                 new HSSFColor.LIGHT_TURQUOISE());
        hash.put(HSSFColor.PALE_BLUE.hexString, new HSSFColor.PALE_BLUE());
        hash.put(HSSFColor.LAVENDER.hexString, new HSSFColor.LAVENDER());
        hash.put(HSSFColor.WHITE.hexString, new HSSFColor.WHITE());
        hash.put(HSSFColor.CORNFLOWER_BLUE.hexString, new HSSFColor.CORNFLOWER_BLUE());
        hash.put(HSSFColor.LEMON_CHIFFON.hexString, new HSSFColor.LEMON_CHIFFON());
        hash.put(HSSFColor.MAROON.hexString, new HSSFColor.MAROON());
        hash.put(HSSFColor.ORCHID.hexString, new HSSFColor.ORCHID());
        hash.put(HSSFColor.CORAL.hexString, new HSSFColor.CORAL());
        hash.put(HSSFColor.ROYAL_BLUE.hexString, new HSSFColor.ROYAL_BLUE());
        hash.put(HSSFColor.LIGHT_CORNFLOWER_BLUE.hexString,
                 new HSSFColor.LIGHT_CORNFLOWER_BLUE());
        return hash;
    }

    /**
     * @return index to the standard palette
     */

    public short getIndex()
    {
        return BLACK.index;
    }

    /**
     * @return  triplet representation like that in Excel
     */

    public short [] getTriplet()
    {
        return BLACK.triplet;
    }

    // its a hack but its a good hack

    /**
     * @return a hex string exactly like a gnumeric triplet
     */

    public String getHexString()
    {
        return BLACK.hexString;
    }

    /**
     * Class BLACK
     *
     */

    public final static class BLACK
        extends HSSFColor
    {
        public final static short   index     = 0x8;
        public final static short[] triplet   =
        {
            0, 0, 0
        };
        public final static String  hexString = "0:0:0";

        public short getIndex()
        {
            return index;
        }

        public short [] getTriplet()
        {
            return triplet;
        }

        public String getHexString()
        {
            return hexString;
        }
    }

    /**
     * Class BROWN
     *
     */

    public final static class BROWN
        extends HSSFColor
    {
        public final static short   index     = 0x3c;
        public final static short[] triplet   =
        {
            153, 51, 0
        };
        public final static String  hexString = "9999:3333:0";

        public short getIndex()
        {
            return index;
        }

        public short [] getTriplet()
        {
            return triplet;
        }

        public String getHexString()
        {
            return hexString;
        }
    }

    /**
     * Class OLIVE_GREEN
     *
     */

    public static class OLIVE_GREEN
        extends HSSFColor
    {
        public final static short   index     = 0x3b;
        public final static short[] triplet   =
        {
            51, 51, 0
        };
        public final static String  hexString = "3333:3333:0";

        public short getIndex()
        {
            return index;
        }

        public short [] getTriplet()
        {
            return triplet;
        }

        public String getHexString()
        {
            return hexString;
        }
    }

    /**
     * Class DARK_GREEN
     *
     */

    public final static class DARK_GREEN
        extends HSSFColor
    {
        public final static short   index     = 0x3a;
        public final static short[] triplet   =
        {
            0, 51, 0
        };
        public final static String  hexString = "0:3333:0";

        public short getIndex()
        {
            return index;
        }

        public short [] getTriplet()
        {
            return triplet;
        }

        public String getHexString()
        {
            return hexString;
        }
    }

    /**
     * Class DARK_TEAL
     *
     */

    public final static class DARK_TEAL
        extends HSSFColor
    {
        public final static short   index     = 0x38;
        public final static short[] triplet   =
        {
            0, 51, 102
        };
        public final static String  hexString = "0:3333:6666";

        public short getIndex()
        {
            return index;
        }

        public short [] getTriplet()
        {
            return triplet;
        }

        public String getHexString()
        {
            return hexString;
        }
    }

    /**
     * Class DARK_BLUE
     *
     */

    public final static class DARK_BLUE
        extends HSSFColor
    {
        public final static short   index     = 0x12;
        public final static short   index2    = 0x20;
        public final static short[] triplet   =
        {
            0, 0, 128
        };
        public final static String  hexString = "0:0:8080";

        public short getIndex()
        {
            return index;
        }

        public short [] getTriplet()
        {
            return triplet;
        }

        public String getHexString()
        {
            return hexString;
        }
    }

    /**
     * Class INDIGO
     *
     */

    public final static class INDIGO
        extends HSSFColor
    {
        public final static short   index     = 0x3e;
        public final static short[] triplet   =
        {
            51, 51, 153
        };
        public final static String  hexString = "3333:3333:9999";

        public short getIndex()
        {
            return index;
        }

        public short [] getTriplet()
        {
            return triplet;
        }

        public String getHexString()
        {
            return hexString;
        }
    }

    /**
     * Class GREY_80_PERCENT
     *
     */

    public final static class GREY_80_PERCENT
        extends HSSFColor
    {
        public final static short   index     = 0x3f;
        public final static short[] triplet   =
        {
            51, 51, 51
        };
        public final static String  hexString = "3333:3333:3333";

        public short getIndex()
        {
            return index;
        }

        public short [] getTriplet()
        {
            return triplet;
        }

        public String getHexString()
        {
            return hexString;
        }
    }

    /**
     * Class DARK_RED
     *
     */

    public final static class DARK_RED
        extends HSSFColor
    {
        public final static short   index     = 0x10;
        public final static short   index2    = 0x25;
        public final static short[] triplet   =
        {
            128, 0, 0
        };
        public final static String  hexString = "8080:0:0";

        public short getIndex()
        {
            return index;
        }

        public short [] getTriplet()
        {
            return triplet;
        }

        public String getHexString()
        {
            return hexString;
        }
    }

    /**
     * Class ORANGE
     *
     */

    public final static class ORANGE
        extends HSSFColor
    {
        public final static short   index     = 0x35;
        public final static short[] triplet   =
        {
            255, 102, 0
        };
        public final static String  hexString = "FFFF:6666:0";

        public short getIndex()
        {
            return index;
        }

        public short [] getTriplet()
        {
            return triplet;
        }

        public String getHexString()
        {
            return hexString;
        }
    }

    /**
     * Class DARK_YELLOW
     *
     */

    public final static class DARK_YELLOW
        extends HSSFColor
    {
        public final static short   index     = 0x13;
        public final static short[] triplet   =
        {
            128, 128, 0
        };
        public final static String  hexString = "8080:8080:0";

        public short getIndex()
        {
            return index;
        }

        public short [] getTriplet()
        {
            return triplet;
        }

        public String getHexString()
        {
            return hexString;
        }
    }

    /**
     * Class GREEN
     *
     */

    public final static class GREEN
        extends HSSFColor
    {
        public final static short   index     = 0x11;
        public final static short[] triplet   =
        {
            0, 128, 0
        };
        public final static String  hexString = "0:8080:0";

        public short getIndex()
        {
            return index;
        }

        public short [] getTriplet()
        {
            return triplet;
        }

        public String getHexString()
        {
            return hexString;
        }
    }

    /**
     * Class TEAL
     *
     */

    public final static class TEAL
        extends HSSFColor
    {
        public final static short   index     = 0x15;
        public final static short   index2    = 0x26;
        public final static short[] triplet   =
        {
            0, 128, 128
        };
        public final static String  hexString = "0:8080:8080";

        public short getIndex()
        {
            return index;
        }

        public short [] getTriplet()
        {
            return triplet;
        }

        public String getHexString()
        {
            return hexString;
        }
    }

    /**
     * Class BLUE
     *
     */

    public final static class BLUE
        extends HSSFColor
    {
        public final static short   index     = 0xc;
        public final static short   index2    = 0x27;
        public final static short[] triplet   =
        {
            0, 0, 255
        };
        public final static String  hexString = "0:0:FFFF";

        public short getIndex()
        {
            return index;
        }

        public short [] getTriplet()
        {
            return triplet;
        }

        public String getHexString()
        {
            return hexString;
        }
    }

    /**
     * Class BLUE_GREY
     *
     */

    public final static class BLUE_GREY
        extends HSSFColor
    {
        public final static short   index     = 0x36;
        public final static short[] triplet   =
        {
            102, 102, 153
        };
        public final static String  hexString = "6666:6666:9999";

        public short getIndex()
        {
            return index;
        }

        public short [] getTriplet()
        {
            return triplet;
        }

        public String getHexString()
        {
            return hexString;
        }
    }

    /**
     * Class GREY_50_PERCENT
     *
     */

    public final static class GREY_50_PERCENT
        extends HSSFColor
    {
        public final static short   index     = 0x17;
        public final static short[] triplet   =
        {
            128, 128, 128
        };
        public final static String  hexString = "8080:8080:8080";

        public short getIndex()
        {
            return index;
        }

        public short [] getTriplet()
        {
            return triplet;
        }

        public String getHexString()
        {
            return hexString;
        }
    }

    /**
     * Class RED
     *
     */

    public final static class RED
        extends HSSFColor
    {
        public final static short   index     = 0xa;
        public final static short[] triplet   =
        {
            255, 0, 0
        };
        public final static String  hexString = "FFFF:0:0";

        public short getIndex()
        {
            return index;
        }

        public short [] getTriplet()
        {
            return triplet;
        }

        public String getHexString()
        {
            return hexString;
        }
    }

    /**
     * Class LIGHT_ORANGE
     *
     */

    public final static class LIGHT_ORANGE
        extends HSSFColor
    {
        public final static short   index     = 0x34;
        public final static short[] triplet   =
        {
            255, 153, 0
        };
        public final static String  hexString = "FFFF:9999:0";

        public short getIndex()
        {
            return index;
        }

        public short [] getTriplet()
        {
            return triplet;
        }

        public String getHexString()
        {
            return hexString;
        }
    }

    /**
     * Class LIME
     *
     */

    public final static class LIME
        extends HSSFColor
    {
        public final static short   index     = 0x32;
        public final static short[] triplet   =
        {
            153, 204, 0
        };
        public final static String  hexString = "9999:CCCC:0";

        public short getIndex()
        {
            return index;
        }

        public short [] getTriplet()
        {
            return triplet;
        }

        public String getHexString()
        {
            return hexString;
        }
    }

    /**
     * Class SEA_GREEN
     *
     */

    public final static class SEA_GREEN
        extends HSSFColor
    {
        public final static short   index     = 0x39;
        public final static short[] triplet   =
        {
            51, 153, 102
        };
        public final static String  hexString = "3333:9999:6666";

        public short getIndex()
        {
            return index;
        }

        public short [] getTriplet()
        {
            return triplet;
        }

        public String getHexString()
        {
            return hexString;
        }
    }

    /**
     * Class AQUA
     *
     */

    public final static class AQUA
        extends HSSFColor
    {
        public final static short   index     = 0x31;
        public final static short[] triplet   =
        {
            51, 204, 204
        };
        public final static String  hexString = "3333:CCCC:CCCC";

        public short getIndex()
        {
            return index;
        }

        public short [] getTriplet()
        {
            return triplet;
        }

        public String getHexString()
        {
            return hexString;
        }
    }

    /**
     * Class LIGHT_BLUE
     *
     */

    public final static class LIGHT_BLUE
        extends HSSFColor
    {
        public final static short   index     = 0x30;
        public final static short[] triplet   =
        {
            51, 102, 255
        };
        public final static String  hexString = "3333:6666:FFFF";

        public short getIndex()
        {
            return index;
        }

        public short [] getTriplet()
        {
            return triplet;
        }

        public String getHexString()
        {
            return hexString;
        }
    }

    /**
     * Class VIOLET
     *
     */

    public final static class VIOLET
        extends HSSFColor
    {
        public final static short   index     = 0x14;
        public final static short   index2    = 0x24;
        public final static short[] triplet   =
        {
            128, 0, 128
        };
        public final static String  hexString = "8080:0:8080";

        public short getIndex()
        {
            return index;
        }

        public short [] getTriplet()
        {
            return triplet;
        }

        public String getHexString()
        {
            return hexString;
        }
    }

    /**
     * Class GREY_40_PERCENT
     *
     */

    public final static class GREY_40_PERCENT
        extends HSSFColor
    {
        public final static short   index     = 0x37;
        public final static short[] triplet   =
        {
            150, 150, 150
        };
        public final static String  hexString = "9696:9696:9696";

        public short getIndex()
        {
            return index;
        }

        public short [] getTriplet()
        {
            return triplet;
        }

        public String getHexString()
        {
            return hexString;
        }
    }

    /**
     * Class PINK
     *
     */

    public final static class PINK
        extends HSSFColor
    {
        public final static short   index     = 0xe;
        public final static short   index2    = 0x21;
        public final static short[] triplet   =
        {
            255, 0, 255
        };
        public final static String  hexString = "FFFF:0:FFFF";

        public short getIndex()
        {
            return index;
        }

        public short [] getTriplet()
        {
            return triplet;
        }

        public String getHexString()
        {
            return hexString;
        }
    }

    /**
     * Class GOLD
     *
     */

    public final static class GOLD
        extends HSSFColor
    {
        public final static short   index     = 0x33;
        public final static short[] triplet   =
        {
            255, 204, 0
        };
        public final static String  hexString = "FFFF:CCCC:0";

        public short getIndex()
        {
            return index;
        }

        public short [] getTriplet()
        {
            return triplet;
        }

        public String getHexString()
        {
            return hexString;
        }
    }

    /**
     * Class YELLOW
     *
     */

    public final static class YELLOW
        extends HSSFColor
    {
        public final static short   index     = 0xd;
        public final static short   index2    = 0x22;
        public final static short[] triplet   =
        {
            255, 255, 0
        };
        public final static String  hexString = "FFFF:FFFF:0";

        public short getIndex()
        {
            return index;
        }

        public short [] getTriplet()
        {
            return triplet;
        }

        public String getHexString()
        {
            return hexString;
        }
    }

    /**
     * Class BRIGHT_GREEN
     *
     */

    public final static class BRIGHT_GREEN
        extends HSSFColor
    {
        public final static short   index     = 0xb;
        public final static short   index2    = 0x23;
        public final static short[] triplet   =
        {
            0, 255, 0
        };
        public final static String  hexString = "0:FFFF:0";

        public short getIndex()
        {
            return index;
        }

        public String getHexString()
        {
            return hexString;
        }

        public short [] getTriplet()
        {
            return triplet;
        }
    }

    /**
     * Class TURQUOISE
     *
     */

    public final static class TURQUOISE
        extends HSSFColor
    {
        public final static short   index     = 0xf;
        public final static short   index2    = 0x23;
        public final static short[] triplet   =
        {
            0, 255, 255
        };
        public final static String  hexString = "0:FFFF:FFFF";

        public short getIndex()
        {
            return index;
        }

        public short [] getTriplet()
        {
            return triplet;
        }

        public String getHexString()
        {
            return hexString;
        }
    }

    /**
     * Class SKY_BLUE
     *
     */

    public final static class SKY_BLUE
        extends HSSFColor
    {
        public final static short   index     = 0x28;
        public final static short[] triplet   =
        {
            0, 204, 255
        };
        public final static String  hexString = "0:CCCC:FFFF";

        public short getIndex()
        {
            return index;
        }

        public short [] getTriplet()
        {
            return triplet;
        }

        public String getHexString()
        {
            return hexString;
        }
    }

    /**
     * Class PLUM
     *
     */

    public final static class PLUM
        extends HSSFColor
    {
        public final static short   index     = 0x3d;
        public final static short   index2    = 0x19;
        public final static short[] triplet   =
        {
            153, 51, 102
        };
        public final static String  hexString = "9999:3333:6666";

        public short getIndex()
        {
            return index;
        }

        public short [] getTriplet()
        {
            return triplet;
        }

        public String getHexString()
        {
            return hexString;
        }
    }

    /**
     * Class GREY_25_PERCENT
     *
     */

    public final static class GREY_25_PERCENT
        extends HSSFColor
    {
        public final static short   index     = 0x16;
        public final static short[] triplet   =
        {
            192, 192, 192
        };
        public final static String  hexString = "C0C0:C0C0:C0C0";

        public short getIndex()
        {
            return index;
        }

        public short [] getTriplet()
        {
            return triplet;
        }

        public String getHexString()
        {
            return hexString;
        }
    }

    /**
     * Class ROSE
     *
     */

    public final static class ROSE
        extends HSSFColor
    {
        public final static short   index     = 0x2d;
        public final static short[] triplet   =
        {
            255, 153, 204
        };
        public final static String  hexString = "FFFF:9999:CCCC";

        public short getIndex()
        {
            return index;
        }

        public short [] getTriplet()
        {
            return triplet;
        }

        public String getHexString()
        {
            return hexString;
        }
    }

    /**
     * Class TAN
     *
     */

    public final static class TAN
        extends HSSFColor
    {
        public final static short   index     = 0x2f;
        public final static short[] triplet   =
        {
            255, 204, 153
        };
        public final static String  hexString = "FFFF:CCCC:9999";

        public short getIndex()
        {
            return index;
        }

        public short [] getTriplet()
        {
            return triplet;
        }

        public String getHexString()
        {
            return hexString;
        }
    }

    /**
     * Class LIGHT_YELLOW
     *
     */

    public final static class LIGHT_YELLOW
        extends HSSFColor
    {
        public final static short   index     = 0x2b;
        public final static short[] triplet   =
        {
            255, 255, 153
        };
        public final static String  hexString = "FFFF:FFFF:9999";

        public short getIndex()
        {
            return index;
        }

        public short [] getTriplet()
        {
            return triplet;
        }

        public String getHexString()
        {
            return hexString;
        }
    }

    /**
     * Class LIGHT_GREEN
     *
     */

    public final static class LIGHT_GREEN
        extends HSSFColor
    {
        public final static short   index     = 0x2a;
        public final static short[] triplet   =
        {
            204, 255, 204
        };
        public final static String  hexString = "CCCC:FFFF:CCCC";

        public short getIndex()
        {
            return index;
        }

        public short [] getTriplet()
        {
            return triplet;
        }

        public String getHexString()
        {
            return hexString;
        }
    }

    /**
     * Class LIGHT_TURQUOISE
     *
     */

    public final static class LIGHT_TURQUOISE
        extends HSSFColor
    {
        public final static short   index     = 0x29;
        public final static short   index2    = 0x1b;
        public final static short[] triplet   =
        {
            204, 255, 255
        };
        public final static String  hexString = "CCCC:FFFF:FFFF";

        public short getIndex()
        {
            return index;
        }

        public short [] getTriplet()
        {
            return triplet;
        }

        public String getHexString()
        {
            return hexString;
        }
    }

    /**
     * Class PALE_BLUE
     *
     */

    public final static class PALE_BLUE
        extends HSSFColor
    {
        public final static short   index     = 0x2c;
        public final static short[] triplet   =
        {
            153, 204, 255
        };
        public final static String  hexString = "9999:CCCC:FFFF";

        public short getIndex()
        {
            return index;
        }

        public short [] getTriplet()
        {
            return triplet;
        }

        public String getHexString()
        {
            return hexString;
        }
    }

    /**
     * Class LAVENDER
     *
     */

    public final static class LAVENDER
        extends HSSFColor
    {
        public final static short   index     = 0x2e;
        public final static short[] triplet   =
        {
            204, 153, 255
        };
        public final static String  hexString = "CCCC:9999:FFFF";

        public short getIndex()
        {
            return index;
        }

        public short [] getTriplet()
        {
            return triplet;
        }

        public String getHexString()
        {
            return hexString;
        }
    }

    /**
     * Class WHITE
     *
     */

    public final static class WHITE
        extends HSSFColor
    {
        public final static short   index     = 0x9;
        public final static short[] triplet   =
        {
            255, 255, 255
        };
        public final static String  hexString = "FFFF:FFFF:FFFF";

        public short getIndex()
        {
            return index;
        }

        public short [] getTriplet()
        {
            return triplet;
        }

        public String getHexString()
        {
            return hexString;
        }
    }
    
    /**
     * Class CORNFLOWER_BLUE
     */
    public final static class CORNFLOWER_BLUE
        extends HSSFColor
    {
        public final static short   index     = 0x18;
        public final static short[] triplet   =
        {
            153, 153, 255
        };
        public final static String  hexString = "9999:9999:FFFF";

        public short getIndex()
        {
            return index;
        }

        public short [] getTriplet()
        {
            return triplet;
        }

        public String getHexString()
        {
            return hexString;
        }
    }
    
    
    /**
     * Class LEMON_CHIFFON
     */
    public final static class LEMON_CHIFFON
        extends HSSFColor
    {
        public final static short   index     = 0x1a;
        public final static short[] triplet   =
        {
            255, 255, 204
        };
        public final static String  hexString = "FFFF:FFFF:CCCC";

        public short getIndex()
        {
            return index;
        }

        public short [] getTriplet()
        {
            return triplet;
        }

        public String getHexString()
        {
            return hexString;
        }
    }
    
    /**
     * Class MAROON
     */
    public final static class MAROON
        extends HSSFColor
    {
        public final static short   index     = 0x19;
        public final static short[] triplet   =
        {
            153, 51, 102
        };
        public final static String  hexString = "9999:3333:6666";

        public short getIndex()
        {
            return index;
        }

        public short [] getTriplet()
        {
            return triplet;
        }

        public String getHexString()
        {
            return hexString;
        }
    }
    
    /**
     * Class ORCHID
     */
    public final static class ORCHID
        extends HSSFColor
    {
        public final static short   index     = 0x1c;
        public final static short[] triplet   =
        {
            102, 0, 102
        };
        public final static String  hexString = "6666:0:6666";

        public short getIndex()
        {
            return index;
        }

        public short [] getTriplet()
        {
            return triplet;
        }

        public String getHexString()
        {
            return hexString;
        }
    }
    
    /**
     * Class CORAL
     */
    public final static class CORAL
        extends HSSFColor
    {
        public final static short   index     = 0x1d;
        public final static short[] triplet   =
        {
            255, 128, 128
        };
        public final static String  hexString = "FFFF:8080:8080";

        public short getIndex()
        {
            return index;
        }

        public short [] getTriplet()
        {
            return triplet;
        }

        public String getHexString()
        {
            return hexString;
        }
    }
    
    /**
     * Class ROYAL_BLUE
     */
    public final static class ROYAL_BLUE
        extends HSSFColor
    {
        public final static short   index     = 0x1e;
        public final static short[] triplet   =
        {
            0, 102, 204
        };
        public final static String  hexString = "0:6666:CCCC";

        public short getIndex()
        {
            return index;
        }

        public short [] getTriplet()
        {
            return triplet;
        }

        public String getHexString()
        {
            return hexString;
        }
    }
    
    /**
     * Class LIGHT_CORNFLOWER_BLUE
     */
    public final static class LIGHT_CORNFLOWER_BLUE
        extends HSSFColor
    {
        public final static short   index     = 0x1f;
        public final static short[] triplet   =
        {
            204, 204, 255
        };
        public final static String  hexString = "CCCC:CCCC:FFFF";

        public short getIndex()
        {
            return index;
        }

        public short [] getTriplet()
        {
            return triplet;
        }

        public String getHexString()
        {
            return hexString;
        }
    }
}

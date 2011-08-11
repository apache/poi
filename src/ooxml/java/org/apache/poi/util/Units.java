package org.apache.poi.util;

/**
 * @author Yegor Kozlov
 */
public class Units {
    public static final int EMU_PER_PIXEL = 9525;
    public static final int EMU_PER_POINT = 12700;

    public static int toEMU(double value){
        return (int)Math.round(EMU_PER_POINT*value);
    }

    public static double toPoints(long emu){
        return (double)emu/EMU_PER_POINT;
    }
}

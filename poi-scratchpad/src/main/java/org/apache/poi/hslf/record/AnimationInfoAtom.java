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

package org.apache.poi.hslf.record;

import static org.apache.poi.util.GenericRecordUtil.getBitsAsString;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.poi.util.GenericRecordUtil;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.LittleEndian;

/**
 * An atom record that specifies the animation information for a shape.
 */
public final class AnimationInfoAtom extends RecordAtom {

    /**
     * whether the animation plays in the reverse direction
     */
    public static final int Reverse = 0x0001;
    /**
     * whether the animation starts automatically
     */
    public static final int Automatic = 0x0004;
    /**
     * whether the animation has an associated sound
     */
    public static final int Sound = 0x0010;
    /**
     * whether all playing sounds are stopped when this animation begins
     */
    public static final int StopSound = 0x0040;
    /**
     * whether an associated sound, media or action verb is activated when the shape is clicked.
     */
    public static final int Play = 0x0100;
    /**
     * specifies that the animation, while playing, stops other slide show actions.
     */
    public static final int Synchronous = 0x0400;
    /**
     * whether the shape is hidden while the animation is not playing
     */
    public static final int Hide = 0x1000;
    /**
     * whether the background of the shape is animated
     */
    public static final int AnimateBg = 0x4000;

    private static final int[] FLAGS_MASKS = {
        Reverse,
        Automatic,
        Sound,
        StopSound,
        Play,
        Synchronous,
        Hide,
        AnimateBg
    };

    private static final String[] FLAGS_NAMES = {
        "REVERSE",
        "AUTOMATIC",
        "SOUND",
        "STOP_SOUND",
        "PLAY",
        "SYNCHRONOUS",
        "HIDE",
        "ANIMATE_BG"
    };

    /**
     * Record header.
     */
    private final byte[] _header;

    /**
     * record data
     */
    private final byte[] _recdata;

    /**
     * Constructs a brand new link related atom record.
     */
    protected AnimationInfoAtom() {
        _recdata = new byte[28];

        _header = new byte[8];
        LittleEndian.putShort(_header, 0, (short)0x01);
        LittleEndian.putShort(_header, 2, (short)getRecordType());
        LittleEndian.putInt(_header, 4, _recdata.length);
    }

    /**
     * Constructs the link related atom record from its
     *  source data.
     *
     * @param source the source data as a byte array.
     * @param start the start offset into the byte array.
     * @param len the length of the slice in the byte array.
     */
    protected AnimationInfoAtom(byte[] source, int start, int len) {
        // Get the header
        _header = Arrays.copyOfRange(source, start, start+8);

        // Grab the record data
        _recdata = IOUtils.safelyClone(source,start+8, len-8, getMaxRecordLength());
    }

    /**
     * Gets the record type.
     * @return the record type.
     */
    public long getRecordType() {
        return RecordTypes.AnimationInfoAtom.typeID;
    }

    /**
     * Write the contents of the record back, so it can be written
     * to disk
     *
     * @param out the output stream to write to.
     * @throws java.io.IOException if an error occurs.
     */
    public void writeOut(OutputStream out) throws IOException {
        out.write(_header);
        out.write(_recdata);
    }

    /**
     * A rgb structure that specifies a color for the dim effect after the animation is complete.
     *
     * @return  color for the dim effect after the animation is complete
     */
    public int getDimColor(){
        return LittleEndian.getInt(_recdata, 0);
    }

    /**
     * A rgb structure that specifies a color for the dim effect after the animation is complete.
     *
     * @param rgb  color for the dim effect after the animation is complete
     */
    public void setDimColor(int rgb){
         LittleEndian.putInt(_recdata, 0, rgb);
    }

    /**
     *  A bit mask specifying options for displaying headers and footers
     *
     * @return A bit mask specifying options for displaying headers and footers
     */
    public int getMask(){
        return LittleEndian.getInt(_recdata, 4);
    }

    /**
     *  A bit mask specifying options for displaying video
     *
     * @param mask A bit mask specifying options for displaying video
     */
    public void setMask(int mask){
        LittleEndian.putInt(_recdata, 4, mask);
    }

    /**
     * @param bit the bit to check
     * @return whether the specified flag is set
     */
    public boolean getFlag(int bit){
        return (getMask() & bit) != 0;
    }

    /**
     * @param  bit the bit to set
     * @param  value whether the specified bit is set
     */
    public void setFlag(int bit, boolean value){
        int mask = getMask();
        if(value) mask |= bit;
        else mask &= ~bit;
        setMask(mask);
    }

    /**
     * A 4-byte unsigned integer that specifies a reference to a sound
     * in the SoundCollectionContainer record to locate the embedded audio
     *
     * @return  reference to a sound
     */
    public int getSoundIdRef(){
        return LittleEndian.getInt(_recdata, 8);
    }

    /**
     * A 4-byte unsigned integer that specifies a reference to a sound
     * in the SoundCollectionContainer record to locate the embedded audio
     *
     * @param id reference to a sound
     */
    public void setSoundIdRef(int id){
         LittleEndian.putInt(_recdata, 8, id);
    }

    /**
     * A signed integer that specifies the delay time, in milliseconds, before the animation starts to play.
     * If {@link #Automatic} is 0x1, this value MUST be greater than or equal to 0; otherwise, this field MUST be ignored.
     */
    public int getDelayTime(){
        return LittleEndian.getInt(_recdata, 12);
    }
    /**
     * A signed integer that specifies the delay time, in milliseconds, before the animation starts to play.
     * If {@link #Automatic} is 0x1, this value MUST be greater than or equal to 0; otherwise, this field MUST be ignored.
     */
    public void setDelayTime(int id){
         LittleEndian.putInt(_recdata, 12, id);
    }

    /**
     * A signed integer that specifies the order of the animation in the slide.
     * It MUST be greater than or equal to -2. The value -2 specifies that this animation follows the order of
     * the corresponding placeholder shape on the main master slide or title master slide.
     * The value -1 SHOULD NOT be used.
     */
    public int getOrderID(){
        return LittleEndian.getInt(_recdata, 16);
    }

    /**
     * A signed integer that specifies the order of the animation in the slide.
     * It MUST be greater than or equal to -2. The value -2 specifies that this animation follows the order of
     * the corresponding placeholder shape on the main master slide or title master slide.
     * The value -1 SHOULD NOT be used.
     */
    public void setOrderID(int id){
         LittleEndian.putInt(_recdata, 16, id);
    }

    /**
     * An unsigned integer that specifies the number of slides that this animation continues playing.
     * This field is utilized only in conjunction with media.
     * The value 0xFFFFFFFF specifies that the animation plays for one slide.
     */
    public int getSlideCount(){
        return LittleEndian.getInt(_recdata, 18);
    }

    /**
     * An unsigned integer that specifies the number of slides that this animation continues playing.
     * This field is utilized only in conjunction with media.
     * The value 0xFFFFFFFF specifies that the animation plays for one slide.
     */
    public void setSlideCount(int id){
         LittleEndian.putInt(_recdata, 18, id);
    }

    public String toString(){
        int mask = getMask();
        return
            "AnimationInfoAtom\n"+
            "\tDimColor: " + getDimColor() + "\n" +
            "\tMask: " + mask + ", 0x"+Integer.toHexString(mask)+"\n" +
            "\t  Reverse: " + getFlag(Reverse)+"\n" +
            "\t  Automatic: " + getFlag(Automatic)+"\n" +
            "\t  Sound: " + getFlag(Sound)+"\n" +
            "\t  StopSound: " + getFlag(StopSound)+"\n" +
            "\t  Play: " + getFlag(Play)+"\n" +
            "\t  Synchronous: " + getFlag(Synchronous)+"\n" +
            "\t  Hide: " + getFlag(Hide)+"\n" +
            "\t  AnimateBg: " + getFlag(AnimateBg)+"\n" +
            "\tSoundIdRef: " + getSoundIdRef() + "\n" +
            "\tDelayTime: " + getDelayTime() + "\n" +
            "\tOrderID: " + getOrderID() + "\n" +
            "\tSlideCount: " + getSlideCount() + "\n";
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties(
            "dimColor", this::getDimColor,
            "flags", getBitsAsString(this::getMask, FLAGS_MASKS, FLAGS_NAMES),
            "soundIdRef", this::getSoundIdRef,
            "delayTime", this::getDelayTime,
            "orderID", this::getOrderID,
            "slideCount", this::getSlideCount
        );
    }
}

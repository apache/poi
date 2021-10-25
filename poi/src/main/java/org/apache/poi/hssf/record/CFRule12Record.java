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

package org.apache.poi.hssf.record;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.poi.hssf.record.cf.ColorGradientFormatting;
import org.apache.poi.hssf.record.cf.ColorGradientThreshold;
import org.apache.poi.hssf.record.cf.DataBarFormatting;
import org.apache.poi.hssf.record.cf.DataBarThreshold;
import org.apache.poi.hssf.record.cf.IconMultiStateFormatting;
import org.apache.poi.hssf.record.cf.IconMultiStateThreshold;
import org.apache.poi.hssf.record.cf.Threshold;
import org.apache.poi.hssf.record.common.ExtendedColor;
import org.apache.poi.hssf.record.common.FtrHeader;
import org.apache.poi.hssf.record.common.FutureRecord;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.ss.formula.Formula;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.ss.usermodel.ConditionalFormattingThreshold.RangeType;
import org.apache.poi.ss.usermodel.IconMultiStateFormatting.IconSet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.LittleEndianOutput;

import static org.apache.logging.log4j.util.Unbox.box;
import static org.apache.poi.hssf.usermodel.HSSFWorkbook.getMaxRecordLength;

/**
 * Conditional Formatting v12 Rule Record (0x087A).
 *
 * <p>This is for newer-style Excel conditional formattings,
 *  from Excel 2007 onwards.
 *
 * <p>{@link CFRuleRecord} is used where the condition type is
 *  {@link #CONDITION_TYPE_CELL_VALUE_IS} or {@link #CONDITION_TYPE_FORMULA},
 *  this is only used for the other types
 */
public final class CFRule12Record extends CFRuleBase implements FutureRecord {

    public static final short sid = 0x087A;

    private FtrHeader futureHeader;
    private int ext_formatting_length;
    private byte[] ext_formatting_data;
    private Formula formula_scale;
    private byte ext_opts;
    private int priority;
    private int template_type;
    private byte template_param_length;
    private byte[] template_params;

    private DataBarFormatting data_bar;
    private IconMultiStateFormatting multistate;
    private ColorGradientFormatting color_gradient;
    // TODO Parse this, see #58150
    private byte[] filter_data;

    public CFRule12Record(CFRule12Record other) {
        super(other);
        futureHeader = (other.futureHeader == null) ? null : other.futureHeader.copy();

        // use min() to gracefully handle cases where the length-property and the array-length do not match
        // we saw some such files in circulation
        ext_formatting_length = Math.min(other.ext_formatting_length, other.ext_formatting_data.length);
        ext_formatting_data = other.ext_formatting_data.clone();

        formula_scale = other.formula_scale.copy();

        ext_opts = other.ext_opts;
        priority = other.priority;
        template_type = other.template_type;
        template_param_length = other.template_param_length;
        template_params = (other.template_params == null) ? null : other.template_params.clone();
        color_gradient = (other.color_gradient == null) ? null : other.color_gradient.copy();
        multistate = (other.multistate == null) ? null : other.multistate.copy();
        data_bar = (other.data_bar == null) ? null : other.data_bar.copy();
        filter_data = (other.filter_data == null) ? null : other.filter_data.clone();
    }

    private CFRule12Record(byte conditionType, byte comparisonOperation) {
        super(conditionType, comparisonOperation);
        setDefaults();
    }

    private CFRule12Record(byte conditionType, byte comparisonOperation, Ptg[] formula1, Ptg[] formula2, Ptg[] formulaScale) {
        super(conditionType, comparisonOperation, formula1, formula2);
        setDefaults();
        this.formula_scale = Formula.create(formulaScale);
    }


    private void setDefaults() {
        futureHeader = new FtrHeader();
        futureHeader.setRecordType(sid);

        ext_formatting_length = 0;
        ext_formatting_data = new byte[4];

        formula_scale = Formula.create(Ptg.EMPTY_PTG_ARRAY);

        ext_opts = 0;
        priority = 0;
        template_type = getConditionType();
        template_param_length = 16;
        template_params = IOUtils.safelyAllocate(template_param_length, getMaxRecordLength());
    }

    /**
     * Creates a new comparison operation rule
     *
     * @param sheet the sheet
     * @param formulaText the first formula text
     *
     * @return a new comparison operation rule
     */
    public static CFRule12Record create(HSSFSheet sheet, String formulaText) {
        Ptg[] formula1 = parseFormula(formulaText, sheet);
        return new CFRule12Record(CONDITION_TYPE_FORMULA, ComparisonOperator.NO_COMPARISON,
                formula1, null, null);
    }

    /**
     * Creates a new comparison operation rule
     *
     * @param sheet the sheet
     * @param comparisonOperation the comparison operation
     * @param formulaText1 the first formula text
     * @param formulaText2 the second formula text
     *
     * @return a new comparison operation rule
     */
    public static CFRule12Record create(HSSFSheet sheet, byte comparisonOperation,
            String formulaText1, String formulaText2) {
        Ptg[] formula1 = parseFormula(formulaText1, sheet);
        Ptg[] formula2 = parseFormula(formulaText2, sheet);
        return new CFRule12Record(CONDITION_TYPE_CELL_VALUE_IS, comparisonOperation,
                formula1, formula2, null);
    }

    /**
     * Creates a new comparison operation rule
     *
     * @param sheet the sheet
     * @param comparisonOperation the comparison operation
     * @param formulaText1 the first formula text
     * @param formulaText2 the second formula text
     * @param formulaTextScale the scale to apply for the comparison
     *
     * @return a new comparison operation rule
     */
    public static CFRule12Record create(HSSFSheet sheet, byte comparisonOperation,
            String formulaText1, String formulaText2, String formulaTextScale) {
        Ptg[] formula1 = parseFormula(formulaText1, sheet);
        Ptg[] formula2 = parseFormula(formulaText2, sheet);
        Ptg[] formula3 = parseFormula(formulaTextScale, sheet);
        return new CFRule12Record(CONDITION_TYPE_CELL_VALUE_IS, comparisonOperation,
                formula1, formula2, formula3);
    }

    /**
     * Creates a new Data Bar formatting
     *
     * @param sheet the sheet
     * @param color the data bar color
     *
     * @return a new Data Bar formatting
     */
    public static CFRule12Record create(HSSFSheet sheet, ExtendedColor color) {
        CFRule12Record r = new CFRule12Record(CONDITION_TYPE_DATA_BAR,
                                              ComparisonOperator.NO_COMPARISON);
        DataBarFormatting dbf = r.createDataBarFormatting();
        dbf.setColor(color);
        dbf.setPercentMin((byte)0);
        dbf.setPercentMax((byte)100);

        DataBarThreshold min = new DataBarThreshold();
        min.setType(RangeType.MIN.id);
        dbf.setThresholdMin(min);

        DataBarThreshold max = new DataBarThreshold();
        max.setType(RangeType.MAX.id);
        dbf.setThresholdMax(max);

        return r;
    }

    /**
     * Creates a new Icon Set / Multi-State formatting
     *
     * @param sheet the sheet
     * @param iconSet the icon set
     *
     * @return a new Icon Set / Multi-State formatting
     */
    public static CFRule12Record create(HSSFSheet sheet, IconSet iconSet) {
        Threshold[] ts = new Threshold[iconSet.num];
        for (int i=0; i<ts.length; i++) {
            ts[i] = new IconMultiStateThreshold();
        }

        CFRule12Record r = new CFRule12Record(CONDITION_TYPE_ICON_SET,
                                              ComparisonOperator.NO_COMPARISON);
        IconMultiStateFormatting imf = r.createMultiStateFormatting();
        imf.setIconSet(iconSet);
        imf.setThresholds(ts);
        return r;
    }

    /**
     * Creates a new Color Scale / Color Gradient formatting
     *
     * @param sheet the sheet
     *
     * @return a new Color Scale / Color Gradient formatting
     */
    public static CFRule12Record createColorScale(HSSFSheet sheet) {
        int numPoints = 3;
        ExtendedColor[] colors = new ExtendedColor[numPoints];
        ColorGradientThreshold[] ts = new ColorGradientThreshold[numPoints];
        for (int i=0; i<ts.length; i++) {
            ts[i] = new ColorGradientThreshold();
            colors[i] = new ExtendedColor();
        }

        CFRule12Record r = new CFRule12Record(CONDITION_TYPE_COLOR_SCALE,
                                              ComparisonOperator.NO_COMPARISON);
        ColorGradientFormatting cgf = r.createColorGradientFormatting();
        cgf.setNumControlPoints(numPoints);
        cgf.setThresholds(ts);
        cgf.setColors(colors);
        return r;
    }

    public CFRule12Record(RecordInputStream in) {
        futureHeader = new FtrHeader(in);
        setConditionType(in.readByte());
        setComparisonOperation(in.readByte());
        int field_3_formula1_len = in.readUShort();
        int field_4_formula2_len = in.readUShort();

        ext_formatting_length = in.readInt();
        ext_formatting_data = new byte[0];
        if (ext_formatting_length == 0) {
            // 2 bytes reserved
            in.readUShort();
        } else {
            long len = readFormatOptions(in);
            if (len < ext_formatting_length) {
                ext_formatting_data = IOUtils.safelyAllocate(ext_formatting_length-len, getMaxRecordLength());
                in.readFully(ext_formatting_data);
            }
        }

        setFormula1(Formula.read(field_3_formula1_len, in));
        setFormula2(Formula.read(field_4_formula2_len, in));

        int formula_scale_len = in.readUShort();
        formula_scale = Formula.read(formula_scale_len, in);

        ext_opts = in.readByte();
        priority = in.readUShort();
        template_type = in.readUShort();
        template_param_length = in.readByte();
        if (template_param_length == 0 || template_param_length == 16) {
            template_params = IOUtils.safelyAllocate(template_param_length, getMaxRecordLength());
            in.readFully(template_params);
        } else {
            LOG.atWarn().log("CF Rule v12 template params length should be 0 or 16, found {}", box(template_param_length));
            in.readRemainder();
        }

        byte type = getConditionType();
        if (type == CONDITION_TYPE_COLOR_SCALE) {
            color_gradient = new ColorGradientFormatting(in);
        } else if (type == CONDITION_TYPE_DATA_BAR) {
            data_bar = new DataBarFormatting(in);
        } else if (type == CONDITION_TYPE_FILTER) {
            filter_data = in.readRemainder();
        } else if (type == CONDITION_TYPE_ICON_SET) {
            multistate = new IconMultiStateFormatting(in);
        }
    }

    public boolean containsDataBarBlock() {
        return (data_bar != null);
    }
    public DataBarFormatting getDataBarFormatting() {
        return data_bar;
    }
    public DataBarFormatting createDataBarFormatting() {
        if (data_bar != null) return data_bar;

        // Convert, setup and return
        setConditionType(CONDITION_TYPE_DATA_BAR);
        data_bar = new DataBarFormatting();
        return data_bar;
    }

    public boolean containsMultiStateBlock() {
        return (multistate != null);
    }
    public IconMultiStateFormatting getMultiStateFormatting() {
        return multistate;
    }
    public IconMultiStateFormatting createMultiStateFormatting() {
        if (multistate != null) return multistate;

        // Convert, setup and return
        setConditionType(CONDITION_TYPE_ICON_SET);
        multistate = new IconMultiStateFormatting();
        return multistate;
    }

    public boolean containsColorGradientBlock() {
        return (color_gradient != null);
    }
    public ColorGradientFormatting getColorGradientFormatting() {
        return color_gradient;
    }
    public ColorGradientFormatting createColorGradientFormatting() {
        if (color_gradient != null) return color_gradient;

        // Convert, setup and return
        setConditionType(CONDITION_TYPE_COLOR_SCALE);
        color_gradient = new ColorGradientFormatting();
        return color_gradient;
    }

    /**
     * get the stack of the scale expression as a list
     *
     * @return list of tokens (casts stack to a list and returns it!)
     * this method can return null is we are unable to create Ptgs from
     *   existing excel file
     * callers should check for null!
     */
    public Ptg[] getParsedExpressionScale() {
        return formula_scale.getTokens();
    }
    public void setParsedExpressionScale(Ptg[] ptgs) {
        formula_scale = Formula.create(ptgs);
    }

    public int getPriority() {
        return priority;
    }
    public void setPriority(int priority) {
        this.priority = priority;
    }

    public short getSid() {
        return sid;
    }

    /**
     * called by the class that is responsible for writing this sucker.
     * Subclasses should implement this so that their data is passed back in a
     * byte array.
     *
     * @param out the stream to write to
     */
    public void serialize(LittleEndianOutput out) {
        futureHeader.serialize(out);

        int formula1Len=getFormulaSize(getFormula1());
        int formula2Len=getFormulaSize(getFormula2());

        out.writeByte(getConditionType());
        out.writeByte(getComparisonOperation());
        out.writeShort(formula1Len);
        out.writeShort(formula2Len);

        // TODO Update ext_formatting_length
        if (ext_formatting_length == 0) {
            out.writeInt(0);
            out.writeShort(0);
        } else {
            out.writeInt(ext_formatting_length);
            serializeFormattingBlock(out);
            out.write(ext_formatting_data);
        }

        getFormula1().serializeTokens(out);
        getFormula2().serializeTokens(out);
        out.writeShort(getFormulaSize(formula_scale));
        formula_scale.serializeTokens(out);

        out.writeByte(ext_opts);
        out.writeShort(priority);
        out.writeShort(template_type);
        out.writeByte(template_param_length);
        out.write(template_params);

        byte type = getConditionType();
        if (type == CONDITION_TYPE_COLOR_SCALE) {
            color_gradient.serialize(out);
        } else if (type == CONDITION_TYPE_DATA_BAR) {
            data_bar.serialize(out);
        } else if (type == CONDITION_TYPE_FILTER) {
            out.write(filter_data);
        } else if (type == CONDITION_TYPE_ICON_SET) {
            multistate.serialize(out);
        }
    }

    protected int getDataSize() {
        int len = FtrHeader.getDataSize() + 6;
        if (ext_formatting_length == 0) {
            len += 6;
        } else {
            len += 4 + getFormattingBlockSize() + ext_formatting_data.length;
        }
        len += getFormulaSize(getFormula1());
        len += getFormulaSize(getFormula2());
        len += 2 + getFormulaSize(formula_scale);
        len += 6 + template_params.length;

        byte type = getConditionType();
        if (type == CONDITION_TYPE_COLOR_SCALE) {
            len += color_gradient.getDataLength();
        } else if (type == CONDITION_TYPE_DATA_BAR) {
            len += data_bar.getDataLength();
        } else if (type == CONDITION_TYPE_FILTER) {
            len += filter_data.length;
        } else if (type == CONDITION_TYPE_ICON_SET) {
            len += multistate.getDataLength();
        }
        return len;
    }

    @Override
    public CFRule12Record copy() {
        return new CFRule12Record(this);
    }

    public short getFutureRecordType() {
        return futureHeader.getRecordType();
    }
    public FtrHeader getFutureHeader() {
        return futureHeader;
    }
    public CellRangeAddress getAssociatedRange() {
        return futureHeader.getAssociatedRange();
    }

    @Override
    public HSSFRecordTypes getGenericRecordType() {
        return HSSFRecordTypes.CF_RULE_12;
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        final Map<String, Supplier<?>> m = new LinkedHashMap<>(super.getGenericProperties());
        m.put("dxFn12Length", () -> ext_formatting_length);
        m.put("futureHeader", this::getFutureHeader);
        m.put("dxFn12Ext", () -> ext_formatting_data);
        m.put("formulaScale", this::getParsedExpressionScale);
        m.put("extOptions", () -> ext_opts);
        m.put("priority", this::getPriority);
        m.put("templateType", () -> template_type);
        m.put("templateParams", () -> template_params);
        m.put("filterData",  () -> filter_data);
        m.put("dataBar", this::getDataBarFormatting);
        m.put("multiState", this::getMultiStateFormatting);
        m.put("colorGradient", this::getColorGradientFormatting);
        return Collections.unmodifiableMap(m);
    }
}

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

package org.apache.poi.ss.util;
/**
 * Contains specific examples of <tt>double</tt> values and their rendering in Excel.
 * 
 * @author Josh Micich
 */
final class NumberToTextConversionExamples {

	private NumberToTextConversionExamples() {
		// no instances of this class
	}

	public static final class ExampleConversion {
		private final String _javaRendering;
		private final String _excelRendering;
		private final double _doubleValue;
		private final long _rawDoubleBits;

		ExampleConversion(long rawDoubleBits, String javaRendering, String excelRendering) {
			double d = Double.longBitsToDouble(rawDoubleBits);
			if ("NaN".equals(javaRendering)) {
				if (!Double.isNaN(d)) {
					throw new IllegalArgumentException("value must be NaN");
				}
			} else {
				if (Double.isNaN(d)) {
					throw new IllegalArgumentException("value must not be NaN");
				}
				// just to be dead sure test conversion in java both ways
				boolean javaToStringOk = javaRendering.equals(Double.toString(d));
				boolean javaParseOk = Double.parseDouble(javaRendering) == d;
				if(!javaToStringOk || !javaParseOk) {
					String msgA = "Specified rawDoubleBits " + doubleToHexString(d) + " encodes to double '" + d + "'.";
					String msgB = "Specified javaRendering '" + javaRendering+ "' parses as double with rawDoubleBits "
						+ doubleToHexString(Double.parseDouble(javaRendering));
					System.err.println(msgA);
					System.err.println(msgB);

					throw new RuntimeException(msgA + msgB);
				}
			}
			_rawDoubleBits = rawDoubleBits;
			_javaRendering = javaRendering;
			_excelRendering = excelRendering;
			_doubleValue = d;
		}
		private static String doubleToHexString(double d) {
			return "0x" + Long.toHexString(Double.doubleToLongBits(d)).toUpperCase() + "L";
		}
		public String getJavaRendering() {
			return _javaRendering;
		}
		public String getExcelRendering() {
			return _excelRendering;
		}
		public double getDoubleValue() {
			return _doubleValue;
		}
		public boolean isNaN() {
			return Double.isNaN(_doubleValue);
		}
		public long getRawDoubleBits() {
			return _rawDoubleBits;
		}
	}

	
	private static final ExampleConversion[] examples = {
		// basic numbers
		ec(0x0000000000000000L, "0.0", "0"),
		ec(0x3FF0000000000000L, "1.0", "1"),
		ec(0x3FF00068DB8BAC71L, "1.0001", "1.0001"),
		ec(0x4087A00000000000L, "756.0", "756"),
		ec(0x401E3D70A3D70A3DL, "7.56", "7.56"),
		
		ec(0x405EDD3C07FB4C99L, "123.45678901234568", "123.456789012346"),
		ec(0x4132D687E3DF2180L, "1234567.8901234567", "1234567.89012346"),
		
		// small numbers
		ec(0x3EE9E409302678BAL, "1.2345678901234568E-5", "1.23456789012346E-05"),
		ec(0x3F202E85BE180B74L, "1.2345678901234567E-4", "0.000123456789012346"),
		ec(0x3F543A272D9E0E51L, "0.0012345678901234567", "0.00123456789012346"),
		ec(0x3F8948B0F90591E6L, "0.012345678901234568", "0.0123456789012346"),

		ec(0x3EE9E409301B5A02L, "1.23456789E-5", "0.0000123456789"),
		
		ec(0x3E6E7D05BDABDE50L, "5.6789012345E-8", "0.000000056789012345"),
		ec(0x3E6E7D05BDAD407EL, "5.67890123456E-8", "5.67890123456E-08"),
		ec(0x3E6E7D06029F18BEL, "5.678902E-8", "0.00000005678902"),

		ec(0x2BCB5733CB32AE6EL, "9.999999999999123E-98",  "9.99999999999912E-98"),
		ec(0x2B617F7D4ED8C59EL, "1.0000000000001235E-99", "1.0000000000001E-99"),
		ec(0x0036319916D67853L, "1.2345678901234578E-307", "1.2345678901235E-307"),

		ec(0x359DEE7A4AD4B81FL, "2.0E-50", "2E-50"),

		// large numbers
		ec(0x41678C29DCD6E9E0L, "1.2345678901234567E7", "12345678.9012346"),
		ec(0x42A674E79C5FE523L, "1.2345678901234568E13", "12345678901234.6"),
		ec(0x42DC12218377DE6BL, "1.2345678901234567E14", "123456789012346"),
		ec(0x43118B54F22AEB03L, "1.2345678901234568E15", "1234567890123460"),
		ec(0x43E56A95319D63E1L, "1.2345678901234567E19", "12345678901234600000"),
		ec(0x441AC53A7E04BCDAL, "1.2345678901234568E20", "1.23456789012346E+20"),
		ec(0xC3E56A95319D63E1L, "-1.2345678901234567E19", "-12345678901234600000"),
		ec(0xC41AC53A7E04BCDAL, "-1.2345678901234568E20", "-1.23456789012346E+20"),

		ec(0x54820FE0BA17F46DL, "1.2345678901234577E99", "1.2345678901235E+99"),
		ec(0x54B693D8E89DF188L, "1.2345678901234576E100", "1.2345678901235E+100"),
		
		ec(0x4A611B0EC57E649AL, "2.0E50", "2E+50"),
		
		// range extremities
		ec(0x7FEFFFFFFFFFFFFFL, "1.7976931348623157E308", "1.7976931348623E+308"),
		ec(0x0010000000000000L, "2.2250738585072014E-308", "2.2250738585072E-308"),
		ec(0x000FFFFFFFFFFFFFL, "2.225073858507201E-308", "0"),
		ec(0x0000000000000001L, "4.9E-324", "0"),
		
		// infinity
		ec(0x7FF0000000000000L, "Infinity", "1.7976931348623E+308"),
		ec(0xFFF0000000000000L, "-Infinity", "1.7976931348623E+308"),

		// shortening due to rounding
		ec(0x441AC7A08EAD02F2L, "1.234999999999999E20", "1.235E+20"),
		ec(0x40FE26BFFFFFFFF9L, "123499.9999999999", "123500"),
		ec(0x3E4A857BFB2F2809L, "1.234999999999999E-8", "0.00000001235"),
		ec(0x3BCD291DEF868C89L, "1.234999999999999E-20", "1.235E-20"),

		// carry up due to rounding
		// For clarity these tests choose values that don't round in java,
		// but will round in excel. In some cases there is almost no difference
		// between excel and java (e.g. 9.9..9E-8)
		ec(0x444B1AE4D6E2EF4FL, "9.999999999999999E20", "1E+21"),
		ec(0x412E847FFFFFFFFFL, "999999.9999999999", "1000000"),
		ec(0x3E45798EE2308C39L, "9.999999999999999E-9", "0.00000001"),
		ec(0x3C32725DD1D243ABL, "9.999999999999999E-19", "0.000000000000000001"),
		ec(0x3BFD83C94FB6D2ABL, "9.999999999999999E-20", "1E-19"),

		ec(0xC44B1AE4D6E2EF4FL, "-9.999999999999999E20", "-1E+21"),
		ec(0xC12E847FFFFFFFFFL, "-999999.9999999999", "-1000000"),
		ec(0xBE45798EE2308C39L, "-9.999999999999999E-9", "-0.00000001"),
		ec(0xBC32725DD1D243ABL, "-9.999999999999999E-19", "-0.000000000000000001"),
		ec(0xBBFD83C94FB6D2ABL, "-9.999999999999999E-20", "-1E-19"),

		
		// NaNs
		// Currently these test cases are not critical, since other limitations prevent any variety in
		// or control of the bit patterns used to encode NaNs in evaluations.
		ec(0xFFFF0420003C0000L, "NaN", "3.484840871308E+308"),
		ec(0x7FF8000000000000L, "NaN", "2.6965397022935E+308"),
		ec(0x7FFF0420003C0000L, "NaN", "3.484840871308E+308"),
		ec(0xFFF8000000000000L, "NaN", "2.6965397022935E+308"),
		ec(0xFFFF0AAAAAAAAAAAL, "NaN", "3.4877119413344E+308"),
		ec(0x7FF80AAAAAAAAAAAL, "NaN", "2.7012211948322E+308"),
		ec(0xFFFFFFFFFFFFFFFFL, "NaN", "3.5953862697246E+308"),
		ec(0x7FFFFFFFFFFFFFFFL, "NaN", "3.5953862697246E+308"),
		ec(0xFFF7FFFFFFFFFFFFL, "NaN", "2.6965397022935E+308"),
	};
	
	private static ExampleConversion ec(long rawDoubleBits, String javaRendering, String excelRendering) {
		return new ExampleConversion(rawDoubleBits, javaRendering, excelRendering);
	}
	
	public static ExampleConversion[] getExampleConversions() {
		return examples.clone();
	}
}

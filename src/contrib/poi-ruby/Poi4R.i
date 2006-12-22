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

%module poi4r


%{

#include <gcj/cni.h>
#include <java/lang/Object.h>
#include <java/lang/Thread.h>
#include <java/lang/ThreadGroup.h>
#include <java/lang/Runnable.h>
#include <java/lang/String.h>
#include <java/lang/Throwable.h>
#include <java/lang/Comparable.h>
#include <java/lang/Integer.h>
#include <java/lang/Long.h>
#include <java/lang/Float.h>
#include <java/lang/Double.h>
#include <java/io/StringWriter.h>
#include <java/io/PrintWriter.h>
#include <java/util/Hashtable.h>
#include <java/util/Date.h>
#include <java/util/Calendar.h>
#include <java/lang/System.h>

#include "org/apache/poi/hssf/usermodel/HSSFWorkbook.h"
#include "org/apache/poi/hssf/usermodel/HSSFSheet.h"
#include "org/apache/poi/hssf/usermodel/HSSFRow.h"
#include "org/apache/poi/hssf/usermodel/HSSFCell.h"
#include "org/apache/poi/hssf/usermodel/HSSFFont.h"
#include "org/apache/poi/hssf/usermodel/HSSFCellStyle.h"
#include "org/apache/poi/hssf/usermodel/HSSFDataFormat.h"
#include "org/apache/poi/hssf/usermodel/HSSFHeader.h"
#include "org/apache/poi/hssf/usermodel/HSSFFooter.h"
#include "org/apache/poi/RubyOutputStream.h"


typedef ::org::apache::poi::hssf::usermodel::HSSFWorkbook *jhworkbook;
typedef ::org::apache::poi::hssf::usermodel::HSSFSheet *jhsheet;
typedef ::org::apache::poi::hssf::usermodel::HSSFRow *jhrow;
typedef ::org::apache::poi::hssf::usermodel::HSSFCell *jhcell;
typedef ::org::apache::poi::hssf::usermodel::HSSFCellStyle *jhcellstyle;
typedef ::org::apache::poi::hssf::usermodel::HSSFFont *jhfont;
typedef ::org::apache::poi::hssf::usermodel::HSSFFooter *jhfooter;
typedef ::org::apache::poi::hssf::usermodel::HSSFHeader *jhheader;
typedef ::org::apache::poi::hssf::usermodel::HSSFDataFormat *jhdataformat;

typedef ::java::util::Date *jdate;
typedef ::java::util::Calendar *jcalendar;
typedef ::java::io::OutputStream *joutputstream;
typedef ::java::io::InputStream *jinputstream;
typedef ::java::util::Collection *jstringCollection;
typedef ::java::util::Collection *jtermCollection;
typedef ::java::util::Locale *jlocale;
typedef ::java::lang::Comparable *jcomparable;
typedef JArray<jobject> *jobjectArray;
typedef JArray<jstring> *jstringArray;


static java::lang::Thread *nextThread;
static java::util::Hashtable *objects;


static void store_reference(jobject object) {
	java::lang::Integer *ji =new java::lang::Integer(java::lang::System::identityHashCode(object));
	jobject jo = objects->get(ji);
	if (!jo) {
	//	printf("put object in hash\n");
		objects->put(ji,object);
	}
}
static VALUE jo2rv(jobject object, swig_type_info *descriptor)
{
    if (object == NULL)
    {
        return Qnil;
    }
    else
    {
        return SWIG_NewPointerObj((void *) object, descriptor, 0);
    }
}
static int cvtptr(VALUE value, void **jo, swig_type_info *info)
{
    if (SWIG_ConvertPtr(value, jo, info, 0) == 0)
        return 0;
    else
    {
        return -1;
    }
}

static int rv2jo(VALUE rv, jobject *jo, swig_type_info *descriptor)
{
    if (NIL_P(rv))
        *jo = NULL;
    else
    {
        java::lang::Object *javaObj;

        if (cvtptr(rv, (void **) &javaObj, descriptor) == -1)
            return 0;

        *jo = javaObj;
    }

    return 1;
}


static jstring r2j(VALUE object)
{
    if (NIL_P(object)){
        return NULL;
    }
    else {
    	char *ps =  STR2CSTR(object);
        jstring js = JvNewStringLatin1(ps);

        if (!js)
        {
    		rb_raise(rb_eRuntimeError, "ruby str cannot be converted to java: %s",ps);
        }

        return js;
    }
}

VALUE j2r(jstring js)
{
    if (!js)
    {
        return Qnil;
    }
    else
    {
        jint len = JvGetStringUTFLength(js);
        char buf[len + 1];

        JvGetStringUTFRegion(js, 0, len, buf);
        buf[len] = '\0';
       
        return rb_str_new2(buf);
    }
}

static void free_java_obj(void* arg1) {
	jobject object =(jobject) arg1;
	java::lang::Integer *ji =new java::lang::Integer(java::lang::System::identityHashCode(object));
        jobject jo = objects->get(ji);
        if (jo) {
        //        printf("removed object from hash\n");
                objects->remove(ji);
        }
}

static void raise_ruby_error(java::lang::Throwable *e) {
	java::io::StringWriter *buffer = new java::io::StringWriter();
    java::io::PrintWriter *writer = new java::io::PrintWriter(buffer);
    e->printStackTrace(writer);
    writer->close();
    jstring message = buffer->toString();
    jint len = JvGetStringUTFLength(message);
    char buf[len + 1];
    JvGetStringUTFRegion(message, 0, len, buf);
    buf[len] = '\0';
	rb_raise(rb_eRuntimeError, "error calling poi \n %s", buf);
}

%}

typedef long jint;
typedef long long jlong;
typedef char jbyte;
typedef float jfloat;
typedef float jdouble;
typedef int jshort;
typedef bool jboolean;

%typemap(in) SWIGTYPE * {

    if (!rv2jo($input, (jobject *) &$1, $1_descriptor))
        rb_raise(rb_eRuntimeError, "Unrecoverable error in SWIG typemapping");
}
%typemap(out) SWIGTYPE * {

    $result = jo2rv($1, $1_descriptor);
}

%typemap(in) org::apache::poi::hssf::usermodel::HSSFWorkbook{

    if (!rv2jo($input, (jobject *) &$1,
               $descriptor(org::apache::poi::hssf::usermodel::HSSFWorkbook *)))
        SWIG_fail;
}
%typemap(out) org::apache::poi::hssf::usermodel::HSSFWorkbook {
    $result = jo2rv($1, $descriptor(org::apache::poi::hssf::usermodel::HSSFWorkbook *));
}

%typemap(in) jhsheet{

    if (!rv2jo($input, (jobject *) &$1,
               $descriptor(org::apache::poi::hssf::usermodel::HSSFSheet *)))
        SWIG_fail;
}
%typemap(out) jhsheet {

    $result = jo2rv($1, $descriptor(org::apache::poi::hssf::usermodel::HSSFSheet *));
}
%typemap(in) jhrow{

    if (!rv2jo($input, (jobject *) &$1,
               $descriptor(org::apache::poi::hssf::usermodel::HSSFRow *)))
        SWIG_fail;
}
%typemap(out) jhrow {

    $result = jo2rv($1, $descriptor(org::apache::poi::hssf::usermodel::HSSFRow *));
}
%typemap(in) jhcell{

    if (!rv2jo($input, (jobject *) &$1,
               $descriptor(org::apache::poi::hssf::usermodel::HSSFCell *)))
        SWIG_fail;
}
%typemap(out) jhcell {

    $result = jo2rv($1, $descriptor(org::apache::poi::hssf::usermodel::HSSFCell *));
}
%typemap(in) jhfont{

    if (!rv2jo($input, (jobject *) &$1,
                $descriptor(org::apache::poi::hssf::usermodel::HSSFFont *)))
         rb_raise(rb_eRuntimeError, "Unrecoverable error in SWIG typemapping of HSSFFont");
}

%typemap(out) jhfont {

    $result = jo2rv($1, $descriptor(org::apache::poi::hssf::usermodel::HSSFFont *));
}

%typemap(in) jhcellstyle{

    if (!rv2jo($input, (jobject *) &$1,
               $descriptor(org::apache::poi::hssf::usermodel::HSSFCellStyle *)))
		rb_raise(rb_eRuntimeError, "Unrecoverable error in SWIG typemapping of HSSFCellStyle");
}
%typemap(out) jhcellstyle {

    $result = jo2rv($1, $descriptor(org::apache::poi::hssf::usermodel::HSSFCellStyle *));
}
%typemap(in) jhdataformat{

    if (!rv2jo($input, (jobject *) &$1,
               $descriptor(org::apache::poi::hssf::usermodel::HSSFDataFormat *)))
	rb_raise(rb_eRuntimeError, "Unrecoverable error in SWIG typemapping of HSSFDataFormat");
}
%typemap(out) jhdataformat {

    $result = jo2rv($1, $descriptor(org::apache::poi::hssf::usermodel::HSSFDataFormat *));
}


%typemap(in) jstring {
    $1 = r2j($input);
}
%typemap(out) jstring {
    $result = j2r($1);
}
%typecheck(SWIG_TYPECHECK_STRING) jstring {
    $1 = ( NIL_P($input) || TYPE($input)==T_STRING );
}

%typemap(in) joutputstream {

        jlong ptr;
		if (!rb_respond_to($input, rb_intern("putc"))) rb_raise(rb_eTypeError,"Expected IO");
        *(VALUE *) &ptr = (VALUE) $input;
        $1 = new org::apache::poi::RubyOutputStream(ptr);
}
%typemap(in) jcalendar {
	$1 = java::util::Calendar::getInstance();
	//$1->setTimeInMillis((long long) NUM2DBL(rb_funcall($input,rb_intern("to_i"),0,NULL))*1000.0);
	$1->set(FIX2INT(rb_funcall($input,rb_intern("year"),0,NULL)),
		FIX2INT(rb_funcall($input,rb_intern("mon"),0,NULL))-1,
		FIX2INT(rb_funcall($input,rb_intern("day"),0,NULL)),
		FIX2INT(rb_funcall($input,rb_intern("hour"),0,NULL)),
		FIX2INT(rb_funcall($input,rb_intern("min"),0,NULL)),
		FIX2INT(rb_funcall($input,rb_intern("sec"),0,NULL))
		);	
}

%typecheck(SWIG_TYPECHECK_POINTER) jcalendar {
    $1 = rb_respond_to($input, rb_intern("asctime"));
}

%typemap(out) jdate {
	jlong t = ((jdate) $1)->getTime();
	//TODO: separate seconds and microsecs
	int ts=t/1000;
	$result=rb_time_new((time_t) ts, 0 );
}


%freefunc org::apache::poi::hssf::usermodel::HSSFWorkbook "free_java_obj";

%exception {
    try {
        $action
    } catch (java::lang::Throwable *e) {
    	raise_ruby_error(e);
    }
}
%exception org::apache::poi::hssf::usermodel::HSSFWorkbook::HSSFWorkbook {
    try {
        $action
		store_reference(result);
    } catch (java::lang::Throwable *e) {
        raise_ruby_error(e);
    }
}




namespace java {
    namespace lang {
        class Object {
            jstring toString();
        };
%nodefault;
	class System : public Object {
        public:
            static jstring getProperty(jstring);
            static jstring getProperty(jstring, jstring);
            static void load(jstring);
            static void loadLibrary(jstring);
            static void mapLibraryName(jstring);
            static void runFinalization();
            static void setProperty(jstring, jstring);
        };
%makedefault;
    }
    namespace io {
%nodefault;
        class InputStream : public ::java::lang::Object {
        };
        class OutputStream : public ::java::lang::Object {
        };
        
%makedefault;
    }
    namespace util {
        class Date : public ::java::lang::Object {
        public:
            Date();
            Date(jlong);
            void setTime(jlong);
            jstring toString();
        };
    }
}


namespace org {
    namespace apache {
        namespace poi {
            namespace hssf {
            	namespace usermodel {
%nodefault; 
	                class HSSFWorkbook : public ::java::lang::Object {
	                public:
                		HSSFWorkbook();
	                    jstring getSheetName(jint);
	                    jint getNumberOfSheets();
	                    void setSheetOrder(jstring,jint);
	                    void setSheetName(jint,jstring);
	                    void setSheetName(jint,jstring,jshort);
	                    jint getSheetIndex(jstring);
	                    jhsheet createSheet();
	                    jhsheet cloneSheet(jint);
	                    jhsheet createSheet(jstring);
	                    jhsheet getSheetAt(jint);
	                    jhsheet getSheet(jstring);
	                    void removeSheetAt(jint);
	                    jhcellstyle createCellStyle();
			    jhfont createFont();
			    jhdataformat createDataFormat(); 
	                    void write(joutputstream);
	                    
	                };

	                class HSSFSheet : public ::java::lang::Object {
	                public:
	                	jhrow createRow(jint);
	                	jhrow getRow(jint);
	                	jhfooter getFooter();
	                	jhheader getHeader();
	                };
	                class HSSFRow : public ::java::lang::Object {
	                public:
	                	jhcell createCell(jshort);
	                	jhcell getCell(jshort);
	                	//jboolean getProtect(); //only in 2.5
	                	
	                };
	                class HSSFCell : public ::java::lang::Object {
	                public:
	                	void setCellValue(jdouble);
	                	void setCellValue(jstring);
	                	void setCellValue(jboolean);
	                	void setCellValue(jcalendar);
	                	void setCellFormula(jstring);
	                	jstring getStringCellValue();
	                	jdouble getNumericCellValue();
	                	jdate getDateCellValue();
	                	jstring getCellFormula();
	                	jboolean getBooleanCellValue();
	                	jint getCellType();
	                	jshort getEncoding();
	                	void setAsActiveCell();
	                	
	                	void setCellStyle(jhcellstyle);
	                	void setEncoding(jshort encoding);
	                	
	                	static const jint CELL_TYPE_BLANK;
	                	static const jint CELL_TYPE_BOOLEAN;
	                	static const jint CELL_TYPE_ERROR;
	                	static const jint CELL_TYPE_FORMULA;
	                	static const jint CELL_TYPE_NUMERIC;
	                	static const jint CELL_TYPE_STRING;
	                	
	                	static const jshort ENCODING_COMPRESSED_UNICODE;
	                	static const jshort ENCODING_UTF_16;
	                };
					class HSSFCellStyle : public ::java::lang::Object {
					public:
						static const jshort ALIGN_CENTER;
						static const jshort ALIGN_CENTER_SELECTION;
						static const jshort ALIGN_FILL;
						static const jshort ALIGN_GENERAL;
						static const jshort ALIGN_JUSTIFY;
						static const jshort ALIGN_LEFT;
						static const jshort ALIGN_RIGHT;
						static const jshort ALT_BARS;
						static const jshort BIG_SPOTS;
						static const jshort BORDER_DASH_DOT;
						static const jshort BORDER_DASH_DOT_DOT;
						static const jshort BORDER_DASHED;
						static const jshort BORDER_DOTTED;
						static const jshort BORDER_DOUBLE;
						static const jshort BORDER_HAIR;
						static const jshort BORDER_MEDIUM;
						static const jshort BORDER_MEDIUM_DASH_DOT;
						static const jshort BORDER_MEDIUM_DASH_DOT_DOT;
						static const jshort BORDER_MEDIUM_DASHED;
						static const jshort BORDER_NONE;
						static const jshort BORDER_SLANTED_DASH_DOT;
						static const jshort BORDER_THICK;
						static const jshort BORDER_THIN;
						static const jshort BRICKS;
						static const jshort DIAMONDS;
						static const jshort FINE_DOTS;
						static const jshort NO_FILL;
						static const jshort SOLID_FOREGROUND;
						static const jshort SPARSE_DOTS;
						static const jshort SQUARES;
						static const jshort THICK_BACKWARD_DIAG;
						static const jshort THICK_FORWARD_DIAG;
						static const jshort THICK_HORZ_BANDS;
						static const jshort THICK_VERT_BANDS;
						static const jshort THIN_BACKWARD_DIAG;
						static const jshort THIN_FORWARD_DIAG;
						static const jshort THIN_HORZ_BANDS;
						static const jshort THIN_VERT_BANDS;
						static const jshort VERTICAL_BOTTOM;
						static const jshort VERTICAL_CENTER;
						static const jshort VERTICAL_JUSTIFY;
						static const jshort VERTICAL_TOP;
						
						jshort getAlignment();
						jshort getBorderBottom();
						jshort getBorderLeft();
						jshort getBorderRight();
						jshort getBorderTop();
						jshort getBottomBorderColor();
						jshort getDataFormat();
						jshort getFillBackgroundColor();
						jshort getFillForegroundColor();
						jshort getFillPattern();
						jshort getFontIndex();
						jboolean getHidden();
						jshort getIndention();
						jshort getIndex();
						jshort getLeftBorderColor();
						jboolean getLocked();
						jshort getRightBorderColor();
						jshort getRotation();
						jshort getTopBorderColor();
						jshort getVerticalAlignment();
						jboolean getWrapText();
						void setAlignment(jshort) ;
						void setBorderBottom(jshort );
						void setBorderLeft(jshort );
						void setBorderRight(jshort );
						void setBorderTop(jshort );
						void setBottomBorderColor(jshort );
						void setDataFormat(jshort );
						void setFillBackgroundColor(jshort );
						void setFillForegroundColor(jshort );
						void setFillPattern(jshort );
						void setFont(jhfont );
						void setHidden(jboolean );
						void setIndention(jshort );
						void setLeftBorderColor(jshort );
						void setLocked(jboolean );
						void setRightBorderColor(jshort );
						void setRotation(jshort );
						void setTopBorderColor(jshort );
						void setVerticalAlignment(jshort );
						void setWrapText(jboolean );
					};
					class HSSFDataFormat : public ::java::lang::Object {
					public:
						static jstring getBuiltinFormat(jshort);
						static jshort getBuiltinFormat(jstring);
						jstring getFormat(jshort);
						jshort getFormat(jstring);
						static jint getNumberOfBuiltinBuiltinFormats();
						//TODO static jlist getBuiltinFormats(); 
		
					};
					class HSSFFont : public ::java::lang::Object {
					public:
						static const jshort 	BOLDWEIGHT_BOLD;
static const jshort 	BOLDWEIGHT_NORMAL;
static const jshort 	COLOR_NORMAL;
static const jshort 	COLOR_RED;
static const jstring 	FONT_ARIAL;
static const jshort 	SS_NONE;
static const jshort 	SS_SUB;
static const jshort 	SS_SUPER;
static const jshort 	U_DOUBLE;
static const jshort 	U_DOUBLE_ACCOUNTING;
static const jshort 	U_NONE;
static const jshort 	U_SINGLE;
static const jshort 	U_SINGLE_ACCOUNTING;
 
 jshort 	getBoldweight();
 jshort 	getColor();
 jshort 	getFontHeight();
 jshort 	getFontHeightInPoints();
 jstring 	getFontName();
 jshort 	getIndex();
 jboolean 	getItalic();
 jboolean 	getStrikeout();
 jshort 	getTypeOffset();
 jshort 	getUnderline();
 void 	setBoldweight(jshort );
 void 	setColor(jshort );
 void 	setFontHeight(jshort );
 void 	setFontHeightInPoints(jshort );
 void 	setFontName(jstring );
 void 	setItalic(jboolean );
 void 	setStrikeout(jboolean );
 void 	setTypeOffset(jshort );
 void 	setUnderline(jshort );
};
%makedefault;
	            }
            }
        }
    }
}





%init %{

    JvCreateJavaVM(NULL);
    JvAttachCurrentThread(NULL, NULL);

    nextThread = new java::lang::Thread();
    objects = new java::util::Hashtable();

    java::util::Hashtable *props = (java::util::Hashtable *)
        java::lang::System::getProperties();
    props->put(JvNewStringUTF("inRuby"), objects);

    JvInitClass(&org::apache::poi::hssf::usermodel::HSSFFont::class$);
    JvInitClass(&org::apache::poi::hssf::usermodel::HSSFCell::class$);
    JvInitClass(&org::apache::poi::hssf::usermodel::HSSFSheet::class$);
    JvInitClass(&org::apache::poi::hssf::usermodel::HSSFCellStyle::class$);

%}


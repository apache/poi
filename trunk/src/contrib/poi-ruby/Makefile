# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
# 
# http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.


# Makefile for building Poi4R
#
# Supported operating systems: Linux, Mac OS X and Windows.
# See INSTALL file for requirements.
# 
# Steps to build
#   1. Edit the sections below as documented
#   2. make all
#   3. make install
#
# The install target installs the Poi4R python extension in python's
# site-packages directory. On Mac OS X, it also installs the gcj runtime
# libraries into $(PREFIX)/lib.
#
# To successfully import the Poi4R extension into Ruby, all required
# libraries need to be found. If the locations you chose are non-standard,
# the relevant DYLD_LIBRARY_PATH (Mac OS X), LD_LIBRARY_PATH (Linux), or 
# PATH (Windows) need to be set accordingly.
# 


VERSION=0.1.0
POI_VER=$(shell grep '<property name="version.id"' ../../../build.xml | awk -F'"' '{print $$4}')
RUBY_VER=1.8

POI4R:=$(shell pwd)
POI=$(POI4R)/poi-$(POI_VER)
#DEBUG=1

# 
# You need to uncomment and edit the variables below in the section
# corresponding to your operating system.
#
# PREFIX: where programs are normally installed on your system (Unix).
# PREFIX_RUBY: where your version of python is installed.
# GCJ_HOME: where GCC/GCJ is installed.
# Windows drive-absolute paths need to be expressed cygwin style.
#

# Mac OS X (Darwin)
#PREFIX=/usr/local
#PREFIX_RUBY=/Library/Frameworks/Ruby.framework/Versions/$(RUBY_VER)
#SWIG=$(PREFIX)/bin/swig
#GCJ_HOME=/usr/local/gcc-3.4.1
#DB=$(POI4R)/db-$(DB_VER)
#PREFIX_DB=/usr/local/BerkeleyDB.$(DB_LIB_VER)

# Linux
PREFIX=/usr
PREFIX_RUBY=$(PREFIX)
SWIG=$(PREFIX)/bin/swig
GCJ_HOME=/usr
#DB=$(POI4R)/db-$(DB_VER)
#PREFIX_DB=$(PREFIX)/BerkeleyDB.$(DB_LIB_VER)

# Windows
#PREFIX_RUBY=/cygdrive/o/Python-2.3.2
#SWIG=/cygdrive/c/utils/bin/swig.exe
#GCJ_HOME=/cygdrive/o/mingw-3.1
#DB=/cygdrive/o/db-$(DB_VER)
#PREFIX_DB=$(DB)

#
# No edits required below
#

OS=$(shell uname)
ifeq ($(findstring CYGWIN,$(OS)),CYGWIN)
OS=Cygwin
endif
ifeq ($(findstring WINNT,$(OS)),WINNT)
OS=Cygwin
endif

ifeq ($(DEBUG),1)
COMP_OPT=DEBUG=1
SUFFIX=d
_SUFFIX=_d
BINDIR=debug
else
COMP_OPT=
SUFFIX=
_SUFFIX=
BINDIR=release
endif

SWIG_OPT=-DSWIG_COBJECT_TYPES -DPOI4R_VER="'$(VERSION)'" -DPOI_VER="'$(POI_VER)'"

JCCFLAGS=--encoding=UTF-8
#JCCFLAGS=--encoding=UTF-8 -findirect-dispatch

ifeq ($(OS),Darwin)
RUBY_SITE=$(PREFIX_RUBY)/lib/ruby$(RUBY_VER)/site-packages
RUBY_INC=$(PREFIX_RUBY)/lib/ruby$(RUBY_VER)
POI4R=$(BINDIR)/poi4r.so
ifeq ($(DEBUG),1)
CCFLAGS=-O0 -g
LDFLAGS=-g
else
CCFLAGS=-O2
LDFLAGS=
endif
else

ifeq ($(OS),Linux)
RUBY_SITE=$(PREFIX_RUBY)/lib/ruby/site-ruby/$(RUBY_VER)/
RUBY_INC=$(PREFIX_RUBY)/lib/ruby/$(RUBY_VER)/i686-linux
POI4R_LIB=$(BINDIR)/poi4r.so
ifeq ($(DEBUG),1)
CCFLAGS=-O0 -g -fPIC
LDFLAGS=-g
else
CCFLAGS=-O2 -fPIC
LDFLAGS=
endif
else

ifeq ($(OS),Cygwin)
RUBY_SITE=`cygpath -aw $(PREFIX_RUBY)/Lib/site-packages`
RUBY_INC=`cygpath -aw $(PREFIX_RUBY)/Include`
RUBY_PC=`cygpath -aw $(PREFIX_RUBY)/PC`
POI4R_LIB=$(BINDIR)/poi4r$(_SUFFIX).so
ifeq ($(DEBUG),1)
CCFLAGS=-O -g 
LDFLAGS=-g
else
CCFLAGS=-O2
LDFLAGS=
endif
else

RUBY=unknown
RUBY_SITE=unknown
endif
endif
endif

CLASSES=$(BINDIR)/classes
JAR_CLASSES=$(CLASSES)/jar

CC=$(GCJ_HOME)/bin/gcc
CXX=$(GCJ_HOME)/bin/g++
JCC=$(GCJ_HOME)/bin/gcj
JCCH=$(GCJ_HOME)/bin/gcjh
JAR=$(GCJ_HOME)/bin/jar
POI_ZIP=poi-$(POI_VER).jar
POI_JAR=poi-$(POI_VER).jar
POI_JAR_WC=poi-$(POI_VER)-*.jar

POI4R_CP:=$(BINDIR)/$(POI_JAR):$(CLASSES)

OBJS=$(BINDIR)/poi.o  $(BINDIR)/io.java.o $(BINDIR)/io.cpp.o

LIBS=$(POI4R_LIB)

default: all

patches:


env:
ifndef PREFIX_RUBY
	@echo Operating system is $(OS)
	@echo You need to edit that section of the Makefile
	@false
else
	@true
endif


$(BINDIR):
	mkdir -p $(BINDIR)/classes/jar


DISTRIB=Poi-$(VERSION)

ifeq ($(OS),Cygwin)
POI4R_CP:=`cygpath -awp $(POI4R_CP)`
endif


ifeq ($(OS),Cygwin)
_poi=`cygpath -aw $(POI)`
else
_poi=$(POI)
endif

$(BINDIR)/$(POI_JAR): 
	cp ../../../build/dist/$(POI_JAR_WC) $(BINDIR)/$(POI_JAR)
	cd $(JAR_CLASSES); $(JAR) -xf ../../$(POI_JAR)

$(BINDIR)/io.java.o: java/org/apache/poi/RubyOutputStream.java
	$(JCC) $(JCCFLAGS) -C -d $(CLASSES) java/org/apache/poi/RubyOutputStream.java
	$(JCC) $(JCCFLAGS) $(CCFLAGS) -I$(GCJ_HOME)/include -c -o $@ java/org/apache/poi/RubyOutputStream.java

$(CLASSES)/org/apache/poi/RubyOutputStream.h: $(BINDIR)/io.java.o Makefile
	mkdir -p $(CLASSES)/org/apache/poi/hssf/usermodel
	$(JCCH) -d $(CLASSES) --classpath=$(JAR_CLASSES) org.apache.poi.hssf.usermodel.HSSFWorkbook
	$(JCCH) -d $(CLASSES) --classpath=$(JAR_CLASSES) org.apache.poi.hssf.usermodel.HSSFSheet
	$(JCCH) -d $(CLASSES) --classpath=$(JAR_CLASSES) org.apache.poi.hssf.usermodel.HSSFRow
	$(JCCH) -d $(CLASSES) --classpath=$(JAR_CLASSES) org.apache.poi.hssf.usermodel.HSSFCell
	$(JCCH) -d $(CLASSES) --classpath=$(JAR_CLASSES) org.apache.poi.hssf.usermodel.HSSFHeader
	$(JCCH) -d $(CLASSES) --classpath=$(JAR_CLASSES) org.apache.poi.hssf.usermodel.HSSFFooter
	$(JCCH) -d $(CLASSES) --classpath=$(JAR_CLASSES) org.apache.poi.hssf.usermodel.HSSFFont
	$(JCCH) -d $(CLASSES) --classpath=$(JAR_CLASSES) org.apache.poi.hssf.usermodel.HSSFDataFormat
	$(JCCH) -d $(CLASSES) --classpath=$(JAR_CLASSES) org.apache.poi.hssf.usermodel.HSSFCellStyle
	$(JCCH) -d $(CLASSES) --classpath=$(CLASSES) org.apache.poi.RubyOutputStream


$(BINDIR)/io.cpp.o: $(CLASSES)/org/apache/poi/RubyOutputStream.h cpp/RubyIO.cpp $(BINDIR)/io.java.o
	$(JCC) -I$(RUBY_INC) -I$(GCJ_HOME)/include -I$(CLASSES) $(CCFLAGS) -c -o $@ cpp/RubyIO.cpp

$(BINDIR)/poi.o: $(BINDIR)/$(POI_JAR)
	$(JCC) $(JCCFLAGS) $(CCFLAGS) -c -o $@ $(BINDIR)/$(POI_JAR)

Poi4R_wrap.cxx: $(BINDIR)/io.cpp.o Poi4R.i 
ifdef SWIG
	$(SWIG) $(SWIG_OPT) -I$(CLASSES) -c++ -ruby Poi4R.i
endif


ifeq ($(OS),Darwin)
$(POI4R_LIB): $(OBJS) Poi4R_wrap.cxx
	$(CXX) -shared -bundle -o $@ $(CCFLAGS) $(SWIG_OPT) $(DB_INC) -I$(GCJ_HOME)/include -I$(CLASSES) -I$(RUBY_INC) Poi4R_wrap.cxx $(OBJS) -L$(GCJ_HOME)/lib -lgcj -liconv -undefined suppress -flat_namespace -multiply_defined suppress
else

ifeq ($(OS),Linux)
$(POI4R_LIB): $(OBJS) Poi4R_wrap.cxx
	$(CXX) -shared -o $@ $(CCFLAGS) $(SWIG_OPT) $(DB_INC) -I$(CLASSES) -I$(RUBY_INC) Poi4R_wrap.cxx $(OBJS) -lgcj
else

ifeq ($(OS),Cygwin)
$(POI4R_LIB): $(OBJS) Poi4R_wrap.cxx
	$(CXX) -c $(CCFLAGS) $(PYDBG) -D_NO_OLDNAMES -D_off_t=off_t $(SWIG_OPT) $(DB_INC) -I$(CLASSES) -I$(RUBY_PC) -I$(RUBY_INC) -o $(BINDIR)/Poi4R_wrap.o Poi4R_wrap.cxx
	$(CXX) -shared $(LDFLAGS) -o $@ $(OBJS) `cygpath -aw $(PREFIX_RUBY)/python23$(_SUFFIX).dll` $(BINDIR)/Poi4R_wrap.o -lgcj -lwin32k -lws2_32
endif
endif
endif


all: env  $(BINDIR) $(LIBS)
	@echo build of $(POI4R_LIB) complete

install:: all
	install Poi4R.rb $(RUBY_SITE)
	install $(POI4R_LIB) $(RUBY_SITE)

ifeq ($(OS),Darwin)
install::
	install $(GCJ_HOME)/lib/libgcj.5.dylib $(PREFIX)/lib
	install $(GCJ_HOME)/lib/libstdc++.6.dylib $(PREFIX)/lib
	install $(GCJ_HOME)/lib/libgcc_s.1.0.dylib $(PREFIX)/lib
else

ifeq ($(OS),Linux)
install::

else

ifeq ($(OS),Cygwin)
install::

endif
endif
endif


clean:
	rm -rf $(BINDIR) Poi4R.rb* Poi4R_wrap.cxx

realclean: clean
	rm -rf $(POI) $(STORE) $(DISTRIB)

distrib::
	mkdir -p $(DISTRIB)/python
	install Poi4R.rb $(DISTRIB)/python
	install $(POI4R_LIB) $(DISTRIB)/python
	install README $(DISTRIB)

ifeq ($(OS),Darwin)
distrib::
ifdef DB
	mkdir -p $(DISTRIB)/db
	install $(LIBDB_JAVA_LIB) $(DISTRIB)/db
	install libdb_java-$(DB_LIB_VER).la.osx $(DISTRIB)/db
endif
	mkdir -p $(DISTRIB)/gcj
	install $(GCJ_HOME)/lib/libgcj.5.dylib $(DISTRIB)/gcj
	install $(GCJ_HOME)/lib/libstdc++.6.dylib $(DISTRIB)/gcj
	install $(GCJ_HOME)/lib/libgcc_s.1.0.dylib $(DISTRIB)/gcj
else

ifeq ($(OS),Linux)
distrib::
ifdef DB
	mkdir -p $(DISTRIB)/db
	install $(LIBDB_JAVA_LIB) $(DISTRIB)/db
endif
	mkdir -p $(DISTRIB)/gcj
	install $(GCJ_HOME)/lib/libgcj.so.5 $(DISTRIB)/gcj
	install $(GCJ_HOME)/lib/libstdc++.so.6 $(DISTRIB)/gcj
	install $(GCJ_HOME)/lib/libgcc_s.so.1 $(DISTRIB)/gcj
else

ifeq ($(OS),Cygwin)
distrib::
ifdef DB
	mkdir -p $(DISTRIB)/db
	install $(LIBDB_JAVA_LIB) $(DISTRIB)/db
endif
endif
endif
endif


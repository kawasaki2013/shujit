# $Id$

# feature

CODE_DB = yes
GDBM = yes

XBOX = 

METAVM = 
METAVM_NO_ARRAY = 

# environment

JDK_VER = 13
J_INCDIR1 = /usr/local/java/include-old
J_INCDIR2 = /usr/local/java/include
J_INCDIR_MD = linux

GCC_VER = 300

# command


CC = /usr/bin/gcc
LD_DYNAMIC = ${CC} -shared#	for GCC and GNU binutils
#LD_DYNAMIC = ld -Bdynamic#	for SunOS 4
ASFLAGS = 
OBJDUMP = /usr/bin/objdump
RUBY = /usr/bin/ruby
STRIP = /usr/bin/strip
CI = /usr/bin/ci
CO = /usr/bin/co
RM = /bin/rm
WC = /usr/bin/wc
ETAGS = /usr/bin/etags
JAVA = /usr/local/java/bin/java
JAVAC = /usr/local/java/bin/javac
JAVAH = /usr/local/java/bin/javah
ifeq (${JDK_VER}, 11)
JAVAH_OLD = ${JAVAH}
JAVAH_JNI = ${JAVAH} -jni
else
JAVAH_OLD = ${JAVAH} -old
JAVAH_JNI = ${JAVAH}
endif
JAR = /usr/local/java/bin/jar
JAVADOC = /usr/local/java/bin/javadoc


GENCONST = gentable.rb
POSTPROC = postcmpl.rb

INCDIR = -I${J_INCDIR1} -I${J_INCDIR1}/${J_INCDIR_MD} -I${J_INCDIR2} -I${J_INCDIR2}/${J_INCDIR_MD} -I${OBJDIR} -I/usr/local/include

CFLAGS_COMMON =# -g -DCOMPILE_DEBUG# -DRUNTIME_DEBUG
COPTFLAGS = -O2
CFLAGS_OPT =
CFLAGS_DBG = -g -DDEBUG -DJCOV
NOOPTCFLAGS = -fPIC -pipe ${CFLAGS_${VARIANT}} ${CFLAGS_COMMON} ${INCDIR}
CFLAGS = ${COPTFLAGS} ${NOOPTCFLAGS}
LIBS =

ifeq (${METAVM}, yes)
TARGET_OPT = libmetavm.so
TARGET_DBG = libmetavm_g.so
else
TARGET_OPT = libshujit.so
TARGET_DBG = libshujit_g.so
endif
ifeq (${VARIANT}, DBG)
OBJDIR = obj_g
else
OBJDIR = obj
endif
GENEDHDR = ${OBJDIR}/constants.h
GENEDOBJ = ${OBJDIR}/constants.o
TOOLS = codedbinfo
TOOLSOBJ = codedbinfo.o
# source code
HDR = compiler.h opcodes_internal.h code.h
OBJ = ${OBJDIR}/code.o ${OBJDIR}/compiler.o ${OBJDIR}/signal.o \
	${OBJDIR}/invoker.o ${OBJDIR}/computil.o ${OBJDIR}/opcodes.o \
	${OBJDIR}/compile.o ${OBJDIR}/runtime.o ${OBJDIR}/optimize.o \
	${OBJDIR}/stack.o ${OBJDIR}/x86tsc.o
ifneq (${JDK_VER}, 11)
	OBJ += ${OBJDIR}/linker.o
endif

ifeq (${CODE_DB}, yes)
	OBJ += ${OBJDIR}/codedb.o
#	CFLAGS += -I/usr/local/include
#	LIBS += -L/usr/local/lib
ifeq (${GDBM}, yes)
	DBLIBS = -lgdbm
else
	DBLIBS = -lndbm
endif
endif
# subdirectories
SUBDIR =

# for Xbox

ifeq (${XBOX}, yes)
	OBJ += ${OBJDIR}/support-xbox.o
endif

# for MetaVM
ifeq (${METAVM}, yes)
	SUBDIR += NET/shudo/metavm metavm
	OBJ += metavm/objectid.o metavm/vmop.o metavm/controller.o \
		metavm/proxy.o metavm/byvalue.o metavm/type.o \
		metavm/nativemtd.o
	JARFILE = MetaVM.jar
endif


#
# rules
#

.PHONY: all optimized debug do_config make_objdir subdir tool jar distclean clean subdirclean tag wc

all: optimized

optimized:
	${MAKE} ${TARGET_OPT} VARIANT=OPT
ifdef STRIP
	${STRIP} ${TARGET_OPT}
endif

debug:
	${MAKE} ${TARGET_DBG} VARIANT=DBG

${TARGET_${VARIANT}}: do_config make_objdir subdir ${OBJ} ${GENEDOBJ}
	${LD_DYNAMIC} -o $@ ${OBJ} ${GENEDOBJ} ${LIBS}

do_config:
	@if [ ! -f config.h ]; then \
		(echo 'Type ./configure then make.'; false) \
	fi

make_objdir:
	@if [ ! -d ${OBJDIR} ]; then \
		mkdir -p ${OBJDIR}; \
	fi

subdir:
ifdef SUBDIR
	@for subdir in ${SUBDIR}; do \
		(cd $$subdir && ${MAKE} all CFLAGS="${CFLAGS}") \
	done
endif

tool: codedbinfo

jar: ${JARFILE}
${JARFILE}: NET/shudo/metavm/*.class
	${JAR} cvf ${JARFILE} NET/shudo/metavm/*.class


${OBJDIR}/%.o: %.c
	${COMPILE.c} -o $@ $<


# generate code.o
ifeq (${GCC_VER}, 270)
${OBJDIR}/tmp.s: code.c compiler.h ${POSTPROC}
	${CC} -S -fno-omit-frame-pointer ${NOOPTCFLAGS} -o $@ $<
${OBJDIR}/code.s: ${OBJDIR}/tmp.s
	${RUBY} ${POSTPROC} < $< > $@
#	${RM} -f $<
${OBJDIR}/code.o: ${OBJDIR}/code.s
else
${OBJDIR}/code.o: code.c code.h compiler.h
	${CC} -fno-omit-frame-pointer ${NOOPTCFLAGS} -c -o $@ $<
${OBJDIR}/code.s: code.c code.h compiler.h
	${CC} -S -fno-omit-frame-pointer ${NOOPTCFLAGS} -o $@ $<
endif


${GENEDHDR}: ${OBJDIR}/code.o ${GENCONST}
	(cd ${OBJDIR} && ${OBJDUMP} -dx code.o | ${RUBY} ../${GENCONST})
${GENEDOBJ:.o=.c}: ${OBJDIR}/code.o ${GENCONST}
	(cd ${OBJDIR} && ${OBJDUMP} -dx code.o | ${RUBY} ../${GENCONST})

${OBJDIR}/compile.o: compiler.h ${GENEDHDR} ${GENEDOBJ:.o=.c}
${OBJDIR}/compiler.o: compiler.c compiler.h
${OBJDIR}/runtime.o: runtime.c compiler.h
${OBJDIR}/computil.o: compiler.h
${OBJDIR}/optimize.o: compiler.h
${OBJDIR}/codedb.o: compiler.h

codedbinfo.o: compiler.h
codedbinfo: codedbinfo.o
	${CC} -o $@ $< ${DBLIBS}

# for JDK 1.2
${OBJDIR}/linker.o: ${OBJDIR}/java_util_Vector.h ${OBJDIR}/java_lang_ClassLoader_NativeLibrary.h
${OBJDIR}/java_util_Vector.h:
	$(JAVAH_OLD) -d ${OBJDIR} java.util.Vector
${OBJDIR}/java_lang_ClassLoader_NativeLibrary.h:
	$(JAVAH_OLD) -d ${OBJDIR} java.lang.ClassLoader\$$NativeLibrary


distclean: clean
	${RM} -f config.h config.status config.log
	${RM} -f metavm/GNUmakefile NET/shudo/metavm/GNUmakefile

clean: subdirclean
	${RM} -f TAGS *~
	${RM} -f config.cache confdefs.h
	${RM} -f ${TARGET_OPT} ${TARGET_DBG} ${TOOLS} ${TOOLSOBJ}
	${RM} -rf obj obj_g
ifeq (${METAVM}, yes)
	${RM} -f ${JARFILE}
endif

subdirclean:
ifdef SUBDIR
	@for subdir in ${SUBDIR}; do \
		(cd $$subdir && ${MAKE} clean) \
	done
endif

tag:
	${ETAGS} *.[hc]

wc:
	${WC} *.[hc] ${GENCONST}

.c.s:
	${CC} $(CFLAGS) $(CPPFLAGS) $(TARGET_ARCH) -S $<

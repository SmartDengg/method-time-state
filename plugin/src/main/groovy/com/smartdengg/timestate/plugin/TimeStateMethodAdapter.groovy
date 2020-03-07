package com.smartdengg.timestate.plugin

import org.jetbrains.annotations.NotNull
import org.objectweb.asm.*

/**
 * 创建时间:  2019/09/25 17:59 <br>
 * 作者:  SmartDengg <br>
 * 描述:
 * */
class TimeStateMethodAdapter extends MethodVisitor implements Opcodes {

  private static final UNKNOWN_LINENUMBER = -1

  private static final String timeStateLoggerOwner = "com/smartdengg/timestate/runtime/TimeStateLogger"
  private static final String timeStateLoggerEntry = "entry"
  private static final String timeStateLoggerExit = "exit"
  private static final String timeStateLoggerLogName = "log"

  //void entry(String encloseDescriptor, String descriptor, String className, String methodName, String arguments, String returnType)
  private static final String timeStateLoggerStartDesc = "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V"

  //void exit(String encloseDescriptor, String descriptor, String lineNumber)
  private static final String timeStateLoggerStopDesc = "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V"

  //  log(String encloseDescriptor)
  private static final String timeStateLoggerLogDesc = "(Ljava/lang/String;)V"

  private String className
  private String methodName
  private String methodDesc
  private String methodArguments
  private String methodReturn
  private String encloseDescriptor

  private boolean hasTimeAnnotation
  private boolean hasFullTimeAnnotation
  private boolean hasTracedAnnotation
  private boolean isWoven
  private int methodEntryLineNumber = UNKNOWN_LINENUMBER
  private List<String> measuredMethodInfo
  private int count

  TimeStateMethodAdapter(MethodVisitor mv, String className, String methodName, String desc,
      List<String> measuredMethodInfo) {
    //noinspection UnnecessaryQualifiedReference
    super(Opcodes.ASM6, mv)
    this.className = className.replace("/", '.')
    this.methodName = methodName
    this.methodDesc = desc
    this.methodArguments = getArguments(methodDesc)
    this.methodReturn = getReturnType(methodDesc)
    this.measuredMethodInfo = measuredMethodInfo
    this.encloseDescriptor =
        getDescriptor(this.className, this.methodName, this.methodArguments, this.methodReturn)
  }

  @Override /*①*/
  AnnotationVisitor visitAnnotation(String desc, boolean visible) {
    hasTimeAnnotation |= desc == "Lcom/smartdengg/timestate/runtime/TimeState;"
    hasFullTimeAnnotation |= desc == "Lcom/smartdengg/timestate/runtime/FullTimeState;"
    hasTracedAnnotation |= desc == "Lcom/smartdengg/timestate/runtime/TimeTraced;"
    return super.visitAnnotation(desc, visible)
  }

  @Override /*②*/
  void visitCode() {
    if (hasAnyTimeStateAnnotation()) {
      count++
      addTimedAnno(mv)
      addTimeStateCodeBlock(className, methodName, methodDesc, "NULL", true, false)
    }
    super.visitCode()
  }

  @Override /*③*/
  void visitLineNumber(int line, Label start) {
    if (hasAnyTimeStateAnnotation()) {
      if (methodEntryLineNumber == UNKNOWN_LINENUMBER) methodEntryLineNumber = line
    }
    super.visitLineNumber(line, start)
  }

  @Override
  void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
    if (hasFullTimeStateAnnotation() && owner != timeStateLoggerOwner) {
      count++
      addTimedAnno(mv)
      addTimeStateCodeBlock(owner.replace("/", '.'), name, desc, "NULL", true, false)
    }
    super.visitMethodInsn(opcode, owner, name, desc, itf)
    if (hasFullTimeStateAnnotation() && owner != timeStateLoggerOwner) {
      addTimeStateCodeBlock(owner.replace("/", '.'), name, desc, "NULL", false, true)
    }
  }

  @Override /*④*/
  void visitInsn(int opcode) {
    if (hasAnyTimeStateAnnotation()) {
      if (opcode >= IRETURN && opcode <= RETURN) {
        addTimeStateCodeBlock(className, methodName, methodDesc,
            String.valueOf(methodEntryLineNumber), false, true)
        addTimeStateLogBlock()
      }
    }
    super.visitInsn(opcode)
  }

  @Override
  void visitMaxs(int maxStack, int maxLocals) {
    if (isWoven) {
      maxStack += 6 * count
    }
    super.visitMaxs(maxStack, maxLocals)
  }

  @Override
  void visitEnd() {
    super.visitEnd()
    if (isWoven) {
      measuredMethodInfo.add(
          methodReturn + " " + methodName + "(" + methodArguments + ")" + methodReturn)
    }
  }

  private void addTimeStateCodeBlock(String owner, String name, String desc, String lineNumber,
      boolean start,
      boolean stop) {

    visitLdcInsn(encloseDescriptor)
    visitLdcInsn(getDescriptor(owner, name, getArguments(desc), getReturnType(desc)))
    if (start) {
      visitLdcInsn(owner)
      visitLdcInsn(name)
      visitLdcInsn(getArguments(desc))
      visitLdcInsn(getReturnType(desc))
      visitMethodInsn(INVOKESTATIC, timeStateLoggerOwner, timeStateLoggerEntry,
          timeStateLoggerStartDesc, false)
    } else if (stop) {
      visitLdcInsn(lineNumber == "NULL" ? "" : lineNumber)
      visitMethodInsn(INVOKESTATIC, timeStateLoggerOwner, timeStateLoggerExit,
          timeStateLoggerStopDesc, false)
    }
  }

  private void addTimeStateLogBlock() {
    visitLdcInsn(encloseDescriptor)
    visitMethodInsn(INVOKESTATIC, timeStateLoggerOwner, timeStateLoggerLogName,
        timeStateLoggerLogDesc, false)
  }

  @NotNull
  private static String getArguments(String methodDesc) {
    Type[] argumentTypes = Type.getArgumentTypes(methodDesc)
    StringBuilder arguments = new StringBuilder(argumentTypes.length)
    for (type in argumentTypes) {
      arguments.append(type.className)
    }
    return arguments.toString()
  }

  @NotNull
  private static String getReturnType(String methodDesc) {
    Type returnType = Type.getReturnType(methodDesc)
    return returnType.className
  }

  private void addTimedAnno(MethodVisitor mv) {
    if (!isWoven) {
      AnnotationVisitor annotationVisitor =
          mv.visitAnnotation("Lcom/smartdengg/timestate/runtime/TimeTraced;", false)
      annotationVisitor.visitEnd()
      this.isWoven = true
    }
  }

  private boolean hasAnyTimeStateAnnotation() {
    return (hasTimeAnnotation || hasFullTimeAnnotation) && !hasTracedAnnotation
  }

  private boolean hasFullTimeStateAnnotation() {
    return hasFullTimeAnnotation && !hasTracedAnnotation
  }

  private static String getDescriptor(String className, String methodName, String arguments,
      String returnType) {
    return "${className}.${methodName}(${arguments})$returnType"
  }
}

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

  //  TimeStateLogger
  private static final String timeStateLoggerOwner = "com/smartdengg/timestate/runtime/TimeStateLogger"
  private static final String timeStateLoggerEntryMethodName = "entry"
  private static final String timeStateLoggerExitMethodName = "exit"
  private static final String timeStateLoggerLogMethodName = "log"

  // void entry(boolean isEnclosing, String descriptor)
  private static final String timeStateLoggerEntryMethodDesc = "(ZLjava/lang/String;)V"
  // void exit(boolean isEnclosing, String descriptor, String lineNumber)
  private static final String timeStateLoggerExitMethodDesc = "(ZLjava/lang/String;Ljava/lang/String;)V"
  // void log()
  private static final String timeStateLoggerLogMethodDesc = "()V"

  //  @TimeState
  private static final String timeStateDesc = "Lcom/smartdengg/timestate/runtime/TimeState;"
  //  @TimeStatePro
  private static final String TimeStateProDesc = "Lcom/smartdengg/timestate/runtime/TimeStatePro;"
  //  @TimeTraced
  private static final String TimeTracedDesc = "Lcom/smartdengg/timestate/runtime/TimeTraced;"

  private String className
  private String methodName
  private String methodDesc
  private String methodArguments
  private String methodReturn
  private String encloseDescriptor

  private boolean hasTimeStateAnnotation
  private boolean hasTimeStateProAnnotation
  private boolean hasTimeTracedAnnotation
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
    hasTimeStateAnnotation |= desc == timeStateDesc
    hasTimeStateProAnnotation |= desc == TimeStateProDesc
    hasTimeTracedAnnotation |= desc == TimeTracedDesc
    return super.visitAnnotation(desc, visible)
  }

  @Override /*②*/
  void visitCode() {
    if (hasAnyTimeStateAnnotation()) {
      count++
      addTimedAnnotation(mv)
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
    boolean shouldWave = hasTimeStateProAnnotation() && owner != timeStateLoggerOwner
    if (shouldWave) {
      count++
      addTimedAnnotation(mv)
      addTimeStateCodeBlock(owner.replace("/", '.'), name, desc, "NULL", true, false)
    }
    super.visitMethodInsn(opcode, owner, name, desc, itf)
    if (shouldWave) {
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
  void visitEnd() {
    super.visitEnd()
    if (isWoven) {
      measuredMethodInfo.add(methodReturn + " " + methodName + "(" + methodArguments + ")")
    }
  }

  private void addTimeStateCodeBlock(String owner, String name, String desc, String lineNumber,
      boolean enter, boolean exit) {
    String methodDescriptor = getDescriptor(owner, name, getArguments(desc), getReturnType(desc))
    if (methodDescriptor == encloseDescriptor) {
      visitInsn(ICONST_1)
    } else {
      visitInsn(ICONST_0)
    }
    visitLdcInsn(methodDescriptor)
    if (enter) {
      visitMethodInsn(INVOKESTATIC, timeStateLoggerOwner, timeStateLoggerEntryMethodName,
          timeStateLoggerEntryMethodDesc, false)
    } else if (exit) {
      visitLdcInsn(lineNumber == "NULL" ? "" : lineNumber)
      visitMethodInsn(INVOKESTATIC, timeStateLoggerOwner, timeStateLoggerExitMethodName,
          timeStateLoggerExitMethodDesc, false)
    }
  }

  private void addTimeStateLogBlock() {
    visitMethodInsn(INVOKESTATIC, timeStateLoggerOwner, timeStateLoggerLogMethodName,
        timeStateLoggerLogMethodDesc, false)
  }

  @NotNull
  private static String getArguments(String methodDesc) {
    Type[] argumentTypes = Type.getArgumentTypes(methodDesc)
    def arguments = []
    for (type in argumentTypes) {
      arguments.add(type.className)
    }
    return arguments.join(';')
  }

  @NotNull
  private static String getReturnType(String methodDesc) {
    Type returnType = Type.getReturnType(methodDesc)
    return returnType.className
  }

  private void addTimedAnnotation(MethodVisitor mv) {
    if (!isWoven) {
      AnnotationVisitor annotationVisitor = mv.visitAnnotation(TimeTracedDesc, false)
      annotationVisitor.visitEnd()
      this.isWoven = true
    }
  }

  private boolean hasAnyTimeStateAnnotation() {
    return (hasTimeStateAnnotation || hasTimeStateProAnnotation) && !hasTimeTracedAnnotation
  }

  private boolean hasTimeStateProAnnotation() {
    return hasTimeStateProAnnotation && !hasTimeTracedAnnotation
  }

  private static String getDescriptor(String owner, String name, String arguments,
      String returnType) {
    return "${owner}/${name}/${arguments}/$returnType"
  }
}

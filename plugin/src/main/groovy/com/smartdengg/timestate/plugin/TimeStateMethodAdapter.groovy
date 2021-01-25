package com.smartdengg.timestate.plugin

import com.smartdengg.timestate.plugin.Constants
import org.jetbrains.annotations.NotNull
import org.objectweb.asm.*

/**
 * 创建时间:  2019/09/25 17:59 <br>
 * 作者:  SmartDengg <br>
 * 描述:  对 @TimeState 和 @TimeStatePro 标记的函数进行字节码的修改*/
class TimeStateMethodAdapter extends MethodVisitor implements Opcodes {

  private static final UNKNOWN_LINENUMBER = -1

  private final String className
  private final String methodName
  private final String methodDesc
  private final String methodArguments
  private final String methodReturn
  private final String encloseDescriptor
  private final List<String> measuredMethodInfo

  private boolean hasTimeStateAnnotation
  private boolean hasTimeStateProAnnotation
  private boolean hasTimeTracedAnnotation
  private boolean isWoven
  private int methodEntryLineNumber = UNKNOWN_LINENUMBER
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
    hasTimeStateAnnotation |= desc == Constants.timeStateDesc
    hasTimeStateProAnnotation |= desc == Constants.timeStateProDesc
    hasTimeTracedAnnotation |= desc == Constants.timeTracedDesc
    return super.visitAnnotation(desc, visible)
  }

  @Override /*②*/
  void visitCode() {
    if (hasAnyTimeStateAnnotation()) {
      count++
      addTimedAnnotation()
      addTimeStateCodeBlock(className, methodName, methodDesc, null, true, false)
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
    boolean shouldWave = hasTimeStateProAnnotation() && owner != Constants.timeStateLoggerOwner
    if (shouldWave) {
      count++
      addTimedAnnotation()
      addTimeStateCodeBlock(owner.replace("/", '.'), name, desc, null, true, false)
    }
    super.visitMethodInsn(opcode, owner, name, desc, itf)
    if (shouldWave) {
      addTimeStateCodeBlock(owner.replace("/", '.'), name, desc, null, false, true)
    }
  }

  @Override /*④*/
  void visitInsn(int opcode) {
    if (hasAnyTimeStateAnnotation()) {
      if (opcode == ATHROW || opcode >= IRETURN && opcode <= RETURN) {
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
      visitMethodInsn(INVOKESTATIC,
          Constants.timeStateLoggerOwner,
          Constants.timeStateLoggerEntryMethodName,
          Constants.timeStateLoggerEntryMethodDesc,
          false)
    } else if (exit) {
      // 不使用 ACONST_NULL 的原因是: 它将生成两条指令 aconst_null 和 checkcast，不够简洁
      visitLdcInsn(lineNumber == null ? '' : lineNumber)
      visitMethodInsn(INVOKESTATIC,
          Constants.timeStateLoggerOwner,
          Constants.timeStateLoggerExitMethodName,
          Constants.timeStateLoggerExitMethodDesc,
          false)
    }
  }

  private void addTimeStateLogBlock() {
    visitMethodInsn(INVOKESTATIC,
        Constants.timeStateLoggerOwner,
        Constants.timeStateLoggerLogMethodName,
        Constants.timeStateLoggerLogMethodDesc,
        false)
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

  private void addTimedAnnotation() {
    if (!isWoven) {
      AnnotationVisitor annotationVisitor = mv.visitAnnotation(Constants.timeTracedDesc, false)
      annotationVisitor.visitEnd()
      isWoven = true
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

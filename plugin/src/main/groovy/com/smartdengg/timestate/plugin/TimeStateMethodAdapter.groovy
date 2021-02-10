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
  private final List<String> tracedMethods

  private boolean isWoven
  private int methodEntryLineNumber = UNKNOWN_LINENUMBER
  private int count
  private PreCheckMethodVisitor preCheckMethodVisitor

  TimeStateMethodAdapter(MethodVisitor mv, String className, String methodName, String desc,
      List<String> tracedMethods) {
    //noinspection UnnecessaryQualifiedReference
    super(Opcodes.ASM6, mv)
    this.className = className.replace("/", '.')
    this.methodName = methodName
    this.methodDesc = desc
    this.methodArguments = getArguments(methodDesc)
    this.methodReturn = getReturnType(methodDesc)
    this.tracedMethods = tracedMethods
    this.encloseDescriptor =
        getDescriptor(this.className, this.methodName, this.methodArguments, this.methodReturn)
  }

  @Override /*②*/
  void visitCode() {
    super.visitCode()
    count++
    addTimedAnnotation()
    addTimeStateCodeBlock(className, methodName, methodDesc, null, true, false)
  }

  @Override /*③*/
  void visitLineNumber(int line, Label start) {
    super.visitLineNumber(line, start)
    if (methodEntryLineNumber == UNKNOWN_LINENUMBER) {
      methodEntryLineNumber = line
    }
  }

  @Override
  void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
    boolean shouldWave = preCheckMethodVisitor.hasTimeStateProAnnotation()
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
    if (opcode == ATHROW || opcode >= IRETURN && opcode <= RETURN) {
      addTimeStateCodeBlock(className, methodName, methodDesc,
          String.valueOf(methodEntryLineNumber), false, true)
      addTimeStateLogBlock()
    }
    super.visitInsn(opcode)
  }

  @Override
  void visitEnd() {
    super.visitEnd()
    if (isWoven) {
      tracedMethods.add(methodReturn + " " + methodName + "(" + methodArguments + ")")
    }
  }

  private void addTimeStateCodeBlock(String owner, String name, String desc, String lineNumber,
      boolean enter, boolean exit) {
    String methodDescriptor = getDescriptor(owner, name, getArguments(desc), getReturnType(desc))
    if (methodDescriptor == encloseDescriptor) {
      mv.visitInsn(ICONST_1)
    } else {
      mv.visitInsn(ICONST_0)
    }
    mv.visitLdcInsn(methodDescriptor)
    if (enter) {
      mv.visitMethodInsn(INVOKESTATIC,
          Constants.timeStateLoggerOwner,
          Constants.timeStateLoggerEntryMethodName,
          Constants.timeStateLoggerEntryMethodDesc,
          false)
    } else if (exit) {
      // 不使用 ACONST_NULL 的原因是: 它将生成两条指令 aconst_null 和 checkcast，不够简洁
      mv.visitLdcInsn(lineNumber == null ? '' : lineNumber)
      mv.visitMethodInsn(INVOKESTATIC,
          Constants.timeStateLoggerOwner,
          Constants.timeStateLoggerExitMethodName,
          Constants.timeStateLoggerExitMethodDesc,
          false)
    }
  }

  private void addTimeStateLogBlock() {
    mv.visitMethodInsn(INVOKESTATIC,
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
    return Type.getReturnType(methodDesc).className
  }

  private void addTimedAnnotation() {
    if (!isWoven) {
      AnnotationVisitor annotationVisitor = mv.visitAnnotation(Constants.timeTracedDesc, false)
      annotationVisitor.visitEnd()
      isWoven = true
    }
  }

  private static String getDescriptor(String owner, String name, String arguments,
      String returnType) {
    return "${owner}/${name}/${arguments}/$returnType"
  }

  void setPreCheckMethodVisitor(PreCheckMethodVisitor preCheckMethodVisitor) {
    this.preCheckMethodVisitor = preCheckMethodVisitor
  }
}

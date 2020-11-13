package com.smartdengg.timestate.plugin

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

/**
 * 创建时间: 2020/06/15 <br>
 * 作者: dengwei <br>
 * 描述: 修改日志输出的 tag
 */
class TAGClassAdapter extends ClassVisitor implements Opcodes {

  private static final String LOGGER_CLASS_NAME = "com.smartdengg.timestate.runtime.TimeStateLogger"

  private String className

  TAGClassAdapter(ClassVisitor classVisitor) {
    //noinspection UnnecessaryQualifiedReference
    super(Opcodes.ASM6, classVisitor)
  }

  @Override
  void visit(int version, int access, String name, String signature, String superName,
      String[] interfaces) {
    super.visit(version, access, name, signature, superName, interfaces)
    this.className = name.replace('/', '.')
  }

  @Override
  MethodVisitor visitMethod(int access, String name, String descriptor, String signature,
      String[] exceptions) {
    MethodVisitor methodVisitor = cv.visitMethod(access, name, descriptor, signature, exceptions)
    if (methodVisitor != null && className == LOGGER_CLASS_NAME && name == '<clinit>') {
      methodVisitor.visitLdcInsn(TimeStateTransform.TAG)
      methodVisitor.visitFieldInsn(PUTSTATIC, LOGGER_CLASS_NAME.replace('.', '/'), "TAG",
          "Ljava/lang/String;")
      ColoredLogger.logBlue("TimeStateLogger.TAG = $TimeStateTransform.TAG")
    }
    if (methodVisitor != null) {
      ColoredLogger.logRed(
          "timestate:class = $className,name = $name,descriptor = $descriptor,signature = $name,signature = $name")
    }
    return methodVisitor
  }
}
package com.smartdengg.timestate.plugin

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

/**
 * 创建时间:  2019/09/25 17:58 <br>
 * 作者:  SmartDengg <br>
 * 描述:
 **/
class TimeStateClassAdapter extends ClassVisitor implements Opcodes {

  String className
  List<String> measuredMethodInfo = []

  TimeStateClassAdapter(ClassVisitor classVisitor) {
    //noinspection UnnecessaryQualifiedReference
    super(Opcodes.ASM6, classVisitor)
  }

  @Override
  void visit(int version, int access, String name, String signature, String superName,
      String[] interfaces) {
    super.visit(version, access, name, signature, superName, interfaces)
    this.className = name
  }

  @Override
  MethodVisitor visitMethod(int access, final String name, String desc, String signature,
      String[] exceptions) {

    MethodVisitor methodVisitor = cv.visitMethod(access, name, desc, signature, exceptions)

    if (methodVisitor != null) {
      methodVisitor =
          new TimeStateMethodAdapter(methodVisitor, className, name, desc, measuredMethodInfo)
    }

    return methodVisitor
  }
}

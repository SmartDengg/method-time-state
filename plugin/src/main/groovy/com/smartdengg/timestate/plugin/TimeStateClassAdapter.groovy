package com.smartdengg.timestate.plugin

import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.Attribute
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Handle
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.MethodNode

/**
 * 创建时间:  2019/09/25 17:58 <br>
 * 作者:  SmartDengg <br>
 * 描述:
 **/
class TimeStateClassAdapter extends ClassVisitor implements Opcodes {

  String className
  List<String> methods = []

  TimeStateClassAdapter(ClassVisitor classVisitor) {
    //noinspection UnnecessaryQualifiedReference
    super(Opcodes.ASM7, classVisitor)
  }

  @Override
  void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
    super.visit(version, access, name, signature, superName, interfaces)
    this.className = name
  }

  @Override
  void visitAttribute(Attribute attribute) {
    super.visitAttribute(attribute)
  }

  @Override
  AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
    return super.visitAnnotation(descriptor, visible)
  }

  @Override
  MethodVisitor visitMethod(int access, final String name, String desc, String signature, String[] exceptions) {
    MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions)
    if (mv != null && className == ("com.smartdengg.timestate.sample.MainActivity".replace('.', '/'))) {
      return new PreCheckMethodVisitor(new MethodNode(access, name, desc, signature, exceptions),
              new TimeStateMethodAdapter(mv, className, name, desc, methods), mv)
//      return new LambdaSeeker(preCheckMethodVisitor, className, name, desc, signature)
    }
    return mv
  }

  private static class LambdaSeeker extends MethodVisitor {
    String className
    String name
    String desc
    String signature

    LambdaSeeker(MethodVisitor methodVisitor, String className, String name, String desc, String signature) {
      super(Opcodes.ASM7, methodVisitor)
      this.className = className
      this.name = name
      this.desc = desc
      this.signature = signature
    }

    @Override
    void visitInvokeDynamicInsn(String name, String desc, Handle bsm, Object... bsmArgs) {
      Type methodType = Type.getMethodType(desc)
      String internalName = methodType.getReturnType().getInternalName()
      super.visitInvokeDynamicInsn(name, desc, bsm, bsmArgs)
    }
  }
}

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
  private static final String LOGGER_TAG_FILED_NAME = "TAG"
  private static final String LOGGER_EMOJI_FILED_NAME = "SUPPORT_EMOJI"

  private String className

  TAGClassAdapter(ClassVisitor classVisitor) {
    //noinspection UnnecessaryQualifiedReference
    super(Opcodes.ASM7, classVisitor)
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
      visitTag(methodVisitor)
      visitEmoji(methodVisitor)
    }
    return methodVisitor
  }

  private static void visitTag(MethodVisitor methodVisitor) {
    methodVisitor.visitLdcInsn(TimeStateTransform.tag)
    methodVisitor.visitFieldInsn(PUTSTATIC, LOGGER_CLASS_NAME.replace('.', '/'),
        LOGGER_TAG_FILED_NAME, "Ljava/lang/String;")
    ColoredLogger.logBlue("TimeStateLogger.TAG = $TimeStateTransform.tag")
  }

  private static void visitEmoji(MethodVisitor methodVisitor) {
    if (TimeStateTransform.emoji) {
      methodVisitor.visitInsn(ICONST_1)
    } else {
      methodVisitor.visitInsn(ICONST_0)
    }
    methodVisitor.visitFieldInsn(PUTSTATIC, LOGGER_CLASS_NAME.replace('.', '/'),
        LOGGER_EMOJI_FILED_NAME, "Z")
    ColoredLogger.logBlue("TimeStateLogger.SUPPORT_EMOJI = $TimeStateTransform.emoji")
  }
}
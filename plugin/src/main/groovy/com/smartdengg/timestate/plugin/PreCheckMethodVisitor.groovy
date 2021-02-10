package com.smartdengg.timestate.plugin

import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.AnnotationNode
import org.objectweb.asm.tree.MethodNode

class PreCheckMethodVisitor extends MethodVisitor {

  private MethodNode methodNode
  private TimeStateMethodAdapter timeStateMethodAdapter
  private MethodVisitor originMethodVisitor
  private boolean hasTimeStateAnnotation
  private boolean hasTimeStateProAnnotation
  private boolean hasTimeTracedAnnotation

  PreCheckMethodVisitor(MethodNode methodNode, TimeStateMethodAdapter timeStateMethodAdapter,
      MethodVisitor originMethodVisitor) {
    super(Opcodes.ASM6, methodNode)
    this.methodNode = methodNode
    this.timeStateMethodAdapter = timeStateMethodAdapter
    this.timeStateMethodAdapter.setPreCheckMethodVisitor(this)
    this.originMethodVisitor = originMethodVisitor
  }

  @Override
  void visitEnd() {
    super.visitEnd()
    if (hasAnyTimeStateAnnotation(methodNode.invisibleAnnotations)) {
      methodNode.accept(timeStateMethodAdapter)
    } else {
      methodNode.accept(originMethodVisitor)
    }
  }

  private boolean hasAnyTimeStateAnnotation(List<AnnotationNode> list) {
    if (list == null || list.size() == 0) {
      return false
    }
    for (AnnotationNode node : list) {
      hasTimeStateAnnotation |= node.desc == Constants.timeStateDesc
      hasTimeStateProAnnotation |= node.desc == Constants.timeStateProDesc
      hasTimeTracedAnnotation |= node.desc == Constants.timeTracedDesc
    }
    return (hasTimeStateAnnotation || hasTimeStateProAnnotation) && !hasTimeTracedAnnotation
  }

  boolean hasTimeStateProAnnotation() {
    return hasTimeStateProAnnotation && !hasTimeTracedAnnotation
  }
}
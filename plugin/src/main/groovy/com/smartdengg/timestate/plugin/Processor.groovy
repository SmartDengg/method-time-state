package com.smartdengg.timestate.plugin

import com.google.common.collect.ImmutableMap
import com.google.common.collect.Iterables
import groovy.transform.PackageScope
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter

import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes

class Processor {

  enum Input {
    JAR,
    FILE
  }

  @PackageScope static void run(Path input, Path output, Input type) throws IOException {

    switch (type) {
      case Input.JAR:
        processJar(input, output)
        break
      case Input.FILE:
        processFile(input, output)
        break
    }
  }

  private static void processJar(Path input, Path output) {

    Map<String, String> env = ImmutableMap.of('create', 'true')
    URI inputUri = URI.create("jar:file:$input")
    URI outputUri = URI.create("jar:file:$output")

    FileSystems.newFileSystem(inputUri, env).withCloseable { inputFileSystem ->
      FileSystems.newFileSystem(outputUri, env).withCloseable { outputFileSystem ->
        Path inputRoot = Iterables.getOnlyElement(inputFileSystem.rootDirectories)
        Path outputRoot = Iterables.getOnlyElement(outputFileSystem.rootDirectories)
        processFile(inputRoot, outputRoot)
      }
    }
  }

  private static void processFile(Path input, Path output) {

    Files.walkFileTree(input, new SimpleFileVisitor<Path>() {
      @Override
      FileVisitResult visitFile(Path inputPath, BasicFileAttributes attrs) throws IOException {
        Path outputPath = Utils.toOutputPath(output, input, inputPath)
        directRun(inputPath, outputPath)
        return FileVisitResult.CONTINUE
      }

      @Override
      FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        Path outputPath = Utils.toOutputPath(output, input, dir)
        Files.createDirectories(outputPath)
        return FileVisitResult.CONTINUE
      }
    })
  }

  @PackageScope static void directRun(Path input, Path output) {
    if (Utils.isMatchCondition(input.toString())) {
      byte[] inputBytes = Files.readAllBytes(input)
      byte[] outputBytes = visitAndReturnBytecode(inputBytes)
      Files.write(output, outputBytes)
    } else {
      Files.copy(input, output)
    }
  }

  private static byte[] visitAndReturnBytecode(byte[] originBytes) {

    ClassReader classReader = new ClassReader(originBytes)
    ClassWriter classWriter = new ClassWriter(classReader, 0)
    TimeStateClassAdapter classAdapter = new TimeStateClassAdapter(classWriter)
    classReader.accept(classAdapter, ClassReader.EXPAND_FRAMES)

    List<String> tracedMethodInfo = classAdapter.measuredMethodInfo
    if (tracedMethodInfo != null && tracedMethodInfo.size() > 0) {
      ColoredLogger.logYellow("[TimeState] $classAdapter.className: ")
      for (String method : tracedMethodInfo) {
        ColoredLogger.logYellow("   --> $method")
      }
    }
    return classWriter.toByteArray()
  }
}
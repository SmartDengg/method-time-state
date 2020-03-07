package com.smartdengg.timestate.plugin

import com.android.annotations.NonNull
import com.android.build.api.transform.*
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.internal.pipeline.TransformManager
import groovy.util.logging.Slf4j
import org.apache.commons.io.FileUtils
import org.gradle.api.Project

import java.nio.file.Files
import java.nio.file.Path

import static com.google.common.base.Preconditions.checkNotNull

@Slf4j
class TimeStateTransform extends Transform {

  private Project project

  TimeStateTransform(Project project) {
    this.project = project
  }

  @NonNull
  @Override
  String getName() {
    return "timeState"
  }

  @NonNull
  @Override
  Set<QualifiedContent.ContentType> getInputTypes() {
    return TransformManager.CONTENT_CLASS
  }

  @NonNull
  @Override
  Set<QualifiedContent.Scope> getScopes() {
    if (project.plugins.hasPlugin(AppPlugin)) return TransformManager.SCOPE_FULL_PROJECT
    return TransformManager.PROJECT_ONLY
  }

  @Override
  boolean isIncremental() {
    return true
  }

  @Override
  void transform(TransformInvocation invocation)
      throws TransformException, InterruptedException, IOException {

    ForkJoinExecutor executor = ForkJoinExecutor.instance

    TimeStateSetting setting = project.extensions["${TimeStateSetting.NAME}"]

    TransformOutputProvider outputProvider = checkNotNull(invocation.getOutputProvider(),
        "Missing output object for run " + getName())
    if (!invocation.isIncremental()) outputProvider.deleteAll()

    try {
      invocation.inputs.each { inputs ->

        inputs.jarInputs.each { jarInput ->

          File input = jarInput.file
          File output = outputProvider.getContentLocation(//
              jarInput.name,
              jarInput.contentTypes,
              jarInput.scopes,
              Format.JAR)

          if (!setting.enable) {
            FileUtils.copyFile(input, output)
          } else {
            if (invocation.isIncremental()) {

              switch (jarInput.status) {
                case Status.NOTCHANGED:
                  break
                case Status.ADDED:
                case Status.CHANGED:
                  Files.deleteIfExists(output.toPath())
                  executor.execute {
                    Processor.run(input.toPath(), output.toPath(), Processor.Input.JAR)
                  }
                  break
                case Status.REMOVED:
                  Files.deleteIfExists(output.toPath())
                  break
              }
            } else {
              executor.execute {
                Processor.run(input.toPath(), output.toPath(), Processor.Input.JAR)
              }
            }
          }
        }

        inputs.directoryInputs.each { directoryInput ->

          File input = directoryInput.file
          File output = outputProvider.getContentLocation(//
              directoryInput.name,
              directoryInput.contentTypes,
              directoryInput.scopes,
              Format.DIRECTORY)

          if (!setting.enable) {
            FileUtils.copyDirectory(input, output)
          } else {
            if (invocation.isIncremental()) {
              directoryInput.changedFiles.each { File inputFile, Status status ->

                Path inputPath = inputFile.toPath()
                Path outputPath = Utils.toOutputPath(output.toPath(), input.toPath(), inputPath)

                switch (status) {
                  case Status.NOTCHANGED:
                    break
                  case Status.ADDED:
                  case Status.CHANGED:
                    executor.execute {
                      //direct run byte code
                      Processor.directRun(inputPath, outputPath)
                    }
                    break
                  case Status.REMOVED:
                    Files.deleteIfExists(outputPath)
                    break
                }
              }
            } else {
              executor.execute {
                Processor.run(input.toPath(), output.toPath(), Processor.Input.FILE)
              }
            }
          }
        }
      }
    } finally {
      executor.waitingForAllTasks()
    }
  }
}

package com.smartdengg.timestate.plugin

import com.android.build.gradle.*
import groovy.util.logging.Slf4j
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project

@Slf4j
class TimeStateGradlePlugin implements Plugin<Project> {

  @Override void apply(Project project) {

    def androidPlugin = [AppPlugin, LibraryPlugin, FeaturePlugin]
        .collect { project.plugins.findPlugin(it) as BasePlugin }
        .find { it != null }

    log.debug('Found Plugin: {}', androidPlugin)

    if (!androidPlugin) {
      throw new GradleException(
          "'com.android.application' or 'com.android.library' or 'com.android.feature' plugin required.")
    }

    project.repositories.maven {
      url "https://jitpack.io"
    }

    println()
    ColoredLogger.logBlue('#### 欢迎使用 TimeState 编译插件，任何疑问请联系 hi4joker@gmail.com ####')
    println()

    project.dependencies {
      implementation 'com.github.SmartDengg:method-time-state-runtime:1.2.0'
    }

    //    project.configurations.implementation.dependencies.add(project.dependencies.create(
    //        project.rootProject.findProject("time-state-runtime")))

    project.extensions["${TimeStateSetting.NAME}"] = project.objects.newInstance(TimeStateSetting)

    def extension = project.extensions.getByName("android") as BaseExtension
    extension.registerTransform(new TimeStateTransform(project))
  }
}



package com.smartdengg.timestate.plugin

import com.android.SdkConstants
import com.android.utils.FileUtils

import java.nio.file.Path

class Utils {

  static File toOutputFile(File outputDir, File inputDir, File inputFile) {
    return new File(outputDir, FileUtils.relativePossiblyNonExistingPath(inputFile, inputDir))
  }

  static Path toOutputPath(Path outputRoot, Path inputRoot, Path inputPath) {
    return outputRoot.resolve(inputRoot.relativize(inputPath))
  }

  static boolean isMatchCondition(String name) {
    return name.endsWith(SdkConstants.DOT_CLASS) && //
        !name.matches('.*/R\\$.*\\.class|.*/R\\.class') && //
        !name.matches('.*/BuildConfig\\.class')
  }
}
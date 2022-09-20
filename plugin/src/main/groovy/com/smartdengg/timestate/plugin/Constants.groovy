package com.smartdengg.timestate.plugin

/**
 * 创建时间:  2021/01/05 17:10 <br>
 * 作者:  SmartDengg <br>
 * 描述:  字符串常量
 */
class Constants {

  //  TimeStateLogger
  static final String timeStateLoggerOwner = "com/smartdengg/timestate/runtime/TimeStateLogger"
  static final String timeStateLoggerEntryMethodName = "entry"
  static final String timeStateLoggerExitMethodName = "exit"
  static final String timeStateLoggerLogMethodName = "log"

  // void entry(boolean isEnclosing, String descriptor)
  static final String timeStateLoggerEntryMethodDesc = "(ZLjava/lang/String;)V"
  // void exit(boolean isEnclosing, String descriptor, String lineNumber)
  static final String timeStateLoggerExitMethodDesc = "(ZLjava/lang/String;Ljava/lang/String;)V"
  // void log()
  static final String timeStateLoggerLogMethodDesc = "(Ljava/lang/String;)V"

  //  @TimeState
  static final String timeStateDesc = "Lcom/smartdengg/timestate/runtime/TimeState;"
  //  @TimeStatePro
  static final String timeStateProDesc = "Lcom/smartdengg/timestate/runtime/TimeStatePro;"
  //  @TimeTraced
  static final String timeTracedDesc = "Lcom/smartdengg/timestate/runtime/TimeTraced;"
}

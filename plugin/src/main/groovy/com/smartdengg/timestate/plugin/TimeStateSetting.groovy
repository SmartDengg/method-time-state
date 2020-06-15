package com.smartdengg.timestate.plugin

/**
 * 创建时间:  2019/11/21 15:55 <br>
 * 作者:  SmartDengg <br>
 * 描述:
 */
class TimeStateSetting {

  static final String NAME = 'timeStateSetting'

  /**
   * 是否开启字节码的重写功能
   */
  boolean enable = true

  /**
   * 日志的输出 TAG
   */
  String tag = 'TimeStateLogger'
}

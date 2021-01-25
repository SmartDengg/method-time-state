package com.smartdengg.timestate.plugin

class TimeStateSetting {

  static final String NAME = 'timeStateSetting'

  /**
   * 是否开启 time state 功能
   */
  boolean enable = true

  /**
   * 设置日志的输出 TAG
   */
  String tag = 'TimeStateLogger'

  /**
   * 设置支持 emoji 表示慢函数 :)
   */
  boolean emoji = boolean
}

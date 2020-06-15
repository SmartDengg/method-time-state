| README.md |
|:---|


time-state
====

**在开发环境中，通过声明注解的方式，输出函数耗时的测量工具。**

作为开发者，性能优化是一个永恒的话题，我们应该密切关注程序的性能。函数是我们关注的最小执行单元，因为所有的逻辑都执行在函数中，因此我们经常需要在函数的开始和结尾处添加打印语句，且应该包含以下信息:

- 当前线程信息
- 具体函数的签名信息
- 函数的执行时长
- 快速定位源文件的行号

因此它看起来应该这样:

```java
Thread[main,5,main] ║ com.smartdengg.timestate.sample.MainActivity#onCreate(android.os.Bundle)void ║ (MainActivity.java:18) ====> COST: 229ms
```

**我想每一位开发者都不愿意重复写那些日志语句，因为你要在函数的开始和结尾各写一遍，并且还不能带入线上版本，这就意味着，写完它，再删掉它，重复如此**。我认为这种方式，不仅浪费时间，还容易在项目里面留下隐患，那些你忘记删除的代码就会成为项目"蛀虫"，因此我们需要简单高效并且安全的方式测量函数执行时间。


因此 **time-state** 就诞生了，这个工具使用字节码重写技术，支持仅添加注解 [@TimeState](#jump-time-state) 或 [@FullTimeState](#jump-full-time-state) 在你的函数上，就能够输出格式漂亮的函数耗时等重要信息。


它与 [hugo](https://github.com/JakeWharton/hugo) 的不同之处在于:   **time-state** 不关心函数的参数值和返回值，它只专注函数的耗时，并且支持增量编译和并发的字节码处理技术，丝毫不会影响你的编译速度。

*注意:本仓库只提供了基本的字节码修改功能，是一个的 Android gradle plugin，运行时的函数测量和日志答应功能由 [method-time-state-runtime](https://github.com/SmartDengg/method-time-state-runtime) 提供*


安装&使用
----

**1.** 在工程的根 `build.gradle` 中添加 **time-state** Android gradle plugin 的依赖，在 [CHANGELOG](./CHANGELOG.md) 中记录了所有可用版本信息。

```groovy
buildscript {
  repositories {
    google()
    jcenter()
    maven { url 'https://jitpack.io' }
  }
  dependencies {
    classpath 'com.android.tools.build:gradle:3.5.3'
    classpath 'com.github.SmartDengg.method-time-state:plugin:1.0.0'
  }
}
```

**2.** 在 module 的 `build.gradle` 中应用 time-state AGP 。

```groovy
apply plugin: 'com.android.application' // or com.android.library
apply plugin: 'timestate'
```

通过 gradle console 查看本次编译后将对哪些函数进行耗时测量的日志。

![](art/log_build.png)

time-state 有以下两种可配置方案:

- `enable` : 是否开启字节码的重写功能，默认为 `true`
- `tag` : 日志的输出 TAG，默认为 `TimeStateLogger`

```groovy
timeStateSetting {
  enable = fasle
  tag = 'TAG'
}
```

*注意: 当 `tag` 发生变更后，因为增量编译的关系，需要先手动执行 `./gradlew clean` ，然后再正常编译项目*

<span id="jump-time-state">@TimeState</span>
----

使用 `@TimeState` 打印函数耗时

```java
  @TimeState 
  private void function40Millis() {
    try {
      Thread.sleep(40);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
```

日志的输出格式如下:

![](art/log_time_state.png)

<span id="jump-full-time-state">@FullTimeState</span>
----

如果函数内存在其他函数调用，你可以使用增强的 `@FullTimeState` 打印全量的函数耗时。

```java
  @FullTimeState 
  private void call() {
    try {
      Thread.sleep(10);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    function20Millis();
    function30Millis();
    function40Millis();
  }
```

日志结果如下，由上至下打印了 `call()` 函数:

![](art/log_full_time_state.png)

R8 / ProGuard
----

```
# keep everything in this package from being removed or renamed
-keep class com.smartdengg.timestate.runtime.** { *; }

# keep everything in this package from being renamed only
-keepnames class com.smartdengg.timestate.runtime.** { *; }
```

问题&反馈
----

欢迎在 [issue](https://github.com/SmartDengg/method-time-state/issues) 中提问或者邮件联系我 hi4joker@gmail.com

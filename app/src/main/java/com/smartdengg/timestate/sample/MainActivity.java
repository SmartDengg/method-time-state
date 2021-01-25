package com.smartdengg.timestate.sample;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.smartdengg.timestate.runtime.TimeStatePro;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainActivity extends AppCompatActivity {

  private AtomicBoolean initAOnce = new AtomicBoolean(false);
  private AtomicBoolean initBOnce = new AtomicBoolean(false);

  @TimeStatePro
  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    for (int i = 0, n = 20; i < n; i++) {
      TextView textView = new TextView(this);
    }

    callIn100Millis("1", "2", "3");

    callRecursive();

    new Thread(new Runnable() {
      @TimeStatePro
      @Override public void run() {
        try {
          Thread.sleep(2000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }).start();

    //callThrowException();
  }

  @TimeStatePro
  void callRecursive() {
    initA();
    initB();
  }

  private void initA() {
    if (initAOnce.compareAndSet(false, true)) {
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      callRecursive();
    }
  }

  private void initB() {
    if (initBOnce.compareAndSet(false, true)) {
      try {
        Thread.sleep(500);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  @TimeStatePro
  private void callIn100Millis(String s1, String s2, String s3) {
    function10Millis();
    function20Millis();
    function30Millis();
    function40Millis();
  }

  void function10Millis() {
    try {
      Thread.sleep(10);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  void function20Millis() {
    try {
      Thread.sleep(20);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  void function30Millis() {
    try {
      Thread.sleep(30);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  void function40Millis() {
    try {
      Thread.sleep(40);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  @TimeStatePro
  private void callThrowException() {
    functionThrowException();
  }

  private void functionThrowException() {
    function20Millis();
    throw new AssertionError();
  }
}

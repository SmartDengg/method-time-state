package com.smartdengg.timestate.sample;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.smartdengg.timestate.runtime.TimeState;
import com.smartdengg.timestate.runtime.TimeStatePro;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainActivity extends AppCompatActivity {

  private AtomicBoolean A = new AtomicBoolean(false);
  private AtomicBoolean B = new AtomicBoolean(false);

  static void function20Millis() {
    try {
      Thread.sleep(20);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  @TimeStatePro @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    for (int i = 0, n = 100; i < n; i++) {
      TextView textView = new TextView(this);
    }

    call();

    new Thread(new Runnable() {

      @TimeStatePro @Override public void run() {
        try {
          Thread.sleep(2000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }).start();

    recursive();
  }

  @TimeStatePro void recursive() {

    initA();
    initB();
  }

  private void initA() {

    if (A.compareAndSet(false, true)) {

      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }

      recursive();
    }
  }

  private void initB() {

    if (B.compareAndSet(false, true)) {

      try {
        Thread.sleep(500);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  @TimeStatePro private void call() {
    try {
      Thread.sleep(10);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    function20Millis();
    function30Millis();
    function40Millis();
  }

  void function30Millis() {
    try {
      Thread.sleep(30);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  @TimeState void function40Millis() {
    try {
      Thread.sleep(40);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}

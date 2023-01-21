package com.richardyang.flutter_mp_plugin;

import io.flutter.plugin.common.EventChannel;
import java.util.ArrayList;
import android.os.Handler;
import android.os.Looper;

final class EventRunnable implements Runnable {
  private Object cachedEvent;
  private EventChannel.EventSink cachedDelegate;

  EventRunnable(EventChannel.EventSink delegate, Object event) {
    super();
    cachedDelegate = delegate;
    cachedEvent = event;
  }

  @Override
  public void run() {
    cachedDelegate.success(cachedEvent);
  }
}

/**
 * An implementation of {@link EventChannel.EventSink} which can wrap an underlying sink.
 *
 * <p>It delivers messages immediately when downstream is available, but it queues messages before
 * the delegate event sink is set with setDelegate.
 *
 * <p>This class is not thread-safe. All calls must be done on the same thread or synchronized
 * externally.
 */
final class QueuingEventSink implements EventChannel.EventSink {
  private EventChannel.EventSink delegate;
  private ArrayList<Object> eventQueue = new ArrayList<>();
  private boolean done = false;
  private Handler uiThreadHandler = new Handler(Looper.getMainLooper());

  private Runnable queueRunnable = new Runnable() {
    @Override
    public void run() {
      for (Object event : eventQueue) {
        if (event instanceof EndOfStreamEvent) {
          delegate.endOfStream();
        } else if (event instanceof ErrorEvent) {
          ErrorEvent errorEvent = (ErrorEvent) event;
          delegate.error(errorEvent.code, errorEvent.message, errorEvent.details);
        } else {
          delegate.success(event);
        }
      }
      eventQueue.clear();
    }
  };

  public void setDelegate(EventChannel.EventSink delegate) {
    this.delegate = delegate;
    maybeFlush();
  }

  @Override
  public void endOfStream() {
    enqueue(new EndOfStreamEvent());
    maybeFlush();
    done = true;
  }

  @Override
  public void error(String code, String message, Object details) {
    enqueue(new ErrorEvent(code, message, details));
    maybeFlush();
  }

  @Override
  public void success(Object event) {
    //enqueue(event);
    //maybeFlush();
    uiThreadHandler.post(new EventRunnable(delegate, event));
  }

  private void enqueue(Object event) {
    if (done) {
      return;
    }
    eventQueue.add(event);
  }

  private void maybeFlush() {
    if (delegate == null) {
      return;
    }
    uiThreadHandler.post(queueRunnable);
  }

  private static class EndOfStreamEvent {}

  private static class ErrorEvent {
    String code;
    String message;
    Object details;

    ErrorEvent(String code, String message, Object details) {
      this.code = code;
      this.message = message;
      this.details = details;
    }
  }
}

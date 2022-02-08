package com.ulashchick.podcast.logging;

import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.appender.AppenderLoggingException;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.ulashchick.podcast.testing.TestListener.TEST_ID;

@Plugin(name = "AllureLogAppender", category = "Core", elementType = "appender", printObject = true)
public class AllureLogAppender extends AbstractAppender {

  private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
  private final Lock readLock = rwLock.readLock();

  public static final Map<String, List<String>> LOGS = new ConcurrentHashMap<>();

  protected AllureLogAppender(@Nonnull String name,
                              @Nonnull Filter filter,
                              @Nonnull Layout<? extends Serializable> layout,
                              boolean ignoreExceptions,
                              @Nonnull Property[] properties) {
    super(name, filter, layout, ignoreExceptions, properties);
  }

  @PluginFactory
  public static AllureLogAppender createAppender(
      @PluginAttribute("name") String name,
      @PluginElement("Layout") Layout<? extends Serializable> layout,
      @PluginElement("Filter") final Filter filter,
      @PluginAttribute("otherAttribute") String otherAttribute) {
    if (name == null) {
      LOGGER.error("No name provided for AllureLogAppender");
      return null;
    }
    if (layout == null) {
      layout = PatternLayout.createDefaultLayout();
    }
    return new AllureLogAppender(name, filter, layout, true, new Property[0]);
  }

  @Override
  public void append(LogEvent event) {
    if (!event.getContextData().containsKey(TEST_ID)) {
      return;
    }

    final String testId = event.getContextData().getValue(TEST_ID);

    LOGS.putIfAbsent(testId, new LinkedList<>());
    LOGS.get(testId).add(format(event));
  }

  private String format(LogEvent event) {
    readLock.lock();

    try {
      final byte[] bytes = getLayout().toByteArray(event);
      return new String(bytes, StandardCharsets.UTF_8);
    } catch (Exception ex) {
      if (!ignoreExceptions()) {
        throw new AppenderLoggingException(ex);
      }
    } finally {
      readLock.unlock();
    }

    return event.toString();
  }
}

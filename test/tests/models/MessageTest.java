package tests.models;

import static org.fest.assertions.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.Test;

import models.Message;

/**
 * @author resamsel
 * @version 18 May 2017
 */
public class MessageTest {
  @Test
  public void getCacheKey() {
    UUID messageId = UUID.randomUUID();

    assertThat(Message.getCacheKey(null)).isNull();
    assertThat(Message.getCacheKey(messageId)).isEqualTo("message:" + messageId);
    assertThat(Message.getCacheKey(messageId, "keys")).isEqualTo("message:" + messageId + ":keys");
    assertThat(Message.getCacheKey(messageId, "keys", "locales"))
        .isEqualTo("message:" + messageId + ":keys:locales");
  }
}
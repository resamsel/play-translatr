package dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import controllers.AbstractController;
import controllers.routes;
import models.Key;
import models.Locale;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.Json;
import play.mvc.Call;
import utils.FormatUtils;

import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Message extends Dto {
  private static final long serialVersionUID = -8848810325610871318L;

  private static final Logger LOGGER = LoggerFactory.getLogger(Message.class);

  public UUID id;

  @JsonIgnore
  public DateTime whenCreated;
  @JsonIgnore
  public DateTime whenUpdated;

  public UUID localeId;
  public String localeName;
  public String localeDisplayName;
  public String localePathName;

  public UUID keyId;
  public String keyName;
  public String keyPathName;

  public UUID projectId;
  public String projectName;
  public String projectOwnerUsername;

  public String value;

  public Message() {}

  private Message(models.Message in) {
    this.id = in.id;
    this.whenCreated = in.whenCreated;
    this.whenUpdated = in.whenUpdated;
    this.value = in.value;

    if (in.locale != null) {
      this.localeId = in.locale.id;
      this.localeName = in.locale.name;
      this.localeDisplayName = FormatUtils.formatDisplayName(in.locale);
      this.localePathName = in.locale.getPathName();
    }

    if (in.key != null) {
      this.keyId = in.key.id;
      this.keyName = in.key.name;
      this.keyPathName = in.key.getPathName();
      if (in.key.project != null) {
        this.projectId = in.key.project.id;
        this.projectName = in.key.project.name;
        this.projectOwnerUsername = in.key.project.owner.username;
      }
    }
  }

  public models.Message toModel(Locale locale, Key key) {
    models.Message out = new models.Message();

    out.id = id;
    out.whenCreated = whenCreated;
    out.whenUpdated = whenUpdated;
    out.locale = locale;
    out.key = key;
    out.value = value;

    LOGGER.trace("DTO Message toModel: {}", Json.toJson(out));

    return out;
  }

  public Call route() {
    if (projectOwnerUsername == null || projectName == null || keyPathName == null)
      return null;

    return routes.Keys.keyBy(projectOwnerUsername, projectName, keyPathName,
        AbstractController.DEFAULT_SEARCH, AbstractController.DEFAULT_ORDER,
        AbstractController.DEFAULT_LIMIT, AbstractController.DEFAULT_OFFSET);
  }

  public static Message validate(Message message) {
    return message;
  }

  public static Message from(models.Message message) {
    return validate(new Message(message));
  }
}

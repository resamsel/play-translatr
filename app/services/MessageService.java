package services;

import com.google.inject.ImplementedBy;
import criterias.MessageCriteria;
import models.Message;
import models.Project;
import services.impl.MessageServiceImpl;

import java.util.List;
import java.util.UUID;

/**
 *
 * @author resamsel
 * @version 29 Aug 2016
 */
@ImplementedBy(MessageServiceImpl.class)
public interface MessageService extends ModelService<Message, UUID, MessageCriteria> {
  /**
   * @param project
   * @return
   */
  int countBy(Project project);

  /**
   * @param projectId
   */
  void resetWordCount(UUID projectId);
}

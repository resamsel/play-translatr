package services;

import com.google.inject.ImplementedBy;
import criterias.LocaleCriteria;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import models.Locale;
import models.Project;
import services.impl.LocaleServiceImpl;

/**
 * @author resamsel
 * @version 29 Aug 2016
 */
@ImplementedBy(LocaleServiceImpl.class)
public interface LocaleService extends ModelService<Locale, UUID, LocaleCriteria> {

  List<Locale> latest(Project project, int limit);

  Locale byProjectAndName(Project project, String name);

  Map<UUID, Double> progress(List<UUID> localeIds, long keysSize);

  void increaseWordCountBy(UUID localeId, int wordCountDiff);

  void resetWordCount(UUID projectId);
}

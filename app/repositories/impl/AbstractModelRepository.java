package repositories.impl;

import actors.ActivityActorRef;
import criterias.AbstractSearchCriteria;
import models.Model;
import org.postgresql.util.PSQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repositories.ModelRepository;
import repositories.Persistence;

import javax.persistence.PersistenceException;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import javax.validation.Validator;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author resamsel
 * @version 9 Sep 2016
 */
public abstract class AbstractModelRepository<MODEL extends Model<MODEL, ID>, ID, CRITERIA extends AbstractSearchCriteria<CRITERIA>>
    implements ModelRepository<MODEL, ID, CRITERIA> {

  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractModelRepository.class);

  protected static final String FETCH_COUNT = "count";

  final ActivityActorRef activityActor;
  protected final Validator validator;
  private final Persistence persistence;

  AbstractModelRepository(Persistence persistence, Validator validator, ActivityActorRef activityActor) {
    this.persistence = persistence;
    this.validator = validator;
    this.activityActor = activityActor;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public MODEL create(MODEL model) {
    LOGGER.debug("create(model={})", model);

    try {
      return save(model);
    } catch (PersistenceException e) {
      if (e.getCause() != null && e.getCause() instanceof PSQLException
          && "23505".equals(((PSQLException) e.getCause()).getSQLState())) {
        throw new ValidationException("Entry already exists (duplicate key)");
      }

      throw new ValidationException(e.getMessage());
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public MODEL update(MODEL model) {
    if (model.getId() == null) {
      throw new ValidationException("Field 'id' required");
    }

    MODEL m = byId(model.getId());

    if (m == null) {
      throw new ValidationException(String.format("Entity with ID '%s' not found", model.getId()));
    }

    return save(m.updateFrom(model));
  }

  protected MODEL validate(MODEL model) {
    Set<ConstraintViolation<MODEL>> violations = validator.validate(model);

    if (!violations.isEmpty()) {
      throw new ConstraintViolationException(
          "Constraint violations detected: " + violations.stream().map(Object::toString).collect(
              Collectors.joining(",")), violations);
    }

    return model;
  }

  @Override
  public MODEL save(MODEL t) {
    boolean update = !persistence.isNew(t);

    preSave(t, update);

    validate(t);

    prePersist(t, update);

    persist(t);

    postSave(t, update);

    return t;
  }

  @Override
  public MODEL persist(MODEL t) {
    persistence.save(t);
    return t;
  }

  protected void preSave(MODEL t, boolean update) {
  }

  protected void prePersist(MODEL t, boolean update) {
  }

  protected void postSave(MODEL t, boolean update) {
  }

  @Override
  public Collection<MODEL> save(Collection<MODEL> t) {
    try {
      preSave(t);
      persist(t);
      postSave(t);
    } catch (Exception e) {
      LOGGER.error("Error while batch saving entities", e);
      throw new PersistenceException(e);
    }

    return t;
  }

  protected Collection<MODEL> persist(Collection<MODEL> t) throws Exception {
    persistence.batchExecute((tx) -> persistence.saveAll(t));

    return t;
  }

  protected void preSave(Collection<MODEL> t) {
  }

  protected void postSave(Collection<MODEL> t) {
  }

  @Override
  public void delete(MODEL t) {
    preDelete(t);
    persistence.delete(t);
    postDelete(t);
  }

  protected void preDelete(MODEL t) {
  }

  protected void postDelete(MODEL t) {
  }

  @Override
  public void delete(Collection<MODEL> t) {
    try {
      preDelete(t);
      persistence.batchExecute((tx) -> persistence.deleteAll(t));
      postDelete(t);
    } catch (Exception e) {
      LOGGER.error("Error while batch deleting entities", e);
      throw new PersistenceException(e);
    }
  }

  protected void preDelete(Collection<MODEL> t) {
  }

  protected void postDelete(Collection<MODEL> t) {
  }
}

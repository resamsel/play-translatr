package dto.errors;

import dto.NotFoundException;

/**
 * @author resamsel
 * @version 18 Jan 2017
 */
public class NotFoundError extends Error {
  private static final long serialVersionUID = -2176399037694866475L;
  public NotFoundErrorInfo error;

  /**
   * 
   */
  public NotFoundError(NotFoundException e) {
    this.error = new NotFoundErrorInfo(e);
  }
}

import { HttpErrorResponse } from '@angular/common/http';
import {
  ConstraintViolation,
  ConstraintViolationErrorInfo,
  ErrorInfos,
  NotFoundErrorInfo,
  PermissionErrorInfo
} from '@dev/translatr-model';
import * as runes from 'runes';

export const firstChar = (name: string): string => {
  if (name === undefined || name === null || name.length === 0) {
    return name;
  }
  return runes.substr(name, 0, 1);
};

export const extractUniqueness = (violation: ConstraintViolation): string | undefined => {
  switch (violation.message) {
    case 'error.nameunique':
    case 'error.usernameunique':
    case 'error.projectnameunique':
    case 'error.projectuserunique':
    case 'error.accesstokennameunique':
      if (!Boolean(violation.invalidValue)) {
        return undefined;
      }
      const key = Object.keys(violation.invalidValue)[0];
      return `${key}.${violation.field}: ${violation.invalidValue[key][violation.field]}`;
    default:
      return undefined;
  }
};

export const errorMessage = (error: HttpErrorResponse | ErrorInfos | Error): string => {
  if (error instanceof HttpErrorResponse) {
    if (!!error.error && !!error.error.error) {
      return errorMessage(error.error.error);
    }

    return error.message;
  }

  if (error instanceof Error) {
    return `${error} (${error.stack})`;
  }

  switch (error.type) {
    case 'ConstraintViolationException':
      return (error as ConstraintViolationErrorInfo).violations
        .map((v: ConstraintViolation) => `${v.field}: ${v.message} (${extractUniqueness(v)})`)
        .join(', ');
    case 'PermissionError':
      return `${error.type}: Scopes needed: ${(error as PermissionErrorInfo).scopes}`;
    case 'NotFoundError':
      return `${error.type}: ${(error as NotFoundErrorInfo).entity} with ID '${
        (error as NotFoundErrorInfo).id
      }' was not found`;
    default:
      return `${error.type}: ${error.message} (${JSON.stringify(error)})`;
  }
};

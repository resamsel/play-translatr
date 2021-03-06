import { RequestCriteria } from './request-criteria';

export interface LocaleCriteria extends RequestCriteria {
  projectId?: string;
  keyId?: string;
  missing?: boolean;
}

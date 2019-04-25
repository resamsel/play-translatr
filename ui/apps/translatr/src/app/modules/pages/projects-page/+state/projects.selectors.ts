import { createFeatureSelector, createSelector } from '@ngrx/store';
import { PROJECTS_FEATURE_KEY, ProjectsState } from './projects.reducer';

// Lookup the 'Projects' feature state managed by NgRx
const getProjectsState = createFeatureSelector<ProjectsState>(
  PROJECTS_FEATURE_KEY
);

const getLoading = createSelector(
  getProjectsState,
  (state: ProjectsState) => state.loading
);
const getError = createSelector(
  getProjectsState,
  (state: ProjectsState) => state.error
);

const getAllProjects = createSelector(
  getProjectsState,
  (state: ProjectsState) => state.pagedList
);

export const projectsQuery = {
  getLoading,
  getError,
  getAllProjects,
};
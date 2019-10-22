import { ProjectMembersPage } from '../../../support/project/project-members-page.po';

describe('Translatr Project Members', () => {
  let page: ProjectMembersPage;

  beforeEach(() => {
    page = new ProjectMembersPage('johndoe', 'p1');

    cy.clearCookies();
    cy.server();

    cy.route('/api/me', 'fixture:me');
    cy.route('/api/johndoe/p1', 'fixture:project/johndoe-p1');
    cy.route('/api/project/*/locales*', 'fixture:project/johndoe-p1-locales');
    cy.route('/api/project/*/keys*', 'fixture:project/johndoe-p1-keys');
    cy.route('/api/project/*/messages*', 'fixture:project/johndoe-p1-messages');
    cy.route('/api/activities/aggregated*',
      'fixture:project/johndoe-p1-activities-aggregated');
  });

  it('should have page name Project Keys', () => {
    // given

    // when
    page.navigateTo();

    // then
    page.getPageName()
      .should('have.text', 'p1');
  });
});

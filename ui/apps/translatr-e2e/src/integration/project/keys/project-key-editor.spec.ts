import { KeyEditorPage } from '../../../support/project/key-editor-page.po';

describe('Project Key Editor', () => {
  let page: KeyEditorPage;

  beforeEach(() => {
    page = new KeyEditorPage('johndoe', 'p1', 'k1');

    cy.clearCookies();
    cy.server();

    cy.route('/api/me?fetch=features', 'fixture:me');
    cy.route('/api/johndoe/p1*', 'fixture:johndoe/p1');
    cy.route('/api/johndoe/p1/keys/k1', 'fixture:johndoe/p1/keys/k1');
    cy.route('/api/project/*/locales*', 'fixture:johndoe/p1/locales');
    cy.route('/api/project/*/locales?*missing=true*', 'fixture:johndoe/p1/locales-missing');
    cy.route('/api/project/*/messages*', 'fixture:johndoe/p1/messages');
    cy.route('/api/project/*/members*', 'fixture:johndoe/p1/members');
    cy.route('/api/project/*/activities*', 'fixture:johndoe/p1/activities');
  });

  it('should have page title Key Editor', () => {
    // given

    // when
    page.navigateTo();

    // then
    page.getPageTitle().should('have.text', 'Key Editor');
  });

  it('should have key k1 selected in sidebar', () => {
    // given

    // when
    page.navigateTo();

    // then
    page.getSelectedKeyField().should('have.value', 'k1');
  });

  it('should have two locales in sidebar', () => {
    // given

    // when
    page.navigateTo();

    // then
    page
      .getNavList()
      .find('.mat-list-item')
      .should('have.length', 2);
  });

  it('should have no locale selected in sidebar', () => {
    // given

    // when
    page.navigateTo();

    // then
    page
      .getNavList()
      .find('.mat-list-item.active')
      .should('have.length', 0);
  });

  it('should have locale selected when activated in sidebar', () => {
    // given

    // when
    page.navigateTo();

    // then
    page
      .getNavList()
      .find('.mat-list-item.locale:first-of-type')
      .should('not.have.class', 'active');
    page
      .getNavList()
      .find('.mat-list-item.locale')
      .first()
      .click()
      .should('have.class', 'active');
  });

  it('should show editor when locale activated in sidebar', () => {
    // given

    // when
    page.navigateTo();

    // then
    page
      .getNavList()
      .find('.mat-list-item.locale')
      .first()
      .click();

    page.getEditor().should('be.visible');
  });

  it('should show translation when locale activated in sidebar', () => {
    // given

    // when
    page.navigateTo();
    page
      .getNavList()
      .find('.mat-list-item.locale')
      .first()
      .click();

    // then
    page.getEditorContents().should('have.text', 'Schlüssel 1');
  });

  it('should show meta when locale activated in sidebar', () => {
    // given

    // when
    page.navigateTo();

    // then
    page
      .getNavList()
      .find('.mat-list-item.locale')
      .first()
      .click();

    page.getMeta().should('be.visible');
  });

  it('should show preview when locale activated in sidebar', () => {
    // given

    // when
    page.navigateTo();

    // then
    page
      .getNavList()
      .find('.mat-list-item.locale')
      .first()
      .click();

    page.getPreviewContents().should('have.text', 'Schlüssel 1');
  });

  it('should show existing translations when translations tab selected', () => {
    // given

    // when
    page.navigateTo();

    // then
    page
      .getNavList()
      .find('.mat-list-item.locale')
      .first()
      .click();

    page
      .getMeta()
      .find('#mat-tab-label-0-1')
      .click();
    page
      .getMeta()
      .find('#mat-tab-content-0-1 .mat-card')
      .should('have.length', 2);
  });

  it('should use translation when use translation is clicked', () => {
    // given

    // when
    page.navigateTo();

    // then
    page
      .getNavList()
      .find('.mat-list-item.locale')
      .first()
      .click();

    page.getMeta().within(el => {
      el.find('#mat-tab-label-0-1').trigger('click');
      el.find('#mat-tab-content-0-1 .mat-card button.use-value')
        .last()
        .trigger('click');
    });

    page.getEditorContents().should('have.text', 'Key One');
  });

  it('should only show locales with missing translations when filtered by those', () => {
    // given
    cy.route('/api/project/*/messages?*localeIds=*', 'fixture:johndoe/p1/messages-missing');

    // when
    page.navigateTo();

    page.getFilterField().focus();
    cy.get('.autocomplete-option')
      .first()
      .trigger('click');

    // then
    cy.get('.selected-option').should('have.length', 1);
    page
      .getNavList()
      .find('.mat-list-item.locale')
      .should('have.length', 1);
  });

  it('should display "Save" button when user settings say so', () => {
    // given, when
    page.navigateTo();
    page
      .getNavList()
      .find('.mat-list-item.locale')
      .first()
      .click();

    // then
    cy.get('.save-button').should('have.text', 'Save');
  });

  it('should display "Save and next" button when user settings say so', () => {
    // given
    cy.route('/api/me?fetch=features', 'fixture:me-save-behavior-saveandnext');

    // when
    page.navigateTo();
    page
      .getNavList()
      .find('.mat-list-item.locale')
      .first()
      .click();

    // then
    cy.get('.save-button').should('have.text', 'Save and next');
  });

  it('should call updateSettings on "Save and next"', () => {
    // given
    cy.route('PUT', '/api/message', 'fixture:johndoe/p1/message');
    cy.route('PATCH', '/api/user/*/settings', 'fixture:me-save-behavior-saveandnext').as(
      'updateSettings'
    );

    // when
    page.navigateTo();
    page
      .getNavList()
      .find('.mat-list-item.locale')
      .first()
      .click();
    cy.get('.menu-button').click();
    cy.get('.save-behavior-saveandnext').click();

    // then
    cy.wait('@updateSettings').then(xhr => {
      expect(xhr.request.body).to.deep.equal({ 'save-behavior': 'saveandnext' });
    });
    page
      .getNavList()
      .find('.mat-list-item.locale')
      .eq(1)
      .should('have.class', 'active');
  });
});

require('@momothepug/tsmodule-alias').play('./');

import Command, { flags } from '@oclif/command';
import { LoadGenerator } from '@translatr/generator';

export class LoadGenerateCommand extends Command {
  static description = 'Generate data (users, projects, locales, keys) by using the API';

  static flags = {
    endpoint: flags.string({
      description: 'The endpoint of the REST API to communicate with.',
      char: 'e',
      env: 'ENDPOINT',
      default: 'http://localhost:9000'
    }),
    'access-token': flags.string({
      description: 'The access token to use when using the REST API.',
      char: 'a',
      env: 'ACCESS_TOKEN',
      default: '-'
    }),

    users: flags.integer({
      description: 'The number of users per minute.',
      char: 'u',
      env: 'USERS',
      default: 60
    }),

    personas: flags.string({
      description: 'The personas to include (default is all personas).',
      char: 'p',
      env: 'PERSONAS',
      default: ''
    })
  };

  async run() {
    const command = this.parse(LoadGenerateCommand);

    await new LoadGenerator({
      baseUrl: command.flags.endpoint,
      accessToken: command.flags['access-token'],
      usersPerMinute: command.flags.users,
      includePersonas: command.flags.personas !== '' ? command.flags.personas.split(',') : []
    }).execute();
  }
}

import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from './app/app.config';
import { App } from './app/app';
import localePt from '@angular/common/locales/pt';
import { registerLocaleData } from '@angular/common';

bootstrapApplication(App, appConfig)
  .catch((err) => console.error(err));
  registerLocaleData(localePt, 'pt-BR');
  

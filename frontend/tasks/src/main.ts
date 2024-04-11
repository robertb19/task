import { bootstrapApplication } from '@angular/platform-browser';
import { AppComponent } from './app/app.component';
import {provideHttpClient} from "@angular/common/http";
import {provideToastr} from "ngx-toastr";
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {importProvidersFrom} from "@angular/core";
import {MatToolbarModule} from "@angular/material/toolbar";

bootstrapApplication(AppComponent, {
  providers: [provideHttpClient(), provideToastr(), provideAnimationsAsync(),
    importProvidersFrom([BrowserAnimationsModule, MatToolbarModule])]
}).catch((err) => console.error(err));

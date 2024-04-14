import { bootstrapApplication } from '@angular/platform-browser';
import { AppComponent } from './app/app.component';
import {provideHttpClient} from "@angular/common/http";
import {provideToastr} from "ngx-toastr";
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {importProvidersFrom} from "@angular/core";
import {MatToolbarModule} from "@angular/material/toolbar";
import {provideRouter, Routes} from "@angular/router";
import {ViewTaskCategoriesComponent} from "./app/components/view-task-categories/view-task-categories.component";
import {ViewTasksComponent} from "./app/components/view-tasks/view-tasks.component";
import {registerLocaleData} from "@angular/common";
import {enGbLocale} from "ngx-bootstrap/chronos";
import {provideNativeDateAdapter} from "@angular/material/core";
import {NgxMaterialTimepickerComponent, NgxMaterialTimepickerModule} from "ngx-material-timepicker";

const routes: Routes = [
  { path: '', component: ViewTaskCategoriesComponent },
  { path: 'categories', component: ViewTaskCategoriesComponent },
  { path: 'tasks', component: ViewTasksComponent }
];

registerLocaleData(enGbLocale, 'en-GB')

bootstrapApplication(AppComponent, {
  providers: [provideHttpClient(), provideToastr(), provideAnimationsAsync(), provideRouter(routes), provideNativeDateAdapter(),
    importProvidersFrom([BrowserAnimationsModule, MatToolbarModule, NgxMaterialTimepickerModule, NgxMaterialTimepickerComponent])]
}).catch((err) => console.error(err));

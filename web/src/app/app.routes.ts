import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: 'catalog',
    loadChildren: () =>
      import('./features/catalog/catalog.routes').then((m) => m.CATALOG_ROUTES),
  },
  { path: '', redirectTo: 'catalog', pathMatch: 'full' },
];

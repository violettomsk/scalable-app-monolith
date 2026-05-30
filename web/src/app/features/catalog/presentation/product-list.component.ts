import { Component, inject, signal } from '@angular/core';
import { ProductRepository } from '../data/product.repository';

@Component({
  selector: 'app-product-list',
  standalone: true,
  template: `<div class="p-6"><h1 class="text-xl font-medium">Catalog</h1></div>`,
})
export class ProductListComponent {
  private repo = inject(ProductRepository);
  readonly loading = signal(false);
  // presentation only — no HTTP here; calls repo for data.
}

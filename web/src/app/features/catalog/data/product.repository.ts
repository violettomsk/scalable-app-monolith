import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Product } from '../domain/product.model';

// Data layer: the only place that knows about HTTP. UI talks to this, not to fetch.
@Injectable({ providedIn: 'root' })
export class ProductRepository {
  private http = inject(HttpClient);

  create(name: string, priceCents: number) {
    return this.http.post<Product>('/api/v1/products', { name, priceCents });
  }
}

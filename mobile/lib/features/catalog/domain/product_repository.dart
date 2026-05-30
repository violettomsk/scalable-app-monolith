import 'product.dart';

// Repository interface lives in domain; the data layer implements it.
abstract interface class ProductRepository {
  Future<Product> create(String name, int priceCents);
}

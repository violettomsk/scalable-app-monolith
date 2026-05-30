import '../domain/product.dart';
import '../domain/product_repository.dart';
import 'product_api.dart';

class ProductRepositoryImpl implements ProductRepository {
  final ProductApi _api;
  ProductRepositoryImpl(this._api);

  @override
  Future<Product> create(String name, int priceCents) async {
    final json = await _api.create(name, priceCents);
    return Product(
      id: json['id'] as String,
      name: json['name'] as String,
      priceCents: json['priceCents'] as int,
    );
  }
}

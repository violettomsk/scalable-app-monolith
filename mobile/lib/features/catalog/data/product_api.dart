import 'package:dio/dio.dart';

// The only place that knows about HTTP transport.
class ProductApi {
  final Dio _dio;
  ProductApi(this._dio);

  Future<Map<String, dynamic>> create(String name, int priceCents) async {
    final res = await _dio.post('/api/v1/products',
        data: {'name': name, 'priceCents': priceCents});
    return res.data as Map<String, dynamic>;
  }
}

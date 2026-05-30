// Pure domain entity — no Flutter, no Dio, no JSON.
class Product {
  final String id;
  final String name;
  final int priceCents;
  Product({required this.id, required this.name, required this.priceCents})
      : assert(priceCents >= 0);
}

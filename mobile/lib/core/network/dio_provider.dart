import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

final dioProvider = Provider<Dio>(
  (ref) => Dio(BaseOptions(
    baseUrl: const String.fromEnvironment('API_BASE_URL',
        defaultValue: 'http://localhost:8080'),
  )),
);

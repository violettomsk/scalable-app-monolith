import { HttpInterceptorFn } from '@angular/common/http';

// Always send the session cookie to the BFF; never attach bearer tokens client-side.
export const credentialsInterceptor: HttpInterceptorFn = (req, next) =>
  next(req.clone({ withCredentials: true }));

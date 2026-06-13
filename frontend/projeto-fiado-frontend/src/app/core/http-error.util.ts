import { HttpErrorResponse } from '@angular/common/http';

export function extrairMensagemErro(error: HttpErrorResponse, fallback: string): string {
  if (error.error && typeof error.error === 'object' && error.error.message) {
    return error.error.message as string;
  }
  return fallback;
}

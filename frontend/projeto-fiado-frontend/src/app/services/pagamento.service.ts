import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../core/api.config';

export interface PagamentoDTO {
  id?: number;
  vendaId: number;
  valor: number;
  dataPagamento?: string;
  formaPagamento: string;
  observacao?: string;
}

@Injectable({ providedIn: 'root' })
export class PagamentoService {
  private apiUrl = `${API_BASE_URL}/pagamentos`;

  constructor(private http: HttpClient) {}

  registrar(pagamento: PagamentoDTO): Observable<PagamentoDTO> {
    return this.http.post<PagamentoDTO>(this.apiUrl, pagamento);
  }

  listarPorVenda(vendaId: number): Observable<PagamentoDTO[]> {
    return this.http.get<PagamentoDTO[]>(`${this.apiUrl}/venda/${vendaId}`);
  }
}

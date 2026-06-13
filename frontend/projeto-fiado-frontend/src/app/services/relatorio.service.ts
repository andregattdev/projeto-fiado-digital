import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../core/api.config';

export interface ClienteDividaRelatorioDTO {
  clienteId: number;
  nome: string;
  cpf?: string;
  telefone?: string;
  totalDevido: number;
  vendaMaisAntiga: string;
  diasEmAtraso: number;
}

export interface RelatorioMensalDTO {
  mes: number;
  ano: number;
  totalVendasNoMes: number;
  totalRecebidoNoMes: number;
  saldoDevedorTotal: number;
  quantidadeDevedores: number;
  devedores: ClienteDividaRelatorioDTO[];
}

export interface DividaVencidaDTO {
  vendaId: number;
  clienteId: number;
  nomeCliente: string;
  cpf?: string;
  telefone?: string;
  dataVenda: string;
  diasEmAtraso: number;
  saldoDevedor: number;
}

@Injectable({ providedIn: 'root' })
export class RelatorioService {
  private apiUrl = `${API_BASE_URL}/relatorios`;

  constructor(private http: HttpClient) {}

  relatorioMensal(mes: number, ano: number): Observable<RelatorioMensalDTO> {
    const params = new HttpParams().set('mes', mes).set('ano', ano);
    return this.http.get<RelatorioMensalDTO>(`${this.apiUrl}/mensal`, { params });
  }

  dividasVencidas(dias?: number): Observable<DividaVencidaDTO[]> {
    let params = new HttpParams();
    if (dias != null) {
      params = params.set('dias', dias);
    }
    return this.http.get<DividaVencidaDTO[]>(`${this.apiUrl}/dividas-vencidas`, { params });
  }
}

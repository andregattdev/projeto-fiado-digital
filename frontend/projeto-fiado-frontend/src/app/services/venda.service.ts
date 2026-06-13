import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../core/api.config';

export interface VendaItemDTO {
  id?: number;
  produto: string;
  quantidade: number;
  precoUnitario: number;
  subtotal?: number;
}

export interface VendaRequestDTO {
  clienteId: number;
  itens: VendaItemDTO[];
  observacao?: string;
}

export interface VendaResponseDTO {
  id: number;
  clienteId: number;
  nomeCliente: string;
  cpfCliente?: string;
  dataVenda: string;
  valorTotal: number;
  observacao?: string;
  ativa: boolean;
  itens: VendaItemDTO[];
  valorPago: number;
  saldoDevedor: number;
}

export interface VendasPeriodoDTO {
  dataInicio: string;
  dataFim: string;
  quantidadeVendas: number;
  totalVendas: number;
  totalRecebido: number;
  vendas: VendaResponseDTO[];
}

@Injectable({ providedIn: 'root' })
export class VendaService {
  private apiUrl = `${API_BASE_URL}/vendas`;

  constructor(private http: HttpClient) {}

  criarVenda(request: VendaRequestDTO): Observable<VendaResponseDTO> {
    return this.http.post<VendaResponseDTO>(this.apiUrl, request);
  }

  listarPorCliente(clienteId: number): Observable<VendaResponseDTO[]> {
    return this.http.get<VendaResponseDTO[]>(`${this.apiUrl}/cliente/${clienteId}`);
  }

  listarEmAberto(): Observable<VendaResponseDTO[]> {
    return this.http.get<VendaResponseDTO[]>(`${this.apiUrl}/em-aberto`);
  }

  cancelarVenda(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}/cancelar`);
  }

  listarPorPeriodo(inicio: string, fim: string): Observable<VendasPeriodoDTO> {
    const params = new HttpParams().set('inicio', inicio).set('fim', fim);
    return this.http.get<VendasPeriodoDTO>(`${this.apiUrl}/periodo`, { params });
  }
}

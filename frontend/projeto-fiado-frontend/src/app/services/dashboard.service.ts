import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../core/api.config';
import { ClienteResumoDTO } from './cliente.service';
import { DividaVencidaDTO } from './relatorio.service';

export interface VendaResumoDTO {
  id: number;
  nomeCliente: string;
  dataVenda: string;
  valorTotal: number;
  saldoDevedor: number;
  ativa: boolean;
}

// Nova interface para mapear a evolução de cada mês vinda do Back-end
export interface EvolucaoMensalDTO {
  mes: string;          // Ex: "Jan", "Fev", "Mar"
  totalVendido: number;  // Barras Azuis (Faturamento)
  totalRecebido: number; // Barras Verdes (Caixa real)
}

export interface DashboardDTO {
  totalAReceber: number;
  totalRecebidoHoje: number;
  quantidadeVendasEmAberto: number;
  quantidadeClientesDevedores: number;
  maioresDevedores: ClienteResumoDTO[];
  ultimasVendas: VendaResumoDTO[];
  quantidadeDividasVencidas: number;
  totalDividasVencidas: number;
  dividasVencidas: DividaVencidaDTO[];
  
  // Integração do Gráfico: Adicionada a lista de evolução dos últimos meses
  evolucaoMensal: EvolucaoMensalDTO[];
}

@Injectable({ providedIn: 'root' })
export class DashboardService {
  private apiUrl = `${API_BASE_URL}/dashboard`;

  constructor(private http: HttpClient) { }

  getDashboard(): Observable<DashboardDTO> {
    return this.http.get<DashboardDTO>(this.apiUrl);
  }
}
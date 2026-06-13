import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../core/api.config';

export interface ClienteDTO {
  id?: number;
  nome: string;
  cpf: string;
  telefone?: string;
  endereco?: string;
  dataNascimento?: string;
  totalDevido?: number;
}

export interface ClienteResumoDTO {
  clienteId: number;
  nome: string;
  cpf: string;
  totalDevido: number;
  telefone?: string;
}

@Injectable({ providedIn: 'root' })
export class ClienteService {
  private apiUrl = `${API_BASE_URL}/clientes`;

  constructor(private http: HttpClient) {}

  listarTodos(): Observable<ClienteDTO[]> {
    return this.http.get<ClienteDTO[]>(this.apiUrl);
  }

  buscarPorId(id: number): Observable<ClienteDTO> {
    return this.http.get<ClienteDTO>(`${this.apiUrl}/${id}`);
  }

  buscarPorNome(nome: string): Observable<ClienteDTO[]> {
    const params = new HttpParams().set('nome', nome);
    return this.http.get<ClienteDTO[]>(`${this.apiUrl}/buscar`, { params });
  }

  listarDevedores(): Observable<ClienteResumoDTO[]> {
    return this.http.get<ClienteResumoDTO[]>(`${this.apiUrl}/devedores`);
  }

  cadastrar(cliente: ClienteDTO): Observable<ClienteDTO> {
    return this.http.post<ClienteDTO>(this.apiUrl, cliente);
  }

  atualizar(id: number, cliente: ClienteDTO): Observable<ClienteDTO> {
    return this.http.put<ClienteDTO>(`${this.apiUrl}/${id}`, cliente);
  }

  deletar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}

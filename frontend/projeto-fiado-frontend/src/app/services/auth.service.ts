import { Injectable, signal } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { API_BASE_URL } from '../core/api.config';

export interface ComercioAuth {
  id: number;
  nomeLoja: string;
  nomeResponsavel: string;
  email: string;
  telefone?: string;
  cnpj?: string;
}

export interface AuthResponse {
  token: string;
  tipo: string;
  expiraEm: number;
  comercio: ComercioAuth;
}

export interface LoginRequest {
  email: string;
  senha: string;
}

export interface RegistroComercioRequest {
  nomeLoja: string;
  nomeResponsavel: string;
  email: string;
  senha: string;
  telefone?: string;
  cnpj?: string;
}

const TOKEN_KEY = 'fiado_auth_token';
const COMERCIO_KEY = 'fiado_auth_comercio';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private apiUrl = `${API_BASE_URL}/auth`;
  readonly comercioLogado = signal<ComercioAuth | null>(this.carregarComercioSalvo());

  constructor(private http: HttpClient) { }

  registroDisponivel(): Observable<{ disponivel: boolean }> {
    return this.http.get<{ disponivel: boolean }>(`${this.apiUrl}/registro-disponivel`);
  }

  login(dados: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, dados).pipe(
      tap(res => this.salvarSessao(res))
    );
  }

  registrar(dados: RegistroComercioRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/registrar`, dados).pipe(
      tap(res => this.salvarSessao(res))
    );
  }

  atualizarPerfil(dados: { nomeLoja: string; nomeResponsavel: string; telefone?: string; cnpj?: string; senhaAtual?: string; novaSenha?: string }): Observable<ComercioAuth> {
  // 1. Recupera o token usando a constante correta do seu arquivo
  const token = localStorage.getItem(TOKEN_KEY); 

  // 2. Monta o cabeçalho de autenticação explicitamente para o PUT
  const headers = new HttpHeaders({
    'Authorization': `Bearer ${token}`
  });

  // 3. Passa os headers no objeto de opções do http.put
  return this.http.put<ComercioAuth>(`${this.apiUrl}/perfil`, dados, { headers }).pipe(
    tap(comercio => {
      localStorage.setItem(COMERCIO_KEY, JSON.stringify(comercio));
      this.comercioLogado.set(comercio);
    })
  );
}

  logout(): void {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(COMERCIO_KEY);
    this.comercioLogado.set(null);
  }

  getToken(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  private salvarSessao(res: AuthResponse): void {
    localStorage.setItem(TOKEN_KEY, res.token);
    localStorage.setItem(COMERCIO_KEY, JSON.stringify(res.comercio));
    this.comercioLogado.set(res.comercio);
  }

  private carregarComercioSalvo(): ComercioAuth | null {
    const raw = localStorage.getItem(COMERCIO_KEY);
    if (!raw) return null;
    try {
      return JSON.parse(raw) as ComercioAuth;
    } catch {
      return null;
    }
  }
}

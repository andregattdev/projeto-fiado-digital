import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { AuthService } from '../services/auth.service';
import { firstValueFrom } from 'rxjs';
import { CnpjPipe } from '../shared/pipes/cnpj-pipe';

@Component({
  selector: 'app-main-layout',
  standalone: true,
  imports: [
    CommonModule,
    RouterOutlet,
    RouterLink,
    RouterLinkActive,
    MatIconModule,
    MatButtonModule,
    CnpjPipe
  ],
  templateUrl: './main-layout.component.html',
  styleUrl: './main-layout.component.css'
})
export class MainLayoutComponent {
  // Injeção do AuthService (já está correto)
  readonly auth = inject(AuthService);

  /**
   * Atualiza dados do perfil do comércio
   * Melhorias: tratamento de erro mais completo, tipagem e feedback
   */
  async atualizarPerfil(): Promise<void> {
    try {
      // Dados do exemplo — na prática, viriam de um formulário
      const dadosAtualizacao = {
        nomeLoja: 'Minha Loja',
        nomeResponsavel: 'André',
        telefone: '43 99999-9999'
      };

      const comercioAtualizado = await firstValueFrom(
        this.auth.atualizarPerfil(dadosAtualizacao)
      );

      console.log('✅ Perfil atualizado com sucesso:', comercioAtualizado);
      // Você pode substituir o reload por navegação normal:
      // this.router.navigate(['/perfil']);
      window.location.href = '/perfil';

    } catch (err) {
      console.error('❌ Erro ao atualizar perfil:', err);
      // Aqui no futuro pode adicionar um toast/mensagem para o usuário
    }
  }

  /**
   * Realiza logout e redireciona para login
   * Melhorias: limpeza total de dados, tratamento de erro
   */
  sair(): void {
    try {
      // Chama o logout do serviço (que já deve limpar token e dados)
      this.auth.logout();

      // Garante limpeza de qualquer dado residual
      localStorage.clear();
      sessionStorage.clear();

      // Redireciona para tela de login
      window.location.href = '/login';

    } catch (erro) {
      console.error('❌ Erro ao sair:', erro);
      // Mesmo com erro, força o redirecionamento
      window.location.href = '/login';
    }
  }

  // ✅ MÉTODOS ADICIONAIS PARA USAR NO SEU HTML (mais seguro)
  get nomeLoja(): string {
    return this.auth.comercioLogado()?.nomeLoja ?? 'Fiado Digital';
  }

  get nomeResponsavel(): string {
    return this.auth.comercioLogado()?.nomeResponsavel ?? 'Usuário';
  }

  get cnpjComercio(): string | undefined {
    return this.auth.comercioLogado()?.cnpj;
  }
}
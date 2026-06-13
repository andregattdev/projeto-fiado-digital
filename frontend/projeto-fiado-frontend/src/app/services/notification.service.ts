import { Injectable } from '@angular/core';
import Swal, { SweetAlertResult } from 'sweetalert2';

@Injectable({
  providedIn: 'root' // Isso garante que o serviço seja um Singleton e funcione em componentes Standalone
})
export class NotificationService {

  // Alerta de Sucesso (fecha sozinho após 2 segundos)
  sucesso(mensagem: string): void {
    Swal.fire({
      title: 'Sucesso!',
      text: mensagem,
      icon: 'success',
      timer: 2000,
      showConfirmButton: false,
      timerProgressBar: true
    });
  }

  // Alerta de Erro (exige que o usuário clique em fechar)
  erro(mensagem: string): void {
    Swal.fire({
      title: 'Erro!',
      text: mensagem,
      icon: 'error',
      confirmButtonColor: '#c62828',
      confirmButtonText: 'Fechar'
    });
  }

  // Confirmação de Ações (Retorna uma Promise com o resultado do clique)
  async confirmarExclusao(item: string): Promise<SweetAlertResult<any>> {
    return Swal.fire({
      title: 'Tem certeza?',
      text: `Você não poderá reverter a exclusão de: ${item}`,
      icon: 'warning',
      showCancelButton: true,
      confirmButtonColor: '#c62828',
      cancelButtonColor: '#9e9e9e',
      confirmButtonText: 'Sim, excluir!',
      cancelButtonText: 'Cancelar'
    });
  }
}
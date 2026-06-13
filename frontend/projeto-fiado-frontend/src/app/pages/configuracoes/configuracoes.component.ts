import { Component, Inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { 
  FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, 
  Validators, ValidationErrors, AbstractControl 
} from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import Swal from 'sweetalert2'; // ✅ Apenas SweetAlert
import { AuthService } from '../../services/auth.service';
import { NotificationService } from '../../services/notification.service';
import { CnpjPipe } from '../../shared/pipes/cnpj-pipe';

@Component({
  selector: 'app-configuracoes',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    CnpjPipe
  ],
  templateUrl: './configuracoes.component.html',
  styleUrl: './configuracoes.component.css'
})
export class ConfiguracoesComponent implements OnInit {
  form: FormGroup;
  salvando = false;
  mostrarSenhaAtual = false;
  mostrarNovaSenha = false;
  mostrarConfirmarSenha = false;

  constructor(
    private readonly fb: FormBuilder,
    private readonly auth: AuthService,
    @Inject(NotificationService) private readonly notification: NotificationService
  ) {
    this.form = this.fb.group({
      nomeLoja: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(120)]],
      nomeResponsavel: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(120)]],
      
      // ✅ EMAIL EDITÁVEL COM VALIDAÇÃO
      email: ['', [Validators.required, Validators.email, Validators.maxLength(150)]],
      
      telefone: ['', [Validators.maxLength(20), this.validarTelefone]],
      cnpj: ['', [this.validarCnpj]],

      // Campos de senha (opcionais)
      senhaAtual: [''],
      novaSenha: ['', [
        Validators.minLength(8),
        Validators.pattern(/^(?=.*[A-Z])(?=.*[!@#$%^&*(),.?":{}|<>])/),
        this.validarForcaSenha
      ]],
      confirmarNovaSenha: ['']
    }, { 
      validators: this.compararSenhas // Validação cruzada: novaSenha = confirmarNovaSenha
    });
  }

  ngOnInit(): void {
    const comercio = this.auth.comercioLogado();
    if (comercio) {
      const cnpjPipe = new CnpjPipe();
      this.form.patchValue({
        nomeLoja: comercio.nomeLoja,
        nomeResponsavel: comercio.nomeResponsavel,
        email: comercio.email,
        telefone: this.formatarTelefoneExibicao(comercio.telefone),
        cnpj: cnpjPipe.transform(comercio.cnpj || '')
      });
    }
  }

  /**
   * Salvar - VERSÃO 2: Segura + SweetAlert2 em todas as mensagens
   */
  salvar(): void {
    if (this.form.invalid) {
      this.marcarCamposComoTocado();
      this.mostrarAviso('Preencha os campos corretamente antes de salvar.');
      return;
    }

    const valores = this.form.getRawValue();
    const emailOriginal = this.auth.comercioLogado()?.email;

    // ✅ REGRAS DE SEGURANÇA:
    const alterouEmail = valores.email.trim() !== emailOriginal;
    const alterouSenha = !!valores.novaSenha?.trim() || !!valores.confirmarNovaSenha?.trim();

    // Se alterou e-mail OU senha → obrigatório senha atual
    if ((alterouEmail || alterouSenha) && !valores.senhaAtual?.trim()) {
      Swal.fire({
        icon: 'warning',
        title: 'Confirmação necessária',
        text: 'Para alterar o e-mail ou a senha, informe sua senha atual.',
        confirmButtonText: 'Entendi',
        confirmButtonColor: '#6c63ff',
        toast: false
      });
      return;
    }

    // Validação de senhas
    if (alterouSenha) {
      if (valores.novaSenha !== valores.confirmarNovaSenha) {
        this.mostrarErro('A nova senha e a confirmação não são iguais.');
        return;
      }
    }

    // ✅ CONFIRMAÇÃO PRINCIPAL (IGUAL AO COMPONENTE DE CLIENTES)
    Swal.fire({
      title: 'Salvar alterações?',
      text: 'Deseja realmente atualizar suas configurações?',
      icon: 'question',
      showCancelButton: true,
      confirmButtonColor: '#6c63ff',
      cancelButtonColor: '#64748b',
      cancelButtonText: 'Cancelar',
      confirmButtonText: 'Sim, salvar'
    }).then(resultado => {
      if (resultado.isConfirmed) {
        this.executarSalvamento(valores, alterouEmail);
      }
    });
  }

  /**
   * Chama a API para salvar
   */
  private executarSalvamento(valores: any, alterouEmail: boolean): void {
    this.salvando = true;

    const dados = this.montarDadosEnvio(valores, alterouEmail);

    this.auth.atualizarPerfil(dados).subscribe({
      next: () => {
        this.salvando = false;
        // ✅ SUCESSO NO CANTO SUPERIOR (IGUAL O OUTRO TS)
        Swal.fire({
          icon: 'success',
          title: 'Salvo com sucesso!',
          text: 'Suas configurações foram atualizadas.',
          timer: 2500,
          showConfirmButton: false,
          toast: true,
          position: 'top-end' // ⬅️ Canto superior direito
        });
        this.limparCamposSenha();
      },
      error: (erro) => {
        this.salvando = false;
        const mensagem = erro?.error?.message || 'Não foi possível salvar. Verifique os dados ou sua senha atual.';
        this.mostrarErro(mensagem);
      }
    });
  }

  // ---------------------------------------------------------------------------
  // ✅ MÉTODOS DE APOIO
  // ---------------------------------------------------------------------------

  private montarDadosEnvio(valores: any, alterouEmail: boolean): any {
    const telefoneLimpo = valores.telefone?.replace(/\D/g, '') || '';
    const cnpjLimpo = valores.cnpj?.replace(/\D/g, '') || '';

    const dados: any = {
      nomeLoja: valores.nomeLoja.trim(),
      nomeResponsavel: valores.nomeResponsavel.trim(),
      email: valores.email.trim(),
      telefone: telefoneLimpo,
      cnpj: cnpjLimpo
    };

    // Envia senha atual apenas se alterou e-mail ou senha
    if (alterouEmail || valores.novaSenha?.trim()) {
      dados.senhaAtual = valores.senhaAtual;
    }

    // Envia nova senha apenas se preenchida
    if (valores.novaSenha?.trim()) {
      dados.novaSenha = valores.novaSenha;
    }

    return dados;
  }

  private formatarTelefoneExibicao(telefone: string | undefined): string {
    if (!telefone) return '';
    const numeros = telefone.replace(/\D/g, '');
    if (numeros.length === 11) return `(${numeros.slice(0,2)}) ${numeros.slice(2,7)}-${numeros.slice(7)}`;
    if (numeros.length === 10) return `(${numeros.slice(0,2)}) ${numeros.slice(2,6)}-${numeros.slice(6)}`;
    return telefone;
  }

  private limparCamposSenha(): void {
    this.form.patchValue({ senhaAtual: '', novaSenha: '', confirmarNovaSenha: '' });
    this.form.get('senhaAtual')?.setErrors(null);
    this.form.get('novaSenha')?.setErrors(null);
  }

  private marcarCamposComoTocado(): void {
    Object.values(this.form.controls).forEach(controle => {
      controle.markAsTouched();
      controle.updateValueAndValidity();
    });
  }

  // ---------------------------------------------------------------------------
  // ✅ VALIDAÇÕES
  // ---------------------------------------------------------------------------

  private validarTelefone(controle: AbstractControl): ValidationErrors | null {
    const valor = controle.value?.replace(/\D/g, '');
    if (!valor) return null;
    if (valor.length < 10 || valor.length > 11) return { telefoneInvalido: true };
    return null;
  }

  private validarCnpj(controle: AbstractControl): ValidationErrors | null {
    const valor = controle.value?.replace(/\D/g, '');
    if (!valor) return null;
    if (valor.length !== 14) return { cnpjInvalido: true };
    if (/^(\d)\1{13}$/.test(valor)) return { cnpjInvalido: true };
    return null;
  }

  private validarForcaSenha(controle: AbstractControl): ValidationErrors | null {
    const valor = controle.value;
    if (!valor) return null;
    const temMaiuscula = /[A-Z]/.test(valor);
    const temEspecial = /[!@#$%^&*(),.?":{}|<>]/.test(valor);
    const temNumero = /\d/.test(valor);
    if (!temMaiuscula || !temEspecial || !temNumero || valor.length < 8) return { senhaFraca: true };
    return null;
  }

  private compararSenhas(grupo: FormGroup): ValidationErrors | null {
    const nova = grupo.get('novaSenha')?.value;
    const confirmar = grupo.get('confirmarNovaSenha')?.value;
    if (nova && confirmar && nova !== confirmar) {
      grupo.get('confirmarNovaSenha')?.setErrors({ senhaDiferente: true });
      return { senhaDiferente: true };
    }
    return null;
  }

  // ---------------------------------------------------------------------------
  // ✅ CONTROLE DE SENHA
  // ---------------------------------------------------------------------------
  alternarVisibilidadeSenhaAtual(): void { this.mostrarSenhaAtual = !this.mostrarSenhaAtual; }
  alternarVisibilidadeNovaSenha(): void { this.mostrarNovaSenha = !this.mostrarNovaSenha; }
  alternarVisibilidadeConfirmarSenha(): void { this.mostrarConfirmarSenha = !this.mostrarConfirmarSenha; }

  // ---------------------------------------------------------------------------
  // ✅ MÉTODOS DE MENSAGEM (PADRÃO SWEETALERT2)
  // ---------------------------------------------------------------------------
  private mostrarAviso(texto: string): void {
    Swal.fire({
      icon: 'warning',
      title: 'Atenção',
      text: texto,
      confirmButtonColor: '#6c63ff',
      toast: true,
      position: 'top-end',
      timer: 3000,
      showConfirmButton: false
    });
  }

  private mostrarErro(texto: string): void {
    Swal.fire({
      icon: 'error',
      title: 'Ops! Algo deu errado',
      text: texto,
      confirmButtonColor: '#6c63ff',
      confirmButtonText: 'Fechar'
    });
  }
}
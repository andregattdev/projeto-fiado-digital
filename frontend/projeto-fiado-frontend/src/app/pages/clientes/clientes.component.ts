import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { 
  FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, 
  Validators, AbstractControl, ValidationErrors 
} from '@angular/forms';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatDialogModule } from '@angular/material/dialog';
import { MatSnackBarModule, MatSnackBar } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatChipsModule } from '@angular/material/chips';
import { RouterLink, ActivatedRoute } from '@angular/router';
import { ClienteService, ClienteDTO } from '../../services/cliente.service';
import { AuthService } from '../../services/auth.service';
import Swal from 'sweetalert2';
import { HttpErrorResponse } from '@angular/common/http';
import { extrairMensagemErro } from '../../core/http-error.util';
import { CpfPipe } from '../../shared/pipes/cpf-pipe';

@Component({
  selector: 'app-clientes',
  standalone: true,
  imports: [
    CommonModule, FormsModule, ReactiveFormsModule,
    MatTableModule, MatButtonModule, MatIconModule,
    MatFormFieldModule, MatInputModule, MatDialogModule,
    MatSnackBarModule, MatProgressSpinnerModule, MatChipsModule, 
    RouterLink, CpfPipe,
  ],
  templateUrl: './clientes.component.html',
  styleUrl: './clientes.component.css'
})
export class ClientesComponent implements OnInit {
  clientes: ClienteDTO[] = [];
  mostrarFormulario = false;
  editando = false;
  clienteEditandoId: number | null = null;
  loading = false;
  salvando = false;
  termoBusca = '';
  form: FormGroup;

  // Colunas exibidas na tabela (fácil de alterar)
  colunasTabela: string[] = ['nome', 'cpf', 'telefone', 'endereco', 'totalDevido', 'acoes'];

  constructor(
    private readonly clienteService: ClienteService,
    private readonly fb: FormBuilder,
    private readonly snack: MatSnackBar,
    private readonly auth: AuthService,
    private readonly route: ActivatedRoute
  ) {
    this.form = this.fb.group({
      nome: ['', [Validators.required, Validators.minLength(3)]],
      cpf: ['', [this.validarCpf]], // ✅ Validação de CPF adicionada
      telefone: ['', [this.validarTelefone]], // ✅ Validação de telefone adicionada
      endereco: [''],
      dataNascimento: ['']
    });
  }

  ngOnInit(): void {
    this.carregarClientes();

    // Abre formulário automaticamente se vier de "Nova Venda" do Dashboard
    this.route.queryParams.subscribe(params => {
      if (params['acao'] === 'novo') {
        this.abrirFormulario();
      }
    });
  }

  /**
   * Carrega lista de clientes do backend
   */
  carregarClientes(): void {
    this.loading = true;
    this.clienteService.listarTodos().subscribe({
      next: (dados) => { 
        this.clientes = dados; 
        this.loading = false; 
      },
      error: (erro) => { 
        this.loading = false; 
        this.mostrarMensagem('Erro ao carregar clientes', 'error');
        console.error(erro);
      }
    });
  }

  /**
   * Busca clientes por nome
   */
  buscar(): void {
    const termo = this.termoBusca.trim();
    if (!termo) { 
      this.carregarClientes(); 
      return; 
    }

    this.loading = true;
    this.clienteService.buscarPorNome(termo).subscribe({
      next: (dados) => { this.clientes = dados; this.loading = false; },
      error: () => { this.loading = false; }
    });
  }

  /**
   * Limpa campo de busca e recarrega tudo
   */
  limparBusca(): void {
    this.termoBusca = '';
    this.carregarClientes();
  }

  /**
   * Abre formulário para novo cadastro
   */
  abrirFormulario(): void {
    this.editando = false;
    this.clienteEditandoId = null;
    this.form.reset();
    this.form.markAsPristine();
    this.form.markAsUntouched();
    this.mostrarFormulario = true;
    
    // Rola suavemente para o topo
    setTimeout(() => window.scrollTo({ top: 0, behavior: 'smooth' }), 100);
  }

  /**
   * Fecha e reseta formulário
   */
  cancelarFormulario(): void {
    this.mostrarFormulario = false;
    this.form.reset();
  }

  /**
   * Prepara formulário para edição com dados atuais
   */
  editarCliente(cliente: ClienteDTO): void {
    this.editando = true;
    this.clienteEditandoId = cliente.id!;
    
    this.form.patchValue({
      nome: cliente.nome,
      cpf: cliente.cpf,
      telefone: cliente.telefone,
      endereco: cliente.endereco,
      dataNascimento: cliente.dataNascimento
    });

    this.mostrarFormulario = true;
    setTimeout(() => window.scrollTo({ top: 0, behavior: 'smooth' }), 100);
  }

  /**
   * Salva: cria ou atualiza cliente
   */
  salvar(): void {
    if (this.form.invalid) {
      Swal.fire({
        icon: 'warning',
        title: 'Formulário incompleto',
        text: 'Preencha corretamente os campos obrigatórios (*).',
        confirmButtonText: 'Entendi',
        confirmButtonColor: '#6c63ff'
      });
      this.marcarCamposComoTocado(); // ✅ Mostra erros nos campos
      return;
    }

    this.salvando = true;
    const dados: ClienteDTO = this.form.value;

    // Remove caracteres especiais antes de enviar
    if (dados.cpf) dados.cpf = dados.cpf.replace(/\D/g, '');
    if (dados.telefone) dados.telefone = dados.telefone.replace(/\D/g, '');

    const operacao$ = this.editando && this.clienteEditandoId
      ? this.clienteService.atualizar(this.clienteEditandoId, dados)
      : this.clienteService.cadastrar(dados);

    operacao$.subscribe({
      next: () => {
        this.salvando = false;
        this.mostrarFormulario = false;
        this.carregarClientes();

        Swal.fire({
          icon: 'success',
          title: 'Sucesso!',
          text: this.editando 
            ? 'Cliente atualizado com sucesso!' 
            : 'Cliente cadastrado com sucesso!',
          timer: 2500,
          showConfirmButton: false,
          toast: true,
          position: 'top-end'
        });
      },
      error: (erro: HttpErrorResponse) => {
        this.salvando = false;
        Swal.fire({
          icon: 'error',
          title: 'Ops! Algo deu errado',
          text: extrairMensagemErro(erro, 'Não foi possível salvar os dados.'),
          confirmButtonText: 'Fechar',
          confirmButtonColor: '#6c63ff'
        });
      }
    });
  }

  /**
   * Confirma e executa exclusão
   */
  confirmarExclusao(cliente: ClienteDTO): void {
    Swal.fire({
      title: 'Excluir cliente?',
      html: `Tem certeza que deseja excluir <strong>${cliente.nome}</strong>?<br>Essa ação não pode ser desfeita!`,
      icon: 'warning',
      showCancelButton: true,
      confirmButtonColor: '#dc2626',
      cancelButtonColor: '#64748b',
      cancelButtonText: 'Cancelar',
      confirmButtonText: 'Sim, excluir'
    }).then(resultado => {
      if (resultado.isConfirmed) {
        this.clienteService.deletar(cliente.id!).subscribe({
          next: () => { 
            this.carregarClientes(); 
            this.mostrarMensagem('Cliente excluído com sucesso', 'success');
          },
          error: () => this.mostrarMensagem('Erro ao excluir cliente', 'error')
        });
      }
    });
  }

  /**
   * Abre WhatsApp com mensagem de cobrança
   */
  compartilharSaldoWhatsApp(cliente: ClienteDTO): void {
    if (!cliente.telefone) {
      this.mostrarMensagem('Cliente sem telefone cadastrado', 'warning');
      return;
    }

    const nomeLoja = this.auth.comercioLogado()?.nomeLoja ?? 'Fiado Digital';
    const valor = cliente.totalDevido ?? 0;
    const valorFormatado = valor.toLocaleString('pt-BR', { 
      style: 'currency', 
      currency: 'BRL' 
    });

    const mensagem = `Olá, ${cliente.nome}! 👋\n` +
      `Passando para lembrar do seu saldo em aberto no comércio *${nomeLoja}*:\n\n` +
      `*Valor devido: ${valorFormatado}*\n\n` +
      `Caso queira efetuar o pagamento ou conversar sobre, estamos à disposição.\n` +
      `Agradecemos a preferência! 😊`;

    const telefoneFormatado = this.formatarTelefoneParaWhatsApp(cliente.telefone);
    const url = `https://wa.me/${telefoneFormatado}?text=${encodeURIComponent(mensagem)}`;

    window.open(url, '_blank');
  }

  // 
  // MÉTODOS DE APOIO E VALIDAÇÕES
  // 

  /**
   * Formata telefone para padrão WhatsApp (55 + DDD + Número)
   */
  private formatarTelefoneParaWhatsApp(telefone: string): string {
    const numeros = telefone.replace(/\D/g, '');
    if (numeros.length === 10 || numeros.length === 11) {
      return `55${numeros}`;
    }
    return numeros;
  }

  /**
   * Valida CPF no formulário
   */
  private validarCpf(controle: AbstractControl): ValidationErrors | null {
    const cpf = controle.value?.replace(/\D/g, '');
    if (!cpf) return null; // CPF é opcional

    // Validação simples de tamanho e dígitos
    if (cpf.length !== 11) return { cpfInvalido: true };
    
    // Elimina CPFs inválidos conhecidos
    if (/^(\d)\1{10}$/.test(cpf)) return { cpfInvalido: true };

    return null;
  }

  /**
   * Valida telefone no formulário
   */
  private validarTelefone(controle: AbstractControl): ValidationErrors | null {
    const tel = controle.value?.replace(/\D/g, '');
    if (!tel) return null; // Telefone é opcional

    if (tel.length < 10 || tel.length > 11) return { telefoneInvalido: true };
    return null;
  }

  /**
   * Marca todos os campos como tocados para mostrar erros
   */
  private marcarCamposComoTocado(): void {
    Object.values(this.form.controls).forEach(controle => {
      controle.markAsTouched();
      controle.updateValueAndValidity();
    });
  }

  /**
   * Exibe mensagem padrão com SnackBar
   */
  private mostrarMensagem(texto: string, tipo: 'success' | 'error' | 'warning'): void {
    this.snack.open(texto, 'Fechar', {
      duration: 3000,
      panelClass: tipo === 'error' ? 'snack-erro' : tipo === 'success' ? 'snack-sucesso' : 'snack-aviso',
      horizontalPosition: 'right',
      verticalPosition: 'top'
    });
  }
}
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { 
  FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, 
  FormArray, Validators, AbstractControl, ValidationErrors 
} from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectModule } from '@angular/material/select';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatExpansionModule } from '@angular/material/expansion';
import { VendaService, VendaResponseDTO, VendaRequestDTO } from '../../services/venda.service';
import { ClienteService, ClienteDTO } from '../../services/cliente.service';
import { PagamentoService } from '../../services/pagamento.service';
import Swal from 'sweetalert2';
import { HttpErrorResponse } from '@angular/common/http';
import { extrairMensagemErro } from '../../core/http-error.util';
import { AuthService } from '../../services/auth.service';
import { CpfPipe } from "../../shared/pipes/cpf-pipe";

// Interface para estruturar o agrupamento no front-end
export interface GrupoVendaCliente {
  nomeCliente: string;
  cpfCliente?: string;
  saldoDevedorTotal: number;
  totalCompras: number;
  compras: VendaResponseDTO[];
}

@Component({
  selector: 'app-vendas',
  standalone: true,
  imports: [
    CommonModule, FormsModule, ReactiveFormsModule,
    MatFormFieldModule, MatInputModule, MatButtonModule,
    MatIconModule, MatSelectModule,
    MatProgressSpinnerModule, MatExpansionModule,
    CpfPipe
  ],
  templateUrl: './vendas.component.html',
  styleUrl: './vendas.component.css'
})
export class VendasComponent implements OnInit {
  vendas: VendaResponseDTO[] = [];
  vendasAgrupadas: GrupoVendaCliente[] = [];
  clientes: ClienteDTO[] = [];
  mostrarFormVenda = false;
  loading = false;
  salvando = false;
  clienteFiltroId: number | null = null;
  apenasEmAberto = false;
  vendaForm: FormGroup;
  cpfClienteSelecionado: string | null = null;

  // Opções de forma de pagamento (centralizado)
  readonly formasPagamento = [
    { valor: 'DINHEIRO', nome: 'Dinheiro' },
    { valor: 'PIX', nome: 'PIX' },
    { valor: 'CARTAO', nome: 'Cartão' },
    { valor: 'TRANSFERENCIA', nome: 'Transferência' }
  ];

  constructor(
    private readonly vendaService: VendaService,
    private readonly clienteService: ClienteService,
    private readonly pagamentoService: PagamentoService,
    private readonly fb: FormBuilder,
    private readonly route: ActivatedRoute,
    private readonly auth: AuthService
  ) {
    this.vendaForm = this.fb.group({
      clienteId: [null, Validators.required],
      observacao: ['', Validators.maxLength(255)],
      itens: this.fb.array([this.criarItemForm()])
    });

    // Atualiza CPF quando seleciona cliente
    this.vendaForm.get('clienteId')!.valueChanges.subscribe(id => {
      const cliente = this.clientes.find(c => c.id === id);
      this.cpfClienteSelecionado = cliente?.cpf || null;
    });
  }

  ngOnInit(): void {
    this.carregarClientes();
    
    // Abertura por parâmetro da URL
    this.route.queryParams.subscribe(params => {
      if (params['clienteId']) {
        this.clienteFiltroId = +params['clienteId'];
        this.vendaForm.patchValue({ clienteId: this.clienteFiltroId });
        this.mostrarFormVenda = true;
      }
      if (params['acao'] === 'novo') {
        this.abrirFormularioNovaVenda();
      }
    });

    this.filtrarVendas();
  }

  // Getter para FormArray de itens
  get itens(): FormArray { return this.vendaForm.get('itens') as FormArray; }

  /**
   * Cria grupo de controles para um item da venda
   */
  criarItemForm(): FormGroup {
    return this.fb.group({
      produto: ['', [Validators.required, Validators.minLength(2)]],
      quantidade: [1, [Validators.required, Validators.min(1), this.validarQuantidade]],
      precoUnitario: [0.01, [Validators.required, Validators.min(0.01), this.validarPreco]]
    });
  }

  /**
   * Adiciona novo item na lista
   */
  adicionarItem(): void { 
    this.itens.push(this.criarItemForm()); 
  }

  /**
   * Remove item da lista (não permite remover o primeiro)
   */
  removerItem(indice: number): void {
    if (this.itens.length <= 1) {
      this.mostrarAviso('Não é possível remover todos os itens.');
      return;
    }
    this.itens.removeAt(indice);
  }

  /**
   * Calcula subtotal de um item
   */
  getSubtotal(indice: number): number {
    const item = this.itens.at(indice);
    const qtd = item.get('quantidade')?.value || 0;
    const preco = item.get('precoUnitario')?.value || 0;
    return qtd * preco;
  }

  /**
   * ✅ MÉTODO RESTAURADO - Chamado no HTML para atualizar valores
   */
  calcularTotalItem(indice: number): void {
    // Método que força a atualização do subtotal e do total da venda
    // O Angular já recalcula automaticamente, mas é necessário para o template
    const subtotal = this.getSubtotal(indice);
    console.log(`Subtotal do item ${indice}: R$ ${subtotal.toFixed(2)}`);
  }

  /**
   * Calcula valor total da venda
   */
  calcularTotal(): number { 
    return this.itens.controls.reduce((soma, _, i) => soma + this.getSubtotal(i), 0); 
  }

  /**
   * Abre/fecha formulário e reseta dados
   */
  toggleNovaVenda(): void {
    this.mostrarFormVenda = !this.mostrarFormVenda;
    if (!this.mostrarFormVenda) {
      this.resetarFormulario();
    } else {
      setTimeout(() => window.scrollTo({ top: 0, behavior: 'smooth' }), 100);
    }
  }

  /**
   * Abre formulário vindo do Dashboard
   */
  abrirFormularioNovaVenda(): void {
    this.mostrarFormVenda = true;
    this.resetarFormulario();
    setTimeout(() => window.scrollTo({ top: 0, behavior: 'smooth' }), 100);
  }

  /**
   * Filtra e carrega vendas conforme opções
   */
  filtrarVendas(): void {
    this.loading = true;

    const requisicao$ = this.clienteFiltroId
      ? this.vendaService.listarPorCliente(this.clienteFiltroId)
      : this.vendaService.listarEmAberto();

    requisicao$.subscribe({
      next: (vendas: VendaResponseDTO[]) => {
        this.vendas = this.apenasEmAberto 
          ? vendas.filter(v => v.ativa && v.saldoDevedor > 0) 
          : vendas;
        
        this.agruparVendasPorCliente();
        this.loading = false;
      },
      error: (erro: HttpErrorResponse) => {
        this.loading = false;
        this.mostrarErro(extrairMensagemErro(erro, 'Erro ao carregar vendas.'));
      }
    });
  }

  /**
   * Agrupa vendas por cliente para visualização organizada
   */
  private agruparVendasPorCliente(): void {
    const mapa = new Map<string, GrupoVendaCliente>();
    
    this.vendas.forEach((venda: VendaResponseDTO) => {
      const chave = `${venda.nomeCliente}|${venda.cpfCliente || ''}`;
      
      if (!mapa.has(chave)) {
        mapa.set(chave, {
          nomeCliente: venda.nomeCliente,
          cpfCliente: venda.cpfCliente,
          saldoDevedorTotal: 0,
          totalCompras: 0,
          compras: []
        });
      }

      const grupo = mapa.get(chave)!;
      grupo.compras.push(venda);
      grupo.totalCompras++;
      
      if (venda.ativa && venda.saldoDevedor > 0) {
        grupo.saldoDevedorTotal += venda.saldoDevedor;
      }
    });

    // Ordena por nome do cliente
    this.vendasAgrupadas = Array.from(mapa.values())
      .sort((a, b) => a.nomeCliente.localeCompare(b.nomeCliente));
  }

  /**
   * Salva nova venda com validações e confirmação
   */
  salvarVenda(): void {
    if (this.vendaForm.invalid) {
      this.marcarCamposComoTocado();
      this.mostrarAviso('Preencha todos os campos obrigatórios corretamente.');
      return;
    }

    const valorTotal = this.calcularTotal();
    if (valorTotal <= 0) {
      this.mostrarAviso('O valor total da venda deve ser maior que R$ 0,00.');
      return;
    }

    // Confirmação antes de salvar
    Swal.fire({
      title: 'Registrar Venda?',
      html: `Valor total: <strong>R$ ${valorTotal.toFixed(2).replace('.', ',')}</strong>`,
      icon: 'question',
      showCancelButton: true,
      confirmButtonColor: '#6c63ff',
      cancelButtonColor: '#64748b',
      cancelButtonText: 'Cancelar',
      confirmButtonText: 'Sim, registrar'
    }).then(resultado => {
      if (!resultado.isConfirmed) return;

      this.salvando = true;
      const dados: VendaRequestDTO = this.limparDadosEnvio(this.vendaForm.value);

      this.vendaService.criarVenda(dados).subscribe({
        next: () => {
          this.salvando = false;
          this.toggleNovaVenda();
          this.filtrarVendas();
          
          // ✅ Mensagem no canto superior padrão
          Swal.fire({
            icon: 'success',
            title: 'Venda registrada!',
            timer: 2500,
            showConfirmButton: false,
            toast: true,
            position: 'top-end'
          });
        },
        error: (erro: HttpErrorResponse) => {
          this.salvando = false;
          this.mostrarErro(extrairMensagemErro(erro, 'Não foi possível registrar a venda.'));
        }
      });
    });
  }

  /**
   * Abre modal para registrar pagamento
   */
  registrarPagamento(venda: VendaResponseDTO): void {
    if (!venda.ativa || venda.saldoDevedor <= 0) {
      this.mostrarAviso('Esta venda não possui saldo pendente.');
      return;
    }

    Swal.fire({
      title: 'Registrar Pagamento',
      html: `
        <div style="text-align: left; padding: 0 8px;">
          <p style="margin-bottom: 12px;">Cliente: <strong>${venda.nomeCliente}</strong></p>
          <p style="margin-bottom: 16px;">Saldo devido: 
            <strong class="text-primary">R$ ${venda.saldoDevedor.toFixed(2).replace('.', ',')}</strong>
          </p>
          
          <label style="font-size: 0.9rem; color: #64748b;">Valor Pago</label>
          <input id="swal-valor" class="swal2-input" type="number" 
                 step="0.01" min="0.01" max="${venda.saldoDevedor}" 
                 placeholder="Ex: 50,00" style="margin-top: 4px;" />
          
          <label style="font-size: 0.9rem; color: #64748b; margin-top: 12px;">Forma de Pagamento</label>
          <select id="swal-forma" class="swal2-select" style="margin-top: 4px; width: 100%;">
            ${this.formasPagamento.map(f => `<option value="${f.valor}">${f.nome}</option>`).join('')}
          </select>
        </div>
      `,
      width: '360px',
      showCancelButton: true,
      confirmButtonText: 'Confirmar',
      cancelButtonText: 'Cancelar',
      confirmButtonColor: '#16a34a',
      cancelButtonColor: '#64748b',
      focusConfirm: false,
      preConfirm: () => {
        const valor = parseFloat((document.getElementById('swal-valor') as HTMLInputElement).value);
        const forma = (document.getElementById('swal-forma') as HTMLSelectElement).value;

        if (!valor || valor <= 0) {
          Swal.showValidationMessage('Informe um valor válido maior que zero.');
          return false;
        }
        if (valor > venda.saldoDevedor) {
          Swal.showValidationMessage(`Valor não pode ultrapassar R$ ${venda.saldoDevedor.toFixed(2).replace('.', ',')}`);
          return false;
        }
        return { valor, formaPagamento: forma };
      }
    }).then(result => {
      if (!result.isConfirmed || !result.value) return;

      this.salvando = true;
      this.pagamentoService.registrar({
        vendaId: venda.id!,
        valor: result.value.valor,
        formaPagamento: result.value.formaPagamento
      }).subscribe({
        next: () => {
          this.salvando = false;
          this.filtrarVendas();
          Swal.fire({
            icon: 'success',
            title: 'Pagamento registrado!',
            timer: 2500,
            showConfirmButton: false,
            toast: true,
            position: 'top-end'
          });
        },
        error: (erro: HttpErrorResponse) => {
          this.salvando = false;
          this.mostrarErro(extrairMensagemErro(erro, 'Não foi possível registrar pagamento.'));
        }
      });
    });
  }

  /**
   * Cancela uma venda
   */
  cancelarVenda(venda: VendaResponseDTO): void {
    Swal.fire({
      title: 'Cancelar Venda?',
      html: `Tem certeza que deseja cancelar esta venda de <strong>${venda.nomeCliente}</strong>?<br>Essa ação não pode ser desfeita.`,
      icon: 'warning',
      showCancelButton: true,
      confirmButtonColor: '#dc2626',
      cancelButtonColor: '#64748b',
      cancelButtonText: 'Não',
      confirmButtonText: 'Sim, cancelar'
    }).then(resultado => {
      if (!resultado.isConfirmed) return;

      this.salvando = true;
      this.vendaService.cancelarVenda(venda.id!).subscribe({
        next: () => {
          this.salvando = false;
          this.filtrarVendas();
          Swal.fire({
            icon: 'success',
            title: 'Venda cancelada',
            timer: 2500,
            showConfirmButton: false,
            toast: true,
            position: 'top-end'
          });
        },
        error: (erro: HttpErrorResponse) => {
          this.salvando = false;
          this.mostrarErro(extrairMensagemErro(erro, 'Não foi possível cancelar.'));
        }
      });
    });
  }

  /**
   * Compartilha venda via WhatsApp
   */
  compartilharVendaWhatsApp(venda: VendaResponseDTO): void {
    const cliente = this.clientes.find(c => c.id === venda.clienteId);
    
    if (!cliente?.telefone) {
      this.mostrarAviso('Cliente não possui telefone cadastrado.');
      return;
    }

    const grupo = this.vendasAgrupadas.find(g => g.nomeCliente === venda.nomeCliente);
    const saldoTotal = grupo?.saldoDevedorTotal ?? venda.saldoDevedor;

    const dataFormatada = new Date(venda.dataVenda).toLocaleString('pt-BR', {
      day: '2-digit', month: '2-digit', year: 'numeric',
      hour: '2-digit', minute: '2-digit'
    });

    // Formata lista de itens
    const itensTexto = venda.itens?.length 
      ? venda.itens.map((item: any) => {
          const subtotal = (item.quantidade * item.precoUnitario).toFixed(2).replace('.', ',');
          const preco = item.precoUnitario.toFixed(2).replace('.', ',');
          return `• ${item.quantidade}x ${item.produto} - R$ ${preco} (Subtotal: R$ ${subtotal})`;
        }).join('\n')
      : '• Sem itens cadastrados';

    // Valores formatados
    const formatarValor = (v: number) => v.toFixed(2).replace('.', ',');
    const valorTotal = formatarValor(venda.valorTotal);
    const valorPago = formatarValor(venda.valorPago);
    const saldoDevedor = formatarValor(venda.saldoDevedor);
    const saldoTotalStr = formatarValor(saldoTotal);
    const nomeLoja = this.auth.comercioLogado()?.nomeLoja || 'Fiado Digital';
    const cpfTexto = venda.cpfCliente ? `CPF: ${venda.cpfCliente}` : 'CPF não informado';

    // Mensagem final
    const mensagem = `Olá, ${venda.nomeCliente}! 📝🛒\n\n` +
      `*Dados da compra*\n` +
      `${cpfTexto}\n` +
      `Realizada em: ${dataFormatada}\n` +
      `Comércio: *${nomeLoja}*\n\n` +
      `*Itens Comprados:*\n${itensTexto}\n\n` +
      `*Resumo Financeiro:*\n` +
      `Total da venda: R$ ${valorTotal}\n` +
      `Valor já pago: R$ ${valorPago}\n` +
      `Saldo pendente desta compra: R$ ${saldoDevedor}\n\n` +
      `🔹 *Seu saldo total acumulado: R$ ${saldoTotalStr}*\n\n` +
      `Qualquer dúvida, estamos à disposição!\nAgradecemos a preferência! 😊`;

    const telefoneFormatado = this.formatarTelefoneParaWhatsApp(cliente.telefone);
    const url = `https://wa.me/${telefoneFormatado}?text=${encodeURIComponent(mensagem)}`;

    window.open(url, '_blank');
  }

  // ---------------------------------------------------------------------------
  // ✅ MÉTODOS DE APOIO E VALIDAÇÕES
  // ---------------------------------------------------------------------------

  private carregarClientes(): void {
    this.clienteService.listarTodos().subscribe({
      next: (lista: ClienteDTO[]) => this.clientes = lista.sort((a, b) => a.nome.localeCompare(b.nome)),
      error: () => this.mostrarErro('Erro ao carregar lista de clientes.')
    });
  }

  private formatarTelefoneParaWhatsApp(telefone: string): string {
    const numeros = telefone.replace(/\D/g, '');
    if (numeros.length === 10 || numeros.length === 11) return `55${numeros}`;
    return numeros;
  }

  private limparDadosEnvio(dados: any): VendaRequestDTO {
    return {
      clienteId: dados.clienteId,
      observacao: dados.observacao?.trim() || null,
      itens: dados.itens.map((i: any) => ({
        produto: i.produto.trim(),
        quantidade: i.quantidade,
        precoUnitario: i.precoUnitario
      }))
    };
  }

  private resetarFormulario(): void {
    this.vendaForm.reset();
    this.cpfClienteSelecionado = null;
    
    // Reseta itens: deixa apenas um, limpo
    while (this.itens.length > 1) this.itens.removeAt(1);
    this.itens.at(0).reset({ produto: '', quantidade: 1, precoUnitario: 0.01 });
    
    this.vendaForm.markAsPristine();
    this.vendaForm.markAsUntouched();
  }

  private marcarCamposComoTocado(): void {
    Object.values(this.vendaForm.controls).forEach(c => c.markAsTouched());
    this.itens.controls.forEach(g => {
      Object.values((g as FormGroup).controls).forEach(c => c.markAsTouched());
    });
  }

  // Validações customizadas
  private validarQuantidade(controle: AbstractControl): ValidationErrors | null {
    const valor = controle.value;
    return valor && valor > 0 ? null : { quantidadeInvalida: true };
  }

  private validarPreco(controle: AbstractControl): ValidationErrors | null {
    const valor = controle.value;
    return valor && valor >= 0.01 ? null : { precoInvalido: true };
  }

  // ---------------------------------------------------------------------------
  // ✅ PADRÃO DE MENSAGENS SWEETALERT2
  // ---------------------------------------------------------------------------
  private mostrarAviso(texto: string): void {
    Swal.fire({
      icon: 'warning',
      title: 'Atenção',
      text: texto,
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
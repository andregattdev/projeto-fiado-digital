import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { RouterLink } from '@angular/router';
import { RelatorioService, RelatorioMensalDTO } from '../../services/relatorio.service';
import { VendaService, VendasPeriodoDTO } from '../../services/venda.service';
import { CpfPipe } from '../../shared/pipes/cpf-pipe';
 

@Component({
  selector: 'app-relatorio',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatIconModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatProgressSpinnerModule,
    RouterLink,
    CpfPipe 
  ],
  templateUrl: './relatorio.component.html',
  styleUrl: './relatorio.component.css'
})
export class RelatorioComponent implements OnInit {
  mes = new Date().getMonth() + 1;
  ano = new Date().getFullYear();
  dataInicio = '';
  dataFim = '';

  relatorio: RelatorioMensalDTO | null = null;
  vendasPeriodo: VendasPeriodoDTO | null = null;
  loadingMensal = false;
  loadingPeriodo = false;

  readonly meses = [
    { valor: 1, nome: 'Janeiro' },
    { valor: 2, nome: 'Fevereiro' },
    { valor: 3, nome: 'Março' },
    { valor: 4, nome: 'Abril' },
    { valor: 5, nome: 'Maio' },
    { valor: 6, nome: 'Junho' },
    { valor: 7, nome: 'Julho' },
    { valor: 8, nome: 'Agosto' },
    { valor: 9, nome: 'Setembro' },
    { valor: 10, nome: 'Outubro' },
    { valor: 11, nome: 'Novembro' },
    { valor: 12, nome: 'Dezembro' }
  ];

  readonly anos: number[] = [];

  constructor(
    private relatorioService: RelatorioService,
    private vendaService: VendaService
  ) {
    const anoAtual = new Date().getFullYear();
    for (let a = anoAtual; a >= anoAtual - 5; a--) {
      this.anos.push(a);
    }
    this.definirPeriodoPadrao();
  }

  ngOnInit(): void {
    this.gerarRelatorioMensal();
  }

  // 3. Método auxiliar para o cabeçalho exibir o nome por extenso (ex: Junho/2026)
  get nomeMesAtual(): string {
    const m = this.meses.find(item => item.valor === Number(this.mes));
    return m ? m.nome : '';
  }

  definirPeriodoPadrao(): void {
    const hoje = new Date();
    const inicio = new Date(hoje.getFullYear(), hoje.getMonth(), 1);
    this.dataInicio = this.formatarData(inicio);
    this.dataFim = this.formatarData(hoje);
  }

  private formatarData(d: Date): string {
    const y = d.getFullYear();
    const m = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    return `${y}-${m}-${day}`;
  }

  gerarRelatorioMensal(): void {
    this.loadingMensal = true;
    this.relatorioService.relatorioMensal(this.mes, this.ano).subscribe({
      next: r => {
        this.relatorio = r;
        this.loadingMensal = false;
      },
      error: () => {
        this.loadingMensal = false;
        this.relatorio = null;
      }
    });
  }

  buscarVendasPeriodo(): void {
    if (!this.dataInicio || !this.dataFim) return;
    this.loadingPeriodo = true;
    this.vendaService.listarPorPeriodo(this.dataInicio, this.dataFim).subscribe({
      next: v => {
        this.vendasPeriodo = v;
        this.loadingPeriodo = false;
      },
      error: () => {
        this.vendasPeriodo = null;
        this.loadingPeriodo = false;
      }
    });
  }

  // Adicione dentro da classe RelatorioComponent
get devedores() {
  return this.relatorio?.devedores || [];
}

  imprimir(): void {
    window.print();
  }
}
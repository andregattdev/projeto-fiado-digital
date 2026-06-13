import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NavigationEnd, Router } from '@angular/router';
import { filter } from 'rxjs/operators';
import { AuthService } from '../../services/auth.service';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatButtonModule } from '@angular/material/button';
import { RouterLink } from '@angular/router';
import { CpfPipe } from '../../shared/pipes/cpf-pipe';
import { DashboardService, DashboardDTO } from '../../services/dashboard.service';
import { Chart, registerables } from 'chart.js';
Chart.register(...registerables);

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatIconModule,
    MatTableModule,
    MatProgressSpinnerModule,
    MatButtonModule,
    CpfPipe,
    RouterLink
  ],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit {

  dados: DashboardDTO | null = null;
  loading = true;
  erroNoServidor = false;
  nomeLoja = '';
  chart: any;

  @ViewChild('graficoFinancas') set graficoCanvas(conteudo: ElementRef<HTMLCanvasElement> | undefined) {
    if (conteudo && !this.chart) {
      // Guarda a referência nativa que o Chart.js precisa
      this._graficoCanvasRef = conteudo;
      // Inicializa o gráfico no momento exato em que a tag nasce na tela
      this.inicializarGrafico();
    }
  }

  private _graficoCanvasRef!: ElementRef<HTMLCanvasElement>;

  constructor(
    private dashboardService: DashboardService,
    private auth: AuthService,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.nomeLoja = this.auth.comercioLogado()?.nomeLoja ?? '';
    this.carregarDadosDashboard();

    this.router.events.pipe(filter(e => e instanceof NavigationEnd)).subscribe(e => {
      const url = (e as NavigationEnd).urlAfterRedirects;
      if (url === '/dashboard' || url.startsWith('/dashboard?')) {
        this.carregarDadosDashboard();
      }
    });
  }

  carregarDadosDashboard(): void {
    this.loading = true;
    this.erroNoServidor = false;

    // Destrói o gráfico antigo ao recarregar os dados do painel
    if (this.chart) {
      this.chart.destroy();
      this.chart = null;
    }

    this.dashboardService.getDashboard().subscribe({
      next: (d) => {
        this.dados = d;
        this.loading = false;

      },
      error: (err) => {
        console.error('Erro crítico ao buscar dados do Dashboard:', err);
        this.erroNoServidor = true;
        this.loading = false;
      }
    });
  }

  inicializarGrafico(): void {
    // 1. BLINDAGEM: Só renderiza se o canvas e os dados do back-end existirem
    if (!this._graficoCanvasRef || !this.dados || !this.dados.evolucaoMensal) return;

    if (this.chart) {
      this.chart.destroy();
    }

    // 2. EXTRAÇÃO DINÂMICA DOS DADOS REAIS DO JAVA
    // Usamos a tipagem explícita :string[] e :number[] para o Chart.js não se confundir
    const mesesLabels: string[] = this.dados.evolucaoMensal.map(item => item.mes);
    const dadosVendas: number[] = this.dados.evolucaoMensal.map(item => item.totalVendido);
    const dadosRecebido: number[] = this.dados.evolucaoMensal.map(item => item.totalRecebido);

    this.chart = new Chart(this._graficoCanvasRef.nativeElement, {
      type: 'bar',
      data: {
        labels: mesesLabels,
        datasets: [
          {
            label: 'Total Vendido (R$)',
            data: dadosVendas, // Agora é interpretado como number[] puro
            backgroundColor: '#0ea5e9',
            borderRadius: 6,
            borderSkipped: false
          },
          {
            label: 'Total Recebido (R$)',
            data: dadosRecebido, // Agora é interpretado como number[] puro
            backgroundColor: '#10b981',
            borderRadius: 6,
            borderSkipped: false
          }
        ]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            position: 'top',
            labels: { font: { weight: 600, family: 'inherit' }, boxWidth: 12 }
          },
          tooltip: {
            callbacks: {
              label: function (context) {
                let value = context.parsed.y || 0;
                return `${context.dataset.label}: R$ ${value.toLocaleString('pt-BR', { minimumFractionDigits: 2 })}`;
              }
            }
          }
        },
        scales: {
          x: { grid: { display: false } },
          y: {
            grid: { color: '#e2e8f0' },
            ticks: {
              callback: function (value) {
                return 'R$ ' + value.toLocaleString('pt-BR');
              }
            }
          }
        }
      }
    });
  }
}
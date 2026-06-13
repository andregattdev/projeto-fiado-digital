import { Routes } from '@angular/router';
import { DashboardComponent } from './pages/dashboard/dashboard.component';
import { ClientesComponent } from './pages/clientes/clientes.component';
import { VendasComponent } from './pages/vendas/vendas.component';
import { RelatorioComponent } from './pages/relatorio/relatorio.component';
import { LoginComponent } from './pages/auth/login/login.component';
import { CadastroComponent } from './pages/auth/cadastro/cadastro.component';
import { MainLayoutComponent } from './layout/main-layout.component';
import { authGuard, guestGuard } from './core/auth.guard';
import { ConfiguracoesComponent } from './pages/configuracoes/configuracoes.component';

export const routes: Routes = [
  { path: 'login', component: LoginComponent, canActivate: [guestGuard] },
  { path: 'cadastro', component: CadastroComponent, canActivate: [guestGuard] },
  {
    path: '',
    component: MainLayoutComponent,
    canActivate: [authGuard],
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      { path: 'dashboard', component: DashboardComponent },
      { path: 'clientes', component: ClientesComponent },
      { path: 'vendas', component: VendasComponent },
      { path: 'vendas/nova', component: VendasComponent },
      { path: 'relatorios', component: RelatorioComponent },
      { path: 'configuracoes', component: ConfiguracoesComponent }
    ]
  },
  { path: '**', redirectTo: 'dashboard' }
];

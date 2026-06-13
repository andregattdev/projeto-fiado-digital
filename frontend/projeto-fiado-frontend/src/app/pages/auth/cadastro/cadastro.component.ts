import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { AuthService } from '../../../services/auth.service';
import { HttpErrorResponse } from '@angular/common/http';
import { extrairMensagemErro } from '../../../core/http-error.util';

@Component({
  selector: 'app-cadastro',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './cadastro.component.html',
  styleUrl: './cadastro.component.css'
})
export class CadastroComponent {
  form: FormGroup;
  loading = false;
  erro = '';

  constructor(
    private fb: FormBuilder,
    private auth: AuthService,
    private router: Router
  ) {
    this.form = this.fb.group({
      nomeLoja: ['', Validators.required],
      nomeResponsavel: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      telefone: [''],
      cnpj: [''],
      // ALTERAÇÃO AQUI: Aumentado para minLength(8) e adicionado o padrão Regex para maiúsculas e caracteres especiais
      senha: ['', [
        Validators.required, 
        Validators.minLength(8),
        Validators.pattern(/^(?=.*[A-Z])(?=.*[!@#$%^&*(),.?":{}|<>])/)
      ]],
      confirmarSenha: ['', Validators.required]
    });
  }

  cadastrar(): void {
    if (this.form.invalid) return;
    const { confirmarSenha, ...dados } = this.form.value;
    if (dados.senha !== confirmarSenha) {
      this.erro = 'As senhas não conferem';
      return;
    }
    this.loading = true;
    this.erro = '';
    this.auth.registrar(dados).subscribe({
      next: () => {
        this.loading = false;
        this.router.navigate(['/dashboard']);
      },
      error: (err: HttpErrorResponse) => {
        this.loading = false;
        this.erro = extrairMensagemErro(err, 'Não foi possível cadastrar.');
      }
    });
  }
}
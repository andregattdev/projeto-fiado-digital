import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'cpf',
})
export class CpfPipe implements PipeTransform {
  transform(value: string | number): string {
    if (!value) return '';

    // Remove qualquer caractere que não seja número
    const cpfPuro = value.toString().replace(/\D/g, '');

    // Verifica se tem os 11 dígitos corretos
    if (cpfPuro.length !== 11) {
      return value.toString(); // Retorna o texto original se não for um CPF válido
    }

    // Aplica a máscara: XXX.XXX.XXX-XX
    return cpfPuro.replace(/(\d{3})(\d{3})(\d{3})(\d{2})/, '$1.$2.$3-$4');
  }
}

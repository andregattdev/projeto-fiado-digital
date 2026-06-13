import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'cnpj',
  standalone: true
})
export class CnpjPipe implements PipeTransform {
  transform(value: string | number | undefined | null): string {
    if (!value) return '';

    // Remove qualquer caractere que não seja número
    const cnpjPuro = value.toString().replace(/\D/g, '');

    // Verifica se tem os 14 dígitos corretos
    if (cnpjPuro.length !== 14) {
      return value.toString(); // Retorna o texto original se não for um CNPJ válido
    }

    // Aplica a máscara: XX.XXX.XXX/XXXX-XX
    return cnpjPuro.replace(/(\d{2})(\d{3})(\d{3})(\d{4})(\d{2})/, '$1.$2.$3/$4-$5');
  }
}

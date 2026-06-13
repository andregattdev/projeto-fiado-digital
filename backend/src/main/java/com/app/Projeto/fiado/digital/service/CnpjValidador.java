package com.app.Projeto.fiado.digital.service;

/**
 * Validador de CNPJ (Cadastro Nacional da Pessoa Jurídica) utilizando o algoritmo padrão de dígitos verificadores.
 */
public class CnpjValidador {

    public static boolean isValid(String cnpj) {
        if (cnpj == null) {
            return false;
        }

        // Remove caracteres não numéricos
        cnpj = cnpj.replaceAll("\\D", "");

        // CNPJ precisa ter exatamente 14 dígitos
        if (cnpj.length() != 14) {
            return false;
        }

        // Evita sequências repetidas comuns
        if (cnpj.matches("(\\d)\\1{13}")) {
            return false;
        }

        try {
            char dig13, dig14;
            int sm, i, r, num, peso;

            // Cálculo do 1º Dígito Verificador
            sm = 0;
            peso = 2;
            for (i = 11; i >= 0; i--) {
                num = (int) (cnpj.charAt(i) - 48);
                sm = sm + (num * peso);
                peso = peso + 1;
                if (peso == 10) {
                    peso = 2;
                }
            }

            r = sm % 11;
            if ((r == 0) || (r == 1)) {
                dig13 = '0';
            } else {
                dig13 = (char) ((11 - r) + 48);
            }

            // Cálculo do 2º Dígito Verificador
            sm = 0;
            peso = 2;
            for (i = 12; i >= 0; i--) {
                num = (int) (cnpj.charAt(i) - 48);
                sm = sm + (num * peso);
                peso = peso + 1;
                if (peso == 10) {
                    peso = 2;
                }
            }

            r = sm % 11;
            if ((r == 0) || (r == 1)) {
                dig14 = '0';
            } else {
                dig14 = (char) ((11 - r) + 48);
            }

            // Verifica se os dígitos calculados batem com os informados no final
            return (dig13 == cnpj.charAt(12)) && (dig14 == cnpj.charAt(13));
        } catch (Exception e) {
            return false;
        }
    }
}

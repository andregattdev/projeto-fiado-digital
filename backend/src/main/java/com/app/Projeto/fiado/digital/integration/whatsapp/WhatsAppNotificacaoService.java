package com.app.Projeto.fiado.digital.integration.whatsapp;

import org.springframework.stereotype.Service;

import com.app.Projeto.fiado.digital.model.Venda;

/**
 * Ponto de extensão para integração futura com WhatsApp.
 * <ul>
 *   <li>Notificar cliente ao fechar compra fiado</li>
 *   <li>Enviar QR Code PIX para pagamento</li>
 *   <li>Confirmar pagamento parcial (saldo restante)</li>
 *   <li>Enviar comprovante quando quitado</li>
 * </ul>
 */
@Service
public class WhatsAppNotificacaoService {

    public void notificarNovaVendaFiado(Venda venda) {
        // TODO: integrar API WhatsApp Business / Evolution API / Twilio
    }

    public void notificarPagamentoParcial(Venda venda) {
        // TODO
    }

    public void notificarQuitacao(Venda venda) {
        // TODO
    }
}

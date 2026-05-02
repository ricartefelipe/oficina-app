package br.com.oficina.ordemservico.adapters.out.mail;

import br.com.oficina.ordemservico.application.port.NotificacaoOrdemServicoPort;
import br.com.oficina.ordemservico.domain.OrdemServico;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * Notificações por SMTP. Em desenvolvimento típico aponta para MailHog ({@code localhost:1025}).
 */
@Component
@ConditionalOnProperty(prefix = "app.notification", name = "enabled", havingValue = "true", matchIfMissing = true)
public class SmtpNotificacaoOrdemServicoAdapter implements NotificacaoOrdemServicoPort {

    private static final Logger log = LoggerFactory.getLogger(SmtpNotificacaoOrdemServicoAdapter.class);

    private final JavaMailSender mailSender;
    private final NotificacaoOrdemServicoProperties props;

    public SmtpNotificacaoOrdemServicoAdapter(JavaMailSender mailSender, NotificacaoOrdemServicoProperties props) {
        this.mailSender = mailSender;
        this.props = props;
    }

    @Override
    public void aoEnviarOrcamento(OrdemServico os) {
        String link = linkPublico(os);
        String corpo = """
                Ola, %s.

                O orcamento da sua ordem de servico foi enviado.
                Valor total: R$ %s
                Codigo de acompanhamento: %s

                Acompanhe e aprove pelo link:
                %s

                Atenciosamente,
                Oficina
                """.formatted(
                os.getCliente().getNome(),
                os.getOrcamentoTotal().toPlainString(),
                os.getTrackingCode(),
                link
        );
        enviar("Orcamento enviado — acompanhe sua OS", corpo);
    }

    @Override
    public void aoOrcamentoAprovado(OrdemServico os) {
        String corpo = """
                Ola, %s.

                O orcamento da ordem %s foi aprovado. A execucao dos servicos foi iniciada.

                Atenciosamente,
                Oficina
                """.formatted(os.getCliente().getNome(), os.getTrackingCode());
        enviar("Orcamento aprovado — OS " + os.getTrackingCode(), corpo);
    }

    @Override
    public void aoOrcamentoRecusado(OrdemServico os) {
        String corpo = """
                Ola, %s.

                O orcamento da ordem %s foi recusado. Entre em contato com a oficina se precisar de um novo orcamento.

                Atenciosamente,
                Oficina
                """.formatted(os.getCliente().getNome(), os.getTrackingCode());
        enviar("Orcamento recusado — OS " + os.getTrackingCode(), corpo);
    }

    @Override
    public void aoVeiculoEntregue(OrdemServico os) {
        String corpo = """
                Ola, %s.

                O veiculo referente a ordem %s foi entregue. Obrigado pela preferencia.

                Atenciosamente,
                Oficina
                """.formatted(os.getCliente().getNome(), os.getTrackingCode());
        enviar("Veiculo entregue — OS " + os.getTrackingCode(), corpo);
    }

    private String linkPublico(OrdemServico os) {
        String base = stripTrailingSlash(props.getPublicBaseUrl());
        return base + "/ordens-servico/" + os.getTrackingCode();
    }

    /** Remove barras finais repetidas sem compilar regex a cada chamada. */
    private static String stripTrailingSlash(String raw) {
        if (raw == null || raw.isEmpty()) {
            return raw == null ? "" : raw;
        }
        String s = raw;
        while (s.endsWith("/")) {
            s = s.substring(0, s.length() - 1);
        }
        return s;
    }

    private void enviar(String assunto, String corpoTexto) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, StandardCharsets.UTF_8.name());
            helper.setFrom(props.getFrom());
            helper.setTo(props.getDefaultRecipient());
            helper.setSubject(assunto);
            helper.setText(corpoTexto, false);
            mailSender.send(message);
            log.info("email_notificacao_enviado assunto={} para={}", assunto, props.getDefaultRecipient());
        } catch (MessagingException | MailException e) {
            log.warn("email_notificacao_falhou assunto={}", assunto, e);
        }
    }
}

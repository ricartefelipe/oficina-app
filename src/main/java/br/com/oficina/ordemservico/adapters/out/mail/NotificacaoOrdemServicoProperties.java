package br.com.oficina.ordemservico.adapters.out.mail;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.notification")
public class NotificacaoOrdemServicoProperties {

    /**
     * Quando falso, nenhum e-mail é enviado (útil em testes ou ambientes sem SMTP).
     */
    private boolean enabled = true;

    /**
     * Endereço From.
     */
    private String from = "noreply@oficina.local";

    /**
     * Destinatário quando o cadastro do cliente não possui e-mail (MVP).
     */
    private String defaultRecipient = "cliente-demo@mailhog.local";

    /**
     * URL base da API pública (sem barra final) para montar links ao cliente.
     */
    private String publicBaseUrl = "http://localhost:8080/api/public";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getDefaultRecipient() {
        return defaultRecipient;
    }

    public void setDefaultRecipient(String defaultRecipient) {
        this.defaultRecipient = defaultRecipient;
    }

    public String getPublicBaseUrl() {
        return publicBaseUrl;
    }

    public void setPublicBaseUrl(String publicBaseUrl) {
        this.publicBaseUrl = publicBaseUrl;
    }
}

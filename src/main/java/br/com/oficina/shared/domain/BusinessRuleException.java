package br.com.oficina.shared.domain;

/**
 * Excecao para violacao de regra de negocio.
 */
public class BusinessRuleException extends DomainException {

    public BusinessRuleException(String message) {
        super(message);
    }
}

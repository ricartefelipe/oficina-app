package br.com.oficina.shared.domain;

/**
 * Excecao de validacao de Value Objects e invariantes.
 */
public class ValidationException extends DomainException {

    public ValidationException(String message) {
        super(message);
    }
}

package br.com.oficina.shared.domain;

/**
 * Excecao base do dominio.
 *
 * Regra:
 * - Use para representar erros de negocio/validacao do dominio.
 * - Nao deve vazar stacktrace para clientes em producao (tratamento sera feito na Parte 3).
 */
public class DomainException extends RuntimeException {

    public DomainException(String message) {
        super(message);
    }

    public DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}

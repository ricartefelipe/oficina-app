package br.com.oficina.shared.validation;

import br.com.oficina.cadastros.cliente.domain.CpfCnpjValidator;
import br.com.oficina.shared.domain.Strings;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidCpfCnpjValidator implements ConstraintValidator<ValidCpfCnpj, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true; // deixe @NotBlank cuidar disso
        }
        String digits = Strings.onlyDigits(value);
        return CpfCnpjValidator.isValid(digits);
    }
}

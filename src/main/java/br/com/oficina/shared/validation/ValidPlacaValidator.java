package br.com.oficina.shared.validation;

import br.com.oficina.cadastros.veiculo.domain.PlacaValidator;
import br.com.oficina.shared.domain.Strings;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidPlacaValidator implements ConstraintValidator<ValidPlaca, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true; // deixe @NotBlank cuidar disso
        }
        String normalized = Strings.alnumUpper(value);
        if (normalized.length() != 7) {
            return false;
        }
        return PlacaValidator.isValid(normalized);
    }
}

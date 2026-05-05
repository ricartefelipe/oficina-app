"""Normalização e validação de CPF/CNPJ (somente dígitos verificadores), alinhado ao domínio Java."""


def only_digits(raw: str) -> str:
    return "".join(ch for ch in raw if ch.isdigit())


def is_all_same_digits(digits: str) -> bool:
    return len(digits) > 0 and digits == digits[0] * len(digits)


def is_valid_cpf(cpf: str) -> bool:
    if cpf is None or len(cpf) != 11 or is_all_same_digits(cpf):
        return False
    sum1 = sum(int(cpf[i]) * (10 - i) for i in range(9))
    d1 = 11 - (sum1 % 11)
    if d1 >= 10:
        d1 = 0
    sum2 = sum(int(cpf[i]) * (11 - i) for i in range(10))
    d2 = 11 - (sum2 % 11)
    if d2 >= 10:
        d2 = 0
    return d1 == int(cpf[9]) and d2 == int(cpf[10])


def is_valid_cnpj(cnpj: str) -> bool:
    if cnpj is None or len(cnpj) != 14 or is_all_same_digits(cnpj):
        return False
    w1 = [5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2]
    w2 = [6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2]
    sum1 = sum(int(cnpj[i]) * w1[i] for i in range(12))
    r1 = sum1 % 11
    d1 = 0 if r1 < 2 else 11 - r1
    sum2 = sum(int(cnpj[i]) * w2[i] for i in range(13))
    r2 = sum2 % 11
    d2 = 0 if r2 < 2 else 11 - r2
    return d1 == int(cnpj[12]) and d2 == int(cnpj[13])


def normalize_valid_digits(raw: str) -> str:
    """Retorna apenas dígitos após validar CPF (11) ou CNPJ (14). Levanta ValueError se inválido."""
    digits = only_digits(raw or "")
    if not digits:
        raise ValueError("cpfCnpj obrigatorio")
    if len(digits) == 11:
        if not is_valid_cpf(digits):
            raise ValueError("CPF invalido")
        return digits
    if len(digits) == 14:
        if not is_valid_cnpj(digits):
            raise ValueError("CNPJ invalido")
        return digits
    raise ValueError("cpfCnpj deve ter 11 (CPF) ou 14 (CNPJ) digitos")

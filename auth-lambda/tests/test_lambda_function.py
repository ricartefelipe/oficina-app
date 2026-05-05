import json
import os
from unittest.mock import patch

import pytest

import lambda_function


@pytest.fixture(autouse=True)
def env_secret():
    os.environ["JWT_SECRET"] = "uma-chave-de-teste-com-pelo-menos-32-bytes!!"
    os.environ["JWT_ISSUER"] = "https://oficina.local/auth/cpf"
    yield
    os.environ.pop("JWT_SECRET", None)


def test_cpf_invalido_retorna_400():
    event = {"body": json.dumps({"cpfCnpj": "11111111111"})}
    resp = lambda_function.lambda_handler(event, None)
    assert resp["statusCode"] == 400


def test_cliente_nao_encontrado_404():
    event = {"body": json.dumps({"cpfCnpj": "529.982.247-25"})}
    with patch.object(lambda_function, "_db_lookup_cliente", return_value=None):
        resp = lambda_function.lambda_handler(event, None)
    assert resp["statusCode"] == 404


def test_cliente_inativo_403():
    event = {"body": json.dumps({"cpfCnpj": "52998224725"})}
    row = {"id": "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee", "status": "INATIVO"}
    with patch.object(lambda_function, "_db_lookup_cliente", return_value=row):
        resp = lambda_function.lambda_handler(event, None)
    assert resp["statusCode"] == 403


def test_cliente_ativo_retorna_jwt():
    event = {"body": json.dumps({"cpfCnpj": "52998224725"})}
    row = {"id": "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee", "status": "ATIVO"}
    with patch.object(lambda_function, "_db_lookup_cliente", return_value=row):
        resp = lambda_function.lambda_handler(event, None)
    assert resp["statusCode"] == 200
    body = json.loads(resp["body"])
    assert body["token_type"] == "Bearer"
    assert body["cliente_id"] == row["id"]
    assert "access_token" in body


def test_sem_jwt_secret_500(monkeypatch):
    monkeypatch.delenv("JWT_SECRET", raising=False)
    event = {"body": json.dumps({"cpfCnpj": "52998224725"})}
    row = {"id": "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee", "status": "ATIVO"}
    with patch.object(lambda_function, "_db_lookup_cliente", return_value=row):
        resp = lambda_function.lambda_handler(event, None)
    assert resp["statusCode"] == 500

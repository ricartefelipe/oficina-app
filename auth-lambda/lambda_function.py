"""
AWS Lambda: autenticação Fase 3 — valida CPF/CNPJ, consulta cliente (PostgreSQL) e emite JWT HS256
consumido pela API Spring (`security.cpf-jwt`).
"""

from __future__ import annotations

import json
import logging
import os
from datetime import datetime, timedelta, timezone
from typing import Any

import jwt
import psycopg2
from psycopg2.extras import RealDictCursor

from cpf_cnpj import normalize_valid_digits

LOG = logging.getLogger()
LOG.setLevel(logging.INFO)


def _json_response(status: int, body: dict[str, Any]) -> dict[str, Any]:
    return {
        "statusCode": status,
        "headers": {"Content-Type": "application/json"},
        "body": json.dumps(body, ensure_ascii=False),
    }


def _parse_body(event: dict[str, Any]) -> dict[str, Any]:
    raw = event.get("body")
    if raw is None:
        return {}
    if isinstance(raw, dict):
        return raw
    try:
        return json.loads(raw or "{}")
    except json.JSONDecodeError:
        raise ValueError("Corpo JSON invalido")


def _issuer_secret_ttl() -> tuple[str, str, int]:
    issuer = os.environ.get("JWT_ISSUER", "https://oficina.local/auth/cpf").strip()
    secret = os.environ.get("JWT_SECRET", "").strip()
    if not secret:
        raise RuntimeError("JWT_SECRET nao configurado")
    try:
        ttl = int(os.environ.get("JWT_EXPIRATION_SECONDS", "3600"))
    except ValueError:
        ttl = 3600
    ttl = max(300, min(ttl, 86400))
    return issuer, secret, ttl


def _db_lookup_cliente(cpf_cnpj_digits: str) -> dict[str, Any] | None:
    conn = psycopg2.connect(
        host=os.environ.get("PGHOST", "localhost"),
        port=int(os.environ.get("PGPORT", "5432")),
        dbname=os.environ.get("PGDATABASE", "oficina"),
        user=os.environ.get("PGUSER", "oficina"),
        password=os.environ.get("PGPASSWORD", ""),
        connect_timeout=5,
    )
    try:
        with conn.cursor(cursor_factory=RealDictCursor) as cur:
            cur.execute(
                "SELECT id::text AS id, status::text AS status FROM clientes WHERE cpf_cnpj = %s",
                (cpf_cnpj_digits,),
            )
            row = cur.fetchone()
            return dict(row) if row else None
    finally:
        conn.close()


def _mint_token(cliente_id: str, cliente_status: str, issuer: str, secret: str, ttl_sec: int) -> str:
    now = datetime.now(timezone.utc)
    payload = {
        "sub": cliente_id,
        "cliente_id": cliente_id,
        "cliente_status": cliente_status,
        "authorities": ["ROLE_CLIENTE"],
        "iss": issuer,
        "iat": now,
        "exp": now + timedelta(seconds=ttl_sec),
    }
    return jwt.encode(payload, secret, algorithm="HS256")


def lambda_handler(event: dict[str, Any], context: Any) -> dict[str, Any]:
    try:
        body = _parse_body(event)
        raw_doc = body.get("cpfCnpj") or body.get("cpf_cnpj") or body.get("cpf") or body.get("documento")
        digits = normalize_valid_digits(str(raw_doc))
        issuer, secret, ttl_sec = _issuer_secret_ttl()
        row = _db_lookup_cliente(digits)
        if row is None:
            return _json_response(404, {"error": "Cliente nao encontrado"})
        status_upper = (row["status"] or "").strip().upper()
        if status_upper != "ATIVO":
            return _json_response(
                403,
                {"error": "Cliente inativo ou bloqueado", "cliente_status": row.get("status")},
            )
        cid = row["id"]
        token = _mint_token(cid, row["status"], issuer, secret, ttl_sec)
        return _json_response(
            200,
            {
                "access_token": token,
                "token_type": "Bearer",
                "expires_in": ttl_sec,
                "cliente_id": cid,
                "cliente_status": row["status"],
            },
        )
    except ValueError as e:
        return _json_response(400, {"error": str(e)})
    except RuntimeError as e:
        LOG.exception("config_invalida")
        return _json_response(500, {"error": str(e)})
    except Exception:
        LOG.exception("auth_lambda_erro")
        return _json_response(500, {"error": "Erro interno"})

locals {
  name_prefix = "${var.project_name}-auth-cpf"
}

data "aws_iam_policy_document" "lambda_assume" {
  statement {
    actions = ["sts:AssumeRole"]
    principals {
      type        = "Service"
      identifiers = ["lambda.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "lambda" {
  count              = var.lambda_role_arn == "" ? 1 : 0
  name               = "${local.name_prefix}-lambda-role"
  assume_role_policy = data.aws_iam_policy_document.lambda_assume.json
}

resource "aws_iam_role_policy_attachment" "lambda_logs" {
  count      = var.lambda_role_arn == "" ? 1 : 0
  role       = aws_iam_role.lambda[0].name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole"
}

resource "aws_iam_role_policy_attachment" "lambda_vpc" {
  count      = var.lambda_role_arn == "" && length(var.lambda_subnet_ids) > 0 ? 1 : 0
  role       = aws_iam_role.lambda[0].name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSLambdaVPCAccessExecutionRole"
}

locals {
  lambda_role_arn = var.lambda_role_arn != "" ? var.lambda_role_arn : aws_iam_role.lambda[0].arn
}

resource "aws_lambda_function" "auth" {
  function_name    = "${local.name_prefix}-fn"
  role             = local.lambda_role_arn
  handler          = "lambda_function.lambda_handler"
  runtime          = "python3.12"
  architectures    = ["x86_64"]
  timeout          = 30
  memory_size      = 256
  filename         = var.lambda_zip_path
  source_code_hash = filebase64sha256(var.lambda_zip_path)

  environment {
    variables = {
      PGHOST                   = var.pg_host
      PGPORT                   = tostring(var.pg_port)
      PGDATABASE               = var.pg_database
      PGUSER                   = var.pg_user
      PGPASSWORD               = var.pg_password
      JWT_ISSUER               = var.jwt_issuer
      JWT_SECRET               = var.jwt_secret
      JWT_EXPIRATION_SECONDS   = var.jwt_expiration_seconds
    }
  }

  dynamic "vpc_config" {
    for_each = length(var.lambda_subnet_ids) > 0 ? [1] : []
    content {
      subnet_ids         = var.lambda_subnet_ids
      security_group_ids = var.lambda_security_group_ids
    }
  }

  lifecycle {
    precondition {
      condition     = fileexists(var.lambda_zip_path)
      error_message = "Arquivo ZIP ausente. Execute ./auth-lambda/build-zip.sh na raiz do repositório."
    }
  }
}

resource "aws_apigatewayv2_api" "http" {
  name          = "${local.name_prefix}-http"
  protocol_type = "HTTP"
}

resource "aws_apigatewayv2_stage" "default" {
  api_id      = aws_apigatewayv2_api.http.id
  name        = "$default"
  auto_deploy = true
}

resource "aws_apigatewayv2_integration" "lambda" {
  api_id                 = aws_apigatewayv2_api.http.id
  integration_type       = "AWS_PROXY"
  integration_uri        = aws_lambda_function.auth.invoke_arn
  payload_format_version = "2.0"
}

resource "aws_apigatewayv2_route" "token" {
  api_id    = aws_apigatewayv2_api.http.id
  route_key = "POST /token"
  target    = "integrations/${aws_apigatewayv2_integration.lambda.id}"
}

resource "aws_lambda_permission" "apigw" {
  statement_id  = "AllowAPIGatewayInvoke"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.auth.function_name
  principal     = "apigateway.amazonaws.com"
  source_arn    = "${aws_apigatewayv2_api.http.execution_arn}/*/*"
}

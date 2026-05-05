output "http_invoke_url" {
  description = "URL base do HTTP API"
  value       = aws_apigatewayv2_stage.default.invoke_url
}

output "token_url" {
  description = "POST JSON {\"cpfCnpj\":\"...\"} — mesmo contrato do README auth-lambda"
  value       = "${aws_apigatewayv2_stage.default.invoke_url}/token"
}

output "lambda_function_name" {
  value = aws_lambda_function.auth.function_name
}

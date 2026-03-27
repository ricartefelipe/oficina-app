package br.com.oficina.cpf.api;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/cliente")
public class ClienteSessaoController {

    @GetMapping("/sessao")
    public Map<String, Object> sessao(@AuthenticationPrincipal Jwt jwt) {
        Object clienteId = jwt.getClaim("cliente_id");
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("clienteId", clienteId != null ? clienteId : jwt.getSubject());
        if (jwt.getIssuer() != null) {
            body.put("issuer", jwt.getIssuer().toString());
        }
        return body;
    }
}

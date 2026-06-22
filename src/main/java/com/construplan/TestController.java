package com.construplan;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @Value("${mysql-password:NOT_FOUND}")
    private String mysqlPassword;

    @GetMapping("/test-secret")
    public String testSecret() {
        if ("NOT_FOUND".equals(mysqlPassword)) {
            return "❌ No se pudo leer el secreto desde Key Vault";
        }
        return "✅ Secreto cargado correctamente";
    }
}

package com.empresa.integracao.servicenow;

import com.empresa.integracao.config.DatabaseConfig;
import com.empresa.integracao.model.Employee;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.logging.Logger;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ServiceNowClient {
    private static final Logger logger = Logger.getLogger(ServiceNowClient.class.getName());
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Configurações específicas para HTTPS no macOS
     */
    static {
        // Configurar propriedades SSL para macOS
        System.setProperty("jdk.httpclient.allowRestrictedHeaders", "connection");
        System.setProperty("jdk.internal.httpclient.disableHostnameVerification", "false");
        
        // Para desenvolvimento com certificados auto-assinados
        if (DatabaseConfig.getServiceNowUrl().contains("dev") || 
            DatabaseConfig.getServiceNowUrl().contains("test")) {
            System.setProperty("jdk.internal.httpclient.disableHostnameVerification", "true");
            logger.warning("Verificação de hostname SSL desativada para ambiente de desenvolvimento");
        }
    }
    
    public String createUser(Employee employee) throws Exception {
        logger.info("Criando usuário no ServiceNow: " + employee.getEmail());
        
        // ... resto do método mantido com logging adicional ...
    }
    
    public String getUserByEmail(String email) throws Exception {
        logger.info("Buscando usuário por email: " + email);
        
        // ... resto do método mantido ...
    }
}
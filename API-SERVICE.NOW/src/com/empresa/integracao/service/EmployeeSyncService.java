import java.util.List;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Serviço de integração adaptado para banco existente
 * Gerencia a sincronização com tratamento robusto de erros
 */
public class EmployeeSyncService {
    private final ServiceNowClient snClient;
    private final ObjectMapper objectMapper;
    
    public EmployeeSyncService() {
        this.snClient = new ServiceNowClient();
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * Executa a sincronização completa com tratamento de erros
     */
    public void syncEmployeesToServiceNow() {
        System.out.println("=== INICIANDO SINCRONIZAÇÃO ===");
        
        // Testa a conexão com o banco primeiro
        if (!DatabaseManager.testConnection()) {
            System.err.println("Falha na conexão com o banco de dados. Abortando sincronização.");
            return;
        }
        
        // Obtém funcionários para sincronizar
        List<Employee> employeesToSync = DatabaseManager.getEmployeesToSync();
        
        if (employeesToSync.isEmpty()) {
            System.out.println("Nenhum funcionário pendente de sincronização.");
            return;
        }
        
        System.out.println("Encontrados " + employeesToSync.size() + " funcionários para sincronizar.");
        
        int successCount = 0;
        int errorCount = 0;
        
        // Processa cada funcionário
        for (Employee employee : employeesToSync) {
            if (processEmployee(employee)) {
                successCount++;
            } else {
                errorCount++;
            }
        }
        
        System.out.println("=== SINCRONIZAÇÃO CONCLUÍDA ===");
        System.out.println("Sucessos: " + successCount);
        System.out.println("Erros: " + errorCount);
        System.out.println("Total processado: " + (successCount + errorCount));
    }
    
    /**
     * Processa um funcionário individualmente
     */
    private boolean processEmployee(Employee employee) {
        try {
            System.out.println("Processando: " + employee.getEmail());
            
            // Verifica se já existe no ServiceNow
            String existingUserResponse = snClient.getUserByEmail(employee.getEmail());
            JsonNode existingUsers = objectMapper.readTree(existingUserResponse).path("result");
            
            String sysId;
            if (existingUsers.size() > 0) {
                // Usuário já existe
                sysId = existingUsers.get(0).path("sys_id").asText();
                System.out.println("Usuário já existe no ServiceNow: " + sysId);
            } else {
                // Cria novo usuário
                String createResponse = snClient.createUser(employee);
                sysId = snClient.extractSysIdFromResponse(createResponse);
                System.out.println("Usuário criado com sucesso: " + sysId);
            }
            
            // Atualiza o banco com informações de sincronização
            employee.setServiceNowSysId(sysId);
            employee.setSyncedToServiceNow(true);
            DatabaseManager.updateEmployeeSyncStatus(employee);
            
            // Pequena pausa para não sobrecarregar a API
            Thread.sleep(300);
            
            return true;
            
        } catch (Exception e) {
            System.err.println("ERRO ao processar " + employee.getEmail() + ": " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Sincroniza um funcionário específico pelo ID
     */
    public boolean syncEmployeeById(int employeeId) {
        // Implementação para buscar um funcionário específico por ID
        // e sincronizá-lo individualmente
        return false;
    }
    
    /**
     * Verifica o status de sincronização de um funcionário
     */
    public String checkSyncStatus(String email) {
        try {
            String response = snClient.getUserByEmail(email);
            JsonNode users = objectMapper.readTree(response).path("result");
            
            if (users.size() > 0) {
                return "EXISTE no ServiceNow - SysID: " + users.get(0).path("sys_id").asText();
            } else {
                return "NÃO ENCONTRADO no ServiceNow";
            }
        } catch (Exception e) {
            return "ERRO ao verificar: " + e.getMessage();
        }
    }
}
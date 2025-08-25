import java.util.Scanner;

/**
 * Classe principal com menu interativo para gerenciar a sincronização
 */
public class Main {
    public static void main(String[] args) {
        try {
            System.out.println("=== SISTEMA DE INTEGRAÇÃO SERVICE-NOW ===");
            System.out.println("Conectando ao banco: " + DatabaseConfig.getDatabaseUrl());
            
            // Testa a conexão com o banco
            if (!DatabaseManager.testConnection()) {
                System.err.println("Não foi possível conectar ao banco de dados.");
                System.err.println("Verifique as configurações em application.properties");
                return;
            }
            
            System.out.println("Conexão com o banco estabelecida com sucesso!");
            
            // Menu interativo
            Scanner scanner = new Scanner(System.in);
            EmployeeSyncService syncService = new EmployeeSyncService();
            
            while (true) {
                System.out.println("\n=== MENU PRINCIPAL ===");
                System.out.println("1. Sincronizar todos os funcionários pendentes");
                System.out.println("2. Verificar status de um funcionário por email");
                System.out.println("3. Testar conexão com ServiceNow");
                System.out.println("4. Sair");
                System.out.print("Escolha uma opção: ");
                
                int choice;
                try {
                    choice = Integer.parseInt(scanner.nextLine());
                } catch (NumberFormatException e) {
                    System.out.println("Opção inválida!");
                    continue;
                }
                
                switch (choice) {
                    case 1:
                        syncService.syncEmployeesToServiceNow();
                        break;
                    case 2:
                        System.out.print("Digite o email do funcionário: ");
                        String email = scanner.nextLine();
                        String status = syncService.checkSyncStatus(email);
                        System.out.println("Status: " + status);
                        break;
                    case 3:
                        testServiceNowConnection();
                        break;
                    case 4:
                        System.out.println("Saindo...");
                        scanner.close();
                        return;
                    default:
                        System.out.println("Opção inválida!");
                }
            }
            
        } catch (Exception e) {
            System.err.println("Erro na execução: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void testServiceNowConnection() {
        try {
            ServiceNowClient client = new ServiceNowClient();
            String response = client.getUserByEmail("teste@empresa.com");
            System.out.println("Conexão com ServiceNow bem-sucedida!");
        } catch (Exception e) {
            System.err.println("Falha na conexão com ServiceNow: " + e.getMessage());
        }
    }
}
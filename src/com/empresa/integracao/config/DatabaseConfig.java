import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Classe de configuração centralizada para múltiplos bancos de dados
 * Lê configurações de um arquivo .properties para maior flexibilidade
 */
public class DatabaseConfig {
    private static final Properties props = new Properties();
    
    static {
        // Carrega as configurações do arquivo properties
        try (InputStream input = DatabaseConfig.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (input != null) {
                props.load(input);
            } else {
                // Configurações padrão se o arquivo não existir
                setDefaultProperties();
            }
        } catch (IOException e) {
            System.err.println("Erro ao carregar configurações: " + e.getMessage());
            setDefaultProperties();
        }
    }
    
    private static void setDefaultProperties() {
        // Configurações padrão para desenvolvimento
        props.setProperty("db.driver", "com.mysql.cj.jdbc.Driver");
        props.setProperty("db.url", "jdbc:mysql://localhost:3306/empresa_db");
        props.setProperty("db.username", "admin");
        props.setProperty("db.password", "password");
        props.setProperty("servicenow.url", "https://your-instance.service-now.com");
        props.setProperty("servicenow.username", "admin_user");
        props.setProperty("servicenow.password", "admin_password");
        props.setProperty("employee.table", "funcionarios");
        props.setProperty("employee.id.column", "id_funcionario");
        props.setProperty("employee.firstname.column", "nome");
        props.setProperty("employee.lastname.column", "sobrenome");
        props.setProperty("employee.email.column", "email");
        props.setProperty("employee.department.column", "departamento");
        props.setProperty("employee.position.column", "cargo");
        props.setProperty("employee.hiredate.column", "data_admissao");
        props.setProperty("employee.sync.column", "sincronizado_servicenow");
        props.setProperty("employee.syncdate.column", "data_sincronizacao");
        props.setProperty("employee.sysid.column", "servicenow_sys_id");
    }
    
    // Métodos de acesso para configurações do banco de dados
    public static String getDatabaseDriver() {
        return props.getProperty("db.driver");
    }
    
    public static String getDatabaseUrl() {
        return props.getProperty("db.url");
    }
    
    public static String getDatabaseUsername() {
        return props.getProperty("db.username");
    }
    
    public static String getDatabasePassword() {
        return props.getProperty("db.password");
    }
    
    // Métodos de acesso para configurações do ServiceNow
    public static String getServiceNowUrl() {
        return props.getProperty("servicenow.url");
    }
    
    public static String getServiceNowUsername() {
        return props.getProperty("servicenow.username");
    }
    
    public static String getServiceNowPassword() {
        return props.getProperty("servicenow.password");
    }
    
    // Métodos de acesso para mapeamento de colunas
    public static String getEmployeeTable() {
        return props.getProperty("employee.table");
    }
    
    public static String getEmployeeIdColumn() {
        return props.getProperty("employee.id.column");
    }
    
    public static String getEmployeeFirstNameColumn() {
        return props.getProperty("employee.firstname.column");
    }
    
    public static String getEmployeeLastNameColumn() {
        return props.getProperty("employee.lastname.column");
    }
    
    public static String getEmployeeEmailColumn() {
        return props.getProperty("employee.email.column");
    }
    
    public static String getEmployeeDepartmentColumn() {
        return props.getProperty("employee.department.column");
    }
    
    public static String getEmployeePositionColumn() {
        return props.getProperty("employee.position.column");
    }
    
    public static String getEmployeeHireDateColumn() {
        return props.getProperty("employee.hiredate.column");
    }
    
    public static String getEmployeeSyncColumn() {
        return props.getProperty("employee.sync.column");
    }
    
    public static String getEmployeeSyncDateColumn() {
        return props.getProperty("employee.syncdate.column");
    }
    
    public static String getEmployeeSysIdColumn() {
        return props.getProperty("employee.sysid.column");
    }
    
    /**
     * Verifica se a tabela de funcionários possui coluna de sincronização
     */
    public static boolean hasSyncColumn() {
        return props.containsKey("employee.sync.column");
    }
}
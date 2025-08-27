import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe genérica para gerenciar conexões com qualquer banco de dados JDBC
 * Adaptável à estrutura existente da base de dados
 */
public class DatabaseManager {
    
    /**
     * Carrega o driver JDBC baseado na configuração
     */
    static {
        try {
            Class.forName(DatabaseConfig.getDatabaseDriver());
            System.out.println("Driver JDBC carregado: " + DatabaseConfig.getDatabaseDriver());
        } catch (ClassNotFoundException e) {
            System.err.println("Erro ao carregar driver JDBC: " + e.getMessage());
        }
    }
    
    /**
     * Estabelece conexão com o banco de dados configurado
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
            DatabaseConfig.getDatabaseUrl(),
            DatabaseConfig.getDatabaseUsername(),
            DatabaseConfig.getDatabasePassword()
        );
    }
    
    /**
     * Recupera funcionários não sincronizados do banco existente
     * Adapta-se à estrutura de colunas configurada
     */
    public static List<Employee> getEmployeesToSync() {
        List<Employee> employees = new ArrayList<>();
        
        // Constrói query dinamicamente baseado nas configurações
        String sql;
        if (DatabaseConfig.hasSyncColumn()) {
            sql = "SELECT * FROM " + DatabaseConfig.getEmployeeTable() + 
                  " WHERE " + DatabaseConfig.getEmployeeSyncColumn() + " = 0 OR " + 
                  DatabaseConfig.getEmployeeSyncColumn() + " IS NULL";
        } else {
            sql = "SELECT * FROM " + DatabaseConfig.getEmployeeTable() + 
                  " WHERE " + DatabaseConfig.getEmployeeSysIdColumn() + " IS NULL";
        }
        
        System.out.println("Executando query: " + sql);
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Employee emp = mapResultSetToEmployee(rs);
                employees.add(emp);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao recuperar funcionários: " + e.getMessage());
        }
        
        return employees;
    }
    
    /**
     * Mapeia um ResultSet para objeto Employee baseado na configuração
     */
    private static Employee mapResultSetToEmployee(ResultSet rs) throws SQLException {
        Employee emp = new Employee();
        
        // Mapeia colunas configuradas para propriedades do Employee
        emp.setId(rs.getInt(DatabaseConfig.getEmployeeIdColumn()));
        emp.setFirstName(rs.getString(DatabaseConfig.getEmployeeFirstNameColumn()));
        emp.setLastName(rs.getString(DatabaseConfig.getEmployeeLastNameColumn()));
        emp.setEmail(rs.getString(DatabaseConfig.getEmployeeEmailColumn()));
        emp.setDepartment(rs.getString(DatabaseConfig.getEmployeeDepartmentColumn()));
        emp.setPosition(rs.getString(DatabaseConfig.getEmployeePositionColumn()));
        
        // Data de admissão (pode ser null)
        Timestamp hireDate = rs.getTimestamp(DatabaseConfig.getEmployeeHireDateColumn());
        if (hireDate != null && !rs.wasNull()) {
            emp.setHireDate(hireDate.toLocalDateTime());
        }
        
        // Campos de sincronização (podem não existir)
        try {
            boolean synced = rs.getBoolean(DatabaseConfig.getEmployeeSyncColumn());
            emp.setSyncedToServiceNow(synced);
        } catch (SQLException e) {
            // Coluna não existe, ignora
        }
        
        try {
            String sysId = rs.getString(DatabaseConfig.getEmployeeSysIdColumn());
            emp.setServiceNowSysId(sysId);
        } catch (SQLException e) {
            // Coluna não existe, ignora
        }
        
        return emp;
    }
    
    /**
     * Atualiza o funcionário com informações de sincronização
     * Cria colunas de sincronização se não existirem
     */
    public static void updateEmployeeSyncStatus(Employee employee) {
        // Primeiro verifica se as colunas de sincronização existem
        if (!checkSyncColumnsExist()) {
            // Se não existem, cria as colunas necessárias
            addSyncColumnsToTable();
        }
        
        // Atualiza os dados de sincronização
        String sql = "UPDATE " + DatabaseConfig.getEmployeeTable() + " SET " +
                    DatabaseConfig.getEmployeeSyncColumn() + " = ?, " +
                    DatabaseConfig.getEmployeeSyncDateColumn() + " = ?, " +
                    DatabaseConfig.getEmployeeSysIdColumn() + " = ? " +
                    "WHERE " + DatabaseConfig.getEmployeeIdColumn() + " = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setBoolean(1, true);
            pstmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setString(3, employee.getServiceNowSysId());
            pstmt.setInt(4, employee.getId());
            
            int rowsAffected = pstmt.executeUpdate();
            System.out.println("Funcionário " + employee.getId() + " atualizado. Linhas afetadas: " + rowsAffected);
            
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar status de sincronização: " + e.getMessage());
        }
    }
    
    /**
     * Verifica se as colunas de sincronização existem na tabela
     */
    private static boolean checkSyncColumnsExist() {
        String tableName = DatabaseConfig.getEmployeeTable();
        
        try (Connection conn = getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            
            // Verifica coluna de sincronização
            ResultSet rs = metaData.getColumns(null, null, tableName, DatabaseConfig.getEmployeeSyncColumn());
            boolean syncColumnExists = rs.next();
            
            // Verifica coluna de data de sincronização
            rs = metaData.getColumns(null, null, tableName, DatabaseConfig.getEmployeeSyncDateColumn());
            boolean syncDateColumnExists = rs.next();
            
            // Verifica coluna de sys_id
            rs = metaData.getColumns(null, null, tableName, DatabaseConfig.getEmployeeSysIdColumn());
            boolean sysIdColumnExists = rs.next();
            
            return syncColumnExists && syncDateColumnExists && sysIdColumnExists;
            
        } catch (SQLException e) {
            System.err.println("Erro ao verificar colunas: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Adiciona colunas de sincronização à tabela existente
     */
    private static void addSyncColumnsToTable() {
        String tableName = DatabaseConfig.getEmployeeTable();
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Adiciona coluna de sincronização se não existir
            if (!checkColumnExists(tableName, DatabaseConfig.getEmployeeSyncColumn())) {
                String sql = "ALTER TABLE " + tableName + " ADD " + 
                           DatabaseConfig.getEmployeeSyncColumn() + " BOOLEAN DEFAULT 0";
                stmt.executeUpdate(sql);
                System.out.println("Coluna " + DatabaseConfig.getEmployeeSyncColumn() + " adicionada.");
            }
            
            // Adiciona coluna de data de sincronização se não existir
            if (!checkColumnExists(tableName, DatabaseConfig.getEmployeeSyncDateColumn())) {
                String sql = "ALTER TABLE " + tableName + " ADD " + 
                           DatabaseConfig.getEmployeeSyncDateColumn() + " TIMESTAMP NULL";
                stmt.executeUpdate(sql);
                System.out.println("Coluna " + DatabaseConfig.getEmployeeSyncDateColumn() + " adicionada.");
            }
            
            // Adiciona coluna de sys_id se não existir
            if (!checkColumnExists(tableName, DatabaseConfig.getEmployeeSysIdColumn())) {
                String sql = "ALTER TABLE " + tableName + " ADD " + 
                           DatabaseConfig.getEmployeeSysIdColumn() + " VARCHAR(100) NULL";
                stmt.executeUpdate(sql);
                System.out.println("Coluna " + DatabaseConfig.getEmployeeSysIdColumn() + " adicionada.");
            }
            
        } catch (SQLException e) {
            System.err.println("Erro ao adicionar colunas: " + e.getMessage());
        }
    }
    
    /**
     * Verifica se uma coluna específica existe na tabela
     */
    private static boolean checkColumnExists(String tableName, String columnName) {
        try (Connection conn = getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet rs = metaData.getColumns(null, null, tableName, columnName);
            return rs.next();
        } catch (SQLException e) {
            return false;
        }
    }
    
    /**
     * Método para testar a conexão com o banco de dados
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn.isValid(5); // Testa com timeout de 5 segundos
        } catch (SQLException e) {
            System.err.println("Falha na conexão com o banco: " + e.getMessage());
            return false;
        }
    }
}

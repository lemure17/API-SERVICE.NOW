import java.time.LocalDateTime;

/**
 * Modelo de funcionário - Versão simplificada
 */
public class Employee {
    public int id;
    public String firstName;
    public String lastName;
    public String email;
    public String department;
    public String position;
    public LocalDateTime hireDate;
    public boolean synced;
    public String serviceNowId;
    
    public Employee() {}
    
    public Employee(int id, String firstName, String lastName, String email, 
                   String department, String position) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.department = department;
        this.position = position;
        this.hireDate = LocalDateTime.now();
        this.synced = false;
    }
    
    @Override
    public String toString() {
        return firstName + " " + lastName + " (" + email + ")";
    }
}
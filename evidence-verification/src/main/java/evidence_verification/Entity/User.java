package evidence_verification.Entity;



import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    private String password;

    private String role;

    @Column(name = "email_verified")
    private Boolean emailVerified = false;

    public User() {
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        if (role == null || role.trim().isEmpty() || "VERIFIER".equalsIgnoreCase(role)) {
            return Role.INVESTIGATOR.name();
        }
        return role.toUpperCase();
    }

    public void setRole(String role) {
        if (role != null && Role.isValid(role)) {
            this.role = role.toUpperCase();
        } else {
            this.role = role;
        }
    }

    public boolean isEmailVerified() {
        return emailVerified == null || emailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    @Column(name = "failed_login_attempts")
    private Integer failedLoginAttempts = 0;

    @Column(name = "lockout_expiration")
    private java.time.LocalDateTime lockoutExpiration;

    public int getFailedLoginAttempts() {
        return failedLoginAttempts == null ? 0 : failedLoginAttempts;
    }

    public void setFailedLoginAttempts(int failedLoginAttempts) {
        this.failedLoginAttempts = failedLoginAttempts;
    }

    public java.time.LocalDateTime getLockoutExpiration() {
        return lockoutExpiration;
    }

    public void setLockoutExpiration(java.time.LocalDateTime lockoutExpiration) {
        this.lockoutExpiration = lockoutExpiration;
    }

    public boolean isAccountLocked() {
        if (lockoutExpiration == null) {
            return false;
        }
        if (java.time.LocalDateTime.now().isAfter(lockoutExpiration)) {
            return false;
        }
        return true;
    }
}
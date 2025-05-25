package entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Token {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String token;
    private String phoneNumber;
    private boolean revoked = false;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;

    public Token() {
    }

    public Token(String token, String phoneNumber, LocalDateTime createdAt, LocalDateTime expiresAt, boolean revoked) {
        this.token = token;
        this.phoneNumber = phoneNumber;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.revoked = revoked;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setRevoked(boolean revoked) {
        this.revoked = revoked;
    }

    public Long getId() {
        return id;
    }

    public String getToken() {
        return token;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
}


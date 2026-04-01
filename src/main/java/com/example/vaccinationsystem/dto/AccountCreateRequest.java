package com.example.vaccinationsystem.dto;

public class AccountCreateRequest {
    private String username;
    private String password;
    private String email;
    private String authority;
    private String name;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAuthority() { return authority; }
    public void setAuthority(String authority) { this.authority = authority; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}

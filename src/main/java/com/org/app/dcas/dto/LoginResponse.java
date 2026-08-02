package com.org.app.dcas.dto;

public class LoginResponse {
    private String token;
    private Long userId;
    private String username;
    private String firstName;
    private String lastName;
    private String companyName;

    public LoginResponse(String token, Long userId, String username, String firstName, String lastName, String companyName) {
        this.token = token;
        this.userId = userId;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.companyName = companyName;
    }

    public String getToken() { return token; }
    public Long getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getCompanyName() { return companyName; }
}

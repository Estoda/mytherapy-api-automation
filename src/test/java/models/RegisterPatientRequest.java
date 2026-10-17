package models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RegisterPatientRequest
{
    private String fullName;
    private String email;
    private String password;
    private String phone;
    private int gender;
    private String dateOfBirth;
    private boolean isAnonymous;

    public RegisterPatientRequest(String fullName, String email, String password,
                                  String phone, int gender, String dateOfBirth,
                                  boolean isAnonymous)
    {
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.isAnonymous = isAnonymous;
    }

    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getPhone() { return phone; }
    public int getGender() { return gender; }
    public String getDateOfBirth() { return dateOfBirth; }

    @JsonProperty("isAnonymous")
    public boolean isAnonymous() { return isAnonymous; }
}
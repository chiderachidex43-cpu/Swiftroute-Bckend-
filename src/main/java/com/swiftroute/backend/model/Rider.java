package com.swiftroute.backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Rider {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String email;

    private String phone;

    private String profilePhotoUrl;

    @Enumerated(EnumType.STRING)
    private RiderStatus status;

    @Enumerated(EnumType.STRING)
    private VehicleType preferredVehicle;
public Long getId(){
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
     public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
     public String getProfilePhotoUrl() {
        return profilePhotoUrl;
    }

    public void setProfilePhotoUrl(String profilePhotoUrl) {
        this.profilePhotoUrl = profilePhotoUrl;
    }

     public RiderStatus getStatus() {
        return status;
    }

    public void setStatus(RiderStatus status) {
        this.status = status;
    }
     public VehicleType getPreferredVehicle() {
        return preferredVehicle;
    }

    public void setPreferredVehicle(VehicleType preferredVehicle) {
        this.preferredVehicle = preferredVehicle;
    }
}
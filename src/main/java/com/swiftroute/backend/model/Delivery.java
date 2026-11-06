package com.swiftroute.backend.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "deliveries")
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tracking_number", unique = true, length = 20)
        private String trackingNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rider_id")
    private User rider;

    @Column(nullable = false)
    private String pickupLocation;

    @Column(nullable = false)
    private String destination;

    @Column(nullable = false)
    private String packageDetails;

    @Column(name = "pickup_country_code", nullable = false, length = 2)
    private String pickupCountryCode;

    @Column(nullable = false)
    private String vehicleType;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "receiver")
private String receiver;

@Column(name = "receiver_phone")
private String receiverPhone;

@Column(name = "receiver_email",  length = 255)
private String receiverEmail;

@Column(name = "goods_name")
private String goodsName;

@Column(name = "quantity")
private Integer quantity;

@Column(name = "delivery_note", length = 2000)
private String note;

@Column(name = "delivery_speed")
private String speed;

@Column(name = "pickup_latitude")
private Double pickupLatitude;

@Column(name = "pickup_longitude")
private Double pickupLongitude;

@Column(name = "destination_latitude")
private Double destinationLatitude;

@Column(name = "destination_longitude")
private Double destinationLongitude;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryStatus status = DeliveryStatus.REQUESTED;
    
    @Column(name = "rider_latitude")
private Double riderLatitude;

@Column(name = "rider_longitude")
private Double riderLongitude;

@Column(name = "rider_location_updated_at")
private LocalDateTime riderLocationUpdatedAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }
    public String getTrackingNumber() {
    return trackingNumber;
}

public void setTrackingNumber(String trackingNumber) {
    this.trackingNumber = trackingNumber;
}

    public User getCustomer() {
        return customer;
    }

    public void setCustomer(User customer) {
        this.customer = customer;
    }

    public User getRider() {
        return rider;
    }

    public void setRider(User rider) {
        this.rider = rider;
    }

    public String getPickupLocation() {
        return pickupLocation;
    }

    public void setPickupLocation(String pickupLocation) {
        this.pickupLocation = pickupLocation;
    }

    public String getPickupCountryCode() {
    return pickupCountryCode;
}

public void setPickupCountryCode(String pickupCountryCode) {
    this.pickupCountryCode = pickupCountryCode;
}

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getPackageDetails() {
        return packageDetails;
    }

    public void setPackageDetails(String packageDetails) {
        this.packageDetails = packageDetails;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public DeliveryStatus getStatus() {
        return status;
    }

    public void setStatus(DeliveryStatus status) {
        this.status = status;
    }

    public String getReceiver() {
    return receiver;
}

public void setReceiver(String receiver) {
    this.receiver = receiver;
}

public String getReceiverPhone() {
    return receiverPhone;
}

public void setReceiverPhone(String receiverPhone) {
    this.receiverPhone = receiverPhone;
}

public String getReceiverEmail() {
    return receiverEmail;
}

public void setReceiverEmail(String receiverEmail) {
    this.receiverEmail = receiverEmail;
}

public String getGoodsName() {
    return goodsName;
}

public void setGoodsName(String goodsName) {
    this.goodsName = goodsName;
}

public Integer getQuantity() {
    return quantity;
}

public void setQuantity(Integer quantity) {
    this.quantity = quantity;
}

public String getNote() {
    return note;
}

public void setNote(String note) {
    this.note = note;
}

public String getSpeed() {
    return speed;
}

public void setSpeed(String speed) {
    this.speed = speed;
}

public Double getPickupLatitude() {
    return pickupLatitude;
}

public void setPickupLatitude(Double pickupLatitude) {
    this.pickupLatitude = pickupLatitude;
}

public Double getPickupLongitude() {
    return pickupLongitude;
}

public void setPickupLongitude(Double pickupLongitude) {
    this.pickupLongitude = pickupLongitude;
}

public Double getDestinationLatitude() {
    return destinationLatitude;
}

public void setDestinationLatitude(Double destinationLatitude) {
    this.destinationLatitude = destinationLatitude;
}

public Double getDestinationLongitude() {
    return destinationLongitude;
}

public void setDestinationLongitude(Double destinationLongitude) {
    this.destinationLongitude = destinationLongitude;
}

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public Double getRiderLatitude() {
    return riderLatitude;
}

public void setRiderLatitude(Double riderLatitude) {
    this.riderLatitude = riderLatitude;
}

public Double getRiderLongitude() {
    return riderLongitude;
}

public void setRiderLongitude(Double riderLongitude) {
    this.riderLongitude = riderLongitude;
}

public LocalDateTime getRiderLocationUpdatedAt() {
    return riderLocationUpdatedAt;
}

public void setRiderLocationUpdatedAt(
        LocalDateTime riderLocationUpdatedAt) {
    this.riderLocationUpdatedAt = riderLocationUpdatedAt;
}
}
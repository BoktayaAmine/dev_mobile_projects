package ma.ensa.project.model;


import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Position {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double latitude;
    private double longitude;
    private LocalDateTime date;
    private String imei;

    // Constructors
    public Position() {}

    public Position(Long id, double latitude, double longitude, LocalDateTime date, String imei) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.date = date;
        this.imei = imei;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }
}

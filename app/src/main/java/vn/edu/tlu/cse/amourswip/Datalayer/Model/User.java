package vn.edu.tlu.cse.amourswip.datalayer.model;

import java.util.List;

public class User {
    private String uid;
    private String email;
    private String name; // Thêm thuộc tính name
    private String gender;
    private String preferredGender;
    private String dateOfBirth;
    private List<String> photos;
    private boolean locationEnabled;
    private double latitude;
    private double longitude;
    private String religion;
    private String residence;
    private String educationLevel;
    private String occupation;

    // Constructor rỗng (yêu cầu bởi Firebase)
    public User() {
    }

    // Constructor đầy đủ
    public User(String uid, String email, String name, String gender, String preferredGender, String dateOfBirth,
                List<String> photos, boolean locationEnabled, double latitude, double longitude,
                String religion, String residence, String educationLevel, String occupation) {
        this.uid = uid;
        this.email = email;
        this.name = name;
        this.gender = gender;
        this.preferredGender = preferredGender;
        this.dateOfBirth = dateOfBirth;
        this.photos = photos;
        this.locationEnabled = locationEnabled;
        this.latitude = latitude;
        this.longitude = longitude;
        this.religion = religion;
        this.residence = residence;
        this.educationLevel = educationLevel;
        this.occupation = occupation;
    }

    // Getters và Setters
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPreferredGender() {
        return preferredGender;
    }

    public void setPreferredGender(String preferredGender) {
        this.preferredGender = preferredGender;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public List<String> getPhotos() {
        return photos;
    }

    public void setPhotos(List<String> photos) {
        this.photos = photos;
    }

    public boolean isLocationEnabled() {
        return locationEnabled;
    }

    public void setLocationEnabled(boolean locationEnabled) {
        this.locationEnabled = locationEnabled;
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

    public String getReligion() {
        return religion;
    }

    public void setReligion(String religion) {
        this.religion = religion;
    }

    public String getResidence() {
        return residence;
    }

    public void setResidence(String residence) {
        this.residence = residence;
    }

    public String getEducationLevel() {
        return educationLevel;
    }

    public void setEducationLevel(String educationLevel) {
        this.educationLevel = educationLevel;
    }

    public String getOccupation() {
        return occupation;
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }
}
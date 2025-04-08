package vn.edu.tlu.cse.amourswip.Datalayer.Model;

import java.util.List;

public class User {
    private String id;
    private String name;
    private int age;
    private List<String> photos;
    private String bio;
    private String email;

    public User() {
    }

    public User(String id, String name, int age, List<String> photos, String bio, String email) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.photos = photos;
        this.bio = bio;
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public List<String> getPhotos() {
        return photos;
    }

    public void setPhotos(List<String> photos) {
        this.photos = photos;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}


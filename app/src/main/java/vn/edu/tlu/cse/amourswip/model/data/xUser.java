package vn.edu.tlu.cse.amourswip.model.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class xUser implements Parcelable {
    private String uid;
    private String email;
    private String name;
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
    private String description;

    public xUser() {}

    public xUser(String uid, String email, String name, String gender, String preferredGender, String dateOfBirth,
                 List<String> photos, boolean locationEnabled, double latitude, double longitude,
                 String religion, String residence, String educationLevel, String occupation, String description) {
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
        this.description = description;
    }

    protected xUser(Parcel in) {
        uid = in.readString();
        email = in.readString();
        name = in.readString();
        gender = in.readString();
        preferredGender = in.readString();
        dateOfBirth = in.readString();
        photos = in.createStringArrayList();
        locationEnabled = in.readByte() != 0;
        latitude = in.readDouble();
        longitude = in.readDouble();
        religion = in.readString();
        residence = in.readString();
        educationLevel = in.readString();
        occupation = in.readString();
        description = in.readString();
    }

    public static final Creator<xUser> CREATOR = new Creator<xUser>() {
        @Override
        public xUser createFromParcel(Parcel in) {
            return new xUser(in);
        }

        @Override
        public xUser[] newArray(int size) {
            return new xUser[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(uid);
        dest.writeString(email);
        dest.writeString(name);
        dest.writeString(gender);
        dest.writeString(preferredGender);
        dest.writeString(dateOfBirth);
        dest.writeStringList(photos);
        dest.writeByte((byte) (locationEnabled ? 1 : 0));
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeString(religion);
        dest.writeString(residence);
        dest.writeString(educationLevel);
        dest.writeString(occupation);
        dest.writeString(description);
    }

    @Override
    public int describeContents() {
        return 0;
    }

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

    public int getAge() {
        if (dateOfBirth == null || dateOfBirth.isEmpty()) {
            return 0;
        }
        try {
            // Hỗ trợ nhiều định dạng ngày sinh
            SimpleDateFormat sdf;
            if (dateOfBirth.matches("\\d{4}-\\d{2}-\\d{2}")) {
                sdf = new SimpleDateFormat("yyyy-MM-dd");
            } else if (dateOfBirth.matches("\\d{2}/\\d{2}/\\d{4}")) {
                sdf = new SimpleDateFormat("dd/MM/yyyy");
            } else {
                return 0; // Định dạng không hợp lệ
            }
            Date birthDate = sdf.parse(dateOfBirth);
            Calendar birthCal = Calendar.getInstance();
            birthCal.setTime(birthDate);
            Calendar today = Calendar.getInstance();
            int age = today.get(Calendar.YEAR) - birthCal.get(Calendar.YEAR);
            if (today.get(Calendar.DAY_OF_YEAR) < birthCal.get(Calendar.DAY_OF_YEAR)) {
                age--;
            }
            return age;
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
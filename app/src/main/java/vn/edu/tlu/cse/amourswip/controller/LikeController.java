package vn.edu.tlu.cse.amourswip.controller;

import android.content.Intent;
import android.os.Bundle;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import vn.edu.tlu.cse.amourswip.R;
import vn.edu.tlu.cse.amourswip.model.data.User;
import vn.edu.tlu.cse.amourswip.model.repository.LikeRepository;
import vn.edu.tlu.cse.amourswip.view.activity.profile.ProfileMyFriendActivity;
import vn.edu.tlu.cse.amourswip.view.fragment.LikeFragment;
import android.location.Location;

public class LikeController {

    private final LikeFragment fragment;
    private final LikeRepository likeRepository;
    private List<User> usersWhoLikedMe;
    private List<User> usersILiked;
    private List<User> filteredUsersWhoLikedMe;
    private List<User> filteredUsersILiked;
    private boolean isLikesTabSelected;
    private double currentLatitude;
    private double currentLongitude;
    private String lastUserIdWhoLikedMe;
    private String lastUserIdILiked;
    private static final int PAGE_SIZE = 10;
    private double maxDistance = Double.MAX_VALUE;
    private int minAge = 0;
    private int maxAge = Integer.MAX_VALUE;
    private String residenceFilter = null;
    private Set<String> userIdsWhoLikedMe = new HashSet<>();
    private Set<String> userIdsILiked = new HashSet<>();

    public LikeController(LikeFragment fragment) {
        this.fragment = fragment;
        this.likeRepository = new LikeRepository();
        this.usersWhoLikedMe = new ArrayList<>();
        this.usersILiked = new ArrayList<>();
        this.filteredUsersWhoLikedMe = new ArrayList<>();
        this.filteredUsersILiked = new ArrayList<>();
        this.isLikesTabSelected = true;
        this.lastUserIdWhoLikedMe = null;
        this.lastUserIdILiked = null;
        loadCurrentUserLocation();
    }

    private void loadCurrentUserLocation() {
        likeRepository.getCurrentUserLocation(new LikeRepository.OnLocationListener() {
            @Override
            public void onSuccess(double latitude, double longitude) {
                currentLatitude = latitude;
                currentLongitude = longitude;
                fragment.setCurrentLocation(latitude, longitude);
                loadUsersWhoLikedMe();
                loadUsersILiked();
            }

            @Override
            public void onError(String error) {
                fragment.showError(error);
                currentLatitude = 0;
                currentLongitude = 0;
                fragment.setCurrentLocation(0, 0);
                loadUsersWhoLikedMe();
                loadUsersILiked();
            }
        });
    }

    public void loadUsersWhoLikedMe() {
        likeRepository.getUsersWhoLikedMe(new LikeRepository.OnResultListener() {
            @Override
            public void onSuccess(List<User> users) {
                if (lastUserIdWhoLikedMe == null) {
                    usersWhoLikedMe.clear();
                    userIdsWhoLikedMe.clear();
                }
                for (User user : users) {
                    if (!userIdsWhoLikedMe.contains(user.getUid())) {
                        usersWhoLikedMe.add(user);
                        userIdsWhoLikedMe.add(user.getUid());
                    }
                }
                if (!users.isEmpty()) {
                    lastUserIdWhoLikedMe = users.get(users.size() - 1).getUid();
                }
                applyFilters();
                if (isLikesTabSelected) {
                    fragment.updateUserList(filteredUsersWhoLikedMe);
                }
            }

            @Override
            public void onEmpty() {
                if (lastUserIdWhoLikedMe == null && isLikesTabSelected) {
                    fragment.updateUserList(new ArrayList<>());
                }
            }

            @Override
            public void onError(String error) {
                fragment.showError(error);
            }

            @Override
            public void onLoading() {
                // Hiển thị loading indicator nếu cần
            }
        }, lastUserIdWhoLikedMe, PAGE_SIZE);
    }

    public void loadUsersILiked() {
        likeRepository.getUsersILiked(new LikeRepository.OnResultListener() {
            @Override
            public void onSuccess(List<User> users) {
                if (lastUserIdILiked == null) {
                    usersILiked.clear();
                    userIdsILiked.clear();
                }
                for (User user : users) {
                    if (!userIdsILiked.contains(user.getUid())) {
                        usersILiked.add(user);
                        userIdsILiked.add(user.getUid());
                    }
                }
                if (!users.isEmpty()) {
                    lastUserIdILiked = users.get(users.size() - 1).getUid();
                }
                applyFilters();
                if (!isLikesTabSelected) {
                    fragment.updateUserList(filteredUsersILiked);
                }
            }

            @Override
            public void onEmpty() {
                if (lastUserIdILiked == null && !isLikesTabSelected) {
                    fragment.updateUserList(new ArrayList<>());
                }
            }

            @Override
            public void onError(String error) {
                fragment.showError(error);
            }

            @Override
            public void onLoading() {
                // Hiển thị loading indicator nếu cần
            }
        }, lastUserIdILiked, PAGE_SIZE);
    }

    public void loadMoreUsers() {
        if (isLikesTabSelected) {
            loadUsersWhoLikedMe();
        } else {
            loadUsersILiked();
        }
    }

    public void onLikesTabClicked() {
        isLikesTabSelected = true;
        lastUserIdWhoLikedMe = null;
        loadUsersWhoLikedMe();
        fragment.updateTabSelection(true);
    }

    public void onLikedTabClicked() {
        isLikesTabSelected = false;
        lastUserIdILiked = null;
        loadUsersILiked();
        fragment.updateTabSelection(false);
    }

    public void onUserClicked(User user) {
        Bundle bundle = new Bundle();
        bundle.putString("friendId", user.getUid());
        fragment.getNavController().navigate(R.id.action_likeFragment_to_profileMyFriendActivity, bundle);
    }

    public void applyFilter(double maxDistance, int minAge, int maxAge, String residence) {
        this.maxDistance = maxDistance;
        this.minAge = minAge;
        this.maxAge = maxAge;
        this.residenceFilter = residence != null && !residence.isEmpty() ? residence.toLowerCase() : null;
        applyFilters();
        fragment.updateUserList(isLikesTabSelected ? filteredUsersWhoLikedMe : filteredUsersILiked);
    }

    private void applyFilters() {
        filteredUsersWhoLikedMe = filterUsers(usersWhoLikedMe);
        filteredUsersILiked = filterUsers(usersILiked);
    }

    private List<User> filterUsers(List<User> users) {
        List<User> filteredList = new ArrayList<>(users);
        filteredList = filteredList.stream()
                .filter(user -> {
                    // Lọc theo khoảng cách
                    double distance = calculateDistance(user.getLatitude(), user.getLongitude());
                    if (distance > maxDistance) {
                        return false;
                    }
                    // Lọc theo tuổi
                    int age = user.getAge();
                    if (age < minAge || age > maxAge) {
                        return false;
                    }
                    // Lọc theo nơi ở
                    if (residenceFilter != null) {
                        String residence = user.getResidence() != null ? user.getResidence().toLowerCase() : "";
                        if (!residence.contains(residenceFilter)) {
                            return false;
                        }
                    }
                    return true;
                })
                .collect(Collectors.toList());
        return filteredList;
    }

    private double calculateDistance(double latitude, double longitude) {
        if (currentLatitude == 0 || currentLongitude == 0) {
            return Double.MAX_VALUE;
        }

        Location currentLocation = new Location("");
        currentLocation.setLatitude(currentLatitude);
        currentLocation.setLongitude(currentLongitude);

        Location userLocation = new Location("");
        userLocation.setLatitude(latitude);
        userLocation.setLongitude(longitude);

        float distanceInMeters = currentLocation.distanceTo(userLocation);
        return distanceInMeters / 1000; // Chuyển sang km
    }

    // Getter để lấy danh sách người dùng (dùng khi khôi phục trạng thái)
    public List<User> getUsersWhoLikedMe() {
        return usersWhoLikedMe;
    }

    public List<User> getUsersILiked() {
        return usersILiked;
    }

    public boolean isLikesTabSelected() {
        return isLikesTabSelected;
    }

    public double getMaxDistance() {
        return maxDistance;
    }

    public int getMinAge() {
        return minAge;
    }

    public int getMaxAge() {
        return maxAge;
    }

    public String getResidenceFilter() {
        return residenceFilter;
    }
}
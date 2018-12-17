package com.futurice.android.reservator.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class CurrentUser {
    private static CurrentUser sharedInstance = null;

    public static CurrentUser getInstance() {
        if (sharedInstance == null) {
            sharedInstance = new CurrentUser();
        }
        return sharedInstance;
    }

    private static User user = new User();

    private CurrentUser() {}

    public void processJson(String json) {
        ObjectMapper mapper = new ObjectMapper();

        User[] users = new User[0];
        try {
            users = mapper.readValue(json, User[].class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        /*
        List<User> users = new ArrayList<>();
        try {
            users = mapper.readValue(result, new TypeReference<List<User>>(){});
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        user = users[0];
    }

    public String getUsername() {
        return user.username;
    }

    // TODO: add photo from Google account
    public static class User {
        private int id;
        private String username;
        private String firstName;
        private String lastName;

        @JsonCreator
        public User(@JsonProperty("id") int id,
                    @JsonProperty("username") String username,
                    @JsonProperty("first_name") String firstName,
                    @JsonProperty("last_name") String lastName) {
            this.id = id;
            this.username = username;
            this.firstName = firstName;
            this.lastName = lastName;
        }

        public User() { this(0, null, null, null); }

        public String getUsername() { return username; }
        public void setUsername(String username) {
            this.username = username;
        }

        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() { return lastName; }
        public void setLastName(String lastName) {
            this.lastName = lastName;
        }
    }
}

package com.example.demo8;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class UsersTest {
    private Users users;

    @Before
    public void setUp() {
        users = new Users("John", "Doe", "123456789", "john@example.com", "http://example.com/image.jpg","online","John1234","Admin","token","John Doe", "123456789","Company Name");
    }


    @Test
    public void testSettersAndGetters() {
        assertEquals("John", users.getFirstName());
        assertEquals("Doe", users.getLastName());
        assertEquals("123456789", users.getMobile());
        assertEquals("john@example.com", users.getEmail());
        assertEquals("http://example.com/image.jpg", users.getImageURL());
        assertEquals("online", users.getStatus());
        assertEquals("John1234", users.getId());
        assertEquals("Admin", users.getTyping());
        assertEquals("token", users.getToken());
        assertEquals("John Doe", users.getFullname());
        assertEquals("123456789", users.getPassword());
        assertEquals("Company Name", users.getCompanyName());

    }

}
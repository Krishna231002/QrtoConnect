package com.example.demo8;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class GroupMessageModelTest {

    GroupMessageModel groupMessageModel;
    @Before
    public void setUp() {

       groupMessageModel = new GroupMessageModel();
        groupMessageModel.setMessage("Message");
        groupMessageModel.setType("Image");
        groupMessageModel.setDate("2023-03-17T12:00:00Z");
        groupMessageModel.setSenderId("senderId");
        groupMessageModel.setName("Robin");
        // Add members to the group if necessary
    }

    @Test
    public void testGettersAndSetters() {
        // Test getters
        assertEquals("senderId", groupMessageModel.getSenderId());
        assertEquals("Message", groupMessageModel.getMessage());
        assertEquals("Image", groupMessageModel.getType());
        assertEquals("2023-03-17T12:00:00Z", groupMessageModel.getDate());
        assertEquals("Robin", groupMessageModel.getName());

    }

}
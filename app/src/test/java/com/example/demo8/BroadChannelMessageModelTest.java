package com.example.demo8;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class BroadChannelMessageModelTest {

   BroadChannelMessageModel broadcastChannelMessageModel;
    @Before
    public void setUp() {

        broadcastChannelMessageModel = new BroadChannelMessageModel();
        broadcastChannelMessageModel.setMessage("Message");
        broadcastChannelMessageModel.setType("Image");
        broadcastChannelMessageModel.setDate("2023-03-17T12:00:00Z");
        broadcastChannelMessageModel.setSenderId("senderId");
        broadcastChannelMessageModel.setName("Robin");
        // Add members to the group if necessary
    }

    @Test
    public void testGettersAndSetters() {
        // Test getters
        assertEquals("senderId", broadcastChannelMessageModel.getSenderId());
        assertEquals("Message", broadcastChannelMessageModel.getMessage());
        assertEquals("Image", broadcastChannelMessageModel.getType());
        assertEquals("2023-03-17T12:00:00Z", broadcastChannelMessageModel.getDate());
        assertEquals("Robin", broadcastChannelMessageModel.getName());

    }


}
package com.example.demo8;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.os.Parcel;



import org.junit.Before;
import org.junit.Test;

public class AllBroadcastChannelModelTest {

    private AllBroadcastChannelModel broadcastChannelModel;

    @Before
    public void setUp() {
        broadcastChannelModel = new AllBroadcastChannelModel();
        broadcastChannelModel.setChannelId("1");
        broadcastChannelModel.setAdminId("admin123");
        broadcastChannelModel.setAdminName("Admin");
        broadcastChannelModel.setRole("admin");
        broadcastChannelModel.setChannelImage("group_image.png");
        broadcastChannelModel.setChannelName("Test Group");
        broadcastChannelModel.setToken("token");
    }

    @Test
    public void testGettersAndSetters() {
        assertEquals("1", broadcastChannelModel.getChannelId());
        assertEquals("admin123", broadcastChannelModel.getAdminId());
        assertEquals("Admin", broadcastChannelModel.getAdminName());
        assertEquals("admin", broadcastChannelModel.getRole());
        assertEquals("group_image.png", broadcastChannelModel.getChannelImage());
        assertEquals("Test Group", broadcastChannelModel.getChannelName());
        assertEquals("token", broadcastChannelModel.getToken());
    }

    @Test
    public void testParcelable() {
        Parcel parcel = mock(Parcel.class);

        // Set up the expected values for the parcel
        when(parcel.readString())
                .thenReturn("admin123")
                .thenReturn("Admin")
                .thenReturn("1")
                .thenReturn("group_image.png")
                .thenReturn("Test Group")
                .thenReturn("admin");

        when(parcel.readByte()).thenReturn((byte) 1);

        // Write the broadcastChannelModel object to the mocked Parcel object
        broadcastChannelModel.writeToParcel(parcel, 0);

        // Reset the parcel for reading
        when(parcel.dataPosition()).thenReturn(0);

        // Reconstruct the AllBroadcastChannelModel object from the mocked Parcel object
        AllBroadcastChannelModel actualChannel = AllBroadcastChannelModel.CREATOR.createFromParcel(parcel);

        // Verify that the reconstructed object matches the expected values
       assertEquals(broadcastChannelModel.getChannelId(), actualChannel.getChannelId());
        assertEquals(broadcastChannelModel.getAdminId(), actualChannel.getAdminId());
        assertEquals(broadcastChannelModel.getAdminName(), actualChannel.getAdminName());
        assertEquals(broadcastChannelModel.getRole(), actualChannel.getRole());
        assertEquals(broadcastChannelModel.getChannelImage(), actualChannel.getChannelImage());
        assertEquals(broadcastChannelModel.getChannelName(), actualChannel.getChannelName());

    }
}

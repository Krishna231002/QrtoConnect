package com.example.demo8;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.os.Parcel;

import com.example.demo8.MyModels.GroupLastMessageModel;
import com.example.demo8.MyModels.GroupMemberModel;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GroupModelsTest {

    private GroupModels originalGroup;

    @Before
    public void setUp() {

        originalGroup = new GroupModels();
        originalGroup.setId("1");
        originalGroup.setAdminId("admin123");
        originalGroup.setAdminName("Admin");
        originalGroup.setCreateAt("2024-03-17");
        originalGroup.setImage("group_image.png");
        originalGroup.setName("Test Group");

        // Add members to the group if necessary
        List<GroupMemberModel> members = new ArrayList<>();
        members.add(new GroupMemberModel("member1", "Admin","token"));
        members.add(new GroupMemberModel("member2", "member","token"));
        originalGroup.setMembers(members);

        // Set the last message model if needed
        GroupLastMessageModel lastMessageModel = new GroupLastMessageModel();
        lastMessageModel.setMessage("Hello, world!");
        lastMessageModel.setDate("2023-03-17T12:00:00Z");
        originalGroup.setLastMessageModel(lastMessageModel);
        originalGroup.setAdmin(true);
    }

    @Test
    public void testGettersAndSetters() {
        // Test getters
        assertEquals("1", originalGroup.getId());
        assertEquals("admin123", originalGroup.getAdminId());
        assertEquals("Admin", originalGroup.getAdminName());
        assertEquals("2024-03-17", originalGroup.getCreateAt());
        assertEquals("group_image.png", originalGroup.getImage());
        assertEquals("Test Group", originalGroup.getName());
        assertTrue(originalGroup.isAdmin());

        // Test setters
        originalGroup.setId("2");
        originalGroup.setAdminId("56789");
       originalGroup.setAdminName("New Admin");
        originalGroup.setCreateAt("2025-03-17");
        originalGroup.setImage("new_group_image.png");
       originalGroup.setName("New Test Group");
        originalGroup.setAdmin(false);

        assertEquals("2", originalGroup.getId());
        assertEquals("56789", originalGroup.getAdminId());
        assertEquals("New Admin",originalGroup.getAdminName());
        assertEquals("2025-03-17", originalGroup.getCreateAt());
        assertEquals("new_group_image.png", originalGroup.getImage());
        assertEquals("New Test Group", originalGroup.getName());
        assertFalse(originalGroup.isAdmin());
    }


    @Test
    public void testParcelable() {
        // Mock the Parcel object
        Parcel parcel = mock(Parcel.class);

        // Set up the expected values for the GroupModels object
        GroupModels expectedGroup = new GroupModels();
        expectedGroup.setId("1");
        expectedGroup.setAdminId("admin123");
        expectedGroup.setAdminName("Admin");
        expectedGroup.setCreateAt("2024-03-17");
        expectedGroup.setImage("group_image.png");
        expectedGroup.setName("Test Group");

        List<GroupMemberModel> member = new ArrayList<>();
        member.add(new GroupMemberModel("Member 1", "member1","token"));
        member.add(new GroupMemberModel("Member 2", "member2","token"));
        expectedGroup.setMembers(member);

        GroupLastMessageModel lastMessageModel = new GroupLastMessageModel();
        lastMessageModel.setMessage("Hello, world!");
        lastMessageModel.setDate("2023-03-17T12:00:00Z");
        expectedGroup.setLastMessageModel(lastMessageModel);
        expectedGroup.setAdmin(true);

        // Set up the expected values for the parcel
        when(parcel.readString()).thenReturn("1")
                .thenReturn("admin123")
                .thenReturn("Admin")
                .thenReturn("2024-03-17")
                .thenReturn("group_image.png")
                .thenReturn("Test Group");

        when(parcel.createTypedArrayList(GroupMemberModel.CREATOR)).thenReturn((ArrayList<GroupMemberModel>) member);

        when(parcel.readParcelable(GroupLastMessageModel.class.getClassLoader())).thenReturn(lastMessageModel);

        when(parcel.readByte()).thenReturn((byte) 1);

        // Write the GroupModels object to the mocked Parcel object
        expectedGroup.writeToParcel(parcel, 0);

        // Reset the parcel for reading
        parcel.setDataPosition(0);

        // Reconstruct the GroupModels object from the mocked Parcel object
        GroupModels actualGroup = GroupModels.CREATOR.createFromParcel(parcel);

        // Verify that the reconstructed GroupModels object matches the expected values
        assertEquals(expectedGroup.getId(), actualGroup.getId());
        assertEquals(expectedGroup.getAdminId(), actualGroup.getAdminId());
        assertEquals(expectedGroup.getAdminName(), actualGroup.getAdminName());
        assertEquals(expectedGroup.getCreateAt(), actualGroup.getCreateAt());
        assertEquals(expectedGroup.getImage(), actualGroup.getImage());
        assertEquals(expectedGroup.getName(), actualGroup.getName());
        assertEquals(expectedGroup.getMembers(), actualGroup.getMembers());
        assertEquals(expectedGroup.getLastMessageModel(), actualGroup.getLastMessageModel());
        assertEquals(expectedGroup.isAdmin(), actualGroup.isAdmin());
    }
}
package com.example.demo8;

public class Constants {

    //db name
    public static final String DATABASE_NAME = "CONTACT_DB";
    //database version
    public static final int DATABASE_VERSION = 2;

    //table creation..
    public static final String TABLE_NAME = "CONTACT_TABLE";

    public static final String MY_TABLE_NAME = "MY_DETAILS";
    public static final String C_ID = "ID";
    public static final String C_IMAGE = "IMAGE";
    public static final String C_FIRST_NAME = "FIRST_NAME";
    public static final String C_LAST_NAME = "LAST_NAME";
    public static final String C_MOBILE_NUMBER = "MOBILE_NUMBER";
    public static final String C_EMAIL = "EMAIL";
    public static final String C_COMPANY_NAME = "COMPANY_NAME";
    public static final String C_NOTES = "NOTES";
    public static final String C_FAVORITE = "FAVORITE" ;

    public static final String MY_ID = "ID";
    public static final String MY_IMAGE = "IMAGE" ;
    public static final String MY_FIRST_NAME = "FIRST_NAME" ;
    public static final String MY_LAST_NAME = "LAST_NAME" ;
    public static final String MY_MOBILE_NUMBER = "MOBILE_NUMBER" ;
    public static final String MY_EMAIL = "EMAIL" ;
    public static final String MY_COMPANY_NAME = "COMPANY_NAME" ;


    //query for table creation..
    public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "("
            + C_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + C_IMAGE + " TEXT, "
            + C_FIRST_NAME + " TEXT, "
            + C_LAST_NAME + " TEXT, "
            + C_MOBILE_NUMBER + " TEXT, "
            + C_EMAIL + " TEXT, "
            + C_COMPANY_NAME + " TEXT, "
            + C_NOTES + " TEXT ,"
            + C_FAVORITE + " INTEGER DEFAULT 0 "
            + ")";

    public static final String CREATE_TABLE_MY_DETAILS  = "CREATE TABLE " + MY_TABLE_NAME + "("
            + MY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT ,"
            + MY_IMAGE + " TEXT ,"
            + MY_FIRST_NAME + " TEXT ,"
            + MY_LAST_NAME + " TEXT ,"
            + MY_MOBILE_NUMBER + " TEXT ,"
            + MY_EMAIL + " TEXT ,"
            + MY_COMPANY_NAME + " TEXT "
            + ")";


}

package com.example.demo8;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

import java.util.ArrayList;

// class for database helper..
public class DbHelper extends SQLiteOpenHelper {
    public DbHelper(Context context) {
        super(context,Constants.DATABASE_NAME,null,Constants.DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        //create table on database..
        db.execSQL(Constants.CREATE_TABLE);

        db.execSQL(Constants.CREATE_TABLE_MY_DETAILS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        //upgrade table if any structure change in db

        //drop table if exists..
        db.execSQL("DROP TABLE IF EXISTS "+Constants.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS "+Constants.MY_TABLE_NAME);

        //create table again..
        onCreate(db);
    }

    //Insert Function for insert data..
    public long insertContact(String image,String firstName,String lastName,String mobilNumber,String email,String companyName,String notes){

        //get writable database to write data on db..
        SQLiteDatabase db = this.getWritableDatabase();

        //create ContentValue class object save data..
        ContentValues contentValues = new ContentValues();

        //id will save automatically
        contentValues.put(Constants.C_IMAGE,image);
        contentValues.put(Constants.C_FIRST_NAME,firstName);
        contentValues.put(Constants.C_LAST_NAME,lastName);
        contentValues.put(Constants.C_MOBILE_NUMBER,mobilNumber);
        contentValues.put(Constants.C_EMAIL,email);
        contentValues.put(Constants.C_COMPANY_NAME,companyName);
        contentValues.put(Constants.C_NOTES,notes);

        //insert data in row,it will return id of record..
        long id = db.insert(Constants.TABLE_NAME,null,contentValues);

        //close db
        db.close();

        return id;
    }

    public void insertMyData(Context context){
        SQLiteDatabase db = getWritableDatabase();

        ContentValues contentValues = new ContentValues();

        // Set a default image resource ID here.
        int defaultImageResource = R.drawable.account_circle_icon;

        // Convert the default image resource to a URI.
        Uri defaultImageUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                "://" + context.getResources().getResourcePackageName(defaultImageResource)
                + '/' + context.getResources().getResourceTypeName(defaultImageResource)
                + '/' + context.getResources().getResourceEntryName(defaultImageResource));

        contentValues.put(Constants.MY_IMAGE, String.valueOf(defaultImageUri));
        contentValues.put(Constants.MY_FIRST_NAME,"Edit Name");
        contentValues.put(Constants.MY_LAST_NAME,"");
        contentValues.put(Constants.MY_MOBILE_NUMBER,"Edit Mobile Number");
        contentValues.put(Constants.MY_EMAIL,"Edit Email");
        contentValues.put(Constants.MY_COMPANY_NAME,"Edit Company Name");

        db.insert(Constants.MY_TABLE_NAME,null,contentValues);

        db.close();

    }

    public void deleteMyData(){
       SQLiteDatabase db = getWritableDatabase();

       db.delete(Constants.MY_TABLE_NAME,Constants.C_ID + " =? ",new String[]{"1"});

       db.close();
    }

    //Update Function for update data..
    public void updateContact(String id,String image,String firstName,String lastName,String mobilNumber,String email,String companyName,String notes){

        //get writable database to write data on db..
        SQLiteDatabase db = this.getWritableDatabase();

        //create ContentValue class object save data..
        ContentValues contentValues = new ContentValues();

        contentValues.put(Constants.C_IMAGE,image);
        contentValues.put(Constants.C_FIRST_NAME,firstName);
        contentValues.put(Constants.C_LAST_NAME,lastName);
        contentValues.put(Constants.C_MOBILE_NUMBER,mobilNumber);
        contentValues.put(Constants.C_EMAIL,email);
        contentValues.put(Constants.C_COMPANY_NAME,companyName);
        contentValues.put(Constants.C_NOTES,notes);

        //update data in row,it will return id of record..
        db.update(Constants.TABLE_NAME,contentValues,Constants.C_ID+" =? ",new String[]{id});

        //close db
        db.close();

    }

    //upgrade my details..
    public void updateMyData(String image,String firstName,String lastName,String mobile,String email,String companyName){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(Constants.MY_IMAGE,image);
        contentValues.put(Constants.MY_FIRST_NAME,firstName);
        contentValues.put(Constants.MY_LAST_NAME,lastName);
        contentValues.put(Constants.MY_MOBILE_NUMBER,mobile);
        contentValues.put(Constants.MY_EMAIL,email);
        contentValues.put(Constants.MY_COMPANY_NAME,companyName);

        db.update(Constants.MY_TABLE_NAME,contentValues,Constants.MY_ID + " =? ", new String[]{"1"});

        db.close();
    }
    //Delete function for delete data by id..
    public void deleteContact(String id){
        //get writable db
        SQLiteDatabase db = getWritableDatabase();

        //delete query
        db.delete(Constants.TABLE_NAME,Constants.C_ID + " =? ",new String[]{id});

        db.close();
    }

    //Favorite function..
    public ArrayList<ModelContact> favorite(){

        ArrayList<ModelContact> arrayList = new ArrayList<>();

        int fav = 1;
        //sql command query for filter favorite contact..
        String favoriteQuery = " SELECT * FROM " + Constants.TABLE_NAME+ " WHERE " + Constants.C_FAVORITE + " = 1" + " ORDER BY " + Constants.C_FIRST_NAME + " COLLATE NOCASE ASC ";

        //get readable db..
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(favoriteQuery,null);

        if(cursor.moveToFirst()){
            do{
                ModelContact modelContact = new ModelContact(
                        ""+cursor.getInt(cursor.getColumnIndexOrThrow(Constants.C_ID)),
                        ""+cursor.getString(cursor.getColumnIndexOrThrow(Constants.C_IMAGE)),
                        ""+cursor.getString(cursor.getColumnIndexOrThrow(Constants.C_FIRST_NAME)),
                        ""+cursor.getString(cursor.getColumnIndexOrThrow(Constants.C_LAST_NAME)),
                        ""+cursor.getString(cursor.getColumnIndexOrThrow(Constants.C_MOBILE_NUMBER)),
                        ""+cursor.getString(cursor.getColumnIndexOrThrow(Constants.C_EMAIL)),
                        ""+cursor.getString(cursor.getColumnIndexOrThrow(Constants.C_COMPANY_NAME)),
                        ""+cursor.getString(cursor.getColumnIndexOrThrow(Constants.C_NOTES)),
                        ""//+cursor.getString(cursor.getColumnIndexOrThrow(Constants.C_FAVORITE))
                );
                arrayList.add(modelContact);
            }while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return arrayList;
    }

    //get data
    public ArrayList<ModelContact> getAllData(){
        //create arrayList
        ArrayList<ModelContact> arrayList = new ArrayList<>();
        //sql command query for find data
        String selectQuery = "SELECT * FROM " + Constants.TABLE_NAME+ " ORDER BY " + Constants.C_FIRST_NAME + " COLLATE NOCASE ASC ";

        //get readable db
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery,null);

        if(cursor.moveToFirst()){
            do{
                ModelContact modelContact = new ModelContact(
                        ""+cursor.getInt(cursor.getColumnIndexOrThrow(Constants.C_ID)),
                        ""+cursor.getString(cursor.getColumnIndexOrThrow(Constants.C_IMAGE)),
                        ""+cursor.getString(cursor.getColumnIndexOrThrow(Constants.C_FIRST_NAME)),
                        ""+cursor.getString(cursor.getColumnIndexOrThrow(Constants.C_LAST_NAME)),
                        ""+cursor.getString(cursor.getColumnIndexOrThrow(Constants.C_MOBILE_NUMBER)),
                        ""+cursor.getString(cursor.getColumnIndexOrThrow(Constants.C_EMAIL)),
                        ""+cursor.getString(cursor.getColumnIndexOrThrow(Constants.C_COMPANY_NAME)),
                        ""+cursor.getString(cursor.getColumnIndexOrThrow(Constants.C_NOTES)),
                        ""//cursor.getString(cursor.getColumnIndexOrThrow(Constants.C_FAVORITE))
                );
                arrayList.add(modelContact);
            }while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return arrayList;
    }


    //search data in sql db
    public ArrayList<ModelContact> getSearchContact(String query){

        //it will return arraylist of modelContact class..
        ArrayList<ModelContact> contactsList = new ArrayList<>();

        //get readable db..
        SQLiteDatabase db = getReadableDatabase();

        //query for search
        String queryToSearch = "SELECT * FROM "+Constants.TABLE_NAME + " WHERE " + Constants.C_FIRST_NAME + " LIKE '%" + query + "%'";

        Cursor cursor = db.rawQuery(queryToSearch,null);

        if (cursor.moveToFirst()){
            do {
                ModelContact modelContact = new ModelContact(
                        ""+cursor.getInt(cursor.getColumnIndexOrThrow(Constants.C_ID)),
                        ""+cursor.getString(cursor.getColumnIndexOrThrow(Constants.C_IMAGE)),
                        ""+cursor.getString(cursor.getColumnIndexOrThrow(Constants.C_FIRST_NAME)),
                        ""+cursor.getString(cursor.getColumnIndexOrThrow(Constants.C_LAST_NAME)),
                        ""+cursor.getString(cursor.getColumnIndexOrThrow(Constants.C_MOBILE_NUMBER)),
                        ""+cursor.getString(cursor.getColumnIndexOrThrow(Constants.C_EMAIL)),
                        ""+cursor.getString(cursor.getColumnIndexOrThrow(Constants.C_COMPANY_NAME)),
                        ""+cursor.getString(cursor.getColumnIndexOrThrow(Constants.C_NOTES)),
                        ""//+cursor.getString(cursor.getColumnIndexOrThrow(Constants.C_FAVORITE))
                );
                contactsList.add(modelContact);
            }while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return contactsList;
    }
}

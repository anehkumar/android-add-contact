package com.contacts;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.MenuItem;

import com.contacts.adapter.ContactListAdapter;
import com.contacts.model.ContactModel;

import java.util.ArrayList;

public class ContactList extends AppCompatActivity {

    RecyclerView friends;
    ArrayList<ContactModel> contactModelArrayList;
    ContactListAdapter contactListAdapter;
    LinearLayoutManager linearLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_list);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        friends = (RecyclerView)findViewById(R.id.friends);

        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        friends.setLayoutManager(linearLayoutManager);
        contactModelArrayList = new ArrayList<>();

        contactListAdapter = new ContactListAdapter(this, contactModelArrayList, this);
        friends.setAdapter(contactListAdapter);

        getAllContacts();

    }

    private ArrayList getAllContacts() {
        ArrayList<String> nameList = new ArrayList<>();
        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        if ((cur != null ? cur.getCount() : 0) > 0) {
            while (cur != null && cur.moveToNext()) {
                String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY));

                ContactModel contactModel = new ContactModel();
                contactModel.setName(name);
                contactModel.setId(id);
                String phoneNo = "";

                nameList.add(name);
                if (cur.getInt(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                    Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{id}, null);
                    while (pCur.moveToNext()) {
                        phoneNo = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        Log.e("phone", phoneNo+" phone");
                        if(phoneNo.length() > 0)
                            contactModel.setPhone(phoneNo);
                    }
                    pCur.close();
                }

                contactModelArrayList.add(contactModel);
            }

            contactListAdapter.notifyDataSetChanged();
        }
        if (cur != null) {
            cur.close();
        }
        return nameList;
    }

    /*
     * Delete contact by it's display name.
     * */
    public void deleteContact(String id)
    {
        long rawContactId = Long.parseLong(id);

        ContentResolver contentResolver = getContentResolver();

        Uri dataContentUri = ContactsContract.Data.CONTENT_URI;

        // Create data table where clause.
        StringBuffer dataWhereClauseBuf = new StringBuffer();
        dataWhereClauseBuf.append(ContactsContract.Data.RAW_CONTACT_ID);
        dataWhereClauseBuf.append(" = ");
        dataWhereClauseBuf.append(rawContactId);

        contentResolver.delete(dataContentUri, dataWhereClauseBuf.toString(), null);

         Uri rawContactUri = ContactsContract.RawContacts.CONTENT_URI;

        // Create raw_contacts table where clause.
        StringBuffer rawContactWhereClause = new StringBuffer();
        rawContactWhereClause.append(ContactsContract.RawContacts._ID);
        rawContactWhereClause.append(" = ");
        rawContactWhereClause.append(rawContactId);

        // Delete raw_contacts table related data.
        contentResolver.delete(rawContactUri, rawContactWhereClause.toString(), null);

        Uri contactUri = ContactsContract.Contacts.CONTENT_URI;

        // Create contacts table where clause.
        StringBuffer contactWhereClause = new StringBuffer();
        contactWhereClause.append(ContactsContract.Contacts._ID);
        contactWhereClause.append(" = ");
        contactWhereClause.append(rawContactId);

        // Delete raw_contacts table related data.
        contentResolver.delete(contactUri, contactWhereClause.toString(), null);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }
}

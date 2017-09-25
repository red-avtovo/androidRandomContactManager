package com.example.admin.myapplication;

import android.Manifest;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.countContactsButton).setOnClickListener(countContactsHandler(this));
        findViewById(R.id.clearContactsButton).setOnClickListener(clearContactsHandler(this));
        findViewById(R.id.addContactsButton).setOnClickListener(addContactsHandler(this));
    }

    @NonNull
    private View.OnClickListener countContactsHandler(final MainActivity mainActivity) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int permissionCheck = ContextCompat.checkSelfPermission(mainActivity,
                        Manifest.permission.READ_CONTACTS);
                if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(mainActivity,
                            new String[]{Manifest.permission.READ_CONTACTS}, 1);

                } else {
                    TextView contactsAmount = (TextView) findViewById(R.id.contactsAmount);
                    Cursor phones = getContentResolver().query(Phone.CONTENT_URI, null, null, null, null);
                    int i = 0;
                    if (phones != null) {
                        while (phones.moveToNext()) {
                            i++;
                        }
                        phones.close();
                    }
                    contactsAmount.setText(String.format("%s", i));
                }
            }
        };
    }

    @NonNull
    private View.OnClickListener clearContactsHandler(final MainActivity mainActivity) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int readPermissionCheck = ContextCompat.checkSelfPermission(mainActivity,
                        Manifest.permission.READ_CONTACTS);
                int writePermissionCheck = ContextCompat.checkSelfPermission(mainActivity,
                        Manifest.permission.WRITE_CONTACTS);
                if (readPermissionCheck != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(mainActivity,
                            new String[]{Manifest.permission.READ_CONTACTS}, 1);

                } else if (writePermissionCheck != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(mainActivity,
                            new String[]{Manifest.permission.WRITE_CONTACTS}, 1);

                } else {
                    Cursor phones = getContentResolver().query(Phone.CONTENT_URI, null, null, null, null);
                    ContentResolver contentResolver = mainActivity.getContentResolver();
                    while (phones.moveToNext()) {
                        String lookupKey = phones.getString(phones.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
                        Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, lookupKey);
                        contentResolver.delete(uri, null, null);
                    }
                    phones.close();
                    countContacts();
                }

            }
        };
    }

    private void countContacts() {
        findViewById(R.id.countContactsButton).performClick();
    }

    @NonNull
    private View.OnClickListener addContactsHandler(final MainActivity mainActivity) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int permissionCheck = ContextCompat.checkSelfPermission(mainActivity,
                        Manifest.permission.WRITE_CONTACTS);
                if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(mainActivity,
                            new String[]{Manifest.permission.WRITE_CONTACTS}, 1);

                } else {
                    EditText contactsToAdd = (EditText) findViewById(R.id.contactsToAddEdit);
                    if (contactsToAdd != null) {
                        try {
                            final  int contacts = Integer.parseInt(contactsToAdd.getText().toString());

                            ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
                            TextView progressText = (TextView) findViewById(R.id.progressText);
                            TextView contactsAmount = (TextView) findViewById(R.id.contactsAmount);

                            ContactInserterAsyncTask task = new ContactInserterAsyncTask();
                            task.setProgressBar(progressBar);
                            task.setProgressText(progressText);
                            task.setCurrentAmount(Integer.parseInt(contactsAmount.getText().toString()));
                            task.setAmount(contacts);
                            task.setMainActivity(mainActivity);
                            task.setCountButton(findViewById(R.id.countContactsButton));
                            task.execute();

                        } catch (NumberFormatException e) {
                            CharSequence text = "Enter the amount, please!";
                            int duration = Toast.LENGTH_SHORT;

                            Toast toast = Toast.makeText(mainActivity, text, duration);
                            toast.show();
                        }
                    }
                }
            }
        };
    }
}

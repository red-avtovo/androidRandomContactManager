package com.example.admin.myapplication;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.Context;
import android.content.OperationApplicationException;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by admin on 19.04.17.
 */

public class ContactInserterAsyncTask extends AsyncTask<Void, Integer, Void> {

    private ProgressBar bar;
    private TextView progressText;
    private int amount;
    private MainActivity mainActivity;
    private View countButton;
    private int currentAmount;

    public void setProgressBar(ProgressBar bar) {
        this.bar = bar;
    }

    @Override
    protected Void doInBackground(Void... params) {
        insertNContacts();
        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        if (this.bar != null) {
            bar.setProgress(values[0]);
        }
        if (this.progressText != null) {
            progressText.setText(String.format("%s%%", values[0]));
        }
        /** Sleep this thread for 100 ms */
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        countButton.performClick();
    }

    public void setProgressText(TextView progressText) {
        this.progressText = progressText;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    private void insertNContacts() {
        for (int i = 0; i < amount; i++) {
            writePhoneContact(
                    String.format("Generated user %s", (i + 1 + currentAmount)),
                    String.format("+49%010d", i + currentAmount),
                    mainActivity);
            int percent = ((i + 1) * 100) / amount;
            publishProgress(percent);
        }
    }

    public void writePhoneContact(String displayName, String number, Context cntx /*App or Activity Ctx*/) {
        Context contetx = cntx; //Application's context or Activity's context
        String strDisplayName = displayName; // Name of the Person to add
        String strNumber = number; //number of the person to add with the Contact

        ArrayList<ContentProviderOperation> cntProOper = new ArrayList<ContentProviderOperation>();
        int contactIndex = cntProOper.size();//ContactSize

        //Newly Inserted contact
        // A raw contact will be inserted ContactsContract.RawContacts table in contacts database.
        cntProOper.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)//Step1
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null).build());

        //Display name will be inserted in ContactsContract.Data table
        cntProOper.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)//Step2
                .withValueBackReference(ContactsContract.RawContacts.Data.RAW_CONTACT_ID, contactIndex)
                .withValue(ContactsContract.RawContacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, strDisplayName) // Name of the contact
                .build());
        //Mobile number will be inserted in ContactsContract.Data table
        cntProOper.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)//Step 3
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, contactIndex)
                .withValue(ContactsContract.RawContacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, strNumber) // Number to be added
                .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE).build()); //Type like HOME, MOBILE etc
        try {
            // We will do batch operation to insert all above data
            //Contains the output of the app of a ContentProviderOperation.
            //It is sure to have exactly one of uri or count set
            ContentProviderResult[] contentProresult = null;
            contentProresult = contetx.getContentResolver().applyBatch(ContactsContract.AUTHORITY, cntProOper); //apply above data insertion into contacts list
        } catch (RemoteException exp) {
            //logs;
        } catch (OperationApplicationException exp) {
            //logs
        }
    }

    public void setMainActivity(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public void setCountButton(View countButton) {
        this.countButton = countButton;
    }

    public void setCurrentAmount(int currentAmount) {
        this.currentAmount = currentAmount;
    }
}

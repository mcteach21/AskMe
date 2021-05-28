package mc.apps.voice.util;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class Query {
    private static final String DISPLAY_NAME = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ?
            ContactsContract.Contacts.DISPLAY_NAME_PRIMARY : ContactsContract.Contacts.DISPLAY_NAME;
    //private static final String FILTER = DISPLAY_NAME + " LIKE 'Yasmine%'"; // " NOT LIKE '%@%'";
    private static final String ORDER = String.format("%1$s COLLATE NOCASE", DISPLAY_NAME);

    @SuppressLint("InlinedApi")
    private static final String[] PROJECTION = {
            ContactsContract.Contacts._ID,
            DISPLAY_NAME,
            ContactsContract.Contacts.HAS_PHONE_NUMBER
    };
    public static class Contact{
        public String name;
        public String email;
        public String phoneNumber;
        @Override
        public String toString() {
            return name + ", tél.=" + phoneNumber +", email=" + email;
        }
    }

    public static void searchContact(Context context,String search_name, INotifyListener listener){
        new MyAsyncTask<String, List<Contact>>() {
            @Override
            public List<Contact> doInBackground(String filter_name) {
                List<Contact> contacts = new ArrayList<>();

                String filter = DISPLAY_NAME + " LIKE '"+filter_name+"%'"; // " NOT LIKE '%@%'";
                try {
                    ContentResolver cr = context.getContentResolver();
                    Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, PROJECTION, filter, null, ORDER);
                    if (cursor != null && cursor.moveToFirst()) {

                        do {
                            String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                            String name = cursor.getString(cursor.getColumnIndex(DISPLAY_NAME));
                            Integer hasPhone = cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

                            String email = null;
                            Cursor ce = cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
                                    ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?", new String[]{id}, null);
                            if (ce != null && ce.moveToFirst()) {
                                email = ce.getString(ce.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
                                ce.close();
                            }
                            String phone = null;
                            if (hasPhone > 0) {
                                Cursor cp = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{id}, null);
                                if (cp != null && cp.moveToFirst()) {
                                    phone = cp.getString(cp.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                                    cp.close();
                                }
                            }

                            // if the user user has an email or phone then add it to contacts
                            if ((!TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
                                    && !email.equalsIgnoreCase(name)) || (!TextUtils.isEmpty(phone))) {
                                Contact contact = new Contact();
                                contact.name = name;
                                contact.email  = email;
                                contact.phoneNumber  = phone;
                                contacts.add(contact);
                            }

                        } while (cursor.moveToNext());
                        cursor.close();
                    }
                } catch (Exception ex) {
                    Log.i("samples", "error: "+ex);
                }

                return contacts;
            }

            @Override
            public void onPostExecute(@Nullable List<Contact> result) {
                //..
            }
            @Override
            public void onPreExecute() { }
            @Override
            public void onProgress(int progress) {}

        }.execute(search_name, listener);
    }
}

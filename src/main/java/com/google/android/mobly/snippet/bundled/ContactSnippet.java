/*
 * Copyright (C) 2024 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.android.mobly.snippet.bundled;

import android.accounts.Account;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.OperationApplicationException;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.ContactsContract;
import androidx.test.platform.app.InstrumentationRegistry;
import com.google.android.mobly.snippet.Snippet;
import com.google.android.mobly.snippet.rpc.Rpc;
import java.util.ArrayList;

/* Snippet class for operating contacts. */
public class ContactSnippet implements Snippet {

  private static final String GOOGLE_ACCOUNT_TYPE = "com.google";
  private final Context context = InstrumentationRegistry.getInstrumentation().getContext();

  @Rpc(description =
      "Add a contact with the given email address. A Google account need to be specified, then the"
          + " contact will be saved to that account.")
  public void addGoogleContact(String contactEmailAddress, String accountEmailAddress)
      throws OperationApplicationException, RemoteException {
    ArrayList<ContentProviderOperation> contentProviderOperations = new ArrayList<>();

    // Specify where the new contact should be stored.
    contentProviderOperations.add(
        ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
            .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, GOOGLE_ACCOUNT_TYPE)
            .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, accountEmailAddress).build());

    // Specify data to associate with the new contact.
    contentProviderOperations.add(
        ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
            .withValue(ContactsContract.Data.MIMETYPE,
                ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
            .withValue(ContactsContract.CommonDataKinds.Email.ADDRESS, contactEmailAddress)
            .withValue(ContactsContract.CommonDataKinds.Email.TYPE,
                ContactsContract.CommonDataKinds.Email.TYPE_HOME).build());

    // Apply the operations to the ContentProvider.
    context.getContentResolver()
        .applyBatch(ContactsContract.AUTHORITY, contentProviderOperations);
  }

  @Rpc(description =
      "Requests an immediate synchronization of contact data for the specified Google account.")
  public void syncGoogleContacts(String accountEmailAddress) {
    Bundle settingsBundle = new Bundle();
    settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
    settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
    ContentResolver.requestSync(new Account(accountEmailAddress, GOOGLE_ACCOUNT_TYPE),
        ContactsContract.AUTHORITY, settingsBundle);
  }

  @Override
  public void shutdown() {
  }
}

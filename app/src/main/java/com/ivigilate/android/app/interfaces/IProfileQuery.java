package com.ivigilate.android.app.interfaces;

import android.provider.ContactsContract;

public interface IProfileQuery {
    int ADDRESS = 0;
    int IS_PRIMARY = 1;

    String[] PROJECTION = {
            ContactsContract.CommonDataKinds.Email.ADDRESS,
            ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
    };
}

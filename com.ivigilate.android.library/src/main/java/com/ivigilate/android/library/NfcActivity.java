package com.ivigilate.android.library;

import android.app.Activity;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Parcelable;

/**
 * Created by joanaPeixoto on 15-Jun-16.
 * Activity registered to handle NFC intents
 */
public class NfcActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onResume() {
        super.onResume();
        processNdfContent();
    }


    @Override
    public void onNewIntent(Intent intent) {
        //onResume gets called after this to handle the new intent
        setIntent(intent);

    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void processNdfContent() {
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction()) ||
                NfcAdapter.ACTION_TECH_DISCOVERED.equals(getIntent().getAction())) {
            Tag tag = getIntent().getParcelableExtra(NfcAdapter.EXTRA_TAG);
            Parcelable[] rawMessages = getIntent().getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);

            //An NdefMessage must have at least one record
            NdefRecord[] firstRecords = null;
            if (rawMessages != null) {
                NdefMessage[] messages = new NdefMessage[rawMessages.length];
                firstRecords = new NdefRecord[messages.length];
                for (int i = 0; i < messages.length; i++) {
                    messages[i] = (NdefMessage) rawMessages[i];
                    firstRecords[i] = messages[i].getRecords()[0];
                }
            }
            if (IVigilateManager.getInstance(this).iVigilateService != null) {
                IVigilateManager.getInstance(this).iVigilateService.ndfSighted(tag , firstRecords);
            }

        }

        finish();
    }
}

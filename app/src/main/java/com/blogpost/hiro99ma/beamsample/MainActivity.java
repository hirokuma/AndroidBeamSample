package com.blogpost.hiro99ma.beamsample;

import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity
        implements NfcAdapter.CreateNdefMessageCallback, NfcAdapter.OnNdefPushCompleteCallback {
    private NfcAdapter mNfcAdapter;

    private static final String TAG = "MainActivity";
    private EditText mEditText;
    private TextView mTextView1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mEditText = (EditText)findViewById(R.id.editInput);
        mTextView1 = (TextView)findViewById(R.id.textView1);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter != null) {
            Log.d(TAG, "set callbacks");
            mNfcAdapter.setNdefPushMessageCallback(this, this);
            mNfcAdapter.setOnNdefPushCompleteCallback(this, this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mNfcAdapter != null) {
            Log.d(TAG, "disable reader mode");
        }
    }

    @Override
    public NdefMessage createNdefMessage(NfcEvent event) {
        //CreateNdefMessageCallback
        //SNEPが可能になったときに呼び出される
        Log.d(TAG, "createNdefMessage");
        CharSequence text = mEditText.getText();
        NdefRecord[] rec = {
                //languageCode : default locale
                NdefRecord.createTextRecord(null, text.toString())
        };
        return new NdefMessage(rec);
    }

    @Override
    public void onNdefPushComplete(NfcEvent event) {
        Log.d(TAG, "onNdefPushComplete");
    }
}

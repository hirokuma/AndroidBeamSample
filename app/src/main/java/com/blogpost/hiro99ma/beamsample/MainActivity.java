package com.blogpost.hiro99ma.beamsample;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity
        implements NfcAdapter.CreateNdefMessageCallback, NfcAdapter.OnNdefPushCompleteCallback {
    private NfcAdapter mNfcAdapter;

    private static final String TAG = "MainActivity";
    private EditText mEditText;
    private TextView mTextView1;
    private TextView mTextView2;

    private final static IntentFilter[] mFilters = new IntentFilter[] {
            new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED),
    };
    private final static String[][] mTechLists = new String[][] {
            new String[] { Ndef.class.getName() },
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mEditText = (EditText)findViewById(R.id.editInput);
        mTextView1 = (TextView)findViewById(R.id.textView1);
        mTextView2 = (TextView)findViewById(R.id.textView2);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter != null) {
            Log.d(TAG, "onResume");
            Intent intent = new Intent(this, this.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    this,
                    0,		//request code
                    intent,
                    0);		//flagなし
            mNfcAdapter.enableForegroundDispatch(this, pendingIntent, mFilters, mTechLists);

            mNfcAdapter.setNdefPushMessageCallback(this, this);
            mNfcAdapter.setOnNdefPushCompleteCallback(this, this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mNfcAdapter != null) {
            Log.d(TAG, "onPause");
            mNfcAdapter.disableForegroundDispatch(this);
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

        CharSequence text = mEditText.getText();
        final StringBuilder sb = new StringBuilder();
        sb.append(text);
        sb.append("\n");
        sb.append(mTextView2.getText());
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTextView2.setText(sb);
            }
        });
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        String action = intent.getAction();
        if (action == null) {
            Log.e(TAG, "fail : null action");
            return;
        }
        Log.d(TAG, "action=" + action);

        //actionはTECH_DISCOVEREDでやってくるのか
//        boolean match = false;
//        for (IntentFilter filter : mFilters) {
//            if (filter.matchAction(action)) {
//                match = true;
//                break;
//            }
//        }
//        if (!match) {
//            Log.e(TAG, "fail : no match intent-filter");
//            return;
//        }

        Parcelable[] rawMsg = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        NdefRecord[] recs = ((NdefMessage)rawMsg[0]).getRecords();
        StringBuilder sb = new StringBuilder();
        for (NdefRecord rec : recs) {
            if (isTextRecord(rec)) {
                String text = getTextAndLangCode(rec);
                sb.append(text);
                sb.append("\n");
            }
        }
        sb.append(mTextView1.getText());
        mTextView1.setText(sb);
    }

    /**
     * NdefRecordがRTDのTextか判定します
     * https://github.com/bs-nfc/ReadRTDText/blob/master/src/jp/co/brilliantservice/android/readrtdtext/HomeActivity.java
     *
     * @param record NDEF record
     * @return true:RTD Text Recotd , false:not RTD Text Record
     */
    private boolean isTextRecord(NdefRecord record) {
        return record.getTnf() == NdefRecord.TNF_WELL_KNOWN
                && Arrays.equals(record.getType(), NdefRecord.RTD_TEXT);
    }

    /**
     * RTD Text Recordから文字列と言語コードを取得します
     * https://github.com/bs-nfc/ReadRTDText/blob/master/src/jp/co/brilliantservice/android/readrtdtext/HomeActivity.java
     *
     * @param record NDEF record
     * @return テキスト(言語コード)
     */
    private String getTextAndLangCode(NdefRecord record) {
        if (record == null)
            throw new IllegalArgumentException();

        byte[] payload = record.getPayload();
        byte flags = payload[0];
        String encoding = ((flags & 0x80) == 0) ? "UTF-8" : "UTF-16";
        int languageCodeLength = flags & 0x3F;
        try {
            //String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");
            String text = new String(payload, 1 + languageCodeLength, payload.length
                    - (1 + languageCodeLength), encoding);

            return String.format("%s", text);
        } catch (UnsupportedEncodingException | IndexOutOfBoundsException e) {
            throw new IllegalArgumentException();
        }
    }
}

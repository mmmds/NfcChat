package com.mmm.nfcchat;

import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.nfc.tech.Ndef;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NfcAdapter.OnNdefPushCompleteCallback, NfcAdapter.CreateNdefMessageCallback {

    private static final String TAG = "MainActivity";
    private NfcAdapter nfcAdapter;
    private static ArrayAdapter<String> messageAdapter;
    private EditText messageInput;
    private static final List<NdefRecord> messagesToSend = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        if (messageAdapter == null) {
            messageAdapter = new ArrayAdapter<String>(this, R.layout.message);
        }
        setContentView(R.layout.activity_main);
        ListView messageListView = (ListView) findViewById(R.id.messageListView);
        messageListView.setAdapter(messageAdapter);
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        messageInput = (EditText) findViewById(R.id.editText);
    }

    public void sendClick(View view) {
        String text = messageInput.getText().toString();
        messageAdapter.add("Send: " + text);
        send(NdefRecord.createTextRecord("en", text));
    }

    public void sendRawClick(View view) {
        String text = messageInput.getText().toString();
        messageAdapter.add("Send raw: " + text);
        send(NdefRecord.createMime("application/octet-stream", text.getBytes()));
    }

    private void send(NdefRecord record) {
        messagesToSend.add(record);
        messageInput.setText("");
        nfcAdapter.setNdefPushMessageCallback(this, this);
        nfcAdapter.setOnNdefPushCompleteCallback(this, this);
    }

    @Override
    protected void onResume() {
        Intent intent = getIntent();
        if (intent != null) {
            Log.d(TAG, intent.getAction());
            Parcelable[] rawMessages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (rawMessages != null) {
                for (int i = 0; i < rawMessages.length; i++) {
                    NdefMessage message = (NdefMessage) rawMessages[i];
                    processMessage(message);
                }
            }
        }
        super.onResume();
    }

    private void processMessage(NdefMessage message) {
        NdefRecord[] records = message.getRecords();
        for (int j = 0; j < records.length; j++) {
            processRecord(records[j]);
        }
    }

    private void processRecord(NdefRecord record) {
        String type = new String(record.getType());
        byte[] payload = record.getPayload();
        String msg = type + ", " + new String(payload) + ", " + Arrays.toString(payload);
        messageAdapter.add(msg);
        Log.d(TAG, msg);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
    }

    @Override
    public NdefMessage createNdefMessage(NfcEvent nfcEvent) {
        Log.d(TAG, "creating message");
        NdefMessage message = new NdefMessage(messagesToSend.toArray(new NdefRecord[0]));
        messagesToSend.clear();
        return message;
    }

    @Override
    public void onNdefPushComplete(NfcEvent nfcEvent) {
        Log.d(TAG, "message sent " + nfcEvent);
    }
}

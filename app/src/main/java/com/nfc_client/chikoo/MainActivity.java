package com.nfc_client.chikoo;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {

    Button btn;
    EditText textField;

    private ServiceConnection sConn;

    private Messenger messenger;
    String refrenceId;
    String status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn = findViewById(R.id.button);
        textField = findViewById(R.id.inputText);

        sConn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Log.d("Service", "Service Connected");
                messenger = new Messenger(service);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Log.e("Service", "Service  disConnected");
                messenger = null;
            }
        };

        bindToService();

        btn.setOnClickListener(v -> {
            String val = textField.getText().toString();
            Message msg = Message
                    .obtain(null, 1);
            try {
//                bindToService();
                msg.replyTo = new Messenger(new ResponseHandler());
                // We pass the value
                Bundle b = new Bundle();
                b.putString("data", "{\"total\":300, \"orderId\":\"1232143214221\",}");

                msg.setData(b);

                messenger.send(msg);
            } catch (RemoteException e) {
                Log.e("MESSENGER EXCEPTION", e.toString());
                e.printStackTrace();
            }
        });
    }

    public void bindToService() {
        Intent intent = new Intent("com.brandverse.chikoo_nfc_android.NfcService");
        intent.setPackage("com.brandverse.chikoo_nfc_android");

        boolean check = bindService(intent, sConn, BIND_AUTO_CREATE);
        if (check) {
            Log.d("NFC SERVICE", "NFC service connected");
        } else {
            Log.d("NFC SERVICE", "NFC service not connected");

        }
    }

    private void checkForPaymentUpdate() {
        Message msg = Message
                .obtain(null, 2);
        msg.replyTo = new Messenger(new ResponseHandler());
        Bundle b = new Bundle();
        b.putString("data", refrenceId);

        msg.setData(b);

        try {
            messenger.send(msg);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    class ResponseHandler extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg.what == 1) {
                refrenceId = msg.getData().getString("respData");
                Log.d("SERVER RESPONSE", refrenceId != null ? refrenceId : "NULL RECEIVED");
                checkForPaymentUpdate();
            } else if (msg.what == 2) {
                Log.d("PAYMENT RESPONSE", msg.getData().toString());
                status = msg.getData().getString("respData");
                Log.d("STATUS RESPONSE", status);
                if (status.equals("Processing")) {
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            checkForPaymentUpdate();
                        }
                    }, 3000);
                } else if (status.equals("Failed")) {
                    textField.setText("Fail ho gyi");
                } else {
                    textField.setText(status );
                }
            }
        }
    }


}
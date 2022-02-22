package aslam.junaid.ledcommands;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.StringDef;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener {

    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 35;
    private final String PREF_FILE_NAME = "CommandsStore";
    private final String KEY_CMD = "Command";
    private final String KEY_NAME = "Name";
    private final String KEY_NUMBER = "Number";
    private final String SENT = "SMS_SENT";
    private final String DEFAULT_NUMBER = "";

    private SharedPreferences preferences = null;
    private SharedPreferences.Editor editor;
    private Menu menu;
    SmsManager smsManager;
    ProgressDialog progress;

    private String number;
    private int cmdNum;
    private Button[] btnCmds;
    private String[] strCmds;
    private String[] strNames;
    private TextView tvLog;
    private String strLog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        smsManager = SmsManager.getDefault();
        preferences = getSharedPreferences(PREF_FILE_NAME, MODE_PRIVATE);
        editor = preferences.edit();
        progress = new ProgressDialog(this);

        btnCmds = new Button[12];
        strCmds = new String[12];
        strNames = new String[12];
        strLog = "";

        btnCmds[0] = (Button) findViewById(R.id.btn_0);
        btnCmds[1] = (Button) findViewById(R.id.btn_1);
        btnCmds[2] = (Button) findViewById(R.id.btn_2);
        btnCmds[3] = (Button) findViewById(R.id.btn_3);
        btnCmds[4] = (Button) findViewById(R.id.btn_4);
        btnCmds[5] = (Button) findViewById(R.id.btn_5);
        btnCmds[6] = (Button) findViewById(R.id.btn_6);
        btnCmds[7] = (Button) findViewById(R.id.btn_7);
        btnCmds[8] = (Button) findViewById(R.id.btn_8);
        btnCmds[9] = (Button) findViewById(R.id.btn_9);
        btnCmds[10] = (Button) findViewById(R.id.btn_10);
        btnCmds[11] = (Button) findViewById(R.id.btn_11);

        tvLog = (TextView) findViewById(R.id.tv_log);

        number = preferences.getString(KEY_NUMBER, DEFAULT_NUMBER);

        for(int i = 0; i < 12; i++) {
            strCmds[i] = preferences.getString(KEY_CMD+i, "Command"+i);
            strNames[i] = preferences.getString(KEY_NAME+i, "Name"+i);
            btnCmds[i].setText(strNames[i]);
            btnCmds[i].setOnClickListener(this);
            btnCmds[i].setOnLongClickListener(this);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        this.menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

        if(id == R.id.action_set_number) {
            final AlertDialog.Builder myAlert = new AlertDialog.Builder(MainActivity.this);

            myAlert.setTitle(getString(R.string.new_number_alert));

            LinearLayout layout = new LinearLayout(MainActivity.this);
            layout.setOrientation(LinearLayout.VERTICAL);

            final EditText etNumberField = new EditText(MainActivity.this);
            etNumberField.setHint(R.string.number_hint);
            etNumberField.setText("0" + number.substring(3, number.length()));
            etNumberField.setSelectAllOnFocus(true);
            etNumberField.setInputType(InputType.TYPE_CLASS_NUMBER);
            etNumberField.setFilters(new InputFilter[]{new InputFilter.LengthFilter(11)});
            layout.addView(etNumberField);

            myAlert.setView(layout);

            myAlert.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.RESULT_HIDDEN, 0);
                    String numText = etNumberField.getText().toString();
                    if(numText.startsWith("03") && (numText.length() == 11)) {
                        numText = getString(R.string.num_prefix) + numText.substring(1, numText.length());
                        editor.putString(KEY_NUMBER, numText);
                        editor.commit();
                        number = numText;
                    } else {
                        Toast.makeText(MainActivity.this, getString(R.string.invalid_number), Toast.LENGTH_SHORT).show();
                    }
                }

            });

            myAlert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.RESULT_HIDDEN, 0);
                }
            });

            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.RESULT_SHOWN, 0);

            myAlert.create().show();
            return true;
        }

        if (id == R.id.action_about_us) {
            aboutUs();
            return true;
        }

        if(id == R.id.action_exit) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void aboutUs() {
        AlertDialog.Builder abt = new AlertDialog.Builder(MainActivity.this);
        abt.setTitle(R.string.about_us);

        abt.setMessage(getString(R.string.developers));
        abt.setNeutralButton(R.string.close_dialog, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        abt.create().show();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        for (int i = 0; i < 12; i++) {
            if(id == btnCmds[i].getId()) {
                cmdNum = i;
                if (ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.SEND_SMS)
                        != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.SEND_SMS},
                            MY_PERMISSIONS_REQUEST_SEND_SMS);

                    // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                    // app-defined int constant. The callback method gets the
                    // result of the request.
                } else {
                    startProgress("Sending SMS", "Wait while SMS is being Sent", false);
                    sendSms();
                }
            }
        }

    }

    private void sendSms() {
        final PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, new Intent(SENT), 0);
        registerReceiver(new BroadcastReceiver(){
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode())
                {
                    case Activity.RESULT_OK:
                        Toast.makeText(getBaseContext(), "SMS sent",
                                Toast.LENGTH_SHORT).show();
                        strLog = strCmds[cmdNum] + "\n" + strLog;
                        tvLog.setText(strLog);
                        progress.dismiss();
                        MainActivity.this.unregisterReceiver(this);
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Toast.makeText(getBaseContext(), "Generic failure",
                                Toast.LENGTH_SHORT).show();
                        progress.dismiss();
                        MainActivity.this.unregisterReceiver(this);
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(getBaseContext(), "No service",
                                Toast.LENGTH_SHORT).show();
                        progress.dismiss();
                        MainActivity.this.unregisterReceiver(this);
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Toast.makeText(getBaseContext(), "Null PDU",
                                Toast.LENGTH_SHORT).show();
                        progress.dismiss();
                        MainActivity.this.unregisterReceiver(this);
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Toast.makeText(getBaseContext(), "Radio off",
                                Toast.LENGTH_SHORT).show();
                        progress.dismiss();
                        MainActivity.this.unregisterReceiver(this);
                        break;
                }
            }
        }, new IntentFilter(SENT));
        smsManager.sendTextMessage(number, null, strCmds[cmdNum], sentPI, null);
    }

    @Override
    public boolean onLongClick(View view) {
        final int id = view.getId();

        Button btn = (Button) view;

        final AlertDialog.Builder myAlert = new AlertDialog.Builder(MainActivity.this);

        myAlert.setTitle(btn.getText());

        LinearLayout layout = new LinearLayout(MainActivity.this);
        layout.setOrientation(LinearLayout.VERTICAL);

        final EditText etNameField = new EditText(MainActivity.this);
        etNameField.setHint(R.string.name_hint);
        etNameField.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
        layout.addView(etNameField);

        final EditText etCommandField = new EditText(MainActivity.this);
        etCommandField.setHint(R.string.command_hint);
        etCommandField.setFilters(new InputFilter[]{new InputFilter.LengthFilter(25)});
        layout.addView(etCommandField);

        myAlert.setView(layout);

        myAlert.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                for(int j = 0; j < 12; j++) {
                    if(btnCmds[j].getId() == id) {
                        if(!etNameField.getText().toString().matches("")) {
                            btnCmds[j].setText(etNameField.getText().toString());
                            strNames[j] = etNameField.getText().toString();
                            editor.putString(KEY_NAME+j, etNameField.getText().toString());
                            editor.commit();
                        }

                        if (!etCommandField.getText().toString().matches("")) {
                            strCmds[j] = etCommandField.getText().toString();
                            editor.putString(KEY_CMD+j, etCommandField.getText().toString());
                            editor.commit();
                        }
                    }
                }
            }
        });

        myAlert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        myAlert.create().show();
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_SEND_SMS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startProgress("Sending SMS", "Wait while SMS is being Sent", false);
                    sendSms();
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {
                    Toast.makeText(MainActivity.this, getString(R.string.sms_permission_denied), Toast.LENGTH_SHORT).show();
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
            return;
        }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private void startProgress(String title, String msg, Boolean state) {
        progress.setTitle(title);
        progress.setMessage(msg);
        progress.setCancelable(state);
        progress.show();
    }
}

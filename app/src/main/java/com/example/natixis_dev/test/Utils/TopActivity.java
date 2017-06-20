package com.example.natixis_dev.test.Utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Toast;

import com.example.natixis_dev.test.ChatBotActivity;
import com.example.natixis_dev.test.R;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.ButterKnife;

public class TopActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 100;
    private static final int MY_PERMISSIONS_REQUEST_WRITE_STORAGE = 200;
    private static final int MY_PERMISSIONS_REQUEST_READ_STORAGE = 300;
    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 400;
    private static final int MY_PERMISSIONS_REQUEST_INTERNET = 500;
    public static final int MY_PERMISSIONS_REQUEST_FINGERPRINT = 600;
    public static final int MY_PERMISSIONS_REQUEST_NFC = 700;

    public static final String APP_TAG = "TestApp - ";
    private static final String ALLOW_KEY = "ALLOWED";
    protected final int REQ_CODE_TAKE_PHOTO = 1;
    private static String currentReceiver;
    private static String currentMessage;
    private static String currentSubject;
    private static Uri currentUriFile;
    private static Locale currentLocale;
    private TextToSpeech tts;
    protected Camera camera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_top);
        ButterKnife.bind(this);
    }

    public void onPause(){
        if(tts !=null){
            tts.stop();
            tts.shutdown();
            tts = null;
        }
        if (camera != null) {
            camera.release();
            camera = null;
        }
        super.onPause();
    }

    protected void changeLanguage(Locale locale) {
        Resources res = getResources();
        Configuration conf = res.getConfiguration();
        conf.locale = new Locale(locale.toString());
        res.updateConfiguration(conf, res.getDisplayMetrics());
        Locale.setDefault(locale);
        recreate();
    }

    protected boolean checkPermission(String permission, int requestCode, String featureName){
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            if (Utils.getFromPreferences(this, ALLOW_KEY)) {
                showSettingsAlertForPermission(this, featureName);
            } else if (ContextCompat.checkSelfPermission(this,
                    permission)

                    != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        permission)) {
                    showAlertForPermission(this, permission, requestCode, featureName);
                } else {
                    // No explanation needed, we can request the permission.
                    ActivityCompat.requestPermissions(this,
                            new String[]{permission},
                            requestCode);
                }
            }
            return false;
        } else {
            return true;
        }
    }

    protected void showAlertForPermission(final Activity activity, final String permission, final int requestCode, String featureName) {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle(getString(R.string.alert_permission_title));
        alertDialog.setMessage(getString(R.string.alert_permission_text, featureName));

        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.alert_permission_negative_button),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                });

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.alert_permission_positive_button),
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        ActivityCompat.requestPermissions(activity,
                                new String[]{permission},
                                requestCode);
                    }
                });
        alertDialog.show();
    }

    protected void showSettingsAlertForPermission(final Activity activity, String featureName) {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle(getString(R.string.alert_permission_title));
        alertDialog.setMessage(getString(R.string.alert_permission_text, featureName));

        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.alert_permission_negative_button),
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        //finish();
                    }
                });

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.alert_permission_settings_button),
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        startInstalledAppDetailsActivity(activity);
                    }
                });

        alertDialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        String permission = null;
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA:
                //for (int i = 0, len = permissions.length; i < len; i++) {
                permission = permissions[0];
                if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    boolean
                            showRationale =
                            ActivityCompat.shouldShowRequestPermissionRationale(
                                    this, permission);
                    if (showRationale) {
                        showAlertForPermission(this, permission, requestCode, "Camera");
                    } else if (!showRationale) {
                        // user denied flagging NEVER ASK AGAIN
                        // you can either enable some fall back,
                        // disable features of your app
                        // or open another dialog explaining
                        // again the permission and directing to
                        // the app setting
                        Utils.saveToPreferences(this, ALLOW_KEY, true);
                    }
                }
                else {
                    takePicture(REQ_CODE_TAKE_PHOTO);
                }
                break;
            case MY_PERMISSIONS_REQUEST_WRITE_STORAGE:
            case MY_PERMISSIONS_REQUEST_READ_STORAGE:
                //for (int i = 0, len = permissions.length; i < len; i++) {
                permission = permissions[0];
                if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    boolean showRationale =
                            ActivityCompat.shouldShowRequestPermissionRationale(
                                    this, permission);

                    if (showRationale) {
                        showAlertForPermission(this, permission, requestCode, "Files");
                    } else if (!showRationale) {
                        // user denied flagging NEVER ASK AGAIN
                        // you can either enable some fall back,
                        // disable features of your app
                        // or open another dialog explaining
                        // again the permission and directing to
                        // the app setting
                        Utils.saveToPreferences(this, ALLOW_KEY, true);
                    }
                }
                else {
                    takePicture(REQ_CODE_TAKE_PHOTO);
                }
                break;
            case MY_PERMISSIONS_REQUEST_SEND_SMS: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    sendSMS(this, currentReceiver, currentMessage);
                }
                break;
            }
            case MY_PERMISSIONS_REQUEST_INTERNET:
            case MY_PERMISSIONS_REQUEST_FINGERPRINT:
            case MY_PERMISSIONS_REQUEST_NFC:
                {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Great!
                }
                break;
            }
            default:
                break;
        }
    }

    protected static void startInstalledAppDetailsActivity(final Activity activity) {
        if (activity == null) {
            return;
        }

        final Intent i = new Intent();
        i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        i.addCategory(Intent.CATEGORY_DEFAULT);
        i.setData(Uri.parse("package:" + activity.getPackageName()));
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        activity.startActivity(i);
    }

    protected void takePicture(int requestCode) {
        if(checkPermission(Manifest.permission.CAMERA, MY_PERMISSIONS_REQUEST_CAMERA, "Camera")
                && checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, MY_PERMISSIONS_REQUEST_WRITE_STORAGE, "Files")
                && checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE, MY_PERMISSIONS_REQUEST_READ_STORAGE, "Files")) {
            final String dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/TestApp/";
            File newDir = new File(dir);
            newDir.mkdirs();

            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
                    .format(new Date());
            String filename = getString(R.string.file_prefix) + timeStamp + getString(R.string.file_extension);
            File newfile = new File(newDir, filename);
            try {
                newfile.createNewFile();
            } catch (IOException e) {
                Log.e(APP_TAG + ChatBotActivity.class.getSimpleName(), "takePicture - ", e);
            }
            currentUriFile = Uri.fromFile(newfile);
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, currentUriFile);
            startActivityForResult(cameraIntent, requestCode);
        }
    }
    
    protected Uri getCurrentUriFile(){
        return currentUriFile;
    }

    /*protected void sendEmail() {
        if (checkPermission(Manifest.permission.INTERNET, MY_PERMISSIONS_REQUEST_INTERNET, "Internet")) {
            Toast.makeText(this, "Not yet implemented.",
                    Toast.LENGTH_LONG).show();
            // Open a dedicated app with intent chooser
        }
    }*/

    public static void sendEmail(TopActivity activity, String sendToAddress, String subject, String message){
        //if (activity.checkPermission(Manifest.permission.INTERNET, MY_PERMISSIONS_REQUEST_INTERNET, "Internet")) {
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
            emailIntent.setData(Uri.parse("mailto:")); // only email apps should handle this
            //emailIntent.setType("plain/text");
            String[] emails = null;
            if(sendToAddress.contains(";")){
                emails = sendToAddress.split(";");
            } else {
                emails = new String[1];
                emails[0] = sendToAddress;
            }
            emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, emails);
            if (subject != null) {
                emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
            }
            if (message != null) {
                emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, message);
            }
            /*if (filename != null)
                emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://"+ filename));*/
            String chooserTitle = activity.getString(R.string.email_chooser_title);
            activity.startActivity(Intent.createChooser(emailIntent, chooserTitle));
        //}
        //currentReceiver = sendToAddress;
        //currentMessage = message;
        //currentSubject = subject;
    }

    /*protected void sendSMS(String sendToNum, String message) {
        if (checkPermission(Manifest.permission.SEND_SMS, MY_PERMISSIONS_REQUEST_SEND_SMS, "SMS")) {
            currentSMSReceiver = sendToNum;
            currentSMSMessage = message;
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(sendToNum.replace("+", "00"), null, message, null, null);
            Toast.makeText(this, "SMS sent.",
                    Toast.LENGTH_LONG).show();
        }
    }*/

    public static void sendSMS(TopActivity activity, String sendToNum, String message) {
        if (activity.checkPermission(Manifest.permission.SEND_SMS, MY_PERMISSIONS_REQUEST_SEND_SMS, "SMS")) {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(sendToNum.replace("+", "00"), null, message, null, null);
            Toast.makeText(activity, activity.getString(R.string.sms_sent),
                    Toast.LENGTH_LONG).show();
        }
        currentReceiver = sendToNum;
        currentMessage = message;
    }

    public void read(String textToRead){
        if(tts == null){
            tts =new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    if(status != TextToSpeech.ERROR) {
                        tts.setLanguage(Locale.getDefault());
                    }
                }
            });
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tts.speak(textToRead, TextToSpeech.QUEUE_FLUSH, null, "REQUEST_CODE");
        }
        else tts.speak(textToRead, TextToSpeech.QUEUE_FLUSH, null);
    }
}

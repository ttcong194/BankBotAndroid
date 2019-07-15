package vn.aiteam.hackathon;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;
import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.messages.MessageHolders;
import com.stfalcon.chatkit.messages.MessageInput;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.aiteam.hackathon.Api.APIService;
import vn.aiteam.hackathon.Api.ApiUtils;
import vn.aiteam.hackathon.Api.Model.Query;
import vn.aiteam.hackathon.Dialog.DialogRecorder;
import vn.aiteam.hackathon.Fragment.MessageDialogFragment;
import vn.aiteam.hackathon.Model.Message;
import vn.aiteam.hackathon.Model.MessagesFixtures;
import vn.aiteam.hackathon.Services.SpeechService;
import vn.aiteam.hackathon.TTS.GCPTTSAdapter;
import vn.aiteam.hackathon.TTS.Gcp.AudioConfig;
import vn.aiteam.hackathon.TTS.Gcp.EAudioEncoding;
import vn.aiteam.hackathon.TTS.Gcp.GCPVoice;
import vn.aiteam.hackathon.TTS.SpeechManager;
import vn.aiteam.hackathon.Utils.UiUtils;
import vn.aiteam.hackathon.Utils.VoiceRecorder;
import vn.aiteam.hackathon.Views.IncomingCameraMessageViewHolder;
import vn.aiteam.hackathon.Views.IncomingVoiceMessageViewHolder;
import vn.aiteam.hackathon.Views.OutcomingCameraMessageViewHolder;
import vn.aiteam.hackathon.Views.OutcomingVoiceMessageViewHolder;

public class MainActivity extends AppCompatActivity implements
        MessagesListAdapter.OnLoadMoreListener, MessageInput.InputListener,
        MessageInput.AttachmentsListener,MessagesListAdapter.SelectionListener,
        MessageHolders.ContentChecker<Message>, View.OnClickListener, MessageDialogFragment.Listener, MediaPlayer.OnCompletionListener {

    private static final byte CONTENT_TYPE_VOICE = 1;
    private static final byte CONTENT_TYPE_CAMERA = 2;


    private static final int CAMERA_PHOTO_REQUEST_CODE = 99;

    private static final int ACTION_RECORD = 10;
    private static final String PATTERN_RECORD = "|action=record:voice";
    private static final int ACTION_CAMERA_FRONT= 11;
    private static final String PATTERN_CAMERA_FRONT = "|action=takephoto:selfie";
    private static final int ACTION_CAMERA_BACK= 12;
    private static final String PATTERN_CAMERA_BACK1 = "|action=takephoto:id1";
    private static final String PATTERN_CAMERA_BACK2 = "|action=takephoto:id2";
    private static final int ACTION_CALL = 13;
    private static final String PATTERN_CALL = "|action=make_call";
    private MessagesList messagesList;
    private MessageInput input;
    //Mode typing
    private static final int MODE_TYPING = 1000;
    private static final int MODE_SPEAKING = 1001;

    private ImageButton recordingCloseButton;
    private ImageButton indicatorRecordingButton;

    private TextView recordingText;

    private LinearLayout recordingInput;
    private View line;


    private static final String FRAGMENT_MESSAGE_DIALOG = "message_dialog";

    private static final String STATE_RESULTS = "results";

    private static final int REQUEST_ALL_PERMISSION = 1;

    private SpeechService mSpeechService;
    File tmpCameraFile;
    private VoiceRecorder mVoiceRecorder;
    private final VoiceRecorder.Callback mVoiceCallback = new VoiceRecorder.Callback() {

        @Override
        public void onVoiceStart() {
            maybeClickHearing = true;
            showStatus(true);
            if (mSpeechService != null) {
                mSpeechService.startRecognizing(mVoiceRecorder.getSampleRate());
            }
        }

        @Override
        public void onVoice(byte[] data, int size) {
            if (mSpeechService != null) {
                mSpeechService.recognize(data, size);
            }
        }

        @Override
        public void onVoiceEnd() {
            showStatus(false);
            if (mSpeechService != null) {
                mSpeechService.finishRecognizing();
            }
        }

    };

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder binder) {
            mSpeechService = SpeechService.from(binder);
            mSpeechService.addListener(mSpeechServiceListener);
            //mStatus.setVisibility(View.VISIBLE);
            if(indicatorRecordingButton != null){
                showStatus(true);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mSpeechService = null;
        }

    };


    private final SpeechService.Listener mSpeechServiceListener =
            new SpeechService.Listener() {
                @Override
                public void onSpeechRecognized(final String text, final boolean isFinal) {
                    if (isFinal) {
                        mVoiceRecorder.dismiss();
                    }
                    if (recordingText != null && !TextUtils.isEmpty(text)) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (isFinal) {
                                    recordingText.setText(null);
                                    MainActivity.this.messagesAdapter.addToStart(
                                            MessagesFixtures.getTextMesageOfLocal(text), true);
                                    query(new Query(text));
                                } else {
                                    recordingText.setText(text);
                                }
                            }
                        });
                    }
                }
            };

    private void showStatus(final boolean hearingVoice) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final Resources resources = getResources();
                final Resources.Theme theme = getTheme();
                int mColorHearing = ResourcesCompat.getColor(resources, R.color.dark_orange, theme);
                int mColorNotHearing = ResourcesCompat.getColor(resources, R.color.dark_gray, theme);

                indicatorRecordingButton.setColorFilter(hearingVoice ? mColorHearing : mColorNotHearing);
            }
        });
    }

    protected final String senderId = "0";
    protected ImageLoader imageLoader;
    protected MessagesListAdapter<Message> messagesAdapter;
    protected String userName;

    private SpeechManager mSpeechManager;
    private GCPTTSAdapter mGCPTTSAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mode = MODE_TYPING;
        mAPIService = ApiUtils.getAPIService();
        imageLoader = new ImageLoader() {
            @Override
            public void loadImage(ImageView imageView, String url, Object payload) {
                int resId = UiUtils.getResourceId(imageView.getResources(),url,"drawable", getPackageName());
                if(resId > 0 ){
                    Picasso.with(MainActivity.this).load(resId).into(imageView);
                }
                else{
                    Picasso.with(MainActivity.this).load(url).into(imageView);
                }
            }
        };

        this.messagesList = (MessagesList) findViewById(R.id.messagesList);
        initAdapter();

        input = (MessageInput) findViewById(R.id.input);
        input.setVisibility(View.VISIBLE);
        input.setInputListener(this);
        input.setAttachmentsListener(this);

        recordingInput = (LinearLayout) findViewById(R.id.recordingInput);
        recordingInput.setVisibility(View.GONE);

        line = findViewById(R.id.line);

        recordingCloseButton = (ImageButton) findViewById(R.id.recordingCloseButton);
        recordingCloseButton.setOnClickListener(this);

        recordingText = (TextView) findViewById(R.id.recordingText);
        indicatorRecordingButton = (ImageButton) findViewById(R.id.indicatorRecordingButton);

        indicatorRecordingButton.setOnClickListener(this);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(messagesAdapter != null){
                    messagesAdapter.addToStart(
                            MessagesFixtures.getTextMesageOfBot(getResources().getString(R.string.bot_hello)), true);
                    if(isTSSOn)
                    startSpeak(getResources().getString(R.string.bot_hello));
                }
                //dispatchTakePictureIntent();
            }
        },500);
        //query(new Query("Xin chào"));
        String dateFormat = new SimpleDateFormat("yyyymmddhhmmss").format(new Date());
        userName = String.format("%s%s","bankbot",dateFormat);

        //setting TTS
        mSpeechManager = new SpeechManager();
        mGCPTTSAdapter = new GCPTTSAdapter();
        mGCPTTSAdapter.addCompleteListener(MainActivity.this);
        mSpeechManager.setSpeech(mGCPTTSAdapter);
    }

    private void initGCPTTSVoice() {
        if (mGCPTTSAdapter == null) return;

        String languageCode = "vi-VN";
        String name = "vi-VN-Wavenet-A";
        float pitch = 0.0f;
        float speakRate = 1.0f;

        Log.i("BEM","languageCode:"+languageCode);
        Log.i("BEM","name:"+name);
        Log.i("BEM","pitch:"+pitch);
        Log.i("BEM","speakRate:"+speakRate);

        GCPVoice gcpVoice = new GCPVoice(languageCode, name);
        AudioConfig audioConfig = new AudioConfig.Builder()
                .addAudioEncoding(EAudioEncoding.MP3)
                .addSpeakingRate(speakRate)
                .addPitch(pitch)
                .build();

        mGCPTTSAdapter.setGCPVoice(gcpVoice);
        mGCPTTSAdapter.setAudioConfig(audioConfig);
    }

    public void startSpeak(String text) {
        mSpeechManager.stopSpeak();
        initGCPTTSVoice();
        mSpeechManager.startSpeak(text);
    }

    public void stopSpeak() {
        mSpeechManager.stopSpeak();
    }

    public void resumeSpeak() {
        mSpeechManager.resume();
    }

    public void pauseSpeak() {
        mSpeechManager.pause();
    }

    public void disposeSpeak() {
        mSpeechManager.dispose();
        mSpeechManager = null;
    }

    private final String TAG = "MainActivity";
    private APIService mAPIService;

    private void query(Query query){
        Gson gson = new Gson();
        String jsonRequest = gson.toJson(query);
        Log.i(TAG, "jsonRequest:"+jsonRequest);
        Log.i(TAG, "Query Function");

        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"),jsonRequest);
        mAPIService.query(requestBody,userName).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                Log.i(TAG, "OK." + response.isSuccessful());
                Log.i(TAG, "Code." + response.code());

                if(response.isSuccessful()) {
                    String responseBase = response.body();
                    Log.i(TAG, "responseBase." + responseBase);

                    Gson gson = new Gson();
                    Type listType = new TypeToken<List<vn.aiteam.hackathon.Api.Model.Response>>(){}.getType();
                    List<vn.aiteam.hackathon.Api.Model.Response> myModelList = gson.fromJson(responseBase, listType);
                    //Log.i(TAG, "count." + myModelList.get(0).getText());
                    StringBuilder textToSpeechBuilder = new StringBuilder();
                    if(myModelList.size() > 0){
                        for (int i=0;i<myModelList.size();i++){
                            vn.aiteam.hackathon.Api.Model.Response item = myModelList.get(i);
                            /*if(messagesAdapter != null){
                                messagesAdapter.addToStart(
                                        MessagesFixtures.getTextMesageOfBot(item.getText()), true);
                            }*/
                            int actionCode = getActionCodeOfText(item.getText());
                            String[] text = getTextByActionCode(item.getText(),actionCode);

                            final String regex = "(https?:\\/\\/(?:www\\.|(?!www))[a-zA-Z0-9][a-zA-Z0-9-]+[a-zA-Z0-9]\\.[^\\s]{2,}|www\\.[a-zA-Z0-9][a-zA-Z0-9-]+[a-zA-Z0-9]\\.[^\\s]{2,}|https?:\\/\\/(?:www\\.|(?!www))[a-zA-Z0-9]+\\.[^\\s]{2,}|www\\.[a-zA-Z0-9]+\\.[^\\s]{2,})";
                            final Pattern pattern = Pattern.compile(regex,Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
                            String textToSpeech = new String(text[0]);
                            final Matcher matcher = pattern.matcher(textToSpeech);

                            while (matcher.find()) {
                                textToSpeech = textToSpeech.replace(matcher.group(0),"liên kết trên.");
                            }

                            textToSpeechBuilder.append(textToSpeech);
                            processTextAndActionCode(text,actionCode);
                        }

                        processSpeech(textToSpeechBuilder.toString());
                    }
                }

            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                t.printStackTrace();
                Log.i(TAG, "onFailure.");
            }
        });
    }

    void processSpeech(String textToSpeech){
        if(isTSSOn) {
            stopSpeak();
            startSpeak(textToSpeech);
        }
    }


    void processTextAndActionCode(final String[] text, int actionCode){
        Log.i(TAG, "text:"+text[0]);
        Log.i(TAG, "actionCode:"+actionCode);
        if(messagesAdapter != null){
            messagesAdapter.addToStart(
                    MessagesFixtures.getTextMesageOfBot(text[0]), true);
            if(mode == MODE_SPEAKING && isTSSOn){
                stopVoiceRecorder();
            }
            /*if(isTSSOn) {
                stopSpeak();
                startSpeak(textToSpeech);
            }*/
        }
        Handler handler = new Handler();
        if(actionCode == ACTION_RECORD){
            if(mode == MODE_SPEAKING){
                stopVoiceRecorder();

            }
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    //Show recorder
                    DialogRecorder dialogRecorder =new DialogRecorder(MainActivity.this, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if(mode == MODE_SPEAKING){
                                if(isTSSOn)
                                {
                                    stopSpeak();
                                }
                                startVoiceRecorder();
                            }
                            if(messagesAdapter != null){
                                messagesAdapter.addToStart(
                                        MessagesFixtures.getVoiceMessage(), true);
                            }
                            query(new Query("###"));
                        }
                    });
                    dialogRecorder.show();
                }
            },500);

        }

        if(actionCode == ACTION_CAMERA_FRONT) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    dispatchTakePictureIntent(1);
                }
            },4000);
        }
        if(actionCode == ACTION_CAMERA_BACK) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    dispatchTakePictureIntent(0);
                }
            },4000);
        }
        if(actionCode == ACTION_CALL){
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent dialIntent = new Intent(Intent.ACTION_DIAL);
                    dialIntent.setData(Uri.parse(text[1]));
                    startActivity(dialIntent);
                }
            },2000);
        }
    }


    int mode;
    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG,"onStart");
        // Prepare Cloud Speech API
        bindService(new Intent(this, SpeechService.class), mServiceConnection, BIND_AUTO_CREATE);

        // Start listening to voices
        /*if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {
            //startVoiceRecorder();
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.RECORD_AUDIO)) {
            showPermissionMessageDialog();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},
                    REQUEST_ALL_PERMISSION);
        }*/
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED && mode == MODE_SPEAKING) {
            Log.i(TAG,"startVoiceRecorder");
            startVoiceRecorder();
        }
        int permissionWriteCheck = ContextCompat.checkSelfPermission(MainActivity.this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE);

        int permissionReadCheck = ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE);


        int permissionCameraCheck = ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.CAMERA);


        int permissionRecordCheck = ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.RECORD_AUDIO);

        if(permissionWriteCheck != PackageManager.PERMISSION_GRANTED
                && permissionReadCheck != PackageManager.PERMISSION_GRANTED
                && permissionCameraCheck != PackageManager.PERMISSION_GRANTED
                && permissionRecordCheck != PackageManager.PERMISSION_GRANTED){

            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.CAMERA,Manifest.permission.RECORD_AUDIO},
                    REQUEST_ALL_PERMISSION);

            // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
            // app-defined int constant. The callback method gets the
            // result of the request.

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        /*if (requestCode == REQUEST_ALL_PERMISSION) {
            if (permissions.length == 1 && grantResults.length == 1
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //startVoiceRecorder();
            } else {
                showPermissionMessageDialog();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }*/


        switch (requestCode) {
            case REQUEST_ALL_PERMISSION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {
                    showPermissionMessageDialog();
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    private void startVoiceRecorder() {
        if (mVoiceRecorder != null) {
            mVoiceRecorder.stop();
        }
        mVoiceRecorder = new VoiceRecorder(mVoiceCallback);
        mVoiceRecorder.start();
    }

    private void stopVoiceRecorder() {
        if (mVoiceRecorder != null) {
            mVoiceRecorder.stop();
            mVoiceRecorder = null;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG,"onStop");
        if(isTSSOn)
        stopSpeak();
        // Stop listening to voice
        stopVoiceRecorder();

        // Stop Cloud Speech API
        mSpeechService.removeListener(mSpeechServiceListener);
        unbindService(mServiceConnection);
        mSpeechService = null;


    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case  R.id.recordingCloseButton:
                RelativeLayout.LayoutParams lpMsgList = (RelativeLayout.LayoutParams)messagesList.getLayoutParams();
                lpMsgList.addRule(RelativeLayout.ABOVE,R.id.input);
                messagesList.setLayoutParams(lpMsgList);

                RelativeLayout.LayoutParams lpLine = (RelativeLayout.LayoutParams)line.getLayoutParams();
                lpLine.addRule(RelativeLayout.ABOVE,R.id.input);
                line.setLayoutParams(lpLine);

                input.setVisibility(View.VISIBLE);
                recordingInput.setVisibility(View.GONE);

                stopVoiceRecorder();
                mode = MODE_TYPING;
                break;
            case  R.id.indicatorRecordingButton:

                if(mode == MODE_SPEAKING && maybeClickHearing){
                    Log.i(TAG,"CLick here button");
                    stopSpeak();
                    startVoiceRecorder();
                    maybeClickHearing = false;
                }
                break;
        }
    }
    //Ham nay tra ve 2 bien
    //Page: So message
    //totalItemCount: So trang va tieu de ben tren
    @Override
    public void onLoadMore(int page, int totalItemsCount) {

    }

    @Override
    public boolean hasContentFor(Message message, byte type) {
        switch (type) {
            case CONTENT_TYPE_VOICE:
                return message.getVoice() != null
                        && message.getVoice().getUrl() != null
                        && !message.getVoice().getUrl().isEmpty();
            case CONTENT_TYPE_CAMERA:
                return  message.getFile() != null;
        }
        return false;
    }

    @Override
    public void onAddAttachments() {
        RelativeLayout.LayoutParams lpMsgList = (RelativeLayout.LayoutParams)messagesList.getLayoutParams();
        lpMsgList.addRule(RelativeLayout.ABOVE,R.id.recordingInput);
        messagesList.setLayoutParams(lpMsgList);

        RelativeLayout.LayoutParams lpLine = (RelativeLayout.LayoutParams)line.getLayoutParams();
        lpLine.addRule(RelativeLayout.ABOVE,R.id.recordingInput);
        line.setLayoutParams(lpLine);

        input.setVisibility(View.GONE);
        recordingInput.setVisibility(View.VISIBLE);

        //hide keyboard
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(input.getInputEditText().getWindowToken(), 0);
        input.getInputEditText().clearFocus();
        mode = MODE_SPEAKING;
        if(isTSSOn){
            stopSpeak();
        }
        startVoiceRecorder();
    }

    @Override
    public boolean onSubmit(CharSequence input) {
        this.messagesAdapter.addToStart(
                MessagesFixtures.getTextMesageOfLocal(input.toString()), true);
        query(new Query(input.toString()));
        return true;
    }


    private void initAdapter() {
        MessageHolders holders = new MessageHolders()
                .setIncomingTextLayout(R.layout.item_custom_incoming_text_message)
                .setOutcomingTextLayout(R.layout.item_custom_outcoming_text_message)
                .setIncomingImageLayout(R.layout.item_custom_incoming_image_message)
                .setOutcomingImageLayout(R.layout.item_custom_outcoming_image_message)
                .registerContentType(
                        CONTENT_TYPE_VOICE,
                        //Tin nhan gui den/ tin nhan nhan
                        IncomingVoiceMessageViewHolder.class,
                        R.layout.item_custom_incoming_voice_message,
                        //Tin nhan gui di
                        OutcomingVoiceMessageViewHolder.class,
                        R.layout.item_custom_outcoming_voice_message,
                        this)
                .registerContentType(
                        CONTENT_TYPE_CAMERA,
                        IncomingCameraMessageViewHolder.class,
                        R.layout.item_custom_incoming_image_message,
                        OutcomingCameraMessageViewHolder.class,
                        R.layout.item_custom_outcoming_image_message,
                        this)
                ;


        this.messagesAdapter = new MessagesListAdapter<>(this.senderId, holders, this.imageLoader);
        this.messagesAdapter.enableSelectionMode(this);
        this.messagesAdapter.setLoadMoreListener(this);
        this.messagesList.setAdapter(this.messagesAdapter);
    }


    @Override
    public void onMessageDialogDismissed() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},
                REQUEST_ALL_PERMISSION);
    }

    private void showPermissionMessageDialog() {
        MessageDialogFragment
                .newInstance(getString(R.string.permission_message))
                .show(getSupportFragmentManager(), FRAGMENT_MESSAGE_DIALOG);
    }

    @Override
    public void onBackPressed() {
        AlertDialog alertDialog =  new AlertDialog.Builder(MainActivity.this)
                .setMessage(getResources().getString(R.string.alert_exit))
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .create();
        alertDialog.show();
    }

    @Override
    public void onSelectionChanged(int count) {

    }

    int getActionCodeOfText(String text){
        if(text.contains(PATTERN_RECORD)){
            return ACTION_RECORD;
        }
        else if(text.contains(PATTERN_CALL)){
            return ACTION_CALL;
        }
        else if(text.contains(PATTERN_CAMERA_BACK1) || text.contains(PATTERN_CAMERA_BACK2)){
            return ACTION_CAMERA_BACK;
        }
        else if(text.contains(PATTERN_CAMERA_FRONT)){
            return ACTION_CAMERA_FRONT;
        }
        else
        return -1;
    }

    String[] getTextByActionCode(String text,int actionCode){

        String newText = "";
        switch (actionCode){
            case -1:
                newText = text;
                break;
            case ACTION_RECORD:
                newText = text.replace(PATTERN_RECORD,"");
                break;
            case ACTION_CAMERA_FRONT:
                newText = text.replace(PATTERN_CAMERA_FRONT,"");
                break;
            case ACTION_CAMERA_BACK:
                if(text.contains(PATTERN_CAMERA_BACK1))
                newText = text.replace(PATTERN_CAMERA_BACK1,"");

                if(text.contains(PATTERN_CAMERA_BACK2))
                    newText = text.replace(PATTERN_CAMERA_BACK2,"");
                break;
            case ACTION_CALL:
                String[] ary = text.replace(PATTERN_CALL,"|tel").split("\\|");
                return ary;
        }
        return new String[]{newText};
    }

    //https://gist.github.com/dirkvranckaert/70bc6812fe0388c8fe4f3bd5c56068c4
    private void dispatchTakePictureIntent(int typeCamera) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            photoFile = createPhotoFile();
            if (photoFile != null) {
                //String pathToFile = photoFile.getAbsolutePath(); //gets the path to the image
                //File mediaFile = new File(pathToFile);
                tmpCameraFile = photoFile;
                Uri photoURI = FileProvider.getUriForFile(MainActivity.this, "vn.aiteam.hackathon.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                takePictureIntent.putExtra("android.intent.extras.CAMERA_FACING", typeCamera);
                startActivityForResult(takePictureIntent, CAMERA_PHOTO_REQUEST_CODE);
            }
        }
    }


    private File createPhotoFile() {
        String name = new SimpleDateFormat("yyyy-mm-dd-hhmmss").format(new Date());
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);  //the directory where we will be storing the pic
        File image = null;

        try {
            image = File.createTempFile("TMP_TAKE_PHOTO", ".jpg", storageDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        InputStream stream = null;
        if (requestCode == CAMERA_PHOTO_REQUEST_CODE && resultCode == Activity.RESULT_OK)
            try {
                /*if(data != null){
                    Log.d("BEM",data.getExtras().getString(MediaStore.EXTRA_OUTPUT));
                }
                else{
                    Log.d("BEM","NULL");
                }*/

                if(this.messagesAdapter != null){
                    this.messagesAdapter.addToStart(MessagesFixtures.getFileMesageOfLocal(tmpCameraFile),true);
                }
                query(new Query("###"));
            }
            /*catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            catch (IOException ex) {
                ex.printStackTrace();
            }*/
            catch(Exception ex){
                ex.printStackTrace();
            }
            finally
            {
                if (stream != null)
                    try {
                        stream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
            }
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        Log.i(TAG,"finish speaking");
        if(mode == MODE_SPEAKING && isTSSOn){
            startVoiceRecorder();
            maybeClickHearing = false;
        }
    }
    boolean isTSSOn = true;
    boolean maybeClickHearing = true;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat_actions_menu, menu);
        MenuItem item = menu.findItem(R.id.myswitch);
        item.setActionView(R.layout.switch_menu);

        Switch mySwitch = item.getActionView().findViewById(R.id.switchForActionBar);
        mySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isTSSOn = isChecked;
                if(!isChecked){
                    stopSpeak();
                }
            }
        });
        return true;
    }
}

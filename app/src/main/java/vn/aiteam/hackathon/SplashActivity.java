package vn.aiteam.hackathon;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.internal.Util;
import okio.BufferedSink;
import okio.Okio;
import okio.Source;
import vn.aiteam.hackathon.Utils.ActivityUtils;

public class SplashActivity extends AppCompatActivity {
    private static final int CAMERA_PHOTO_REQUEST_CODE = 99;
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;


    Button clickButton;
    ImageView iconLogo;

    File tmpCameraFile;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        clickButton = findViewById(R.id.buttonCamera);
        clickButton.setVisibility(View.GONE);
        iconLogo = findViewById(R.id.iconLogo);
        iconLogo.setVisibility(View.GONE);
        clickButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //final String regex = "(https?:\\/\\/(?:www\\.|(?!www))[a-zA-Z0-9][a-zA-Z0-9-]+[a-zA-Z0-9]\\.[^\\s]{2,}|www\\.[a-zA-Z0-9][a-zA-Z0-9-]+[a-zA-Z0-9]\\.[^\\s]{2,}|https?:\\/\\/(?:www\\.|(?!www))[a-zA-Z0-9]+\\.[^\\s]{2,}|www\\.[a-zA-Z0-9]+\\.[^\\s]{2,})";
                //final String regex = "https?:\\/\\/(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_\\+.~#?&\\/\\/=]*)";
                final String regex = "(\\d+ *)+";
                /*final String string = "Vâng, mời Quý khách tham khảo các tin tức mới nhất về Ngân hàng SHB:\n"
                        + "1. SHB tăng lãi suất kỳ hạn 9 tháng lên đến 8,2%/năm http://shb.com.vn/category/tin-tuc2\n"
                        + "2. Thông báo lựa chọn tổ chức đấu giá tài sản\n"
                        + "3. Thông báo thay đổi địa điểm PGD Châu Đốc thuộc SHB Chi nhánh An Giang\n"
                        + "4. Thông báo về việc thực hiện quyền thu giữ tài sản bảo đảm để xử lý nợ\n"
                        + "5. SHB được vinh danh là doanh nghiệp có môi trường làm việc tốt nhất Châu Á\n"
                        + "Để có thêm thông tin chi tiết. Quý khách vui lòng truy cập http://shb.com.vn/category/tin-tuc. Xin cảm ơn!";*/

                 String string = "11 33 99 510\n"
                        + "em ở 987 568 \n"
                        + "cmt 113399510";

                final Pattern pattern = Pattern.compile(regex,Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
                final Matcher matcher = pattern.matcher(string);

                /*if (matcher.find()) {
                    Log.i("BEM","Full match: " + matcher.group(0));
                    String finalValue = string.replace(matcher.group(0),"liên kết trên.");
                    Log.i("BEM","finalValue: " + finalValue);
                }*/

                while (matcher.find()) {
                    String fullMatch = matcher.group(0);
                    System.out.println("Full match: " + matcher.group(0));
                    String removeSpace = fullMatch.replaceAll("\\s+","");
                    System.out.println("removeSpace " + removeSpace);
                    string = string.replace(fullMatch,removeSpace);
                    System.out.println("string " + string);
                    /*for (int i = 1; i <= matcher.groupCount(); i++) {
                        System.out.println("Group " + i + ": " + matcher.group(i));
                    }*/
                }

                ActivityUtils.nextActivityForResult(SplashActivity.this,MakePhoto2Activity.class,CAMERA_PHOTO_REQUEST_CODE);

                //requestCamera();
                //dispatchTakePictureIntent();

                //new AsyncExample().execute();

            }
        });
        int permissionCheck = ContextCompat.checkSelfPermission(SplashActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE);
        if(permissionCheck != PackageManager.PERMISSION_GRANTED) {
            requestPermitWriteFile();
        }
        /*else {*/
            Handler h = new Handler();
            h.postDelayed(new Runnable() {
                @Override
                public void run() {
                ActivityUtils.nextActivity(SplashActivity.this,MainActivity.class);
                finish();

                }
            }, 5000);

        //}
    }

    private class AsyncExample extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {

            try {
                /*OkHttpClient client = new OkHttpClient();

                File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                String path = storageDir.getAbsolutePath() + "/TMP_TAKE_PHOTO1995318831.jpg";

                File checkFile = new File(path);

                if(checkFile.exists()){
                    Log.d("BEM","file exists");
                }

                MediaType MEDIA_TYPE_MARKDOWN = MediaType.parse("image/jpg");
                //= MediaType.parse("text/x-markdown; charset=utf-8");

                InputStream inputStream = new FileInputStream(path);

                RequestBody requestBody = create(MEDIA_TYPE_MARKDOWN, inputStream);
                Request request = new Request.Builder()
                        .url("http://35.202.67.118:8000/upload_img")
                        .post(requestBody)
                        .build();

                Response response = client.newCall(request).execute();
                if (!response.isSuccessful())
                    throw new IOException("Unexpected code " + response);
                Log.d("POST", response.body().string());*/


                OkHttpClient client = new OkHttpClient();

                File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                String path = storageDir.getAbsolutePath() + "/TMP_TAKE_PHOTO1995318831.jpg";

                File checkFile = new File(path);

                if(checkFile.exists()){
                    Log.d("BEM","file exists");
                }

                MediaType MEDIA_TYPE_ = MediaType.parse("image/jpg");

                // Use the imgur image upload API as documented at https://api.imgur.com/endpoints/image
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        //.addFormDataPart("title", "Square Logo")
                        .addFormDataPart("file", "ID1.png",
                                RequestBody.create(MEDIA_TYPE_,checkFile))
                        .build();
                Request request = new Request.Builder()
                        //.header("Authorization", "Client-ID " + IMGUR_CLIENT_ID)
                        .url("http://35.202.67.118:8000/upload_img")
                        .post(requestBody)
                        .build();

                Response response = client.newCall(request).execute();
                if (!response.isSuccessful())
                    throw new IOException("Unexpected code " + response);
                Log.d("POST", response.body().string());
            }
            catch (IOException ex){
                ex.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }

    public static RequestBody create(final MediaType mediaType, final InputStream inputStream) {
        return new RequestBody() {
            @Override
            public MediaType contentType() {
                return mediaType;
            }

            @Override
            public long contentLength() {
                try {
                    return inputStream.available();
                } catch (IOException e) {
                    return 0;
                }
            }

            @Override
            public void writeTo(BufferedSink sink) throws IOException {
                Source source = null;
                try {
                    source = Okio.source(inputStream);
                    sink.writeAll(source);
                } finally {
                    Util.closeQuietly(source);
                }
            }
        };
    }

    private static final int REQUEST_PERMISSION_CODE= 741;
    private void requestPermitWriteFile(){
        // Assume thisActivity is the current activity
        int permissionWriteCheck = ContextCompat.checkSelfPermission(SplashActivity.this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE);

        int permissionReadCheck = ContextCompat.checkSelfPermission(SplashActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE);


        int permissionCameraCheck = ContextCompat.checkSelfPermission(SplashActivity.this,
                Manifest.permission.CAMERA);


        if(permissionWriteCheck != PackageManager.PERMISSION_GRANTED && permissionReadCheck != PackageManager.PERMISSION_GRANTED && permissionCameraCheck != PackageManager.PERMISSION_GRANTED){

            ActivityCompat.requestPermissions(SplashActivity.this,
                    new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.CAMERA},
                    REQUEST_PERMISSION_CODE);

            // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
            // app-defined int constant. The callback method gets the
            // result of the request.

        }

    }

    //https://gist.github.com/dirkvranckaert/70bc6812fe0388c8fe4f3bd5c56068c4
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            photoFile = createPhotoFile();
            if (photoFile != null) {
                //String pathToFile = photoFile.getAbsolutePath(); //gets the path to the image
                //File mediaFile = new File(pathToFile);
                tmpCameraFile = photoFile;
                Uri photoURI = FileProvider.getUriForFile(SplashActivity.this, "vn.aiteam.hackathon.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                takePictureIntent.putExtra("android.intent.extras.CAMERA_FACING", 1);
                startActivityForResult(takePictureIntent, CAMERA_PHOTO_REQUEST_CODE);
            }
        }
    }


    private File createPhotoFile() {
        String name = new SimpleDateFormat("yyyy-mm-dd-hhmmss").format(new Date());
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);  //the directory where we will be storing the pic
        File image = null;

        try {
            image = File.createTempFile(name, ".jpg", storageDir);
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
                if(data != null){
                    Log.d("BEM",data.getExtras().getString(MediaStore.EXTRA_OUTPUT));
                }
                else{
                    Log.d("BEM","NULL");
                }
                Picasso.with(SplashActivity.this).load(tmpCameraFile).into(iconLogo);
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
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //requestCamera();
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {
                    //requestCamera();
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
}

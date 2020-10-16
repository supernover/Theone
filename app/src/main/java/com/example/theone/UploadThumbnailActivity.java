package com.example.theone;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;

public class UploadThumbnailActivity extends AppCompatActivity {
    Uri videothumburi;
    String thumbnail_url;
    ImageView thumbnail_image;
    StorageReference mStoragerefthumbnails;
    DatabaseReference referenceVideos;
    TextView textSelected;
    RadioButton radioButtonLatest,radioButtonpopular, radioButtonNotype,radioButtonSlide;
    StorageTask mStorageTask;
    DatabaseReference  updatedataref;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_thumbnail);
        textSelected = findViewById(R.id.textNothumbnailselected);
        thumbnail_image = findViewById(R.id.imageview);
        radioButtonLatest = findViewById(R.id.radiolatestMovies);
        radioButtonpopular = findViewById(R.id.radiobestpopularMovies);
        radioButtonNotype = findViewById(R.id.radioNotype);
        radioButtonSlide = findViewById(R.id.radioSlideMovies);
        mStoragerefthumbnails = FirebaseStorage.getInstance().getReference().child("VideoThumbnails");
        referenceVideos = FirebaseDatabase.getInstance().getReference().child("Videos");

        String currentUid = getIntent().getExtras().getString("currentuid");
        updatedataref = FirebaseDatabase.getInstance().getReference("videos").child(currentUid);






        radioButtonNotype.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //String LatestMovies = radioButtonLatest.getText().toString();
                updatedataref.child("video_type").setValue("");
                updatedataref.child("video_slide").setValue("");
                Toast.makeText(UploadThumbnailActivity.this, "selected: no type", Toast.LENGTH_SHORT).show();




            }
        });


        radioButtonLatest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String LatestMovies = radioButtonLatest.getText().toString();
                updatedataref.child("video_type").setValue(LatestMovies);
                updatedataref.child("video_slide").setValue("");
                Toast.makeText(UploadThumbnailActivity.this, "selected: "+LatestMovies, Toast.LENGTH_SHORT).show();


            }
        });
        radioButtonpopular.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String popularMovies = radioButtonpopular.getText().toString();
                updatedataref.child("video_type").setValue(popularMovies);
                updatedataref.child("video_slide").setValue("");
                Toast.makeText(UploadThumbnailActivity.this, "selected: "+popularMovies, Toast.LENGTH_SHORT).show();


            }
        });
        radioButtonSlide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String slideMovies = radioButtonSlide.getText().toString();
                updatedataref.child("video_slide").setValue(slideMovies);
                Toast.makeText(UploadThumbnailActivity.this, "selected: "+slideMovies, Toast.LENGTH_SHORT).show();
            }
        });

    }
    public void showimagechooser(View view){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,102);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 102 || resultCode == RESULT_OK || data.getData() != null ){
           videothumburi = data.getData();
           try {
               String thumbname = getFilename(videothumburi);
               textSelected.setText(thumbname);
               Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), videothumburi);
               thumbnail_image.setImageBitmap(bitmap);

           }catch (IOException e){
               e.printStackTrace();
           }
        }
    }

    private String getFilename (Uri uri){
        /*String result = null;
        if(uri.getScheme().equals("content")){
            Cursor cursor = getContentResolver().query(uri, null, null, null, null );
            try {
                if(cursor != null || cursor.moveToFirst()){
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));

                }
            }finally {
                cursor.close();
            }
        }
        if (result == null){
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1){
                result = result.substring(cut + 1);
            }
        }*/
        String result = null;
        String scheme = uri.getScheme();

        if (scheme.equals("file")) {
            result = uri.getLastPathSegment();
        }
        else if (scheme.equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
            }
        }
        return result;
    }

    private void uploadFiles(){
        if(videothumburi != null){
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("wait uploading thumbnail...");
            progressDialog.show();
            String video_title = getIntent().getExtras().getString("thumbnailsName");
            final StorageReference sRef = mStoragerefthumbnails.child(video_title + "." + getfileExtension(videothumburi));

            sRef.putFile(videothumburi).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    sRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            thumbnail_url = uri.toString();
                            updatedataref.child("video_thumb").setValue(thumbnail_url);
                            progressDialog.dismiss();
                            Toast.makeText(UploadThumbnailActivity.this,"Files uploaded",
                                    Toast.LENGTH_SHORT).show();



                        }
                    });

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    Toast.makeText(UploadThumbnailActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred()/ taskSnapshot.getTotalByteCount());
                    progressDialog.setMessage("uploaded " + ((int)progress) + " %...");



                }
            });
        }
    }

    public void uploadfiletofirebase(View v){
        if(textSelected.equals("no thumbnail selected")){
            Toast.makeText(this, "first select an Image", Toast.LENGTH_SHORT).show();
        /*}else {
            if(mStorageTask != null || mStorageTask.isInProgress())
            {
                Toast.makeText(this,"upload files allready in progress", Toast.LENGTH_SHORT).show();*/
            }else {
                uploadFiles();
            }


        }


    public String getfileExtension(Uri uri){
        ContentResolver cr = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(cr.getType(uri));


    }






}
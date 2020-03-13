package com.example.testopencv;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.FileNotFoundException;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    // Used for logging success or failure messages
    private static final String TAG = "OCVSample::Activity";
    private static final int GALLERY_REQUEST_CODE = 1889;
    private MenuItem mItemSwitchCamera = null;
    private ImageView imageView;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i("OpenCV", "OpenCV loaded successfully");
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };


    public MainActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.show_camera);

        imageView = (ImageView) findViewById(R.id.image1);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d("OpenCV", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d("OpenCV", "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }


    public void pickFromGallery(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        String[] mimeTypes= {"image/jpeg","image/png"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES,mimeTypes);
        // Launching the Intent
        startActivityForResult(intent,GALLERY_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Bitmap bitmap = null;
        if (resultCode == Activity.RESULT_OK)
            switch (requestCode){
                case GALLERY_REQUEST_CODE:
                    //data.getData returns the content URI for the selected Image
                    Uri selectedImage = data.getData();
                    try {
                        bitmap = Bitmap.createBitmap(MediaStore.Images.Media.getBitmap(getContentResolver(),selectedImage));
                    }
                    catch(FileNotFoundException f){
                    }
                    catch (IOException e) {

                    }
                    Mat image = new Mat(bitmap.getHeight(),bitmap.getWidth(),CvType.CV_8UC4);
                    Utils.bitmapToMat(bitmap,image);
                    Mat mask =new Mat(bitmap.getHeight(),bitmap.getWidth(),CvType.CV_8UC1);
                    Mat processedImage =new Mat(bitmap.getHeight(),bitmap.getWidth(),CvType.CV_8UC4);
                    Imgproc.cvtColor(image,processedImage,Imgproc.COLOR_RGB2HSV);
                    Core.inRange(processedImage,new Scalar(0,150,0),new Scalar(10,255,255),mask);
                    Core.bitwise_and(image,image,processedImage,mask);
                    Utils.matToBitmap(processedImage,bitmap);
                    imageView.setImageBitmap(bitmap);
                    break;
            }
    }
}

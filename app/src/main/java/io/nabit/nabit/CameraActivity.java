package io.nabit.nabit;

import android.content.Intent;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


public class CameraActivity extends ActionBarActivity {
    private static final String TAG = CameraActivity.class.getSimpleName();

    private static Camera mCamera;
    private static CameraPreview mPreview;
    private static CameraHandlerThread mCameraHandlerThread;

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        Log.d("MainThread", Thread.currentThread().getName());//debugging

        mCameraHandlerThread = CameraHandlerThread.getInstance();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_camera, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mCamera != null) {
            releaseCameraAndPreview();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        initializeCamera();

        initializeCameraView();
    }

    /** A safe way to get an instance of the Camera object. */
    private static void initializeCamera(){
        try {
            mCamera = Camera.open(); // attempt to get a Camera instance
        } catch (Exception e) {
            mCamera = null;
            Log.d(TAG, "Camera is not available for use. Need more graceful way to handle this in production");
            e.printStackTrace();
        }
    }

    private void initializeCameraView() {
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);

        if (mPreview == null) {
            mPreview = new CameraPreview(this, mCamera);

            // Add a listener to the Capture button
            Button captureButton = (Button) findViewById(R.id.button_capture);
            captureButton.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // get an image from the camera
                            mCamera.takePicture(null, null, mPicture);
                        }
                    }
            );
        } else {
            mPreview.setCamera(mCamera);
            mPreview.getHolder().addCallback(mPreview);

            preview.removeView(mPreview);
        }

        preview.addView(mPreview);
    }

    private void releaseCameraAndPreview() {
        if (mCamera != null) {
            mPreview.setCamera(null);
            mPreview.getHolder().removeCallback(mPreview);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            File pictureFile = MediaFiles.getOutputMediaFile(MEDIA_TYPE_IMAGE);
            if (pictureFile == null){
                Log.d(TAG, "Error creating media file, check storage permissions: ");
                return;
            }

            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
                
                galleryAddPic("file:"+pictureFile.getAbsolutePath());
            } catch (FileNotFoundException e) {
                Log.d(TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d(TAG, "Error accessing file: " + e.getMessage());
            }
        }
    };

    private void galleryAddPic(String currentPhotoPath) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(currentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

}

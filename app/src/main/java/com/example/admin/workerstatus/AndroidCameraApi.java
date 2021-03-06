package com.example.admin.workerstatus;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.location.Location;
import android.media.Image;
import android.media.ImageReader;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.firebase.storage.UploadTask.TaskSnapshot;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import br.com.safety.locationlistenerhelper.core.CurrentLocationListener;
import br.com.safety.locationlistenerhelper.core.CurrentLocationReceiver;
import br.com.safety.locationlistenerhelper.core.LocationTracker;

import static java.util.Arrays.asList;

public class AndroidCameraApi extends AppCompatActivity {

    private static final String TAG = "AndroidCameraApi";
    private Button takePictureButton;
    private TextureView textureView;
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    private String cameraId;
    protected CameraDevice cameraDevice;
    protected CameraCaptureSession cameraCaptureSessions;
    protected CaptureRequest captureRequest;
    protected CaptureRequest.Builder captureRequestBuilder;
    private Size imageDimension;
    private ImageReader imageReader;
    private File file;
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private boolean mFlashSupported;
    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;
    private StorageReference storageReference = FirebaseStorage.getInstance().getReference();
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
    private String mc2 = "nil";
    private CheckIn checkIn;
    private String status;

    private Calendar c = Calendar.getInstance();
    SimpleDateFormat dateformat = new SimpleDateFormat("dd-MM-yyyy");
    SimpleDateFormat timeformat = new SimpleDateFormat("hh:mm aaa");
    SimpleDateFormat dayformat = new SimpleDateFormat("EEEE");
    private String todayDate = dateformat.format(c.getTime());
    private String time = timeformat.format(c.getTime());
    private String day = dayformat.format(c.getTime());

    private String[] split = firebaseUser.getEmail().split("@");
    private String name = split[0];
    private String statuskey = "";
    private String location;

    private DatabaseReference dbrefCheckIn = FirebaseDatabase.getInstance().getReference().child("CheckIns");
    private DatabaseReference dbrefHours = FirebaseDatabase.getInstance().getReference().child("Hours");

    private ArrayList<String> arrayList = new ArrayList<>();

    private LocationTracker locationTracker = new LocationTracker("my.action");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_android_camera_api);

        TextView tvSmile = (TextView) findViewById(R.id.tvSmile);
        textureView = (TextureView) findViewById(R.id.texture);
        assert textureView != null;
        textureView.setSurfaceTextureListener(textureListener);
        takePictureButton = (Button) findViewById(R.id.btn_takepicture);

        Intent intent = getIntent();
        if( getIntent().getExtras() != null)
        {
            String mc = intent.getStringExtra("mc");
            if(mc!=null) {
                String key [] = mc.split(",");
                String mckey = key[0];
                location = key[1];

                if (mckey.equals("mc")) {
                    mc2 = "mc";
                    checkIn = new CheckIn(name, time, todayDate, "on MC", false, day, location);

                    takePictureButton.setText("Take picture of MC");
                    tvSmile.setText("Please take a clear image of the MC");

                    status = "CheckIn";

                } else {
                    mc2 = "no mc";
                    if(c.get(Calendar.MINUTE) > 0){
                        c.set(Calendar.MINUTE, 0);
                        c.set(Calendar.HOUR, c.get(Calendar.HOUR) + 1);
                        String time2 = timeformat.format(c.getTime());
                        checkIn = new CheckIn(name, time2, todayDate, "working", false, day, location);
                    }
                    else{
                        checkIn = new CheckIn(name, time, todayDate, "working", false, day, location);
                    }

                    status = "CheckIn";
                }
            }

            String checkout = intent.getStringExtra("checkout");
            if(checkout!=null){
                if(checkout.equals("checkout")){
                    takePictureButton.setText("Take picture for check out");

                    status = "CheckOut";
                }
            }
        }

        assert takePictureButton != null;
        takePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePicture();
            }
        });

    }


    TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            //open your camera here
            openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            // Transform you image captured size according to the surface width and height
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        }
    };

    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            //This is called when the camera is open
            Log.e(TAG, "onOpened");
            cameraDevice = camera;
            createCameraPreview();
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            cameraDevice.close();
        }

        @Override
        public void onError(CameraDevice camera, int error) {
//            cameraDevice.close();
//            cameraDevice = null;
            camera.close();
            camera = null;
        }
    };

    final CameraCaptureSession.CaptureCallback captureCallbackListener = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
            Toast.makeText(AndroidCameraApi.this, "Saved:" + file, Toast.LENGTH_SHORT).show();
            createCameraPreview();
        }
    };

    protected void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("Camera Background");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    protected void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    protected void takePicture() {
        if (null == cameraDevice) {
            Log.e(TAG, "cameraDevice is null");
            return;
        }
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraDevice.getId());
            Size[] jpegSizes = null;
            if (characteristics != null) {
                jpegSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG);
            }
            int width = 640;
            int height = 480;
            if (jpegSizes != null && 0 < jpegSizes.length) {
                width = jpegSizes[0].getWidth();
                height = jpegSizes[0].getHeight();
            }
            ImageReader reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1);
            List<Surface> outputSurfaces = new ArrayList<Surface>(2);
            outputSurfaces.add(reader.getSurface());
            outputSurfaces.add(new Surface(textureView.getSurfaceTexture()));
            final CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(reader.getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

            if(mc2.equals("mc")) {
                captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, 90);
            }
            else {
                captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, 270);
            }
            final File file = new File(Environment.getExternalStorageDirectory() + "/image.jpg");
            ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    Image image = null;
                    try {
                        image = reader.acquireLatestImage();
                        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                        byte[] bytes = new byte[buffer.capacity()];
                        buffer.get(bytes);
                        save(bytes);

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (image != null) {
                            image.close();
                        }
                    }
                }

                private void save(byte[] bytes) throws IOException {
                    OutputStream output = null;
                    try {
                        output = new FileOutputStream(file);
                        output.write(bytes);
                    } finally {
                        if (null != output) {
                            output.close();
                            uploadImage(file);

                            pushCheckin(new OnGetDataListener() {
                                @Override
                                public void onSuccess(ArrayList arrayList, HashMap hashMap) {

                                    if(arrayList.contains(name)){
                                        System.out.println("name exist do not run push");

                                        if(mc2.equals("mc")) {
                                            Iterator it = hashMap.entrySet().iterator();
                                            while (it.hasNext()) {
                                                Map.Entry pair = (Map.Entry) it.next();
                                                if(pair.getKey().equals(name)){
                                                    String key = pair.getValue().toString();

                                                    dbrefCheckIn.child(key).setValue(checkIn);
                                                }
                                                break;
                                            }
                                        }
                                        else if(status.equals("CheckOut")){

                                            Iterator it = hashMap.entrySet().iterator();
                                            while (it.hasNext()) {
                                                Map.Entry pair = (Map.Entry) it.next();
                                                if(pair.getKey().equals(name)){
                                                    System.out.println("a1");
                                                    final String key = pair.getValue().toString();

                                                    dbrefCheckIn.child(key).child("checkout").setValue(time);
                                                    stopTracker();


                                                    //calculate hour
                                                    dbrefCheckIn.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                            System.out.println("in here 1");
                                                            final CheckIn checkIn = dataSnapshot.getValue(CheckIn.class);

                                                            System.out.println(checkIn.getCheckin() + "time in");
                                                            System.out.println(time + "time out");

                                                            try {
                                                                System.out.println("in here 2");
                                                                SimpleDateFormat format = new SimpleDateFormat("hh:mm aa");
                                                                Date Date1 = format.parse(checkIn.getCheckin());
                                                                Date Date2 = format.parse(time);
                                                                long mills = Date2.getTime() - Date1.getTime();
                                                                int Hours = (int) (mills/(1000 * 60 * 60));
                                                                double Mins = (int) (mills/(1000*60)) % 60;

                                                                if(Mins > 40){
                                                                    //round up to 1
                                                                    Mins = 0;
                                                                    Hours =+ 1;
                                                                }
                                                                else if(Mins >= 30 && Mins <= 40){
                                                                    //round up to 45
                                                                    Mins = 0.5;
                                                                }
                                                                else if(Mins >= 11 && Mins <= 30){
                                                                    Mins = 0.5;
                                                                }
                                                                else if(Mins <= 10){
                                                                    Mins = 0;
                                                                }

                                                                final double diff = Hours + Mins; // updated value every1 second
                                                                System.out.println("time difference is : " + diff);

                                                                dbrefCheckIn.child(key).child("hours").setValue(diff);

                                                                final List<String> Weekdays = asList("Monday", "Tuesday", "Wednesday", "Thursday", "Friday");

                                                                pushHour(new OnGetHourDataListener() {
                                                                    @Override
                                                                    public void onSuccess(ArrayList arrayList, HashMap hashMap) {

                                                                        final String[] split = todayDate.split("-");
                                                                        String newDate = split[1] + "-" + split[2];
                                                                        if(arrayList.contains(newDate)){
                                                                            System.out.println("called 1");
                                                                            //edit and add on
//                                                                            Iterator it = hashMap.entrySet().iterator();
//                                                                            while (it.hasNext()) {
//                                                                                Map.Entry pair = (Map.Entry) it.next();
//                                                                                if(pair.getKey().equals(name)){
//                                                                                    final String key = pair.getValue().toString();

                                                                            Iterator it = hashMap.entrySet().iterator();
                                                                            while(it.hasNext()){

                                                                                Map.Entry pair = (Map.Entry) it.next();
                                                                                if(pair.getKey().equals(newDate)){
                                                                                    System.out.println("called 2");
                                                                                    final String keyHour = pair.getValue().toString();
                                                                                    dbrefHours.child(keyHour).addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                        @Override
                                                                                        public void onDataChange(DataSnapshot dataSnapshot) {

                                                                                            Double normalHours = 0.0;
                                                                                            Double otHours = 0.0;
                                                                                            if(Weekdays.contains(checkIn.getDay())){
                                                                                                if(diff >= 8){
                                                                                                    normalHours = 8.0;
                                                                                                    otHours = diff - 8.0;

                                                                                                }
                                                                                                else{
                                                                                                    normalHours = diff;
                                                                                                }
                                                                                            }

                                                                                            Hours hour = dataSnapshot.getValue(Hours.class);

                                                                                            double normalHour = Double.valueOf(hour.getNormal());
                                                                                            String finalnormalHour = String.valueOf(normalHour + normalHours);

                                                                                            double otHour = Double.valueOf(hour.getOvertime());
                                                                                            String finalotHour = String.valueOf(otHour + otHours);

                                                                                            dbrefHours.child(keyHour).child("normal").setValue(finalnormalHour);
                                                                                            dbrefHours.child(keyHour).child("overtime").setValue(finalotHour);

                                                                                        }

                                                                                        @Override
                                                                                        public void onCancelled(DatabaseError databaseError) {

                                                                                        }
                                                                                    });
                                                                                }
                                                                                break;
                                                                            }
                                                                        }
                                                                        else{
                                                                            //push new

                                                                            Double newnormalHours = 0.0;
                                                                            Double newotHours = 0.0;
                                                                            if(Weekdays.contains(checkIn.getDay())){
                                                                                if(diff >= 8){
                                                                                    newnormalHours = 8.0;
                                                                                    newotHours = diff - 8.0;

                                                                                }
                                                                                else{
                                                                                    newnormalHours = diff;
                                                                                }
                                                                            }

                                                                            Hours hours = new Hours(name, newDate, String.valueOf(newnormalHours), String.valueOf(newotHours), "0");
                                                                            dbrefHours.push().setValue(hours);
                                                                        }
                                                                    }
                                                                });

                                                            } catch (ParseException e) {
                                                                e.printStackTrace();
                                                            }



                                                        }

                                                        @Override
                                                        public void onCancelled(DatabaseError databaseError) {

                                                        }
                                                    });
                                                    break;
                                                }
                                            }
                                        }
                                    }

                                    else{
                                        System.out.println("name not found you can push");
                                        System.out.println(arrayList.size());
                                        dbrefCheckIn.push().setValue(checkIn);
                                        startTracker();
                                    }
                                }
                            });
                        }
                    }
                }
            };
            reader.setOnImageAvailableListener(readerListener, mBackgroundHandler);
            final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
//                    Toast.makeText(AndroidCameraApi.this, "Saved:" + file, Toast.LENGTH_SHORT).show();
                    createCameraPreview();
                }
            };

            cameraDevice.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    try {
                        session.capture(captureBuilder.build(), captureListener, mBackgroundHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {
                }
            }, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    protected void createCameraPreview() {
        try {
            SurfaceTexture texture = textureView.getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(imageDimension.getWidth(), imageDimension.getHeight());
            Surface surface = new Surface(texture);
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);
            cameraDevice.createCaptureSession(asList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    //The camera is already closed
                    if (null == cameraDevice) {
                        return;
                    }
                    // When the session is ready, we start displaying the preview.
                    cameraCaptureSessions = cameraCaptureSession;
                    updatePreview();
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Toast.makeText(AndroidCameraApi.this, "Configuration change", Toast.LENGTH_SHORT).show();
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void openCamera() {
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        Log.e(TAG, "is camera open");
        try {
            if(mc2!=null) {
                if (mc2.equals("mc")) {
                    cameraId = manager.getCameraIdList()[0];
                } else {
                    cameraId = manager.getCameraIdList()[1];
                }
            }

            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            assert map != null;
            imageDimension = map.getOutputSizes(SurfaceTexture.class)[0];
            // Add permission for camera and let user grant the permission
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(AndroidCameraApi.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
                return;
            }
            manager.openCamera(cameraId, stateCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        Log.e(TAG, "openCamera X");
    }

    protected void updatePreview() {
        if (null == cameraDevice) {
            Log.e(TAG, "updatePreview error, return");
        }
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        try {
            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void closeCamera() {
        if (null != cameraDevice) {
            cameraDevice.close();
            cameraDevice = null;
        }
        if (null != imageReader) {
            imageReader.close();
            imageReader = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                // close the app
                Toast.makeText(AndroidCameraApi.this, "Sorry!!!, you can't use this app without granting permission", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "onResume");
        startBackgroundThread();

        if (textureView.isAvailable()) {
            openCamera();
        } else {
            textureView.setSurfaceTextureListener(textureListener);
        }
    }

    @Override
    protected void onPause() {
        Log.e(TAG, "onPause");
        //closeCamera();
        stopBackgroundThread();
        super.onPause();
    }



    private void uploadImage(File file) {

        if(status.equals("CheckIn")){
            statuskey = "checkin";
        }
        else if(status.equals("CheckOut")){
            statuskey = "checkout";
        }

        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        final String todayDate = df.format(c.getTime());


        //displaying a progress dialog while upload is going on
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Saving");
        progressDialog.show();

        Uri file2 = Uri.fromFile(file.getAbsoluteFile());
        StorageReference riversRef = storageReference.child(todayDate + "/" + statuskey.concat("-") + name +".jpg");
        riversRef.putFile(file2)
                .addOnSuccessListener(new OnSuccessListener<TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //if the upload is successfull
                        //hiding the progress dialog
                        progressDialog.dismiss();

                        //and displaying a success toast
                        Toast.makeText(getApplicationContext(), "Thank you, attendance has been taken!", Toast.LENGTH_LONG).show();
                        Intent i = new Intent(getBaseContext(), AttendanceActivity.class);
                        i.putExtra("key", "fromCam");
                        startActivity(i);
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        //if the upload is not successfull
                        //hiding the progress dialog
                        progressDialog.dismiss();

                        //and displaying error message
                        Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
        }

    private void startTracker(){

        locationTracker
                .setInterval(1000 * 60 * 60)
                .setGps(true)
                .setNetWork(false)
                .start(getBaseContext(), AndroidCameraApi.this);

        System.out.println("called it");
    }

    private void stopTracker(){

        locationTracker
//                .setInterval(50000)
                .setGps(true)
                .setNetWork(false)
                 .currentLocation(new CurrentLocationReceiver(new CurrentLocationListener() {

                            @Override
                            public void onCurrentLocation(Location location) {
                               Log.d("callback", ":onCurrentLocation" + location.getLongitude());
                               locationTracker.stopLocationService(getBaseContext());
                            }

                            @Override
                            public void onPermissionDiened() {
                                Log.d("callback", ":onPermissionDiened");
                                locationTracker.stopLocationService(getBaseContext());
                            }
                 }))

                .start(getBaseContext(), this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        closeCamera();
    }

    public interface OnGetDataListener {
        //make new interface for call back
        void onSuccess(ArrayList arrayList, HashMap hashMap);
    }

    private void pushCheckin(final OnGetDataListener listener){

        dbrefCheckIn.orderByChild("date").equalTo(todayDate).addListenerForSingleValueEvent(new ValueEventListener() {
            ArrayList<String> arrayList = new ArrayList<String>();
            HashMap<String, String> hashMap = new HashMap<>();

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
                    CheckIn checkIn = childDataSnapshot.getValue(CheckIn.class);
                    arrayList.add(checkIn.getName());
                    hashMap.put(checkIn.getName(), childDataSnapshot.getKey());
                    System.out.println("in pushCheckin");
                }
                listener.onSuccess(arrayList, hashMap);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

//        //check if it exists first then push
//        dbrefCheckIn.orderByChild("date").equalTo(todayDate).addValueEventListener(new ValueEventListener() {
//            ArrayList<String> arrayList = new ArrayList<String>();
//            HashMap<String, String> hashMap = new HashMap<>();
//
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
//                    CheckIn checkIn = childDataSnapshot.getValue(CheckIn.class);
//                    arrayList.add(checkIn.getName());
//                    hashMap.put(checkIn.getName(), childDataSnapshot.getKey());
//                    System.out.println("in pushCheckin");
//                }
//                listener.onSuccess(arrayList, hashMap);
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
    }

    public interface OnGetHourDataListener {
        //make new interface for call back
        void onSuccess(ArrayList arrayList, HashMap hashMap);
    }

    private void pushHour(final OnGetHourDataListener listener){

        dbrefHours.orderByChild("name").equalTo(name).addListenerForSingleValueEvent(new ValueEventListener() {
            ArrayList<String> arrayList = new ArrayList<String>();
            HashMap<String, String> hashMap = new HashMap<>();

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
                    Hours hour = childDataSnapshot.getValue(Hours.class);
                    arrayList.add(hour.getMonth());
                    hashMap.put(hour.getMonth(), childDataSnapshot.getKey());
                }
                listener.onSuccess(arrayList, hashMap);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

//        dbrefHours.orderByChild("name").equalTo(name).addValueEventListener(new ValueEventListener() {
//            ArrayList<String> arrayList = new ArrayList<String>();
//            HashMap<String, String> hashMap = new HashMap<>();
//
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
//                    Hours hour = childDataSnapshot.getValue(Hours.class);
//                    arrayList.add(hour.getMonth());
//                    hashMap.put(hour.getMonth(), childDataSnapshot.getKey());
//                }
//                listener.onSuccess(arrayList, hashMap);
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
    }

}

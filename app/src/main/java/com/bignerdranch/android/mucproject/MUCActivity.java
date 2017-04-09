package com.bignerdranch.android.mucproject;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Environment;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.indooratlas.android.sdk.IALocation;
import com.indooratlas.android.sdk.IALocationListener;
import com.indooratlas.android.sdk.IALocationManager;
import com.indooratlas.android.sdk.IALocationRequest;
import com.indooratlas.android.sdk.IARegion;
import com.indooratlas.android.sdk.resources.IAFloorPlan;
import com.indooratlas.android.sdk.resources.IALatLng;
import com.indooratlas.android.sdk.resources.IAResourceManager;
import com.indooratlas.android.sdk.resources.IAResult;
import com.indooratlas.android.sdk.resources.IAResultCallback;
import com.indooratlas.android.sdk.resources.IATask;

import java.io.File;

public class MUCActivity extends AppCompatActivity {

    private IALocationManager mIALocationManager;
    private static final String TAG = "MUCProject";
    private static final String POSITION_TAG = "Position";
    private Position mPos;
    private Position [] pointsOfInterest = new Position[] {
            new Position(51.521703, -0.130002),
            new Position(51.521734, -0.130012),
            new Position(51.521796, -0.130082),
            new Position(51.521817, -0.130105),
            new Position(51.521806, -0.130206)
    };

    private IAResourceManager mFloorPlanManager;
    private ImageView mFloorPlanImage;
    private static final float dotRadius = 1.0f;
    private BlueDotView mImageView;
    private long mDownloadId;
    private DownloadManager mDownloadManager;
    private IATask<IAFloorPlan> mPendingAsyncResult;
    private IAFloorPlan mFloorPlan;

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference myRef = database.getReference("Position");

    private IALocationListener mIALocationListener = new IALocationListener() {

        // Called when the location has changed.
        @Override
        public void onLocationChanged(IALocation location) {
            mPos = new Position(location.getLatitude(), location.getLongitude(), location.getTime());
            Log.d(TAG, "Latitude: " + location.getLatitude());
            Log.d(TAG, "Longitude: " + location.getLongitude());
            Log.d(TAG, "Time: " + location.getTime());
            if (mImageView != null && mImageView.isReady()) {
                IALatLng latLng = new IALatLng(location.getLatitude(), location.getLongitude());
                PointF point = mFloorPlan.coordinateToPoint(latLng);
                mImageView.setDotCenter(point);
                mImageView.postInvalidate();
            }

            //check if device is close to a point of interest
            for (int i = 0; i < 5; i++) {
                int messageResId = 0;
                if (i == 0) {
                    //Entrance to 404/405
                    Log.d(TAG, "Position comparison: " + mPos.isPositionWithinRange(pointsOfInterest[i]));
                    if(mPos.isPositionWithinRange(pointsOfInterest[i])){
                        messageResId = R.string.labs_404_405;
                        Toast.makeText(MUCActivity.this, messageResId, Toast.LENGTH_SHORT).show();
                    }
                } else if (i == 1) {
                    //Entrance to 403
                    Log.d(TAG, "Position comparison: " + mPos.isPositionWithinRange(pointsOfInterest[i]));
                    if(mPos.isPositionWithinRange(pointsOfInterest[i])){
                        messageResId = R.string.lab_403;
                        Toast.makeText(MUCActivity.this, messageResId, Toast.LENGTH_SHORT).show();
                    }
                } else if (i == 2) {
                    //Main Entrance to Labs
                    Log.d(TAG, "Position comparison: " + mPos.isPositionWithinRange(pointsOfInterest[i]));
                    if(mPos.isPositionWithinRange(pointsOfInterest[i])){
                        messageResId = R.string.main_entrance_labs;
                        Toast.makeText(MUCActivity.this, messageResId, Toast.LENGTH_SHORT).show();
                    }
                } else if (i == 3) {
                    //Stairs
                    Log.d(TAG, "Position comparison: " + mPos.isPositionWithinRange(pointsOfInterest[i]));
                    if(mPos.isPositionWithinRange(pointsOfInterest[i])){
                        messageResId = R.string.stairs;
                        Toast.makeText(MUCActivity.this, messageResId, Toast.LENGTH_SHORT).show();
                    }
                } else if (i == 4) {
                    //Lifts A/B
                    Log.d(TAG, "Position comparison: " + mPos.isPositionWithinRange(pointsOfInterest[i]));
                    if(mPos.isPositionWithinRange(pointsOfInterest[i])){
                        messageResId = R.string.lifts;
                        Toast.makeText(MUCActivity.this, messageResId, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    //No location matched
                    Log.d(TAG, "Position comparison: " + mPos.isPositionWithinRange(pointsOfInterest[i]));
                }

                //Insert Position Object in Firebase
                myRef.push().setValue(mPos);
            }
        }
            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }
        };

    private IARegion.Listener mRegionListener = new IARegion.Listener() {
        @Override
        public void onEnterRegion(IARegion region) {
            if (region.getType() == IARegion.TYPE_FLOOR_PLAN) {
                String id = region.getId();
                Log.d(TAG, "floorPlan changed to " + id);
                Toast.makeText(MUCActivity.this, id, Toast.LENGTH_SHORT).show();
                fetchFloorPlan(id);
            }
        }

        @Override
        public void onExitRegion(IARegion region) {
            // leaving a previously entered region
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_muc);

        Log.d(TAG, "onCreate() called");

        mImageView = (BlueDotView) findViewById(R.id.imageView);
        mDownloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        mIALocationManager = IALocationManager.create(this);
        mFloorPlanImage = (ImageView) findViewById(R.id.image);
        mFloorPlanManager = IAResourceManager.create(this);


    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume() called");
        IALocationRequest mRequest = IALocationRequest.create();
        //start receiving location updates every 1 second
        mRequest.setFastestInterval(1000);
        mIALocationManager.requestLocationUpdates(mRequest, mIALocationListener);
        mIALocationManager.registerRegionListener(mRegionListener);
    }


    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause() called");
        mIALocationManager.removeLocationUpdates(mIALocationListener);
        mIALocationManager.unregisterRegionListener(mRegionListener);
    }


    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy() called");
        mIALocationManager.destroy();
        super.onDestroy();
    }

    /*  Broadcast receiver for floor plan image download */
    private BroadcastReceiver onComplete = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0L);
            if (id != mDownloadId) {
                Log.w(TAG, "Ignore unrelated download");
                return;
            }
            Log.w(TAG, "Image download completed");
            Bundle extras = intent.getExtras();
            DownloadManager.Query q = new DownloadManager.Query();
            q.setFilterById(extras.getLong(DownloadManager.EXTRA_DOWNLOAD_ID));
            Cursor c = mDownloadManager.query(q);

            if (c.moveToFirst()) {
                int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
                if (status == DownloadManager.STATUS_SUCCESSFUL) {
                    // process download
                    String filePath = c.getString(c.getColumnIndex(
                            DownloadManager.COLUMN_LOCAL_FILENAME));
                    showFloorPlanImage(filePath);
                }
            }
            c.close();
        }
    };


    private void fetchFloorPlan(String id) {
        cancelPendingNetworkCalls();
        final IATask<IAFloorPlan> asyncResult = mFloorPlanManager.fetchFloorPlanWithId(id);
        mPendingAsyncResult = asyncResult;
        if (mPendingAsyncResult != null) {
            mPendingAsyncResult.setCallback(new IAResultCallback<IAFloorPlan>() {
                @Override
                public void onResult(IAResult<IAFloorPlan> result) {
                    Log.d(TAG, "fetch floor plan result:" + result);
                    if (result.isSuccess() && result.getResult() != null) {
                        mFloorPlan = result.getResult();
                        String fileName = mFloorPlan.getId() + ".img";
                        String filePath = Environment.getExternalStorageDirectory() + "/"
                                + Environment.DIRECTORY_DOWNLOADS + "/" + fileName;
                        File file = new File(filePath);
                        if (!file.exists()) {
                            DownloadManager.Request request =
                                    new DownloadManager.Request(Uri.parse(mFloorPlan.getUrl()));
                            request.setDescription("IndoorAtlas floor plan");
                            request.setTitle("Floor plan");
                            request.setDestinationInExternalPublicDir(Environment.
                                    DIRECTORY_DOWNLOADS, fileName);

                            mDownloadId = mDownloadManager.enqueue(request);
                        } else {
                            showFloorPlanImage(filePath);
                        }
                    } else {
                        // do something with error
                        if (!asyncResult.isCancelled()) {
                            Toast.makeText(MUCActivity.this,
                                    (result.getError() != null
                                            ? "error loading floor plan: " + result.getError()
                                            : "access to floor plan denied"), Toast.LENGTH_LONG)
                                    .show();
                        }
                    }
                }
            }, Looper.getMainLooper());
        }
    }

    private void cancelPendingNetworkCalls() {
        if (mPendingAsyncResult != null && !mPendingAsyncResult.isCancelled()) {
            mPendingAsyncResult.cancel();
        }
    }

    private void showFloorPlanImage(String filePath) {
        Log.w(TAG, "showFloorPlanImage: " + filePath);
        mImageView.setRadius(mFloorPlan.getMetersToPixels() * dotRadius);
        mImageView.setImage(ImageSource.uri(filePath));
    }

}

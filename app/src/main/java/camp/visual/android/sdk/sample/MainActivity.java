package camp.visual.android.sdk.sample;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;

import android.os.Handler;
import android.os.HandlerThread;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import camp.visual.android.sdk.sample.view.CalibrationViewer;
import camp.visual.android.sdk.sample.view.PointView;
import camp.visual.eyedid.gazetracker.GazeTracker;
import camp.visual.eyedid.gazetracker.callback.CalibrationCallback;
import camp.visual.eyedid.gazetracker.callback.InitializationCallback;
import camp.visual.eyedid.gazetracker.callback.StatusCallback;
import camp.visual.eyedid.gazetracker.callback.TrackingCallback;
import camp.visual.eyedid.gazetracker.constant.CalibrationModeType;
import camp.visual.eyedid.gazetracker.constant.GazeTrackerOptions;
import camp.visual.eyedid.gazetracker.constant.StatusErrorType;
import camp.visual.eyedid.gazetracker.metrics.BlinkInfo;
import camp.visual.eyedid.gazetracker.metrics.FaceInfo;
import camp.visual.eyedid.gazetracker.metrics.GazeInfo;
import camp.visual.eyedid.gazetracker.metrics.UserStatusInfo;
import camp.visual.eyedid.gazetracker.metrics.state.TrackingState;
import camp.visual.eyedid.gazetracker.util.ViewLayoutChecker;

public class MainActivity extends AppCompatActivity {
  private GazeTracker gazeTracker;
  private final String EYEDID_SDK_LICENSE = "typo your license key";
  private final CalibrationModeType calibrationType = CalibrationModeType.DEFAULT;
  private final String[] PERMISSIONS = new String[]{
      Manifest.permission.CAMERA
  };
  private final int REQ_PERMISSION = 1000;

  private View layoutProgress;
  private PointView viewPoint;
  private boolean skipProgress = false;
  private Button btnStartTracking, btnStopTracking, btnStartCalibration;
  private CalibrationViewer viewCalibration;
  private final ViewLayoutChecker viewLayoutChecker = new ViewLayoutChecker();
  private Handler backgroundHandler;
  private final HandlerThread backgroundThread = new HandlerThread("background");

  private final TrackingCallback trackingCallback = new TrackingCallback() {
    @Override
    public void onMetrics(long timestamp, GazeInfo gazeInfo, FaceInfo faceInfo, BlinkInfo blinkInfo,
        UserStatusInfo userStatusInfo) {
      if (gazeInfo.trackingState == TrackingState.SUCCESS) {
        viewPoint.setPosition(gazeInfo.x, gazeInfo.y);
      }
    }

    @Override
    public void onDrop(long timestamp) {
      Log.d("MainActivity", "drop frame " + timestamp);
    }
  };

  private boolean isFirstPoint = false;

  private final CalibrationCallback calibrationCallback = new CalibrationCallback() {

    @Override
    public void onCalibrationProgress(float progress) {
      if (!skipProgress)  {
        runOnUiThread(() -> viewCalibration.setPointAnimationPower(progress));
      }
    }

    @Override
    public void onCalibrationNextPoint(final float x, final float y) {
      runOnUiThread(() -> {
        viewCalibration.setVisibility(View.VISIBLE);
        if (isFirstPoint) {
          // viewCalibration.changeDraw(false, "Stare at this red point.");
          backgroundHandler.postDelayed(() -> showCalibrationPointView(x, y), 2500);
        } else {
          showCalibrationPointView(x, y);
        }
      });
    }

    @Override
    public void onCalibrationFinished(double[] calibrationData) {
      // When calibration is finished, calibration data is stored to SharedPreference
      hideCalibrationView();
      showToast("calibrationFinished", true);
    }

    @Override
    public void onCalibrationCanceled(double[] doubles) {
      showToast("calibrationCanceled", true);
    }
  };

  private final StatusCallback statusCallback = new StatusCallback() {
    @Override
    public void onStarted() {
      // isTracking true
      // When if camera stream starting
      runOnUiThread(() -> {
        btnStartTracking.setEnabled(false);
        btnStopTracking.setEnabled(true);
        btnStartCalibration.setEnabled(true);
      });

    }

    @Override
    public void onStopped(StatusErrorType error) {
      // isTracking false
      // When if camera stream stopping
      runOnUiThread(() -> {
        btnStartTracking.setEnabled(true);
        btnStopTracking.setEnabled(false);
        btnStartCalibration.setEnabled(false);
      });
      if (error != StatusErrorType.ERROR_NONE) {
        if (error == StatusErrorType.ERROR_CAMERA_START) {// When if camera stream can't start
          showToast("ERROR_CAMERA_START ", false);
        } else if (error
            == StatusErrorType.ERROR_CAMERA_INTERRUPT) {// When if camera stream interrupted
          showToast("ERROR_CAMERA_INTERRUPT ", false);
        }
      }
    }
  };

  private final View.OnClickListener onClickListener = new View.OnClickListener() {
    @Override
    public void onClick(View v) {
      if(gazeTracker != null) {
        if (v == btnStartTracking) {
          gazeTracker.startTracking();
        } else if (v == btnStopTracking) {
          gazeTracker.stopTracking();
        } else if (v == btnStartCalibration) {
          startCalibration();
        }
      }
    }
  };

  private final InitializationCallback initializationCallback = (gazeTracker, error) -> {
    if (gazeTracker == null) {
      showToast("error : " + error.name(), true);
    } else {
      this.gazeTracker = gazeTracker;
      this.gazeTracker.setTrackingCallback(trackingCallback);
      this.gazeTracker.setCalibrationCallback(calibrationCallback);
      this.gazeTracker.setStatusCallback(statusCallback);
      this.btnStartTracking.setEnabled(true);
    }
    hideProgress();
  };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    EdgeToEdge.enable(this);
    setContentView(R.layout.activity_main);
    ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
      Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
      v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
      return insets;
    });
    initViews();
    checkPermission(); // needs camera permission.
    backgroundThread.start();
    backgroundHandler = new Handler(backgroundThread.getLooper());
  }

  private void initViews() {
    TextView txtSDKVersion = findViewById(R.id.txt_sdk_version);
    txtSDKVersion.setText(GazeTracker.getVersionName());
    layoutProgress = findViewById(R.id.layout_progress);
    viewCalibration = findViewById(R.id.view_calibration);
    viewPoint = findViewById(R.id.view_point);
    btnStartTracking = findViewById(R.id.btn_start_tracking);
    btnStartTracking.setOnClickListener(onClickListener);
    btnStopTracking = findViewById(R.id.btn_stop_tracking);
    btnStopTracking.setOnClickListener(onClickListener);
    btnStartCalibration = findViewById(R.id.btn_start_calibration);
    btnStartCalibration.setOnClickListener(onClickListener);
    btnStartTracking.setEnabled(false);
    btnStopTracking.setEnabled(false);
    btnStartCalibration.setEnabled(false);
    viewPoint.setPosition(-999,-999);
    viewLayoutChecker.setOverlayView(viewPoint, (x, y) -> {
      viewPoint.setOffset(x, y);
      viewCalibration.setOffset(x, y);
    });
  }

  private void checkPermission() {
    // Check permission status
    if (hasPermissions()) {
      checkPermission(true);
    } else {
      ActivityCompat.requestPermissions(this, PERMISSIONS, REQ_PERMISSION);
    }
  }

  private boolean hasPermissions() {
    int result;
    // Check permission status in string array
    for (String perms : PERMISSIONS) {
      if (perms.equals(Manifest.permission.SYSTEM_ALERT_WINDOW)) {
        if (!Settings.canDrawOverlays(this)) {
          return false;
        }
      }
      result = ContextCompat.checkSelfPermission(this, perms);
      if (result == PackageManager.PERMISSION_DENIED) {
        // When if unauthorized permission found
        return false;
      }
    }
    // When if all permission allowed
    return true;
  }

  private void checkPermission(boolean isGranted) {
    if (!isGranted) {
      showToast("not granted permissions", true);
      finish();
    } else {
      permissionGranted();
    }
  }

  private void showToast(final String msg, final boolean isShort) {
    runOnUiThread(() -> Toast.makeText(MainActivity.this, msg,
        isShort ? Toast.LENGTH_SHORT : Toast.LENGTH_LONG).show());
  }

  private void permissionGranted() {
    showProgress();
    initTracker();
  }

  private void initTracker() {
    GazeTrackerOptions options = new GazeTrackerOptions.Builder().build();
    GazeTracker.initGazeTracker(this, EYEDID_SDK_LICENSE, initializationCallback, options);
  }

  private void showProgress() {
    if (layoutProgress != null) {
      runOnUiThread(() -> {
        layoutProgress.setVisibility(View.VISIBLE);
      });
    }
  }

  private void hideProgress() {
    if (layoutProgress != null) {
      runOnUiThread(() -> {
        layoutProgress.setVisibility(View.GONE);
      });
    }
  }

  private void hideCalibrationView() {
    runOnUiThread(() -> {
      viewCalibration.setVisibility(View.INVISIBLE);
      btnStartCalibration.setEnabled(true);
      viewPoint.setVisibility(View.VISIBLE);
    });
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
      @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if (requestCode == REQ_PERMISSION) {
      if (grantResults.length > 0) {
        boolean cameraPermissionAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
        checkPermission(cameraPermissionAccepted);
      }
    }
  }

  private void showCalibrationPointView(final float x, final float y){
    skipProgress = true;
    viewCalibration.setPointAnimationPower(0);
    viewCalibration.setEnableText(false);
    viewCalibration.nextPointColor();
    viewCalibration.setPointPosition(x, y);
    long delay;

    if (isFirstPoint) {
     delay = 0;
    } else {
      delay = 1200;
    }

    backgroundHandler.postDelayed(() -> {
      if(gazeTracker != null)
        gazeTracker.startCollectSamples();
      skipProgress = false;
    }, delay);

    isFirstPoint = false;
  }

  private void startCalibration() {
    if (gazeTracker == null) return;
    boolean isSuccess = gazeTracker.startCalibration(calibrationType);


    if (isSuccess) {

      isFirstPoint = true;
      runOnUiThread(() -> {
        viewCalibration.setPointPosition(-9999, -9999);
        viewCalibration.setEnableText(true);
        viewPoint.setVisibility(View.INVISIBLE);
        btnStartCalibration.setEnabled(false);
          });

    } else {
      showToast("calibration start fail", false);
    }
  }
}

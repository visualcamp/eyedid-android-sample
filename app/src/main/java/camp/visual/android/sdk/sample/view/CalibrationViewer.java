package camp.visual.android.sdk.sample.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import camp.visual.android.sdk.sample.R;

public class CalibrationViewer extends ViewGroup {

  // Default color constants
  private static final int DEFAULT_BACKGROUND_COLOR = Color.rgb(0x64, 0x5E, 0x5E);
  private static final int DEFAULT_TEXT_COLOR = Color.WHITE;
  private static final float DEFAULT_TEXT_SIZE_SP = 16f;

  private final String TAG = "CalibrationViewer";
  // Array of point colors
  private int[] pointColors = {
      Color.rgb(0xEF, 0x53, 0x50), // Red
      Color.rgb(0xAB, 0x47, 0xBC), // Purple
      Color.rgb(0xFF, 0xA7, 0x26), // Orange
      Color.rgb(0x42, 0xA5, 0xF5), // Blue
      Color.rgb(0x66, 0xBB, 0x6A), // Green
      Color.rgb(0xCA, 0x9A, 0x00), // Brown
      Color.rgb(0xFF, 0xFD, 0x00)  // Yellow
  };

  private int currentColorIndex = 0;

  private Paint backgroundPaint;
  private TextPaint textPaint;
  private String message = "Please stare at this point.";
  private boolean isEnableText = true;
  private CalibrationPoint calibrationPoint;

  private float offsetX = 0, offsetY = 0;

  public CalibrationViewer(Context context) {
    super(context);
    init(context, null);
  }

  public CalibrationViewer(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context, attrs);
  }

  public CalibrationViewer(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(context, attrs);
  }

  private void init(Context context, AttributeSet attrs) {
    // Initialize background paint
    backgroundPaint = new Paint();
    backgroundPaint.setStyle(Paint.Style.FILL);
    backgroundPaint.setColor(DEFAULT_BACKGROUND_COLOR);

    // Initialize text paint
    textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    textPaint.setColor(DEFAULT_TEXT_COLOR);
    textPaint.setTextAlign(Paint.Align.CENTER);
    textPaint.setTextSize(spToPx(DEFAULT_TEXT_SIZE_SP));

    // Initialize and add CalibrationPoint
    calibrationPoint = new CalibrationPoint(context);
    addView(calibrationPoint);

    setWillNotDraw(false); // Necessary to call onDraw
  }

  private float spToPx(float sp) {
    return sp * getResources().getDisplayMetrics().scaledDensity;
  }

  // Set offset
  public void setOffset(float x, float y) {
    offsetX = x;
    offsetY = y;
    requestLayout();
  }

  public void setEnableText(boolean isEnableText) {
    this.isEnableText = isEnableText;
  }

  // Set message
  public void setMessage(String msg) {
    this.message = msg;
    invalidate();
  }

  // Set text color
  public void setTextColor(int color) {
    textPaint.setColor(color);
    invalidate();
  }

  // Set text size
  public void setTextSize(float textSizeSp) {
    textPaint.setTextSize(spToPx(textSizeSp));
    invalidate();
  }

  // Set background color
  @Override
  public void setBackgroundColor(int color) {
    backgroundPaint.setColor(color);
    invalidate();
  }

  // Set point colors array
  public void setPointColors(int[] colors) {
    if (colors != null && colors.length > 0) {
      this.pointColors = colors;
      currentColorIndex = 0;
      calibrationPoint.updateColor(pointColors[currentColorIndex]);
    }
  }

  // Change to the next point color
  public void nextPointColor() {
    currentColorIndex = (currentColorIndex + 1) % pointColors.length;
    calibrationPoint.updateColor(pointColors[currentColorIndex]);
    invalidate();
  }

  // Set point position
  public void setPointPosition(float x, float y) {
    float px = x - offsetX;
    float py = y - offsetY;

    calibrationPoint.setPosition(px, py);
    requestLayout();
  }

  // Set point animation power
  public void setPointAnimationPower(float power) {
    calibrationPoint.setAnimationPower(power);
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    // Measure CalibrationPoint
    measureChild(calibrationPoint, widthMeasureSpec, heightMeasureSpec);

    int width = resolveSize(calibrationPoint.getMeasuredWidth(), widthMeasureSpec);
    int height = resolveSize(calibrationPoint.getMeasuredHeight(), heightMeasureSpec);

    setMeasuredDimension(width, height);
  }

  @Override
  protected void onLayout(boolean changed, int l, int t, int r, int b) {
    // Set position of CalibrationPoint
    float centerX = calibrationPoint.getDesiredX();
    float centerY = calibrationPoint.getDesiredY();
    float radius = calibrationPoint.getRadius();

    int left = (int) (centerX - radius);
    int top = (int) (centerY - radius);
    int right = (int) (centerX + radius);
    int bottom = (int) (centerY + radius);

    calibrationPoint.layout(left, top, right, bottom);
  }

  @Override
  protected void onDraw(Canvas canvas) {
    // Draw background
    canvas.drawRect(0, 0, getWidth(), getHeight(), backgroundPaint);

    // Draw message
    if (message != null && !message.isEmpty() && isEnableText) {
      float x = getWidth() / 2f;
      float y = getHeight() / 2f - (textPaint.descent() + textPaint.ascent()) / 2f;
      canvas.drawText(message, x, y, textPaint);
    }
  }

  // CalibrationPoint inner class
  private static class CalibrationPoint extends View {
    private final String TAG = "CalibrationViewer";
    private static final float DEFAULT_RADIUS_DP = 30f;

    private Paint outerPaint;
    private Paint innerPaint;

    private float desiredX = 0f, desiredY = 0f;
    private float radius;

    private float animationPower = 0f;

    public CalibrationPoint(Context context) {
      super(context);
      init();
    }

    private void init() {
      radius = dpToPx(DEFAULT_RADIUS_DP);

      outerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
      outerPaint.setStyle(Paint.Style.FILL);

      innerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
      innerPaint.setStyle(Paint.Style.FILL);
    }

    private float dpToPx(float dp) {
      return dp * getResources().getDisplayMetrics().density;
    }

    public void setPosition(float x, float y) {
      this.desiredX = x;
      this.desiredY = y;
      requestLayout();
    }

    public float getDesiredX() {
      return desiredX;
    }

    public float getDesiredY() {
      return desiredY;
    }

    public void updateColor(int color) {
      int red = Color.red(color);
      int green = Color.green(color);
      int blue = Color.blue(color);
      outerPaint.setColor(Color.argb(100, red, green, blue));
      innerPaint.setColor(color);
      invalidate();
    }

    public void setAnimationPower(float power) {
      this.animationPower = power;
      invalidate();
    }

    public float getRadius() {
      return radius;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
      int size = (int) (2 * radius);
      setMeasuredDimension(size, size);
    }

    @Override
    protected void onDraw(Canvas canvas) {
      float animRadius = radius - ((radius / 2) * animationPower);
      float center = getWidth() / 2f;

      canvas.drawCircle(center, center, radius, outerPaint);
      canvas.drawCircle(center, center, animRadius, innerPaint);
    }
  }
}
package camp.visual.android.sdk.sample.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;

public class PointView  extends View {

  private final int pointColor = Color.rgb(0x84, 0x5e, 0xc2);
  private float offsetX, offsetY;
  private final PointF position = new PointF();

  private Paint paint;

  public PointView(Context context) {
    super(context);
    init();
  }

  public PointView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public PointView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  public PointView(Context context, @Nullable AttributeSet attrs, int defStyleAttr,
      int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    init();
  }

  private void init() {
    paint = new Paint();
    paint.setColor(pointColor);
    paint.setStrokeWidth(2f);
  }

  public void setOffset(int x, int y) {
    offsetX = x;
    offsetY = y;
  }

  public void setPosition(float x, float y) {
    position.x = x - offsetX;
    position.y = y - offsetY;
    invalidate();
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    canvas.drawCircle(position.x, position.y, 15, paint);
  }

}

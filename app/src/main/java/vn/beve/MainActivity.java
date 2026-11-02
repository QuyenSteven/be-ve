package vn.beve;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

public class MainActivity extends Activity {
    private final VolumeChord chord = new VolumeChord();
    private LinearLayout controls, root;
    private Drawing drawing;
    private boolean locked;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        if (android.os.Build.VERSION.SDK_INT >= 35) root.setOnApplyWindowInsetsListener((view, insets) -> {
            view.setPadding(locked ? 0 : insets.getSystemWindowInsetLeft(), locked ? 0 : insets.getSystemWindowInsetTop(), locked ? 0 : insets.getSystemWindowInsetRight(), locked ? 0 : insets.getSystemWindowInsetBottom());
            return insets;
        });
        controls = new LinearLayout(this);
        controls.setOrientation(LinearLayout.VERTICAL);
        controls.setPadding(12, 8, 12, 8);
        root.addView(controls);
        drawing = new Drawing();
        root.addView(drawing, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(root);

        TextView hint = new TextView(this);
        hint.setText("Chọn màu và cỡ nét, rồi bấm Bắt đầu. Mở khóa: giữ cùng lúc hai nút âm lượng.");
        hint.setTextSize(16);
        controls.addView(hint);
        LinearLayout colors = new LinearLayout(this);
        int[] palette = {Color.BLACK, Color.RED, 0xffff8800, 0xffffcc00, 0xff00a85a, 0xff0088ee, 0xff9944cc, 0xffff66aa};
        for (int color : palette) {
            Button button = new Button(this);
            button.setBackgroundTintList(android.content.res.ColorStateList.valueOf(color));
            button.setContentDescription("Màu " + Integer.toHexString(color));
            button.setOnClickListener(v -> drawing.color = color);
            colors.addView(button, new LinearLayout.LayoutParams(0, dp(48), 1));
        }
        controls.addView(colors);
        TextView sizeLabel = new TextView(this);
        sizeLabel.setText("Cỡ nét");
        controls.addView(sizeLabel);
        SeekBar size = new SeekBar(this);
        size.setMax(56); size.setProgress(10);
        size.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar bar, int value, boolean fromUser) { drawing.width = dp(value + 4); }
            public void onStartTrackingTouch(SeekBar bar) { }
            public void onStopTrackingTouch(SeekBar bar) { }
        });
        controls.addView(size);
        LinearLayout actions = new LinearLayout(this);
        Button clear = new Button(this); clear.setText("Xóa tranh");
        clear.setOnClickListener(v -> new AlertDialog.Builder(this).setMessage("Xóa bức tranh này?").setNegativeButton("Không", null).setPositiveButton("Xóa", (d, w) -> drawing.clear()).show());
        actions.addView(clear, new LinearLayout.LayoutParams(0, -2, 1));
        Button start = new Button(this); start.setText("Bắt đầu vẽ");
        start.setOnClickListener(v -> { setLocked(true); startLockTask(); });
        actions.addView(start, new LinearLayout.LayoutParams(0, -2, 1));
        controls.addView(actions);
    }

    private int dp(int n) { return Math.round(n * getResources().getDisplayMetrics().density); }
    private void setLocked(boolean value) {
        locked = value; chord.reset();
        controls.setVisibility(value ? View.GONE : View.VISIBLE);
        root.requestApplyInsets();
        getWindow().getDecorView().setSystemUiVisibility(value
            ? View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            : View.SYSTEM_UI_FLAG_VISIBLE);
    }
    @Override public boolean dispatchKeyEvent(KeyEvent event) {
        int key = event.getKeyCode();
        if (locked && (key == KeyEvent.KEYCODE_VOLUME_UP || key == KeyEvent.KEYCODE_VOLUME_DOWN)) {
            boolean up = key == KeyEvent.KEYCODE_VOLUME_UP;
            if (event.getAction() == KeyEvent.ACTION_DOWN && chord.press(up)) { stopLockTask(); setLocked(false); }
            else if (event.getAction() == KeyEvent.ACTION_UP) chord.release(up);
            return true;
        }
        if (locked && key == KeyEvent.KEYCODE_BACK) return true;
        return super.dispatchKeyEvent(event);
    }
    @Override protected void onPause() { chord.reset(); super.onPause(); }
    @Override public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && locked) setLocked(true);
    }

    private class Drawing extends View {
        private Bitmap bitmap;
        private Canvas paper;
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Path path = new Path();
        private int pointer = -1;
        int color = Color.BLACK;
        float width = dp(14);
        Drawing() { super(MainActivity.this); setBackgroundColor(Color.WHITE); }
        @Override protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            if (w <= 0 || h <= 0) return;
            Bitmap next = Bitmap.createBitmap(Math.max(w, bitmap == null ? 0 : bitmap.getWidth()), Math.max(h, bitmap == null ? 0 : bitmap.getHeight()), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(next);
            if (bitmap != null) canvas.drawBitmap(bitmap, 0, 0, null);
            bitmap = next; paper = canvas;
        }
        void clear() { if (bitmap != null) bitmap.eraseColor(Color.TRANSPARENT); invalidate(); }
        @Override protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            if (bitmap != null) canvas.drawBitmap(bitmap, 0, 0, null);
        }
        @Override public boolean onTouchEvent(MotionEvent event) {
            if (paper == null) return true;
            int action = event.getActionMasked();
            if (action == MotionEvent.ACTION_DOWN) {
                pointer = event.getPointerId(0);
                paint.setColor(color); paint.setStrokeWidth(width); paint.setStrokeCap(Paint.Cap.ROUND); paint.setStrokeJoin(Paint.Join.ROUND); paint.setStyle(Paint.Style.STROKE);
                path.reset(); path.moveTo(event.getX(), event.getY());
                paper.drawPoint(event.getX(), event.getY(), paint); invalidate();
            } else if (action == MotionEvent.ACTION_MOVE && pointer != -1) {
                int index = event.findPointerIndex(pointer);
                if (index >= 0) {
                    for (int i = 0; i < event.getHistorySize(); i++) path.lineTo(event.getHistoricalX(index, i), event.getHistoricalY(index, i));
                    path.lineTo(event.getX(index), event.getY(index));
                    paper.drawPath(path, paint); path.reset(); path.moveTo(event.getX(index), event.getY(index)); invalidate();
                }
            } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL || (action == MotionEvent.ACTION_POINTER_UP && event.getPointerId(event.getActionIndex()) == pointer)) {
                pointer = -1; path.reset();
            }
            return true;
        }
    }
}




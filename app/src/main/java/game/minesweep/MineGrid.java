package game.minesweep;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Scroller;

import java.util.ArrayList;

/**
 * Created by j-zhangyang5 on 2017/1/20.
 */

public class MineGrid extends View implements GameView{

    private static final int COLOR_DIG = 0x33ff0000;
    private static final int COLOR_BASE = 0xff666666;
    private static final int COLOR_NORMAL = 0xff6f91bd;
    private static final int COLOR_CAUSE = 0xffff0000;
    private static final int COLOR_MINE = 0xff999999;
    private static final int COLOR_QUESTION = 0xfffef100;
    private static final int COLOR_FLAG = 0xffec1b42;
    private static final int[] COLOR_NUMBER = new int[] {
            0xff3e47cb, 0xff21b04b, 0xffd05a23, 0xffb97956, 0xffa248a3, 0xff6f91bd, 0xff870014, 0xff000000
    };

    private GameControl control;
    private boolean isAvailable = false;
    private int endState = 0;
    private RectF rect = new RectF();
    private int countX, countY;

    private ArrayList<Core.Grid> data = new ArrayList<>();

    private boolean inited = false;

    private float spacing;
    private float maxSize;

    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint.FontMetrics fm = new Paint.FontMetrics();

    private int scrollState;

    private static final int SCROLL_STATE_REST = 0;
    private static final int SCROLL_STATE_DRAG = 1;
    private static final int SCROLL_STATE_FLING = 2;

    private Scroller scroller;
    private int touchState = 1;
    private int touchSlop = 30;
    private VelocityTracker tracker;

    private static final int TOUCH_STATE_REST = 0;
    private static final int TOUCH_STATE_MOVING = 1;
    private static final int TOUCH_STATE_MULTI = 2;
    private static final int TOUCH_STATE_MULTI_DONE = 3;

    private PointF pointDown1 = new PointF(), pointDown2 = new PointF();
    private PointF point1 = new PointF(), point2 = new PointF();
    private PointF scrollPosition = new PointF();
    private float zoomFrom = 1f;
    private DoubleClickChecker clickChecker;

    private boolean digMode = true;

    public MineGrid(Context context) {
        this(context, null);
    }

    public MineGrid(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MineGrid(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setWillNotDraw(false);
        spacing = context.getResources().getDisplayMetrics().density * 1;
        scroller = new Scroller(context);
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        maxSize = context.getResources().getDisplayMetrics().density * 48;
        clickChecker = new DoubleClickChecker(context);
        paint.setTextAlign(Paint.Align.CENTER);
    }

    public void setGameSettings(int width, int height) {
        countX = width;
        countY = height;
    }

    public void setDigMode(boolean digMode) {
        if (this.digMode != digMode) {
            this.digMode = digMode;
            invalidate();
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        int w = right - left;
        int h = bottom - top;
        if (inited || w == 0 || h == 0 || countX == 0 || countY == 0) {
            return;
        }
        inited = true;
        float wl = w - (countX + 1) * spacing;
        float hl = h - (countY + 1) * spacing;
        float wu = wl / countX;
        float hu = hl / countY;
        float size = Math.min(wu, hu);
        maxSize = Math.max(size, maxSize);
        float rw = size * countX + spacing * (countX + 1);
        float rh = size * countY + spacing * (countY + 1);
        rect.set((w - rw) / 2f, (h - rh) / 2f, (w + rw) / 2f, (h + rh) / 2f);
    }

    @Override
    public void setGameControl(GameControl control) {
        this.control = control;
    }

    @Override
    public void newGame() {
        isAvailable = true;
        endState = 0;
        digMode = true;
        inited = false;
        data = control.getData();
        requestLayout();
    }

    @Override
    public void setTime(int h, int m, int s) {
        // ignore
    }

    @Override
    public void setMines(int mines) {
        // ignore
    }

    public void update(int[] dirty) {
        float wu = (rect.width() - spacing) / countX;
        float wh = (rect.height() - spacing) / countY;
        int l = (int) (rect.left + spacing + dirty[0] * wu);
        int t = (int) (rect.top + spacing + dirty[1] * wh);
        int r = (int) (rect.left + (dirty[2] + 1) * wu + 0.5f);
        int b = (int) (rect.top + (dirty[3] + 1) * wh + 0.5f);
        invalidate(l, t, r, b);
    }

    public void endGame(boolean win) {
        isAvailable = false;
        endState = win ? 1 : -1;
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (tracker == null) {
            tracker = VelocityTracker.obtain();
        }
        tracker.addMovement(event);
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                touchState = scroller.isFinished() ? TOUCH_STATE_REST : TOUCH_STATE_MOVING;
                if (!scroller.isFinished()) {
                    scroller.abortAnimation();
                }
                pointDown1.set(event.getX(), event.getY());
                point1.set(event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_MOVE:
                if (touchState == TOUCH_STATE_REST) {
                    point1.set(event.getX(), event.getY());
                    float diff = (float) Math.hypot(point1.x - pointDown1.x, point1.y - pointDown1.y);
                    if (diff > touchSlop) {
                        touchState = TOUCH_STATE_MOVING;
                        pointDown1.set(point1);
                    }
                } else if (touchState == TOUCH_STATE_MOVING) {
                    float dx = event.getX() - point1.x;
                    float dy = event.getY() - point1.y;
                    scrollByInternal(dx, dy, true);
                    point1.set(event.getX(), event.getY());
                } else if (touchState == TOUCH_STATE_MULTI) {
                    float x1 = event.getX(0);
                    float y1 = event.getY(0);
                    float x2 = event.getX(1);
                    float y2 = event.getY(1);
                    float s1 = (float) Math.hypot(point1.x - point2.x, point1.y - point2.y);
                    float s2 = (float) Math.hypot(x1 - x2, y1 - y2);
                    float rx1 = x1 - point1.x;
                    float ry1 = y1 - point1.y;
                    float l1 = (float) Math.hypot(rx1, ry1);
                    float rx2 = x2 - point2.x;
                    float ry2 = y2 - point2.y;
                    float l2 = (float) Math.hypot(rx2, ry2);
                    float ox = point1.x - point2.x;
                    float oy = point1.y - point2.y;
                    if (l1 == 0 && l2 != 0) {
                        float cos = (rx2 * ox + ry2 * oy) / (s1 * l2);
                        if (cos <= -0.87f) {
                            zoom(s2 / s1);
                        } else if (cos >= 0.87f) {
                            scrollByInternal(x2 - point2.x, y2 - point2.y, true);
                        }
                    } else if (l2 == 0 && l1 != 0) {
                        float cos = -(rx1 * ox + ry1 * oy) / (s1 * l2);
                        if (cos <= -0.87f) {
                            zoom(s2 / s1);
                        } else if (cos >= 0.87f) {
                            scrollByInternal(x1 - point1.x, y1 - point1.y, true);
                        }
                    } else if (l1 != 0 && l2 != 0) {
                        float cos = (rx1 * rx2 + ry1 * ry2) / (l1 * l2);
                        if (cos <= -0.87f) {
                            zoom(s2 / s1);
                        } else if (cos >= 0.87f) {
                            scrollByInternal(Math.max(rx1, rx2), Math.max(ry1, ry2), true);
                        }
                    }
                    point1.set(x1, y1);
                    point2.set(x2, y2);
                }
                break;
            case MotionEvent.ACTION_UP:
                if (touchState == TOUCH_STATE_MOVING) {
                    VelocityTracker tracker = this.tracker;
                    tracker.computeCurrentVelocity(1000, ViewConfiguration.get(getContext()).getScaledMaximumFlingVelocity());
                    float vx = tracker.getXVelocity();
                    float vy = tracker.getYVelocity();
                    fling(vx, vy);
                } else if (touchState == TOUCH_STATE_MULTI) {
//                    VelocityTracker tracker = this.tracker;
//                    tracker.computeCurrentVelocity(1000, ViewConfiguration.get(getContext()).getScaledMaximumFlingVelocity());
//                    float vx1 = tracker.getXVelocity(0);
//                    float vy1 = tracker.getYVelocity(0);
//                    float vx2 = tracker.getXVelocity(1);
//                    float vy2 = tracker.getXVelocity(1);
                } else if (touchState == TOUCH_STATE_REST) {
                    tapAt(event.getX(), event.getY());
                }
            case MotionEvent.ACTION_CANCEL:
                if (tracker != null) {
                    tracker.recycle();
                    tracker = null;
                }
                touchState = TOUCH_STATE_REST;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                if (touchState == TOUCH_STATE_REST || touchState == TOUCH_STATE_MULTI_DONE) {
                    touchState = TOUCH_STATE_MULTI;
                    pointDown2.set(event.getX(), event.getY());
                    point2.set(event.getX(), event.getY());
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
                if (event.getPointerCount() == 1) {
                    touchState = TOUCH_STATE_MULTI_DONE;
                }
                break;
        }
        return true;
    }

    private boolean scrollByInternal(float dx, float dy, boolean force) {
        rect.offset(dx, dy);
        boolean moreX = true;
        if (rect.width() <= getWidth()) {
            rect.offsetTo((getWidth() - rect.width()) / 2f, rect.top);
            moreX = false;
        } else if (rect.left > 0) {
            rect.offsetTo(0, rect.top);
            moreX = false;
        } else if (rect.right < getWidth()) {
            rect.offsetTo(getWidth() - rect.width(), rect.top);
            moreX = false;
        }
        boolean moreY = true;
        if (rect.height() <= getHeight()) {
            rect.offsetTo(rect.left, (getHeight() - rect.height()) / 2f);
            moreY = false;
        } else if (rect.top > 0) {
            rect.offsetTo(rect.left, 0);
            moreY = false;
        } else if (rect.bottom < getHeight()) {
            rect.offsetTo(rect.left, getHeight() - rect.height());
            moreY = false;
        }
        if (force || moreX || moreY) {
            invalidate();
            return true;
        }
        return false;
    }

    private void fling(float vx, float vy) {
        scrollPosition.set(0, 0);
        scroller.fling(0, 0, (int) vx, (int) vy, vx > 0 ? 0 : Integer.MIN_VALUE, vx > 0 ? Integer.MAX_VALUE : 0, vy > 0 ? 0: Integer.MIN_VALUE, vy > 0 ? Integer.MAX_VALUE : 0);
        invalidate();
    }

    private void zoom(float zoom) {
        float cw = rect.width();
        float ch = rect.height();
        float zw = cw * zoom;
        float zh = ch * zoom;

        if (zw <= getWidth() && zh <= getHeight()) {
            if (getWidth() - zw > getHeight() - zh) {

            }
        }

    }

    private void tapAt(float x, float y) {
        if (!isAvailable) {
            return;
        }
        clickChecker.check(x, y);
    }

    private void clickAt(float x, float y) {
        float wu = (rect.width() - spacing) / countX;
        float wh = (rect.height() - spacing) / countY;
        int vx = (int) ((x - rect.left - spacing / 2f) / wu);
        int vy = (int) ((y - rect.top - spacing / 2f) / wh);
        if (vx < 0 || vx >= countX || vy < 0 || vy >= countY) {
            return;
        }
        if (digMode) {
            control.dig(vx, vy);
        } else {
            control.mark(vx, vy);
        }
    }

    private void doubleClickAt(float x, float y) {
        float wu = (rect.width() - spacing) / countX;
        float wh = (rect.height() - spacing) / countY;
        int vx = (int) ((x - rect.left - spacing / 2f) / wu);
        int vy = (int) ((y - rect.top - spacing / 2f) / wh);
        if (vx < 0 || vx >= countX || vy < 0 || vy >= countY) {
            return;
        }
        control.sweep(vx, vy);
    }

    private void computeScrollInternal() {
        if (scroller.computeScrollOffset()) {
            float x = scroller.getCurrX();
            float y = scroller.getCurrY();
            if (!scrollByInternal(scrollPosition.x - x, scrollPosition.y - y, false)) {
                scroller.forceFinished(true);
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (digMode) {
            canvas.drawColor(COLOR_DIG);
        }

        if (data == null || data.size() == 0 || rect.isEmpty()) {
            return;
        }

        computeScrollInternal();

        float wu = (rect.width() - (countX + 1) * spacing) / countX;
        float hu = (rect.height() - (countY + 1) * spacing) / countY;

        paint.setTextSize(wu);
        paint.getFontMetrics(fm);

        for (Core.Grid grid : data) {
            int x = grid.index % countX;
            int y = grid.index / countX;
            drawGrid(canvas, grid, rect.left + spacing + x * (wu + spacing), rect.top + spacing + y * (hu + spacing), wu, hu);
        }
    }

    private void drawGrid(Canvas canvas, Core.Grid grid, float l, float t, float w, float h) {
        if (grid.state == Core.Grid.STATE_NORMAL || grid.state == Core.Grid.STATE_FLAG || grid.state == Core.Grid.STATE_QUESTION) {
            if (endState != -1) {
                paint.setColor(COLOR_NORMAL);
            } else {
                if (grid.value == -1 && grid.cause) {
                    paint.setColor(COLOR_CAUSE);
                } else if (grid.value == -1) {
                    paint.setColor(COLOR_MINE);
                } else {
                    paint.setColor(COLOR_NORMAL);
                }
            }
        } else {
            if (endState == -1 && grid.cause && grid.value == -1) {
                paint.setColor(COLOR_CAUSE);
            } else {
                paint.setColor(COLOR_BASE);
            }
        }
        canvas.drawRect(l, t, l + w, t + h, paint);

        if (grid.state == Core.Grid.STATE_OPEN) {
            if (grid.value == -1) {
                drawMine(canvas, l, t, w, h);
            } else if (grid.value > 0) {
                drawNumber(canvas, l, t, w, h, grid.value);
            }
        } else {
            if (grid.value == -1 && endState == -1) {
                drawMine(canvas, l, t, w, h);
            } else if (grid.state == Core.Grid.STATE_FLAG) {
                drawFlag(canvas, l, t, w, h);
            } else if (grid.state == Core.Grid.STATE_QUESTION) {
                drawQuestion(canvas, l, t, w, h);
            }
        }
    }

    private void drawMine(Canvas canvas, float l, float t, float w, float h) {
//        paint.setColor(0xff000000);
//        float cx = l + w / 2f;
//        float cy = t + h / 2f;
//        canvas.drawCircle(cx, cy, w * 0.45f, paint);
//        float hw = w * 0.45f * (0.414f / 2f + 1) / 1.414f;
//        canvas.drawRect(cx - hw, cy - hw, cx + hw, cy + hw, paint);
        paint.setColor(0xff000000);
        canvas.drawText("¤", l + w / 2f, t + h / 2f - (fm.top + fm.bottom) / 2f, paint);
    }

    private void drawNumber(Canvas canvas, float l, float t, float w, float h, int num) {
        paint.setColor(COLOR_NUMBER[num - 1]);
        canvas.drawText(Integer.toString(num), l + w / 2f, t + h / 2f - (fm.top + fm.bottom) / 2f, paint);
    }

    private void drawFlag(Canvas canvas, float l, float t, float w, float h) {
        paint.setColor(COLOR_FLAG);
        canvas.drawText("†", l + w / 2f, t + h / 2f - (fm.top + fm.bottom) / 2f, paint);
    }

    private void drawQuestion(Canvas canvas, float l, float t, float w, float h) {
        paint.setColor(COLOR_QUESTION);
        canvas.drawText("?", l + w / 2f, t + h / 2f - (fm.top + fm.bottom) / 2f, paint);
    }

    private class DoubleClickChecker implements Runnable {

        long clickTime;
        float x, y;
        long timeout;

        DoubleClickChecker(Context context) {
            timeout = ViewConfiguration.getDoubleTapTimeout();
        }

        void check(float x, float y) {
            long now = System.currentTimeMillis();
            if (now - clickTime > timeout || Math.hypot(x - this.x, y - this.y) > 2 * touchSlop) {
                this.x = x;
                this.y = y;
                clickTime = now;
                postDelayed(this, timeout);
            } else {
                removeCallbacks(this);
                clickTime = 0;
                doubleClickAt(this.x, this.y);
            }
        }


        @Override
        public void run() {
            clickAt(x, y);
        }
    }
}

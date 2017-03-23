package com.yyx.dragclock;
/**
 * made in 浪小花
 * <p/>
 * location : page1
 * <p/>
 * aim:可拖动，用来设置时间
 * 变量很多。。。
 */

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;


public class DragClockView extends View {
    private static final String TAG = DragClockView.class.getName();
    private static int centerx; //圆心的x坐标
    private static int centery;
    private static int ballMoveX;  // 可以拖动的大球的圆心，位于圆环上
    private static int ballMoveY;
    private static int radius; // 指圆环外围一侧的半径
    //时间文字，默认15分钟
    private static int minute = 15;
    private static int hour = 0;
    private double oldAngle;
    private double realAngle;//圆圈的角度，绘制圆弧和圆圈的标准，0～360
    private double curAngle;  //当前手指滑动对应的位置角度
    private double downAngle = 0;//手指按下时对应的角度
    private double downRealAngle = 0;//手指按下时，realAngle的值

    //画圆环用的
    private Paint paint;
    private int strokeWidth = 0;//圆环宽度
    private int fixArcColor;//背景圆环的颜色
    private int dragArcColor;//可拖动圆环的颜色
    private int pointCircleColor;//圆圈的颜色
    private int textSize;
    private int textColor;

    public DragClockView(Context context) {
        this(context, null);
        // TODO Auto-generated constructor stub
    }

    public DragClockView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.DragClockView);
        fixArcColor = ta.getColor(R.styleable.DragClockView_fixArcColor, Color.WHITE);
        dragArcColor = ta.getColor(R.styleable.DragClockView_dragArcColor, Color.rgb(244, 242, 10));
        pointCircleColor = ta.getColor(R.styleable.DragClockView_pointCircleColor, Color.rgb(244, 242, 10));
        textColor = ta.getColor(R.styleable.DragClockView_pointCircleColor, Color.BLACK);
        strokeWidth = ta.getDimensionPixelSize(R.styleable.DragClockView_strokeWidth, 30);
        radius = ta.getDimensionPixelSize(R.styleable.DragClockView_radius, 130);
        textSize = ta.getDimensionPixelSize(R.styleable.DragClockView_radius, radius / 7 * 2);
        hour = ta.getInt(R.styleable.DragClockView_initHour, 0);
        minute = ta.getInt(R.styleable.DragClockView_initHour, 15);
        centerx = radius * 3 / 2;
        centery = radius * 8 / 7;
        //指示球的位置
        ballMoveX = centerx + radius;
        ballMoveY = centery;
        //初始化进度条的位置
        realAngle = oldAngle = curAngle = 6 * minute;
        paint = new Paint();
    }

    public DragClockView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        // TODO Auto-generated method stub
        super.onDraw(canvas);
        drawClock(canvas);
        drawText(canvas);
    }

    /**
     * 按转动结果绘制倒计时时间
     */
    private void drawText(Canvas canvas) {
        int delta = radius / 7;//加一点绘制的偏移量，让圆圈位于圆环中间
        paint.setStrokeWidth(4);
        paint.setColor(textColor);
        paint.setTextSize(textSize);
        paint.setTypeface(Typeface.MONOSPACE); //设置字体
        if (minute < 10)
            canvas.drawText("0" + minute + "m", centerx + delta, centery + delta / 2, paint);
        else
            canvas.drawText("" + minute + "m", centerx + delta, centery + delta / 2, paint);

        if (hour < 10) {
            canvas.drawText("0" + hour + "h:", centerx - delta * 4, centery + delta / 2, paint);
        } else {
            canvas.drawText("" + hour + "h:", centerx - delta * 4, centery + delta / 2, paint);
        }
    }

    /**
     * 绘制背景圆环
     *
     * @param canvas
     */
    private void drawClock(Canvas canvas) {
        paint.setStrokeWidth(strokeWidth); //设置圆环的宽度
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);  //消除锯齿
        RectF oval = new RectF(centerx - radius, centery - radius, centerx
                + radius, centery + radius);  //用于定义的圆弧的形状和大小的界限
        /**
         * 背景圆环
         */
        paint.setColor(Color.WHITE);  //设置颜色
        canvas.drawArc(oval, -90, 360, true, paint);
        /**
         *覆盖圆弧
         */
        paint.setColor(Color.rgb(244, 242, 10));  //设置进度环颜色
        canvas.drawArc(oval, -90, (float) realAngle, false, paint);  //根据进度画圆弧
        /**
         *画可以拖动的圆
         */
        getBallXY(realAngle);
        paint.setColor(Color.rgb(244, 242, 10));  //设置进度的颜色
        canvas.drawCircle(ballMoveX, ballMoveY, 8, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float distance = 0;// 手指位置与圆点之间的距离
        float downX;
        float downY;//手指按下的位置坐标
        float pressX;
        float pressY;//手指当前的位置坐标
        double deltaAngle = 0;//相对转动的角度

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            downX = event.getX();
            downY = event.getY();
            //            distance = (downX - centerx) * (downX - centerx)
            //                    + (downY - centery) * (downY - centery);
            //            if (distance > radius * radius / 8) {
            downAngle = location2Angle(downX, downY, centerx, centery);
            downRealAngle = realAngle;
            Log.d(TAG, "onTouchEvent: " + downAngle + "downX: " + downX + "downY: " + downY);
            //            }
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            pressX = event.getX();
            pressY = event.getY();
            distance = (pressX - centerx) * (pressX - centerx)
                    + (pressY - centery) * (pressY - centery);
            if (distance < radius * radius * 2 && distance > radius * radius / 5)    // 确保按在了大环上
            {
                curAngle = location2Angle(pressX, pressY, centerx, centery);
                oldAngle = realAngle;
                deltaAngle = curAngle - downAngle;
                realAngle = (int) (downRealAngle + deltaAngle) % 360;
                realAngle = realAngle < 0 ? realAngle + 360 : realAngle;
                minute = (int) realAngle / 6;
                Log.d(TAG, "realAngle: " + realAngle + " curAngle: " + curAngle + "downAngle: " + downAngle + " deltaAngle: " + deltaAngle);
                if (realAngle < 20 && oldAngle > 340) {//临界值可以做适当调整，根据测算跨越60分钟时的realAngle和oldAngle评判
                    if (hour == 23) {
                        hour = 0;
                    } else {
                        hour++;
                    }
                } else if (oldAngle < 20 && realAngle > 340) {
                    if (hour == 0) {
                        hour = 23;
                    } else {
                        hour--;
                    }
                }
                invalidate();
            }
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            invalidate();
        }
        return true;
    }

    /**
     * 计算进度条的角度和小球的位置
     */
    private double location2Angle(float pressx, float pressy, int centerx, int centery) {
        double angle = 0;
        if (pressx < centerx) {
            if (pressy < centery) {     // 左上区域
                angle = Math.atan((centery - pressy) / (centerx - pressx));//得到弧度制角度
                angle = angle * 180 / Math.PI;
                angle = 270 + angle;//换算成角度制
            } else {                     // 左下区域
                angle = Math.atan((pressy - centery) / (centerx - pressx));//得到弧度制角度
                angle = angle * 180 / Math.PI;
                angle = 270 - angle;//换算成角度制
            }
        } else {
            if (pressy < centery) {      // 右上区域
                angle = Math.atan((centery - pressy) / (pressx - centerx));//得到弧度制角度
                angle = angle * 180 / Math.PI;
                angle = 90 - angle;//换算成角度制
            } else {                    // 右下区域
                angle = Math.atan((pressy - centery) / (pressx - centerx));//得到弧度制角度
                angle = angle * 180 / Math.PI;
                angle = 90 + angle;//换算成角度制
            }
        }
        return angle;
    }

    /**
     * 计算指示球的坐标
     *
     * @param realAngle
     */
    private void getBallXY(double realAngle) {
        if (realAngle >= 270) {     // 左上区域
            ballMoveX = centerx - (int) (Math.cos(angle2Radians(realAngle - 270)) * (radius));
            ballMoveY = centery - (int) (Math.sin(angle2Radians(realAngle - 270)) * (radius));
        } else if (realAngle >= 180) {                     // 左下区域
            ballMoveX = centerx - (int) (Math.cos(angle2Radians(270 - realAngle)) * radius);
            ballMoveY = centery + (int) (Math.sin(angle2Radians(270 - realAngle)) * radius);
        } else if (realAngle >= 90) {      //  右下区域
            ballMoveX = centerx + (int) (Math.cos(angle2Radians(realAngle - 90)) * radius);
            ballMoveY = centery + (int) (Math.sin(angle2Radians(realAngle - 90)) * radius);
        } else {                    // 右上区域
            ballMoveX = centerx + (int) (Math.cos(angle2Radians(90 - realAngle)) * radius);
            ballMoveY = centery - (int) (Math.sin(angle2Radians(90 - realAngle)) * radius);
        }
        Log.d(TAG, "realAngle: " + angle2Radians(realAngle) + "ballMoveX: " + ballMoveX + "ballMoveY: " + ballMoveY);
    }

    /**
     * 角度制转弧度制
     *
     * @param angle
     * @return
     */
    private double angle2Radians(double angle) {
        return angle / 180 * Math.PI;
    }

    public int getHour() {
        return hour;
    }

    private void setHour(int hour) {
        this.hour = hour;
    }

    private void setAngle(double angle) {
        realAngle = angle;
    }

    public int getMinute() {
        return minute;
    }

    private void setMinute(int minute) {
        this.minute = minute;
    }

    public void makeHourMinute(int mHour, int mMin) {
        setHour(mHour);
        setMinute(mMin);
        setAngle(mMin * 6);
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension((int) (radius * 3), (int) ((radius * 3)));
    }

}

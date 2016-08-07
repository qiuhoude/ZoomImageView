package houde.zoomimageview.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import com.orhanobut.logger.Logger;

/**
 * Created by Administrator on 2016/7/31.
 */
public class ZooImageView extends ImageView implements
        ViewTreeObserver.OnGlobalLayoutListener,
        ScaleGestureDetector.OnScaleGestureListener, //缩放手势的
        View.OnTouchListener {

    private Matrix mScaleMatrix;

    /**
     * 获取系统的缩放建议值
     */
    private int mTouchSlop;

    private int originCenterX;
    private int originCenterY;

    //缩放手势辅助类
    private ScaleGestureDetector mScaleGestureDetector;

    private GestureDetector mGestureDetector;

    //手势
    private GestureDetector.OnGestureListener gestureListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            //先判断是双击是要放大还是恢复原状
            if (getScale() > mInitScale) {
                //缩小
                postDelayed(new AutoScaleRunnable(getScale(), mInitScale, getWidth() / 2, getHeight() / 2), 16);
            } else {
                //放大
//                mScaleMatrix.postScale(mMidScale, mMidScale,
//                        e.getX(), e.getY());
//                checkBorderAndCenterWhenScale();
//                setImageMatrix(mScaleMatrix);
                postDelayed(new AutoScaleRunnable(getScale(), mMidScale, e.getX(), e.getY()), 16);
            }
            return true;
        }

    };

    /**
     * 平滑缩放
     */
    private class AutoScaleRunnable implements Runnable {

        //缩放的目标值
        private float mTargetScale;
        private float mStartScale;//开始的缩放值
        //缩放的中心点
        private float x;
        private float y;

        //缩放的梯度
        private final float BIGGER = 1.07f;
        private final float SMALL = 0.93f;
        private float temScale;
        private boolean isSmall;

        public AutoScaleRunnable(float mStartScale, float mTargetScale, float x, float y) {
            this.mTargetScale = mTargetScale;
            this.mStartScale = mStartScale;
            this.x = x;
            this.y = y;
            if (mStartScale > mTargetScale) {
                temScale = SMALL;
                isSmall = true;
            }

            if (mStartScale <= mTargetScale) {
                temScale = BIGGER;
                isSmall = false;
            }
        }

        @Override
        public void run() {
            mScaleMatrix.postScale(temScale, temScale,
                    x, y);
            checkBorderAndCenterWhenScale();
            setImageMatrix(mScaleMatrix);

            float currentScale = getScale();
//            Logger.d("run .... temScale : %f ;currentScale : %f ; mStartScale : %f ; mTargetScale : %f", temScale, currentScale,mStartScale,mTargetScale);
            if ((isSmall && currentScale > mTargetScale)  //缩小
                    || (!isSmall && currentScale < mTargetScale))//放大
            {

                postDelayed(this, 16);
            } else {
                //设置目标值
                if (mTargetScale == mInitScale) {
                    resetInitScale();
                    return;
                }
                float scale = mTargetScale / currentScale;
                mScaleMatrix.postScale(scale, scale,
                        x, y);
                checkBorderAndCenterWhenScale();
                setImageMatrix(mScaleMatrix);
            }
        }
    }

    public ZooImageView(Context context) {
        this(context, null);
    }

    public ZooImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ZooImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }


    private void init(Context context) {
        mScaleMatrix = new Matrix();
        setScaleType(ScaleType.MATRIX);

        //多点触控的缩放手势类
        mScaleGestureDetector = new ScaleGestureDetector(context, this);

        mGestureDetector = new GestureDetector(context, gestureListener);

        setOnTouchListener(this);

        //获取系统建议值
        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    }


    /**
     * 是否是第一次进来
     */
    private boolean mOnce;

    /**
     * 初始化的缩放值
     */
    private float mInitScale;

    /**
     * 双击放大的值
     */
    private float mMidScale;
    /**
     * 放大的最大值
     */
    private float mMaxScale;

    /**
     * 缩放的最小值
     */
    private float mMinScale;

    /**
     * 记录上一次多点的数量
     * (需要的原因:当触摸点3个点减少成为2个点的时候,中心的位置会发生变化,这样效果显得很突兀)
     */
    private int mLastPointerCount;

    private boolean isCanDrag;

    //当绑定到window的时候调用
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    //当从window上移除的时候调用
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        getViewTreeObserver().removeOnGlobalLayoutListener(this);
    }


    /**
     * 获取viewImage加载完成后的图片
     */
    @Override
    public void onGlobalLayout() {
        if (!mOnce) {
            //获取控件的宽高
            int width = getWidth();
            int height = getHeight();

            //获取图片
            Drawable d = getDrawable();
            if (d == null) {
                return;
            }
            int dw = d.getIntrinsicWidth();
            int dh = d.getIntrinsicHeight();

            //缩放图片
            float scale = 1; //缩放值
            if (dw > width && dh < height) {
                //按照宽度 缩小
                scale = width * 1.0f / dw;
            }
            if (dw < width && dh > height) {
                // 按照高度 缩小
                scale = height * 1.0f / height;
            }

            if ((dw > width && dh > height) || (dw < width && dh < height)) {
                //宽高最小值 缩小 或 放大
                scale = Math.min(width * 1.0f / dw, height * 1.0f / dh);
            }

            //初始化的缩放比例
            mInitScale = scale;
            mMaxScale = mInitScale * 6;
            mMidScale = mInitScale * 2;
            mMinScale = mInitScale * 0.3f;

            //移动图到控件的中间
            originCenterX = width / 2 - dw / 2;
            originCenterY = height / 2 - dh / 2;

            //设置值
            resetInitScale();

            mOnce = true;
        }
    }

    //获取当前图片的缩放值
    public float getScale() {
        float[] value = new float[9];
        mScaleMatrix.getValues(value);
        //通过Matrix,获取x轴的缩放值
        return value[Matrix.MSCALE_X];
    }

    private static final String TAG = "ZooImageView";

    @Override
    public boolean onScale(ScaleGestureDetector detector) {

        float scale = getScale();

        float scaleFactor = detector.getScaleFactor();

        if (getDrawable() == null) return true;
        //缩放的区间 initScale maxScale
        if ((scale < mMaxScale && scaleFactor > 1.0f)
                || (scale > mMinScale && scaleFactor < 1.0f)) {

            if (scale * scaleFactor < mMinScale) {
                //如果缩小动作,边界不能小于最小值
                scaleFactor = mMinScale / scale;
            }
            if (scale * scaleFactor > mMaxScale) {
                //边界条件处理
                scaleFactor = mMaxScale / scale;
            }

//            Logger.d("scaleFactor : %s ; scale : %s", scaleFactor, scale);
            //设置矩阵,缩放中心点位置
            mScaleMatrix.postScale(scaleFactor, scaleFactor,
                    detector.getFocusX(), detector.getFocusY());

            checkBorderAndCenterWhenScale();

            setImageMatrix(mScaleMatrix);
        }
        return true;
    }


    /**
     * 获取图片发大缩小后的矩形大小
     *
     * @return
     */
    private RectF getMatrixRectF() {
        Matrix m = mScaleMatrix;
        RectF rf = new RectF();
        Drawable d = getDrawable();
        if (d != null) {
            rf.set(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
            m.mapRect(rf);
        }
        return rf;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        //开始缩放,返回true 拦截事件给自己处理
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {


    }


    private float mLastX;
    private float mLastY;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (mGestureDetector.onTouchEvent(event)) {
            //传递给手势辅助类
            return true;
        }
        //把事件交给GestureDetector处理
        mScaleGestureDetector.onTouchEvent(event);

        float x = 0;
        float y = 0;
        //获取多点触碰的点个数,并计算多点的中心位置
        int pointerCount = event.getPointerCount();

        for (int i = 0; i < pointerCount; i++) {
            x += event.getX(i);
            y += event.getY(i);
        }
        x /= pointerCount;
        y /= pointerCount;

        float dx = x - mLastX;
        float dy = y - mLastY;

        mLastX = x;
        mLastY = y;

        if (mLastPointerCount != pointerCount) {
            //触摸点个数发生变化的时候,是不让移动的
            isCanDrag = false;
        }
        mLastPointerCount = pointerCount;

//        Logger.d("pointerCount : %s ;mLastPointerCount : %d; " +
//                "x : %f ; y : %f ; dx : %f ; dy : %f　", pointerCount, mLastPointerCount, x, y, dx, dy);


        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {

                if (getParent() instanceof ViewPager) {
                    //不让父控件拦截自己的事件
//                    getParent().requestDisallowInterceptTouchEvent(true);
                }

                break;
            }
            case MotionEvent.ACTION_MOVE: {
                //自由移动图片
                if (!isCanDrag) {
                    //不能移动,再次检查是否可以移动
                    isCanDrag = isMoveAction(dx, dy);
                } else {
                    if (getDrawable() != null) {
                        mScaleMatrix.postTranslate(dx, dy);
                        //边界条件的控制
                        checkBorderWhenTranslate();
                        setImageMatrix(mScaleMatrix);
                    }
                }

                break;
            }
            case MotionEvent.ACTION_UP: {

                //结束缩放,如果是缩小就恢复原状
                float scale = getScale();
                //        Logger.d("onScaleEnd()  mInitScale : %s ; scaleFactor : %s ; scale : %s", mInitScale, scaleFactor, scale);
                if (scale <= mInitScale) {
//                    resetInitScale();
                    postDelayed(new AutoScaleRunnable(getScale(), mInitScale, getWidth() / 2, getHeight() / 2), 16);
//                    mScaleMatrix.setTranslate(originCenterX, originCenterY);
//                    mScaleMatrix.postScale(mInitScale, mInitScale, getWidth() / 2, getHeight() / 2);
//                    setImageMatrix(mScaleMatrix);
                }
                break;
            }
            case MotionEvent.ACTION_CANCEL: {
                break;
            }
            case MotionEvent.ACTION_POINTER_DOWN: {
                Logger.d("ACTION_POINTER_DOWN 多点");
                break;
            }
        }
        return true;
    }

    private void resetInitScale() {
        mScaleMatrix.setTranslate(originCenterX, originCenterY);
        mScaleMatrix.postScale(mInitScale, mInitScale, getWidth() / 2, getHeight() / 2);
        setImageMatrix(mScaleMatrix);
    }


    /**
     * 移动边界检查
     */
    private void checkBorderWhenTranslate() {
        doNotBorderHaSpace();
    }


    private void doNotBorderHaSpace() {
        if (getScale() >= mInitScale) {
            RectF rect = getMatrixRectF();
            float deltaX = 0;//偏移量
            float deltaY = 0;
            int w = getWidth();
            int h = getHeight();

            //出现白边的调整
            if (rect.width() >= w) {
                if (rect.left > 0) {
                    //左边出现白边情况
                    deltaX = -rect.left;
                }
                if (rect.right < w) {
                    //右边出现白边
                    deltaX = w - rect.right;
                }

                //横图,高度没有占满全屏的情况,不让纵向的图片移除屏幕
                if (rect.height() < h) {
                    if (rect.top < 0) {
                        deltaY = -rect.top;
                    }
                    if (rect.bottom > h) {
                        deltaY = h - rect.bottom;
                    }
                }
            }
            if (rect.height() >= h) {
                if (rect.top >= 0) {
                    //上
                    deltaY = -rect.top;
                }
                if (rect.bottom < h) {
                    deltaY = h - rect.bottom;
                }

                if (rect.width() < w) {
                    //竖图,宽度没有占满全屏的情况,不让横向的图片移除屏幕
                    if (rect.left < 0) {
                        deltaX = -rect.left;
                    }
                    if (rect.right > w) {
                        deltaX = w - rect.right;
                    }
                }

            }
            mScaleMatrix.postTranslate(deltaX, deltaY);
            setImageMatrix(mScaleMatrix);
        }
    }


    /**
     * 在缩放的时候进行边界和位置的调整(不让出现白边,并且居中)
     */
    private void checkBorderAndCenterWhenScale() {
        doNotBorderHaSpace();
    }


    /**
     * 判断是否可以进行移动
     *
     * @param dx
     * @param dy
     * @return
     */
    public boolean isMoveAction(float dx, float dy) {

        return Math.sqrt(dx * dx + dy * dy) > mTouchSlop;
    }
}

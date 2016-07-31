package houde.zoomimageview.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

/**
 * Created by Administrator on 2016/7/31.
 */
public class ZooImageView extends ImageView implements
        ViewTreeObserver.OnGlobalLayoutListener,
        ScaleGestureDetector.OnScaleGestureListener,
        View.OnTouchListener{

    private Matrix mScaleMartrix;
    private ScaleGestureDetector mScaleGestureDetector;

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
        mScaleMartrix = new Matrix();
        setScaleType(ScaleType.MATRIX);

        //多点触控的缩放手势类
        mScaleGestureDetector = new ScaleGestureDetector(context, this);
    }


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
            mMaxScale = mInitScale * 4;
            mMidScale = mInitScale * 2;


            //移动图到控件的中间
            int dx = width / 2 - dw / 2;
            int dy = height / 2 - dh / 2;


            //设置值
            mScaleMartrix.postTranslate(dx, dy);
            mScaleMartrix.postScale(mInitScale, mInitScale, width / 2, height / 2);
            setImageMatrix(mScaleMartrix);
            mOnce = true;
        }
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        return false;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        return false;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }
}

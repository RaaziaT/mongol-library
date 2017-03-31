package net.studymongolian.mongollibrary;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver;


public class MongolTextView extends View  implements ViewTreeObserver.OnPreDrawListener {

    private final static int DEFAULT_FONT_SIZE_SP = 17;
    private static final int STICKY_WIDTH_UNDEFINED = -1;
    private static final String TAG = "CustomView";



    private String mText;
    private int mTextColor;
    private float mTextSizePx;
    private int mGravity = Gravity.TOP;
    private TextPaint mTextPaint;
    private Paint mPaint;
    private MongolLayout mLayout;
    private boolean mNeedsRelayout = false;

    private int mStickyWidth = STICKY_WIDTH_UNDEFINED;
    //private int mBreakHeight = 0;
    //private int mLastDesiredWidth = 0;
    private int[] mOnMeasureData = new int[6];
//    private int mOldWidthSpec = 0;
//    private int mOldHeight = 0;
//    private int mOldDesiredWidth = 0;
//    private int mNewDesiredWidth = 0;

    // programmatic constructor1
    public MongolTextView(Context context) {
        super(context);

        mText = "";
        mTextSizePx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                DEFAULT_FONT_SIZE_SP, getResources().getDisplayMetrics());
        mTextColor = Color.BLACK;
        mGravity = Gravity.TOP;

        init();
    }

    // xml constructor
    public MongolTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs, R.styleable.MongolTextView, 0, 0);

        try {
            mText = a.getString(R.styleable.MongolTextView_text);
            if (mText == null) {
                mText = ""; // FIXME still crashes if text is not set in xml
            }
            mTextSizePx = a.getDimensionPixelSize(R.styleable.MongolTextView_textSize, 0);
            mTextColor = a.getColor(R.styleable.MongolTextView_textColor, Color.BLACK);
            mGravity = a.getInteger(R.styleable.MongolTextView_gravity, Gravity.TOP);
        } finally {
            a.recycle();
        }

        init();
    }

    private void init() {
        mTextPaint = new TextPaint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setColor(mTextColor);
        if (mTextSizePx <= 0) {
            mTextSizePx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                    DEFAULT_FONT_SIZE_SP, getResources().getDisplayMetrics());
        }
        mTextPaint.setTextSize(mTextSizePx);


        mPaint = new Paint();
        mPaint.setColor(Color.WHITE);
        mPaint.setStyle(Paint.Style.FILL);

        // initialize the layout, but the height still needs to be set
        //mLayout = new MongolLayout(mText, mTextPaint);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        // TODO do fewer expensive calculations here

        // TODO don't need to calculate this if using sticky width
        // TODO pass in a limit where we can stop measuring?
        int desiredHeight = (int) MongolLayout.getDesiredHeight(mText, 0, mText.length(), mTextPaint);

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;

        //Measure Height
        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            height = Math.min(desiredHeight, heightSize);
        } else {
            height = desiredHeight;
        }

        // add padding calculations
        int desiredWidth;
        //mLastDesiredWidth = desiredWidth;

        // desired width
        if (mStickyWidth != STICKY_WIDTH_UNDEFINED) {
            // used if the first layout got the wrong size
            desiredWidth = mStickyWidth;
        } else {
            MongolLayout layout = new MongolLayout(mText, 0, mText.length(), mTextPaint, height, Gravity.TOP, 1, 0, false, Integer.MAX_VALUE);
            desiredWidth = layout.getWidth();
        }

        //Measure Width
        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            width = Math.min(desiredWidth, widthSize);
        } else {
            width = desiredWidth;
        }

        // record results
        recordMeasureResults(widthMeasureSpec, height, desiredWidth);

        setMeasuredDimension(width, height);
    }

    private static final int OLD_WIDTH_SPEC_INDEX = 0;
    private static final int OLD_CHOSEN_HEIGHT_INDEX = 1;
    private static final int OLD_DESIRED_WIDTH_INDEX = 2;
    private static final int NEW_WIDTH_SPEC_INDEX = 3;
    private static final int NEW_CHOSEN_HEIGHT_INDEX = 4;
    private static final int NEW_DESIRED_WIDTH_INDEX = 5;
    private void recordMeasureResults(int widthMeasureSpec, int chosenHeight, int desiredWidth) {
        // this keeps track of the last two onMeasure passes

        mOnMeasureData[OLD_WIDTH_SPEC_INDEX] = mOnMeasureData[NEW_WIDTH_SPEC_INDEX];
        mOnMeasureData[OLD_CHOSEN_HEIGHT_INDEX] = mOnMeasureData[NEW_CHOSEN_HEIGHT_INDEX];
        mOnMeasureData[OLD_DESIRED_WIDTH_INDEX] = mOnMeasureData[NEW_DESIRED_WIDTH_INDEX];
        mOnMeasureData[NEW_WIDTH_SPEC_INDEX] = widthMeasureSpec;
        mOnMeasureData[NEW_CHOSEN_HEIGHT_INDEX] = chosenHeight;
        mOnMeasureData[NEW_DESIRED_WIDTH_INDEX] = desiredWidth;
    }

//    @Override
//    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        // Tell the parent layout how big this view would like to be
//        // but still respect any requirements (measure specs) that are passed down.
//
//        // determine the height
//        int height;
//        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
//        int heightRequirement = MeasureSpec.getSize(heightMeasureSpec);
//        if (heightMode == MeasureSpec.EXACTLY) {
//            height = heightRequirement;
//        } else {
//            int desiredHeight = (int) MongolLayout.getDesiredHeight(mText, 0, mText.length(), mTextPaint)
//                    + getPaddingTop() + getPaddingBottom();
//            if (heightMode == MeasureSpec.AT_MOST && desiredHeight > heightRequirement) {
//                height = heightRequirement;
//            } else {
//                height = desiredHeight;
//            }
//        }
//
//        // determine the width
//        int width;
//        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
//        int widthRequirement = MeasureSpec.getSize(widthMeasureSpec);
//        if (widthMode == MeasureSpec.EXACTLY) {
//            width = widthRequirement;
//        } else {
////            if (mStaticLayout == null || mStaticLayout.getHeight() != height || mStaticLayoutNeedsRedraw) {
////                int wrapHeight = height - getPaddingTop() - getPaddingBottom();
////                mStaticLayout = new MongolStaticLayout(mText, mTextPaint, wrapHeight, Gravity.TOP, 1, 0);
////                mStaticLayoutNeedsRedraw = false;
////            }
//            mLayout.setHeight(height);
//            int desiredWidth = mLayout.getWidth() + getPaddingLeft() + getPaddingRight();
//            if (widthMode == MeasureSpec.AT_MOST && desiredWidth > widthRequirement) {
//                width = widthRequirement;
//            } else {
//                width = desiredWidth;
//            }
//        }
//
//        // Required call: set width and height
//        setMeasuredDimension(width, height);
//    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        //if (h != oldh) {
//            mLayout.setHeight(h);
//            mNeedsRelayout = false;
        //}

        mLayout = new MongolLayout(mText, 0, mText.length(), mTextPaint, h, Gravity.TOP, 1, 0, false, Integer.MAX_VALUE);

    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int w = right - left;
        int h = bottom - top;
        super.onLayout(changed, left, top, right, bottom);


        // Here we need to determine if the width has been unnecessarily constrained.
        // We will try for a re-fit only once. If the sticky width is defined, we have
        // already tried to re-fit once, so we are not going to have another go at it since it
        // will (probably) have the same result.
        // old width mode was AT_MOST
        // old desiredWidth did not exceed the old width size
        // the new height was less than the old height
        // the old desired width equals the new width
        // the new desired width was greater than the old desired width
        int oldWidthMode = MeasureSpec.getMode(mOnMeasureData[OLD_WIDTH_SPEC_INDEX]);
        int oldWidthSize = MeasureSpec.getSize(mOnMeasureData[OLD_WIDTH_SPEC_INDEX]);

        if (oldWidthMode == MeasureSpec.AT_MOST &&
                mOnMeasureData[OLD_DESIRED_WIDTH_INDEX] <= oldWidthSize &&
                h < mOnMeasureData[OLD_CHOSEN_HEIGHT_INDEX] &&
                w < mOnMeasureData[NEW_DESIRED_WIDTH_INDEX] &&
                mStickyWidth == STICKY_WIDTH_UNDEFINED) {

            mStickyWidth = mOnMeasureData[NEW_DESIRED_WIDTH_INDEX];
            getViewTreeObserver().addOnPreDrawListener(this);
        } else {
            mStickyWidth = STICKY_WIDTH_UNDEFINED;
        }

//        if (h <= mBreakHeight && (w < mLastDesiredWidth) && (mStickyWidth == STICKY_WIDTH_UNDEFINED)) {
//            mStickyWidth = mLastDesiredWidth;
//            getViewTreeObserver().addOnPreDrawListener(this);
//        } else {
//            mStickyWidth = STICKY_WIDTH_UNDEFINED;
//        }
        mOnMeasureData = new int[6];
        Log.d(TAG, ">>>>onLayout: w=" + w + " h=" + h + " mStickyWidth=" + mStickyWidth);



        // TODO Make it a MongolLayout where parameters can be adjusted rather than creating a new
        // layout every time.

//        int h = bottom - top;
//        mLayout.setHeight(h);
        if (mNeedsRelayout) {
            mLayout.reflowLines();
            mNeedsRelayout = false;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        mLayout.draw(canvas);
    }


    public CharSequence getText() {
        return mText;
    }

    public void setText(String text) {
        mText = text;
        mNeedsRelayout = true;
        invalidate();
        requestLayout();
    }

    public int getTextColor() {
        return mTextColor;
    }

    public void setTextColor(int color) {
        mTextColor = color;
        mTextPaint.setColor(mTextColor);
        invalidate();
    }

    /**
     * @return text size in pixels
     */
    public float getTextSize() {
        return mTextSizePx;
    }

    /**
     * @param size in SP units
     */
    public void setTextSize(int size) {
        mTextSizePx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                size, getResources().getDisplayMetrics());
        mTextPaint.setTextSize(mTextSizePx);
        //mStaticLayoutNeedsRedraw = true;
        mNeedsRelayout = true;
        invalidate();
        requestLayout();
    }

    public int getGravity() {
        return mGravity;
    }

    /**
     *  This sets a custom gravity attribute but uses the same values as Android gravity.
     *  The gravity values are used as integers and not flags. Thus, combining two
     *  flags with | will not work.
     *
     * @param gravity Choices are Gravity.TOP (default), Gravity.CENTER_HORIZONTAL, and Gravity.BOTTOM
     */
    public void setGravity(int gravity) {
        if (mGravity != gravity) {
            mGravity = gravity;
            invalidate();
        }
    }

    @Override
    public boolean onPreDraw() {
        getViewTreeObserver().removeOnPreDrawListener(this);
        if (mStickyWidth == STICKY_WIDTH_UNDEFINED) { // Happy with the selected width.
            return true;
        }

        Log.d(TAG, ">>>>onPreDraw() requesting new layout");
        requestLayout();
        return false;
    }
}
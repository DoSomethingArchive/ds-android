package org.dosomething.android.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * A square ImageView that adjusts its height based on the width. Used particularly in the Campaign
 * gallery to have the images actually stretch to fit the column width.
 *
 * Kudos to this guy here for the idea: http://stackoverflow.com/a/15264039
 */
public class SquareImageView extends ImageView {
    public SquareImageView(Context context) {
        super(context);
    }

    public SquareImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(getMeasuredWidth(), getMeasuredWidth()); // Snap to width
    }
}

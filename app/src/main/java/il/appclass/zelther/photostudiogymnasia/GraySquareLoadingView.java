package il.appclass.zelther.photostudiogymnasia;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;

public class GraySquareLoadingView extends View {

    RotateAnimation rotateAnimation;

    {
        rotateAnimation = new RotateAnimation(0,360, Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
        rotateAnimation.setDuration(4000);
        rotateAnimation.setRepeatCount(Animation.INFINITE);
    }

    public GraySquareLoadingView(Context context) {
        super(context);
    }

    public GraySquareLoadingView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GraySquareLoadingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.GRAY);
    }

    /**
     * Sets the loading animation on and off
     * @param on If true - set visibility to visible and sets animation. If false, set visibility to gone.
     */
    public void setAnimationOn(boolean on) {
        if(on) {
            super.startAnimation(rotateAnimation);
            super.setVisibility(View.VISIBLE);
        } else {
            super.clearAnimation();
            super.setVisibility(View.GONE);
        }
    }
}

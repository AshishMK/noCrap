package com.x.nocrap.utils.animation;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.os.Handler;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import com.x.nocrap.R;
import com.x.nocrap.application.AppController;
import com.x.nocrap.utils.Screen;


/**
 * Class provide various animation for the views in application wide
 */
public class AnimUtil {

    /**
     * Rotate the target view with animation
     *
     * @param view
     */
    public static void rotateView(View view) {
        int height = view.getHeight();
        int width = view.getHeight();
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(view, View.ROTATION, 0.0f, 360.0f);
        ObjectAnimator pivotX = ObjectAnimator.ofFloat(view, "pivotX", width / 2);
        ObjectAnimator pivotY = ObjectAnimator.ofFloat(view, "pivotY", height / 2);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(3500);
        objectAnimator.setRepeatCount(Animation.INFINITE);
        animatorSet.setInterpolator(new LinearInterpolator());
        animatorSet.playTogether(objectAnimator, pivotX, pivotY);
        view.setTag(animatorSet);
        animatorSet.start();
    }

    /**
     * Slide in a view from right corner
     */
    public static ObjectAnimator slideInFromRight(View v, int parentWidth) {
        ObjectAnimator translateAnimation =
                ObjectAnimator.ofFloat(v, View.TRANSLATION_X,
                        parentWidth, 0);
        translateAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
        translateAnimation.setDuration(800);
        //translateAnimation.setRepeatMode(ValueAnimator.REVERSE);
        //translateAnimation.setRepeatCount(1);
        translateAnimation.start();
        return translateAnimation;
    }

    /**
     * Slide out a view from right corner
     */
    public static ObjectAnimator slideOutFromRight(View v, int parentWidth) {
        ObjectAnimator translateAnimation =
                ObjectAnimator.ofFloat(v, View.TRANSLATION_X,
                        0, parentWidth);
        translateAnimation.setInterpolator(new LinearInterpolator());
        translateAnimation.setDuration(800);
        //translateAnimation.setRepeatMode(ValueAnimator.REVERSE);
        //translateAnimation.setRepeatCount(1);
        translateAnimation.start();
        return translateAnimation;
    }


    /**
     * Animate view on discreet or minute levels
     */
    public static ValueAnimator progressAnimator(int length, int duration) {
        ValueAnimator va;
        va = ValueAnimator.ofInt(0, length);
        va.setDuration(duration);
        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                //progressBarMusic.setProgress(i+(Integer) animation.getAnimatedValue());
            }
        });

        va.start();
        return va;
    }

    /**
     * Rotate the target view with animation
     *
     * @param view
     */
    public static void translateUpDown(View view) {
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, 0.0f, Screen.dp(8), 0, Screen.dp(8));
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(2400);
        objectAnimator.setRepeatMode(ValueAnimator.REVERSE);
        objectAnimator.setRepeatCount(Animation.INFINITE);
        animatorSet.setInterpolator(new LinearInterpolator());
        animatorSet.playTogether(objectAnimator);
        view.setTag(animatorSet);
        animatorSet.start();
    }

    /**
     * method to apply reveal animation on target view
     *
     * @param viewRoot
     */
    public static void animateRevealShow(View viewRoot) {
        int cx = (viewRoot.getLeft() + viewRoot.getRight()) / 2;
        int cy = (viewRoot.getTop() + viewRoot.getBottom()) / 2;
        int finalRadius = Math.max(viewRoot.getWidth(), viewRoot.getHeight());
        Animator anim = ViewAnimationUtils.createCircularReveal(viewRoot, cx, cy, 0, finalRadius);
        viewRoot.setVisibility(View.VISIBLE);
        anim.setDuration(AppController.getInstance().getResources().getInteger(R.integer.anim_duration_mid_long));
        anim.setInterpolator(new AccelerateInterpolator());
        anim.start();
    }


    public static void likeAnimation(
            DotsView vDotsView,
            CircleViewAnim vCircle,
            ImageView ivStar) {


        ivStar.setImageResource(R.drawable.ic_love_fill);
        vCircle.setInnerCircleRadiusProgress(0);
        vCircle.setOuterCircleRadiusProgress(0);
        vDotsView.setCurrentProgress(0);

        AnimatorSet animatorSet = new AnimatorSet();
 /*       ObjectAnimator starScaleYAnimator = ObjectAnimator.ofFloat(ivStar, ImageView.SCALE_Y, 0.2f, 1f);
        starScaleYAnimator.setDuration(350);
        starScaleYAnimator.setStartDelay(250);
      //  starScaleYAnimator.setInterpolator(new OvershootInterpolator(4));

        ObjectAnimator starScaleXAnimator = ObjectAnimator.ofFloat(ivStar, ImageView.SCALE_X, 0.2f, 1f);
        starScaleXAnimator.setDuration(350);
        starScaleXAnimator.setStartDelay(250);*/
        //starScaleXAnimator.setInterpolator(new OvershootInterpolator(4));


        PropertyValuesHolder pvhX = PropertyValuesHolder.ofFloat("scaleX", .0f, 1.5f, 1, 1.5f, 0);
        PropertyValuesHolder pvhY = PropertyValuesHolder.ofFloat("scaleY", .0f, 1.5f, 1, 1.5f, 0);
        ObjectAnimator starScaleYAnimator = ObjectAnimator.ofPropertyValuesHolder(ivStar, pvhX, pvhY, PropertyValuesHolder.ofFloat(View.ALPHA.getName(), 0.2f, 1f, 1, 0.2f));

        starScaleYAnimator.setDuration(1500);
        //starScaleYAnimator.setRepeatMode(ValueAnimator.REVERSE);
        //starScaleYAnimator.setRepeatCount(1);

        ObjectAnimator outerCircleAnimator = ObjectAnimator.ofFloat(vCircle, CircleViewAnim.OUTER_CIRCLE_RADIUS_PROGRESS, 0.1f, 1f);
        outerCircleAnimator.setDuration(250);
        outerCircleAnimator.setInterpolator(new DecelerateInterpolator());

        ObjectAnimator innerCircleAnimator = ObjectAnimator.ofFloat(vCircle, CircleViewAnim.INNER_CIRCLE_RADIUS_PROGRESS, 0.1f, 1f);
        innerCircleAnimator.setDuration(200);
        innerCircleAnimator.setStartDelay(200);
        innerCircleAnimator.setInterpolator(new DecelerateInterpolator());
        ObjectAnimator dotsAnimator = ObjectAnimator.ofFloat(vDotsView, DotsView.DOTS_PROGRESS, 0, 1f);
        dotsAnimator.setDuration(900);
        dotsAnimator.setStartDelay(50);
        dotsAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        animatorSet.playTogether(
                outerCircleAnimator,
                innerCircleAnimator,
                starScaleYAnimator,
                dotsAnimator
        );
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                //    super.onAnimationStart(animation);
                ivStar.setVisibility(View.VISIBLE);


            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                ivStar.setVisibility(View.GONE);

                new Handler().post(new Runnable() {
                    @Override
                    public void run() {

                        //     addToFav();
                    }
                });
            }

            @Override
            public void onAnimationCancel(Animator animation) {
               /* vCircle.setInnerCircleRadiusProgress(0);
                vCircle.setOuterCircleRadiusProgress(0);
                vDotsView.setCurrentProgress(0);
                ivStar.setScaleX(1);
                ivStar.setScaleY(1);
                ivStar.setAlpha(1f);*/
                ivStar.setVisibility(View.GONE);
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {

                        //addToFav();
                    }
                });
            }
        });
        animatorSet.start();
    }

}

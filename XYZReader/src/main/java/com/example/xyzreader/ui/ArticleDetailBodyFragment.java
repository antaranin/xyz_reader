package com.example.xyzreader.ui;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Fragment;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.widget.NestedScrollView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.xyzreader.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import hugo.weaving.DebugLog;

/**
 * A fragment representing a single Article detail screen. This fragment is
 * either contained in a {@link ArticleListActivity} in two-pane mode (on
 * tablets) or a {@link ArticleDetailActivity} on handsets.
 */
public class ArticleDetailBodyFragment extends Fragment
{
    public static final String ARG_ITEM_ID = "item_id";
    private static final String TAG = "ArticleDetailFragment";
    private static final String LOG_TAG = "ArticleDetailBodyFragment";

    @Bind(R.id.body_scroll)
    NestedScrollView scrollView;

    @Bind(R.id.article_body)
    TextView bodyView;

    private String body;
    private Integer backgroundColor;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ArticleDetailBodyFragment()
    {
    }

    public static ArticleDetailBodyFragment newInstance()
    {
        ArticleDetailBodyFragment fragment = new ArticleDetailBodyFragment();
        return fragment;
    }

    static float progress(float v, float min, float max)
    {
        return constrain((v - min) / (max - min), 0, 1);
    }

    static float constrain(float val, float min, float max)
    {
        if (val < min)
        {
            return min;
        }
        else if (val > max)
        {
            return max;
        }
        else
        {
            return val;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null)
        {
            log("Restoring");
        }

        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View mRootView = inflater.inflate(R.layout.fragment_article_detail, container, false);
        ButterKnife.bind(this, mRootView);


        if(body != null)
            bodyView.setText(Html.fromHtml(body));
        if(backgroundColor != null)
            setBackgroundColor(backgroundColor);
        bodyView.setTypeface(Typeface.createFromAsset(getResources().getAssets(), "Rosario-Regular.ttf"));
        return mRootView;
    }

    public void setBackgroundColor(int backgroundColor)
    {
        this.backgroundColor = backgroundColor;
        if(scrollView != null)
        {
            Drawable scrollBackground = scrollView.getBackground();
            int currentColor = scrollBackground != null ? ((ColorDrawable) scrollBackground).getColor() : Color.WHITE;
            ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), currentColor, backgroundColor);
            colorAnimation.setDuration(800); // milliseconds
            colorAnimation.addUpdateListener(animator -> scrollView.setBackgroundColor((int) animator.getAnimatedValue()));
            colorAnimation.start();
        }
    }

    @DebugLog
    public void setBody(String body)
    {
        this.body = body;
        if(bodyView != null)
            bodyView.setText(Html.fromHtml(body));
    }

    @SuppressLint("LongLogTag")
    private void log(String message)
    {
        Log.d(LOG_TAG, message);
    }
}

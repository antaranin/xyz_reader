package com.example.xyzreader.ui;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.FragmentManager;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.util.Log;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.data.ItemsContract;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import hugo.weaving.DebugLog;

/**
 * An activity representing a single Article detail screen, letting you swipe between articles.
 */
public class ArticleDetailActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor>
{

    private static final String LOG_TAG = "ArticleDetailActivity";
    @Bind(R.id.backdrop)
    ImageView backdrop;
    @Bind(R.id.collapsing_toolbar)
    CollapsingToolbarLayout collapsingToolbarLayout;
    private Cursor mCursor;
    private long mStartId;
    private int imageColor;
    private int primaryColor;
    private int previousColor;
    private ViewPager mPager;
    private MyPagerAdapter mPagerAdapter;

    private int cursorPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
        setContentView(R.layout.activity_article_detail);
        ButterKnife.bind(this);

        Toolbar toolbar = ButterKnife.findById(this, R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        //getSupportActionBar().setTitle("Title");

        getLoaderManager().initLoader(0, null, this);

        primaryColor = ContextCompat.getColor(this, R.color.primary);
        previousColor = primaryColor;
        imageColor = primaryColor;

        AppBarLayout barLayout = ButterKnife.findById(this, R.id.appbar);

        barLayout.addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {

            //log(String.format("Offset => %s, barLayout height => %s, support action bar height => %s",
                    //verticalOffset, barLayout.getHeight(), getSupportActionBar().getHeight()));
            //log("collapsing tolbar height => " + collapsingToolbarLayout.getHeight());
            Integer colorToSet = null;
            if (verticalOffset <= -ViewCompat.getMinimumHeight(collapsingToolbarLayout) * 2)
            {
                if (imageColor != previousColor)
                    colorToSet = imageColor;

            }
            else
            {
                if (primaryColor != previousColor)
                    colorToSet = primaryColor;
            }

            if (colorToSet == null)
                return;

            previousColor = colorToSet;


            if (colorToSet == primaryColor)
                colorToSet = Color.WHITE;
            mPagerAdapter.getFragment(mPager.getCurrentItem()).setBackgroundColor(colorToSet);

        });

        mPagerAdapter = new MyPagerAdapter(getFragmentManager());
        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(mPagerAdapter);
        mPager.setPageMargin((int) TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics()));
        mPager.setPageMarginDrawable(new ColorDrawable(0x22000000));

        mPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener()
        {
            @Override
            public void onPageSelected(int position)
            {
                log("Page selected => " + position);
                if (mCursor != null)
                {
                    mCursor.moveToPosition(position);
                    cursorPosition = position;
                    set_toolbar_data();
                }
            }
        });

        if (savedInstanceState == null)
        {
            if (getIntent() != null && getIntent().getData() != null)
            {
                mStartId = ItemsContract.Items.getItemId(getIntent().getData());
            }
        }
        else
        {
            cursorPosition = savedInstanceState.getInt("CursorPos", -1);
            if(cursorPosition != -1 && mCursor != null)
            {
                mCursor.moveToPosition(cursorPosition);
                set_toolbar_data();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putInt("CursorPos", cursorPosition);
    }

    @OnClick(R.id.fab)
    void on_share_btn_pressed(View btn)
    {
        log("Share btn pressed");
        startActivity(Intent.createChooser(ShareCompat.IntentBuilder.from(this)
                .setType("text/plain")
                .setText("Some sample text")
                .getIntent(), getString(R.string.action_share)));
    }


    @DebugLog
    private void set_toolbar_data()
    {
        imageColor = primaryColor;
        String title = mCursor.getString(ArticleLoader.Query.TITLE);


        collapsingToolbarLayout.setTitle(title);
        log("Title =>  " + title);
        log("Cursor position " + cursorPosition);

        TextView date_tv = ButterKnife.findById(this, R.id.sub_title);
        date_tv.setText(String.format(getString(R.string.by_placeholder),
                        DateUtils.getRelativeTimeSpanString(
                                mCursor.getLong(ArticleLoader.Query.PUBLISHED_DATE),
                                System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                                DateUtils.FORMAT_ABBREV_ALL).toString(),
                        mCursor.getString(ArticleLoader.Query.AUTHOR))
        );

        String body = mCursor.getString(ArticleLoader.Query.BODY);
        log("Body => " + body);
        if(mPagerAdapter.getFragment(mPager.getCurrentItem()) != null)
            mPagerAdapter.getFragment(mPager.getCurrentItem()).setBody(body);

        Glide.with(ArticleDetailActivity.this)
                .load(mCursor.getString(ArticleLoader.Query.PHOTO_URL))
                .centerCrop()
                .crossFade()
                .skipMemoryCache(true)
                .listener(new RequestListener<String, GlideDrawable>()
                {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource)
                    {
                        imageColor = primaryColor;
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource)
                    {
                        Palette.from(((GlideBitmapDrawable) resource).getBitmap())
                                .maximumColorCount(12)
                                .generate(palette -> {
                                    imageColor = palette
                                            .getDarkMutedColor(ContextCompat.getColor(ArticleDetailActivity.this, R.color.primary_dark));
                                    collapsingToolbarLayout.setContentScrimColor(imageColor);
                                    if (previousColor != primaryColor)
                                    {
                                        mPagerAdapter.getFragment(mPager.getCurrentItem()).setBackgroundColor(imageColor);
                                        previousColor = imageColor;
                                    }

                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                                    {
                                        Window window = getWindow();

                                        // clear FLAG_TRANSLUCENT_STATUS flag:
                                        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

                                        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
                                        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                                        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), window.getStatusBarColor(), imageColor);
                                        colorAnimation.setDuration(300); // milliseconds
                                        colorAnimation.addUpdateListener(animator -> window.setStatusBarColor((int) animator.getAnimatedValue()));
                                        colorAnimation.start();
                                    }
                                });

                        return false;
                    }
                })
                .into(backdrop);

    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle)
    {
        return ArticleLoader.newAllArticlesInstance(this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor)
    {
        mCursor = cursor;
        mPagerAdapter.notifyDataSetChanged();
        if(cursorPosition != -1)
        {
            mCursor.moveToPosition(cursorPosition);
            mPager.setCurrentItem(cursorPosition, false);
            set_toolbar_data();
        }
        else if (mStartId > 0)
        {
            mCursor.moveToFirst();
            while (!mCursor.isAfterLast())
            {
                if (mCursor.getLong(ArticleLoader.Query._ID) == mStartId)
                {
                    cursorPosition = mCursor.getPosition();
                    mPager.setCurrentItem(cursorPosition, false);
                    set_toolbar_data();
                    break;
                }
                mCursor.moveToNext();
            }
            mStartId = 0;
        }

        // Select the start ID
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader)
    {
        mCursor = null;
        cursorPosition = -1;
        mPagerAdapter.notifyDataSetChanged();
    }

    private void log(String message)
    {
        Log.d(LOG_TAG, message);
    }

    private class MyPagerAdapter extends FragmentStatePagerAdapter
    {
        private SparseArray<ArticleDetailBodyFragment> fragments = new SparseArray<>();

        public MyPagerAdapter(FragmentManager fm)
        {
            super(fm);
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object)
        {
            super.setPrimaryItem(container, position, object);
            if (fragments.get(position) == null)
                fragments.append(position, (ArticleDetailBodyFragment) object);
        }

        @Override
        public ArticleDetailBodyFragment getItem(int position)
        {
            return new ArticleDetailBodyFragment();
        }

        public ArticleDetailBodyFragment getFragment(int position)
        {
            return fragments.get(position);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position)
        {
            ArticleDetailBodyFragment fragment = (ArticleDetailBodyFragment) super.instantiateItem(container, position);
            fragments.append(position, fragment);
            if(cursorPosition == position && mCursor != null)
            {
                set_toolbar_data();
            }
            return fragment;
        }

        @Override
        public int getCount()
        {
            return (mCursor != null) ? mCursor.getCount() : 0;
        }
    }
}

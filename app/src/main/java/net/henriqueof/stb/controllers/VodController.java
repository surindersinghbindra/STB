package net.henriqueof.stb.controllers;

import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bluelinelabs.conductor.RouterTransaction;
import com.bluelinelabs.conductor.changehandler.FadeChangeHandler;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.IAdapter;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;
import com.mikepenz.fastadapter_extensions.scroll.EndlessRecyclerOnScrollListener;
import com.squareup.picasso.Picasso;

import net.henriqueof.stb.CustomLayoutManager;
import net.henriqueof.stb.R;
import net.henriqueof.stb.application.Application;
import net.henriqueof.stb.controllers.base.BaseController;
import net.henriqueof.stb.items.CategoryItem;
import net.henriqueof.stb.items.VodItem;
import net.henriqueof.stb.services.StalkerService;
import net.henriqueof.stb.services.StbService;

import butterknife.BindView;
import fm.jiecao.jcvideoplayer_lib.JCMediaManager;
import fm.jiecao.jcvideoplayer_lib.JCUserAction;
import fm.jiecao.jcvideoplayer_lib.JCUtils;
import fm.jiecao.jcvideoplayer_lib.JCVideoPlayer;
import fm.jiecao.jcvideoplayer_lib.JCVideoPlayerManager;
import fm.jiecao.jcvideoplayer_lib.JCVideoPlayerStandard;

public class VodController extends BaseController implements StbService.StateListener {
    private static final String TAG = "VodController";

    // UI References
    @BindView(R.id.thumbs_checkbox) CheckBox thumbnailView;
    @BindView(R.id.datesort_checkbox) CheckBox dateSort;
    @BindView(R.id.namesort_checkbox) CheckBox nameSort;
    @BindView(R.id.categories_rv) RecyclerView categoriesRecyclerView;
    @BindView(R.id.vod_rv) RecyclerView vodRecyclerView;
    @BindView(R.id.vod_picture) ImageView vodPicture;
    @BindView(R.id.vod_name) TextView vodName;
    @BindView(R.id.vod_description) TextView vodDescription;
    @BindView(R.id.preview_layout) LinearLayout previewLayout;

    private StbService stbService;
    private FastItemAdapter<CategoryItem> categoriesAdapter;
    private FastItemAdapter<VodItem> vodAdapter;

    private String currentSortBy = "added";
    private String currentCategoryId = "*";

    public enum ListType { LIST, GRID }
    private ListType listType;

    private int selectedCategoryPosition;

    private int mSeekTimePosition = 0;
    private float seekThreshold = 1;

    @Override
    protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
        return inflater.inflate(R.layout.controller_vod, container, false);
    }

    @Override
    protected void onViewBound(@NonNull View view) {
        super.onViewBound(view);

        listType = ListType.GRID;

        // Reference to application instance
        Application application = (Application) getActivity().getApplication();

        // Observe list/thumbnail view checkbox and adjust the list accordingly
        thumbnailView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    listType = ListType.GRID;
                    vodRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 5));
                    previewLayout.setVisibility(View.GONE);
                    resetVodList();
                } else {
                    listType = ListType.LIST;
                    CustomLayoutManager customLayoutManager = new CustomLayoutManager(getActivity());
                    customLayoutManager.setFocusHandler(new CustomLayoutManager.FocusHandler() {
                        @Override
                        public void onFocusPosition(int position) {
                            VodItem item = vodAdapter.getItem(position);
                            String pictureUri;

                            if (item.getPicture().startsWith("http://") || item.getPicture().startsWith("https://"))
                                pictureUri = item.getPicture();
                            else
                                pictureUri = stbService.getServer().replace("/stalker_portal/c/", item.getPicture());

                            Picasso.with(getActivity()).load(pictureUri).fit().into(vodPicture);
                            vodName.setText(item.getName());
                            vodDescription.setText(item.getDescription());
                        }
                    });
                    vodRecyclerView.setLayoutManager(customLayoutManager);
                    previewLayout.setVisibility(View.VISIBLE);
                    resetVodList();
                }
            }
        });

        dateSort.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    nameSort.setChecked(false);
                    currentSortBy = "added";
                    resetVodList();
                }
            }
        });

        nameSort.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    dateSort.setChecked(false);
                    currentSortBy = "name";
                    resetVodList();
                }
            }
        });

        categoriesAdapter = new FastItemAdapter<>();
        categoriesAdapter.withSelectable(true);
        categoriesAdapter.withOnClickListener(new FastAdapter.OnClickListener<CategoryItem>() {
            @Override
            public boolean onClick(View v, IAdapter<CategoryItem> adapter, CategoryItem item, int position) {
                selectedCategoryPosition = position;
                currentCategoryId = item.getCategoryId();
                resetVodList();

                return true;
            }
        });

        vodAdapter = new FastItemAdapter<>();
        vodAdapter.withSelectable(true);
        vodAdapter.withOnClickListener(new FastAdapter.OnClickListener<VodItem>() {
            @Override
            public boolean onClick(View v, IAdapter<VodItem> adapter, final VodItem item, int position) {

                stbService.vodCreateLink(item.getCmd(), new StbService.Request<StalkerService.Stream>() {
                    @Override
                    public void done(StalkerService.Stream response) {
                        // TODO:
                        JCVideoPlayerStandard.FULLSCREEN_ORIENTATION = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                        JCVideoPlayerStandard.startFullscreen(getActivity(), JCVideoPlayerStandard.class, response.cmd, item.getName());
                    }

                    @Override
                    public void error() {
                        Log.e(TAG, "Error getting stream URL");

                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setTitle("Error").setMessage("Nothing to play").setPositiveButton("OK", null).show();
                    }
                });
                return true;
            }
        });

        categoriesRecyclerView.setHasFixedSize(true);
        categoriesRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        categoriesRecyclerView.setAdapter(categoriesAdapter);

        vodRecyclerView.setHasFixedSize(true);
        if (listType == ListType.LIST)
            vodRecyclerView.setLayoutManager(new CustomLayoutManager(getActivity()));
        else
            vodRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 5));
        thumbnailView.performClick();
        vodRecyclerView.setAdapter(vodAdapter);

        stbService = application.getStbService();
        application.setStateListener(this);
    }

    @Override
    public boolean handleBack() {
        if (JCVideoPlayer.backPress()) {
            return true;
        } else if (vodRecyclerView.getFocusedChild() != null) {
            // Focus on the last selected category item
            categoriesRecyclerView.findViewHolderForAdapterPosition(selectedCategoryPosition).itemView.requestFocus();
            return true;
        } else {
            return super.handleBack();
        }
    }

    @Override
    public void onStateChange(StbService.State newState) {
        if (newState == StbService.State.LOADED) {

            // Called to force the list to request the correct item layout
            // called here because it depends on a valid stbService object
            thumbnailView.setChecked(true);

            // Get categories list.
            stbService.vodGetCategories(new StbService.Request<StalkerService.Category[]>() {
                @Override
                public void done(StalkerService.Category[] response) {
                    for (StalkerService.Category category : response) {
                        categoriesAdapter.add(new CategoryItem(category));
                    }
                }

                @Override
                public void error() {
                    Log.e(TAG, "Error retrieving categories list");
                }
            });

            // Get movies list.
            resetVodList();
        } else if (newState == StbService.State.ERROR || newState == StbService.State.DISCONNECTED) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Disconnected from the server")
                    .setMessage("Please check your internet connection and connect again from the settings screen")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            getRouter().handleBack();
                        }
                    })
                    .setNeutralButton("Settings", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            getRouter().pushController(RouterTransaction.with(new SettingsController())
                                    .pushChangeHandler(new FadeChangeHandler())
                                    .popChangeHandler(new FadeChangeHandler()));
                        }
                    })
                    .setCancelable(false)
                    .show();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        JCVideoPlayer jcVideoPlayer = JCVideoPlayerManager.getCurrentJcvd();

        boolean playing  = jcVideoPlayer != null && jcVideoPlayer.currentState == JCVideoPlayer.CURRENT_STATE_PLAYING;

        if (playing && (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT || keyCode == KeyEvent.KEYCODE_DPAD_LEFT
                || keyCode == KeyEvent.KEYCODE_MEDIA_PREVIOUS || keyCode == KeyEvent.KEYCODE_MEDIA_NEXT)) {
            int totalTimeDuration = jcVideoPlayer.getDuration();

            if (mSeekTimePosition == 0)
                mSeekTimePosition = jcVideoPlayer.getCurrentPositionWhenPlaying();

            float deltaX;

            if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT || keyCode == KeyEvent.KEYCODE_MEDIA_NEXT) {
                deltaX = 1;
                mSeekTimePosition = mSeekTimePosition + (int)(1500 * seekThreshold);

                if (mSeekTimePosition > totalTimeDuration)
                    mSeekTimePosition = totalTimeDuration;
            } else {
                deltaX = -1;
                mSeekTimePosition = mSeekTimePosition - (int)(1500 * seekThreshold);

                if (mSeekTimePosition < 0)
                    mSeekTimePosition = 0;
            }

            seekThreshold += 0.1;

            String seekTime = JCUtils.stringForTime(mSeekTimePosition);
            String totalTime = JCUtils.stringForTime(totalTimeDuration);

            jcVideoPlayer.showProgressDialog(deltaX, seekTime, mSeekTimePosition, totalTime, totalTimeDuration);

            return true;
        } else
            return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        // TODO: Volume icon
        JCVideoPlayer jcVideoPlayer = JCVideoPlayerManager.getCurrentJcvd();

        boolean playing  = jcVideoPlayer != null && jcVideoPlayer.currentState == JCVideoPlayer.CURRENT_STATE_PLAYING;

        if (playing && keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
            jcVideoPlayer.startButton.performClick();
            return true;
        } else if (playing && (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT || keyCode == KeyEvent.KEYCODE_DPAD_LEFT
        || keyCode == KeyEvent.KEYCODE_MEDIA_PREVIOUS || keyCode == KeyEvent.KEYCODE_MEDIA_NEXT)) {
            jcVideoPlayer.dismissProgressDialog();

            jcVideoPlayer.onEvent(JCUserAction.ON_TOUCH_SCREEN_SEEK_POSITION);
            JCMediaManager.instance().mediaPlayer.seekTo(mSeekTimePosition);
            int duration = jcVideoPlayer.getDuration();
            int progress = mSeekTimePosition * 100 / (duration == 0 ? 1 : duration);
            jcVideoPlayer.progressBar.setProgress(progress);

            seekThreshold = 1;

            return true;
        } else if (keyCode == KeyEvent.KEYCODE_PROG_BLUE) {
            // go to Live TV screen
            JCVideoPlayer.releaseAllVideos();

            getRouter().pushController(RouterTransaction.with(new LiveTvController())
                    .pushChangeHandler(new FadeChangeHandler())
                    .popChangeHandler(new FadeChangeHandler()));
            return true;
        } else
            return super.onKeyUp(keyCode, event);
    }

    private void resetVodList() {
        vodAdapter.clear();
        vodRecyclerView.setAdapter(vodAdapter); // Necessary to force invalidation of item layout.

        vodRecyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener(vodRecyclerView.getLayoutManager(), 14) {
            @Override
            public void onLoadMore(final int currentPage) {
                Log.d(TAG, "onLoadMore: currentPage: " + currentPage);

                stbService.vodGetOrderedList(currentCategoryId, currentPage + 1, currentSortBy, new StbService.Request<StalkerService.Vod[]>() {
                    @Override
                    public void done(StalkerService.Vod[] response) {
                        for (StalkerService.Vod vod : response) {
                            vodAdapter.add(vodAdapter.getAdapterItemCount(), new VodItem(vod, VodController.this));
                        }
                    }

                    @Override
                    public void error() {
                        Log.e(TAG, "Error retrieving movies list");
                    }
                });
            }
        });

        getMovieList(1);
    }

    public ListType getListType() {
        return listType;
    }

    public String getServer() {
        return stbService.getServer();
    }

    private void getMovieList(final int page) {

        // TODO: sometimes the object api can be null, in that case we need to find a way to retry to use with when available
        if (stbService == null)
            return;

        stbService.vodGetOrderedList(currentCategoryId, page, currentSortBy, new StbService.Request<StalkerService.Vod[]>() {
            @Override
            public void done(StalkerService.Vod[] response) {
                for (StalkerService.Vod vod : response) {
                    vodAdapter.add(new VodItem(vod, VodController.this));
                }

                if (page == 1 && response.length > 0) {
                    vodRecyclerView.requestFocus();
                }
            }

            @Override
            public void error() {
                Log.e(TAG, "Error retrieving movies list");
            }
        });
    }
}

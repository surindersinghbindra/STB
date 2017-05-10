package net.henriqueof.stb.controllers;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bluelinelabs.conductor.Controller;
import com.bluelinelabs.conductor.RouterTransaction;
import com.bluelinelabs.conductor.changehandler.FadeChangeHandler;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.IAdapter;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;
import com.mikepenz.fastadapter_extensions.scroll.EndlessRecyclerOnScrollListener;

import net.henriqueof.stb.MainActivity;
import net.henriqueof.stb.R;
import net.henriqueof.stb.CustomLayoutManager;
import net.henriqueof.stb.application.Application;
import net.henriqueof.stb.controllers.base.BaseController;
import net.henriqueof.stb.items.GenreItem;
import net.henriqueof.stb.items.ChannelItem;
import net.henriqueof.stb.items.EpgItem;
import net.henriqueof.stb.services.StalkerService;
import net.henriqueof.stb.services.StbService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import butterknife.BindView;
import fm.jiecao.jcvideoplayer_lib.JCUserAction;
import fm.jiecao.jcvideoplayer_lib.JCVideoPlayer;
import fm.jiecao.jcvideoplayer_lib.JCVideoPlayerStandard;

public class LiveTvController extends BaseController implements StbService.StateListener {

    private static final String TAG = "LiveTvController";

    // UI references.
    @BindView(R.id.categories_rv) RecyclerView categoriesRecyclerView;
    @BindView(R.id.channels_rv) RecyclerView channelsRecyclerView;
    @BindView(R.id.epg_rv) RecyclerView epgRecyclerView;
    @BindView(R.id.hud_view) TableLayout hudView;
    @BindView(R.id.channel_name) TextView channelName;
    @BindView(R.id.channel_info) TextView channelInfo;

    @BindView(R.id.all_button) LinearLayout allButton;
    @BindView(R.id.favorite_button) LinearLayout favoriteButton;
    @BindView(R.id.lock_button) LinearLayout lockButton;

    @BindView(R.id.videoplayer) JCVideoPlayerStandard jcVideoPlayerStandard;

    // ...
    private boolean fullscreen;

    // Reference to application instance
    private Application application;
    private MainActivity activity;
    private StbService stbService;

    private String currentStreamUrl;

    private FastItemAdapter<ChannelItem> channelsAdapter;
    private FastItemAdapter<GenreItem> categoriesAdapter;
    private FastItemAdapter<EpgItem> epgAdapter;

    private EndlessRecyclerOnScrollListener endlessRecyclerOnScrollListener;

    private String currentGenreId;
    private int selectedGenrePosition;
    private int selectedChannelPosition;

    private SharedPreferences sharedPref;

    @Override
    protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
        return inflater.inflate(R.layout.controller_live_tv, container, false);
    }

    @Override
    protected void onViewBound(@NonNull View view) {
        super.onViewBound(view);

        // ...
        jcVideoPlayerStandard.widthRatio= 16;
        jcVideoPlayerStandard.heightRatio  = 9;

        // ...
        activity = (MainActivity) getActivity();
        assert activity != null;

        application = (Application) activity.getApplication();
        stbService = application.getStbService();
        sharedPref = application.getSharedPreferences(Application.SETTINGS_KEY, Context.MODE_PRIVATE);

        setUpLists();

        // All, favorite and lock button handlers
        View.OnClickListener onClickListener = new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                    String tag = view.getTag().toString();

                // Clear previous selected item and selected the clicked one
                RecyclerView.ViewHolder viewHolder = categoriesRecyclerView.findViewHolderForAdapterPosition(selectedGenrePosition);
                if (viewHolder != null)
                {
                    View itemView = viewHolder.itemView;
                    itemView.setSelected(false);
                }

                allButton.setSelected(false);
                favoriteButton.setSelected(false);
                lockButton.setSelected(false);

                    switch (tag) {
                        case "all_channels":
                            allButton.setSelected(true);
                            currentGenreId = "*";
                            resetChannelsList();
                            break;
                        case "favorite_channels":
                            favoriteButton.setSelected(true);
                            currentGenreId = null;
                            loadFavChannels();
                            break;
                        case "locked_channels":
                            lockButton.setSelected(true);
                            channelsAdapter.set(new ArrayList<ChannelItem>());
                            break;
                    }
                }
        };

        allButton.setOnClickListener(onClickListener);
        favoriteButton.setOnClickListener(onClickListener);
        lockButton.setOnClickListener(onClickListener);

        allButton.setSelected(true);
        application.setStateListener(this);

        // Controller life cycle listener to stop video playback when focus is lost
        addLifecycleListener(new LifecycleListener() {
            @Override
            public void preDetach(@NonNull Controller controller, @NonNull View view) {
                super.preDetach(controller, view);

                JCVideoPlayer.releaseAllVideos();
            }
        });
    }

    @Override
    protected void onActivityPaused(@NonNull Activity activity) {
        super.onActivityPaused(activity);

        JCVideoPlayer.releaseAllVideos();
    }

    @Override
    protected void onActivityResumed(@NonNull Activity activity) {
        super.onActivityResumed(activity);

        // TODO: restore playing channel state
    }

    @Override
    public void onStateChange(StbService.State newState) {

        if (newState == StbService.State.LOADED) {
            //resetChannelsList();

            // Set last played channel as current
            setCurrentChannel(application.getCurrentChannel());

            // Load all categories
            stbService.itvGetGenres(new StbService.Request<List<StalkerService.Genre>>() {
                @Override
                public void done(List<StalkerService.Genre> response) {

                    List<GenreItem> genreItems = new ArrayList<>();
                    for (StalkerService.Genre genre : response) {
                        if (genre.title.equals("All"))
                            continue;

                        genreItems.add(new GenreItem(genre));
                    }

                    categoriesAdapter.set(genreItems);
                    rememberGenre();
                }

                @Override
                public void error() {
                    Log.e(TAG, "Error loading genres");
                }
            });
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
    public boolean handleBack() {

        if (JCVideoPlayer.backPress())
        {
            fullscreen = false;
            return true;
        } else if (epgRecyclerView.getFocusedChild() != null) {
            // Focus on the last selected channel item
            RecyclerView.ViewHolder selected = channelsRecyclerView.findViewHolderForAdapterPosition(selectedChannelPosition);
            if (selected != null)
                selected.itemView.requestFocus();
            else
                Log.e(TAG, "Unknown selected category position " + selectedChannelPosition);

            return true;
        } else if (channelsRecyclerView.getFocusedChild() != null) {
            // Focus on the last selected genre item
            RecyclerView.ViewHolder selected = categoriesRecyclerView.findViewHolderForAdapterPosition(selectedGenrePosition);
            if (selected != null) {
                selected.itemView.requestFocus();
            }
            else
                Log.e(TAG, "Unknown selected genre position " + selectedGenrePosition);

            return true;
        } else {
            return super.handleBack();
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_DPAD_DOWN || keyCode == KeyEvent.KEYCODE_PAGE_DOWN ||
                keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_PAGE_UP) && fullscreen) {

            if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN || keyCode == KeyEvent.KEYCODE_PAGE_DOWN) {
                application.setSelectedChannel(application.getNextChannel());
            } else {
                application.setSelectedChannel(application.getPreviousChannel());
            }

            ChannelItem channelItem = application.getCurrentChannel();
            setCurrentChannel(channelItem);

            return true;
        } else if (keyCode == KeyEvent.KEYCODE_PROG_BLUE) {
            // go to VoD screen
            JCVideoPlayer.releaseAllVideos();

            getRouter().pushController(RouterTransaction.with(new VodController())
                    .pushChangeHandler(new FadeChangeHandler())
                    .popChangeHandler(new FadeChangeHandler()));
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_PROG_GREEN) {

            if (channelsRecyclerView.hasFocus()) {

                final int position = channelsRecyclerView.getChildAdapterPosition(channelsRecyclerView.getFocusedChild());
                final ChannelItem channel = channelsAdapter.getAdapterItem(position);

                // Retrieve favorite channels list
                stbService.getFavIds(new StbService.Request<String[]>() {
                    @Override
                    public void done(String[] response) {
                        List<String> favoriteChannelsIds = new ArrayList<>(Arrays.asList(response));

                        if (channel.getFavorite() == 1) {
                            channel.setFavorite(0);

                            for (Iterator<String> iterator = favoriteChannelsIds.listIterator(); iterator.hasNext(); ) {
                                String a = iterator.next();
                                if (a.equals(String.valueOf(channel.getChannelId()))) {
                                    iterator.remove();
                                }
                            }

                            Toast.makeText(activity, "channel removed to favorites " + channel.getChannelName(), Toast.LENGTH_LONG).show();
                        } else {
                            channel.setFavorite(1);
                            favoriteChannelsIds.add(String.valueOf(channel.getChannelId()));
                            Toast.makeText(activity, "channel added to favorites "  + channel.getChannelName(), Toast.LENGTH_LONG).show();
                        }

                        stbService.itvSetFav(android.text.TextUtils.join(",", favoriteChannelsIds));
                        channelsAdapter.set(position, channel);
                    }

                    @Override
                    public void error() {

                    }
                });

                channelsAdapter.set(position, channel);
            }

            return true;
        } else if (keyCode == KeyEvent.KEYCODE_PROG_YELLOW) {
            // Lock/unlock
            Toast.makeText(activity, "Yellow button pressed", Toast.LENGTH_LONG).show();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
            if (fullscreen) {
                fullscreen = JCVideoPlayer.backPress();
            }

            return !fullscreen;
        } else if(keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9) {
            // TODO: handle numeral keys
            return true;
        } else
            return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Pass back button event to the system.
        // TODO: handle numeral keys to change channels
        if (keyCode == KeyEvent.KEYCODE_BACK)
            return super.onKeyDown(keyCode, event);

        return fullscreen || super.onKeyDown(keyCode, event);
    }

    private void setUpLists() {
        // ...
        currentGenreId = "*";

        channelsAdapter = new FastItemAdapter<>();
        categoriesAdapter = new FastItemAdapter<>();
        epgAdapter = new FastItemAdapter<>();

        categoriesAdapter.withSelectable(true);
        categoriesAdapter.withOnClickListener(new FastAdapter.OnClickListener<GenreItem>() {
            @Override
            public boolean onClick(View v, IAdapter<GenreItem> adapter, final GenreItem item, final int position) {

                if (item.getCensored() == 1) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);

                    // Set up the input
                    final EditText input = new EditText(activity);

                    // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                    input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

                    // TODO: Better dialog
                    builder.setTitle("Parental control")
                            .setMessage("\nCensored category\nPlease enter password to continue.")
                            .setView(input)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String password = input.getText().toString();
                                    StalkerService.Profile profile = stbService.getProfile();

                                    if (password.equals(profile.parent_password)) {
                                        Toast.makeText(activity, "( ͡° ͜ʖ ͡°)", Toast.LENGTH_LONG).show();

                                        allButton.setSelected(false);
                                        favoriteButton.setSelected(false);
                                        lockButton.setSelected(false);

                                        selectedGenrePosition = position;
                                        currentGenreId = item.getCategoryId();
                                        resetChannelsList();
                                    } else {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                                        builder.setTitle("Wrong password!")
                                                .setPositiveButton("OK", null)
                                                .show();
                                    }
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .show();

                    return false;
                } else {
                    allButton.setSelected(false);
                    favoriteButton.setSelected(false);
                    lockButton.setSelected(false);

                    selectedGenrePosition = position;
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putInt("current_genre", selectedGenrePosition).apply();

                    currentGenreId = item.getCategoryId();
                    resetChannelsList();

                    return true;
                }
            }
        });

        // ...
        channelsAdapter.withSelectable(true);
        channelsAdapter.withOnClickListener(new FastAdapter.OnClickListener<ChannelItem>() {
            @Override
            public boolean onClick(View v, IAdapter<ChannelItem> adapter, ChannelItem item, int position) {

                if (fullscreen) {
                    JCVideoPlayer.backPress();
                    fullscreen = false;
                    return true;
                }

                // if the clicked channel is the selected one goes fullscreen otherwise select the channel
                if (application.getCurrentChannel() != null && application.getCurrentChannel().getChannelNumber() == item.getChannelNumber()) {
                    jcVideoPlayerStandard.onEvent(JCUserAction.ON_ENTER_FULLSCREEN);
                    jcVideoPlayerStandard.startWindowFullscreen();
                    fullscreen = true;
                } else {
                    setCurrentChannel(item);
                }

                selectedChannelPosition = position;
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putInt("current_channel", selectedChannelPosition).apply();

                return true;
            }
        });

        epgAdapter.withOnClickListener(new FastAdapter.OnClickListener<EpgItem>() {
            @Override
            public boolean onClick(View v, IAdapter<EpgItem> adapter, EpgItem item, int position) {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle(item.getEventTitle())
                        .setMessage(item.getPlot())
                        .show();

                return true;
            }
        });

        // Setup lists
        categoriesRecyclerView.setHasFixedSize(true);
        channelsRecyclerView.setHasFixedSize(true);
        epgRecyclerView.setHasFixedSize(true);

        categoriesRecyclerView.setLayoutManager(new LinearLayoutManager(activity));
        final CustomLayoutManager customLayoutManager = new CustomLayoutManager(activity);

        // Update EPG items when a channel on the list is focused
        customLayoutManager.setFocusHandler(new CustomLayoutManager.FocusHandler() {
            @Override
            public void onFocusPosition(int position) {
                final ChannelItem channel = channelsAdapter.getItem(position);

                // TODO: should never happen...
                if (channel == null)
                {
                    Log.e(TAG, "onFocusPosition: channel == null");
                    return;
                }
/*
                // Necessary to activate marquee
                TextView channelName_ = (TextView) customLayoutManager.findViewByPosition(position).findViewById(R.id.channel_name);
                if (channelName_ != null)
                    channelName_.setSelected(true);
*/
                channelName.setText(channel.getChannelNumber() + " " + channel.getChannelName());

                epgAdapter.clear();

                stbService.itvGetShortEpg(channel.getChannelId(), new StbService.Request<StalkerService.EpgItem[]>() {
                    @Override
                    public void done(StalkerService.EpgItem[] response) {

                        for (StalkerService.EpgItem epgItem : response) {
                            epgAdapter.add(new EpgItem(epgItem.t_time, epgItem.name, epgItem.descr));
                        }
                    }

                    @Override
                    public void error() {
                    }
                });
                }
        });

        channelsRecyclerView.setLayoutManager(customLayoutManager);
        epgRecyclerView.setLayoutManager(new LinearLayoutManager(activity));

        categoriesRecyclerView.setAdapter(categoriesAdapter);
        epgRecyclerView.setAdapter(epgAdapter);

        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(channelsRecyclerView.getContext(), DividerItemDecoration.VERTICAL);
        channelsRecyclerView.addItemDecoration(itemDecoration);
        epgRecyclerView.addItemDecoration(itemDecoration);

        channelsRecyclerView.setAdapter(channelsAdapter);
        endlessRecyclerOnScrollListener = new EndlessRecyclerOnScrollListener() {
            @Override
            public void onLoadMore(final int currentPage) {

                if (currentGenreId == null)
                    return;

                Log.d(TAG, "onLoadMore: currentPage: " + currentPage);

                stbService.itvGetOrderedList(currentGenreId, currentPage + 1, new StbService.Request<StalkerService.Channel[]>() {
                    @Override
                    public void done(StalkerService.Channel[] response) {
                        if (currentPage == 0 && response.length > 0) {
                            channelsRecyclerView.requestFocus();
                        }

                        for (StalkerService.Channel channel : response) {
                            if (!channel.logo.isEmpty()) {

                                // If channel logo is relative adjust to the correct absolute path
                                if (!channel.logo.startsWith("http://") && !channel.logo.startsWith("https://"))
                                    channel.logo = stbService.getServer() + "misc/logos/320/" + channel.logo;
                            }

                            channelsAdapter.add(channelsAdapter.getAdapterItemCount(), new ChannelItem(channel));
                        }
                    }

                    @Override
                    public void error() {
                    }
                });
            }
        };

        channelsRecyclerView.addOnScrollListener(endlessRecyclerOnScrollListener);
    }

    private void resetChannelsList() {
        endlessRecyclerOnScrollListener.resetPageCount();
        channelsAdapter.clear();
    }

    private void loadFavChannels() {
        resetChannelsList();

        stbService.getAllFavChannels(new StbService.Request<StalkerService.Channel[]>() {
            @Override
            public void done(StalkerService.Channel[] response) {
                if (response.length > 0) {
                    channelsRecyclerView.requestFocus();
                }

                for (StalkerService.Channel channel : response) {
                    if (!channel.logo.isEmpty()) {

                        // If channel logo is relative adjust to the correct absolute path
                        if (!channel.logo.startsWith("http://") && !channel.logo.startsWith("https://"))
                            channel.logo = stbService.getServer() + "misc/logos/320/" + channel.logo;
                    }

                    channelsAdapter.add(channelsAdapter.getAdapterItemCount(), new ChannelItem(channel));
                }
            }

            @Override
            public void error() {

            }
        });
    }

    private void setCurrentChannel(final ChannelItem channel) {

        if (channel == null)
            return;

        application.setSelectedChannel(channel);

        if (channel.useHttpTmpLink() == 1) {
            stbService.itvCreateLink(channel.getCmd(), new StbService.Request<StalkerService.Stream>() {
                @Override
                public void done(StalkerService.Stream response) {
                    currentStreamUrl = response.cmd;

                    if (fullscreen) {
                        jcVideoPlayerStandard.setUp(currentStreamUrl, JCVideoPlayerStandard.SCREEN_WINDOW_FULLSCREEN, channel.getChannelName());
                        jcVideoPlayerStandard.startVideo();
                        jcVideoPlayerStandard.onEvent(JCUserAction.ON_ENTER_FULLSCREEN);
                        jcVideoPlayerStandard.startWindowFullscreen();
                    } else {
                        jcVideoPlayerStandard.setUp(currentStreamUrl, JCVideoPlayerStandard.SCREEN_LAYOUT_NORMAL, channel.getChannelName());
                        jcVideoPlayerStandard.startVideo();
                    }

                    Log.d(TAG, "currentStreamUrl: " + currentStreamUrl);
                }

                @Override
                public void error() {

                }
            });
        }
        else {
            currentStreamUrl = channel.getCmd();

            if (currentStreamUrl.indexOf(' ') != -1)
                currentStreamUrl = currentStreamUrl.substring(currentStreamUrl.indexOf(' ') + 1);

            if (fullscreen) {
                jcVideoPlayerStandard.setUp(currentStreamUrl, JCVideoPlayerStandard.SCREEN_WINDOW_FULLSCREEN, channel.getChannelName());
                jcVideoPlayerStandard.startVideo();
                jcVideoPlayerStandard.onEvent(JCUserAction.ON_ENTER_FULLSCREEN);
                jcVideoPlayerStandard.startWindowFullscreen();
            } else {
                jcVideoPlayerStandard.setUp(currentStreamUrl, JCVideoPlayerStandard.SCREEN_LAYOUT_NORMAL, channel.getChannelName());
                jcVideoPlayerStandard.startVideo();
            }

            Log.d(TAG, "currentStreamUrl: " + currentStreamUrl);
        }

        channelName.setText(channel.getChannelNumber() + " " + channel.getChannelName());
        channelInfo.setText("Channel INFO");
    }

    private void rememberGenre() {

        sharedPref = application.getSharedPreferences(Application.SETTINGS_KEY, Context.MODE_PRIVATE);
        selectedGenrePosition = sharedPref.getInt("current_genre", 0);
        //selectedGenrePosition = sharedPref.getInt("current_genre", 0);

        categoriesRecyclerView.smoothScrollToPosition(selectedGenrePosition);
        categoriesAdapter.select(selectedGenrePosition, true);

        // Fix crash
        GenreItem genreItem = categoriesAdapter.getAdapterItem(selectedGenrePosition);
        if (genreItem != null)
            currentGenreId = genreItem.getCategoryId();

        resetChannelsList();

        //channelsRecyclerView.smoothScrollToPosition(selectedChannelPosition);
        //channelsRecyclerView.findViewHolderForAdapterPosition(selectedChannelPosition).itemView.setSelected(true);
    }
}
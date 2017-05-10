package net.henriqueof.stb.application;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;

import net.henriqueof.stb.items.ChannelItem;
import net.henriqueof.stb.services.StalkerService;
import net.henriqueof.stb.services.StbService;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import fm.jiecao.jcvideoplayer_lib.JCVideoPlayer;
import io.fabric.sdk.android.Fabric;

public class Application extends android.app.Application implements StbService.StateListener{
    private static final String TAG = "Application";
    public static final String SERVER_SETTINGS_KEY = "server_settings";
    public static final String SETTINGS_KEY = "settings";

    private int activeServer;
    private StbService stbService;

    private ChannelItem currentChannel;
    private List<ChannelItem> channelItemList;

    private Handler watchdogHandler;
    private Runnable watchdogRunnable;

    private StbService.StateListener stateListener;
    private StbService.State currentState;

    private ConnectivityManager connectivityManager;

    @Override
    public void onCreate() {
        super.onCreate();

        // Setup player options
        JCVideoPlayer.ACTION_BAR_EXIST = false;
        JCVideoPlayer.TOOL_BAR_EXIST = false;
        JCVideoPlayer.FULLSCREEN_ORIENTATION = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        JCVideoPlayer.NORMAL_ORIENTATION = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        JCVideoPlayer.SAVE_PROGRESS = true;
        JCVideoPlayer.WIFI_TIP_DIALOG_SHOWED = true;

        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        channelItemList = new ArrayList<>();

        Fabric.with(this, new Crashlytics());

        // Load settings
        SharedPreferences sharedPref = getSharedPreferences(SETTINGS_KEY, Context.MODE_PRIVATE);
        activeServer = sharedPref.getInt("active_server", -1);

        // Connect to last active server
        if (activeServer == -1) {
            Toast.makeText(getApplicationContext(), "No server configured, please add at least one server on the settings screen", Toast.LENGTH_SHORT).show();
        } else {
            connectServer(activeServer);
        }

    }

    @Override
    public void onStateChange(StbService.State newState) {
        currentState = newState;
        StalkerService.Profile profile = stbService.getProfile();

        if (currentState == StbService.State.LOADED)  {
            List<StalkerService.Channel> channelList = stbService.getChannelList();
            channelItemList.clear();

            if (channelList == null)
            {
                Log.e(TAG, "onStateChange: channelList == null");
                return;
            }

            for (StalkerService.Channel channel : channelList) {
                if (!channel.logo.isEmpty()) {
                    // If channel logo is relative adjust to the correct absolute path
                    if (!channel.logo.startsWith("http://") && !channel.logo.startsWith("https://"))
                        channel.logo = stbService.getServer() + "misc/logos/320/" + channel.logo;
                }

                channelItemList.add(new ChannelItem(channel));

                if (channel.id == profile.last_itv_id)
                    currentChannel = new ChannelItem(channel);
            }

            // Start watchdog service (keep alive)
            watchdogRunnable = new Runnable() {
                @Override
                public void run() {
                    // Run watchdog request
                    if (stbService != null)
                        stbService.watchdog();

                    // Enqueue next run
                    watchdogHandler.postDelayed(this, 30000);
                }
            };

            watchdogHandler.postDelayed(watchdogRunnable, 30000);
        }

        if (stateListener != null)
            stateListener.onStateChange(currentState);
    }

    public void connectServer(int server) {
        Log.d(TAG, "Connecting to server on slot " + server);

        // Check for internet connection
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        if (!isConnected) {
            Log.d(TAG, "No connection available");
        }

        if (server != -1)
            activeServer = server;

        SharedPreferences.Editor editor = getSharedPreferences(SETTINGS_KEY, Context.MODE_PRIVATE).edit();
        editor.putInt("active_server", activeServer);
        editor.apply();

        SharedPreferences sharedPref = getSharedPreferences(SERVER_SETTINGS_KEY, Context.MODE_PRIVATE);
        String serverAddress = sharedPref.getString("server_address_" + activeServer, "");
        String userName = sharedPref.getString("username_" + activeServer, "");
        String password = sharedPref.getString("password_" + activeServer, "");

        // Initialize TV service and get all channels
        StbService.Builder builder = new StbService.Builder(serverAddress, getMacAddr(), this);
        builder.timezone(TimeZone.getDefault().getID()).serial(Build.SERIAL);

        stbService = builder.build();
        stbService.connect();

        // Check if watchdog thread is running then start it
        if (watchdogHandler == null) {
            watchdogHandler = new Handler();
        } else {
            watchdogHandler.removeCallbacks(watchdogRunnable);
        }
    }

    public StbService getStbService() {
        return stbService;
    }

    public int getActiveServer() {
        return activeServer;
    }

    public void setSelectedChannel(ChannelItem channel) {
        if (channel == null)
            return;

        currentChannel = channel;

        // Send channel id to server
        stbService.itvSetLastId(channel.getChannelId());

        // Save it on settings
        SharedPreferences.Editor editor = getSharedPreferences(SERVER_SETTINGS_KEY, Context.MODE_PRIVATE).edit();
        editor.putInt("current_channel_" + activeServer, currentChannel.getChannelNumber());
        editor.apply();
    }

    public List<ChannelItem> getChannelItemList() {
        return channelItemList;
    }

    public ChannelItem getPreviousChannel() {
        int currentChannelIndex = channelItemList.indexOf(currentChannel);

        Log.d(TAG, "currentChannelIndex: " + currentChannelIndex);

        if (currentChannelIndex > 0)
            return channelItemList.get(currentChannelIndex - 1);

        return null;
    }

    public ChannelItem getCurrentChannel() {
        return currentChannel;
    }

    public ChannelItem getNextChannel() {
        int currentChannelIndex = channelItemList.indexOf(currentChannel);

        Log.d(TAG, "currentChannelIndex: " + currentChannelIndex);

        if (currentChannelIndex > -1 && currentChannelIndex < channelItemList.size())
            return channelItemList.get(currentChannelIndex + 1);

        return null;
    }

    public void setStateListener(StbService.StateListener stateListener) {
        this.stateListener = stateListener;
        this.stateListener.onStateChange(currentState);
    }

    // http://stackoverflow.com/questions/33159224/getting-mac-address-in-android-6-0
    public String getMacAddr() {
/*
        // Get cached MAC address
        SharedPreferences sharedPref = getSharedPreferences(SETTINGS_KEY, Context.MODE_PRIVATE);
        String mac = sharedPref.getString("mac_address", "");

        if (!mac.isEmpty())
            return mac;

        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(Integer.toHexString(b & 0xFF) + ":");
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }

                mac = "00:1A:79" + res1.toString().toUpperCase().substring(8);

                SharedPreferences.Editor editor = getSharedPreferences(SETTINGS_KEY, Context.MODE_PRIVATE).edit();
                editor.putString("mac_address", mac);
                editor.apply();

                return mac;
                //return res1.toString();
            }
        } catch (Exception ex) {
            //handle exception
        }*/
        return "00:1A:79:CA:91:27";
    }
}

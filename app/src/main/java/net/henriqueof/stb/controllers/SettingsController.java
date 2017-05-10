package net.henriqueof.stb.controllers;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bluelinelabs.conductor.RouterTransaction;
import com.bluelinelabs.conductor.changehandler.FadeChangeHandler;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.IAdapter;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;

import net.henriqueof.stb.MainActivity;
import net.henriqueof.stb.R;
import net.henriqueof.stb.application.Application;
import net.henriqueof.stb.controllers.base.BaseController;
import net.henriqueof.stb.items.ChannelItem;
import net.henriqueof.stb.items.ServerItem;
import net.henriqueof.stb.services.StbService;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;


public class SettingsController extends BaseController implements StbService.StateListener {
    private static final String TAG = "SettingsController";

    // UI references.
    @BindView(R.id.servers_rv)
    RecyclerView serverRecyclerView;
    @BindView(R.id.channels_rv)
    RecyclerView channelsRecyclerView;
    @BindView(R.id.refresh_button)
    LinearLayout refreshButton;
    @BindView(R.id.settings_info)
    TextView settingsInfo;
    @BindView(R.id.progress_layout)
    LinearLayout progressLayout;
    @BindView(R.id.refresh_icon)
    ImageView refreshIcon;

    private FastItemAdapter<ServerItem> serversAdapter;
    private FastItemAdapter<ChannelItem> channelsAdapter;
    private SharedPreferences sharedPref;

    // Reference to application instance
    private Application application;
    private MainActivity activity;

    private Animation rotateAnimation;

    private int activeServer;

    @Override
    protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
        return inflater.inflate(R.layout.controller_settings, container, false);
    }

    @Override
    protected void onViewBound(@NonNull View view) {
        super.onViewBound(view);

        rotateAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.android_rotate_animation);

        activity = (MainActivity) getActivity();
        assert activity != null;
        application = (Application) activity.getApplication();

        // set MAC address on text view
        settingsInfo.setText(String.format("MAC Address: %s", application.getMacAddr()));

        // retrieve current active server
        sharedPref = application.getSharedPreferences(Application.SETTINGS_KEY, Context.MODE_PRIVATE);
        activeServer = sharedPref.getInt("active_server", -1);

        // ...
        sharedPref = application.getSharedPreferences(Application.SERVER_SETTINGS_KEY, Context.MODE_PRIVATE);

        // Load server settings
        int numSlots = sharedPref.getInt("num_slots", 10);
        List<ServerItem> serverItemList = new ArrayList<>();

        for (int i = 0; i < numSlots; i++) {

            String name = sharedPref.getString("server_name_" + i, "Server " + (i + 1));
            String address = sharedPref.getString("server_address_" + i, "");
            int slot = sharedPref.getInt("server_slot_" + i, i);

            serverItemList.add(new ServerItem(name, address, slot, false));
        }

        serversAdapter = new FastItemAdapter<>();
        serversAdapter.set(serverItemList);
        serversAdapter.withOnClickListener(new FastAdapter.OnClickListener<ServerItem>() {
            @Override
            public boolean onClick(View v, IAdapter<ServerItem> adapter, ServerItem item, int position) {

                Dialog settingsDialog = buildSettingsDialog(item, position);
                settingsDialog.show();

                return true;
            }
        });

        serverRecyclerView.setHasFixedSize(true);
        serverRecyclerView.setLayoutManager(new LinearLayoutManager(activity));
        serverRecyclerView.setAdapter(serversAdapter);

        // Setup channels list adapter
        channelsAdapter = new FastItemAdapter<>();
        channelsRecyclerView.setHasFixedSize(true);
        channelsRecyclerView.setLayoutManager(new LinearLayoutManager(activity));
        channelsRecyclerView.setAdapter(channelsAdapter);

        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO:
                application.connectServer(-1);
                refreshIcon.startAnimation(rotateAnimation);
            }
        });

        application.setStateListener(SettingsController.this);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_PROG_RED) {
            // Connect server...
            View focusedChild = serverRecyclerView.getFocusedChild();
            if (focusedChild != null) {
                int position = serverRecyclerView.getChildAdapterPosition(focusedChild);
                application.connectServer(position);
            } else {
                Toast.makeText(getApplicationContext(), "Please select a server to connect.", Toast.LENGTH_LONG).show();
            }
        } else if (keyCode == KeyEvent.KEYCODE_PROG_GREEN) {
            // Add/Del Fav
        } else if (keyCode == KeyEvent.KEYCODE_PROG_YELLOW) {
            // Lock/Unlock
        } else if (keyCode == KeyEvent.KEYCODE_PROG_BLUE) {
            getRouter().pushController(RouterTransaction.with(new VodController())
                    .pushChangeHandler(new FadeChangeHandler())
                    .popChangeHandler(new FadeChangeHandler()));
            return true;
        }

        return super.onKeyUp(keyCode, event);
    }

    @Override
    public void onStateChange(StbService.State newState) {
        View progressBar = null;
        View activeView = null;

        RecyclerView.ViewHolder viewHolder = serverRecyclerView.findViewHolderForAdapterPosition(activeServer);


        if (viewHolder == null) {
            Log.e(TAG, "onStateChange: viewHolder == null");
        } else {
            View serverView = viewHolder.itemView;
            progressBar = serverView.findViewById(R.id.progress_bar);
            activeView = serverView.findViewById(R.id.icon_active);
        }

        if (newState == StbService.State.CONNECTING) {
            channelsAdapter.clear();

            if (progressBar != null)
                progressBar.setVisibility(View.VISIBLE);

            if (activeView != null)
                activeView.setVisibility(View.VISIBLE);

            progressLayout.setVisibility(View.VISIBLE);
        } else if (newState == StbService.State.CONNECTED) {
            if (progressBar != null)
                progressBar.setVisibility(View.VISIBLE);

            progressLayout.setVisibility(View.VISIBLE);
        } else if (newState == StbService.State.LOADED) {
            channelsAdapter.set(application.getChannelItemList());
            Toast.makeText(activity, "Loaded " + channelsAdapter.getItemCount() + " channels", Toast.LENGTH_LONG).show();

            if (progressBar != null)
                progressBar.setVisibility(View.INVISIBLE);

            progressLayout.setVisibility(View.INVISIBLE);
            refreshIcon.setAnimation(null);
        } else if (newState == StbService.State.ERROR) {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle("Error")
                    .setMessage("Error connecting to the server")
                    .setPositiveButton("OK", null)
                    .show();

            if (progressBar != null)
                progressBar.setVisibility(View.INVISIBLE);

            progressLayout.setVisibility(View.INVISIBLE);
            refreshIcon.setAnimation(null);
        }
    }

    private Dialog buildSettingsDialog(ServerItem serverItem, final int position) {

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        // Get the layout inflater
        LayoutInflater inflater = activity.getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        View view = inflater.inflate(R.layout.dialog_server_settings, null);

        TextView serverName = (TextView) view.findViewById(R.id.server_name);
        TextView serverAddress = (TextView) view.findViewById(R.id.server_address);
        CheckBox credentialsCheck = (CheckBox) view.findViewById(R.id.credentials_check);
        TextView userName = (TextView) view.findViewById(R.id.username);
        TextView password = (TextView) view.findViewById(R.id.password);

        final TextInputLayout userInputLayout = (TextInputLayout) view.findViewById(R.id.user_text_layout);
        final TextInputLayout passInputLayout = (TextInputLayout) view.findViewById(R.id.pass_text_layout);

        credentialsCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                if (b) {
                    userInputLayout.setVisibility(View.VISIBLE);
                    passInputLayout.setVisibility(View.VISIBLE);
                } else {
                    userInputLayout.setVisibility(View.GONE);
                    passInputLayout.setVisibility(View.GONE);
                }
            }
        });

        final int slot = serverItem.getSlot();
        activeServer = slot;

        Boolean useCredentials = sharedPref.getBoolean("use_credentials_" + slot, false);

        serverName.setText(serverItem.getServerName());
        //serverAddress.setText(serverItem.getServerAddress());
        if (!serverItem.getServerAddress().isEmpty())
            serverAddress.setText("*****************");

        credentialsCheck.setChecked(useCredentials);
        userName.setText(sharedPref.getString("username_" + slot, ""));
        password.setText(sharedPref.getString("password_" + slot, ""));

        builder.setView(view)
                // Add action buttons
                .setPositiveButton("Connect", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                        TextView serverNameView = (TextView) ((AlertDialog) dialog).findViewById(R.id.server_name);
                        TextView serverAddressView = (TextView) ((AlertDialog) dialog).findViewById(R.id.server_address);
                        CheckBox credentialsCheck = (CheckBox) ((AlertDialog) dialog).findViewById(R.id.credentials_check);

                        TextView userName = (TextView) ((AlertDialog) dialog).findViewById(R.id.username);
                        TextView password = (TextView) ((AlertDialog) dialog).findViewById(R.id.password);

                        String serverName = serverNameView.getText().toString();
                        String serverAddress = serverAddressView.getText().toString();
                        Boolean useCredentials = credentialsCheck.isChecked();

                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString("server_name_" + slot, serverName);

                        if (!serverAddress.equals("*****************"))
                            editor.putString("server_address_" + slot, serverAddress);

                        editor.putBoolean("use_credentials_" + slot, useCredentials);
                        editor.putString("username_" + slot, userName.getText().toString());
                        editor.putString("password_" + slot, password.getText().toString());
                        editor.apply();

                        for (int i = 0; i < serversAdapter.getAdapterItemCount(); i++) {
                            ServerItem item= serversAdapter.getAdapterItem(i);
                            item.setUnderProgress(false);
                            serversAdapter.set(i, item);
                            serversAdapter.notifyAdapterItemChanged(i);
                        }

                        serversAdapter.set(position, new ServerItem(serverName, serverAddress, slot, true));
                        serversAdapter.notifyAdapterItemChanged(position);

                        // Debug saved changes
                        Log.d(TAG, "Saving changes to server on slot " + slot);
                        Log.d(TAG, "Server URL " + serverAddress);
                        Log.d(TAG, "Use credentials " + useCredentials);

                        if (serverAddress.length() > 0) {
                            application.connectServer(slot);

                            //stbService = application.getStbService();
                            application.setStateListener(SettingsController.this);
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });

        return builder.create();
    }
}

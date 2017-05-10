package net.henriqueof.stb.controllers;

import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.bluelinelabs.conductor.RouterTransaction;
import com.bluelinelabs.conductor.changehandler.FadeChangeHandler;

import net.henriqueof.stb.MainActivity;
import net.henriqueof.stb.R;
import net.henriqueof.stb.application.Application;
import net.henriqueof.stb.controllers.base.BaseController;

import butterknife.BindView;

public class MenuController extends BaseController {

    // UI references.
    @BindView(R.id.menu_info) TextView menuInfo;
    @BindView(R.id.live_tv_button) FrameLayout liveTv;
    @BindView(R.id.vod_button) FrameLayout vod;
    @BindView(R.id.settings_button) FrameLayout setting;

    @Override
    protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
        return inflater.inflate(R.layout.controller_menu, container, false);
    }

    @Override
    protected void onViewBound(@NonNull View view) {
        super.onViewBound(view);

        final MainActivity activity = (MainActivity) getActivity();
        final Application application = (Application) activity.getApplication();

        menuInfo.setText(String.format("MAC Address = %s", application.getMacAddr()));

        vod.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (application.getActiveServer() == -1) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    builder.setTitle("Alert")
                            .setMessage("No server configured, please add at least one server on the settings screen")
                            .setPositiveButton("OK", null)
                            .setNeutralButton("Settings", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    getRouter().pushController(RouterTransaction.with(new SettingsController())
                                            .pushChangeHandler(new FadeChangeHandler())
                                            .popChangeHandler(new FadeChangeHandler()));
                                }
                            })
                            .show();
                } else {
                    getRouter().pushController(RouterTransaction.with(new VodController())
                            .pushChangeHandler(new FadeChangeHandler())
                            .popChangeHandler(new FadeChangeHandler()));
                }

            }
        });

        liveTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (application.getActiveServer() == -1) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    builder.setTitle("Alert")
                            .setMessage("No server configured, please add at least one server on the settings screen")
                            .setPositiveButton("OK", null)
                            .setNeutralButton("Settings", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    getRouter().pushController(RouterTransaction.with(new SettingsController())
                                            .pushChangeHandler(new FadeChangeHandler())
                                            .popChangeHandler(new FadeChangeHandler()));
                                }
                            })
                            .show();
                } else {
                    getRouter().pushController(RouterTransaction.with(new LiveTvController())
                            .pushChangeHandler(new FadeChangeHandler())
                            .popChangeHandler(new FadeChangeHandler()));
                }
            }
        });

        setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                getRouter().pushController(RouterTransaction.with(new SettingsController1())
                        .pushChangeHandler(new FadeChangeHandler())
                        .popChangeHandler(new FadeChangeHandler()));
            }
        });

        liveTv.requestFocus();
    }
}

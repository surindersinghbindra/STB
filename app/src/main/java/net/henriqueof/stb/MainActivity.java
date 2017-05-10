package net.henriqueof.stb;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;

import com.bluelinelabs.conductor.Conductor;
import com.bluelinelabs.conductor.Controller;
import com.bluelinelabs.conductor.Router;
import com.bluelinelabs.conductor.RouterTransaction;

import net.henriqueof.stb.controllers.LiveTvController;
import net.henriqueof.stb.controllers.MenuController;
import net.henriqueof.stb.controllers.SettingsController;
import net.henriqueof.stb.controllers.SettingsController1;
import net.henriqueof.stb.controllers.VodController;
import net.henriqueof.stb.controllers.base.BaseController;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MainActivity extends AppCompatActivity {

    // UI references.
    @BindView(R.id.controller_container) ViewGroup container;

    private Router router;
    private Unbinder unbinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        unbinder = ButterKnife.bind(this);

        router = Conductor.attachRouter(this, container, savedInstanceState);

        // TODO: direct the user to the right controller
        MenuController menuController = new MenuController();
        menuController.setRetainViewMode(Controller.RetainViewMode.RETAIN_DETACH);

        Bundle extras = getIntent().getExtras();

        if (!router.hasRootController()) {

            if (extras == null)
                router.setRoot(RouterTransaction.with(menuController));
            else {
                Object start = extras.get("start");

                if (start == null)
                    router.setRoot(RouterTransaction.with(menuController));
                else if (start.equals("livetv"))
                    router.setRoot(RouterTransaction.with(new LiveTvController()));
                else if (start.equals("vod"))
                    router.setRoot(RouterTransaction.with(new VodController()));
                else if (start.equals("settings"))
                    router.setRoot(RouterTransaction.with(new SettingsController1()));
                else
                    router.setRoot(RouterTransaction.with(menuController));
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    @Override
    public void onBackPressed() {

        if (!router.handleBack()) {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        List<RouterTransaction> list = router.getBackstack();

        if (list.isEmpty())
            return super.onKeyDown(keyCode, event);

        BaseController controller = (BaseController) (list.get(list.size() - 1).controller());

        if (controller.onKeyDown(keyCode, event))
            return true;
        else
            return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        List<RouterTransaction> list = router.getBackstack();

        if (list.isEmpty())
            return super.onKeyUp(keyCode, event);

        BaseController controller = (BaseController) (list.get(list.size() - 1).controller());

        if (controller.onKeyUp(keyCode, event))
            return true;
        else
            return super.onKeyUp(keyCode, event);
    }
}

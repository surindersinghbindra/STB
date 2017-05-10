package net.henriqueof.stb.controllers.base;

import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.View;

import com.bluelinelabs.conductor.Controller;

public abstract class BaseController extends ButterKnifeController {

    protected BaseController() { }

    @Override
    protected void onAttach(@NonNull View view) {
        super.onAttach(view);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) { return false; }

    public boolean onKeyUp(int keyCode, KeyEvent event) { return false; }
}

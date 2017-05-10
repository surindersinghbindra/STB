package net.henriqueof.stb;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by Carlos Henrique on 31/01/2017.
 */

public class CustomLayoutManager extends LinearLayoutManager {
    public CustomLayoutManager(Context context) {
        super(context);
    }

    private FocusHandler focusHandler;
/*
    @Override
    public View onInterceptFocusSearch(View focused, int direction) {
        int count = getItemCount();

        if (direction == View.FOCUS_DOWN) {
            View view = getChildAt(count - 1);

            if (view == focused) {
                return focused;
            }

        } else if (direction == View.FOCUS_UP) {
            View view = getChildAt(0);

            if (view == focused) {
                return focused;
            }
        }

        return super .onInterceptFocusSearch(focused, direction);
    }
*/

    @Override
    public boolean onRequestChildFocus(RecyclerView parent, RecyclerView.State state, View child, View focused) {
        return super.onRequestChildFocus(parent, state, child, focused);
    }
/*
    @Override
    public boolean onRequestChildFocus(RecyclerView parent, RecyclerView.State state, View child, View focused) {

        if (focusHandler != null)
            focusHandler.onFocusPosition(getPosition(focused));

        return super.onRequestChildFocus(parent, state, child, focused);
    }
*/

    public void setFocusHandler(FocusHandler focusHandler) {
        this.focusHandler = focusHandler;
    }

    public interface FocusHandler {
        void onFocusPosition(int position);
    }
}

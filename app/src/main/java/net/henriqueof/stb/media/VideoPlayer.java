package net.henriqueof.stb.media;

import android.content.Context;
import android.util.AttributeSet;

import fm.jiecao.jcvideoplayer_lib.JCVideoPlayerStandard;

/**
 * Created by henri on 09/04/2017.
 */

public class VideoPlayer extends JCVideoPlayerStandard {
    public VideoPlayer(Context context) {
        super(context);
    }

    public VideoPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void init(Context context) {
        super.init(context);
    }

    @Override
    public void setUp(String url, int screen, Object... objects) {
        super.setUp(url, screen, objects);
    }

    @Override
    public void onVideoSizeChanged() {
        //super.onVideoSizeChanged();
    }
}

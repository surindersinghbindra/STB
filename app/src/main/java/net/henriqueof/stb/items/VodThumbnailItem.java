package net.henriqueof.stb.items;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mikepenz.fastadapter.items.AbstractItem;
import com.squareup.picasso.Picasso;

import net.henriqueof.stb.R;
import net.henriqueof.stb.services.StalkerService;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Carlos Henrique on 05/01/2017.
 */

public class VodThumbnailItem extends AbstractItem<VodThumbnailItem, VodThumbnailItem.ViewHolder> {

    private StalkerService.Vod vod;
    private String server;

    public VodThumbnailItem(StalkerService.Vod vod, String server) {
        this.vod = vod;
        this.server = server;
    }

    @Override
    public int getType() {
        return 0;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.item_vod_thumbnail;
    }

    @Override
    public void bindView(ViewHolder holder, List<Object> payloads) {
        super.bindView(holder, payloads);

        String pictureUri;

        if (vod.screenshot_uri.startsWith("http://") || vod.screenshot_uri.startsWith("https://"))
            pictureUri = vod.screenshot_uri;
        else
            pictureUri = server.replace("/stalker_portal/c/", vod.screenshot_uri);

        Picasso.with(holder.vodPicture.getContext()).load(pictureUri).fit().into(holder.vodPicture);

        holder.vodName.setText(vod.name);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.vod_picture) ImageView vodPicture;
        @BindView(R.id.vod_name) TextView vodName;

        public ViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }
    }
}

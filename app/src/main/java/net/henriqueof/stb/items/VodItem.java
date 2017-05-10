package net.henriqueof.stb.items;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mikepenz.fastadapter.items.AbstractItem;
import com.mikepenz.fastadapter.utils.ViewHolderFactory;
import com.squareup.picasso.Picasso;

import net.henriqueof.stb.R;
import net.henriqueof.stb.controllers.VodController;
import net.henriqueof.stb.services.StalkerService;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Carlos Henrique on 26/12/2016.
 */

public class VodItem extends AbstractItem<VodItem, VodItem.ViewHolder> {

    // the static ViewHolderFactory which will be used to generate the ViewHolder for this Item
    private static final ViewHolderFactory<? extends ViewHolder> FACTORY = new ItemFactory();

    private StalkerService.Vod vod;
    private VodController controller;

    public VodItem(StalkerService.Vod vod, VodController controller) {
        this.vod = vod;
        this.controller = controller;
    }

    @Override
    public int getType() {
        return 0;
    }

    @Override
    public int getLayoutRes() {
        if (controller.getListType() == VodController.ListType.LIST)
            return R.layout.item_vod;
        else
            return R.layout.item_vod_thumbnail;
    }

    @Override
    public void bindView(ViewHolder holder, List payloads) {
        super.bindView(holder, payloads);

        holder.vodName.setText(vod.name);

        if (controller.getListType() == VodController.ListType.LIST) {
            holder.vodPicture.setVisibility(View.GONE);
            holder.vodTime.setText(vod.added);
        } else {
            holder.vodTime.setVisibility(View.GONE);
            String pictureUri;

            if (vod.screenshot_uri.startsWith("http://") || vod.screenshot_uri.startsWith("https://"))
                pictureUri = vod.screenshot_uri;
            else
                pictureUri = controller.getServer().replace("/stalker_portal/c/", vod.screenshot_uri);

            Picasso.with(holder.vodPicture.getContext()).load(pictureUri).fit().into(holder.vodPicture);
        }

    }

    public String getPicture() {
        return vod.screenshot_uri;
    }

    public String getName() {
        return vod.name;
    }

    public String getDescription() {
        return vod.description;
    }

    public String getCmd() {
        return vod.cmd;
    }

    /**
     * our ItemFactory implementation which creates the ViewHolder for our adapter.
     * It is highly recommended to implement a ViewHolderFactory as it is 0-1ms faster for ViewHolder creation,
     * and it is also many many times more efficient if you define custom listeners on views within your item.
     */
    protected static class ItemFactory implements ViewHolderFactory<ViewHolder> {
        public ViewHolder create(View v) {
            return new ViewHolder(v);
        }
    }

    /**
     * return our ViewHolderFactory implementation here
     *
     * @return
     */
    @Override
    public ViewHolderFactory<? extends ViewHolder> getFactory() {
        return FACTORY;
    }

    protected static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.vod_picture) ImageView vodPicture;
        @BindView(R.id.vod_name) TextView vodName;
        @BindView(R.id.vod_time) TextView vodTime;

        public ViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }
    }
}

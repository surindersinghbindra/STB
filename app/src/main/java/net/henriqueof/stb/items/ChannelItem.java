package net.henriqueof.stb.items;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mikepenz.fastadapter.items.AbstractItem;
import com.mikepenz.fastadapter.utils.ViewHolderFactory;
import com.squareup.picasso.Picasso;

import net.henriqueof.stb.R;
import net.henriqueof.stb.services.StalkerService;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ChannelItem extends AbstractItem<ChannelItem, ChannelItem.ViewHolder> {

    // the static ViewHolderFactory which will be used to generate the ViewHolder for this Item
    private static final ViewHolderFactory<? extends ChannelItem.ViewHolder> FACTORY = new ChannelItem.ItemFactory();

    private StalkerService.Channel channel;

    public ChannelItem(StalkerService.Channel channel) {
        this.channel = channel;
    }

    @Override
    public int getType() {
        return 0;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.item_channel;
    }

    @Override
    public void bindView(ViewHolder holder, List payloads) {
        super.bindView(holder, payloads);

        if (!channel.logo.isEmpty()) {
            Picasso.with(holder.channelIcon.getContext()).load(channel.logo).into(holder.channelIcon);
        }

        String formatted = String.format("%04d", channel.number) + " " + channel.name;
        holder.channelName.setText(formatted);

        if (channel.fav == 1)
            holder.favoriteIcon.setVisibility(View.VISIBLE);
        else
            holder.favoriteIcon.setVisibility(View.INVISIBLE);
    }

    @Override
    public boolean equals(Object o) {
        return o != null && o instanceof ChannelItem && channel.id == ((ChannelItem) o).getChannelId();
    }

    public int getChannelId() {
        return channel.id;
    }

    public int getChannelNumber() {
        return channel.number;
    }

    public String getChannelName() {
        return channel.name;
    }

    public int getFavorite() {
        return channel.fav;
    }

    public void setFavorite(int fav) {
        channel.fav = fav;
    }

    public int useHttpTmpLink() {
        return channel.use_http_tmp_link;
    }

    public String getCmd() {
        return channel.cmd;
    }

    /**
     * our ItemFactory implementation which creates the ViewHolder for our adapter.
     * It is highly recommended to implement a ViewHolderFactory as it is 0-1ms faster for ViewHolder creation,
     * and it is also many many times more efficient if you define custom listeners on views within your item.
     */
    protected static class ItemFactory implements ViewHolderFactory<ChannelItem.ViewHolder> {
        public ChannelItem.ViewHolder create(View v) {
            return new ChannelItem.ViewHolder(v);
        }
    }

    /**
     * return our ViewHolderFactory implementation here
     *
     * @return
     */
    @Override
    public ViewHolderFactory<? extends ChannelItem.ViewHolder> getFactory() {
        return FACTORY;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.channel_icon) ImageView channelIcon;
        @BindView(R.id.channel_name) TextView channelName;
        @BindView(R.id.favorite_icon) ImageView favoriteIcon;

        public ViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }
    }
}
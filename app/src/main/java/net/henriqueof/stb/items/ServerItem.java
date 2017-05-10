package net.henriqueof.stb.items;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mikepenz.fastadapter.items.AbstractItem;
import com.mikepenz.fastadapter.utils.ViewHolderFactory;

import net.henriqueof.stb.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class ServerItem extends AbstractItem<ServerItem, ServerItem.ViewHolder> {

    // the static ViewHolderFactory which will be used to generate the ViewHolder for this Item
    private static final ViewHolderFactory<? extends ServerItem.ViewHolder> FACTORY = new ServerItem.ItemFactory();

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public void setSlot(int slot) {
        this.slot = slot;
    }

    public boolean isActive() {
        return active;
    }

    private String serverName;
    private String serverAddress;
    private int slot;
    private boolean active;

    private boolean isUnderProgress;

    public ServerItem(String serverName, String serverAddress, int slot, boolean isUnderProgress) {
        this.serverName = serverName;
        this.serverAddress = serverAddress;
        this.slot = slot;
        this.isUnderProgress = isUnderProgress;
    }

    public ServerItem() {

    }

    @Override
    public int getType() {
        return 0;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.item_server;
    }

    public String getServerName() {
        return serverName;
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public int getSlot() {
        return slot;
    }

    public boolean getActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isUnderProgress() {
        return isUnderProgress;
    }

    public void setUnderProgress(boolean underProgress) {
        isUnderProgress = underProgress;
    }

    @Override
    public void bindView(ViewHolder holder, List payloads) {
        super.bindView(holder, payloads);

        holder.serverName.setText(serverName);

        if (serverAddress.isEmpty())
            holder.serverAddress.setText("URL:");
        else
            holder.serverAddress.setText("URL: **************");

        if (active)
            holder.iconActive.setVisibility(View.VISIBLE);
        else
            holder.iconActive.setVisibility(View.INVISIBLE);

        if (isUnderProgress) {
            holder.progressBar.setVisibility(View.VISIBLE);
        } else {
            holder.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * our ItemFactory implementation which creates the ViewHolder for our adapter.
     * It is highly recommended to implement a ViewHolderFactory as it is 0-1ms faster for ViewHolder creation,
     * and it is also many many times more efficient if you define custom listeners on views within your item.
     */
    protected static class ItemFactory implements ViewHolderFactory<ServerItem.ViewHolder> {
        public ServerItem.ViewHolder create(View v) {
            return new ServerItem.ViewHolder(v);
        }
    }

    /**
     * return our ViewHolderFactory implementation here
     *
     * @return
     */
    @Override
    public ViewHolderFactory<? extends ServerItem.ViewHolder> getFactory() {
        return FACTORY;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.server_name)
        TextView serverName;
        @BindView(R.id.server_address)
        TextView serverAddress;
        @BindView(R.id.icon_active)
        ImageView iconActive;
        @BindView(R.id.progress_bar)
        ProgressBar progressBar;

        public ViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }
    }
}

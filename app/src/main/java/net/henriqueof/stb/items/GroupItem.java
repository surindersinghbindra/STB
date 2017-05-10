package net.henriqueof.stb.items;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mikepenz.fastadapter.items.AbstractItem;
import com.mikepenz.fastadapter.utils.ViewHolderFactory;

import net.henriqueof.stb.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class GroupItem extends AbstractItem<GroupItem, GroupItem.ViewHolder> {

    // the static ViewHolderFactory which will be used to generate the ViewHolder for this Item
    private static final ViewHolderFactory<? extends GroupItem.ViewHolder> FACTORY = new GroupItem.ItemFactory();

    private int iconId;
    private String description;

    public GroupItem(int iconId, String description) {

        this.iconId = iconId;
        this.description = description;
    }

    @Override
    public int getType() {
        return 0;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.item_group;
    }

    @Override
    public void bindView(ViewHolder holder, List payloads) {
        super.bindView(holder, payloads);

        holder.groupIcon.setImageResource(iconId);
        holder.groupDescription.setText(description);
    }

    /**
     * our ItemFactory implementation which creates the ViewHolder for our adapter.
     * It is highly recommended to implement a ViewHolderFactory as it is 0-1ms faster for ViewHolder creation,
     * and it is also many many times more efficient if you define custom listeners on views within your item.
     */
    protected static class ItemFactory implements ViewHolderFactory<GroupItem.ViewHolder> {
        public GroupItem.ViewHolder create(View v) {
            return new GroupItem.ViewHolder(v);
        }
    }

    /**
     * return our ViewHolderFactory implementation here
     *
     * @return
     */
    @Override
    public ViewHolderFactory<? extends GroupItem.ViewHolder> getFactory() {
        return FACTORY;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.start_time) ImageView groupIcon;
        @BindView(R.id.event_title) TextView groupDescription;

        public ViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }
    }
}

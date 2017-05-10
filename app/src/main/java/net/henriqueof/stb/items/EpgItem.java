package net.henriqueof.stb.items;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.mikepenz.fastadapter.items.AbstractItem;
import com.mikepenz.fastadapter.utils.ViewHolderFactory;

import net.henriqueof.stb.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EpgItem extends AbstractItem<EpgItem, EpgItem.ViewHolder> {

    // the static ViewHolderFactory which will be used to generate the ViewHolder for this Item
    private static final ViewHolderFactory<? extends EpgItem.ViewHolder> FACTORY = new EpgItem.ItemFactory();

    private String startTime;
    private String eventTitle;
    private String plot;

    public EpgItem(String startTime, String eventTitle, String plot) {
        this.startTime = startTime;
        this.eventTitle = eventTitle;
        this.plot = plot;
    }

    @Override
    public int getType() {
        return 0;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.item_epg;
    }

    @Override
    public void bindView(ViewHolder holder, List payloads) {
        super.bindView(holder, payloads);

        holder.startTime.setText(startTime);
        holder.eventTitle.setText(eventTitle);
    }

    public String getEventTitle() {
        return eventTitle;
    }

    public String getPlot() {
        return plot;
    }

    /**
     * our ItemFactory implementation which creates the ViewHolder for our adapter.
     * It is highly recommended to implement a ViewHolderFactory as it is 0-1ms faster for ViewHolder creation,
     * and it is also many many times more efficient if you define custom listeners on views within your item.
     */
    protected static class ItemFactory implements ViewHolderFactory<EpgItem.ViewHolder> {
        public EpgItem.ViewHolder create(View v) {
            return new EpgItem.ViewHolder(v);
        }
    }

    /**
     * return our ViewHolderFactory implementation here
     *
     * @return
     */
    @Override
    public ViewHolderFactory<? extends EpgItem.ViewHolder> getFactory() {
        return FACTORY;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.start_time) TextView startTime;
        @BindView(R.id.event_title) TextView eventTitle;

        public ViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }
    }
}

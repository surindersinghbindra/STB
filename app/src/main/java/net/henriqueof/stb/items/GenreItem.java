package net.henriqueof.stb.items;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.mikepenz.fastadapter.items.AbstractItem;
import com.mikepenz.fastadapter.utils.ViewHolderFactory;

import net.henriqueof.stb.R;
import net.henriqueof.stb.services.StalkerService;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class GenreItem extends AbstractItem<GenreItem, GenreItem.ViewHolder> {

    // the static ViewHolderFactory which will be used to generate the ViewHolder for this Item
    private static final ViewHolderFactory<? extends GenreItem.ViewHolder> FACTORY = new GenreItem.ItemFactory();

    private StalkerService.Genre genre;

    public GenreItem(StalkerService.Genre genre) {
        this.genre = genre;
    }

    @Override
    public int getType() {
        return 0;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.item_category;
    }

    @Override
    public void bindView(ViewHolder holder, List payloads) {
        super.bindView(holder, payloads);

        holder.categoryName.setText(genre.title);
    }

    public String getCategoryId() {
        return genre.id;
    }

    public int getCensored() {
        return genre.censored;
    }

    /**
     * our ItemFactory implementation which creates the ViewHolder for our adapter.
     * It is highly recommended to implement a ViewHolderFactory as it is 0-1ms faster for ViewHolder creation,
     * and it is also many many times more efficient if you define custom listeners on views within your item.
     */
    protected static class ItemFactory implements ViewHolderFactory<GenreItem.ViewHolder> {
        public GenreItem.ViewHolder create(View v) {
            return new GenreItem.ViewHolder(v);
        }
    }

    /**
     * return our ViewHolderFactory implementation here
     *
     * @return
     */
    @Override
    public ViewHolderFactory<? extends GenreItem.ViewHolder> getFactory() {
        return FACTORY;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.category_name) TextView categoryName;

        public ViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }
    }
}

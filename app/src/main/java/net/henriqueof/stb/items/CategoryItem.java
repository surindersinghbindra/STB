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

/**
 * Created by Carlos Henrique on 26/12/2016.
 */

public class CategoryItem extends AbstractItem<CategoryItem, CategoryItem.ViewHolder> {

    // the static ViewHolderFactory which will be used to generate the ViewHolder for this Item
    private static final ViewHolderFactory<? extends CategoryItem.ViewHolder> FACTORY = new CategoryItem.ItemFactory();

    private StalkerService.Category category;

    public CategoryItem(StalkerService.Category category) {
        this.category = category;
    }

    @Override
    public int getType() {
        return 0;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.item_list;
    }

    @Override
    public void bindView(ViewHolder holder, List payloads) {
        super.bindView(holder, payloads);

        holder.textView.setText(category.title);
    }

    public String getCategoryId() {
        return category.id;
    }

    /**
     * our ItemFactory implementation which creates the ViewHolder for our adapter.
     * It is highly recommended to implement a ViewHolderFactory as it is 0-1ms faster for ViewHolder creation,
     * and it is also many many times more efficient if you define custom listeners on views within your item.
     */
    protected static class ItemFactory implements ViewHolderFactory<CategoryItem.ViewHolder> {
        public CategoryItem.ViewHolder create(View v) {
            return new CategoryItem.ViewHolder(v);
        }
    }

    /**
     * return our ViewHolderFactory implementation here
     *
     * @return
     */
    @Override
    public ViewHolderFactory<? extends CategoryItem.ViewHolder> getFactory() {
        return FACTORY;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(android.R.id.text1) TextView textView;

        public ViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }
    }
}

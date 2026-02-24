package com.example.leveluplife.ui.shop;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.leveluplife.R;
import com.example.leveluplife.data.entity.ShopItem;

public class ShopAdapter extends ListAdapter<ShopItem, ShopAdapter.ViewHolder> {

    public interface OnBuyClickListener {
        void onBuyClick(ShopItem item);
    }

    private final OnBuyClickListener listener;

    public ShopAdapter(OnBuyClickListener listener) {
        super(new DiffCallback());
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_shop, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ShopItem item = getItem(position);
        holder.icon.setText(item.getIcon());
        holder.name.setText(item.getName());
        holder.description.setText(item.getDescription());
        holder.price.setText("💰 " + item.getPrice());
        holder.btnBuy.setOnClickListener(v -> listener.onBuyClick(item));
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView icon, name, description, price;
        Button btnBuy;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            icon        = itemView.findViewById(R.id.shop_icon);
            name        = itemView.findViewById(R.id.shop_name);
            description = itemView.findViewById(R.id.shop_description);
            price       = itemView.findViewById(R.id.shop_price);
            btnBuy      = itemView.findViewById(R.id.btn_buy);
        }
    }

    static class DiffCallback extends DiffUtil.ItemCallback<ShopItem> {
        @Override
        public boolean areItemsTheSame(@NonNull ShopItem o, @NonNull ShopItem n) {
            return o.getId() == n.getId();
        }

        @SuppressLint("DiffUtilEquals")
        @Override
        public boolean areContentsTheSame(@NonNull ShopItem o, @NonNull ShopItem n) {
            return o.getPrice() == n.getPrice() && o.isAvailable() == n.isAvailable();
        }
    }
}

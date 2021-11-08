package com.tomorrow_eyes.buddhacount;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

//import com.tomorrow_eyes.buddhacount.placeholder.PlaceholderContent.PlaceholderItem;
import com.tomorrow_eyes.buddhacount.ItemContent.CountItem;
import com.tomorrow_eyes.buddhacount.databinding.FragmentItemBinding;

import java.time.format.DateTimeFormatter;
import java.util.List;


public class MyItemRecyclerViewAdapter extends RecyclerView.Adapter<MyItemRecyclerViewAdapter.ViewHolder> {

    private final List<CountItem> mValues;

    public MyItemRecyclerViewAdapter(List<CountItem> items) {
        mValues = items;
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        FragmentItemBinding binding = FragmentItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        View view = binding.getRoot();
        ViewHolder viewHolder = new ViewHolder(binding);
        view.setOnLongClickListener(v -> {
            int position = viewHolder.getLayoutPosition();
            //String msg = "Item<" + position + "> long Clicked!";
            //Snackbar.make(view, msg, Snackbar.LENGTH_SHORT).show();
            if (longClickListener != null)
                longClickListener.onRecyclerViewItemLongClick(position, view);
            return true;
        });
        return viewHolder;
    }


    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        CountItem item = mValues.get(position);
        holder.mItem = item;
        String str = item.id;
        int l1= str.length();
        if ( l1 < 2) str = "  ".substring(l1) + str;
        holder.mIdView.setText(str);
        holder.mContentView.setText(item.content);
        str = String.valueOf(item.count);
        holder.mCountView.setText(str);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yy-MM-dd");
        holder.mMarkView.setText(item.mark.format(formatter));
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView mIdView;
        public final TextView mContentView;
        public final TextView mCountView;
        public final TextView mMarkView;
        public CountItem mItem;

        public ViewHolder(FragmentItemBinding binding) {
            super(binding.getRoot());
            mIdView = binding.itemNumber;
            mContentView = binding.content;
            mCountView = binding.count;
            mMarkView = binding.mark;
        }

        @NonNull @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }

    public interface OnRecyclerViewItemLongClickListener {
        void onRecyclerViewItemLongClick(int position, View view);
    }

    private OnRecyclerViewItemLongClickListener longClickListener;
    public void setOnRecyclerViewItemLongClickListener(OnRecyclerViewItemLongClickListener longClickListener) {
        this.longClickListener = longClickListener;
    }


}
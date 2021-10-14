package com.tomorrow_eyes.buddhacount;

import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

//import com.tomorrow_eyes.buddhacount.placeholder.PlaceholderContent.PlaceholderItem;
import com.tomorrow_eyes.buddhacount.ItemContent.CountItem;
import com.tomorrow_eyes.buddhacount.databinding.FragmentItemBinding;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;


public class MyItemRecyclerViewAdapter extends RecyclerView.Adapter<MyItemRecyclerViewAdapter.ViewHolder> {

    private final List<CountItem> mValues;

    public MyItemRecyclerViewAdapter(List<CountItem> items) {
        mValues = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        return new ViewHolder(FragmentItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));

    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        CountItem item = mValues.get(position);
        holder.mItem = item;
        holder.mIdView.setText(item.id);
        holder.mContentView.setText(item.content);
        holder.mCountView.setText(String.valueOf(item.count));
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

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}
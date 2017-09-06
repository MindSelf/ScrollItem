package com.example.zhaolexi.scrollitem;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

/**
 * Created by ZHAOLEXI on 2017/9/4.
 */

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    List<String> datas;
    Context context;
    private ScrollItem lastItemView;
    private int lastPosition=-1;

    public RecyclerViewAdapter(List<String> datas,RecyclerView recyclerView) {
        this.datas=datas;
        //recyclerView上下滑动时将展开的ScrollItem合拢
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (lastPosition != -1) {
                    lastItemView.resetViewSmoothly();
                    lastItemView = null;
                    lastPosition=-1;
                }
                super.onScrolled(recyclerView, dx, dy);
            }
        });
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        private TextView tv;

        public ViewHolder(View itemView) {
            super(itemView);
            tv = (TextView) itemView.findViewById(R.id.name);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
        final ViewHolder holder = new ViewHolder(view);


        holder.tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position=holder.getAdapterPosition();
                Toast.makeText(context, "Click "+datas.get(position), Toast.LENGTH_SHORT).show();
            }
        });

        ScrollItem scrollItem=(ScrollItem) holder.itemView;
        scrollItem.setDeleteListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lastPosition=-1;
                int position=holder.getAdapterPosition();
                //不对item进行刷新直接remove的话不会更新布局
                //虽然其他item也复用了这个viewHolder，不过只刷新position上的viewHolder不能刷新复用这个viewHolder的其他item，不知道有没有其他更好的方法
                notifyItemRangeChanged(0, datas.size());
                datas.remove(position);
                notifyItemRemoved(position);
            }
        });

        scrollItem.setOnScrollListener(new ScrollItem.OnScrollListener() {
            @Override
            public void onScrollStateChange(ScrollItem view, int state) {
                switch (state) {
                    case STATE_DELETABLE:
                        lastPosition=holder.getAdapterPosition();
                        lastItemView=view;
                        break;
                    case STATE_UNDELETABLE:
                        if (lastItemView == view) {
                            lastPosition=-1;
                            lastItemView=null;
                        }
                        break;
                    case STATE_STARTMOVING:
                        //滑动时将上次的展开的ScrollItem合拢
                        if (lastItemView != null) {
                            lastItemView.resetViewSmoothly();
                        }
                        break;
                }
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.tv.setText(datas.get(position));
    }

    @Override
    public int getItemCount() {
        return datas.size();
    }
}

package com.example.familylamp;

import android.content.res.Configuration;
import android.view.animation.AnimationUtils;

import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;

public class LampAnimator extends DefaultItemAnimator {

    @Override
    public boolean animateRemove(final RecyclerView.ViewHolder holder) {
        return super.animateRemove(holder);
    }

    @Override
    public boolean animateAdd(final RecyclerView.ViewHolder holder) {
        if (holder.itemView.getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            holder.itemView.setAnimation(AnimationUtils.loadAnimation(
                    holder.itemView.getContext(),
                    R.anim.slide_from_left
            ));
        } else {
            holder.itemView.setAnimation(AnimationUtils.loadAnimation(
                    holder.itemView.getContext(),
                    R.anim.slide_from_bottom
            ));
        }
        return super.animateAdd(holder);
    }

    @Override
    public boolean animateMove(final RecyclerView.ViewHolder holder, final int fromX, final int fromY, final int toX, final int toY) {
        return false;
    }

}

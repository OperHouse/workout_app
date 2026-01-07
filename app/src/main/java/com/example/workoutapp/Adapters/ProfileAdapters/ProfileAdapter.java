package com.example.workoutapp.Adapters.ProfileAdapters;

import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workoutapp.Models.ProfileModels.ProfileItemModel;
import com.example.workoutapp.R;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class ProfileAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface OnChildItemClickListener {
        void onChildItemClick(int flatPosition, String title);
        void onGroupItemClick(int flatPosition, String title); // Метод для клика по группе-кнопке
    }

    public final List<ProfileItemModel> dataList;
    private final Resources res;
    private final OnChildItemClickListener clickListener;

    private static final float DEFAULT_ELEVATION_DP = 8f;
    private static final float DEFAULT_RADIUS_DP = 12f;
    private static final int DEFAULT_BOTTOM_MARGIN_DP = 10;

    public ProfileAdapter(List<ProfileItemModel> dataList, Resources res, OnChildItemClickListener clickListener) {
        this.dataList = dataList;
        this.res = res;
        this.clickListener = clickListener;
    }

    @Override
    public int getItemViewType(int position) {
        return dataList.get(position).type;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == ProfileItemModel.TYPE_GROUP) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.group_items_card, parent, false);
            return new GroupViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.child_item_layout, parent, false);
            return new ChildViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ProfileItemModel item = dataList.get(position);
        float density = res.getDisplayMetrics().density;

        holder.itemView.setVisibility(item.isVisible ? View.VISIBLE : View.GONE);
        RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) holder.itemView.getLayoutParams();
        if (!item.isVisible) {
            lp.height = 0; lp.width = 0;
            lp.setMargins(0, 0, 0, 0);
        } else {
            lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
        }
        holder.itemView.setLayoutParams(lp);

        if (getItemViewType(position) == ProfileItemModel.TYPE_GROUP) {
            GroupViewHolder groupHolder = (GroupViewHolder) holder;
            groupHolder.groupNameTextView.setText(item.title);
            MaterialCardView card = groupHolder.frameLayout;

            // Проверка: есть ли дети у группы?
            boolean hasChildren = (position + 1 < dataList.size() && dataList.get(position + 1).type == ProfileItemModel.TYPE_CHILD);

            // Если детей нет (группа-кнопка), скрываем стрелочку
            groupHolder.expandIndicator.setVisibility(hasChildren ? View.VISIBLE : View.GONE);

            card.setStrokeColor(res.getColor(R.color.light_gray2));
            card.setStrokeWidth((int) (2 * density));
            card.setCardBackgroundColor(res.getColor(R.color.background_1));

            if (item.isExpanded && hasChildren) {
                float radius = DEFAULT_RADIUS_DP * density;
                card.setShapeAppearanceModel(card.getShapeAppearanceModel().toBuilder()
                        .setTopLeftCornerSize(radius).setTopRightCornerSize(radius)
                        .setBottomLeftCornerSize(0).setBottomRightCornerSize(0).build());
                setBottomMargin(card, 0);
            } else {
                float radius = DEFAULT_RADIUS_DP * density;
                card.setShapeAppearanceModel(card.getShapeAppearanceModel().toBuilder().setAllCornerSizes(radius).build());
                setBottomMargin(card, (int) (DEFAULT_BOTTOM_MARGIN_DP * density));
            }

            groupHolder.linearLayout.setBackground(null);
            groupHolder.expandIndicator.setImageResource(item.isExpanded ?
                    R.drawable.ic_arrow_up_foreground : R.drawable.ic_arrow_down_foreground);

        } else {
            ChildViewHolder childHolder = (ChildViewHolder) holder;
            childHolder.childTextView.setText(item.title);

            boolean isLastChild = (position + 1 >= dataList.size() || dataList.get(position + 1).type == ProfileItemModel.TYPE_GROUP);
            if (isLastChild) {
                childHolder.itemView.setBackgroundResource(R.drawable.group_bg_bottom);
                setBottomMargin(childHolder.itemView, (int) (DEFAULT_BOTTOM_MARGIN_DP * density));
            } else {
                childHolder.itemView.setBackgroundResource(R.drawable.group_bg_mid);
                setBottomMargin(childHolder.itemView, 0);
            }
        }
    }

    private void setBottomMargin(View view, int bottomMargin) {
        if (view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            params.bottomMargin = bottomMargin;
            view.requestLayout();
        }
    }

    @Override
    public int getItemCount() { return dataList.size(); }

    private void toggleGroup(int position) {
        ProfileItemModel group = dataList.get(position);
        group.isExpanded = !group.isExpanded;
        boolean expanding = group.isExpanded;
        int childItemIndexStart = -1;
        int count = 0;

        for (int i = position + 1; i < dataList.size(); i++) {
            ProfileItemModel item = dataList.get(i);
            if (item.type == ProfileItemModel.TYPE_CHILD && item.groupIndex == position) {
                item.isVisible = expanding;
                if (childItemIndexStart == -1) childItemIndexStart = i;
                count++;
            } else if (item.type == ProfileItemModel.TYPE_GROUP) break;
        }

        if (count > 0) notifyItemRangeChanged(childItemIndexStart, count);
        notifyItemChanged(position);
    }

    public class GroupViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView groupNameTextView;
        public ImageView expandIndicator;
        public LinearLayout linearLayout;
        public MaterialCardView frameLayout;

        public GroupViewHolder(View itemView) {
            super(itemView);
            this.frameLayout = (MaterialCardView) itemView;
            groupNameTextView = itemView.findViewById(R.id.group_name);
            expandIndicator = itemView.findViewById(R.id.expand_indicator);
            linearLayout = itemView.findViewById(R.id.leaner_layout);
            linearLayout.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                boolean hasChildren = (position + 1 < dataList.size() && dataList.get(position + 1).type == ProfileItemModel.TYPE_CHILD);
                if (!hasChildren && clickListener != null) {
                    clickListener.onGroupItemClick(position, dataList.get(position).title);
                } else {
                    toggleGroup(position);
                }
            }
        }
    }

    public class ChildViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView childTextView;
        public ChildViewHolder(View itemView) {
            super(itemView);
            childTextView = itemView.findViewById(R.id.child_name);
            itemView.setOnClickListener(this);
        }
        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION && clickListener != null) {
                clickListener.onChildItemClick(position, dataList.get(position).title);
            }
        }
    }
}
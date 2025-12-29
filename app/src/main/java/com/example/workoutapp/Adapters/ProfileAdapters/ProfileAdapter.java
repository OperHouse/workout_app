package com.example.workoutapp.Adapters.ProfileAdapters;
// Используйте свой пакет

import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workoutapp.Models.ProfileModels.ProfileItemModel;
import com.example.workoutapp.R;

import java.util.List;

public class ProfileAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface OnChildItemClickListener {
        /**
         * Вызывается при нажатии на дочерний элемент.
         * @param flatPosition Плоская позиция элемента в списке dataList.
         * @param title Заголовок нажатого элемента (для дополнительной информации).
         */
        void onChildItemClick(int flatPosition, String title);
    }

    public final List<ProfileItemModel> dataList;
    private final Resources res;
    private final OnChildItemClickListener clickListener;


    // Константы из вашего XML для свернутого состояния
    private static final float DEFAULT_ELEVATION_DP = 8f;
    private static final float DEFAULT_RADIUS_DP = 12f;
    private static final int DEFAULT_BOTTOM_MARGIN_DP = 10;

    // Передаем список данных и Resources для работы с dp/px


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
            // group_item_card.xml - Корневой элемент CardView
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.group_items_card, parent, false);
            return new GroupViewHolder(view);
        } else {
            // child_item_layout.xml - Корневой элемент LinearLayout
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.child_item_layout, parent, false);
            return new ChildViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ProfileItemModel item = dataList.get(position);

        // --- Логика скрытия/отображения ---
        holder.itemView.setVisibility(item.isVisible ? View.VISIBLE : View.GONE);
        holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                item.isVisible ? ViewGroup.LayoutParams.WRAP_CONTENT : 0
        ));

        float density = res.getDisplayMetrics().density;

        // --- Логика для отображения группы или дочернего элемента ---
        if (getItemViewType(position) == ProfileItemModel.TYPE_GROUP) {
            // Настроим CardView для группы
            GroupViewHolder groupHolder = (GroupViewHolder) holder;
            groupHolder.groupNameTextView.setText(item.title);

            // Получаем LayoutParams для динамического изменения маргинов
            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) groupHolder.cardView.getLayoutParams();
            boolean hasChildren = (position + 1 < dataList.size() &&
                    dataList.get(position + 1).groupIndex == position);

            if (item.isExpanded) {
                // Состояние раскрытой группы
                groupHolder.cardView.setRadius(0f);
                layoutParams.bottomMargin = 0;
                groupHolder.linearLayout.setBackgroundResource(hasChildren ? R.drawable.group_bg_top : R.drawable.card_border);
                groupHolder.expandIndicator.setImageResource(R.drawable.ic_spinner_arrow_down);
            } else {
                // Состояние свернутой группы
                groupHolder.cardView.setCardElevation(DEFAULT_ELEVATION_DP * density);
                groupHolder.cardView.setRadius(DEFAULT_RADIUS_DP * density);
                layoutParams.bottomMargin = (int) (DEFAULT_BOTTOM_MARGIN_DP * density);
                groupHolder.linearLayout.setBackgroundResource(R.drawable.card_border);
                groupHolder.expandIndicator.setImageResource(R.drawable.ic_spinner_arrow_down);
            }

            groupHolder.cardView.setLayoutParams(layoutParams);
        } else {
            // Настройка дочернего элемента
            ChildViewHolder childHolder = (ChildViewHolder) holder;
            childHolder.childTextView.setText(item.title);

            // Убираем тень для дочернего элемента
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                holder.itemView.setElevation(0f);
            }

            int groupIndex = item.groupIndex;
            boolean isLastChildInGroup = (position + 1 >= dataList.size() ||
                    dataList.get(position + 1).type == ProfileItemModel.TYPE_GROUP ||
                    dataList.get(position + 1).groupIndex != groupIndex);

            ViewGroup.MarginLayoutParams childLayoutParams =
                    (ViewGroup.MarginLayoutParams) holder.itemView.getLayoutParams();

            childLayoutParams.topMargin = 0;

            if (isLastChildInGroup) {
                holder.itemView.setBackgroundResource(R.drawable.group_bg_bottom);
                childLayoutParams.bottomMargin = (int) (DEFAULT_BOTTOM_MARGIN_DP * density);
            } else {
                holder.itemView.setBackgroundResource(R.drawable.group_bg_mid);
                childLayoutParams.bottomMargin = 0;
            }
            holder.itemView.setLayoutParams(childLayoutParams);
        }
    }




    @Override
    public int getItemCount() {
        return dataList.size();
    }

    // Логика раскрытия/сворачивания с анимацией
    private void toggleGroup(int position) {
        ProfileItemModel group = dataList.get(position);
        if (group.type != ProfileItemModel.TYPE_GROUP) return;

        group.isExpanded = !group.isExpanded;  // Переключаем флаг раскрытия/сворачивания группы

        boolean expanding = group.isExpanded;  // Состояние раскрытия

        int childItemIndexStart = -1;
        int count = 0;

        // Перебираем элементы после группы, чтобы обновить состояние видимости дочерних элементов
        for (int i = position + 1; i < dataList.size(); i++) {
            ProfileItemModel item = dataList.get(i);

            if (item.type == ProfileItemModel.TYPE_CHILD && item.groupIndex == position) {
                item.isVisible = expanding;  // Устанавливаем видимость для дочернего элемента
                if (childItemIndexStart == -1) {
                    childItemIndexStart = i;  // Сохраняем индекс первого дочернего элемента
                }
                count++;
            } else if (item.type == ProfileItemModel.TYPE_GROUP) {
                break;  // Достигли следующей группы
            }
        }

        // Если найдены дочерние элементы, обновляем их
        if (count > 0 && childItemIndexStart != -1) {
            notifyItemRangeChanged(childItemIndexStart, count);  // Обновляем диапазон дочерних элементов
        }

        // Обновляем саму группу
        notifyItemChanged(position);  // Обновляем состояние самой группы (иконка, цвет фона)
    }


    // --- ViewHolders ---

    public class GroupViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView groupNameTextView;
        public ImageView expandIndicator;
        public LinearLayout linearLayout;
        public CardView cardView;

        public GroupViewHolder(View itemView) {
            super(itemView);
            // itemview - это CardView (корневой элемент group_item_card.xml)
            this.cardView = (CardView) itemView;

            groupNameTextView = itemView.findViewById(R.id.group_name);
            expandIndicator = itemView.findViewById(R.id.expand_indicator);
            linearLayout = itemView.findViewById(R.id.leaner_layout); // Внутренний LinearLayout

            linearLayout.setOnClickListener(this); // Нажатие на внутренний layout
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                toggleGroup(position);
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
            if (position != RecyclerView.NO_POSITION) {
                ProfileItemModel item = dataList.get(position);
                // Проверяем, что это действительно дочерний элемент и что слушатель установлен
                if (item.type == ProfileItemModel.TYPE_CHILD && clickListener != null) {
                    clickListener.onChildItemClick(position, item.title);
                }
            }
        }
    }


}
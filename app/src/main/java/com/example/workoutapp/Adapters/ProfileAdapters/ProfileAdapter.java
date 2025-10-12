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

        // --- 1. Логика скрытия/отображения ---

        // Управляем видимостью элемента. Эта логика необходима для корректной работы
        // ItemAnimator при появлении/исчезновении элементов.
        holder.itemView.setVisibility(item.isVisible ? View.VISIBLE : View.GONE);
        holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                item.isVisible ? ViewGroup.LayoutParams.WRAP_CONTENT : 0
        ));

        // Получаем плотность экрана для перевода dp в px
        float density = res.getDisplayMetrics().density;

        // --- 2. Логика границ, тени и маргинов ---

        if (getItemViewType(position) == ProfileItemModel.TYPE_GROUP) {
            // A. Настройка группы (CardView)
            GroupViewHolder groupHolder = (GroupViewHolder) holder;
            CardView cardView = groupHolder.cardView;

            groupHolder.groupNameTextView.setText(item.title);

            // Получаем LayoutParams для динамического изменения нижнего маргина
            ViewGroup.MarginLayoutParams layoutParams =
                    (ViewGroup.MarginLayoutParams) cardView.getLayoutParams();

            // Проверяем, есть ли дети для правильного применения фона/скругления
            boolean hasChildren = (position + 1 < dataList.size() &&
                    dataList.get(position + 1).groupIndex == position);

            if (item.isExpanded) {
                // СОСТОЯНИЕ: РАСКРЫТО (Прилипание)

                // Убираем тень и скругление CardView (для прилипания сверху)
                //cardView.setCardElevation(0f);
                cardView.setRadius(0f);

                // Убираем нижний маргин (чтобы прилипнуть к первому дочернему элементу)
                layoutParams.bottomMargin = 0;

                // Применяем фон для верхней части (обводка только сверху/по бокам)
                if (hasChildren) {
                    groupHolder.linearLayout.setBackgroundResource(R.drawable.group_bg_top);
                } else {
                    // Группа раскрыта, но детей нет -> возвращаем CardView в одиночное состояние
                    cardView.setCardElevation(DEFAULT_ELEVATION_DP * density);
                    cardView.setRadius(DEFAULT_RADIUS_DP * density);
                    layoutParams.bottomMargin = (int) (DEFAULT_BOTTOM_MARGIN_DP * density);
                    groupHolder.linearLayout.setBackgroundResource(R.drawable.card_border);
                }

                groupHolder.expandIndicator.setImageResource(R.drawable.ic_spinner_arrow_down); // Используйте вашу иконку
                cardView.setLayoutParams(layoutParams);

            } else {
                // СОСТОЯНИЕ: СВЕРНУТО (Левитация)

                // Возвращаем тень и скругление из XML (или констант)
                cardView.setCardElevation(DEFAULT_ELEVATION_DP * density);
                cardView.setRadius(DEFAULT_RADIUS_DP * density);

                // Возвращаем нижний маргин (для расстояния между группами)
                layoutParams.bottomMargin = (int) (DEFAULT_BOTTOM_MARGIN_DP * density);

                // Возвращаем фон (ваш стандартный card_border)
                groupHolder.linearLayout.setBackgroundResource(R.drawable.card_border);

                groupHolder.expandIndicator.setImageResource(R.drawable.ic_spinner_arrow_down); // Используйте вашу иконку
                cardView.setLayoutParams(layoutParams);
            }


        } else {
            // B. Настройка дочернего элемента (LinearLayout)
            ChildViewHolder childHolder = (ChildViewHolder) holder;
            childHolder.childTextView.setText(item.title);

            // Убираем тень и скругление (дети всегда плоские)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                holder.itemView.setElevation(0f);
            }

            // Проверяем, является ли текущий элемент последним ребенком в этой группе
            int groupIndex = item.groupIndex;
            boolean isLastChildInGroup = (position + 1 >= dataList.size() ||
                    dataList.get(position + 1).type == ProfileItemModel.TYPE_GROUP ||
                    dataList.get(position + 1).groupIndex != groupIndex);

            ViewGroup.MarginLayoutParams childLayoutParams =
                    (ViewGroup.MarginLayoutParams) holder.itemView.getLayoutParams();

            // *** ИЗМЕНЕНИЕ ДЛЯ ПРИЛИПАНИЯ СВЕРХУ ***
            // Устанавливаем верхний маргин в 0, чтобы прилипнуть к предыдущему элементу (группе или другому ребенку)
            childLayoutParams.topMargin = 0;
            // ***************************************

            if (isLastChildInGroup) {
                // Последний ребенок: обводка по бокам и снизу, скругление снизу
                holder.itemView.setBackgroundResource(R.drawable.group_bg_bottom);
                // Добавляем маргин, чтобы создать расстояние до следующей группы
                childLayoutParams.bottomMargin = (int) (DEFAULT_BOTTOM_MARGIN_DP * density);
            } else {
                // Средний ребенок: обводка только по бокам
                holder.itemView.setBackgroundResource(R.drawable.group_bg_mid);
                // Убираем маргин для прилипания
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

        group.isExpanded = !group.isExpanded;
        boolean expanding = group.isExpanded;

        int childItemIndexStart = -1;
        int count = 0;

        // 1. Перебираем список, чтобы обновить флаг isVisible и найти диапазон детей
        for (int i = position + 1; i < dataList.size(); i++) {
            ProfileItemModel item = dataList.get(i);

            if (item.type == ProfileItemModel.TYPE_CHILD && item.groupIndex == position) {
                item.isVisible = expanding; // Обновляем состояние видимости элемента
                if (childItemIndexStart == -1) {
                    childItemIndexStart = i;
                }
                count++;
            } else if (item.type == ProfileItemModel.TYPE_GROUP) {
                break; // Достигли следующей группы
            }
        }

        // 2. Запускаем анимацию на дочерних элементах
        if (count > 0 && childItemIndexStart != -1) {
            // Используем notifyItemRangeChanged() для запуска ItemAnimator на элементах,
            // которые меняют видимость (VISIBLE <-> GONE). Это дает плавную анимацию.
            notifyItemRangeChanged(childItemIndexStart, count);
        }

        // 3. Обновляем саму группу (для смены фона, радиуса и стрелки)
        notifyItemChanged(position);
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
package com.example.workoutapp.Adapters.ProfileAdapters;
// Используйте свой пакет

import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workoutapp.Models.ProfileModels.ProfileItemModel;
import com.example.workoutapp.R;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class ProfileAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface OnChildItemClickListener {
        /**
         * Вызывается при нажатии на дочерний элемент.
         *
         * @param flatPosition Плоская позиция элемента в списке dataList.
         * @param title        Заголовок нажатого элемента (для дополнительной информации).
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
        float density = res.getDisplayMetrics().density;

        // Скрытие/показ элемента
        holder.itemView.setVisibility(item.isVisible ? View.VISIBLE : View.GONE);
        RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) holder.itemView.getLayoutParams();
        if (!item.isVisible) {
            lp.height = 0;
            lp.width = 0;
            lp.setMargins(0, 0, 0, 0);
        } else {
            lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
            // Возвращаем стандартные отступы, если нужно
        }
        holder.itemView.setLayoutParams(lp);

        if (getItemViewType(position) == ProfileItemModel.TYPE_GROUP) {
            GroupViewHolder groupHolder = (GroupViewHolder) holder;
            groupHolder.groupNameTextView.setText(item.title);

            MaterialCardView card = groupHolder.frameLayout;

            // 1. Настройка обводки и фона (теперь через методы MaterialCardView)
            card.setStrokeColor(res.getColor(R.color.light_gray2));
            card.setStrokeWidth((int) (2 * density));
            card.setCardBackgroundColor(res.getColor(R.color.background_1));

            // 2. Логика скругления углов и теней
            if (item.isExpanded) {
                // Раскрыта: скругляем ТОЛЬКО ВЕРХНИЕ углы, убираем нижний отступ
                float radius = DEFAULT_RADIUS_DP * density;
                card.setShapeAppearanceModel(
                        card.getShapeAppearanceModel()
                                .toBuilder()
                                .setTopLeftCornerSize(radius)
                                .setTopRightCornerSize(radius)
                                .setBottomLeftCornerSize(0)
                                .setBottomRightCornerSize(0)
                                .build()
                );
                card.setCardElevation(DEFAULT_ELEVATION_DP * density); // Оставляем тень
                setBottomMargin(card, 0); // Убираем отступ снизу, чтобы "приклеить" детей
            } else {
                // Свернута: скругляем ВСЕ углы, возвращаем отступ
                float radius = DEFAULT_RADIUS_DP * density;
                card.setShapeAppearanceModel(
                        card.getShapeAppearanceModel()
                                .toBuilder()
                                .setAllCornerSizes(radius)
                                .build()
                );
                card.setCardElevation(DEFAULT_ELEVATION_DP * density);
                setBottomMargin(card, (int) (DEFAULT_BOTTOM_MARGIN_DP * density));
            }

            // Убираем старый фон у внутреннего слоя!
            groupHolder.linearLayout.setBackground(null);
            groupHolder.expandIndicator.setImageResource(item.isExpanded ?
                    R.drawable.ic_arrow_up_foreground : R.drawable.ic_arrow_down_foreground);

        } else {
            ChildViewHolder childHolder = (ChildViewHolder) holder;
            childHolder.childTextView.setText(item.title);

            // 1. Убираем/ставим стрелку в зависимости от группы
            if (item.groupIndex >= 0 && item.groupIndex < dataList.size()) {
                String parentTitle = dataList.get(item.groupIndex).title;
                String progressTitle = res.getString(R.string.group_progress);

                if (parentTitle.equals(progressTitle)) {
                    // Группа "Прогресс" — полностью убираем все иконки (лево, верх, право, низ)
                    childHolder.childTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);

                    // Также отключаем кликабельность, чтобы не было эффекта нажатия
                    childHolder.itemView.setClickable(false);
                    childHolder.itemView.setFocusable(false);
                } else {
                    // Остальные группы — возвращаем стрелку вправо
                    childHolder.childTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_next_foreground, 0);

                    // Включаем кликабельность обратно
                    childHolder.itemView.setClickable(true);
                    childHolder.itemView.setFocusable(true);
                }
            }

            // Определяем, является ли этот ребенок последним в группе
            boolean isLastChild = (position + 1 >= dataList.size() ||
                    dataList.get(position + 1).type == ProfileItemModel.TYPE_GROUP);

            if (isLastChild) {
                // Последний ребенок: используем фон с закругленными нижними углами
                childHolder.itemView.setBackgroundResource(R.drawable.group_bg_bottom);
                setBottomMargin(childHolder.itemView, (int) (DEFAULT_BOTTOM_MARGIN_DP * density));
            } else {
                // Средний ребенок: прямые углы
                childHolder.itemView.setBackgroundResource(R.drawable.group_bg_mid);
                setBottomMargin(childHolder.itemView, 0);
            }
        }
    }

    // Вспомогательный метод для изменения отступов
    private void setBottomMargin(View view, int bottomMargin) {
        if (view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            params.bottomMargin = bottomMargin;
            view.requestLayout();
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
        public MaterialCardView frameLayout;

        public GroupViewHolder(View itemView) {
            super(itemView);
            // itemview - это CardView (корневой элемент group_item_card.xml)
            this.frameLayout = (MaterialCardView) itemView;

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
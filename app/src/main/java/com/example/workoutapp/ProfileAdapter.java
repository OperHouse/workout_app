package com.example.workoutapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workoutapp.ProfileItem; // Убедитесь, что путь верный

import java.util.ArrayList;
import java.util.List;

public class ProfileAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<ProfileItem> dataList;

    public ProfileAdapter(List<ProfileItem> dataList) {
        // Мы работаем с копией, которую будем изменять, чтобы избежать проблем с исходными данными
        this.dataList = dataList;
    }

    @Override
    public int getItemViewType(int position) {
        return dataList.get(position).type;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == ProfileItem.TYPE_GROUP) {
            // Используем group_items_card.xml для группы
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.group_items_card, parent, false);
            return new GroupViewHolder(view);
        } else {
            // Создаем простой макет для дочернего элемента
            // ТРЕБУЕТСЯ: child_item_layout.xml (см. шаг 4)
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.child_item_layout, parent, false);
            return new ChildViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ProfileItem item = dataList.get(position);

        if (getItemViewType(position) == ProfileItem.TYPE_GROUP) {
            GroupViewHolder groupHolder = (GroupViewHolder) holder;
            groupHolder.groupNameTextView.setText(item.title);
            // Тут можно добавить логику для стрелки-индикатора isExpanded

        } else {
            ChildViewHolder childHolder = (ChildViewHolder) holder;
            childHolder.childTextView.setText(item.title);

            // Главная логика: скрытие/отображение дочернего элемента
            childHolder.itemView.setVisibility(item.isVisible ? View.VISIBLE : View.GONE);
            // Установка LayoutParams нужна, чтобы элемент не занимал место, когда скрыт.
            childHolder.itemView.setLayoutParams(new RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    item.isVisible ? ViewGroup.LayoutParams.WRAP_CONTENT : 0
            ));
        }
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    // ====================================
    // Логика раскрытия/сворачивания
    // ====================================

    private void toggleGroup(int position) {
        ProfileItem group = dataList.get(position);
        if (group.type != ProfileItem.TYPE_GROUP) return;

        group.isExpanded = !group.isExpanded;

        // Перебор списка для изменения isVisible у дочерних элементов
        for (int i = position + 1; i < dataList.size(); i++) {
            ProfileItem item = dataList.get(i);

            // Если item - дочерний элемент текущей группы
            if (item.type == ProfileItem.TYPE_CHILD && item.groupIndex == position) {
                item.isVisible = group.isExpanded;
            }
            // Если мы дошли до следующей группы, останавливаемся
            else if (item.type == ProfileItem.TYPE_GROUP) {
                break;
            }
        }

        // Обновление всего списка, чтобы изменения вступили в силу
        // В больших списках лучше использовать DiffUtil, но для маленького списка это проще
        notifyDataSetChanged();
    }

    // ====================================
    // ViewHolders
    // ====================================

    // ViewHolder для элемента ГРУППЫ (на основе group_items_card.xml)
    public class GroupViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView groupNameTextView;
        public LinearLayout linearLayout;

        public GroupViewHolder(View itemView) {
            super(itemView);
            groupNameTextView = itemView.findViewById(R.id.group_name);
            linearLayout = itemView.findViewById(R.id.leaner_layout);

            // Устанавливаем слушатель на весь элемент группы
            linearLayout.setOnClickListener(this);


        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                toggleGroup(position);
            }
        }
    }

    // ViewHolder для ДОЧЕРНЕГО элемента (на основе child_item_layout.xml)
    public class ChildViewHolder extends RecyclerView.ViewHolder {
        public TextView childTextView;

        public ChildViewHolder(View itemView) {
            super(itemView);
            childTextView = itemView.findViewById(R.id.child_name); // ID TextView в child_item_layout
        }
    }
}
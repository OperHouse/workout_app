package com.example.workoutapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

public abstract class ExpandableListAdapter extends BaseExpandableListAdapter {

    private Context context;
    private List<String> groupList;
    private Map<String, List<String>> childList;
    private Map<String, Integer> groupIcons; // Карта иконок для групп

    public ExpandableListAdapter(Context context, List<String> groupList, Map<String, List<String>> childList, Map<String, Integer> groupIcons) {
        this.context = context;
        this.groupList = groupList;
        this.childList = childList;
        this.groupIcons = groupIcons;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return childList.get(groupList.get(groupPosition)).get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return childList.get(groupList.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return groupList.get(groupPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
                             View convertView, ViewGroup parent) {

        String childText = (String) getChild(groupPosition, childPosition);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.expandable_child, null);








        TextView textView = view.findViewById(R.id.childText);
        ImageView checkmark = view.findViewById(R.id.checkmark);

        textView.setText(childText);

        // Можно добавить логику для отображения галочки
        checkmark.setVisibility(childPosition % 2 == 0 ? View.VISIBLE : View.INVISIBLE);

        return view;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        String groupText = (String) getGroup(groupPosition);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.expandable_group, null);

        TextView textView = view.findViewById(R.id.groupText);
        ImageView groupIcon = view.findViewById(R.id.groupIcon);
        ImageView indicator = view.findViewById(R.id.indicator);

        textView.setText(groupText);

        // Устанавливаем иконку для группы
        Integer iconResId = groupIcons.get(groupText);
        if (iconResId != null) {
            groupIcon.setImageResource(iconResId);
        } else {
            groupIcon.setImageResource(R.drawable.ic_back);
        }

        // Индикатор (стрелка вниз/вверх)
        indicator.setImageResource(isExpanded ? android.R.drawable.arrow_up_float : android.R.drawable.arrow_down_float);

        // ✅ Меняем фон в зависимости от состояния
        View rootLayout = view.findViewById(R.id.group_root); // Это ID корневого layout внутри expandable_group.xml
        if (isExpanded) {
            rootLayout.setBackgroundResource(R.drawable.card_border_top_rounded);
        } else {
            rootLayout.setBackgroundResource(R.drawable.card_border);
        }

        return view;
    }


    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}

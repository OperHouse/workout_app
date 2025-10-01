package com.example.workoutapp.Fragments.ProfileFragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.workoutapp.ExpandableListAdapter;
import com.example.workoutapp.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ProfileFragment extends Fragment {

    private ExpandableListView expandableListView;
    private ExpandableListAdapter expandableListAdapter;

    /**
     * Вспомогательный метод для динамической установки высоты ExpandableListView.
     * Это необходимо, когда ExpandableListView находится внутри ScrollView/NestedScrollView
     * и его собственная прокрутка отключена, чтобы родительский скролл знал
     * общую высоту контента.
     */
    private void setListViewHeight(ExpandableListView listView) {
        ExpandableListAdapter listAdapter = (ExpandableListAdapter) listView.getExpandableListAdapter();
        if (listAdapter == null) {
            return;
        }

        int totalHeight = 0;
        // Используем AT_MOST для определения максимальной ширины
        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.AT_MOST);

        // Перебираем все группы, чтобы измерить их высоту
        for (int i = 0; i < listAdapter.getGroupCount(); i++) {
            View groupItem = listAdapter.getGroupView(i, false, null, listView);
            groupItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += groupItem.getMeasuredHeight();

            // Если группа раскрыта, добавляем высоту всех дочерних элементов
            if (listView.isGroupExpanded(i)) {
                for (int j = 0; j < listAdapter.getChildrenCount(i); j++) {
                    View listItem = listAdapter.getChildView(i, j, false, null, listView);
                    listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
                    totalHeight += listItem.getMeasuredHeight();
                }
            }
        }

        // Учитываем разделители между элементами
        int dividerHeight = listView.getDividerHeight() * (listAdapter.getGroupCount() - 1);
        for (int i = 0; i < listAdapter.getGroupCount(); i++) {
            if (listView.isGroupExpanded(i)) {
                // Добавляем разделители между дочерними элементами, если они есть
                dividerHeight += listView.getDividerHeight() * listAdapter.getChildrenCount(i);
            }
        }
        totalHeight += dividerHeight;

        // Устанавливаем новую высоту
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight;
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View ProfileView = inflater.inflate(R.layout.fragment_profile, container, false);

        // Настройка отображения даты
        TextView dateTextView = ProfileView.findViewById(R.id.dateTextProfile);
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, d MMMM", new Locale("ru", "RU"));
        String formattedDate = dateFormat.format(calendar.getTime());
        formattedDate = formattedDate.substring(0, 1).toUpperCase() + formattedDate.substring(1);
        dateTextView.setText(formattedDate);

        // Настройка ExpandableListView
        expandableListView = ProfileView.findViewById(R.id.expandableListView);

        // 1. Фикс для проблемы дергающейся прокрутки: ОТКЛЮЧАЕМ собственную прокрутку.
        // ЭТО НЕОБХОДИМО ОСТАВИТЬ, чтобы избежать конфликта прокрутки (дерганья).
        expandableListView.setNestedScrollingEnabled(false);

        List<String> groupList = new ArrayList<>();
        groupList.add("Профиль");
        groupList.add("Питание");
        groupList.add("Активность");
        groupList.add("Данные");

        Map<String, List<String>> childList = new HashMap<>();
        childList.put("Профиль", new ArrayList<String>() {{
            add("Имя");
            add("Фамилия");
            add("Электронная почта");
        }});
        childList.put("Питание", new ArrayList<String>() {{
            add("Завтрак");
            add("Ужин");
            add("Полдник");
        }});
        childList.put("Активность", new ArrayList<String>() {{
            add("Тренировка 1");
            add("Тренировка 2");
        }});
        childList.put("Данные", new ArrayList<String>() {{
            add("Вес");
            add("Рост");
            add("Возраст");
        }});

        Map<String, Integer> groupIcons = new HashMap<>();
        groupIcons.put("Профиль", R.drawable.ic_user_profile);
        groupIcons.put("Питание", R.drawable.ic_user_profile);
        groupIcons.put("Активность", R.drawable.ic_user_profile);
        groupIcons.put("Данные", R.drawable.ic_user_profile);

        // Инициализация адаптера
        expandableListAdapter = new ExpandableListAdapter(getActivity(), groupList, childList, groupIcons) {
            @Override
            public int getGroupCount() {
                return 4;
            }
        };
        expandableListView.setAdapter(expandableListAdapter);

        // 2. Вызываем метод установки высоты после инициализации,
        // чтобы NestedScrollView мог прокручиваться сразу.
        // Используем post(), чтобы убедиться, что ширина уже измерена.
        expandableListView.post(new Runnable() {
            @Override
            public void run() {
                setListViewHeight(expandableListView);
            }
        });


        // 3. Устанавливаем слушателей, чтобы ПЕРЕСЧИТЫВАТЬ высоту при раскрытии/сворачивании группы
        // Это восстанавливает прокрутку при изменении размера списка.
        expandableListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition) {
                setListViewHeight(expandableListView);
            }
        });

        expandableListView.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {
            @Override
            public void onGroupCollapse(int groupPosition) {
                setListViewHeight(expandableListView);
            }
        });


        return ProfileView;
    }
}

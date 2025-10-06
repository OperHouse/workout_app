// Файл: ProfileFragment.java (Обновленный код)

package com.example.workoutapp.Fragments.ProfileFragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workoutapp.ProfileAdapter; // Импорт нового адаптера
import com.example.workoutapp.ProfileItem;
import com.example.workoutapp.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ProfileFragment extends Fragment {

    private RecyclerView groupRecyclerView;
    private ProfileAdapter profileAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View ProfileView = inflater.inflate(R.layout.fragment_profile, container, false);

        // Настройка отображения даты (оставляем)
        TextView dateTextView = ProfileView.findViewById(R.id.dateTextProfile);
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, d MMMM", new Locale("ru", "RU"));
        String formattedDate = dateFormat.format(calendar.getTime());
        formattedDate = formattedDate.substring(0, 1).toUpperCase() + formattedDate.substring(1);
        dateTextView.setText(formattedDate);

        // ===============================================
        // НАСТРОЙКА RECYCLERVIEW ДЛЯ РАСКРЫВАЮЩЕГОСЯ СПИСКА
        // ===============================================

        groupRecyclerView = ProfileView.findViewById(R.id.group_RV);

        // Устанавливаем LayoutManager
        groupRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Важно: Отключаем скроллинг RecyclerView, так как он находится внутри NestedScrollView
        groupRecyclerView.setNestedScrollingEnabled(false);

        // Подготовка данных
        List<ProfileItem> dataItems = prepareDataForRecyclerView();

        // Инициализация и установка адаптера
        profileAdapter = new ProfileAdapter(dataItems);
        groupRecyclerView.setAdapter(profileAdapter);

        return ProfileView;
    }

    /**
     * Преобразует иерархические данные в плоский список ProfileItem
     * для использования в RecyclerView.
     */
    private List<ProfileItem> prepareDataForRecyclerView() {
        // Иерархические данные (как раньше в ExpandableListView)
        List<String> groupNames = new ArrayList<>();
        groupNames.add("Профиль");
        groupNames.add("Питание");
        groupNames.add("Активность");
        groupNames.add("Данные");

        Map<String, List<String>> childListMap = new HashMap<>();
        childListMap.put("Профиль", new ArrayList<String>() {{
            add("Имя");
            add("Фамилия");
            add("Электронная почта");
        }});
        childListMap.put("Питание", new ArrayList<String>() {{
            add("Завтрак");
            add("Ужин");
            add("Полдник");
        }});
        childListMap.put("Активность", new ArrayList<String>() {{
            add("Тренировка 1");
            add("Тренировка 2");
        }});
        childListMap.put("Данные", new ArrayList<String>() {{
            add("Вес");
            add("Рост");
            add("Возраст");
        }});

        List<ProfileItem> flatList = new ArrayList<>();
        int groupIndexCounter = 0; // Счетчик для отслеживания индекса группы в flatList

        for (String groupName : groupNames) {
            // 1. Добавляем элемент группы
            ProfileItem group = new ProfileItem(groupName, false); // Изначально не раскрыт
            flatList.add(group);

            // 2. Добавляем дочерние элементы
            List<String> children = childListMap.get(groupName);
            for (String childName : children) {
                // groupIndexCounter - это индекс родительской группы
                flatList.add(new ProfileItem(childName, groupIndexCounter));
            }
            // Обновляем счетчик индекса группы для следующей итерации
            groupIndexCounter = flatList.size();
        }
        return flatList;
    }
}
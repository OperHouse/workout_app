package com.example.workoutapp.Fragments.ActivityStatisticFragment;

import android.graphics.Color;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workoutapp.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class WeekCalendarAdapter extends RecyclerView.Adapter<WeekCalendarAdapter.ViewHolder> {

    private OnDayClickListener listener;

    private final int MAX_DAYS = 10000;
    private int selectedPosition;
    private final int OFFSET;

    private final Calendar today = Calendar.getInstance();
    private final SimpleDateFormat dayNameFormat = new SimpleDateFormat("EE", Locale.getDefault());
    private final SimpleDateFormat fullDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    public interface OnDayClickListener {
        // Передаем дату строкой для удобного поиска в БД и объект модели для UI
        void onDayClick(String fullDate, String dayNumber);
    }

    public WeekCalendarAdapter(OnDayClickListener listener) {
        this.listener = listener;

        // Вычисляем, сколько дней до конца текущей недели, чтобы "Сегодня" было в нужном месте
        Calendar cal = Calendar.getInstance();
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK); // 1 - Воскресенье, 2 - Понедельник...

        // Приводим к формату Пн=0, Вс=6
        int daysFromMonday = (dayOfWeek == Calendar.SUNDAY) ? 6 : dayOfWeek - 2;

        // OFFSET будет указывать на сегодняшний день в "бесконечном" потоке
        this.OFFSET = 5000;
        this.selectedPosition = OFFSET;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_week_day, parent, false);

        // Расчет ширины (1/7 экрана)
        int availableWidth = parent.getMeasuredWidth();
        if (availableWidth <= 0) {
            availableWidth = parent.getContext().getResources().getDisplayMetrics().widthPixels;
            float paddingPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32, parent.getResources().getDisplayMetrics());
            availableWidth -= (int) paddingPx;
        }

        int itemWidth = availableWidth / 7;
        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.width = itemWidth;
        view.setLayoutParams(params);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Calendar itemDate = (Calendar) today.clone();
        // Сдвигаем дату относительно OFFSET
        itemDate.add(Calendar.DAY_OF_YEAR, position - OFFSET);

        String dayNumber = String.valueOf(itemDate.get(Calendar.DAY_OF_MONTH));
        String dayName = dayNameFormat.format(itemDate.getTime()).toUpperCase();
        String fullDate = fullDateFormat.format(itemDate.getTime());

        holder.tvDayName.setText(dayName);
        holder.tvDate.setText(dayNumber);

        // Логика выделения
        boolean isSelected = (position == selectedPosition);
        holder.itemView.setSelected(isSelected);
        holder.itemView.setBackgroundResource(R.drawable.bg_week_day_selected);

        // Проверка на будущее время
        boolean isFuture = itemDate.after(today) && itemDate.get(Calendar.DAY_OF_YEAR) != today.get(Calendar.DAY_OF_YEAR);

        if (isFuture) {
            holder.tvDayName.setTextColor(Color.DKGRAY);
            holder.tvDate.setTextColor(Color.DKGRAY);
            holder.itemView.setAlpha(0.5f);
            holder.itemView.setEnabled(false);
        } else {
            holder.itemView.setAlpha(1.0f);
            holder.itemView.setEnabled(true);

            if (isSelected) {
                holder.tvDayName.setTextColor(Color.WHITE);
                holder.tvDate.setTextColor(Color.WHITE);
            } else {
                holder.tvDayName.setTextColor(Color.GRAY);
                holder.tvDate.setTextColor(Color.WHITE);
            }

            holder.itemView.setOnClickListener(v -> {
                int oldPos = selectedPosition;
                selectedPosition = holder.getAdapterPosition();

                // Обновляем только два элемента для экономии ресурсов
                notifyItemChanged(oldPos);
                notifyItemChanged(selectedPosition);

                if (listener != null) {
                    listener.onDayClick(fullDate, dayNumber);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return MAX_DAYS;
    }

    public void setSelectedPosition(int position) {
        int oldPos = selectedPosition;
        this.selectedPosition = position;
        notifyItemChanged(oldPos); // Снимаем выделение со старого
        notifyItemChanged(selectedPosition); // Рисуем выделение на новом
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDayName, tvDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDayName = itemView.findViewById(R.id.tv_day_name);
            tvDate = itemView.findViewById(R.id.tv_date);
        }
    }
}
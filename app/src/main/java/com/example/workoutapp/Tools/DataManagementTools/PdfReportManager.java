package com.example.workoutapp.Tools.DataManagementTools;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;

import com.example.workoutapp.Data.Tables.AppDataBase;
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.PDPage;
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream;
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle;
import com.tom_roush.pdfbox.pdmodel.font.PDFont;
import com.tom_roush.pdfbox.pdmodel.font.PDType0Font;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PdfReportManager {

    private final Context context;
    private final SQLiteDatabase db;
    private static final float MARGIN = 50;
    private static final float PAGE_WIDTH = PDRectangle.A4.getWidth();

    private static final float[] COLUMN_WIDTHS_NUTRITION = {150f, 45f, 45f, 45f, 45f};
    private static final String[] HEADERS_NUTRITION = {"Прием пищи", "Ккал", "Жир", "Угл", "Белк"};

    public PdfReportManager(Context context, SQLiteDatabase db) {
        this.context = context;
        this.db = db;
        PDFBoxResourceLoader.init(context);
    }

    public File createReport(List<String> categories) {
        try (PDDocument document = new PDDocument()) {
            // 1. Загружаем шрифты
            PDFont font = PDType0Font.load(document, context.getAssets().open("fonts/roboto-regular.ttf"));
            PDFont fontBold = PDType0Font.load(document, context.getAssets().open("fonts/roboto-bold.ttf"));

            // 2. Создаем первую страницу и инициализируем обертку потока
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            // Массив из одного элемента позволяет методам внутри менять объект потока
            PDPageContentStream[] streamWrapper = { new PDPageContentStream(document, page) };
            float y = page.getMediaBox().getHeight() - MARGIN;

            // 3. Рисуем общий заголовок отчета
            y = drawHeader(streamWrapper[0], fontBold, font, y);

            // 4. Основной цикл по категориям
            for (String category : categories) {

                // Проверка: если места осталось совсем мало перед новым разделом (< 150)
                if (y < 150) {
                    streamWrapper[0].close();
                    PDPage newPage = new PDPage(PDRectangle.A4);
                    document.addPage(newPage);
                    streamWrapper[0] = new PDPageContentStream(document, newPage);
                    y = newPage.getMediaBox().getHeight() - MARGIN;
                }

                // Отрисовка заголовка текущего раздела
                y = drawSectionHeader(streamWrapper[0], fontBold, category, y);

                // Выбор логики отрисовки
                switch (category) {
                    case "activity":
                        // ВАЖНО: если эти методы тоже будут длинными,
                        // их тоже стоит перевести на streamWrapper в будущем
                        y = drawActivitySection(streamWrapper[0], font, fontBold, y);
                        break;

                    case "workouts":
                        y = drawWorkoutsTable(streamWrapper[0], font, y);
                        break;

                    case "nutrition":
                        // Здесь используем streamWrapper, так как тут 4 графика и большая таблица
                        y = drawNutritionSection(streamWrapper, document, font, fontBold, y);

                        // Дополнительная проверка перед таблицей (на случай, если графики съели всё место)
                        if (y < 100) {
                            streamWrapper[0].close();
                            PDPage newPage = new PDPage(PDRectangle.A4);
                            document.addPage(newPage);
                            streamWrapper[0] = new PDPageContentStream(document, newPage);
                            y = newPage.getMediaBox().getHeight() - MARGIN;
                        }

                        // Отрисовка детальной таблицы питания
                        y = drawDetailedNutritionTable(streamWrapper, document, font, fontBold, y);
                        break;
                }

                y -= 40; // Отступ между разделами
            }

            // Закрываем поток последней открытой страницы
            streamWrapper[0].close();

            // 5. Сохранение файла во внутренний кэш
            File cacheDir = new File(context.getCacheDir(), "exports");
            if (!cacheDir.exists()) cacheDir.mkdirs();

            File file = new File(cacheDir, "Workout_Full_Report.pdf");
            document.save(new FileOutputStream(file));

            Log.d("PDF_REPORT", "Отчет успешно создан: " + file.getAbsolutePath());
            return file;

        } catch (Exception e) {
            Log.e("PDF_ERROR", "Критическая ошибка при формировании PDF: ", e);
            return null;
        }
    }

    private float drawHeader(PDPageContentStream stream, PDFont bold, PDFont regular, float y) throws Exception {
        stream.beginText();
        stream.setFont(bold, 20);
        stream.newLineAtOffset(MARGIN, y);
        stream.showText("ОТЧЕТ О ПРОГРЕССЕ");
        stream.endText();

        y -= 20;
        stream.beginText();
        stream.setFont(regular, 10);
        stream.newLineAtOffset(MARGIN, y);
        String date = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(new Date());
        stream.showText("Дата формирования: " + date);
        stream.endText();

        return y - 40;
    }

    private float drawSectionHeader(PDPageContentStream stream, PDFont bold, String category, float y) throws Exception {
        stream.setNonStrokingColor(230, 230, 230);
        stream.addRect(MARGIN, y - 5, PAGE_WIDTH - 2 * MARGIN, 20);
        stream.fill();

        stream.beginText();
        stream.setNonStrokingColor(0, 0, 0);
        stream.setFont(bold, 12);
        stream.newLineAtOffset(MARGIN + 5, y);
        stream.showText(getFriendlyTitle(category));
        stream.endText();
        return y - 30;
    }

    // ====================== ACTIVITY SECTION (Steps, Dist, Cals) ======================
    private float drawActivitySection(PDPageContentStream stream, PDFont font, PDFont bold, float y) throws Exception {
        String query = "SELECT " +
                AppDataBase.DAILY_ACTIVITY_TRACKING_ACTIVITY_DATE + ", " +
                AppDataBase.DAILY_ACTIVITY_TRACKING_ACTIVITY_STEPS + ", " +
                AppDataBase.DAILY_ACTIVITY_TRACKING_CALORIES_BURN + ", " +
                AppDataBase.DAILY_ACTIVITY_TRACKING_ACTIVITY_DISTANCE +
                " FROM " + AppDataBase.DAILY_ACTIVITY_TRACKING_TABLE +
                " ORDER BY rowid DESC LIMIT 7";

        Cursor cursor = db.rawQuery(query, null);
        List<String> dates = new ArrayList<>();
        List<Float> steps = new ArrayList<>();
        List<Float> cals = new ArrayList<>();

        while (cursor.moveToNext()) {
            dates.add(cursor.getString(0).substring(5)); // урезаем год
            steps.add((float) cursor.getInt(1));
            cals.add((float) cursor.getFloat(2));
        }
        cursor.close();

        if (steps.isEmpty()) return y;

        // Рисуем график шагов (столбчатый)
        y = drawMiniBarChart(stream, font, "Шаги (последние 7 записей)", dates, steps, 10000f, y);
        y -= 40;
        // Рисуем график сожженных калорий
        y = drawMiniBarChart(stream, font, "Сожженные калории", dates, cals, 3000f, y);

        return y;
    }

    // ====================== WORKOUTS TABLE ======================
    private float drawWorkoutsTable(PDPageContentStream stream, PDFont font, float y) throws Exception {
        // Запрос: Название упражнения, Дата и количество связанных сетов (подходов)
        String query = "SELECT e." + AppDataBase.WORKOUT_EXERCISE_NAME + ", e." + AppDataBase.WORKOUT_EXERCISE_DATE + ", " +
                "(SELECT COUNT(*) FROM " + AppDataBase.STRENGTH_SET_DETAILS_TABLE +
                " s WHERE s." + AppDataBase.STRENGTH_SET_ID + " = e." + AppDataBase.WORKOUT_EXERCISE_ID + ") as set_count" +
                " FROM " + AppDataBase.WORKOUT_EXERCISE_TABLE + " e" +
                " ORDER BY e." + AppDataBase.WORKOUT_EXERCISE_DATE + " DESC LIMIT 15";

        Cursor cursor = db.rawQuery(query, null);

        // Заголовки
        stream.setFont(font, 10);
        float startX = MARGIN + 5;
        stream.beginText();
        stream.newLineAtOffset(startX, y);
        stream.showText("Упражнение");
        stream.newLineAtOffset(220, 0);
        stream.showText("Подходы");
        stream.newLineAtOffset(80, 0);
        stream.showText("Дата");
        stream.endText();

        y -= 5;
        stream.setLineWidth(1f);
        stream.moveTo(MARGIN, y);
        stream.lineTo(PAGE_WIDTH - MARGIN, y);
        stream.stroke();
        y -= 15;

        while (cursor.moveToNext()) {
            String name = cursor.getString(0);
            String date = cursor.getString(1);
            int sets = cursor.getInt(2);

            stream.beginText();
            stream.setFont(font, 9);
            stream.newLineAtOffset(startX, y);
            stream.showText(name.length() > 30 ? name.substring(0, 28) + ".." : name);
            stream.newLineAtOffset(220, 0);
            stream.showText(sets > 0 ? String.valueOf(sets) : "—");
            stream.newLineAtOffset(80, 0);
            stream.showText(date);
            stream.endText();

            y -= 15;
            if (y < MARGIN + 30) break;
        }
        cursor.close();
        return y;
    }

    // ====================== NUTRITION SECTION (Cals, BJU) ======================
    private float drawNutritionSection(PDPageContentStream[] streamWrapper, PDDocument document, PDFont font, PDFont bold, float y) throws Exception {
        // 1. Получение данных из БД
        String query = "SELECT " +
                AppDataBase.DAILY_FOOD_TRACKING_DATE + ", " +
                AppDataBase.TRACKING_CALORIES + ", " +
                AppDataBase.TRACKING_PROTEIN + ", " +
                AppDataBase.TRACKING_FAT + ", " +
                AppDataBase.TRACKING_CARB +
                " FROM " + AppDataBase.DAILY_FOOD_TRACKING_TABLE + " ORDER BY rowid DESC LIMIT 7";

        Cursor cursor = db.rawQuery(query, null);
        List<String> dates = new ArrayList<>();
        List<Float> cals = new ArrayList<>();
        List<Float> pro = new ArrayList<>();
        List<Float> fat = new ArrayList<>();
        List<Float> carb = new ArrayList<>();

        while (cursor.moveToNext()) {
            String rawDate = cursor.getString(0);

            try {
                SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date parsed = dbFormat.parse(rawDate);

                SimpleDateFormat outFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
                dates.add(outFormat.format(parsed));

            } catch (Exception e) {
                dates.add(rawDate); // если вдруг формат другой
            }
            cals.add(cursor.getFloat(1));
            pro.add(cursor.getFloat(2));
            fat.add(cursor.getFloat(3));
            carb.add(cursor.getFloat(4));
        }
        cursor.close();

        if (dates.isEmpty()) return y;

        // Данные для цикличной отрисовки 4-х графиков
        String[] titles = {"Калории", "Белки (г)", "Жиры (г)", "Углеводы (г)"};
        List<List<Float>> allValues = new ArrayList<>();
        allValues.add(cals);
        allValues.add(pro);
        allValues.add(fat);
        allValues.add(carb);

        // Замени эти значения на реальное получение целей из твоего профиля/настроек
        float[] goals = {2500f, 150f, 80f, 300f};

        int[] colors = {
                Color.parseColor("#00BCD4"), // Циан
                Color.parseColor("#4CAF50"), // Зеленый
                Color.parseColor("#FFC107"), // Желтый
                Color.parseColor("#9C27B0")  // Фиолетовый
        };

        // 2. Отрисовка графиков
        for (int i = 0; i < titles.length; i++) {

            // ПРОВЕРКА МЕСТА: Если до конца страницы меньше 180 единиц, создаем новую
            if (y < 180) {
                streamWrapper[0].close();
                PDPage newPage = new PDPage(PDRectangle.A4);
                document.addPage(newPage);
                streamWrapper[0] = new PDPageContentStream(document, newPage);
                y = newPage.getMediaBox().getHeight() - MARGIN;

                // Вместо drawSectionHeader пишем простой понятный заголовок
                streamWrapper[0].beginText();
                streamWrapper[0].setFont(bold, 12);
                streamWrapper[0].setNonStrokingColor(100, 100, 100);
                streamWrapper[0].newLineAtOffset(MARGIN, y);
                streamWrapper[0].showText("История питания (продолжение)");
                streamWrapper[0].endText();
                y -= 30;
            }

            // Рисуем сам график
            y = drawBarChartWithGoal(streamWrapper[0], font, bold, titles[i], dates, allValues.get(i), goals[i], colors[i], y);

            y -= 40; // Отступ после каждого графика
        }

        return y;
    }
    private float drawBarChartWithGoal(PDPageContentStream stream, PDFont font, PDFont bold,
                                       String title, List<String> labels, List<Float> values,
                                       float goal, int color, float y) throws Exception {
        float chartHeight = 100f;
        float chartWidth = PAGE_WIDTH - (2 * MARGIN) - 40f;
        float leftAxisPadding = 40f;
        float chartLeft = MARGIN + leftAxisPadding;

        // 1. Заголовок
        stream.beginText();
        stream.setFont(bold, 10);
        stream.setNonStrokingColor(0, 0, 0);
        stream.newLineAtOffset(MARGIN, y);
        stream.showText(title + " (Цель: " + (int)goal + ")");
        stream.endText();

        y -= 15;
        float chartBottom = y - chartHeight;

        // Расчет максимума
        float maxVal = goal;
        for (float v : values) if (v > maxVal) maxVal = v;
        float maxValueOnChart = maxVal * 1.2f;

        // 2. Сетка и шкала Y
        stream.setLineWidth(0.5f);
        stream.setStrokingColor(220, 220, 220);
        stream.setLineDashPattern(new float[]{2, 2}, 0);

        int divisions = 4;
        for (int i = 0; i <= divisions; i++) {
            float val = (maxValueOnChart / divisions) * i;
            float currentY = chartBottom + (chartHeight * (val / maxValueOnChart));

            stream.moveTo(chartLeft, currentY);
            stream.lineTo(chartLeft + chartWidth, currentY);
            stream.stroke();

            stream.beginText();
            stream.setFont(font, 7);
            stream.setNonStrokingColor(120, 120, 120);
            stream.newLineAtOffset(MARGIN, currentY - 2);
            stream.showText(String.valueOf((int)val));
            stream.endText();
        }

        // 3. Линия цели (Цветная)
        float goalY = chartBottom + (chartHeight * (goal / maxValueOnChart));
        stream.setLineWidth(1.2f);
        stream.setLineDashPattern(new float[]{4, 2}, 0);
        stream.setStrokingColor((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF);
        stream.moveTo(chartLeft, goalY);
        stream.lineTo(chartLeft + chartWidth, goalY);
        stream.stroke();
        stream.setLineDashPattern(new float[]{}, 0);

        // 4. Столбики и даты
        float spacing = chartWidth / 7f;
        float barWidth = spacing * 0.5f;

        for (int i = 0; i < values.size(); i++) {
            float barH = (values.get(i) / maxValueOnChart) * chartHeight;
            float x = chartLeft + (i * spacing) + (spacing - barWidth) / 2;

            // Столбик
            stream.setNonStrokingColor((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF);
            stream.addRect(x, chartBottom, barWidth, barH);
            stream.fill();

            // ПОДПИСЬ ДАТЫ (Исправлено)
            String dateText = labels.get(i);
            stream.beginText();
            stream.setFont(font, 7);
            stream.setNonStrokingColor(50, 50, 50);

            // Рассчитываем точное центрирование по горизонтали
            // (Ширина символа в PDFBox делится на 1000 и умножается на размер шрифта)
            float textWidth = font.getStringWidth(dateText) / 1000f * 7f;
            float textX = x + (barWidth / 2f) - (textWidth / 2f);

            stream.newLineAtOffset(textX, chartBottom - 12);
            stream.showText(dateText); // ТУТ БОЛЬШЕ НЕТ ПРОВЕРОК НА LENGTH И ТОЧЕК
            stream.endText();
        }

        return chartBottom - 20;
    }


    // ====================== УНИВЕРСАЛЬНЫЙ ГРАФИК (BAR CHART) ======================
    private float drawMiniBarChart(PDPageContentStream stream, PDFont font, String title,
                                   List<String> labels, List<Float> values, float maxVal, float y) throws Exception {
        float chartHeight = 80;
        float chartWidth = PAGE_WIDTH - 2 * MARGIN - 50;
        float barWidth = 25;
        float spacing = chartWidth / 7;

        stream.beginText();
        stream.setFont(font, 10);
        stream.newLineAtOffset(MARGIN, y);
        stream.showText(title);
        stream.endText();

        y -= chartHeight + 20;

        // Оси
        stream.setLineWidth(1f);
        stream.moveTo(MARGIN + 30, y);
        stream.lineTo(MARGIN + 30, y + chartHeight); // Y
        stream.lineTo(MARGIN + 30 + chartWidth, y);  // X
        stream.stroke();

        for (int i = 0; i < values.size(); i++) {
            float barH = (values.get(i) / maxVal) * chartHeight;
            if (barH > chartHeight) barH = chartHeight;

            stream.setNonStrokingColor(100, 150, 255);
            stream.addRect(MARGIN + 40 + (i * spacing), y, barWidth, barH);
            stream.fill();

            // Подпись даты
            stream.beginText();
            stream.setNonStrokingColor(0, 0, 0);
            stream.setFont(font, 8);
            stream.newLineAtOffset(MARGIN + 40 + (i * spacing), y - 12);
            stream.showText(labels.get(i));
            stream.endText();
        }

        return y - 20;
    }

    private String getFriendlyTitle(String category) {
        switch (category) {
            case "activity": return "Активность (Шаги, Калории, Дистанция)";
            case "workouts": return "История тренировок";
            case "nutrition": return "История питания (БЖУ)";
            default: return "Раздел: " + category;
        }
    }

    private float drawDetailedNutritionTable(PDPageContentStream[] streamWrapper,
                                             PDDocument document,
                                             PDFont font,
                                             PDFont bold,
                                             float y) throws Exception {
        float startX = MARGIN;
        float tableWidth = PAGE_WIDTH - (2 * MARGIN);
        float rowHeight = 18f;

        float[] colWidths = {210f, 40f, 40f, 40f, 40f};
        String[] headers = {"Продукт / Дата", "Ккал", "Жир", "Угл", "Белк"};

        // 1. Шапка таблицы
        streamWrapper[0].setNonStrokingColor(240, 240, 240);
        streamWrapper[0].addRect(startX, y - rowHeight, tableWidth, rowHeight);
        streamWrapper[0].fill();
        streamWrapper[0].setNonStrokingColor(0, 0, 0);
        drawRowText(streamWrapper[0], bold, 9, startX + 5, y - 13, headers, colWidths);
        y -= rowHeight;

        String query = "SELECT mn." + AppDataBase.MEAL_NAME + ", mn." + AppDataBase.MEAL_DATA + ", " +
                "mf." + AppDataBase.MEAL_FOOD_NAME + ", mf." + AppDataBase.MEAL_FOOD_CALORIES + ", " +
                "mf." + AppDataBase.MEAL_FOOD_FAT + ", mf." + AppDataBase.MEAL_FOOD_CARB + ", " +
                "mf." + AppDataBase.MEAL_FOOD_PROTEIN +
                " FROM " + AppDataBase.MEAL_NAME_TABLE + " mn " +
                " JOIN " + AppDataBase.CONNECTING_MEAL_TABLE + " c ON mn." + AppDataBase.MEAL_NAME_ID + " = c." + AppDataBase.CONNECTING_MEAL_NAME_ID +
                " JOIN " + AppDataBase.MEAL_FOOD_TABLE + " mf ON c." + AppDataBase.CONNECTING_MEAL_FOOD_ID + " = mf." + AppDataBase.MEAL_FOOD_ID +
                " ORDER BY mn." + AppDataBase.MEAL_DATA + " DESC, mn." + AppDataBase.MEAL_NAME_ID;

        Cursor cursor = db.rawQuery(query, null);

        String lastMealGroup = "";
        float mealCals = 0, mealFat = 0, mealCarb = 0, mealProt = 0;
        boolean isFirst = true;

        while (cursor.moveToNext()) {
            // ПРОВЕРКА МЕСТА: Если места мало, создаем новую страницу
            if (y < MARGIN + 60) {
                streamWrapper[0].close();
                PDPage newPage = new PDPage(PDRectangle.A4);
                document.addPage(newPage);
                streamWrapper[0] = new PDPageContentStream(document, newPage);
                y = newPage.getMediaBox().getHeight() - MARGIN;

                // Повторно рисуем шапку таблицы на новой странице для наглядности
                streamWrapper[0].setNonStrokingColor(240, 240, 240);
                streamWrapper[0].addRect(startX, y - rowHeight, tableWidth, rowHeight);
                streamWrapper[0].fill();
                streamWrapper[0].setNonStrokingColor(0, 0, 0);
                drawRowText(streamWrapper[0], bold, 9, startX + 5, y - 13, headers, colWidths);
                y -= rowHeight;
            }

            String mealName = cursor.getString(0);
            String mealDate = cursor.getString(1);
            String foodName = cursor.getString(2);
            float cal = cursor.getFloat(3);
            float fat = cursor.getFloat(4);
            float carb = cursor.getFloat(5);
            float prot = cursor.getFloat(6);

            String currentMealGroup = mealName + " (" + mealDate + ")";

            // Итог предыдущего приема
            if (!currentMealGroup.equals(lastMealGroup) && !isFirst) {
                y = drawMealTotalRow(streamWrapper[0], bold, startX, y, tableWidth, rowHeight, colWidths, "Итого", mealCals, mealFat, mealCarb, mealProt);
                mealCals = 0; mealFat = 0; mealCarb = 0; mealProt = 0;
            }

            // Заголовок группы (Завтрак/Обед)
            if (!currentMealGroup.equals(lastMealGroup)) {
                y -= rowHeight;
                streamWrapper[0].setNonStrokingColor(250, 250, 250);
                streamWrapper[0].addRect(startX, y, tableWidth, rowHeight);
                streamWrapper[0].fill();
                streamWrapper[0].setNonStrokingColor(0, 0, 0);
                streamWrapper[0].setFont(bold, 9);
                streamWrapper[0].beginText();
                streamWrapper[0].newLineAtOffset(startX + 5, y + 5);
                streamWrapper[0].showText(currentMealGroup);
                streamWrapper[0].endText();
                lastMealGroup = currentMealGroup;
                isFirst = false;
            }

            // Строка продукта
            y -= rowHeight;
            String[] rowData = { foodName, String.valueOf((int)cal), String.format("%.1f", fat), String.format("%.1f", carb), String.format("%.1f", prot) };
            drawRowText(streamWrapper[0], font, 8, startX + 5, y + 5, rowData, colWidths);

            mealCals += cal; mealFat += fat; mealCarb += carb; mealProt += prot;

            // Линия разделитель
            streamWrapper[0].setStrokingColor(230, 230, 230);
            streamWrapper[0].setLineWidth(0.5f);
            streamWrapper[0].moveTo(startX, y);
            streamWrapper[0].lineTo(startX + tableWidth, y);
            streamWrapper[0].stroke();
        }

        // Финальный итог
        if (!isFirst) {
            y = drawMealTotalRow(streamWrapper[0], bold, startX, y, tableWidth, rowHeight, colWidths, "Итого", mealCals, mealFat, mealCarb, mealProt);
        }

        cursor.close();
        return y;
    }

    /**
     * Вспомогательный метод для отрисовки строки "Итого"
     */
    private float drawMealTotalRow(PDPageContentStream stream, PDFont bold, float startX, float y, float tableWidth, float rowHeight, float[] colWidths, String label, float c, float f, float carb, float p) throws Exception {
        y -= rowHeight;
        stream.setNonStrokingColor(245, 245, 255); // Нежно-голубой фон для итогов
        stream.addRect(startX, y, tableWidth, rowHeight);
        stream.fill();
        stream.setNonStrokingColor(0, 0, 0);

        String[] totals = {
                "ВСЕГО:",
                String.valueOf((int)c),
                String.format("%.1f", f),
                String.format("%.1f", carb),
                String.format("%.1f", p)
        };
        drawRowText(stream, bold, 8, startX + 5, y + 5, totals, colWidths);
        return y - 5; // Небольшой отступ после итога
    }

    private void drawRowText(PDPageContentStream stream, PDFont font, float size, float x, float y, String[] data, float[] widths) throws Exception {
        stream.setFont(font, size);
        float currentX = x;
        for (int i = 0; i < data.length; i++) {
            stream.beginText();
            stream.newLineAtOffset(currentX, y);
            String text = data[i] != null ? data[i] : "0";
            // Обрезка длинных названий продуктов
            if (i == 0 && text.length() > 35) text = text.substring(0, 32) + "...";
            stream.showText(text);
            stream.endText();
            currentX += widths[i];
        }
    }
}
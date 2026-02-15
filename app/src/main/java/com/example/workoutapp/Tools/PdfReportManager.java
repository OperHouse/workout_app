package com.example.workoutapp.Tools;

import android.content.Context;
import android.util.Log;

import com.example.workoutapp.Data.Tables.AppDataBase;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.PDPage;
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream;
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle;
import com.tom_roush.pdfbox.pdmodel.font.PDFont;
import com.tom_roush.pdfbox.pdmodel.font.PDType0Font;
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader;

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
            // Загружаем шрифты (убедитесь, что имена файлов в assets совпадают)
            PDFont font = PDType0Font.load(document, context.getAssets().open("fonts/roboto-regular.ttf"));
            PDFont fontBold = PDType0Font.load(document, context.getAssets().open("fonts/roboto-bold.ttf"));

            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            PDPageContentStream stream = new PDPageContentStream(document, page);

            float y = page.getMediaBox().getHeight() - MARGIN;

            // 1. Заголовок отчета
            y = drawHeader(stream, fontBold, font, y);

            for (String category : categories) {
                // Проверка свободного места перед началом нового раздела
                if (y < 300) {
                    stream.close();
                    page = new PDPage(PDRectangle.A4);
                    document.addPage(page);
                    stream = new PDPageContentStream(document, page);
                    y = page.getMediaBox().getHeight() - MARGIN;
                }

                // 2. Отрисовка заголовка раздела (с серой подложкой)
                y = drawSectionHeader(stream, fontBold, category, y);

                // 3. Выбор метода отрисовки в зависимости от категории
                switch (category) {
                    case "activity":
                        // Графики шагов и калорий
                        y = drawActivitySection(stream, font, fontBold, y);
                        break;

                    case "workouts":
                        // Таблица упражнений
                        y = drawWorkoutsTable(stream, font, y);
                        break;

                    case "nutrition":
                        // Детальная таблица в стиле FatSecret + График БЖУ
                        // Сначала рисуем график БЖУ
                        y = drawNutritionSection(stream, font, fontBold, y);

                        // Проверка места перед большой таблицей
                        if (y < 200) {
                            stream.close();
                            page = new PDPage(PDRectangle.A4);
                            document.addPage(page);
                            stream = new PDPageContentStream(document, page);
                            y = page.getMediaBox().getHeight() - MARGIN;
                        }

                        // Затем рисуем детальную таблицу с JOIN-запросом
                        y = drawDetailedNutritionTable(stream, font, fontBold, y);
                        break;
                }

                y -= 40; // Отступ между разделами
            }

            stream.close();

            // 4. Сохранение файла
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
        String query = "SELECT " + AppDataBase.WORKOUT_EXERCISE_NAME + ", " + AppDataBase.WORKOUT_EXERCISE_DATE +
                " FROM " + AppDataBase.WORKOUT_EXERCISE_TABLE + " LIMIT 10";

        Cursor cursor = db.rawQuery(query, null);
        // Заголовки таблицы
        stream.setFont(font, 10);
        float startX = MARGIN + 10;
        stream.beginText();
        stream.newLineAtOffset(startX, y);
        stream.showText("Упражнение");
        stream.newLineAtOffset(300, 0);
        stream.showText("Дата");
        stream.endText();
        y -= 5;
        stream.moveTo(MARGIN, y);
        stream.lineTo(PAGE_WIDTH - MARGIN, y);
        stream.stroke();
        y -= 15;

        while (cursor.moveToNext()) {
            stream.beginText();
            stream.newLineAtOffset(startX, y);
            stream.showText(cursor.getString(0));
            stream.newLineAtOffset(300, 0);
            stream.showText(cursor.getString(1));
            stream.endText();
            y -= 15;
        }
        cursor.close();
        return y;
    }

    // ====================== NUTRITION SECTION (Cals, BJU) ======================
    private float drawNutritionSection(PDPageContentStream stream, PDFont font, PDFont bold, float y) throws Exception {
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

        while (cursor.moveToNext()) {
            dates.add(cursor.getString(0));
            cals.add((float) cursor.getInt(1));
        }
        cursor.close();

        if (!cals.isEmpty()) {
            y = drawMiniBarChart(stream, font, "Потребление калорий", dates, cals, 4000f, y);
        }
        return y;
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

    private float drawDetailedNutritionTable(PDPageContentStream stream, PDFont font, PDFont bold, float y) throws Exception {
        float startX = MARGIN;
        float tableWidth = PAGE_WIDTH - (2 * MARGIN);
        float rowHeight = 18f;

        // Колонки: Продукт, Кал, Жир, Угл, Белк
        float[] colWidths = {180f, 40f, 40f, 40f, 40f};
        String[] headers = {"Продукт/Прием пищи", "Ккал", "Жир", "Угл", "Белк"};

        // 1. Рисуем шапку таблицы (как в FoodDiary_260214_foods.pdf )
        stream.setNonStrokingColor(240, 240, 240);
        stream.addRect(startX, y - rowHeight, tableWidth, rowHeight);
        stream.fill();
        stream.setNonStrokingColor(0, 0, 0);

        drawRowText(stream, bold, 9, startX + 5, y - 13, headers, colWidths);
        y -= rowHeight;

        // 2. SQL запрос для получения приемов пищи и продуктов
        String query = "SELECT mn." + AppDataBase.MEAL_NAME + ", mn." + AppDataBase.MEAL_DATA + ", " +
                "mf." + AppDataBase.MEAL_FOOD_NAME + ", mf." + AppDataBase.MEAL_FOOD_CALORIES + ", " +
                "mf." + AppDataBase.MEAL_FOOD_FAT + ", mf." + AppDataBase.MEAL_FOOD_CARB + ", " +
                "mf." + AppDataBase.MEAL_FOOD_PROTEIN + ", mf." + AppDataBase.MEAL_FOOD_AMOUNT +
                " FROM " + AppDataBase.MEAL_NAME_TABLE + " mn " +
                " JOIN " + AppDataBase.CONNECTING_MEAL_TABLE + " c ON mn." + AppDataBase.MEAL_NAME_ID + " = c." + AppDataBase.CONNECTING_MEAL_NAME_ID +
                " JOIN " + AppDataBase.MEAL_FOOD_TABLE + " mf ON c." + AppDataBase.CONNECTING_MEAL_FOOD_ID + " = mf." + AppDataBase.MEAL_FOOD_ID +
                " ORDER BY mn." + AppDataBase.MEAL_DATA + " DESC, mn." + AppDataBase.MEAL_NAME_ID;

        Cursor cursor = db.rawQuery(query, null);
        String lastMealName = "";
        float dayCals = 0, dayFat = 0, dayCarb = 0, dayProt = 0;

        while (cursor.moveToNext()) {
            String currentMeal = cursor.getString(0); // Завтрак/Обед
            String foodName = cursor.getString(2);
            float cal = cursor.getFloat(3);
            float fat = cursor.getFloat(4);
            float carb = cursor.getFloat(5);
            float prot = cursor.getFloat(6);

            // Если начался новый прием пищи, рисуем подзаголовок (Завтрак, Обед...)
            if (!currentMeal.equals(lastMealName)) {
                stream.setFont(bold, 9);
                y -= rowHeight;
                stream.beginText();
                stream.newLineAtOffset(startX + 5, y + 5);
                stream.showText(currentMeal);
                stream.endText();
                lastMealName = currentMeal;
            }

            // Рисуем строку продукта
            y -= rowHeight;
            String[] rowData = {
                    foodName,
                    String.valueOf((int)cal),
                    String.format("%.1f", fat),
                    String.format("%.1f", carb),
                    String.format("%.1f", prot)
            };
            drawRowText(stream, font, 8, startX + 15, y + 5, rowData, colWidths);

            // Суммируем итоги
            dayCals += cal; dayFat += fat; dayCarb += carb; dayProt += prot;

            // Тонкая линия между продуктами
            stream.setStrokingColor(220, 220, 220);
            stream.setLineWidth(0.5f);
            stream.moveTo(startX, y);
            stream.lineTo(startX + tableWidth, y);
            stream.stroke();

            if (y < MARGIN + 40) break; // Проверка конца страницы
        }
        cursor.close();

        // 3. Итоговая строка (Всего)
        y -= rowHeight;
        stream.setNonStrokingColor(245, 245, 245);
        stream.addRect(startX, y, tableWidth, rowHeight);
        stream.fill();
        stream.setNonStrokingColor(0, 0, 0);

        String[] totals = {"ВСЕГО", (int)dayCals + "", String.format("%.1f", dayFat), String.format("%.1f", dayCarb), String.format("%.1f", dayProt)};
        drawRowText(stream, bold, 9, startX + 5, y + 5, totals, colWidths);

        return y - 20;
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
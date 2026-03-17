package com.example.workoutapp.Tools;

import java.io.UnsupportedEncodingException;
import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class MailHelper {
    // Твои данные
    private static final String SENDER_EMAIL = "workoutapp_help@mail.ru";
    private static final String APP_PASSWORD = "JbCXNKEmNaGNGUMBxCt6";

    public static void sendWelcomeEmail(String userEmail, String userName) {
        new Thread(() -> {
            Properties props = new Properties();

            // Авто-настройка хоста
            if (SENDER_EMAIL.contains("mail.ru")) {
                props.put("mail.smtp.host", "smtp.mail.ru");
            } else if (SENDER_EMAIL.contains("yandex")) {
                props.put("mail.smtp.host", "smtp.yandex.ru");
            } else {
                props.put("mail.smtp.host", "smtp.gmail.com");
            }

            props.put("mail.smtp.port", "465");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.socketFactory.port", "465");
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.ssl.enable", "true");

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(SENDER_EMAIL, APP_PASSWORD);
                }
            });

            try {
                Message message = new MimeMessage(session);

                // Красивое имя отправителя вместо простого адреса
                try {
                    message.setFrom(new InternetAddress(SENDER_EMAIL, "WorkoutApp"));
                } catch (UnsupportedEncodingException e) {
                    message.setFrom(new InternetAddress(SENDER_EMAIL));
                }

                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(userEmail));
                message.setSubject("Добро пожаловать в команду WorkoutApp! 💪");

                // Расширенный HTML шаблон
                String htmlContent = "<div style='font-family: Arial, sans-serif; background-color: #121212; color: #ffffff; padding: 30px; border-radius: 15px; max-width: 600px; margin: auto;'>"
                        + "<h1 style='color: #00B0FF; text-align: center; border-bottom: 1px solid #333; padding-bottom: 10px;'>WorkoutApp</h1>"
                        + "<p style='font-size: 18px;'>Привет, <b>" + userName + "</b>!</p>"
                        + "<p>Твоя почта успешно подтверждена. Теперь ты — часть нашего спортивного сообщества!</p>"
                        + "<div style='background-color: #1E1E1E; padding: 15px; border-radius: 10px; margin: 20px 0;'>"
                        + "<p style='margin: 0; color: #00B0FF; font-weight: bold;'>С чего начать?</p>"
                        + "<ul style='padding-left: 20px; line-height: 1.6;'>"
                        + "<li>Заполни параметры тела в профиле</li>"
                        + "<li>Выбери готовую программу тренировок</li>"
                        + "<li>Установи напоминания, чтобы не пропускать занятия</li>"
                        + "</ul>"
                        + "</div>"
                        + "<p style='text-align: center; font-style: italic; color: #888;'>«Сила не в том, чтобы победить других, а в том, чтобы победить себя вчерашнего».</p>"
                        + "<p style='margin-top: 30px; border-top: 1px solid #333; padding-top: 20px; font-size: 12px; color: #666; text-align: center;'>"
                        + "Вы получили это письмо, так как зарегистрировались в приложении WorkoutApp."
                        + "</p>"
                        + "</div>";

                message.setContent(htmlContent, "text/html; charset=utf-8");
                Transport.send(message);

            } catch (MessagingException e) {
                // Если возникнет ошибка (например, нет интернета), она будет в логах (Logcat)
                e.printStackTrace();
            }
        }).start();
    }
}
package ru.jmdevelop.snptechtest.core;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import ru.jmdevelop.snptechtest.entity.FormByUser;
import ru.jmdevelop.snptechtest.entity.UserState;
import ru.jmdevelop.snptechtest.repo.UserFormRepository;


@Component
@Slf4j
public class TgBotMainClass implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {

    private final TelegramClient telegramClient;
    private final ReportSendler reportSendler;
    private final UserFormRepository userFormRepository;


    @Value("${telegram.bot.token}")
    private String token;

    public TgBotMainClass(@Value("${telegram.bot.token}") String botToken, UserFormRepository userFormRepository, ReportSendler reportSendler) {
        this.telegramClient = new OkHttpTelegramClient(botToken);
        this.userFormRepository = userFormRepository;
        this.reportSendler = reportSendler;
    }


    @Override
    public String getBotToken() {
        return token;
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return this;
    }

    @Override
    public void consume(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return;
        }

        String messageText = update.getMessage().getText();
        Long chatId = update.getMessage().getChatId();
        FormByUser user = userFormRepository.findById(chatId)
                .orElseGet(() -> {
                    FormByUser newUser = new FormByUser(chatId);
                    return newUser;
                });

        if (messageText.startsWith("/")) {
            commandBot(user, messageText);
        } else {
            formInputBot(user, messageText);
        }
    }

    private void commandBot(FormByUser user, String command) {
        switch (command) {
            case "/start" -> handleStart(user);
            case "/form" -> startForm(user);
            case "/report" -> sendWordFile(user);
        }
    }

    private void handleStart(FormByUser user) {
        resetUser(user);
        sendMessage(user.getTelegramId(), "Добро пожаловать! Используйте /form для начала опроса");
    }

    private void startForm(FormByUser user) {
        resetUser(user);
        user.setState(UserState.IN_PROGRESS);
        sendMessage(user.getTelegramId(), "Введите ваше имя:");
        userFormRepository.save(user);
    }


    private void formInputBot(FormByUser user, String messageText) {
        if (user.getState() != UserState.IN_PROGRESS) {
            sendMessage(user.getTelegramId(), "Введите /form чтобы начать опрос");
            return;
        }
        if (user.getName() == null) {
            processNameInput(user, messageText);
        } else if (user.getEmail() == null) {
            processEmailInput(user, messageText);
        } else if (user.getGrade() == null) {
            processGradeInput(user, messageText);
        }
    }

    private void processNameInput(FormByUser user, String name) {
        user.setName(name);
        sendMessage(user.getTelegramId(), "Введите ваш email:");
        userFormRepository.save(user);
    }

    private void processEmailInput(FormByUser user, String email) {
        if (!isValidEmail(email)) {
            sendMessage(user.getTelegramId(), "Неверный формат email. Попробуйте снова:");
            return;
        }

        user.setEmail(email);
        userFormRepository.save(user);
        sendMessage(user.getTelegramId(), "Оцените сервис от 1 до 10:");
    }

    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[\\w.-]+@[\\w.-]+\\.[a-z]{2,}$");
    }

    private void processGradeInput(FormByUser user, String input) {
        try {
            int grade = Integer.parseInt(input);
            if (grade < 1 || grade > 10) {
                throw new NumberFormatException();
            }

            user.setGrade(grade);
            user.setState(UserState.COMPLETED);
            userFormRepository.save(user);
            sendMessage(user.getTelegramId(), "Спасибо! Анкета сохранена.");
        } catch (NumberFormatException e) {
            sendMessage(user.getTelegramId(), "Пожалуйста, введите число от 1 до 10:");
        }
    }

    private void sendWordFile(FormByUser user) {
        sendMessage(user.getTelegramId(), "⌛ Генерация отчета...");

        reportSendler.generateReport(user.getTelegramId())
                .thenAccept(reportFile -> {
                    try {
                        telegramClient.execute(SendDocument.builder()
                                .chatId(user.getTelegramId())
                                .document(new InputFile(reportFile))
                                .build());
                        sendMessage(user.getTelegramId(), "✅ Отчет готов!");
                    } catch (Exception e) {
                        log.error("Ошибка отправки", e);
                        sendMessage(user.getTelegramId(), "❌ Ошибка отправки отчета");
                    } finally {
                        if (reportFile != null && reportFile.exists()) {
                            reportFile.delete();
                        }
                    }
                })
                .exceptionally(e -> {
                    log.error("Ошибка генерации", e);
                    sendMessage(user.getTelegramId(), "❌ Ошибка генерации отчета");
                    return null;
                });
    }

    private void resetUser(FormByUser user) {
        user.setName(null);
        user.setEmail(null);
        user.setGrade(null);
        user.setState(UserState.NEW);
        userFormRepository.save(user);
    }

    @SneakyThrows
    private void sendMessage(Long chatId, String text) {
        SendMessage message = SendMessage.builder()
                .text(text)
                .chatId(chatId)
                .build();
        telegramClient.execute(message);

    }

}


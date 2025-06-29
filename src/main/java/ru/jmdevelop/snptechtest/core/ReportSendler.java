package ru.jmdevelop.snptechtest.core;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ru.jmdevelop.snptechtest.entity.FormByUser;
import ru.jmdevelop.snptechtest.exceptions.UserNotFoundException;
import ru.jmdevelop.snptechtest.repo.UserFormRepository;
import org.apache.poi.xwpf.usermodel.*;

import java.io.File;
import java.io.FileOutputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@Service
@RequiredArgsConstructor
public class ReportSendler {
    private final UserFormRepository repository;

    @Async
    public CompletableFuture<File> generateReport(Long chatId) {
        try {
            FormByUser form = repository.findById(chatId)
                    .orElseThrow(() -> new UserNotFoundException("Данные пользователя отсутствуют"));

            XWPFDocument doc = new XWPFDocument();

            XWPFTable table = doc.createTable(1, 3);

            table.getRow(0).getCell(0).setText("Имя");
            table.getRow(0).getCell(1).setText("Email");
            table.getRow(0).getCell(2).setText("Оценка");


            XWPFTableRow dataRow = table.createRow();
            dataRow.getCell(0).setText(form.getName() != null ? form.getName() : "N/A");
            dataRow.getCell(1).setText(form.getEmail() != null ? form.getEmail() : "N/A");
            dataRow.getCell(2).setText(form.getGrade() != null ? String.valueOf(form.getGrade()) : "N/A");

            File reportFile = File.createTempFile("report_", ".docx");
            try (FileOutputStream out = new FileOutputStream(reportFile)) {
                doc.write(out);
            }

            return CompletableFuture.completedFuture(reportFile);
        } catch (UserNotFoundException e) {
            throw new CompletionException(e);
        }
        catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }
}





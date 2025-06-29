Инструкция по сборке и запуску

1. Настройка окружения

   Создайте файл .env в корне проекта:
   DB_USER=postgres
   DB_PASSWORD=yourpassword
   DB_NAME=bot_db
   TELEGRAM_BOT_TOKEN=your_telegram_bot_token

2. Запуск через Docker

   Собрать и запустить контейнеры
   docker-compose up --build -d
3. Остановка

   docker-compose down


    Описание доступных команд бота
Основные команды:
   /start - Начало работы с ботом

   /form - Запуск анкеты:

        Шаг 1: Введите ваше имя

        Шаг 2: Введите email (проверка формата)

        Шаг 3: Оцените сервис (1-10)

   /report - Генерация отчета в формате Word:

        Содержит: Имя, Email, Оценку

        Формат: DOCX (Microsoft Word)
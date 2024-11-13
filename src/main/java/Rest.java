import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Rest {
    private static String sessionId = "";

    public static void main(String[] args) throws Exception {
        // 1. Получить список всех пользователей
        getAllUsers();

        // 2. Сохранить пользователя с id = 3, name = James, lastName = Brown, age = на ваш выбор.
        saveUser(3, "James", "Brown", 30); // Выберите возраст по своему усмотрению

//        // 3. Изменить пользователя с id = 3. Имя на Thomas, фамилию на Shelby
//        updateUser(3, "Thomas", "Shelby");


        User userToUpdate = new User(3L, "Thomas", "Shelby", (byte) 30); // Пример возраста, где 30 является возрастом

// Изменяем пользователя с Id 3 на Thomas Shelby и возраст 30
        updateUser(userToUpdate);

        // 4. Удалить пользователя с id = 3.
        deleteUser(3);
    }

    private static void getAllUsers() throws Exception {
        String url = "http://94.198.50.185:7081/api/users";
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/json");

        // Получаем session id из cookie
        if (connection.getHeaderField("Set-Cookie") != null) {
            sessionId = connection.getHeaderField("Set-Cookie").split(";")[0];
            System.out.println("Session ID: " + sessionId);
        }

        // Читаем ответ
        if (connection.getResponseCode() == 200) {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            // Закрываем потоки
            in.close();
            System.out.println("Список пользователей: " + content.toString());
        } else {
            System.out.println("Ошибка при получении списка пользователей: " + connection.getResponseCode());
        }

        connection.disconnect();
    }

    private static void saveUser(int id, String name, String lastName, int age) throws Exception {
        String url = "http://94.198.50.185:7081/api/users";
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Cookie", sessionId);
        connection.setDoOutput(true);

        // JSON для нового пользователя
        String jsonInputString = String.format("{\"id\":%d,\"name\":\"%s\",\"lastName\":\"%s\",\"age\":%d}", id, name, lastName, age);

        try (DataOutputStream os = new DataOutputStream(connection.getOutputStream())) {
            os.writeBytes(jsonInputString);
            os.flush();
        }

        // Читаем ответ
        String responseBody = getResponseBody(connection);
        if (connection.getResponseCode() == 200) {
            System.out.println("Пользователь сохранен: " + id + responseBody);
        } else {
            System.out.println("Ошибка при сохранении пользователя: " + connection.getResponseCode());
        }

        connection.disconnect();
    }

//    private static void updateUser(int id, String name, String lastName) throws Exception {
//        String url = "http://94.198.50.185:7081/api/users"; // Обратите внимание, мы используем API /users для PUT
//        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
//        connection.setRequestMethod("PUT");
//        connection.setRequestProperty("Content-Type", "application/json");
//        connection.setRequestProperty("Cookie", sessionId);
//        connection.setDoOutput(true);
//
//        // JSON для обновленного пользователя, необходимо передать ID в теле запроса
//        String jsonInputString = String.format("{\"id\":%d,\"name\":\"%s\",\"lastName\":\"%s\"}", id, name, lastName);
//
//        try (DataOutputStream os = new DataOutputStream(connection.getOutputStream())) {
//            os.writeBytes(jsonInputString);
//            os.flush();
//        }
//
//        // Читаем ответ
//        if (connection.getResponseCode() == 200) {
//            System.out.println("Пользователь обновлен: " + id);
//        } else {
//            System.out.println("Ошибка при обновлении пользователя: " + connection.getResponseCode());
//        }
//
//        connection.disconnect();
//    }


    private static void updateUser(User user) throws Exception {
        String url = "http://94.198.50.185:7081/api/users"; // URL для обновления
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("PUT");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Cookie", sessionId);
        connection.setDoOutput(true);


        // Формируем JSON из объекта User
        String jsonInputString = String.format("{\"id\":%d,\"name\":\"%s\",\"lastName\":\"%s\",\"age\":%d}",
                user.getId(),
                user.getName(),
                user.getLastName(),
                user.getAge() // здесь мы должны использовать int, так что преобразуем Byte в int
        );

        try (DataOutputStream os = new DataOutputStream(connection.getOutputStream())) {
            os.writeBytes(jsonInputString);
            os.flush();
        }

        // Проверяем ответ от сервера
        String responseBody = getResponseBody(connection);
        if (connection.getResponseCode() == 200) {
            System.out.println("Пользователь обновлен: " + user.getId() + responseBody);
        } else {
            String errorMessage = "";
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                errorMessage += line;
            }
            reader.close();
            System.out.println("Ошибка при обновлении пользователя: " + connection.getResponseCode() + " - " + errorMessage);
        }

        connection.disconnect();
    }

    private static void deleteUser(int id) throws Exception {
        String url = "http://94.198.50.185:7081/api/users/" + id;
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("DELETE");
        connection.setRequestProperty("Cookie", sessionId);

        // Читаем ответ
        String responseBody = getResponseBody(connection);
        if (connection.getResponseCode() == 200) {
            System.out.println("Пользователь удален: " + id + responseBody);
        } else {
            System.out.println("Ошибка при удалении пользователя: " + connection.getResponseCode());
        }

        connection.disconnect();
    }

    private static String getResponseBody(HttpURLConnection connection) throws Exception {
        StringBuilder responseBody = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                responseBody.append(line);
            }
        }
        return responseBody.toString();
    }
}
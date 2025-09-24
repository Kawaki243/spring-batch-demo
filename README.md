# 📦 Spring Batch Demo

Demo Project sử dụng **Spring Batch** để import dữ liệu người dùng từ file CSV vào cơ sở dữ liệu.

---

## 🚀 Tính năng
- Đọc dữ liệu từ file CSV (`users-1000.csv`).
- Map từng dòng CSV thành đối tượng `UserEntity`.
- Xử lý dữ liệu trước khi ghi (ví dụ: chuyển `firstName`, `lastName` sang chữ hoa).
- Ghi dữ liệu đã xử lý vào database qua **Spring Data JPA** (`UserRepository`).
- Khởi chạy Job qua REST API (`/jobs/importData`).

---

## 🛠️ Công nghệ sử dụng
- **Spring Boot**
- **Spring Batch**
- **Spring Data JPA**
- **H2 Database** (hoặc bất kỳ DB nào bạn cấu hình)
- **Lombok** (tuỳ chọn)

---

## 📂 Cấu trúc project

```
src/main/java/com/example/batch/
│
├── config/
│   └── SpringBatchConfig.java      # Cấu hình Job, Step, Reader, Processor, Writer
│
├── controller/
│   └── JobController.java          # REST API để chạy job
│
├── entity/
│   └── UserEntity.java             # Entity ánh xạ dữ liệu người dùng
│
├── processor/
│   └── UserProcessor.java          # Xử lý dữ liệu (chuyển tên sang chữ hoa)
│
├── repository/
│   └── UserRepository.java         # JPA Repository
```
---

## ⚙️ Flow hoạt động

```text
+------------------+
|   CSV File       |
| users-1000.csv   |
+--------+---------+
         |
         v
+--------+---------+
| ItemReader       |
| (FlatFileReader) |
+--------+---------+
         |
         v
+--------+---------+
| ItemProcessor    |
| (UserProcessor)  |
| - Uppercase name |
+--------+---------+
         |
         v
+--------+---------+
| ItemWriter       |
| (Repository.save)|
+--------+---------+
         |
         v
+--------+---------+
|   Database       |
|   (UserEntity)   |
+------------------+
```

- Reader: đọc dữ liệu từ CSV.  
- Processor: xử lý dữ liệu (`firstName`, `lastName` → uppercase).  
- Writer: lưu dữ liệu vào DB qua `UserRepository.save()`.  

---

## ▶️ Chạy project

### 1. Clone project
```bash
git clone https://github.com/Kawaki243/spring-batch-csv-importer.git
cd spring-batch-demo
```

### 2. Chạy ứng dụng
```bash
docker build -t springbatch-app . && docker run --rm --name springbatch-container springbatch-app
```

### 3. Gọi API để chạy Job
Sử dụng **Postman** hoặc `curl`:

```bash
curl -X POST http://localhost:8080/api/v1.O/jobs/importData
```

Kết quả trả về:  
- `"COMPLETED"` nếu job chạy thành công.  
- `"Job failed with exception: ..."` nếu có lỗi.  

---

## 📝 Ví dụ dữ liệu CSV

File `users-1000.csv`:

```csv
id,userId,firstName,lastName,gender,email,phone,dateOfBirth,jobTitle
1,U001,John,Doe,Male,john@example.com,123456789,1990-01-01,Engineer
2,U002,Jane,Smith,Female,jane@example.com,987654321,1992-02-02,Manager
```

Sau khi import, dữ liệu trong DB sẽ là:
- `firstName = "JOHN"`, `lastName = "DOE"`
- `firstName = "JANE"`, `lastName = "SMITH"`

---

## 📜 License
MIT License. Bạn có thể sử dụng và chỉnh sửa cho mục đích cá nhân hoặc học tập.

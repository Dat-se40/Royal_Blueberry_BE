# Royal Blueberry Dictionary Backend

## 1. Tổng quan dự án (Project Overview)

**Royal Blueberry Dictionary Backend** là hệ thống máy chủ dịch vụ viết bằng **Spring Boot (Java)**, đóng vai trò cung cấp dữ liệu toàn diện cho ứng dụng từ điển trên client (C# WPF). Hệ thống được thiết kế tối ưu hóa hiệu năng, bảo mật qua cơ chế token, tích hợp tìm kiếm ngữ nghĩa thông minh và đồng bộ dữ liệu linh hoạt từ các dịch vụ từ điển quốc tế hàng đầu.

| Thông tin | Giá trị |
|-----------|---------|
| Framework | Spring Boot 3.2.x |
| Java | 17 |
| Database | MongoDB |
| API docs | Swagger UI tại `http://localhost:8080/swagger-ui/index.html` |
| Base URL | `http://localhost:8080/api/` |

---

## 2. Các chức năng tổng quát (General Features)

Hệ thống bao gồm các phân hệ chức năng cốt lõi sau:

### Phân hệ Xác thực & Bảo mật (Authentication & Security)

- **Xác thực JWT (JSON Web Token):** Đăng nhập, đăng ký tài khoản cục bộ, cơ chế cấp phát và làm mới token (`RefreshToken`) an toàn.
- **Tích hợp Google OAuth2:** Hỗ trợ đăng nhập nhanh bằng tài khoản Google thông qua tầng trao đổi mã xác thực API (`GoogleLoginRequest`).
- **Phân quyền người dùng:** Quản lý quyền truy cập nghiêm ngặt dựa trên vai trò (`Role`: ADMIN, USER) qua cấu hình bảo mật `SecurityConfig`.

### Phân hệ Tra cứu Từ điển (Dictionary Core Services)

- **Tích hợp Multi-Client bên ngoài:**
  - `FreeDictionaryClient`: Đồng bộ định nghĩa, từ loại, ví dụ từ Free Dictionary API công khai.
  - `MerriamWebsterClient`: Kết nối tới Merriam-Webster Dictionary & Thesaurus API nhằm lấy thông tin tra cứu chuyên sâu, từ đồng nghĩa/trái nghĩa (`MWThesaurusEntry`), biến thể từ loại (`MWInflection`) và file âm thanh phát âm chuẩn (`MWSound`).
- **Merge & fallback:** `FindWordServiceImpl` gộp dữ liệu từ nhiều nguồn, ưu tiên phonetic/audio từ Free Dictionary và fallback sang Merriam-Webster (kể cả dạng chia động từ trong `ins[]`).

### Phân hệ Tìm kiếm Ngữ nghĩa (AI Semantic Search)

- **Vector Embedding:** Sử dụng `EmbeddingService` phối hợp cùng mô hình nhúng ngôn ngữ để chuyển hóa các định nghĩa, ngữ cảnh của từ vựng thành chuỗi vector số học.
- **Semantic Search:** Lưu trữ chuỗi vector vào MongoDB (`EmbedWordVector`) và thực hiện tính toán độ tương đồng cosine (Cosine Similarity) để tìm kiếm từ vựng theo ý nghĩa hoặc ngữ cảnh gần đúng.
- **Lưu ý:** Phần FE chưa triển khai do vấn đề thời gian; backend vẫn có sẵn để học tập và thử nghiệm. Có thể tắt bằng profile `no_ai` (xem mục Cài đặt).

### Quản lý Gói Từ vựng & Thẻ (Package & Tag Management)

- **Vocabulary Packages:** Cho phép tạo lập các gói từ vựng theo chủ đề (ví dụ: TOEIC, IELTS, Oxford 3000) qua `PackageService`.
- **Package Details:** Quản lý danh sách chi tiết các từ nằm trong từng gói từ vựng, hỗ trợ thêm, sửa, xóa để đồng bộ xuống ứng dụng client.
- **Word Tagging:** Phân loại từ vựng bằng nhãn (`Tag`) tùy biến, thiết lập mối quan hệ nhiều-nhiều qua `WordTagRelation`, giúp người dùng lọc và quản lý lộ trình học tập.

### Lịch sử Game Flashcard (Game Log)

- **Game Sessions:** Lưu từng lần chơi flashcard của user vào collection `Game Sessions` trên MongoDB.
- **API:** `POST/GET/DELETE /api/game-logs/sessions`, `GET /api/game-logs/summary` — đồng bộ lịch sử game từ WPF client thay cho file JSON local.
- **Thống kê:** Tổng số game, tổng thẻ đã học, độ chính xác trung bình, tổng thời gian học — tính bằng MongoDB aggregation.

---

## 3. Kiến trúc hệ thống & Quy chuẩn viết code (Architecture & Coding Conventions)

Dự án áp dụng mô hình **Layered Architecture** (Kiến trúc phân tầng) kết hợp các nguyên tắc thiết kế hướng miền:

### Cấu trúc thư mục & Phân lớp mã nguồn

Mã nguồn được tổ chức thành các gói (`package`) có trách nhiệm đơn nhất:

| Package | Trách nhiệm |
|---------|-------------|
| `client/` | Gọi API bên ngoài (Free Dictionary, Merriam-Webster) |
| `config/` | Cấu hình hệ thống (Security, OpenAPI/Swagger, Logging Filter) |
| `controller/` | Tiếp nhận HTTP request từ client C# WPF |
| `dto/` | Data Transfer Objects; chia nhỏ theo domain (`dto/auth/`, `dto/gamelog/`, ...) |
| `entity/` | Ánh xạ MongoDB (`@Document`); thư mục con theo nguồn dữ liệu (`entity/free/`, `entity/merriam/`) |
| `exception/` | Xử lý lỗi tập trung qua `GlobalExceptionHandler` |
| `mapper/` | Chuyển đổi Entity ↔ DTO (`PackageMapper`, `PackageDetailMapper`) |
| `repository/` | Giao tiếp MongoDB qua Spring Data (`MongoRepository`) |
| `security/` | JWT filter, `CustomUserDetailsService` |
| `service/` | Business logic; Interface + Implementation (`PackageService` / `PackageServiceImpl`) |
| `util/` | Enum và tiện ích (`Role`, `TokenType`, `AuthProvider`) |

### Quy chuẩn đặt tên (Naming Conventions)

- **Class & Interface:** `PascalCase`, hậu tố theo tầng: `...Controller`, `...Service`, `...ServiceImpl`, `...Repository`, `...Mapper`, `...Config`.
- **Dữ liệu API:** Phân biệt DTO chung (`WordDetailDto`) và DTO endpoint (`RegisterRequest`, `SaveGameSessionRequest`).
- **Test classes:** Đặt trong `src/test/java/`, phản chiếu cấu trúc `src/main/java/`, hậu tố `Test`.

---

## 4. Cài đặt & Chạy dự án (Installation & Setup)

### 4.1. Yêu cầu hệ thống

| Công cụ | Phiên bản khuyến nghị |
|---------|----------------------|
| Java JDK | **17** (bắt buộc; không dùng Java 21+) |
| Maven | 3.8+ (hoặc dùng `./mvnw` có sẵn trong repo) |
| MongoDB | Atlas hoặc instance local có thể kết nối qua URI |
| Docker | Tùy chọn — dùng để chạy container |
| Git | Clone repository |

### 4.2. Cấu hình biến môi trường

File `.env` chứa toàn bộ key và secret cần thiết để chạy dự án. **Không có sẵn trong repository** — vui lòng **liên hệ người phát triển** để được cấp file `.env` hoặc bộ key tương ứng.

Sau khi nhận được, đặt file `.env` tại thư mục gốc project (tuyệt đối không commit lên Git).

Các biến môi trường cần có:

| Nhóm | Biến |
|------|------|
| MongoDB | `MONGODB_URI`, `MONGODB_DBNAME` |
| Merriam-Webster | `MERRIAM_DICT_KEY`, `MERRIAM_THESAURUS_KEY`, `MERRIAM_DICT_URI`, `MERRIAM_THESAURUS_URI` |
| Free Dictionary | `FREE_DICTIONARY_URI` |
| JWT | `JWT_ACCESS_KEY`, `JWT_REFRESH_KEY`, `JWT_ACCESS_EXPIRATION`, `JWT_REFRESH_EXPIRATION` |
| Google OAuth2 | `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`, `GOOGLE_REDIRECT_URI` |

> Tất cả giá trị nhạy cảm được nạp qua `${VARIABLE_NAME}` trong `application.properties`, không hardcode trong mã nguồn.

### 4.3. Chạy bằng Docker (khuyến nghị)

Cách nhanh nhất để chạy toàn bộ backend kèm model AI embedding:

```bash
docker compose up --build
```

- Ứng dụng chạy tại: **http://localhost:8080**
- Swagger UI: **http://localhost:8080/swagger-ui/index.html**
- Lần build đầu sẽ **tải model HuggingFace** (`all-MiniLM-L6-v2`) — có thể mất vài phút.

Chi tiết build/deploy container: xem [README.Docker.md](./README.Docker.md).

### 4.4. Chạy local bằng Maven (dev)

**Bước 1 — Clone & vào thư mục project:**

```bash
git clone <repository-url>
cd Royal_Blueberry
```

**Bước 2 — Nhận file `.env` từ người phát triển** và đặt vào thư mục gốc project (xem mục 4.2).

**Bước 3 — Export biến môi trường** (Windows PowerShell):

```powershell
Get-Content .env | ForEach-Object {
  if ($_ -match '^\s*([^#][^=]+)=(.*)$') {
    [System.Environment]::SetEnvironmentVariable($matches[1].Trim(), $matches[2].Trim(), 'Process')
  }
}
```

Linux/macOS:

```bash
set -a && source .env && set +a
```

**Bước 4 — Chạy ứng dụng:**

```bash
# Windows
.\mvnw.cmd spring-boot:run

# Linux/macOS
./mvnw spring-boot:run
```

**Chạy không cần AI embedding** (bỏ qua tải model, tắt semantic search):

```bash
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=no_ai
```

**Chạy tests:**

```bash
.\mvnw.cmd test -Dspring.profiles.active=no_ai
```

### 4.5. Kiểm tra sau khi chạy

| Kiểm tra | URL / Lệnh |
|----------|------------|
| Tra cứu từ (public) | `GET http://localhost:8080/api/searching/get-detail/hello` |
| Swagger | `http://localhost:8080/swagger-ui/index.html` |
| Đăng ký tài khoản | `POST http://localhost:8080/api/auth/register` |
| Health (log startup) | Console hiển thị `Started RoyalBlueberryApplication` |

### 4.6. Lưu ý khi phát triển

- **Java 17:** Project build với Java 17. Nếu máy đang dùng Java 21/25, có thể gặp lỗi compile Lombok — hãy chỉnh `JAVA_HOME` về JDK 17.
- **Embedding service:** Nếu không dùng semantic search, chạy profile `no_ai` thay vì comment code thủ công.
- **MongoDB SSL:** `application.properties` bật `spring.data.mongodb.ssl.enabled=true` — cần URI Atlas hoặc cấu hình SSL phù hợp.

---

## 5. Hạ tầng & Triển khai (Infrastructure & Deployment)

- **Containerization:** Dockerfile multi-stage (tải model AI → build Maven → runtime JRE 17). Orchestration qua `compose.yaml`.
- **Quản lý môi trường:** JWT secret, API keys Merriam-Webster, MongoDB credentials **không** hardcode — chỉ qua biến môi trường hoặc `.env`.
- **Database:** MongoDB (cloud Atlas hoặc self-hosted); auto-index creation được bật cho các collection như `Game Sessions`, `Tags`.

---

## 6. Tóm tắt API chính

| Nhóm | Base path | Auth |
|------|-----------|------|
| Auth | `/api/auth` | Một số endpoint public |
| Tra cứu từ | `/api/searching` | Public (`GET`) |
| Semantic search | `/api/searching/semantic` | Public (`GET`); embed cần auth |
| Packages | `/api/packages` | Bearer token |
| Package details | `/api/packages/details` | Bearer token |
| Tags & Relations | `/api/tags`, `/api/relations` | Bearer token |
| Game logs | `/api/game-logs` | Bearer token |

---

Good luck!

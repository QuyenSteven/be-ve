# Bé vẽ 1.0

Ứng dụng Android 5.0 trở lên, không cần Internet. Chọn màu và cỡ nét rồi nhấn **Bắt đầu vẽ**; nếu Android hỏi ghim màn hình, chọn ghim để hạn chế thoát ứng dụng. Bé dùng ngón tay vẽ. Người lớn giữ đồng thời nút tăng và giảm âm lượng để mở bảng điều khiển; có thể xóa tranh sau khi xác nhận.

Cần thử trên chính máy tính bảng: mỗi hãng có thể xử lý tổ hợp phím hoặc chế độ ghim khác nhau. Không có ứng dụng thông thường nào khóa được nút nguồn và mọi cử chỉ hệ thống trên tất cả máy. Nếu không đồng ý ghim màn hình, ứng dụng chỉ ẩn thanh hệ thống và bé vẫn có thể thoát bằng cử chỉ. Hãy bật tính năng ghim ứng dụng trong cài đặt Android nếu máy yêu cầu.

Tranh chỉ tồn tại trong phiên chạy; đóng ứng dụng hoặc hệ điều hành thu hồi ứng dụng có thể làm mất tranh. Không có chức năng lưu tranh.

[Tải ứng dụng Bé vẽ 1.0](tai-ve/Be-ve-1.0.apk) (chọn Download raw file trên GitHub). Sao chép sang tablet và cài đặt, có thể cần cho phép cài ứng dụng từ nguồn này. Đây là bản ký debug.

Build trên Windows với JDK 17, Gradle 8.13 và Android SDK 36: `gradle --no-daemon --max-workers=2 testDebugUnitTest assembleDebug lintDebug`.



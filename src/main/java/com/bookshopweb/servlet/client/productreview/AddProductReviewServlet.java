package com.bookshopweb.servlet.client.productreview;

import com.bookshopweb.beans.ProductReview;
import com.bookshopweb.service.ProductReviewService;
import com.bookshopweb.utils.Protector;
import com.bookshopweb.utils.Validator;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@WebServlet(name = "AddProductReviewServlet", value = "/addProductReview")
public class AddProductReviewServlet extends HttpServlet {
    // Khởi tạo ProductReviewService để xử lý logic liên quan đến đánh giá sản phẩm.
    private final ProductReviewService productReviewService = new ProductReviewService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Không xử lý yêu cầu GET trong servlet này.
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Lưu các giá trị đầu vào từ biểu mẫu vào một Map.
        Map<String, String> values = new HashMap<>();
        values.put("userId", request.getParameter("userId"));
        values.put("productId", request.getParameter("productId"));
        values.put("ratingScore", request.getParameter("ratingScore"));
        values.put("content", request.getParameter("content"));

        // Xác thực đầu vào và lưu các lỗi vi phạm (violations) vào một Map.
        Map<String, List<String>> violations = new HashMap<>();
        violations.put("ratingScoreViolations", Validator.of(values.get("ratingScore"))
                .isNotNull() // Kiểm tra không null.
                .toList());
        violations.put("contentViolations", Validator.of(values.get("content"))
                .isNotNullAndEmpty() // Kiểm tra không null hoặc rỗng.
                .isAtLeastOfLength(10) // Kiểm tra độ dài ít nhất là 10 ký tự.
                .toList());

        // Tổng số lỗi vi phạm.
        int sumOfViolations = violations.values().stream().mapToInt(List::size).sum();

        // Các thông báo thành công và lỗi.
        String successMessage = "Đã đánh giá thành công!";
        String errorAddReviewMessage = "Đã có lỗi truy vấn!";

        // Biến để xác định vị trí cuộn sau khi xử lý.
        AtomicReference<String> anchor = new AtomicReference<>("");

        // Nếu không có lỗi vi phạm, tiến hành thêm đánh giá.
        if (sumOfViolations == 0) {
            ProductReview productReview = new ProductReview(
                    0L, // ID tự động tăng.
                    Protector.of(() -> Long.parseLong(values.get("userId"))).get(0L), // Lấy userId.
                    Protector.of(() -> Long.parseLong(values.get("productId"))).get(0L), // Lấy productId.
                    Protector.of(() -> Integer.parseInt(values.get("ratingScore"))).get(0), // Lấy điểm đánh giá.
                    values.get("content"), // Nội dung đánh giá.
                    1, // Trạng thái đánh giá (có thể là hoạt động).
                    LocalDateTime.now(), // Thời gian hiện tại.
                    null // Thời gian cập nhật (null khi mới tạo).
            );

            // Gọi phương thức insert từ ProductReviewService để thêm đánh giá vào cơ sở dữ liệu.
            Protector.of(() -> productReviewService.insert(productReview))
                    .done(r -> {
                        // Thành công: đặt thông báo thành công và cuộn đến khu vực đánh giá.
                        request.getSession().setAttribute("successMessage", successMessage);
                        anchor.set("#review");
                    })
                    .fail(e -> {
                        // Thất bại: lưu các giá trị nhập vào và thông báo lỗi vào session, cuộn đến biểu mẫu.
                        request.getSession().setAttribute("values", values);
                        request.getSession().setAttribute("errorAddReviewMessage", errorAddReviewMessage);
                        anchor.set("#review-form");
                    });
        } else {
            // Nếu có lỗi vi phạm:
            // Lưu các giá trị nhập vào và danh sách lỗi vào session, cuộn đến biểu mẫu.
            request.getSession().setAttribute("values", values);
            request.getSession().setAttribute("violations", violations);
            anchor.set("#review-form");
        }

        // Chuyển hướng về trang sản phẩm với vị trí cuộn phù hợp (đánh giá hoặc biểu mẫu).
        response.sendRedirect(request.getContextPath() + "/product?id=" + values.get("productId") + anchor);
    }
}


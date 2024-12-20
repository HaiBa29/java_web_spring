package com.bookshopweb.servlet.client.productreview;

import com.bookshopweb.service.ProductReviewService;
import com.bookshopweb.utils.Protector;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "DeleteProductReviewServlet", value = "/deleteProductReview")
// Đánh dấu servlet với URL mapping "/deleteProductReview" để xử lý yêu cầu xóa đánh giá sản phẩm.
public class DeleteProductReviewServlet extends HttpServlet {
    // Khởi tạo ProductReviewService để xử lý logic liên quan đến đánh giá sản phẩm.
    private final ProductReviewService productReviewService = new ProductReviewService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Không xử lý yêu cầu GET trong servlet này.
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Lấy ID của đánh giá cần xóa từ tham số yêu cầu, nếu lỗi mặc định là 0L.
        long productReviewId = Protector.of(() -> Long.parseLong(request.getParameter("productReviewId"))).get(0L);

        // Lấy ID sản phẩm từ tham số yêu cầu để chuyển hướng sau khi xóa.
        String productId = request.getParameter("productId");

        // Thông báo thành công và lỗi.
        String successMessage = "Đã xóa đánh giá thành công!";
        String errorDeleteReviewMessage = "Đã có lỗi truy vấn!";

        // Gọi phương thức xóa đánh giá từ ProductReviewService.
        Protector.of(() -> productReviewService.delete(productReviewId))
                .done(r -> {
                    // Thành công: đặt thông báo thành công vào session.
                    request.getSession().setAttribute("successMessage", successMessage);
                })
                .fail(e -> {
                    // Thất bại: đặt thông báo lỗi vào session.
                    request.getSession().setAttribute("errorDeleteReviewMessage", errorDeleteReviewMessage);
                });

        // Chuyển hướng về trang sản phẩm sau khi xử lý, cuộn đến khu vực đánh giá.
        response.sendRedirect(request.getContextPath() + "/product?id=" + productId + "#review");
    }
}


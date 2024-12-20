package com.bookshopweb.servlet.admin.order;

import com.bookshopweb.service.OrderService;
import com.bookshopweb.utils.Protector;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "UpdateOrderServlet", value = "/admin/orderManager/update")
public class UpdateOrderServlet extends HttpServlet {
    // Khởi tạo OrderService để xử lý các thao tác liên quan đến đơn hàng.
    private final OrderService orderService = new OrderService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Không xử lý yêu cầu GET trong servlet này.
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Lấy ID của đơn hàng từ tham số yêu cầu, nếu lỗi mặc định là 0L.
        long id = Protector.of(() -> Long.parseLong(request.getParameter("id"))).get(0L);

        // Lấy hành động cần thực hiện (CONFIRM, CANCEL, RESET) từ tham số yêu cầu.
        String action = request.getParameter("action");

        // Thông báo lỗi mặc định.
        String errorMessage = "Đã có lỗi truy vấn!";

        // Xử lý yêu cầu xác nhận đơn hàng (CONFIRM).
        if ("CONFIRM".equals(action)) {
            // Thông báo thành công khi xác nhận đơn hàng.
            String successMessage = String.format("Đã xác nhận đã giao đơn hàng #%s thành công!", id);
            // Gọi phương thức confirm từ OrderService và xử lý kết quả.
            Protector.of(() -> orderService.confirm(id))
                    .done(r -> request.getSession().setAttribute("successMessage", successMessage)) // Thành công: đặt thông báo vào session.
                    .fail(e -> request.getSession().setAttribute("errorMessage", errorMessage));    // Thất bại: đặt lỗi vào session.
        }

        // Xử lý yêu cầu hủy đơn hàng (CANCEL).
        if ("CANCEL".equals(action)) {
            // Thông báo thành công khi hủy đơn hàng.
            String successMessage = String.format("Đã hủy đơn hàng #%s thành công!", id);
            // Gọi phương thức cancel từ OrderService và xử lý kết quả.
            Protector.of(() -> orderService.cancel(id))
                    .done(r -> request.getSession().setAttribute("successMessage", successMessage)) // Thành công: đặt thông báo vào session.
                    .fail(e -> request.getSession().setAttribute("errorMessage", errorMessage));    // Thất bại: đặt lỗi vào session.
        }

        // Xử lý yêu cầu đặt lại trạng thái đơn hàng (RESET).
        if ("RESET".equals(action)) {
            // Thông báo thành công khi đặt lại trạng thái đơn hàng.
            String successMessage = String.format("Đã đặt lại trạng thái là đang giao hàng cho đơn hàng #%s thành công!", id);
            // Gọi phương thức reset từ OrderService và xử lý kết quả.
            Protector.of(() -> orderService.reset(id))
                    .done(r -> request.getSession().setAttribute("successMessage", successMessage)) // Thành công: đặt thông báo vào session.
                    .fail(e -> request.getSession().setAttribute("errorMessage", errorMessage));    // Thất bại: đặt lỗi vào session.
        }

        // Chuyển hướng về trang quản lý đơn hàng sau khi xử lý xong.
        response.sendRedirect(request.getContextPath() + "/admin/orderManager");
    }
}


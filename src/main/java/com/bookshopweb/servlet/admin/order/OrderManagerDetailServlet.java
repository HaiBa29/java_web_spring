package com.bookshopweb.servlet.admin.order;

import com.bookshopweb.beans.Order;
import com.bookshopweb.beans.OrderItem;
import com.bookshopweb.service.OrderItemService;
import com.bookshopweb.service.OrderService;
import com.bookshopweb.service.ProductService;
import com.bookshopweb.service.UserService;
import com.bookshopweb.utils.Protector;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.bookshopweb.servlet.admin.order.OrderManagerServlet.calculateTotalPrice;

@WebServlet(name = "OrderManagerDetailServlet", value = "/admin/orderManager/detail")
public class OrderManagerDetailServlet extends HttpServlet {
    // Khởi tạo các dịch vụ cần thiết để xử lý yêu cầu.
    private final OrderService orderService = new OrderService();
    private final UserService userService = new UserService();
    private final OrderItemService orderItemService = new OrderItemService();
    private final ProductService productService = new ProductService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Lấy ID của đơn hàng từ tham số yêu cầu, nếu lỗi mặc định là 0L.
        long id = Protector.of(() -> Long.parseLong(request.getParameter("id"))).get(0L);

        // Lấy thông tin đơn hàng theo ID từ dịch vụ.
        Optional<Order> orderFromServer = Protector.of(() -> orderService.getById(id)).get(Optional::empty);

        if (orderFromServer.isPresent()) {
            // Nếu đơn hàng tồn tại:
            Order order = orderFromServer.get();

            // Lấy thông tin người dùng liên quan đến đơn hàng và set vào đơn hàng.
            Protector.of(() -> userService.getById(order.getUserId()))
                    .get(Optional::empty)
                    .ifPresent(order::setUser);

            // Lấy danh sách các mục (item) trong đơn hàng.
            List<OrderItem> orderItems = Protector.of(() -> orderItemService.getByOrderId(order.getId()))
                    .get(ArrayList::new);

            // Gắn thông tin sản phẩm vào từng mục trong đơn hàng.
            orderItems.forEach(orderItem -> Protector.of(() -> productService.getById(orderItem.getProductId()))
                    .get(Optional.empty())
                    .ifPresent(orderItem::setProduct));

            // Set danh sách các mục và tính tổng giá trị đơn hàng (bao gồm phí giao hàng).
            order.setOrderItems(orderItems);
            order.setTotalPrice(calculateTotalPrice(orderItems, order.getDeliveryPrice()));

            // Đặt đối tượng đơn hàng vào request và chuyển tiếp đến trang JSP để hiển thị chi tiết.
            request.setAttribute("order", order);
            request.getRequestDispatcher("/WEB-INF/views/orderManagerDetailView.jsp").forward(request, response);
        } else {
            // Nếu đơn hàng không tồn tại, chuyển hướng về trang quản lý đơn hàng.
            response.sendRedirect(request.getContextPath() + "/admin/orderManager");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Phương thức POST không được sử dụng trong servlet này.
    }

    // Hàm tính tổng giá trị đơn hàng, bao gồm giá của các mục và phí giao hàng.
    private double calculateTotalPrice(List<OrderItem> orderItems, double deliveryPrice) {
        return orderItems.stream().mapToDouble(item -> item.getQuantity() * item.getProduct().getPrice()).sum() + deliveryPrice;
    }
}

package com.bookshopweb.servlet.admin.category;

import com.bookshopweb.beans.Category;
import com.bookshopweb.service.CategoryService;
import com.bookshopweb.utils.Protector;
import com.bookshopweb.utils.TextUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

@WebServlet(name = "CategoryDetailServlet", value = "/admin/categoryManager/detail")

public class CategoryDetailServlet extends HttpServlet {

    // Tạo một instance của CategoryService để xử lý logic liên quan đến danh mục.
    private final CategoryService categoryService = new CategoryService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Lấy tham số "id" từ yêu cầu HTTP (GET). Nếu không có giá trị hợp lệ, sử dụng giá trị mặc định là 0L.
        long id = Protector.of(() -> Long.parseLong(request.getParameter("id"))).get(0L);

        // Gọi phương thức từ CategoryService để lấy thông tin danh mục theo ID.
        // Nếu xảy ra lỗi hoặc không có danh mục nào với ID đó, trả về Optional.empty().
        Optional<Category> categoryFromServer = Protector.of(() -> categoryService.getById(id)).get(Optional::empty);

        // Kiểm tra xem danh mục có tồn tại không.
        if (categoryFromServer.isPresent()) {
            // Nếu danh mục tồn tại, lấy thông tin từ Optional.
            Category category = categoryFromServer.get();

            // Chuyển đổi chuỗi mô tả của danh mục thành định dạng đoạn văn (paragraph).
            // Nếu mô tả null, sử dụng chuỗi rỗng.
            category.setDescription(TextUtils.toParagraph(Optional.ofNullable(category.getDescription()).orElse("")));

            // Đặt danh mục làm thuộc tính trong yêu cầu (request) để gửi đến giao diện.
            request.setAttribute("category", category);

            // Chuyển tiếp yêu cầu tới tệp JSP để hiển thị chi tiết danh mục.
            request.getRequestDispatcher("/WEB-INF/views/categoryDetailView.jsp").forward(request, response);
        } else {
            // Nếu danh mục không tồn tại, chuyển hướng người dùng về trang quản lý danh mục.
            response.sendRedirect(request.getContextPath() + "/admin/categoryManager");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Phương thức POST không được sử dụng trong servlet này.
        // Có thể để trống hoặc thêm logic xử lý khi cần trong tương lai.
    }
}


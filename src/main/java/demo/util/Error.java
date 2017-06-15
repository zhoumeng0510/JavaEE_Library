package demo.util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by zhoumeng on
 * 2017.6.15.
 * 下午 02:25.
 */
public class Error {
    public static void showError(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setAttribute("message", "Error.");
        response.sendRedirect("default.jsp");
    }
}

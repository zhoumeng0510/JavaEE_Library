package demo.servlet;

import demo.util.Db;
import demo.util.Error;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by zhoumeng on
 * 2017.6.16.
 * 下午 02:07.
 */
@WebServlet(urlPatterns = "/userBook")
public class UserBookAction extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");

        if ("borrowBook".equals(action)) {
            borrowBook(req, resp);
            return;
        }

        if ("returnBook".equals(action)) {
            returnBook(req, resp);
            return;
        }
        Error.showError(req, resp);
    }

    private void borrowBook(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        int userId = Integer.parseInt(req.getParameter("userId"));
        String[] bookIds = req.getParameterValues("bookIds");

        for (String bookIdString : bookIds) {
            int bookId = Integer.parseInt(bookIdString);
            if (canBorrow(bookId, req, resp)) {
                // DML TRANSACTION
                borrowBook(req, resp, userId, bookId);
            } else {
                // TODO: 6/16/17 通知用户那些书不能借
            }
        }
        resp.sendRedirect("index.jsp");
    }

    private boolean canBorrow(int bookId, HttpServletRequest req, HttpServletResponse resp) {
        Connection connection = Db.getConnection();
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        String sql = "SELECT * FROM javaee_library.book WHERE id = ?";

        try {
            if (connection != null) {
                preparedStatement = connection.prepareStatement(sql);
            } else {
                Error.showError(req, resp);
                return false;
            }
            preparedStatement.setInt(1, bookId);
            resultSet = preparedStatement.executeQuery();
            resultSet.next();
            int amount = resultSet.getInt("amount");
            if (amount > 0) {
                return true;
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void borrowBook(HttpServletRequest req, HttpServletResponse resp, int userId, int bookId) throws IOException {
        Connection connection = Db.getConnection();
        PreparedStatement preparedStatement = null;

        String sql = "INSERT INTO javaee_library.user_book(userId, bookId) VALUE (?, ?)";

        if (connection == null) {
            Error.showError(req, resp);
            return;
        }
        try {
            connection.setAutoCommit(false); // 1. 开启一次事务

            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, userId);
            preparedStatement.setInt(2, bookId);
            preparedStatement.executeUpdate(); // DML 1

            sql = "UPDATE javaee_library.book SET amount = amount - 1 WHERE id = ?";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, bookId);
            preparedStatement.executeUpdate(); // DML 2

            connection.commit(); // 2. commit
        } catch (Exception e) {
            e.printStackTrace();
            try {
                connection.rollback(); // 3. rollback
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        } finally {
            Db.close(null, preparedStatement, connection);
        }
    }

    private void returnBook(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        int userBookId = Integer.parseInt(req.getParameter("userBookId"));
        int bookId = Integer.parseInt(req.getParameter("bookId"));

        Connection connection = Db.getConnection();
        PreparedStatement preparedStatement = null;

        String sql = "UPDATE javaee_library.user_book SET returnTime = now() WHERE id = ?";

        try {
            if (connection != null) {
                preparedStatement = connection.prepareStatement(sql);
            } else {
                Error.showError(req, resp);
                return;
            }

            connection.setAutoCommit(false);

            preparedStatement.setInt(1, userBookId);
            preparedStatement.executeUpdate();

            sql = "UPDATE javaee_library.book SET amount = amount + 1 WHERE id = ?";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, bookId);
            preparedStatement.executeUpdate();

            connection.commit();

            resp.sendRedirect("index.jsp"); // // TODO: 6/16/17
        } catch (Exception e) {
            e.printStackTrace();
            try {
                connection.rollback();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        } finally {
            Db.close(null, preparedStatement, connection);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }
}

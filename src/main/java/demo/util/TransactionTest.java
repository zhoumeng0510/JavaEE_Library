package demo.util;

import java.sql.*;

/**
 * Created by zhoumeng on
 * 2017.6.16.
 * 下午 03:25.
 */
public class TransactionTest {
    public static void main(String[] args) {
        Connection connection = Db.getConnection();
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        String sql = "INSERT INTO javaee_library.user VALUE (NULL, 'u', 'p', 'r')";

        try {
            if (connection == null) {
                return;
            }
            connection.setAutoCommit(false); // 关闭自动提交；同时开启了一次事务
            preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.executeUpdate(); // DML 1 : INSERT


            resultSet = preparedStatement.getGeneratedKeys();
            resultSet.next();
            int id = resultSet.getInt(1);
            System.out.println("id: " + id);

            System.out.println(1/0);

            sql = "DELETE FROM javaee_library.user WHERE id = ?";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, id);
            preparedStatement.executeUpdate(); // DML 2 : DELETE

            connection.commit(); // 2. commit
        } catch (Exception e) {
            e.printStackTrace();
            try {
                connection.rollback(); // 3. rollback
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        } finally {
            Db.close(resultSet, preparedStatement, connection);
        }
    }
}

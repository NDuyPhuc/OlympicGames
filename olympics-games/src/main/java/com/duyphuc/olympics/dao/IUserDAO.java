package com.duyphuc.olympics.dao;

import com.duyphuc.olympics.model.User;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface IUserDAO {
    Optional<User> getUserByUsernameOptional(String username);
    boolean addUser(User user) throws SQLException; // Thêm throws SQLException nếu có thể
    boolean updateUserPassword(String username, String newHashedPassword) throws SQLException;
    List<User> getAllUsers() throws SQLException;
    boolean updateUser(User user) throws SQLException;
    boolean deleteUser(int userId) throws SQLException;

    // Giữ lại phương thức cũ nếu vẫn muốn sử dụng, nhưng nên chuyển dần sang Optional
    default User getUserByUsername(String username) {
        return getUserByUsernameOptional(username).orElse(null);
    }
}
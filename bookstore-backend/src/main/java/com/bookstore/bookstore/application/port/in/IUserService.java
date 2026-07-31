package com.bookstore.bookstore.application.port.in;

import com.bookstore.bookstore.application.command.DeleteUserCommand;
import com.bookstore.bookstore.application.command.CreateUserCommand;
import com.bookstore.bookstore.application.command.UpdateUserCommand;
import com.bookstore.bookstore.application.command.UpdateStaffUserCommand;
import com.bookstore.bookstore.application.command.UpdateUserLockCommand;
import com.bookstore.bookstore.application.query.PageQuery;
import com.bookstore.bookstore.application.result.PageSliceResult;
import com.bookstore.bookstore.domain.model.User;
import java.util.List;
import java.util.UUID;

public interface IUserService {

    List<User> getAll();

    List<User> getCustomers();

    PageSliceResult<User> getCustomers(PageQuery pageQuery);

    List<User> getStaffs();

    PageSliceResult<User> getStaffs(PageQuery pageQuery);

    List<User> getAdmins();

    PageSliceResult<User> getAdmins(PageQuery pageQuery);

    List<User> getShippers();

    PageSliceResult<User> getShippers(PageQuery pageQuery);

    List<User> getAllIncludingDeleted();

    User create(User user);

    User createByAdmin(CreateUserCommand command);

    User getById(UUID userId);

    User getByIdIncludingDeleted(UUID userId);

    User update(UpdateUserCommand command);

    User updateStaffByAdmin(UpdateStaffUserCommand command);

    User updateLockByAdmin(UpdateUserLockCommand command);

    void deleteByAdmin(DeleteUserCommand command);

    void delete(DeleteUserCommand command);
}

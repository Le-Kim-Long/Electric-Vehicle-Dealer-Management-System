import React, { useEffect, useState } from "react";
import "./UserManagement.css";
import { fetchAllUsers, searchUsers, searchUsersByRole, searchUsersByDealer } from "../../services/adminApi";
import { createUserAccount, fetchDealerNames, fetchRoleNames, updateUserAccount } from "../../services/adminApi";

const UserManagement = () => {
  const [users, setUsers] = useState([]);
  // State to track password visibility per userId
  const [visiblePasswords, setVisiblePasswords] = useState({});
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState("");
  const [searchRole, setSearchRole] = useState("");
  const [searchDealer, setSearchDealer] = useState("");
  const [searching, setSearching] = useState(false);

  // State cho form tạo tài khoản mới
  const [newUser, setNewUser] = useState({
    username: "",
    password: "",
    email: "",
    phoneNumber: "",
    roleName: "DealerStaff",
    dealerName: ""
  });
  const [creating, setCreating] = useState(false);
  const [createError, setCreateError] = useState("");
  const [dealerNames, setDealerNames] = useState([]);
  const [roleNames, setRoleNames] = useState([]);
  const [showCreateForm, setShowCreateForm] = useState(false);
  const [showUpdateForm, setShowUpdateForm] = useState(false);
  const [updateUser, setUpdateUser] = useState(null);
  const [updating, setUpdating] = useState(false);
  const [updateError, setUpdateError] = useState("");

  useEffect(() => {
    fetchAllUsers()
      .then((data) => {
        setUsers(data);
        setLoading(false);
      })
      .catch(() => setLoading(false));
    // Lấy danh sách tên đại lý
    fetchDealerNames()
      .then(setDealerNames)
      .catch(() => setDealerNames([]));
    // Lấy danh sách role
    fetchRoleNames()
      .then(setRoleNames)
      .catch(() => setRoleNames([]));
  }, []);

  // Search handler for text, role, and dealer
  const handleSearch = async (e) => {
    e.preventDefault();
    setSearching(true);
    try {
      // Build query string for API
      let queryString = '';
      const params = [];
      if (search.trim()) params.push(`keyword=${encodeURIComponent(search.trim())}`);
      if (searchRole) params.push(`role=${encodeURIComponent(searchRole)}`);
      if (searchDealer) params.push(`dealer=${encodeURIComponent(searchDealer)}`);
      if (params.length) queryString = '?' + params.join('&');
      // Call API directly using fetch
      const token = localStorage.getItem('token');
      const response = await fetch(`http://localhost:8080/api/admin/users/search${queryString}`, {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Accept': 'application/json'
        }
      });
      if (!response.ok) throw new Error('Failed to search users');
      const result = await response.json();
      setUsers(result);
    } catch {
      setUsers([]);
    }
    setSearching(false);
  };

  // Reset search filters and reload all users
  const handleResetSearch = async () => {
    setSearch("");
    setSearchRole("");
    setSearchDealer("");
    setSearching(true);
    try {
      const allUsers = await fetchAllUsers();
      setUsers(allUsers);
    } catch {
      setUsers([]);
    }
    setSearching(false);
  };

  // Xử lý tạo tài khoản mới
  const handleCreateUser = async (e) => {
    e.preventDefault();
    setCreating(true);
    setCreateError("");
    try {
      await createUserAccount(newUser);
      // Sau khi tạo thành công, load lại danh sách user
      const updatedUsers = await fetchAllUsers();
      setUsers(updatedUsers);
      // Reset form
      setNewUser({
        username: "",
        password: "",
        email: "",
        phoneNumber: "",
        roleName: "DealerStaff",
        dealerName: ""
      });
    } catch (err) {
      setCreateError("Tạo tài khoản thất bại. Vui lòng kiểm tra lại thông tin hoặc thử lại sau.");
    }
    setCreating(false);
  };

  // Hiển thị form cập nhật user
  const handleShowUpdateForm = (user) => {
    setUpdateUser({ ...user });
    setShowUpdateForm(true);
    setUpdateError("");
  };

  const handleUpdateUserChange = (field, value) => {
    setUpdateUser((prev) => ({ ...prev, [field]: value }));
  };

  // Xử lý cập nhật user
  const handleUpdateUserSubmit = async (e) => {
    e.preventDefault();
    setUpdating(true);
    setUpdateError("");
    try {
      await updateUserAccount(updateUser.userId, {
        username: updateUser.username,
        password: updateUser.password,
        email: updateUser.email,
        phoneNumber: updateUser.phoneNumber,
        roleName: updateUser.roleName,
        dealerName: updateUser.dealerName,
        status: updateUser.status
      });
      // Sau khi cập nhật thành công, load lại danh sách user
      const updatedUsers = await fetchAllUsers();
      setUsers(updatedUsers);
      setShowUpdateForm(false);
    } catch (err) {
      setUpdateError("Cập nhật tài khoản thất bại. Vui lòng kiểm tra lại thông tin hoặc thử lại sau.");
    }
    setUpdating(false);
  };

  // Toggle password visibility for a user
  const handleTogglePassword = (userId) => {
    setVisiblePasswords((prev) => ({
      ...prev,
      [userId]: !prev[userId]
    }));
  };

  return (
    <div className="user-management-container">
      <h2>Quản lý tài khoản</h2>
      <div className="search-create-row">
        <form onSubmit={handleSearch} className="search-form">
          <input
            type="text"
            placeholder="Tìm kiếm theo tên, email..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
          <select
            className="search-role-select min-width-140"
            value={searchRole}
            onChange={async e => {
              const value = e.target.value;
              setSearchRole(value);
              setSearching(true);
              try {
                let result = [];
                if (value) {
                  result = await searchUsersByRole(value);
                } else {
                  result = await fetchAllUsers();
                }
                setUsers(result);
              } catch {
                setUsers([]);
              }
              setSearching(false);
            }}
          >
            <option value="">Tất cả vai trò</option>
            {roleNames.map(role => (
              <option key={role.roleId} value={role.roleName}>{role.roleName}</option>
            ))}
          </select>
          <select
            className="search-dealer-select min-width-140"
            value={searchDealer}
            onChange={async e => {
              const value = e.target.value;
              setSearchDealer(value);
              setSearching(true);
              try {
                let result = [];
                if (value) {
                  result = await searchUsersByDealer(value);
                } else {
                  result = await fetchAllUsers();
                }
                setUsers(result);
              } catch {
                setUsers([]);
              }
              setSearching(false);
            }}
          >
            <option value="">Tất cả đại lý</option>
            {dealerNames.map(name => (
              <option key={name} value={name}>{name}</option>
            ))}
          </select>
          <button type="submit">
            Tìm kiếm
          </button>
          <button
            type="button"
            className="reset-search-btn"
            onClick={handleResetSearch}
          >
            Làm mới
          </button>
        </form>
        <button
          type="button"
          className="create-user-toggle-btn"
          onClick={() => setShowCreateForm((prev) => !prev)}
        >
          {showCreateForm ? "Đóng" : "Tạo tài khoản mới"}
        </button>
      </div>
      {showCreateForm && (
        <div className="user-modal-overlay" onClick={() => setShowCreateForm(false)}>
          <div className="create-user-modal" onClick={e => e.stopPropagation()}>
            <div className="create-user-modal-header">
              <h3>Tạo tài khoản mới</h3>
              <button className="create-user-modal-close" type="button" onClick={() => setShowCreateForm(false)}>&times;</button>
            </div>
            <form onSubmit={handleCreateUser} className="create-user-form" autoComplete="off">
              <div className="form-row">
                <div className="form-group">
                  <input type="text" required autoComplete="new-username" placeholder="Tên người dùng" value={newUser.username} onChange={e => setNewUser({ ...newUser, username: e.target.value })} />
                </div>
                <div className="form-group">
                  <input type="password" required autoComplete="new-password" placeholder="Mật khẩu" value={newUser.password} onChange={e => setNewUser({ ...newUser, password: e.target.value })} />
                </div>
                <div className="form-group">
                  <input type="email" required autoComplete="off" placeholder="Email" value={newUser.email} onChange={e => setNewUser({ ...newUser, email: e.target.value })} />
                </div>
              </div>
              <div className="form-row">
                <div className="form-group">
                  <input type="text" required autoComplete="off" placeholder="Số điện thoại" value={newUser.phoneNumber} onChange={e => setNewUser({ ...newUser, phoneNumber: e.target.value })} />
                </div>
                <div className="form-group">
                  <select required value={newUser.roleName} onChange={e => setNewUser({ ...newUser, roleName: e.target.value })}>
                    <option value="">Chọn vai trò...</option>
                    {roleNames.map(role => (
                      <option key={role.roleId} value={role.roleName}>{role.roleName}</option>
                    ))}
                  </select>
                </div>
                {(newUser.roleName === "DealerStaff" || newUser.roleName === "DealerManager") ? (
                  <div className="form-group">
                    <select
                      required
                      value={newUser.dealerName}
                      onChange={e => setNewUser({ ...newUser, dealerName: e.target.value })}
                    >
                      <option value="">Chọn đại lý...</option>
                      {dealerNames.map(name => (
                        <option key={name} value={name}>{name}</option>
                      ))}
                    </select>
                  </div>
                ) : null}
              </div>
              <button type="submit" disabled={creating} className="create-user-submit-btn">
                {creating ? "Đang tạo..." : "Tạo tài khoản"}
              </button>
              {createError && <div className="error-message">{createError}</div>}
            </form>
          </div>
        </div>
      )}

      {/* Modal cập nhật user */}
      {showUpdateForm && updateUser && (
        <div className="user-modal-overlay" onClick={() => setShowUpdateForm(false)}>
          <div className="create-user-modal" onClick={e => e.stopPropagation()}>
            <div className="create-user-modal-header">
              <h3>Cập nhật tài khoản</h3>
              <button className="create-user-modal-close" type="button" onClick={() => setShowUpdateForm(false)}>&times;</button>
            </div>
            <form onSubmit={handleUpdateUserSubmit} className="create-user-form" autoComplete="off">
              <div className="form-row">
                <div className="form-group">
                  <input type="text" required placeholder="Tên người dùng" value={updateUser.username} onChange={e => handleUpdateUserChange("username", e.target.value)} />
                </div>
                <div className="form-group">
                  <input type="password" required placeholder="Mật khẩu" value={updateUser.password} onChange={e => handleUpdateUserChange("password", e.target.value)} />
                </div>
                <div className="form-group">
                  <input type="email" required placeholder="Email" value={updateUser.email} onChange={e => handleUpdateUserChange("email", e.target.value)} />
                </div>
              </div>
              <div className="form-row">
                <div className="form-group">
                  <input type="text" required placeholder="Số điện thoại" value={updateUser.phoneNumber} onChange={e => handleUpdateUserChange("phoneNumber", e.target.value)} />
                </div>
                <div className="form-group">
                  <select required value={updateUser.roleName} onChange={e => handleUpdateUserChange("roleName", e.target.value)}>
                    <option value="">Chọn vai trò...</option>
                    {roleNames.map(role => (
                      <option key={role.roleId} value={role.roleName}>{role.roleName}</option>
                    ))}
                  </select>
                </div>
                {(updateUser.roleName === "DealerStaff" || updateUser.roleName === "DealerManager") ? (
                  <div className="form-group">
                    <select
                      required
                      value={updateUser.dealerName}
                      onChange={e => handleUpdateUserChange("dealerName", e.target.value)}
                    >
                      <option value="">Chọn đại lý...</option>
                      {dealerNames.map(name => (
                        <option key={name} value={name}>{name}</option>
                      ))}
                    </select>
                  </div>
                ) : null}
                <div className="form-group">
                  <select required value={updateUser.status} onChange={e => handleUpdateUserChange("status", e.target.value)}>
                    <option value="Active">Active</option>
                    <option value="Inactive">Inactive</option>
                  </select>
                </div>
              </div>
              <button type="submit" disabled={updating} className="create-user-submit-btn">
                {updating ? "Đang cập nhật..." : "Cập nhật"}
              </button>
              {updateError && <div className="error-message">{updateError}</div>}
            </form>
          </div>
        </div>
      )}

      {(loading || searching) ? (
        <p>Đang tải dữ liệu...</p>
      ) : (
        <table className="user-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Tên người dùng</th>
              <th>Email</th>
              <th>Mật khẩu</th>
              <th>Số điện thoại</th>
              <th>Vai trò</th>
              <th>Đại lý</th>
              <th>Trạng thái</th>
              <th>Ngày tạo</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {users.length === 0 ? (
              <tr><td colSpan={10} style={{ textAlign: "center" }}>Không có dữ liệu</td></tr>
            ) : (
              users.map((user) => (
                <tr key={user.userId}>
                  <td>{user.userId}</td>
                  <td>{user.username}</td>
                  <td>{user.email}</td>
                  <td>
                    {user.password ? (
                      <span>
                        {visiblePasswords[user.userId] ? user.password : "••••••••"}
                        <button
                          type="button"
                          className="toggle-password-btn"
                          onClick={() => handleTogglePassword(user.userId)}
                        >
                          {visiblePasswords[user.userId] ? "Ẩn" : "Hiện"}
                        </button>
                      </span>
                    ) : ""}
                  </td>
                  <td>{user.phoneNumber}</td>
                  <td>{user.roleName}</td>
                  <td>{user.dealerName || "-"}</td>
                  <td>{user.status}</td>
                  <td>{new Date(user.createdDate).toLocaleString()}</td>
                  <td>
                    <button type="button" className="update-user-btn" onClick={() => handleShowUpdateForm(user)}>Cập nhật</button>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      )}
    </div>
  );
};

export default UserManagement;
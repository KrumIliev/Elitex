package tma.elitex.utils;

import java.util.Set;

/**
 * Holder class for user information
 *
 * Created by Krum Iliev.
 */
public class User {

    public int mUserId;
    public String mUserName;
    public int mDepartmentId;
    public String mDepartmentName;
    public String mDepartmentKind;
    public Set<String> mRoles;
    public boolean mKeepLogged;

    public User(int userId, String userName, int departmentId, String departmentName, String departmentKind, Set<String> roles, boolean keepLogged) {
        this.mUserId = userId;
        this.mUserName = userName;
        this.mDepartmentId = departmentId;
        this.mDepartmentName = departmentName;
        this.mDepartmentKind = departmentKind;
        this.mRoles = roles;
        this.mKeepLogged = keepLogged;
    }
}

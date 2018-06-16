package com.coder520.common.security;

import com.coder520.user.entity.Permission;
import com.coder520.user.entity.Role;
import com.coder520.user.entity.User;
import com.coder520.user.service.UserService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;

public class MyRealm extends AuthorizingRealm {

    @Autowired
    private UserService userService;
//    授权
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        String username= (String) principalCollection.getPrimaryPrincipal();
        User user=userService.findUserByUserName(username);
        SimpleAuthorizationInfo authorizationInfo=new SimpleAuthorizationInfo();
        for (Role role:user.getRoleList()){
            if(role.getRole()!=null&&!role.getRole().equals("")) {
                authorizationInfo.addRole(role.getRole());
            }
            for (Permission permission:role.getPermissionList()){
                if (permission.getPermission()!=null&&!permission.getPermission().equals(""))
                {
                    authorizationInfo.addStringPermission(permission.getPermission());
                }
            }
        }
        return authorizationInfo;
    }
//认证 登录
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        UsernamePasswordToken usernamePasswordToken= (UsernamePasswordToken) authenticationToken;
        String username=usernamePasswordToken.getUsername();
        User user=userService.findUserByUserName(username);
        if (user==null) {
            return null;
        }else {
            AuthenticationInfo info=new SimpleAuthenticationInfo(user.getUsername(),user.getPassword(),getName());
            SecurityUtils.getSubject().getSession().setAttribute("userinfo",user);
            return info;
        }
    }
}

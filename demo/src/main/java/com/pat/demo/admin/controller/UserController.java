package com.pat.demo.admin.controller;

import com.artsail.admin.model.domain.Query.UserQuery;
import com.artsail.admin.model.domain.User;
import com.artsail.admin.model.domain.VO.UserRegisterVO;
import com.artsail.admin.model.domain.VO.UserVO;
import com.artsail.admin.model.domain.request.UserLoginDTO;
import com.artsail.admin.model.domain.request.UserRegisterDTO;
import com.artsail.common.domain.LoginResult;
import com.artsail.common.domain.Result;
import com.artsail.admin.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static com.artsail.admin.contant.UserConstant.ADMIN_ROLE;
import static com.artsail.admin.contant.UserConstant.USER_LOGIN_STATE;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userservice;

    @GetMapping("/currentUser")
    public Result<UserVO> getCurrentUser(HttpServletRequest request){
        User currentUser = (User) request.getSession().getAttribute(USER_LOGIN_STATE);


        if (currentUser == null){
            return Result.error("未登录");
        }
        UserVO safetyUser = convertToVO(currentUser);
        return Result.success(safetyUser);
    }

    @RequestMapping("/register/account")
    public Result<UserRegisterVO> userRegister(@RequestBody UserRegisterDTO userRegisterDTO){
        if (userRegisterDTO == null){
            return Result.error("请求参数不能为空");
        }
        String userAccount = userRegisterDTO.getUserAccount();
        String userPassword = userRegisterDTO.getUserPassword();
        String checkPassword = userRegisterDTO.getCheckPassword();

        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)){
            return Result.error("参数不能为空");
        }

        Long userId = userservice.userRegister(userAccount, userPassword, checkPassword);
        if (userId < 0){
            return Result.error("注册失败");
        }
        UserRegisterVO userVO = new UserRegisterVO(userId);
        return  Result.success(userVO);

    }

    @PostMapping("/login/account")
    public Result<LoginResult> userLogin(@RequestBody UserLoginDTO userLoginDTO, HttpServletRequest re){

        if (userLoginDTO == null) {
            return Result.error("请求参数不能为空");
        }
        String userAccount = userLoginDTO.getUserAccount();
        String userPassword = userLoginDTO.getUserPassword();


        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            return Result.error("用户名或密码不能为空");
        }



        User user = userservice.userLogin(userAccount, userPassword,re);
        UserVO userVO = convertToVO(user);
        String role = userVO.getUserRole() == ADMIN_ROLE ? "admin" : "user";


        LoginResult loginResult = LoginResult.success(userVO, role);

        // 3. 直接返回成功
        return Result.success(loginResult);


    }



    @GetMapping("/search")
    public Result<List<User>> searchUsers(UserQuery userQuery, HttpServletRequest Req){
        // 检查管理员权限
        // TODO: 测试完成后取消注释
         // if (!isAdmin(Req)){
        //     return Result.error("无权限");
        // }

        List<User> users = userservice.getUserLists(userQuery);

        return Result.success(users);

    }

    @PostMapping("/delete")
    public Result<Boolean> delete(@RequestBody Long id, HttpServletRequest request){
        if(!isAdmin(request)){
            return Result.error("无权限");
        }
        if(id<=0){
            return Result.error("ID无效");
        }
        boolean ok = userservice.removeById(id);
        return ok ? Result.success(true) : Result.error("删除失败");
    }

    @PostMapping("/deletes")
    public Result<Boolean> deletes(@RequestBody List<Long> ids, HttpServletRequest request){
        if(!isAdmin(request)){
            return Result.error("无权限");
        }
        if(ids.size() <= 0){
            return Result.error("ID列表为空");
        }
        boolean ok = userservice.removeByIds(ids);
        return ok ? Result.success(true) : Result.error("删除失败");
    }

@PostMapping("/logout")
    public Result<String> userLogout(HttpServletRequest request){

        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return Result.success("注销成功");
    }


    @PutMapping("/role")
    public Result<String> updateUserRole(@RequestBody Map<String, Object> body) {
        Long id = body.get("id") != null ? Long.valueOf(body.get("id").toString()) : null;
        Integer userRole = body.get("userRole") != null ? Integer.valueOf(body.get("userRole").toString()) : null;
        if (id == null || userRole == null) {
            return Result.error(400, "参数不能为空");
        }
        User user = userservice.getById(id);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }
        user.setUserRole(userRole);
        userservice.updateById(user);
        return Result.success("角色更新成功");
    }

    private UserVO convertToVO (User user){
        UserVO userVO = new UserVO();
        userVO.setId(user.getId());
        userVO.setUserRole(user.getUserRole());
        userVO.setUserName(user.getUserName());
        userVO.setUserAccount(user.getUserAccount());
        userVO.setAvatarUrl(user.getAvatarUrl());
        userVO.setGender(user.getGender());
        userVO.setPhone(user.getPhone());
        userVO.setEmail(user.getEmail());
        return userVO;

    }

    private boolean isAdmin(HttpServletRequest request) {

        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        if (userObj instanceof User user) {
            return user.getUserRole() == ADMIN_ROLE;
        }
        return false;
    }

}

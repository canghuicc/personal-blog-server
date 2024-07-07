package com.blog.web.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.blog.web.config.Result;
import com.blog.web.entity.User;
import com.blog.web.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 用户表 前端控制器
 *
 * @author 苍晖
 * @since 2024-07-01
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 通过POST请求添加用户信息到数据库。
     *
     * @param user 包含待添加用户信息的请求体。
     * @return 如果用户添加成功，返回一个成功的Result对象；如果添加失败，返回一个错误的Result对象。
     * Result对象中不包含具体添加的用户信息。
     */
    @PostMapping("/adduser")
    public Result<User> saveUser(@RequestBody User user) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, user.getUsername());
        User user1 = userMapper.selectOne(wrapper);
        if (user1 != null) {
            // 如果存在相同的用户名，则返回错误信息
            return Result.error("用户名已存在");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        // 调用UserMapper的insert方法插入用户数据
        int rows = userMapper.insert(user);

        // 根据插入数据的行数判断操作是否成功
        if (rows > 0) {
            return Result.success("增加用户成功");
        } else {
            return Result.error("增加用户失败");
        }
    }

    /**
     * 通过DELETE请求删除指定ID的用户。
     *
     * @param userId 要删除的用户的ID，由路径参数提供。
     * @return 返回一个Result对象，其中包含删除操作的结果消息。
     * 如果删除成功（影响的行数大于0），消息为"删除成功"；
     * 如果删除失败（影响的行数为0），消息为"删除失败"。
     * 注意，此方法不返回删除的用户对象，因为操作的焦点是删除而非获取用户信息。
     */
    @DeleteMapping("/deleteuser/{userId}")
    public Result<User> removeUser(@PathVariable("userId") Integer userId) {
        int rows = userMapper.deleteById(userId);
        if (rows > 0) {
            return Result.success("删除成功");
        } else {
            return Result.error("删除失败");
        }
    }

    /**
     * 通过PUT方法更新用户信息。
     * 接收一个User对象作为请求体，其中包含了待更新的用户信息。
     * 如果请求体中的密码、电子邮件、头像路径或用户昵称非空，则更新相应的字段。
     * 用户角色如果非空，也会被更新。
     * 使用LambdaQueryWrapper构建查询条件，根据用户ID进行更新。
     * 返回更新后的用户信息。
     *
     * @param user 包含待更新用户信息的请求体。
     * @return 更新成功的用户信息。
     */
    @PutMapping("/updateuser")
    public Result<User> updateUser(@RequestBody User user) {
        // 如果密码非空，更新密码
        if (StringUtils.isNotBlank(user.getPassword())) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        // 如果电子邮件非空，更新电子邮件
        if (StringUtils.isNotBlank(user.getEmail())) {
            user.setEmail(user.getEmail());
        }
        // 如果头像路径非空，更新头像路径
        if (StringUtils.isNotBlank(user.getAvatarPath())) {
            user.setAvatarPath(user.getAvatarPath());
        }
        // 如果用户昵称非空，更新用户昵称
        if (StringUtils.isNotBlank(user.getUserNickname())) {
            user.setUserNickname(user.getUserNickname());
        }
        // 如果用户角色非空，更新用户角色
        if (user.getUserRole() != null) {
            user.setUserRole(user.getUserRole());
        }
        user.setUpdatedAt(LocalDateTime.now());

        // 更新用户信息
        // 根据非空属性更新记录
        int rows = userMapper.updateById(user);

        if (rows > 0) {
            return Result.success("更新成功");
        } else {
            return Result.error("更新失败");
        }
    }

    /**
     * 根据用户名或电子邮件获取用户信息。
     * <p>
     * 该接口支持通过用户名或电子邮件查询用户。如果同时提供了用户名和电子邮件，则返回匹配这两个条件的用户；
     * 如果只提供了用户名或电子邮件中的一个，则返回匹配该条件的用户。如果查询条件都不满足，则返回错误信息。
     *
     * @param username 用户名，可选参数。
     * @param email    电子邮件，可选参数。
     * @return 如果找到用户，则返回用户的详细信息；如果未找到用户，则返回错误信息。
     */
    @GetMapping("/getuser")
    public Result<User> getUser(@RequestParam(value = "username", required = false) String username, @RequestParam(value = "email", required = false) String email) {
        // 初始化查询条件
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

        // 根据提供的用户名和电子邮件构建查询条件
        if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(email)) {
            wrapper.eq(User::getUsername, username).eq(User::getEmail, email);
        } else if (StringUtils.isNotBlank(username)) {
            wrapper.eq(User::getUsername, username);
        } else if (StringUtils.isNotBlank(email)) {
            wrapper.eq(User::getEmail, email);
        }

        // 根据构建的查询条件尝试获取用户信息
        User user1 = userMapper.selectOne(wrapper);
        // 根据查询结果返回相应的响应
        if (user1 != null) {
            return Result.success(user1);
        } else {
            return Result.error("未找到用户");
        }
    }

    /**
     * 获取所有用户的信息。
     * <p>
     * 本接口不需要接收任何参数，调用成功后，返回所有用户的信息。
     *
     * @return Result<User> 返回一个结果对象，其中包含所有用户的信息。如果操作失败，结果对象中会包含相应的错误信息。
     */
    @GetMapping("/getalluser")
    public Result<List<User>> getAllUser() {
        List<User> users = userMapper.selectList(null);
        if (users != null) {
            return Result.success(users);
        } else {
            return Result.error("获取失败");
        }
    }

    /**
     * 用户登录接口
     * <p>
     * 通过接收POST请求来验证用户凭据，并返回登录令牌。
     *
     * @param user 包含登录凭据的用户对象，用户名和密码。
     * @return 如果凭据有效，返回包含登录令牌的映射；否则返回错误消息。
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody User user) {
        // 根据用户名查询数据库中的用户记录
        User user1 = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, user.getUsername()));

        Map<String, Object> map;
        // 验证用户凭据是否正确
        if (user1 != null && passwordEncoder.matches(user.getPassword(), user1.getPassword())) {
            // 生成唯一的登录令牌
            // 生成token
            String token = "user:" + UUID.randomUUID();
            map = new HashMap<>();
            map.put("token", token);

            // 从返回的用户对象中移除密码信息，以增强安全性
            user1.setPassword(null);
            redisTemplate.opsForValue().set("user_token:", token, 30, TimeUnit.MINUTES);
        } else {
            // 如果凭据无效，返回错误消息
            return Result.error("用户名或密码错误");
        }
        // 返回包含登录令牌的映射
        return Result.success(map);
    }

    /**
     * 管理员登录接口。
     * 通过接收用户名和密码，验证管理员身份，并返回登录令牌。
     *
     * @param user 包含用户名和密码的用户对象。
     * @return 如果登录成功，返回包含登录令牌的映射；如果登录失败，返回错误消息。
     */
    @PostMapping("/admin/login")
    public Result<Map<String, Object>> AdminLogin(@RequestBody User user) {
        // 根据用户名查询数据库中的用户记录
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, user.getUsername());
        User user1 = userMapper.selectOne(wrapper);

        Map<String, Object> map;
        // 验证用户是否存在，密码是否正确，并且用户角色是否为管理员
        if (user1 != null && passwordEncoder.matches(user.getPassword(), user1.getPassword()) && user1.getUserRole() == 1) {
            // 生成token
            String token = "AdminUser:" + UUID.randomUUID();
            map = new HashMap<>();
            map.put("token", token);

            // 从返回的用户对象中移除密码信息，以增强安全性
            user1.setPassword(null);
            // 将登录令牌存储在Redis中，有效期30分钟
            redisTemplate.opsForValue().set("AdminUser_token:", token, 30, TimeUnit.MINUTES);
        } else {
            // 如果凭据无效，返回错误消息
            return Result.error("用户名或密码错误");
        }
        // 返回包含登录令牌的映射
        return Result.success(map);
    }

    /**
     * 用户注销接口。
     * <p>
     * 通过删除Redis中对应的用户令牌，实现用户登出功能。
     * 用户登出时，不会涉及业务逻辑处理，主要是删除令牌，中断用户的会话。
     *
     * @param token 用户的令牌，用于唯一标识用户会话。令牌存储在客户端，通过请求头X-Token传递。
     * @return 返回登出结果，成功时返回"退出成功"。
     */
    @PostMapping("/logout")
    public Result<String> logout(@RequestHeader("X-Token") String token) {
        // 定义token前缀对应的Redis键
        String tokenKey;

        // 判断令牌前缀并分配对应的Redis键
        if (token.startsWith("user")) {
            tokenKey = "user_token";
        } else if (token.startsWith("AdminUser")) {
            tokenKey = "AdminUser_token";
        } else {
            return Result.error("token不正确");
        }
        // 从Redis中删除指定的用户令牌，实现登出功能。
        redisTemplate.delete(tokenKey);
        // 返回登出成功的信息。
        return Result.success("退出成功");
    }

    /**
     * 用户注册接口。
     * 通过接收@RequestBody注解的User对象，注册新用户。首先检查用户名是否已存在，并验证密码是否匹配，
     * 如果存在相同用户名和密码，则返回错误信息。如果不存在，则对用户信息进行处理（如加密密码）后插入数据库，
     * 并返回注册成功信息。
     *
     * @param user 用户信息对象，包含用户名、密码、邮箱和昵称等信息。
     * @return 如果注册成功，返回包含成功消息的结果对象；如果用户名已存在，返回包含错误消息的结果对象。
     */
    @PostMapping("/register")
    public Result<User> register(@RequestBody User user) {
        // 检查用户名是否已存在
        // 查询数据库中是否存在相同的用户名
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, user.getUsername());
        User user1 = userMapper.selectOne(wrapper);
        if (user1 != null) {
            // 如果存在相同的用户名，则返回错误信息
            return Result.error("用户名已存在");
        }

        // 设置新用户信息，并加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        // 获取邮箱和昵称字段存入queryWrapper
        user.setEmail(user.getEmail());
        user.setUserNickname(user.getUserNickname());
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        // 对用户信息进行插入前的处理，如设置创建时间等
//        timestampHandler.preprocessForInsert(user);
        // 插入用户信息到数据库
        userMapper.insert(user);
        // 注册成功，返回成功信息
        return Result.success("注册成功！");
    }

}


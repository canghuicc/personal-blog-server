package com.blog.web.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.blog.web.config.Result;
import com.blog.web.config.TimestampHandler;
import com.blog.web.entity.User;
import com.blog.web.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * <p>
 * 用户表 前端控制器
 * </p>
 *
 * @author canghui
 * @since 2024-05-05
 */
@Controller
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private TimestampHandler timestampHandler;

    @Autowired
    private UserMapper userMapper;

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
        // 在插入用户数据前，对用户信息执行时间戳处理
        // 在保存用户之前，处理时间戳
        timestampHandler.preprocessForInsert(user);

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
        // 如果密码非空，更新密码字段。这里存在重复赋值的问题，可能是一个笔误。
        // 如果密码非空，更新密码
        // 检查前端传来的非空字段并进行更新
        if (StringUtils.isNotBlank(user.getPassword())) {
            user.setPassword(user.getPassword());
        }
        // 如果电子邮件非空，更新电子邮件字段。
        // 如果电子邮件非空，更新电子邮件
        if (StringUtils.isNotBlank(user.getEmail())) {
            user.setEmail(user.getEmail());
        }
        // 如果头像路径非空，更新头像路径字段。
        // 如果头像路径非空，更新头像路径
        if (StringUtils.isNotBlank(user.getAvatarPath())) {
            user.setAvatarPath(user.getAvatarPath());
        }
        // 如果用户昵称非空，更新用户昵称字段。
        // 如果用户昵称非空，更新用户昵称
        if (StringUtils.isNotBlank(user.getUserNickname())) {
            user.setUserNickname(user.getUserNickname());
        }
        // 如果用户角色非空，更新用户角色字段。
        // 如果用户角色非空，更新用户角色
        if (user.getUserRole() != null) {
            user.setUserRole(user.getUserRole());
        }

        // 构建查询条件，指定更新用户时使用的条件为用户ID等于传入的用户ID。
        // 构建查询Wrapper，根据用户ID进行更新
        // 使用LambdaQueryWrapper更新记录，假设userId是主键
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUserId, user.getUserId());

        // 使用userMapper更新用户信息。这里没有处理更新返回值，通常情况下更新操作会返回受影响的行数。
        // 更新用户信息
        // 根据非空属性更新记录
        int rows = userMapper.update(user, wrapper);

        if (rows > 0) {
            return Result.success("更新成功");
        } else {
            return Result.error("更新失败");
        }
    }

    /**
     * 根据用户名和电子邮件获取用户信息。
     * <p>
     * 该接口支持通过用户名或电子邮件查询用户。如果同时提供了用户名和电子邮件，则返回匹配这两个条件的用户；
     * 如果只提供了用户名或电子邮件中的一个，则返回匹配该条件的用户。如果查询条件都不满足，则返回错误信息。
     *
     * @param username 用户名，可选参数。
     * @param email    电子邮件，可选参数。
     * @return 如果找到用户，则返回用户的详细信息；如果未找到用户，则返回错误信息。
     */
    @GetMapping("/getuser")
    public Result<User> getUser(
            @RequestParam(value = "username", required = false) String username,
            @RequestParam(value = "email", required = false) String email) {
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
    public Result<User> getAllUser() {
        List<User> users = userMapper.selectList(null);
        if (users != null) {
            return Result.success((User) users);
        } else {
            return Result.error("获取失败");
        }
    }

    /**
     * 用户登录接口
     *
     * @param user 包含用户名和密码的用户对象，通过RequestBody接收前端传来的JSON数据
     * @return 返回一个结果对象，如果登录成功，返回包含token的Map；如果登录失败，返回错误信息
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody User user) {
        // 根据传入的用户名和密码查询数据库中的用户信息
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, user.getUsername());
        queryWrapper.eq(User::getPassword, user.getPassword());
        User user1 = userMapper.selectOne(queryWrapper);

        Map<String, Object> map;
        // 判断用户信息是否查询成功，若成功则生成并返回token
        if (user1 != null) {
            // 生成token
            String token = "user:" + UUID.randomUUID();
            map = new HashMap<>();
            map.put("token", token);

            // 将用户信息（不含密码）与token绑定，存储到Redis中，有效期30分钟
            user1.setPassword(null);
//            redisTemplate.opsForValue().set(token, user1,30, TimeUnit.MINUTES);
        } else {
            // 登录失败，返回错误信息
            return Result.error("用户名或密码错误");
        }
        // 返回登录结果
        return Result.success(map);
    }

    /**
     * 用户注册接口
     *
     * @param user 用户信息对象，包含用户名、密码、邮箱和用户昵称
     * @return 返回操作结果，如果注册成功返回成功信息，如果用户名或邮箱已存在返回错误信息
     */
    @PostMapping("/register")
    public Result<User> register(@RequestBody User user) {
        // 查询数据库中是否存在相同的用户名和密码
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, user.getUsername());
        queryWrapper.eq(User::getPassword, user.getPassword());
        if (userMapper.selectOne(queryWrapper) != null) {
            // 如果存在相同的用户名和密码，则返回错误信息
            return Result.error("用户名已存在");
        }

        // 获取邮箱和昵称字段存入queryWrapper
        queryWrapper.eq(User::getEmail, user.getEmail());
        queryWrapper.eq(User::getUserNickname, user.getUserNickname());
        // 对用户信息进行插入前的处理，如设置创建时间等
        timestampHandler.preprocessForInsert(user);
        // 插入用户信息到数据库
        userMapper.insert(user);
        // 注册成功，返回成功信息
        return Result.success("注册成功！");
    }
}

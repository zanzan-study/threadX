package com.threadx.metrics.server.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.threadx.metrics.server.common.code.LoginExceptionCode;
import com.threadx.metrics.server.common.context.LoginContext;
import com.threadx.metrics.server.common.exceptions.LoginException;
import com.threadx.metrics.server.common.utils.ThreadxJwtUtil;
import com.threadx.metrics.server.constant.RedisCacheKey;
import com.threadx.metrics.server.constant.UserConstant;
import com.threadx.metrics.server.dto.UserLoginDto;
import com.threadx.metrics.server.entity.User;
import com.threadx.metrics.server.mapper.UserMapper;
import com.threadx.metrics.server.service.MenuService;
import com.threadx.metrics.server.service.PermissionService;
import com.threadx.metrics.server.service.UserService;
import com.threadx.metrics.server.vo.UserVo;
import org.springframework.aop.framework.AopContext;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 用户实现
 *
 * @author huangfukexing
 * @date 2023/6/1 08:13
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final StringRedisTemplate redisTemplate;
    private final MenuService menuService;
    private final PermissionService permissionService;

    public UserServiceImpl(StringRedisTemplate redisTemplate, MenuService menuService, PermissionService permissionService) {
        this.redisTemplate = redisTemplate;
        this.menuService = menuService;
        this.permissionService = permissionService;
    }

    @Override
    public String login(UserLoginDto userLoginDto) {
        //根据用户名查询用户信息
        User user = ((UserServiceImpl) AopContext.currentProxy()).findByUserName(userLoginDto.getUserName());
        if (user == null) {
            throw new LoginException(LoginExceptionCode.USER_NAME_OR_PASSWORD_ERROR);
        }
        String state = user.getState();
        if (UserConstant.DISABLE.equals(state)) {
            throw new LoginException(LoginExceptionCode.USER_IS_FROZEN);
        }

        if (BCrypt.checkpw(userLoginDto.getPassword(), user.getPassword())) {
            //生成token
            UserVo userVo = new UserVo();
            BeanUtil.copyProperties(user, userVo);
            LoginContext.setUserData(userVo);
            Long id = user.getId();
            String tokenKey = String.format("%s%s", IdUtil.fastSimpleUUID(), id);
            String cacheKey = String.format(RedisCacheKey.USER_TOKEN_CACHE, id, tokenKey);
            //返回生成的token
            String token = ThreadxJwtUtil.generateToken(userVo);
            //查询菜单信息
            menuService.findThisUserMenu();
            //查询权限信息
            permissionService.findThisUserPermission();
            //缓存令牌
            redisTemplate.opsForValue().set(cacheKey, token, 1, TimeUnit.HOURS);
            //设置数据
            LoginContext.setUserData(userVo);
            return tokenKey;

        } else {
            throw new LoginException(LoginExceptionCode.USER_NAME_OR_PASSWORD_ERROR);
        }
    }

    @Override
    public User findByUserName(String userName) {
        if (StrUtil.isBlank(userName)) {
            return null;
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_name", userName);
        return baseMapper.selectOne(queryWrapper);
    }

    @Override
    @SuppressWarnings("all")
    public void logout() {
        UserVo userData = LoginContext.getUserData();
        if (userData != null) {
            Set<String> keys = redisTemplate.keys(String.format(RedisCacheKey.USER_CACHE, userData.getId()) + "*");
            if (CollUtil.isNotEmpty(keys)) {
                keys.forEach(redisTemplate::delete);
            }
        }


    }

}

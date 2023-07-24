package com.threadx.metrics.server.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.threadx.metrics.server.common.code.LoginExceptionCode;
import com.threadx.metrics.server.common.context.LoginContext;
import com.threadx.metrics.server.common.exceptions.LoginException;
import com.threadx.metrics.server.constant.RedisCacheKey;
import com.threadx.metrics.server.entity.Menu;
import com.threadx.metrics.server.mapper.MenuMapper;
import com.threadx.metrics.server.service.MenuService;
import com.threadx.metrics.server.service.RoleMenuService;
import com.threadx.metrics.server.dto.UserDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 菜单实现类
 *
 * @author huangfukexing
 * @date 2023/6/1 14:36
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class MenuServiceImpl extends ServiceImpl<MenuMapper, Menu> implements MenuService {

    private final StringRedisTemplate redisTemplate;
    private final RoleMenuService roleMenuService;

    public MenuServiceImpl(StringRedisTemplate redisTemplate, RoleMenuService roleMenuService) {
        this.redisTemplate = redisTemplate;
        this.roleMenuService = roleMenuService;
    }

    @Override
    public List<Menu> findThisUserMenu() {
        UserDto userData = LoginContext.getUserData();
        if(userData == null) {
            throw new LoginException(LoginExceptionCode.USER_NOT_LOGIN_ERROR);
        }
        Long userId = userData.getId();
        //先查询缓存
        String menuCacheKey = String.format(RedisCacheKey.USER_MENU_CACHE, userId);
        String menuDataStr = redisTemplate.opsForValue().get(menuCacheKey);

        List<Menu> menus = new ArrayList<>();
        if (StrUtil.isNotBlank(menuDataStr)) {
            menus = JSONUtil.toList(menuDataStr, Menu.class);
        } else {
            Set<Long> menuIds = roleMenuService.findByRoleIds(userData.getRoleIds());
            if (CollUtil.isNotEmpty(menuIds)) {
                QueryWrapper<Menu> queryWrapper = new QueryWrapper<>();
                queryWrapper.in("id", menuIds);
                menus = baseMapper.selectList(queryWrapper);
                Comparator<Menu> comparator = Comparator.comparingInt(Menu::getSort);
                menus.sort(comparator);

            }
        }
        if (CollUtil.isNotEmpty(menus)) {

            redisTemplate.opsForValue().set(menuCacheKey, JSONUtil.toJsonStr(menus), 1, TimeUnit.HOURS);
        }

        return menus;
    }

    @Override
    public List<Menu> findAllMenu() {
        List<Menu> menus;
        if (redisTemplate.hasKey(RedisCacheKey.MENU_ALL_CACHE)) {
            //先查询缓存
            String menuAll = redisTemplate.opsForValue().get(RedisCacheKey.MENU_ALL_CACHE);
            //格式化数据
            menus = JSONUtil.toList(menuAll, Menu.class);
            //数据续约
            redisTemplate.expire(RedisCacheKey.MENU_ALL_CACHE, 1, TimeUnit.DAYS);
        }else {
            //查询所有的菜单数据
            menus = baseMapper.selectList(new QueryWrapper<>());
            redisTemplate.opsForValue().set(RedisCacheKey.MENU_ALL_CACHE, JSONUtil.toJsonStr(menus), 1, TimeUnit.DAYS);

        }
        //数据续期
        return menus;
    }
}

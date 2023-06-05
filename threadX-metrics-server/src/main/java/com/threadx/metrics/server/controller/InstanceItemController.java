package com.threadx.metrics.server.controller;

import com.threadx.metrics.server.common.annotations.GlobalResultPackage;
import com.threadx.metrics.server.common.annotations.Log;
import com.threadx.metrics.server.common.annotations.Login;
import com.threadx.metrics.server.common.annotations.UserPermission;
import com.threadx.metrics.server.conditions.InstanceItemDataConditions;
import com.threadx.metrics.server.conditions.InstanceItemFindConditions;
import com.threadx.metrics.server.entity.InstanceItem;
import com.threadx.metrics.server.enums.LogEnum;
import com.threadx.metrics.server.service.InstanceItemService;
import com.threadx.metrics.server.vo.InstanceItemDataVo;
import com.threadx.metrics.server.vo.InstanceItemVo;
import com.threadx.metrics.server.vo.ThreadxPage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 实例接口
 *
 * @author huangfukexing
 * @date 2023/5/30 15:23
 */
@RestController
@GlobalResultPackage
@Api(tags = "实例信息")
@RequestMapping("/instanceItem")
public class InstanceItemController {

    private final InstanceItemService instanceItemService;

    public InstanceItemController(InstanceItemService instanceItemService) {
        this.instanceItemService = instanceItemService;
    }

    @ApiOperation(value = "分页查询实例数据")
    @PostMapping("findByPage")
    public ThreadxPage<InstanceItemVo> findByPage(@RequestBody InstanceItemFindConditions conditions) {
        return instanceItemService.findByPage(conditions);
    }

    @Login
    @ApiOperation(value = "最近7天常用实例top10")
    @GetMapping("commonlyUsedTop10")
    public List<InstanceItemVo> commonlyUsedTop10() {
        return instanceItemService.commonlyUsedTop10();
    }

    @Login
    @Log(LogEnum.QUERY_INSTANCE_DESC)
    @ApiOperation(value = "实例详情查询")
    @PostMapping("instanceDetails")
    public InstanceItemDataVo instanceDetails(@RequestBody InstanceItemDataConditions instanceId){
        return instanceItemService.instanceDetails(instanceId);
    }
}

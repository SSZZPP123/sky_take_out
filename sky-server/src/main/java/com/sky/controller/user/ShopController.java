package com.sky.controller.user;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

/**
 * @ClassName ShopController
 * @Desccription
 * @Author SongZiPeng
 * @Date 2023-10-23 20:25
 **/

@RestController("userShopController")
@RequestMapping("/user/shop")
@Api("店铺相关接口")
@Slf4j
public class ShopController {

    @Autowired
    private RedisTemplate redisTemplate;

    @GetMapping("/status")
    @ApiOperation("获取店铺营业状态")
    public Result<Integer> getStatus(){
        Integer shop_status = (Integer) redisTemplate.opsForValue().get("SHOP_STATUS");
        log.info("设置店铺状态为：{}", shop_status == 1 ? "营业中" : "打烊");
        return Result.success(shop_status);
    }

}

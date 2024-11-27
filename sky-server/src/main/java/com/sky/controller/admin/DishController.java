package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("admin/dish")
@Slf4j
@Api(tags = "菜品相关接口")
public class DishController {
    @Autowired
    private DishService dishService;
    @Autowired
    private RedisTemplate redisTemplate;
    @PostMapping
    @ApiOperation("添加菜品")
    public Result save(@RequestBody DishDTO dishDTO){
        log.info("新增菜品,{}",dishDTO);
        dishService.save(dishDTO);
//        String key = "dish_" + dishDTO.getCategoryId();
//        redisTemplate.delete(key);
        return Result.success();
    }
    @GetMapping("/page")
    @ApiOperation("菜品分页查询")
    public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO){
        PageResult pageResult = dishService.page(dishPageQueryDTO);
        return Result.success(pageResult);
    }
    @DeleteMapping
    @ApiOperation("删除/批量删除菜品")
    public Result delete(@RequestParam List<Long> ids){
        dishService.delete(ids);
        cleanCache("dish_*");
        return Result.success();
    }
    @ApiOperation("根据id查询菜品")
    @GetMapping("{id}")
    public Result<DishVO> getByIdWithFlavor(@PathVariable Long id){
        log.info("根据id查询菜品，{}",id);
        DishVO dishVO = dishService.getByIdWithFlavor(id);
        return Result.success(dishVO);
    }
    @PutMapping
    @ApiOperation("更新菜品")
    public Result update(@RequestBody DishDTO dishDTO){
        log.info("更新菜品，{}",dishDTO);
        dishService.update(dishDTO);
        cleanCache("dish_*");
        return Result.success();
    }
    @PostMapping("status/{status}")
    @ApiOperation("设置菜品起售或停售")
    public Result starOrStop (@PathVariable Integer status , Long id){
        log.info("菜品起售或停售");
        dishService.update(status,id);
        cleanCache("dish_*");
        return Result.success();
    }
    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<Dish>> getByCategoryId(Long categoryId){
        List<Dish> dishes = dishService.getByCategoryId(categoryId);
        return Result.success(dishes);
    }
    private void cleanCache(String key){
        Set keys = redisTemplate.keys(key);
        redisTemplate.delete(keys);
    }
}

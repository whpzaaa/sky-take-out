package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("admin/dish")
@Slf4j
@Api("菜品相关接口")
public class DishController {
    @Autowired
    private DishService dishService;
    @PostMapping
    @ApiOperation("添加菜品")
    public Result save(@RequestBody DishDTO dishDTO){
        log.info("新增菜品,{}",dishDTO);
        dishService.save(dishDTO);
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
    public Result update(@RequestBody DishDTO dishDTO){
        log.info("更新菜品，{}",dishDTO);
        dishService.update(dishDTO);
        return Result.success();
    }
}

package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.annotation.AutoFill;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class DishServiceImpl implements DishService {
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    //事务注解 保证多表操作一致性 既要插入菜品表又要插入口味表
    @Transactional
    public void save(DishDTO dishDTO) {
        //dish表中需要传入dish对象 dto对象中的口味集合要传入口味表中
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        //将菜品传入dish表中
        dishMapper.insert(dish);
//        //根据名字获取传入的菜品id
//        Long id = dishMapper.selectByName(dish.getName());
        Long id = dish.getId();
        log.info("当前菜品id为{}",id);
        //获取口味集合
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && flavors.size()!= 0) {
            //遍历集合 并将id赋给集合中所有的flavor的dish_id
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(id);
            });
            //将口味集合批量插入到表中
            dishFlavorMapper.save(flavors);
        }
    }

    @Override
    public PageResult page(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(),dishPageQueryDTO.getPageSize());
        Page<DishVO> page = dishMapper.page(dishPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }
}

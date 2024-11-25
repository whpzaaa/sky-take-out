package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.annotation.AutoFill;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class DishServiceImpl implements DishService {
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
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

    @Override
    public void delete(List<Long> ids) {
        //一次可以删一个也可以删多个
        //起售中的菜品不能删
        for (Long id : ids) {
            Dish dish = dishMapper.getById(id);
            if (dish.getStatus() == StatusConstant.ENABLE){
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }
        //被套餐setmeal关联的菜品不能删
        List<Long> list = setmealDishMapper.getSetmealIdByDishIds(ids);
        if (list != null && list.size() > 0 ){
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }
        dishMapper.delete(ids);
        //菜品删除后 相关联的口味flavor也要删掉
        dishFlavorMapper.deleteByDishIds(ids);
    }

    @Override
    public DishVO getByIdWithFlavor(Long id) {
        DishVO dishVO = new DishVO();
        //根据id查询菜品
        Dish dish = dishMapper.getById(id);
        BeanUtils.copyProperties(dish,dishVO);
        //根据dishId查询口味
        List<DishFlavor> dishFlavorList = dishFlavorMapper.getByDishId(id);
        dishVO.setFlavors(dishFlavorList);
        return dishVO;
    }
    //更新菜品和口味需绑定
    @Transactional
    @Override
    public void update(DishDTO dishDTO) {
        Dish dish = new Dish();
        //更新菜品表
        BeanUtils.copyProperties(dishDTO,dish);
        dishMapper.update(dish);
        //更新口味表
        //先删除原有的口味
        dishFlavorMapper.deleteByDishId(dishDTO.getId());
        //再插入传来的口味
        //先根据dishId获取口味集合
        List<DishFlavor> flavorList = dishDTO.getFlavors();
        //如果口味集合不为空 则将dish的id赋给集合中每个dishflavor的dishid
        if (flavorList != null && flavorList.size() > 0) {
            flavorList.forEach(dishFlavor -> {
                dishFlavor.setDishId(dishDTO.getId());
            });
            //再将口味集合插入到口味表中
            dishFlavorMapper.save(flavorList);
        }

    }
    /**
     * 条件查询菜品和口味
     * @param dish
     * @return
     */
    public List<DishVO> listWithFlavor(Dish dish) {
        List<Dish> dishList = dishMapper.list(dish);

        List<DishVO> dishVOList = new ArrayList<>();

        for (Dish d : dishList) {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(d,dishVO);

            //根据菜品id查询对应的口味
            List<DishFlavor> flavors = dishFlavorMapper.getByDishId(d.getId());

            dishVO.setFlavors(flavors);
            dishVOList.add(dishVO);
        }

        return dishVOList;
    }

}

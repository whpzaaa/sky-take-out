package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.enumeration.OperationType;
import com.sky.result.PageResult;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface EmployeeMapper {

    /**
     * 根据用户名查询员工
     * @param username
     * @return
     */
    @Select("select * from sky_take_out.employee where username = #{username}")
    Employee getByUsername(String username);
    @Insert(("insert into  sky_take_out.employee(name, username, password, phone, sex, id_number, create_time, update_time, create_user, update_user,status) " +
            "values " +
            "(#{name},#{username},#{password},#{phone},#{sex},#{idNumber},#{createTime},#{updateTime},#{createUser},#{updateUser},#{status})"))
    //设置autofill注解为公共字段自动填充
    @AutoFill(value = OperationType.INSERT)
    void save(Employee employee);

    Page<Employee> page(EmployeePageQueryDTO employeePageQueryDTO);

    //设置autofill注解为公共字段自动填充
    @AutoFill(value = OperationType.UPDATE)
    void update(Employee e);
    @Select("select * from sky_take_out.employee where id = #{id}")
    Employee getById(Long id);
}

package com.health.healthplatform.mapper;

import com.health.healthplatform.entity.Category;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface CategoryMapper {

    @Select("SELECT * FROM categories")
    List<Category> selectAll();

    @Select("SELECT * FROM categories WHERE id = #{id}")
    Category selectById(Integer id);

    @Insert("INSERT INTO categories(name, icon, description) VALUES(#{name}, #{icon}, #{description})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Category category);

    @Update("UPDATE categories SET name=#{name}, icon=#{icon}, description=#{description} WHERE id=#{id}")
    void update(Category category);

    @Delete("DELETE FROM categories WHERE id=#{id}")
    void delete(Integer id);
}
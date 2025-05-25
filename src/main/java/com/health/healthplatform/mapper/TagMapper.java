package com.health.healthplatform.mapper;

import com.health.healthplatform.entity.Tag;
import org.apache.ibatis.annotations.*;
import org.springframework.transaction.annotation.Transactional;

@Mapper
public interface TagMapper {
    @Insert("INSERT INTO tags(name) VALUES(#{name})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Tag tag);

    @Select("SELECT * FROM tags WHERE name = #{name}")
    Tag selectByName(String name);

    @Select("SELECT id FROM tags WHERE name = #{name}")
    Long findTagId(String name);

    @Insert("INSERT INTO tags(name) "+
            "SELECT #{name} FROM DUAL "+
            "WHERE NOT EXISTS (SELECT 1 FROM tags WHERE name = #{name})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertIfNotExists(Tag tag);

    @Transactional
    default Long getOrCreateTag(String name) {
        Long tagId = findTagId(name);
        if (tagId == null) {
            Tag tag = new Tag();
            tag.setName(name);
            insertIfNotExists(tag);
            tagId = tag.getId();
        }
        return tagId;
    }
}
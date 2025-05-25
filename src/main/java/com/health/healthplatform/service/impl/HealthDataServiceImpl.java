//package com.health.healthplatform.service.impl;
//
//import com.health.healthplatform.DTO.HealthDataDTO;
//import com.health.healthplatform.entity.healthdata.HealthData;
//import com.health.healthplatform.mapper.health_data.HealthDataMapper;
//import com.health.healthplatform.service.health_data.HealthDataService;
//import com.health.healthplatform.vo.HealthDataVO;
//import lombok.extern.slf4j.Slf4j;  // 添加这行
//import org.springframework.beans.BeanUtils;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDate;
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Slf4j
//@Service
//public class HealthDataServiceImpl implements HealthDataService {
//
//    @Autowired
//    private HealthDataMapper healthDataMapper;
//
//    @Override
//    public HealthDataVO getLatestHealthData(Long userId) {
//        log.info("获取用户最新健康数据, userId={}", userId);
//        HealthData healthData = healthDataMapper.selectLatestByUserId(userId);
//        return this.convertToVO(healthData);  // 修改这里，添加 this
//    }
//
//    @Override
//    @Transactional
//    public void addHealthData(Long userId, HealthDataDTO dto) {
//        log.info("添加健康数据, userId={}, dto={}", userId, dto);
//        HealthData healthData = new HealthData();
//        BeanUtils.copyProperties(dto, healthData);
//        healthData.setUserId(userId);
//        healthData.setRecordDate(LocalDate.now());  // 设置记录日期为当前日期
//        healthDataMapper.insert(healthData);
//    }
//
//    @Override
//    public List<HealthDataVO> getHealthDataHistory(Long userId, LocalDate startDate, LocalDate endDate) {
//        log.info("获取健康数据历史记录, userId={}, startDate={}, endDate={}", userId, startDate, endDate);
//        List<HealthData> healthDataList = healthDataMapper.selectByUserIdAndDateRange(userId, startDate, endDate);
//        log.info("查询到的数据条数: {}", healthDataList.size());
//        return healthDataList.stream()
//                .map(this::convertToVO)  // 使用方法引用
//                .collect(Collectors.toList());
//    }
//
//    private HealthDataVO convertToVO(HealthData healthData) {
//        if (healthData == null) {
//            return null;
//        }
//        HealthDataVO vo = new HealthDataVO();
//        BeanUtils.copyProperties(healthData, vo);
//        return vo;
//    }
//}
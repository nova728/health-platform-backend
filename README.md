# TODO

  1. 登录忘记密码功能  :white_check_mark:
  2. 健康成就
  3. 当健康数据为空时healthdata界面的显示（前端）
  4. 百度地图api


# 问题

- 论坛模块
- 浏览一次 浏览量+2

todo
微信通知接入 ok
健康分析、运动建议、营养建议和睡眠分析结果，每一周向确认接受通知的用户发送一次阿里云短信
SmsService 31行需要创建阿里云短信模板，类似：亲爱的用户，您的健康周报：${report}

饮食模块 ok
前端使用示例：
Map<String, String> request = new HashMap<>();
request.put("imagePath", "/path/to/food_image.jpg");

ResponseEntity<FoodRecognitionResponseDTO> response = restTemplate.postForEntity(
    "http://your-server/api/foods/recognize",
    request,
    FoodRecognitionResponseDTO.class
);

if (response.getStatusCode() == HttpStatus.OK) {
    FoodRecognitionResponseDTO result = response.getBody();
    // 处理识别结果
} else {
    // 处理错误
}



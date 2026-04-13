package com.plantadoption.common;

import lombok.Getter;

/**
 * 错误码枚举
 */
@Getter
public enum ErrorCode {
    
    SUCCESS(200, "操作成功"),
    
    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未授权，请先登录"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不允许"),
    
    SYSTEM_ERROR(500, "系统内部错误"),
    SERVICE_UNAVAILABLE(503, "服务暂不可用"),
    
    USER_NOT_FOUND(1001, "用户不存在"),
    USER_DISABLED(1002, "用户已被禁用"),
    PHONE_EXISTS(1003, "手机号已注册"),
    PHONE_NOT_EXISTS(1004, "手机号未注册"),
    VERIFICATION_CODE_ERROR(1005, "验证码错误"),
    VERIFICATION_CODE_EXPIRED(1006, "验证码已过期"),
    PASSWORD_ERROR(1007, "密码错误"),
    TOKEN_INVALID(1008, "Token无效"),
    TOKEN_EXPIRED(1009, "Token已过期"),
    
    PLANT_NOT_FOUND(2001, "绿植不存在"),
    PLANT_NOT_AVAILABLE(2002, "绿植不可领养"),
    PLANT_ALREADY_ADOPTED(2003, "绿植已被领养"),
    
    ADOPTION_LIMIT_EXCEEDED(3001, "领养数量已达上限"),
    ADOPTION_RECORD_NOT_FOUND(3002, "领养记录不存在"),
    ADOPTION_ALREADY_EXISTS(3003, "已存在待审批的领养申请"),
    ADOPTION_STATUS_ERROR(3004, "领养状态错误"),
    
    CARE_LOG_NOT_FOUND(4001, "养护记录不存在"),
    
    POST_NOT_FOUND(5001, "帖子不存在"),
    COMMENT_NOT_FOUND(5002, "评论不存在"),
    ALREADY_LIKED(5003, "已点赞"),
    NOT_LIKED(5004, "未点赞"),
    
    FILE_UPLOAD_ERROR(6001, "文件上传失败"),
    FILE_TYPE_ERROR(6002, "文件类型不支持"),
    FILE_SIZE_EXCEEDED(6003, "文件大小超出限制"),
    
    DEPARTMENT_NOT_FOUND(7001, "部门不存在"),
    CONFIG_NOT_FOUND(7002, "配置项不存在");
    
    private final Integer code;
    private final String message;
    
    ErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}

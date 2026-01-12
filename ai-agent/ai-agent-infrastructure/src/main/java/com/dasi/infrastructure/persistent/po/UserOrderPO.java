package com.dasi.infrastructure.persistent.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserOrderPO {

    /** 自增ID */
    private Long id;
    /** 用户姓名 */
    private String userName;
    /** 用户编号 */
    private String userId;
    /** 用户电话 */
    private String userMobile;
    /** 商品编号 */
    private String sku;
    /** 商品名称 */
    private String skuName;
    /** 订单ID */
    private String orderId;
    /** 商品数量 */
    private int quantity;
    /** 商品价格 */
    private BigDecimal unitPrice;
    /** 折扣金额 */
    private BigDecimal discountAmount;
    /** 费率金额 */
    private BigDecimal tax;
    /** 支付金额 */
    private BigDecimal totalAmount;
    /** 订单日期 */
    private Date orderDate;
    /** 订单状态 */
    private int orderStatus;
    /** 逻辑删单 */
    private int isDelete;
    /** 唯一索引 */
    private String uuid;
    /** 设备地址 */
    private String ipv4;
    /** 设备地址 */
    private byte[] ipv6;
    /** 扩展数据 */
    private String extData;
    /** 更新时间 */
    private Date updateTime;
    /** 创建时间 */
    private Date createTime;

}

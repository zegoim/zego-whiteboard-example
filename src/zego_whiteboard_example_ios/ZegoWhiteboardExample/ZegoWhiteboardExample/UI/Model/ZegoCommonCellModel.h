//
//  ZegoCommonCellModel.h
//  ZegoWhiteboardExample
//
//  Created by Xuyang Nie on 2020/11/26.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN
//白板操作工具枚举
typedef NS_ENUM(NSUInteger, ZegoDrawingToolViewItemType) {
    ZegoDrawingToolViewItemTypePath         = 0x1,      // 涂鸦画笔
    ZegoDrawingToolViewItemTypeText         = 0x2,      // 文本
    ZegoDrawingToolViewItemTypeLine         = 0x4,      // 直线
    ZegoDrawingToolViewItemTypeRect         = 0x8,      // 矩形
    ZegoDrawingToolViewItemTypeEllipse      = 0x10,     // 圆
    ZegoDrawingToolViewItemTypeArrow        = 0x20,     // 选择箭头
    ZegoDrawingToolViewItemTypeEraser       = 0x40,     // 橡皮擦
    ZegoDrawingToolViewItemTypeFormat       = 0x81,     // 颜色
    ZegoDrawingToolViewItemTypeUndo         = 0x110,    // 撤销
    ZegoDrawingToolViewItemTypeRedo         = 0x200,    // 恢复
    ZegoDrawingToolViewItemTypeClear        = 0x400,    // 清空
    ZegoDrawingToolViewItemTypeDrag         = 0x600,    // 拖拽箭头
    ZegoDrawingToolViewItemTypeLaser        = 0x80,     // 激光笔
    ZegoDrawingToolViewItemTypeDynamicClick = 0x100,    // 动态 PPT 点击
    
    ZegoDrawingToolViewItemTypeJustTest     = ~0,       // 仅供内部测试
};

//操作事件标记枚举，用于将列表视图中的事件对应 SDK 中具体的方法
typedef NS_ENUM(NSUInteger, ZegoOperationEventFlagType) {
    ZegoOperationEventFlagTypeOperationMode                 = 10001,
    ZegoOperationEventFlagTypeFontType                       = 10002,
    ZegoOperationEventFlagTypeToolType                       = 10003,
    ZegoOperationEventFlagTypeDrawColor                      = 10004,
    ZegoOperationEventFlagTypeFontSize                       = 10005,
    ZegoOperationEventFlagTypeLineWidth                      = 10006,
    ZegoOperationEventFlagTypeClearModel                     = 10007,
    ZegoOperationEventFlagTypeSpecialTextGraphic             = 10008,
    ZegoOperationEventFlagTypeDefaultText                    = 10009,
    ZegoOperationEventFlagTypeUploadGraphic                  = 10010,
    ZegoOperationEventFlagTypeSelectedGraphic                = 10011,
    ZegoOperationEventFlagTypeUploadPicByURL               = 10012,
    ZegoOperationEventFlagTypeUploadPicByAlbum             = 10013,
    ZegoOperationEventFlagTypeUploadLog                    = 10014,
    
    ZegoOperationEventFlagTypeWhiteboardAdd                  = 20001,
    ZegoOperationEventFlagTypeWhiteboardClearCache           = 20002,
    
    ZegoOperationEventFlagTypeDocsUpload                     = 30001,
    ZegoOperationEventFlagTypeDocsCacheFile                  = 30003,
    ZegoOperationEventFlagTypeDocsQueryCache                 = 30004,
    ZegoOperationEventFlagTypePreview                        = 30005,
    ZegoOperationEventFlagTypeStepAutoPaging                 = 30006,
};

/*
 通用列表cell模型, 目前适用5中cell 类型
 Switch             -- >   title              switch
 Text               -- >   title    text      btn
 Picker             -- >   title    picker
 FunctionPannel     -- >   title    btn       btn
 Combination        -- >   title
                            option 口  option 口
 */

typedef enum : NSUInteger {
    ZegoSettingTableViewCellTypeSwitch = 1,
    ZegoSettingTableViewCellTypeText,
    ZegoSettingTableViewCellTypePicker,
    ZegoSettingTableViewCellTypeFunctionPannel,
} ZegoSettingTableViewCellType;


// 操作列表视图中单元cell 的数据模型，用于使用标准模板视图文件，构建出操作列表。
// 实现流程
// 操作承载视图               从bundle 中读取 操作列表视图结构     实例化操作列表
//     |                           |         |                   |
// ZegoFunctionPannelView --> bundle --> .plist文件 --> ZegoOperationPannelView
//                                                                  |
//                                                                  |
// ZegoBoardOperationManager <-- ZegoOperationPannelEventHandler <---
//                                      |
//                                   列表事件处理
@class  ZegoCommonCellModel;
@protocol ZegoSettingTableViewCellDelegate <NSObject>

- (void)onSettingCellValueChange:(ZegoCommonCellModel *)valueChangeModel;

@end
@interface ZegoCellOptionModel : NSObject
@property (nonatomic, copy) NSString *title;            //子功能名称
@property (nonatomic, strong) id value;                 //子功能选择值，
@property (nonatomic, copy) NSString *defaultValue;     //子功能选择初始值
@end

@interface ZegoCommonCellModel : NSObject
@property (nonatomic, copy) NSString *title;                                //功能栏名称
@property (nonatomic, assign) ZegoSettingTableViewCellType type;            //功能栏类型
@property (nonatomic, strong) NSArray <ZegoCellOptionModel *> *options;     //功能选择候选值（pickerModel下）或子功能信息（textModel下）
@property (nonatomic, strong) id value;                                     //功能选择结果


@property (nonatomic, copy) NSString *tag;                                  //自定义tag
@property (nonatomic, copy) NSString *placeholder;                          //占位text（textModel下）
@property (nonatomic, strong) NSDictionary *extra;                          //自定义拓展字段
@property (nonatomic, assign) CGFloat cellHeight;                           //cell行高
@property (nonatomic, assign) ZegoOperationEventFlagType eventNumber;       //事件ID



- (instancetype)initWithTitle:(NSString *)title type:(ZegoSettingTableViewCellType)type options:(nullable NSArray *)options value:(nullable id)value;

@end

NS_ASSUME_NONNULL_END

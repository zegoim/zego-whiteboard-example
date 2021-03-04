//
//  ZegoFilePreviewViewModel.h
//  ZegoWhiteboardVideoDemo
//
//  Created by MartinNie on 2020/8/21.
//  Copyright © 2020 zego. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
@class ZegoFilePreviewView;
NS_ASSUME_NONNULL_BEGIN
//此对象是用来控制预览视图与外部联动的逻辑中间层
@interface ZegoFilePreviewManager : NSObject
@property (nonatomic, assign, readonly) NSInteger currentPage;//当前预览选择页
@property (nonatomic, assign, readonly) BOOL isShow;  //是否正在显示

@property (nonatomic, copy) void(^selectedPageBlock)(NSInteger index);//index 从0 开始

+ (instancetype)shareManager;
//设置父视图和预览数据源
- (void)setupPreviewData:(NSArray *)dataArray;
//清除预览视图和数据以及状态
- (void)reset;

//设置预览视图 当前定位的页码
- (void)setCurrentPageCount:(NSInteger)pageCount;

//显示预览视图，并定位到指定页码。
- (void)showPreviewWithPage:(NSInteger)pageCount;

//隐藏预览视图
- (void)hiddenPreview;
@end

NS_ASSUME_NONNULL_END

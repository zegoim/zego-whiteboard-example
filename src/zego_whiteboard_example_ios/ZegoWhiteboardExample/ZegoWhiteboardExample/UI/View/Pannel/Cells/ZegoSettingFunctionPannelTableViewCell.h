//
//  ZegoSettingFunctionPannelTableViewCell.h
//  ZegoWhiteboardExample
//
//  Created by Xuyang Nie on 2020/11/26.
//

#import <UIKit/UIKit.h>
#import "ZegoCommonCellModel.h"
NS_ASSUME_NONNULL_BEGIN
#define ZegoSettingFunctionPannelTableViewCellID @"ZegoSettingFunctionPannelTableViewCellID"

@interface ZegoSettingFunctionPannelTableViewCell : UITableViewCell
@property (nonatomic, weak) id<ZegoSettingTableViewCellDelegate> delegate;

@property (nonatomic, strong) ZegoCommonCellModel *model;


+ (CGFloat)getFunctionPannelCellHeight;
@end

NS_ASSUME_NONNULL_END

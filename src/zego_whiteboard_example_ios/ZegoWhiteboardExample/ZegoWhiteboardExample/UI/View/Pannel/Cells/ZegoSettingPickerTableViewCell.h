//
//  ZegoSettingPickerTableViewCell.h
//  ZegoWhiteboardExample
//
//  Created by Xuyang Nie on 2020/11/26.
//

#import <UIKit/UIKit.h>
#import "ZegoCommonCellModel.h"
NS_ASSUME_NONNULL_BEGIN
#define ZegoSettingPickerTableViewCellID @"ZegoSettingPickerTableViewCellID"

@interface ZegoSettingPickerTableViewCell : UITableViewCell
@property (nonatomic, weak) id<ZegoSettingTableViewCellDelegate> delegate;

@property (nonatomic, strong) ZegoCommonCellModel *model;

+ (CGFloat)getPickerCellHeight;
@end

NS_ASSUME_NONNULL_END

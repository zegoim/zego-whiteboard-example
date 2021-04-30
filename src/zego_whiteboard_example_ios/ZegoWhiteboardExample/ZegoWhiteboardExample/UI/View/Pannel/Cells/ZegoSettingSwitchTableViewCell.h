//
//  ZegoSettingSwitchTableViewCell.h
//  ZegoWhiteboardExample
//
//  Created by Xuyang Nie on 2020/11/26.
//

#import <UIKit/UIKit.h>
#import "ZegoCommonCellModel.h"
NS_ASSUME_NONNULL_BEGIN
#define ZegoSettingSwitchTableViewCellID @"ZegoSettingSwitchTableViewCellID"
@interface ZegoSettingSwitchTableViewCell : UITableViewCell

@property (nonatomic, weak) id<ZegoSettingTableViewCellDelegate> delegate;

@property (nonatomic, strong) ZegoCommonCellModel *model;

+ (CGFloat)getSwitchCellHeightWithModel:(ZegoCommonCellModel *)model;


@end

@interface ZegoSettingSwitchView : UIView
@property (nonatomic, strong) UILabel *titleLabel;
@property (nonatomic, strong) UISwitch *stateSwitch;
@property (nonatomic, strong) void(^didClickSwitchBlock)(BOOL state);
@end
NS_ASSUME_NONNULL_END

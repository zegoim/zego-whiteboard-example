//
//  ZegoSettingTextTableViewCell.h
//  ZegoWhiteboardExample
//
//  Created by Xuyang Nie on 2020/11/26.
//

#import <UIKit/UIKit.h>
#import "ZegoCommonCellModel.h"
NS_ASSUME_NONNULL_BEGIN
#define ZegoSettingTextTableViewCellID @"ZegoSettingTextTableViewCellID"

@interface ZegoSettingTextView : UIView
@property (nonatomic, strong) UITextField *inputTF;
@property (nonatomic, strong) UILabel *titleLabel;

@end

@interface ZegoSettingTextTableViewCell : UITableViewCell
@property (nonatomic, weak) id<ZegoSettingTableViewCellDelegate> delegate;

@property (nonatomic, strong) ZegoCommonCellModel *model;

+ (CGFloat)getTextCellHeightWithModel:(ZegoCommonCellModel *)model;
@end


NS_ASSUME_NONNULL_END

//
//  ZegoFileSelectTableViewCell.h
//  ZegoWhiteboardExample
//
//  Created by MartinNie on 2020/12/2.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface ZegoFileSelectTableViewCell : UITableViewCell
@property (nonatomic, strong) void(^didClickDeleteBlock)();
@property (nonatomic, copy) NSString *title;
@property (nonatomic, assign) BOOL selectedStyle;

@end

NS_ASSUME_NONNULL_END

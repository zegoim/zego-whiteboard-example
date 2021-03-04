//
//  ZegoOperationPannelEventHandler.h
//  ZegoWhiteboardExample
//
//  Created by MartinNie on 2020/12/1.
//

#import <Foundation/Foundation.h>
#import "ZegoCommonCellModel.h"
NS_ASSUME_NONNULL_BEGIN

@protocol ZegoOperationPannelEventHandlerDelegate <NSObject>

- (void)pictureSelect;

@end

@interface ZegoOperationPannelEventHandler : NSObject<ZegoSettingTableViewCellDelegate>

@property (nonatomic,weak)id<ZegoOperationPannelEventHandlerDelegate>delegate;

@end

NS_ASSUME_NONNULL_END

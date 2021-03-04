//
//  ZegoOperationPannelView.h
//  ZegoWhiteboardExample
//
//  Created by Xuyang Nie on 2020/11/27.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@protocol ZegoOperationPannelViewDelegate <NSObject>

- (void)chousePictureEvent;

@end

@interface ZegoOperationPannelView : UIView

- (instancetype)initWithFileName:(NSString *)fileName;

@property (nonatomic,weak)id<ZegoOperationPannelViewDelegate>delegate;

@end

NS_ASSUME_NONNULL_END

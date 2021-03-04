//
//  ZegoProgessHUD.h
//  ZegoWhiteboardExample
//
//  Created by MartinNie on 2020/12/5.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface ZegoProgessHUD : NSObject

- (instancetype)initWithTitle:(NSString *)title cancelBlock:(void(^)(void))cancelBlock;


- (void)updateProgress:(CGFloat)progress;
+ (void)showIndicatorHUDText:(NSString *)text;
+ (void)showTipMessage:(NSString *)message ;
+ (void)showTipMessageWithErrorCode:(int)error; //根据错误码弹提示
+ (void)dismiss;
@end

NS_ASSUME_NONNULL_END

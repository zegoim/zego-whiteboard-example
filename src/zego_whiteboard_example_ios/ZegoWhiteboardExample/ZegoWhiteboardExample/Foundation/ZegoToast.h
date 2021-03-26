//
//  ZegoToast.h
//  ZegoWhiteboardExample
//
//  Created by Vic on 2021/3/13.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface ZegoToast : NSObject

+ (void)toastWithMessage:(NSString *)msg;
+ (void)toastWithError:(int)error;

@end

NS_ASSUME_NONNULL_END

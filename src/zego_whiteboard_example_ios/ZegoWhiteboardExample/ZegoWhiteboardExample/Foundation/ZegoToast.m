//
//  ZegoToast.m
//  ZegoWhiteboardExample
//
//  Created by Vic on 2021/3/13.
//

#import "ZegoToast.h"
#import "Toast.h"

@implementation ZegoToast

+ (void)toastWithMessage:(NSString *)msg {
    UIWindow *window = [[UIApplication sharedApplication] keyWindow];
    [window makeToast:msg duration:2 position:[NSValue valueWithCGPoint:window.center] style:nil];
}

+ (void)toastWithError:(int)error {
    NSString *msg = [NSString stringWithFormat:@"错误码: %d", error];
    [self toastWithMessage:msg];
}

@end

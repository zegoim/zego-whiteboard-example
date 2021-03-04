//
//  ZGFormatValidator.m
//  ZegoWhiteboardViewDemo
//
//  Created by Vic on 2020/12/24.
//  Copyright © 2020 zego. All rights reserved.
//

#import "ZGFormatValidator.h"

@implementation ZGFormatValidator

+ (BOOL)validateString:(NSString *)string withRegex:(NSString *)regex {
    if (!string || ![string isKindOfClass:[NSString class]]) {
        return NO;
    }
    NSPredicate *predicate = [NSPredicate predicateWithFormat:@"SELF MATCHES %@", regex];
    BOOL ret = [predicate evaluateWithObject:string];
    return ret;
}

@end

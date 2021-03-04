//
//  NSString+FormatValidator.m
//  ZegoWhiteboardViewDemo
//
//  Created by Vic on 2020/12/24.
//  Copyright © 2020 zego. All rights reserved.
//

#import "NSString+FormatValidator.h"
#import "ZGFormatValidator.h"

@implementation NSString (FormatValidator)

- (BOOL)isURL {
    return [ZGFormatValidator validateString:self withRegex:ZG_URL_REGEX];
}

@end

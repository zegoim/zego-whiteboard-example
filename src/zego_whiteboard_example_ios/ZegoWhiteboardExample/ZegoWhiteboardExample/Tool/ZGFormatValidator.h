//
//  ZGFormatValidator.h
//  ZegoWhiteboardViewDemo
//
//  Created by Vic on 2020/12/24.
//  Copyright © 2020 zego. All rights reserved.
//

#import <Foundation/Foundation.h>

#define ZG_URL_REGEX @"(http|https)://((\\w)*|([0-9]*)|([-|_])*)+([\\.|/]((\\w)*|([0-9]*)|([-|_])*))+"

NS_ASSUME_NONNULL_BEGIN

@interface ZGFormatValidator : NSObject

+ (BOOL)validateString:(NSString *)string withRegex:(NSString *)regex;

@end

NS_ASSUME_NONNULL_END

//
//  NSString+ContentOperation.m
//  ZegoWhiteboardExample
//
//  Created by zego on 2021/5/13.
//

#import "NSString+ContentOperation.h"

@implementation NSString (ContentOperation)

- (NSInteger)getByteNum {
    NSInteger strlength = 0;
    char* p = (char*)[self cStringUsingEncoding:NSUTF8StringEncoding];
    for (NSInteger i=0 ; i < [self lengthOfBytesUsingEncoding:NSUTF8StringEncoding] ;i++) {
        if (*p) {
            p++;
            strlength++;
        }
        else {
            p++;
        }
    }
    return strlength;
}

- (NSString *)subStringByByteWithLength:(NSInteger)length {
    
    if ([self getByteNum] <= length) {
        return self;
    }
    NSString *subStr = self;
    NSString * resultStr = @"";
    if (self.length > 128) {
        subStr = [self substringToIndex:128];
    }
    for(int i = 0; i < [subStr length]; i++){
        resultStr = [subStr substringToIndex:[subStr length] - i];
        if ([resultStr getByteNum] <= length) {
            break;
        }

    }
    return resultStr;
}

@end

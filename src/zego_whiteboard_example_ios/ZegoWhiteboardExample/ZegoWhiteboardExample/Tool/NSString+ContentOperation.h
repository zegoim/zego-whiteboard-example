//
//  NSString+ContentOperation.h
//  ZegoWhiteboardExample
//
//  Created by zego on 2021/5/13.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface NSString (ContentOperation)

/// 获取字符串的字节数
- (NSInteger)getByteNum;

/// 字符串截取
/// @param length 长度
- (NSString *)subStringByByteWithLength:(NSInteger)length;

@end

NS_ASSUME_NONNULL_END

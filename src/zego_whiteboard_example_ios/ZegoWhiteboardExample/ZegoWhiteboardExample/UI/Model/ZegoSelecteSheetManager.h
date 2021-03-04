//
//  ZegoSelecteSheetManager.h
//  ZegoWhiteboardExample
//
//  Created by MartinNie on 2020/12/4.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface ZegoSelecteSheetManager : NSObject




+ (instancetype)shareManager;
- (void)showSheetWithTitle:(NSString *)title optionArray:(NSArray *)optionArray selectedBlock:(void(^)(NSInteger index))selectedBlock;
- (void)initAlertSheetWithOptionArray:(NSArray *)optionArray;
- (void)hiddenSheet;
@end

NS_ASSUME_NONNULL_END

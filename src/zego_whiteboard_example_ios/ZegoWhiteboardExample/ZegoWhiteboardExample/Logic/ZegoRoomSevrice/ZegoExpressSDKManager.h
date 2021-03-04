//
//  ZegoExpressSDKManager.h
//  ZegoWhiteboardExample
//
//  Created by Xuyang Nie on 2020/11/24.
//

#ifdef  ZegoRoomSeviceSDKFlagExpress

#import <Foundation/Foundation.h>
#import "ZegoRoomSeviceDelegate.h"
NS_ASSUME_NONNULL_BEGIN

@interface ZegoExpressSDKManager : NSObject<ZegoRoomSeviceDelegate>

@property (nonatomic, weak) id <ZegoRoomSeviceClientDelegate> delegate;

+ (instancetype)shareManager;
- (NSString *)getExpressVersion;
@end

NS_ASSUME_NONNULL_END
#endif

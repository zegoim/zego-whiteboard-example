//
//  ZegoLiveRoomSDKManager.h
//  ZegoWhiteboardExample
//
//  Created by Xuyang Nie on 2020/11/24.
//
#ifdef  ZegoRoomSeviceSDKFlagLiveRoom
#import <Foundation/Foundation.h>
#import "ZegoRoomSeviceDelegate.h"
NS_ASSUME_NONNULL_BEGIN

@interface ZegoLiveRoomSDKManager : NSObject<ZegoRoomSeviceDelegate>
@property (nonatomic, weak) id <ZegoRoomSeviceClientDelegate> delegate;

+ (instancetype)shareManager;
- (NSString *)getLiveRoomVersion;
@end

NS_ASSUME_NONNULL_END
#endif
